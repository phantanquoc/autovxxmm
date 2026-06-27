# Design — Admin UI redesign

## 1. Design tokens

### Color palette (CSS variables, support light + dark)

```css
:root {
  /* Brand */
  --primary: 211 100% 50%;        /* #3699FF — Metronic blue */
  --primary-foreground: 0 0% 100%;

  /* Semantic */
  --success: 152 72% 47%;          /* #1BC5BD teal — timestamp pill */
  --warning: 38 92% 50%;
  --danger:  354 78% 60%;          /* #F1416C — LOSE badge */
  --info:    211 100% 50%;

  /* Neutrals */
  --background:        0 0% 100%;
  --foreground:        220 13% 18%;     /* #2A2D38 */
  --muted:             220 14% 96%;     /* #F4F6FA — page bg */
  --muted-foreground:  220 9% 46%;
  --border:            220 13% 91%;
  --card:              0 0% 100%;
  --card-foreground:   220 13% 18%;
  --sidebar:           0 0% 100%;
  --sidebar-foreground: 220 9% 46%;
  --sidebar-active:    211 100% 50%;
  --sidebar-active-bg: 211 100% 96%;
}

.dark {
  --background:        222 16% 11%;     /* #151821 */
  --foreground:        220 13% 91%;
  --muted:             222 16% 14%;
  --muted-foreground:  220 9% 60%;
  --border:            222 16% 20%;
  --card:              222 16% 14%;
  --card-foreground:   220 13% 91%;
  --sidebar:           222 16% 9%;
  --sidebar-foreground: 220 9% 70%;
  --sidebar-active-bg: 211 50% 18%;
}
```

### Status color map (đặc thù VXMM)

| State | Light bg | Light text | Dark bg | Use |
|---|---|---|---|---|
| WAIT | `#E1E3EA` | `#7E8299` | `#2B2B40/60` | Đơn đang chờ |
| BET | `#FFF4DE` | `#FFA800` | `#392F28` | Đặt cược |
| LOSE | `#FFE2E5` | `#F1416C` | `#3A2434` | Thua cuộc |
| WIN | `#E8FFF3` | `#50CD89` | `#1C3238` | Thắng cuộc |
| REWARD | `#C9F7F5` | `#1BC5BD` | `#0C3A38` | Đã trả thưởng |
| ERROR | `#FFE2E5` | `#F64E60` | `#3A2434` | Lỗi |
| ONLINE (bot) | dot `#50CD89` | — | — | Đang treo |
| OFFLINE (bot) | dot `#A1A5B7` | — | — | Tắt |
| CONNECTING | dot `#FFA800` animate-pulse | — | — | Đang kết nối |

### Typography

```ts
// tailwind.config.ts
fontFamily: {
  sans: ['Inter', 'ui-sans-serif', 'system-ui'],
  mono: ['JetBrains Mono', 'monospace'],  // cho cột xu/id
}
```

Scale: `text-xs` (11px row meta), `text-sm` (13px body table), `text-base` (15px form), `text-lg` (heading card), `text-2xl` (page title), `text-3xl` (stat number)

### Spacing & radius

- Page padding: `p-6` (24px)
- Card padding: `p-5`
- Table row height: `h-12` (48px) — đủ cho 2-line cell
- Border radius: `rounded-md` (6px) default, `rounded-full` cho pill badge
- Card shadow: `shadow-sm` default, `shadow-md` cho hover

## 2. Layout architecture

```
┌────────────────────────────────────────────────────────────┐
│ Topbar (h-14, sticky)                                      │
├──────────┬─────────────────────────────────────────────────┤
│          │ PageHeader (title + breadcrumb + actions)        │
│ Sidebar  ├─────────────────────────────────────────────────┤
│ w-60     │                                                  │
│ collapse │ Page content (max-w-screen-2xl mx-auto p-6)      │
│ → w-16   │                                                  │
│          │                                                  │
└──────────┴─────────────────────────────────────────────────┘
```

