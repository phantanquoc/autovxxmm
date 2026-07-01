# me-api Specification

## Purpose
TBD - created by archiving change add-per-user-tenant-ownership. Update Purpose after archive.
## Requirements
### Requirement: /api/me/* routes MUST scope every operation to the caller
All routes under `/api/me/*` SHALL require a valid authentication token (any role) and SHALL force `ownerId = req.user.sub` on every read filter, every create payload, and every update/delete predicate. Calls from one user SHALL NOT be able to observe or modify another user's data through these routes.

#### Scenario: Anonymous request is rejected
- **WHEN** a request without a valid bearer token reaches any `/api/me/*` endpoint
- **THEN** the response SHALL be `401 Unauthorized`

#### Scenario: GET /api/me/bots returns only the caller's bots
- **WHEN** a user with id=2 calls `GET /api/me/bots`
- **THEN** the response SHALL contain only `Bot` rows where `ownerId=2` and `deletedAt IS NULL`

#### Scenario: POST /api/me/bots stamps the caller as owner
- **WHEN** a user with id=2 calls `POST /api/me/bots` with a valid bot payload
- **THEN** a new `Bot` row SHALL be created with `ownerId=2`, regardless of any `ownerId` field present in the request body

#### Scenario: PUT /api/me/bots/:id rejects updates to other users' bots
- **WHEN** a user with id=2 calls `PUT /api/me/bots/:id` for a bot whose `ownerId=1`
- **THEN** the response SHALL be `404 Not Found` and the bot SHALL be unchanged

#### Scenario: DELETE /api/me/bots/:id rejects deletes of other users' bots
- **WHEN** a user with id=2 calls `DELETE /api/me/bots/:id` for a bot whose `ownerId=1`
- **THEN** the response SHALL be `404 Not Found` and the bot SHALL be unchanged

### Requirement: /api/me read-only routes
`/api/me/orders`, `/api/me/tradeLogs`, and `/api/me/stats` SHALL be read-only relative to their resources (no mutating verbs) and SHALL filter strictly to the caller's `ownerId`.

`/api/me/profile` SHALL expose `GET` and `PUT`:
- `GET /api/me/profile` SHALL return `{id, username, role, collectThreshold, collectKeep}` where `id`, `username`, `role` derive from the verified JWT and `collectThreshold`, `collectKeep` come from the caller's `User` row.
- `PUT /api/me/profile` SHALL accept body `{ collectThreshold?: number, collectKeep?: number }`, validate `collectThreshold >= 0`, `collectKeep >= 0`, and `collectThreshold > collectKeep` (when either field is present, both effective values after merge MUST still satisfy these rules), and update the caller's `User` row. The route SHALL reject any other field with `400 Bad Request`. The route SHALL NOT allow changing `username`, `role`, or `password` through this endpoint.

#### Scenario: GET /api/me/orders lists only caller orders
- **WHEN** a user with id=2 calls `GET /api/me/orders`
- **THEN** every row in the response SHALL have `ownerId=2`

#### Scenario: GET /api/me/tradeLogs lists only caller trade logs
- **WHEN** a user with id=2 calls `GET /api/me/tradeLogs`
- **THEN** every row in the response SHALL have `ownerId=2`

#### Scenario: GET /api/me/profile returns the JWT subject plus collect settings
- **WHEN** a user with id=2, username "tranhuutho", `collectThreshold=2000000`, `collectKeep=500000` calls `GET /api/me/profile`
- **THEN** the response SHALL contain `{id: 2, username: "tranhuutho", role: "USER", collectThreshold: 2000000, collectKeep: 500000}`

#### Scenario: GET /api/me/stats counts only caller resources
- **WHEN** a user with id=2 calls `GET /api/me/stats`
- **THEN** every count (bots, orders, trade logs) SHALL include only rows where `ownerId=2`

#### Scenario: PUT /api/me/profile updates threshold and keep
- **WHEN** a user calls `PUT /api/me/profile` with body `{collectThreshold: 3000000, collectKeep: 100000}`
- **THEN** the row SHALL be updated and the response SHALL reflect the new values

#### Scenario: PUT /api/me/profile rejects threshold <= keep
- **WHEN** a user calls `PUT /api/me/profile` with body `{collectThreshold: 100, collectKeep: 100}`
- **THEN** the response SHALL be `400 Bad Request` and the row SHALL be unchanged

#### Scenario: PUT /api/me/profile rejects negative values
- **WHEN** a user calls `PUT /api/me/profile` with body `{collectKeep: -5}`
- **THEN** the response SHALL be `400 Bad Request` and the row SHALL be unchanged

#### Scenario: PUT /api/me/profile cannot change role or username
- **WHEN** a USER calls `PUT /api/me/profile` with body `{role: "ADMIN", username: "x"}`
- **THEN** the response SHALL be `400 Bad Request`, and the caller's `role` and `username` SHALL be unchanged

### Requirement: /api/me/blocks SHALL allow per-owner CRUD
`/api/me/blocks` SHALL expose `GET`, `POST`, and `DELETE /:id` only. Reads and deletes SHALL filter by `ownerId = req.user.sub`; creates SHALL stamp the caller as owner and SHALL honor the `(ownerId, serverId, name)` uniqueness constraint.

#### Scenario: Two users can block the same nickname independently
- **WHEN** user A and user B each call `POST /api/me/blocks` with `(serverId=5, name='foo')`
- **THEN** both calls SHALL succeed with `200` and create separate rows with their respective `ownerId`

#### Scenario: Duplicate block under the same owner is rejected
- **WHEN** a user calls `POST /api/me/blocks` with `(serverId=5, name='foo')` after already creating that same `(serverId, name)`
- **THEN** the response SHALL be `400` and no duplicate row SHALL be created

#### Scenario: DELETE rejects another user's block id
- **WHEN** user A calls `DELETE /api/me/blocks/:id` where the block belongs to user B
- **THEN** the response SHALL be `404 Not Found` and the block SHALL remain

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
