import { Router } from 'express';
import { prisma } from '../../lib/prisma.js';
import { getOwnerScope } from '../../lib/scope.js';
import { parseListQuery, setRange } from '../../lib/list.js';

const router = Router();

router.get('/', async (req, res) => {
  const ownerId = getOwnerScope(req);
  const p = parseListQuery(req);
  const and: Record<string, unknown>[] = [{ ownerId }];
  if (p.q)
    and.push({
      OR: [
        { name: { contains: p.q, mode: 'insensitive' } },
        { customer: { contains: p.q, mode: 'insensitive' } },
      ],
    });
  if (p.filters.serverId) and.push({ serverId: Number(p.filters.serverId) });
  if (p.filters.type) and.push({ type: Number(p.filters.type) });

  const where = { AND: and };
  const [total, items] = await Promise.all([
    prisma.tradeLog.count({ where }),
    prisma.tradeLog.findMany({
      where,
      skip: p.skip,
      take: p.take,
      orderBy: p.sort ? { [p.sort]: p.order } : { id: 'desc' },
    }),
  ]);
  setRange(res, 'tradeLogs', p.start, items.length, total);
  res.json(items);
});

export default router;