### Sidebar (`components/layout/Sidebar.tsx`)

```tsx
<aside className="w-60 border-r bg-sidebar h-screen sticky top-0 flex flex-col">
  <div className="h-14 px-5 flex items-center gap-2 border-b">
    <Logo />
    <span className="font-bold text-lg tracking-wide">VXMM</span>
    <button onClick={toggleCollapse} className="ml-auto"><ChevronsLeft/></button>
  </div>
  <nav className="flex-1 overflow-y-auto py-4 px-3 space-y-6">
    <SidebarSection>
      <SidebarItem icon={LayoutDashboard} to="/" label="Dashboard" />
    </SidebarSection>

    <SidebarSection title="QUẢN LÝ">
      <SidebarItem icon={Bot}        to="/bots"        label="Bot"      badge={onlineCount} />
      <SidebarItem icon={ShoppingCart} to="/orders"    label="Đơn hàng" badge={activeOrders} badgeVariant="warning" />
      <SidebarItem icon={Coins}      to="/trade-logs"  label="Giao dịch xu" />
      <SidebarItem icon={Ban}        to="/blocks"      label="Block list" />
    </SidebarSection>

    <SidebarSection title="HỆ THỐNG">
      <SidebarItem icon={Server}     to="/servers"     label="Máy chủ" />
      <SidebarItem icon={Users}      to="/users"       label="Người dùng" />
      <SidebarItem icon={Settings}   to="/settings"    label="Cài đặt" />
    </SidebarSection>
  </nav>
  <SidebarFooter user={currentUser} />  {/* avatar + username + logout dropdown */}
</aside>
```

- Active item: `bg-sidebar-active-bg text-sidebar-active` với border-left 3px
- Hover: `bg-muted/50`
- Collapse mode: chỉ hiện icon, hover hiện tooltip

### Topbar (`components/layout/Topbar.tsx`)

```tsx
<header className="h-14 px-6 border-b bg-card sticky top-0 z-10 flex items-center gap-4">
  <Breadcrumb className="hidden md:flex" />
  <div className="ml-auto flex items-center gap-3">
    <ThemeToggle />
    <NotificationBell />        {/* hiện đơn mới, bot offline... — Phase sau */}
    <UserMenu>                  {/* avatar + dropdown: profile, settings, logout */}
      <span className="text-sm font-medium">{user.username}</span>
      <Avatar fallback={user.username[0]} />
    </UserMenu>
  </div>
</header>
```

## 3. Page-by-page layouts

### 3.1 Login (`features/auth/LoginPage.tsx`)

```tsx
<div className="min-h-screen flex items-center justify-center bg-muted relative overflow-hidden">
  {/* SVG dots/network pattern background, opacity 30% */}
  <DotsPattern className="absolute inset-0 -z-10" />

  <Card className="w-full max-w-md shadow-lg">
    <CardContent className="p-8 space-y-6">
      <div className="flex flex-col items-center space-y-3">
        <Logo size={64} />
        <h1 className="text-2xl font-bold">Đăng nhập</h1>
        <p className="text-sm text-muted-foreground">Nhập tài khoản và mật khẩu của bạn:</p>
      </div>

      <form className="space-y-4">
        <Input placeholder="Tài khoản" {...register('username')} className="h-12 bg-muted" />
        <Input placeholder="Mật khẩu" type="password" {...register('password')} className="h-12 bg-muted" />

        <div className="flex items-center justify-between text-sm">
          <label className="flex items-center gap-2">
            <Checkbox /> Nhớ đăng nhập
          </label>
          <button type="button" className="text-muted-foreground hover:text-primary"
                  onClick={() => toast.info('Liên hệ admin')}>
            Quên mật khẩu?
          </button>
        </div>

        <Button type="submit" className="w-full h-12">Đăng nhập</Button>
        {error && <p className="text-sm text-danger text-center">{error}</p>}
      </form>

      <div className="text-center text-sm text-muted-foreground">
        Chưa có tài khoản?{' '}
        <button onClick={() => toast.info('Liên hệ admin')} className="text-primary">Đăng ký!</button>
      </div>
    </CardContent>
  </Card>
</div>
```

