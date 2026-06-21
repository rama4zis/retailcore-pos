import { type FormEvent, useMemo, useState } from 'react'

import { EmptyState } from '../../../components/feedback/EmptyState'
import { ErrorBanner } from '../../../components/feedback/ErrorBanner'
import { Skeleton } from '../../../components/feedback/Skeleton'
import { PageHeader } from '../../../components/layout/PageHeader'
import { Badge } from '../../../components/ui/Badge'
import { Button } from '../../../components/ui/Button'
import { Card } from '../../../components/ui/Card'
import { Input } from '../../../components/ui/Input'
import { Select } from '../../../components/ui/Select'
import {
  getApiErrorMessage,
  getApiFieldErrors,
  isForbiddenApiError,
  type ApiFieldErrorMap,
} from '../../../lib/api/errors'
import type { UserCreateRequest, UserResponse, UserRole } from '../../../lib/api/users'
import { formatDateTime } from '../../../lib/format/date'
import {
  useChangeUserActiveMutation,
  useChangeUserRoleMutation,
  useCreateUserMutation,
  useUsersQuery,
} from '../queries'

const roles: UserRole[] = ['ADMIN', 'MANAGER', 'CASHIER']

interface UserFormState {
  active: boolean
  email: string
  name: string
  password: string
  role: UserRole
}

const initialFormState: UserFormState = {
  active: true,
  email: '',
  name: '',
  password: '',
  role: 'CASHIER',
}

function roleBadgeVariant(role: UserRole) {
  if (role === 'ADMIN') {
    return 'danger'
  }

  if (role === 'MANAGER') {
    return 'info'
  }

  return 'neutral'
}

function getPageErrorTitle(error: unknown) {
  return isForbiddenApiError(error) ? 'Access denied' : 'Users unavailable'
}

function getPageErrorMessage(error: unknown) {
  if (isForbiddenApiError(error)) {
    return 'User management is restricted to ADMIN users.'
  }

  return getApiErrorMessage(error, 'The user list could not be loaded.')
}

function UserTableSkeleton() {
  return (
    <div className="space-y-3" aria-label="Users loading" role="status">
      {Array.from({ length: 5 }, (_, index) => (
        <div className="grid gap-3 rounded-xl border border-rc-border p-4 md:grid-cols-[1.4fr_0.7fr_0.7fr_1fr]" key={index}>
          <Skeleton className="h-5" />
          <Skeleton className="h-5" />
          <Skeleton className="h-5" />
          <Skeleton className="h-9" />
        </div>
      ))}
    </div>
  )
}

