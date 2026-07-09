# UI DNA — VXMM Admin Dashboard

## Stack
- React 18+ (hooks, context)
- Vite 5
- TailwindCSS 3 (HSL CSS variable theme, mobile-first)
- shadcn/ui (Radix primitives)
- Lucide icons
- Hash router (`createHashRouter`)

## Design Tokens
See full palette: `openspec/changes/archive/2026-06-26-redesign-admin-ui/design.md`

## Responsive Shell Pattern

### Breakpoints (Tailwind defaults)
| Name | Min-width | Usage |
|------|-----------|-------|
| (base) | 0 | Phones — drawer nav, p-4, single-col |
| sm | 640px | Minor adjustments (show pagination count) |
| md | 768px | Persistent sidebar, p-6, standard tables |
| xl | 1280px | Full grids (4-col stats) |

### Navigation: Off-canvas Drawer (< md) + Sticky Sidebar (>= md)
- Below `md`: sidebar is `fixed inset-y-0 left-0 z-50 w-60`, hidden via `-translate-x-full`, shown via `translate-x-0`
- Backdrop: `fixed inset-0 z-40 bg-black/50 md:hidden`
- Desktop: `md:static md:translate-x-0 md:sticky md:top-0 md:h-screen`, collapse toggle `md:w-16`/`md:w-60`
- State: React Context (`SidebarProvider` in `hooks/useSidebar.tsx`)
- Close triggers: nav item click, backdrop click, Esc key, viewport crosses md
- Accessibility: `role="dialog"`, `aria-modal`, focus trap, return focus on close, body scroll lock
- Animation: `transition-transform duration-200 ease-in-out motion-reduce:transition-none`

### Topbar
- Hamburger button: `md:hidden`, opens drawer, `aria-label="Mo menu"`, `aria-expanded`
- Padding: `px-4 md:px-6`

### Main Content
- Padding: `p-4 md:p-6`
- Max width: `max-w-screen-2xl mx-auto`

### DataTable
- Horizontal scroll via `overflow-x-auto` on wrapper
- No card-view alternative (too many list pages, consistency > novelty)
- Toolbar search: `min-w-0 sm:min-w-48` (full width on phone)
- Pagination: `flex-wrap gap-2`, count text `hidden sm:block`

### PageHeader
- `flex-wrap gap-2` to allow actions to wrap
- Title: `text-xl md:text-2xl`

## Component Patterns
- Use `cn()` for conditional Tailwind classes
- Use CSS variables (not hardcoded hex) for all theme colors
- Prefer Tailwind responsive prefixes over JS media queries
- All interactive elements need `aria-label` if icon-only
- Focus ring: `focus-visible:ring-2 ring-primary`
