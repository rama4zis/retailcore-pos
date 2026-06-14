import type { ReactNode } from 'react'
import { Navigate, useLocation } from 'react-router-dom'

import { Spinner } from '../components/feedback/Spinner'
import { useAuth } from '../features/auth/useAuth'
import type { UserRole } from '../lib/api/users'
import { ForbiddenPage } from './ForbiddenPage'

interface RoleGuardProps {
  allowedRoles: readonly UserRole[]
  children: ReactNode
}

export function RoleGuard({ allowedRoles, children }: RoleGuardProps) {
  const { status, user } = useAuth()
  const location = useLocation()

  if (status === 'loading') {
    return (
      <div className="flex min-h-64 items-center justify-center text-rc-secondary">
        <div className="flex items-center gap-3">
          <Spinner />
          <span className="text-sm font-medium">Checking route access</span>
        </div>
      </div>
    )
  }

  if (status === 'anonymous') {
    return <Navigate replace state={{ from: location }} to="/login" />
  }

  if (!user || !allowedRoles.includes(user.role)) {
    return <ForbiddenPage allowedRoles={allowedRoles} userRole={user?.role} />
  }

  return children
}
