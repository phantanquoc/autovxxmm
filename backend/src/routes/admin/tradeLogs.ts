import { Router } from 'express';
import { prisma } from '../../lib/prisma.js';
import { authRequired } from '../../middleware/auth.js';
import { adminRequired } from '../../middleware/admin.js';
import { parseListQuery, setRange } from '../../lib/list.js';
import { notFound } from '../../lib/errors.js';

const router = Router();
router.use(authRequired, adminRequired);

router.get('/', async (req, res) => {
  const p = parseListQuery(req);
  const and: Record<string, unknown>[] = [];
  if (p.q)
    and.push({
      OR: [
        { name: { contains: p.q, mode: 'insensitive' } },
        { customer: { contains: p.q, mode: 'insensitive' } },
      ],
    });
  if (p.filters.serverId) and.push({ serverId: Number(p.filters.serverId) });
  if (p.filters.type) and.push({ type: Number(p.filters.type) });
  const where = and.length ? { AND: and } : {};

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

router.get('/:id', async (req, res) => {
  const id = BigInt(req.params.id);
  const log = await prisma.tradeLog.findUnique({ where: { id } });
  if (!log) throw notFound();
  res.json(log);
});

export default router;
