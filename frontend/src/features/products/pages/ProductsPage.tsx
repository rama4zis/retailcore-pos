import { useMemo, useState, type FormEvent } from 'react'
import { Link } from 'react-router-dom'

import { EmptyState } from '../../../components/feedback/EmptyState'
import { ErrorBanner } from '../../../components/feedback/ErrorBanner'
import { Skeleton } from '../../../components/feedback/Skeleton'
import { PageHeader } from '../../../components/layout/PageHeader'
import { Badge } from '../../../components/ui/Badge'
import { Button } from '../../../components/ui/Button'
import { Card } from '../../../components/ui/Card'
import { Input } from '../../../components/ui/Input'
import { Select } from '../../../components/ui/Select'
import { Textarea } from '../../../components/ui/Textarea'
import type { CategoryResponse } from '../../../lib/api/categories'
import {
  getApiErrorMessage,
  getApiFieldErrors,
  isForbiddenApiError,
} from '../../../lib/api/errors'
import type {
  ProductCreateRequest,
  ProductResponse,
  ProductUpdateRequest,
} from '../../../lib/api/products'
import { classNames } from '../../../lib/classNames'
import { formatCurrency } from '../../../lib/format/currency'
import { formatDateTime } from '../../../lib/format/date'
import { nullableTrimmedText, requiredTrimmedText } from '../../catalog/formUtils'
import { useCategoriesQuery } from '../../categories/queries'
import {
  useChangeProductActiveMutation,
  useCreateProductMutation,
  useProductsQuery,
  useUpdateProductMutation,
} from '../queries'

interface ProductFormState {
  active: boolean
  barcode: string
  categoryId: string
  description: string
  name: string
  price: string
  sku: string
}

interface ProductFormProps {
  categories: CategoryResponse[]
  editingProduct: ProductResponse | null
  error: unknown
  fieldErrors: Record<string, string>
  form: ProductFormState
  isPending: boolean
  onCancelEdit: () => void
  onChange: (form: ProductFormState) => void
  onSubmit: (event: FormEvent<HTMLFormElement>) => void
}

interface ProductTableProps {
  editingProductId: number | null
  items: ProductResponse[]
  onEdit: (product: ProductResponse) => void
  onToggleActive: (product: ProductResponse) => void
  togglingProductId: number | null
}

const emptyProductForm: ProductFormState = {
  active: true,
  barcode: '',
  categoryId: '',
  description: '',
  name: '',
  price: '',
  sku: '',
}

function mapProductToForm(product: ProductResponse): ProductFormState {
  return {
    active: product.active,
    barcode: product.barcode ?? '',
    categoryId: product.categoryId.toString(),
    description: product.description ?? '',
    name: product.name,
    price: product.price.toString(),
    sku: product.sku,
  }
}

function buildCreateRequest(form: ProductFormState): ProductCreateRequest {
  return {
    active: form.active,
    barcode: nullableTrimmedText(form.barcode),
    categoryId: Number(form.categoryId),
    description: nullableTrimmedText(form.description),
    name: requiredTrimmedText(form.name),
    price: Number(form.price),
    sku: requiredTrimmedText(form.sku),
  }
}

function buildUpdateRequest(form: ProductFormState): ProductUpdateRequest {
  return {
    active: form.active,
    barcode: nullableTrimmedText(form.barcode),
    categoryId: Number(form.categoryId),
    description: nullableTrimmedText(form.description),
    name: requiredTrimmedText(form.name),
    price: Number(form.price),
    sku: requiredTrimmedText(form.sku),
  }
}

function getProductErrorTitle(error: unknown) {
  return isForbiddenApiError(error) ? 'Access denied' : 'Product request failed'
}

function getProductErrorMessage(error: unknown) {
  if (isForbiddenApiError(error)) {
    return 'The backend refused this product request for the current account.'
  }

  return getApiErrorMessage(error, 'Product data could not be loaded.')
}

function getCategoryOptionLabel(category: CategoryResponse) {
  return category.active ? category.name : `${category.name} (inactive)`
}

function matchesProductSearch(product: ProductResponse, query: string) {
  const normalizedQuery = query.trim().toLowerCase()

  if (!normalizedQuery) {
    return true
  }

  return [product.name, product.sku, product.barcode, product.categoryName]
    .filter((value): value is string => Boolean(value))
    .some((value) => value.toLowerCase().includes(normalizedQuery))
}

