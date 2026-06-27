import { OrderListPage } from '@/features/orders/OrderListPage'

export function RecentOrdersTable() {
  return <OrderListPage limit={10} hideHeader />
}
