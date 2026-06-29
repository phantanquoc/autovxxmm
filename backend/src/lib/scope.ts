import type { Request } from 'express';
import { badRequest } from './errors.js';

/**
 * Returns the ownerId for the authenticated caller.
 * Reads req.user.sub which is set by authRequired middleware.
 * Throws if the user is not authenticated.
 */
export function getOwnerScope(req: Request): number {
  if (req.user === undefined || req.user.sub === undefined) {
    throw new Error('getOwnerScope called without authenticated user');
  }
  return req.user.sub;
}

/**
 * Pulls optional ownerId filter from a parsed list query or raw query string.
 * Validates that the value is a positive integer.
 * Returns undefined when the parameter is absent or invalid (falls back to "no filter").
 */
export function parseAdminOwnerFilter(req: Request): number | undefined {
  // Support both ?ownerId=N (raw) and filter JSON containing ownerId
  let raw: unknown = req.query.ownerId;

  // Also check inside filter JSON parsed by parseListQuery (stored in p.filters.ownerId)
  if (raw === undefined) {
    try {
      const filterParam = req.query.filter;
      if (typeof filterParam === 'string') {
        const parsed = JSON.parse(filterParam) as Record<string, unknown>;
        if (parsed.ownerId !== undefined) raw = parsed.ownerId;
      }
    } catch {
      // ignore parse errors
    }
  }

  if (raw === undefined || raw === null || raw === '') return undefined;

  const n = Number(raw);
  if (!Number.isInteger(n) || n <= 0) return undefined;

  return n;
}
