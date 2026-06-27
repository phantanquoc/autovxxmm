import type { NextFunction, Request, Response } from 'express';
import { unauthorized, badRequest } from '../lib/errors.js';
import { verifyJwt, type JwtPayload } from '../lib/jwt.js';

declare module 'express-serve-static-core' {
  interface Request {
    user?: JwtPayload;
    client: number;
  }
}

export function authRequired(req: Request, _res: Response, next: NextFunction) {
  const header = req.header('authorization');
  if (!header || !header.startsWith('Bearer ')) {
    return next(unauthorized('Missing bearer token'));
  }
  try {
    req.user = verifyJwt(header.slice(7));
    return next();
  } catch {
    return next(unauthorized('Invalid or expired token'));
  }
}

// Java client gửi `client: <1..10>` header khi chạy split-client. Khi không có header
// (chế độ normal) ta dùng client=0 — đại diện cho "all bots / no partition".
export function readClient(req: Request, _res: Response, next: NextFunction) {
  const raw = req.header('client');
  if (raw === undefined) {
    req.client = 0;
    return next();
  }
  const parsed = Number(raw);
  if (!Number.isInteger(parsed) || parsed < 0 || parsed > 10) {
    return next(badRequest('Invalid client header'));
  }
  req.client = parsed;
  return next();
}
