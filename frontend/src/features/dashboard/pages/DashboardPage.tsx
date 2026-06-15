import { useMemo } from 'react'
import { Link } from 'react-router-dom'

import { EmptyState } from '../../../components/feedback/EmptyState'
import { ErrorBanner } from '../../../components/feedback/ErrorBanner'
import { Skeleton } from '../../../components/feedback/Skeleton'
import { PageHeader } from '../../../components/layout/PageHeader'
import { Badge } from '../../../components/ui/Badge'
import { Button } from '../../../components/ui/Button'
import { Card } from '../../../components/ui/Card'
import { getApiErrorMessage, isForbiddenApiError } from '../../../lib/api/errors'
import type { InventoryStockResponse } from '../../../lib/api/inventory'
import type { TopSellingProductResponse } from '../../../lib/api/reports'
import type { UserRole } from '../../../lib/api/users'
import { classNames } from '../../../lib/classNames'
import { formatCurrency } from '../../../lib/format/currency'
import {
  formatDateTime,
  formatMonth,
  getCurrentYearMonth,
  toLocalDateInputValue,
} from '../../../lib/format/date'
import { useAuth } from '../../auth/useAuth'
import { useCashierDashboardQuery, useManagementDashboardQueries } from '../queries'

interface KpiCardProps {
  description: string
  error: unknown
  isLoading: boolean
  label: string
  meta?: string
  onRetry: () => void
  value?: string
}

interface TopProductsPreviewProps {
  error: unknown
  isLoading: boolean
  isRefreshing: boolean
  items?: TopSellingProductResponse[]
  onRetry: () => void
}

interface LowStockPreviewProps {
  description: string
  error: unknown
  isLoading: boolean
  isRefreshing: boolean
  items?: InventoryStockResponse[]
  onRetry: () => void
  title: string
}

interface QuickActionCardProps {
  description: string
  label: string
  to: string
}

const TOP_PRODUCTS_PREVIEW_LIMIT = 5
const LOW_STOCK_PREVIEW_LIMIT = 6

function formatCount(value: number) {
  return new Intl.NumberFormat(undefined, { maximumFractionDigits: 0 }).format(value)
}

function getDashboardTitle(role: UserRole | null | undefined) {
  if (role === 'CASHIER') {
    return 'Cashier dashboard'
  }

  return 'Dashboard overview'
}

function getErrorTitle(error: unknown) {
  return isForbiddenApiError(error) ? 'Access denied' : 'Dashboard data unavailable'
}

function getErrorMessage(error: unknown) {
  if (isForbiddenApiError(error)) {
    return 'The backend refused this dashboard request for the current role.'
  }

  return getApiErrorMessage(error, 'Dashboard data could not be loaded.')
}

function RetryButton({ onRetry }: { onRetry: () => void }) {
  return (
    <Button onClick={onRetry} size="sm" variant="secondary">
      Retry
    </Button>
  )
}

function KpiCard({
  description,
  error,
  isLoading,
  label,
  meta,
  onRetry,
  value,
}: KpiCardProps) {
  return (
    <Card description={description} title={label}>
      {isLoading ? (
        <div className="space-y-4" aria-label={`${label} loading`} role="status">
          <Skeleton className="h-9 w-32" radius="lg" />
          <Skeleton className="h-4 w-48" />
        </div>
      ) : error ? (
        <ErrorBanner
          action={<RetryButton onRetry={onRetry} />}
          message={getErrorMessage(error)}
          title={getErrorTitle(error)}
        />
      ) : (
        <div className="space-y-3">
          <p className="font-rc-data text-3xl font-semibold tracking-tight text-rc-foreground">
            {value ?? '—'}
          </p>
          {meta ? <p className="text-sm text-rc-secondary">{meta}</p> : null}
        </div>
      )}
    </Card>
  )
}

function TableSkeleton({ rows = 4 }: { rows?: number }) {
  return (
    <div className="space-y-3" aria-label="Dashboard table loading" role="status">
      {Array.from({ length: rows }, (_, index) => (
        <div className="grid grid-cols-[1fr_5rem] gap-4" key={index}>
          <Skeleton className="h-5" />
          <Skeleton className="h-5" />
        </div>
      ))}
    </div>
  )
}

