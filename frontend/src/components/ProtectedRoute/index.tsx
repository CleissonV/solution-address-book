import { Navigate, Outlet, useLocation } from 'react-router-dom'
import { useAuth } from '@/auth/AuthContext'
import { AppLoader } from '@/components/ui/feedback'

export function ProtectedRoute() {
  const { user, loading } = useAuth()
  const location = useLocation()
  if (loading) return <AppLoader>Carregando ambiente...</AppLoader>
  return user ? <Outlet /> : <Navigate to="/login" replace state={{ from: location }} />
}
