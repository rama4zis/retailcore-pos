const currencyFormatter = new Intl.NumberFormat(undefined, {
  currency: 'USD',
  style: 'currency',
})

export function formatCurrency(amount: number) {
  return currencyFormatter.format(amount)
}
