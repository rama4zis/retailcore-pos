import { useId, type ComponentPropsWithoutRef, type ReactNode } from 'react'

import { classNames } from '../../lib/classNames'

interface CardProps extends Omit<ComponentPropsWithoutRef<'section'>, 'title'> {
  actions?: ReactNode
  description?: ReactNode
  title?: ReactNode
}

export function Card({
  actions,
  children,
  className,
  description,
  title,
  ...props
}: CardProps) {
  const titleId = useId()
  const hasHeader = title || description || actions

  return (
    <section
      aria-labelledby={title ? titleId : undefined}
      className={classNames(
        'rounded-2xl border border-rc-border bg-rc-surface text-rc-foreground shadow-rc-card',
        className,
      )}
      {...props}
    >
      {hasHeader ? (
        <div className="flex flex-col gap-3 border-b border-rc-border px-5 py-4 sm:flex-row sm:items-start sm:justify-between">
          <div className="space-y-1">
            {title ? (
              <h2 className="text-base font-semibold tracking-tight" id={titleId}>
                {title}
              </h2>
            ) : null}
            {description ? (
              <p className="text-sm leading-6 text-rc-secondary">{description}</p>
            ) : null}
          </div>
          {actions ? <div className="shrink-0">{actions}</div> : null}
        </div>
      ) : null}
      <div className="p-5">{children}</div>
    </section>
  )
}
