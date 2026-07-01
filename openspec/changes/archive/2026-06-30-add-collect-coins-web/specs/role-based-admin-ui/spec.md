## MODIFIED Requirements

### Requirement: Sidebar SHALL render a role-specific menu set
The sidebar SHALL show different items depending on the JWT role of the active session.

For `role='ADMIN'`:
- Main: Dashboard
- Manage: Bot, Bot gom xu, Đơn hàng, Giao dịch xu, Block list
- System: Máy chủ, Người dùng, Cài đặt

For any non-ADMIN role (USER):
- Main: Dashboard
- Manage: Bot, Bot gom xu, Đơn hàng, Giao dịch xu, Block list
- System: Cài đặt

#### Scenario: ADMIN sidebar shows admin-only entries
- **WHEN** an ADMIN session renders the sidebar
- **THEN** "Máy chủ" and "Người dùng" SHALL appear under "Hệ thống"

#### Scenario: USER sidebar hides admin-only entries
- **WHEN** a USER session renders the sidebar
- **THEN** "Máy chủ" and "Người dùng" SHALL NOT appear; the rest of the menu structure SHALL match the role-specific list above

#### Scenario: Bot gom xu entry is visible to both roles
- **WHEN** any authenticated session (USER or ADMIN) renders the sidebar
- **THEN** an entry labelled "Bot gom xu" SHALL appear under "Quản lý" / "Manage", linking to `#/collect-bots`
