-- CreateEnum: BotRole
CREATE TYPE "BotRole" AS ENUM ('ORDER', 'COLLECT');

-- Phase 1: Add nullable role column to Bot (backfill-safe)
ALTER TABLE "Bot" ADD COLUMN "role" "BotRole";

-- Phase 2: Backfill existing rows to ORDER
UPDATE "Bot" SET "role" = 'ORDER' WHERE "role" IS NULL;

-- Phase 3: Set NOT NULL + DEFAULT
ALTER TABLE "Bot" ALTER COLUMN "role" SET NOT NULL;
ALTER TABLE "Bot" ALTER COLUMN "role" SET DEFAULT 'ORDER';

-- Phase 4: Create composite index for per-owner role-filtered queries
CREATE INDEX "Bot_ownerId_role_deletedAt_idx" ON "Bot"("ownerId", "role", "deletedAt");

-- Add collect settings to User (both NOT NULL with defaults, so no backfill needed)
ALTER TABLE "User" ADD COLUMN "collectThreshold" INTEGER NOT NULL DEFAULT 1000000;
ALTER TABLE "User" ADD COLUMN "collectKeep" INTEGER NOT NULL DEFAULT 0;

-- Create CollectTask table
CREATE TABLE "CollectTask" (
    "id"             SERIAL PRIMARY KEY,
    "ownerId"        INTEGER NOT NULL,
    "status"         INTEGER NOT NULL DEFAULT 0,
    "targets"        JSONB NOT NULL,
    "threshold"      INTEGER NOT NULL,
    "keep"           INTEGER NOT NULL,
    "totalCollected" INTEGER NOT NULL DEFAULT 0,
    "createdAt"      TIMESTAMP(3) NOT NULL DEFAULT CURRENT_TIMESTAMP,
    "updatedAt"      TIMESTAMP(3) NOT NULL,
    "completedAt"    TIMESTAMP(3),
    CONSTRAINT "CollectTask_ownerId_fkey" FOREIGN KEY ("ownerId")
        REFERENCES "User"("id") ON DELETE RESTRICT ON UPDATE CASCADE
);

-- Index for polling oldest pending task per owner
CREATE INDEX "CollectTask_ownerId_status_createdAt_idx"
    ON "CollectTask"("ownerId", "status", "createdAt");

-- Add collectTasksAt to ClientSyncState
ALTER TABLE "ClientSyncState"
    ADD COLUMN "collectTasksAt" TIMESTAMP(3) NOT NULL DEFAULT CURRENT_TIMESTAMP;
