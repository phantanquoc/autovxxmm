import { useRouteError } from 'react-router-dom'
import { Button } from '@/components/ui/button'

export function ErrorBoundary() {
  const error = useRouteError() as Error | undefined
  return (
    <div className="min-h-screen flex items-center justify-center bg-muted">
      <div className="bg-card border border-destructive/30 rounded-lg p-8 max-w-md w-full text-center space-y-4">
        <div className="w-12 h-12 rounded-full bg-destructive/10 flex items-center justify-center mx-auto">
          <span className="text-destructive text-xl font-bold">!</span>
        </div>
        <h1 className="text-xl font-bold text-destructive">Đã xảy ra lỗi</h1>
        {error?.message && (
          <p className="text-sm text-muted-foreground font-mono bg-muted rounded p-2">
            {error.message}
          </p>
        )}
        <Button onClick={() => window.location.reload()} variant="outline">
          Tải lại trang
        </Button>
      </div>
    </div>
  )
}
