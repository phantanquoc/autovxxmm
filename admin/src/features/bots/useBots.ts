import { useQuery, useMutation, useQueryClient } from '@tanstack/react-query'
import { api, apiList } from '@/lib/api'
import { buildQS, QueryParams } from '@/lib/buildQS'
import { getCurrentUser } from '@/lib/auth'

export interface Bot {
  id: number
  serverId: number
  account: string
  password: string
  charName: string
  mapId: number
  zoneId: number
  posX: number
  posY: number
  manager: string
  chat: string
  sms: string
  enable: boolean
  playFee: number
  typeLuckyDraw: number
  client: number
  obsName?: string
  obsLevel?: number
  obsClan?: string
  obsCoin?: number
  obsGold?: number
  obsStatus: 'ONLINE' | 'OFFLINE' | 'CONNECTING'
  obsLastOnline: number
  createdAt: string
  updatedAt: string
}

export interface BotListParams extends QueryParams {
  filter?: Record<string, unknown>
  ownerId?: number
}

export function useBots(p: BotListParams) {
  const role = getCurrentUser()?.role
  const isAdmin = role === 'ADMIN'
  const basePath = isAdmin ? '/admin/bots' : '/me/bots'

  const params: BotListParams = { ...p }
  if (isAdmin && p.ownerId !== undefined) {
    // Pass ownerId inside the filter JSON so parseAdminOwnerFilter picks it up
    params.filter = { ...params.filter, ownerId: p.ownerId }
  }
  delete params.ownerId

  return useQuery({
    queryKey: ['bots', basePath, params],
    queryFn: () => apiList<Bot>(`${basePath}?${buildQS(params)}`),
    refetchInterval: 10000,
  })
}

export function useBotMutation() {
  const qc = useQueryClient()
  return useMutation({
    mutationFn: (b: Partial<Bot>) =>
      b.id
        ? api(`/me/bots/${b.id}`, { method: 'PUT', body: JSON.stringify(b) })
        : api('/me/bots', { method: 'POST', body: JSON.stringify(b) }),
    onSuccess: () => qc.invalidateQueries({ queryKey: ['bots'] }),
  })
}

export function useBotDelete() {
  const qc = useQueryClient()
  return useMutation({
    mutationFn: (id: number) => api(`/me/bots/${id}`, { method: 'DELETE' }),
    onSuccess: () => qc.invalidateQueries({ queryKey: ['bots'] }),
  })
}
