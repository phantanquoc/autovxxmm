import { Router } from 'express';
import { z } from 'zod';
import { BotRole, BotStatus } from '@prisma/client';
import { prisma } from '../../lib/prisma.js';
import { getOwnerScope } from '../../lib/scope.js';
import { parseListQuery, setRange } from '../../lib/list.js';
import { badRequest, notFound } from '../../lib/errors.js';
import { COLLECT_STATUS } from '../../lib/botRoles.js';

const router = Router();

// POST /api/me/collect/scan — preview targets without side effects
router.post('/scan', async (req, res) => {
  const ownerId = getOwnerScope(req);

  // Get caller's threshold and keep
  const user = await prisma.user.findUnique({
    where: { id: ownerId },
    select: { collectThreshold: true, collectKeep: true },
  });
  if (!user) {
    res.status(404).json({ error: 'User not found' });
    return;
  }

  const { collectThreshold: threshold, collectKeep: keep } = user;

  // Count available collector bots
  const collectBotsAvailable = await prisma.bot.count({
    where: { ownerId, role: BotRole.COLLECT, deletedAt: null },
  });

  // Find order bots that are online and exceed threshold
  const candidateBots = await prisma.bot.findMany({
    where: {
      ownerId,
      role: BotRole.ORDER,
      deletedAt: null,
      obsStatus: BotStatus.ONLINE,
      obsCoin: { gt: threshold },
    },
    select: { id: true, charName: true, serverId: true, obsCoin: true },
  });

  const targets = candidateBots.map((b) => ({
    botId: b.id,
    charName: b.charName,
    serverId: b.serverId,
    coinNow: b.obsCoin ?? 0,
    expectedCollect: (b.obsCoin ?? 0) - keep,
  }));

  res.json({ targets, collectBotsAvailable, threshold, keep });
});

// POST /api/me/collect/tasks — commit a collect task
const createTaskBody = z.object({
  botIds: z.array(z.number().int()).min(1),
});

router.post('/tasks', async (req, res) => {
  const ownerId = getOwnerScope(req);

  const parsed = createTaskBody.safeParse(req.body);
  if (!parsed.success) {
    throw badRequest('Invalid body', parsed.error.flatten());
  }

  const { botIds } = parsed.data;

  // Get caller's current threshold and keep (snapshot at task creation time)
  const user = await prisma.user.findUnique({
    where: { id: ownerId },
    select: { collectThreshold: true, collectKeep: true },
  });
  if (!user) {
    res.status(404).json({ error: 'User not found' });
    return;
  }

  const { collectThreshold: threshold, collectKeep: keep } = user;

  // Recompute targets from current state — drop ineligible botIds silently
  const eligible = await prisma.bot.findMany({
    where: {
      id: { in: botIds },
      ownerId,
      role: BotRole.ORDER,
      deletedAt: null,
      obsStatus: BotStatus.ONLINE,
      obsCoin: { gt: threshold },
    },
    select: { id: true, charName: true, serverId: true, obsCoin: true },
  });

  if (eligible.length === 0) {
    throw badRequest('No eligible bots cross the current threshold');
  }

  const targets = eligible.map((b) => ({
    botId: b.id,
    charName: b.charName,
    serverId: b.serverId,
    coinNow: b.obsCoin ?? 0,
    expectedCollect: (b.obsCoin ?? 0) - keep,
  }));

  const task = await prisma.collectTask.create({
    data: {
      ownerId,
      status: COLLECT_STATUS.PENDING,
      targets,
      threshold,
      keep,
      totalCollected: 0,
    },
  });

  res.status(201).json(task);
});

// GET /api/me/collect/tasks — list tasks for caller
router.get('/tasks', async (req, res) => {
  const ownerId = getOwnerScope(req);
  const p = parseListQuery(req);

  const and: Record<string, unknown>[] = [{ ownerId }];
  if (p.filters.status !== undefined) {
    and.push({ status: Number(p.filters.status) });
  }

  const where = { AND: and };
  const [total, items] = await Promise.all([
    prisma.collectTask.count({ where }),
    prisma.collectTask.findMany({
      where,
      skip: p.skip,
      take: p.take,
      orderBy: p.sort ? { [p.sort]: p.order } : { createdAt: 'desc' },
    }),
  ]);
  setRange(res, 'collect-tasks', p.start, items.length, total);
  res.json(items);
});

// GET /api/me/collect/tasks/:id — detail
router.get('/tasks/:id', async (req, res) => {
  const ownerId = getOwnerScope(req);
  const id = Number(req.params.id);
  const task = await prisma.collectTask.findUnique({ where: { id } });
  if (!task || task.ownerId !== ownerId) throw notFound();
  res.json(task);
});

export default router;
