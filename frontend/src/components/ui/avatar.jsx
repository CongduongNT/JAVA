import * as React from 'react'
import { cn } from '@/lib/utils'

function Avatar({ className, size = 'default', children, ...props }) {
  return (
    <span
      data-slot="avatar"
      data-size={size}
      className={cn(
        'relative flex size-8 shrink-0 overflow-hidden rounded-full select-none',
        size === 'lg' && 'size-10',
        size === 'sm' && 'size-6',
        className
      )}
      {...props}
    >
      {children}
    </span>
  )
}

function AvatarImage({ className, src, alt, onError, ...props }) {
  const [failed, setFailed] = React.useState(false)

  if (!src || failed) return null

  return (
    <img
      data-slot="avatar-image"
      src={src}
      alt={alt || ''}
      className={cn('aspect-square size-full object-cover', className)}
      onError={() => setFailed(true)}
      {...props}
    />
  )
}

function AvatarFallback({ className, children, ...props }) {
  return (
    <span
      data-slot="avatar-fallback"
      className={cn(
        'flex size-full items-center justify-center rounded-full bg-muted text-sm font-medium text-muted-foreground',
        className
      )}
      {...props}
    >
      {children}
    </span>
  )
}

function AvatarBadge({ className, ...props }) {
  return (
    <span
      data-slot="avatar-badge"
      className={cn(
        'absolute right-0 bottom-0 z-10 inline-flex items-center justify-center rounded-full bg-primary text-primary-foreground ring-2 ring-background',
        className
      )}
      {...props}
    />
  )
}

function AvatarGroup({ className, ...props }) {
  return (
    <div
      data-slot="avatar-group"
      className={cn('flex -space-x-2', className)}
      {...props}
    />
  )
}

function AvatarGroupCount({ className, ...props }) {
  return (
    <div
      data-slot="avatar-group-count"
      className={cn(
        'relative flex size-8 shrink-0 items-center justify-center rounded-full bg-muted text-sm text-muted-foreground ring-2 ring-background',
        className
      )}
      {...props}
    />
  )
}

export {
  Avatar,
  AvatarImage,
  AvatarFallback,
  AvatarGroup,
  AvatarGroupCount,
  AvatarBadge,
}
