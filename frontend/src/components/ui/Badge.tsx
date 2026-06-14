import type { ComponentPropsWithoutRef } from 'react'

import { classNames } from '../../lib/classNames'

type BadgeSize = 'sm' | 'md'
type BadgeVariant = 'neutral' | 'success' | 'warning' | 'danger' | 'info'

interface BadgeProps extends ComponentPropsWithoutRef<'span'> {
  size?: BadgeSize
  variant?: BadgeVariant
}

const sizeClasses: Record<BadgeSize, string> = {
  sm: 'px-2 py-0.5 text-xs',
  md: 'px-2.5 py-1 text-sm',
}

const variantClasses: Record<BadgeVariant, string> = {
  neutral: 'border-rc-border bg-rc-muted text-rc-secondary',
  success: 'border-rc-accent/25 bg-rc-accent-muted text-rc-accent-strong',
  warning: 'border-rc-warning/30 bg-rc-warning-muted text-rc-warning-strong',
  danger:
    'border-rc-destructive/25 bg-rc-destructive-muted text-rc-destructive-strong',
  info: 'border-rc-info/25 bg-rc-info-muted text-rc-info-strong',
}

export function Badge({
  children,
  className,
  size = 'sm',
  variant = 'neutral',
  ...props
}: BadgeProps) {
  return (
    <span
      className={classNames(
        'inline-flex items-center rounded-full border font-medium leading-none',
        sizeClasses[size],
        variantClasses[variant],
        className,
      )}
      {...props}
    >
      {children}
    </span>
  )
}
