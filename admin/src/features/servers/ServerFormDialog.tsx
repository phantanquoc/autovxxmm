import { useState } from 'react'
import { useForm } from 'react-hook-form'
import { zodResolver } from '@hookform/resolvers/zod'
import { z } from 'zod'
import { toast } from 'sonner'
import { Server, useServerMutation } from './useServers'
import { Button } from '@/components/ui/button'
import { Input } from '@/components/ui/input'
import { Label } from '@/components/ui/label'
import {
  Dialog,
  DialogContent,
  DialogHeader,
  DialogTitle,
  DialogFooter,
} from '@/components/ui/dialog'

const schema = z.object({
  id: z.number().int().optional(),
  name: z.string().min(1, 'Tên không được để trống'),
  ip: z.string().min(1, 'IP không được để trống'),
  port: z.number().int().min(1).max(65535),
  type: z.number().int().default(0),
})

type FormData = z.infer<typeof schema>

interface ServerFormDialogProps {
  open: boolean
  onClose: () => void
  initialData?: Server
}

export function ServerFormDialog({ open, onClose, initialData }: ServerFormDialogProps) {
  const mutation = useServerMutation()

  const { register, handleSubmit, formState: { errors }, reset } = useForm<FormData>({
    resolver: zodResolver(schema),
    defaultValues: initialData ?? { port: 7878, type: 0 },
  })

  async function onSubmit(data: FormData) {
    try {
      await mutation.mutateAsync(initialData ? { ...data, id: initialData.id } : data)
      toast.success(initialData ? 'Đã cập nhật máy chủ' : 'Đã thêm máy chủ')
      reset()
      onClose()
    } catch (err: unknown) {
      toast.error(err instanceof Error ? err.message : 'Có lỗi xảy ra')
    }
  }

  return (
    <Dialog open={open} onOpenChange={v => !v && onClose()}>
      <DialogContent className="max-w-md">
        <DialogHeader>
          <DialogTitle>{initialData ? 'Chỉnh sửa máy chủ' : 'Thêm máy chủ'}</DialogTitle>
        </DialogHeader>
        <form onSubmit={handleSubmit(onSubmit)} className="space-y-4">
          {!initialData && (
            <div className="space-y-1">
              <Label>ID (số)</Label>
              <Input type="number" {...register('id', { valueAsNumber: true })} />
            </div>
          )}
          <div className="space-y-1">
            <Label>Tên</Label>
            <Input {...register('name')} />
            {errors.name && <p className="text-xs text-destructive">{errors.name.message}</p>}
          </div>
          <div className="grid grid-cols-2 gap-3">
            <div className="space-y-1">
              <Label>IP</Label>
              <Input {...register('ip')} />
              {errors.ip && <p className="text-xs text-destructive">{errors.ip.message}</p>}
            </div>
            <div className="space-y-1">
              <Label>Port</Label>
              <Input type="number" {...register('port', { valueAsNumber: true })} />
            </div>
          </div>
          <DialogFooter>
            <Button type="button" variant="outline" onClick={onClose}>Huỷ</Button>
            <Button type="submit" disabled={mutation.isPending}>
              {mutation.isPending ? 'Đang lưu...' : 'Lưu'}
            </Button>
          </DialogFooter>
        </form>
      </DialogContent>
    </Dialog>
  )
}
