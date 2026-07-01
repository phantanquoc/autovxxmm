# client-scoping Specification

## Purpose
TBD - created by archiving change add-per-user-tenant-ownership. Update Purpose after archive.
## Requirements
### Requirement: Java-client bot routes SHALL filter by caller ownership
`GET /api/client/bots/:mode`, `GET /api/client/bots/:mode/check-update`, `GET /api/client/bots/:mode/new`, `GET /api/client/bots/:mode/updated`, `GET /api/client/bots/:mode/deleted`, `GET /api/client/bots/:mode/changed/new`, `GET /api/client/bots/:mode/changed/delete`, `PUT /api/client/bots/:mode`, and `PUT /api/client/bots/:mode/exit` SHALL include `ownerId = req.user.sub` in every WHERE clause they execute against `Bot` and related tables. Reassignment and observer-push operations SHALL be limited to rows owned by the caller.

#### Scenario: Java client logged in as user A only sees its own bots
- **WHEN** the Java client authenticates as user A and calls `GET /api/client/bots/normal`
- **THEN** the response SHALL contain only `Bot` rows with `ownerId = A` and `deletedAt IS NULL`

#### Scenario: check-update never reports another user's bots
- **WHEN** the Java client authenticates as user A and calls `GET /api/client/bots/normal/check-update`
- **THEN** the booleans SHALL be computed only against `Bot` rows where `ownerId = A`

#### Scenario: Observer push cannot update another user's bots
- **WHEN** the Java client authenticates as user A and `PUT /api/client/bots/normal` includes an `id` whose `Bot.ownerId != A`
- **THEN** that bot row SHALL NOT be updated (the `updateMany` predicate SHALL include `ownerId = A`)

#### Scenario: Existing admin Java sessions keep working
- **WHEN** an ADMIN-role JWT is used by the Java client and the existing 4 bots have `ownerId = 1`
- **THEN** `GET /api/client/bots/normal` SHALL return those same 4 bots and `check-update` polling SHALL keep returning HTTP 200

### Requirement: Java-client order routes SHALL stamp and filter by owner
`POST /api/client/orders/create` SHALL set `Order.ownerId = req.user.sub` on creation. `GET /api/client/orders/check-update` and the order-state mutation endpoints (`PUT /api/client/orders/bet|lose|win|reward|error|log`) SHALL include `ownerId = req.user.sub` in their WHERE clauses so a user cannot read or alter another user's orders.

#### Scenario: Created order belongs to the caller
- **WHEN** user A calls `POST /api/client/orders/create` with valid payload
- **THEN** the new `Order` row SHALL have `ownerId = A`, regardless of any explicit `ownerId` in the request body

#### Scenario: check-update is scoped per owner
- **WHEN** user A calls `GET /api/client/orders/check-update`
- **THEN** the returned `ordersUpdateStatus` and `ordersDelete` arrays SHALL only contain orders with `ownerId = A`

#### Scenario: State mutation cannot affect another user's order
- **WHEN** user A calls `PUT /api/client/orders/bet|lose|win|reward|error|log` with an order id whose `ownerId != A`
- **THEN** the `updateMany` SHALL match zero rows and no Order SHALL be modified

### Requirement: Java-client trade-log route SHALL stamp the caller as owner
`PUT /api/log/trade` SHALL set `TradeLog.ownerId = req.user.sub` when inserting the new trade log. The route SHALL NOT trust any client-supplied `ownerId`.

#### Scenario: New trade log carries caller's owner
- **WHEN** user A calls `PUT /api/log/trade` with a valid payload
- **THEN** the new `TradeLog` row SHALL have `ownerId = A`

### Requirement: Server resource list SHALL remain shared
`GET /api/resource/server` SHALL keep its current shape (returning all servers) and SHALL NOT be filtered by `ownerId`, because the in-game server list is configuration shared by all users.

#### Scenario: Any user fetches the full server list
- **WHEN** any authenticated user calls `GET /api/resource/server`
- **THEN** the response SHALL include all `Server` rows ordered by id ascending

### Requirement: Java-client collector bot route SHALL filter by caller ownership
`GET /api/client/bots/collect` SHALL include `ownerId = req.user.sub AND role = 'COLLECT' AND deletedAt IS NULL` in its WHERE clause and SHALL return the same JSON shape as `GET /api/client/bots/normal`.

#### Scenario: Java client logged in as user A only sees its own collectors
- **WHEN** the Java client authenticates as user A and calls `GET /api/client/bots/collect`
- **THEN** the response SHALL contain only `Bot` rows where `ownerId = A AND role = 'COLLECT' AND deletedAt IS NULL`

#### Scenario: Response shape matches the normal endpoint
- **WHEN** a Java client compares the response shape of `GET /api/client/bots/normal` and `GET /api/client/bots/collect`
- **THEN** both endpoints SHALL return bots with the same field set and the same JSON types

### Requirement: Java-client collect-task routes SHALL stamp and filter by caller
`GET /api/client/collect/check-update`, `GET /api/client/collect/pending`, and `POST /api/client/collect/ack` SHALL all include `ownerId = req.user.sub` in their WHERE / `updateMany` clauses against `CollectTask`. `GET /api/client/collect/pending` SHALL atomically move the oldest caller-owned PENDING task to IN_PROGRESS using a conditional update (`updateMany WHERE id = ? AND status = 0`) and return 404 if the conditional update matched zero rows. `POST /api/client/collect/ack` SHALL set the task to DONE or FAILED only when the caller owns it, and SHALL stamp `ownerId = req.user.sub` on every `TradeLog` it inserts.

#### Scenario: check-update never reports another user's task
- **WHEN** user A's Java client calls `GET /api/client/collect/check-update` and user B owns a PENDING task
- **THEN** user A SHALL get `{ hasPending: false, taskId: null }`

#### Scenario: pending refuses another user's task id
- **WHEN** user A's Java client calls `GET /api/client/collect/pending` and only user B owns PENDING tasks
- **THEN** the response SHALL be `404 Not Found`

#### Scenario: ack rejects another user's task
- **WHEN** user A's Java client posts ack for a `taskId` whose `ownerId = B`
- **THEN** the response SHALL be `404 Not Found` and the row SHALL be unchanged

#### Scenario: ack-created TradeLog rows carry the caller as owner
- **WHEN** user A's Java client posts an ack that triggers TradeLog inserts
- **THEN** every new `TradeLog` row SHALL have `ownerId = A`
