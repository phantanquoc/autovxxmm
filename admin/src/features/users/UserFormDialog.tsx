import { useForm, Controller } from 'react-hook-form'
import { zodResolver } from '@hookform/resolvers/zod'
import { z } from 'zod'
import { toast } from 'sonner'
import { AdminUser, useUserMutation } from './useUsers'
import { Button } from '@/components/ui/button'
import { Input } from '@/components/ui/input'
import { Label } from '@/components/ui/label'
import { Switch } from '@/components/ui/switch'
import { Select, SelectContent, SelectItem, SelectTrigger, SelectValue } from '@/components/ui/select'
import { Dialog, DialogContent, DialogHeader, DialogTitle, DialogFooter } from '@/components/ui/dialog'

const createSchema = z.object({
  username: z.string().min(1),
  password: z.string().min(6, 'Ít nhất 6 ký tự'),
  role: z.enum(['ADMIN', 'USER']),
  enabled: z.boolean().default(true),
})

const editSchema = z.object({
  password: z.string().min(6, 'Ít nhất 6 ký tự').optional().or(z.literal('')),
  role: z.enum(['ADMIN', 'USER']),
  enabled: z.boolean(),
})

type CreateForm = z.infer<typeof createSchema>
type EditForm = z.infer<typeof editSchema>

interface UserFormDialogProps {
  open: boolean
  onClose: () => void
  initialData?: AdminUser
}

export function UserFormDialog({ open, onClose, initialData }: UserFormDialogProps) {
  const mutation = useUserMutation()

  const { register: registerCreate, handleSubmit: handleCreate, control: ctrlCreate, formState: { errors: errCreate } } = useForm<CreateForm>({
    resolver: zodResolver(createSchema),
    defaultValues: { role: 'USER', enabled: true },
  })

  const { register: registerEdit, handleSubmit: handleEdit, control: ctrlEdit, formState: { errors: errEdit } } = useForm<EditForm>({
    resolver: zodResolver(editSchema),
    defaultValues: initialData ? { role: initialData.role, enabled: initialData.enabled, password: '' } : undefined,
  })

  async function onCreateSubmit(data: CreateForm) {
    try {
      await mutation.mutateAsync(data)
      toast.success('Đã tạo người dùng')
      onClose()
    } catch (err: unknown) {
      toast.error(err instanceof Error ? err.message : 'Có lỗi xảy ra')
    }
  }

  async function onEditSubmit(data: EditForm) {
    try {
      const payload: Record<string, unknown> = { id: initialData!.id, role: data.role, enabled: data.enabled }
      if (data.password) payload.password = data.password
      await mutation.mutateAsync(payload)
      toast.success('Đã cập nhật người dùng')
      onClose()
    } catch (err: unknown) {
      toast.error(err instanceof Error ? err.message : 'Có lỗi xảy ra')
    }
  }

  if (initialData) {
    return (
      <Dialog open={open} onOpenChange={v => !v && onClose()}>
        <DialogContent className="max-w-md">
          <DialogHeader><DialogTitle>Chỉnh sửa người dùng: {initialData.username}</DialogTitle></DialogHeader>
          <form onSubmit={handleEdit(onEditSubmit)} className="space-y-4">
            <div className="space-y-1">
              <Label>Mật khẩu mới (để trống = không đổi)</Label>
              <Input type="password" {...registerEdit('password')} placeholder="••••••" />
              {errEdit.password && <p className="text-xs text-destructive">{errEdit.password.message}</p>}
            </div>
            <div className="space-y-1">
              <Label>Vai trò</Label>
              <Controller name="role" control={ctrlEdit} render={({ field }) => (
                <Select value={field.value} onValueChange={field.onChange}>
                  <SelectTrigger><SelectValue /></SelectTrigger>
                  <SelectContent>
                    <SelectItem value="ADMIN">Admin</SelectItem>
                    <SelectItem value="USER">User</SelectItem>
                  </SelectContent>
                </Select>
              )} />
            </div>
            <div className="flex items-center gap-2">
              <Controller name="enabled" control={ctrlEdit} render={({ field }) => (
                <Switch checked={field.value} onCheckedChange={field.onChange} />
              )} />
              <Label>Kích hoạt</Label>
            </div>
            <DialogFooter>
              <Button type="button" variant="outline" onClick={onClose}>Huỷ</Button>
              <Button type="submit" disabled={mutation.isPending}>{mutation.isPending ? 'Đang lưu...' : 'Lưu'}</Button>
            </DialogFooter>
          </form>
        </DialogContent>
      </Dialog>
    )
  }

  return (
    <Dialog open={open} onOpenChange={v => !v && onClose()}>
      <DialogContent className="max-w-md">
        <DialogHeader><DialogTitle>Tạo người dùng mới</DialogTitle></DialogHeader>
        <form onSubmit={handleCreate(onCreateSubmit)} className="space-y-4">
          <div className="space-y-1">
            <Label>Tài khoản</Label>
            <Input {...registerCreate('username')} />
            {errCreate.username && <p className="text-xs text-destructive">{errCreate.username.message}</p>}
          </div>
          <div className="space-y-1">
            <Label>Mật khẩu</Label>
            <Input type="password" {...registerCreate('password')} />
            {errCreate.password && <p className="text-xs text-destructive">{errCreate.password.message}</p>}
          </div>
          <div className="space-y-1">
            <Label>Vai trò</Label>
            <Controller name="role" control={ctrlCreate} render={({ field }) => (
              <Select value={field.value} onValueChange={field.onChange}>
                <SelectTrigger><SelectValue /></SelectTrigger>
                <SelectContent>
                  <SelectItem value="ADMIN">Admin</SelectItem>
                  <SelectItem value="USER">User</SelectItem>
                </SelectContent>
              </Select>
            )} />
          </div>
          <div className="flex items-center gap-2">
            <Controller name="enabled" control={ctrlCreate} render={({ field }) => (
              <Switch checked={field.value} onCheckedChange={field.onChange} />
            )} />
            <Label>Kích hoạt</Label>
          </div>
          <DialogFooter>
            <Button type="button" variant="outline" onClick={onClose}>Huỷ</Button>
            <Button type="submit" disabled={mutation.isPending}>{mutation.isPending ? 'Đang lưu...' : 'Tạo'}</Button>
          </DialogFooter>
        </form>
      </DialogContent>
    </Dialog>
  )
}
