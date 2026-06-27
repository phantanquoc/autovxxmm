import { Card, CardContent, CardHeader, Grid, Typography } from '@mui/material';
import { useEffect, useState } from 'react';
import { Title } from 'react-admin';
import { fetchStats } from './dataProvider';

interface Stats {
  bots: { total: number; online: number };
  orders: { total: number; active: number; last24h: number };
  tradeLogs: { total: number; last24h: number };
}

function Tile({ title, value, sub }: { title: string; value: string | number; sub?: string }) {
  return (
    <Card>
      <CardHeader title={title} titleTypographyProps={{ variant: 'subtitle2' }} />
      <CardContent>
        <Typography variant="h4">{value}</Typography>
        {sub && (
          <Typography variant="body2" color="text.secondary">
            {sub}
          </Typography>
        )}
      </CardContent>
    </Card>
  );
}

export function Dashboard() {
  const [stats, setStats] = useState<Stats | null>(null);
  const [err, setErr] = useState<string | null>(null);
  useEffect(() => {
    fetchStats()
      .then(setStats)
      .catch((e: Error) => setErr(e.message));
  }, []);

  return (
    <>
      <Title title="Dashboard" />
      <div style={{ padding: 16 }}>
        <Typography variant="h5" gutterBottom>
          Tổng quan VXMM
        </Typography>
        {err && <Typography color="error">{err}</Typography>}
        <Grid container spacing={2}>
          <Grid item xs={12} md={4}>
            <Tile
              title="Bot đang treo"
              value={stats ? `${stats.bots.online} / ${stats.bots.total}` : '…'}
              sub="online / tổng"
            />
          </Grid>
          <Grid item xs={12} md={4}>
            <Tile
              title="Đơn hàng"
              value={stats ? stats.orders.active : '…'}
              sub={stats ? `${stats.orders.last24h} đơn 24h qua / ${stats.orders.total} tổng` : ''}
            />
          </Grid>
          <Grid item xs={12} md={4}>
            <Tile
              title="Giao dịch xu (24h)"
              value={stats ? stats.tradeLogs.last24h : '…'}
              sub={stats ? `tổng lịch sử: ${stats.tradeLogs.total}` : ''}
            />
          </Grid>
        </Grid>
      </div>
    </>
  );
}
