# Post-archive follow-up notes

Two MINOR findings from the verify pass — non-blocking, but worth addressing later:

1. Dead hook `useOrderDelete` in `admin/src/features/orders/useOrders.ts` (lines 50-56) points at a nonexistent route; remove it in a future cleanup.

2. `backend/src/routes/admin/orders.ts:46` — the `GET /:id` handler should reject soft-deleted orders for consistency with how `admin/bots.ts:48` and the general admin pattern handle soft-deletes.
