import type { ComponentPropsWithoutRef } from 'react'

import { classNames } from '../../lib/classNames'

type SkeletonRadius = 'sm' | 'md' | 'lg' | 'full'

interface SkeletonProps extends ComponentPropsWithoutRef<'div'> {
  lines?: number
  radius?: SkeletonRadius
}

const radiusClasses: Record<SkeletonRadius, string> = {
  sm: 'rounded',
  md: 'rounded-md',
  lg: 'rounded-xl',
  full: 'rounded-full',
}

export function Skeleton({
  className,
  lines = 1,
  radius = 'md',
  ...props
}: SkeletonProps) {
  if (lines <= 1) {
    return (
      <div
        aria-hidden="true"
        className={classNames(
          'h-4 animate-pulse bg-rc-muted',
          radiusClasses[radius],
          className,
        )}
        {...props}
      />
    )
  }

  return (
    <div aria-hidden="true" className={classNames('space-y-3', className)} {...props}>
      {Array.from({ length: lines }, (_, index) => (
        <div
          className={classNames(
            'h-4 animate-pulse bg-rc-muted',
            radiusClasses[radius],
            index === lines - 1 ? 'w-2/3' : 'w-full',
          )}
          key={index}
        />
      ))}
    </div>
  )
}
