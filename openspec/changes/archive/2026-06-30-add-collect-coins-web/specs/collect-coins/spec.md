## ADDED Requirements

### Requirement: Bot table SHALL distinguish ORDER vs COLLECT via a non-null role column

`Bot` SHALL gain a non-null `role` column of enum type `BotRole` with values `ORDER` and `COLLECT`. The default SHALL be `ORDER`. Existing rows SHALL be backfilled to `ORDER` by the migration before the NOT NULL constraint is applied. A composite index `(ownerId, role, deletedAt)` SHALL exist to support per-owner role-scoped queries.

#### Scenario: Pre-existing bots are ORDER after migration
- **WHEN** the migration runs against a database that contains existing bots created before this change
- **THEN** every existing `Bot` row SHALL have `role = 'ORDER'` and `deletedAt` unchanged

#### Scenario: Insert without role uses the ORDER default
- **WHEN** a new `Bot` row is inserted without an explicit `role`
- **THEN** the stored row SHALL have `role = 'ORDER'`

#### Scenario: Insert with role outside the enum is rejected
- **WHEN** a new `Bot` row is inserted with `role = 'WATCHER'`
- **THEN** the database SHALL reject the insert with an enum-domain error

### Requirement: User SHALL carry collect threshold and keep settings

`User` SHALL gain two non-null integer columns: `collectThreshold` (default `1000000`) and `collectKeep` (default `0`). Both values SHALL be `>= 0` and the relation `collectThreshold > collectKeep` SHALL be enforced by the backend on every write.

#### Scenario: New users get default threshold and keep
- **WHEN** a new `User` row is inserted without explicit threshold/keep values
- **THEN** the row SHALL have `collectThreshold = 1000000` and `collectKeep = 0`

#### Scenario: PUT /api/me/profile with threshold <= keep is rejected
- **WHEN** the caller submits `PUT /api/me/profile` with body `{ collectThreshold: 5000, collectKeep: 5000 }` (or threshold less than keep)
- **THEN** the response SHALL be `400 Bad Request` with a Zod validation error and the row SHALL be unchanged

#### Scenario: PUT /api/me/profile with negative value is rejected
- **WHEN** the caller submits `PUT /api/me/profile` with body `{ collectKeep: -1 }`
- **THEN** the response SHALL be `400 Bad Request` and the row SHALL be unchanged

#### Scenario: GET /api/me/profile returns the threshold/keep
- **WHEN** the caller calls `GET /api/me/profile`
- **THEN** the response SHALL include `collectThreshold` and `collectKeep` as integers reflecting the stored values

### Requirement: CollectTask SHALL be a tenant-owned resource with a fixed lifecycle

A `CollectTask` table SHALL exist with columns: `id`, `ownerId` (non-null FK to `User`, `ON DELETE RESTRICT`), `status` (Int, non-null, default 0), `targets` (JSON, non-null), `threshold` (Int, non-null), `keep` (Int, non-null), `totalCollected` (Int, non-null, default 0), `createdAt`, `updatedAt`, `completedAt` (nullable). Allowed status values SHALL be `0=PENDING`, `1=IN_PROGRESS`, `2=DONE`, `3=FAILED`, `4=CANCELLED`. The index `(ownerId, status, createdAt)` SHALL exist.

#### Scenario: Insert with no owner is rejected
- **WHEN** a `CollectTask` row is inserted without an `ownerId`
- **THEN** the database SHALL reject the insert with a NOT NULL violation

#### Scenario: Status transitions are linear forward-only
- **WHEN** a `CollectTask` is in status `2 (DONE)` and the backend attempts to set it back to `0 (PENDING)`
- **THEN** the backend SHALL reject the transition and the row SHALL remain `DONE`

#### Scenario: Deleting an owner with existing CollectTask is blocked
- **WHEN** the database receives `DELETE FROM "User" WHERE id = X` and X owns at least one `CollectTask` row
- **THEN** the delete SHALL fail with a foreign-key constraint error

### Requirement: /api/me/collect-bots/* SHALL CRUD bots scoped to role=COLLECT and the caller

`GET /api/me/collect-bots`, `POST /api/me/collect-bots`, `GET /api/me/collect-bots/:id`, `PUT /api/me/collect-bots/:id`, and `DELETE /api/me/collect-bots/:id` SHALL behave like `/api/me/bots/*` except that they SHALL force `role = 'COLLECT'` on every read filter and on every create payload. Requests that include `role` in their body SHALL have that field ignored on creation and rejected on update.

#### Scenario: POST stamps role=COLLECT regardless of body
- **WHEN** a USER calls `POST /api/me/collect-bots` with body `{ ..., role: 'ORDER' }`
- **THEN** the new `Bot` row SHALL have `role = 'COLLECT'` and the response SHALL reflect `role = 'COLLECT'`

#### Scenario: GET returns only COLLECT bots of the caller
- **WHEN** user A (who has 3 ORDER bots and 2 COLLECT bots) calls `GET /api/me/collect-bots`
- **THEN** the response SHALL contain exactly the 2 COLLECT bots of user A

