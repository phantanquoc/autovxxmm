import { Router, type Request } from 'express';
import { z } from 'zod';
import { BotStatus, BotRole } from '@prisma/client';
import { prisma } from '../lib/prisma.js';
import { authRequired, readClient } from '../middleware/auth.js';
import { getAuthContext, getSyncState, noContent } from '../lib/sync.js';
import { getOwnerScope } from '../lib/scope.js';
import { badRequest } from '../lib/errors.js';

const router = Router();

type Mode = 'normal' | 'split';

// Java client gửi mode qua URL: /api/client/bots/normal hoặc /api/client/bots/split
// Trong split mode, header `client` quyết định nhóm bot nào client thấy.
async function modeFilter(req: Request): Promise<{ mode: Mode; client: number }> {
  const mode = req.params.mode;
  if (mode !== 'normal' && mode !== 'split') throw badRequest('Invalid mode');
  return { mode, client: mode === 'split' ? req.client : 0 };
}

function botToJson(b: {
  id: number;
  account: string;
  password: string;
  charName: string;
  serverId: number;
  mapId: number;
  zoneId: number;
  posX: number;
  posY: number;
  manager: string;
  chat: string;
  sms: string;
  enable: boolean;
  playFee: number;
  typeLuckyDraw: number;
}) {
  return {
    id: b.id,
    account: b.account,
    password: b.password,
    charName: b.charName,
    serverId: b.serverId,
    mapId: b.mapId,
    zoneId: b.zoneId,
    posX: b.posX,
    posY: b.posY,
    manager: b.manager,
    chat: b.chat,
    sms: b.sms,
    enable: b.enable,
    playFee: b.playFee,
    typeLuckyDraw: b.typeLuckyDraw,
  };
}

function whereForClient(mode: Mode, client: number, ownerId: number) {
  if (mode === 'normal') return { deletedAt: null, ownerId, role: BotRole.ORDER };
  return { deletedAt: null, client, ownerId, role: BotRole.ORDER };
}

// Observer push payload — shared by normal/split and collect PUT handlers
const observePayload = z.array(
  z.object({
    id: z.number().int(),
    name: z.string(),
    level: z.number().int(),
    clan: z.string().nullable(),
    coin: z.number().int(),
    gold: z.number().int(),
    status: z.nativeEnum(BotStatus),
    lastOnline: z.number().int().nonnegative(),
  })
);

// ─── /api/client/bots/collect routes ────────────────────────────────────────
// Registered BEFORE the /:mode wildcard so they take precedence.
// Returns the caller's COLLECT bots in the same shape as /normal.
// Option (b): separate /collect endpoint — less invasive to the normal/split logic.

router.get('/collect', authRequired, readClient, async (req, res) => {
  const ctx = getAuthContext(req);
  const ownerId = getOwnerScope(req);
  const now = new Date();

  const bots = await prisma.bot.findMany({
    where: { ownerId, role: BotRole.COLLECT, deletedAt: null },
    orderBy: { id: 'asc' },
  });

  // Upsert a sync state entry for collect bots (client=0 since collect bots have no split mode)
  await prisma.clientSyncState.upsert({
    where: { userId_client: { userId: ctx.userId, client: 0 } },
    update: { collectTasksAt: now, lastSeenAt: now },
    create: { userId: ctx.userId, client: 0, collectTasksAt: now, lastSeenAt: now },
  });

  res.json(bots.map(botToJson));
});

router.get('/collect/check-update', authRequired, readClient, async (req, res) => {
  const ctx = getAuthContext(req);
  const ownerId = getOwnerScope(req);
  const state = await getSyncState(ctx.userId, 0);
  const now = new Date();

  const [hasNewBots, hasUpdatedBots, hasDeletedBots] = await Promise.all([
    prisma.bot.count({
      where: { ownerId, role: BotRole.COLLECT, deletedAt: null, createdAt: { gt: state.newBotsAt } },
    }),
    prisma.bot.count({
      where: {
        ownerId, role: BotRole.COLLECT, deletedAt: null,
        updatedAt: { gt: state.updatedBotsAt }, createdAt: { lte: state.newBotsAt },
      },
    }),
    prisma.bot.count({
      where: { ownerId, role: BotRole.COLLECT, deletedAt: { gt: state.deletedBotsAt } },
    }),
  ]);

  await prisma.clientSyncState.update({
    where: { userId_client: { userId: ctx.userId, client: 0 } },
    data: { lastSeenAt: now },
  });

  res.json({
    hasNewBots: hasNewBots > 0,
    hasUpdatedBots: hasUpdatedBots > 0,
    hasDeletedBots: hasDeletedBots > 0,
    hasChangedClientBotsToDelete: false,
    hasChangedClientNewBots: false,
  });
});

