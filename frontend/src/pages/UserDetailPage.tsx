import { ArrowLeft, CalendarDays, Fingerprint, MapPin, Shield } from 'lucide-react'
import { Link, useParams } from 'react-router-dom'
import { useAuth } from '../auth/AuthContext'
import { AddressCard } from '../components/AddressCard'
import { AddressDialog } from '../components/AddressDialog'
import { EditUserDialog } from '../components/EditUserDialog'
import { ProfileAvatar } from '../components/ProfileAvatar'
import { ProfilePhotoDialog } from '../components/ProfilePhotoDialog'
import { UserStatusAction } from '../components/UserStatusAction'
import { Badge } from '../components/ui/badge'
import { useUser } from '../features/users/api'
import { formatCpf, formatDate } from '../lib/utils'

export function UserDetailPage() {
  const { id } = useParams()
  const { user: actor, updateCurrentUser } = useAuth()
  const { data: user, isLoading, isError } = useUser(id)

  if (isLoading) return <div className="app-loader"><span className="spinner" />Carregando perfil...</div>
  if (isError || !user || !id) return <div className="page"><div className="alert alert--error">Usuário não encontrado ou acesso não permitido.</div></div>

  const active = user.status === 'ACTIVE'

  return (
    <div className="page">
      <header className="page-header page-header--detail">
        <div className="page-header__identity">
          {actor?.role === 'ADMIN' && <Link className="back-link" to="/users"><ArrowLeft size={18} />Voltar</Link>}
          <div className="profile-heading"><ProfileAvatar user={user} size="xl" /><div><span className="eyebrow eyebrow--brand">Perfil do usuário</span><h1>{user.name}</h1><div className="badge-group"><Badge tone={user.role === 'ADMIN' ? 'brand' : 'neutral'}>{user.role === 'ADMIN' ? 'Administrador' : 'Usuário comum'}</Badge>{!active ? <Badge tone="danger">Conta desativada</Badge> : null}</div></div></div>
        </div>
        <div className="page-header__actions">
          {active && actor?.id === user.id ? <ProfilePhotoDialog
            user={user}
            onUpdated={(profilePhotoVersion) => {
              if (actor?.id === user.id) updateCurrentUser({ ...actor, profilePhotoVersion })
            }}
          /> : null}
          {active ? <EditUserDialog
            user={user}
            canEditRole={actor?.role === 'ADMIN'}
            onUpdated={(updated) => {
              if (actor?.id === updated.id) updateCurrentUser(updated)
            }}
          /> : null}
          {active ? <AddressDialog userId={id} /> : null}
          {actor?.role === 'ADMIN' && actor.id !== user.id ? <UserStatusAction user={user} /> : null}
        </div>
      </header>
      {!active ? <div className="alert alert--warning account-status-notice">Conta desativada. Cadastro disponível somente para consulta até ser reativado.</div> : null}
      <section className="profile-grid">
        <article className="profile-card"><Fingerprint /><span>CPF</span><strong>{formatCpf(user.cpf)}</strong></article>
        <article className="profile-card"><CalendarDays /><span>Nascimento</span><strong>{formatDate(user.birthDate)}</strong></article>
        <article className="profile-card"><Shield /><span>Nível de acesso</span><strong>{user.role === 'ADMIN' ? 'Administrador' : 'Padrão'}</strong></article>
      </section>
      <section className="content-card">
        <div className="content-card__toolbar"><div><h2>Endereços</h2><p>{user.addresses.length} {user.addresses.length === 1 ? 'endereço cadastrado' : 'endereços cadastrados'}</p></div></div>
        {user.addresses.length === 0 ? <div className="empty-state"><MapPin /><strong>Nenhum endereço cadastrado</strong><span>{active ? 'Adicione o primeiro endereço deste usuário.' : 'Esta conta não possui endereços cadastrados.'}</span></div> : <div className="address-grid">{user.addresses.map((address) => <AddressCard key={address.id} address={address} userId={id} readOnly={!active} />)}</div>}
      </section>
    </div>
  )
}
