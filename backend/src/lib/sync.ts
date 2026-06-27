import type { Request, Response } from 'express';
import { prisma } from './prisma.js';

// Mỗi (userId, client) có 1 dòng ClientSyncState theo dõi timestamp của từng loại change.
// Helper getOrCreate đảm bảo dòng đó tồn tại trước khi đọc/ghi.
export async function getSyncState(userId: number, client: number) {
  return prisma.clientSyncState.upsert({
    where: { userId_client: { userId, client } },
    update: {},
    create: { userId, client },
  });
}

export function getAuthContext(req: Request) {
  const userId = req.user?.sub;
  const client = req.client;
  if (userId === undefined) throw new Error('getAuthContext called without auth');
  return { userId, client };
}

// Một số endpoint của Java trả về body rỗng (PUT exit, PUT bet/lose/win,…). Bọc cho gọn.
export function noContent(res: Response) {
  res.status(200).json({ ok: true });
}
