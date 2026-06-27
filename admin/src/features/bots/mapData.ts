// Map ID → tên map của Ninja School. Tên load runtime từ server game nên admin
// không có cách lấy đầy đủ offline; danh sách dưới đây là các map phổ biến,
// user vẫn nhập ID tuỳ ý cho map ngoài list.
export const NINJA_MAPS: Array<{ id: number; name: string }> = [
  { id: 0, name: 'Làng Lá' },
  { id: 1, name: 'Làng Cát' },
  { id: 2, name: 'Làng Đá' },
  { id: 3, name: 'Làng Mây' },
  { id: 4, name: 'Làng Sương Mù' },
  { id: 5, name: 'Làng Âm' },
  { id: 10, name: 'Khu rừng phía Tây' },
  { id: 11, name: 'Khu rừng phía Đông' },
  { id: 12, name: 'Khu rừng phía Nam' },
  { id: 13, name: 'Khu rừng phía Bắc' },
  { id: 20, name: 'Cánh Đồng Lúa' },
  { id: 21, name: 'Bãi Tha Ma' },
  { id: 22, name: 'Làng Tone' },
  { id: 23, name: 'Đảo Rùa' },
  { id: 24, name: 'Đỉnh Núi Tuyết' },
  { id: 25, name: 'Hang Động Bí Ẩn' },
  { id: 30, name: 'Map Phụ Bản' },
  { id: 31, name: 'Map Sự Kiện' },
  { id: 50, name: 'Đấu Trường' },
  { id: 51, name: 'Đấu Trường Boss' },
  { id: 100, name: 'Map Đặc Biệt 100' },
  { id: 101, name: 'Map Đặc Biệt 101' },
  { id: 102, name: 'Map Đặc Biệt 102' },
]

export function getMapLabel(id: number): string {
  const m = NINJA_MAPS.find(x => x.id === id)
  return m ? `${id} — ${m.name}` : `#${id}`
}
