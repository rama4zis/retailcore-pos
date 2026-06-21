import { useEffect, useRef, useState } from 'react'

import type { InventoryStockResponse } from '../../../lib/api/inventory'
import { Badge } from '../../../components/ui/Badge'
import { Button } from '../../../components/ui/Button'
import { Input } from '../../../components/ui/Input'
import { Textarea } from '../../../components/ui/Textarea'
import { ErrorBanner } from '../../../components/feedback/ErrorBanner'
import { getApiErrorMessage } from '../../../lib/api/errors'
import { useAdjustStock } from '../queries'

interface StockAdjustmentModalProps {
  onClose: () => void
  stock: InventoryStockResponse
}

export function StockAdjustmentModal({ onClose, stock }: StockAdjustmentModalProps) {
  const closeButtonRef = useRef<HTMLButtonElement>(null)
  const [quantityChange, setQuantityChange] = useState('')
  const [lowStockThreshold, setLowStockThreshold] = useState(stock.lowStockThreshold.toString())
  const [reason, setReason] = useState('')

  const adjustMutation = useAdjustStock(stock.productId)
  const errorMessage = adjustMutation.error ? getApiErrorMessage(adjustMutation.error) : null

  const handleSubmit = (e: React.FormEvent) => {
    e.preventDefault()

    const change = parseInt(quantityChange, 10)
    if (isNaN(change) || change === 0) {
      return
    }

    const threshold = parseInt(lowStockThreshold, 10)
    if (isNaN(threshold) || threshold < 0) {
      return
    }

    adjustMutation.mutate(
      {
        lowStockThreshold: threshold,
        quantityChange: change,
        reason: reason.trim() || null,
      },
      {
        onSuccess: () => {
          onClose()
        },
      },
    )
  }

  const projectedStock = stock.quantity + (parseInt(quantityChange, 10) || 0)

  useEffect(() => {
    closeButtonRef.current?.focus()

    const handleKeyDown = (event: KeyboardEvent) => {
      if (event.key === 'Escape') {
        onClose()
      }
    }

    document.addEventListener('keydown', handleKeyDown)

    return () => document.removeEventListener('keydown', handleKeyDown)
  }, [onClose])

  return (
    <div className="fixed inset-0 z-50 flex items-center justify-center bg-slate-900/50 p-4">
      <div
        aria-labelledby="stock-adjustment-title"
        aria-modal="true"
        className="max-h-[90vh] w-full max-w-lg overflow-y-auto rounded-lg bg-white p-6 shadow-xl"
        role="dialog"
      >
        <div className="mb-4 flex items-start justify-between gap-4">
          <h2 className="text-xl font-semibold text-slate-900" id="stock-adjustment-title">
            Adjust Stock
          </h2>
          <Button ref={closeButtonRef} size="sm" type="button" variant="ghost" onClick={onClose}>
            Close
          </Button>
        </div>

        <div className="mb-4 rounded-lg border border-slate-200 bg-slate-50 p-4">
          <div className="mb-1 text-sm font-medium text-slate-700">{stock.productName}</div>
          <div className="mb-2 text-xs text-slate-600">SKU: {stock.sku}</div>
          <div className="flex items-center gap-3">
            <div>
              <div className="text-xs text-slate-600">Current Stock</div>
              <div className="text-2xl font-bold text-slate-900">{stock.quantity}</div>
            </div>
            {stock.lowStock && (
              <Badge className="bg-amber-100 text-amber-800">Low Stock</Badge>
            )}
          </div>
        </div>

        {errorMessage && <ErrorBanner className="mb-4" message={errorMessage} />}

        <form onSubmit={handleSubmit}>
          <div className="space-y-4">
            <Input
              label="Quantity Change"
              placeholder="e.g., 50 or -10"
              required
              type="number"
              value={quantityChange}
              onChange={(e) => setQuantityChange(e.target.value)}
              helperText={
                quantityChange
                  ? projectedStock >= 0
                    ? `New stock will be ${projectedStock}`
                    : `Cannot reduce stock below zero (projected: ${projectedStock})`
                  : 'Enter positive to add, negative to reduce'
              }
            />

            <Input
              label="Low Stock Threshold"
              min={0}
              required
              type="number"
              value={lowStockThreshold}
              onChange={(e) => setLowStockThreshold(e.target.value)}
              helperText="Alert when stock falls to or below this level"
            />

            <Textarea
              label="Reason (optional)"
              maxLength={500}
              placeholder="e.g., Received shipment, inventory count correction, damaged goods"
              rows={3}
              value={reason}
              onChange={(e) => setReason(e.target.value)}
            />
          </div>

          <div className="mt-6 flex justify-end gap-3">
            <Button disabled={adjustMutation.isPending} type="button" variant="secondary" onClick={onClose}>
              Cancel
            </Button>
            <Button
              disabled={
                adjustMutation.isPending ||
                !quantityChange ||
                parseInt(quantityChange, 10) === 0 ||
                projectedStock < 0
              }
              isLoading={adjustMutation.isPending}
              type="submit"
            >
              Adjust Stock
            </Button>
          </div>
        </form>
      </div>
    </div>
  )
}
