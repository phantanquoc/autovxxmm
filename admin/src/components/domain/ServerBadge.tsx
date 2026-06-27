interface ServerBadgeProps {
  id: number
}

export function ServerBadge({ id }: ServerBadgeProps) {
  return (
    <span className="inline-flex items-center rounded-full bg-blue-50 dark:bg-blue-900/30 text-blue-600 dark:text-blue-400 px-2 py-0.5 text-xs font-mono font-medium shrink-0">
      SV{id}
    </span>
  )
}
