import { Router } from 'express';
import { z } from 'zod';
import { prisma } from '../../lib/prisma.js';
import { authRequired } from '../../middleware/auth.js';
import { adminRequired } from '../../middleware/admin.js';
import { parseListQuery, setRange } from '../../lib/list.js';
import { notFound } from '../../lib/errors.js';

const router = Router();
router.use(authRequired, adminRequired);

router.get('/', async (req, res) => {
  const p = parseListQuery(req);
  const where = p.q
    ? { OR: [{ name: { contains: p.q, mode: 'insensitive' as const } }, { ip: { contains: p.q } }] }
    : {};
  const [total, items] = await Promise.all([
    prisma.server.count({ where }),
    prisma.server.findMany({
      where,
      skip: p.skip,
      take: p.take,
      orderBy: p.sort ? { [p.sort]: p.order } : { id: 'asc' },
    }),
  ]);
  setRange(res, 'servers', p.start, items.length, total);
  res.json(items);
});

router.get('/:id', async (req, res) => {
  const id = Number(req.params.id);
  const server = await prisma.server.findUnique({ where: { id } });
  if (!server) throw notFound();
  res.json(server);
});

const body = z.object({
  id: z.number().int(),
  name: z.string().min(1),
  ip: z.string().min(1),
  port: z.number().int().positive(),
  type: z.number().int().default(0),
});

router.post('/', async (req, res) => {
  const b = body.parse(req.body);
  const server = await prisma.server.create({ data: b });
  res.json(server);
});

router.put('/:id', async (req, res) => {
  const id = Number(req.params.id);
  const b = body.partial().parse(req.body);
  const server = await prisma.server.update({ where: { id }, data: b });
  res.json(server);
});

router.delete('/:id', async (req, res) => {
  const id = Number(req.params.id);
  await prisma.server.delete({ where: { id } });
  res.json({ id });
});

export default router;
