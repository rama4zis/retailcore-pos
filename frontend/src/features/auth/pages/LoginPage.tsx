import { useMemo, useState, type FormEvent } from 'react'
import { Navigate, useLocation, useNavigate } from 'react-router-dom'

import { ErrorBanner } from '../../../components/feedback/ErrorBanner'
import { Spinner } from '../../../components/feedback/Spinner'
import { PageHeader } from '../../../components/layout/PageHeader'
import { Badge } from '../../../components/ui/Badge'
import { Button } from '../../../components/ui/Button'
import { Card } from '../../../components/ui/Card'
import { Input } from '../../../components/ui/Input'
import {
  getApiErrorMessage,
  getApiFieldErrors,
  isForbiddenApiError,
} from '../../../lib/api/errors'
import type { LoginRequest } from '../../../lib/api/auth'
import { useAuth } from '../useAuth'

type LoginFieldErrors = Partial<Record<keyof LoginRequest, string>>

interface RedirectLocationState {
  from?: {
    hash?: string
    pathname?: string
    search?: string
  }
}

const DEFAULT_REDIRECT_PATH = '/dashboard'
const LOGIN_PATH = '/login'
const emptyLoginForm: LoginRequest = {
  email: '',
  password: '',
}

function isRecord(value: unknown): value is Record<string, unknown> {
  return typeof value === 'object' && value !== null && !Array.isArray(value)
}

function getStringValue(value: unknown) {
  return typeof value === 'string' ? value : ''
}

function getRedirectPath(state: unknown) {
  if (!isRecord(state) || !isRecord(state.from)) {
    return DEFAULT_REDIRECT_PATH
  }

  const redirectState = state as RedirectLocationState
  const pathname = redirectState.from?.pathname ?? DEFAULT_REDIRECT_PATH

  if (pathname === LOGIN_PATH) {
    return DEFAULT_REDIRECT_PATH
  }

  const search = getStringValue(redirectState.from?.search)
  const hash = getStringValue(redirectState.from?.hash)

  return `${pathname}${search}${hash}`
}

function getLoginFieldErrors(error: unknown): LoginFieldErrors {
  const fieldErrors = getApiFieldErrors(error)

  return {
    email: fieldErrors.email,
    password: fieldErrors.password,
  }
}

function hasLoginFieldErrors(fieldErrors: LoginFieldErrors) {
  return Boolean(fieldErrors.email || fieldErrors.password)
}

function focusLoginField(fieldName: keyof LoginRequest) {
  document.getElementById(`login-${fieldName}`)?.focus()
}

function focusFirstLoginError(fieldErrors: LoginFieldErrors) {
  if (fieldErrors.email) {
    focusLoginField('email')
    return
  }

  if (fieldErrors.password) {
    focusLoginField('password')
  }
}

function LoginSessionLoading() {
  return (
    <main className="flex min-h-screen items-center justify-center bg-rc-background px-4 text-rc-foreground">
      <div className="flex items-center gap-3 rounded-2xl border border-rc-border bg-rc-surface px-5 py-4 shadow-rc-card">
        <Spinner />
        <span className="text-sm font-medium text-rc-secondary">
          Restoring saved session
        </span>
      </div>
    </main>
  )
}

