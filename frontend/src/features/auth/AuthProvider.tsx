import { useCallback, useEffect, useMemo, useState, type ReactNode } from 'react'
import { useQueryClient } from '@tanstack/react-query'

import { AUTH_UNAUTHORIZED_EVENT_NAME } from '../../lib/api/client'
import {
  clearStoredAuthToken,
  getStoredAuthToken,
  setStoredAuthToken,
} from '../../lib/auth/tokenStorage'
import type { UserResponse } from '../../lib/api/users'
import { AuthContext } from './AuthContext'
import type { AuthContextValue, AuthStatus } from './types'

interface AuthProviderProps {
  children: ReactNode
}

export function AuthProvider({ children }: AuthProviderProps) {
  const queryClient = useQueryClient()
  const [token, setToken] = useState<string | null>(() => getStoredAuthToken())
  const [user, setUser] = useState<UserResponse | null>(null)
  const [status, setStatus] = useState<AuthStatus>('anonymous')

  const clearSession = useCallback(() => {
    clearStoredAuthToken()
    setToken(null)
    setUser(null)
    setStatus('anonymous')
    queryClient.clear()
  }, [queryClient])

  useEffect(() => {
    if (typeof window === 'undefined') {
      return undefined
    }

    const handleUnauthorized = () => {
      clearSession()
    }

    window.addEventListener(AUTH_UNAUTHORIZED_EVENT_NAME, handleUnauthorized)

    return () => {
      window.removeEventListener(AUTH_UNAUTHORIZED_EVENT_NAME, handleUnauthorized)
    }
  }, [clearSession])

  const setAuthenticatedSession = useCallback((nextToken: string, nextUser: UserResponse) => {
    setStoredAuthToken(nextToken)
    setToken(nextToken)
    setUser(nextUser)
    setStatus('authenticated')
  }, [])

  const refreshMe = useCallback(async () => {
    return null
  }, [])

  const value = useMemo<AuthContextValue>(
    () => ({
      clearSession,
      isAuthenticated: status === 'authenticated',
      refreshMe,
      setAuthenticatedSession,
      status,
      token,
      user,
    }),
    [clearSession, refreshMe, setAuthenticatedSession, status, token, user],
  )

  return <AuthContext.Provider value={value}>{children}</AuthContext.Provider>
}
