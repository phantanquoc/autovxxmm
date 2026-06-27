import { Router } from 'express';
import { prisma } from '../lib/prisma.js';
import { authRequired } from '../middleware/auth.js';

const router = Router();

// GET /api/resource/server — trả về list game server. Client cache 1 lần khi login.
router.get('/server', authRequired, async (_req, res) => {
  const servers = await prisma.server.findMany({
    orderBy: { id: 'asc' },
    select: { id: true, name: true, ip: true, port: true, type: true },
  });
  res.json(servers);
});

export default router;
