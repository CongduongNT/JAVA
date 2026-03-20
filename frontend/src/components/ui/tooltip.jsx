"use client"

import * as React from 'react'
import { Tooltip as TooltipPrimitive } from '@base-ui/react/tooltip'

import { cn } from '@/lib/utils'

function TooltipProvider({ delayDuration = 300, children }) {
  return (
    <TooltipPrimitive.Provider
      data-slot="tooltip-provider"
      delayDuration={delayDuration}
    >
      {children}
    </TooltipPrimitive.Provider>
  )
}

function Tooltip({ children, ...props }) {
  return (
    <TooltipPrimitive.Root data-slot="tooltip" {...props}>
      {children}
    </TooltipPrimitive.Root>
  )
}

function TooltipTrigger({ className, ...props }) {
  return (
    <TooltipPrimitive.Trigger
      data-slot="tooltip-trigger"
      className={cn('outline-none', className)}
      {...props}
    />
  )
}

function TooltipContent({ className, sideOffset = 4, align = 'center', ...props }) {
  return (
    <TooltipPrimitive.Positioner sideOffset={sideOffset} align={align}>
      <TooltipPrimitive.Content
        data-slot="tooltip-content"
        className={cn(
          'z-50 max-w-xs rounded-md bg-popover px-3 py-1.5 text-xs text-popover-foreground shadow-md ring-1 ring-border animate-in fade-in-0 zoom-in-95 data-[state=closed]:animate-out data-[state=closed]:fade-out-0 data-[state=closed]:zoom-out-95',
          className
        )}
        {...props}
      />
    </TooltipPrimitive.Positioner>
  )
}

export { TooltipProvider, Tooltip, TooltipTrigger, TooltipContent }
