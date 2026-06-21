import { useMemo, useState } from 'react'

import { EmptyState } from '../../../components/feedback/EmptyState'
import { ErrorBanner } from '../../../components/feedback/ErrorBanner'
import { Skeleton } from '../../../components/feedback/Skeleton'
import { PageHeader } from '../../../components/layout/PageHeader'
import { Badge } from '../../../components/ui/Badge'
import { Button } from '../../../components/ui/Button'
import { Card } from '../../../components/ui/Card'
import { Input } from '../../../components/ui/Input'
import { Select } from '../../../components/ui/Select'
import { getApiErrorMessage, isForbiddenApiError } from '../../../lib/api/errors'
import type { InventoryStockResponse } from '../../../lib/api/inventory'
import type {
  CashierSalesReportResponse,
  PaymentMethodSummaryResponse,
  SalesTotalResponse,
  TopSellingProductResponse,
} from '../../../lib/api/reports'
import { formatCurrency } from '../../../lib/format/currency'
import {
  formatDateTime,
  formatMonth,
  getCurrentYearMonth,
  toLocalDateInputValue,
} from '../../../lib/format/date'
import {
  useDailySalesQuery,
  useLowStockReportQuery,
  useMonthlySalesQuery,
  usePaymentSummaryQuery,
  useSalesByCashierQuery,
  useTopProductsQuery,
} from '../queries'

const monthOptions = [
  { label: 'January', value: 1 },
  { label: 'February', value: 2 },
  { label: 'March', value: 3 },
  { label: 'April', value: 4 },
  { label: 'May', value: 5 },
  { label: 'June', value: 6 },
  { label: 'July', value: 7 },
  { label: 'August', value: 8 },
  { label: 'September', value: 9 },
  { label: 'October', value: 10 },
  { label: 'November', value: 11 },
  { label: 'December', value: 12 },
]

function formatCount(value: number) {
  return new Intl.NumberFormat(undefined, { maximumFractionDigits: 0 }).format(value)
}

function getErrorTitle(error: unknown) {
  return isForbiddenApiError(error) ? 'Access denied' : 'Report unavailable'
}

function getErrorMessage(error: unknown) {
  if (isForbiddenApiError(error)) {
    return 'Reports are restricted to ADMIN and MANAGER users.'
  }

  return getApiErrorMessage(error, 'The report data could not be loaded.')
}

function RetryButton({ onRetry }: { onRetry: () => void }) {
  return (
    <Button onClick={onRetry} size="sm" variant="secondary">
      Retry
    </Button>
  )
}

function TableSkeleton({ rows = 5 }: { rows?: number }) {
  return (
    <div className="space-y-3" aria-label="Report table loading" role="status">
      {Array.from({ length: rows }, (_, index) => (
        <div className="grid grid-cols-[1fr_6rem_7rem] gap-4" key={index}>
          <Skeleton className="h-5" />
          <Skeleton className="h-5" />
          <Skeleton className="h-5" />
        </div>
      ))}
    </div>
  )
}

function TotalCard({
  error,
  isFetching,
  isLoading,
  label,
  onRetry,
  periodLabel,
  value,
}: {
  error: unknown
  isFetching: boolean
  isLoading: boolean
  label: string
  onRetry: () => void
  periodLabel: string
  value?: SalesTotalResponse
}) {
  return (
    <Card
      actions={isFetching && !isLoading ? <Badge variant="info">Refreshing</Badge> : null}
      description={`Period: ${value?.period ?? periodLabel}`}
      title={label}
    >
      {isLoading ? (
        <div className="space-y-4" aria-label={`${label} loading`} role="status">
          <Skeleton className="h-10 w-40" radius="lg" />
          <Skeleton className="h-4 w-52" />
        </div>
      ) : error ? (
        <ErrorBanner
          action={<RetryButton onRetry={onRetry} />}
          message={getErrorMessage(error)}
          title={getErrorTitle(error)}
        />
      ) : (
        <p className="font-rc-data text-4xl font-semibold tracking-tight text-rc-foreground">
          {formatCurrency(value?.totalAmount ?? 0)}
        </p>
      )}
    </Card>
  )
}

