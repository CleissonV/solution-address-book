import { useEffect, useState } from 'react';
import { useProfilePhoto } from '@/features/users/api';
import { cn } from '@/lib/utils';
import type { UserSummary } from '@/types';
import { Avatar, AvatarFallback, AvatarImage } from '@/components/ui/avatar';
import styles from './styles.module.css';

interface ProfileAvatarProps {
  user: Pick<UserSummary, 'id' | 'name' | 'profilePhotoVersion'>;
  size?: 'default' | 'large' | 'xl';
  className?: string;
}

function initials(name: string) {
  return (
    name
      .trim()
      .split(/\s+/)
      .slice(0, 2)
      .map((part) => part.charAt(0))
      .join('')
      .toUpperCase() || '?'
  );
}

export function ProfileAvatar({ user, size = 'default', className }: ProfileAvatarProps) {
  const { data: photo } = useProfilePhoto(user.id, user.profilePhotoVersion);
  const [source, setSource] = useState<string>();

  useEffect(() => {
    if (!photo) {
      setSource(undefined);
      return;
    }
    const objectUrl = URL.createObjectURL(photo);
    setSource(objectUrl);
    return () => URL.revokeObjectURL(objectUrl);
  }, [photo]);

  return (
    <Avatar className={cn(size === 'large' && styles.large, size === 'xl' && styles.xl, className)}>
      {source ? <AvatarImage src={source} alt={`Foto de ${user.name}`} /> : null}
      <AvatarFallback aria-label={`Avatar de ${user.name}`}>{initials(user.name)}</AvatarFallback>
    </Avatar>
  );
}