router.get('/collect/new', authRequired, readClient, async (req, res) => {
  const ctx = getAuthContext(req);
  const ownerId = getOwnerScope(req);
  const state = await getSyncState(ctx.userId, 0);
  const now = new Date();

  const bots = await prisma.bot.findMany({
    where: { ownerId, role: BotRole.COLLECT, deletedAt: null, createdAt: { gt: state.newBotsAt } },
    orderBy: { id: 'asc' },
  });

  await prisma.clientSyncState.update({
    where: { userId_client: { userId: ctx.userId, client: 0 } },
    data: { newBotsAt: now, updatedBotsAt: now, lastSeenAt: now },
  });

  res.json(bots.map(botToJson));
});

router.get('/collect/updated', authRequired, readClient, async (req, res) => {
  const ctx = getAuthContext(req);
  const ownerId = getOwnerScope(req);
  const state = await getSyncState(ctx.userId, 0);
  const now = new Date();

  const bots = await prisma.bot.findMany({
    where: {
      ownerId, role: BotRole.COLLECT, deletedAt: null,
      updatedAt: { gt: state.updatedBotsAt }, createdAt: { lte: state.newBotsAt },
    },
    orderBy: { id: 'asc' },
  });

  await prisma.clientSyncState.update({
    where: { userId_client: { userId: ctx.userId, client: 0 } },
    data: { updatedBotsAt: now, lastSeenAt: now },
  });

  res.json(bots.map(botToJson));
});

router.get('/collect/deleted', authRequired, readClient, async (req, res) => {
  const ctx = getAuthContext(req);
  const ownerId = getOwnerScope(req);
  const state = await getSyncState(ctx.userId, 0);
  const now = new Date();

  const bots = await prisma.bot.findMany({
    where: { ownerId, role: BotRole.COLLECT, deletedAt: { gt: state.deletedBotsAt } },
    select: { id: true },
  });

  await prisma.clientSyncState.update({
    where: { userId_client: { userId: ctx.userId, client: 0 } },
    data: { deletedBotsAt: now, lastSeenAt: now },
  });

  res.json(bots.map((b) => b.id));
});

// Observer push for collect bots — same endpoint format as normal/split
router.put('/collect', authRequired, readClient, async (req, res) => {
  const ownerId = getOwnerScope(req);
  const items = observePayload.parse(req.body);
  if (items.length === 0) {
    noContent(res);
    return;
  }
  await prisma.$transaction(
    items.map((it) =>
      prisma.bot.updateMany({
        where: { id: it.id, ownerId, role: BotRole.COLLECT },
        data: {
          obsName: it.name,
          obsLevel: it.level,
          obsClan: it.clan,
          obsCoin: it.coin,
          obsGold: it.gold,
          obsStatus: it.status,
          obsLastOnline: BigInt(it.lastOnline),
        },
      })
    )
  );
  noContent(res);
});

// ─── End /collect routes ─────────────────────────────────────────────────────

// Initial full sync.
router.get('/:mode', authRequired, readClient, async (req, res) => {
  const { mode, client } = await modeFilter(req);
  const ctx = getAuthContext(req);
  const ownerId = getOwnerScope(req);
  const now = new Date();

  const bots = await prisma.bot.findMany({
    where: whereForClient(mode, client, ownerId),
    orderBy: { id: 'asc' },
  });

  await prisma.clientSyncState.upsert({
    where: { userId_client: { userId: ctx.userId, client } },
    update: {
      newBotsAt: now,
      updatedBotsAt: now,
      deletedBotsAt: now,
      reassignedOutAt: now,
      reassignedInAt: now,
      lastSeenAt: now,
    },
    create: {
      userId: ctx.userId,
      client,
      newBotsAt: now,
      updatedBotsAt: now,
      deletedBotsAt: now,
      reassignedOutAt: now,
      reassignedInAt: now,
      lastSeenAt: now,
    },
  });

  res.json(bots.map(botToJson));
});

