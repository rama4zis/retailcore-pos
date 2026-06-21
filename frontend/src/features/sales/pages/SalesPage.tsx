import { useMemo, useState, type FormEvent } from 'react'

import { EmptyState } from '../../../components/feedback/EmptyState'
import { ErrorBanner } from '../../../components/feedback/ErrorBanner'
import { Spinner } from '../../../components/feedback/Spinner'
import { Badge } from '../../../components/ui/Badge'
import { Button } from '../../../components/ui/Button'
import { Card } from '../../../components/ui/Card'
import { Input } from '../../../components/ui/Input'
import { Textarea } from '../../../components/ui/Textarea'
import { getApiErrorMessage } from '../../../lib/api/errors'
import type { RefundRequest, SaleItemResponse, SaleResponse } from '../../../lib/api/sales'
import { formatCurrency } from '../../../lib/format/currency'
import { formatDateTime } from '../../../lib/format/date'
import { useRefundMutation, useSaleQuery, useSalesQuery } from '../queries'

type RefundQuantities = Record<number, number>

function getStatusVariant(status: SaleResponse['status']) {
  if (status === 'REFUNDED') return 'danger'
  if (status === 'PARTIALLY_REFUNDED') return 'warning'
  return 'success'
}

function getLineRefundQuantity(quantities: RefundQuantities, productId: number) {
  return quantities[productId] ?? 0
}

function buildRefundRequest(quantities: RefundQuantities, reason: string): RefundRequest {
  return {
    items: Object.entries(quantities)
      .map(([productId, quantity]) => ({ productId: Number(productId), quantity }))
      .filter((item) => item.quantity > 0),
    reason: reason.trim() || null,
  }
}

function SaleSummaryRow({
  isSelected,
  onSelect,
  sale,
}: {
  isSelected: boolean
  onSelect: () => void
  sale: SaleResponse
}) {
  return (
    <button
      className={`w-full rounded-xl border p-4 text-left transition-colors focus-visible:outline-2 focus-visible:outline-offset-2 focus-visible:outline-rc-ring ${
        isSelected
          ? 'border-rc-accent bg-rc-accent-muted/40'
          : 'border-rc-border bg-rc-surface hover:border-rc-primary/40 hover:bg-rc-muted/40'
      }`}
      onClick={onSelect}
      type="button"
    >
      <div className="flex flex-wrap items-start justify-between gap-3">
        <div>
          <p className="font-semibold text-rc-foreground">Sale #{sale.saleNumber}</p>
          <p className="mt-1 text-sm text-rc-secondary">
            {formatDateTime(sale.completedAt)} by {sale.cashierName}
          </p>
        </div>
        <div className="text-right">
          <p className="font-semibold tabular-nums text-rc-foreground">
            {formatCurrency(sale.totalAmount)}
          </p>
          <Badge className="mt-2" variant={getStatusVariant(sale.status)}>
            {sale.status.replace('_', ' ')}
          </Badge>
        </div>
      </div>
      <p className="mt-3 text-sm text-rc-secondary">
        {sale.items.length} item{sale.items.length === 1 ? '' : 's'}
      </p>
    </button>
  )
}

function SaleItemsTable({ items }: { items: SaleItemResponse[] }) {
  return (
    <div className="overflow-x-auto">
      <table className="min-w-full divide-y divide-rc-border text-sm">
        <thead className="bg-rc-muted/60 text-left text-xs font-semibold uppercase tracking-wide text-rc-secondary">
          <tr>
            <th className="px-3 py-2">Item</th>
            <th className="px-3 py-2 text-right">Qty</th>
            <th className="px-3 py-2 text-right">Unit</th>
            <th className="px-3 py-2 text-right">Line total</th>
          </tr>
        </thead>
        <tbody className="divide-y divide-rc-border">
          {items.map((item) => (
            <tr key={item.id}>
              <td className="px-3 py-3">
                <p className="font-medium text-rc-foreground">{item.productName}</p>
                <p className="text-xs text-rc-secondary">{item.sku}</p>
              </td>
              <td className="px-3 py-3 text-right tabular-nums">{item.quantity}</td>
              <td className="px-3 py-3 text-right tabular-nums">
                {formatCurrency(item.unitPrice)}
              </td>
              <td className="px-3 py-3 text-right font-medium tabular-nums">
                {formatCurrency(item.lineTotal)}
              </td>
            </tr>
          ))}
        </tbody>
      </table>
    </div>
  )
}

