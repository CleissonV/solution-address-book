import { useEffect, useState, type FormEvent } from 'react'
import { CheckCircle2, LoaderCircle, MapPin, Plus } from 'lucide-react'
import { lookupPostalCode, useCreateAddress, useUpdateAddress } from '../features/users/api'
import { formatZipCode, getErrorMessage } from '../lib/utils'
import type { Address, PostalCode } from '../types'
import { Button } from './ui/button'
import { Dialog } from './ui/dialog'
import { Input } from './ui/input'

interface AddressDialogProps {
  userId: string
  address?: Address
  open?: boolean
  onOpenChange?: (open: boolean) => void
}

const emptyForm = { zipCode: '', number: '', complement: '', primary: false }

export function AddressDialog({ userId, address, open: controlledOpen, onOpenChange }: AddressDialogProps) {
  const [internalOpen, setInternalOpen] = useState(false)
  const open = controlledOpen ?? internalOpen
  const setOpen = onOpenChange ?? setInternalOpen
  const [form, setForm] = useState(emptyForm)
  const [postalCode, setPostalCode] = useState<PostalCode | null>(null)
  const [lookupLoading, setLookupLoading] = useState(false)
  const [error, setError] = useState('')
  const createMutation = useCreateAddress(userId)
  const updateMutation = useUpdateAddress(userId, address?.id ?? '')
  const mutation = address ? updateMutation : createMutation

  useEffect(() => {
    if (!open) return
    setForm(address ? { zipCode: formatZipCode(address.zipCode), number: address.number, complement: address.complement ?? '', primary: address.primary } : emptyForm)
    setPostalCode(address ? { zipCode: address.zipCode, street: address.street, neighborhood: address.neighborhood, city: address.city, state: address.state } : null)
    setError('')
  }, [address, open])

  async function searchZipCode() {
    const clean = form.zipCode.replace(/\D/g, '')
    if (clean.length !== 8) {
      setPostalCode(null)
      if (clean.length > 0) setError('Informe um CEP com 8 dígitos.')
      return
    }
    setLookupLoading(true)
    setError('')
    try {
      setPostalCode(await lookupPostalCode(clean))
    } catch (exception) {
      setPostalCode(null)
      setError(getErrorMessage(exception))
    } finally {
      setLookupLoading(false)
    }
  }

  async function submit(event: FormEvent) {
    event.preventDefault()
    setError('')
    if (!postalCode) {
      setError('Consulte um CEP válido antes de salvar.')
      return
    }
    try {
      await mutation.mutateAsync({ ...form, zipCode: form.zipCode.replace(/\D/g, '') })
      setOpen(false)
    } catch (exception) {
      setError(getErrorMessage(exception))
    }
  }

  return (
    <>
      {controlledOpen === undefined && <Button onClick={() => setOpen(true)}><Plus size={18} />Novo endereço</Button>}
      <Dialog open={open} onOpenChange={setOpen} title={address ? 'Editar endereço' : 'Novo endereço'}
        description="Informe o CEP para preencher automaticamente os dados."
        footer={<><Button variant="secondary" onClick={() => setOpen(false)}>Cancelar</Button><Button type="submit" form="address-form" disabled={mutation.isPending || lookupLoading}>{mutation.isPending ? 'Salvando...' : 'Salvar endereço'}</Button></>}>
        <form id="address-form" className="form-grid" onSubmit={submit}>
          {error && <div className="alert alert--error form-grid__full" role="alert">{error}</div>}
          <div className="zip-field">
            <Input label="CEP" name="zipCode" inputMode="numeric" placeholder="00000-000" value={form.zipCode}
              onChange={(e) => { setForm({ ...form, zipCode: formatZipCode(e.target.value) }); setPostalCode(null) }} onBlur={searchZipCode} required />
            <span className="zip-field__status">{lookupLoading ? <LoaderCircle className="spin" size={18} /> : postalCode ? <CheckCircle2 size={18} /> : null}</span>
          </div>
          <Input label="Número" name="number" placeholder="Ex.: 300" maxLength={20} value={form.number} onChange={(e) => setForm({ ...form, number: e.target.value })} required />
          <Input className="form-grid__full" label="Complemento" name="complement" placeholder="Sala, bloco ou referência (opcional)" maxLength={120} value={form.complement} onChange={(e) => setForm({ ...form, complement: e.target.value })} />
          {postalCode && <div className="postal-preview form-grid__full"><MapPin size={20} /><div><strong>{postalCode.street}</strong><span>{postalCode.neighborhood} · {postalCode.city}/{postalCode.state}</span></div></div>}
          <label className="check-field form-grid__full"><input type="checkbox" checked={form.primary} onChange={(e) => setForm({ ...form, primary: e.target.checked })} /><span><strong>Endereço principal</strong><small>Usado como referência padrão para este usuário.</small></span></label>
        </form>
      </Dialog>
    </>
  )
}
