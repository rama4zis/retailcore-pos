import { useQueries, useQuery } from '@tanstack/react-query'

import {
  getDailySales,
  getMonthlySales,
  listLowStockReport,
  listPaymentSummary,
  listSalesByCashier,
  listTopSellingProducts,
} from '../../lib/api/reports'

export const reportsQueryKeys = {
  all: ['reports'] as const,
  cashierSales: () => [...reportsQueryKeys.all, 'sales-by-cashier'] as const,
  dailySales: (date: string) => [...reportsQueryKeys.all, 'daily-sales', date] as const,
  lowStock: () => [...reportsQueryKeys.all, 'low-stock'] as const,
  monthlySales: (year: number, month: number) =>
    [...reportsQueryKeys.all, 'monthly-sales', year, month] as const,
  paymentSummary: () => [...reportsQueryKeys.all, 'payment-summary'] as const,
  topProducts: () => [...reportsQueryKeys.all, 'top-products'] as const,
}

export function useDailySalesQuery(date: string) {
  return useQuery({
    enabled: Boolean(date),
    queryFn: () => getDailySales(date),
    queryKey: reportsQueryKeys.dailySales(date),
  })
}

export function useMonthlySalesQuery(year: number, month: number) {
  return useQuery({
    enabled: year > 0 && month >= 1 && month <= 12,
    queryFn: () => getMonthlySales(year, month),
    queryKey: reportsQueryKeys.monthlySales(year, month),
  })
}

export function useReportsOverviewQueries() {
  return useQueries({
    queries: [
      {
        queryFn: listTopSellingProducts,
        queryKey: reportsQueryKeys.topProducts(),
      },
      {
        queryFn: listLowStockReport,
        queryKey: reportsQueryKeys.lowStock(),
      },
      {
        queryFn: listSalesByCashier,
        queryKey: reportsQueryKeys.cashierSales(),
      },
      {
        queryFn: listPaymentSummary,
        queryKey: reportsQueryKeys.paymentSummary(),
      },
    ],
  })
}

export function useTopProductsQuery() {
  return useQuery({
    queryFn: listTopSellingProducts,
    queryKey: reportsQueryKeys.topProducts(),
  })
}

export function useLowStockReportQuery() {
  return useQuery({
    queryFn: listLowStockReport,
    queryKey: reportsQueryKeys.lowStock(),
  })
}

export function useSalesByCashierQuery() {
  return useQuery({
    queryFn: listSalesByCashier,
    queryKey: reportsQueryKeys.cashierSales(),
  })
}

export function usePaymentSummaryQuery() {
  return useQuery({
    queryFn: listPaymentSummary,
    queryKey: reportsQueryKeys.paymentSummary(),
  })
}