### 3.2 Dashboard (`features/dashboard/DashboardPage.tsx`)

```tsx
<div className="space-y-6">
  <PageHeader title="Dashboard" />

  {/* Row 1: 4 stat cards */}
  <div className="grid grid-cols-1 md:grid-cols-2 xl:grid-cols-4 gap-4">
    <StatCard icon={Bot}     label="Bot đang treo" value={`${online}/${total}`}
              trend={+12} variant="primary" />
    <StatCard icon={ShoppingCart} label="Đơn 24h" value={orders24h}
              sub={`${active} đơn đang xử lý`} variant="warning" />
    <StatCard icon={Coins}   label="Giao dịch xu 24h" value={trade24h}
              sub={`Tổng ${formatXu(totalCoinFlow)}`} variant="success" />
    <StatCard icon={Users}   label="Người chơi unique" value={uniqueCustomers}
              variant="info" />
  </div>

  {/* Row 2: chart + side stat */}
  <div className="grid grid-cols-1 xl:grid-cols-3 gap-4">
    <Card className="xl:col-span-2">
      <CardHeader>
        <div className="flex items-center justify-between">
          <div>
            <CardTitle>Phân tích đơn hàng</CardTitle>
            <CardDescription>Số đơn theo {tab === 'day' ? 'ngày' : 'tháng'}</CardDescription>
          </div>
          <Tabs value={tab} onValueChange={setTab}>
            <TabsList>
              <TabsTrigger value="day">Ngày</TabsTrigger>
              <TabsTrigger value="month">Tháng</TabsTrigger>
            </TabsList>
          </Tabs>
        </div>
      </CardHeader>
      <CardContent>
        <OrderTrendChart data={chartData} />  {/* Recharts LineChart */}
      </CardContent>
    </Card>

    <Card>
      <CardHeader>
        <CardTitle>Thống kê tài khoản</CardTitle>
        <CardDescription>Chi tiết số liệu</CardDescription>
      </CardHeader>
      <CardContent className="space-y-3">
        <StatRow icon={<ShoppingCart className="text-primary"/>} bg="bg-primary/10" label="Tổng đơn hàng" value={total} />
        <StatRow icon={<Clock className="text-warning"/>}       bg="bg-warning/10" label="Đơn đang chờ"  value={waiting} />
        <StatRow icon={<CheckCircle className="text-success"/>} bg="bg-success/10" label="Tài khoản hoạt động" value={online} />
        <StatRow icon={<XCircle className="text-muted-foreground"/>} bg="bg-muted" label="Tài khoản offline" value={offline} />
        <StatRow icon={<Ban className="text-danger"/>}          bg="bg-danger/10"  label="Tài khoản bị khoá" value={disabled} />
      </CardContent>
    </Card>
  </div>

  {/* Row 3: recent orders */}
  <Card>
    <CardHeader>
      <CardTitle>Đơn hàng gần đây</CardTitle>
    </CardHeader>
    <CardContent>
      <RecentOrdersTable limit={10} />  {/* reuse OrderListPage table với 10 row */}
    </CardContent>
  </Card>
</div>
```

### 3.3 Bot list (`features/bots/BotListPage.tsx`)

Layout dày đặc kiểu mẫu 4. Mỗi row hiển thị 7 cell:

