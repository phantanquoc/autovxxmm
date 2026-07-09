import { createContext, useContext, useState, useEffect, useCallback, ReactNode } from 'react'

const COLLAPSE_KEY = 'vxmm.sidebar.collapsed'

interface SidebarContextValue {
  collapsed: boolean
  toggleCollapse: () => void
  isDrawerOpen: boolean
  openDrawer: () => void
  closeDrawer: () => void
}

const SidebarContext = createContext<SidebarContextValue | null>(null)

export function SidebarProvider({ children }: { children: ReactNode }) {
  const [collapsed, setCollapsed] = useState(() => {
    return localStorage.getItem(COLLAPSE_KEY) === 'true'
  })
  const [isDrawerOpen, setIsDrawerOpen] = useState(false)

  function toggleCollapse() {
    const next = !collapsed
    setCollapsed(next)
    localStorage.setItem(COLLAPSE_KEY, String(next))
  }

  const closeDrawer = useCallback(() => {
    setIsDrawerOpen(false)
  }, [])

  const openDrawer = useCallback(() => {
    setIsDrawerOpen(true)
  }, [])

  // Lock body scroll when drawer is open
  useEffect(() => {
    if (isDrawerOpen) {
      document.body.style.overflow = 'hidden'
    } else {
      document.body.style.overflow = ''
    }
    return () => {
      document.body.style.overflow = ''
    }
  }, [isDrawerOpen])

  // Close drawer on Escape key
  useEffect(() => {
    if (!isDrawerOpen) return
    function handleKeyDown(e: KeyboardEvent) {
      if (e.key === 'Escape') {
        closeDrawer()
      }
    }
    document.addEventListener('keydown', handleKeyDown)
    return () => document.removeEventListener('keydown', handleKeyDown)
  }, [isDrawerOpen, closeDrawer])

  // Close drawer when viewport crosses md breakpoint (768px)
  useEffect(() => {
    const mq = window.matchMedia('(min-width: 768px)')
    function handleChange(e: MediaQueryListEvent) {
      if (e.matches) setIsDrawerOpen(false)
    }
    mq.addEventListener('change', handleChange)
    return () => mq.removeEventListener('change', handleChange)
  }, [])

  return (
    <SidebarContext.Provider value={{ collapsed, toggleCollapse, isDrawerOpen, openDrawer, closeDrawer }}>
      {children}
    </SidebarContext.Provider>
  )
}

export function useSidebar() {
  const ctx = useContext(SidebarContext)
  if (!ctx) throw new Error('useSidebar must be used within SidebarProvider')
  return ctx
}
