import { useQuery, useMutation, useQueryClient } from '@tanstack/react-query'
import { api, apiList, type ListResult } from '@/lib/api'
import { buildQS, QueryParams } from '@/lib/buildQS'

export interface Server {
  id: number
  name: string
  ip: string
  port: number
  type: number
}

export function useServers(params?: Partial<QueryParams>) {
  const p: QueryParams = { page: 1, pageSize: 100, sort: 'id', order: 'asc', ...params }
  return useQuery({
    queryKey: ['servers', p],
    queryFn: () => apiList<Server>(`/admin/servers?${buildQS(p)}`),
  })
}

// Public-to-any-authenticated-user list of servers, served by /api/resource/server.
// Use this in any USER-facing form/dropdown — /admin/servers is ADMIN-only.
export function useResourceServers() {
  return useQuery({
    queryKey: ['resource-servers'],
    queryFn: async (): Promise<ListResult<Server>> => {
      const items = await api<Server[]>('/resource/server')
      return { items, total: items.length }
    },
    staleTime: 5 * 60 * 1000,
  })
}

export function useServerMutation() {
  const qc = useQueryClient()
  return useMutation({
    mutationFn: (s: Partial<Server>) =>
      s.id
        ? api(`/admin/servers/${s.id}`, { method: 'PUT', body: JSON.stringify(s) })
        : api('/admin/servers', { method: 'POST', body: JSON.stringify(s) }),
    onSuccess: () => qc.invalidateQueries({ queryKey: ['servers'] }),
  })
}

export function useServerDelete() {
  const qc = useQueryClient()
  return useMutation({
    mutationFn: (id: number) => api(`/admin/servers/${id}`, { method: 'DELETE' }),
    onSuccess: () => qc.invalidateQueries({ queryKey: ['servers'] }),
  })
}
