import type { ComponentPropsWithoutRef, ReactNode } from 'react'

import { classNames } from '../../lib/classNames'

interface EmptyStateProps extends ComponentPropsWithoutRef<'section'> {
  action?: ReactNode
  description: string
  icon?: ReactNode
  title: string
}

export function EmptyState({
  action,
  className,
  description,
  icon,
  title,
  ...props
}: EmptyStateProps) {
  return (
    <section
      className={classNames(
        'rounded-2xl border border-dashed border-rc-border bg-rc-surface px-6 py-10 text-center shadow-sm',
        className,
      )}
      {...props}
    >
      {icon ? (
        <div className="mx-auto mb-4 flex size-12 items-center justify-center rounded-full bg-rc-muted text-rc-secondary">
          {icon}
        </div>
      ) : null}
      <h2 className="text-lg font-semibold tracking-tight text-rc-foreground">{title}</h2>
      <p className="mx-auto mt-2 max-w-md text-sm leading-6 text-rc-secondary">
        {description}
      </p>
      {action ? <div className="mt-5 flex justify-center">{action}</div> : null}
    </section>
  )
}
