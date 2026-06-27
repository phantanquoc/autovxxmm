import { cn } from '@/lib/utils'
import { formatXu } from '@/lib/format'

interface XuAmountProps {
  value: number
  muted?: boolean
  showUnit?: boolean
}

export function XuAmount({ value, muted = false, showUnit = true }: XuAmountProps) {
  return (
    <span className={cn('font-mono text-sm', muted && 'text-muted-foreground')}>
      {formatXu(value)}{showUnit ? ' xu' : ''}
    </span>
  )
}
