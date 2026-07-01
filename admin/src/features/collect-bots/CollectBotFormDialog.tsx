import { useForm, Controller } from 'react-hook-form'
import { zodResolver } from '@hookform/resolvers/zod'
import { z } from 'zod'
import { toast } from 'sonner'
import { CollectBot, useCollectBotMutation } from './useCollectBots'
import { useResourceServers } from '@/features/servers/useServers'
import { NINJA_MAPS } from '@/features/bots/mapData'
import { Button } from '@/components/ui/button'
import { Input } from '@/components/ui/input'
import { Label } from '@/components/ui/label'
import { Switch } from '@/components/ui/switch'
import { Select, SelectContent, SelectItem, SelectTrigger, SelectValue } from '@/components/ui/select'
import {
  Dialog,
  DialogContent,
  DialogHeader,
  DialogTitle,
  DialogFooter,
} from '@/components/ui/dialog'

// Collect bots do NOT have playFee or typeLuckyDraw
const schema = z.object({
  serverId: z.number().int().min(1),
  account: z.string().min(1),
  password: z.string().min(1),
  charName: z.string().default(''),
  mapId: z.number().int().min(0).default(0),
  zoneId: z.number().int().min(0).default(0),
  posX: z.number().int().default(0),
  posY: z.number().int().default(0),
  manager: z.string().default(''),
  chat: z.string().default(''),
  sms: z.string().default(''),
  enable: z.boolean().default(true),
})

type FormData = z.infer<typeof schema>

interface CollectBotFormDialogProps {
  open: boolean
  onClose: () => void
  initialData?: CollectBot
}

export function CollectBotFormDialog({ open, onClose, initialData }: CollectBotFormDialogProps) {
  const mutation = useCollectBotMutation()
  const { data: serverResult } = useResourceServers()
  const servers = serverResult?.items ?? []

  const { register, handleSubmit, control, formState: { errors } } = useForm<FormData>({
    resolver: zodResolver(schema),
    defaultValues: initialData
      ? {
          serverId: initialData.serverId,
          account: initialData.account,
          password: initialData.password,
          charName: initialData.charName,
          mapId: initialData.mapId,
          zoneId: initialData.zoneId,
          posX: initialData.posX,
          posY: initialData.posY,
          manager: initialData.manager,
          chat: initialData.chat,
          sms: initialData.sms,
          enable: initialData.enable,
        }
      : { enable: true },
  })

  async function onSubmit(data: FormData) {
    try {
      await mutation.mutateAsync(initialData ? { ...data, id: initialData.id } : data)
      toast.success(initialData ? 'Đã cập nhật bot gom xu' : 'Đã thêm bot gom xu')
      onClose()
    } catch (err: unknown) {
      toast.error(err instanceof Error ? err.message : 'Có lỗi xảy ra')
    }
  }

  return (
    <Dialog open={open} onOpenChange={v => !v && onClose()}>
      <DialogContent className="max-w-2xl max-h-[90vh] overflow-y-auto">
        <DialogHeader>
          <DialogTitle>{initialData ? 'Chỉnh sửa bot gom xu' : 'Thêm bot gom xu mới'}</DialogTitle>
        </DialogHeader>
        <form onSubmit={handleSubmit(onSubmit)} className="space-y-4">
          <div className="grid grid-cols-2 gap-4">
            <div className="space-y-3">
              <div className="space-y-1">
                <Label>Server</Label>
                <Controller
                  name="serverId"
                  control={control}
                  render={({ field }) => (
                    <Select value={String(field.value || '')} onValueChange={v => field.onChange(Number(v))}>
                      <SelectTrigger>
                        <SelectValue placeholder="Chọn server" />
                      </SelectTrigger>
                      <SelectContent>
                        {servers.map(s => (
                          <SelectItem key={s.id} value={String(s.id)}>{s.name}</SelectItem>
                        ))}
                      </SelectContent>
                    </Select>
                  )}
                />
                {errors.serverId && <p className="text-xs text-destructive">Chọn server</p>}
              </div>
              <div className="space-y-1">
                <Label>Tài khoản</Label>
                <Input {...register('account')} />
                {errors.account && <p className="text-xs text-destructive">{errors.account.message}</p>}
              </div>
              <div className="space-y-1">
                <Label>Mật khẩu</Label>
                <Input type="password" {...register('password')} />
                {errors.password && <p className="text-xs text-destructive">{errors.password.message}</p>}
              </div>
              <div className="space-y-1">
                <Label>Tên nhân vật</Label>
                <Input {...register('charName')} />
              </div>
              <div className="space-y-1">
                <Label>Tên nhân vật quản lý</Label>
                <Input {...register('manager')} placeholder="Tên char trong game" />
              </div>
            </div>

            <div className="space-y-3">
              <div className="grid grid-cols-2 gap-2">
                <div className="space-y-1">
                  <Label>Map</Label>
                  <Controller
                    name="mapId"
                    control={control}
                    render={({ field }) => (
                      <>
                        <Input
                          list="collect-map-list"
                          type="number"
                          value={field.value ?? 0}
                          onChange={e => field.onChange(Number(e.target.value))}
                        />
                        <datalist id="collect-map-list">
                          {NINJA_MAPS.map(m => (
                            <option key={m.id} value={m.id}>{m.name}</option>
                          ))}
                        </datalist>
                      </>
                    )}
                  />
                </div>
                <div className="space-y-1">
                  <Label>Khu vực</Label>
                  <Input type="number" min={0} {...register('zoneId', { valueAsNumber: true })} />
                </div>
                <div className="space-y-1">
                  <Label>Pos X</Label>
                  <Input type="number" {...register('posX', { valueAsNumber: true })} />
                </div>
                <div className="space-y-1">
                  <Label>Pos Y</Label>
                  <Input type="number" {...register('posY', { valueAsNumber: true })} />
                </div>
              </div>
            </div>
          </div>

          <div className="space-y-1">
            <Label>Chat</Label>
            <textarea
              {...register('chat')}
              className="w-full rounded-md border border-input bg-background px-3 py-2 text-sm min-h-[60px]"
            />
          </div>
          <div className="space-y-1">
            <Label>SMS</Label>
            <textarea
              {...register('sms')}
              className="w-full rounded-md border border-input bg-background px-3 py-2 text-sm min-h-[60px]"
            />
          </div>
          <div className="flex items-center gap-2">
            <Controller
              name="enable"
              control={control}
              render={({ field }) => (
                <Switch checked={field.value} onCheckedChange={field.onChange} />
              )}
            />
            <Label>Bật bot</Label>
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
