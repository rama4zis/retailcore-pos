import { useMemo, useState } from 'react'

import type { InventoryStockResponse } from '../../../lib/api/inventory'
import type { ProductResponse } from '../../../lib/api/products'
import { PageHeader } from '../../../components/layout/PageHeader'
import { Badge } from '../../../components/ui/Badge'
import { Button } from '../../../components/ui/Button'
import { Card } from '../../../components/ui/Card'
import { ErrorBanner } from '../../../components/feedback/ErrorBanner'
import { EmptyState } from '../../../components/feedback/EmptyState'
import { Spinner } from '../../../components/feedback/Spinner'
import { getApiErrorMessage } from '../../../lib/api/errors'
import { formatDateTime } from '../../../lib/format/date'
import { useProductsQuery } from '../../../features/products/queries'
import { StockAdjustmentModal } from '../components/StockAdjustmentModal'
import { StockMovementsModal } from '../components/StockMovementsModal'
import { useInventoryList, useLowStockInventory } from '../queries'

type ViewMode = 'all' | 'low-stock'

/* ------------------------------------------------------------------ */
/*  Merge products + inventory data into display rows                  */
/* ------------------------------------------------------------------ */

interface InventoryDisplayRow {
  productId: number
  sku: string
  productName: string
  quantity: number
  lowStockThreshold: number
  lowStock: boolean
  hasRecord: boolean
  updatedAt: string
}

function buildAllStockRows(
  products: ProductResponse[],
  inventory: InventoryStockResponse[],
): InventoryDisplayRow[] {
  const invByProductId = new Map<number, InventoryStockResponse>()
  for (const inv of inventory) {
    invByProductId.set(inv.productId, inv)
  }

  return products
    .filter((p) => p.active)
    .map((p) => {
      const inv = invByProductId.get(p.id)
      if (inv) {
        return {
          productId: inv.productId,
          sku: inv.sku,
          productName: inv.productName,
          quantity: inv.quantity,
          lowStockThreshold: inv.lowStockThreshold,
          lowStock: inv.lowStock,
          hasRecord: true,
          updatedAt: inv.updatedAt,
        }
      }
      return {
        productId: p.id,
        sku: p.sku,
        productName: p.name,
        quantity: 0,
        lowStockThreshold: 10,
        lowStock: true,
        hasRecord: false,
        updatedAt: '',
      }
    })
}

function stockToInventoryResponse(row: InventoryDisplayRow): InventoryStockResponse {
  return {
    productId: row.productId,
    sku: row.sku,
    productName: row.productName,
    quantity: row.quantity,
    lowStockThreshold: row.lowStockThreshold,
    lowStock: row.lowStock,
    createdAt: '',
    updatedAt: row.updatedAt,
  }
}

/* ------------------------------------------------------------------ */
/*  Inventory page                                                     */
/* ------------------------------------------------------------------ */

