## 1. Database schema and migration

- [x] 1.1 Take a pg_dump backup of the running `vxmm-pg` Postgres container before any schema change
- [x] 1.2 Add `enum BotRole { ORDER, COLLECT }` to `backend/prisma/schema.prisma`
- [x] 1.3 Add `role BotRole @default(ORDER)` to model `Bot` in `backend/prisma/schema.prisma` and add composite index `@@index([ownerId, role, deletedAt])`
- [x] 1.4 Add `collectThreshold Int @default(1000000)` and `collectKeep Int @default(0)` to model `User`
- [x] 1.5 Add new model `CollectTask` (`id`, `ownerId` FK with `onDelete: Restrict`, `status Int @default(0)`, `targets Json`, `threshold Int`, `keep Int`, `totalCollected Int @default(0)`, `createdAt`, `updatedAt`, `completedAt DateTime?`, index `@@index([ownerId, status, createdAt])`) plus relation on `User`
- [x] 1.6 Add `collectTasksAt DateTime @default(now())` to model `ClientSyncState`
- [x] 1.7 Write the Prisma migration SQL by hand under `backend/prisma/migrations/<timestamp>_add_collect_coins/migration.sql` with 4 phases: CREATE TYPE → ADD COLUMNs nullable → UPDATE backfill `role='ORDER'` → SET NOT NULL + SET DEFAULT → CREATE INDEXes → CREATE TABLE `CollectTask` → ADD COLUMN `ClientSyncState.collectTasksAt`
- [x] 1.8 Run `prisma migrate deploy` and verify migration applies cleanly against the running container ← (verify: every existing Bot row has role='ORDER' and indexes are present in pg_indexes)
- [x] 1.9 Regenerate Prisma client via `prisma generate`

## 2. Backend shared helpers and types

- [x] 2.1 Add a `BOT_ROLE` enum re-export (`'ORDER' | 'COLLECT'`) plus Zod schemas in `backend/src/lib/` so route handlers can reuse them
- [x] 2.2 Extend `getOwnerScope` test paths or helper modules to make `role`-aware filtering ergonomic; no behavior change for existing callers
- [x] 2.3 Audit `parseListQuery` in `backend/src/lib/list.ts` to ensure it supports filtering `CollectTask` rows by `status`

## 3. Backend: /api/me/profile mutation + threshold/keep exposure

- [x] 3.1 Update the existing `GET /api/me/profile` handler in `backend/src/routes/me/profile.ts` (or equivalent) to include `collectThreshold` and `collectKeep` from the caller's `User` row
- [x] 3.2 Add `PUT /api/me/profile` route with Zod body schema `{ collectThreshold?: number().int().nonnegative(), collectKeep?: number().int().nonnegative() }`, refining `collectThreshold > collectKeep` after merging with current row values
- [x] 3.3 Reject any extra/unknown fields in the PUT body (e.g., `role`, `username`, `password`) with `400 Bad Request`
- [x] 3.4 Mount the new route in `backend/src/app.ts` ← (verify: PUT/GET pair behaves per the me-api spec scenarios, including the threshold > keep refinement and the unknown-field rejection)

## 4. Backend: /api/me/collect-bots CRUD

- [x] 4.1 Create `backend/src/routes/me/collectBots.ts` modeled on `backend/src/routes/me/bots.ts` (USER tenant CRUD)
- [x] 4.2 In every read filter add `role: 'COLLECT'` alongside the existing `ownerId` filter
- [x] 4.3 In `POST`, force `role: 'COLLECT'` on the persisted record regardless of payload
- [x] 4.4 In `PUT`, reject any body that includes `role` with `400 Bad Request`
- [x] 4.5 Mount router at `/api/me/collect-bots` in `backend/src/app.ts` ← (verify: list/detail/create/update/delete behave exactly like /api/me/bots but role=COLLECT is enforced)

## 5. Backend: /api/me/collect scan + tasks endpoints

