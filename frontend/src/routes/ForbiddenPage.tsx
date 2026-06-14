import type { UserRole } from '../lib/api/users'
import { ErrorBanner } from '../components/feedback/ErrorBanner'
import { Badge } from '../components/ui/Badge'
import { Card } from '../components/ui/Card'
import { formatAllowedRoles } from './navConfig'

interface ForbiddenPageProps {
  allowedRoles: readonly UserRole[]
  userRole?: UserRole
}

export function ForbiddenPage({ allowedRoles, userRole }: ForbiddenPageProps) {
  return (
    <div className="space-y-5">
      <ErrorBanner
        message="Your current role cannot access this route. The backend will still enforce this; the frontend is only removing bad menu options before you click them."
        title="Access denied"
      />
      <Card title="Route access">
        <div className="flex flex-wrap gap-2 text-sm text-rc-secondary">
          <Badge variant="info">Allowed: {formatAllowedRoles(allowedRoles)}</Badge>
          <Badge variant="warning">Current: {userRole ?? 'Unknown'}</Badge>
        </div>
      </Card>
    </div>
  )
}
