// Wrap ra-data-simple-rest để inject Bearer JWT vào mọi request.
import simpleRestProvider from 'ra-data-simple-rest';
import { fetchUtils } from 'react-admin';
import { ADMIN_BASE, API_URL } from './config';
import { getToken } from './authProvider';

const httpClient: typeof fetchUtils.fetchJson = (url, options = {}) => {
  const headers = new Headers(
    options.headers || { Accept: 'application/json' }
  ) as Headers;
  const token = getToken();
  if (token) headers.set('Authorization', `Bearer ${token}`);
  return fetchUtils.fetchJson(url, { ...options, headers });
};

export const dataProvider = simpleRestProvider(ADMIN_BASE, httpClient);

// API thống kê riêng cho Dashboard.
export async function fetchStats() {
  const token = getToken();
  const res = await fetch(`${API_URL}/api/admin/stats`, {
    headers: token ? { Authorization: `Bearer ${token}` } : {},
  });
  if (!res.ok) throw new Error('Cannot load stats');
  return res.json();
}
