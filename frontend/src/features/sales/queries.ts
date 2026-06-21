import { useMutation, useQuery, useQueryClient } from '@tanstack/react-query'

import {
  checkoutSale,
  getSale,
  listSales,
  refundSale,
  type CheckoutRequest,
  type RefundRequest,
} from '../../lib/api/sales'

export const salesQueryKeys = {
  all: ['sales'] as const,
  detail: (id: number) => ['sales', id] as const,
}

export function useSalesQuery() {
  return useQuery({
    queryFn: listSales,
    queryKey: salesQueryKeys.all,
  })
}

export function useSaleQuery(id: number | null) {
  return useQuery({
    enabled: id !== null,
    queryFn: () => getSale(id as number),
    queryKey: id === null ? ['sales', 'detail', 'missing'] : salesQueryKeys.detail(id),
  })
}

export function useCheckoutMutation() {
  const queryClient = useQueryClient()

  return useMutation({
    mutationFn: (request: CheckoutRequest) => checkoutSale(request),
    onSuccess: () => {
      void queryClient.invalidateQueries({ queryKey: ['products'] })
      void queryClient.invalidateQueries({ queryKey: ['inventory'] })
      void queryClient.invalidateQueries({ queryKey: salesQueryKeys.all })
      void queryClient.invalidateQueries({ queryKey: ['dashboard'] })
      void queryClient.invalidateQueries({ queryKey: ['reports'] })
    },
  })
}

export function useRefundMutation(saleId: number) {
  const queryClient = useQueryClient()

  return useMutation({
    mutationFn: (request: RefundRequest) => refundSale(saleId, request),
    onSuccess: () => {
      void queryClient.invalidateQueries({ queryKey: salesQueryKeys.all })
      void queryClient.invalidateQueries({ queryKey: salesQueryKeys.detail(saleId) })
      void queryClient.invalidateQueries({ queryKey: ['inventory'] })
      void queryClient.invalidateQueries({ queryKey: ['dashboard'] })
      void queryClient.invalidateQueries({ queryKey: ['reports'] })
    },
  })
}
