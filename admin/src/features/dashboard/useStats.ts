import { useQuery } from '@tanstack/react-query'
import { api } from '@/lib/api'
import { getCurrentUser } from '@/lib/auth'

interface StatsData {
  bots: { total: number; online: number }
  orders: { total: number; active: number; last24h: number }
  tradeLogs: { total: number; last24h: number }
  coins: { totalBet: number; totalCollected: number }
}

export function useStats(ownerId?: number) {
  const role = getCurrentUser()?.role
  const isAdmin = role === 'ADMIN'

  const url = isAdmin
    ? ownerId !== undefined
      ? `/admin/stats?ownerId=${ownerId}`
      : '/admin/stats'
    : '/me/stats'

  return useQuery({
    queryKey: ['stats', url],
    queryFn: () => api<StatsData>(url),
    refetchInterval: 30000,
  })
}
