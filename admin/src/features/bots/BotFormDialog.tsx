import { useForm, Controller } from 'react-hook-form'
import { zodResolver } from '@hookform/resolvers/zod'
import { z } from 'zod'
import { toast } from 'sonner'
import { Bot, useBotMutation } from './useBots'
import { useServers } from '@/features/servers/useServers'
import { NINJA_MAPS } from './mapData'
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
  playFee: z.number().int().min(0).max(100).default(0),
  typeLuckyDraw: z.number().int().min(0).max(1).default(0),
  client: z.number().int().min(0).default(0),
})

type FormData = z.infer<typeof schema>

interface BotFormDialogProps {
  open: boolean
  onClose: () => void
  initialData?: Bot
}

export function BotFormDialog({ open, onClose, initialData }: BotFormDialogProps) {
  const mutation = useBotMutation()
  const { data: serverResult } = useServers()
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
          playFee: initialData.playFee,
          typeLuckyDraw: initialData.typeLuckyDraw,
          client: initialData.client,
        }
      : { enable: true, playFee: 0, typeLuckyDraw: 0, client: 0 },
  })

  async function onSubmit(data: FormData) {
    try {
      await mutation.mutateAsync(initialData ? { ...data, id: initialData.id } : data)
      toast.success(initialData ? 'Đã cập nhật bot' : 'Đã thêm bot')
      onClose()
    } catch (err: unknown) {
      toast.error(err instanceof Error ? err.message : 'Có lỗi xảy ra')
    }
  }

  return (
    <Dialog open={open} onOpenChange={v => !v && onClose()}>
      <DialogContent className="max-w-2xl max-h-[90vh] overflow-y-auto">
        <DialogHeader>
          <DialogTitle>{initialData ? 'Chỉnh sửa bot' : 'Thêm bot mới'}</DialogTitle>
        </DialogHeader>
        <form onSubmit={handleSubmit(onSubmit)} className="space-y-4">
          <div className="grid grid-cols-2 gap-4">
            {/* Left column */}
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
                <p className="text-[11px] text-muted-foreground">
                  Bot chỉ chấp nhận mời party / giao dịch xu từ nhân vật in-game có tên này.
                </p>
              </div>
            </div>

            {/* Right column */}
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
                          list="map-list"
                          type="number"
                          value={field.value ?? 0}
                          onChange={e => field.onChange(Number(e.target.value))}
                        />
                        <datalist id="map-list">
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
              <div className="space-y-1">
                <Label>Phí (%)</Label>
                <Input
                  type="number"
                  min={0}
                  max={100}
                  {...register('playFee', { valueAsNumber: true })}
                />
                <p className="text-[11px] text-muted-foreground">
                  Phần trăm phí trên số xu thắng (0–100).
                </p>
              </div>
              <div className="space-y-1">
                <Label>Loại vòng xoay</Label>
                <Controller
                  name="typeLuckyDraw"
                  control={control}
                  render={({ field }) => (
                    <Select
                      value={String(field.value ?? 0)}
                      onValueChange={v => field.onChange(Number(v))}
                    >
                      <SelectTrigger>
                        <SelectValue />
                      </SelectTrigger>
                      <SelectContent>
                        <SelectItem value="0">VIP</SelectItem>
                        <SelectItem value="1">Thường</SelectItem>
                      </SelectContent>
                    </Select>
                  )}
                />
              </div>
              <div className="space-y-1">
                <Label>Client</Label>
                <Input type="number" min={0} {...register('client', { valueAsNumber: true })} />
              </div>
            </div>
          </div>

          {/* Full width fields */}
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
