import * as React from 'react'
import { Check, ChevronDown } from 'lucide-react'
import { cn } from '@/lib/utils'

// Context for Select state
const SelectContext = React.createContext(null)

function Select({ value, onValueChange, defaultValue, disabled, children, ...props }) {
  const [open, setOpen] = React.useState(false)
  const [internalValue, setInternalValue] = React.useState(defaultValue ?? value ?? '')
  const controlled = value !== undefined
  const currentValue = controlled ? value : internalValue
  const triggerRef = React.useRef(null)

  const handleSelect = (val) => {
    if (!controlled) setInternalValue(val)
    onValueChange?.(val)
    setOpen(false)
  }

  // Close on outside click
  React.useEffect(() => {
    if (!open) return
    const handleClick = (e) => {
      if (!triggerRef.current?.closest('[data-slot="select"]')?.contains(e.target)) {
        setOpen(false)
      }
    }
    document.addEventListener('mousedown', handleClick)
    return () => document.removeEventListener('mousedown', handleClick)
  }, [open])

  return (
    <SelectContext.Provider value={{ value: currentValue, handleSelect, open, setOpen, disabled }}>
      <div data-slot="select" className="relative" ref={triggerRef} {...props}>
        {children}
      </div>
    </SelectContext.Provider>
  )
}

function SelectTrigger({ className, placeholder, children, ...props }) {
  const ctx = React.useContext(SelectContext)
  // children may contain SelectValue — find label from within SelectContent via context
  return (
    <button
      type="button"
      data-slot="select-trigger"
      disabled={ctx?.disabled}
      onClick={() => ctx?.setOpen(!ctx?.open)}
      className={cn(
        'flex h-8 w-full items-center justify-between gap-2 rounded-lg border border-input bg-background px-2.5 py-1 text-sm outline-none transition',
        'focus-visible:border-ring focus-visible:ring-2 focus-visible:ring-ring/50',
        'disabled:cursor-not-allowed disabled:opacity-50',
        className
      )}
      {...props}
    >
      <span data-slot="select-value" className="truncate text-foreground">
        {ctx?.displayLabel || <span className="text-muted-foreground">{placeholder}</span>}
      </span>
      <ChevronDown className={cn('size-4 text-muted-foreground shrink-0 transition-transform', ctx?.open && 'rotate-180')} />
    </button>
  )
}

// Internal context to pass label → trigger display
const SelectDisplayContext = React.createContext(null)

function SelectContent({ className, children, ...props }) {
  const ctx = React.useContext(SelectContext)
  if (!ctx?.open) return null

  return (
    <div
      data-slot="select-content"
      className={cn(
        'absolute top-full left-0 z-50 min-w-full mt-1 rounded-lg border border-border bg-popover text-popover-foreground shadow-md',
        'animate-in fade-in-0 zoom-in-95',
        className
      )}
      {...props}
    >
      <div className="p-1">
        {children}
      </div>
    </div>
  )
}

function SelectItem({ className, children, value, disabled, ...props }) {
  const ctx = React.useContext(SelectContext)
  const isSelected = ctx?.value === value

  return (
    <div
      data-slot="select-item"
      data-highlighted={isSelected || undefined}
      role="option"
      aria-selected={isSelected}
      onClick={() => !disabled && ctx?.handleSelect(value)}
      className={cn(
        'relative flex cursor-pointer select-none items-center gap-2 rounded-md px-2.5 py-1.5 text-sm outline-none transition-colors',
        'hover:bg-accent hover:text-accent-foreground',
        isSelected && 'bg-accent text-accent-foreground',
        disabled && 'pointer-events-none opacity-50',
        className
      )}
      {...props}
    >
      <span className="absolute left-2 flex size-4 items-center justify-center">
        {isSelected && <Check className="size-4" />}
      </span>
      <span className="pl-4">{children}</span>
    </div>
  )
}

function SelectLabel({ className, ...props }) {
  return (
    <div
      data-slot="select-label"
      className={cn('px-2.5 py-1 text-xs font-medium text-muted-foreground', className)}
      {...props}
    />
  )
}

function SelectSeparator({ className, ...props }) {
  return (
    <div
      data-slot="select-separator"
      className={cn('my-1 h-px bg-border', className)}
      {...props}
    />
  )
}

// SelectValue is kept for backward compat but does nothing (SelectTrigger handles display)
const SelectValue = React.forwardRef(function SelectValue(_props, _ref) {
  return null
})

export {
  Select,
  SelectTrigger,
  SelectContent,
  SelectItem,
  SelectLabel,
  SelectSeparator,
  SelectValue,
}
