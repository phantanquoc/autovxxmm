import { useState, useEffect } from 'react'
import { NavLink, useLocation } from 'react-router-dom'
import {
  LayoutDashboard, Bot, ShoppingCart, Coins, Ban, Server, Users, Settings,
  ChevronsLeft, ChevronsRight, LogOut, Receipt
} from 'lucide-react'
import { cn } from '@/lib/utils'
import { clearToken, getCurrentUser } from '@/lib/auth'
import { Avatar, AvatarFallback } from '@/components/ui/avatar'
import { Tooltip, TooltipContent, TooltipTrigger, TooltipProvider } from '@/components/ui/tooltip'

const COLLAPSE_KEY = 'vxmm.sidebar.collapsed'

interface NavItem {
  icon: React.ElementType
  to: string
  label: string
}

const mainItems: NavItem[] = [
  { icon: LayoutDashboard, to: '/', label: 'Dashboard' },
]

const manageItems: NavItem[] = [
  { icon: Bot, to: '/bots', label: 'Bot' },
  { icon: Coins, to: '/collect-bots', label: 'Bot gom xu' },
  { icon: ShoppingCart, to: '/orders', label: 'Đơn hàng' },
  { icon: Receipt, to: '/trade-logs', label: 'Giao dịch xu' },
  { icon: Ban, to: '/blocks', label: 'Block list' },
]

const systemItems: NavItem[] = [
  { icon: Server, to: '/servers', label: 'Máy chủ' },
  { icon: Users, to: '/users', label: 'Người dùng' },
  { icon: Settings, to: '/settings', label: 'Cài đặt' },
]

function SidebarItem({ icon: Icon, to, label, collapsed }: NavItem & { collapsed: boolean }) {
  const location = useLocation()
  const isActive = to === '/' ? location.pathname === '/' : location.pathname.startsWith(to)

  const content = (
    <NavLink
      to={to}
      className={cn(
        'flex items-center gap-3 px-3 py-2.5 rounded-md text-sm font-medium transition-colors relative',
        'hover:bg-muted/50',
        isActive && 'bg-sidebar-active-bg text-sidebar-active border-l-[3px] border-sidebar-active pl-[calc(0.75rem-3px)]',
        !isActive && 'text-sidebar-foreground',
        collapsed && 'justify-center px-2'
      )}
    >
      <Icon className="h-5 w-5 shrink-0" />
      {!collapsed && <span>{label}</span>}
    </NavLink>
  )

  if (collapsed) {
    return (
      <Tooltip>
        <TooltipTrigger asChild>{content}</TooltipTrigger>
        <TooltipContent side="right">{label}</TooltipContent>
      </Tooltip>
    )
  }
  return content
}

export function Sidebar() {
  const [collapsed, setCollapsed] = useState(() => {
    return localStorage.getItem(COLLAPSE_KEY) === 'true'
  })

  const user = getCurrentUser()
  const isAdmin = user?.role === 'ADMIN'

  function toggleCollapse() {
    const next = !collapsed
    setCollapsed(next)
    localStorage.setItem(COLLAPSE_KEY, String(next))
  }

  function handleLogout() {
    clearToken()
    window.location.hash = '#/login'
  }

  // Admin sees Servers + Users; USER sees only Settings in system items
  const visibleSystemItems = isAdmin ? systemItems : [systemItems[2]]

  return (
    <TooltipProvider delayDuration={0}>
      <aside
        className={cn(
          'border-r bg-sidebar h-screen sticky top-0 flex flex-col transition-all duration-200',
          collapsed ? 'w-16' : 'w-60'
        )}
      >
        {/* Header */}
        <div className="h-14 px-4 flex items-center gap-2 border-b shrink-0">
          <div className="w-8 h-8 rounded-md bg-primary flex items-center justify-center shrink-0">
            <span className="text-white font-bold text-xs">VX</span>
          </div>
          {!collapsed && (
            <span className="font-bold text-lg tracking-wide">VXMM</span>
          )}
          <button
            onClick={toggleCollapse}
            className="ml-auto text-sidebar-foreground hover:text-foreground transition-colors"
            aria-label={collapsed ? 'Mở rộng sidebar' : 'Thu gọn sidebar'}
          >
            {collapsed ? <ChevronsRight className="h-4 w-4" /> : <ChevronsLeft className="h-4 w-4" />}
          </button>
        </div>

        {/* Nav */}
        <nav className="flex-1 overflow-y-auto py-4 px-2 space-y-5" role="navigation">
          <div className="space-y-1">
            {mainItems.map(item => (
              <SidebarItem key={item.to} {...item} collapsed={collapsed} />
            ))}
          </div>

          <div className="space-y-1">
            {!collapsed && (
              <p className="px-3 text-xs font-semibold text-muted-foreground uppercase tracking-wider mb-2">
                Quản lý
              </p>
            )}
            {manageItems.map(item => (
              <SidebarItem key={item.to} {...item} collapsed={collapsed} />
            ))}
          </div>

          <div className="space-y-1">
            {!collapsed && (
              <p className="px-3 text-xs font-semibold text-muted-foreground uppercase tracking-wider mb-2">
                Hệ thống
              </p>
            )}
            {visibleSystemItems.map(item => (
              <SidebarItem key={item.to} {...item} collapsed={collapsed} />
            ))}
          </div>
        </nav>

        {/* Footer */}
        <div className="border-t p-3 shrink-0">
          <div className={cn('flex items-center gap-2', collapsed && 'justify-center')}>
            <Avatar className="h-8 w-8 shrink-0">
              <AvatarFallback className="bg-primary text-white text-xs">
                {user?.username?.[0]?.toUpperCase() ?? 'A'}
              </AvatarFallback>
            </Avatar>
            {!collapsed && (
              <>
                <span className="text-sm font-medium flex-1 truncate">{user?.username ?? 'Admin'}</span>
                <button
                  onClick={handleLogout}
                  className="text-muted-foreground hover:text-destructive transition-colors"
                  aria-label="Đăng xuất"
                >
                  <LogOut className="h-4 w-4" />
                </button>
              </>
            )}
          </div>
        </div>
      </aside>
    </TooltipProvider>
  )
}
