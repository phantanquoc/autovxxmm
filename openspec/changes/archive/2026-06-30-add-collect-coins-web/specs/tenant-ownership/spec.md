## MODIFIED Requirements

### Requirement: Tenant resources MUST be owned by a User
Every `Bot`, `Order`, `TradeLog`, `Block`, and `CollectTask` row SHALL be linked to exactly one row in `User` through a non-nullable `ownerId` foreign key with `ON DELETE RESTRICT`. The system MUST reject any insert that does not specify a valid owner.

#### Scenario: Insert with missing owner is rejected
- **WHEN** a row is inserted into `Bot`, `Order`, `TradeLog`, `Block`, or `CollectTask` without an `ownerId`
- **THEN** the database SHALL reject the insert with a NOT NULL violation

#### Scenario: Insert with non-existent owner is rejected
- **WHEN** a row is inserted with `ownerId` pointing to a user id that does not exist
- **THEN** the database SHALL reject the insert with a foreign-key violation

#### Scenario: Deleting an owner with existing tenant data is blocked
- **WHEN** a `DELETE FROM "User"` is attempted on an id that owns any Bot/Order/TradeLog/Block/CollectTask row
- **THEN** the delete SHALL fail with a foreign-key constraint error (RESTRICT)

## ADDED Requirements

### Requirement: Bot role and per-owner role index MUST be in place
`Bot` SHALL carry a non-null `role` column of enum `BotRole { ORDER, COLLECT }` defaulting to `'ORDER'`. The composite index `Bot(ownerId, role, deletedAt)` SHALL exist so that per-owner role-filtered queries (e.g., `WHERE ownerId = ? AND role = 'COLLECT' AND deletedAt IS NULL`) use an Index Scan.

#### Scenario: Bot role column is NOT NULL after migration
- **WHEN** the migration completes against a database that contained existing bots
- **THEN** every `Bot` row SHALL have `role = 'ORDER'` and a query selecting `WHERE role IS NULL` SHALL return zero rows

#### Scenario: Per-owner role index is used
- **WHEN** `EXPLAIN ANALYZE SELECT * FROM "Bot" WHERE "ownerId" = 1 AND "role" = 'COLLECT' AND "deletedAt" IS NULL;` is executed against a populated database
- **THEN** the plan SHALL show an `Index Scan` or `Bitmap Index Scan` using `Bot_ownerId_role_deletedAt_idx`

### Requirement: User settings MUST carry default collect threshold and keep
`User` SHALL gain two non-null integer columns `collectThreshold` (default `1000000`) and `collectKeep` (default `0`). The migration SHALL backfill defaults for every pre-existing user.

#### Scenario: Pre-existing users get defaults
- **WHEN** the migration runs against a database that contains existing users
- **THEN** every existing `User` row SHALL have `collectThreshold = 1000000` and `collectKeep = 0`

### Requirement: CollectTask indexes MUST be created
The migration SHALL create the index `CollectTask(ownerId, status, createdAt)` so that the polling endpoint `GET /api/client/collect/check-update` can pick the oldest PENDING task in O(log N).

#### Scenario: CollectTask polling uses the index
- **WHEN** `EXPLAIN ANALYZE SELECT * FROM "CollectTask" WHERE "ownerId" = 1 AND "status" = 0 ORDER BY "createdAt" ASC LIMIT 1;` is executed
- **THEN** the plan SHALL show an `Index Scan` using `CollectTask_ownerId_status_createdAt_idx`
