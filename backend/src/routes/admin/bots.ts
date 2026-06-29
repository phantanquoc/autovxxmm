import { Router } from 'express';
import { prisma } from '../../lib/prisma.js';
import { authRequired } from '../../middleware/auth.js';
import { adminRequired } from '../../middleware/admin.js';
import { parseListQuery, setRange } from '../../lib/list.js';
import { parseAdminOwnerFilter } from '../../lib/scope.js';
import { notFound } from '../../lib/errors.js';

const router = Router();
router.use(authRequired, adminRequired);

router.get('/', async (req, res) => {
  const p = parseListQuery(req);
  const and: Record<string, unknown>[] = [{ deletedAt: null }];
  if (p.q)
    and.push({
      OR: [
        { account: { contains: p.q, mode: 'insensitive' } },
        { charName: { contains: p.q, mode: 'insensitive' } },
        { manager: { contains: p.q, mode: 'insensitive' } },
      ],
    });
  if (p.filters.serverId) and.push({ serverId: Number(p.filters.serverId) });
  if (p.filters.client) and.push({ client: Number(p.filters.client) });
  if (p.filters.enable !== undefined) and.push({ enable: p.filters.enable === 'true' });
  if (p.filters.obsStatus) and.push({ obsStatus: p.filters.obsStatus as 'ONLINE' | 'OFFLINE' | 'CONNECTING' });

  const ownerId = parseAdminOwnerFilter(req);
  if (ownerId !== undefined) and.push({ ownerId });

  const where = { AND: and };
  const [total, items] = await Promise.all([
    prisma.bot.count({ where }),
    prisma.bot.findMany({
      where,
      skip: p.skip,
      take: p.take,
      orderBy: p.sort ? { [p.sort]: p.order } : { id: 'asc' },
    }),
  ]);
  setRange(res, 'bots', p.start, items.length, total);
  res.json(items);
});

router.get('/:id', async (req, res) => {
  const id = Number(req.params.id);
  const bot = await prisma.bot.findUnique({ where: { id } });
  if (!bot || bot.deletedAt) throw notFound();
  res.json(bot);
});

export default router;
