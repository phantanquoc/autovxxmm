-- CreateEnum
CREATE TYPE "Role" AS ENUM ('ADMIN', 'USER');

-- CreateEnum
CREATE TYPE "BotStatus" AS ENUM ('ONLINE', 'OFFLINE', 'CONNECTING');

-- CreateTable
CREATE TABLE "User" (
    "id" SERIAL NOT NULL,
    "username" TEXT NOT NULL,
    "password" TEXT NOT NULL,
    "role" "Role" NOT NULL DEFAULT 'USER',
    "enabled" BOOLEAN NOT NULL DEFAULT true,
    "createdAt" TIMESTAMP(3) NOT NULL DEFAULT CURRENT_TIMESTAMP,
    "updatedAt" TIMESTAMP(3) NOT NULL,

    CONSTRAINT "User_pkey" PRIMARY KEY ("id")
);

-- CreateTable
CREATE TABLE "Server" (
    "id" INTEGER NOT NULL,
    "name" TEXT NOT NULL,
    "ip" TEXT NOT NULL,
    "port" INTEGER NOT NULL,
    "type" INTEGER NOT NULL DEFAULT 0,

    CONSTRAINT "Server_pkey" PRIMARY KEY ("id")
);

-- CreateTable
CREATE TABLE "Bot" (
    "id" SERIAL NOT NULL,
    "serverId" INTEGER NOT NULL,
    "account" TEXT NOT NULL,
    "password" TEXT NOT NULL,
    "charName" TEXT NOT NULL,
    "mapId" INTEGER NOT NULL DEFAULT 0,
    "zoneId" INTEGER NOT NULL DEFAULT 0,
    "posX" INTEGER NOT NULL DEFAULT 0,
    "posY" INTEGER NOT NULL DEFAULT 0,
    "manager" TEXT NOT NULL DEFAULT '',
    "chat" TEXT NOT NULL DEFAULT '',
    "sms" TEXT NOT NULL DEFAULT '',
    "enable" BOOLEAN NOT NULL DEFAULT true,
    "playFee" INTEGER NOT NULL DEFAULT 0,
    "typeLuckyDraw" INTEGER NOT NULL DEFAULT 0,
    "client" INTEGER NOT NULL DEFAULT 0,
    "obsName" TEXT,
    "obsLevel" INTEGER,
    "obsClan" TEXT,
    "obsCoin" INTEGER,
    "obsGold" INTEGER,
    "obsStatus" "BotStatus" NOT NULL DEFAULT 'OFFLINE',
    "obsLastOnline" BIGINT NOT NULL DEFAULT 0,
    "createdAt" TIMESTAMP(3) NOT NULL DEFAULT CURRENT_TIMESTAMP,
    "updatedAt" TIMESTAMP(3) NOT NULL,
    "deletedAt" TIMESTAMP(3),

    CONSTRAINT "Bot_pkey" PRIMARY KEY ("id")
);

-- CreateTable
CREATE TABLE "BotReassignment" (
    "id" BIGSERIAL NOT NULL,
    "botId" INTEGER NOT NULL,
    "fromClient" INTEGER NOT NULL,
    "toClient" INTEGER NOT NULL,
    "createdAt" TIMESTAMP(3) NOT NULL DEFAULT CURRENT_TIMESTAMP,

    CONSTRAINT "BotReassignment_pkey" PRIMARY KEY ("id")
);

-- CreateTable
CREATE TABLE "Order" (
    "id" SERIAL NOT NULL,
    "serverId" INTEGER NOT NULL,
    "botId" INTEGER NOT NULL,
    "client" INTEGER NOT NULL DEFAULT 0,
    "name" TEXT NOT NULL,
    "bot" TEXT NOT NULL,
    "second" INTEGER NOT NULL,
    "type" INTEGER NOT NULL,
    "timeStart" BIGINT NOT NULL,
    "timeStop" BIGINT NOT NULL DEFAULT 0,
    "coinOrder" INTEGER NOT NULL,
    "coinWin" INTEGER NOT NULL DEFAULT 0,
    "coinFee" INTEGER NOT NULL DEFAULT 0,
    "coinReward" INTEGER NOT NULL DEFAULT 0,
    "status" INTEGER NOT NULL DEFAULT 0,
    "winName" TEXT,
    "reason" TEXT,
    "log" TEXT,
    "createdAt" TIMESTAMP(3) NOT NULL DEFAULT CURRENT_TIMESTAMP,
    "updatedAt" TIMESTAMP(3) NOT NULL,
    "deletedAt" TIMESTAMP(3),

    CONSTRAINT "Order_pkey" PRIMARY KEY ("id")
);

