## Context

The VXMM stack today is single-tenant by accident:

- `backend/prisma/schema.prisma` defines `Bot`, `Order`, `TradeLog`, `Block` with no link to `User`. Only `ClientSyncState` references `userId`, and that is just bookkeeping for the per-client sync deltas.
- `backend/src/routes/admin/*` apply `authRequired` + `adminRequired` and operate on the global pool. There is no notion of "the admin's bots" vs "another user's bots".
- `backend/src/routes/bots.ts`, `orders.ts`, and `log.ts` are the endpoints the Java client polls. They filter by `client (0..10)` for split-mode but never by user.
- `admin/src/features/auth/AuthGuard.tsx` rejects any JWT whose `role !== 'ADMIN'` with an `AccessDenied` screen, so USER-role accounts have no path into the SPA today.
- The DB is small: 2 users (`admin` id=1, `tranhuutho` id=2), 4 bots, 2 orders, 1 trade log, 15 servers, 0 blocks, all logically owned by admin.

The user wants three things at the same time:
1. Each user should have its own data (tenant isolation).
2. Admins should still be able to *see* tenant data for supervision (filterable by user), but not mutate it.
3. New users should be able to sign themselves up at `/register` and use the system immediately.

The Java client is in production. Rebuilding it is undesirable. The backend must take responsibility for owner-scoping; the client just continues sending the same HTTP requests with its existing JWT.

## Goals / Non-Goals

**Goals**
- Strict per-user ownership at the database layer for `Bot`, `Order`, `TradeLog`, `Block` (NOT NULL FK to `User`, RESTRICT on delete).
- Three clear endpoint groups, each with a single, predictable scoping rule:
  - `/api/admin/*` — ADMIN only, read-only over tenant data, full CRUD for users/servers.
  - `/api/me/*` — any authenticated user, force `ownerId = req.user.sub`.
  - `/api/client/*` — Java-tool routes, same forced scoping as `/api/me/*` but keeping the existing request/response contract.
- Public `POST /api/register` that produces an enabled USER and returns a JWT.
- Frontend: role-aware sidebar, admin-only route guard, public `/register` page, per-resource hooks that switch base path by role.
- Performance: composite indexes that cover the hot per-owner queries (Bot list, Bot per-client list, Order check-update, Order status counts, TradeLog feed) so a future "many users with many bots" workload stays fast.
- Backwards compatibility for the Java client: no rebuild, same payloads, same status codes.

**Non-Goals**
- Password reset / forgot-password flows.
- Email verification or any anti-abuse for `/register` (the user has accepted this risk; the deployment is private/self-hosted).
- Rate-limiting (also accepted out of scope).
- An admin audit log of "which admin looked at which user's data".
- A password-change form for end users in the SPA (admin form is sufficient for now).
- Multi-owner sharing, team workspaces, or role hierarchies beyond ADMIN/USER.

## Decisions

### D1. Ownership lives on the leaf tables, not on a wrapper "Tenant"
We add `ownerId` directly to `Bot`, `Order`, `TradeLog`, `Block`. We do **not** introduce a separate `Tenant` table.

- *Why*: there is exactly one tenant per user, and the user table is already the natural identity. A `Tenant` table would be a synonym with no extra columns to justify the join.
- *Alternative considered*: a `Tenant(id, userId)` table with `tenantId` on each resource. Rejected — extra join for every query, no near-term need (no plan for team/group ownership).

### D2. `Order` and `TradeLog` get their own `ownerId` column (denormalized, not via Bot)
Even though every order is created against a bot, `Order` and `TradeLog` carry their own `ownerId` field.

- *Why*: it keeps every per-user filter to a single index lookup, no join. The hot path is `WHERE ownerId = ? AND ...`; joining `Order JOIN Bot` adds a needless dependency and breaks if a bot is hard-deleted in the future.
- *Trade-off*: a future "bot reassigned to a different owner" feature would need to backfill `Order.ownerId` along with `Bot.ownerId`. We accept that — bot reassignment between users is not a planned operation.

### D3. `Block` uniqueness is per-owner
`@@unique([ownerId, serverId, name])` replaces `@@unique([serverId, name])`.

- *Why*: two users blocking the same game nickname on the same game server is a legitimate, expected scenario — they each maintain a personal blocklist. With the old constraint, the second user would get a unique-violation.
- *Note*: this is technically a breaking change to the constraint shape; safe because the table is empty in the current DB.

