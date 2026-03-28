"use client"

import * as React from 'react'
import { Select as SelectPrimitive } from '@base-ui/react/select'
import { Check, ChevronDown } from 'lucide-react'

import { cn } from '@/lib/utils'

function Select({ children, ...props }) {
  return <SelectPrimitive data-slot="select" {...props}>{children}</SelectPrimitive>
}

function SelectTrigger({ className, ...props }) {
  return (
    <SelectPrimitive.Trigger
      data-slot="select-trigger"
      className={cn(
        'flex h-8 w-full items-center justify-between gap-2 rounded-lg border border-input bg-background px-2.5 py-1 text-sm outline-none transition focus-visible:border-ring focus-visible:ring-3 focus-visible:ring-ring/50 disabled:cursor-not-allowed disabled:opacity-50',
        className
      )}
      {...props}
    >
      <SelectPrimitive.Value data-slot="select-value" className="truncate text-foreground" />
      <SelectPrimitive.Icon data-slot="select-icon">
        <ChevronDown className="size-4 text-muted-foreground" />
      </SelectPrimitive.Icon>
    </SelectPrimitive.Trigger>
  )
}

function SelectContent({ className, position = 'popper', ...props }) {
  return (
    <SelectPrimitive.Portal>
      <SelectPrimitive.Positioner sideOffset={4} position={position}>
        <SelectPrimitive.Content
          data-slot="select-content"
          className={cn(
            'z-50 min-w-[var(--select-trigger-width)] rounded-lg border border-border bg-popover text-popover-foreground shadow-md animate-in fade-in-0 zoom-in-95 data-[state=closed]:animate-out data-[state=closed]:fade-out-0 data-[state=closed]:zoom-out-95',
            className
          )}
          {...props}
        />
      </SelectPrimitive.Positioner>
    </SelectPrimitive.Portal>
  )
}

function SelectItem({ className, children, ...props }) {
  return (
    <SelectPrimitive.Item
      data-slot="select-item"
      className={cn(
        'relative flex cursor-default select-none items-center gap-2 rounded-md px-2.5 py-1.5 text-sm outline-none transition-colors data-[disabled]:pointer-events-none data-[disabled]:opacity-50 data-[highlighted]:bg-accent data-[highlighted]:text-accent-foreground',
        className
      )}
      {...props}
    >
      <SelectPrimitive.ItemIndicator className="absolute left-2">
        <Check className="size-4" />
      </SelectPrimitive.ItemIndicator>
      <SelectPrimitive.ItemText>{children}</SelectPrimitive.ItemText>
    </SelectPrimitive.Item>
  )
}

function SelectLabel({ className, ...props }) {
  return (
    <SelectPrimitive.Label
      data-slot="select-label"
      className={cn('px-2.5 py-1 text-xs font-medium text-muted-foreground', className)}
      {...props}
    />
  )
}

function SelectSeparator({ className, ...props }) {
  return (
    <SelectPrimitive.Separator
      data-slot="select-separator"
      className={cn('my-1 h-px bg-border', className)}
      {...props}
    />
  )
}

const SelectValue = SelectPrimitive.Value

export {
  Select,
  SelectTrigger,
  SelectContent,
  SelectItem,
  SelectLabel,
  SelectSeparator,
  SelectValue,
}
