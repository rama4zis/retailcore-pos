import type { InventoryStockResponse } from '../../../lib/api/inventory'
import { Badge } from '../../../components/ui/Badge'
import { Button } from '../../../components/ui/Button'
import { ErrorBanner } from '../../../components/feedback/ErrorBanner'
import { Spinner } from '../../../components/feedback/Spinner'
import { EmptyState } from '../../../components/feedback/EmptyState'
import { getApiErrorMessage } from '../../../lib/api/errors'
import { formatDateTime } from '../../../lib/format/date'
import { useStockMovements } from '../queries'

interface StockMovementsModalProps {
  onClose: () => void
  stock: InventoryStockResponse
}

const movementTypeLabels: Record<string, string> = {
  ADJUSTMENT: 'Adjustment',
  REFUND: 'Refund',
  SALE: 'Sale',
}

const movementTypeColors: Record<string, string> = {
  ADJUSTMENT: 'bg-blue-100 text-blue-800',
  REFUND: 'bg-emerald-100 text-emerald-800',
  SALE: 'bg-purple-100 text-purple-800',
}

export function StockMovementsModal({ onClose, stock }: StockMovementsModalProps) {
  const movementsQuery = useStockMovements(stock.productId)
  const errorMessage = movementsQuery.error ? getApiErrorMessage(movementsQuery.error) : null

  return (
    <div className="fixed inset-0 z-50 flex items-center justify-center bg-slate-900/50 p-4">
      <div className="flex max-h-[90vh] w-full max-w-4xl flex-col rounded-lg bg-white shadow-xl">
        <div className="border-b border-slate-200 p-6">
          <h2 className="mb-2 text-xl font-semibold text-slate-900">Stock Movement History</h2>
          <div className="text-sm text-slate-700">{stock.productName}</div>
          <div className="text-xs text-slate-600">SKU: {stock.sku}</div>
        </div>

        <div className="flex-1 overflow-y-auto p-6">
          {movementsQuery.isLoading && (
            <div className="flex justify-center py-12">
              <Spinner size="lg" />
            </div>
          )}

          {errorMessage && <ErrorBanner message={errorMessage} />}

          {movementsQuery.isSuccess && movementsQuery.data.length === 0 && (
            <EmptyState
              description="No stock movements have been recorded yet."
              title="No Movement History"
            />
          )}

          {movementsQuery.isSuccess && movementsQuery.data.length > 0 && (
            <div className="overflow-x-auto">
              <table className="w-full">
                <thead>
                  <tr className="border-b border-slate-200 text-left text-xs font-medium uppercase tracking-wide text-slate-600">
                    <th className="pb-3">Date</th>
                    <th className="pb-3">Type</th>
                    <th className="pb-3 text-right">Change</th>
                    <th className="pb-3 text-right">Stock After</th>
                    <th className="pb-3">Reason</th>
                  </tr>
                </thead>
                <tbody className="divide-y divide-slate-100">
                  {movementsQuery.data.map((movement) => (
                    <tr key={movement.id} className="text-sm">
                      <td className="py-3 text-slate-700">
                        {formatDateTime(movement.createdAt)}
                      </td>
                      <td className="py-3">
                        <Badge
                          className={movementTypeColors[movement.movementType] || 'bg-slate-100 text-slate-800'}
                        >
                          {movementTypeLabels[movement.movementType] || movement.movementType}
                        </Badge>
                      </td>
                      <td
                        className={`py-3 text-right font-medium ${
                          movement.quantityChange > 0 ? 'text-emerald-700' : 'text-red-700'
                        }`}
                      >
                        {movement.quantityChange > 0 ? '+' : ''}
                        {movement.quantityChange}
                      </td>
                      <td className="py-3 text-right font-semibold text-slate-900">
                        {movement.stockAfter}
                      </td>
                      <td className="py-3 text-slate-600">
                        {movement.reason || '—'}
                      </td>
                    </tr>
                  ))}
                </tbody>
              </table>
            </div>
          )}
        </div>

        <div className="border-t border-slate-200 p-6">
          <div className="flex justify-end">
            <Button variant="secondary" onClick={onClose}>
              Close
            </Button>
          </div>
        </div>
      </div>
    </div>
  )
}
