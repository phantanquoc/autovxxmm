import { useQuery, useMutation, useQueryClient } from '@tanstack/react-query'
import { api, apiList } from '@/lib/api'
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