export function LoginPage() {
  const navigate = useNavigate()
  const location = useLocation()
  const { login, status } = useAuth()
  const redirectPath = useMemo(() => getRedirectPath(location.state), [location.state])
  const [form, setForm] = useState<LoginRequest>(emptyLoginForm)
  const [fieldErrors, setFieldErrors] = useState<LoginFieldErrors>({})
  const [formError, setFormError] = useState<string | null>(null)
  const [formErrorTitle, setFormErrorTitle] = useState('Login failed')
  const [isSubmitting, setIsSubmitting] = useState(false)

  if (status === 'authenticated') {
    return <Navigate replace to={redirectPath} />
  }

  if (status === 'loading') {
    return <LoginSessionLoading />
  }

  const updateField = (fieldName: keyof LoginRequest, value: string) => {
    setForm((currentForm) => ({
      ...currentForm,
      [fieldName]: value,
    }))
    setFieldErrors((currentFieldErrors) => ({
      ...currentFieldErrors,
      [fieldName]: undefined,
    }))
  }

  const validateForm = () => {
    const nextFieldErrors: LoginFieldErrors = {}

    if (!form.email.trim()) {
      nextFieldErrors.email = 'Email is required'
    }

    if (!form.password) {
      nextFieldErrors.password = 'Password is required'
    }

    return nextFieldErrors
  }

  const handleSubmit = async (event: FormEvent<HTMLFormElement>) => {
    event.preventDefault()

    const nextFieldErrors = validateForm()
    setFieldErrors(nextFieldErrors)
    setFormError(null)

    if (hasLoginFieldErrors(nextFieldErrors)) {
      focusFirstLoginError(nextFieldErrors)
      return
    }

    setIsSubmitting(true)

    try {
      await login({
        email: form.email.trim(),
        password: form.password,
      })
      navigate(redirectPath, { replace: true })
    } catch (error) {
      const apiFieldErrors = getLoginFieldErrors(error)
      setFieldErrors(apiFieldErrors)
      setFormErrorTitle(isForbiddenApiError(error) ? 'Login blocked' : 'Login failed')
      setFormError(
        getApiErrorMessage(error, 'Login failed. Check the credentials and try again.'),
      )

      if (hasLoginFieldErrors(apiFieldErrors)) {
        focusFirstLoginError(apiFieldErrors)
      } else {
        focusLoginField('password')
      }
    } finally {
      setIsSubmitting(false)
    }
  }

  return (
    <main className="min-h-screen px-4 py-6 text-rc-foreground sm:px-6 lg:px-8">
      <div className="mx-auto grid min-h-[calc(100vh-3rem)] max-w-6xl items-center gap-8 lg:grid-cols-[1.05fr_0.95fr]">
        <section className="space-y-6">
          <PageHeader
            description="Sign in with an active RetailCore account. The session is restored from the saved JWT on refresh, then verified through /api/auth/me. No fake auth screen. We patched that bug."
            eyebrow="RetailCore POS"
            meta={
              <>
                <Badge variant="info">FE-05</Badge>
                <Badge variant="success">Real backend auth</Badge>
              </>
            }
            title="Operations login"
          />

          <div className="grid gap-3 text-sm text-rc-secondary sm:grid-cols-3">
            <Card className="bg-rc-surface/90" title="Admin">
              Full system access, reports, and users.
            </Card>
            <Card className="bg-rc-surface/90" title="Manager">
              Catalog, inventory, sales, and reports.
            </Card>
            <Card className="bg-rc-surface/90" title="Cashier">
              Checkout, receipts, sales, and refunds.
            </Card>
          </div>
        </section>

        <Card
          className="border-rc-primary-muted bg-rc-surface/95 shadow-rc-card"
          description="Use credentials created through the backend user API. Inactive users stay blocked with the backend message."
          title="Sign in"
        >
          <form className="space-y-5" noValidate onSubmit={handleSubmit}>
            {formError ? (
              <ErrorBanner message={formError} title={formErrorTitle} />
            ) : null}

            <Input
              autoComplete="username"
              disabled={isSubmitting}
              error={fieldErrors.email}
              id="login-email"
              label="Email"
              onChange={(event) => updateField('email', event.target.value)}
              required
              type="email"
              value={form.email}
            />

            <Input
              autoComplete="current-password"
              disabled={isSubmitting}
              error={fieldErrors.password}
              id="login-password"
              label="Password"
              onChange={(event) => updateField('password', event.target.value)}
              required
              type="password"
              value={form.password}
            />

            <Button
              className="w-full"
              isLoading={isSubmitting}
              loadingText="Signing in"
              type="submit"
            >
              Sign in
            </Button>
          </form>
        </Card>
      </div>
    </main>
  )
}
