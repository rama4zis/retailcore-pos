import { useState } from 'react'

import type { InventoryStockResponse } from '../../../lib/api/inventory'
import { PageHeader } from '../../../components/layout/PageHeader'
import { Badge } from '../../../components/ui/Badge'
import { Button } from '../../../components/ui/Button'
import { Card } from '../../../components/ui/Card'
import { ErrorBanner } from '../../../components/feedback/ErrorBanner'
import { EmptyState } from '../../../components/feedback/EmptyState'
import { Spinner } from '../../../components/feedback/Spinner'
import { getApiErrorMessage } from '../../../lib/api/errors'
import { formatDateTime } from '../../../lib/format/date'
import { StockAdjustmentModal } from '../components/StockAdjustmentModal'
import { StockMovementsModal } from '../components/StockMovementsModal'
import { useInventoryList, useLowStockInventory } from '../queries'

type ViewMode = 'all' | 'low-stock'

export function InventoryPage() {
  const [viewMode, setViewMode] = useState<ViewMode>('all')
  const [selectedStock, setSelectedStock] = useState<InventoryStockResponse | null>(null)
  const [showAdjustModal, setShowAdjustModal] = useState(false)
  const [showMovementsModal, setShowMovementsModal] = useState(false)

  const inventoryQuery = useInventoryList()
  const lowStockQuery = useLowStockInventory()

  const activeQuery = viewMode === 'all' ? inventoryQuery : lowStockQuery
  const errorMessage = activeQuery.error ? getApiErrorMessage(activeQuery.error) : null

  const handleAdjustClick = (stock: InventoryStockResponse) => {
    setSelectedStock(stock)
    setShowAdjustModal(true)
  }

  const handleHistoryClick = (stock: InventoryStockResponse) => {
    setSelectedStock(stock)
    setShowMovementsModal(true)
  }

  const lowStockCount = inventoryQuery.data?.filter((s) => s.lowStock).length || 0

  return (
    <div>
      <PageHeader title="Inventory Management">
        <div className="flex gap-2">
          <Button
            size="sm"
            variant={viewMode === 'all' ? 'primary' : 'secondary'}
            onClick={() => setViewMode('all')}
          >
            All Stock
          </Button>
          <Button
            size="sm"
            variant={viewMode === 'low-stock' ? 'primary' : 'secondary'}
            onClick={() => setViewMode('low-stock')}
          >
            Low Stock {lowStockCount > 0 && `(${lowStockCount})`}
          </Button>
        </div>
      </PageHeader>

      <div className="space-y-6">
        {activeQuery.isLoading && (
          <Card>
            <div className="flex justify-center py-12">
              <Spinner size="lg" />
            </div>
          </Card>
        )}

        {errorMessage && (
          <Card>
            <ErrorBanner message={errorMessage} />
          </Card>
        )}

        {activeQuery.isSuccess && activeQuery.data.length === 0 && (
          <Card>
            <EmptyState
              description={
                viewMode === 'low-stock'
                  ? 'All products are above their low-stock thresholds.'
                  : 'No inventory records found. Products will appear here after you add them.'
              }
              title={viewMode === 'low-stock' ? 'No Low Stock Items' : 'No Inventory'}
            />
          </Card>
        )}

        {activeQuery.isSuccess && activeQuery.data.length > 0 && (
          <Card>
            <div className="overflow-x-auto">
              <table className="w-full">
                <thead>
                  <tr className="border-b border-slate-200 text-left text-xs font-medium uppercase tracking-wide text-slate-600">
                    <th className="pb-3 pr-4">Product</th>
                    <th className="pb-3 pr-4">SKU</th>
                    <th className="pb-3 pr-4 text-right">Stock</th>
                    <th className="pb-3 pr-4 text-right">Threshold</th>
                    <th className="pb-3 pr-4">Status</th>
                    <th className="pb-3 pr-4">Updated</th>
                    <th className="pb-3 text-right">Actions</th>
                  </tr>
                </thead>
                <tbody className="divide-y divide-slate-100">
                  {activeQuery.data.map((stock) => (
                    <tr key={stock.productId} className="text-sm">
                      <td className="py-3 pr-4 font-medium text-slate-900">
                        {stock.productName}
                      </td>
                      <td className="py-3 pr-4 font-mono text-slate-600">{stock.sku}</td>
                      <td
                        className={`py-3 pr-4 text-right text-base font-semibold ${
                          stock.lowStock ? 'text-amber-700' : 'text-slate-900'
                        }`}
                      >
                        {stock.quantity}
                      </td>
                      <td className="py-3 pr-4 text-right text-slate-600">
                        {stock.lowStockThreshold}
                      </td>
                      <td className="py-3 pr-4">
                        {stock.lowStock ? (
                          <Badge className="bg-amber-100 text-amber-800">Low Stock</Badge>
                        ) : (
                          <Badge className="bg-emerald-100 text-emerald-800">In Stock</Badge>
                        )}
                      </td>
                      <td className="py-3 pr-4 text-slate-600">
                        {formatDateTime(stock.updatedAt)}
                      </td>
                      <td className="py-3 text-right">
                        <div className="flex justify-end gap-2">
                          <Button
                            size="sm"
                            variant="secondary"
                            onClick={() => handleHistoryClick(stock)}
                          >
                            History
                          </Button>
                          <Button size="sm" onClick={() => handleAdjustClick(stock)}>
                            Adjust
                          </Button>
                        </div>
                      </td>
                    </tr>
                  ))}
                </tbody>
              </table>
            </div>
          </Card>
        )}
      </div>

      {showAdjustModal && selectedStock && (
        <StockAdjustmentModal
          stock={selectedStock}
          onClose={() => {
            setShowAdjustModal(false)
            setSelectedStock(null)
          }}
        />
      )}

      {showMovementsModal && selectedStock && (
        <StockMovementsModal
          stock={selectedStock}
          onClose={() => {
            setShowMovementsModal(false)
            setSelectedStock(null)
          }}
        />
      )}
    </div>
  )
}
