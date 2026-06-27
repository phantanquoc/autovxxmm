import { useForm, Controller } from 'react-hook-form'
import { zodResolver } from '@hookform/resolvers/zod'
import { z } from 'zod'
import { toast } from 'sonner'
import { useBlockCreate } from './useBlocks'
import { useServers } from '@/features/servers/useServers'
import { Button } from '@/components/ui/button'
import { Input } from '@/components/ui/input'
import { Label } from '@/components/ui/label'
import { Select, SelectContent, SelectItem, SelectTrigger, SelectValue } from '@/components/ui/select'
import { Dialog, DialogContent, DialogHeader, DialogTitle, DialogFooter } from '@/components/ui/dialog'

const schema = z.object({
  serverId: z.number().int().min(1, 'Chọn server'),
  name: z.string().min(1, 'Nhập tên người chơi'),
  reason: z.string().optional(),
})

type FormData = z.infer<typeof schema>

interface BlockFormDialogProps {
  open: boolean
  onClose: () => void
}

export function BlockFormDialog({ open, onClose }: BlockFormDialogProps) {
  const { data: serverResult } = useServers()
  const servers = serverResult?.items ?? []
  const createMutation = useBlockCreate()

  const { register, handleSubmit, control, reset, formState: { errors } } = useForm<FormData>({
    resolver: zodResolver(schema),
  })

  async function onSubmit(data: FormData) {
    try {
      await createMutation.mutateAsync(data)
      toast.success('Đã thêm block')
      reset()
      onClose()
    } catch (err: unknown) {
      toast.error(err instanceof Error ? err.message : 'Có lỗi xảy ra')
    }
  }

  return (
    <Dialog open={open} onOpenChange={v => !v && onClose()}>
      <DialogContent className="max-w-md">
        <DialogHeader><DialogTitle>Thêm block</DialogTitle></DialogHeader>
        <form onSubmit={handleSubmit(onSubmit)} className="space-y-4">
          <div className="space-y-1">
            <Label>Server</Label>
            <Controller name="serverId" control={control} render={({ field }) => (
              <Select value={String(field.value || '')} onValueChange={v => field.onChange(Number(v))}>
                <SelectTrigger><SelectValue placeholder="Chọn server" /></SelectTrigger>
                <SelectContent>
                  {servers.map(s => <SelectItem key={s.id} value={String(s.id)}>{s.name}</SelectItem>)}
                </SelectContent>
              </Select>
            )} />
            {errors.serverId && <p className="text-xs text-destructive">{errors.serverId.message}</p>}
          </div>
          <div className="space-y-1">
            <Label>Tên người chơi</Label>
            <Input {...register('name')} />
            {errors.name && <p className="text-xs text-destructive">{errors.name.message}</p>}
          </div>
          <div className="space-y-1">
            <Label>Lý do (tuỳ chọn)</Label>
            <Input {...register('reason')} />
          </div>
          <DialogFooter>
            <Button type="button" variant="outline" onClick={onClose}>Huỷ</Button>
            <Button type="submit" disabled={createMutation.isPending}>
              {createMutation.isPending ? 'Đang lưu...' : 'Thêm'}
            </Button>
          </DialogFooter>
        </form>
      </DialogContent>
    </Dialog>
  )
}