function RefundForm({ sale }: { sale: SaleResponse }) {
  const [quantities, setQuantities] = useState<RefundQuantities>({})
  const [reason, setReason] = useState('')
  const refundMutation = useRefundMutation(sale.id)
  const selectedItems = buildRefundRequest(quantities, reason).items
  const canRefund = sale.status !== 'REFUNDED'
  const submitDisabled = !canRefund || selectedItems.length === 0 || refundMutation.isPending

  function updateQuantity(item: SaleItemResponse, value: string) {
    const nextQuantity = Math.min(item.quantity, Math.max(0, Number(value) || 0))
    setQuantities((current) => ({ ...current, [item.productId]: nextQuantity }))
  }

  function handleSubmit(event: FormEvent<HTMLFormElement>) {
    event.preventDefault()
    const request = buildRefundRequest(quantities, reason)
    if (request.items.length === 0) return
    refundMutation.mutate(request, {
      onSuccess: () => {
        setQuantities({})
        setReason('')
      },
    })
  }

  return (
    <form className="space-y-4" onSubmit={handleSubmit}>
      {refundMutation.isError ? (
        <ErrorBanner
          message={getApiErrorMessage(refundMutation.error, 'Refund failed.')}
          title="Refund could not be completed"
        />
      ) : null}

      {!canRefund ? (
        <ErrorBanner
          message="This sale is already fully refunded. No further refund can be submitted."
          title="No refundable quantity remains"
        />
      ) : null}

      <div className="space-y-3">
        {sale.items.map((item) => (
          <div
            className="grid gap-3 rounded-xl border border-rc-border bg-rc-muted/30 p-3 sm:grid-cols-[1fr_8rem] sm:items-center"
            key={item.id}
          >
            <div>
              <p className="font-medium text-rc-foreground">{item.productName}</p>
              <p className="text-sm text-rc-secondary">
                Sold quantity: {item.quantity} - {formatCurrency(item.unitPrice)} each
              </p>
            </div>
            <Input
              disabled={!canRefund || refundMutation.isPending}
              label={`Refund quantity for ${item.productName}`}
              max={item.quantity}
              min={0}
              onChange={(event) => updateQuantity(item, event.target.value)}
              type="number"
              value={getLineRefundQuantity(quantities, item.productId)}
            />
          </div>
        ))}
      </div>

      <Textarea
        disabled={!canRefund || refundMutation.isPending}
        helperText="Optional. Backend accepts up to 500 characters."
        label="Refund reason"
        maxLength={500}
        onChange={(event) => setReason(event.target.value)}
        value={reason}
      />

      <div className="flex flex-col gap-3 sm:flex-row sm:items-center sm:justify-between">
        <p className="text-sm text-rc-secondary">
          Selected refund lines: <span className="font-semibold">{selectedItems.length}</span>
        </p>
        <Button disabled={submitDisabled} isLoading={refundMutation.isPending} type="submit" variant="danger">
          Submit refund
        </Button>
      </div>
    </form>
  )
}

