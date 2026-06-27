import { useForm } from 'react-hook-form'
import { zodResolver } from '@hookform/resolvers/zod'
import { z } from 'zod'
import { useState } from 'react'
import { useNavigate } from 'react-router-dom'
import { toast } from 'sonner'
import { api } from '@/lib/api'
import { setToken, decodeJwt } from '@/lib/auth'
import { Button } from '@/components/ui/button'
import { Input } from '@/components/ui/input'
import { Card, CardContent } from '@/components/ui/card'
import { Checkbox } from '@/components/ui/checkbox'
import { Label } from '@/components/ui/label'
import { DotsPattern } from '@/components/decorative/DotsPattern'

const loginSchema = z.object({
  username: z.string().min(1, 'Tài khoản không được để trống'),
  password: z.string().min(1, 'Mật khẩu không được để trống'),
})

type LoginForm = z.infer<typeof loginSchema>

interface LoginResponse {
  jwt: string
}

export function LoginPage() {
  const navigate = useNavigate()
  const [isLoading, setIsLoading] = useState(false)

  const { register, handleSubmit, formState: { errors } } = useForm<LoginForm>({
    resolver: zodResolver(loginSchema),
  })

  async function onSubmit(data: LoginForm) {
    setIsLoading(true)
    try {
      const res = await api<LoginResponse>('/login', {
        method: 'POST',
        body: JSON.stringify(data),
      })
      const payload = decodeJwt(res.jwt)
      if (!payload || payload.role !== 'ADMIN') {
        toast.error('Tài khoản không có quyền admin')
        return
      }
      setToken(res.jwt)
      navigate('/')
    } catch (err: unknown) {
      const msg = err instanceof Error ? err.message : 'Sai tài khoản hoặc mật khẩu'
      toast.error(msg || 'Sai tài khoản hoặc mật khẩu')
    } finally {
      setIsLoading(false)
    }
  }

  return (
    <div className="min-h-screen flex items-center justify-center bg-muted relative overflow-hidden">
      <DotsPattern className="absolute inset-0 -z-10" />

      <Card className="w-full max-w-md shadow-lg">
        <CardContent className="p-8 space-y-6">
          <div className="flex flex-col items-center space-y-3">
            <div className="w-16 h-16 rounded-2xl bg-primary flex items-center justify-center">
              <span className="text-white font-bold text-2xl">VX</span>
            </div>
            <h1 className="text-2xl font-bold">Đăng nhập</h1>
            <p className="text-sm text-muted-foreground">Nhập tài khoản và mật khẩu của bạn:</p>
          </div>

          <form onSubmit={handleSubmit(onSubmit)} className="space-y-4">
            <div className="space-y-1">
              <Input
                placeholder="Tài khoản"
                {...register('username')}
                className="h-12 bg-muted"
                autoComplete="username"
              />
              {errors.username && (
                <p className="text-xs text-destructive">{errors.username.message}</p>
              )}
            </div>
            <div className="space-y-1">
              <Input
                placeholder="Mật khẩu"
                type="password"
                {...register('password')}
                className="h-12 bg-muted"
                autoComplete="current-password"
              />
              {errors.password && (
                <p className="text-xs text-destructive">{errors.password.message}</p>
              )}
            </div>

            <div className="flex items-center justify-between text-sm">
              <label className="flex items-center gap-2 cursor-pointer">
                <Checkbox />
                <span>Nhớ đăng nhập</span>
              </label>
              <button
                type="button"
                className="text-muted-foreground hover:text-primary transition-colors"
                onClick={() => toast.info('Liên hệ admin để đặt lại mật khẩu')}
              >
                Quên mật khẩu?
              </button>
            </div>

            <Button type="submit" className="w-full h-12" disabled={isLoading}>
              {isLoading ? 'Đang đăng nhập...' : 'Đăng nhập'}
            </Button>
          </form>

          <div className="text-center text-sm text-muted-foreground">
            Chưa có tài khoản?{' '}
            <button
              onClick={() => toast.info('Liên hệ admin để tạo tài khoản')}
              className="text-primary hover:underline"
            >
              Đăng ký!
            </button>
          </div>
        </CardContent>
      </Card>
    </div>
  )
}
