import { apiClient } from './client'

export type UserRole = 'ADMIN' | 'MANAGER' | 'CASHIER'

export interface UserCreateRequest {
  email: string
  name: string
  password: string
  role: UserRole
  active?: boolean | null
}

export interface UserRoleRequest {
  role: UserRole
}

export interface UserActiveRequest {
  active: boolean
}

export interface UserResponse {
  id: number
  email: string
  name: string
  role: UserRole
  active: boolean
  createdAt: string
  updatedAt: string
}

export async function createUser(request: UserCreateRequest): Promise<UserResponse> {
  const response = await apiClient.post<UserResponse>('/api/users', request)
  return response.data
}

export async function listUsers(): Promise<UserResponse[]> {
  const response = await apiClient.get<UserResponse[]>('/api/users')
  return response.data
}

export async function changeUserRole(
  id: number,
  request: UserRoleRequest,
): Promise<UserResponse> {
  const response = await apiClient.patch<UserResponse>(`/api/users/${id}/role`, request)
  return response.data
}

export async function changeUserActive(
  id: number,
  request: UserActiveRequest,
): Promise<UserResponse> {
  const response = await apiClient.patch<UserResponse>(`/api/users/${id}/active`, request)
  return response.data
}