export function InventoryPage() {
  const [viewMode, setViewMode] = useState<ViewMode>('all')
  const [selectedRow, setSelectedRow] = useState<InventoryDisplayRow | null>(null)
  const [showAdjustModal, setShowAdjustModal] = useState(false)
  const [showMovementsModal, setShowMovementsModal] = useState(false)

  const inventoryQuery = useInventoryList()
  const lowStockQuery = useLowStockInventory()
  const productsQuery = useProductsQuery()

  const rawError = viewMode === 'all'
    ? (inventoryQuery.error ?? productsQuery.error)
    : lowStockQuery.error
  const errorMessage = rawError ? getApiErrorMessage(rawError) : null

  const lowStockCount = useMemo(
    () => inventoryQuery.data?.filter((s) => s.lowStock).length ?? 0,
    [inventoryQuery.data],
  )

  /* Merge active products with inventory data for "All Stock" view */
  const allStockRows = useMemo(() => {
    if (!inventoryQuery.data || !productsQuery.data) return null
    return buildAllStockRows(productsQuery.data, inventoryQuery.data)
  }, [inventoryQuery.data, productsQuery.data])

  const isLoading =
    viewMode === 'all'
      ? inventoryQuery.isLoading || productsQuery.isLoading
      : lowStockQuery.isLoading

  const handleAdjustClick = (row: InventoryDisplayRow) => {
    setSelectedRow(row)
    setShowAdjustModal(true)
  }

  const handleHistoryClick = (row: InventoryDisplayRow) => {
    setSelectedRow(row)
    setShowMovementsModal(true)
  }

  const handleCloseModal = () => {
    setShowAdjustModal(false)
    setShowMovementsModal(false)
    setSelectedRow(null)
  }

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
        {isLoading && (
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

        {/* ---- All Stock: merged product + inventory table ---- */}
        {viewMode === 'all' &&
          !isLoading &&
          !errorMessage &&
          allStockRows &&
          allStockRows.length > 0 && <StockTable rows={allStockRows} onAdjust={handleAdjustClick} onHistory={handleHistoryClick} />}

        {viewMode === 'all' &&
          !isLoading &&
          !errorMessage &&
          allStockRows &&
          allStockRows.length === 0 && (
            <Card>
              <EmptyState
                title="No Products"
                description="No active products found. Create products in the Products page first."
              />
            </Card>
          )}

        {/* ---- Low Stock: from API ---- */}
        {viewMode === 'low-stock' && !isLoading && !errorMessage && lowStockQuery.data && lowStockQuery.data.length > 0 && (
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
                  {lowStockQuery.data.map((stock) => (
                    <tr key={stock.productId} className="text-sm">
                      <td className="py-3 pr-4 font-medium text-slate-900">{stock.productName}</td>
                      <td className="py-3 pr-4 font-mono text-slate-600">{stock.sku}</td>
                      <td className="py-3 pr-4 text-right text-base font-semibold text-amber-700">
                        {stock.quantity}
                      </td>
                      <td className="py-3 pr-4 text-right text-slate-600">{stock.lowStockThreshold}</td>
                      <td className="py-3 pr-4">
                        <Badge className="bg-amber-100 text-amber-800">Low Stock</Badge>
                      </td>
                      <td className="py-3 pr-4 text-slate-600">{formatDateTime(stock.updatedAt)}</td>
                      <td className="py-3 text-right">
                        <div className="flex justify-end gap-2">
                          <Button
                            size="sm"
                            variant="secondary"
                            onClick={() => {
                              setSelectedRow({
                                productId: stock.productId,
                                sku: stock.sku,
                                productName: stock.productName,
                                quantity: stock.quantity,
                                lowStockThreshold: stock.lowStockThreshold,
                                lowStock: stock.lowStock,
                                hasRecord: true,
                                updatedAt: stock.updatedAt,
                              })
                              setShowMovementsModal(true)
                            }}
                          >
                            History
                          </Button>
                          <Button
                            size="sm"
                            onClick={() => {
                              setSelectedRow({
                                productId: stock.productId,
                                sku: stock.sku,
                                productName: stock.productName,
                                quantity: stock.quantity,
                                lowStockThreshold: stock.lowStockThreshold,
                                lowStock: stock.lowStock,
                                hasRecord: true,
                                updatedAt: stock.updatedAt,
                              })
                              setShowAdjustModal(true)
                            }}
                          >
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

        {viewMode === 'low-stock' &&
          !isLoading &&
          !errorMessage &&
          lowStockQuery.data &&
          lowStockQuery.data.length === 0 && (
            <Card>
              <EmptyState
                title="No Low Stock Items"
                description="All products are above their low-stock thresholds."
              />
            </Card>
          )}
      </div>

      {showAdjustModal && selectedRow && (
        <StockAdjustmentModal
          stock={stockToInventoryResponse(selectedRow)}
          onClose={handleCloseModal}
        />
      )}

      {showMovementsModal && selectedRow && (
        <StockMovementsModal
          stock={stockToInventoryResponse(selectedRow)}
          onClose={handleCloseModal}
        />
      )}
    </div>
  )
}

/* ------------------------------------------------------------------ */
/*  Stock table sub-component                                          */
/* ------------------------------------------------------------------ */

interface StockTableProps {
  rows: InventoryDisplayRow[]
  onAdjust: (row: InventoryDisplayRow) => void
  onHistory: (row: InventoryDisplayRow) => void
}

function StockTable({ rows, onAdjust, onHistory }: StockTableProps) {
  return (
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
            {rows.map((row) => (
              <tr key={row.productId} className="text-sm">
                <td className="py-3 pr-4 font-medium text-slate-900">{row.productName}</td>
                <td className="py-3 pr-4 font-mono text-slate-600">{row.sku}</td>
                <td
                  className={`py-3 pr-4 text-right text-base font-semibold ${
                    row.lowStock ? 'text-amber-700' : 'text-slate-900'
                  }`}
                >
                  {row.quantity}
                </td>
                <td className="py-3 pr-4 text-right text-slate-600">{row.lowStockThreshold}</td>
                <td className="py-3 pr-4">
                  {!row.hasRecord ? (
                    <Badge className="bg-slate-100 text-slate-600">No Stock Yet</Badge>
                  ) : row.lowStock ? (
                    <Badge className="bg-amber-100 text-amber-800">Low Stock</Badge>
                  ) : (
                    <Badge className="bg-emerald-100 text-emerald-800">In Stock</Badge>
                  )}
                </td>
                <td className="py-3 pr-4 text-slate-600">
                  {row.updatedAt ? formatDateTime(row.updatedAt) : '—'}
                </td>
                <td className="py-3 text-right">
                  <div className="flex justify-end gap-2">
                    {row.hasRecord ? (
                      <Button size="sm" variant="secondary" onClick={() => onHistory(row)}>
                        History
                      </Button>
                    ) : (
                      <span className="px-3 py-1.5 text-xs text-slate-400">—</span>
                    )}
                    <Button size="sm" onClick={() => onAdjust(row)}>
                      {row.hasRecord ? 'Adjust' : 'Add Stock'}
                    </Button>
                  </div>
                </td>
              </tr>
            ))}
          </tbody>
        </table>
      </div>
    </Card>
  )
}
