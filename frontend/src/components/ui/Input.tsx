import { useId, type ComponentPropsWithoutRef } from 'react'

import { classNames } from '../../lib/classNames'

interface InputProps extends ComponentPropsWithoutRef<'input'> {
  containerClassName?: string
  error?: string
  helperText?: string
  hideLabel?: boolean
  label: string
}

export function Input({
  className,
  containerClassName,
  error,
  helperText,
  hideLabel = false,
  id,
  label,
  required,
  ...props
}: InputProps) {
  const generatedId = useId()
  const inputId = id ?? `${generatedId}-input`
  const helperId = helperText ? `${inputId}-helper` : undefined
  const errorId = error ? `${inputId}-error` : undefined
  const describedBy = [helperId, errorId].filter(Boolean).join(' ') || undefined

  return (
    <div className={classNames('space-y-2', containerClassName)}>
      <label
        className={classNames(
          'text-sm font-medium text-rc-foreground',
          hideLabel ? 'sr-only' : 'block',
        )}
        htmlFor={inputId}
      >
        {label}
        {required ? (
          <span aria-hidden="true" className="ml-1 text-rc-destructive">
            *
          </span>
        ) : null}
      </label>
      <input
        aria-describedby={describedBy}
        aria-invalid={error ? true : undefined}
        className={classNames(
          'block min-h-11 w-full rounded-lg border bg-rc-surface px-3 py-2 text-sm text-rc-foreground shadow-sm transition-colors placeholder:text-rc-secondary/60 focus:border-rc-primary focus:outline-none focus:ring-2 focus:ring-rc-ring/20 disabled:cursor-not-allowed disabled:bg-rc-muted disabled:text-rc-secondary',
          error
            ? 'border-rc-destructive focus:border-rc-destructive focus:ring-rc-destructive/20'
            : 'border-rc-border',
          className,
        )}
        id={inputId}
        required={required}
        {...props}
      />
      {helperText ? (
        <p className="text-sm leading-5 text-rc-secondary" id={helperId}>
          {helperText}
        </p>
      ) : null}
      {error ? (
        <p className="text-sm font-medium leading-5 text-rc-destructive" id={errorId}>
          {error}
        </p>
      ) : null}
    </div>
  )
}
