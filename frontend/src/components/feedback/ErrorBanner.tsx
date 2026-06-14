import type { ComponentPropsWithoutRef, ReactNode } from 'react'

import { classNames } from '../../lib/classNames'

interface ErrorBannerProps extends ComponentPropsWithoutRef<'div'> {
  action?: ReactNode
  message: string
  title?: string
}

export function ErrorBanner({
  action,
  className,
  message,
  title = 'Something went wrong',
  ...props
}: ErrorBannerProps) {
  return (
    <div
      className={classNames(
        'rounded-xl border border-rc-destructive/25 bg-rc-destructive-muted px-4 py-3 text-rc-destructive-strong shadow-sm',
        className,
      )}
      role="alert"
      {...props}
    >
      <div className="flex flex-col gap-3 sm:flex-row sm:items-start sm:justify-between">
        <div className="space-y-1">
          <p className="text-sm font-semibold">{title}</p>
          <p className="text-sm leading-6">{message}</p>
        </div>
        {action ? <div className="shrink-0">{action}</div> : null}
      </div>
    </div>
  )
}