### D4. Migration is a 4-step Prisma migration, with explicit backfill
The migration runs as one Prisma migration whose generated SQL is hand-edited to:
1. `ALTER TABLE ... ADD COLUMN "ownerId" INTEGER NULL;`
2. `UPDATE "Bot" SET "ownerId" = 1; UPDATE "Order" SET "ownerId" = 1; UPDATE "TradeLog" SET "ownerId" = 1; UPDATE "Block" SET "ownerId" = 1;`
3. `ALTER TABLE ... ALTER COLUMN "ownerId" SET NOT NULL; ALTER TABLE ... ADD CONSTRAINT ... FOREIGN KEY ("ownerId") REFERENCES "User"("id") ON DELETE RESTRICT;`
4. `CREATE INDEX ...` for the composite indexes; `DROP CONSTRAINT` + `ADD CONSTRAINT` for the new Block uniqueness.

- *Why*: `prisma migrate` defaults to "add NOT NULL with default" if you don't customize, which is wrong (we don't want a default; we want a backfill). Editing the SQL keeps `prisma migrate deploy` deterministic.
- *Pre-step*: `docker exec vxmm-pg pg_dump -U vxmm vxmm > backup.sql` is run by `apply` before invoking the migration, so any failure is recoverable.

### D5. JWT shape stays the same
We keep `{sub, username, role}` in the JWT and pull `ownerId = req.user.sub` everywhere. We do **not** add a separate `ownerId` claim.

- *Why*: `sub` already identifies the user. A duplicate claim creates two sources of truth.
- *Trade-off*: registration must produce JWTs with the same claim shape as login. We centralize via the existing `signJwt` helper.

### D6. Scope helper lives in `backend/src/lib/scope.ts`
Two functions:
- `getOwnerScope(req)` — reads `req.user.sub`, throws if missing. Used by every `/api/me/*` and `/api/client/*` handler.
- `parseAdminOwnerFilter(req)` — pulls the optional `ownerId` filter for admin list pages, validates as a positive integer, returns `number | undefined`.

- *Why*: a single source of truth so a future audit ("does every query filter by owner?") is a grep for these helpers.
- *Alternative considered*: Express middleware that attaches `req.ownerScope`. Rejected — handlers already pull from `req.user`, a function call is just as terse and easier to reason about.

### D7. Java client compatibility is achieved by silently scoping on the backend
Endpoints under `/api/client/*` continue to accept the exact same payloads. Where the payload included an `ownerId`, we ignore the client's value and override with `req.user.sub`. Where the payload did not, we inject it. The response shape is unchanged.

- *Why*: the Java jar is binary and rebuilding is the user's pain. Silent scoping is invisible to the client.
- *Trade-off*: there is no way for the Java client to operate "as another user". That's the desired property here.

### D8. Admin tenant routes lose mutating verbs
`POST/PUT/DELETE` are removed from `admin/bots.ts`, `admin/orders.ts`, `admin/blocks.ts`. `admin/users.ts` and `admin/servers.ts` keep full CRUD.

- *Why*: the locked decision is "admin is supervisory, not operational". Keeping the routes would invite "admin override" creep.
- *Alternative considered*: keep the routes but add a `?force=1` super-admin override. Rejected — yagni, and it creates a place where the ownership invariant can be skipped.

### D9. Frontend hooks branch on the decoded JWT role
Each feature's `useXxx.ts` reads the role via `getCurrentUser()?.role` and chooses `/api/admin/<resource>` or `/api/me/<resource>` accordingly. The page components stay role-agnostic for the *data shape*, but conditionally render the admin user-filter dropdown.

- *Why*: reuses the existing UI components (DataTable, columns, dialogs). The only branching is "which URL" and "show extra filter for admin".
- *Alternative considered*: two parallel sets of pages (`AdminBotListPage` vs `MeBotListPage`). Rejected — duplicates UI for no real difference.

### D10. AdminOnlyRoute is a small wrapper, not a router-level matcher
`AdminOnlyRoute` is a component that reads the JWT, redirects USER tokens to `/`, and renders children for ADMIN. It wraps individual route elements in `routes.tsx`.

- *Why*: works inside `createHashRouter` without adding a custom loader/error layer; keeps the role check colocated with the route definition.
- *Alternative considered*: a separate routes-config branch per role. Rejected — more code, harder to see at a glance which routes are admin-only.

### D11. Composite indexes are tuned for the actual query shapes seen in the code
- `Bot(ownerId, deletedAt)` — admin/me bot list (`WHERE ownerId = ? AND deletedAt IS NULL`).
- `Bot(ownerId, client, deletedAt)` — Java client split-mode (`WHERE ownerId = ? AND client = ? AND deletedAt IS NULL`).
- `Order(ownerId, status, deletedAt)` — admin/me order list with status filter.
- `Order(ownerId, updatedAt)` — Java client `check-update` (`WHERE ownerId = ? AND updatedAt > ?`).
- `TradeLog(ownerId, createdAt)` — admin/me trade-log feed (sorted by createdAt desc).

The existing `(updatedAt)` index on Bot and the existing `(serverId, deletedAt)` index stay; they cover non-owner filter paths if we ever need them.

