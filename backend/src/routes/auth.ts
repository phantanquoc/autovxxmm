import { Router } from 'express';
import bcrypt from 'bcryptjs';
import { z } from 'zod';
import { prisma } from '../lib/prisma.js';
import { signJwt } from '../lib/jwt.js';
import { unauthorized } from '../lib/errors.js';

const router = Router();

const loginBody = z.object({
  username: z.string().min(1),
  password: z.string().min(1),
});

router.post('/login', async (req, res) => {
  const body = loginBody.parse(req.body);
  const user = await prisma.user.findUnique({ where: { username: body.username } });
  if (!user || !user.enabled) throw unauthorized('Sai tài khoản hoặc mật khẩu');

  const ok = await bcrypt.compare(body.password, user.password);
  if (!ok) throw unauthorized('Sai tài khoản hoặc mật khẩu');

  const jwt = signJwt({ sub: user.id, username: user.username, role: user.role });
  res.json({ jwt });
});

router.get('/health', (_req, res) => {
  res.json({ status: 'ok', time: Date.now() });
});

export default router;
