import { Moon, Sun, LogOut, Menu } from 'lucide-react'
import { Button } from '@/components/ui/button'
import { Avatar, AvatarFallback } from '@/components/ui/avatar'
import {
  DropdownMenu,
  DropdownMenuContent,
  DropdownMenuItem,
  DropdownMenuSeparator,
  DropdownMenuTrigger,
} from '@/components/ui/dropdown-menu'
import { clearToken, getCurrentUser } from '@/lib/auth'
import { useTheme } from '@/hooks/useTheme'
import { useSidebar } from '@/hooks/useSidebar'
import { useNavigate } from 'react-router-dom'

export function Topbar() {
  const { theme, toggleTheme } = useTheme()
  const { isDrawerOpen, openDrawer } = useSidebar()
  const user = getCurrentUser()
  const navigate = useNavigate()

  function handleLogout() {
    clearToken()
    navigate('/login')
  }

  return (
    <header className="h-14 px-4 md:px-6 border-b bg-card sticky top-0 z-10 flex items-center gap-4 shrink-0">
      <Button
        variant="ghost"
        size="icon"
        className="md:hidden"
        onClick={openDrawer}
        aria-label="Mở menu"
        aria-expanded={isDrawerOpen}
      >
        <Menu className="h-5 w-5" />
      </Button>
      <div className="flex-1" />
      <div className="flex items-center gap-2">
        <Button
          variant="ghost"
          size="icon"
          onClick={toggleTheme}
          aria-label={theme === 'dark' ? 'Chuyển sáng' : 'Chuyển tối'}
        >
          {theme === 'dark' ? <Sun className="h-4 w-4" /> : <Moon className="h-4 w-4" />}
        </Button>

        <DropdownMenu>
          <DropdownMenuTrigger asChild>
            <Button variant="ghost" className="flex items-center gap-2 h-9 px-2">
              <Avatar className="h-7 w-7">
                <AvatarFallback className="bg-primary text-white text-xs">
                  {user?.username?.[0]?.toUpperCase() ?? 'A'}
                </AvatarFallback>
              </Avatar>
              <span className="text-sm font-medium hidden md:block">{user?.username ?? 'Admin'}</span>
            </Button>
          </DropdownMenuTrigger>
          <DropdownMenuContent align="end" className="w-48">
            <DropdownMenuItem onClick={() => navigate('/settings')}>Cài đặt</DropdownMenuItem>
            <DropdownMenuSeparator />
            <DropdownMenuItem onClick={handleLogout} className="text-destructive">
              <LogOut className="mr-2 h-4 w-4" />
              Đăng xuất
            </DropdownMenuItem>
          </DropdownMenuContent>
        </DropdownMenu>
      </div>
    </header>
  )
}
