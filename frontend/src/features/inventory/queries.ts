import { useMutation, useQuery, useQueryClient } from '@tanstack/react-query'

import {
  adjustInventoryStock,
  getInventoryStock,
  listInventory,
  listLowStockInventory,
  listStockMovements,
  type InventoryStockResponse,
  type StockAdjustmentRequest,
  type StockMovementResponse,
} from '../../lib/api/inventory'

export function useInventoryList() {
  return useQuery<InventoryStockResponse[]>({
    queryFn: listInventory,
    queryKey: ['inventory'],
  })
}

export function useLowStockInventory() {
  return useQuery<InventoryStockResponse[]>({
    queryFn: listLowStockInventory,
    queryKey: ['inventory', 'low-stock'],
  })
}

export function useInventoryStock(productId: number | null) {
  return useQuery<InventoryStockResponse>({
    enabled: productId !== null,
    queryFn: () => getInventoryStock(productId!),
    queryKey: ['inventory', productId],
  })
}

export function useStockMovements(productId: number | null) {
  return useQuery<StockMovementResponse[]>({
    enabled: productId !== null,
    queryFn: () => listStockMovements(productId!),
    queryKey: ['inventory', productId, 'movements'],
  })
}

export function useAdjustStock(productId: number) {
  const queryClient = useQueryClient()

  return useMutation({
    mutationFn: (request: StockAdjustmentRequest) => adjustInventoryStock(productId, request),
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: ['inventory'] })
      queryClient.invalidateQueries({ queryKey: ['products'] })
      queryClient.invalidateQueries({ queryKey: ['reports'] })
    },
  })
}
