import { apiClient } from './client'

export type StockMovementType = 'ADJUSTMENT' | 'SALE' | 'REFUND'

export interface StockAdjustmentRequest {
  quantityChange: number
  lowStockThreshold?: number | null
  reason?: string | null
}

export interface InventoryStockResponse {
  productId: number
  sku: string
  productName: string
  quantity: number
  lowStockThreshold: number
  lowStock: boolean
  createdAt: string
  updatedAt: string
}

export interface StockMovementResponse {
  id: number
  productId: number
  sku: string
  movementType: StockMovementType
  quantityChange: number
  stockAfter: number
  reason: string | null
  createdAt: string
}

export async function listInventory(): Promise<InventoryStockResponse[]> {
  const response = await apiClient.get<InventoryStockResponse[]>('/api/inventory')
  return response.data
}

export async function listLowStockInventory(): Promise<InventoryStockResponse[]> {
  const response = await apiClient.get<InventoryStockResponse[]>('/api/inventory/low-stock')
  return response.data
}

export async function getInventoryStock(productId: number): Promise<InventoryStockResponse> {
  const response = await apiClient.get<InventoryStockResponse>(`/api/inventory/${productId}`)
  return response.data
}

export async function adjustInventoryStock(
  productId: number,
  request: StockAdjustmentRequest,
): Promise<InventoryStockResponse> {
  const response = await apiClient.post<InventoryStockResponse>(
    `/api/inventory/${productId}/adjust`,
    request,
  )
  return response.data
}

export async function listStockMovements(productId: number): Promise<StockMovementResponse[]> {
  const response = await apiClient.get<StockMovementResponse[]>(
    `/api/inventory/${productId}/movements`,
  )
  return response.data
}
