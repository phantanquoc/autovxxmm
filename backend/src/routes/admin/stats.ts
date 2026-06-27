import { Router } from 'express';
import { prisma } from '../../lib/prisma.js';
import { authRequired } from '../../middleware/auth.js';
import { adminRequired } from '../../middleware/admin.js';

const router = Router();
router.use(authRequired, adminRequired);

router.get('/', async (_req, res) => {
  const since24h = new Date(Date.now() - 24 * 60 * 60 * 1000);
  const [
    totalBots,
    onlineBots,
    totalOrders,
    activeOrders,
    last24hOrders,
    totalTradeLogs,
    last24hTrade,
  ] = await Promise.all([
    prisma.bot.count({ where: { deletedAt: null } }),
    prisma.bot.count({ where: { deletedAt: null, obsStatus: 'ONLINE' } }),
    prisma.order.count({ where: { deletedAt: null } }),
    prisma.order.count({ where: { deletedAt: null, status: { in: [0, 1, 3] } } }),
    prisma.order.count({ where: { deletedAt: null, createdAt: { gte: since24h } } }),
    prisma.tradeLog.count(),
    prisma.tradeLog.count({ where: { createdAt: { gte: since24h } } }),
  ]);
  res.json({
    bots: { total: totalBots, online: onlineBots },
    orders: { total: totalOrders, active: activeOrders, last24h: last24hOrders },
    tradeLogs: { total: totalTradeLogs, last24h: last24hTrade },
  });
});

export default router;
