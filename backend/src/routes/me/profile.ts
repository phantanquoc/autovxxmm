import { Router } from 'express';
import { z } from 'zod';
import { prisma } from '../../lib/prisma.js';
import { getOwnerScope } from '../../lib/scope.js';
import { badRequest } from '../../lib/errors.js';

const router = Router();

// GET /api/me/profile — returns id, username, role, collectThreshold, collectKeep
router.get('/', async (req, res) => {
  const ownerId = getOwnerScope(req);
  const user = await prisma.user.findUnique({
    where: { id: ownerId },
    select: { id: true, username: true, role: true, collectThreshold: true, collectKeep: true },
  });
  if (!user) {
    res.status(404).json({ error: 'User not found' });
    return;
  }
  res.json({
    id: user.id,
    username: user.username,
    role: user.role,
    collectThreshold: user.collectThreshold,
    collectKeep: user.collectKeep,
  });
});

// Zod strict schema — extra fields like role/username/password cause a parse error
const putBody = z.object({
  collectThreshold: z.number().int().nonnegative().optional(),
  collectKeep: z.number().int().nonnegative().optional(),
}).strict();

// PUT /api/me/profile — updates collectThreshold and/or collectKeep
router.put('/', async (req, res) => {
  const ownerId = getOwnerScope(req);

  const parsed = putBody.safeParse(req.body);
  if (!parsed.success) {
    throw badRequest('Invalid fields', parsed.error.flatten());
  }

  const { collectThreshold: newThreshold, collectKeep: newKeep } = parsed.data;

  // Load current values for partial merge validation
  const current = await prisma.user.findUnique({
    where: { id: ownerId },
    select: { collectThreshold: true, collectKeep: true },
  });
  if (!current) {
    res.status(404).json({ error: 'User not found' });
    return;
  }

  const effectiveThreshold = newThreshold ?? current.collectThreshold;
  const effectiveKeep = newKeep ?? current.collectKeep;

  // threshold must be strictly greater than keep
  if (effectiveThreshold <= effectiveKeep) {
    throw badRequest('collectThreshold must be greater than collectKeep');
  }

  const updated = await prisma.user.update({
    where: { id: ownerId },
    data: {
      ...(newThreshold !== undefined && { collectThreshold: newThreshold }),
      ...(newKeep !== undefined && { collectKeep: newKeep }),
    },
    select: { id: true, username: true, role: true, collectThreshold: true, collectKeep: true },
  });

  res.json({
    id: updated.id,
    username: updated.username,
    role: updated.role,
    collectThreshold: updated.collectThreshold,
    collectKeep: updated.collectKeep,
  });
});

export default router;
