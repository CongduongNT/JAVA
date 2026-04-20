import * as React from 'react'
import { createPortal } from 'react-dom'
import { cn } from '@/lib/utils'

/**
 * AlertDialog – pure native React implementation (no @base-ui/react).
 * Dùng controlled open/onOpenChange pattern giống Radix UI.
 */
function AlertDialog({ open, onOpenChange, children }) {
  return (
    <AlertDialogContext.Provider value={{ open: !!open, onOpenChange }}>
      {children}
    </AlertDialogContext.Provider>
  )
}

const AlertDialogContext = React.createContext({ open: false, onOpenChange: () => {} })

function useAlertDialog() {
  return React.useContext(AlertDialogContext)
}

function AlertDialogTrigger({ children, asChild, ...props }) {
  const { onOpenChange } = useAlertDialog()
  if (asChild && React.isValidElement(children)) {
    return React.cloneElement(children, {
      ...props,
      onClick: (e) => {
        children.props.onClick?.(e)
        onOpenChange?.(true)
      },
    })
  }
  return (
    <button type="button" data-slot="alert-dialog-trigger" onClick={() => onOpenChange?.(true)} {...props}>
      {children}
    </button>
  )
}

function AlertDialogPortal({ children }) {
  return createPortal(children, document.body)
}

function AlertDialogOverlay({ className, ...props }) {
  const { onOpenChange } = useAlertDialog()
  return (
    <div
      data-slot="alert-dialog-overlay"
      className={cn(
        'fixed inset-0 z-50 bg-black/50 backdrop-blur-sm animate-in fade-in-0',
        className
      )}
      onClick={() => onOpenChange?.(false)}
      {...props}
    />
  )
}

function AlertDialogContent({ className, children, ...props }) {
  const { open, onOpenChange } = useAlertDialog()

  // Close on Escape
  React.useEffect(() => {
    if (!open) return
    const handler = (e) => { if (e.key === 'Escape') onOpenChange?.(false) }
    document.addEventListener('keydown', handler)
    return () => document.removeEventListener('keydown', handler)
  }, [open, onOpenChange])

  if (!open) return null

  return (
    <AlertDialogPortal>
      <AlertDialogOverlay />
      <div
        role="alertdialog"
        aria-modal="true"
        data-slot="alert-dialog-content"
        className={cn(
          'fixed left-1/2 top-1/2 z-50 w-[95vw] max-w-md -translate-x-1/2 -translate-y-1/2',
          'rounded-xl border border-border bg-card p-6 shadow-xl outline-none',
          'animate-in fade-in-0 zoom-in-95',
          className
        )}
        onClick={(e) => e.stopPropagation()}
        {...props}
      >
        {children}
      </div>
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
      className={cn('flex flex-col-reverse gap-2 sm:flex-row sm:justify-end', className)}
      {...props}
    />
  )
}

function AlertDialogTitle({ className, ...props }) {
  return (
    <h2
      data-slot="alert-dialog-title"
      className={cn('text-lg font-semibold leading-none tracking-tight', className)}
      {...props}
    />
  )
}

function AlertDialogDescription({ className, ...props }) {
  return (
    <p
      data-slot="alert-dialog-description"
      className={cn('text-sm text-muted-foreground', className)}
      {...props}
    />
  )
}

function AlertDialogAction({ className, onClick, children, ...props }) {
  const { onOpenChange } = useAlertDialog()
  return (
    <button
      type="button"
      data-slot="alert-dialog-action"
      className={cn(
        'inline-flex h-9 items-center justify-center rounded-md bg-primary px-4 text-sm font-medium text-primary-foreground transition-colors hover:bg-primary/90 focus-visible:outline-none focus-visible:ring-2 focus-visible:ring-ring disabled:pointer-events-none disabled:opacity-50',
        className
      )}
      onClick={(e) => {
        onClick?.(e)
        onOpenChange?.(false)
      }}
      {...props}
    >
      {children}
    </button>
  )
}

function AlertDialogCancel({ className, onClick, children, ...props }) {
  const { onOpenChange } = useAlertDialog()
  return (
    <button
      type="button"
      data-slot="alert-dialog-cancel"
      className={cn(
        'inline-flex h-9 items-center justify-center rounded-md border border-border bg-background px-4 text-sm font-medium text-foreground transition-colors hover:bg-muted focus-visible:outline-none focus-visible:ring-2 focus-visible:ring-ring disabled:pointer-events-none disabled:opacity-50',
        className
      )}
      onClick={(e) => {
        onClick?.(e)
        onOpenChange?.(false)
      }}
      {...props}
    >
      {children}
    </button>
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
