import { getToken, isExpired, decodeJwt, setToken, clearToken } from '@/lib/auth'

export function useAuth() {
  const token = getToken()
  const user = token ? decodeJwt(token) : null
  const authenticated = !!token && !isExpired(token)

  return {
    user,
    authenticated,
    isAdmin: user?.role === 'ADMIN',
    setToken,
    clearToken,
  }
}
