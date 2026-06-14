import { Navigate, useLocation } from 'react-router-dom'

import { Badge } from '../components/ui/Badge'
import { Card } from '../components/ui/Card'
import { PageHeader } from '../components/layout/PageHeader'
import { useAuth } from '../features/auth/useAuth'

interface LocationState {
  from?: {
    pathname?: string
  }
}

function getRedirectPath(state: unknown) {
  if (typeof state !== 'object' || state === null || !('from' in state)) {
    return '/dashboard'
  }

  const locationState = state as LocationState
  return locationState.from?.pathname ?? '/dashboard'
}

export function LoginPlaceholderPage() {
  const { status } = useAuth()
  const location = useLocation()
  const redirectPath = getRedirectPath(location.state)

  if (status === 'authenticated') {
    return <Navigate replace to={redirectPath} />
  }

  return (
    <main className="min-h-screen px-4 py-6 text-rc-foreground sm:px-6 lg:px-8">
      <div className="mx-auto flex min-h-[calc(100vh-3rem)] max-w-3xl flex-col justify-center gap-5">
        <PageHeader
          description="The public login route is registered, but the real form and session restore belong to FE-05. This screen exists so protected redirects have somewhere honest to land."
          eyebrow="RetailCore POS"
          meta={
            <>
              <Badge variant="info">FE-04 shell</Badge>
              <Badge variant="warning">Login incomplete</Badge>
            </>
          }
          title="Login route ready"
        />

        <Card title="Next checkpoint">
          <p className="text-sm leading-6 text-rc-secondary">
            FE-05 will connect POST /api/auth/login, GET /api/auth/me, token
            persistence, logout, and redirect-back behavior. Until then, this is
            a route target, not a fake auth screen.
          </p>
        </Card>
      </div>
    </main>
  )
}
