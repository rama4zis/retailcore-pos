import { useMemo, useState, type FormEvent } from 'react'

import { EmptyState } from '../../../components/feedback/EmptyState'
import { ErrorBanner } from '../../../components/feedback/ErrorBanner'
import { Skeleton } from '../../../components/feedback/Skeleton'
import { PageHeader } from '../../../components/layout/PageHeader'
import { Badge } from '../../../components/ui/Badge'
import { Button } from '../../../components/ui/Button'
import { Card } from '../../../components/ui/Card'
import { Input } from '../../../components/ui/Input'
import { Textarea } from '../../../components/ui/Textarea'
import {
  getApiErrorMessage,
  getApiFieldErrors,
  isForbiddenApiError,
} from '../../../lib/api/errors'
import type {
  CategoryCreateRequest,
  CategoryResponse,
  CategoryUpdateRequest,
} from '../../../lib/api/categories'
import { classNames } from '../../../lib/classNames'
import { formatDateTime } from '../../../lib/format/date'
import { nullableTrimmedText, requiredTrimmedText } from '../../catalog/formUtils'
import {
  useCategoriesQuery,
  useCreateCategoryMutation,
  useDeleteCategoryMutation,
  useUpdateCategoryMutation,
} from '../queries'

interface CategoryFormState {
  active: boolean
  description: string
  name: string
}

interface CategoryFormProps {
  editingCategory: CategoryResponse | null
  error: unknown
  fieldErrors: Record<string, string>
  form: CategoryFormState
  isPending: boolean
  onCancelEdit: () => void
  onChange: (form: CategoryFormState) => void
  onSubmit: (event: FormEvent<HTMLFormElement>) => void
}

interface CategoryTableProps {
  deletingCategoryId: number | null
  editingCategoryId: number | null
  items: CategoryResponse[]
  onDelete: (category: CategoryResponse) => void
  onEdit: (category: CategoryResponse) => void
}

const emptyCategoryForm: CategoryFormState = {
  active: true,
  description: '',
  name: '',
}

function mapCategoryToForm(category: CategoryResponse): CategoryFormState {
  return {
    active: category.active,
    description: category.description ?? '',
    name: category.name,
  }
}

function buildCreateRequest(form: CategoryFormState): CategoryCreateRequest {
  return {
    description: nullableTrimmedText(form.description),
    name: requiredTrimmedText(form.name),
  }
}

function buildUpdateRequest(form: CategoryFormState): CategoryUpdateRequest {
  return {
    active: form.active,
    description: nullableTrimmedText(form.description),
    name: requiredTrimmedText(form.name),
  }
}

function getCategoryErrorTitle(error: unknown) {
  return isForbiddenApiError(error) ? 'Access denied' : 'Category request failed'
}

function getCategoryErrorMessage(error: unknown) {
  if (isForbiddenApiError(error)) {
    return 'The backend refused this category request for the current account.'
  }

  return getApiErrorMessage(error, 'Category data could not be loaded.')
}

function CategoryTableSkeleton() {
  return (
    <div className="space-y-3" aria-label="Categories loading" role="status">
      {Array.from({ length: 5 }, (_, index) => (
        <div className="grid gap-3 rounded-xl border border-rc-border p-3 sm:grid-cols-[1fr_8rem_10rem]" key={index}>
          <Skeleton className="h-5" />
          <Skeleton className="h-5" />
          <Skeleton className="h-5" />
        </div>
      ))}
    </div>
  )
}