```tsx
<PageHeader title="Bot" actions={
  <Button onClick={() => setOpenCreate(true)}>
    <Plus className="mr-2 h-4 w-4" /> Thêm bot
  </Button>
}/>

<Card>
  <DataTableToolbar
    searchPlaceholder="Tìm acc/char/manager..."
    filters={[
      { type: 'select', key: 'serverId', label: 'Server', options: servers },
      { type: 'select', key: 'client',   label: 'Client', options: CLIENT_CHOICES },
      { type: 'select', key: 'obsStatus', label: 'Trạng thái', options: STATUS_CHOICES },
      { type: 'boolean', key: 'enable',  label: 'Bật' },
    ]}
  />

  <DataTable columns={[
    {
      header: 'Tài khoản',
      cell: (b) => (
        <div className="flex items-center gap-3">
          <ServerBadge id={b.serverId} />  {/* SV<N> chip xanh nhạt */}
          <div className="flex flex-col">
            <span className="font-medium text-sm">#{b.id} - {b.account}</span>
            <span className="text-xs text-muted-foreground">{server.name}</span>
          </div>
        </div>
      ),
    },
    { header: 'Quản lý',   cell: (b) => b.manager || <em className="text-muted-foreground">—</em> },
    { header: 'Nhân vật',  cell: (b) => (
      <div className="flex items-center gap-2">
        <BotStatusDot status={b.obsStatus} />
        <span>{b.obsName || b.charName}</span>
      </div>
    )},
    { header: 'Vị trí', cell: (b) => (
      <div className="flex flex-col text-xs">
        <span>Map {b.mapId}</span>
        <span className="text-muted-foreground">Khu vực {b.zoneId}</span>
      </div>
    )},
    { header: 'Xu / Lượng', cell: (b) => (
      <div className="flex flex-col text-xs font-mono">
        <XuAmount value={b.obsCoin ?? 0} />
        <span className="text-muted-foreground">{(b.obsGold ?? 0).toLocaleString()} lượng</span>
      </div>
    )},
    { header: 'Bật', cell: (b) => (
      <Switch checked={b.enable} onCheckedChange={(v) => toggleEnable(b.id, v)} />
    )},
    { header: 'Cập nhật', cell: (b) => (
      <Badge variant="teal">{formatDateTime(b.updatedAt)}</Badge>
    )},
    { header: '', cell: (b) => (
      <DropdownMenu>
        <DropdownMenuTrigger><MoreVertical className="h-4 w-4"/></DropdownMenuTrigger>
        <DropdownMenuContent>
          <DropdownMenuItem onSelect={() => openEdit(b)}>Chỉnh sửa</DropdownMenuItem>
          <DropdownMenuItem onSelect={() => duplicate(b)}>Nhân bản</DropdownMenuItem>
          <DropdownMenuSeparator />
          <DropdownMenuItem onSelect={() => softDelete(b)} className="text-danger">Xoá</DropdownMenuItem>
        </DropdownMenuContent>
      </DropdownMenu>
    )},
  ]} data={bots} />

  <DataTablePagination />
</Card>

<BotFormDialog open={openCreate} ... />
```

### 3.4 Order list (`features/orders/OrderListPage.tsx`)

Card row giống mẫu 3 — mỗi row có `border-b` + `hover:bg-muted/30`, không phải card riêng:

```tsx
<DataTable columns={[
  { header: 'Máy chủ',     cell: (o) => <span className="font-medium">{server(o.serverId).name}</span> },
  { header: 'Tên bot',     cell: (o) => o.bot },
  { header: 'Tên người chơi', cell: (o) => o.name },
  { header: 'Giây cược',   cell: (o) => <span>{o.second} giây</span> },
  { header: 'Xu đặt',      cell: (o) => <XuAmount value={o.coinOrder} /> },
  { header: 'Xu thắng',    cell: (o) => <XuAmount value={o.coinWin ?? 0} muted /> },
  { header: 'Xu nhận',     cell: (o) => <XuAmount value={o.coinReward ?? 0} muted /> },
  { header: 'Trạng thái',  cell: (o) => <StatusBadge status={o.status} /> },
  { header: 'Thời gian',   cell: (o) => (
    <div className="flex flex-col text-xs whitespace-nowrap">
      <span>{formatDateTime(o.timeStart)}</span>
      {o.timeStop > 0 && <span className="text-muted-foreground">{formatDateTime(o.timeStop)}</span>}
    </div>
  )},
  { header: 'Tuỳ chọn', cell: (o) => (
    <div className="flex items-center gap-1">
      <Switch checked={o.status !== 5} disabled />
      <Button variant="ghost" size="icon" onClick={() => del(o)}><Trash2 className="h-4 w-4"/></Button>
    </div>
  )},
]} data={orders} />
```

