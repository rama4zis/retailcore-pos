import type { ComponentPropsWithoutRef, ReactNode } from 'react'

import { classNames } from '../../lib/classNames'

interface PageHeaderProps extends ComponentPropsWithoutRef<'header'> {
  actions?: ReactNode
  description?: ReactNode
  eyebrow?: string
  meta?: ReactNode
  title: string
}

export function PageHeader({
  actions,
  className,
  description,
  eyebrow,
  meta,
  title,
  ...props
}: PageHeaderProps) {
  return (
    <header
      className={classNames(
        'flex flex-col gap-4 rounded-2xl border border-rc-border bg-rc-surface/85 px-5 py-5 shadow-rc-card backdrop-blur sm:flex-row sm:items-end sm:justify-between',
        className,
      )}
      {...props}
    >
      <div className="max-w-3xl space-y-2">
        {eyebrow ? (
          <p className="text-xs font-semibold uppercase tracking-[0.2em] text-rc-accent">
            {eyebrow}
          </p>
        ) : null}
        <div className="space-y-2">
          <h1 className="text-2xl font-semibold tracking-tight text-rc-foreground sm:text-3xl">
            {title}
          </h1>
          {description ? (
            <p className="text-sm leading-6 text-rc-secondary sm:text-base">
              {description}
            </p>
          ) : null}
        </div>
        {meta ? <div className="flex flex-wrap gap-2 pt-1">{meta}</div> : null}
      </div>
      {actions ? <div className="flex shrink-0 flex-wrap gap-2">{actions}</div> : null}
    </header>
  )
}
