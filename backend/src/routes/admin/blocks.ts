import { Router } from 'express';
import { prisma } from '../../lib/prisma.js';
import { authRequired } from '../../middleware/auth.js';
import { adminRequired } from '../../middleware/admin.js';
import { parseListQuery, setRange } from '../../lib/list.js';
import { parseAdminOwnerFilter } from '../../lib/scope.js';

const router = Router();
router.use(authRequired, adminRequired);

router.get('/', async (req, res) => {
  const p = parseListQuery(req);
  const and: Record<string, unknown>[] = [];
  if (p.q) and.push({ name: { contains: p.q, mode: 'insensitive' as const } });

  const ownerId = parseAdminOwnerFilter(req);
  if (ownerId !== undefined) and.push({ ownerId });

  const where = and.length ? { AND: and } : {};
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

export default router;
