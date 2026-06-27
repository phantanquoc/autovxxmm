import { ReactNode } from 'react'

interface PageHeaderProps {
  title: string
  breadcrumb?: ReactNode
  actions?: ReactNode
}

export function PageHeader({ title, breadcrumb, actions }: PageHeaderProps) {
  return (
    <div className="flex items-center justify-between mb-6">
      <div>
        {breadcrumb && <div className="text-sm text-muted-foreground mb-1">{breadcrumb}</div>}
        <h1 className="text-2xl font-bold tracking-tight">{title}</h1>
      </div>
      {actions && <div className="flex items-center gap-2">{actions}</div>}
    </div>
  )
}
