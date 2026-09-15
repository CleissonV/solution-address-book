import type { HTMLAttributes, PropsWithChildren } from 'react';
import { cn } from '@/lib/utils';
import styles from './feedback.module.css';

type AlertTone = 'error' | 'warning';

export function Alert({
  tone = 'error',
  className,
  ...props
}: HTMLAttributes<HTMLDivElement> & { tone?: AlertTone }) {
  return <div className={cn(styles.alert, styles[tone], className)} role="alert" {...props} />;
}

export function Spinner({ light = false, className }: { light?: boolean; className?: string }) {
  return (
    <span
      className={cn(styles.spinner, light && styles.spinnerLight, className)}
      aria-hidden="true"
    />
  );
}

export function AppLoader({ children }: PropsWithChildren) {
  return (
    <div className={styles.loader}>
      <Spinner />
      {children}
    </div>
  );
}
