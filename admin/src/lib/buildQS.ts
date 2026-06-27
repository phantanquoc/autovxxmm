export interface QueryParams {
  page?: number
  pageSize?: number
  sort?: string
  order?: 'asc' | 'desc'
  filter?: Record<string, unknown>
}

export function buildQS(p: QueryParams): string {
  const { page = 1, pageSize = 25, sort = 'id', order = 'asc', filter = {} } = p
  const start = (page - 1) * pageSize
  const end = start + pageSize - 1

  const params = new URLSearchParams()
  params.set('filter', JSON.stringify(filter))
  params.set('range', JSON.stringify([start, end]))
  params.set('sort', JSON.stringify([sort, order.toUpperCase()]))

  return params.toString()
}
