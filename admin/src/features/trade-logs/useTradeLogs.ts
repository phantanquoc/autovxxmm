import { useQuery } from '@tanstack/react-query'
import { apiList } from '@/lib/api'
import { buildQS, QueryParams } from '@/lib/buildQS'

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

export function useTradeLogs(p: QueryParams) {
  return useQuery({
    queryKey: ['tradeLogs', p],
    queryFn: () => apiList<TradeLog>(`/admin/tradeLogs?${buildQS(p)}`),
    staleTime: 10000,
  })
}
