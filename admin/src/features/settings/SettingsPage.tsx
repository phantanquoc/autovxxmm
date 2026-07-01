import { useState } from 'react'
import { useForm } from 'react-hook-form'
import { zodResolver } from '@hookform/resolvers/zod'
import { z } from 'zod'
import { toast } from 'sonner'
import { useQuery } from '@tanstack/react-query'
import { PageHeader } from '@/components/layout/PageHeader'
import { Card, CardContent, CardHeader, CardTitle } from '@/components/ui/card'
import { Button } from '@/components/ui/button'
import { Input } from '@/components/ui/input'
import { Label } from '@/components/ui/label'
import { Tabs, TabsContent, TabsList, TabsTrigger } from '@/components/ui/tabs'
import { Switch } from '@/components/ui/switch'
import { api } from '@/lib/api'
import { getCurrentUser } from '@/lib/auth'
import { useTheme } from '@/hooks/useTheme'

const passwordSchema = z.object({
  currentPassword: z.string().min(1, 'Nhập mật khẩu hiện tại'),
  newPassword: z.string().min(6, 'Ít nhất 6 ký tự'),
  confirmPassword: z.string().min(6),
}).refine(d => d.newPassword === d.confirmPassword, {
  message: 'Mật khẩu xác nhận không khớp',
  path: ['confirmPassword'],
})

type PasswordForm = z.infer<typeof passwordSchema>

// Collect settings schema — threshold must be > keep, both >= 0
const collectSchema = z.object({
  collectThreshold: z.number({ invalid_type_error: 'Nhập số' }).int().nonnegative('Phải >= 0'),
  collectKeep: z.number({ invalid_type_error: 'Nhập số' }).int().nonnegative('Phải >= 0'),
}).refine(d => d.collectThreshold > d.collectKeep, {
  message: 'Ngưỡng phải lớn hơn Để lại',
  path: ['collectThreshold'],
})

type CollectForm = z.infer<typeof collectSchema>

interface ProfileData {
  id: number
  username: string
  role: string
  collectThreshold: number
  collectKeep: number
}

