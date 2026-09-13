import { Camera, ImagePlus, Trash2 } from 'lucide-react'
import { useEffect, useState, type ChangeEvent, type FormEvent } from 'react'
import { useDeleteProfilePhoto, useUploadProfilePhoto } from '../features/users/api'
import { getErrorMessage } from '../lib/utils'
import type { UserDetails } from '../types'
import { ProfileAvatar } from './ProfileAvatar'
import { Button } from './ui/button'
import { Dialog } from './ui/dialog'

const MAX_PHOTO_SIZE = 2 * 1024 * 1024
const ACCEPTED_TYPES = new Set(['image/png', 'image/jpeg'])

interface ProfilePhotoDialogProps {
  user: UserDetails
  onUpdated: (version?: number) => void
}

export function ProfilePhotoDialog({ user, onUpdated }: ProfilePhotoDialogProps) {
  const [open, setOpen] = useState(false)
  const [file, setFile] = useState<File>()
  const [previewUrl, setPreviewUrl] = useState<string>()
  const [removeCurrent, setRemoveCurrent] = useState(false)
  const [error, setError] = useState('')
  const upload = useUploadProfilePhoto(user.id)
  const remove = useDeleteProfilePhoto(user.id)
  const pending = upload.isPending || remove.isPending

  useEffect(() => {
    if (!file) {
      setPreviewUrl(undefined)
      return
    }
    const objectUrl = URL.createObjectURL(file)
    setPreviewUrl(objectUrl)
    return () => URL.revokeObjectURL(objectUrl)
  }, [file])

  function changeOpen(nextOpen: boolean) {
    setOpen(nextOpen)
    if (nextOpen) {
      setFile(undefined)
      setRemoveCurrent(false)
      setError('')
    }
  }

  function selectPhoto(event: ChangeEvent<HTMLInputElement>) {
    const selected = event.target.files?.[0]
    setError('')
    if (!selected) return
    if (!ACCEPTED_TYPES.has(selected.type)) {
      setFile(undefined)
      setError('Use uma imagem PNG ou JPEG.')
      event.target.value = ''
      return
    }
    if (selected.size > MAX_PHOTO_SIZE) {
      setFile(undefined)
      setError('A foto deve ter no máximo 2 MB.')
      event.target.value = ''
      return
    }
    setFile(selected)
    setRemoveCurrent(false)
  }

  async function submit(event: FormEvent) {
    event.preventDefault()
    setError('')
    try {
      if (file) {
        const result = await upload.mutateAsync(file)
        onUpdated(result.version)
      } else if (removeCurrent && user.profilePhotoVersion !== undefined) {
        await remove.mutateAsync()
        onUpdated()
      } else {
        setError('Escolha uma nova foto ou marque a atual para remoção.')
        return
      }
      setOpen(false)
    } catch (exception) {
      setError(getErrorMessage(exception))
    }
  }

  return (
    <>
      <Button variant="secondary" onClick={() => changeOpen(true)}><Camera size={18} />Alterar foto</Button>
      <Dialog
        open={open}
        onOpenChange={changeOpen}
        title="Foto de perfil"
        description="Use uma imagem PNG ou JPEG de até 2 MB."
        footer={(
          <>
            <Button variant="secondary" onClick={() => setOpen(false)}>Cancelar</Button>
            <Button type="submit" form="profile-photo" disabled={pending}>
              {pending ? 'Salvando...' : 'Salvar foto'}
            </Button>
          </>
        )}
      >
        <form id="profile-photo" className="photo-form" onSubmit={submit}>
          {error ? <div className="alert alert--error" role="alert">{error}</div> : null}
          <div className="photo-preview">
            {previewUrl ? <img src={previewUrl} alt="Pré-visualização da nova foto" /> : (
              <ProfileAvatar
                user={{ ...user, profilePhotoVersion: removeCurrent ? undefined : user.profilePhotoVersion }}
                size="xl"
              />
            )}
            <div><strong>{file ? file.name : user.name}</strong><span>{file ? 'Nova foto selecionada' : removeCurrent ? 'Foto atual será removida' : 'Foto atual'}</span></div>
          </div>
          <label className="file-picker">
            <span className="field__label">Escolher foto</span>
            <span className="file-picker__control">
              <span className="file-picker__button">Selecionar foto</span>
              <span className="file-picker__name" aria-live="polite">
                {file?.name ?? 'Nenhum arquivo selecionado'}
              </span>
            </span>
            <input
              className="file-picker__input"
              name="profilePhoto"
              type="file"
              accept="image/png,image/jpeg"
              onChange={selectPhoto}
            />
          </label>
          {user.profilePhotoVersion !== undefined && !file ? (
            <Button className="photo-remove" variant="danger" size="small" onClick={() => setRemoveCurrent(!removeCurrent)}>
              <Trash2 size={16} />{removeCurrent ? 'Manter foto atual' : 'Remover foto atual'}
            </Button>
          ) : (
            <p className="photo-form__hint"><ImagePlus size={16} />A imagem será recortada visualmente para caber no avatar.</p>
          )}
        </form>
      </Dialog>
    </>
  )
}