function TopProductsTable({
  error,
  isFetching,
  isLoading,
  items,
  onRetry,
}: {
  error: unknown
  isFetching: boolean
  isLoading: boolean
  items?: TopSellingProductResponse[]
  onRetry: () => void
}) {
  return (
    <Card
      actions={isFetching && !isLoading ? <Badge variant="info">Refreshing</Badge> : null}
      description="Ranked by quantity sold and gross sales from /api/reports/top-products."
      title="Top-selling products"
    >
      {isLoading ? <TableSkeleton /> : null}
      {!isLoading && error ? (
        <ErrorBanner
          action={<RetryButton onRetry={onRetry} />}
          message={getErrorMessage(error)}
          title={getErrorTitle(error)}
        />
      ) : null}
      {!isLoading && !error && (!items || items.length === 0) ? (
        <EmptyState
          description="No products have completed sales in the selected reporting data yet."
          title="No top products yet"
        />
      ) : null}
      {!isLoading && !error && items && items.length > 0 ? (
        <div className="overflow-x-auto">
          <table className="min-w-full text-left text-sm">
            <thead className="border-b border-rc-border text-xs uppercase tracking-[0.16em] text-rc-secondary">
              <tr>
                <th className="pb-3 pr-4 font-semibold" scope="col">Product</th>
                <th className="pb-3 pr-4 text-right font-semibold" scope="col">Quantity sold</th>
                <th className="pb-3 text-right font-semibold" scope="col">Gross sales</th>
              </tr>
            </thead>
            <tbody className="divide-y divide-rc-border">
              {items.map((item) => (
                <tr key={item.productId}>
                  <td className="py-3 pr-4">
                    <div className="font-medium text-rc-foreground">{item.productName}</div>
                    <div className="font-rc-data text-xs text-rc-secondary">{item.sku}</div>
                  </td>
                  <td className="py-3 pr-4 text-right font-rc-data text-rc-foreground">{formatCount(item.quantitySold)}</td>
                  <td className="py-3 text-right font-rc-data text-rc-foreground">{formatCurrency(item.grossSales)}</td>
                </tr>
              ))}
            </tbody>
          </table>
        </div>
      ) : null}
    </Card>
  )
}

function LowStockTable({
  error,
  isFetching,
  isLoading,
  items,
  onRetry,
}: {
  error: unknown
  isFetching: boolean
  isLoading: boolean
  items?: InventoryStockResponse[]
  onRetry: () => void
}) {
  return (
    <Card
      actions={isFetching && !isLoading ? <Badge variant="info">Refreshing</Badge> : null}
      description="Products at or below their configured threshold from /api/reports/low-stock."
      title="Low-stock report"
    >
      {isLoading ? <TableSkeleton /> : null}
      {!isLoading && error ? (
        <ErrorBanner action={<RetryButton onRetry={onRetry} />} message={getErrorMessage(error)} title={getErrorTitle(error)} />
      ) : null}
      {!isLoading && !error && (!items || items.length === 0) ? (
        <EmptyState description="No products are currently low on stock." title="No low-stock items" />
      ) : null}
      {!isLoading && !error && items && items.length > 0 ? (
        <div className="space-y-3">
          {items.map((item) => (
            <div className="rounded-xl border border-rc-warning/25 bg-rc-warning-muted/55 p-3" key={item.productId}>
              <div className="flex flex-col gap-3 sm:flex-row sm:items-center sm:justify-between">
                <div className="min-w-0">
                  <div className="flex flex-wrap items-center gap-2">
                    <p className="font-medium text-rc-foreground">{item.productName}</p>
                    <Badge variant="warning">Low stock</Badge>
                  </div>
                  <p className="font-rc-data text-xs text-rc-secondary">{item.sku}</p>
                  <p className="text-xs text-rc-secondary">Updated {formatDateTime(item.updatedAt)}</p>
                </div>
                <div className="grid grid-cols-2 gap-4 text-sm sm:min-w-56">
                  <div>
                    <p className="text-xs uppercase tracking-[0.16em] text-rc-secondary">On hand</p>
                    <p className="font-rc-data text-lg font-semibold text-rc-warning-strong">{formatCount(item.quantity)}</p>
                  </div>
                  <div>
                    <p className="text-xs uppercase tracking-[0.16em] text-rc-secondary">Threshold</p>
                    <p className="font-rc-data text-lg font-semibold text-rc-foreground">{formatCount(item.lowStockThreshold)}</p>
                  </div>
                </div>
              </div>
            </div>
          ))}
        </div>
      ) : null}
    </Card>
  )
}