function SaleDetailPanel({ saleId }: { saleId: number | null }) {
  const saleQuery = useSaleQuery(saleId)

  if (saleId === null) {
    return (
      <EmptyState
        description="Select a sale from the history list to review line items and submit eligible refunds."
        title="Select a sale"
      />
    )
  }

  if (saleQuery.isLoading) {
    return <Spinner label="Loading sale detail" />
  }

  if (saleQuery.isError) {
    return (
      <ErrorBanner
        action={<Button onClick={() => void saleQuery.refetch()} variant="secondary">Retry</Button>}
        message={getApiErrorMessage(saleQuery.error, 'Unable to load sale detail.')}
        title="Sale detail unavailable"
      />
    )
  }

  const sale = saleQuery.data

  if (!sale) return null

  return (
    <div className="space-y-4">
      <Card
        description={`${formatDateTime(sale.completedAt)} by ${sale.cashierName}`}
        title={`Sale #${sale.saleNumber}`}
        actions={<Badge variant={getStatusVariant(sale.status)}>{sale.status.replace('_', ' ')}</Badge>}
      >
        <div className="grid gap-3 text-sm sm:grid-cols-3">
          <div className="rounded-xl bg-rc-muted/50 p-3">
            <p className="text-rc-secondary">Total</p>
            <p className="mt-1 text-lg font-semibold tabular-nums">{formatCurrency(sale.totalAmount)}</p>
          </div>
          <div className="rounded-xl bg-rc-muted/50 p-3">
            <p className="text-rc-secondary">Cashier</p>
            <p className="mt-1 font-semibold">{sale.cashierName}</p>
          </div>
          <div className="rounded-xl bg-rc-muted/50 p-3">
            <p className="text-rc-secondary">Items</p>
            <p className="mt-1 font-semibold tabular-nums">{sale.items.length}</p>
          </div>
        </div>
      </Card>

      <Card title="Sale Items" description="Original sold quantities from the backend sale record.">
        <SaleItemsTable items={sale.items} />
      </Card>

      <Card title="Refund Items" description="Enter quantities to return. Backend prevents over-refunds and conflicts.">
        <RefundForm sale={sale} />
      </Card>
    </div>
  )
}

export function SalesPage() {
  const salesQuery = useSalesQuery()
  const [selectedSaleId, setSelectedSaleId] = useState<number | null>(null)
  const sales = useMemo(() => salesQuery.data ?? [], [salesQuery.data])
  const selectedId = selectedSaleId ?? sales[0]?.id ?? null

  const totals = useMemo(() => {
    const gross = sales.reduce((sum, sale) => sum + sale.totalAmount, 0)
    return { count: sales.length, gross }
  }, [sales])

  return (
    <div className="space-y-6">
      <div className="flex flex-col gap-3 sm:flex-row sm:items-end sm:justify-between">
        <div>
          <p className="text-sm font-semibold uppercase tracking-wide text-rc-accent">FE-10</p>
          <h1 className="text-2xl font-semibold tracking-tight text-rc-foreground">Sales History</h1>
          <p className="mt-1 max-w-2xl text-sm leading-6 text-rc-secondary">
            Review completed sales, inspect sale detail, and refund eligible item quantities.
          </p>
        </div>
        <div className="rounded-xl border border-rc-border bg-rc-surface px-4 py-3 text-sm shadow-sm">
          <span className="text-rc-secondary">Loaded sales</span>{' '}
          <span className="font-semibold tabular-nums text-rc-foreground">{totals.count}</span>{' '}
          <span className="text-rc-secondary">- Gross</span>{' '}
          <span className="font-semibold tabular-nums text-rc-foreground">{formatCurrency(totals.gross)}</span>
        </div>
      </div>

      {salesQuery.isError ? (
        <ErrorBanner
          action={<Button onClick={() => void salesQuery.refetch()} variant="secondary">Retry</Button>}
          message={getApiErrorMessage(salesQuery.error, 'Unable to load sales history.')}
          title="Sales history unavailable"
        />
      ) : null}

      {salesQuery.isLoading ? <Spinner label="Loading sales history" /> : null}

      {!salesQuery.isLoading && !salesQuery.isError && sales.length === 0 ? (
        <EmptyState
          description="Sales created through checkout will appear here for receipt review and refund handling."
          title="No sales yet"
        />
      ) : null}

      {sales.length > 0 ? (
        <div className="grid gap-6 xl:grid-cols-[minmax(20rem,24rem)_1fr]">
          <Card
            description="Most recent sales from GET /api/sales. Select one to load backend detail."
            title="History"
          >
            <div className="space-y-3">
              {sales.map((sale) => (
                <SaleSummaryRow
                  isSelected={sale.id === selectedId}
                  key={sale.id}
                  onSelect={() => setSelectedSaleId(sale.id)}
                  sale={sale}
                />
              ))}
            </div>
          </Card>

          <SaleDetailPanel saleId={selectedId} />
        </div>
      ) : null}
    </div>
  )
}
