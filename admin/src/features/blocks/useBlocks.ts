import { useQuery, useMutation, useQueryClient } from '@tanstack/react-query'
import { api, apiList } from '@/lib/api'
import { buildQS, QueryParams } from '@/lib/buildQS'
import { getCurrentUser } from '@/lib/auth'

export interface Block {
  id: number
  serverId: number
  name: string
  reason?: string
  createdAt: string
}

export interface BlockListParams extends Partial<QueryParams> {
  ownerId?: number
}

export function useBlocks(p?: BlockListParams) {
  const role = getCurrentUser()?.role
  const isAdmin = role === 'ADMIN'
  const basePath = isAdmin ? '/admin/blocks' : '/me/blocks'

  const params: BlockListParams = { page: 1, pageSize: 50, sort: 'id', order: 'desc', ...p }
  if (isAdmin && p?.ownerId !== undefined) {
    params.filter = { ...(params.filter as Record<string, unknown>), ownerId: p.ownerId }
  }
  delete params.ownerId

  return useQuery({
    queryKey: ['blocks', basePath, params],
    queryFn: () => apiList<Block>(`${basePath}?${buildQS(params)}`),
  })
}

export function useBlockCreate() {
  const qc = useQueryClient()
  return useMutation({
    mutationFn: (b: { serverId: number; name: string; reason?: string }) =>
      api('/me/blocks', { method: 'POST', body: JSON.stringify(b) }),
    onSuccess: () => qc.invalidateQueries({ queryKey: ['blocks'] }),
  })
}

export function useBlockDelete() {
  const qc = useQueryClient()
  return useMutation({
    mutationFn: (id: number) => api(`/me/blocks/${id}`, { method: 'DELETE' }),
    onSuccess: () => qc.invalidateQueries({ queryKey: ['blocks'] }),
  })
}
