import type { UserResponse } from '../../lib/api/users'

export type AuthStatus = 'loading' | 'authenticated' | 'anonymous'

export interface AuthContextValue {
  clearSession: () => void
  isAuthenticated: boolean
  refreshMe: () => Promise<UserResponse | null>
  setAuthenticatedSession: (token: string, user: UserResponse) => void
  status: AuthStatus
  token: string | null
  user: UserResponse | null
}
