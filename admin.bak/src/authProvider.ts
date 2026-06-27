// Bearer-JWT auth provider tương thích với /api/login + admin role.
import type { AuthProvider } from 'react-admin';
import { API_URL } from './config';

const TOKEN_KEY = 'vxmm.jwt';

interface JwtClaims {
  sub: number;
  username: string;
  role: 'ADMIN' | 'USER';
  exp: number;
}

function decode(token: string): JwtClaims | null {
  try {
    const [, payload] = token.split('.');
    return JSON.parse(atob(payload));
  } catch {
    return null;
  }
}

export const authProvider: AuthProvider = {
  async login({ username, password }) {
    const res = await fetch(`${API_URL}/api/login`, {
      method: 'POST',
      headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify({ username, password }),
    });
    if (!res.ok) {
      const body = await res.json().catch(() => ({}));
      throw new Error(body.message || 'Sai tài khoản hoặc mật khẩu');
    }
    const { jwt } = await res.json();
    const claims = decode(jwt);
    if (!claims || claims.role !== 'ADMIN') {
      throw new Error('Tài khoản không có quyền admin');
    }
    localStorage.setItem(TOKEN_KEY, jwt);
  },
  async logout() {
    localStorage.removeItem(TOKEN_KEY);
  },
  async checkAuth() {
    const token = localStorage.getItem(TOKEN_KEY);
    if (!token) throw new Error();
    const claims = decode(token);
    if (!claims || claims.exp * 1000 < Date.now()) {
      localStorage.removeItem(TOKEN_KEY);
      throw new Error();
    }
  },
  async checkError(error) {
    if (error?.status === 401 || error?.status === 403) {
      localStorage.removeItem(TOKEN_KEY);
      throw new Error();
    }
  },
  async getPermissions() {
    const token = localStorage.getItem(TOKEN_KEY);
    if (!token) return null;
    return decode(token)?.role ?? null;
  },
  async getIdentity() {
    const token = localStorage.getItem(TOKEN_KEY);
    if (!token) return { id: 0 };
    const c = decode(token);
    return { id: c?.sub ?? 0, fullName: c?.username ?? '' };
  },
};

export function getToken(): string | null {
  return localStorage.getItem(TOKEN_KEY);
}
