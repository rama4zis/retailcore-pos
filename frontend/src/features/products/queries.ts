import { useMutation, useQuery, useQueryClient } from '@tanstack/react-query'

import {
  changeProductActive,
  createProduct,
  listProducts,
  updateProduct,
  type ProductActiveRequest,
  type ProductCreateRequest,
  type ProductUpdateRequest,
} from '../../lib/api/products'

interface UpdateProductVariables {
  id: number
  request: ProductUpdateRequest
}

interface ChangeProductActiveVariables {
  id: number
  request: ProductActiveRequest
}

export const productQueryKeys = {
  all: ['products'] as const,
  detail: (id: number) => [...productQueryKeys.all, id] as const,
  list: () => [...productQueryKeys.all, 'list'] as const,
}

export function useProductsQuery() {
  return useQuery({
    queryFn: listProducts,
    queryKey: productQueryKeys.list(),
  })
}

export function useCreateProductMutation() {
  const queryClient = useQueryClient()

  return useMutation({
    mutationFn: (request: ProductCreateRequest) => createProduct(request),
    onSuccess: () => {
      void queryClient.invalidateQueries({ queryKey: productQueryKeys.all })
      void queryClient.invalidateQueries({ queryKey: ['dashboard'] })
    },
  })
}

export function useUpdateProductMutation() {
  const queryClient = useQueryClient()

  return useMutation({
    mutationFn: ({ id, request }: UpdateProductVariables) => updateProduct(id, request),
    onSuccess: (product) => {
      void queryClient.invalidateQueries({ queryKey: productQueryKeys.all })
      void queryClient.invalidateQueries({ queryKey: productQueryKeys.detail(product.id) })
      void queryClient.invalidateQueries({ queryKey: ['dashboard'] })
    },
  })
}

export function useChangeProductActiveMutation() {
  const queryClient = useQueryClient()

  return useMutation({
    mutationFn: ({ id, request }: ChangeProductActiveVariables) =>
      changeProductActive(id, request),
    onSuccess: (product) => {
      void queryClient.invalidateQueries({ queryKey: productQueryKeys.all })
      void queryClient.invalidateQueries({ queryKey: productQueryKeys.detail(product.id) })
      void queryClient.invalidateQueries({ queryKey: ['dashboard'] })
    },
  })
}
