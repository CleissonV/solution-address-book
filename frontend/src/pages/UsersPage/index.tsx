import { ChevronRight, Search, UserCheck, UserX, UsersRound } from 'lucide-react'
import { useMemo, useState } from 'react'
import { Navigate, useNavigate } from 'react-router-dom'
import { useAuth } from '@/auth/AuthContext'
import { CreateUserDialog } from '@/components/CreateUserDialog'
import { ProfileAvatar } from '@/components/ProfileAvatar'
import { Badge } from '@/components/ui/badge'
import { Input } from '@/components/ui/input'
import { useUsers } from '@/features/users/api'
import { formatCpf } from '@/lib/utils'
import type { UserStatus } from '@/types'
import { Alert, Spinner } from '@/components/ui/feedback'
import { SelectField } from '@/components/ui/input'
import { cn } from '@/lib/utils'
import styles from './styles.module.css'

export function UsersPage() {
  const { user } = useAuth()
  const navigate = useNavigate()
  const [search, setSearch] = useState('')
  const [statusFilter, setStatusFilter] = useState<UserStatus | 'ALL'>('ACTIVE')
  const { data = [], isLoading, isError } = useUsers(user?.role === 'ADMIN')
  const filtered = useMemo(() => {
    const term = search.toLowerCase().replace(/\D/g, '') || search.toLowerCase()
    return data.filter((item) => (statusFilter === 'ALL' || item.status === statusFilter)
      && (item.name.toLowerCase().includes(search.toLowerCase()) || item.cpf.includes(term)))
  }, [data, search, statusFilter])
  const activeUsers = data.filter((item) => item.status === 'ACTIVE').length

  if (user?.role !== 'ADMIN') return <Navigate to={`/users/${user?.id}`} replace />

  return (
    <div className={styles.page}>
      <header className={styles.header}><div><span className={styles.eyebrow}>Administração</span><h1>Usuários</h1><p>Gerencie cadastros e endereços da plataforma.</p></div><CreateUserDialog /></header>
      <section className={styles.stats}>
        <article className={styles.statCard}><div className={styles.statIcon}><UserCheck /></div><div><span>Usuários ativos</span><strong>{activeUsers}</strong></div></article>
        <article className={styles.statCard}><div className={styles.statIcon}><UserX /></div><div><span>Contas desativadas</span><strong>{data.length - activeUsers}</strong></div></article>
      </section>
      <section className={styles.content}>
        <div className={styles.toolbar}><div><h2>Lista de usuários</h2><p>{filtered.length} {filtered.length === 1 ? 'resultado' : 'resultados'}</p></div><div className={styles.filters}><SelectField label="Status" fieldClassName={styles.filterField} value={statusFilter} onChange={(event) => setStatusFilter(event.target.value as UserStatus | 'ALL')}><option value="ACTIVE">Ativos</option><option value="INACTIVE">Desativados</option><option value="ALL">Todos</option></SelectField><div className={styles.search}><Search size={18} /><Input fieldClassName={styles.searchField} className={styles.searchInput} label="Buscar" aria-label="Buscar por nome ou CPF" placeholder="Nome ou CPF" value={search} onChange={(e) => setSearch(e.target.value)} /></div></div></div>
        {isLoading ? <div className={styles.empty}><Spinner />Carregando usuários...</div> : isError ? <Alert className={styles.loadError}>Não foi possível carregar os usuários.</Alert> : filtered.length === 0 ? <div className={styles.empty}><UsersRound /><strong>Nenhum usuário encontrado</strong><span>Ajuste a busca ou crie um novo cadastro.</span></div> : (
          <div className={styles.list}>
            {filtered.map((item) => (
              <button key={item.id} className={cn(styles.row, item.status === 'INACTIVE' && styles.inactive)} onClick={() => navigate(`/users/${item.id}`)}>
                <ProfileAvatar user={item} size="large" className={item.status === 'INACTIVE' ? styles.inactiveAvatar : undefined} />
                <span className={styles.main}><strong>{item.name}</strong><small>{formatCpf(item.cpf)}</small></span>
                <span className={styles.badges}><Badge tone={item.role === 'ADMIN' ? 'brand' : 'neutral'}>{item.role === 'ADMIN' ? 'Administrador' : 'Usuário'}</Badge>{item.status === 'INACTIVE' ? <Badge tone="danger">Inativo</Badge> : null}</span>
                <ChevronRight size={20} className={styles.arrow} />
              </button>
            ))}
          </div>
        )}
      </section>
    </div>
  )
}
