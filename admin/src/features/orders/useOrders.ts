import { useQuery, useMutation, useQueryClient } from '@tanstack/react-query'
import { api, apiList } from '@/lib/api'
import { buildQS, QueryParams } from '@/lib/buildQS'
import { getCurrentUser } from '@/lib/auth'

export interface Order {
  id: number
  serverId: number
  botId: number
  client: number
  name: string
  bot: string
  second: number
  type: number
  timeStart: number
  timeStop: number
  coinOrder: number
  coinWin: number
  coinFee: number
  coinReward: number
  status: number
  winName?: string
  reason?: string
  createdAt: string
  updatedAt: string
}

export interface OrderListParams extends QueryParams {
  ownerId?: number
}

export function useOrders(p: OrderListParams) {
  const role = getCurrentUser()?.role
  const isAdmin = role === 'ADMIN'
  const basePath = isAdmin ? '/admin/orders' : '/me/orders'

  const params: OrderListParams = { ...p }
  if (isAdmin && p.ownerId !== undefined) {
    params.filter = { ...(params.filter as Record<string, unknown>), ownerId: p.ownerId }
  }
  delete params.ownerId

  return useQuery({
    queryKey: ['orders', basePath, params],
    queryFn: () => apiList<Order>(`${basePath}?${buildQS(params)}`),
    refetchInterval: 8000,
    refetchIntervalInBackground: true,
  })
}

export function useOrderDelete() {
  const qc = useQueryClient()
  return useMutation({
    mutationFn: (id: number) => api(`/me/orders/${id}`, { method: 'DELETE' }),
    onSuccess: () => qc.invalidateQueries({ queryKey: ['orders'] }),
  })
}
