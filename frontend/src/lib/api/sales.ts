import { apiClient } from './client'

export type PaymentMethod = 'CASH' | 'CARD'
export type SaleStatus = 'COMPLETED' | 'PARTIALLY_REFUNDED' | 'REFUNDED'

export interface CheckoutItemRequest {
  productId: number
  quantity: number
}

export interface CheckoutPaymentRequest {
  method: PaymentMethod
  amount: number
  cashTendered?: number | null
}

export interface CheckoutRequest {
  items: CheckoutItemRequest[]
  payment: CheckoutPaymentRequest
}

export interface SaleItemResponse {
  id: number
  productId: number
  sku: string
  productName: string
  quantity: number
  unitPrice: number
  lineTotal: number
}

export interface SaleResponse {
  id: number
  saleNumber: string
  cashierId: number
  cashierName: string
  status: SaleStatus
  totalAmount: number
  completedAt: string
  items: SaleItemResponse[]
}

export interface ReceiptItemResponse {
  productId: number
  sku: string
  productName: string
  quantity: number
  unitPrice: number
  lineTotal: number
}

export interface ReceiptPaymentResponse {
  method: PaymentMethod
  amount: number
  cashTendered: number | null
  changeAmount: number
  paidAt: string
}

export interface ReceiptResponse {
  saleId: number
  saleNumber: string
  cashierName: string
  completedAt: string
  items: ReceiptItemResponse[]
  totalAmount: number
  payment: ReceiptPaymentResponse
  changeAmount: number
}

export interface RefundItemRequest {
  productId: number
  quantity: number
}

export interface RefundRequest {
  items: RefundItemRequest[]
  reason?: string | null
}

export interface RefundItemResponse {
  id: number
  productId: number
  sku: string
  productName: string
  quantity: number
  unitPrice: number
  lineTotal: number
}

export interface RefundResponse {
  id: number
  saleId: number
  saleNumber: string
  saleStatus: SaleStatus
  totalAmount: number
  reason: string | null
  refundedAt: string
  items: RefundItemResponse[]
}

export async function checkoutSale(request: CheckoutRequest): Promise<ReceiptResponse> {
  const response = await apiClient.post<ReceiptResponse>('/api/sales/checkout', request)
  return response.data
}

export async function listSales(): Promise<SaleResponse[]> {
  const response = await apiClient.get<SaleResponse[]>('/api/sales')
  return response.data
}

export async function getSale(id: number): Promise<SaleResponse> {
  const response = await apiClient.get<SaleResponse>(`/api/sales/${id}`)
  return response.data
}

export async function refundSale(
  id: number,
  request: RefundRequest,
): Promise<RefundResponse> {
  const response = await apiClient.post<RefundResponse>(`/api/sales/${id}/refunds`, request)
  return response.data
}
