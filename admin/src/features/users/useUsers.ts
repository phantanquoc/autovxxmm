import { useQuery, useMutation, useQueryClient } from '@tanstack/react-query'
import { api, apiList } from '@/lib/api'
import { buildQS, QueryParams } from '@/lib/buildQS'

export interface AdminUser {
  id: number
  username: string
  role: 'ADMIN' | 'USER'
  enabled: boolean
  createdAt: string
  updatedAt: string
}

export function useUsers(p?: Partial<QueryParams>) {
  const params: QueryParams = { page: 1, pageSize: 50, sort: 'id', order: 'asc', ...p }
  return useQuery({
    queryKey: ['users', params],
    queryFn: () => apiList<AdminUser>(`/admin/users?${buildQS(params)}`),
  })
}

export function useUserMutation() {
  const qc = useQueryClient()
  return useMutation({
    mutationFn: (u: Partial<AdminUser> & { password?: string }) =>
      u.id
        ? api(`/admin/users/${u.id}`, { method: 'PUT', body: JSON.stringify(u) })
        : api('/admin/users', { method: 'POST', body: JSON.stringify(u) }),
    onSuccess: () => qc.invalidateQueries({ queryKey: ['users'] }),
  })
}

export function useUserDelete() {
  const qc = useQueryClient()
  return useMutation({
    mutationFn: (id: number) => api(`/admin/users/${id}`, { method: 'DELETE' }),
    onSuccess: () => qc.invalidateQueries({ queryKey: ['users'] }),
  })
}