function CreateUserForm() {
  const createUserMutation = useCreateUserMutation()
  const [formState, setFormState] = useState<UserFormState>(initialFormState)
  const fieldErrors = getApiFieldErrors(createUserMutation.error)

  function updateField<K extends keyof UserFormState>(field: K, value: UserFormState[K]) {
    setFormState((current) => ({ ...current, [field]: value }))
  }

  function handleSubmit(event: FormEvent<HTMLFormElement>) {
    event.preventDefault()

    const request: UserCreateRequest = {
      active: formState.active,
      email: formState.email.trim(),
      name: formState.name.trim(),
      password: formState.password,
      role: formState.role,
    }

    createUserMutation.mutate(request, {
      onSuccess: () => {
        setFormState(initialFormState)
      },
    })
  }

  return (
    <Card
      description="Creates an account through POST /api/users. Passwords are sent only on submit and cleared after a successful create."
      title="Create user"
    >
      <form className="space-y-4" onSubmit={handleSubmit}>
        <div className="grid gap-4 lg:grid-cols-2">
          <Input
            autoComplete="off"
            disabled={createUserMutation.isPending}
            error={fieldErrors.email}
            label="Email"
            maxLength={160}
            onChange={(event) => updateField('email', event.target.value)}
            required
            type="email"
            value={formState.email}
          />
          <Input
            autoComplete="off"
            disabled={createUserMutation.isPending}
            error={fieldErrors.name}
            label="Name"
            maxLength={160}
            onChange={(event) => updateField('name', event.target.value)}
            required
            value={formState.name}
          />
        </div>
        <div className="grid gap-4 lg:grid-cols-[1fr_12rem_12rem]">
          <Input
            autoComplete="new-password"
            disabled={createUserMutation.isPending}
            error={fieldErrors.password}
            helperText="8 to 72 characters. The password is never displayed after submission."
            label="Temporary password"
            maxLength={72}
            minLength={8}
            onChange={(event) => updateField('password', event.target.value)}
            required
            type="password"
            value={formState.password}
          />
          <Select
            disabled={createUserMutation.isPending}
            error={fieldErrors.role}
            label="Role"
            onChange={(event) => updateField('role', event.target.value as UserRole)}
            value={formState.role}
          >
            {roles.map((role) => (
              <option key={role} value={role}>{role}</option>
            ))}
          </Select>
          <Select
            disabled={createUserMutation.isPending}
            error={fieldErrors.active}
            label="Status"
            onChange={(event) => updateField('active', event.target.value === 'true')}
            value={String(formState.active)}
          >
            <option value="true">Active</option>
            <option value="false">Inactive</option>
          </Select>
        </div>

        {createUserMutation.error ? (
          <ErrorBanner message={getApiErrorMessage(createUserMutation.error, 'User could not be created.')} title="Create failed" />
        ) : null}
        {createUserMutation.isSuccess ? (
          <div aria-live="polite">
            <Badge variant="success">User created</Badge>
          </div>
        ) : null}

        <div className="flex justify-end">
          <Button isLoading={createUserMutation.isPending} loadingText="Creating" type="submit">
            Create user
          </Button>
        </div>
      </form>
    </Card>
  )
}

function RoleSelect({
  disabled,
  fieldErrors,
  onChangeRole,
  user,
}: {
  disabled: boolean
  fieldErrors: ApiFieldErrorMap
  onChangeRole: (user: UserResponse, role: UserRole) => void
  user: UserResponse
}) {
  return (
    <Select
      disabled={disabled}
      error={fieldErrors.role}
      hideLabel
      label={`Role for ${user.name}`}
      onChange={(event) => onChangeRole(user, event.target.value as UserRole)}
      value={user.role}
    >
      {roles.map((role) => (
        <option key={role} value={role}>{role}</option>
      ))}
    </Select>
  )
}

