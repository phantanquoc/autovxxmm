// React Admin v5 + ra-data-simple-rest@5.x gửi query format mới:
//   ?filter={"serverId":1,"q":"foo"}&range=[0,24]&sort=["id","ASC"]
// (JSON-encoded). Format legacy (v3/v4) là: _start, _end, _sort, _order, q, <field>=<value>.
// Backend chấp nhận cả 2 cho khoẻ.
import type { Request, Response } from 'express';

export interface ListParams {
  start: number;
  end: number;
  take: number;
  skip: number;
  sort?: string;
  order: 'asc' | 'desc';
  q?: string;
  filters: Record<string, string>;
}

const RESERVED = new Set(['_sort', '_order', '_start', '_end', 'q', 'filter', 'range', 'sort']);

function safeJson<T = unknown>(raw: unknown): T | undefined {
  if (typeof raw !== 'string') return undefined;
  try {
    return JSON.parse(raw) as T;
  } catch {
    return undefined;
  }
}

export function parseListQuery(req: Request): ListParams {
  const rangeArr = safeJson<[number, number]>(req.query.range);
  const sortArr = safeJson<[string, string]>(req.query.sort);
  const filterObj = safeJson<Record<string, unknown>>(req.query.filter);

  // Range — ưu tiên format JSON, fallback legacy _start/_end. ra-data-simple-rest
  // gửi range dạng [start, end] inclusive end.
  let start = 0;
  let endInclusive = 24;
  if (rangeArr && Array.isArray(rangeArr) && rangeArr.length === 2) {
    start = Math.max(0, Number(rangeArr[0]) || 0);
    endInclusive = Math.max(start, Number(rangeArr[1]) || start + 24);
  } else {
    start = Math.max(0, Number(req.query._start ?? 0));
    const endExclusive = Math.max(start + 1, Number(req.query._end ?? start + 25));
    endInclusive = endExclusive - 1;
  }
  const take = Math.min(200, Math.max(1, endInclusive - start + 1));

  // Sort
  let sort: string | undefined;
  let order: 'asc' | 'desc' = 'asc';
  if (sortArr && Array.isArray(sortArr) && sortArr.length === 2) {
    sort = sortArr[0] || undefined;
    order = String(sortArr[1]).toLowerCase() === 'desc' ? 'desc' : 'asc';
  } else {
    sort = (req.query._sort as string | undefined) || undefined;
    order = ((req.query._order as string | undefined) ?? 'asc').toLowerCase() === 'desc' ? 'desc' : 'asc';
  }

  // Filters — merge giữa filter JSON và query params legacy.
  const filters: Record<string, string> = {};
  let q: string | undefined;

  if (filterObj && typeof filterObj === 'object') {
    for (const [k, v] of Object.entries(filterObj)) {
      if (v === undefined || v === null || v === '') continue;
      if (k === 'q') {
        q = String(v);
        continue;
      }
      filters[k] = String(v);
    }
  }
  for (const [k, v] of Object.entries(req.query)) {
    if (RESERVED.has(k)) continue;
    if (typeof v === 'string') filters[k] = v;
  }
  if (!q && typeof req.query.q === 'string') q = req.query.q;

  return { start, end: endInclusive + 1, take, skip: start, sort, order, q, filters };
}

export function setRange(res: Response, resource: string, start: number, count: number, total: number) {
  const end = count > 0 ? start + count - 1 : start;
  res.setHeader('Content-Range', `${resource} ${start}-${end}/${total}`);
  res.setHeader('Access-Control-Expose-Headers', 'Content-Range');
}
