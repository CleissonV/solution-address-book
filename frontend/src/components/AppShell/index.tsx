import { LogOut, MapPin, Users } from 'lucide-react'
import { NavLink, Outlet } from 'react-router-dom'
import { useAuth } from '@/auth/AuthContext'
import { Button } from '@/components/ui/button'
import { ProfileAvatar } from '@/components/ProfileAvatar'
import { cn } from '@/lib/utils'
import styles from './styles.module.css'

export function AppShell() {
  const { user, logout } = useAuth()
  if (!user) return null

  return (
    <div className={styles.shell}>
      <aside className={styles.sidebar}>
        <img className={styles.logo} src="/solution-logo.svg" alt="Solution" />
        <nav className={styles.nav} aria-label="Navegação principal">
          {user.role === 'ADMIN' && (
            <NavLink end to="/users" className={({ isActive }) => cn(styles.navItem, isActive && styles.active)}>
              <Users size={19} /> Usuários
            </NavLink>
          )}
          <NavLink to={`/users/${user.id}`} className={({ isActive }) => cn(styles.navItem, isActive && styles.active)}>
            <MapPin size={19} /> {user.role === 'ADMIN' ? 'Meu perfil' : 'Meus endereços'}
          </NavLink>
        </nav>
        <div className={styles.account}>
          <ProfileAvatar user={user} />
          <div className={styles.identity}><strong>{user.name}</strong><span>{user.role === 'ADMIN' ? 'Administrador' : 'Usuário'}</span></div>
          <Button aria-label="Sair" title="Sair" variant="ghost" size="icon" onClick={logout}><LogOut size={18} /></Button>
        </div>
      </aside>
      <main className={styles.main}><Outlet /></main>
    </div>
  )
}
