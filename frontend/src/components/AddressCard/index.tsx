import { Check, MapPin, MoreHorizontal, Pencil, Star, Trash2 } from 'lucide-react'
import { useState } from 'react'
import { useDeleteAddress, useSetPrimary } from '@/features/users/api'
import { cn, getErrorMessage, formatZipCode } from '@/lib/utils'
import type { Address } from '@/types'
import { AddressDialog } from '@/components/AddressDialog'
import { Badge } from '@/components/ui/badge'
import { Button } from '@/components/ui/button'
import { Alert } from '@/components/ui/feedback'
import {
  AlertDialog,
  AlertDialogAction,
  AlertDialogCancel,
  AlertDialogContent,
  AlertDialogDescription,
  AlertDialogFooter,
  AlertDialogHeader,
  AlertDialogTitle,
} from '@/components/ui/alert-dialog'
import styles from './styles.module.css'

export function AddressCard({ address, userId, readOnly = false }: { address: Address; userId: string; readOnly?: boolean }) {
  const [editing, setEditing] = useState(false)
  const [menuOpen, setMenuOpen] = useState(false)
  const [deleteOpen, setDeleteOpen] = useState(false)
  const [error, setError] = useState('')
  const primaryMutation = useSetPrimary(userId)
  const deleteMutation = useDeleteAddress(userId)

  async function setPrimary() {
    setError('')
    try { await primaryMutation.mutateAsync(address.id); setMenuOpen(false) }
    catch (exception) { setError(getErrorMessage(exception)) }
  }

  async function remove() {
    setError('')
    try { await deleteMutation.mutateAsync(address.id); setDeleteOpen(false) }
    catch (exception) { setError(getErrorMessage(exception)) }
  }

  return (
    <article className={cn(styles.card, address.primary && styles.primary)} data-testid="address-card">
      <div className={styles.top}>
        <div className={styles.icon}><MapPin size={21} /></div>
        <div className={styles.title}><strong>{address.street}, {address.number}</strong><span>{address.complement || 'Sem complemento'}</span></div>
        {address.primary && <Badge tone="success"><Check size={13} />Principal</Badge>}
        {!readOnly ? <div className={styles.menuWrap}>
          <Button size="icon" variant="ghost" aria-label="Ações do endereço" onClick={() => setMenuOpen(!menuOpen)}><MoreHorizontal size={20} /></Button>
          {menuOpen && <div className={styles.menu}>
            <button onClick={() => { setEditing(true); setMenuOpen(false) }}><Pencil size={16} />Editar</button>
            {!address.primary && <button onClick={setPrimary} disabled={primaryMutation.isPending}><Star size={16} />Tornar principal</button>}
            <button className={styles.danger} onClick={() => { setDeleteOpen(true); setMenuOpen(false) }}><Trash2 size={16} />Excluir</button>
          </div>}
        </div> : null}
      </div>
      <div className={styles.details}><span>{address.neighborhood}</span><span>{address.city} / {address.state}</span><span>CEP {formatZipCode(address.zipCode)}</span></div>
      {error && <Alert className={styles.error}>{error}</Alert>}
      {!readOnly ? <AddressDialog userId={userId} address={address} open={editing} onOpenChange={setEditing} /> : null}
      {!readOnly ? <AlertDialog open={deleteOpen} onOpenChange={setDeleteOpen}>
        <AlertDialogContent>
          <AlertDialogHeader>
            <div>
              <AlertDialogTitle>Excluir endereço?</AlertDialogTitle>
              <AlertDialogDescription>
                {address.street}, {address.number} será removido permanentemente. Esta ação não pode ser desfeita.
              </AlertDialogDescription>
            </div>
          </AlertDialogHeader>
          <AlertDialogFooter>
            <AlertDialogCancel>Cancelar</AlertDialogCancel>
            <AlertDialogAction
              onClick={(event) => { event.preventDefault(); void remove() }}
              disabled={deleteMutation.isPending}
            >
              {deleteMutation.isPending ? 'Excluindo…' : 'Excluir endereço'}
            </AlertDialogAction>
          </AlertDialogFooter>
        </AlertDialogContent>
      </AlertDialog> : null}
    </article>
  )
}
