import { NavLink, Outlet, useNavigate } from 'react-router-dom'

import { useAuth } from '../../features/auth/useAuth'
import { classNames } from '../../lib/classNames'
import { Badge } from '../ui/Badge'
import { Button } from '../ui/Button'
import { getNavItemsForRole } from '../../routes/navConfig'

function getNavLinkClassName({ isActive }: { isActive: boolean }) {
  return classNames(
    'flex min-h-11 items-center justify-between gap-3 rounded-xl px-3 py-2 text-sm font-medium transition-colors focus-visible:outline-2 focus-visible:outline-offset-2 focus-visible:outline-rc-ring',
    isActive
      ? 'bg-rc-primary text-rc-on-primary shadow-sm'
      : 'text-rc-secondary hover:bg-rc-muted hover:text-rc-foreground',
  )
}

export function AppLayout() {
  const navigate = useNavigate()
  const { logout, user } = useAuth()
  const navItems = getNavItemsForRole(user?.role)

  const handleSignOut = () => {
    logout()
    navigate('/login', { replace: true })
  }

  return (
    <div className="min-h-screen bg-rc-background text-rc-foreground">
      <div className="lg:grid lg:min-h-screen lg:grid-cols-[17rem_1fr]">
        <aside className="hidden border-r border-rc-border bg-rc-surface/95 px-4 py-5 shadow-sm lg:flex lg:flex-col">
          <div className="space-y-1 px-2">
            <p className="text-xs font-semibold uppercase tracking-[0.22em] text-rc-accent">
              RetailCore
            </p>
            <h1 className="text-xl font-semibold tracking-tight">POS Console</h1>
          </div>

          <nav aria-label="Primary navigation" className="mt-8 flex flex-1 flex-col gap-1">
            {navItems.map((item) => (
              <NavLink className={getNavLinkClassName} key={item.id} to={item.path}>
                <span>{item.label}</span>
                <span className="font-rc-data text-xs opacity-70">{item.buildStep}</span>
              </NavLink>
            ))}
          </nav>

          <div className="mt-6 rounded-2xl border border-rc-border bg-rc-muted p-3">
            <p className="text-sm font-semibold text-rc-foreground">
              {user?.name ?? 'Session pending'}
            </p>
            <div className="mt-2 flex items-center justify-between gap-2">
              <Badge variant="info">{user?.role ?? 'No role'}</Badge>
              <Button onClick={handleSignOut} size="sm" variant="ghost">
                Sign out
              </Button>
            </div>
          </div>
        </aside>

        <div className="flex min-h-screen flex-col">
          <header className="sticky top-0 z-10 border-b border-rc-border bg-rc-surface/90 px-4 py-3 shadow-sm backdrop-blur lg:px-8">
            <div className="flex items-center justify-between gap-4">
              <div className="min-w-0">
                <p className="text-xs font-semibold uppercase tracking-[0.2em] text-rc-accent lg:hidden">
                  RetailCore POS
                </p>
                <p className="truncate text-sm text-rc-secondary">
                  Protected operations shell
                </p>
              </div>
              <div className="flex items-center gap-2">
                <Badge variant="neutral">{user?.role ?? 'No role'}</Badge>
                <Button onClick={handleSignOut} size="sm" variant="secondary">
                  Sign out
                </Button>
              </div>
            </div>
          </header>

          <main className="flex-1 px-4 py-6 pb-32 sm:px-6 lg:px-8 lg:pb-8">
            <div className="mx-auto max-w-7xl">
              <Outlet />
            </div>
          </main>

          <nav
            aria-label="Mobile primary navigation"
            className="fixed inset-x-0 bottom-0 z-20 grid border-t border-rc-border bg-rc-surface/95 px-2 py-2 shadow-[0_-8px_24px_rgb(15_23_42/0.08)] backdrop-blur lg:hidden"
            style={{ gridTemplateColumns: `repeat(${Math.min(Math.max(navItems.length, 1), 4)}, minmax(0, 1fr))` }}
          >
            {navItems.map((item) => (
              <NavLink
                className={({ isActive }) =>
                  classNames(
                    'min-h-11 rounded-lg px-2 py-2 text-center text-xs font-semibold transition-colors focus-visible:outline-2 focus-visible:outline-offset-2 focus-visible:outline-rc-ring',
                    isActive
                      ? 'bg-rc-primary text-rc-on-primary'
                      : 'text-rc-secondary hover:bg-rc-muted hover:text-rc-foreground',
                  )
                }
                key={item.id}
                to={item.path}
              >
                {item.label}
              </NavLink>
            ))}
          </nav>
        </div>
      </div>
    </div>
  )
}
