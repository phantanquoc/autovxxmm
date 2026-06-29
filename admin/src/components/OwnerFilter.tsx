import { useUsers } from '@/features/users/useUsers'
import {
  Select,
  SelectContent,
  SelectItem,
  SelectTrigger,
  SelectValue,
} from '@/components/ui/select'

interface OwnerFilterProps {
  value: number | undefined
  onChange: (ownerId: number | undefined) => void
}

/**
 * Admin-only dropdown that fetches the user list and lets the admin filter
 * tenant data by a specific owner. Shows "Tất cả" as the reset option.
 */
export function OwnerFilter({ value, onChange }: OwnerFilterProps) {
  const { data } = useUsers({ pageSize: 100 })
  const users = data?.items ?? []

  function handleChange(val: string) {
    if (val === '__all__') {
      onChange(undefined)
    } else {
      onChange(Number(val))
    }
  }

  return (
    <Select
      value={value !== undefined ? String(value) : '__all__'}
      onValueChange={handleChange}
    >
      <SelectTrigger className="w-44" aria-label="Lọc theo người dùng">
        <SelectValue placeholder="Người dùng" />
      </SelectTrigger>
      <SelectContent>
        <SelectItem value="__all__">Tất cả</SelectItem>
        {users.map((u) => (
          <SelectItem key={u.id} value={String(u.id)}>
            {u.username}
          </SelectItem>
        ))}
      </SelectContent>
    </Select>
  )
}
