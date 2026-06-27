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

const createBody = z.object({
  serverId: z.number().int(),
  account: z.string().min(1),
  password: z.string().min(1),
  charName: z.string().default(''),
  mapId: z.number().int().default(0),
  zoneId: z.number().int().default(0),
  posX: z.number().int().default(0),
  posY: z.number().int().default(0),
  manager: z.string().default(''),
  chat: z.string().default(''),
  sms: z.string().default(''),
  enable: z.boolean().default(true),
  playFee: z.number().int().default(0),
  typeLuckyDraw: z.number().int().default(0),
  client: z.number().int().min(0).max(10).default(0),
});

router.post('/', async (req, res) => {
  const b = createBody.parse(req.body);
  const bot = await prisma.bot.create({ data: b });
  res.json(bot);
});

router.put('/:id', async (req, res) => {
  const id = Number(req.params.id);
  const b = createBody.partial().parse(req.body);
  const existing = await prisma.bot.findUnique({ where: { id } });
  if (!existing) throw notFound();

  // Khi client field thay đổi → log reassignment để client cũ/mới sync đúng.
  if (b.client !== undefined && b.client !== existing.client) {
    await prisma.botReassignment.create({
      data: { botId: id, fromClient: existing.client, toClient: b.client },
    });
  }
  const bot = await prisma.bot.update({ where: { id }, data: b });
  res.json(bot);
});

router.delete('/:id', async (req, res) => {
  const id = Number(req.params.id);
  const bot = await prisma.bot.update({ where: { id }, data: { deletedAt: new Date() } });
  res.json({ id: bot.id });
});

export default router;
