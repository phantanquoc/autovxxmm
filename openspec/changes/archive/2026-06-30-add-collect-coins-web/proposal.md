## Why

Today the "Gom xu" (collect-coins) flow only exists in the legacy Java desktop UI: operators must edit a local txt file to register collector accounts and press a local button to start a collect run. The web admin SPA cannot create collector bots, cannot configure thresholds, and has no visibility into collect runs. This forces every user to keep the Java jar open, blocks per-user threshold tuning, and prevents the per-user tenancy model from extending to collector bots. By moving the entire collect lifecycle to the backend + admin SPA we unify bot management (order + collector under one CRUD model), let each user own their own threshold/keep settings, and reduce the Java client to a pure executor that polls a backend task queue.

## What Changes

- **BREAKING** (Java client): the Java jar SHALL no longer load `botCollect` accounts from a local txt file and SHALL no longer expose a local "Gom xu" / "Import txt" button. Both UI controls and the file-import code path are removed.
- **BREAKING** (collect logic): the hardcoded VIP rule in `CollectScreen` (`coins - 10000000` for VIP, `coins` for normal) is replaced by a single user-owned `collectKeep` value pushed from the backend. Bots in VIP mode are also gommed down to `collectKeep` once the user confirms the run.
- Database: `Bot.role` (enum `BotRole { ORDER, COLLECT }`, default `ORDER`); `User.collectThreshold` (default `1_000_000`) and `User.collectKeep` (default `0`); new `CollectTask` model (status, snapshot threshold/keep, target list, total collected, timestamps); new `ClientSyncState.collectTasksAt`; index `Bot(ownerId, role, deletedAt)`.
- Backend API:
  - `GET /api/me/profile` extended with `collectThreshold` + `collectKeep`.
  - `PUT /api/me/profile` (new) — update threshold/keep with Zod validation (`threshold > keep`, both `>= 0`).
  - `GET/POST/PUT/DELETE /api/me/collect-bots/*` — CRUD over `Bot.role = COLLECT`, mirrors `/api/me/bots/*` but stamps `role=COLLECT` and rejects payloads that try to override it.
  - `POST /api/me/collect/scan` — preview only; returns the list of `role=ORDER` bots currently above threshold plus available collector count and current X/Y.
  - `POST /api/me/collect/tasks` — confirm and create a `CollectTask` with target snapshot.
  - `GET /api/me/collect/tasks` and `GET /api/me/collect/tasks/:id` — task list/detail for the history view.
  - `GET /api/client/bots/collect` — Java client fetches collector bots (role=COLLECT) for its own ownerId, mirroring the `/normal` shape including sync-delta endpoints.
  - `GET /api/client/collect/check-update`, `GET /api/client/collect/pending`, `POST /api/client/collect/ack` — Java client polls for pending CollectTasks and reports per-bot results; ack also writes one `TradeLog` row of type `2` (GOM XU) per successfully gommed bot.
- Admin SPA:
  - New route `/collect-bots` with sidebar entry "Bot gom xu" (USER role only). List + CRUD + "Gom xu" button that previews scan results in a shadcn `AlertDialog` and creates the task on confirm.
  - New "Lịch sử gom xu" panel showing recent `CollectTask` rows with status badge and totals.
  - Settings page: new "Gom xu" card with two numeric inputs (Ngưỡng X, Để lại Y) wired to `PUT /api/me/profile`.
- Java client (decompiled source under `extracted/src/`):
  - `Bot.createBotCollect` attaches a `BotObserver` so collector bots push obsCoin/obsStatus every 10 s like order bots.
  - `CollectScreen.getCoinKeep()` (currently a hardcoded conditional) is rewritten to read the user-scoped `collectKeep` value.
  - `ApplicationUI` drops `button_CollectCoin` + `button_ImportBotCollect` declarations, listeners, and layout adds.
  - `BotService.botCollects` is loaded from the new `GET /api/client/bots/collect` endpoint with the same delta-sync pattern used for `botOrders`.
  - A new poller (`CollectTaskPoller` or new branch inside `BotUpdaterService`) fetches pending CollectTasks, starts `CollectScreen.onCollect(true)` on each collector bot, and acks results on completion or timeout.

## Capabilities

### New Capabilities
- `collect-coins`: the whole user-facing collect-coins capability — `Bot.role`, per-user threshold/keep settings, `CollectTask` lifecycle (PENDING → IN_PROGRESS → DONE/FAILED), `/api/me/collect/*` flow (scan preview + task creation + history), `/api/client/collect/*` Java contract, and `/api/me/collect-bots/*` CRUD.

### Modified Capabilities
- `me-api`: `GET /api/me/profile` now exposes `collectThreshold` and `collectKeep`; a new `PUT /api/me/profile` mutating route is admitted alongside the existing read-only `/api/me/*` routes (the read-only requirement is narrowed to the listed resources rather than the whole `/api/me/profile` URL).
- `client-scoping`: the new `GET /api/client/bots/collect` and `/api/client/collect/*` endpoints SHALL be added to the list of Java-client routes that filter and stamp by `ownerId = req.user.sub`.
- `tenant-ownership`: `CollectTask` joins `Bot`, `Order`, `TradeLog`, `Block` as a tenant-owned resource (non-null `ownerId` with `ON DELETE RESTRICT`).
- `role-based-admin-ui`: the sidebar gains a new "Bot gom xu" entry that is visible to both USER and ADMIN, kept consistent with how `Bots`/`Orders` are exposed today.

## Impact

- **Database / migrations**: one new Prisma migration that runs a 4-phase change for `Bot.role` (ADD NULL → backfill `'ORDER'` → SET NOT NULL → CREATE INDEX), adds two columns on `User`, creates the `CollectTask` table, and adds the `collectTasksAt` column on `ClientSyncState`. No downtime expected: existing bots backfill to `role='ORDER'` and existing users get the column defaults.
- **Backend (`backend/src/`)**: new route module(s) under `routes/me/` for collect-bots + collect tasks + profile mutation; new route module under `routes/` (or extended `bots.ts` + new `collect.ts`) for the `/api/client/bots/collect` and `/api/client/collect/*` endpoints; mounting in `app.ts`; new helpers in `src/lib/` if needed (kept minimal). All routes reuse `getOwnerScope`.
- **Admin SPA (`admin/src/`)**: new feature folder `features/collect-bots/` (list page, CRUD form, scan-confirm dialog, history panel, hooks); Settings feature gains a "Gom xu" card; sidebar component + `routes.tsx` register the new entry.
- **Java client (`extracted/src/`)**: edits to `core/model/Bot.java`, `core/module/impl/CollectScreen.java`, `ui/ApplicationUI.java`, `core/service/BotService.java`, `core/service/BotUpdaterService.java` (or a new sibling poller). User SHALL rebuild the jar with `javac -source 1.8 -target 1.8`.
- **Backwards compatibility**: the existing `/api/client/bots/normal|split` endpoints SHALL keep returning the same payload shape for any caller that has not been rebuilt; collector bots SHALL be invisible to those endpoints because their `role` is `COLLECT`. Existing JWTs continue to authenticate without re-issue.
- **Out of scope**: cancelling an in-progress task, automatic retry for FAILED tasks, multi-server simultaneous gom orchestration, realtime/websocket notifications, and any change to the `/api/admin/*` surface.
