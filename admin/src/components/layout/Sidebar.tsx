import { useEffect, useRef } from 'react'
import { NavLink, useLocation } from 'react-router-dom'
import {
  LayoutDashboard, Bot, ShoppingCart, Coins, Ban, Server, Users, Settings,
  ChevronsLeft, ChevronsRight, LogOut, Receipt, X
} from 'lucide-react'
import { cn } from '@/lib/utils'
import { clearToken, getCurrentUser } from '@/lib/auth'
import { Avatar, AvatarFallback } from '@/components/ui/avatar'
import { Tooltip, TooltipContent, TooltipTrigger, TooltipProvider } from '@/components/ui/tooltip'
import { useSidebar } from '@/hooks/useSidebar'

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

function SidebarItem({ icon: Icon, to, label, collapsed, onNavigate }: NavItem & { collapsed: boolean; onNavigate?: () => void }) {
  const location = useLocation()
  const isActive = to === '/' ? location.pathname === '/' : location.pathname.startsWith(to)

  const content = (
    <NavLink
      to={to}
      onClick={onNavigate}
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
  const { collapsed, toggleCollapse, isDrawerOpen, closeDrawer } = useSidebar()
  const drawerRef = useRef<HTMLElement>(null)
  const previousFocusRef = useRef<HTMLElement | null>(null)

  const user = getCurrentUser()
  const isAdmin = user?.role === 'ADMIN'

  function handleLogout() {
    clearToken()
    window.location.hash = '#/login'
  }

  // Admin sees Servers + Users; USER sees only Settings in system items
  const visibleSystemItems = isAdmin ? systemItems : [systemItems[2]]

  // Focus trap: when drawer opens, focus the drawer; when it closes, return focus
  useEffect(() => {
    if (isDrawerOpen) {
      previousFocusRef.current = document.activeElement as HTMLElement | null
      // Small delay to allow transition to start
      requestAnimationFrame(() => {
        drawerRef.current?.focus()
      })
    } else if (previousFocusRef.current) {
      previousFocusRef.current.focus()
      previousFocusRef.current = null
    }
  }, [isDrawerOpen])

  // On mobile, drawer uses expanded (not collapsed) nav items
  const mobileCollapsed = false

  const sidebarContent = (showCollapsed: boolean, onItemClick?: () => void) => (
    <>
      {/* Header */}
      <div className="h-14 px-4 flex items-center gap-2 border-b shrink-0">
        <div className="w-8 h-8 rounded-md bg-primary flex items-center justify-center shrink-0">
          <span className="text-white font-bold text-xs">VX</span>
        </div>
        {!showCollapsed && (
          <span className="font-bold text-lg tracking-wide">VXMM</span>
        )}
        {/* Desktop: collapse toggle; Mobile: close button */}
        <button
          onClick={onItemClick ? closeDrawer : toggleCollapse}
          className="ml-auto text-sidebar-foreground hover:text-foreground transition-colors"
          aria-label={onItemClick ? 'Đóng menu' : (showCollapsed ? 'Mở rộng sidebar' : 'Thu gọn sidebar')}
        >
          {onItemClick ? (
            <X className="h-4 w-4" />
          ) : showCollapsed ? (
            <ChevronsRight className="h-4 w-4" />
          ) : (
            <ChevronsLeft className="h-4 w-4" />
          )}
        </button>
      </div>

      {/* Nav */}
      <nav className="flex-1 overflow-y-auto py-4 px-2 space-y-5" role="navigation">
        <div className="space-y-1">
          {mainItems.map(item => (
            <SidebarItem key={item.to} {...item} collapsed={showCollapsed} onNavigate={onItemClick} />
          ))}
        </div>

        <div className="space-y-1">
          {!showCollapsed && (
            <p className="px-3 text-xs font-semibold text-muted-foreground uppercase tracking-wider mb-2">
              Quản lý
            </p>
          )}
          {manageItems.map(item => (
            <SidebarItem key={item.to} {...item} collapsed={showCollapsed} onNavigate={onItemClick} />
          ))}
        </div>

        <div className="space-y-1">
          {!showCollapsed && (
            <p className="px-3 text-xs font-semibold text-muted-foreground uppercase tracking-wider mb-2">
              Hệ thống
            </p>
          )}
          {visibleSystemItems.map(item => (
            <SidebarItem key={item.to} {...item} collapsed={showCollapsed} onNavigate={onItemClick} />
          ))}
        </div>
      </nav>

      {/* Footer */}
      <div className="border-t p-3 shrink-0">
        <div className={cn('flex items-center gap-2', showCollapsed && 'justify-center')}>
          <Avatar className="h-8 w-8 shrink-0">
            <AvatarFallback className="bg-primary text-white text-xs">
              {user?.username?.[0]?.toUpperCase() ?? 'A'}
            </AvatarFallback>
          </Avatar>
          {!showCollapsed && (
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
    </>
  )

  return (
    <TooltipProvider delayDuration={0}>
      {/* Mobile: off-canvas drawer */}
      {isDrawerOpen && (
        <div
          className="fixed inset-0 z-40 bg-black/50 md:hidden"
          onClick={closeDrawer}
          aria-hidden="true"
        />
      )}
      <aside
        ref={drawerRef}
        tabIndex={-1}
        role={isDrawerOpen ? 'dialog' : undefined}
        aria-modal={isDrawerOpen ? true : undefined}
        aria-label={isDrawerOpen ? 'Menu điều hướng' : undefined}
        className={cn(
          // Mobile: fixed off-canvas drawer
          'fixed inset-y-0 left-0 z-50 w-60 bg-sidebar flex flex-col border-r',
          'transition-transform duration-200 ease-in-out motion-reduce:transition-none',
          isDrawerOpen ? 'translate-x-0' : '-translate-x-full',
          // Desktop: static sidebar in flex layout
          'md:static md:translate-x-0 md:transition-[width] md:duration-200',
          'md:h-screen md:sticky md:top-0',
          collapsed ? 'md:w-16' : 'md:w-60'
        )}
      >
        {/* On mobile (drawer), always show expanded content */}
        <div className="flex flex-col h-full md:hidden">
          {sidebarContent(mobileCollapsed, closeDrawer)}
        </div>
        {/* On desktop, show collapsible content */}
        <div className="hidden md:flex md:flex-col md:h-full">
          {sidebarContent(collapsed)}
        </div>
      </aside>
    </TooltipProvider>
  )
}
