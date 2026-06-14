import type { UserRole } from '../lib/api/users'

export type ProtectedRouteId =
  | 'dashboard'
  | 'categories'
  | 'products'
  | 'inventory'
  | 'checkout'
  | 'sales'
  | 'reports'
  | 'users'

export interface ProtectedRouteConfig {
  buildStep: string
  description: string
  id: ProtectedRouteId
  label: string
  path: string
  roles: readonly UserRole[]
}

const allRoles: readonly UserRole[] = ['ADMIN', 'MANAGER', 'CASHIER']
const managementRoles: readonly UserRole[] = ['ADMIN', 'MANAGER']
const adminOnly: readonly UserRole[] = ['ADMIN']

export const protectedRoutes: readonly ProtectedRouteConfig[] = [
  {
    buildStep: 'FE-06',
    description: 'Overview shell for role-aware POS operations.',
    id: 'dashboard',
    label: 'Dashboard',
    path: '/dashboard',
    roles: allRoles,
  },
  {
    buildStep: 'FE-07',
    description: 'Category management for catalog owners.',
    id: 'categories',
    label: 'Categories',
    path: '/categories',
    roles: managementRoles,
  },
  {
    buildStep: 'FE-07',
    description: 'Product catalog management and active status control.',
    id: 'products',
    label: 'Products',
    path: '/products',
    roles: managementRoles,
  },
  {
    buildStep: 'FE-08',
    description: 'Inventory stock levels, low-stock review, and adjustments.',
    id: 'inventory',
    label: 'Inventory',
    path: '/inventory',
    roles: managementRoles,
  },
  {
    buildStep: 'FE-09',
    description: 'Cashier checkout workflow and receipt handoff.',
    id: 'checkout',
    label: 'Checkout',
    path: '/checkout',
    roles: allRoles,
  },
  {
    buildStep: 'FE-10',
    description: 'Sales history, sale detail review, and refunds.',
    id: 'sales',
    label: 'Sales',
    path: '/sales',
    roles: allRoles,
  },
  {
    buildStep: 'FE-11',
    description: 'Manager and admin reporting views.',
    id: 'reports',
    label: 'Reports',
    path: '/reports',
    roles: managementRoles,
  },
  {
    buildStep: 'FE-12',
    description: 'Admin-only user management.',
    id: 'users',
    label: 'Users',
    path: '/users',
    roles: adminOnly,
  },
]

export function canRoleAccessRoute(
  role: UserRole | null | undefined,
  route: ProtectedRouteConfig,
) {
  return Boolean(role && route.roles.includes(role))
}

export function getNavItemsForRole(role: UserRole | null | undefined) {
  if (!role) {
    return []
  }

  return protectedRoutes.filter((route) => canRoleAccessRoute(role, route))
}

export function formatAllowedRoles(roles: readonly UserRole[]) {
  return roles.join(' / ')
}
