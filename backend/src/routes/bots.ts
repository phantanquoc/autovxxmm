import { Router, type Request } from 'express';
import { z } from 'zod';
import { BotStatus } from '@prisma/client';
import { prisma } from '../lib/prisma.js';
import { authRequired, readClient } from '../middleware/auth.js';
import { getAuthContext, getSyncState, noContent } from '../lib/sync.js';
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

function whereForClient(mode: Mode, client: number) {
  if (mode === 'normal') return { deletedAt: null };
  return { deletedAt: null, client };
}

// Initial full sync.
router.get('/:mode', authRequired, readClient, async (req, res) => {
  const { mode, client } = await modeFilter(req);
  const ctx = getAuthContext(req);
  const now = new Date();

  const bots = await prisma.bot.findMany({
    where: whereForClient(mode, client),
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
  const state = await getSyncState(ctx.userId, client);

  const baseWhere = whereForClient(mode, client);

  const [hasNewBots, hasUpdatedBots, hasDeletedBots, reassignedOut, reassignedIn] = await Promise.all([
    prisma.bot.count({ where: { ...baseWhere, createdAt: { gt: state.newBotsAt } } }),
    prisma.bot.count({
      where: { ...baseWhere, updatedAt: { gt: state.updatedBotsAt }, createdAt: { lte: state.newBotsAt } },
    }),
    prisma.bot.count({
      where: mode === 'normal'
        ? { deletedAt: { gt: state.deletedBotsAt } }
        : { deletedAt: { gt: state.deletedBotsAt }, client },
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
  const state = await getSyncState(ctx.userId, client);
  const now = new Date();

  const bots = await prisma.bot.findMany({
    where: { ...whereForClient(mode, client), createdAt: { gt: state.newBotsAt } },
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
  const state = await getSyncState(ctx.userId, client);
  const now = new Date();

  const bots = await prisma.bot.findMany({
    where: {
      ...whereForClient(mode, client),
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
  const state = await getSyncState(ctx.userId, client);
  const now = new Date();

  const bots = await prisma.bot.findMany({
    where: mode === 'normal'
      ? { deletedAt: { gt: state.deletedBotsAt } }
      : { deletedAt: { gt: state.deletedBotsAt }, client },
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
  const state = await getSyncState(ctx.userId, client);
  const now = new Date();

  const rows = await prisma.botReassignment.findMany({
    where: { fromClient: client, createdAt: { gt: state.reassignedOutAt } },
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
  const state = await getSyncState(ctx.userId, client);
  const now = new Date();

  const rows = await prisma.botReassignment.findMany({
    where: { toClient: client, createdAt: { gt: state.reassignedInAt } },
    orderBy: { createdAt: 'asc' },
  });

  const botIds = [...new Set(rows.map((r) => r.botId))];
  const bots = botIds.length
    ? await prisma.bot.findMany({
        where: { id: { in: botIds }, deletedAt: null, client },
      })
    : [];

  await prisma.clientSyncState.update({
    where: { userId_client: { userId: ctx.userId, client } },
    data: { reassignedInAt: now, lastSeenAt: now },
  });

  res.json(bots.map(botToJson));
});

// Observer push (10s/lần).
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

router.put('/:mode', authRequired, readClient, async (req, res) => {
  await modeFilter(req); // validate
  const items = observePayload.parse(req.body);
  if (items.length === 0) {
    noContent(res);
    return;
  }
  await prisma.$transaction(
    items.map((it) =>
      prisma.bot.updateMany({
        // updateMany để không throw nếu bot đã bị xoá
        where: { id: it.id },
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
  // Client tắt — set tất cả bot trong nhóm sang OFFLINE.
  const ctx = getAuthContext(req);
  if (req.params.mode === 'split') {
    await prisma.bot.updateMany({
      where: { client: req.client, deletedAt: null },
      data: { obsStatus: BotStatus.OFFLINE },
    });
  } else {
    await prisma.bot.updateMany({
      where: { deletedAt: null },
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
