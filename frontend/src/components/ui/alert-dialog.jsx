"use client"

import * as React from 'react'
import { Dialog as DialogPrimitive } from '@base-ui/react/dialog'

import { cn } from '@/lib/utils'

function AlertDialog({ ...props }) {
  return <DialogPrimitive.Root data-slot="alert-dialog" modal {...props} />
}

function AlertDialogTrigger(props) {
  return <DialogPrimitive.Trigger data-slot="alert-dialog-trigger" {...props} />
}

function AlertDialogPortal(props) {
  return <DialogPrimitive.Portal data-slot="alert-dialog-portal" {...props} />
}

function AlertDialogOverlay({ className, ...props }) {
  return (
    <DialogPrimitive.Backdrop
      data-slot="alert-dialog-overlay"
      className={cn(
        'fixed inset-0 z-50 bg-background/80 backdrop-blur-sm data-[state=open]:animate-in data-[state=closed]:animate-out data-[state=closed]:fade-out-0 data-[state=open]:fade-in-0',
        className
      )}
      {...props}
    />
  )
}

function AlertDialogContent({ className, ...props }) {
  return (
    <AlertDialogPortal>
      <AlertDialogOverlay />
      <DialogPrimitive.Positioner>
        <DialogPrimitive.Content
          data-slot="alert-dialog-content"
          className={cn(
            'fixed left-[50%] top-[50%] z-50 w-[95vw] max-w-md translate-x-[-50%] translate-y-[-50%] rounded-lg border border-border bg-card p-6 shadow-xl outline-none data-[state=open]:animate-in data-[state=closed]:animate-out data-[state=closed]:fade-out-0 data-[state=closed]:zoom-out-95 data-[state=open]:fade-in-0 data-[state=open]:zoom-in-95',
            className
          )}
          {...props}
        />
      </DialogPrimitive.Positioner>
    </AlertDialogPortal>
  )
}

function AlertDialogHeader({ className, ...props }) {
  return (
    <div
      data-slot="alert-dialog-header"
      className={cn('flex flex-col space-y-1.5 text-center sm:text-left', className)}
      {...props}
    />
  )
}

function AlertDialogFooter({ className, ...props }) {
  return (
    <div
      data-slot="alert-dialog-footer"
      className={cn('flex flex-col-reverse gap-2 sm:flex-row sm:justify-end sm:space-x-2', className)}
      {...props}
    />
  )
}

function AlertDialogTitle({ className, ...props }) {
  return (
    <DialogPrimitive.Title
      data-slot="alert-dialog-title"
      className={cn('text-lg font-semibold leading-none tracking-tight', className)}
      {...props}
    />
  )
}

function AlertDialogDescription({ className, ...props }) {
  return (
    <DialogPrimitive.Description
      data-slot="alert-dialog-description"
      className={cn('text-sm text-muted-foreground', className)}
      {...props}
    />
  )
}

function AlertDialogAction({ className, ...props }) {
  return (
    <DialogPrimitive.Close
      data-slot="alert-dialog-action"
      className={cn(
        'inline-flex h-9 items-center justify-center rounded-md bg-primary px-4 text-sm font-medium text-primary-foreground transition-colors hover:bg-primary/90 focus-visible:outline-none focus-visible:ring-2 focus-visible:ring-ring focus-visible:ring-offset-2 disabled:pointer-events-none disabled:opacity-50',
        className
      )}
      {...props}
    />
  )
}

function AlertDialogCancel({ className, ...props }) {
  return (
    <DialogPrimitive.Close
      data-slot="alert-dialog-cancel"
      className={cn(
        'inline-flex h-9 items-center justify-center rounded-md border border-border bg-background px-4 text-sm font-medium text-foreground transition-colors hover:bg-muted focus-visible:outline-none focus-visible:ring-2 focus-visible:ring-ring focus-visible:ring-offset-2 disabled:pointer-events-none disabled:opacity-50',
        className
      )}
      {...props}
    />
  )
}

export {
  AlertDialog,
  AlertDialogTrigger,
  AlertDialogContent,
  AlertDialogHeader,
  AlertDialogFooter,
  AlertDialogTitle,
  AlertDialogDescription,
  AlertDialogAction,
  AlertDialogCancel,
}