### 3.5 Trade log (`features/trade-logs/TradeLogListPage.tsx`)

Columns: ID, Server, Bot name, Customer, Before/After/Change (3 cột xu format), Type badge (NHẬN XU/TRẢ THƯỞNG/GOM XU), Description (truncate), Time. Filter search + server + type.

### 3.6 Server (`features/servers/ServerListPage.tsx`)

Bảng đơn giản 5 cột (id, name, ip, port, type) + button "Thêm". Edit qua dialog modal.

### 3.7 Users (`features/users/UserListPage.tsx`)

Bảng (username, role badge, enabled switch, createdAt). Modal create/edit. Không cho xoá chính mình.

### 3.8 Block list (`features/blocks/BlockListPage.tsx`)

Bảng (server, player name, reason, createdAt, action delete). Modal "Thêm block".

### 3.9 Settings (`features/settings/SettingsPage.tsx`)

Tabs:
- **Hồ sơ**: đổi password (yêu cầu nhập password cũ)
- **Giao diện**: theme light/dark toggle, sidebar collapse mặc định
- **Hệ thống**: hiện info backend URL, version, link tới `/api/health`

## 4. Reusable component contracts

### `<StatusBadge>`
```tsx
type Props = { status: 0|1|2|3|4|5; size?: 'sm'|'md' }
// 0=WAIT(grey) 1=BET(yellow) 2=LOSE(red) 3=WIN(green) 4=REWARD(teal) 5=ERROR(orange)
// Render pill: rounded-full px-3 py-1 text-xs font-semibold uppercase tracking-wide
```

### `<XuAmount>`
```tsx
type Props = { value: number; muted?: boolean; showUnit?: boolean }
// Render: "1,000,000 xu" với font-mono, muted version dùng text-muted-foreground
// Nếu value === 0 và muted → render "0 xu" muted
```

### `<DataTable>`
```tsx
type Column<T> = { header: ReactNode; cell: (row: T) => ReactNode; sortKey?: string; align?: 'left'|'right'; width?: string }
type Props<T> = {
  columns: Column<T>[];
  data: T[];
  isLoading?: boolean;
  emptyState?: ReactNode;
  rowKey: (row: T) => string | number;
  onRowClick?: (row: T) => void;
};
// Skeleton 5 row khi loading, EmptyState khi 0 data, hover row + cursor-pointer nếu onRowClick
```

### `<BotFormDialog>`
- Modal (shadcn Dialog) với 2 cột grid, fields:
  - Cột trái: server (Select), account, password, charName, manager
  - Cột phải: mapId, zoneId, posX, posY, playFee, typeLuckyDraw, client
  - Full width: chat (textarea), sms (textarea), enable (switch)
- Submit → POST/PUT, invalidate query, đóng dialog, toast "Đã lưu"

## 5. Data layer

### `lib/api.ts`
```ts
class ApiError extends Error { status: number; body?: unknown }

export async function api<T>(path: string, opts: RequestInit = {}): Promise<T> {
  const headers = new Headers(opts.headers);
  headers.set('Content-Type', 'application/json');
  const token = getToken();
  if (token) headers.set('Authorization', `Bearer ${token}`);
  const res = await fetch(`/api${path}`, { ...opts, headers });
  if (!res.ok) {
    if (res.status === 401) { clearToken(); window.location.hash = '/login'; }
    throw new ApiError(res.status, await res.text());
  }
  // For list endpoints, also expose Content-Range to caller via attached prop
  const total = parseInt(res.headers.get('Content-Range')?.split('/')[1] ?? '0', 10);
  const data = res.status === 204 ? null : await res.json();
  return Object.assign(data, { __total: total });
}
```

