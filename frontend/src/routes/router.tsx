import { createBrowserRouter, Navigate } from 'react-router-dom'

import { AppLayout } from '../components/layout/AppLayout'
import { LoginPage } from '../features/auth/pages/LoginPage'
import { CategoriesPage } from '../features/categories/pages/CategoriesPage'
import { DashboardPage } from '../features/dashboard/pages/DashboardPage'
import { InventoryPage } from '../features/inventory/pages/InventoryPage'
import { ProductsPage } from '../features/products/pages/ProductsPage'
import { CheckoutPage } from '../features/sales/pages/CheckoutPage'
import { SalesPage } from '../features/sales/pages/SalesPage'
import { NotFoundPage } from './NotFoundPage'
import { ProtectedRoute } from './ProtectedRoute'
import { RoleGuard } from './RoleGuard'
import { RoutePlaceholderPage } from './RoutePlaceholderPage'
import { protectedRoutes, type ProtectedRouteConfig } from './navConfig'

function getProtectedRouteElement(route: ProtectedRouteConfig) {
  if (route.id === 'dashboard') {
    return <DashboardPage />
  }

  if (route.id === 'categories') {
    return <CategoriesPage />
  }

  if (route.id === 'products') {
    return <ProductsPage />
  }

  if (route.id === 'inventory') {
    return <InventoryPage />
  }

  if (route.id === 'checkout') {
    return <CheckoutPage />
  }

  if (route.id === 'sales') {
    return <SalesPage />
  }

  return <RoutePlaceholderPage route={route} />
}

export const router = createBrowserRouter([
  {
    element: <LoginPage />,
    path: '/login',
  },
  {
    children: [
      {
        element: <Navigate replace to="/dashboard" />,
        index: true,
      },
      ...protectedRoutes.map((route) => ({
        element: (
          <RoleGuard allowedRoles={route.roles}>
            {getProtectedRouteElement(route)}
          </RoleGuard>
        ),
        path: route.path.slice(1),
      })),
    ],
    element: (
      <ProtectedRoute>
        <AppLayout />
      </ProtectedRoute>
    ),
    path: '/',
  },
  {
    element: <NotFoundPage />,
    path: '*',
  },
])
