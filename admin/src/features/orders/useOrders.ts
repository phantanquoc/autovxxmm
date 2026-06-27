import { useQuery, useMutation, useQueryClient } from '@tanstack/react-query'
import { api, apiList } from '@/lib/api'
import { buildQS, QueryParams } from '@/lib/buildQS'

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

export function useOrders(p: QueryParams) {
  return useQuery({
    queryKey: ['orders', p],
    queryFn: () => apiList<Order>(`/admin/orders?${buildQS(p)}`),
    refetchInterval: 15000,
  })
}

export function useOrderDelete() {
  const qc = useQueryClient()
  return useMutation({
    mutationFn: (id: number) => api(`/admin/orders/${id}`, { method: 'DELETE' }),
    onSuccess: () => qc.invalidateQueries({ queryKey: ['orders'] }),
  })
}