#### Scenario: GET cannot see another user's COLLECT bot
- **WHEN** user A calls `GET /api/me/collect-bots/:id` where the bot's `ownerId != A`
- **THEN** the response SHALL be `404 Not Found`

#### Scenario: PUT rejects role transitions
- **WHEN** a USER calls `PUT /api/me/collect-bots/:id` with body `{ role: 'ORDER' }`
- **THEN** the response SHALL be `400 Bad Request` and the row SHALL be unchanged

### Requirement: POST /api/me/collect/scan SHALL preview the targets without side effects

`POST /api/me/collect/scan` SHALL query `Bot` rows where `ownerId = req.user.sub AND role = 'ORDER' AND deletedAt IS NULL AND obsCoin > User.collectThreshold AND obsStatus = 'ONLINE'`, and SHALL return `{ targets: Array<{ botId, charName, serverId, coinNow, expectedCollect }>, collectBotsAvailable: number, threshold: number, keep: number }`. The endpoint SHALL NOT create any database row.

#### Scenario: Scan returns no targets when no bot exceeds threshold
- **WHEN** user A's ORDER bots all have `obsCoin <= collectThreshold`
- **THEN** the response SHALL have `targets = []` and the response SHALL still include `collectBotsAvailable`, `threshold`, and `keep`

#### Scenario: Scan returns the right expectedCollect
- **WHEN** user A's threshold is `1000000`, keep is `0`, and an online ORDER bot has `obsCoin = 5_500_000`
- **THEN** that bot's entry SHALL show `coinNow = 5500000` and `expectedCollect = 5500000`

#### Scenario: Scan never creates a CollectTask
- **WHEN** `POST /api/me/collect/scan` is called
- **THEN** the count of `CollectTask` rows for user A SHALL be unchanged

#### Scenario: Scan does not include offline bots
- **WHEN** a user's ORDER bot has `obsCoin > threshold` but `obsStatus = 'OFFLINE'`
- **THEN** that bot SHALL NOT appear in `targets`

#### Scenario: Scan does not include COLLECT bots
- **WHEN** user A has both ORDER and COLLECT bots above threshold and online
- **THEN** only the ORDER bots SHALL appear in `targets`

### Requirement: POST /api/me/collect/tasks SHALL commit a CollectTask with frozen snapshot

`POST /api/me/collect/tasks` SHALL accept body `{ botIds: number[] }`, recompute the target list at request time from current `obsCoin`, freeze `threshold` and `keep` from the caller's `User` row, set `status = 0`, and persist the `targets` JSON. Bot ids that do not belong to the caller, are not ORDER, or no longer cross the threshold SHALL be silently dropped from the persisted target list. The response SHALL be the created `CollectTask` row.

#### Scenario: Task is created with frozen threshold/keep snapshot
- **WHEN** user A (threshold=1000000, keep=0) calls `POST /api/me/collect/tasks` with botIds=[10, 11]
- **THEN** the new row SHALL have `threshold = 1000000` and `keep = 0` even if user A later edits their settings

#### Scenario: Bots that no longer cross threshold are dropped
- **WHEN** user A submits botIds=[10, 11] but bot 11 has obsCoin below threshold at request time
- **THEN** the persisted `targets` JSON SHALL contain only bot 10

#### Scenario: Cannot include another user's bot
- **WHEN** user A submits botIds=[42] but bot 42 has `ownerId = B`
- **THEN** bot 42 SHALL be dropped from the persisted targets

#### Scenario: Empty target list returns 400
- **WHEN** the recomputed target list is empty (all bots dropped, or `botIds = []`)
- **THEN** the response SHALL be `400 Bad Request` and no row SHALL be created

#### Scenario: Created task starts as PENDING
- **WHEN** a `CollectTask` is created
- **THEN** its initial `status` SHALL be `0 (PENDING)`, `totalCollected` SHALL be `0`, and `completedAt` SHALL be `NULL`

### Requirement: GET /api/me/collect/tasks SHALL list and detail tasks scoped to caller

`GET /api/me/collect/tasks` SHALL return a list of `CollectTask` rows where `ownerId = req.user.sub`, ordered `createdAt DESC`, optionally filtered by `?status=<int>`, with pagination headers consistent with the rest of `/api/me/*`. `GET /api/me/collect/tasks/:id` SHALL return one row or `404` when the row does not belong to the caller.

#### Scenario: List only the caller's tasks
- **WHEN** user A calls `GET /api/me/collect/tasks`
- **THEN** the response SHALL contain only rows where `ownerId = A`

#### Scenario: Detail rejects another user's task
- **WHEN** user A calls `GET /api/me/collect/tasks/:id` for a task whose `ownerId = B`
- **THEN** the response SHALL be `404 Not Found`

### Requirement: GET /api/client/bots/collect SHALL serve the Java collector roster