-- CreateTable
CREATE TABLE "TradeLog" (
    "id" BIGSERIAL NOT NULL,
    "serverId" INTEGER NOT NULL,
    "name" TEXT NOT NULL,
    "customer" TEXT NOT NULL,
    "before" INTEGER NOT NULL,
    "after" INTEGER NOT NULL,
    "change" INTEGER NOT NULL,
    "description" TEXT NOT NULL,
    "type" INTEGER NOT NULL,
    "timeStart" BIGINT NOT NULL,
    "timeStop" BIGINT NOT NULL,
    "createdAt" TIMESTAMP(3) NOT NULL DEFAULT CURRENT_TIMESTAMP,

    CONSTRAINT "TradeLog_pkey" PRIMARY KEY ("id")
);

-- CreateTable
CREATE TABLE "Block" (
    "id" SERIAL NOT NULL,
    "serverId" INTEGER NOT NULL,
    "name" TEXT NOT NULL,
    "reason" TEXT,
    "createdAt" TIMESTAMP(3) NOT NULL DEFAULT CURRENT_TIMESTAMP,

    CONSTRAINT "Block_pkey" PRIMARY KEY ("id")
);

-- CreateTable
CREATE TABLE "ClientSyncState" (
    "id" SERIAL NOT NULL,
    "userId" INTEGER NOT NULL,
    "client" INTEGER NOT NULL DEFAULT 0,
    "newBotsAt" TIMESTAMP(3) NOT NULL DEFAULT CURRENT_TIMESTAMP,
    "updatedBotsAt" TIMESTAMP(3) NOT NULL DEFAULT CURRENT_TIMESTAMP,
    "deletedBotsAt" TIMESTAMP(3) NOT NULL DEFAULT CURRENT_TIMESTAMP,
    "reassignedOutAt" TIMESTAMP(3) NOT NULL DEFAULT CURRENT_TIMESTAMP,
    "reassignedInAt" TIMESTAMP(3) NOT NULL DEFAULT CURRENT_TIMESTAMP,
    "ordersUpdatedAt" TIMESTAMP(3) NOT NULL DEFAULT CURRENT_TIMESTAMP,
    "ordersDeletedAt" TIMESTAMP(3) NOT NULL DEFAULT CURRENT_TIMESTAMP,
    "lastSeenAt" TIMESTAMP(3) NOT NULL DEFAULT CURRENT_TIMESTAMP,

    CONSTRAINT "ClientSyncState_pkey" PRIMARY KEY ("id")
);

-- CreateIndex
CREATE UNIQUE INDEX "User_username_key" ON "User"("username");

-- CreateIndex
CREATE INDEX "Bot_serverId_deletedAt_idx" ON "Bot"("serverId", "deletedAt");

-- CreateIndex
CREATE INDEX "Bot_client_deletedAt_idx" ON "Bot"("client", "deletedAt");

-- CreateIndex
CREATE INDEX "Bot_updatedAt_idx" ON "Bot"("updatedAt");

-- CreateIndex
CREATE INDEX "BotReassignment_createdAt_idx" ON "BotReassignment"("createdAt");

-- CreateIndex
CREATE INDEX "BotReassignment_botId_idx" ON "BotReassignment"("botId");

-- CreateIndex
CREATE INDEX "Order_botId_status_idx" ON "Order"("botId", "status");

-- CreateIndex
CREATE INDEX "Order_serverId_status_idx" ON "Order"("serverId", "status");

-- CreateIndex
CREATE INDEX "Order_updatedAt_idx" ON "Order"("updatedAt");

-- CreateIndex
CREATE INDEX "TradeLog_serverId_customer_idx" ON "TradeLog"("serverId", "customer");

-- CreateIndex
CREATE INDEX "TradeLog_createdAt_idx" ON "TradeLog"("createdAt");

-- CreateIndex
CREATE UNIQUE INDEX "Block_serverId_name_key" ON "Block"("serverId", "name");

-- CreateIndex
CREATE UNIQUE INDEX "ClientSyncState_userId_client_key" ON "ClientSyncState"("userId", "client");

-- AddForeignKey
ALTER TABLE "Bot" ADD CONSTRAINT "Bot_serverId_fkey" FOREIGN KEY ("serverId") REFERENCES "Server"("id") ON DELETE RESTRICT ON UPDATE CASCADE;

-- AddForeignKey
ALTER TABLE "BotReassignment" ADD CONSTRAINT "BotReassignment_botId_fkey" FOREIGN KEY ("botId") REFERENCES "Bot"("id") ON DELETE CASCADE ON UPDATE CASCADE;

-- AddForeignKey
ALTER TABLE "Order" ADD CONSTRAINT "Order_botId_fkey" FOREIGN KEY ("botId") REFERENCES "Bot"("id") ON DELETE RESTRICT ON UPDATE CASCADE;

-- AddForeignKey
ALTER TABLE "ClientSyncState" ADD CONSTRAINT "ClientSyncState_userId_fkey" FOREIGN KEY ("userId") REFERENCES "User"("id") ON DELETE CASCADE ON UPDATE CASCADE;
