# Proposal — Redesign Admin UI (Metronic-style)

## Why

Admin hiện tại dùng **React Admin 5 + MUI** với theme Material Design mặc định. Vấn đề:

1. **Design không khớp domain** — user là dev game vận hành, cần UI dày đặc thông tin (nhiều cột, nhiều row trên 1 màn hình) theo phong cách "ops dashboard" như Metronic. MUI Datagrid mặc định quá thoáng, padding lớn, ít phân cấp visual.
2. **Khó custom layout** — React Admin có Layout/AppBar/Sidebar gắn cứng. Muốn làm sidebar Metronic-style (logo + section header + items + nested groups), card layout cho table row, badge pill cho status, toggle inline — đều phải override deeply và đã gặp 3 lần lỗi version mismatch (xem `tool-vxmm-backend-selfhost` memory).
3. **Thiếu nhiều page** đặc thù VXMM:
   - Dashboard không có line chart đơn hàng theo ngày/tháng
   - Bot list không có view dạng card row (server badge + thông tin 2 dòng + toggle + timestamp pill)
   - Đơn hàng không có status pill màu (THUA CUỘC đỏ, THẮNG xanh...)
   - Trade log không có format xu có dấu phẩy
   - Không có Settings page
4. **Visual identity** — user gửi 4 screenshot Metronic-style với palette xanh/đỏ/teal pastel, typography clean, spacing tight. Cần system riêng.

## What

