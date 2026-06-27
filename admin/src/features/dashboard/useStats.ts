import { useQuery } from '@tanstack/react-query'
import { api } from '@/lib/api'

interface StatsData {
  bots: { total: number; online: number }
  orders: { total: number; active: number; last24h: number }
  tradeLogs: { total: number; last24h: number }
}

export function useStats() {
  return useQuery({
    queryKey: ['stats'],
    queryFn: () => api<StatsData>('/admin/stats'),
    refetchInterval: 30000,
  })
}
