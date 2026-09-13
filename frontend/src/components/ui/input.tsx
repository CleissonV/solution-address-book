import { forwardRef, type InputHTMLAttributes } from 'react'
import { cn } from '@/lib/utils'

interface InputProps extends InputHTMLAttributes<HTMLInputElement> {
  label: string
  error?: string
  hint?: string
}

export const Input = forwardRef<HTMLInputElement, InputProps>(
  ({ label, error, hint, className, id, ...props }, ref) => {
    const inputId = id ?? props.name
    return (
      <label className="field" htmlFor={inputId}>
        <span className="field__label">{label}</span>
        <input ref={ref} id={inputId} className={cn('input', error && 'input--error', className)} {...props} />
        {error ? <span className="field__error">{error}</span> : hint ? <span className="field__hint">{hint}</span> : null}
      </label>
    )
  },
)
Input.displayName = 'Input'
