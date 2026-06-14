import { Badge } from '../components/ui/Badge'
import { Card } from '../components/ui/Card'
import { PageHeader } from '../components/layout/PageHeader'
import type { ProtectedRouteConfig } from './navConfig'
import { formatAllowedRoles } from './navConfig'

interface RoutePlaceholderPageProps {
  route: ProtectedRouteConfig
}

export function RoutePlaceholderPage({ route }: RoutePlaceholderPageProps) {
  return (
    <div className="space-y-5">
      <PageHeader
        description={`${route.description} This route is wired for ${route.buildStep}, but the API-backed screen is intentionally incomplete in FE-04.`}
        eyebrow="Route placeholder"
        meta={
          <>
            <Badge variant="info">{route.buildStep}</Badge>
            <Badge variant="neutral">{route.path}</Badge>
            <Badge variant="success">{formatAllowedRoles(route.roles)}</Badge>
          </>
        }
        title={route.label}
      />

      <Card
        description="This checkpoint only proves routing, guards, layout, and navigation. Real backend calls arrive in the feature checkpoint listed above."
        title="Incomplete by design"
      >
        <p className="text-sm leading-6 text-rc-secondary">
          No mock rows. No fake KPIs. No pretend checkout. The route exists so later
          steps can replace this panel with real API-backed UI without moving the
          shell again. Efficient. Slightly less dramatic than your usual plans.
        </p>
      </Card>
    </div>
  )
}
