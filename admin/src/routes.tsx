import { createHashRouter } from 'react-router-dom'
import { AppLayout } from '@/components/layout/AppLayout'
import { AuthGuard, AdminOnlyRoute } from '@/features/auth/AuthGuard'
import { LoginPage } from '@/features/auth/LoginPage'
import { RegisterPage } from '@/features/auth/RegisterPage'
import { DashboardPage } from '@/features/dashboard/DashboardPage'
import { BotListPage } from '@/features/bots/BotListPage'
import { CollectBotsListPage } from '@/features/collect-bots/CollectBotsListPage'
import { OrderListPage } from '@/features/orders/OrderListPage'
import { TradeLogListPage } from '@/features/trade-logs/TradeLogListPage'
import { ServerListPage } from '@/features/servers/ServerListPage'
import { UserListPage } from '@/features/users/UserListPage'
import { BlockListPage } from '@/features/blocks/BlockListPage'
import { SettingsPage } from '@/features/settings/SettingsPage'
import { NotFound } from '@/features/error/NotFound'
import { ErrorBoundary } from '@/features/error/ErrorBoundary'

export const router = createHashRouter([
  { path: '/login', element: <LoginPage /> },
  { path: '/register', element: <RegisterPage /> },
  {
    element: (
      <AuthGuard>
        <AppLayout />
      </AuthGuard>
    ),
    errorElement: <ErrorBoundary />,
    children: [
      { path: '/', element: <DashboardPage /> },
      { path: '/bots', element: <BotListPage /> },
      { path: '/collect-bots', element: <CollectBotsListPage /> },
      { path: '/orders', element: <OrderListPage /> },
      { path: '/trade-logs', element: <TradeLogListPage /> },
      {
        path: '/servers',
        element: (
          <AdminOnlyRoute>
            <ServerListPage />
          </AdminOnlyRoute>
        ),
      },
      {
        path: '/users',
        element: (
          <AdminOnlyRoute>
            <UserListPage />
          </AdminOnlyRoute>
        ),
      },
      { path: '/blocks', element: <BlockListPage /> },
      { path: '/settings', element: <SettingsPage /> },
      { path: '*', element: <NotFound /> },
    ],
  },
])
