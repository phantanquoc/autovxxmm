import { useState } from 'react'
import { Bot, ShoppingCart, Coins, Wallet, Clock, CheckCircle, XCircle, Ban } from 'lucide-react'
import { useStats } from './useStats'
import { StatCard, StatRow } from './StatCards'
import { OrderTrendChart } from './OrderTrendChart'
import { RecentOrdersTable } from './RecentOrdersTable'
import { PageHeader } from '@/components/layout/PageHeader'
import { formatXu } from '@/lib/format'
import { Card, CardContent, CardDescription, CardHeader, CardTitle } from '@/components/ui/card'
import { Tabs, TabsList, TabsTrigger } from '@/components/ui/tabs'
import { OwnerFilter } from '@/components/OwnerFilter'
import { getCurrentUser } from '@/lib/auth'

export function DashboardPage() {
  const [chartTab, setChartTab] = useState<'day' | 'month'>('day')
  const [ownerIdFilter, setOwnerIdFilter] = useState<number | undefined>(undefined)
  const isAdmin = getCurrentUser()?.role === 'ADMIN'
  const { data: stats, isLoading } = useStats(isAdmin ? ownerIdFilter : undefined)

  return (
    <div className="space-y-6">
      <PageHeader title="Dashboard" />

      {isAdmin && (
        <div className="flex items-center gap-2">
          <span className="text-sm text-muted-foreground">Người dùng:</span>
          <OwnerFilter value={ownerIdFilter} onChange={setOwnerIdFilter} />
        </div>
      )}

      {/* Row 1: Stat cards */}
      <div className="grid grid-cols-1 md:grid-cols-2 xl:grid-cols-4 gap-4">
        <StatCard
          icon={Bot}
          label="Bot đang treo"
          value={stats ? `${stats.bots.online}/${stats.bots.total}` : '—'}
          variant="primary"
          loading={isLoading}
        />
        <StatCard
          icon={ShoppingCart}
          label="Đơn 24h"
          value={stats?.orders.last24h ?? '—'}
          sub={stats ? `${stats.orders.active} đơn đang xử lý` : undefined}
          variant="warning"
          loading={isLoading}
        />
        <StatCard
          icon={Coins}
          label="Giao dịch xu 24h"
          value={stats?.tradeLogs.last24h ?? '—'}
          sub={stats ? `Tổng: ${stats.tradeLogs.total}` : undefined}
          variant="success"
          loading={isLoading}
        />
        <StatCard
          icon={Wallet}
          label="Tổng xu bot cược"
          value={stats ? `${formatXu(stats.coins.totalBet)} xu` : '—'}
          sub={stats ? `Gom xu: ${formatXu(stats.coins.totalCollected)} xu` : undefined}
          variant="info"
          loading={isLoading}
        />
      </div>

      {/* Row 2: chart + side stat */}
      <div className="grid grid-cols-1 xl:grid-cols-3 gap-4">
        <Card className="xl:col-span-2">
          <CardHeader>
            <div className="flex items-center justify-between">
              <div>
                <CardTitle>Phân tích đơn hàng</CardTitle>
                <CardDescription>
                  Số đơn theo {chartTab === 'day' ? 'ngày' : 'tháng'}
                </CardDescription>
              </div>
              <Tabs value={chartTab} onValueChange={v => setChartTab(v as 'day' | 'month')}>
                <TabsList>
                  <TabsTrigger value="day">Ngày</TabsTrigger>
                  <TabsTrigger value="month">Tháng</TabsTrigger>
                </TabsList>
              </Tabs>
            </div>
          </CardHeader>
          <CardContent>
            <OrderTrendChart tab={chartTab} />
          </CardContent>
        </Card>

        <Card>
          <CardHeader>
            <CardTitle>Thống kê tài khoản</CardTitle>
            <CardDescription>Chi tiết số liệu</CardDescription>
          </CardHeader>
          <CardContent className="space-y-1">
            <StatRow
              icon={<ShoppingCart className="h-4 w-4 text-[#3699FF]" />}
              bg="bg-[#E1F0FF] dark:bg-[#1a2a3a]"
              label="Tổng đơn hàng"
              value={stats?.orders.total ?? '—'}
            />
            <StatRow
              icon={<Clock className="h-4 w-4 text-[#FFA800]" />}
              bg="bg-[#FFF4DE] dark:bg-[#392F28]"
              label="Đơn đang chờ"
              value={stats?.orders.active ?? '—'}
            />
            <StatRow
              icon={<CheckCircle className="h-4 w-4 text-[#50CD89]" />}
              bg="bg-[#E8FFF3] dark:bg-[#1C3238]"
              label="Bot đang online"
              value={stats?.bots.online ?? '—'}
            />
            <StatRow
              icon={<XCircle className="h-4 w-4 text-muted-foreground" />}
              bg="bg-muted"
              label="Bot offline"
              value={stats ? stats.bots.total - stats.bots.online : '—'}
            />
            <StatRow
              icon={<Ban className="h-4 w-4 text-[#F1416C]" />}
              bg="bg-[#FFE2E5] dark:bg-[#3A2434]"
              label="Giao dịch hôm nay"
              value={stats?.tradeLogs.last24h ?? '—'}
            />
          </CardContent>
        </Card>
      </div>

      {/* Row 3: recent orders */}
      <Card>
        <CardHeader>
          <CardTitle>Đơn hàng gần đây</CardTitle>
        </CardHeader>
        <CardContent className="p-0">
          <RecentOrdersTable />
        </CardContent>
      </Card>
    </div>
  )
}
