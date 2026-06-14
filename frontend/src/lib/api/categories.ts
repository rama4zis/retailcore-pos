import { apiClient } from './client'

export interface CategoryCreateRequest {
  name: string
  description?: string | null
}

export interface CategoryUpdateRequest {
  name: string
  description?: string | null
  active: boolean
}

export interface CategoryResponse {
  id: number
  name: string
  description: string | null
  active: boolean
  createdAt: string
  updatedAt: string
}

export async function createCategory(
  request: CategoryCreateRequest,
): Promise<CategoryResponse> {
  const response = await apiClient.post<CategoryResponse>('/api/categories', request)
  return response.data
}

export async function listCategories(): Promise<CategoryResponse[]> {
  const response = await apiClient.get<CategoryResponse[]>('/api/categories')
  return response.data
}

export async function getCategory(id: number): Promise<CategoryResponse> {
  const response = await apiClient.get<CategoryResponse>(`/api/categories/${id}`)
  return response.data
}

export async function updateCategory(
  id: number,
  request: CategoryUpdateRequest,
): Promise<CategoryResponse> {
  const response = await apiClient.put<CategoryResponse>(`/api/categories/${id}`, request)
  return response.data
}

export async function deleteCategory(id: number): Promise<void> {
  await apiClient.delete<void>(`/api/categories/${id}`)
}
