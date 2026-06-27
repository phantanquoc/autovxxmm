import { Router } from 'express';
import bcrypt from 'bcryptjs';
import { z } from 'zod';
import { Role } from '@prisma/client';
import { prisma } from '../../lib/prisma.js';
import { authRequired } from '../../middleware/auth.js';
import { adminRequired } from '../../middleware/admin.js';
import { parseListQuery, setRange } from '../../lib/list.js';
import { badRequest, notFound } from '../../lib/errors.js';

const router = Router();
router.use(authRequired, adminRequired);

router.get('/', async (req, res) => {
  const p = parseListQuery(req);
  const where = p.q ? { username: { contains: p.q, mode: 'insensitive' as const } } : {};
  const [total, items] = await Promise.all([
    prisma.user.count({ where }),
    prisma.user.findMany({
      where,
      skip: p.skip,
      take: p.take,
      orderBy: p.sort ? { [p.sort]: p.order } : { id: 'asc' },
      select: { id: true, username: true, role: true, enabled: true, createdAt: true, updatedAt: true },
    }),
  ]);
  setRange(res, 'users', p.start, items.length, total);
  res.json(items);
});

router.get('/:id', async (req, res) => {
  const id = Number(req.params.id);
  const user = await prisma.user.findUnique({
    where: { id },
    select: { id: true, username: true, role: true, enabled: true, createdAt: true, updatedAt: true },
  });
  if (!user) throw notFound();
  res.json(user);
});

const createBody = z.object({
  username: z.string().min(3),
  password: z.string().min(6),
  role: z.nativeEnum(Role).default(Role.USER),
  enabled: z.boolean().default(true),
});
router.post('/', async (req, res) => {
  const b = createBody.parse(req.body);
  const exists = await prisma.user.findUnique({ where: { username: b.username } });
  if (exists) throw badRequest('Username đã tồn tại');
  const user = await prisma.user.create({
    data: { username: b.username, password: await bcrypt.hash(b.password, 10), role: b.role, enabled: b.enabled },
    select: { id: true, username: true, role: true, enabled: true, createdAt: true, updatedAt: true },
  });
  res.json(user);
});

const updateBody = z.object({
  username: z.string().min(3).optional(),
  password: z.string().min(6).optional(),
  role: z.nativeEnum(Role).optional(),
  enabled: z.boolean().optional(),
});
router.put('/:id', async (req, res) => {
  const id = Number(req.params.id);
  const b = updateBody.parse(req.body);
  const data: Record<string, unknown> = {};
  if (b.username !== undefined) data.username = b.username;
  if (b.password) data.password = await bcrypt.hash(b.password, 10);
  if (b.role !== undefined) data.role = b.role;
  if (b.enabled !== undefined) data.enabled = b.enabled;
  const user = await prisma.user.update({
    where: { id },
    data,
    select: { id: true, username: true, role: true, enabled: true, createdAt: true, updatedAt: true },
  });
  res.json(user);
});

router.delete('/:id', async (req, res) => {
  const id = Number(req.params.id);
  if (id === req.user?.sub) throw badRequest('Không thể xoá chính mình');
  await prisma.user.delete({ where: { id } });
  res.json({ id });
});

router.get('/me/profile', async (req, res) => {
  // React Admin gọi để biết role hiện tại
  res.json({ id: req.user!.sub, username: req.user!.username, role: req.user!.role });
});

export default router;
