## ADDED Requirements

### Requirement: Tenant resources MUST be owned by a User
Every `Bot`, `Order`, `TradeLog`, and `Block` row SHALL be linked to exactly one row in `User` through a non-nullable `ownerId` foreign key with `ON DELETE RESTRICT`. The system MUST reject any insert that does not specify a valid owner.

#### Scenario: Insert with missing owner is rejected
- **WHEN** a row is inserted into `Bot`, `Order`, `TradeLog`, or `Block` without an `ownerId`
- **THEN** the database SHALL reject the insert with a NOT NULL violation

#### Scenario: Insert with non-existent owner is rejected
- **WHEN** a row is inserted with `ownerId` pointing to a user id that does not exist
- **THEN** the database SHALL reject the insert with a foreign-key violation

#### Scenario: Deleting an owner with existing tenant data is blocked
- **WHEN** a `DELETE FROM "User"` is attempted on an id that owns any Bot/Order/TradeLog/Block row
- **THEN** the delete SHALL fail with a foreign-key constraint error (RESTRICT)

### Requirement: Migration MUST backfill existing rows to admin
The migration that introduces `ownerId` SHALL backfill every pre-existing row in `Bot`, `Order`, `TradeLog`, and `Block` with `ownerId = 1` before applying the NOT NULL constraint, so no data is lost or orphaned.

#### Scenario: Pre-existing rows are owned by admin after migration
- **WHEN** the migration runs against a database that contains existing tenant data
- **THEN** every existing row in Bot/Order/TradeLog/Block SHALL have `ownerId = 1` after migration completes

#### Scenario: Migration is wrapped by a pre-migration backup
- **WHEN** the migration is executed against the docker `vxmm-pg` container
- **THEN** a `pg_dump` backup SHALL be taken and written to a known path before the schema change

### Requirement: Per-owner indexes MUST be created
The migration SHALL create the following composite indexes to keep per-owner queries fast as data grows:
- `Bot(ownerId, deletedAt)`
- `Bot(ownerId, client, deletedAt)`
- `Order(ownerId, status, deletedAt)`
- `Order(ownerId, updatedAt)`
- `TradeLog(ownerId, createdAt)`

#### Scenario: Indexes are present after migration
- **WHEN** querying `pg_indexes` for the listed tables after migration
- **THEN** each composite index above SHALL exist

#### Scenario: Per-owner Bot list query uses the new index
- **WHEN** `EXPLAIN ANALYZE SELECT * FROM "Bot" WHERE "ownerId" = 1 AND "deletedAt" IS NULL;` is run
- **THEN** the plan SHALL show an `Index Scan` or `Bitmap Index Scan` using the new `Bot(ownerId, deletedAt)` index

### Requirement: Block uniqueness MUST be scoped per owner
The `Block` table's uniqueness constraint SHALL be `(ownerId, serverId, name)`, replacing the previous `(serverId, name)` constraint. Two different users SHALL be able to block the same in-game nickname on the same server independently.

#### Scenario: Two users block the same nickname on the same server
- **WHEN** user A inserts a Block row `(ownerId=A, serverId=5, name='foo')` and user B inserts `(ownerId=B, serverId=5, name='foo')`
- **THEN** both inserts SHALL succeed

#### Scenario: Same user cannot block the same nickname twice on the same server
- **WHEN** user A inserts two Block rows with `(ownerId=A, serverId=5, name='foo')`
- **THEN** the second insert SHALL fail with a unique-constraint violation
