import { useState } from 'react'
import { Plus, MoreVertical } from 'lucide-react'
import { toast } from 'sonner'
import { useBots, useBotMutation, useBotDelete, Bot } from './useBots'
import { BotFormDialog } from './BotFormDialog'
import { NINJA_MAPS } from './mapData'
import { useServers } from '@/features/servers/useServers'
import { PageHeader } from '@/components/layout/PageHeader'
import { Card } from '@/components/ui/card'
import { Button } from '@/components/ui/button'
import { Switch } from '@/components/ui/switch'
import { Badge } from '@/components/ui/badge'
import { DataTable } from '@/components/data-table/DataTable'
import { DataTablePagination } from '@/components/data-table/DataTablePagination'
import { DataTableToolbar } from '@/components/data-table/DataTableToolbar'
import { EmptyState } from '@/components/data-table/EmptyState'
import { ServerBadge } from '@/components/domain/ServerBadge'
import { BotStatusDot } from '@/components/domain/BotStatusDot'
import { XuAmount } from '@/components/domain/XuAmount'
import { OwnerFilter } from '@/components/OwnerFilter'
import { getCurrentUser } from '@/lib/auth'
import {
  DropdownMenu,
  DropdownMenuContent,
  DropdownMenuItem,
  DropdownMenuSeparator,
  DropdownMenuTrigger,
} from '@/components/ui/dropdown-menu'
import { formatDateTime } from '@/lib/format'

const STATUS_OPTIONS = [
  { value: 'ONLINE', label: 'Online' },
  { value: 'OFFLINE', label: 'Offline' },
  { value: 'CONNECTING', label: 'Đang kết nối' },
]

const CLIENT_CHOICES = [
  { value: '0', label: 'Normal' },
  { value: '1', label: 'Client 1' },
  { value: '2', label: 'Client 2' },
  { value: '3', label: 'Client 3' },
  { value: '4', label: 'Client 4' },
  { value: '5', label: 'Client 5' },
  { value: '6', label: 'Client 6' },
  { value: '7', label: 'Client 7' },
  { value: '8', label: 'Client 8' },
  { value: '9', label: 'Client 9' },
  { value: '10', label: 'Client 10' },
]

