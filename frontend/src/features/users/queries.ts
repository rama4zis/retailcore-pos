import { useMutation, useQuery, useQueryClient } from '@tanstack/react-query'

import {
  changeUserActive,
  changeUserRole,
  createUser,
  listUsers,
  type UserActiveRequest,
  type UserCreateRequest,
  type UserRoleRequest,
} from '../../lib/api/users'

export const usersQueryKeys = {
  all: ['users'] as const,
  lists: () => [...usersQueryKeys.all, 'list'] as const,
}

export function useUsersQuery() {
  return useQuery({
    queryFn: listUsers,
    queryKey: usersQueryKeys.lists(),
  })
}

export function useCreateUserMutation() {
  const queryClient = useQueryClient()

  return useMutation({
    mutationFn: (request: UserCreateRequest) => createUser(request),
    onSuccess: () => {
      void queryClient.invalidateQueries({ queryKey: usersQueryKeys.all })
    },
  })
}

export function useChangeUserRoleMutation() {
  const queryClient = useQueryClient()

  return useMutation({
    mutationFn: ({ id, request }: { id: number; request: UserRoleRequest }) =>
      changeUserRole(id, request),
    onSuccess: () => {
      void queryClient.invalidateQueries({ queryKey: usersQueryKeys.all })
    },
  })
}

export function useChangeUserActiveMutation() {
  const queryClient = useQueryClient()

  return useMutation({
    mutationFn: ({ id, request }: { id: number; request: UserActiveRequest }) =>
      changeUserActive(id, request),
    onSuccess: () => {
      void queryClient.invalidateQueries({ queryKey: usersQueryKeys.all })
    },
  })
}
