import { forwardRef, type ButtonHTMLAttributes, type ReactNode } from 'react'

import { classNames } from '../../lib/classNames'
import { Spinner } from '../feedback/Spinner'

type ButtonSize = 'sm' | 'md' | 'lg'
type ButtonVariant = 'primary' | 'secondary' | 'ghost' | 'danger' | 'success'

interface ButtonProps extends ButtonHTMLAttributes<HTMLButtonElement> {
  isLoading?: boolean
  leftIcon?: ReactNode
  loadingText?: string
  rightIcon?: ReactNode
  size?: ButtonSize
  variant?: ButtonVariant
}

const sizeClasses: Record<ButtonSize, string> = {
  sm: 'min-h-9 px-3 text-sm',
  md: 'min-h-11 px-4 text-sm',
  lg: 'min-h-12 px-5 text-base',
}

const variantClasses: Record<ButtonVariant, string> = {
  primary:
    'border-rc-primary bg-rc-primary text-rc-on-primary hover:bg-rc-primary-strong',
  secondary:
    'border-rc-border bg-rc-surface text-rc-primary hover:bg-rc-muted',
  ghost: 'border-transparent bg-transparent text-rc-secondary hover:bg-rc-muted',
  danger:
    'border-rc-destructive bg-rc-destructive text-white hover:bg-rc-destructive-strong',
  success: 'border-rc-accent bg-rc-accent text-white hover:bg-rc-accent-strong',
}

export const Button = forwardRef<HTMLButtonElement, ButtonProps>(function Button({
  children,
  className,
  disabled,
  isLoading = false,
  leftIcon,
  loadingText,
  rightIcon,
  size = 'md',
  type = 'button',
  variant = 'primary',
  ...props
}, ref) {
  const isDisabled = disabled || isLoading

  return (
    <button
      className={classNames(
        'inline-flex items-center justify-center gap-2 rounded-lg border font-semibold shadow-sm transition-colors focus-visible:outline-2 focus-visible:outline-offset-2 focus-visible:outline-rc-ring disabled:cursor-not-allowed disabled:opacity-60',
        sizeClasses[size],
        variantClasses[variant],
        className,
      )}
      disabled={isDisabled}
      ref={ref}
      type={type}
      {...props}
    >
      {isLoading ? <Spinner label={loadingText ?? 'Loading'} size="sm" /> : leftIcon}
      <span>{isLoading && loadingText ? loadingText : children}</span>
      {!isLoading ? rightIcon : null}
    </button>
  )
})
