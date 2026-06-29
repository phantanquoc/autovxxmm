## 1. Pre-flight & schema

- [x] 1.1 Take a Postgres backup: `docker exec vxmm-pg pg_dump -U vxmm vxmm > backend/backup-pre-tenant.sql`
- [x] 1.2 Stop the backend dev server before running migrations (frontend dev server can stay up)
- [x] 1.3 Update `backend/prisma/schema.prisma`:
  - [x] 1.3.1 Add `ownerId Int` + `owner User @relation(fields: [ownerId], references: [id], onDelete: Restrict)` on `Bot`, `Order`, `TradeLog`, `Block`
  - [x] 1.3.2 Add reciprocal collections on `User`: `bots Bot[]`, `orders Order[]`, `tradeLogs TradeLog[]`, `blocks Block[]`
  - [x] 1.3.3 Add composite indexes — Bot `(ownerId, deletedAt)` and `(ownerId, client, deletedAt)`; Order `(ownerId, status, deletedAt)` and `(ownerId, updatedAt)`; TradeLog `(ownerId, createdAt)`
  - [x] 1.3.4 Replace `Block` `@@unique([serverId, name])` with `@@unique([ownerId, serverId, name])`
- [x] 1.4 Create migration without applying: `npx prisma migrate dev --create-only --name per_user_tenant_ownership`
- [x] 1.5 Hand-edit the generated SQL so the order is: ADD COLUMN NULLABLE → UPDATE backfill (`SET ownerId = 1`) for all four tables → ALTER NOT NULL → ADD FOREIGN KEY (RESTRICT) → CREATE INDEX (all five composite indexes) → DROP old Block UNIQUE, ADD new Block UNIQUE
- [x] 1.6 Apply: `npx prisma migrate dev` then `npx prisma generate`
- [x] 1.7 Sanity check: `docker exec vxmm-pg psql -U vxmm -d vxmm -c 'SELECT "ownerId", count(*) FROM "Bot" GROUP BY 1;'` (and same for Order, TradeLog) — all rows MUST report `ownerId = 1`
- [x] 1.8 Performance check: `EXPLAIN ANALYZE SELECT * FROM "Bot" WHERE "ownerId" = 1 AND "deletedAt" IS NULL;` — plan MUST use the new `(ownerId, deletedAt)` index ← (verify: schema matches design D2/D3/D11, backfill complete, all five new indexes present, Block uniqueness swapped, EXPLAIN uses index)

## 2. Backend — scope helper and registration

- [x] 2.1 Create `backend/src/lib/scope.ts` exporting:
  - [x] 2.1.1 `getOwnerScope(req)` — returns `number` from `req.user.sub`, throws if `req.user` is undefined
  - [x] 2.1.2 `parseAdminOwnerFilter(req)` — pulls optional `ownerId` from `p.filters.ownerId` (post-parseListQuery) or `req.query.ownerId`, validates as positive integer, returns `number | undefined`
