import { Pencil } from 'lucide-react';
import { useEffect, useState, type FormEvent } from 'react';
import { useUpdateUser } from '@/features/users/api';
import { isValidCpf } from '@/lib/cpf';
import { formatCpf, getErrorMessage } from '@/lib/utils';
import type { Role, UserDetails } from '@/types';
import { Button } from '@/components/ui/button';
import { Dialog } from '@/components/ui/dialog';
import { Alert } from '@/components/ui/feedback';
import formStyles from '@/components/ui/form.module.css';
import { Input, SelectField } from '@/components/ui/input';

interface EditUserDialogProps {
  user: UserDetails;
  canEditRole: boolean;
  onUpdated: (user: UserDetails) => void;
}

function formFrom(user: UserDetails) {
  return { name: user.name, cpf: formatCpf(user.cpf), birthDate: user.birthDate, role: user.role };
}

export function EditUserDialog({ user, canEditRole, onUpdated }: EditUserDialogProps) {
  const [open, setOpen] = useState(false);
  const [form, setForm] = useState(() => formFrom(user));
  const [error, setError] = useState('');
  const mutation = useUpdateUser(user.id);

  useEffect(() => {
    if (!open) return;
    setForm(formFrom(user));
    setError('');
  }, [open, user]);

  async function submit(event: FormEvent) {
    event.preventDefault();
    setError('');
    if (!isValidCpf(form.cpf)) {
      setError('Informe um CPF válido.');
      return;
    }

    try {
      const updated = await mutation.mutateAsync({
        name: form.name,
        cpf: form.cpf,
        birthDate: form.birthDate,
        role: canEditRole ? form.role : undefined,
      });
      onUpdated(updated);
      setOpen(false);
    } catch (exception) {
      setError(getErrorMessage(exception));
    }
  }

  return (
    <>
      <Button variant="secondary" onClick={() => setOpen(true)}>
        <Pencil size={18} />
        Editar dados
      </Button>
      <Dialog
        open={open}
        onOpenChange={setOpen}
        title="Editar usuário"
        description={
          canEditRole
            ? 'Atualize dados pessoais e nível de acesso.'
            : 'Atualize seus dados pessoais.'
        }
        footer={
          <>
            <Button variant="secondary" onClick={() => setOpen(false)}>
              Cancelar
            </Button>
            <Button type="submit" form="edit-user" disabled={mutation.isPending}>
              {mutation.isPending ? 'Salvando...' : 'Salvar alterações'}
            </Button>
          </>
        }
      >
        <form id="edit-user" className={formStyles.grid} onSubmit={submit}>
          {error ? <Alert className={formStyles.full}>{error}</Alert> : null}
          <Input
            fieldClassName={formStyles.full}
            label="Nome completo"
            name="name"
            value={form.name}
            onChange={(event) => setForm({ ...form, name: event.target.value })}
            maxLength={160}
            required
          />
          <Input
            label="CPF"
            name="cpf"
            inputMode="numeric"
            value={form.cpf}
            onChange={(event) => setForm({ ...form, cpf: formatCpf(event.target.value) })}
            required
          />
          <Input
            label="Data de nascimento"
            name="birthDate"
            type="date"
            value={form.birthDate}
            onChange={(event) => setForm({ ...form, birthDate: event.target.value })}
            required
          />
          {canEditRole ? (
            <SelectField
              label="Perfil"
              value={form.role}
              onChange={(event) => setForm({ ...form, role: event.target.value as Role })}
            >
              <option value="USER">Usuário comum</option>
              <option value="ADMIN">Administrador</option>
            </SelectField>
          ) : null}
        </form>
      </Dialog>
    </>
  );
}
