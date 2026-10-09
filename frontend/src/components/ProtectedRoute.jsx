import { Navigate, Outlet, useLocation } from 'react-router-dom'
import { useAuth } from '../context/useAuth'
import { dashboardPathForRole, loginPathForRole } from '../routes/roles'
import LoadingSpinner from './LoadingSpinner'

// UI-only guard. The backend independently enforces authentication and roles on
// every request; this simply avoids rendering screens the user cannot use.
function ProtectedRoute({ allowedRole }) {
  const { user, initializing } = useAuth()
  const location = useLocation()

  if (initializing) {
    return <LoadingSpinner full label="Checking your session…" />
  }

  if (!user) {
    const loginPath = allowedRole ? loginPathForRole(allowedRole) : '/login'
    return <Navigate to={loginPath} replace state={{ from: location.pathname }} />
  }

  if (allowedRole && user.role !== allowedRole) {
    return <Navigate to={dashboardPathForRole(user.role)} replace />
  }

  return <Outlet />
}

export default ProtectedRoute
