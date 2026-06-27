const JWT_KEY = 'vxmm.jwt'

export function getToken(): string | null {
  return localStorage.getItem(JWT_KEY)
}

export function setToken(token: string): void {
  localStorage.setItem(JWT_KEY, token)
}

export function clearToken(): void {
  localStorage.removeItem(JWT_KEY)
}

export interface JwtPayload {
  sub: string
  username?: string
  role?: string
  exp?: number
  iat?: number
}

export function decodeJwt(token: string): JwtPayload | null {
  try {
    const parts = token.split('.')
    if (parts.length !== 3) return null
    const payload = JSON.parse(atob(parts[1].replace(/-/g, '+').replace(/_/g, '/')))
    return payload as JwtPayload
  } catch {
    return null
  }
}

export function isExpired(token: string): boolean {
  const payload = decodeJwt(token)
  if (!payload || !payload.exp) return true
  return Date.now() / 1000 > payload.exp
}

export function getCurrentUser(): JwtPayload | null {
  const token = getToken()
  if (!token) return null
  return decodeJwt(token)
}