function CategoryForm({
  editingCategory,
  error,
  fieldErrors,
  form,
  isPending,
  onCancelEdit,
  onChange,
  onSubmit,
}: CategoryFormProps) {
  const isEditing = Boolean(editingCategory)

  return (
    <Card
      description={
        isEditing
          ? 'Update the selected category. Product rows refresh after category changes so names stay in sync.'
          : 'Create catalog groups used by the product form category selector.'
      }
      title={isEditing ? `Edit ${editingCategory?.name}` : 'Create category'}
    >
      <form className="space-y-4" onSubmit={onSubmit}>
        {error ? (
          <ErrorBanner
            message={getCategoryErrorMessage(error)}
            title={getCategoryErrorTitle(error)}
          />
        ) : null}

        <Input
          autoComplete="off"
          disabled={isPending}
          error={fieldErrors.name}
          label="Category name"
          maxLength={100}
          onChange={(event) => onChange({ ...form, name: event.target.value })}
          placeholder="Beverages"
          required
          value={form.name}
        />

        <Textarea
          disabled={isPending}
          error={fieldErrors.description}
          helperText="Optional. Maximum 500 characters."
          label="Description"
          maxLength={500}
          onChange={(event) => onChange({ ...form, description: event.target.value })}
          placeholder="Shelf grouping, department notes, or merchandising hint."
          value={form.description}
        />

        {isEditing ? (
          <label className="flex min-h-11 items-center gap-3 rounded-xl border border-rc-border bg-rc-muted px-3 py-2 text-sm font-medium text-rc-foreground">
            <input
              checked={form.active}
              className="size-4 accent-rc-accent"
              disabled={isPending}
              onChange={(event) => onChange({ ...form, active: event.target.checked })}
              type="checkbox"
            />
            Active category
            {fieldErrors.active ? (
              <span className="text-sm text-rc-destructive">{fieldErrors.active}</span>
            ) : null}
          </label>
        ) : null}

        <div className="flex flex-wrap gap-2">
          <Button
            isLoading={isPending}
            loadingText={isEditing ? 'Saving category' : 'Creating category'}
            type="submit"
            variant="success"
          >
            {isEditing ? 'Save category' : 'Create category'}
          </Button>
          {isEditing ? (
            <Button disabled={isPending} onClick={onCancelEdit} type="button" variant="secondary">
              Cancel edit
            </Button>
          ) : null}
        </div>
      </form>
    </Card>
  )
}

function CategoryTable({
  deletingCategoryId,
  editingCategoryId,
  items,
  onDelete,
  onEdit,
}: CategoryTableProps) {
  return (
    <div className="overflow-x-auto">
      <table className="min-w-full text-left text-sm">
        <thead className="border-b border-rc-border text-xs uppercase tracking-[0.16em] text-rc-secondary">
          <tr>
            <th className="pb-3 pr-4 font-semibold" scope="col">
              Category
            </th>
            <th className="pb-3 pr-4 font-semibold" scope="col">
              Status
            </th>
            <th className="pb-3 pr-4 font-semibold" scope="col">
              Updated
            </th>
            <th className="pb-3 text-right font-semibold" scope="col">
              Actions
            </th>
          </tr>
        </thead>
        <tbody className="divide-y divide-rc-border">
          {items.map((category) => (
            <tr
              className={classNames(
                'align-top transition-colors',
                editingCategoryId === category.id ? 'bg-rc-accent-muted/30' : undefined,
              )}
              key={category.id}
            >
              <td className="py-4 pr-4">
                <div className="font-medium text-rc-foreground">{category.name}</div>
                <p className="mt-1 max-w-xl text-sm leading-6 text-rc-secondary">
                  {category.description || 'No description'}
                </p>
              </td>
              <td className="py-4 pr-4">
                <Badge variant={category.active ? 'success' : 'neutral'}>
                  {category.active ? 'Active' : 'Inactive'}
                </Badge>
              </td>
              <td className="py-4 pr-4 font-rc-data text-xs text-rc-secondary">
                {formatDateTime(category.updatedAt)}
              </td>
              <td className="py-4 text-right">
                <div className="flex flex-wrap justify-end gap-2">
                  <Button onClick={() => onEdit(category)} size="sm" variant="secondary">
                    Edit
                  </Button>
                  <Button
                    isLoading={deletingCategoryId === category.id}
                    loadingText="Deleting"
                    onClick={() => onDelete(category)}
                    size="sm"
                    variant="danger"
                  >
                    Delete
                  </Button>
                </div>
              </td>
            </tr>
          ))}
        </tbody>
      </table>
    </div>
  )
}