function CashierSalesTable({
  error,
  isFetching,
  isLoading,
  items,
  onRetry,
}: {
  error: unknown
  isFetching: boolean
  isLoading: boolean
  items?: CashierSalesReportResponse[]
  onRetry: () => void
}) {
  return (
    <Card
      actions={isFetching && !isLoading ? <Badge variant="info">Refreshing</Badge> : null}
      description="Sales totals grouped by cashier from /api/reports/sales-by-cashier."
      title="Sales by cashier"
    >
      {isLoading ? <TableSkeleton /> : null}
      {!isLoading && error ? (
        <ErrorBanner action={<RetryButton onRetry={onRetry} />} message={getErrorMessage(error)} title={getErrorTitle(error)} />
      ) : null}
      {!isLoading && !error && (!items || items.length === 0) ? (
        <EmptyState description="No cashier sales totals are available yet." title="No cashier sales" />
      ) : null}
      {!isLoading && !error && items && items.length > 0 ? (
        <div className="overflow-x-auto">
          <table className="min-w-full text-left text-sm">
            <thead className="border-b border-rc-border text-xs uppercase tracking-[0.16em] text-rc-secondary">
              <tr>
                <th className="pb-3 pr-4 font-semibold" scope="col">Cashier</th>
                <th className="pb-3 pr-4 text-right font-semibold" scope="col">Sales</th>
                <th className="pb-3 text-right font-semibold" scope="col">Total</th>
              </tr>
            </thead>
            <tbody className="divide-y divide-rc-border">
              {items.map((item) => (
                <tr key={item.cashierId}>
                  <td className="py-3 pr-4">
                    <div className="font-medium text-rc-foreground">{item.cashierName}</div>
                    <div className="text-xs text-rc-secondary">{item.cashierEmail}</div>
                  </td>
                  <td className="py-3 pr-4 text-right font-rc-data text-rc-foreground">{formatCount(item.saleCount)}</td>
                  <td className="py-3 text-right font-rc-data text-rc-foreground">{formatCurrency(item.totalAmount)}</td>
                </tr>
              ))}
            </tbody>
          </table>
        </div>
      ) : null}
    </Card>
  )
}

function PaymentSummaryTable({
  error,
  isFetching,
  isLoading,
  items,
  onRetry,
}: {
  error: unknown
  isFetching: boolean
  isLoading: boolean
  items?: PaymentMethodSummaryResponse[]
  onRetry: () => void
}) {
  return (
    <Card
      actions={isFetching && !isLoading ? <Badge variant="info">Refreshing</Badge> : null}
      description="Payment counts and total captured amounts from /api/reports/payment-summary."
      title="Payment method summary"
    >
      {isLoading ? <TableSkeleton rows={2} /> : null}
      {!isLoading && error ? (
        <ErrorBanner action={<RetryButton onRetry={onRetry} />} message={getErrorMessage(error)} title={getErrorTitle(error)} />
      ) : null}
      {!isLoading && !error && (!items || items.length === 0) ? (
        <EmptyState description="No payments have been captured yet." title="No payment data" />
      ) : null}
      {!isLoading && !error && items && items.length > 0 ? (
        <div className="grid gap-3 sm:grid-cols-2">
          {items.map((item) => (
            <div className="rounded-xl border border-rc-border bg-rc-background p-4" key={item.method}>
              <div className="flex items-center justify-between gap-3">
                <Badge variant="info">{item.method}</Badge>
                <span className="font-rc-data text-sm text-rc-secondary">{formatCount(item.paymentCount)} payments</span>
              </div>
              <p className="mt-4 font-rc-data text-2xl font-semibold text-rc-foreground">{formatCurrency(item.totalAmount)}</p>
            </div>
          ))}
        </div>
      ) : null}
    </Card>
  )
}

