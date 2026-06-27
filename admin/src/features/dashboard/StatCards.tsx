import { ReactNode } from 'react'
import { cn } from '@/lib/utils'
import { Card, CardContent } from '@/components/ui/card'
import { Skeleton } from '@/components/ui/skeleton'

interface StatCardProps {
  icon: React.ElementType
  label: string
  value: string | number
  sub?: string
  trend?: number
  variant?: 'primary' | 'warning' | 'success' | 'info'
  loading?: boolean
}

const variantMap = {
  primary: 'text-[#3699FF] bg-[#E1F0FF] dark:bg-[#1a2a3a]',
  warning: 'text-[#FFA800] bg-[#FFF4DE] dark:bg-[#392F28]',
  success: 'text-[#50CD89] bg-[#E8FFF3] dark:bg-[#1C3238]',
  info: 'text-[#3699FF] bg-[#E1F0FF] dark:bg-[#1a2a3a]',
}

export function StatCard({ icon: Icon, label, value, sub, variant = 'primary', loading }: StatCardProps) {
  const colorClass = variantMap[variant]

  if (loading) {
    return (
      <Card>
        <CardContent className="p-5">
          <Skeleton className="h-12 w-full" />
        </CardContent>
      </Card>
    )
  }

  return (
    <Card className="hover:shadow-md transition-shadow">
      <CardContent className="p-5">
        <div className="flex items-center gap-4">
          <div className={cn('w-12 h-12 rounded-lg flex items-center justify-center shrink-0', colorClass)}>
            <Icon className="h-6 w-6" />
          </div>
          <div className="flex-1 min-w-0">
            <p className="text-sm text-muted-foreground truncate">{label}</p>
            <p className="text-2xl font-bold tracking-tight">{value}</p>
            {sub && <p className="text-xs text-muted-foreground mt-0.5">{sub}</p>}
          </div>
        </div>
      </CardContent>
    </Card>
  )
}

interface StatRowProps {
  icon: ReactNode
  bg: string
  label: string
  value: string | number
}

export function StatRow({ icon, bg, label, value }: StatRowProps) {
  return (
    <div className="flex items-center gap-3 py-1.5">
      <div className={cn('w-8 h-8 rounded-md flex items-center justify-center shrink-0', bg)}>
        {icon}
      </div>
      <span className="flex-1 text-sm text-muted-foreground">{label}</span>
      <span className="font-semibold text-sm">{value}</span>
    </div>
  )
}
