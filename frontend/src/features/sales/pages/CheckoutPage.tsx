import { useMemo, useRef, useState } from 'react'

import { ErrorBanner } from '../../../components/feedback/ErrorBanner'
import { Spinner } from '../../../components/feedback/Spinner'
import { Badge } from '../../../components/ui/Badge'
import { Button } from '../../../components/ui/Button'
import { Card } from '../../../components/ui/Card'
import { Input } from '../../../components/ui/Input'
import { Select } from '../../../components/ui/Select'
import { useProductsQuery } from '../../../features/products/queries'
import { formatCurrency } from '../../../lib/format/currency'
import { useCheckoutMutation } from '../queries'
import type {
  CheckoutItemRequest,
  CheckoutPaymentRequest,
  CheckoutRequest,
  ReceiptResponse,
} from '../../../lib/api/sales'
import type { ProductResponse } from '../../../lib/api/products'

type PaymentMethod = 'CASH' | 'CARD'

interface CartLineItem {
  productId: number
  sku: string
  productName: string
  unitPrice: number
  quantity: number
}

function toCheckoutRequest(
  cart: CartLineItem[],
  method: PaymentMethod,
  cashTendered: number | null,
  total: number,
): CheckoutRequest {
  const items: CheckoutItemRequest[] = cart.map((ci) => ({
    productId: ci.productId,
    quantity: ci.quantity,
  }))
  const payment: CheckoutPaymentRequest = {
    method,
    amount: total,
    cashTendered: method === 'CASH' ? cashTendered : null,
  }
  return { items, payment }
}

function sumCart(cart: CartLineItem[]): number {
  return cart.reduce((acc, ci) => acc + ci.unitPrice * ci.quantity, 0)
}

function filterProducts(
  products: ProductResponse[],
  query: string,
): ProductResponse[] {
  if (!query.trim()) return products
  const lower = query.toLowerCase()
  return products.filter(
    (p) =>
      p.name.toLowerCase().includes(lower) ||
      p.sku.toLowerCase().includes(lower) ||
      (p.barcode && p.barcode.toLowerCase().includes(lower)),
  )
}

/* ------------------------------------------------------------------ */
/*  Product card                                                       */
/* ------------------------------------------------------------------ */

interface ProductCardProps {
  product: ProductResponse
  onAdd: (product: ProductResponse) => void
  cartQuantity: number
}

function ProductCard({ product, onAdd, cartQuantity }: ProductCardProps) {
  const inCart = cartQuantity > 0

  return (
    <button
      className={`flex flex-col gap-1 rounded-xl border p-3 text-left shadow-sm transition-all focus-visible:outline-2 focus-visible:outline-offset-2 focus-visible:outline-rc-ring ${
        inCart
          ? 'border-rc-accent/40 bg-rc-accent-muted/30'
          : 'border-rc-border bg-rc-surface hover:border-rc-primary/40'
      } ${!product.active ? 'opacity-50' : ''}`}
      disabled={!product.active}
      onClick={() => onAdd(product)}
      type="button"
    >
      <div className="flex items-start justify-between gap-2">
        <span className="text-sm font-medium leading-tight text-rc-foreground line-clamp-2">
          {product.name}
        </span>
        {inCart ? (
          <Badge size="sm" variant="success">
            {cartQuantity}
          </Badge>
        ) : null}
      </div>
      <p className="text-xs text-rc-secondary">{product.sku}</p>
      <p className="text-sm font-semibold text-rc-accent">
        {formatCurrency(product.price)}
      </p>
      {!product.active ? (
        <p className="mt-0.5 text-xs font-medium text-rc-destructive">Inactive</p>
      ) : null}
    </button>
  )
}

/* ------------------------------------------------------------------ */
/*  Cart line item                                                     */
/* ------------------------------------------------------------------ */

interface CartItemRowProps {
  item: CartLineItem
  onIncrement: () => void
  onDecrement: () => void
  onRemove: () => void
}

