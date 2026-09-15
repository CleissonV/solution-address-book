import type { PropsWithChildren, ReactNode } from 'react';
import * as DialogPrimitive from '@radix-ui/react-dialog';
import { X } from 'lucide-react';
import styles from './dialog.module.css';

interface DialogProps extends PropsWithChildren {
  open: boolean;
  onOpenChange: (open: boolean) => void;
  title: string;
  description?: string;
  footer?: ReactNode;
}

export function Dialog({ open, onOpenChange, title, description, footer, children }: DialogProps) {
  return (
    <DialogPrimitive.Root open={open} onOpenChange={onOpenChange}>
      <DialogPrimitive.Portal>
        <DialogPrimitive.Overlay className={styles.overlay} />
        <DialogPrimitive.Content className={styles.content}>
          <div className={styles.header}>
            <div>
              <DialogPrimitive.Title className={styles.title}>{title}</DialogPrimitive.Title>
              {description && (
                <DialogPrimitive.Description className={styles.description}>
                  {description}
                </DialogPrimitive.Description>
              )}
            </div>
            <DialogPrimitive.Close className={styles.close} aria-label="Fechar">
              <X size={20} />
            </DialogPrimitive.Close>
          </div>
          <div className={styles.body}>{children}</div>
          {footer && <div className={styles.footer}>{footer}</div>}
        </DialogPrimitive.Content>
      </DialogPrimitive.Portal>
    </DialogPrimitive.Root>
  );
}