export function BotListPage() {
  const [page, setPage] = useState(1)
  const pageSize = 25
  const [filterValues, setFilterValues] = useState<Record<string, string>>({})
  const [searchQ, setSearchQ] = useState('')
  const [openCreate, setOpenCreate] = useState(false)
  const [editTarget, setEditTarget] = useState<Bot | null>(null)
  const [ownerIdFilter, setOwnerIdFilter] = useState<number | undefined>(undefined)

  const isAdmin = getCurrentUser()?.role === 'ADMIN'

  const { data: serverResult } = useServers()
  const servers = serverResult?.items ?? []
  const serverOptions = servers.map(s => ({ value: String(s.id), label: s.name }))

  const filter: Record<string, unknown> = {}
  if (searchQ) filter.q = searchQ
  Object.entries(filterValues).forEach(([k, v]) => {
    if (!v) return
    if (k === 'enable') {
      filter[k] = v === 'true'
    } else if (k === 'client') {
      filter[k] = Number(v)
    } else {
      filter[k] = v
    }
  })

  const { data: botResult, isLoading } = useBots({
    page,
    pageSize,
    sort: 'id',
    order: 'asc',
    filter,
    ownerId: ownerIdFilter,
  })
  const bots = botResult?.items ?? []
  const total = botResult?.total ?? 0

  const mutation = useBotMutation()
  const deleteMutation = useBotDelete()

  function handleFilter(key: string, value: string) {
    setFilterValues(prev => {
      const next = { ...prev }
      if (value) next[key] = value
      else delete next[key]
      return next
    })
    setPage(1)
  }

  async function toggleEnable(id: number, value: boolean) {
    if (isAdmin) return // admin cannot mutate
    try {
      await mutation.mutateAsync({ id, enable: value })
    } catch (err: unknown) {
      toast.error(err instanceof Error ? err.message : 'Có lỗi xảy ra')
    }
  }

  async function handleDelete(bot: Bot) {
    if (!confirm(`Xoá bot "${bot.account}"?`)) return
    try {
      await deleteMutation.mutateAsync(bot.id)
      toast.success('Đã xoá bot')
    } catch (err: unknown) {
      toast.error(err instanceof Error ? err.message : 'Có lỗi xảy ra')
    }
  }

  async function handleDuplicate(bot: Bot) {
    try {
      const { id: _id, obsName: _on, obsLevel: _ol, obsClan: _oc, obsCoin: _oco, obsGold: _og, obsStatus: _os, obsLastOnline: _olo, createdAt: _ca, updatedAt: _ua, ...rest } = bot
      await mutation.mutateAsync({ ...rest, account: `${bot.account}_copy` })
      toast.success('Đã nhân bản bot')
    } catch (err: unknown) {
      toast.error(err instanceof Error ? err.message : 'Có lỗi xảy ra')
    }
  }

  return (
    <div>
      <PageHeader
        title="Bot"
        actions={
          !isAdmin ? (
            <Button onClick={() => setOpenCreate(true)}>
              <Plus className="mr-2 h-4 w-4" />
              Thêm bot
            </Button>
          ) : undefined
        }
      />

      {isAdmin && (
        <div className="mb-4 flex items-center gap-2">
          <span className="text-sm text-muted-foreground">Người dùng:</span>
          <OwnerFilter value={ownerIdFilter} onChange={(v) => { setOwnerIdFilter(v); setPage(1) }} />
        </div>
      )}

      <Card>
        <DataTableToolbar
          searchPlaceholder="Tìm acc/char/manager..."
          onSearch={q => { setSearchQ(q); setPage(1) }}
          filters={[
            { type: 'select', key: 'serverId', label: 'Server', options: serverOptions },
            { type: 'select', key: 'obsStatus', label: 'Trạng thái', options: STATUS_OPTIONS },
            { type: 'select', key: 'client', label: 'Client', options: CLIENT_CHOICES },
            { type: 'boolean', key: 'enable', label: 'Bật' },
          ]}
          onFilter={handleFilter}
          filterValues={filterValues}
        />

        <DataTable
          rowKey={r => r.id}
          data={bots}
          isLoading={isLoading}
          emptyState={
            <EmptyState
              title="Chưa có bot"
              action={!isAdmin ? <Button onClick={() => setOpenCreate(true)}>Thêm bot</Button> : undefined}
            />
          }
          columns={[
            {
              header: 'Tài khoản',
              cell: (b) => {
                const server = servers.find(s => s.id === b.serverId)
                return (
                  <div className="flex items-center gap-3">
                    <ServerBadge id={b.serverId} />
                    <div className="flex flex-col">
                      <span className="font-medium text-sm">#{b.id} - {b.account}</span>
                      <span className="text-xs text-muted-foreground">{server?.name ?? 'SV' + b.serverId}</span>
                    </div>
                  </div>
                )
              },
            },
            {
              header: 'Quản lý',
              cell: (b) => b.manager || <em className="text-muted-foreground text-xs">—</em>,
            },
            {
              header: 'Nhân vật',
              cell: (b) => (
                <div className="flex items-center gap-2">
                  <BotStatusDot status={b.obsStatus} />
                  <span className="text-sm">{b.obsName || b.charName || '—'}</span>
                </div>
              ),
            },
            {
              header: 'Vị trí',
              cell: (b) => {
                const mapName = NINJA_MAPS.find(m => m.id === b.mapId)?.name
                return (
                  <div className="flex flex-col text-xs">
                    <span>{mapName ? `${mapName} (${b.mapId})` : `Map ${b.mapId}`}</span>
                    <span className="text-muted-foreground">Khu vực {b.zoneId}</span>
                  </div>
                )
              },
            },
            {
              header: 'Xu / Lượng',
              cell: (b) => (
                <div className="flex flex-col text-xs font-mono">
                  <XuAmount value={b.obsCoin ?? 0} />
                  <span className="text-muted-foreground">{(b.obsGold ?? 0).toLocaleString()} lượng</span>
                </div>
              ),
            },
            {
              header: 'Bật',
              cell: (b) => (
                <Switch
                  checked={b.enable}
                  onCheckedChange={v => toggleEnable(b.id, v)}
                  disabled={isAdmin}
                  aria-label={`Bot ${b.account} ${b.enable ? 'đang bật' : 'đang tắt'}`}
                />
              ),
              width: '60px',
            },
            {
              header: 'Cập nhật',
              cell: (b) => (
                <Badge variant="teal" className="text-xs whitespace-nowrap">
                  {formatDateTime(b.updatedAt)}
                </Badge>
              ),
            },
            ...(!isAdmin ? [{
              header: '',
              cell: (b: Bot) => (
                <DropdownMenu>
                  <DropdownMenuTrigger asChild>
                    <Button variant="ghost" size="icon" aria-label="Tuỳ chọn">
                      <MoreVertical className="h-4 w-4" />
                    </Button>
                  </DropdownMenuTrigger>
                  <DropdownMenuContent align="end">
                    <DropdownMenuItem onSelect={() => setEditTarget(b)}>Chỉnh sửa</DropdownMenuItem>
                    <DropdownMenuItem onSelect={() => handleDuplicate(b)}>Nhân bản</DropdownMenuItem>
                    <DropdownMenuSeparator />
                    <DropdownMenuItem onSelect={() => handleDelete(b)} className="text-destructive">
                      Xoá
                    </DropdownMenuItem>
                  </DropdownMenuContent>
                </DropdownMenu>
              ),
              width: '50px',
            }] : []),
          ]}
        />

        <DataTablePagination
          page={page}
          pageSize={pageSize}
          total={total}
          onPageChange={setPage}
        />
      </Card>

      {!isAdmin && (
        <>
          <BotFormDialog open={openCreate} onClose={() => setOpenCreate(false)} />
          {editTarget && (
            <BotFormDialog
              open
              onClose={() => setEditTarget(null)}
              initialData={editTarget}
            />
          )}
        </>
      )}
    </div>
  )
}
