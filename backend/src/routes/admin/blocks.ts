import { Router } from 'express';
import { z } from 'zod';
import { prisma } from '../../lib/prisma.js';
import { authRequired } from '../../middleware/auth.js';
import { adminRequired } from '../../middleware/admin.js';
import { parseListQuery, setRange } from '../../lib/list.js';

const router = Router();
router.use(authRequired, adminRequired);

router.get('/', async (req, res) => {
  const p = parseListQuery(req);
  const where = p.q ? { name: { contains: p.q, mode: 'insensitive' as const } } : {};
  const [total, items] = await Promise.all([
    prisma.block.count({ where }),
    prisma.block.findMany({
      where,
      skip: p.skip,
      take: p.take,
      orderBy: p.sort ? { [p.sort]: p.order } : { id: 'desc' },
    }),
  ]);
  setRange(res, 'blocks', p.start, items.length, total);
  res.json(items);
});

const body = z.object({
  serverId: z.number().int(),
  name: z.string().min(1),
  reason: z.string().nullable().optional(),
});
router.post('/', async (req, res) => {
  const b = body.parse(req.body);
  const created = await prisma.block.create({ data: b });
  res.json(created);
});

router.delete('/:id', async (req, res) => {
  const id = Number(req.params.id);
  await prisma.block.delete({ where: { id } });
  res.json({ id });
});

export default router;
