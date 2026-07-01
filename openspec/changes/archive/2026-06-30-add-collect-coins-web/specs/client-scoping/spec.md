## ADDED Requirements

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
