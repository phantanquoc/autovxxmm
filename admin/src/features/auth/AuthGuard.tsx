import { ReactNode } from 'react'
import { Navigate } from 'react-router-dom'
import { getToken, isExpired, decodeJwt } from '@/lib/auth'

interface AuthGuardProps {
  children: ReactNode
}

export function AuthGuard({ children }: AuthGuardProps) {
  const token = getToken()

  if (!token || isExpired(token)) {
    return <Navigate to="/login" replace />
  }

  const payload = decodeJwt(token)
  if (!payload || payload.role !== 'ADMIN') {
    return <AccessDenied />
  }

  return <>{children}</>
}

export function AccessDenied() {
  function handleLogout() {
    localStorage.clear()
    window.location.hash = '#/login'
  }

  return (
    <div className="min-h-screen flex items-center justify-center bg-muted">
      <div className="text-center space-y-4">
        <h1 className="text-2xl font-bold text-destructive">Truy cập bị từ chối</h1>
        <p className="text-muted-foreground">Bạn không có quyền truy cập trang này.</p>
        <button onClick={handleLogout} className="text-primary underline text-sm">
          Đăng xuất
        </button>
      </div>
    </div>
  )
}
