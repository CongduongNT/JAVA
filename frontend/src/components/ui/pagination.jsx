import { cn } from '@/lib/utils'

function Pagination({ className, ...props }) {
  return (
    <nav
      role="navigation"
      aria-label="pagination"
      className={cn('mx-auto flex w-full items-center justify-center', className)}
      {...props}
    />
  )
}

function PaginationContent({ className, ...props }) {
  return (
    <ul
      className={cn('flex flex-row flex-wrap items-center gap-1 text-sm font-medium', className)}
      {...props}
    />
  )
}

function PaginationItem({ className, ...props }) {
  return <li className={cn('flex', className)} {...props} />
}

function PaginationLink({ className, isActive, ...props }) {
  return (
    <button
      type="button"
      aria-current={isActive ? 'page' : undefined}
      className={cn(
        'inline-flex h-9 min-w-9 items-center justify-center rounded-md border border-transparent px-3 text-sm transition-colors hover:bg-muted focus-visible:outline-none focus-visible:ring-2 focus-visible:ring-ring focus-visible:ring-offset-2',
        isActive
          ? 'bg-primary text-primary-foreground shadow'
          : 'border-border bg-background text-foreground',
        className
      )}
      {...props}
    />
  )
}

function PaginationPrevious({ className, ...props }) {
  return (
    <button
      type="button"
      className={cn(
        'inline-flex h-9 items-center rounded-md border border-border bg-background px-3 text-sm text-foreground transition-colors hover:bg-muted focus-visible:outline-none focus-visible:ring-2 focus-visible:ring-ring focus-visible:ring-offset-2',
        className
      )}
      {...props}
    >
      Prev
    </button>
  )
}

function PaginationNext({ className, ...props }) {
  return (
    <button
      type="button"
      className={cn(
        'inline-flex h-9 items-center rounded-md border border-border bg-background px-3 text-sm text-foreground transition-colors hover:bg-muted focus-visible:outline-none focus-visible:ring-2 focus-visible:ring-ring focus-visible:ring-offset-2',
        className
      )}
      {...props}
    >
      Next
    </button>
  )
}

function PaginationEllipsis({ className, ...props }) {
  return (
    <span
      className={cn(
        'inline-flex h-9 items-center justify-center px-2 text-sm text-muted-foreground',
        className
      )}
      {...props}
    >
      …
    </span>
  )
}

export {
  Pagination,
  PaginationContent,
  PaginationItem,
  PaginationLink,
  PaginationPrevious,
  PaginationNext,
  PaginationEllipsis,
}