function TopProductsPreview({
  error,
  isLoading,
  isRefreshing,
  items,
  onRetry,
}: TopProductsPreviewProps) {
  const previewItems = items?.slice(0, TOP_PRODUCTS_PREVIEW_LIMIT) ?? []

  return (
    <Card
      actions={isRefreshing ? <Badge variant="info">Refreshing</Badge> : null}
      description="Ranked by sold quantity and gross sales from /api/reports/top-products."
      title="Top products preview"
    >
      {isLoading ? <TableSkeleton /> : null}

      {!isLoading && error ? (
        <ErrorBanner
          action={<RetryButton onRetry={onRetry} />}
          message={getErrorMessage(error)}
          title={getErrorTitle(error)}
        />
      ) : null}

      {!isLoading && !error && previewItems.length === 0 ? (
        <EmptyState
          description="No top-selling products were returned by the reports API yet. Complete sales first, then this panel has something to rank."
          title="No sales ranking yet"
        />
      ) : null}

      {!isLoading && !error && previewItems.length > 0 ? (
        <div className="overflow-x-auto">
          <table className="min-w-full text-left text-sm">
            <thead className="border-b border-rc-border text-xs uppercase tracking-[0.16em] text-rc-secondary">
              <tr>
                <th className="pb-3 pr-4 font-semibold" scope="col">
                  Product
                </th>
                <th className="pb-3 pr-4 font-semibold" scope="col">
                  Qty
                </th>
                <th className="pb-3 text-right font-semibold" scope="col">
                  Gross sales
                </th>
              </tr>
            </thead>
            <tbody className="divide-y divide-rc-border">
              {previewItems.map((item) => (
                <tr key={item.productId}>
                  <td className="py-3 pr-4">
                    <div className="font-medium text-rc-foreground">{item.productName}</div>
                    <div className="font-rc-data text-xs text-rc-secondary">{item.sku}</div>
                  </td>
                  <td className="py-3 pr-4 font-rc-data text-rc-foreground">
                    {formatCount(item.quantitySold)}
                  </td>
                  <td className="py-3 text-right font-rc-data text-rc-foreground">
                    {formatCurrency(item.grossSales)}
                  </td>
                </tr>
              ))}
            </tbody>
          </table>
        </div>
      ) : null}
    </Card>
  )
}

function LowStockPreview({
  description,
  error,
  isLoading,
  isRefreshing,
  items,
  onRetry,
  title,
}: LowStockPreviewProps) {
  const previewItems = items?.slice(0, LOW_STOCK_PREVIEW_LIMIT) ?? []

  return (
    <Card
      actions={isRefreshing ? <Badge variant="info">Refreshing</Badge> : null}
      description={description}
      title={title}
    >
      {isLoading ? <TableSkeleton rows={5} /> : null}

      {!isLoading && error ? (
        <ErrorBanner
          action={<RetryButton onRetry={onRetry} />}
          message={getErrorMessage(error)}
          title={getErrorTitle(error)}
        />
      ) : null}

      {!isLoading && !error && previewItems.length === 0 ? (
        <EmptyState
          description="No products are currently at or below the low-stock threshold. Rare clean state. Screenshot it before reality patches it."
          title="No low-stock alerts"
        />
      ) : null}

      {!isLoading && !error && previewItems.length > 0 ? (
        <div className="space-y-3">
          {previewItems.map((item) => (
            <div
              className="flex flex-col gap-3 rounded-xl border border-rc-warning/25 bg-rc-warning-muted/55 p-3 sm:flex-row sm:items-center sm:justify-between"
              key={item.productId}
            >
              <div className="min-w-0">
                <div className="flex flex-wrap items-center gap-2">
                  <p className="font-medium text-rc-foreground">{item.productName}</p>
                  <Badge variant="warning">Low stock</Badge>
                </div>
                <p className="font-rc-data text-xs text-rc-secondary">{item.sku}</p>
                <p className="text-xs text-rc-secondary">
                  Updated {formatDateTime(item.updatedAt)}
                </p>
              </div>
              <div className="grid grid-cols-2 gap-3 text-sm sm:min-w-52">
                <div>
                  <p className="text-xs uppercase tracking-[0.16em] text-rc-secondary">
                    On hand
                  </p>
                  <p className="font-rc-data text-lg font-semibold text-rc-warning-strong">
                    {formatCount(item.quantity)}
                  </p>
                </div>
                <div>
                  <p className="text-xs uppercase tracking-[0.16em] text-rc-secondary">
                    Threshold
                  </p>
                  <p className="font-rc-data text-lg font-semibold text-rc-foreground">
                    {formatCount(item.lowStockThreshold)}
                  </p>
                </div>
              </div>
            </div>
          ))}
        </div>
      ) : null}
    </Card>
  )
}

function QuickActionCard({ description, label, to }: QuickActionCardProps) {
  return (
    <Link
      className={classNames(
        'block rounded-2xl border border-rc-border bg-rc-surface p-5 shadow-rc-card transition-colors',
        'hover:border-rc-accent/40 hover:bg-rc-accent-muted/35',
        'focus-visible:outline-2 focus-visible:outline-offset-2 focus-visible:outline-rc-ring',
      )}
      to={to}
    >
      <p className="text-base font-semibold tracking-tight text-rc-foreground">{label}</p>
      <p className="mt-2 text-sm leading-6 text-rc-secondary">{description}</p>
    </Link>
  )
}

