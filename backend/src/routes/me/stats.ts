import { Router } from 'express';
import { prisma } from '../../lib/prisma.js';
import { getOwnerScope } from '../../lib/scope.js';

const router = Router();

router.get('/', async (req, res) => {
  const ownerId = getOwnerScope(req);
  const since24h = new Date(Date.now() - 24 * 60 * 60 * 1000);

  const [
    totalBots,
    onlineBots,
    totalOrders,
    activeOrders,
    last24hOrders,
    totalTradeLogs,
    last24hTrade,
    betSum,
    collectSum,
  ] = await Promise.all([
    prisma.bot.count({ where: { ownerId, deletedAt: null } }),
    prisma.bot.count({ where: { ownerId, deletedAt: null, obsStatus: 'ONLINE' } }),
    prisma.order.count({ where: { ownerId, deletedAt: null } }),
    prisma.order.count({ where: { ownerId, deletedAt: null, status: { in: [0, 1, 3] } } }),
    prisma.order.count({ where: { ownerId, deletedAt: null, createdAt: { gte: since24h } } }),
    prisma.tradeLog.count({ where: { ownerId } }),
    prisma.tradeLog.count({ where: { ownerId, createdAt: { gte: since24h } } }),
    prisma.order.aggregate({ _sum: { coinOrder: true }, where: { ownerId, deletedAt: null } }),
    prisma.collectTask.aggregate({ _sum: { totalCollected: true }, where: { ownerId } }),
  ]);

  res.json({
    bots: { total: totalBots, online: onlineBots },
    orders: { total: totalOrders, active: activeOrders, last24h: last24hOrders },
    tradeLogs: { total: totalTradeLogs, last24h: last24hTrade },
    coins: {
      totalBet: betSum._sum.coinOrder ?? 0,
      totalCollected: collectSum._sum.totalCollected ?? 0,
    },
  });
});

export default router;
