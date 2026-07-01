import { useState } from 'react'
import { Plus, MoreVertical, Coins } from 'lucide-react'
import { toast } from 'sonner'
import { useQueryClient } from '@tanstack/react-query'
import {
  useCollectBots, useCollectBotMutation, useCollectBotDelete, useCollectTasks,
  useScanCollect, useCreateCollectTask, CollectBot, CollectTask, ScanResult,
} from './useCollectBots'
import { CollectBotFormDialog } from './CollectBotFormDialog'
import { useResourceServers } from '@/features/servers/useServers'
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
import {
  Dialog, DialogContent, DialogHeader, DialogTitle, DialogFooter, DialogDescription,
} from '@/components/ui/dialog'
import {
  DropdownMenu, DropdownMenuContent, DropdownMenuItem,
  DropdownMenuSeparator, DropdownMenuTrigger,
} from '@/components/ui/dropdown-menu'
import { formatDateTime } from '@/lib/format'

const STATUS_OPTIONS = [
  { value: 'ONLINE', label: 'Online' },
  { value: 'OFFLINE', label: 'Offline' },
  { value: 'CONNECTING', label: 'Đang kết nối' },
]

function collectStatusBadge(status: number) {
  switch (status) {
    case 0: return <Badge variant="secondary">PENDING</Badge>
    case 1: return <Badge variant="warning">IN_PROGRESS</Badge>
    case 2: return <Badge variant="success">DONE</Badge>
    case 3: return <Badge variant="danger">FAILED</Badge>
    case 4: return <Badge variant="outline">CANCELLED</Badge>
    default: return <Badge variant="outline">{status}</Badge>
  }
}

interface ScanDialogProps {
  open: boolean
  onClose: () => void
  scanResult: ScanResult | null
  onConfirm: (botIds: number[]) => void
  isLoading: boolean
}

function ScanConfirmDialog({ open, onClose, scanResult, onConfirm, isLoading }: ScanDialogProps) {
  const totalExpected = scanResult?.targets.reduce((s, t) => s + t.expectedCollect, 0) ?? 0

  function handleConfirm() {
    if (!scanResult) return
    onConfirm(scanResult.targets.map(t => t.botId))
  }

  return (
    <Dialog open={open} onOpenChange={v => !v && onClose()}>
      <DialogContent className="max-w-2xl max-h-[90vh] overflow-y-auto">
        <DialogHeader>
          <DialogTitle>Xác nhận gom xu</DialogTitle>
          <DialogDescription>
            {scanResult && (
              <span>
                Bot gom xu sẵn có: <strong>{scanResult.collectBotsAvailable}</strong> |{' '}
                Ngưỡng: <strong>{scanResult.threshold.toLocaleString('vi-VN')} xu</strong> |{' '}
                Để lại: <strong>{scanResult.keep.toLocaleString('vi-VN')} xu</strong>
              </span>
            )}
          </DialogDescription>
        </DialogHeader>

        {scanResult && scanResult.targets.length === 0 ? (
          <p className="text-sm text-muted-foreground py-4">Không có bot nào vượt ngưỡng hiện tại.</p>
        ) : (
          <div className="space-y-2 max-h-64 overflow-y-auto">
            <table className="w-full text-sm">
              <thead>
                <tr className="text-muted-foreground text-xs border-b">
                  <th className="text-left py-1">Nhân vật</th>
                  <th className="text-right py-1">Xu hiện tại</th>
                  <th className="text-right py-1">Gom được</th>
                </tr>
              </thead>
              <tbody>
                {scanResult?.targets.map(t => (
                  <tr key={t.botId} className="border-b last:border-0">
                    <td className="py-1">{t.charName} <span className="text-muted-foreground text-xs">(SV{t.serverId})</span></td>
                    <td className="text-right py-1 font-mono">{t.coinNow.toLocaleString('vi-VN')}</td>
                    <td className="text-right py-1 font-mono text-green-600">{t.expectedCollect.toLocaleString('vi-VN')}</td>
                  </tr>
                ))}
              </tbody>
            </table>
            <div className="flex justify-end text-sm font-semibold pt-1">
              Tổng gom: {totalExpected.toLocaleString('vi-VN')} xu
            </div>
          </div>
        )}

        <DialogFooter>
          <Button variant="outline" onClick={onClose}>Huỷ</Button>
          <Button
            onClick={handleConfirm}
            disabled={isLoading || !scanResult || scanResult.targets.length === 0}
          >
            {isLoading ? 'Đang tạo...' : 'Gom xu'}
          </Button>
        </DialogFooter>
      </DialogContent>
    </Dialog>
  )
}

