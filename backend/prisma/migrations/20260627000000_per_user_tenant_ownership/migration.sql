-- Migration: per_user_tenant_ownership
-- Phase 1: Add ownerId columns as NULLABLE (no default, no constraint yet)
ALTER TABLE "Bot"      ADD COLUMN "ownerId" INTEGER;
ALTER TABLE "Order"    ADD COLUMN "ownerId" INTEGER;
ALTER TABLE "TradeLog" ADD COLUMN "ownerId" INTEGER;
ALTER TABLE "Block"    ADD COLUMN "ownerId" INTEGER;

-- Phase 2: Backfill all existing rows to admin (id=1)
UPDATE "Bot"      SET "ownerId" = 1;
UPDATE "Order"    SET "ownerId" = 1;
UPDATE "TradeLog" SET "ownerId" = 1;
UPDATE "Block"    SET "ownerId" = 1;

-- Phase 3: Set NOT NULL constraint and add foreign keys (RESTRICT on delete)
ALTER TABLE "Bot"      ALTER COLUMN "ownerId" SET NOT NULL;
ALTER TABLE "Order"    ALTER COLUMN "ownerId" SET NOT NULL;
ALTER TABLE "TradeLog" ALTER COLUMN "ownerId" SET NOT NULL;
ALTER TABLE "Block"    ALTER COLUMN "ownerId" SET NOT NULL;

ALTER TABLE "Bot"      ADD CONSTRAINT "Bot_ownerId_fkey"      FOREIGN KEY ("ownerId") REFERENCES "User"("id") ON DELETE RESTRICT ON UPDATE CASCADE;
ALTER TABLE "Order"    ADD CONSTRAINT "Order_ownerId_fkey"    FOREIGN KEY ("ownerId") REFERENCES "User"("id") ON DELETE RESTRICT ON UPDATE CASCADE;
ALTER TABLE "TradeLog" ADD CONSTRAINT "TradeLog_ownerId_fkey" FOREIGN KEY ("ownerId") REFERENCES "User"("id") ON DELETE RESTRICT ON UPDATE CASCADE;
ALTER TABLE "Block"    ADD CONSTRAINT "Block_ownerId_fkey"    FOREIGN KEY ("ownerId") REFERENCES "User"("id") ON DELETE RESTRICT ON UPDATE CASCADE;

-- Phase 4: Create composite indexes
CREATE INDEX "Bot_ownerId_deletedAt_idx"        ON "Bot"      ("ownerId", "deletedAt");
CREATE INDEX "Bot_ownerId_client_deletedAt_idx" ON "Bot"      ("ownerId", "client", "deletedAt");
CREATE INDEX "Order_ownerId_status_deletedAt_idx" ON "Order"  ("ownerId", "status", "deletedAt");
CREATE INDEX "Order_ownerId_updatedAt_idx"       ON "Order"   ("ownerId", "updatedAt");
CREATE INDEX "TradeLog_ownerId_createdAt_idx"    ON "TradeLog" ("ownerId", "createdAt");

-- Phase 4b: Swap Block unique constraint from (serverId, name) to (ownerId, serverId, name)
-- (Block table was empty in prod, so no constraint name to drop from prior state;
--  but Prisma may have named it - drop by name if it exists, otherwise the add suffices)
ALTER TABLE "Block" DROP CONSTRAINT IF EXISTS "Block_serverId_name_key";
ALTER TABLE "Block" ADD CONSTRAINT "Block_ownerId_serverId_name_key" UNIQUE ("ownerId", "serverId", "name");
