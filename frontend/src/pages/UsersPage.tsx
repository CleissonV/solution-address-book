import { ChevronRight, Search, UserCheck, UserX, UsersRound } from 'lucide-react'
import { useMemo, useState } from 'react'
import { Navigate, useNavigate } from 'react-router-dom'
import { useAuth } from '../auth/AuthContext'
import { CreateUserDialog } from '../components/CreateUserDialog'
import { ProfileAvatar } from '../components/ProfileAvatar'
import { Badge } from '../components/ui/badge'
import { Input } from '../components/ui/input'
import { useUsers } from '../features/users/api'
import { formatCpf } from '../lib/utils'
import type { UserStatus } from '../types'

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
    <div className="page">
      <header className="page-header"><div><span className="eyebrow eyebrow--brand">Administração</span><h1>Usuários</h1><p>Gerencie cadastros e endereços da plataforma.</p></div><CreateUserDialog /></header>
      <section className="stats-grid">
        <article className="stat-card"><div className="stat-card__icon"><UserCheck /></div><div><span>Usuários ativos</span><strong>{activeUsers}</strong></div></article>
        <article className="stat-card"><div className="stat-card__icon"><UserX /></div><div><span>Contas desativadas</span><strong>{data.length - activeUsers}</strong></div></article>
      </section>
      <section className="content-card">
        <div className="content-card__toolbar"><div><h2>Lista de usuários</h2><p>{filtered.length} {filtered.length === 1 ? 'resultado' : 'resultados'}</p></div><div className="list-filters"><label className="field filter-field"><span className="field__label">Status</span><select className="input" value={statusFilter} onChange={(event) => setStatusFilter(event.target.value as UserStatus | 'ALL')}><option value="ACTIVE">Ativos</option><option value="INACTIVE">Desativados</option><option value="ALL">Todos</option></select></label><div className="search-field"><Search size={18} /><Input label="Buscar" aria-label="Buscar por nome ou CPF" placeholder="Nome ou CPF" value={search} onChange={(e) => setSearch(e.target.value)} /></div></div></div>
        {isLoading ? <div className="empty-state"><span className="spinner" />Carregando usuários...</div> : isError ? <div className="alert alert--error">Não foi possível carregar os usuários.</div> : filtered.length === 0 ? <div className="empty-state"><UsersRound /><strong>Nenhum usuário encontrado</strong><span>Ajuste a busca ou crie um novo cadastro.</span></div> : (
          <div className="user-list">
            {filtered.map((item) => (
              <button key={item.id} className={item.status === 'INACTIVE' ? 'user-row user-row--inactive' : 'user-row'} onClick={() => navigate(`/users/${item.id}`)}>
                <ProfileAvatar user={item} size="large" />
                <span className="user-row__main"><strong>{item.name}</strong><small>{formatCpf(item.cpf)}</small></span>
                <span className="badge-group"><Badge tone={item.role === 'ADMIN' ? 'brand' : 'neutral'}>{item.role === 'ADMIN' ? 'Administrador' : 'Usuário'}</Badge>{item.status === 'INACTIVE' ? <Badge tone="danger">Inativo</Badge> : null}</span>
                <ChevronRight size={20} className="user-row__arrow" />
              </button>
            ))}
          </div>
        )}
      </section>
    </div>
  )
}
