import { cn } from '@/lib/utils'

type BotStatus = 'ONLINE' | 'OFFLINE' | 'CONNECTING' | string

interface BotStatusDotProps {
  status: BotStatus
}

export function BotStatusDot({ status }: BotStatusDotProps) {
  const colorClass =
    status === 'ONLINE' ? 'bg-[#50CD89]' :
    status === 'CONNECTING' ? 'bg-[#FFA800] animate-pulse-slow' :
    'bg-[#A1A5B7]'

  return (
    <span
      className={cn('inline-block rounded-full w-2 h-2 shrink-0', colorClass)}
      title={status}
      aria-label={`Trạng thái: ${status}`}
    />
  )
}
