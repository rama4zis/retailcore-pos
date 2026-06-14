import { createBrowserRouter, Navigate } from 'react-router-dom'

import { AppLayout } from '../components/layout/AppLayout'
import { LoginPlaceholderPage } from './LoginPlaceholderPage'
import { NotFoundPage } from './NotFoundPage'
import { ProtectedRoute } from './ProtectedRoute'
import { RoleGuard } from './RoleGuard'
import { RoutePlaceholderPage } from './RoutePlaceholderPage'
import { protectedRoutes } from './navConfig'

export const router = createBrowserRouter([
  {
    element: <LoginPlaceholderPage />,
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
            <RoutePlaceholderPage route={route} />
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
