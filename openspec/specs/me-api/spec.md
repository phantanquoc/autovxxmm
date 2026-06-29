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
`/api/me/orders`, `/api/me/tradeLogs`, `/api/me/profile`, and `/api/me/stats` SHALL be read-only relative to their resources (no mutating verbs) and SHALL filter strictly to the caller's `ownerId`. `/api/me/profile` SHALL return the caller's `{id, username, role}` derived from the verified JWT.

#### Scenario: GET /api/me/orders lists only caller orders
- **WHEN** a user with id=2 calls `GET /api/me/orders`
- **THEN** every row in the response SHALL have `ownerId=2`

#### Scenario: GET /api/me/tradeLogs lists only caller trade logs
- **WHEN** a user with id=2 calls `GET /api/me/tradeLogs`
- **THEN** every row in the response SHALL have `ownerId=2`

#### Scenario: GET /api/me/profile returns the JWT subject
- **WHEN** a user with id=2 and username "tranhuutho" calls `GET /api/me/profile`
- **THEN** the response SHALL contain `{id: 2, username: "tranhuutho", role: "USER"}`

#### Scenario: GET /api/me/stats counts only caller resources
- **WHEN** a user with id=2 calls `GET /api/me/stats`
- **THEN** every count (bots, orders, trade logs) SHALL include only rows where `ownerId=2`

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

