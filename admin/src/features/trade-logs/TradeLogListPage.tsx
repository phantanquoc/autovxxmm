import { useState } from 'react'
import { useTradeLogs, TRADE_TYPE_MAP } from './useTradeLogs'
import { useResourceServers } from '@/features/servers/useServers'
import { PageHeader } from '@/components/layout/PageHeader'
import { Card } from '@/components/ui/card'
import { Badge } from '@/components/ui/badge'
import { DataTable } from '@/components/data-table/DataTable'
import { DataTablePagination } from '@/components/data-table/DataTablePagination'
import { DataTableToolbar } from '@/components/data-table/DataTableToolbar'
import { EmptyState } from '@/components/data-table/EmptyState'
import { XuAmount } from '@/components/domain/XuAmount'
import { OwnerFilter } from '@/components/OwnerFilter'
import { getCurrentUser } from '@/lib/auth'
import { formatDateTime } from '@/lib/format'

const TYPE_OPTIONS = Object.entries(TRADE_TYPE_MAP).map(([v, l]) => ({ value: v, label: l }))

export function TradeLogListPage() {
  const [page, setPage] = useState(1)
  const pageSize = 25
  const [filterValues, setFilterValues] = useState<Record<string, string>>({})
  const [searchQ, setSearchQ] = useState('')
  const [ownerIdFilter, setOwnerIdFilter] = useState<number | undefined>(undefined)

  const isAdmin = getCurrentUser()?.role === 'ADMIN'

  const { data: serverResult } = useResourceServers()
  const servers = serverResult?.items ?? []
  const serverOptions = servers.map(s => ({ value: String(s.id), label: s.name }))

  const filter: Record<string, unknown> = { ...filterValues }
  if (searchQ) filter.q = searchQ

  const { data: logResult, isLoading } = useTradeLogs({
    page,
    pageSize,
    sort: 'id',
    order: 'desc',
    filter,
    ownerId: ownerIdFilter,
  })
  const logs = logResult?.items ?? []
  const total = logResult?.total ?? 0

  function handleFilter(key: string, value: string) {
    setFilterValues(prev => {
      const next = { ...prev }
      if (value) next[key] = value
      else delete next[key]
      return next
    })
    setPage(1)
  }

  return (
    <div>
      <PageHeader title="Giao dịch xu" />
      <Card>
        {isAdmin && (
          <div className="px-4 pt-4 flex items-center gap-2">
            <span className="text-sm text-muted-foreground">Người dùng:</span>
            <OwnerFilter value={ownerIdFilter} onChange={(v) => { setOwnerIdFilter(v); setPage(1) }} />
          </div>
        )}
        <DataTableToolbar
          searchPlaceholder="Tìm tên bot/khách..."
          onSearch={q => { setSearchQ(q); setPage(1) }}
          filters={[
            { type: 'select', key: 'serverId', label: 'Server', options: serverOptions },
            { type: 'select', key: 'type', label: 'Loại', options: TYPE_OPTIONS },
          ]}
          onFilter={handleFilter}
          filterValues={filterValues}
        />

        <DataTable
          rowKey={r => r.id}
          data={logs}
          isLoading={isLoading}
          emptyState={<EmptyState title="Chưa có giao dịch" />}
          columns={[
            { header: 'ID', cell: l => <span className="font-mono text-xs">{String(l.id)}</span>, width: '80px' },
            {
              header: 'Server',
              cell: l => {
                const server = servers.find(s => s.id === l.serverId)
                return <span className="font-medium">{server?.name ?? 'SV' + l.serverId}</span>
              },
            },
            { header: 'Bot', cell: l => <span className="text-sm">{l.name}</span> },
            { header: 'Khách', cell: l => <span className="text-sm">{l.customer}</span> },
            { header: 'Trước', cell: l => <XuAmount value={l.before} muted /> },
            { header: 'Sau', cell: l => <XuAmount value={l.after} /> },
            {
              header: 'Thay đổi',
              cell: l => (
                <span className={l.change >= 0 ? 'text-[#50CD89] font-mono' : 'text-[#F1416C] font-mono'}>
                  {l.change >= 0 ? '+' : ''}{l.change.toLocaleString()}
                </span>
              ),
            },
            {
              header: 'Loại',
              cell: l => (
                <Badge variant="secondary" className="text-xs">
                  {TRADE_TYPE_MAP[l.type] ?? String(l.type)}
                </Badge>
              ),
            },
            {
              header: 'Mô tả',
              cell: l => (
                <span className="text-xs text-muted-foreground truncate max-w-[200px] block">
                  {l.description}
                </span>
              ),
            },
            {
              header: 'Thời gian',
              cell: l => (
                <div className="flex flex-col text-xs whitespace-nowrap">
                  <span>{formatDateTime(l.timeStart)}</span>
                </div>
              ),
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
    </div>
  )
}