router.get('/:mode/check-update', authRequired, readClient, async (req, res) => {
  const { mode, client } = await modeFilter(req);
  const ctx = getAuthContext(req);
  const ownerId = getOwnerScope(req);
  const state = await getSyncState(ctx.userId, client);

  const baseWhere = whereForClient(mode, client, ownerId);

  const [hasNewBots, hasUpdatedBots, hasDeletedBots, reassignedOut, reassignedIn] = await Promise.all([
    prisma.bot.count({ where: { ...baseWhere, createdAt: { gt: state.newBotsAt } } }),
    prisma.bot.count({
      where: { ...baseWhere, updatedAt: { gt: state.updatedBotsAt }, createdAt: { lte: state.newBotsAt } },
    }),
    prisma.bot.count({
      where: mode === 'normal'
        ? { ownerId, role: BotRole.ORDER, deletedAt: { gt: state.deletedBotsAt } }
        : { ownerId, role: BotRole.ORDER, deletedAt: { gt: state.deletedBotsAt }, client },
    }),
    mode === 'split'
      ? prisma.botReassignment.count({
          where: { fromClient: client, createdAt: { gt: state.reassignedOutAt } },
        })
      : Promise.resolve(0),
    mode === 'split'
      ? prisma.botReassignment.count({
          where: { toClient: client, createdAt: { gt: state.reassignedInAt } },
        })
      : Promise.resolve(0),
  ]);

  res.json({
    hasNewBots: hasNewBots > 0,
    hasUpdatedBots: hasUpdatedBots > 0,
    hasDeletedBots: hasDeletedBots > 0,
    hasChangedClientBotsToDelete: reassignedOut > 0,
    hasChangedClientNewBots: reassignedIn > 0,
  });
});

router.get('/:mode/new', authRequired, readClient, async (req, res) => {
  const { mode, client } = await modeFilter(req);
  const ctx = getAuthContext(req);
  const ownerId = getOwnerScope(req);
  const state = await getSyncState(ctx.userId, client);
  const now = new Date();

  const bots = await prisma.bot.findMany({
    where: { ...whereForClient(mode, client, ownerId), createdAt: { gt: state.newBotsAt } },
    orderBy: { id: 'asc' },
  });

  await prisma.clientSyncState.update({
    where: { userId_client: { userId: ctx.userId, client } },
    data: { newBotsAt: now, updatedBotsAt: now, lastSeenAt: now },
  });

  res.json(bots.map(botToJson));
});

router.get('/:mode/updated', authRequired, readClient, async (req, res) => {
  const { mode, client } = await modeFilter(req);
  const ctx = getAuthContext(req);
  const ownerId = getOwnerScope(req);
  const state = await getSyncState(ctx.userId, client);
  const now = new Date();

  const bots = await prisma.bot.findMany({
    where: {
      ...whereForClient(mode, client, ownerId),
      updatedAt: { gt: state.updatedBotsAt },
      createdAt: { lte: state.newBotsAt },
    },
    orderBy: { id: 'asc' },
  });

  await prisma.clientSyncState.update({
    where: { userId_client: { userId: ctx.userId, client } },
    data: { updatedBotsAt: now, lastSeenAt: now },
  });

  res.json(bots.map(botToJson));
});

router.get('/:mode/deleted', authRequired, readClient, async (req, res) => {
  const { mode, client } = await modeFilter(req);
  const ctx = getAuthContext(req);
  const ownerId = getOwnerScope(req);
  const state = await getSyncState(ctx.userId, client);
  const now = new Date();

  const bots = await prisma.bot.findMany({
    where: mode === 'normal'
      ? { ownerId, role: BotRole.ORDER, deletedAt: { gt: state.deletedBotsAt } }
      : { ownerId, role: BotRole.ORDER, deletedAt: { gt: state.deletedBotsAt }, client },
    select: { id: true },
  });

  await prisma.clientSyncState.update({
    where: { userId_client: { userId: ctx.userId, client } },
    data: { deletedBotsAt: now, lastSeenAt: now },
  });

  res.json(bots.map((b) => b.id));
});

