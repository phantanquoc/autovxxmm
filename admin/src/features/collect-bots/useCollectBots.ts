import { useQuery, useMutation, useQueryClient } from '@tanstack/react-query'
import { api, apiList } from '@/lib/api'
import { buildQS, QueryParams } from '@/lib/buildQS'

export interface CollectBot {
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
  client: number
  role: string
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

export interface CollectTask {
  id: number
  ownerId: number
  status: number
  targets: Array<{
    botId: number
    charName: string
    serverId: number
    coinNow: number
    expectedCollect: number
  }>
  threshold: number
  keep: number
  totalCollected: number
  createdAt: string
  updatedAt: string
  completedAt: string | null
}

export interface ScanResult {
  targets: Array<{
    botId: number
    charName: string
    serverId: number
    coinNow: number
    expectedCollect: number
  }>
  collectBotsAvailable: number
  threshold: number
  keep: number
}

export function useCollectBots(p: QueryParams) {
  return useQuery({
    queryKey: ['collect-bots', p],
    queryFn: () => apiList<CollectBot>(`/me/collect-bots?${buildQS(p)}`),
    refetchInterval: 5000,
    refetchIntervalInBackground: true,
  })
}

export function useCollectBotMutation() {
  const qc = useQueryClient()
  return useMutation({
    mutationFn: (b: Partial<CollectBot>) =>
      b.id
        ? api(`/me/collect-bots/${b.id}`, { method: 'PUT', body: JSON.stringify(b) })
        : api('/me/collect-bots', { method: 'POST', body: JSON.stringify(b) }),
    onSuccess: () => qc.invalidateQueries({ queryKey: ['collect-bots'] }),
  })
}

export function useCollectBotDelete() {
  const qc = useQueryClient()
  return useMutation({
    mutationFn: (id: number) => api(`/me/collect-bots/${id}`, { method: 'DELETE' }),
    onSuccess: () => qc.invalidateQueries({ queryKey: ['collect-bots'] }),
  })
}

export function useScanCollect() {
  return useMutation({
    mutationFn: () => api<ScanResult>('/me/collect/scan', { method: 'POST' }),
  })
}

export function useCreateCollectTask() {
  const qc = useQueryClient()
  return useMutation({
    mutationFn: (botIds: number[]) =>
      api<CollectTask>('/me/collect/tasks', {
        method: 'POST',
        body: JSON.stringify({ botIds }),
      }),
    onSuccess: () => {
      qc.invalidateQueries({ queryKey: ['collect-tasks'] })
      qc.invalidateQueries({ queryKey: ['collect-bots'] })
    },
  })
}

export function useCollectTasks(statusFilter?: number) {
  const query = useQuery({
    queryKey: ['collect-tasks', statusFilter],
    queryFn: () =>
      apiList<CollectTask>(
        `/me/collect/tasks?${statusFilter !== undefined ? `filter=${JSON.stringify({ status: statusFilter })}` : ''}`
      ),
    refetchInterval: (query) => {
      // Poll every 5s when any row is PENDING or IN_PROGRESS so progress updates quickly.
      const items = query.state.data?.items
      if (items && items.some((t) => t.status === 0 || t.status === 1)) return 5000
      // Otherwise poll every 30s to pick up new tasks created elsewhere.
      return 30000
    },
    refetchIntervalInBackground: true,
  })
  return query
}
