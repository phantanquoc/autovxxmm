import { useState } from 'react'
import { Search, X } from 'lucide-react'
import { Input } from '@/components/ui/input'
import { Button } from '@/components/ui/button'
import {
  Select,
  SelectContent,
  SelectItem,
  SelectTrigger,
  SelectValue,
} from '@/components/ui/select'

interface FilterOption {
  value: string
  label: string
}

type FilterDef =
  | { type: 'select'; key: string; label: string; options: FilterOption[] }
  | { type: 'boolean'; key: string; label: string }

interface DataTableToolbarProps {
  searchPlaceholder?: string
  onSearch?: (q: string) => void
  filters?: FilterDef[]
  onFilter?: (key: string, value: string) => void
  filterValues?: Record<string, string>
  extra?: React.ReactNode
}

export function DataTableToolbar({
  searchPlaceholder = 'Tìm kiếm...',
  onSearch,
  filters,
  onFilter,
  filterValues = {},
  extra,
}: DataTableToolbarProps) {
  const [search, setSearch] = useState('')

  function handleSearch(value: string) {
    setSearch(value)
    onSearch?.(value)
  }

  function clearSearch() {
    setSearch('')
    onSearch?.('')
  }

  return (
    <div className="flex flex-wrap items-center gap-2 p-4 border-b">
      {onSearch && (
        <div className="relative flex-1 min-w-0 sm:min-w-48 max-w-sm">
          <Search className="absolute left-3 top-1/2 -translate-y-1/2 h-4 w-4 text-muted-foreground" />
          <Input
            className="pl-9 pr-8 h-9"
            placeholder={searchPlaceholder}
            value={search}
            onChange={e => handleSearch(e.target.value)}
          />
          {search && (
            <button
              onClick={clearSearch}
              className="absolute right-2 top-1/2 -translate-y-1/2 text-muted-foreground hover:text-foreground"
            >
              <X className="h-3 w-3" />
            </button>
          )}
        </div>
      )}

      {filters?.map(f => {
        if (f.type === 'boolean') {
          const current = filterValues[f.key] ?? '__all__'
          return (
            <Select
              key={f.key}
              value={current}
              onValueChange={v => onFilter?.(f.key, v === '__all__' ? '' : v)}
            >
              <SelectTrigger className="h-9 min-w-[130px] w-auto">
                <SelectValue placeholder={f.label} />
              </SelectTrigger>
              <SelectContent>
                <SelectItem value="__all__">Tất cả {f.label}</SelectItem>
                <SelectItem value="true">Đang bật</SelectItem>
                <SelectItem value="false">Đã tắt</SelectItem>
              </SelectContent>
            </Select>
          )
        }

        return (
          <Select
            key={f.key}
            value={filterValues[f.key] ?? '__all__'}
            onValueChange={v => onFilter?.(f.key, v === '__all__' ? '' : v)}
          >
            <SelectTrigger className="h-9 min-w-[130px] w-auto">
              <SelectValue placeholder={f.label} />
            </SelectTrigger>
            <SelectContent>
              <SelectItem value="__all__">Tất cả {f.label}</SelectItem>
              {f.options.map(o => (
                <SelectItem key={o.value} value={o.value}>{o.label}</SelectItem>
              ))}
            </SelectContent>
          </Select>
        )
      })}

      {extra}
    </div>
  )
}
