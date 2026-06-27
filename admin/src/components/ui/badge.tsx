import * as React from 'react'
import { cva, type VariantProps } from 'class-variance-authority'
import { cn } from '@/lib/utils'

const badgeVariants = cva(
  'inline-flex items-center rounded-full border px-2.5 py-0.5 text-xs font-semibold transition-colors focus:outline-none focus:ring-2 focus:ring-ring focus:ring-offset-2',
  {
    variants: {
      variant: {
        default: 'border-transparent bg-primary text-primary-foreground hover:bg-primary/80',
        secondary: 'border-transparent bg-secondary text-secondary-foreground hover:bg-secondary/80',
        destructive: 'border-transparent bg-destructive text-destructive-foreground hover:bg-destructive/80',
        outline: 'text-foreground',
        teal: 'border-transparent bg-[#C9F7F5] text-[#1BC5BD] dark:bg-[#0C3A38] dark:text-[#1BC5BD]',
        success: 'border-transparent bg-[#E8FFF3] text-[#50CD89] dark:bg-[#1C3238] dark:text-[#50CD89]',
        warning: 'border-transparent bg-[#FFF4DE] text-[#FFA800] dark:bg-[#392F28] dark:text-[#FFA800]',
        danger: 'border-transparent bg-[#FFE2E5] text-[#F1416C] dark:bg-[#3A2434] dark:text-[#F1416C]',
      },
    },
    defaultVariants: {
      variant: 'default',
    },
  }
)

export interface BadgeProps
  extends React.HTMLAttributes<HTMLDivElement>,
    VariantProps<typeof badgeVariants> {}

function Badge({ className, variant, ...props }: BadgeProps) {
  return <div className={cn(badgeVariants({ variant }), className)} {...props} />
}

export { Badge, badgeVariants }
