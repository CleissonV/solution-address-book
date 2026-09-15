import { useState, type FormEvent } from 'react';
import { UserPlus } from 'lucide-react';
import { useCreateUser } from '@/features/users/api';
import { isValidCpf } from '@/lib/cpf';
import { formatCpf, getErrorMessage } from '@/lib/utils';
import type { Role } from '@/types';
import { Button } from '@/components/ui/button';
import { Dialog } from '@/components/ui/dialog';
import { Alert } from '@/components/ui/feedback';
import formStyles from '@/components/ui/form.module.css';
import { Input, SelectField } from '@/components/ui/input';

const initialForm = { name: '', cpf: '', birthDate: '', password: '', role: 'USER' as Role };

export function CreateUserDialog() {
  const [open, setOpen] = useState(false);
  const [form, setForm] = useState(initialForm);
  const [error, setError] = useState('');
  const mutation = useCreateUser();

  async function submit(event: FormEvent) {
    event.preventDefault();
    setError('');
    if (!isValidCpf(form.cpf)) {
      setError('Informe um CPF válido.');
      return;
    }
    try {
      await mutation.mutateAsync(form);
      setForm(initialForm);
      setOpen(false);
    } catch (exception) {
      setError(getErrorMessage(exception));
    }
  }

  return (
    <>
      <Button onClick={() => setOpen(true)}>
        <UserPlus size={18} />
        Novo usuário
      </Button>
      <Dialog
        open={open}
        onOpenChange={setOpen}
        title="Novo usuário"
        description="Crie o acesso e defina o nível de permissão."
        footer={
          <>
            <Button variant="secondary" onClick={() => setOpen(false)}>
              Cancelar
            </Button>
            <Button type="submit" form="create-user" disabled={mutation.isPending}>
              {mutation.isPending ? 'Salvando...' : 'Criar usuário'}
            </Button>
          </>
        }
      >
        <form id="create-user" className={formStyles.grid} onSubmit={submit}>
          {error && <Alert className={formStyles.full}>{error}</Alert>}
          <Input
            fieldClassName={formStyles.full}
            label="Nome completo"
            name="name"
            placeholder="Ex.: Maria Oliveira"
            value={form.name}
            onChange={(e) => setForm({ ...form, name: e.target.value })}
            maxLength={160}
            required
          />
          <Input
            label="CPF"
            name="cpf"
            inputMode="numeric"
            placeholder="000.000.000-00"
            value={form.cpf}
            onChange={(e) => setForm({ ...form, cpf: formatCpf(e.target.value) })}
            required
          />
          <Input
            label="Data de nascimento"
            name="birthDate"
            type="date"
            value={form.birthDate}
            onChange={(e) => setForm({ ...form, birthDate: e.target.value })}
            required
          />
          <Input
            label="Senha inicial"
            name="password"
            type="password"
            minLength={8}
            hint="Mínimo de 8 caracteres"
            value={form.password}
            onChange={(e) => setForm({ ...form, password: e.target.value })}
            required
          />
          <SelectField
            label="Perfil"
            value={form.role}
            onChange={(e) => setForm({ ...form, role: e.target.value as Role })}
          >
            <option value="USER">Usuário comum</option>
            <option value="ADMIN">Administrador</option>
          </SelectField>
        </form>
      </Dialog>
    </>
  );
}
