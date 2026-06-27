import { useQuery, useMutation, useQueryClient } from '@tanstack/react-query'
import { api, apiList } from '@/lib/api'
import { buildQS, QueryParams } from '@/lib/buildQS'

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
}

export function useBots(p: BotListParams) {
  return useQuery({
    queryKey: ['bots', p],
    queryFn: () => apiList<Bot>(`/admin/bots?${buildQS(p)}`),
    refetchInterval: 10000,
  })
}

export function useBotMutation() {
  const qc = useQueryClient()
  return useMutation({
    mutationFn: (b: Partial<Bot>) =>
      b.id
        ? api(`/admin/bots/${b.id}`, { method: 'PUT', body: JSON.stringify(b) })
        : api('/admin/bots', { method: 'POST', body: JSON.stringify(b) }),
    onSuccess: () => qc.invalidateQueries({ queryKey: ['bots'] }),
  })
}

export function useBotDelete() {
  const qc = useQueryClient()
  return useMutation({
    mutationFn: (id: number) => api(`/admin/bots/${id}`, { method: 'DELETE' }),
    onSuccess: () => qc.invalidateQueries({ queryKey: ['bots'] }),
  })
}
