import { Check, MapPin, MoreHorizontal, Pencil, Star, Trash2 } from 'lucide-react'
import { useState } from 'react'
import { useDeleteAddress, useSetPrimary } from '../features/users/api'
import { getErrorMessage, formatZipCode } from '../lib/utils'
import type { Address } from '../types'
import { AddressDialog } from './AddressDialog'
import { Badge } from './ui/badge'
import { Button } from './ui/button'
import {
  AlertDialog,
  AlertDialogAction,
  AlertDialogCancel,
  AlertDialogContent,
  AlertDialogDescription,
  AlertDialogFooter,
  AlertDialogHeader,
  AlertDialogTitle,
} from './ui/alert-dialog'

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
    <article className={address.primary ? 'address-card address-card--primary' : 'address-card'}>
      <div className="address-card__top">
        <div className="address-card__icon"><MapPin size={21} /></div>
        <div className="address-card__title"><strong>{address.street}, {address.number}</strong><span>{address.complement || 'Sem complemento'}</span></div>
        {address.primary && <Badge tone="success"><Check size={13} />Principal</Badge>}
        {!readOnly ? <div className="menu-wrap">
          <Button size="icon" variant="ghost" aria-label="Ações do endereço" onClick={() => setMenuOpen(!menuOpen)}><MoreHorizontal size={20} /></Button>
          {menuOpen && <div className="action-menu">
            <button onClick={() => { setEditing(true); setMenuOpen(false) }}><Pencil size={16} />Editar</button>
            {!address.primary && <button onClick={setPrimary} disabled={primaryMutation.isPending}><Star size={16} />Tornar principal</button>}
            <button className="action-menu__danger" onClick={() => { setDeleteOpen(true); setMenuOpen(false) }}><Trash2 size={16} />Excluir</button>
          </div>}
        </div> : null}
      </div>
      <div className="address-card__details"><span>{address.neighborhood}</span><span>{address.city} / {address.state}</span><span>CEP {formatZipCode(address.zipCode)}</span></div>
      {error && <div className="alert alert--error">{error}</div>}
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
