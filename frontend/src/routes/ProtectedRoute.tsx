import type { ReactNode } from 'react'
import { Navigate, useLocation } from 'react-router-dom'

import { Spinner } from '../components/feedback/Spinner'
import { useAuth } from '../features/auth/useAuth'

interface ProtectedRouteProps {
  children: ReactNode
}

function ProtectedRouteLoading() {
  return (
    <main className="flex min-h-screen items-center justify-center bg-rc-background px-4 text-rc-foreground">
      <div className="flex items-center gap-3 rounded-2xl border border-rc-border bg-rc-surface px-5 py-4 shadow-rc-card">
        <Spinner />
        <span className="text-sm font-medium text-rc-secondary">
          Checking session
        </span>
      </div>
    </main>
  )
}

export function ProtectedRoute({ children }: ProtectedRouteProps) {
  const { status } = useAuth()
  const location = useLocation()

  if (status === 'loading') {
    return <ProtectedRouteLoading />
  }

  if (status === 'anonymous') {
    return <Navigate replace state={{ from: location }} to="/login" />
  }

  return children
}
