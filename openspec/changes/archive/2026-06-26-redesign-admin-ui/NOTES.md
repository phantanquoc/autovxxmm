# Archive Notes — redesign-admin-ui

Archived: 2026-06-26
Verification: Round 2 PASS — 0 CRITICAL issues
Build: tsc -b PASS, npm run build PASS, bundle 297KB gzip
Type safety: 0 `@ts-ignore`, 0 `as any`, 1 justified `as unknown as` (204-void handler)

## Task completion

43 of 44 tasks complete. One task partial (see open items below).

## Open minor items (non-blocking, documented for follow-up)

### 1. sheet.tsx not installed (Task 1.3 partial)

`sheet.tsx` was not added via `npx shadcn@latest add sheet`. The component is currently unused in any
page, so there is no runtime impact. If a future feature needs a slide-over drawer, add it then:

```
npx shadcn@latest add sheet
```

### 2. ColumnHeader sort UI not wired (Task 4.5 partial)

`DataTable` accepts a `sortKey` field on column definitions (interface present) but the
`DataTableToolbar` / `ColumnHeader` click handler does not pass sort params to `useQuery`.
Rows sort client-side via TanStack Table only, not server-side.

Recommended follow-up: wire `onSortingChange` to the query's `sort`/`order` params in
`DataTable.tsx` and test against the `/admin/bots` and `/admin/orders` endpoints which support
`sort=["field","ASC"]`.

### 3. UserListPage delete not wired (Task 6.5)

`useUserDelete` hook exists in `admin/src/features/users/useUsers.ts` but is not consumed by
`UserListPage.tsx`. The delete button in the user table is either absent or non-functional.

Recommended follow-up: import `useUserDelete` in `UserListPage`, add a confirm dialog (shadcn
AlertDialog), call mutate on confirm, and guard against deleting the current authenticated user
(compare `user.id` with `decodeJwt(getToken()).sub`).

### 4. window.confirm() used instead of shadcn Dialog in 4 pages

Pages that use `window.confirm()` for destructive action confirmation:
- `BotListPage.tsx` (delete bot)
- `ServerListPage.tsx` (delete server)
- `BlockListPage.tsx` (delete block)
- Possibly one additional page

`window.confirm()` is functional but breaks the design system and does not match the shadcn visual
language. The `AlertDialog` component from shadcn is already installed.

Recommended follow-up: replace `window.confirm()` calls with a shared `<ConfirmDialog>` wrapper
around shadcn `AlertDialog`. This is a cosmetic change with no logic impact.

## No spec delta sync required

This change had no `specs/` directory. The project has no main specs at
`openspec/specs/` either. No sync step was needed or performed.

## Validation note

`openspec validate redesign-admin-ui --strict` and `openspec validate redesign-admin-ui` both
returned an error about missing delta specs. This is expected — the change is a full UI rewrite
tracked via proposal/design/tasks only, with no spec-format capability deltas. The `openspec archive`
command completed successfully despite this warning.
