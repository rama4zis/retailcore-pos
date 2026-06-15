import { useCallback, useEffect, useMemo, useState, type ReactNode } from 'react'
import { useQueryClient } from '@tanstack/react-query'

import { AUTH_UNAUTHORIZED_EVENT_NAME } from '../../lib/api/client'
import {
  getCurrentUser,
  login as requestLogin,
  type LoginRequest,
} from '../../lib/api/auth'
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

function getInitialAuthStatus(): AuthStatus {
  return getStoredAuthToken() ? 'loading' : 'anonymous'
}

export function AuthProvider({ children }: AuthProviderProps) {
  const queryClient = useQueryClient()
  const [token, setToken] = useState<string | null>(() => getStoredAuthToken())
  const [user, setUser] = useState<UserResponse | null>(null)
  const [status, setStatus] = useState<AuthStatus>(() => getInitialAuthStatus())

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

  useEffect(() => {
    const storedToken = getStoredAuthToken()

    if (!storedToken) {
      return undefined
    }

    let isCurrent = true

    getCurrentUser()
      .then((currentUser) => {
        if (!isCurrent || getStoredAuthToken() !== storedToken) {
          return
        }

        setToken(storedToken)
        setUser(currentUser)
        setStatus('authenticated')
      })
      .catch(() => {
        if (!isCurrent) {
          return
        }

        clearSession()
      })

    return () => {
      isCurrent = false
    }
  }, [clearSession])

  const login = useCallback(
    async (request: LoginRequest) => {
      const authResponse = await requestLogin(request)

      queryClient.clear()
      setStoredAuthToken(authResponse.token)
      setToken(authResponse.token)
      setUser(authResponse.user)
      setStatus('authenticated')

      return authResponse.user
    },
    [queryClient],
  )

  const logout = useCallback(() => {
    clearSession()
  }, [clearSession])

  const refreshMe = useCallback(async () => {
    const storedToken = getStoredAuthToken()

    if (!storedToken) {
      clearSession()
      return null
    }

    setStatus('loading')

    try {
      const currentUser = await getCurrentUser()
      setToken(storedToken)
      setUser(currentUser)
      setStatus('authenticated')
      return currentUser
    } catch {
      clearSession()
      return null
    }
  }, [clearSession])

  const value = useMemo<AuthContextValue>(
    () => ({
      clearSession,
      isAuthenticated: status === 'authenticated',
      login,
      logout,
      refreshMe,
      status,
      token,
      user,
    }),
    [clearSession, login, logout, refreshMe, status, token, user],
  )

  return <AuthContext.Provider value={value}>{children}</AuthContext.Provider>
}
