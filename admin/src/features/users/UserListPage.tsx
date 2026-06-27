import { useState } from 'react'
import { Plus, Pencil } from 'lucide-react'
import { toast } from 'sonner'
import { useUsers, AdminUser } from './useUsers'
import { UserFormDialog } from './UserFormDialog'
import { getCurrentUser } from '@/lib/auth'
import { PageHeader } from '@/components/layout/PageHeader'
import { Card } from '@/components/ui/card'
import { Button } from '@/components/ui/button'
import { Badge } from '@/components/ui/badge'
import { DataTable } from '@/components/data-table/DataTable'
import { EmptyState } from '@/components/data-table/EmptyState'
import { formatDateTime } from '@/lib/format'

export function UserListPage() {
  const [openCreate, setOpenCreate] = useState(false)
  const [editTarget, setEditTarget] = useState<AdminUser | null>(null)

  const { data: userResult, isLoading } = useUsers()
  const users = userResult?.items ?? []
  const currentUser = getCurrentUser()

  return (
    <div>
      <PageHeader
        title="Người dùng"
        actions={
          <Button onClick={() => setOpenCreate(true)}>
            <Plus className="mr-2 h-4 w-4" />
            Thêm người dùng
          </Button>
        }
      />

      <Card>
        <DataTable
          rowKey={r => r.id}
          data={users}
          isLoading={isLoading}
          emptyState={<EmptyState title="Chưa có người dùng" />}
          columns={[
            { header: 'Tài khoản', cell: u => <span className="font-medium">{u.username}</span> },
            {
              header: 'Vai trò',
              cell: u => (
                <Badge variant={u.role === 'ADMIN' ? 'default' : 'secondary'}>
                  {u.role}
                </Badge>
              ),
            },
            {
              header: 'Trạng thái',
              cell: u => (
                <Badge variant={u.enabled ? 'success' : 'danger'}>
                  {u.enabled ? 'Kích hoạt' : 'Vô hiệu'}
                </Badge>
              ),
            },
            { header: 'Tạo lúc', cell: u => <span className="text-xs text-muted-foreground">{formatDateTime(u.createdAt)}</span> },
            {
              header: '',
              cell: u => {
                const isSelf = currentUser?.sub === String(u.id)
                return (
                  <div className="flex items-center gap-1">
                    <Button variant="ghost" size="icon" onClick={() => setEditTarget(u)} aria-label="Chỉnh sửa">
                      <Pencil className="h-4 w-4" />
                    </Button>
                    {isSelf && (
                      <span className="text-xs text-muted-foreground ml-1">(bạn)</span>
                    )}
                  </div>
                )
              },
              width: '100px',
            },
          ]}
        />
      </Card>

      <UserFormDialog open={openCreate} onClose={() => setOpenCreate(false)} />
      {editTarget && (
        <UserFormDialog open onClose={() => setEditTarget(null)} initialData={editTarget} />
      )}
    </div>
  )
}
