import type { ComponentPropsWithoutRef } from 'react'

import { classNames } from '../../lib/classNames'

type SpinnerSize = 'sm' | 'md' | 'lg'

interface SpinnerProps extends ComponentPropsWithoutRef<'span'> {
  label?: string
  size?: SpinnerSize
}

const sizeClasses: Record<SpinnerSize, string> = {
  sm: 'size-4 border-2',
  md: 'size-5 border-2',
  lg: 'size-8 border-[3px]',
}

export function Spinner({
  className,
  label = 'Loading',
  size = 'md',
  ...props
}: SpinnerProps) {
  return (
    <span
      aria-live="polite"
      className={classNames('inline-flex items-center justify-center', className)}
      role="status"
      {...props}
    >
      <span
        aria-hidden="true"
        className={classNames(
          'animate-spin rounded-full border-current border-r-transparent text-current',
          sizeClasses[size],
        )}
      />
      <span className="sr-only">{label}</span>
    </span>
  )
}
