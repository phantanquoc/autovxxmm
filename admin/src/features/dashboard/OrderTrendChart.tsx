import { useMemo } from 'react'
import { useQuery } from '@tanstack/react-query'
import { format } from 'date-fns'
import { apiList } from '@/lib/api'
import { buildQS } from '@/lib/buildQS'
import { getCurrentUser } from '@/lib/auth'
import { Order } from '@/features/orders/useOrders'
import {
  LineChart,
  Line,
  XAxis,
  YAxis,
  CartesianGrid,
  Tooltip,
  ResponsiveContainer,
} from 'recharts'

interface OrderTrendChartProps {
  tab: 'day' | 'month'
}

export function OrderTrendChart({ tab }: OrderTrendChartProps) {
  const qs = buildQS({ page: 1, pageSize: 200, sort: 'createdAt', order: 'desc' })
  const isAdmin = getCurrentUser()?.role === 'ADMIN'
  const basePath = isAdmin ? '/admin/orders' : '/me/orders'
  const { data: result, isLoading } = useQuery({
    queryKey: ['orders-chart', basePath, qs],
    queryFn: () => apiList<Order>(`${basePath}?${qs}`),
    staleTime: 60000,
  })
  const orders = result?.items ?? []

  const chartData = useMemo(() => {
    const groups: Record<string, number> = {}
    orders.forEach(o => {
      const d = new Date(o.createdAt)
      const key = tab === 'day'
        ? format(d, 'dd/MM')
        : format(d, 'MM/yyyy')
      groups[key] = (groups[key] ?? 0) + 1
    })
    return Object.entries(groups)
      .sort(([a], [b]) => a.localeCompare(b))
      .map(([date, count]) => ({ date, count }))
  }, [orders, tab])

  if (isLoading) {
    return <div className="h-[200px] flex items-center justify-center text-muted-foreground text-sm">Đang tải...</div>
  }

  if (chartData.length === 0) {
    return <div className="h-[200px] flex items-center justify-center text-muted-foreground text-sm">Không có dữ liệu</div>
  }

  return (
    <ResponsiveContainer width="100%" height={200}>
      <LineChart data={chartData} margin={{ top: 5, right: 10, left: 0, bottom: 5 }}>
        <CartesianGrid strokeDasharray="3 3" stroke="hsl(var(--border))" />
        <XAxis dataKey="date" tick={{ fontSize: 11, fill: 'hsl(var(--muted-foreground))' }} />
        <YAxis tick={{ fontSize: 11, fill: 'hsl(var(--muted-foreground))' }} />
        <Tooltip
          contentStyle={{
            background: 'hsl(var(--card))',
            border: '1px solid hsl(var(--border))',
            borderRadius: '6px',
            fontSize: 12,
          }}
        />
        <Line type="monotone" dataKey="count" stroke="hsl(var(--primary))" strokeWidth={2} dot={false} />
      </LineChart>
    </ResponsiveContainer>
  )
}
