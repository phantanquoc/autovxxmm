import { useForm } from 'react-hook-form'
import { zodResolver } from '@hookform/resolvers/zod'
import { z } from 'zod'
import { useState } from 'react'
import { useNavigate, Link } from 'react-router-dom'
import { toast } from 'sonner'
import { api } from '@/lib/api'
import { setToken } from '@/lib/auth'
import { Button } from '@/components/ui/button'
import { Input } from '@/components/ui/input'
import { Card, CardContent } from '@/components/ui/card'
import { DotsPattern } from '@/components/decorative/DotsPattern'

const registerSchema = z.object({
  username: z.string().min(3, 'Tài khoản phải có ít nhất 3 ký tự'),
  password: z.string().min(6, 'Mật khẩu phải có ít nhất 6 ký tự'),
  confirmPassword: z.string().min(1, 'Vui lòng xác nhận mật khẩu'),
}).refine((d) => d.password === d.confirmPassword, {
  message: 'Mật khẩu xác nhận không khớp',
  path: ['confirmPassword'],
})

type RegisterForm = z.infer<typeof registerSchema>

interface RegisterResponse {
  jwt: string
}

export function RegisterPage() {
  const navigate = useNavigate()
  const [isLoading, setIsLoading] = useState(false)

  const { register, handleSubmit, formState: { errors } } = useForm<RegisterForm>({
    resolver: zodResolver(registerSchema),
  })

  async function onSubmit(data: RegisterForm) {
    setIsLoading(true)
    try {
      const res = await api<RegisterResponse>('/register', {
        method: 'POST',
        body: JSON.stringify({ username: data.username, password: data.password }),
      })
      setToken(res.jwt)
      navigate('/')
    } catch (err: unknown) {
      const msg = err instanceof Error ? err.message : 'Đăng ký thất bại'
      toast.error(msg || 'Đăng ký thất bại')
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
            <h1 className="text-2xl font-bold">Đăng ký</h1>
            <p className="text-sm text-muted-foreground">Tạo tài khoản mới:</p>
          </div>

          <form onSubmit={handleSubmit(onSubmit)} className="space-y-4">
            <div className="space-y-1">
              <Input
                placeholder="Tài khoản (ít nhất 3 ký tự)"
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
                placeholder="Mật khẩu (ít nhất 6 ký tự)"
                type="password"
                {...register('password')}
                className="h-12 bg-muted"
                autoComplete="new-password"
              />
              {errors.password && (
                <p className="text-xs text-destructive">{errors.password.message}</p>
              )}
            </div>
            <div className="space-y-1">
              <Input
                placeholder="Xác nhận mật khẩu"
                type="password"
                {...register('confirmPassword')}
                className="h-12 bg-muted"
                autoComplete="new-password"
              />
              {errors.confirmPassword && (
                <p className="text-xs text-destructive">{errors.confirmPassword.message}</p>
              )}
            </div>

            <Button type="submit" className="w-full h-12" disabled={isLoading}>
              {isLoading ? 'Đang đăng ký...' : 'Đăng ký'}
            </Button>
          </form>

          <div className="text-center text-sm text-muted-foreground">
            Đã có tài khoản?{' '}
            <Link to="/login" className="text-primary hover:underline">
              Đăng nhập
            </Link>
          </div>
        </CardContent>
      </Card>
    </div>
  )
}