- [x] 5.1 Create `backend/src/routes/me/collect.ts` exporting an Express router for `/api/me/collect/*`
- [x] 5.2 Implement `POST /api/me/collect/scan`: read caller's `collectThreshold`/`collectKeep`, query `Bot WHERE ownerId, role='ORDER', deletedAt IS NULL, obsCoin > threshold, obsStatus = 'ONLINE'`, return `{ targets: [{botId, charName, serverId, coinNow, expectedCollect}], collectBotsAvailable, threshold, keep }` where `expectedCollect = coinNow - keep` and `collectBotsAvailable` counts the caller's `role='COLLECT'` bots
- [x] 5.3 Implement `POST /api/me/collect/tasks`: Zod body `{ botIds: number().int().array().min(1) }`; recompute target list at request time (re-fetch obsCoin, re-filter by current threshold/role/owner); reject with `400` when the recomputed list is empty; persist `CollectTask` with snapshot threshold/keep + targets JSON
- [x] 5.4 Implement `GET /api/me/collect/tasks` list with `parseListQuery` filtering by `ownerId` and optional `status`
- [x] 5.5 Implement `GET /api/me/collect/tasks/:id` returning the row or `404` when ownership mismatches
- [x] 5.6 Mount the collect router in `backend/src/app.ts` ← (verify: all five collect endpoints honor the collect-coins spec scenarios, especially the empty-target-list 400, the silent-drop of non-eligible botIds, and the frozen threshold/keep snapshot)

## 6. Backend: /api/client/bots/collect

- [x] 6.1 Extend `backend/src/routes/bots.ts` (or its `/api/client/bots/:mode` handler) so that `mode === 'collect'` is a valid value alongside `normal` and `split`, and the resulting query filters `role: 'COLLECT'` instead of the existing client/mode partitioning
- [x] 6.2 Ensure the response body shape for `/collect` matches `/normal` exactly (same fields, same types) — derive both from one serializer
- [x] 6.3 Confirm sync-delta endpoints (`check-update`, `new`, `updated`, `deleted`) extend to `mode=collect` and reuse the existing `ClientSyncState` per-mode timestamps (or expose `collect` as a virtual mode that maps onto the same `newBotsAt`/`updatedBotsAt`/`deletedBotsAt` pair but filters `role='COLLECT'`)
- [x] 6.4 Verify existing `mode=normal`/`mode=split` callers still return identical payloads to the pre-change baseline ← (verify: bots/normal and bots/split responses byte-identical to baseline; bots/collect filters role=COLLECT and is owner-scoped)

## 7. Backend: /api/client/collect (check-update + pending + ack)

