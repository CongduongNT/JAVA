import { useSelector } from 'react-redux'
import { Navigate, Outlet } from 'react-router-dom'

/**
 * ProtectedRoute — redirects to /login if user is not authenticated.
 * Checks both isAuthenticated flag and token presence in localStorage
 * to handle page refreshes gracefully.
 */
export default function ProtectedRoute() {
  const { isAuthenticated, token } = useSelector((state) => state.auth)

  // Allow access if authenticated or if a token exists (persisted from previous session)
  const canAccess = isAuthenticated || !!token

  if (!canAccess) {
    return <Navigate to="/login" replace />
  }

  return <Outlet />
}
