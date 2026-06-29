import { Router } from 'express';
import { z } from 'zod';
import { prisma } from '../../lib/prisma.js';
import { getOwnerScope } from '../../lib/scope.js';
import { parseListQuery, setRange } from '../../lib/list.js';
import { badRequest, notFound } from '../../lib/errors.js';

const router = Router();

router.get('/', async (req, res) => {
  const ownerId = getOwnerScope(req);
  const p = parseListQuery(req);
  const and: Record<string, unknown>[] = [{ ownerId }];
  if (p.q) and.push({ name: { contains: p.q, mode: 'insensitive' } });

  const where = { AND: and };
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

const blockBody = z.object({
  serverId: z.number().int(),
  name: z.string().min(1),
  reason: z.string().nullable().optional(),
});

router.post('/', async (req, res) => {
  const ownerId = getOwnerScope(req);
  const b = blockBody.parse(req.body);
  try {
    const created = await prisma.block.create({
      data: { ...b, ownerId },
    });
    res.json(created);
  } catch (err: unknown) {
    // Unique constraint violation: (ownerId, serverId, name)
    const msg = err instanceof Error ? err.message : '';
    if (msg.includes('Unique constraint') || msg.includes('unique constraint')) {
      throw badRequest('Tên block này đã tồn tại trong server');
    }
    throw err;
  }
});

router.delete('/:id', async (req, res) => {
  const ownerId = getOwnerScope(req);
  const id = Number(req.params.id);
  const existing = await prisma.block.findUnique({ where: { id } });
  if (!existing || existing.ownerId !== ownerId) throw notFound();
  await prisma.block.delete({ where: { id } });
  res.json({ id });
});

export default router;