function UsersTable({ users }: { users: UserResponse[] }) {
  const changeRoleMutation = useChangeUserRoleMutation()
  const changeActiveMutation = useChangeUserActiveMutation()
  const roleFieldErrors = getApiFieldErrors(changeRoleMutation.error)
  const activeFieldErrors = getApiFieldErrors(changeActiveMutation.error)
  const sortedUsers = useMemo(
    () => [...users].sort((a, b) => a.name.localeCompare(b.name)),
    [users],
  )

  function handleChangeRole(user: UserResponse, role: UserRole) {
    if (role === user.role) {
      return
    }

    changeRoleMutation.mutate({ id: user.id, request: { role } })
  }

  function handleToggleActive(user: UserResponse) {
    changeActiveMutation.mutate({ id: user.id, request: { active: !user.active } })
  }

  const mutationError = changeRoleMutation.error ?? changeActiveMutation.error

  return (
    <Card
      description="Update roles with PATCH /api/users/{id}/role and active status with PATCH /api/users/{id}/active."
      title="User accounts"
    >
      {mutationError ? (
        <ErrorBanner className="mb-4" message={getApiErrorMessage(mutationError, 'The user update failed.')} title="Update failed" />
      ) : null}
      <div className="overflow-x-auto">
        <table className="min-w-full text-left text-sm">
          <thead className="border-b border-rc-border text-xs uppercase tracking-[0.16em] text-rc-secondary">
            <tr>
              <th className="pb-3 pr-4 font-semibold" scope="col">User</th>
              <th className="pb-3 pr-4 font-semibold" scope="col">Role</th>
              <th className="pb-3 pr-4 font-semibold" scope="col">Status</th>
              <th className="pb-3 pr-4 font-semibold" scope="col">Created</th>
              <th className="pb-3 text-right font-semibold" scope="col">Actions</th>
            </tr>
          </thead>
          <tbody className="divide-y divide-rc-border">
            {sortedUsers.map((user) => {
              const rolePending = changeRoleMutation.isPending && changeRoleMutation.variables?.id === user.id
              const activePending = changeActiveMutation.isPending && changeActiveMutation.variables?.id === user.id

              return (
                <tr key={user.id}>
                  <td className="py-3 pr-4 align-top">
                    <div className="font-medium text-rc-foreground">{user.name}</div>
                    <div className="text-xs text-rc-secondary">{user.email}</div>
                  </td>
                  <td className="min-w-44 py-3 pr-4 align-top">
                    <div className="space-y-2">
                      <Badge variant={roleBadgeVariant(user.role)}>{user.role}</Badge>
                      <RoleSelect
                        disabled={rolePending || activePending}
                        fieldErrors={rolePending ? roleFieldErrors : {}}
                        onChangeRole={handleChangeRole}
                        user={user}
                      />
                    </div>
                  </td>
                  <td className="py-3 pr-4 align-top">
                    <Badge variant={user.active ? 'success' : 'danger'}>{user.active ? 'Active' : 'Inactive'}</Badge>
                  </td>
                  <td className="py-3 pr-4 align-top font-rc-data text-xs text-rc-secondary">
                    {formatDateTime(user.createdAt)}
                  </td>
                  <td className="py-3 text-right align-top">
                    <Button
                      isLoading={activePending}
                      loadingText="Saving"
                      onClick={() => handleToggleActive(user)}
                      size="sm"
                      variant={user.active ? 'danger' : 'success'}
                    >
                      {user.active ? 'Deactivate' : 'Activate'}
                    </Button>
                    {activePending && activeFieldErrors.active ? (
                      <p className="mt-2 text-sm font-medium text-rc-destructive">{activeFieldErrors.active}</p>
                    ) : null}
                  </td>
                </tr>
              )
            })}
          </tbody>
        </table>
      </div>
    </Card>
  )
}

export function UsersPage() {
  const usersQuery = useUsersQuery()

  return (
    <div className="space-y-5">
      <PageHeader
        description="Admin-only account administration backed by /api/users. Manage user creation, role changes, and active status without exposing submitted passwords."
        eyebrow="FE-12 admin users"
        meta={
          <>
            <Badge variant="success">ADMIN</Badge>
            <Badge variant="info">4 user endpoints</Badge>
            {usersQuery.isFetching && !usersQuery.isLoading ? <Badge variant="info">Refreshing</Badge> : null}
          </>
        }
        title="User management"
      />

      <CreateUserForm />

      {usersQuery.isLoading ? <UserTableSkeleton /> : null}
      {!usersQuery.isLoading && usersQuery.error ? (
        <ErrorBanner
          action={<Button onClick={() => void usersQuery.refetch()} size="sm" variant="secondary">Retry</Button>}
          message={getPageErrorMessage(usersQuery.error)}
          title={getPageErrorTitle(usersQuery.error)}
        />
      ) : null}
      {!usersQuery.isLoading && !usersQuery.error && usersQuery.data?.length === 0 ? (
        <EmptyState description="No users are available yet. Create the first operational account above." title="No users found" />
      ) : null}
      {!usersQuery.isLoading && !usersQuery.error && usersQuery.data && usersQuery.data.length > 0 ? (
        <UsersTable users={usersQuery.data} />
      ) : null}
    </div>
  )
}
