import { ErrorBanner } from '../components/feedback/ErrorBanner'
import { EmptyState } from '../components/feedback/EmptyState'
import { Skeleton } from '../components/feedback/Skeleton'
import { Spinner } from '../components/feedback/Spinner'
import { PageHeader } from '../components/layout/PageHeader'
import { Badge } from '../components/ui/Badge'
import { Button } from '../components/ui/Button'
import { Card } from '../components/ui/Card'
import { Input } from '../components/ui/Input'
import { Select } from '../components/ui/Select'

export function App() {
  return (
    <main className="min-h-screen px-4 py-6 text-rc-foreground sm:px-6 lg:px-8">
      <div className="mx-auto max-w-6xl space-y-6">
        <PageHeader
          actions={
            <>
              <Badge size="md" variant="success">
                FE-02
              </Badge>
              <Badge size="md" variant="info">
                UI foundation
              </Badge>
            </>
          }
          description="Design tokens, global styles, and shared controls are ready for the API-backed checkpoints that come next. No backend calls here. Calm down, network tab."
          eyebrow="RetailCore POS"
          meta={
            <>
              <Badge variant="neutral">Slate surfaces</Badge>
              <Badge variant="success">Emerald actions</Badge>
              <Badge variant="warning">Clear stock warnings</Badge>
            </>
          }
          title="Operations UI base layer"
        />

        <section className="grid gap-5 lg:grid-cols-2">
          <Card
            actions={<Badge variant="success">Keyboard friendly</Badge>}
            description="Reusable form controls for checkout, catalog, inventory, and reports. Labels stay visible because placeholder-only UX is a trap."
            title="Form primitives"
          >
            <div className="grid gap-4 sm:grid-cols-2">
              <Input
                helperText="Accepts SKU, barcode, or product name in later checkout flows."
                label="Product search"
                placeholder="Search inventory"
                type="search"
              />
              <Select defaultValue="CASH" label="Payment method">
                <option value="CASH">Cash</option>
                <option value="CARD">Card</option>
              </Select>
            </div>
            <div className="mt-5 flex flex-wrap gap-3">
              <Button>Save changes</Button>
              <Button variant="secondary">Review later</Button>
              <Button variant="danger">Delete record</Button>
            </div>
          </Card>

          <Card
            description="Status language is text-first, with color as reinforcement. Future pages can reuse these states instead of inventing one-off styles."
            title="Status and feedback"
          >
            <div className="flex flex-wrap gap-2">
              <Badge variant="neutral">Inactive</Badge>
              <Badge variant="success">In stock</Badge>
              <Badge variant="warning">Low stock</Badge>
              <Badge variant="danger">Conflict</Badge>
              <Badge variant="info">Manager only</Badge>
            </div>
            <div className="mt-5 grid gap-4">
              <ErrorBanner
                message="The backend will own real messages later. This component is ready to display them without swallowing useful error details."
                title="API error preview"
              />
              <div className="flex items-center gap-3 rounded-xl border border-rc-border bg-rc-muted px-4 py-3 text-sm text-rc-secondary">
                <Spinner size="sm" />
                Loading inventory rows
              </div>
            </div>
          </Card>
        </section>

        <section className="grid gap-5 lg:grid-cols-[2fr_1fr]">
          <Card
            actions={<Button size="sm" variant="secondary">Retry</Button>}
            description="Foundational table-loading visuals for dense POS pages without mocking API data."
            title="Loading structure"
          >
            <div className="space-y-3">
              <Skeleton className="h-6 w-1/3" />
              <Skeleton lines={4} />
              <div className="grid gap-3 sm:grid-cols-3">
                <Skeleton className="h-16" radius="lg" />
                <Skeleton className="h-16" radius="lg" />
                <Skeleton className="h-16" radius="lg" />
              </div>
            </div>
          </Card>

          <EmptyState
            action={<Button size="sm" variant="success">Add first item</Button>}
            description="Use this when a real API-backed list returns nothing. No fake rows. No stage props."
            title="Nothing to show yet"
          />
        </section>

        <Card
          description="These numbers are static component samples, not business metrics. The real dashboard gets wired in FE-06."
          title="Data card rhythm"
        >
          <div className="grid gap-4 sm:grid-cols-3">
            <div className="rounded-xl border border-rc-border bg-rc-surface-raised p-4">
              <p className="text-xs font-semibold uppercase tracking-wide text-rc-secondary">
                Target row height
              </p>
              <p className="mt-2 font-rc-data text-2xl font-semibold text-rc-foreground">
                44px
              </p>
            </div>
            <div className="rounded-xl border border-rc-border bg-rc-surface-raised p-4">
              <p className="text-xs font-semibold uppercase tracking-wide text-rc-secondary">
                Accent token
              </p>
              <p className="mt-2 font-rc-data text-2xl font-semibold text-rc-accent">
                rc-accent
              </p>
            </div>
            <div className="rounded-xl border border-rc-border bg-rc-surface-raised p-4">
              <p className="text-xs font-semibold uppercase tracking-wide text-rc-secondary">
                Base text
              </p>
              <p className="mt-2 font-rc-data text-2xl font-semibold text-rc-foreground">
                16px
              </p>
            </div>
          </div>
        </Card>
      </div>
    </main>
  )
}
