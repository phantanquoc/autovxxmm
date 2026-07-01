import { Router } from 'express';
import { z } from 'zod';
import { prisma } from '../lib/prisma.js';
import { authRequired } from '../middleware/auth.js';
import { getOwnerScope } from '../lib/scope.js';
import { badRequest, notFound } from '../lib/errors.js';
import { COLLECT_STATUS } from '../lib/botRoles.js';

const router = Router();

// GET /api/client/collect/check-update
// Returns { hasPending: bool, taskId: int|null } for the oldest PENDING task of the caller.
router.get('/check-update', authRequired, async (req, res) => {
  const ownerId = getOwnerScope(req);

  const task = await prisma.collectTask.findFirst({
    where: { ownerId, status: COLLECT_STATUS.PENDING },
    orderBy: { createdAt: 'asc' },
    select: { id: true },
  });

  res.json({
    hasPending: task !== null,
    taskId: task?.id ?? null,
  });
});

// GET /api/client/collect/pending
// Atomically picks the oldest PENDING task and transitions it to IN_PROGRESS.
// Returns 404 if none found or if the conditional update matched 0 rows.
router.get('/pending', authRequired, async (req, res) => {
  const ownerId = getOwnerScope(req);

  // Find oldest PENDING task
  const task = await prisma.collectTask.findFirst({
    where: { ownerId, status: COLLECT_STATUS.PENDING },
    orderBy: { createdAt: 'asc' },
  });

  if (!task) throw notFound('No pending collect task');

  // Atomically transition PENDING → IN_PROGRESS via conditional updateMany
  const result = await prisma.collectTask.updateMany({
    where: { id: task.id, ownerId, status: COLLECT_STATUS.PENDING },
    data: { status: COLLECT_STATUS.IN_PROGRESS },
  });

  if (result.count === 0) {
    // Another tick already grabbed it
    throw notFound('No pending collect task');
  }

  // Return the updated task
  const updated = await prisma.collectTask.findUnique({ where: { id: task.id } });
  res.json(updated);
});

// POST /api/client/collect/ack
// Reports per-bot results and writes TradeLog rows for successfully collected bots.
const perBotResultSchema = z.object({
  botId: z.number().int(),
  collected: z.number().int().nonnegative(),
  error: z.string().optional(),
});

const ackBody = z.object({
  taskId: z.number().int(),
  status: z.enum(['DONE', 'FAILED']),
  totalCollected: z.number().int().nonnegative(),
  perBotResults: z.array(perBotResultSchema),
});

router.post('/ack', authRequired, async (req, res) => {
  const ownerId = getOwnerScope(req);

  const parsed = ackBody.safeParse(req.body);
  if (!parsed.success) {
    throw badRequest('Invalid ack body', parsed.error.flatten());
  }

  const { taskId, status, totalCollected, perBotResults } = parsed.data;
  const newStatus = status === 'DONE' ? COLLECT_STATUS.DONE : COLLECT_STATUS.FAILED;
  const completedAt = new Date();
  const now = Date.now();

  // Fetch the task to validate ownership and get createdAt for TradeLog timeStart
  const task = await prisma.collectTask.findFirst({
    where: { id: taskId, ownerId, status: COLLECT_STATUS.IN_PROGRESS },
  });

  if (!task) throw notFound('Task not found or not in progress');

  // Collect bots that had collected > 0 for TradeLog inserts
  const successResults = perBotResults.filter((r) => r.collected > 0);

  // Fetch Bot rows needed for TradeLog (serverId, charName)
  const botIds = successResults.map((r) => r.botId);
  const bots = botIds.length
    ? await prisma.bot.findMany({
        where: { id: { in: botIds }, ownerId },
        select: { id: true, serverId: true, charName: true, obsCoin: true },
      })
    : [];

  const botMap = new Map(bots.map((b) => [b.id, b]));

  // Use a transaction: update task + insert all TradeLogs atomically
  await prisma.$transaction([
    prisma.collectTask.updateMany({
      where: { id: taskId, ownerId, status: COLLECT_STATUS.IN_PROGRESS },
      data: { status: newStatus, totalCollected, completedAt },
    }),
    ...successResults.map((r) => {
      const bot = botMap.get(r.botId);
      if (!bot) return prisma.$queryRaw`SELECT 1`; // skip if bot not found
      const coinAfter = (bot.obsCoin ?? 0);
      const coinBefore = coinAfter + r.collected;
      return prisma.tradeLog.create({
        data: {
          ownerId,
          serverId: bot.serverId,
          name: bot.charName,
          customer: bot.charName,
          before: coinBefore,
          after: coinAfter,
          change: r.collected,
          description: `GOM XU task#${taskId}`,
          type: 2,
          timeStart: BigInt(task.createdAt.getTime()),
          timeStop: BigInt(now),
        },
      });
    }),
  ]);

  res.json({ ok: true });
});

export default router;
