import { forwardRef, type InputHTMLAttributes, type SelectHTMLAttributes } from 'react';
import { cn } from '@/lib/utils';
import styles from './input.module.css';

interface InputProps extends InputHTMLAttributes<HTMLInputElement> {
  label: string;
  error?: string;
  hint?: string;
  fieldClassName?: string;
}

export const Input = forwardRef<HTMLInputElement, InputProps>(
  ({ label, error, hint, className, fieldClassName, id, ...props }, ref) => {
    const inputId = id ?? props.name;
    return (
      <label className={cn(styles.field, fieldClassName)} htmlFor={inputId}>
        <span className={styles.label}>{label}</span>
        <input
          ref={ref}
          id={inputId}
          className={cn(styles.input, error && styles.inputError, className)}
          {...props}
        />
        {error ? (
          <span className={styles.error}>{error}</span>
        ) : hint ? (
          <span className={styles.hint}>{hint}</span>
        ) : null}
      </label>
    );
  },
);
Input.displayName = 'Input';

interface SelectFieldProps extends SelectHTMLAttributes<HTMLSelectElement> {
  label: string;
  fieldClassName?: string;
}

export function SelectField({
  label,
  className,
  fieldClassName,
  id,
  name,
  children,
  ...props
}: SelectFieldProps) {
  const selectId = id ?? name;
  return (
    <label className={cn(styles.field, fieldClassName)} htmlFor={selectId}>
      <span className={styles.label}>{label}</span>
      <select id={selectId} name={name} className={cn(styles.input, className)} {...props}>
        {children}
      </select>
    </label>
  );
}
