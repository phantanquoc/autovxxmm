import type { ErrorRequestHandler } from 'express';
import { ZodError } from 'zod';
import { HttpError } from '../lib/errors.js';
import { logger } from '../lib/logger.js';

export const errorHandler: ErrorRequestHandler = (err, _req, res, _next) => {
  if (err instanceof HttpError) {
    res.status(err.status).json({ message: err.message, ...(err.data ? { data: err.data } : {}) });
    return;
  }
  if (err instanceof ZodError) {
    res.status(400).json({ message: 'Validation failed', errors: err.flatten() });
    return;
  }
  logger.error({ err }, 'unhandled error');
  res.status(500).json({ message: 'Internal server error' });
};
