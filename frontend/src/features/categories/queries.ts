import { useMutation, useQuery, useQueryClient } from '@tanstack/react-query'

import {
  createCategory,
  deleteCategory,
  listCategories,
  updateCategory,
  type CategoryCreateRequest,
  type CategoryUpdateRequest,
} from '../../lib/api/categories'

interface UpdateCategoryVariables {
  id: number
  request: CategoryUpdateRequest
}

export const categoryQueryKeys = {
  all: ['categories'] as const,
  detail: (id: number) => [...categoryQueryKeys.all, id] as const,
  list: () => [...categoryQueryKeys.all, 'list'] as const,
}

export function useCategoriesQuery() {
  return useQuery({
    queryFn: listCategories,
    queryKey: categoryQueryKeys.list(),
  })
}

export function useCreateCategoryMutation() {
  const queryClient = useQueryClient()

  return useMutation({
    mutationFn: (request: CategoryCreateRequest) => createCategory(request),
    onSuccess: () => {
      void queryClient.invalidateQueries({ queryKey: categoryQueryKeys.all })
    },
  })
}

export function useUpdateCategoryMutation() {
  const queryClient = useQueryClient()

  return useMutation({
    mutationFn: ({ id, request }: UpdateCategoryVariables) => updateCategory(id, request),
    onSuccess: (category) => {
      void queryClient.invalidateQueries({ queryKey: categoryQueryKeys.all })
      void queryClient.invalidateQueries({ queryKey: categoryQueryKeys.detail(category.id) })
      void queryClient.invalidateQueries({ queryKey: ['products'] })
    },
  })
}

export function useDeleteCategoryMutation() {
  const queryClient = useQueryClient()

  return useMutation({
    mutationFn: (id: number) => deleteCategory(id),
    onSuccess: () => {
      void queryClient.invalidateQueries({ queryKey: categoryQueryKeys.all })
      void queryClient.invalidateQueries({ queryKey: ['products'] })
    },
  })
}