- [x] 2.2 Update `backend/src/routes/auth.ts` to add `POST /register`:
  - [x] 2.2.1 Zod body: `username` min 3, `password` min 6
  - [x] 2.2.2 Reject duplicate username with `badRequest('Username đã tồn tại')`
  - [x] 2.2.3 `bcrypt.hash(password, 10)`, create user with `role: 'USER'`, `enabled: true`
  - [x] 2.2.4 Return `{ jwt: signJwt({sub, username, role}) }` (200) ← (verify: matches auth-registration spec, JWT works against /api/me/*)

## 3. Backend — /api/me/* routes

- [x] 3.1 Create folder `backend/src/routes/me/`
- [x] 3.2 `me/profile.ts`: `GET /` returns `{id, username, role}` from `req.user`
- [x] 3.3 `me/bots.ts`: full CRUD using `getOwnerScope` — every where filter and every create/update payload MUST inject `ownerId`. PUT/DELETE MUST 404 if the bot's `ownerId` doesn't match the caller. PUT MUST log `BotReassignment` only when `client` changes AND the caller still owns the bot.
- [x] 3.4 `me/orders.ts`: `GET /` (list with parseListQuery) and `GET /:id`, both filter by `ownerId = req.user.sub` and `deletedAt: null`
- [x] 3.5 `me/tradeLogs.ts`: `GET /` only, list with parseListQuery, filter by `ownerId = req.user.sub`
- [x] 3.6 `me/blocks.ts`: `GET /`, `POST /` (force ownerId, honor `(ownerId, serverId, name)` uniqueness — catch unique violation and return 400 with friendly message), `DELETE /:id` (404 if not owner)
- [x] 3.7 `me/stats.ts`: same shape as `admin/stats.ts` but every count filters by `ownerId = req.user.sub`
- [x] 3.8 Mount under `/api/me/*` in `backend/src/app.ts` (profile, bots, orders, tradeLogs, blocks, stats) — apply `authRequired` at the router level, do not apply `adminRequired` ← (verify: me-api spec scenarios pass — owner-scoping enforced on every verb, cross-tenant reads/writes return 404)

## 4. Backend — Java client routes (/api/client/*)

- [x] 4.1 Update `backend/src/routes/bots.ts`:
  - [x] 4.1.1 Change `whereForClient(mode, client)` to `whereForClient(mode, client, ownerId)` and include `ownerId` in all returned where clauses
  - [x] 4.1.2 Pass `ctx.userId` (already `req.user.sub`) as `ownerId` in every call site
  - [x] 4.1.3 The PUT observer push (`updateMany`) MUST include `ownerId: ctx.userId` in its where
  - [x] 4.1.4 The PUT `/:mode/exit` `updateMany` MUST include `ownerId: ctx.userId` in its where
  - [x] 4.1.5 The `BotReassignment` queries in `/:mode/changed/new` and `/:mode/changed/delete` MUST only consider bots owned by the caller (join via `Bot.ownerId` or pre-filter the botIds)
- [x] 4.2 Update `backend/src/routes/orders.ts`:
  - [x] 4.2.1 `POST /create` stamps `ownerId: ctx.userId` on the new order
  - [x] 4.2.2 `PUT /bet|lose|win|reward|error|log` — each `updateMany` adds `ownerId: ctx.userId` to its where so foreign orders cannot be touched
  - [x] 4.2.3 `GET /check-update` adds `ownerId: ctx.userId` to both the `updatedClause` and `deletedClause`
- [x] 4.3 Update `backend/src/routes/log.ts`: `PUT /trade` stamps `ownerId: req.user.sub` on the new TradeLog ← (verify: client-scoping spec scenarios pass — every Java-client read filters by owner; every write stamps owner; resource/server unchanged)

## 5. Backend — admin routes become read-only over tenant data

- [x] 5.1 `backend/src/routes/admin/bots.ts`:
  - [x] 5.1.1 Remove the POST, PUT, and DELETE route registrations entirely (leave GET list + GET :id)
  - [x] 5.1.2 GET list reads optional `ownerId` via `parseAdminOwnerFilter(req)`; when provided, push `{ ownerId }` into the `AND` array
- [x] 5.2 `backend/src/routes/admin/orders.ts`: remove DELETE; GET list and GET :id keep working; add ownerId filter to GET list
- [x] 5.3 `backend/src/routes/admin/tradeLogs.ts`: add optional ownerId filter to GET list
- [x] 5.4 `backend/src/routes/admin/blocks.ts`: remove POST and DELETE; GET list adds optional ownerId filter
- [x] 5.5 `backend/src/routes/admin/stats.ts`: when query has a valid `ownerId`, every count filters by it; when absent, behavior is unchanged
- [x] 5.6 `backend/src/routes/admin/users.ts` and `admin/servers.ts`: leave as is (no changes required) ← (verify: admin-supervision spec scenarios pass — tenant routes are read-only, ownerId filter narrows results, users/servers CRUD preserved)

## 6. Frontend — auth, registration, route guards

- [x] 6.1 `admin/src/features/auth/AuthGuard.tsx`:
  - [x] 6.1.1 Drop the `payload.role !== 'ADMIN'` rejection so any valid, non-expired token enters the SPA
  - [x] 6.1.2 Export a new `AdminOnlyRoute` component that returns `<Navigate to="/" />` for non-ADMIN tokens, renders children for ADMIN
- [x] 6.2 `admin/src/features/auth/LoginPage.tsx`: drop the post-login role check; replace the "Đăng ký" toast trigger with a `<Link to="/register">Đăng ký!</Link>` (or HashRouter equivalent)
- [x] 6.3 Create `admin/src/features/auth/RegisterPage.tsx` mirroring LoginPage's visual style: username/password/confirm form, validation (match the backend), POST `/api/register`, `setToken(jwt)`, `navigate('/')`. Include a "Đã có tài khoản? Đăng nhập" link back to `/login`.
- [x] 6.4 `admin/src/routes.tsx`: add `{ path: '/register', element: <RegisterPage /> }` outside `AuthGuard`; wrap the elements for `/users` and `/servers` in `<AdminOnlyRoute>...</AdminOnlyRoute>` ← (verify: auth-registration + role-based-admin-ui scenarios for guards/redirects pass)

## 7. Frontend — sidebar and feature hooks

- [x] 7.1 `admin/src/components/layout/Sidebar.tsx`: read `getCurrentUser()?.role`; render the ADMIN menu set (Dashboard + Bot/Order/Trade/Block + Server/Users/Settings) when role is `'ADMIN'`, the USER menu set (Dashboard + Bot/Order/Trade/Block + Settings) otherwise
- [x] 7.2 Update each feature hook to switch base path by role and forward an optional `ownerId` filter (admin only):
  - [x] 7.2.1 `admin/src/features/bots/useBots.ts`
  - [x] 7.2.2 `admin/src/features/orders/useOrders.ts`
  - [x] 7.2.3 `admin/src/features/trade-logs/useTradeLogs.ts`
  - [x] 7.2.4 `admin/src/features/blocks/useBlocks.ts`
  - [x] 7.2.5 Dashboard hook (locate in `features/dashboard/`) — use `/api/me/stats` for USER, `/api/admin/stats` for ADMIN with optional ownerId
- [x] 7.3 For mutation hooks in the same files (createBot, updateBot, deleteBot, createBlock, deleteBlock), point them at `/api/me/<resource>` regardless of role (admin loses these capabilities; only USER can mutate, but admin is unlikely to mutate from these pages anyway since admin pages will hide the buttons)
- [x] 7.4 In `admin/src/features/bots/BotListPage.tsx` (and Order/TradeLog/Block list pages) hide the "Thêm" / Edit / Delete buttons when the active role is `'ADMIN'` so the read-only contract is honored in UI ← (verify: role-based-admin-ui spec scenarios pass — hooks target correct base, USER lacks admin filter, ADMIN lacks Add buttons on tenant pages)

## 8. Frontend — admin user-filter dropdown

- [x] 8.1 Add a reusable `OwnerFilter` component under `admin/src/components/` (or `features/admin/`): fetches `/api/admin/users` (top 100), renders a shadcn Select with "Tất cả" as the default, controls a parent-managed `ownerId | undefined` state
- [x] 8.2 Wire `OwnerFilter` into `BotListPage`, `OrderListPage`, `TradeLogListPage`, `BlockListPage`, and `DashboardPage` — show only when `getCurrentUser()?.role === 'ADMIN'`
- [x] 8.3 Each page passes `ownerId` to its hook; hooks forward it as a query string parameter compatible with `parseAdminOwnerFilter` (e.g. via `filter[ownerId]` or `?ownerId=N`) ← (verify: admin-supervision UI scenarios pass — filter visible only for ADMIN, selection drives API call)

## 9. Verification

- [x] 9.1 Restart backend dev server and frontend dev server; confirm both come up clean
- [x] 9.2 `curl -s -X POST http://localhost:8009/api/register -H 'content-type: application/json' -d '{"username":"e2e_test","password":"secret123"}'` returns a JWT; immediately use it to call `GET /api/me/profile` and expect `{role: "USER"}`
- [x] 9.3 Log in to the SPA as that new user — confirm USER sidebar (no Máy chủ / Người dùng), confirm `#/users` redirects to `#/`
- [x] 9.4 Log in as admin — confirm full sidebar; on BotList confirm the "Người dùng" dropdown appears; pick `tranhuutho` and confirm result is empty; pick `admin` and confirm 4 bots show
- [x] 9.5 Run the Java tool against the backend with the admin credentials — confirm `/api/client/bots/normal/check-update` still returns 200 and `/api/client/bots/normal` returns the 4 admin-owned bots
- [x] 9.6 Spot-check admin tenant routes are read-only: `curl -X POST http://localhost:8009/api/admin/bots -H 'authorization: Bearer <admin-jwt>' -d '{}'` MUST NOT return 200
- [x] 9.7 Spot-check `/api/me/bots` POST stamps owner correctly: register a fresh user, POST a bot, then SELECT it and confirm `ownerId` matches the new user's id ← (verify: every spec scenario above passes end-to-end against the running stack)

## 10. Cleanup

- [x] 10.1 Remove `backend/backup-pre-tenant.sql` from git status once the change is archived (keep on disk but `.gitignore` it if you want to retain locally)
- [x] 10.2 Update `MEMORY.md` index entry for `tool-vxmm-backend-selfhost` if any path or default behavior changed (admin no longer hard-blocks USER, new `/api/me/*`, `/register`) ← (verify: nothing leftover in tasks.md unchecked, README of backend mentions new endpoints if applicable)
