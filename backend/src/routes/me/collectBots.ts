import { Router } from 'express';
import { z } from 'zod';
import { BotRole } from '@prisma/client';
import { prisma } from '../../lib/prisma.js';
import { getOwnerScope } from '../../lib/scope.js';
import { parseListQuery, setRange } from '../../lib/list.js';
import { notFound, badRequest } from '../../lib/errors.js';

const router = Router();

// Body schema for collect bots — excludes playFee and typeLuckyDraw (collector bots don't need them)
const collectBotBody = z.object({
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
  client: z.number().int().min(0).max(10).default(0),
});

router.get('/', async (req, res) => {
  const ownerId = getOwnerScope(req);
  const p = parseListQuery(req);
  const and: Record<string, unknown>[] = [
    { deletedAt: null },
    { ownerId },
    { role: BotRole.COLLECT },  // always filter to COLLECT
  ];
  if (p.q)
    and.push({
      OR: [
        { account: { contains: p.q, mode: 'insensitive' } },
        { charName: { contains: p.q, mode: 'insensitive' } },
        { manager: { contains: p.q, mode: 'insensitive' } },
      ],
    });
  if (p.filters.serverId) and.push({ serverId: Number(p.filters.serverId) });
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
  setRange(res, 'collect-bots', p.start, items.length, total);
  res.json(items);
});

router.get('/:id', async (req, res) => {
  const ownerId = getOwnerScope(req);
  const id = Number(req.params.id);
  const bot = await prisma.bot.findUnique({ where: { id } });
  if (!bot || bot.deletedAt || bot.ownerId !== ownerId || bot.role !== BotRole.COLLECT) throw notFound();
  res.json(bot);
});

router.post('/', async (req, res) => {
  const ownerId = getOwnerScope(req);
  const b = collectBotBody.parse(req.body);
  // Block account+server collision with any non-deleted bot the owner already has
  // (ORDER or COLLECT). Game server only allows one connection per account, so
  // two bots with the same credentials would kick each other off in a loop.
  const collision = await prisma.bot.findFirst({
    where: { ownerId, serverId: b.serverId, account: b.account, deletedAt: null },
    select: { id: true, role: true },
  });
  if (collision) {
    throw badRequest(
      `Tài khoản "${b.account}" đã được dùng cho bot ${collision.role} #${collision.id} ở server này. Hãy dùng tài khoản khác cho bot gom xu.`
    );
  }
  const bot = await prisma.bot.create({
    data: { ...b, ownerId, role: BotRole.COLLECT },
  });
  res.json(bot);
});

router.put('/:id', async (req, res) => {
  const ownerId = getOwnerScope(req);
  const id = Number(req.params.id);
  const existing = await prisma.bot.findUnique({ where: { id } });
  if (!existing || existing.ownerId !== ownerId || existing.role !== BotRole.COLLECT) throw notFound();

  // Reject any body that includes `role`
  if ('role' in req.body) {
    throw badRequest('Cannot change role of a collect bot');
  }

  const b = collectBotBody.partial().parse(req.body);
  // If account or server changed, re-check for collisions with the owner's other bots.
  const nextAccount = b.account ?? existing.account;
  const nextServerId = b.serverId ?? existing.serverId;
  if (nextAccount !== existing.account || nextServerId !== existing.serverId) {
    const collision = await prisma.bot.findFirst({
      where: {
        ownerId,
        serverId: nextServerId,
        account: nextAccount,
        deletedAt: null,
        NOT: { id },
      },
      select: { id: true, role: true },
    });
    if (collision) {
      throw badRequest(
        `Tài khoản "${nextAccount}" đã được dùng cho bot ${collision.role} #${collision.id} ở server này.`
      );
    }
  }
  const bot = await prisma.bot.update({ where: { id }, data: b });
  res.json(bot);
});

router.delete('/:id', async (req, res) => {
  const ownerId = getOwnerScope(req);
  const id = Number(req.params.id);
  const existing = await prisma.bot.findUnique({ where: { id } });
  if (!existing || existing.ownerId !== ownerId || existing.role !== BotRole.COLLECT) throw notFound();
  const bot = await prisma.bot.update({ where: { id }, data: { deletedAt: new Date() } });
  res.json({ id: bot.id });
});

export default router;
