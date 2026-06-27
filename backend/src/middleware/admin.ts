import type { NextFunction, Request, Response } from 'express';
import { forbidden } from '../lib/errors.js';

export function adminRequired(req: Request, _res: Response, next: NextFunction) {
  if (!req.user || req.user.role !== 'ADMIN') return next(forbidden('Admin only'));
  return next();
}
