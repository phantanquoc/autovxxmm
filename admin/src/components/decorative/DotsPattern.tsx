import { cn } from '@/lib/utils'

interface DotsPatternProps {
  className?: string
}

export function DotsPattern({ className }: DotsPatternProps) {
  return (
    <svg
      className={cn('w-full h-full opacity-30 text-primary', className)}
      xmlns="http://www.w3.org/2000/svg"
      aria-hidden="true"
    >
      <defs>
        <pattern id="dots" x="0" y="0" width="24" height="24" patternUnits="userSpaceOnUse">
          <circle cx="2" cy="2" r="1.5" fill="currentColor" />
        </pattern>
      </defs>
      <rect width="100%" height="100%" fill="url(#dots)" />
    </svg>
  )
}
