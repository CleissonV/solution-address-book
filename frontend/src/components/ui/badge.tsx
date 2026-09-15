import type { PropsWithChildren } from 'react';
import { cn } from '@/lib/utils';
import styles from './badge.module.css';

export function Badge({
  children,
  tone = 'neutral',
}: PropsWithChildren<{ tone?: 'neutral' | 'brand' | 'success' | 'danger' }>) {
  return <span className={cn(styles.badge, styles[tone])}>{children}</span>;
}
