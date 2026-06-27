import { useNavigate } from 'react-router-dom'
import { Button } from '@/components/ui/button'

export function NotFound() {
  const navigate = useNavigate()
  return (
    <div className="flex flex-col items-center justify-center min-h-[60vh] gap-4 text-center">
      <div className="text-8xl font-bold text-muted-foreground/30">404</div>
      <h1 className="text-2xl font-bold">Không tìm thấy trang</h1>
      <p className="text-muted-foreground">Trang bạn tìm kiếm không tồn tại hoặc đã bị xoá.</p>
      <Button onClick={() => navigate('/')}>Về trang chủ</Button>
    </div>
  )
}
