import { BotRole } from '@prisma/client';

// Re-export BotRole enum values as constants for convenience in route handlers.
export const BOT_ROLE = BotRole;
export type BotRoleType = keyof typeof BotRole;

// Status constants for CollectTask
export const COLLECT_STATUS = {
  PENDING: 0,
  IN_PROGRESS: 1,
  DONE: 2,
  FAILED: 3,
  CANCELLED: 4,
} as const;

export type CollectStatusType = typeof COLLECT_STATUS[keyof typeof COLLECT_STATUS];
