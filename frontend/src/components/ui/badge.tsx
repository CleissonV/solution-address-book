import type { PropsWithChildren } from 'react'
import { cn } from '@/lib/utils'

export function Badge({ children, tone = 'neutral' }: PropsWithChildren<{ tone?: 'neutral' | 'brand' | 'success' | 'danger' }>) {
  return <span className={cn('badge', `badge--${tone}`)}>{children}</span>
}
