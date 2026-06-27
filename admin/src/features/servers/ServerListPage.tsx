import { useState } from 'react'
import { Plus, Pencil, Trash2 } from 'lucide-react'
import { toast } from 'sonner'
import { useServers, useServerDelete, Server } from './useServers'
import { ServerFormDialog } from './ServerFormDialog'
import { PageHeader } from '@/components/layout/PageHeader'
import { Card } from '@/components/ui/card'
import { Button } from '@/components/ui/button'
import { DataTable } from '@/components/data-table/DataTable'
import { EmptyState } from '@/components/data-table/EmptyState'

export function ServerListPage() {
  const [openCreate, setOpenCreate] = useState(false)
  const [editTarget, setEditTarget] = useState<Server | null>(null)

  const { data: serverResult, isLoading } = useServers()
  const servers = serverResult?.items ?? []
  const deleteMutation = useServerDelete()

  async function handleDelete(s: Server) {
    if (!confirm(`Xoá máy chủ "${s.name}"?`)) return
    try {
      await deleteMutation.mutateAsync(s.id)
      toast.success('Đã xoá máy chủ')
    } catch (err: unknown) {
      toast.error(err instanceof Error ? err.message : 'Có lỗi xảy ra')
    }
  }

  return (
    <div>
      <PageHeader
        title="Máy chủ"
        actions={
          <Button onClick={() => setOpenCreate(true)}>
            <Plus className="mr-2 h-4 w-4" />
            Thêm máy chủ
          </Button>
        }
      />

      <Card>
        <DataTable
          rowKey={r => r.id}
          data={servers}
          isLoading={isLoading}
          emptyState={
            <EmptyState
              title="Chưa có máy chủ"
              action={<Button onClick={() => setOpenCreate(true)}>Thêm máy chủ</Button>}
            />
          }
          columns={[
            { header: 'ID', cell: r => <span className="font-mono">{r.id}</span>, width: '60px' },
            { header: 'Tên', cell: r => <span className="font-medium">{r.name}</span> },
            { header: 'IP', cell: r => <span className="font-mono text-sm">{r.ip}</span> },
            { header: 'Port', cell: r => <span className="font-mono">{r.port}</span> },
            { header: 'Type', cell: r => <span>{r.type}</span> },
            {
              header: '',
              cell: r => (
                <div className="flex items-center gap-1">
                  <Button variant="ghost" size="icon" onClick={() => setEditTarget(r)} aria-label="Chỉnh sửa">
                    <Pencil className="h-4 w-4" />
                  </Button>
                  <Button
                    variant="ghost"
                    size="icon"
                    className="text-destructive hover:text-destructive"
                    onClick={() => handleDelete(r)}
                    aria-label="Xoá"
                  >
                    <Trash2 className="h-4 w-4" />
                  </Button>
                </div>
              ),
              width: '100px',
            },
          ]}
        />
      </Card>

      <ServerFormDialog open={openCreate} onClose={() => setOpenCreate(false)} />
      {editTarget && (
        <ServerFormDialog
          open
          onClose={() => setEditTarget(null)}
          initialData={editTarget}
        />
      )}
    </div>
  )
}
