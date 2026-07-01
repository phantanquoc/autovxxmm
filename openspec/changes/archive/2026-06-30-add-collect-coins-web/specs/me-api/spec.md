## MODIFIED Requirements

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
