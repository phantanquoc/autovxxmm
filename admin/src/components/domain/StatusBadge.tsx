import { cn } from '@/lib/utils'

const STATUS_MAP = {
  0: { label: 'ĐANG CHỜ', className: 'bg-[#E1E3EA] text-[#7E8299] dark:bg-[#2B2B40]/60 dark:text-[#7E8299]' },
  1: { label: 'ĐẶT CƯỢC', className: 'bg-[#FFF4DE] text-[#FFA800] dark:bg-[#392F28] dark:text-[#FFA800]' },
  2: { label: 'THUA CUỘC', className: 'bg-[#FFE2E5] text-[#F1416C] dark:bg-[#3A2434] dark:text-[#F1416C]' },
  3: { label: 'THẮNG CUỘC', className: 'bg-[#E8FFF3] text-[#50CD89] dark:bg-[#1C3238] dark:text-[#50CD89]' },
  4: { label: 'ĐÃ THƯỞNG', className: 'bg-[#C9F7F5] text-[#1BC5BD] dark:bg-[#0C3A38] dark:text-[#1BC5BD]' },
  5: { label: 'LỖI', className: 'bg-[#FFE2E5] text-[#F64E60] dark:bg-[#3A2434] dark:text-[#F64E60]' },
} as const

type StatusKey = keyof typeof STATUS_MAP

interface StatusBadgeProps {
  status: number
  size?: 'sm' | 'md'
}

export function StatusBadge({ status, size = 'md' }: StatusBadgeProps) {
  const entry = STATUS_MAP[status as StatusKey] ?? STATUS_MAP[0]
  return (
    <span
      className={cn(
        'inline-flex items-center rounded-full font-semibold uppercase tracking-wide',
        entry.className,
        size === 'sm' ? 'px-2 py-0.5 text-[10px]' : 'px-3 py-1 text-xs'
      )}
    >
      {entry.label}
    </span>
  )
}