- [x] 7.1 Create `backend/src/routes/clientCollect.ts` mounted under `/api/client/collect`
- [x] 7.2 `GET /api/client/collect/check-update`: `SELECT id FROM CollectTask WHERE ownerId = req.user.sub AND status = 0 ORDER BY createdAt ASC LIMIT 1`, return `{ hasPending: <bool>, taskId: <id|null> }`
- [x] 7.3 `GET /api/client/collect/pending`: pick the oldest PENDING row, then `updateMany WHERE id = ? AND status = 0 SET status = 1`; if 0 rows updated → return `404 Not Found`; else return the task body including `threshold`, `keep`, `targets`
- [x] 7.4 `POST /api/client/collect/ack`: Zod body `{ taskId: int, status: 'DONE'|'FAILED', totalCollected: int.nonneg, perBotResults: [{botId: int, collected: int.nonneg, error: string().optional()}] }`; `updateMany WHERE id = ? AND ownerId = req.user.sub AND status = 1 SET status = 2|3, totalCollected, completedAt = now()`; if 0 rows updated → return `404 Not Found`
- [x] 7.5 In the ack handler, after the task update succeeds, INSERT one `TradeLog` row per `perBotResults[]` entry where `collected > 0` (type=2, ownerId, serverId from the Bot row, customer = charName, change = collected, before = (after + collected), after = (before - collected), description = `"GOM XU task#${taskId}"`, timeStart = task.createdAt epoch ms, timeStop = now epoch ms)
- [x] 7.6 Mount the router in `backend/src/app.ts` ← (verify: check-update is owner-scoped, pending atomically transitions PENDING→IN_PROGRESS, ack writes one TradeLog per collected bot and rejects another user's taskId)

## 8. Admin SPA: shared sidebar + routes

- [x] 8.1 Add a "Bot gom xu" entry in `admin/src/components/layout/Sidebar.tsx` (or equivalent) under the "Manage" group for both USER and ADMIN sessions, using `lucide-react` `Coins` icon
- [x] 8.2 Register the route `/collect-bots` in `admin/src/routes.tsx` inside `AuthGuard` (no `AdminOnlyRoute` wrap)
- [x] 8.3 Create the feature folder `admin/src/features/collect-bots/`

## 9. Admin SPA: collect-bots list + CRUD

- [x] 9.1 Create `admin/src/features/collect-bots/useCollectBots.ts` modeled on `features/bots/useBots.ts` but pointed at `/api/me/collect-bots`
- [x] 9.2 Create `CollectBotsListPage.tsx` with a DataTable showing account, charName, server, obsCoin, obsStatus, obsLastOnline, actions — pattern from `BotsListPage`
- [x] 9.3 Create CRUD form (modal or page) with fields serverId, account, password, charName, mapId, zoneId, manager, chat, sms, enable; EXCLUDE `playFee` and `typeLuckyDraw`
- [x] 9.4 Wire the React Query mutations against `/api/me/collect-bots` and invalidate the list on success

## 10. Admin SPA: "Gom xu" trigger and confirmation dialog

- [x] 10.1 Add a "Gom xu" header button on `CollectBotsListPage` that calls a new `useScanCollect()` hook posting to `/api/me/collect/scan`
- [x] 10.2 Open a shadcn `AlertDialog` showing: `collectBotsAvailable`, `threshold`, `keep`, the targets list (charName | server | coinNow → keep Y / collect X), sum of `expectedCollect`, primary action "Gom xu", cancel "Huỷ"
- [x] 10.3 On primary action click, call `POST /api/me/collect/tasks` with `botIds = targets.map(t => t.botId)`, show a success toast on 201/200, refresh the history list
- [x] 10.4 Handle the `400 empty target list` case with a toast that explains no bot crosses threshold

## 11. Admin SPA: collect task history panel

- [x] 11.1 Create `useCollectTasks.ts` hook (list + detail) calling `/api/me/collect/tasks`
- [x] 11.2 Render a section "Lịch sử gom xu" on `CollectBotsListPage` (or a tab) showing a DataTable: id, status badge, totalCollected, createdAt, completedAt, target count
- [x] 11.3 Status badge mapping: 0 PENDING (slate), 1 IN_PROGRESS (yellow), 2 DONE (green), 3 FAILED (red), 4 CANCELLED (gray) — reuse the existing badge helper used by Orders
- [x] 11.4 Poll the list every 10s when at least one row is in 0 or 1 status (auto-refetch) ← (verify: visiting the page after a task ack shows status=DONE within ~10s without manual refresh)

## 12. Admin SPA: Settings card "Gom xu"

- [x] 12.1 Add a new card titled "Gom xu" inside the existing Settings page (`admin/src/features/settings/*`)
- [x] 12.2 Two numeric inputs: "Ngưỡng X (xu)" and "Để lại Y (xu)"; load defaults from `GET /api/me/profile`
- [x] 12.3 React Hook Form with Zod schema: both `>= 0`, threshold `>` keep; show inline errors
- [x] 12.4 Save button posts `PUT /api/me/profile` and shows a success toast ← (verify: GET /api/me/profile after save returns updated values, scan endpoint immediately uses new threshold)

## 13. Java patch: model + observer + collect logic

- [x] 13.1 In `extracted/src/core/model/Bot.java` `createBotCollect` (currently around line 117), attach a `BotObserver` (matching how `createBotOrder` does it) so collect bots push obsCoin/obsStatus through `BotUpdaterService`
- [x] 13.2 In `extracted/src/core/module/impl/CollectScreen.java`, replace the hardcoded conditional `return typeLucky == 0 ? coins - 10000000 : coins;` (around line 77) with `return Math.max(0L, coins - User.collectKeep);` where `User.collectKeep` is the per-user keep value carried in the active CollectTask payload (introduce a setter / field on `Bot` or `CollectScreen` populated from the task payload when the poller starts the run)
- [x] 13.3 Rename or repurpose the `typeLucky` branch so it is never read by collect logic anymore; preserve order-bot behavior that still relies on `typeLuckyDraw`

## 14. Java patch: UI button removal

- [x] 14.1 In `extracted/src/ui/ApplicationUI.java`, remove the `button_CollectCoin` declaration, its listener block (around line 253), its `setText("Gom xu")` initializer (around line 575), and every `add(...)` call placing it into the layout
- [x] 14.2 Remove `button_ImportBotCollect` declaration, listener, label initializer, and layout placement
- [x] 14.3 Remove the field declarations and any references in `ApplicationUI` constructor / `actionPerformed` switch
- [x] 14.4 If `Application.alert(this, "Bạn chưa có tài khoản gom xu nào!");` was the only UX path for the empty-collect-roster case, drop it (the web shows availability in the dialog now) ← (verify: build artifact does not reference button_CollectCoin or button_ImportBotCollect anywhere)

## 15. Java patch: BotService.botCollects backed by HTTP

- [x] 15.1 In `extracted/src/core/service/BotService.java`, remove or repurpose the txt-file import code path that populates `botCollects`
- [x] 15.2 Add a method `refreshBotCollects()` that calls `GET /api/client/bots/collect` (reuse the same HTTP helper used by `refreshBotOrders`) and replaces the local list
- [x] 15.3 Wire the new fetch into the existing sync-delta loop (next to the existing `mode=normal`/`mode=split` polling) so collect bots are kept up to date the same way order bots are ← (verify: starting the Java jar with a USER JWT populates BotService.botCollects from the backend without any local txt file)

## 16. Java patch: collect-task poller

- [x] 16.1 Inside `extracted/src/core/service/BotUpdaterService.java` (or a new sibling service called from the same run loop), add a `timeCheckCollect` cursor that fires every 10 s
- [x] 16.2 On fire, call `GET /api/client/collect/check-update`; if `hasPending=true`, call `GET /api/client/collect/pending` to fetch the task body
- [x] 16.3 Apply `task.keep` to every collect bot (so `CollectScreen.getCoinKeep` returns `coins - keep`); call `CollectScreen.onCollect(true)` on each collect bot in `BotService.botCollects`
- [x] 16.4 Track per-bot results: when a collect bot reports a successful trade (existing `CollectTrade` success path) record `{ botId: target.botId, collected: amount }`
- [x] 16.5 When all targets are done or a per-task timeout (e.g., 5 minutes) elapses, call `POST /api/client/collect/ack` with `status='DONE'` if `totalCollected > 0` else `'FAILED'`, then set `onCollectCoin=false` on every collect bot ← (verify: a manual end-to-end run shows PENDING→IN_PROGRESS via /pending, and the task moves to DONE with TradeLog rows after ack)

## 17. Build verification

- [x] 17.1 `cd backend && npx prisma generate && npx tsc --noEmit` — ensure type checks pass
- [x] 17.2 `cd admin && npm run build` — ensure SPA builds without TS or Vite errors
- [x] 17.3 Compile the patched Java sources with `javac -source 1.8 -target 1.8` against the project's existing classpath
- [x] 17.4 Smoke run: start backend + admin, log in as a USER, add one collect bot, set X=1_000_000 + Y=0 in Settings, click "Gom xu", observe the task move PENDING→IN_PROGRESS→DONE and the TradeLog rows appear in "Giao dịch xu" ← (verify: full end-to-end matches all collect-coins spec scenarios)
