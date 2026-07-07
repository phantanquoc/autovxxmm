import { Router } from 'express';
import { z } from 'zod';
import { prisma } from '../lib/prisma.js';
import { authRequired, readClient } from '../middleware/auth.js';
import { getAuthContext, getSyncState, noContent } from '../lib/sync.js';
import { getOwnerScope } from '../lib/scope.js';

const router = Router();

const STATUS_WAIT = 0;
const STATUS_BET = 1;
const STATUS_LOSE = 2;
const STATUS_WIN = 3;
const STATUS_REWARD = 4;
const STATUS_ERROR = 5;

const createBody = z.object({
  serverId: z.number().int(),
  name: z.string(),
  bot: z.string(),
  second: z.number().int(),
  type: z.number().int(),
  timeStart: z.number().int(),
  coinOrder: z.number().int(),
  botId: z.number().int(),
  // client cũ (jar non-split) gửi sentinel -1 nghĩa là "không chia client";
  // giá trị âm hoặc thiếu đều chuẩn hoá về 0 = không phân vùng,
  // để cứu ngay jar đã phân phối mà không phải rải lại.
  client: z.number().int().default(0).transform((v) => (v < 0 ? 0 : v)),
});

router.post('/create', authRequired, readClient, async (req, res) => {
  const b = createBody.parse(req.body);
  const ownerId = getOwnerScope(req);
  const order = await prisma.order.create({
    data: {
      serverId: b.serverId,
      botId: b.botId,
      client: b.client,
      name: b.name,
      bot: b.bot,
      second: b.second,
      type: b.type,
      timeStart: BigInt(b.timeStart),
      coinOrder: b.coinOrder,
      status: STATUS_WAIT,
      ownerId,
    },
    select: { id: true },
  });
  res.json(order);
});

const idBody = z.object({ id: z.number().int() });

router.put('/bet', authRequired, async (req, res) => {
  const { id } = idBody.parse(req.body);
  const ownerId = getOwnerScope(req);
  await prisma.order.updateMany({ where: { id, ownerId }, data: { status: STATUS_BET } });
  noContent(res);
});

const loseBody = z.object({ id: z.number().int(), winName: z.string() });
router.put('/lose', authRequired, async (req, res) => {
  const b = loseBody.parse(req.body);
  const ownerId = getOwnerScope(req);
  await prisma.order.updateMany({
    where: { id: b.id, ownerId },
    data: { status: STATUS_LOSE, winName: b.winName, timeStop: BigInt(Date.now()) },
  });
  noContent(res);
});

const winBody = z.object({
  id: z.number().int(),
  coinWin: z.number().int(),
  coinFee: z.number().int(),
  coinReward: z.number().int(),
});
router.put('/win', authRequired, async (req, res) => {
  const b = winBody.parse(req.body);
  const ownerId = getOwnerScope(req);
  await prisma.order.updateMany({
    where: { id: b.id, ownerId },
    data: {
      status: STATUS_WIN,
      coinWin: b.coinWin,
      coinFee: b.coinFee,
      coinReward: b.coinReward,
      timeStop: BigInt(Date.now()),
    },
  });
  noContent(res);
});

router.put('/reward', authRequired, async (req, res) => {
  const { id } = idBody.parse(req.body);
  const ownerId = getOwnerScope(req);
  await prisma.order.updateMany({ where: { id, ownerId }, data: { status: STATUS_REWARD } });
  noContent(res);
});

const errorBody = z.object({ id: z.number().int(), reason: z.string() });
router.put('/error', authRequired, async (req, res) => {
  const b = errorBody.parse(req.body);
  const ownerId = getOwnerScope(req);
  await prisma.order.updateMany({
    where: { id: b.id, ownerId },
    data: { status: STATUS_ERROR, reason: b.reason, timeStop: BigInt(Date.now()) },
  });
  noContent(res);
});

const logBody = z.object({ id: z.number().int(), log: z.string() });
router.put('/log', authRequired, async (req, res) => {
  const b = logBody.parse(req.body);
  const ownerId = getOwnerScope(req);
  await prisma.order.updateMany({ where: { id: b.id, ownerId }, data: { log: b.log } });
  noContent(res);
});

router.get('/check-update', authRequired, readClient, async (req, res) => {
  const ctx = getAuthContext(req);
  const ownerId = getOwnerScope(req);
  const state = await getSyncState(ctx.userId, req.client);
  const now = new Date();

  const updatedClause = req.client === 0
    ? { ownerId, updatedAt: { gt: state.ordersUpdatedAt } }
    : { ownerId, updatedAt: { gt: state.ordersUpdatedAt }, client: req.client };

  const deletedClause = req.client === 0
    ? { ownerId, deletedAt: { gt: state.ordersDeletedAt } }
    : { ownerId, deletedAt: { gt: state.ordersDeletedAt }, client: req.client };

  const [updated, deleted] = await Promise.all([
    prisma.order.findMany({
      where: { ...updatedClause, deletedAt: null },
      select: { id: true, botId: true, serverId: true, status: true },
    }),
    prisma.order.findMany({
      where: deletedClause,
      select: { id: true, botId: true, serverId: true },
    }),
  ]);

  await prisma.clientSyncState.update({
    where: { userId_client: { userId: ctx.userId, client: req.client } },
    data: { ordersUpdatedAt: now, ordersDeletedAt: now, lastSeenAt: now },
  });

  res.json({
    ordersUpdateStatus: updated.map((o) => ({
      id: o.id,
      botId: o.botId,
      serverId: o.serverId,
      status: o.status,
    })),
    ordersDelete: deleted.map((o) => ({
      id: o.id,
      botId: o.botId,
      serverId: o.serverId,
    })),
  });
});

export default router;