// Bot bị admin reassign từ client này sang client khác — client cũ phải xoá khỏi memory,
// client mới phải tải về như bot mới.
router.get('/:mode/changed/delete', authRequired, readClient, async (req, res) => {
  const { mode, client } = await modeFilter(req);
  if (mode !== 'split') {
    res.json([]);
    return;
  }
  const ctx = getAuthContext(req);
  const ownerId = getOwnerScope(req);
  const state = await getSyncState(ctx.userId, client);
  const now = new Date();

  // Only return reassignments for bots owned by the caller
  const rows = await prisma.botReassignment.findMany({
    where: {
      fromClient: client,
      createdAt: { gt: state.reassignedOutAt },
      bot: { ownerId },
    },
    select: { botId: true },
  });

  await prisma.clientSyncState.update({
    where: { userId_client: { userId: ctx.userId, client } },
    data: { reassignedOutAt: now, lastSeenAt: now },
  });

  res.json(rows.map((r) => r.botId));
});

router.get('/:mode/changed/new', authRequired, readClient, async (req, res) => {
  const { mode, client } = await modeFilter(req);
  if (mode !== 'split') {
    res.json([]);
    return;
  }
  const ctx = getAuthContext(req);
  const ownerId = getOwnerScope(req);
  const state = await getSyncState(ctx.userId, client);
  const now = new Date();

  const rows = await prisma.botReassignment.findMany({
    where: {
      toClient: client,
      createdAt: { gt: state.reassignedInAt },
      bot: { ownerId },
    },
    orderBy: { createdAt: 'asc' },
  });

  const botIds = [...new Set(rows.map((r) => r.botId))];
  const bots = botIds.length
    ? await prisma.bot.findMany({
        where: { id: { in: botIds }, ownerId, deletedAt: null, client, role: BotRole.ORDER },
      })
    : [];

  await prisma.clientSyncState.update({
    where: { userId_client: { userId: ctx.userId, client } },
    data: { reassignedInAt: now, lastSeenAt: now },
  });

  res.json(bots.map(botToJson));
});

router.put('/:mode', authRequired, readClient, async (req, res) => {
  await modeFilter(req); // validate
  const ownerId = getOwnerScope(req);
  const items = observePayload.parse(req.body);
  if (items.length === 0) {
    noContent(res);
    return;
  }
  await prisma.$transaction(
    items.map((it) =>
      prisma.bot.updateMany({
        // updateMany để không throw nếu bot đã bị xoá; ownerId filter ensures cross-owner safety
        // role=ORDER filter ensures COLLECT bots can only be updated via PUT /collect
        where: { id: it.id, ownerId, role: BotRole.ORDER },
        data: {
          obsName: it.name,
          obsLevel: it.level,
          obsClan: it.clan,
          obsCoin: it.coin,
          obsGold: it.gold,
          obsStatus: it.status,
          obsLastOnline: BigInt(it.lastOnline),
        },
      })
    )
  );
  noContent(res);
});

router.put('/:mode/exit', authRequired, readClient, async (req, res) => {
  await modeFilter(req);
  const ctx = getAuthContext(req);
  const ownerId = getOwnerScope(req);
  if (req.params.mode === 'split') {
    await prisma.bot.updateMany({
      where: { ownerId, role: BotRole.ORDER, client: req.client, deletedAt: null },
      data: { obsStatus: BotStatus.OFFLINE },
    });
  } else {
    await prisma.bot.updateMany({
      where: { ownerId, role: BotRole.ORDER, deletedAt: null },
      data: { obsStatus: BotStatus.OFFLINE },
    });
  }
  await prisma.clientSyncState.updateMany({
    where: { userId: ctx.userId, client: req.client },
    data: { lastSeenAt: new Date() },
  });
  noContent(res);
});

export default router;
