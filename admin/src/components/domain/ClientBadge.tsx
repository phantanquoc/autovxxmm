import { cn } from '@/lib/utils'

interface ClientBadgeProps {
  value: number
}

export function ClientBadge({ value }: ClientBadgeProps) {
  if (!value || value === 0) {
    return <span className="text-sm text-muted-foreground">Normal</span>
  }
  return (
    <span className={cn(
      'inline-flex items-center rounded-full px-2 py-0.5 text-xs font-medium',
      'bg-purple-50 dark:bg-purple-900/30 text-purple-700 dark:text-purple-400'
    )}>
      Client {value}
    </span>
  )
}