export function CategoriesPage() {
  const categoriesQuery = useCategoriesQuery()
  const createCategoryMutation = useCreateCategoryMutation()
  const updateCategoryMutation = useUpdateCategoryMutation()
  const deleteCategoryMutation = useDeleteCategoryMutation()
  const [form, setForm] = useState<CategoryFormState>(emptyCategoryForm)
  const [editingCategory, setEditingCategory] = useState<CategoryResponse | null>(null)
  const [statusMessage, setStatusMessage] = useState<string | null>(null)
  const [deletingCategoryId, setDeletingCategoryId] = useState<number | null>(null)

  const categories = useMemo(
    () =>
      [...(categoriesQuery.data ?? [])].sort((firstCategory, secondCategory) =>
        firstCategory.name.localeCompare(secondCategory.name),
      ),
    [categoriesQuery.data],
  )
  const formMutation = editingCategory ? updateCategoryMutation : createCategoryMutation
  const fieldErrors = getApiFieldErrors(formMutation.error)
  const isFormPending = createCategoryMutation.isPending || updateCategoryMutation.isPending
  const isRefreshing = categoriesQuery.isFetching && !categoriesQuery.isLoading

  const resetForm = () => {
    setForm(emptyCategoryForm)
    setEditingCategory(null)
    createCategoryMutation.reset()
    updateCategoryMutation.reset()
  }

  const handleEdit = (category: CategoryResponse) => {
    createCategoryMutation.reset()
    updateCategoryMutation.reset()
    setStatusMessage(null)
    setEditingCategory(category)
    setForm(mapCategoryToForm(category))
  }

  const handleSubmit = async (event: FormEvent<HTMLFormElement>) => {
    event.preventDefault()
    setStatusMessage(null)

    try {
      if (editingCategory) {
        const updatedCategory = await updateCategoryMutation.mutateAsync({
          id: editingCategory.id,
          request: buildUpdateRequest(form),
        })
        setStatusMessage(`Category ${updatedCategory.name} saved.`)
        resetForm()
        return
      }

      const createdCategory = await createCategoryMutation.mutateAsync(buildCreateRequest(form))
      setStatusMessage(`Category ${createdCategory.name} created.`)
      resetForm()
    } catch {
      setStatusMessage(null)
    }
  }

  const handleDelete = async (category: CategoryResponse) => {
    deleteCategoryMutation.reset()
    setStatusMessage(null)

    const confirmed = window.confirm(
      `Delete category "${category.name}"? The backend will block this if products still use it.`,
    )

    if (!confirmed) {
      return
    }

    setDeletingCategoryId(category.id)

    try {
      await deleteCategoryMutation.mutateAsync(category.id)
      setStatusMessage(`Category ${category.name} deleted.`)

      if (editingCategory?.id === category.id) {
        resetForm()
      }
    } catch {
      setStatusMessage(null)
    } finally {
      setDeletingCategoryId(null)
    }
  }

  return (
    <div className="space-y-5">
      <PageHeader
        description="Create, edit, and delete category records through the real catalog API. Deletes show backend conflict messages when products still reference the category."
        eyebrow="FE-07 catalog management"
        meta={
          <>
            <Badge variant="success">ADMIN / MANAGER</Badge>
            <Badge variant="neutral">{categories.length} categories</Badge>
            {isRefreshing ? <Badge variant="info">Refreshing</Badge> : null}
          </>
        }
        title="Categories"
      />

      {statusMessage ? (
        <div
          aria-live="polite"
          className="rounded-xl border border-rc-accent/25 bg-rc-accent-muted px-4 py-3 text-sm font-medium text-rc-accent-strong"
        >
          {statusMessage}
        </div>
      ) : null}

      <div className="grid gap-5 xl:grid-cols-[0.78fr_1.22fr]">
        <CategoryForm
          editingCategory={editingCategory}
          error={formMutation.error}
          fieldErrors={fieldErrors}
          form={form}
          isPending={isFormPending}
          onCancelEdit={resetForm}
          onChange={setForm}
          onSubmit={handleSubmit}
        />

        <Card
          actions={isRefreshing ? <Badge variant="info">Refreshing</Badge> : null}
          description="Loaded from GET /api/categories. Product category selectors use this same data source."
          title="Category list"
        >
          {categoriesQuery.isLoading ? <CategoryTableSkeleton /> : null}

          {!categoriesQuery.isLoading && categoriesQuery.error ? (
            <ErrorBanner
              action={
                <Button onClick={() => void categoriesQuery.refetch()} size="sm" variant="secondary">
                  Retry
                </Button>
              }
              message={getCategoryErrorMessage(categoriesQuery.error)}
              title={getCategoryErrorTitle(categoriesQuery.error)}
            />
          ) : null}

          {!categoriesQuery.isLoading && deleteCategoryMutation.error ? (
            <ErrorBanner
              className="mb-4"
              message={getCategoryErrorMessage(deleteCategoryMutation.error)}
              title={getCategoryErrorTitle(deleteCategoryMutation.error)}
            />
          ) : null}

          {!categoriesQuery.isLoading && !categoriesQuery.error && categories.length === 0 ? (
            <EmptyState
              action={
                <Button onClick={() => setForm(emptyCategoryForm)} variant="success">
                  Start category form
                </Button>
              }
              description="No categories exist yet. Create one first so products can attach to a real category."
              title="No categories yet"
            />
          ) : null}

          {!categoriesQuery.isLoading && !categoriesQuery.error && categories.length > 0 ? (
            <CategoryTable
              deletingCategoryId={deletingCategoryId}
              editingCategoryId={editingCategory?.id ?? null}
              items={categories}
              onDelete={handleDelete}
              onEdit={handleEdit}
            />
          ) : null}
        </Card>
      </div>
    </div>
  )
}
