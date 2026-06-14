import { apiClient } from './client'

export interface ProductCreateRequest {
  categoryId: number
  sku: string
  barcode?: string | null
  name: string
  description?: string | null
  price: number
  active?: boolean | null
}

export interface ProductUpdateRequest {
  categoryId: number
  sku: string
  barcode?: string | null
  name: string
  description?: string | null
  price: number
  active: boolean
}

export interface ProductActiveRequest {
  active: boolean
}

export interface ProductResponse {
  id: number
  categoryId: number
  categoryName: string
  sku: string
  barcode: string | null
  name: string
  description: string | null
  price: number
  active: boolean
  createdAt: string
  updatedAt: string
}

export async function createProduct(request: ProductCreateRequest): Promise<ProductResponse> {
  const response = await apiClient.post<ProductResponse>('/api/products', request)
  return response.data
}

export async function listProducts(): Promise<ProductResponse[]> {
  const response = await apiClient.get<ProductResponse[]>('/api/products')
  return response.data
}

export async function getProduct(id: number): Promise<ProductResponse> {
  const response = await apiClient.get<ProductResponse>(`/api/products/${id}`)
  return response.data
}

export async function updateProduct(
  id: number,
  request: ProductUpdateRequest,
): Promise<ProductResponse> {
  const response = await apiClient.put<ProductResponse>(`/api/products/${id}`, request)
  return response.data
}

export async function changeProductActive(
  id: number,
  request: ProductActiveRequest,
): Promise<ProductResponse> {
  const response = await apiClient.patch<ProductResponse>(`/api/products/${id}/active`, request)
  return response.data
}
