## Why

The backend currently has no concept of data ownership: `Bot`, `Order`, `TradeLog`, and `Block` are shared pools that any ADMIN can see, while non-ADMIN users are hard-blocked by the frontend `AuthGuard`. There is no path for a regular user to operate the tool with their own data, no public registration, and the Java client cannot scope its bot pool to the user who logged in.

This change introduces a true two-role tenancy model so each user owns their data, ADMIN steps back to a supervisory role, and the system is ready for self-registration.

## What Changes

- **BREAKING (data model)**: add a non-nullable `ownerId` foreign key on `Bot`, `Order`, `TradeLog`, `Block`. Existing rows are backfilled to admin (id=1) during migration.
- **BREAKING (Block uniqueness)**: change the `Block` unique constraint from `(serverId, name)` to `(ownerId, serverId, name)` so two users can independently block the same in-game nickname.
- Add new public endpoint `POST /api/register` (zod-validated, bcrypt-hashed, creates `role=USER`, returns JWT immediately).
- Add a new `/api/me/*` endpoint group (profile, bots, orders, trade-logs, blocks, stats) where the backend forces `ownerId = req.user.sub` on every query and mutation.
- Modify `/api/admin/*` to become read-only for tenant data (bots, orders, trade-logs, blocks) with an optional `?ownerId=N` filter for ADMIN supervision. `users` and `servers` remain full CRUD.
- Modify `/api/client/*` (Java-client routes: `routes/bots.ts`, `routes/orders.ts`, `routes/log.ts`) so every read filters by `ownerId = req.user.sub` and every write stamps that owner.
- Add new Prisma indexes tuned for the per-owner query paths (composite `(ownerId, ...)` indexes on bots/orders/trade-logs).
- Frontend `AuthGuard` no longer hard-rejects non-ADMIN users; an `AdminOnlyRoute` wrapper protects admin-only pages (`/users`, `/servers`).
- New `RegisterPage` at `/register` (public route, outside `AuthGuard`).
- `Sidebar` renders one of two menu sets based on the current JWT role; admin list-pages (Bot/Order/Trade/Block/Dashboard) gain a "Người dùng" filter dropdown.
- Per-feature hooks (`useBots`, `useOrders`, `useTradeLogs`, `useBlocks`, dashboard) auto-switch between `/api/admin/*` and `/api/me/*` based on the decoded JWT role.

## Capabilities

### New Capabilities
- `tenant-ownership`: data-model invariants and migration that turn `Bot`, `Order`, `TradeLog`, `Block` into per-user resources owned by a `User`.
- `auth-registration`: public self-service registration endpoint and frontend flow that issues a JWT for the newly created USER.
- `me-api`: authenticated `/api/me/*` endpoint group that scopes every read and write to the caller's `ownerId`.
- `admin-supervision`: read-only `/api/admin/*` view over tenant data with an optional cross-user `ownerId` filter, plus user/server CRUD.
- `client-scoping`: Java-client endpoints (`/api/client/*`) that filter and stamp ownership based on the authenticated user.
- `role-based-admin-ui`: admin SPA route guards, sidebar, and feature hooks that adapt to ADMIN vs USER role.

### Modified Capabilities
<!-- None — openspec/specs/ is empty, no prior capabilities to amend. -->

## Impact

- **Database (PostgreSQL via Prisma)**: schema migration adds 4 NOT NULL FK columns and a new unique constraint on `Block`; existing rows are backfilled to `ownerId=1`. Migration must be wrapped with a `pg_dump` backup first.
- **Backend routes** (`backend/src/`): modifies `routes/auth.ts`, `routes/bots.ts`, `routes/orders.ts`, `routes/log.ts`, `routes/admin/{bots,orders,tradeLogs,blocks,stats}.ts`; introduces `routes/me/{profile,bots,orders,tradeLogs,blocks,stats}.ts` and `lib/scope.ts`; updates `app.ts` to mount `/api/me`.
- **Frontend (admin SPA)** (`admin/src/`): modifies `features/auth/{AuthGuard,LoginPage}.tsx`, `routes.tsx`, `components/layout/Sidebar.tsx`, hooks in `features/{bots,orders,trade-logs,blocks,dashboard}/use*.ts`, and list pages in those features; introduces `features/auth/RegisterPage.tsx` and an `AdminOnlyRoute` component.
- **External clients**: the Java tool client does **not** need to be rebuilt. Backend filtering by JWT preserves the existing request/response contract.
- **Out of scope (intentionally deferred)**: password reset/forgot, email verification, `/register` rate-limiting, admin audit log, password change UI for end users.
