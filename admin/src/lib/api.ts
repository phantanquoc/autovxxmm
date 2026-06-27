import { getToken, clearToken } from './auth'

export class ApiError extends Error {
  status: number
  body?: unknown

  constructor(status: number, message: string, body?: unknown) {
    super(message)
    this.name = 'ApiError'
    this.status = status
    this.body = body
  }
}

export interface ListResult<T> {
  items: T[]
  total: number
}

async function fetchWithAuth(path: string, opts: RequestInit = {}): Promise<Response> {
  const headers = new Headers(opts.headers as HeadersInit)
  if (!headers.has('Content-Type') && opts.body) {
    headers.set('Content-Type', 'application/json')
  }
  const token = getToken()
  if (token) headers.set('Authorization', `Bearer ${token}`)

  const res = await fetch(`/api${path}`, { ...opts, headers })

  if (!res.ok) {
    if (res.status === 401) {
      clearToken()
      window.location.hash = '#/login'
    }
    const text = await res.text().catch(() => '')
    throw new ApiError(res.status, text || res.statusText, text)
  }

  return res
}

/** For non-list endpoints: stats, mutations, login, etc. */
export async function api<T>(path: string, opts: RequestInit = {}): Promise<T> {
  const res = await fetchWithAuth(path, opts)
  if (res.status === 204) return undefined as unknown as T
  return res.json() as Promise<T>
}

/** For list endpoints that return an array + Content-Range header. */
export async function apiList<T>(path: string, opts: RequestInit = {}): Promise<ListResult<T>> {
  const res = await fetchWithAuth(path, opts)

  const totalHeader = res.headers.get('Content-Range')
  const total = totalHeader ? parseInt(totalHeader.split('/')[1] ?? '0', 10) : 0

  if (res.status === 204) {
    return { items: [], total: 0 }
  }

  const items = (await res.json()) as T[]
  return { items, total }
}
