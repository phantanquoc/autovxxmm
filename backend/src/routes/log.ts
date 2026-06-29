import { Router } from 'express';
import { z } from 'zod';
import { prisma } from '../lib/prisma.js';
import { authRequired } from '../middleware/auth.js';
import { noContent } from '../lib/sync.js';
import { getOwnerScope } from '../lib/scope.js';

const router = Router();

const tradeBody = z.object({
  serverId: z.number().int(),
  name: z.string(),
  customer: z.string(),
  before: z.number().int(),
  after: z.number().int(),
  change: z.number().int(),
  description: z.string(),
  type: z.number().int(),
  timeStart: z.number().int(),
  timeStop: z.number().int(),
});

router.put('/trade', authRequired, async (req, res) => {
  const b = tradeBody.parse(req.body);
  const ownerId = getOwnerScope(req);
  await prisma.tradeLog.create({
    data: {
      serverId: b.serverId,
      name: b.name,
      customer: b.customer,
      before: b.before,
      after: b.after,
      change: b.change,
      description: b.description,
      type: b.type,
      timeStart: BigInt(b.timeStart),
      timeStop: BigInt(b.timeStop),
      ownerId,
    },
  });
  noContent(res);
});

export default router;