export function SettingsPage() {
  const { theme, toggleTheme } = useTheme()
  const currentUser = getCurrentUser()

  const { data: health } = useQuery({
    queryKey: ['health'],
    queryFn: () => api<{ status: string; time: number }>('/health'),
  })

  const { data: profile } = useQuery({
    queryKey: ['me-profile'],
    queryFn: () => api<ProfileData>('/me/profile'),
  })

  const { register, handleSubmit, reset, formState: { errors } } = useForm<PasswordForm>({
    resolver: zodResolver(passwordSchema),
  })

  const {
    register: registerCollect,
    handleSubmit: handleSubmitCollect,
    formState: { errors: collectErrors },
    reset: resetCollect,
  } = useForm<CollectForm>({
    resolver: zodResolver(collectSchema),
    values: profile
      ? { collectThreshold: profile.collectThreshold, collectKeep: profile.collectKeep }
      : undefined,
  })

  async function onChangePassword(data: PasswordForm) {
    if (!currentUser?.sub) return
    try {
      await api(`/admin/users/${currentUser.sub}`, {
        method: 'PUT',
        body: JSON.stringify({
          currentPassword: data.currentPassword,
          password: data.newPassword,
        }),
      })
      toast.success('Đã đổi mật khẩu')
      reset()
    } catch (err: unknown) {
      toast.error(err instanceof Error ? err.message : 'Sai mật khẩu hiện tại')
    }
  }

  async function onSaveCollect(data: CollectForm) {
    try {
      await api('/me/profile', {
        method: 'PUT',
        body: JSON.stringify(data),
      })
      toast.success('Đã lưu cài đặt gom xu')
    } catch (err: unknown) {
      toast.error(err instanceof Error ? err.message : 'Có lỗi xảy ra')
    }
  }

  return (
    <div>
      <PageHeader title="Cài đặt" />

      <Tabs defaultValue="profile">
        <TabsList className="mb-4">
          <TabsTrigger value="profile">Hồ sơ</TabsTrigger>
          <TabsTrigger value="collect">Gom xu</TabsTrigger>
          <TabsTrigger value="appearance">Giao diện</TabsTrigger>
          <TabsTrigger value="system">Hệ thống</TabsTrigger>
        </TabsList>

        <TabsContent value="profile">
          <Card>
            <CardHeader>
              <CardTitle>Đổi mật khẩu</CardTitle>
            </CardHeader>
            <CardContent>
              <form onSubmit={handleSubmit(onChangePassword)} className="space-y-4 max-w-sm">
                <div className="space-y-1">
                  <Label>Tài khoản</Label>
                  <Input value={currentUser?.username ?? ''} disabled />
                </div>
                <div className="space-y-1">
                  <Label>Mật khẩu hiện tại</Label>
                  <Input type="password" {...register('currentPassword')} />
                  {errors.currentPassword && <p className="text-xs text-destructive">{errors.currentPassword.message}</p>}
                </div>
                <div className="space-y-1">
                  <Label>Mật khẩu mới</Label>
                  <Input type="password" {...register('newPassword')} />
                  {errors.newPassword && <p className="text-xs text-destructive">{errors.newPassword.message}</p>}
                </div>
                <div className="space-y-1">
                  <Label>Xác nhận mật khẩu</Label>
                  <Input type="password" {...register('confirmPassword')} />
                  {errors.confirmPassword && <p className="text-xs text-destructive">{errors.confirmPassword.message}</p>}
                </div>
                <Button type="submit">Cập nhật mật khẩu</Button>
              </form>
            </CardContent>
          </Card>
        </TabsContent>

        <TabsContent value="collect">
          <Card>
            <CardHeader>
              <CardTitle>Gom xu</CardTitle>
            </CardHeader>
            <CardContent>
              <form onSubmit={handleSubmitCollect(onSaveCollect)} className="space-y-4 max-w-sm">
                <div className="space-y-1">
                  <Label>Ngưỡng X (xu)</Label>
                  <Input
                    type="number"
                    min={0}
                    {...registerCollect('collectThreshold', { valueAsNumber: true })}
                  />
                  <p className="text-[11px] text-muted-foreground">
                    Bot có xu vượt ngưỡng này sẽ được đưa vào danh sách gom.
                  </p>
                  {collectErrors.collectThreshold && (
                    <p className="text-xs text-destructive">{collectErrors.collectThreshold.message}</p>
                  )}
                </div>
                <div className="space-y-1">
                  <Label>Để lại Y (xu)</Label>
                  <Input
                    type="number"
                    min={0}
                    {...registerCollect('collectKeep', { valueAsNumber: true })}
                  />
                  <p className="text-[11px] text-muted-foreground">
                    Số xu giữ lại sau khi gom (phải nhỏ hơn ngưỡng X).
                  </p>
                  {collectErrors.collectKeep && (
                    <p className="text-xs text-destructive">{collectErrors.collectKeep.message}</p>
                  )}
                </div>
                <Button type="submit">Lưu</Button>
              </form>
            </CardContent>
          </Card>
        </TabsContent>

        <TabsContent value="appearance">
          <Card>
            <CardHeader><CardTitle>Giao diện</CardTitle></CardHeader>
            <CardContent className="space-y-4">
              <div className="flex items-center justify-between py-2">
                <div>
                  <p className="font-medium">Chế độ tối</p>
                  <p className="text-sm text-muted-foreground">Bật giao diện tối</p>
                </div>
                <Switch
                  checked={theme === 'dark'}
                  onCheckedChange={toggleTheme}
                  aria-label="Chế độ tối"
                />
              </div>
            </CardContent>
          </Card>
        </TabsContent>

        <TabsContent value="system">
          <Card>
            <CardHeader><CardTitle>Thông tin hệ thống</CardTitle></CardHeader>
            <CardContent className="space-y-3">
              <div className="grid grid-cols-2 gap-4 text-sm">
                <div>
                  <p className="text-muted-foreground">Backend URL</p>
                  <p className="font-mono">http://localhost:8009</p>
                </div>
                <div>
                  <p className="text-muted-foreground">Trạng thái</p>
                  <p className={health?.status === 'ok' ? 'text-[#50CD89] font-medium' : 'text-muted-foreground'}>
                    {health?.status ?? 'Đang kiểm tra...'}
                  </p>
                </div>
                <div>
                  <p className="text-muted-foreground">Server time</p>
                  <p className="font-mono text-xs">{health?.time ? new Date(health.time).toLocaleString('vi-VN') : '—'}</p>
                </div>
                <div>
                  <p className="text-muted-foreground">Admin version</p>
                  <p className="font-mono">1.0.0</p>
                </div>
              </div>
              <div className="pt-2">
                <a
                  href="/api/health"
                  target="_blank"
                  rel="noopener noreferrer"
                  className="text-primary text-sm hover:underline"
                >
                  Kiểm tra /api/health
                </a>
              </div>
            </CardContent>
          </Card>
        </TabsContent>
      </Tabs>
    </div>
  )
}
