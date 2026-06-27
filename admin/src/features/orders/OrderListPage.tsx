import { useState } from 'react'
import { Trash2 } from 'lucide-react'
import { toast } from 'sonner'
import { useOrders, useOrderDelete, Order } from './useOrders'
import { useServers } from '@/features/servers/useServers'
import { PageHeader } from '@/components/layout/PageHeader'
import { Card } from '@/components/ui/card'
import { Button } from '@/components/ui/button'
import { Switch } from '@/components/ui/switch'
import { DataTable } from '@/components/data-table/DataTable'
import { DataTablePagination } from '@/components/data-table/DataTablePagination'
import { DataTableToolbar } from '@/components/data-table/DataTableToolbar'
import { EmptyState } from '@/components/data-table/EmptyState'
import { StatusBadge } from '@/components/domain/StatusBadge'
import { XuAmount } from '@/components/domain/XuAmount'
import { formatDateTime } from '@/lib/format'

const STATUS_OPTIONS = [
  { value: '0', label: 'Đang chờ' },
  { value: '1', label: 'Đặt cược' },
  { value: '2', label: 'Thua cuộc' },
  { value: '3', label: 'Thắng cuộc' },
  { value: '4', label: 'Đã thưởng' },
  { value: '5', label: 'Lỗi' },
]

interface OrderListPageProps {
  limit?: number
  hideHeader?: boolean
}

export function OrderListPage({ limit, hideHeader }: OrderListPageProps = {}) {
  const [page, setPage] = useState(1)
  const pageSize = limit ?? 25
  const [filterValues, setFilterValues] = useState<Record<string, string>>({})
  const [searchQ, setSearchQ] = useState('')

  const { data: serverResult } = useServers()
  const servers = serverResult?.items ?? []

  const filter: Record<string, unknown> = { ...filterValues }
  if (searchQ) filter.q = searchQ

  const { data: orderResult, isLoading } = useOrders({
    page,
    pageSize,
    sort: 'id',
    order: 'desc',
    filter,
  })
  const orders = orderResult?.items ?? []
  const total = orderResult?.total ?? 0

  const deleteMutation = useOrderDelete()

  function handleFilter(key: string, value: string) {
    setFilterValues(prev => {
      const next = { ...prev }
      if (value) next[key] = value
      else delete next[key]
      return next
    })
    setPage(1)
  }

  async function handleDelete(order: Order) {
    if (!confirm(`Xoá đơn hàng #${order.id}?`)) return
    try {
      await deleteMutation.mutateAsync(order.id)
      toast.success('Đã xoá đơn hàng')
    } catch (err: unknown) {
      toast.error(err instanceof Error ? err.message : 'Có lỗi xảy ra')
    }
  }

  const serverOptions = servers.map(s => ({ value: String(s.id), label: s.name }))

  const tableContent = (
    <>
      {!limit && (
        <DataTableToolbar
          searchPlaceholder="Tìm tên bot/người chơi..."
          onSearch={q => { setSearchQ(q); setPage(1) }}
          filters={[
            { type: 'select', key: 'serverId', label: 'Server', options: serverOptions },
            { type: 'select', key: 'status', label: 'Trạng thái', options: STATUS_OPTIONS },
          ]}
          onFilter={handleFilter}
          filterValues={filterValues}
        />
      )}
      <DataTable
        rowKey={r => r.id}
        data={orders}
        isLoading={isLoading}
        emptyState={<EmptyState title="Chưa có đơn hàng" />}
        columns={[
          {
            header: 'Máy chủ',
            cell: (o) => {
              const server = servers.find(s => s.id === o.serverId)
              return <span className="font-medium">{server?.name ?? 'SV' + o.serverId}</span>
            },
          },
          { header: 'Tên bot', cell: (o) => <span className="text-sm">{o.bot}</span> },
          { header: 'Người chơi', cell: (o) => <span className="text-sm">{o.name}</span> },
          { header: 'Giây cược', cell: (o) => <span className="text-sm">{o.second}s</span> },
          { header: 'Xu đặt', cell: (o) => <XuAmount value={o.coinOrder} /> },
          { header: 'Xu thắng', cell: (o) => <XuAmount value={o.coinWin} muted /> },
          { header: 'Xu nhận', cell: (o) => <XuAmount value={o.coinReward} muted /> },
          { header: 'Trạng thái', cell: (o) => <StatusBadge status={o.status} /> },
          {
            header: 'Thời gian',
            cell: (o) => (
              <div className="flex flex-col text-xs whitespace-nowrap">
                <span>{formatDateTime(o.timeStart)}</span>
                {o.timeStop > 0 && (
                  <span className="text-muted-foreground">{formatDateTime(o.timeStop)}</span>
                )}
              </div>
            ),
          },
          {
            header: 'Tuỳ chọn',
            cell: (o) => (
              <div className="flex items-center gap-1">
                <Switch checked={o.status !== 5} disabled aria-label="Trạng thái" />
                <Button
                  variant="ghost"
                  size="icon"
                  className="text-destructive hover:text-destructive"
                  onClick={() => handleDelete(o)}
                  aria-label="Xoá đơn"
                >
                  <Trash2 className="h-4 w-4" />
                </Button>
              </div>
            ),
            width: '100px',
          },
        ]}
      />
      {!limit && (
        <DataTablePagination
          page={page}
          pageSize={pageSize}
          total={total}
          onPageChange={setPage}
        />
      )}
    </>
  )

  if (hideHeader) return tableContent

  return (
    <div>
      <PageHeader title="Đơn hàng" />
      <Card>{tableContent}</Card>
    </div>
  )
}
