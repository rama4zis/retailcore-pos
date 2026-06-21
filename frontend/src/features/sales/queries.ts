import { useMutation, useQueryClient } from '@tanstack/react-query'

import { checkoutSale, type CheckoutRequest } from '../../lib/api/sales'

export function useCheckoutMutation() {
  const queryClient = useQueryClient()

  return useMutation({
    mutationFn: (request: CheckoutRequest) => checkoutSale(request),
    onSuccess: () => {
      void queryClient.invalidateQueries({ queryKey: ['products'] })
      void queryClient.invalidateQueries({ queryKey: ['inventory'] })
      void queryClient.invalidateQueries({ queryKey: ['sales'] })
      void queryClient.invalidateQueries({ queryKey: ['dashboard'] })
      void queryClient.invalidateQueries({ queryKey: ['reports'] })
    },
  })
}
