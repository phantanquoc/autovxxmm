# Tasks — Redesign Admin UI

Mỗi task được viết tự-contained để autopilot chạy từng bước. Acceptance criteria phải verify được bằng command hoặc visual check. Khi gặp lỗi, dừng task, log lại, không tự rollback toàn bộ.

## Phase 0 — Backup & cleanup

### Task 0.1 — Backup admin hiện tại
- **Action**: `mv D:\tool_vxmm\admin D:\tool_vxmm\admin.bak` (giữ làm reference khi cần đối chiếu logic cũ)
- **Verify**: `ls D:\tool_vxmm\admin.bak\src\` ra danh sách file cũ
- **Files**: chỉ rename folder

### Task 0.2 — Scaffold admin mới với Vite + React + TS
- **Action**: `cd D:\tool_vxmm && npm create vite@latest admin -- --template react-ts` (non-interactive: dùng `--yes` hoặc echo answers)
- **Verify**: `ls admin/src/` thấy `App.tsx`, `main.tsx`
- **Files**: tạo mới toàn bộ `D:\tool_vxmm\admin\`

### Task 0.3 — Copy `.gitignore` + giữ stub
- **Action**: viết `admin/.gitignore` (node_modules, dist, .env, .vite)
- **Verify**: `cat admin/.gitignore` chứa các pattern trên
- **Files**: `admin/.gitignore`

## Phase 1 — Cài stack & design tokens

### Task 1.1 — Cài deps base
- **Action**: trong `admin/` chạy:
  ```
  npm install react-router-dom@6.27.0 @tanstack/react-query@5 @tanstack/react-table@8 \
    react-hook-form @hookform/resolvers zod \
    clsx tailwind-merge class-variance-authority lucide-react \
    recharts date-fns sonner

  npm install -D tailwindcss@3 postcss autoprefixer @types/node
  ```
- **Verify**: `npm ls react-router-dom @tanstack/react-query tailwindcss lucide-react` không error
- **Files**: `admin/package.json` (auto-update)

### Task 1.2 — Cài shadcn/ui CLI và init
- **Action**:
  ```
  npx shadcn@latest init -d
  ```
  Trả lời defaults: TypeScript yes, style "default", base color "slate", CSS variables yes, tailwind config path mặc định, components path `src/components/ui`, utils path `src/lib/utils`.
- **Verify**: `ls admin/src/components/ui/` đã có, `cat admin/components.json` đúng schema
- **Files**: `admin/components.json`, `admin/tailwind.config.ts`, `admin/src/lib/utils.ts`, `admin/src/index.css`

### Task 1.3 — Thêm shadcn components cần dùng
- **Action**:
  ```
  npx shadcn@latest add button card input label dialog dropdown-menu badge \
    switch select tabs sheet skeleton sonner avatar checkbox separator tooltip
  ```
- **Verify**: `ls admin/src/components/ui/` có đủ 17 file `.tsx`
- **Files**: `admin/src/components/ui/*.tsx`

### Task 1.4 — Override design tokens (palette + status colors)
- **Action**: viết `admin/src/index.css` theo `design.md` section 1 (CSS variables `:root` + `.dark`)
- **Verify**: `cat admin/src/index.css | grep --primary` thấy `211 100% 50%`
- **Files**: `admin/src/index.css`

### Task 1.5 — Cập nhật `tailwind.config.ts`
- **Action**: thêm fontFamily Inter + JetBrains Mono (qua Google Fonts CDN trong `index.html`), thêm color tokens trỏ về CSS vars, thêm animation `pulse-slow`
- **Verify**: build pass `npx tsc --noEmit`
- **Files**: `admin/tailwind.config.ts`, `admin/index.html` (thêm `<link>` Google Fonts)

### Task 1.6 — Vite config + proxy
- **Action**: viết `admin/vite.config.ts` với proxy `/api → localhost:8009` (giữ y như cũ ở `admin.bak/vite.config.ts`)
- **Verify**: `npm run dev` start được, `curl http://localhost:5173/api/health` trả 200
- **Files**: `admin/vite.config.ts`

## Phase 2 — Lib + data layer

### Task 2.1 — `lib/utils.ts` cn() helper
- **Action**: viết hàm `cn(...inputs)` dùng `clsx` + `twMerge` (shadcn init đã làm sẵn, verify lại)
- **Verify**: `cat admin/src/lib/utils.ts` có `export function cn`
- **Files**: `admin/src/lib/utils.ts`

### Task 2.2 — `lib/auth.ts` JWT storage
- **Action**: implement `getToken/setToken/clearToken/decodeJwt/isExpired`. Key localStorage: `vxmm.jwt`
- **Verify**: type-check pass
- **Files**: `admin/src/lib/auth.ts`

### Task 2.3 — `lib/api.ts` fetch wrapper
- **Action**: implement theo design.md section 5. Throw ApiError với status. Auto-redirect login khi 401. Attach `__total` từ Content-Range cho list response.
- **Verify**: type-check pass; viết test stub gọi `/api/health` trong dev
- **Notes**: `apiList<T>` added for list endpoints — eliminates `__total` cast. `api<T>` kept for non-list endpoints (stats, mutations, login).
- **Files**: `admin/src/lib/api.ts`

### Task 2.4 — `lib/format.ts`
- **Action**: implement `formatXu(n)` ("1,000,000"), `formatDateTime(ms|iso)` ("16-02-2026 00:34:09"), `formatRelative(ms)` ("3 phút trước") dùng date-fns
- **Verify**: type-check pass
- **Files**: `admin/src/lib/format.ts`

### Task 2.5 — `lib/queryClient.ts`
- **Action**: tạo QueryClient với defaultOptions retry=1, staleTime=5000
- **Verify**: type-check pass
- **Files**: `admin/src/lib/queryClient.ts`

### Task 2.6 — `lib/buildQS.ts`
- **Action**: hàm encode params thành `?filter={...}&range=[0,24]&sort=["id","ASC"]` URL-encoded, khớp format backend `list.ts`
- **Verify**: unit test inline: `buildQS({page:1, pageSize:25, sort:'id', order:'asc', filter:{q:'x'}})` → `filter=%7B%22q%22%3A%22x%22%7D&range=%5B0%2C24%5D&sort=%5B%22id%22%2C%22ASC%22%5D`
- **Files**: `admin/src/lib/buildQS.ts`

## Phase 3 — Layout shell

### Task 3.1 — `components/layout/AppLayout.tsx`
- **Action**: layout 2-cột: Sidebar fixed width + main column (Topbar sticky + `<Outlet/>`)
- **Verify**: route protected wrapper render được
- **Files**: `admin/src/components/layout/AppLayout.tsx`

### Task 3.2 — `Sidebar.tsx`
- **Action**: theo design.md section 2. Active item dùng `useLocation()`. State collapse trong localStorage `vxmm.sidebar.collapsed`.
- **Verify**: click toggle, sidebar collapse 240px → 64px
- **Files**: `admin/src/components/layout/Sidebar.tsx`

### Task 3.3 — `Topbar.tsx` + `UserMenu` + `ThemeToggle`
- **Action**: theo design.md section 2. ThemeToggle bật/tắt class `dark` trên `<html>`, persist `vxmm.theme`
- **Verify**: click toggle, body bg đổi
- **Files**: `admin/src/components/layout/Topbar.tsx`, `Topbar` subcomponents

### Task 3.4 — `PageHeader.tsx`
- **Action**: nhận `title`, `breadcrumb?`, `actions?` children render bên phải
- **Files**: `admin/src/components/layout/PageHeader.tsx`

### Task 3.5 — `AuthGuard` route wrapper
- **Action**: check token + isExpired, redirect `/login` nếu fail. Lấy `role` ADMIN required, nếu USER → render `<AccessDenied/>`
- **Files**: `admin/src/features/auth/AuthGuard.tsx`, `admin/src/features/auth/AccessDenied.tsx`

## Phase 4 — Reusable components

### Task 4.1 — `<StatusBadge>` (đơn hàng)
- **Action**: theo design.md section 4. Switch theo `status` 0-5, mỗi case map color + label tiếng Việt: ĐANG CHỜ / ĐẶT CƯỢC / THUA CUỘC / THẮNG CUỘC / ĐÃ THƯỞNG / LỖI
- **Files**: `admin/src/components/domain/StatusBadge.tsx`

### Task 4.2 — `<BotStatusDot>`
- **Action**: chấm tròn 8px, màu theo ONLINE/OFFLINE/CONNECTING. CONNECTING animate-pulse
- **Files**: `admin/src/components/domain/BotStatusDot.tsx`

### Task 4.3 — `<ServerBadge>`
- **Action**: pill xanh nhạt `SV<N>` font-mono. Nhận `id` (number)
- **Files**: `admin/src/components/domain/ServerBadge.tsx`

### Task 4.4 — `<XuAmount>` + `<ClientBadge>`
- **Action**: `<XuAmount value muted? showUnit=true>` render `1,000,000 xu`. `<ClientBadge value>` render "Normal" hoặc "Client N" với màu khác nhau
- **Files**: `admin/src/components/domain/XuAmount.tsx`, `ClientBadge.tsx`

### Task 4.5 — `<DataTable>` generic
- **Action**: theo design.md section 4. Skeleton 5 row khi loading. EmptyState component có icon + text + optional CTA.
- **Verify**: type-check pass, render với dummy data 5 row 3 col
- **Files**: `admin/src/components/data-table/DataTable.tsx`, `DataTablePagination.tsx`, `DataTableToolbar.tsx`, `EmptyState.tsx`

## Phase 5 — Auth + Bootstrap

### Task 5.1 — `LoginPage.tsx`
- **Action**: theo design.md section 3.1. Form react-hook-form + zod (username min 1, password min 1). Submit gọi `api('/login', POST)`, decode JWT, check role ADMIN, lưu token, navigate `/`.
- **Verify**: nhập `admin`/`admin@123` → vào dashboard. Nhập sai → toast danger "Sai tài khoản hoặc mật khẩu"
- **Files**: `admin/src/features/auth/LoginPage.tsx`, `useAuth.ts`

### Task 5.2 — DotsPattern background
- **Action**: viết SVG inline với pattern chấm tròn + đường nối nhạt, full screen, opacity 0.3
- **Files**: `admin/src/components/decorative/DotsPattern.tsx`

### Task 5.3 — Router + `App.tsx` + `main.tsx`
- **Action**: theo design.md section 6. createHashRouter, QueryClientProvider, Toaster (sonner)
- **Verify**: app boot, route `/login` render, route `/` redirect login khi chưa auth
- **Files**: `admin/src/App.tsx`, `admin/src/main.tsx`, `admin/src/routes.tsx`

## Phase 6 — Pages

### Task 6.1 — `useServers` hook + ServerListPage + ServerFormDialog
- **Action**: hook list + create + update + delete. Page bảng đơn giản, modal form.
- **Verify**: tạo server mới, edit, refresh thấy data đúng
- **Files**: `admin/src/features/servers/*`

### Task 6.2 — `useBots` hook + BotListPage + BotFormDialog
- **Action**: theo design.md section 3.3. Filter ribbon, table dày đặc, toggle inline, dropdown actions, modal form 2-cột.
- **Verify**:
  - List 3 bot demo có sẵn
  - Filter server=Bokken còn 1 bot
  - Toggle enable off → backend record updated
  - Tạo bot mới → list refresh
  - Edit bot → sau khi save, list cập nhật
- **Notes**: Filters complete: server, client, obsStatus, enable
- **Files**: `admin/src/features/bots/*`

### Task 6.3 — `useOrders` hook + OrderListPage
- **Action**: theo design.md section 3.4. Filter + status badge + 2-line timestamp.
- **Verify**: thấy 2 đơn demo, filter status=WAIT (0) còn 2, status=WIN (3) còn 0
- **Files**: `admin/src/features/orders/*`

### Task 6.4 — `useTradeLogs` + TradeLogListPage
- **Action**: bảng read-only, filter + format xu
- **Verify**: thấy 1 trade log demo
- **Files**: `admin/src/features/trade-logs/*`

### Task 6.5 — `useUsers` + UserListPage + UserFormDialog
- **Action**: CRUD users. Không cho xoá user hiện tại (compare với JWT.sub)
- **Verify**: list có admin user, không xoá được, tạo user thường được
- **Files**: `admin/src/features/users/*`

### Task 6.6 — `useBlocks` + BlockListPage + BlockFormDialog
- **Action**: list + create + delete
- **Verify**: thêm 1 block, delete được
- **Files**: `admin/src/features/blocks/*`

### Task 6.7 — `SettingsPage`
- **Action**: Tabs Hồ sơ / Giao diện / Hệ thống. Tab Hệ thống fetch `/api/health` + version từ `package.json`
- **Verify**: theme toggle hoạt động, đổi password (gọi PUT /admin/users/:id với password mới)
- **Files**: `admin/src/features/settings/*`

### Task 6.8 — `DashboardPage`
- **Action**: theo design.md section 3.2. StatCard component, OrderTrendChart (Recharts LineChart fetch `/admin/orders?range=[0,200]&sort=["createdAt","DESC"]` rồi group by day), StatRow, RecentOrdersTable.
- **Verify**: dashboard render 4 stat + chart (có thể flat line nếu 0 data) + side stats + recent table
- **Files**: `admin/src/features/dashboard/*`

## Phase 7 — Polish

### Task 7.1 — NotFound + Error boundary
- **Action**: route `*` render NotFound, root ErrorBoundary catch render lỗi → page lỗi đỏ với "Reload" button
- **Files**: `admin/src/features/error/NotFound.tsx`, `ErrorBoundary.tsx`

### Task 7.2 — Loading skeletons & EmptyState mọi page
- **Action**: kiểm tra mọi list page có `isLoading` state + `empty` state đúng
- **Verify**: tạm xoá hết bot ở DB → BotListPage hiện empty state với CTA "Thêm bot"

### Task 7.3 — Toast hệ thống
- **Action**: gắn `<Toaster richColors position="top-right" />` ở App.tsx. Mọi mutation success → toast "Đã lưu" / "Đã xoá". Mọi error → toast danger với message.

### Task 7.4 — Keyboard shortcuts (optional, low priority)
- **Action**: `Ctrl+K` mở command palette (cmdk lib) — chỉ làm nếu dư thời gian
- **Skip nếu** không có thời gian

## Phase 8 — Verification

### Task 8.1 — Type-check + Build
- **Action**:
  ```
  cd D:\tool_vxmm\admin
  npx tsc -b
  npm run build
  ```
- **Verify**: exit 0, dist/ < 800KB gzip

### Task 8.2 — Smoke test toàn bộ flow
- **Action**: start `npm run dev` ở background, dùng `osf-browser-automation` skill với task:
  > Mở http://localhost:5173, login admin/admin@123, navigate tuần tự: Dashboard → Bot list (tạo 1 bot mới, edit, toggle, xoá) → Đơn hàng (filter status) → Trade log → Server → Users → Blocks (thêm 1 block) → Settings (toggle theme dark → light). Chụp screenshot mỗi trang. Báo cáo lỗi console nếu có.
- **Verify**: 0 lỗi console error. 8 screenshot lưu trong `D:\tool_vxmm\design\screenshots\`

### Task 8.3 — Visual diff với 4 mockup gốc
- **Action**: so screenshot Login/Dashboard/Order list/Bot list với 4 mockup user gửi. Đánh giá theo checklist trong `design.md` section 10.
- **Verify**: ≥ 6/8 element key match (badge, color, layout structure). Note lại element nào cần tinh chỉnh.

### Task 8.4 — Cập nhật memory
- **Action**: viết hoặc cập nhật memory `tool-vxmm-backend-selfhost` với stack mới (shadcn thay React Admin) + path mới
- **Files**: memory dir của session

## Phase 9 — Cleanup (optional sau khi user OK)

### Task 9.1 — Xoá `admin.bak`
- **Action**: chỉ làm khi user xác nhận admin mới chạy ổn ≥ 1 ngày. Mặc định để đó.

---

## Dependency graph

```
Phase 0 → Phase 1 (scaffold required) → Phase 2 (lib) → Phase 3 (layout)
                                              ↓             ↓
                                         Phase 4 (components) ─→ Phase 5 (auth)
                                                                      ↓
                                                            Phase 6 (pages, parallel possible)
                                                                      ↓
                                                                 Phase 7 (polish)
                                                                      ↓
                                                                 Phase 8 (verify)
```

Phase 6 các task có thể chạy song song (6.1-6.7 độc lập), trừ 6.8 (Dashboard) cần các page khác xong vì RecentOrdersTable reuse component từ 6.3.

## Autopilot guidance

- **Khi gặp lỗi**: dừng task hiện tại, log lỗi + file + line, KHÔNG tự rollback các task trước
- **Khi npm install fail**: thử `npm install --legacy-peer-deps`, log warning
- **Khi shadcn add lỗi**: kiểm tra `components.json` đã đúng, retry
- **Khi backend không trả 200**: check `docker ps` thấy `vxmm-pg`, check `curl localhost:8009/api/health`. Nếu backend tắt → start lại trước
- **Khi visual không match**: ưu tiên đúng functionally trước, visual tinh chỉnh ở Phase 7
- **Type-check fail không bỏ qua**: phải fix trước khi sang task tiếp
- **Test với data thật**: dùng admin/admin@123 + 3 bot demo + 2 order demo + 1 trade log đã seed sẵn trong DB

## Estimated effort

| Phase | Tasks | LOC estimate |
|---|---|---|
| 0 — Backup & scaffold | 3 | 30 |
| 1 — Stack + tokens | 6 | 80 |
| 2 — Lib | 6 | 200 |
| 3 — Layout | 5 | 400 |
| 4 — Components | 5 | 500 |
| 5 — Auth | 3 | 250 |
| 6 — Pages | 8 | 2000 |
| 7 — Polish | 4 | 200 |
| 8 — Verify | 4 | — |
| **Tổng** | **44** | **~3700 LOC** |

Autopilot có thể chia thành ~10 PR nhỏ nếu muốn review từng phần, hoặc 1 PR lớn cho toàn rewrite.