function CartItemRow({ item, onIncrement, onDecrement, onRemove }: CartItemRowProps) {
  return (
    <div className="flex items-center gap-3 border-b border-rc-border pb-3 last:border-b-0">
      <div className="min-w-0 flex-1">
        <p className="truncate text-sm font-medium text-rc-foreground">
          {item.productName}
        </p>
        <p className="text-xs text-rc-secondary">{item.sku}</p>
      </div>

      <div className="flex shrink-0 items-center gap-1">
        <button
          aria-label={`Decrease quantity of ${item.productName}`}
          className="flex size-7 items-center justify-center rounded-md border border-rc-border text-sm font-medium text-rc-secondary hover:bg-rc-muted disabled:opacity-40"
          disabled={item.quantity <= 1}
          onClick={onDecrement}
          type="button"
        >
          −
        </button>
        <span className="w-8 text-center text-sm font-semibold tabular-nums text-rc-foreground">
          {item.quantity}
        </span>
        <button
          aria-label={`Increase quantity of ${item.productName}`}
          className="flex size-7 items-center justify-center rounded-md border border-rc-border text-sm font-medium text-rc-secondary hover:bg-rc-muted"
          onClick={onIncrement}
          type="button"
        >
          +
        </button>
      </div>

      <p className="w-20 text-right text-sm font-medium tabular-nums text-rc-foreground">
        {formatCurrency(item.unitPrice * item.quantity)}
      </p>

      <button
        aria-label={`Remove ${item.productName} from cart`}
        className="flex size-7 shrink-0 items-center justify-center rounded-md text-rc-secondary hover:bg-rc-destructive-muted hover:text-rc-destructive-strong"
        onClick={onRemove}
        title="Remove"
        type="button"
      >
        <svg
          aria-hidden="true"
          className="size-4"
          fill="none"
          stroke="currentColor"
          strokeWidth="2"
          viewBox="0 0 24 24"
        >
          <path
            d="M3 6h18M19 6v14a2 2 0 01-2 2H7a2 2 0 01-2-2V6m3 0V4a2 2 0 012-2h4a2 2 0 012 2v2"
            strokeLinecap="round"
            strokeLinejoin="round"
          />
        </svg>
      </button>
    </div>
  )
}

/* ------------------------------------------------------------------ */
/*  Receipt modal                                                      */
/* ------------------------------------------------------------------ */

interface ReceiptModalProps {
  receipt: ReceiptResponse
  onNewSale: () => void
}

function ReceiptModal({ receipt, onNewSale }: ReceiptModalProps) {
  const modalRef = useRef<HTMLDivElement>(null)

  return (
    <div
      aria-modal="true"
      className="fixed inset-0 z-50 flex items-center justify-center bg-black/50 p-4 backdrop-blur-sm"
      role="dialog"
    >
      <div
        className="flex max-h-[90vh] w-full max-w-lg flex-col overflow-y-auto rounded-2xl border border-rc-border bg-rc-background shadow-lg"
        ref={modalRef}
      >
        {/* Header */}
        <div className="border-b border-rc-border px-6 py-5 text-center">
          <p className="text-xs font-semibold uppercase tracking-wider text-rc-accent">
            Payment successful
          </p>
          <h2 className="mt-1 text-xl font-semibold text-rc-foreground">
            Receipt
          </h2>
          <p className="mt-1 text-sm text-rc-secondary">
            Sale #{receipt.saleNumber}
          </p>
        </div>

        <div className="space-y-4 px-6 py-5">
          {/* Meta */}
          <div className="flex justify-between text-sm">
            <span className="text-rc-secondary">Cashier</span>
            <span className="font-medium text-rc-foreground">{receipt.cashierName}</span>
          </div>
          <div className="flex justify-between text-sm">
            <span className="text-rc-secondary">Date</span>
            <span className="font-medium text-rc-foreground">
              {new Intl.DateTimeFormat(undefined, {
                dateStyle: 'medium',
                timeStyle: 'short',
              }).format(new Date(receipt.completedAt))}
            </span>
          </div>

          {/* Items */}
          <div className="border-t border-rc-border pt-4">
            <p className="mb-2 text-xs font-semibold uppercase tracking-wider text-rc-secondary">
              Items
            </p>
            <table className="w-full text-sm">
              <thead>
                <tr className="text-left text-xs text-rc-secondary">
                  <th className="pb-1 font-medium">Item</th>
                  <th className="pb-1 text-right font-medium">Qty</th>
                  <th className="pb-1 text-right font-medium">Price</th>
                  <th className="pb-1 text-right font-medium">Total</th>
                </tr>
              </thead>
              <tbody>
                {receipt.items.map((item) => (
                  <tr
                    className="border-t border-rc-border/50 text-rc-foreground"
                    key={item.productId}
                  >
                    <td className="py-1.5 pr-2">
                      <p className="truncate font-medium">{item.productName}</p>
                      <p className="text-xs text-rc-secondary">{item.sku}</p>
                    </td>
                    <td className="py-1.5 text-right tabular-nums">{item.quantity}</td>
                    <td className="py-1.5 text-right tabular-nums">
                      {formatCurrency(item.unitPrice)}
                    </td>
                    <td className="py-1.5 text-right tabular-nums">
                      {formatCurrency(item.lineTotal)}
                    </td>
                  </tr>
                ))}
              </tbody>
            </table>
          </div>

          {/* Payment */}
          <div className="border-t border-rc-border pt-4">
            <div className="flex justify-between text-sm">
              <span className="text-rc-secondary">Payment method</span>
              <span className="font-medium text-rc-foreground">
                {receipt.payment.method}
              </span>
            </div>
            <div className="mt-1 flex justify-between text-sm">
              <span className="text-rc-secondary">Amount paid</span>
              <span className="font-medium text-rc-foreground">
                {formatCurrency(receipt.payment.amount)}
              </span>
            </div>
            {receipt.payment.method === 'CASH' && receipt.payment.cashTendered != null ? (
              <div className="mt-1 flex justify-between text-sm">
                <span className="text-rc-secondary">Cash tendered</span>
                <span className="font-medium text-rc-foreground">
                  {formatCurrency(receipt.payment.cashTendered)}
                </span>
              </div>
            ) : null}
            <div className="mt-3 flex justify-between border-t border-rc-border pt-3 text-base">
              <span className="font-semibold text-rc-foreground">Change</span>
              <span className="font-semibold text-rc-accent">
                {formatCurrency(receipt.changeAmount)}
              </span>
            </div>
            <div className="mt-1 flex justify-between text-lg">
              <span className="font-bold text-rc-foreground">Total</span>
              <span className="font-bold text-rc-foreground">
                {formatCurrency(receipt.totalAmount)}
              </span>
            </div>
          </div>
        </div>

        {/* Footer */}
        <div className="border-t border-rc-border px-6 py-4">
          <Button
            className="w-full"
            onClick={onNewSale}
            size="lg"
            variant="primary"
          >
            New Sale
          </Button>
        </div>
      </div>
    </div>
  )
}

