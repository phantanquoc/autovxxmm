import 'express-async-errors';
import express from 'express';
import cors from 'cors';
import helmet from 'helmet';
import morgan from 'morgan';
import { env } from './env.js';
import { patchBigIntJson } from './lib/json.js';
import { logger } from './lib/logger.js';
import { errorHandler } from './middleware/error.js';
import { authRequired } from './middleware/auth.js';
import authRoutes from './routes/auth.js';
import resourceRoutes from './routes/resource.js';
import botRoutes from './routes/bots.js';
import orderRoutes from './routes/orders.js';
import logRoutes from './routes/log.js';
import adminUsers from './routes/admin/users.js';
import adminServers from './routes/admin/servers.js';
import adminBots from './routes/admin/bots.js';
import adminOrders from './routes/admin/orders.js';
import adminTradeLogs from './routes/admin/tradeLogs.js';
import adminBlocks from './routes/admin/blocks.js';
import adminStats from './routes/admin/stats.js';
import meProfile from './routes/me/profile.js';
import meBots from './routes/me/bots.js';
import meOrders from './routes/me/orders.js';
import meTradeLogs from './routes/me/tradeLogs.js';
import meBlocks from './routes/me/blocks.js';
import meStats from './routes/me/stats.js';

patchBigIntJson();

const app = express();
app.use(helmet());
app.use(
  cors({
    origin: true,
    credentials: true,
    exposedHeaders: ['Content-Range'],
  })
);
app.use(express.json({ limit: '2mb' }));
if (env.NODE_ENV !== 'test') app.use(morgan('tiny'));

app.use('/api', authRoutes);
app.use('/api/resource', resourceRoutes);
app.use('/api/client/bots', botRoutes);
app.use('/api/client/orders', orderRoutes);
app.use('/api/log', logRoutes);

app.use('/api/admin/users', adminUsers);
app.use('/api/admin/servers', adminServers);
app.use('/api/admin/bots', adminBots);
app.use('/api/admin/orders', adminOrders);
app.use('/api/admin/tradeLogs', adminTradeLogs);
app.use('/api/admin/blocks', adminBlocks);
app.use('/api/admin/stats', adminStats);

// /api/me/* — any authenticated user, forced to own scope
const meRouter = express.Router();
meRouter.use(authRequired);
meRouter.use('/profile', meProfile);
meRouter.use('/bots', meBots);
meRouter.use('/orders', meOrders);
meRouter.use('/tradeLogs', meTradeLogs);
meRouter.use('/blocks', meBlocks);
meRouter.use('/stats', meStats);
app.use('/api/me', meRouter);

app.use(errorHandler);

if (env.NODE_ENV !== 'test') {
  app.listen(env.PORT, () => {
    logger.info(`VXMM backend listening on :${env.PORT}`);
  });
}

export default app;
