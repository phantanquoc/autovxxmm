import { Router } from 'express';
import { getOwnerScope } from '../../lib/scope.js';

const router = Router();

router.get('/', (req, res) => {
  const user = req.user!;
  res.json({
    id: getOwnerScope(req),
    username: user.username,
    role: user.role,
  });
});

export default router;
