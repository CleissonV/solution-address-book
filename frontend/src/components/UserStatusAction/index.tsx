import { RotateCcw, UserX } from 'lucide-react';
import { useState } from 'react';
import { useUpdateUserStatus } from '@/features/users/api';
import { getErrorMessage } from '@/lib/utils';
import type { UserDetails } from '@/types';
import { Alert } from '@/components/ui/feedback';
import {
  AlertDialog,
  AlertDialogAction,
  AlertDialogCancel,
  AlertDialogContent,
  AlertDialogDescription,
  AlertDialogFooter,
  AlertDialogHeader,
  AlertDialogTitle,
  AlertDialogTrigger,
} from '@/components/ui/alert-dialog';
import { Button } from '@/components/ui/button';
import styles from './styles.module.css';

interface UserStatusActionProps {
  user: UserDetails;
}

export function UserStatusAction({ user }: UserStatusActionProps) {
  const [open, setOpen] = useState(false);
  const [error, setError] = useState('');
  const mutation = useUpdateUserStatus(user.id);
  const active = user.status === 'ACTIVE';

  async function updateStatus() {
    setError('');
    try {
      await mutation.mutateAsync(active ? 'INACTIVE' : 'ACTIVE');
      setOpen(false);
    } catch (exception) {
      setError(getErrorMessage(exception));
    }
  }

  return (
    <AlertDialog open={open} onOpenChange={setOpen}>
      <AlertDialogTrigger asChild>
        <Button variant={active ? 'danger' : 'secondary'}>
          {active ? <UserX size={18} /> : <RotateCcw size={18} />}
          {active ? 'Desativar conta' : 'Reativar conta'}
        </Button>
      </AlertDialogTrigger>
      <AlertDialogContent>
        <AlertDialogHeader>
          <div>
            <AlertDialogTitle>{active ? 'Desativar conta?' : 'Reativar conta?'}</AlertDialogTitle>
            <AlertDialogDescription>
              {active
                ? `${user.name} perderá acesso imediatamente. Dados e endereços serão preservados.`
                : `${user.name} poderá entrar novamente e voltar a alterar seus dados.`}
            </AlertDialogDescription>
          </div>
        </AlertDialogHeader>
        {error ? <Alert className={styles.error}>{error}</Alert> : null}
        <AlertDialogFooter>
          <AlertDialogCancel>Cancelar</AlertDialogCancel>
          <AlertDialogAction
            variant={active ? 'danger' : 'primary'}
            onClick={(event) => {
              event.preventDefault();
              void updateStatus();
            }}
            disabled={mutation.isPending}
          >
            {mutation.isPending ? 'Salvando...' : active ? 'Desativar conta' : 'Reativar conta'}
          </AlertDialogAction>
        </AlertDialogFooter>
      </AlertDialogContent>
    </AlertDialog>
  );
}
