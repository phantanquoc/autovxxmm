import { useState } from 'react'
import { Plus, Trash2 } from 'lucide-react'
import { toast } from 'sonner'
import { useBlocks, useBlockDelete, Block } from './useBlocks'
import { BlockFormDialog } from './BlockFormDialog'
import { useResourceServers } from '@/features/servers/useServers'
import { PageHeader } from '@/components/layout/PageHeader'
import { Card } from '@/components/ui/card'
import { Button } from '@/components/ui/button'
import { DataTable } from '@/components/data-table/DataTable'
import { EmptyState } from '@/components/data-table/EmptyState'
import { OwnerFilter } from '@/components/OwnerFilter'
import { getCurrentUser } from '@/lib/auth'
import { formatDateTime } from '@/lib/format'

export function BlockListPage() {
  const [openCreate, setOpenCreate] = useState(false)
  const [ownerIdFilter, setOwnerIdFilter] = useState<number | undefined>(undefined)

  const isAdmin = getCurrentUser()?.role === 'ADMIN'

  const { data: blockResult, isLoading } = useBlocks({ ownerId: ownerIdFilter })
  const blocks = blockResult?.items ?? []
  const { data: serverResult } = useResourceServers()
  const servers = serverResult?.items ?? []
  const deleteMutation = useBlockDelete()

  async function handleDelete(block: Block) {
    if (!confirm(`Xoá block "${block.name}"?`)) return
    try {
      await deleteMutation.mutateAsync(block.id)
      toast.success('Đã xoá block')
    } catch (err: unknown) {
      toast.error(err instanceof Error ? err.message : 'Có lỗi xảy ra')
    }
  }

  return (
    <div>
      <PageHeader
        title="Block list"
        actions={
          !isAdmin ? (
            <Button onClick={() => setOpenCreate(true)}>
              <Plus className="mr-2 h-4 w-4" />
              Thêm block
            </Button>
          ) : undefined
        }
      />

      {isAdmin && (
        <div className="mb-4 flex items-center gap-2">
          <span className="text-sm text-muted-foreground">Người dùng:</span>
          <OwnerFilter value={ownerIdFilter} onChange={setOwnerIdFilter} />
        </div>
      )}

      <Card>
        <DataTable
          rowKey={r => r.id}
          data={blocks}
          isLoading={isLoading}
          emptyState={
            <EmptyState
              title="Chưa có block nào"
              action={!isAdmin ? <Button onClick={() => setOpenCreate(true)}>Thêm block</Button> : undefined}
            />
          }
          columns={[
            { header: 'ID', cell: r => <span className="font-mono text-xs">{r.id}</span>, width: '60px' },
            {
              header: 'Server',
              cell: r => {
                const server = servers.find(s => s.id === r.serverId)
                return <span className="font-medium">{server?.name ?? 'SV' + r.serverId}</span>
              },
            },
            { header: 'Người chơi', cell: r => <span className="font-medium">{r.name}</span> },
            { header: 'Lý do', cell: r => <span className="text-sm text-muted-foreground">{r.reason ?? '—'}</span> },
            { header: 'Tạo lúc', cell: r => <span className="text-xs text-muted-foreground">{formatDateTime(r.createdAt)}</span> },
            ...(!isAdmin ? [{
              header: '',
              cell: (r: Block) => (
                <Button
                  variant="ghost"
                  size="icon"
                  className="text-destructive hover:text-destructive"
                  onClick={() => handleDelete(r)}
                  aria-label="Xoá block"
                >
                  <Trash2 className="h-4 w-4" />
                </Button>
              ),
              width: '60px',
            }] : []),
          ]}
        />
      </Card>

      {!isAdmin && (
        <BlockFormDialog open={openCreate} onClose={() => setOpenCreate(false)} />
      )}
    </div>
  )
}
