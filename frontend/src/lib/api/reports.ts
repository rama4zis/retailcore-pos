import { apiClient } from './client'
import type { InventoryStockResponse } from './inventory'
import type { PaymentMethod } from './sales'

export interface SalesTotalResponse {
  period: string
  totalAmount: number
}

export interface TopSellingProductResponse {
  productId: number
  sku: string
  productName: string
  quantitySold: number
  grossSales: number
}

export interface CashierSalesReportResponse {
  cashierId: number
  cashierName: string
  cashierEmail: string
  saleCount: number
  totalAmount: number
}

export interface PaymentMethodSummaryResponse {
  method: PaymentMethod
  paymentCount: number
  totalAmount: number
}

export async function getDailySales(date: string): Promise<SalesTotalResponse> {
  const response = await apiClient.get<SalesTotalResponse>('/api/reports/daily-sales', {
    params: { date },
  })
  return response.data
}

export async function getMonthlySales(
  year: number,
  month: number,
): Promise<SalesTotalResponse> {
  const response = await apiClient.get<SalesTotalResponse>('/api/reports/monthly-sales', {
    params: { year, month },
  })
  return response.data
}

export async function listTopSellingProducts(): Promise<TopSellingProductResponse[]> {
  const response = await apiClient.get<TopSellingProductResponse[]>(
    '/api/reports/top-products',
  )
  return response.data
}

export async function listLowStockReport(): Promise<InventoryStockResponse[]> {
  const response = await apiClient.get<InventoryStockResponse[]>('/api/reports/low-stock')
  return response.data
}

export async function listSalesByCashier(): Promise<CashierSalesReportResponse[]> {
  const response = await apiClient.get<CashierSalesReportResponse[]>(
    '/api/reports/sales-by-cashier',
  )
  return response.data
}

export async function listPaymentSummary(): Promise<PaymentMethodSummaryResponse[]> {
  const response = await apiClient.get<PaymentMethodSummaryResponse[]>(
    '/api/reports/payment-summary',
  )
  return response.data
}
