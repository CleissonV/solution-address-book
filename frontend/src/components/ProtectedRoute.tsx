import { Navigate, Outlet, useLocation } from 'react-router-dom'
import { useAuth } from '../auth/AuthContext'

export function ProtectedRoute() {
  const { user, loading } = useAuth()
  const location = useLocation()
  if (loading) return <div className="app-loader"><span className="spinner" />Carregando ambiente...</div>
  return user ? <Outlet /> : <Navigate to="/login" replace state={{ from: location }} />
}

