## Context

The legacy Java tool ships with a Swing UI that owns the entire "Gom xu" (collect-coins) experience:

- A "Nhập danh sách bot gom xu" button parses a local txt file into `BotService.botCollects` (see `extracted/src/core/service/BotService.java`).
- A "Gom xu / Dừng gom xu" button toggles `onCollectCoin` and calls `CollectScreen.onCollect(state)` on every collector (see `extracted/src/ui/ApplicationUI.java`).
- `CollectScreen.getCoinKeep()` hardcodes "keep 10 000 000 for VIP, else keep 0" (see `extracted/src/core/module/impl/CollectScreen.java`).
- Collector bots have no `BotObserver` attached in `Bot.createBotCollect(...)`, so the backend never receives their obsCoin / obsStatus.

Meanwhile the self-host stack (Node + Express + Prisma 5 + Postgres 16, plus a React 18 + shadcn admin SPA) already supports a per-user tenancy model with `ownerId` on every tenant table and the `/api/me/*` family of routes. The `Order` resource already demonstrates the "Java polls check-update → executes → acks" pattern we want to replicate for collect tasks.

This change closes the gap so that **all** collect-coins inputs (collector accounts, threshold X, keep Y, trigger) live in the web admin, while the Java jar becomes a thin executor that polls a `CollectTask` queue and reports results.

Stakeholders: operator (the user who owns the bots), admin (supervisory read-only role today), and the Java jar binary that ships to operator machines. No external dependency or third-party integration is introduced.

## Goals / Non-Goals

**Goals:**
- Single source of truth for collector bots and collect parameters is the backend (`Bot` table with `role=COLLECT`, `User.collectThreshold`, `User.collectKeep`).
- Each user can scope their own collector roster, their own threshold/keep, and their own task history without leaking into another user's data.
- Web operator can preview which order-bots will be gommed before committing (so accidental "all bots" runs are caught), then commit a `CollectTask` with a frozen target list.
- Java jar polls `/api/client/collect/check-update`, fetches a single pending task at a time, executes, and acks. Re-using the existing JWT + sync-delta pattern keeps the wire protocol coherent.
- `Bot.role = COLLECT` rows produce observer pushes every 10 s identical to order bots — operators can see the collector roster's coin/status in the admin SPA in near-real-time.
- Override the VIP rule: keep amount = `User.collectKeep` regardless of `typeLuckyDraw`.

**Non-Goals:**
- Cancelling a `CollectTask` once it has entered `IN_PROGRESS`. The Java jar runs to completion or to its own timeout, then acks DONE/FAILED.
- Automatic retry for FAILED tasks. The operator inspects history and re-clicks "Gom xu" if needed.
- Realtime / websocket notifications. Polling at the existing 10 s cadence is sufficient.
- Cross-server orchestration logic. The Java tool already routes a collector to a same-server order bot via `CollectScreen.onAliveActivities`, and that logic is unchanged.
- Migrating already-collected historical data into `CollectTask` — the history view starts from the first run after this change ships.
- Admin (ADMIN role) gaining a write surface over collector data: ADMIN keeps its read-only `/api/admin/*` view of bots/orders, parity with how it already treats orders today.

## Decisions

### Decision 1: One `Bot` table with a `role` discriminator vs. a sibling `CollectBot` table

We extend the existing `Bot` model with `role BotRole @default(ORDER)` rather than creating a parallel `CollectBot` table.

**Why X over Y:**
- The Java tool already treats collectors as `Bot` instances (same `Bot.java` class, only constructor differs via `createBotCollect`). Forking the storage model would have forced an artificial split in the wire format too.
- `Bot` already carries every field a collector needs (`account`, `password`, `charName`, `serverId`, `obsCoin`, `obsStatus`, …). The only collector-specific behavior is "do not run order screens", which `BotService` already toggles via the list it pulls them from.
- A single table keeps `/api/me/bots/*` and `/api/me/collect-bots/*` two thin filters of the same query rather than two duplicated CRUD routes. Indexes `Bot(ownerId, deletedAt)` and the new `Bot(ownerId, role, deletedAt)` both scale linearly with bot count per owner.

**Alternatives considered:**
- A new `CollectBot` table with a 1-1 FK to `Bot` (rejected — none of the collector-specific columns we considered actually exist).
- A boolean `isCollector` flag (rejected — enum lets us reuse the Java constant `ROLE_COLLECT=2` and keeps room for future roles such as `WATCHER`).

### Decision 2: Per-user threshold/keep on `User` vs. per-bot vs. per-task

