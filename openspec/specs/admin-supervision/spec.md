# admin-supervision Specification

## Purpose
TBD - created by archiving change add-per-user-tenant-ownership. Update Purpose after archive.
## Requirements
### Requirement: Admin tenant-data routes SHALL be read-only
`/api/admin/bots`, `/api/admin/orders`, `/api/admin/tradeLogs`, and `/api/admin/blocks` SHALL expose only read verbs (`GET` list and `GET /:id` where applicable). Any attempt to POST, PUT, or DELETE tenant data through `/api/admin/*` SHALL return `404` or `405` (mounted handler returns nothing).

#### Scenario: Admin cannot create or modify a Bot via admin routes
- **WHEN** an ADMIN token calls `POST /api/admin/bots`, `PUT /api/admin/bots/:id`, or `DELETE /api/admin/bots/:id`
- **THEN** the response SHALL NOT be `200`, and no `Bot` row SHALL be created, updated, or soft-deleted

#### Scenario: Admin cannot delete a tenant order
- **WHEN** an ADMIN token calls `DELETE /api/admin/orders/:id`
- **THEN** the response SHALL NOT be `200`, and no `Order` row SHALL be affected

#### Scenario: Admin cannot create or delete blocks via admin routes
- **WHEN** an ADMIN token calls `POST /api/admin/blocks` or `DELETE /api/admin/blocks/:id`
- **THEN** the response SHALL NOT be `200`, and no `Block` row SHALL be affected

### Requirement: Admin tenant-data list endpoints SHALL accept an optional ownerId filter
`GET /api/admin/bots`, `GET /api/admin/orders`, `GET /api/admin/tradeLogs`, `GET /api/admin/blocks`, and `GET /api/admin/stats` SHALL accept an optional `ownerId` filter (compatible with the existing list-query parser). When omitted, results SHALL span all owners. When provided as a positive integer, results SHALL be filtered to that owner.

#### Scenario: No ownerId filter shows all bots
- **WHEN** an ADMIN calls `GET /api/admin/bots` without `ownerId`
- **THEN** the response SHALL include bots from every owner

#### Scenario: ownerId filter narrows the result
- **WHEN** an ADMIN calls `GET /api/admin/bots` with `ownerId=2`
- **THEN** every row in the response SHALL have `ownerId=2`

#### Scenario: ownerId filter applies to stats counts
- **WHEN** an ADMIN calls `GET /api/admin/stats?ownerId=2`
- **THEN** every count in the response SHALL include only rows owned by user id=2

#### Scenario: Invalid ownerId is ignored or rejected, never bypassed
- **WHEN** an ADMIN sends `ownerId` that is not a positive integer (e.g. `abc`, `-1`)
- **THEN** the response SHALL either fall back to "no owner filter" or return `400`, but SHALL NOT cause a query that errors or leaks data unintentionally

### Requirement: Admin user/server CRUD SHALL be preserved
`/api/admin/users` and `/api/admin/servers` SHALL continue to support full CRUD (GET list, GET :id, POST, PUT, DELETE) for ADMIN tokens only.

#### Scenario: ADMIN can create a new user
- **WHEN** an ADMIN calls `POST /api/admin/users` with a valid payload
- **THEN** the user SHALL be created and returned with the same shape as before this change

#### Scenario: Non-ADMIN cannot reach admin user routes
- **WHEN** a USER token is presented to any `/api/admin/users` route
- **THEN** the response SHALL be `403 Forbidden`

### Requirement: Frontend admin pages SHALL expose a user filter
When the active JWT has `role='ADMIN'`, the BotList, OrderList, TradeLogList, BlockList, and Dashboard pages SHALL render a "Người dùng" (user) filter dropdown sourced from `/api/admin/users`. Selecting a user SHALL pass `ownerId` to the underlying API call; selecting "Tất cả" SHALL omit the filter. USER-role sessions SHALL NOT see this dropdown.

#### Scenario: ADMIN sees a user filter on tenant pages
- **WHEN** an ADMIN navigates to BotList, OrderList, TradeLogList, BlockList, or Dashboard
- **THEN** a "Người dùng" dropdown SHALL be visible above the data

#### Scenario: USER does not see the user filter
- **WHEN** a USER navigates to the same pages
- **THEN** the "Người dùng" dropdown SHALL NOT render

#### Scenario: Filter selection drives the API call
- **WHEN** an ADMIN selects user "tranhuutho" (id=2) in the filter on BotList
- **THEN** the page SHALL refetch with `ownerId=2` and SHALL show only bots owned by id=2