function ManagementDashboard({ role }: { role: UserRole }) {
  const reportDate = useMemo(() => new Date(), [])
  const reportDateValue = toLocalDateInputValue(reportDate)
  const reportMonth = getCurrentYearMonth(reportDate)
  const reportMonthLabel = formatMonth(reportDate)
  const {
    dailySalesQuery,
    lowStockQuery,
    monthlySalesQuery,
    topProductsQuery,
  } = useManagementDashboardQueries({
    enabled: true,
    reportDate: reportDateValue,
    reportMonth,
  })

  const lowStockCount = lowStockQuery.data?.length ?? 0
  const isRefreshing = [dailySalesQuery, monthlySalesQuery, lowStockQuery, topProductsQuery].some(
    (query) => query.isFetching && !query.isLoading,
  )

  return (
    <div className="space-y-5">
      <PageHeader
        description="Management KPIs are loaded from report endpoints. Cashier-safe mode is separate, because 403 spam is not a dashboard strategy."
        eyebrow="FE-06 dashboard"
        meta={
          <>
            <Badge variant="info">{role}</Badge>
            <Badge variant="success">/api/reports enabled</Badge>
            {isRefreshing ? <Badge variant="info">Refreshing</Badge> : null}
          </>
        }
        title={getDashboardTitle(role)}
      />

      <div className="grid gap-4 md:grid-cols-3">
        <KpiCard
          description="Daily sales total from /api/reports/daily-sales."
          error={dailySalesQuery.error}
          isLoading={dailySalesQuery.isLoading}
          label="Today sales"
          meta={`Period ${dailySalesQuery.data?.period ?? reportDateValue}`}
          onRetry={() => void dailySalesQuery.refetch()}
          value={
            dailySalesQuery.data ? formatCurrency(dailySalesQuery.data.totalAmount) : undefined
          }
        />
        <KpiCard
          description="Monthly sales total from /api/reports/monthly-sales."
          error={monthlySalesQuery.error}
          isLoading={monthlySalesQuery.isLoading}
          label="Month sales"
          meta={monthlySalesQuery.data?.period ?? reportMonthLabel}
          onRetry={() => void monthlySalesQuery.refetch()}
          value={
            monthlySalesQuery.data
              ? formatCurrency(monthlySalesQuery.data.totalAmount)
              : undefined
          }
        />
        <KpiCard
          description="Low-stock count from /api/reports/low-stock."
          error={lowStockQuery.error}
          isLoading={lowStockQuery.isLoading}
          label="Low-stock items"
          meta="Products at or below threshold"
          onRetry={() => void lowStockQuery.refetch()}
          value={lowStockQuery.data ? formatCount(lowStockCount) : undefined}
        />
      </div>

      <div className="grid gap-5 xl:grid-cols-[1.15fr_0.85fr]">
        <TopProductsPreview
          error={topProductsQuery.error}
          isLoading={topProductsQuery.isLoading}
          isRefreshing={topProductsQuery.isFetching && !topProductsQuery.isLoading}
          items={topProductsQuery.data}
          onRetry={() => void topProductsQuery.refetch()}
        />
        <LowStockPreview
          description="Management low-stock report from /api/reports/low-stock."
          error={lowStockQuery.error}
          isLoading={lowStockQuery.isLoading}
          isRefreshing={lowStockQuery.isFetching && !lowStockQuery.isLoading}
          items={lowStockQuery.data}
          onRetry={() => void lowStockQuery.refetch()}
          title="Low-stock watch"
        />
      </div>
    </div>
  )
}

function CashierDashboard({ role }: { role: UserRole }) {
  const lowStockQuery = useCashierDashboardQuery({ enabled: true })

  return (
    <div className="space-y-5">
      <PageHeader
        description="Cashier view avoids /api/reports/** and sticks to allowed operational data plus checkout actions. No forbidden endpoint roulette."
        eyebrow="FE-06 dashboard"
        meta={
          <>
            <Badge variant="info">{role}</Badge>
            <Badge variant="success">Reports blocked by design</Badge>
          </>
        }
        title={getDashboardTitle(role)}
      />

      <div className="grid gap-4 md:grid-cols-2">
        <QuickActionCard
          description="Start a real checkout flow when FE-09 lands. Route access is already role-aware."
          label="Go to checkout"
          to="/checkout"
        />
        <QuickActionCard
          description="Open sales history for receipt lookup and refunds when FE-10 lands."
          label="Review sales"
          to="/sales"
        />
      </div>

      <LowStockPreview
        description="Cashier-safe operational watch from /api/inventory/low-stock. No /api/reports calls are made for this role."
        error={lowStockQuery.error}
        isLoading={lowStockQuery.isLoading}
        isRefreshing={lowStockQuery.isFetching && !lowStockQuery.isLoading}
        items={lowStockQuery.data}
        onRetry={() => void lowStockQuery.refetch()}
        title="Inventory alerts"
      />
    </div>
  )
}

export function DashboardPage() {
  const { user } = useAuth()
  const role = user?.role

  if (role === 'ADMIN' || role === 'MANAGER') {
    return <ManagementDashboard role={role} />
  }

  if (role === 'CASHIER') {
    return <CashierDashboard role={role} />
  }

  return (
    <ErrorBanner
      message="No authenticated role is available for this dashboard session. Sign in again to reload route access."
      title="Dashboard unavailable"
    />
  )
}
