import { LogOut, MapPin, Users } from 'lucide-react'
import { NavLink, Outlet } from 'react-router-dom'
import { useAuth } from '../auth/AuthContext'
import { Button } from './ui/button'
import { ProfileAvatar } from './ProfileAvatar'

export function AppShell() {
  const { user, logout } = useAuth()
  if (!user) return null

  return (
    <div className="app-shell">
      <aside className="sidebar">
        <img className="sidebar__logo" src="/solution-logo.svg" alt="Solution" />
        <nav className="sidebar__nav" aria-label="Navegação principal">
          {user.role === 'ADMIN' && (
            <NavLink end to="/users" className={({ isActive }) => isActive ? 'nav-item nav-item--active' : 'nav-item'}>
              <Users size={19} /> Usuários
            </NavLink>
          )}
          <NavLink to={`/users/${user.id}`} className={({ isActive }) => isActive ? 'nav-item nav-item--active' : 'nav-item'}>
            <MapPin size={19} /> {user.role === 'ADMIN' ? 'Meu perfil' : 'Meus endereços'}
          </NavLink>
        </nav>
        <div className="sidebar__account">
          <ProfileAvatar user={user} />
          <div className="sidebar__identity"><strong>{user.name}</strong><span>{user.role === 'ADMIN' ? 'Administrador' : 'Usuário'}</span></div>
          <Button aria-label="Sair" title="Sair" variant="ghost" size="icon" onClick={logout}><LogOut size={18} /></Button>
        </div>
      </aside>
      <main className="main-content"><Outlet /></main>
    </div>
  )
}
