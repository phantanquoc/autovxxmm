import { useQuery } from '@tanstack/react-query'
import { apiList } from '@/lib/api'
import { buildQS, QueryParams } from '@/lib/buildQS'
import { getCurrentUser } from '@/lib/auth'

export interface TradeLog {
  id: number
  serverId: number
  name: string
  customer: string
  before: number
  after: number
  change: number
  description: string
  type: number
  timeStart: number
  timeStop: number
  createdAt: string
}

export const TRADE_TYPE_MAP: Record<number, string> = {
  0: 'NHẬN XU',
  1: 'TRẢ THƯỞNG',
  2: 'GOM XU',
}

export interface TradeLogListParams extends QueryParams {
  ownerId?: number
}

export function useTradeLogs(p: TradeLogListParams) {
  const role = getCurrentUser()?.role
  const isAdmin = role === 'ADMIN'
  const basePath = isAdmin ? '/admin/tradeLogs' : '/me/tradeLogs'

  const params: TradeLogListParams = { ...p }
  if (isAdmin && p.ownerId !== undefined) {
    params.filter = { ...(params.filter as Record<string, unknown>), ownerId: p.ownerId }
  }
  delete params.ownerId

  return useQuery({
    queryKey: ['tradeLogs', basePath, params],
    queryFn: () => apiList<TradeLog>(`${basePath}?${buildQS(params)}`),
    refetchInterval: 15000,
    refetchIntervalInBackground: true,
    staleTime: 10000,
  })
}
