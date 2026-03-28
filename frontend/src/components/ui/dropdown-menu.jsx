"use client"

import * as React from 'react'
import { Menu as MenuPrimitive } from '@base-ui/react/menu'
import { ChevronRight, Check } from 'lucide-react'

import { cn } from '@/lib/utils'

function DropdownMenu(props) {
  return <MenuPrimitive.Root data-slot="dropdown-menu" {...props} />
}

function DropdownMenuPortal(props) {
  return <MenuPrimitive.Portal data-slot="dropdown-menu-portal" {...props} />
}

function DropdownMenuTrigger({ className, ...props }) {
  return (
    <MenuPrimitive.Trigger
      data-slot="dropdown-menu-trigger"
      className={cn('outline-none', className)}
      {...props}
    />
  )
}

function DropdownMenuContent({
  align = 'start',
  alignOffset = 0,
  side = 'bottom',
  sideOffset = 4,
  className,
  ...props
}) {
  return (
    <MenuPrimitive.Portal>
      <MenuPrimitive.Positioner
        className="isolate z-50 outline-none"
        align={align}
        alignOffset={alignOffset}
        side={side}
        sideOffset={sideOffset}
      >
        <MenuPrimitive.Content
          data-slot="dropdown-menu-content"
          className={cn(
            'min-w-52 rounded-lg border border-border bg-popover p-1.5 text-popover-foreground shadow-xl focus-visible:outline-none data-[state=open]:animate-in data-[state=closed]:animate-out data-[state=closed]:fade-out-0 data-[state=closed]:zoom-out-95 data-[state=open]:fade-in-0 data-[state=open]:zoom-in-95',
            className
          )}
          {...props}
        />
      </MenuPrimitive.Positioner>
    </MenuPrimitive.Portal>
  )
}

function DropdownMenuLabel({ inset, className, ...props }) {
  return (
    <MenuPrimitive.Label
      data-slot="dropdown-menu-label"
      className={cn(
        'px-2.5 py-1.5 text-xs font-medium text-muted-foreground',
        inset && 'pl-8',
        className
      )}
      {...props}
    />
  )
}

function DropdownMenuItem({ inset, className, ...props }) {
  return (
    <MenuPrimitive.Item
      data-slot="dropdown-menu-item"
      className={cn(
        'relative flex w-full cursor-default select-none items-center gap-2 rounded-md px-2.5 py-2 text-sm outline-none transition-colors data-[disabled]:pointer-events-none data-[disabled]:opacity-50 data-[highlighted]:bg-accent data-[highlighted]:text-accent-foreground',
        inset && 'pl-8',
        className
      )}
      {...props}
    />
  )
}

function DropdownMenuGroup(props) {
  return <MenuPrimitive.Group data-slot="dropdown-menu-group" {...props} />
}

function DropdownMenuPortalItem(props) {
  return <MenuPrimitive.Portal data-slot="dropdown-menu-portal-item" {...props} />
}

function DropdownMenuSub(props) {
  return <MenuPrimitive.Sub data-slot="dropdown-menu-sub" {...props} />
}

function DropdownMenuSubTrigger({ inset, className, children, ...props }) {
  return (
    <MenuPrimitive.SubTrigger
      data-slot="dropdown-menu-sub-trigger"
      className={cn(
        'flex cursor-default select-none items-center gap-2 rounded-md px-2.5 py-2 text-sm outline-none data-[disabled]:pointer-events-none data-[disabled]:opacity-50 data-[state=open]:bg-accent data-[state=open]:text-accent-foreground',
        inset && 'pl-8',
        className
      )}
      {...props}
    >
      {children}
      <ChevronRight className="ml-auto size-4" />
    </MenuPrimitive.SubTrigger>
  )
}

function DropdownMenuSubContent({ className, ...props }) {
  return (
    <MenuPrimitive.SubContent
      data-slot="dropdown-menu-sub-content"
      className={cn(
        'min-w-48 rounded-lg border border-border bg-popover p-1.5 text-popover-foreground shadow-xl focus-visible:outline-none data-[state=open]:animate-in data-[state=closed]:animate-out data-[state=closed]:fade-out-0 data-[state=closed]:zoom-out-95 data-[state=open]:fade-in-0 data-[state=open]:zoom-in-95',
        className
      )}
      {...props}
    />
  )
}

function DropdownMenuSeparator({ className, ...props }) {
  return (
    <MenuPrimitive.Separator
      data-slot="dropdown-menu-separator"
      className={cn('my-1 h-px bg-border', className)}
      {...props}
    />
  )
}

function DropdownMenuCheckboxItem({ className, children, checked, ...props }) {
  return (
    <MenuPrimitive.CheckboxItem
      data-slot="dropdown-menu-checkbox-item"
      className={cn(
        'relative flex cursor-default select-none items-center rounded-md py-2 pl-8 pr-2.5 text-sm outline-none transition-colors data-[disabled]:pointer-events-none data-[disabled]:opacity-50 data-[highlighted]:bg-accent data-[highlighted]:text-accent-foreground',
        className
      )}
      checked={checked}
      {...props}
    >
      <span className="absolute left-2.5 flex size-4 items-center justify-center">
        <MenuPrimitive.ItemIndicator>
          <Check className="size-4" />
        </MenuPrimitive.ItemIndicator>
      </span>
      {children}
    </MenuPrimitive.CheckboxItem>
  )
}

function DropdownMenuRadioGroup(props) {
  return <MenuPrimitive.RadioGroup data-slot="dropdown-menu-radio-group" {...props} />
}

function DropdownMenuRadioItem({ className, children, ...props }) {
  return (
    <MenuPrimitive.RadioItem
      data-slot="dropdown-menu-radio-item"
      className={cn(
        'relative flex cursor-default select-none items-center rounded-md py-2 pl-8 pr-2.5 text-sm outline-none transition-colors data-[disabled]:pointer-events-none data-[disabled]:opacity-50 data-[highlighted]:bg-accent data-[highlighted]:text-accent-foreground',
        className
      )}
      {...props}
    >
      <span className="absolute left-2.5 flex size-4 items-center justify-center">
        <MenuPrimitive.ItemIndicator>
          <div className="size-2.5 rounded-full bg-primary" />
        </MenuPrimitive.ItemIndicator>
      </span>
      {children}
    </MenuPrimitive.RadioItem>
  )
}

function DropdownMenuShortcut({ className, ...props }) {
  return (
    <span
      data-slot="dropdown-menu-shortcut"
      className={cn('ml-auto text-xs tracking-widest text-muted-foreground', className)}
      {...props}
    />
  )
}

export {
  DropdownMenu,
  DropdownMenuTrigger,
  DropdownMenuContent,
  DropdownMenuItem,
  DropdownMenuCheckboxItem,
  DropdownMenuRadioItem,
  DropdownMenuLabel,
  DropdownMenuSeparator,
  DropdownMenuShortcut,
  DropdownMenuGroup,
  DropdownMenuPortal,
  DropdownMenuSub,
  DropdownMenuSubTrigger,
  DropdownMenuSubContent,
  DropdownMenuPortalItem,
  DropdownMenuRadioGroup,
}
