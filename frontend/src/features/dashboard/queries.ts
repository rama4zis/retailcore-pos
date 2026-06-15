import { useQuery } from '@tanstack/react-query'

import { listLowStockInventory } from '../../lib/api/inventory'
import {
  getDailySales,
  getMonthlySales,
  listLowStockReport,
  listTopSellingProducts,
} from '../../lib/api/reports'

interface YearMonth {
  month: number
  year: number
}

interface UseManagementDashboardQueriesOptions {
  enabled: boolean
  reportDate: string
  reportMonth: YearMonth
}

interface UseCashierDashboardQueryOptions {
  enabled: boolean
}

export const dashboardQueryKeys = {
  all: ['dashboard'] as const,
  cashierLowStock: () => [...dashboardQueryKeys.all, 'cashier-low-stock'] as const,
  dailySales: (date: string) => [...dashboardQueryKeys.all, 'daily-sales', date] as const,
  managementLowStock: () => [...dashboardQueryKeys.all, 'management-low-stock'] as const,
  monthlySales: (year: number, month: number) =>
    [...dashboardQueryKeys.all, 'monthly-sales', year, month] as const,
  topProducts: () => [...dashboardQueryKeys.all, 'top-products'] as const,
}

export function useManagementDashboardQueries({
  enabled,
  reportDate,
  reportMonth,
}: UseManagementDashboardQueriesOptions) {
  const dailySalesQuery = useQuery({
    enabled,
    queryFn: () => getDailySales(reportDate),
    queryKey: dashboardQueryKeys.dailySales(reportDate),
  })

  const monthlySalesQuery = useQuery({
    enabled,
    queryFn: () => getMonthlySales(reportMonth.year, reportMonth.month),
    queryKey: dashboardQueryKeys.monthlySales(reportMonth.year, reportMonth.month),
  })

  const lowStockQuery = useQuery({
    enabled,
    queryFn: listLowStockReport,
    queryKey: dashboardQueryKeys.managementLowStock(),
  })

  const topProductsQuery = useQuery({
    enabled,
    queryFn: listTopSellingProducts,
    queryKey: dashboardQueryKeys.topProducts(),
  })

  return {
    dailySalesQuery,
    lowStockQuery,
    monthlySalesQuery,
    topProductsQuery,
  }
}

export function useCashierDashboardQuery({ enabled }: UseCashierDashboardQueryOptions) {
  return useQuery({
    enabled,
    queryFn: listLowStockInventory,
    queryKey: dashboardQueryKeys.cashierLowStock(),
  })
}