export function CollectBotsListPage() {
  const [page, setPage] = useState(1)
  const pageSize = 25
  const [filterValues, setFilterValues] = useState<Record<string, string>>({})
  const [searchQ, setSearchQ] = useState('')
  const [openCreate, setOpenCreate] = useState(false)
  const [editTarget, setEditTarget] = useState<CollectBot | null>(null)
  const [scanOpen, setScanOpen] = useState(false)
  const [scanResult, setScanResult] = useState<ScanResult | null>(null)

  const { data: serverResult } = useResourceServers()
  const servers = serverResult?.items ?? []
  const serverOptions = servers.map(s => ({ value: String(s.id), label: s.name }))

  const filter: Record<string, unknown> = {}
  if (searchQ) filter.q = searchQ
  Object.entries(filterValues).forEach(([k, v]) => {
    if (!v) return
    if (k === 'obsStatus') filter[k] = v
    else if (k === 'serverId') filter[k] = Number(v)
    else filter[k] = v
  })

  const { data: botResult, isLoading } = useCollectBots({ page, pageSize, sort: 'id', order: 'asc', filter })
  const bots = botResult?.items ?? []
  const total = botResult?.total ?? 0

  const { data: taskResult } = useCollectTasks()
  const tasks = taskResult?.items ?? []

  const mutation = useCollectBotMutation()
  const deleteMutation = useCollectBotDelete()
  const scanMutation = useScanCollect()
  const createTaskMutation = useCreateCollectTask()
  const qc = useQueryClient()

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
    try {
      await mutation.mutateAsync({ id, enable: value })
    } catch (err: unknown) {
      toast.error(err instanceof Error ? err.message : 'Có lỗi xảy ra')
    }
  }

  async function handleDelete(bot: CollectBot) {
    if (!confirm(`Xoá bot gom xu "${bot.account}"?`)) return
    try {
      await deleteMutation.mutateAsync(bot.id)
      toast.success('Đã xoá bot gom xu')
    } catch (err: unknown) {
      toast.error(err instanceof Error ? err.message : 'Có lỗi xảy ra')
    }
  }

  async function handleScan() {
    try {
      // Refresh bot list cache trước khi quét để hiển thị xu mới nhất khi đối chiếu.
      qc.invalidateQueries({ queryKey: ['bots'] })
      qc.invalidateQueries({ queryKey: ['collect-bots'] })
      const result = await scanMutation.mutateAsync()
      setScanResult(result)
      setScanOpen(true)
    } catch (err: unknown) {
      toast.error(err instanceof Error ? err.message : 'Có lỗi khi quét')
    }
  }

  async function handleConfirmTask(botIds: number[]) {
    try {
      await createTaskMutation.mutateAsync(botIds)
      toast.success('Đã tạo lệnh gom xu')
      setScanOpen(false)
      setScanResult(null)
    } catch (err: unknown) {
      const msg = err instanceof Error ? err.message : 'Có lỗi xảy ra'
      // 400 = no bot crosses threshold
      if (msg.includes('400') || msg.toLowerCase().includes('threshold')) {
        toast.error('Không có bot nào vượt ngưỡng hiện tại.')
      } else {
        toast.error(msg)
      }
    }
  }

  return (
    <div>
      <PageHeader
        title="Bot gom xu"
        actions={
          <div className="flex gap-2">
            <Button variant="outline" onClick={handleScan} disabled={scanMutation.isPending}>
              <Coins className="mr-2 h-4 w-4" />
              {scanMutation.isPending ? 'Đang quét...' : 'Gom xu'}
            </Button>
            <Button onClick={() => setOpenCreate(true)}>
              <Plus className="mr-2 h-4 w-4" />
              Thêm bot gom xu
            </Button>
          </div>
        }
      />

      <Card>
        <DataTableToolbar
          searchPlaceholder="Tìm acc/char/manager..."
          onSearch={q => { setSearchQ(q); setPage(1) }}
          filters={[
            { type: 'select', key: 'serverId', label: 'Server', options: serverOptions },
            { type: 'select', key: 'obsStatus', label: 'Trạng thái', options: STATUS_OPTIONS },
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
              title="Chưa có bot gom xu"
              action={<Button onClick={() => setOpenCreate(true)}>Thêm bot gom xu</Button>}
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
              header: 'Nhân vật',
              cell: (b) => (
                <div className="flex items-center gap-2">
                  <BotStatusDot status={b.obsStatus} />
                  <span className="text-sm">{b.obsName || b.charName || b.account || '—'}</span>
                </div>
              ),
            },
            {
              header: 'Xu',
              cell: (b) => <XuAmount value={b.obsCoin ?? 0} />,
            },
            {
              header: 'Cập nhật',
              cell: (b) => (
                <Badge variant="teal" className="text-xs whitespace-nowrap">
                  {formatDateTime(b.updatedAt)}
                </Badge>
              ),
            },
            {
              header: 'Bật',
              cell: (b) => (
                <Switch
                  checked={b.enable}
                  onCheckedChange={v => toggleEnable(b.id, v)}
                  aria-label={`Bot ${b.account} ${b.enable ? 'đang bật' : 'đang tắt'}`}
                />
              ),
              width: '60px',
            },
            {
              header: '',
              cell: (b) => (
                <DropdownMenu>
                  <DropdownMenuTrigger asChild>
                    <Button variant="ghost" size="icon" aria-label="Tuỳ chọn">
                      <MoreVertical className="h-4 w-4" />
                    </Button>
                  </DropdownMenuTrigger>
                  <DropdownMenuContent align="end">
                    <DropdownMenuItem onSelect={() => setEditTarget(b)}>Chỉnh sửa</DropdownMenuItem>
                    <DropdownMenuSeparator />
                    <DropdownMenuItem onSelect={() => handleDelete(b)} className="text-destructive">
                      Xoá
                    </DropdownMenuItem>
                  </DropdownMenuContent>
                </DropdownMenu>
              ),
              width: '50px',
            },
          ]}
        />

        <DataTablePagination
          page={page}
          pageSize={pageSize}
          total={total}
          onPageChange={setPage}
        />
      </Card>

      {/* Collect task history */}
      <div className="mt-6">
        <h2 className="text-lg font-semibold mb-3">Lịch sử gom xu</h2>
        <Card>
          <DataTable
            rowKey={r => r.id}
            data={tasks}
            isLoading={false}
            emptyState={<EmptyState title="Chưa có lịch sử gom xu" />}
            columns={[
              { header: '#', cell: (t: CollectTask) => t.id },
              { header: 'Trạng thái', cell: (t: CollectTask) => collectStatusBadge(t.status) },
              {
                header: 'Ngưỡng / Để lại',
                cell: (t: CollectTask) => (
                  <span className="text-xs font-mono">
                    {t.threshold.toLocaleString('vi-VN')} / {t.keep.toLocaleString('vi-VN')}
                  </span>
                ),
              },
              {
                header: 'Đã gom',
                cell: (t: CollectTask) => (
                  <span className="font-mono text-green-600">{t.totalCollected.toLocaleString('vi-VN')}</span>
                ),
              },
              {
                header: 'Bot mục tiêu',
                cell: (t: CollectTask) => Array.isArray(t.targets) ? t.targets.length : '—',
              },
              {
                header: 'Tạo lúc',
                cell: (t: CollectTask) => (
                  <Badge variant="teal" className="text-xs">{formatDateTime(t.createdAt)}</Badge>
                ),
              },
              {
                header: 'Hoàn thành',
                cell: (t: CollectTask) => t.completedAt ? (
                  <Badge variant="teal" className="text-xs">{formatDateTime(t.completedAt)}</Badge>
                ) : '—',
              },
            ]}
          />
        </Card>
      </div>

      {/* Dialogs */}
      <CollectBotFormDialog open={openCreate} onClose={() => setOpenCreate(false)} />
      {editTarget && (
        <CollectBotFormDialog
          open
          onClose={() => setEditTarget(null)}
          initialData={editTarget}
        />
      )}

      <ScanConfirmDialog
        open={scanOpen}
        onClose={() => { setScanOpen(false); setScanResult(null) }}
        scanResult={scanResult}
        onConfirm={handleConfirmTask}
        isLoading={createTaskMutation.isPending}
      />
    </div>
  )
}
