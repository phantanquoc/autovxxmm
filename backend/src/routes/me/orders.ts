import { Router } from 'express';
import { prisma } from '../../lib/prisma.js';
import { getOwnerScope } from '../../lib/scope.js';
import { parseListQuery, setRange } from '../../lib/list.js';
import { notFound } from '../../lib/errors.js';

const router = Router();

router.get('/', async (req, res) => {
  const ownerId = getOwnerScope(req);
  const p = parseListQuery(req);
  const and: Record<string, unknown>[] = [{ deletedAt: null }, { ownerId }];
  if (p.q)
    and.push({
      OR: [
        { name: { contains: p.q, mode: 'insensitive' } },
        { bot: { contains: p.q, mode: 'insensitive' } },
      ],
    });
  if (p.filters.serverId) and.push({ serverId: Number(p.filters.serverId) });
  if (p.filters.botId) and.push({ botId: Number(p.filters.botId) });
  if (p.filters.status) and.push({ status: Number(p.filters.status) });

  const where = { AND: and };
  const [total, items] = await Promise.all([
    prisma.order.count({ where }),
    prisma.order.findMany({
      where,
      skip: p.skip,
      take: p.take,
      orderBy: p.sort ? { [p.sort]: p.order } : { id: 'desc' },
    }),
  ]);
  setRange(res, 'orders', p.start, items.length, total);
  res.json(items);
});

router.get('/:id', async (req, res) => {
  const ownerId = getOwnerScope(req);
  const id = Number(req.params.id);
  const order = await prisma.order.findUnique({ where: { id } });
  if (!order || order.ownerId !== ownerId) throw notFound();
  res.json(order);
});

export default router;
