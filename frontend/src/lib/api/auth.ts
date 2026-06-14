import { apiClient } from './client'
import type { UserResponse } from './users'

export interface LoginRequest {
  email: string
  password: string
}

export interface AuthResponse {
  token: string
  user: UserResponse
}

export async function login(request: LoginRequest): Promise<AuthResponse> {
  const response = await apiClient.post<AuthResponse>('/api/auth/login', request)
  return response.data
}

export async function getCurrentUser(): Promise<UserResponse> {
  const response = await apiClient.get<UserResponse>('/api/auth/me')
  return response.data
}
