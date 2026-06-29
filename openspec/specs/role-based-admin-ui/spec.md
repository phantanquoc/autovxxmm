# role-based-admin-ui Specification

## Purpose
TBD - created by archiving change add-per-user-tenant-ownership. Update Purpose after archive.
## Requirements
### Requirement: AuthGuard SHALL admit any authenticated session
The frontend `AuthGuard` SHALL admit any user whose JWT exists in `localStorage` and is not expired, regardless of role. The previous behavior of rejecting non-ADMIN tokens with `AccessDenied` SHALL be removed.

#### Scenario: USER session enters the admin SPA
- **WHEN** a JWT with `role='USER'` is in `localStorage` and not expired
- **THEN** `AuthGuard` SHALL render its children (the `AppLayout`) and SHALL NOT redirect to login or show `AccessDenied`

#### Scenario: Missing or expired token redirects to login
- **WHEN** there is no token in `localStorage`, or the token's `exp` is in the past
- **THEN** `AuthGuard` SHALL redirect to `/login`

### Requirement: Admin-only routes SHALL be protected by an explicit guard
An `AdminOnlyRoute` wrapper SHALL protect routes that only ADMIN tokens may access (at minimum `/users` and `/servers`). USER-role tokens that hit those routes SHALL be redirected back to `/` (Dashboard) instead of being shown the page.

#### Scenario: USER cannot open /users
- **WHEN** a USER-role session navigates to `#/users`
- **THEN** the router SHALL redirect to `#/` without rendering the user list

#### Scenario: USER cannot open /servers
- **WHEN** a USER-role session navigates to `#/servers`
- **THEN** the router SHALL redirect to `#/` without rendering the server list

#### Scenario: ADMIN can open both
- **WHEN** an ADMIN-role session navigates to `#/users` or `#/servers`
- **THEN** the corresponding page SHALL render

### Requirement: Sidebar SHALL render a role-specific menu set
The sidebar SHALL show different items depending on the JWT role of the active session.

For `role='ADMIN'`:
- Main: Dashboard
- Manage: Bot, Đơn hàng, Giao dịch xu, Block list
- System: Máy chủ, Người dùng, Cài đặt

For any non-ADMIN role (USER):
- Main: Dashboard
- Manage: Bot, Đơn hàng, Giao dịch xu, Block list
- System: Cài đặt

#### Scenario: ADMIN sidebar shows admin-only entries
- **WHEN** an ADMIN session renders the sidebar
- **THEN** "Máy chủ" and "Người dùng" SHALL appear under "Hệ thống"

#### Scenario: USER sidebar hides admin-only entries
- **WHEN** a USER session renders the sidebar
- **THEN** "Máy chủ" and "Người dùng" SHALL NOT appear; the rest of the menu structure SHALL match the role-specific list above

### Requirement: Feature hooks SHALL target the correct API base by role
The data hooks for `bots`, `orders`, `trade-logs`, `blocks`, and the `dashboard` stats SHALL pick `/api/admin/<resource>` when the current JWT has `role='ADMIN'` and `/api/me/<resource>` otherwise. The same hooks SHALL pass ADMIN-only `ownerId` filters into admin requests when the UI sets them.

#### Scenario: ADMIN hooks call admin endpoints
- **WHEN** an ADMIN session opens BotList
- **THEN** the underlying hook SHALL request `/api/admin/bots` (optionally with `ownerId` from the filter)

#### Scenario: USER hooks call me endpoints
- **WHEN** a USER session opens BotList
- **THEN** the underlying hook SHALL request `/api/me/bots` and SHALL NOT include any `ownerId` filter parameter

### Requirement: Login page SHALL admit non-ADMIN tokens and link to /register
The login page SHALL accept any returned JWT (drop the role gate that previously rejected non-ADMIN logins) and SHALL replace the placeholder "Đăng ký" toast with a router link to `/register`.

#### Scenario: USER can log in through the login form
- **WHEN** a USER-role account submits valid credentials on `/login`
- **THEN** the SPA SHALL store the JWT and navigate to `/` without showing "Tài khoản không có quyền admin"

#### Scenario: "Đăng ký" link goes to /register
- **WHEN** the visitor clicks the "Đăng ký!" affordance on `/login`
- **THEN** the SPA SHALL route to `#/register`