**Rewrite hoàn toàn** folder `D:\tool_vxmm\admin\src\` với stack mới:

- **Build**: Vite 5 (giữ nguyên), TypeScript strict
- **UI**: Tailwind CSS 3 + shadcn/ui (Radix primitives) — copy components vào codebase, không phụ thuộc theme runtime
- **Routing**: React Router 6
- **State/Data**: TanStack Query 5 — fetch + cache + invalidate
- **Forms**: React Hook Form + Zod
- **Charts**: Recharts (nhẹ, đủ cho line/bar/pie)
- **Icons**: Lucide React
- **Tables**: TanStack Table 8 (headless, full control)
- **Date utils**: date-fns

## Scope

### In scope (file/folder MỚI sẽ tạo)

```
D:\tool_vxmm\admin\
├── src\
│   ├── main.tsx                  # bootstrap
│   ├── App.tsx                   # router + providers
│   ├── globals.css               # tailwind directives + CSS vars
│   ├── lib\
│   │   ├── api.ts                # fetch wrapper với JWT auto-inject
│   │   ├── queryClient.ts        # TanStack Query config
│   │   ├── auth.ts               # token storage + decode JWT
│   │   ├── format.ts             # formatXu, formatDateTime, formatRelative
│   │   └── utils.ts              # cn() classname merge
│   ├── components\
│   │   ├── ui\                   # shadcn primitives: button, card, input, select, dialog, dropdown-menu, badge, switch, table, tabs, sheet, skeleton, toast
│   │   ├── layout\
│   │   │   ├── Sidebar.tsx
│   │   │   ├── Topbar.tsx
│   │   │   ├── AppLayout.tsx
│   │   │   └── PageHeader.tsx
│   │   ├── data-table\
│   │   │   ├── DataTable.tsx     # generic TanStack Table wrapper
│   │   │   ├── DataTablePagination.tsx
│   │   │   ├── DataTableToolbar.tsx   # search + filter chips
│   │   │   └── ColumnHeader.tsx       # sortable column header
│   │   └── domain\
│   │       ├── StatusBadge.tsx        # đơn hàng WAIT/BET/LOSE/WIN/REWARD/ERROR
│   │       ├── BotStatusDot.tsx       # ONLINE/OFFLINE/CONNECTING
│   │       ├── ServerBadge.tsx        # SV<N> chip
│   │       ├── XuAmount.tsx           # format số có dấu phẩy + suffix "xu"
│   │       └── ClientBadge.tsx        # Normal / Client 1-10
│   ├── features\
│   │   ├── auth\
│   │   │   ├── LoginPage.tsx
│   │   │   └── useAuth.ts
│   │   ├── dashboard\
│   │   │   ├── DashboardPage.tsx
│   │   │   ├── StatCards.tsx
│   │   │   ├── OrderTrendChart.tsx
│   │   │   └── RecentOrdersTable.tsx
│   │   ├── bots\
│   │   │   ├── BotListPage.tsx        # dày đặc kiểu mẫu 4 (account list)
│   │   │   ├── BotFormDialog.tsx      # modal create/edit
│   │   │   └── useBots.ts             # query hooks
│   │   ├── orders\
│   │   │   ├── OrderListPage.tsx      # card row kiểu mẫu 3
│   │   │   └── useOrders.ts
│   │   ├── trade-logs\
│   │   │   ├── TradeLogListPage.tsx
│   │   │   └── useTradeLogs.ts
│   │   ├── servers\
│   │   │   ├── ServerListPage.tsx
│   │   │   ├── ServerFormDialog.tsx
│   │   │   └── useServers.ts
│   │   ├── users\
│   │   │   ├── UserListPage.tsx
│   │   │   ├── UserFormDialog.tsx
│   │   │   └── useUsers.ts
│   │   ├── blocks\
│   │   │   ├── BlockListPage.tsx
│   │   │   ├── BlockFormDialog.tsx
│   │   │   └── useBlocks.ts
│   │   └── settings\
│   │       └── SettingsPage.tsx       # đổi password, theme toggle, info backend
│   └── routes.tsx                # route table
├── tailwind.config.ts
├── postcss.config.js
├── components.json               # shadcn cli config
├── index.html
├── package.json                  # rewrite deps
├── tsconfig.json
└── vite.config.ts
```

### Giữ nguyên (KHÔNG sửa)

- `D:\tool_vxmm\backend\**` — API hiện tại đã đủ. Không thêm/sửa endpoint nào trong scope này.
- `D:\tool_vxmm\docker-compose.yml`
- DB schema

### Sẽ xoá

- `D:\tool_vxmm\admin\src\authProvider.ts` (logic JWT giữ nhưng viết lại trong `lib/auth.ts`)
- `D:\tool_vxmm\admin\src\dataProvider.ts` (thay bằng `lib/api.ts` + TanStack Query hooks)
- `D:\tool_vxmm\admin\src\Dashboard.tsx`
- `D:\tool_vxmm\admin\src\resources\**` (toàn bộ)
- Deps cũ trong `package.json`: `react-admin`, `ra-data-simple-rest`, `@mui/*`, `@emotion/*`, các `overrides` ra-*

## Out of scope

- Mobile responsive (focus desktop ops dashboard, sẽ làm sau)
- i18n (giữ tiếng Việt hardcode, sẽ extract sau nếu cần)
- Real-time WebSocket (giữ polling qua TanStack Query refetchInterval)
- Đăng ký tài khoản (link "Đăng ký" ở mockup login chỉ hiển thị thông báo "Liên hệ admin")
- "Quên mật khẩu" flow (button disabled với tooltip "Liên hệ admin")
- Backend endpoint mới — nếu page nào cần data backend chưa có, dùng client-side computation hoặc skip page đó, **không sửa backend trong scope này**

## Risks

| Risk | Mitigation |
|---|---|
| Rewrite full UI = nhiều việc | Spec chia 8 phase rõ ràng, mỗi phase test được riêng |
| Mất feature đang dùng (filter, edit form) | Acceptance criteria liệt kê đầy đủ feature cũ cần giữ |
| TanStack Table learning curve | Wrap thành `<DataTable>` generic, các page chỉ cần khai báo columns |
| Bundle size tăng do thêm Tailwind + shadcn | Tailwind purge mode → CSS nhỏ. shadcn = component được copy, tree-shake tự nhiên |
| Style không match screenshot 100% | Spec có color token cụ thể, design.md mô tả layout từng page bằng pseudo-JSX |

## Acceptance criteria (toàn cục)

1. `npm run build` pass, bundle < 800KB gzip
2. Đăng nhập `admin`/`admin@123` → vào dashboard
3. Mọi page CRUD cũ vẫn hoạt động (Bot/Server/User/Block create/edit, Order/TradeLog read)
4. Filter/sort/pagination hoạt động với backend đã có (format `filter`/`range`/`sort` JSON hoặc `_start`/`_end` legacy)
5. Visual match 80%+ với 4 screenshot user gửi:
   - Login: card centered, logo, 2 input, button xanh primary
   - Sidebar: logo "VXMM" + menu items + section header DỊCH VỤ
   - Dashboard: 4 stat cards + line chart + recent orders
   - Order list: card row với badge pill status + 2-line timestamp
   - Bot list: dày đặc — badge SV<N> đầu row + thông tin lồng 2 dòng + toggle enable + timestamp pill xanh teal
6. Dark mode toggle hoạt động ở Settings (nếu được implement)
7. Type-check pass: `tsc -b` exit 0
8. Lint pass (nếu có ESLint)

## Reference

- 4 screenshot user gửi 2026-06-26
- Metronic dashboard: <https://preview.keenthemes.com/metronic8>
- shadcn/ui dashboard examples: <https://ui.shadcn.com/examples/dashboard>
- TanStack Table examples: <https://tanstack.com/table/v8/docs/framework/react/examples/basic>