function ProductTableSkeleton() {
  return (
    <div className="space-y-3" aria-label="Products loading" role="status">
      {Array.from({ length: 6 }, (_, index) => (
        <div
          className="grid gap-3 rounded-xl border border-rc-border p-3 lg:grid-cols-[1.4fr_0.8fr_0.8fr_0.7fr_0.9fr]"
          key={index}
        >
          <Skeleton className="h-5" />
          <Skeleton className="h-5" />
          <Skeleton className="h-5" />
          <Skeleton className="h-5" />
          <Skeleton className="h-5" />
        </div>
      ))}
    </div>
  )
}

function NoCategoryCallout() {
  return (
    <ErrorBanner
      action={
        <Link
          className="inline-flex min-h-9 items-center justify-center rounded-lg border border-rc-border bg-rc-surface px-3 text-sm font-semibold text-rc-primary shadow-sm transition-colors hover:bg-rc-muted focus-visible:outline-2 focus-visible:outline-offset-2 focus-visible:outline-rc-ring"
          to="/categories"
        >
          Create category
        </Link>
      }
      message="Products require a real category. Create one first; no fake dropdown options are getting spawned here."
      title="No categories available"
    />
  )
}

function ProductForm({
  categories,
  editingProduct,
  error,
  fieldErrors,
  form,
  isPending,
  onCancelEdit,
  onChange,
  onSubmit,
}: ProductFormProps) {
  const isEditing = Boolean(editingProduct)
  const hasCategories = categories.length > 0
  const isSubmitDisabled = isPending || !hasCategories

  return (
    <Card
      description={
        isEditing
          ? 'Update SKU, category, pricing, and active status for an existing catalog item.'
          : 'Create a product using real category options from GET /api/categories.'
      }
      title={isEditing ? `Edit ${editingProduct?.name}` : 'Create product'}
    >
      <form className="space-y-4" onSubmit={onSubmit}>
        {!hasCategories ? <NoCategoryCallout /> : null}
        {error ? (
          <ErrorBanner
            message={getProductErrorMessage(error)}
            title={getProductErrorTitle(error)}
          />
        ) : null}

        <Select
          disabled={isPending || !hasCategories}
          error={fieldErrors.categoryId}
          label="Category"
          onChange={(event) => onChange({ ...form, categoryId: event.target.value })}
          required
          value={form.categoryId}
        >
          <option value="">Select category</option>
          {categories.map((category) => (
            <option key={category.id} value={category.id}>
              {getCategoryOptionLabel(category)}
            </option>
          ))}
        </Select>

        <div className="grid gap-4 sm:grid-cols-2">
          <Input
            autoComplete="off"
            disabled={isPending}
            error={fieldErrors.sku}
            label="SKU"
            maxLength={80}
            onChange={(event) => onChange({ ...form, sku: event.target.value })}
            placeholder="SKU-001"
            required
            value={form.sku}
          />
          <Input
            autoComplete="off"
            disabled={isPending}
            error={fieldErrors.barcode}
            helperText="Optional. Must be unique when provided."
            label="Barcode"
            maxLength={80}
            onChange={(event) => onChange({ ...form, barcode: event.target.value })}
            placeholder="0123456789012"
            value={form.barcode}
          />
        </div>

        <Input
          autoComplete="off"
          disabled={isPending}
          error={fieldErrors.name}
          label="Product name"
          maxLength={160}
          onChange={(event) => onChange({ ...form, name: event.target.value })}
          placeholder="Sparkling water"
          required
          value={form.name}
        />

        <Input
          disabled={isPending}
          error={fieldErrors.price}
          label="Price"
          min="0.01"
          onChange={(event) => onChange({ ...form, price: event.target.value })}
          placeholder="3.50"
          required
          step="0.01"
          type="number"
          value={form.price}
        />

        <Textarea
          disabled={isPending}
          error={fieldErrors.description}
          helperText="Optional. Maximum 1000 characters."
          label="Description"
          maxLength={1000}
          onChange={(event) => onChange({ ...form, description: event.target.value })}
          placeholder="Register-facing item notes or shelf description."
          value={form.description}
        />

        <label className="flex min-h-11 items-center gap-3 rounded-xl border border-rc-border bg-rc-muted px-3 py-2 text-sm font-medium text-rc-foreground">
          <input
            checked={form.active}
            className="size-4 accent-rc-accent"
            disabled={isPending}
            onChange={(event) => onChange({ ...form, active: event.target.checked })}
            type="checkbox"
          />
          Active product
          {fieldErrors.active ? (
            <span className="text-sm text-rc-destructive">{fieldErrors.active}</span>
          ) : null}
        </label>

        <div className="flex flex-wrap gap-2">
          <Button
            disabled={isSubmitDisabled}
            isLoading={isPending}
            loadingText={isEditing ? 'Saving product' : 'Creating product'}
            type="submit"
            variant="success"
          >
            {isEditing ? 'Save product' : 'Create product'}
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

function ProductTable({
  editingProductId,
  items,
  onEdit,
  onToggleActive,
  togglingProductId,
}: ProductTableProps) {
  return (
    <div className="overflow-x-auto">
      <table className="min-w-full text-left text-sm">
        <thead className="border-b border-rc-border text-xs uppercase tracking-[0.16em] text-rc-secondary">
          <tr>
            <th className="pb-3 pr-4 font-semibold" scope="col">
              Product
            </th>
            <th className="pb-3 pr-4 font-semibold" scope="col">
              Category
            </th>
            <th className="pb-3 pr-4 font-semibold" scope="col">
              Price
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
          {items.map((product) => (
            <tr
              className={classNames(
                'align-top transition-colors',
                editingProductId === product.id ? 'bg-rc-accent-muted/30' : undefined,
              )}
              key={product.id}
            >
              <td className="py-4 pr-4">
                <div className="font-medium text-rc-foreground">{product.name}</div>
                <div className="mt-1 flex flex-wrap gap-2 text-xs text-rc-secondary">
                  <span className="font-rc-data">SKU {product.sku}</span>
                  {product.barcode ? (
                    <span className="font-rc-data">Barcode {product.barcode}</span>
                  ) : null}
                </div>
                {product.description ? (
                  <p className="mt-2 max-w-xl text-sm leading-6 text-rc-secondary">
                    {product.description}
                  </p>
                ) : null}
              </td>
              <td className="py-4 pr-4 text-rc-foreground">{product.categoryName}</td>
              <td className="py-4 pr-4 font-rc-data font-semibold text-rc-foreground">
                {formatCurrency(product.price)}
              </td>
              <td className="py-4 pr-4">
                <Badge variant={product.active ? 'success' : 'neutral'}>
                  {product.active ? 'Active' : 'Inactive'}
                </Badge>
              </td>
              <td className="py-4 pr-4 font-rc-data text-xs text-rc-secondary">
                {formatDateTime(product.updatedAt)}
              </td>
              <td className="py-4 text-right">
                <div className="flex flex-wrap justify-end gap-2">
                  <Button onClick={() => onEdit(product)} size="sm" variant="secondary">
                    Edit
                  </Button>
                  <Button
                    isLoading={togglingProductId === product.id}
                    loadingText="Saving"
                    onClick={() => onToggleActive(product)}
                    size="sm"
                    variant={product.active ? 'ghost' : 'success'}
                  >
                    {product.active ? 'Disable' : 'Enable'}
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

export function ProductsPage() {
  const productsQuery = useProductsQuery()
  const categoriesQuery = useCategoriesQuery()
  const createProductMutation = useCreateProductMutation()
  const updateProductMutation = useUpdateProductMutation()
  const changeProductActiveMutation = useChangeProductActiveMutation()
  const [form, setForm] = useState<ProductFormState>(emptyProductForm)
  const [editingProduct, setEditingProduct] = useState<ProductResponse | null>(null)
  const [searchQuery, setSearchQuery] = useState('')
  const [statusMessage, setStatusMessage] = useState<string | null>(null)
  const [togglingProductId, setTogglingProductId] = useState<number | null>(null)

  const categories = useMemo(
    () =>
      [...(categoriesQuery.data ?? [])].sort((firstCategory, secondCategory) =>
        firstCategory.name.localeCompare(secondCategory.name),
      ),
    [categoriesQuery.data],
  )
  const products = useMemo(
    () =>
      [...(productsQuery.data ?? [])].sort((firstProduct, secondProduct) =>
        firstProduct.name.localeCompare(secondProduct.name),
      ),
    [productsQuery.data],
  )
  const filteredProducts = useMemo(
    () => products.filter((product) => matchesProductSearch(product, searchQuery)),
    [products, searchQuery],
  )
  const formMutation = editingProduct ? updateProductMutation : createProductMutation
  const fieldErrors = getApiFieldErrors(formMutation.error)
  const isFormPending = createProductMutation.isPending || updateProductMutation.isPending
  const isInitialLoading = productsQuery.isLoading || categoriesQuery.isLoading
  const queryError = productsQuery.error ?? categoriesQuery.error
  const isRefreshing =
    (productsQuery.isFetching && !productsQuery.isLoading) ||
    (categoriesQuery.isFetching && !categoriesQuery.isLoading)

  const resetForm = () => {
    setForm(emptyProductForm)
    setEditingProduct(null)
    createProductMutation.reset()
    updateProductMutation.reset()
  }

  const handleEdit = (product: ProductResponse) => {
    createProductMutation.reset()
    updateProductMutation.reset()
    setStatusMessage(null)
    setEditingProduct(product)
    setForm(mapProductToForm(product))
  }

  const handleSubmit = async (event: FormEvent<HTMLFormElement>) => {
    event.preventDefault()
    setStatusMessage(null)

    try {
      if (editingProduct) {
        const updatedProduct = await updateProductMutation.mutateAsync({
          id: editingProduct.id,
          request: buildUpdateRequest(form),
        })
        setStatusMessage(`Product ${updatedProduct.name} saved.`)
        resetForm()
        return
      }

      const createdProduct = await createProductMutation.mutateAsync(buildCreateRequest(form))
      setStatusMessage(`Product ${createdProduct.name} created.`)
      resetForm()
    } catch {
      setStatusMessage(null)
    }
  }

  const handleToggleActive = async (product: ProductResponse) => {
    changeProductActiveMutation.reset()
    setStatusMessage(null)
    setTogglingProductId(product.id)

    try {
      const updatedProduct = await changeProductActiveMutation.mutateAsync({
        id: product.id,
        request: { active: !product.active },
      })
      setStatusMessage(
        `Product ${updatedProduct.name} ${updatedProduct.active ? 'enabled' : 'disabled'}.`,
      )

      if (editingProduct?.id === product.id) {
        setEditingProduct(updatedProduct)
        setForm(mapProductToForm(updatedProduct))
      }
    } catch {
      setStatusMessage(null)
    } finally {
      setTogglingProductId(null)
    }
  }

  return (
    <div className="space-y-5">
      <PageHeader
        description="Create products, edit catalog details, and toggle active status through the real products API. Category options come from live category data."
        eyebrow="FE-07 catalog management"
        meta={
          <>
            <Badge variant="success">ADMIN / MANAGER</Badge>
            <Badge variant="neutral">{products.length} products</Badge>
            <Badge variant="neutral">{categories.length} categories</Badge>
            {isRefreshing ? <Badge variant="info">Refreshing</Badge> : null}
          </>
        }
        title="Products"
      />

      {statusMessage ? (
        <div
          aria-live="polite"
          className="rounded-xl border border-rc-accent/25 bg-rc-accent-muted px-4 py-3 text-sm font-medium text-rc-accent-strong"
        >
          {statusMessage}
        </div>
      ) : null}

      <div className="grid gap-5 2xl:grid-cols-[0.82fr_1.18fr]">
        <ProductForm
          categories={categories}
          editingProduct={editingProduct}
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
          description="Loaded from GET /api/products. Search filters loaded rows only; no fake pagination contract invented."
          title="Product list"
        >
          <div className="mb-4">
            <Input
              disabled={isInitialLoading}
              label="Search products"
              onChange={(event) => setSearchQuery(event.target.value)}
              placeholder="Search name, SKU, barcode, or category"
              type="search"
              value={searchQuery}
            />
          </div>

          {isInitialLoading ? <ProductTableSkeleton /> : null}

          {!isInitialLoading && queryError ? (
            <ErrorBanner
              action={
                <Button
                  onClick={() => {
                    void productsQuery.refetch()
                    void categoriesQuery.refetch()
                  }}
                  size="sm"
                  variant="secondary"
                >
                  Retry
                </Button>
              }
              message={getProductErrorMessage(queryError)}
              title={getProductErrorTitle(queryError)}
            />
          ) : null}

          {!isInitialLoading && changeProductActiveMutation.error ? (
            <ErrorBanner
              className="mb-4"
              message={getProductErrorMessage(changeProductActiveMutation.error)}
              title={getProductErrorTitle(changeProductActiveMutation.error)}
            />
          ) : null}

          {!isInitialLoading && !queryError && products.length === 0 ? (
            <EmptyState
              action={
                <Button disabled={categories.length === 0} onClick={() => setForm(emptyProductForm)} variant="success">
                  Start product form
                </Button>
              }
              description="No products exist yet. Create one with a real category, SKU, and price before checkout gets unlocked."
              title="No products yet"
            />
          ) : null}

          {!isInitialLoading && !queryError && products.length > 0 && filteredProducts.length === 0 ? (
            <EmptyState
              action={
                <Button onClick={() => setSearchQuery('')} variant="secondary">
                  Clear search
                </Button>
              }
              description="Loaded products exist, but none match this search. Your query whiffed. Happens."
              title="No matching products"
            />
          ) : null}

          {!isInitialLoading && !queryError && filteredProducts.length > 0 ? (
            <ProductTable
              editingProductId={editingProduct?.id ?? null}
              items={filteredProducts}
              onEdit={handleEdit}
              onToggleActive={handleToggleActive}
              togglingProductId={togglingProductId}
            />
          ) : null}
        </Card>
      </div>
    </div>
  )
}