- *Why*: leading-column selectivity is `ownerId`, so every per-owner query benefits. We don't add `(serverId, ownerId, ...)` variants because no current query starts with serverId without also having ownerId.

## Risks / Trade-offs

- **[Risk] Migration silently leaves NULLs and then ALTER NOT NULL fails** → Mitigation: backfill (`UPDATE ... SET ownerId = 1`) runs **before** `ALTER ... SET NOT NULL` in the same migration. A pre-run `pg_dump` is taken so we can restore.
- **[Risk] A backend handler is missed during the refactor and continues to read/write data without owner scoping** → Mitigation: every owner-scoped operation MUST go through `getOwnerScope(req)` in `backend/src/lib/scope.ts`; verifier greps for missed references to `prisma.bot/order/tradeLog/block` that don't include `ownerId` in their where/data.
- **[Risk] Admin user filter on the frontend lets an admin accidentally mutate USER data** → Mitigation: admin routes have no mutating verbs at all (D8). UI never exposes Add/Edit/Delete buttons on admin tenant pages.
- **[Risk] Public `/register` is abuse-prone** → Mitigation accepted as out of scope per user decision; deployment is private/self-hosted. If exposed publicly later, add rate-limit and CAPTCHA as a follow-up change.
- **[Risk] Java client breaking** → Mitigation: contract is byte-compatible; the client never sends `ownerId` today, and any field it does send that we now override is dropped silently. Verification step explicitly polls the existing endpoints to confirm 200 OK.
- **[Risk] Block uniqueness change collides on existing data** → Mitigation: the `Block` table is empty in the current DB, verified before migration.
- **[Trade-off] Denormalized `ownerId` on Order/TradeLog** → Faster reads, slightly more responsibility on write paths. We accept this; see D2.

## Migration Plan

1. **Pre-flight**
   - `docker exec vxmm-pg pg_dump -U vxmm vxmm > backup.sql` (in the change directory or under `backend/`).
   - Stop the backend dev server (so Prisma client regeneration is clean). Frontend dev server can stay up.

2. **Schema**
   - Edit `backend/prisma/schema.prisma`: add `ownerId Int` + `owner User @relation(fields: [ownerId], references: [id], onDelete: Restrict)` on Bot/Order/TradeLog/Block; add reciprocal `bots Bot[]`, `orders Order[]`, `tradeLogs TradeLog[]`, `blocks Block[]` on `User`; add composite indexes; change `Block` `@@unique` to `[ownerId, serverId, name]`.
   - `npx prisma migrate dev --create-only --name per_user_tenant_ownership` (don't apply yet).
   - Edit the generated SQL: split the auto-generated NOT NULL into ADD-COLUMN-NULL, UPDATE backfill, ALTER NOT NULL, ADD FK, CREATE INDEX, DROP/ADD UNIQUE.

3. **Apply**
   - `npx prisma migrate dev` (now applies the edited migration).
   - `npx prisma generate` (regenerate client, picks up `ownerId` typings).
   - Sanity check: `SELECT "ownerId", count(*) FROM "Bot" GROUP BY 1;` → all rows on owner 1.

4. **Backend code**
   - Add `src/lib/scope.ts`.
   - Add `src/routes/me/*` and mount under `/api/me` in `app.ts`.
   - Update `src/routes/auth.ts` (POST /register).
   - Update `src/routes/bots.ts`, `orders.ts`, `log.ts` (Java client) to use `getOwnerScope`.
   - Update `src/routes/admin/{bots,orders,tradeLogs,blocks,stats}.ts` (remove mutating verbs, add ownerId filter).

5. **Frontend code**
   - Add `features/auth/RegisterPage.tsx`, register `/register` in `routes.tsx`.
   - Update `features/auth/AuthGuard.tsx` (drop role gate), add `AdminOnlyRoute`.
   - Update `features/auth/LoginPage.tsx` (drop role gate, link to /register).
   - Update `components/layout/Sidebar.tsx` (role-aware menu).
   - Update feature hooks (`bots/useBots.ts`, `orders/useOrders.ts`, `trade-logs/useTradeLogs.ts`, `blocks/useBlocks.ts`, dashboard hook).
   - Update list pages to render the admin "Người dùng" filter when role=ADMIN.

6. **Verification**
   - Manual: register a fresh user, log in, confirm USER-only sidebar, confirm `/users` redirects, confirm POST /api/me/bots stamps `ownerId`.
   - Manual: admin login, confirm filter dropdown changes Bot list, confirm Tool Java still polls successfully.
   - DB: `EXPLAIN ANALYZE` the per-owner Bot list query to confirm the composite index is used.

7. **Rollback**
   - Restore `backup.sql`: `docker exec -i vxmm-pg psql -U vxmm vxmm < backup.sql` (after `DROP DATABASE` / `CREATE DATABASE` if needed).
   - `git revert` the change branch; restart backend + frontend dev servers.