export function ReportsPage() {
  const today = useMemo(() => new Date(), [])
  const currentMonth = useMemo(() => getCurrentYearMonth(today), [today])
  const [dailyDate, setDailyDate] = useState(() => toLocalDateInputValue(today))
  const [month, setMonth] = useState(() => currentMonth.month)
  const [year, setYear] = useState(() => currentMonth.year)

  const dailySalesQuery = useDailySalesQuery(dailyDate)
  const monthlySalesQuery = useMonthlySalesQuery(year, month)
  const topProductsQuery = useTopProductsQuery()
  const lowStockQuery = useLowStockReportQuery()
  const cashierSalesQuery = useSalesByCashierQuery()
  const paymentSummaryQuery = usePaymentSummaryQuery()

  const isRefreshing = [
    dailySalesQuery,
    monthlySalesQuery,
    topProductsQuery,
    lowStockQuery,
    cashierSalesQuery,
    paymentSummaryQuery,
  ].some((query) => query.isFetching && !query.isLoading)

  return (
    <div className="space-y-5">
      <PageHeader
        description="Manager and admin reports loaded from backend reporting endpoints. Tables stay readable on small screens and provide the chart alternative by default."
        eyebrow="FE-11 reports"
        meta={
          <>
            <Badge variant="success">ADMIN / MANAGER</Badge>
            <Badge variant="info">6 report endpoints</Badge>
            {isRefreshing ? <Badge variant="info">Refreshing</Badge> : null}
          </>
        }
        title="Reports"
      />

      <div className="grid gap-5 xl:grid-cols-2">
        <Card description="Choose a calendar date. The request sends date=YYYY-MM-DD." title="Daily sales total">
          <div className="space-y-4">
            <Input label="Report date" max="9999-12-31" onChange={(event) => setDailyDate(event.target.value)} type="date" value={dailyDate} />
            <TotalCard
              error={dailySalesQuery.error}
              isFetching={dailySalesQuery.isFetching}
              isLoading={dailySalesQuery.isLoading}
              label="Daily total"
              onRetry={() => void dailySalesQuery.refetch()}
              periodLabel={dailyDate}
              value={dailySalesQuery.data}
            />
          </div>
        </Card>

        <Card description="Choose a valid month and year. The request sends year and month query params." title="Monthly sales total">
          <div className="space-y-4">
            <div className="grid gap-4 sm:grid-cols-[1fr_8rem]">
              <Select label="Report month" onChange={(event) => setMonth(Number(event.target.value))} value={month}>
                {monthOptions.map((option) => (
                  <option key={option.value} value={option.value}>{option.label}</option>
                ))}
              </Select>
              <Input label="Year" min={2000} onChange={(event) => setYear(Number(event.target.value))} type="number" value={year} />
            </div>
            <TotalCard
              error={monthlySalesQuery.error}
              isFetching={monthlySalesQuery.isFetching}
              isLoading={monthlySalesQuery.isLoading}
              label="Monthly total"
              onRetry={() => void monthlySalesQuery.refetch()}
              periodLabel={formatMonth(new Date(year, month - 1, 1))}
              value={monthlySalesQuery.data}
            />
          </div>
        </Card>
      </div>

      <div className="grid gap-5 xl:grid-cols-[1.15fr_0.85fr]">
        <TopProductsTable
          error={topProductsQuery.error}
          isFetching={topProductsQuery.isFetching}
          isLoading={topProductsQuery.isLoading}
          items={topProductsQuery.data}
          onRetry={() => void topProductsQuery.refetch()}
        />
        <LowStockTable
          error={lowStockQuery.error}
          isFetching={lowStockQuery.isFetching}
          isLoading={lowStockQuery.isLoading}
          items={lowStockQuery.data}
          onRetry={() => void lowStockQuery.refetch()}
        />
      </div>

      <div className="grid gap-5 xl:grid-cols-2">
        <CashierSalesTable
          error={cashierSalesQuery.error}
          isFetching={cashierSalesQuery.isFetching}
          isLoading={cashierSalesQuery.isLoading}
          items={cashierSalesQuery.data}
          onRetry={() => void cashierSalesQuery.refetch()}
        />
        <PaymentSummaryTable
          error={paymentSummaryQuery.error}
          isFetching={paymentSummaryQuery.isFetching}
          isLoading={paymentSummaryQuery.isLoading}
          items={paymentSummaryQuery.data}
          onRetry={() => void paymentSummaryQuery.refetch()}
        />
      </div>
    </div>
  )
}