### TanStack Query hooks pattern (`features/bots/useBots.ts`)
```ts
type BotListParams = { page: number; pageSize: number; sort?: string; order?: 'asc'|'desc'; filter?: Record<string, unknown> }

export function useBots(p: BotListParams) {
  return useQuery({
    queryKey: ['bots', p],
    queryFn: () => api<Bot[]>(`/admin/bots?${buildQS(p)}`),
    refetchInterval: 10000,           // poll mỗi 10s cho observer fresh
  });
}

export function useBotMutation() {
  const qc = useQueryClient();
  return useMutation({
    mutationFn: (b: Partial<Bot>) =>
      b.id ? api(`/admin/bots/${b.id}`, { method: 'PUT', body: JSON.stringify(b) })
           : api('/admin/bots', { method: 'POST', body: JSON.stringify(b) }),
    onSuccess: () => qc.invalidateQueries({ queryKey: ['bots'] }),
  });
}
```

Backend đã hỗ trợ format `filter={"q":"foo","serverId":1}&range=[0,24]&sort=["id","ASC"]` (xem `backend/src/lib/list.ts`). `buildQS` encode đúng format này.

## 6. Routing (`routes.tsx`)

```ts
const router = createHashRouter([
  { path: '/login', element: <LoginPage /> },
  {
    element: <AuthGuard><AppLayout /></AuthGuard>,
    children: [
      { path: '/',            element: <DashboardPage /> },
      { path: '/bots',        element: <BotListPage /> },
      { path: '/orders',      element: <OrderListPage /> },
      { path: '/trade-logs',  element: <TradeLogListPage /> },
      { path: '/servers',     element: <ServerListPage /> },
      { path: '/users',       element: <UserListPage /> },
      { path: '/blocks',      element: <BlockListPage /> },
      { path: '/settings',    element: <SettingsPage /> },
      { path: '*',            element: <NotFound /> },
    ],
  },
]);
```

Dùng `createHashRouter` để khớp với `localhost:5173/#/login` user đã quen.

## 7. Dark mode

CSS variable strategy — toggle class `dark` trên `<html>`. Persist trong `localStorage.theme`. Default theo `prefers-color-scheme`.

## 8. Empty/Loading/Error states

- **Empty**: icon + text "Chưa có dữ liệu" + CTA tạo mới (nếu page có Create)
- **Loading**: Skeleton rows (DataTable tự render khi `isLoading`)
- **Error**: Banner đỏ "Không tải được dữ liệu" + button "Thử lại"
- **Toast**: shadcn `sonner` cho success/error notifications

## 9. Accessibility

- Mọi button có `aria-label` nếu chỉ có icon
- Sidebar nav dùng `<nav>` + `role="navigation"`
- Focus ring visible (`focus-visible:ring-2 ring-primary`)
- Tab order hợp lý trong form
- Modal đóng được bằng ESC, click outside
- Contrast ratio ≥ 4.5:1 với text body (đã check palette)

## 10. Visual reference checklist (so với 4 screenshot)

| Mẫu | Element key | Phải có |
|---|---|---|
| 1 Login | Logo lớn centered, 2 input bg muted, button primary full width, "Đăng nhập" / "Quên mật khẩu" / "Đăng ký" link | ✅ section 3.1 |
| 2 Dashboard | Sidebar trắng + section header "QUẢN LÝ", topbar avatar, banner info, card chart "Phân tích bán hàng" tabs Ngày/Tháng, side card "Thống kê tài khoản" 5 stat rows icon vuông màu | ✅ section 3.2 |
| 3 Orders | Bảng card row spacing, badge "THUA CUỘC" đỏ pill, thời gian 2 dòng, toggle + delete cuối row | ✅ section 3.4 |
| 4 Bot list | Tabs trên cùng "Tài khoản / Danh sách", button "Thêm tài khoản" primary, mỗi row có badge "SV<N>" + "#ID - tên" 2 dòng + owner + nhân vật + location 2 dòng + xu/lượng 2 dòng + toggle + timestamp pill teal + dropdown | ✅ section 3.3 |