/* ------------------------------------------------------------------ */
/*  Checkout page                                                      */
/* ------------------------------------------------------------------ */

export function CheckoutPage() {
  const productsQuery = useProductsQuery()
  const checkoutMutation = useCheckoutMutation()

  const [searchQuery, setSearchQuery] = useState('')
  const [cart, setCart] = useState<CartLineItem[]>([])
  const [paymentMethod, setPaymentMethod] = useState<PaymentMethod>('CASH')
  const [cashTendered, setCashTendered] = useState<string>('')
  const [receipt, setReceipt] = useState<ReceiptResponse | null>(null)

  const total = useMemo(() => sumCart(cart), [cart])
  const cashTenderedNum = cashTendered === '' ? 0 : Number.parseFloat(cashTendered) || 0
  const changeAmount = paymentMethod === 'CASH' ? cashTenderedNum - total : 0
  const canCheckout =
    cart.length > 0 &&
    total > 0 &&
    !checkoutMutation.isPending &&
    (paymentMethod !== 'CASH' || cashTenderedNum >= total)

  const filteredProducts = useMemo(
    () => filterProducts(productsQuery.data ?? [], searchQuery),
    [productsQuery.data, searchQuery],
  )

  function handleAddToCart(product: ProductResponse) {
    setCart((prev) => {
      const existing = prev.find((ci) => ci.productId === product.id)
      if (existing) {
        return prev.map((ci) =>
          ci.productId === product.id ? { ...ci, quantity: ci.quantity + 1 } : ci,
        )
      }
      return [
        ...prev,
        {
          productId: product.id,
          sku: product.sku,
          productName: product.name,
          unitPrice: product.price,
          quantity: 1,
        },
      ]
    })
  }

  function handleIncrement(productId: number) {
    setCart((prev) =>
      prev.map((ci) =>
        ci.productId === productId ? { ...ci, quantity: ci.quantity + 1 } : ci,
      ),
    )
  }

  function handleDecrement(productId: number) {
    setCart((prev) =>
      prev.map((ci) =>
        ci.productId === productId && ci.quantity > 1
          ? { ...ci, quantity: ci.quantity - 1 }
          : ci,
      ),
    )
  }

  function handleRemove(productId: number) {
    setCart((prev) => prev.filter((ci) => ci.productId !== productId))
  }

  function cartQuantity(productId: number): number {
    return cart.find((ci) => ci.productId === productId)?.quantity ?? 0
  }

  function handleCheckout() {
    const request = toCheckoutRequest(cart, paymentMethod, cashTenderedNum, total)
    checkoutMutation.mutate(request, {
      onSuccess: (data) => {
        setReceipt(data)
        setCart([])
        setCashTendered('')
        setSearchQuery('')
        setPaymentMethod('CASH')
      },
    })
  }

  function handleNewSale() {
    setReceipt(null)
  }

  /* ---------- Loading / error states for product loading ---------- */

  if (productsQuery.isError) {
    return (
      <div className="space-y-5">
        <h1 className="text-2xl font-semibold tracking-tight text-rc-foreground sm:text-3xl">
          Checkout
        </h1>
        <ErrorBanner
          action={
            <Button onClick={() => productsQuery.refetch()} variant="secondary">
              Retry
            </Button>
          }
          message={
            productsQuery.error instanceof Error
              ? productsQuery.error.message
              : 'Failed to load products'
          }
          title="Could not load products"
        />
      </div>
    )
  }

  return (
    <div className="space-y-5">
      <h1 className="text-2xl font-semibold tracking-tight text-rc-foreground sm:text-3xl">
        Checkout
      </h1>

      <div className="flex flex-col gap-5 lg:flex-row">
        {/* ---- Left panel: product picker ---- */}
        <div className="flex-1 space-y-4">
          <Card>
            <Input
              label="Search products"
              onChange={(e) => setSearchQuery(e.target.value)}
              placeholder="Search by name, SKU, or barcode…"
              value={searchQuery}
            />
          </Card>

          {productsQuery.isLoading ? (
            <Card>
              <div className="flex items-center justify-center py-10">
                <Spinner label="Loading products" size="lg" />
              </div>
            </Card>
          ) : filteredProducts.length === 0 ? (
            <Card>
              <p className="py-8 text-center text-sm text-rc-secondary">
                {searchQuery
                  ? 'No products match your search.'
                  : 'No products available.'}
              </p>
            </Card>
          ) : (
            <div className="grid grid-cols-1 gap-3 sm:grid-cols-2 xl:grid-cols-3">
              {filteredProducts.map((product) => (
                <ProductCard
                  cartQuantity={cartQuantity(product.id)}
                  key={product.id}
                  onAdd={handleAddToCart}
                  product={product}
                />
              ))}
            </div>
          )}
        </div>

        {/* ---- Right panel: cart + payment ---- */}
        <div className="w-full space-y-4 lg:w-96">
          {/* Cart */}
          <Card title={`Cart (${cart.reduce((a, ci) => a + ci.quantity, 0)} items)`}>
            {cart.length === 0 ? (
              <p className="py-6 text-center text-sm text-rc-secondary">
                Select products to start a sale.
              </p>
            ) : (
              <div className="space-y-3">
                {cart.map((ci) => (
                  <CartItemRow
                    item={ci}
                    key={ci.productId}
                    onDecrement={() => handleDecrement(ci.productId)}
                    onIncrement={() => handleIncrement(ci.productId)}
                    onRemove={() => handleRemove(ci.productId)}
                  />
                ))}

                <div className="flex items-center justify-between border-t border-rc-border pt-3">
                  <span className="text-sm font-medium text-rc-foreground">Total</span>
                  <span className="text-lg font-bold tabular-nums text-rc-foreground">
                    {formatCurrency(total)}
                  </span>
                </div>
              </div>
            )}
          </Card>

          {/* Payment */}
          <Card title="Payment">
            <div className="space-y-4">
              <Select
                label="Payment method"
                onChange={(e) => {
                  setPaymentMethod(e.target.value as PaymentMethod)
                  if (e.target.value === 'CARD') setCashTendered('')
                }}
                value={paymentMethod}
              >
                <option value="CASH">Cash</option>
                <option value="CARD">Card</option>
              </Select>

              {paymentMethod === 'CASH' ? (
                <Input
                  label="Cash tendered"
                  min="0"
                  onChange={(e) => setCashTendered(e.target.value)}
                  placeholder="0.00"
                  step="0.01"
                  type="number"
                  value={cashTendered}
                />
              ) : null}

              {paymentMethod === 'CASH' && cashTenderedNum > 0 ? (
                <div className="flex items-center justify-between rounded-lg bg-rc-muted px-3 py-2">
                  <span className="text-sm text-rc-secondary">Change</span>
                  <span
                    className={`text-sm font-semibold tabular-nums ${
                      changeAmount < 0 ? 'text-rc-destructive' : 'text-rc-accent'
                    }`}
                  >
                    {changeAmount >= 0
                      ? formatCurrency(changeAmount)
                      : `-${formatCurrency(Math.abs(changeAmount))}`}
                  </span>
                </div>
              ) : null}
            </div>
          </Card>

          {/* Submit */}
          <Button
            className="w-full"
            disabled={!canCheckout}
            isLoading={checkoutMutation.isPending}
            loadingText="Processing checkout…"
            onClick={handleCheckout}
            size="lg"
            variant="success"
          >
            Complete Sale — {formatCurrency(total)}
          </Button>

          {/* Mutation error */}
          {checkoutMutation.isError ? (
            <ErrorBanner
              message={
                checkoutMutation.error instanceof Error
                  ? checkoutMutation.error.message
                  : 'Checkout failed'
              }
              title="Checkout failed"
            />
          ) : null}
        </div>
      </div>

      {/* Receipt modal */}
      {receipt ? <ReceiptModal onNewSale={handleNewSale} receipt={receipt} /> : null}
    </div>
  )
}