`GET /api/client/bots/collect` SHALL return the caller's `Bot` rows where `role = 'COLLECT' AND deletedAt IS NULL`, in the same JSON shape used by `GET /api/client/bots/normal`. The endpoint SHALL filter by `ownerId = req.user.sub`.

#### Scenario: Java client logged in as user A only sees user A's collectors
- **WHEN** the Java client authenticates as user A and calls `GET /api/client/bots/collect`
- **THEN** the response SHALL contain only `Bot` rows where `ownerId = A AND role = 'COLLECT' AND deletedAt IS NULL`

#### Scenario: Response shape matches /api/client/bots/normal
- **WHEN** the Java client calls `GET /api/client/bots/collect`
- **THEN** each returned bot SHALL include the same field names and types as the response from `GET /api/client/bots/normal`

### Requirement: Collect task polling SHALL follow the Order check-update + pending + ack pattern

`GET /api/client/collect/check-update` SHALL return `{ hasPending: boolean, taskId: number|null }` for the oldest PENDING `CollectTask` of the caller (or `false`/`null` when none). `GET /api/client/collect/pending` SHALL atomically return the oldest PENDING `CollectTask` and update its status to `IN_PROGRESS`. `POST /api/client/collect/ack` SHALL accept body `{ taskId, status: 'DONE'|'FAILED', totalCollected, perBotResults: Array<{ botId, collected, error? }> }`, update the task's `status`, `totalCollected`, and `completedAt`, and write one `TradeLog` row of `type = 2` for each `perBotResults[]` entry whose `collected > 0`.

#### Scenario: check-update reports the oldest PENDING task
- **WHEN** user A has two PENDING `CollectTask` rows (created at t=1 and t=2) and the Java client calls `GET /api/client/collect/check-update`
- **THEN** the response SHALL be `{ hasPending: true, taskId: <id of the t=1 row> }`

#### Scenario: pending atomically transitions PENDING to IN_PROGRESS
- **WHEN** the Java client calls `GET /api/client/collect/pending` while a task is PENDING
- **THEN** the response SHALL be the task body and the underlying row SHALL be `IN_PROGRESS` after the call

#### Scenario: pending returns 404 when nothing is pending
- **WHEN** the Java client calls `GET /api/client/collect/pending` and no PENDING task exists for the caller
- **THEN** the response SHALL be `404 Not Found`

#### Scenario: ack writes one TradeLog per successfully collected bot
- **WHEN** the Java client calls `POST /api/client/collect/ack` with `perBotResults` containing two entries with `collected > 0` and one entry with `collected = 0`
- **THEN** exactly two `TradeLog` rows of `type = 2` SHALL be created, with `ownerId = req.user.sub`

#### Scenario: ack moves task to DONE when status='DONE'
- **WHEN** the Java client posts ack with `status = 'DONE'` and `totalCollected = 7_500_000`
- **THEN** the task SHALL be `status = 2 (DONE)`, `totalCollected = 7_500_000`, and `completedAt` SHALL be set to now

#### Scenario: ack moves task to FAILED when status='FAILED'
- **WHEN** the Java client posts ack with `status = 'FAILED'`
- **THEN** the task SHALL be `status = 3 (FAILED)` and `completedAt` SHALL be set to now

#### Scenario: ack rejects another user's task
- **WHEN** user A's Java client posts ack for a `taskId` whose `ownerId = B`
- **THEN** the response SHALL be `404 Not Found` and the row SHALL be unchanged

### Requirement: Web admin SPA SHALL surface collector bots, settings, and trigger flow

The admin SPA SHALL include a sidebar entry "Bot gom xu" routed to `/collect-bots`, a list page with CRUD for collector bots, a "Gom xu" trigger button that calls the scan endpoint and shows a confirm dialog, a history panel listing `CollectTask` rows, and a Settings card titled "Gom xu" with `collectThreshold` and `collectKeep` numeric inputs.

#### Scenario: USER sees the Bot gom xu sidebar entry
- **WHEN** a USER-role session enters the admin SPA
- **THEN** the sidebar SHALL include an entry labelled "Bot gom xu" linking to `#/collect-bots`

#### Scenario: Clicking Gom xu opens a confirmation dialog
- **WHEN** the user clicks the "Gom xu" header button on `/collect-bots`
- **THEN** the SPA SHALL call `POST /api/me/collect/scan`, render the returned targets in a shadcn `AlertDialog`, and show a primary action "Gom xu" plus a "Huỷ" cancel button

#### Scenario: Confirming creates a task and refreshes the history panel
- **WHEN** the user clicks the dialog's "Gom xu" primary action
- **THEN** the SPA SHALL call `POST /api/me/collect/tasks` with `botIds` of every target the user did not deselect, show a success toast, and refresh the history list

#### Scenario: Settings card validates threshold > keep before submit
- **WHEN** the user types threshold=5000 and keep=5000 in the "Gom xu" card and clicks Save
- **THEN** the form SHALL show an inline validation error and SHALL NOT call `PUT /api/me/profile`
