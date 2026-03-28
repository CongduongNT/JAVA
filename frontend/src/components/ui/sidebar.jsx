"use client"

import * as React from 'react'
import { AlignJustify } from 'lucide-react'
import { cn } from '@/lib/utils'

const SidebarContext = React.createContext({
  collapsed: false,
  setCollapsed: () => {},
})

function SidebarProvider({ children }) {
  const [collapsed, setCollapsed] = React.useState(false)
  const value = React.useMemo(() => ({ collapsed, setCollapsed }), [collapsed])
  return (
    <SidebarContext.Provider value={value}>
      <div data-slot="sidebar-shell" className="flex min-h-screen bg-background">
        {children}
      </div>
    </SidebarContext.Provider>
  )
}

function useSidebar() {
  return React.useContext(SidebarContext)
}

function Sidebar({ className, collapsible = 'icon', ...props }) {
  const { collapsed } = useSidebar()
  return (
    <aside
      data-slot="sidebar"
      data-collapsible={collapsible}
      data-collapsed={collapsed}
      className={cn(
        'relative flex h-screen shrink-0 flex-col border-r border-sidebar-border bg-sidebar text-sidebar-foreground transition-[width] duration-200',
        collapsed ? 'w-16' : 'w-64',
        className
      )}
      {...props}
    />
  )
}

function SidebarHeader({ className, ...props }) {
  return (
    <div
      data-slot="sidebar-header"
      className={cn('flex h-16 items-center px-4', className)}
      {...props}
    />
  )
}

function SidebarContent({ className, ...props }) {
  return (
    <div
      data-slot="sidebar-content"
      className={cn('flex-1 space-y-4 overflow-y-auto px-2 py-3', className)}
      {...props}
    />
  )
}

function SidebarFooter({ className, ...props }) {
  return (
    <div
      data-slot="sidebar-footer"
      className={cn('px-2 py-3 border-t border-sidebar-border', className)}
      {...props}
    />
  )
}

function SidebarGroup({ className, ...props }) {
  return (
    <div data-slot="sidebar-group" className={cn('space-y-2', className)} {...props} />
  )
}

function SidebarGroupLabel({ className, ...props }) {
  return (
    <div
      data-slot="sidebar-group-label"
      className={cn(
        'px-3 text-[11px] font-semibold uppercase tracking-[0.08em] text-sidebar-accent-foreground/80',
        className
      )}
      {...props}
    />
  )
}

function SidebarMenu({ className, ...props }) {
  return (
    <ul
      data-slot="sidebar-menu"
      className={cn('space-y-1 px-1 text-sm font-medium', className)}
      {...props}
    />
  )
}

function SidebarMenuItem({ className, ...props }) {
  return (
    <li data-slot="sidebar-menu-item" className={cn('group', className)} {...props} />
  )
}

function SidebarMenuButton({
  isActive,
  disabled,
  tooltip,
  className,
  children,
  render,
  ...props
}) {
  const { collapsed } = useSidebar()
  const Component = render ? 'div' : 'button'
  const content = (
    <Component
      data-slot="sidebar-menu-button"
      data-active={isActive}
      data-disabled={disabled}
      className={cn(
        'flex w-full items-center gap-3 rounded-lg px-3 py-2 text-left transition-colors',
        isActive
          ? 'bg-sidebar-accent text-sidebar-foreground'
          : 'text-sidebar-foreground/90 hover:bg-sidebar-accent hover:text-sidebar-foreground',
        disabled && 'cursor-not-allowed opacity-50',
        className
      )}
      {...props}
    >
      {children}
    </Component>
  )

  if (render) {
    return React.cloneElement(render, {
      children: content,
      className: cn('block', render.props?.className),
    })
  }

  if (tooltip && collapsed) {
    return (
      <div className="relative group">
        {content}
        <div className="pointer-events-none absolute left-full top-1/2 ml-2 -translate-y-1/2 rounded-md bg-popover px-2 py-1 text-xs text-popover-foreground opacity-0 shadow transition-opacity group-hover:opacity-100">
          {tooltip}
        </div>
      </div>
    )
  }

  return content
}

function SidebarInset({ className, ...props }) {
  return (
    <div
      data-slot="sidebar-inset"
      className={cn('flex min-h-screen flex-1 flex-col bg-background', className)}
      {...props}
    />
  )
}

function SidebarTrigger({ className, ...props }) {
  const { collapsed, setCollapsed } = useSidebar()
  return (
    <button
      type="button"
      data-slot="sidebar-trigger"
      className={cn(
        'inline-flex items-center justify-center rounded-md border border-border bg-card p-2 text-muted-foreground shadow-sm transition hover:bg-accent hover:text-foreground',
        className
      )}
      aria-label="Toggle sidebar"
      onClick={() => setCollapsed(!collapsed)}
      {...props}
    >
      <AlignJustify className="size-4" />
    </button>
  )
}

function SidebarRail({ className, ...props }) {
  const { collapsed, setCollapsed } = useSidebar()
  return (
    <div
      data-slot="sidebar-rail"
      className={cn(
        'absolute right-0 top-0 flex h-full w-2 cursor-ew-resize items-center justify-center bg-transparent',
        className
      )}
      onClick={() => setCollapsed(!collapsed)}
      {...props}
    />
  )
}

export {
  SidebarProvider,
  Sidebar,
  SidebarHeader,
  SidebarContent,
  SidebarFooter,
  SidebarGroup,
  SidebarGroupLabel,
  SidebarMenu,
  SidebarMenuItem,
  SidebarMenuButton,
  SidebarInset,
  SidebarTrigger,
  SidebarRail,
}
