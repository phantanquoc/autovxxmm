import { useQuery, useMutation, useQueryClient } from '@tanstack/react-query'
import { api, apiList } from '@/lib/api'
import { buildQS, QueryParams } from '@/lib/buildQS'

export interface Block {
  id: number
  serverId: number
  name: string
  reason?: string
  createdAt: string
}

export function useBlocks(p?: Partial<QueryParams>) {
  const params: QueryParams = { page: 1, pageSize: 50, sort: 'id', order: 'desc', ...p }
  return useQuery({
    queryKey: ['blocks', params],
    queryFn: () => apiList<Block>(`/admin/blocks?${buildQS(params)}`),
  })
}

export function useBlockCreate() {
  const qc = useQueryClient()
  return useMutation({
    mutationFn: (b: { serverId: number; name: string; reason?: string }) =>
      api('/admin/blocks', { method: 'POST', body: JSON.stringify(b) }),
    onSuccess: () => qc.invalidateQueries({ queryKey: ['blocks'] }),
  })
}

export function useBlockDelete() {
  const qc = useQueryClient()
  return useMutation({
    mutationFn: (id: number) => api(`/admin/blocks/${id}`, { method: 'DELETE' }),
    onSuccess: () => qc.invalidateQueries({ queryKey: ['blocks'] }),
  })
}
