import type { LoginRequest } from '../../lib/api/auth'
import type { UserResponse } from '../../lib/api/users'

export type AuthStatus = 'loading' | 'authenticated' | 'anonymous'

export interface AuthContextValue {
  clearSession: () => void
  isAuthenticated: boolean
  login: (request: LoginRequest) => Promise<UserResponse>
  logout: () => void
  refreshMe: () => Promise<UserResponse | null>
  status: AuthStatus
  token: string | null
  user: UserResponse | null
}