`collectThreshold` and `collectKeep` are stored on `User` and frozen onto each `CollectTask` at creation time.

**Why X over Y:**
- The operator's mental model is "I want every bot above 1M xu gommed down to 0". That is a user-wide policy, not a per-bot one.
- Snapshotting threshold/keep onto `CollectTask` (columns `threshold`, `keep`) means a task's intended outcome is auditable even after the user edits their settings later.
- The Java side reads `keep` from the task payload, not from `User`, which removes a separate `/api/me/profile` round-trip from the executor's hot path.

**Alternatives considered:**
- Per-bot overrides (rejected — adds two more columns on `Bot` for a feature no one requested).
- Per-task only (rejected — would force the operator to retype values every run).

### Decision 3: Preview/scan endpoint is a separate POST that does not create a task

`POST /api/me/collect/scan` returns a target preview without side effects. Confirmation goes through `POST /api/me/collect/tasks` carrying the explicit `botIds[]` array.

**Why X over Y:**
- The web UI has a confirm dialog ("12 bot, ~120tr xu"). Doing scan-and-commit in one call would force the dialog to show "we already created a task; click to keep it" which is confusing.
- Sending `botIds[]` (not the whole target list) in the commit step lets the backend recompute `coinBefore` and `expectedCollect` from current `obsCoin`, so a stale preview shown for too long does not silently lock in old numbers.
- Both endpoints are POST because scan can be expensive (full Bot scan over the user's roster) and we don't want intermediate caches.

**Alternatives considered:**
- GET `/api/me/collect/scan` (rejected — GET should be idempotent; cache busters everywhere would be ugly).
- Single endpoint with `dryRun=true` flag (rejected — two POSTs are clearer in OpenSpec scenarios).

### Decision 4: `CollectTask` lifecycle stored as integer status, not enum

`CollectTask.status` is `Int` with `0=PENDING, 1=IN_PROGRESS, 2=DONE, 3=FAILED, 4=CANCELLED` to match the convention already used by `Order.status` in this codebase.

**Why X over Y:**
- Mirrors `Order.status` (also `Int`), so the admin frontend's existing status-badge helper applies without a new branch.
- Avoids a Prisma enum migration. Adding a new status value later is a code-only change instead of a schema migration.

**Alternatives considered:**
- Prisma enum (rejected — extra migration cost, no real benefit when the values are tightly coupled to the Java client which talks JSON-int anyway).

### Decision 5: Java client polls collect tasks through `BotUpdaterService`, not a new thread

The new "is there a pending collect task?" check is added to `BotUpdaterService` (which already polls bot details every 10 s) at a slower interval (every 10 s by default, reusing `UPDATE_BOT_DETAILS_INTERVAL`).

**Why X over Y:**
- Avoids spinning up an additional thread inside the Java jar (the existing thread pool is already constrained).
- Keeps the "all backend polling lives in one service" invariant that already holds for bot details, sync-delta, and order check-update.
- A pending task moves to `IN_PROGRESS` on the very first `GET /api/client/collect/pending` call (server-side mark), so even if two service ticks happen to overlap, the second one sees no pending work.

**Alternatives considered:**
- A dedicated `CollectTaskPoller` (rejected — duplicates existing scheduling infrastructure).
- SSE / WebSocket from backend to Java (rejected — the rest of the protocol is plain HTTP poll; adding a long-lived channel just for collect is asymmetric).

### Decision 6: Removing the Java-side "Import txt" + "Gom xu" buttons rather than hiding them

Both UI controls and their listeners are deleted outright.

**Why X over Y:**
- The user explicitly asked for the buttons to be gone (no fallback). Leaving dead code invites accidental re-enabling later.
- The Java jar still needs to ship for execution, but it has no operator-facing collect inputs anymore. The user re-builds the jar via `javac -source 1.8 -target 1.8` as they already do for other Java patches in this project.

**Alternatives considered:**
- Hiding behind a feature flag (rejected — the project does not use feature flags, and the migration is one-way).

### Decision 7: `TradeLog` rows are written by the **backend** on ack, not by the Java client

When `POST /api/client/collect/ack` reports `status=DONE` with `perBotResults[]`, the backend writes one `TradeLog` per gommed bot (`type=2 GOM_XU`).

**Why X over Y:**
- Single owner of truth for tenant logs. The Java side already writes `TradeLog` for order events via `PUT /api/log/trade`, but here we have the ack payload anyway, so we can centralize.
- Avoids a second HTTP call from the Java side per gommed bot.

**Alternatives considered:**
- Java writes via the existing `/api/log/trade` endpoint (rejected — adds N more HTTP calls per task with no value).

## Risks / Trade-offs

- **Risk:** A stale scan preview is shown for a long time, then the user confirms; threshold-crossing bots may have moved below the threshold in the meantime.
  → **Mitigation:** the commit endpoint recomputes `coinBefore` from current `obsCoin` and filters out anything that no longer crosses threshold; the snapshot stored on the task reflects the actual target set, not the preview.
- **Risk:** A collector bot is offline when its task starts → the run produces partial DONE plus FAILED entries.
  → **Mitigation:** `perBotResults[]` allows mixed outcomes; `CollectTask.status` is `DONE` if at least one bot collected, `FAILED` if none did; `totalCollected` records the actual sum.
- **Risk:** Two `CollectTask`s become PENDING concurrently for the same user (e.g., a rebuild of the Java jar restarts mid-run).
  → **Mitigation:** `/api/client/collect/pending` returns the oldest PENDING and marks it `IN_PROGRESS` atomically (Prisma `update`+`where status=PENDING` returns 0 rows if another tick already grabbed it).
- **Risk:** The Java client compiles against `--source 1.8` and the new poller uses Java-17 APIs.
  → **Mitigation:** the poller reuses existing `HttpURLConnection` / `Res.t()` helpers and `BotUpdaterService` patterns, which are all Java-8 safe.
- **Risk:** Backward compatibility with un-rebuilt Java jars in the field that may still try to load `botCollects` from a local txt.
  → **Mitigation:** un-rebuilt jars keep working in their old way (they don't talk to `/api/client/bots/collect`); however, they ignore web-side configuration entirely. We document the rebuild requirement in the change archive notes.
- **Risk:** `Bot.role` migration on a non-empty database leaves rows with NULL role between ADD and UPDATE.
  → **Mitigation:** use the existing 4-phase migration pattern (ADD NULL → UPDATE backfill → SET NOT NULL → CREATE INDEX) already used for `ownerId`; migration runs in a single transaction inside `prisma migrate deploy`.
- **Trade-off:** Storing `targets` as JSON on `CollectTask` means we can't easily index per-target-bot queries. We accept this because the admin view only needs per-task aggregate info, and per-bot history can be derived from `TradeLog` rows.

## Migration Plan

1. Author Prisma migration `add_collect_coins`:
   - `ALTER TYPE` for `BotRole` enum or `CREATE TYPE` (depends on Prisma generator output).
   - `ALTER TABLE "Bot" ADD COLUMN "role" "BotRole"` (nullable initially).
   - `UPDATE "Bot" SET "role" = 'ORDER' WHERE "role" IS NULL`.
   - `ALTER TABLE "Bot" ALTER COLUMN "role" SET NOT NULL`, `SET DEFAULT 'ORDER'`.
   - `CREATE INDEX "Bot_ownerId_role_deletedAt_idx"`.
   - `ALTER TABLE "User" ADD COLUMN "collectThreshold" INTEGER NOT NULL DEFAULT 1000000`.
   - `ALTER TABLE "User" ADD COLUMN "collectKeep" INTEGER NOT NULL DEFAULT 0`.
   - `CREATE TABLE "CollectTask" (...)` with FK to `User` (`ON DELETE RESTRICT`) and indexes `(ownerId, status, createdAt)`.
   - `ALTER TABLE "ClientSyncState" ADD COLUMN "collectTasksAt" TIMESTAMP NOT NULL DEFAULT now()`.
2. Take a pg_dump backup of the running `vxmm-pg` container (same pattern memorialized in `tool-vxmm-backend-selfhost`).
3. Apply backend changes (routes, lib, app mounts).
4. Apply admin SPA changes.
5. Apply Java source patches under `extracted/src/`; rebuild jar with `javac -source 1.8 -target 1.8 -d build-dir`.
6. Smoke test path: USER logs in → sees "Bot gom xu" → adds one collector → opens Settings → sets X=1_000_000, Y=0 → opens collect-bots page → clicks "Gom xu" → confirms → watches task move PENDING → IN_PROGRESS → DONE in history; Java jar logs show observer push for collector + task ack.

**Rollback:** If the migration fails midway, `prisma migrate resolve --rolled-back add_collect_coins` plus restoring the pg_dump returns to pre-change state. Old Java jars keep working because the `/api/client/bots/normal|split` endpoints stay untouched and continue returning `role='ORDER'` rows.

## Open Questions

All open questions from earlier exploration were resolved by the user's a-A/b-A/c-A/d-A/e-A selection. None remain.
