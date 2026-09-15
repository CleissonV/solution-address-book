import { ArrowLeft, CalendarDays, Fingerprint, MapPin, Shield } from 'lucide-react';
import { Link, useParams } from 'react-router-dom';
import { useAuth } from '@/auth/AuthContext';
import { AddressCard } from '@/components/AddressCard';
import { AddressDialog } from '@/components/AddressDialog';
import { EditUserDialog } from '@/components/EditUserDialog';
import { ProfileAvatar } from '@/components/ProfileAvatar';
import { ProfilePhotoDialog } from '@/components/ProfilePhotoDialog';
import { UserStatusAction } from '@/components/UserStatusAction';
import { Badge } from '@/components/ui/badge';
import { useUser } from '@/features/users/api';
import { formatCpf, formatDate } from '@/lib/utils';
import { Alert, AppLoader } from '@/components/ui/feedback';
import styles from './styles.module.css';

export function UserDetailPage() {
  const { id } = useParams();
  const { user: actor, updateCurrentUser } = useAuth();
  const { data: user, isLoading, isError } = useUser(id);

  if (isLoading) return <AppLoader>Carregando perfil...</AppLoader>;
  if (isError || !user || !id)
    return (
      <div className={styles.page}>
        <Alert>Usuário não encontrado ou acesso não permitido.</Alert>
      </div>
    );

  const active = user.status === 'ACTIVE';

  return (
    <div className={styles.page}>
      <header className={styles.header}>
        <div className={styles.identity}>
          {actor?.role === 'ADMIN' && (
            <Link className={styles.backLink} to="/users">
              <ArrowLeft size={18} />
              Voltar
            </Link>
          )}
          <div className={styles.profileHeading}>
            <ProfileAvatar user={user} size="xl" />
            <div>
              <span className={styles.eyebrow}>Perfil do usuário</span>
              <h1>{user.name}</h1>
              <div className={styles.badges}>
                <Badge tone={user.role === 'ADMIN' ? 'brand' : 'neutral'}>
                  {user.role === 'ADMIN' ? 'Administrador' : 'Usuário comum'}
                </Badge>
                {!active ? <Badge tone="danger">Conta desativada</Badge> : null}
              </div>
            </div>
          </div>
        </div>
        <div className={styles.actions}>
          {active && actor?.id === user.id ? (
            <ProfilePhotoDialog
              user={user}
              onUpdated={(profilePhotoVersion) => {
                if (actor?.id === user.id) updateCurrentUser({ ...actor, profilePhotoVersion });
              }}
            />
          ) : null}
          {active ? (
            <EditUserDialog
              user={user}
              canEditRole={actor?.role === 'ADMIN'}
              onUpdated={(updated) => {
                if (actor?.id === updated.id) updateCurrentUser(updated);
              }}
            />
          ) : null}
          {active ? <AddressDialog userId={id} /> : null}
          {actor?.role === 'ADMIN' && actor.id !== user.id ? (
            <UserStatusAction user={user} />
          ) : null}
        </div>
      </header>
      {!active ? (
        <Alert tone="warning" className={styles.statusNotice}>
          Conta desativada. Cadastro disponível somente para consulta até ser reativado.
        </Alert>
      ) : null}
      <section className={styles.profileGrid}>
        <article className={styles.profileCard}>
          <Fingerprint />
          <span>CPF</span>
          <strong>{formatCpf(user.cpf)}</strong>
        </article>
        <article className={styles.profileCard}>
          <CalendarDays />
          <span>Nascimento</span>
          <strong>{formatDate(user.birthDate)}</strong>
        </article>
        <article className={styles.profileCard}>
          <Shield />
          <span>Nível de acesso</span>
          <strong>{user.role === 'ADMIN' ? 'Administrador' : 'Padrão'}</strong>
        </article>
      </section>
      <section className={styles.content}>
        <div className={styles.toolbar}>
          <div>
            <h2>Endereços</h2>
            <p>
              {user.addresses.length}{' '}
              {user.addresses.length === 1 ? 'endereço cadastrado' : 'endereços cadastrados'}
            </p>
          </div>
        </div>
        {user.addresses.length === 0 ? (
          <div className={styles.empty}>
            <MapPin />
            <strong>Nenhum endereço cadastrado</strong>
            <span>
              {active
                ? 'Adicione o primeiro endereço deste usuário.'
                : 'Esta conta não possui endereços cadastrados.'}
            </span>
          </div>
        ) : (
          <div className={styles.addressGrid}>
            {user.addresses.map((address) => (
              <AddressCard key={address.id} address={address} userId={id} readOnly={!active} />
            ))}
          </div>
        )}
      </section>
    </div>
  );
}
