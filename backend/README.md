## Phase 1 — Backend scaffold

Đã có:
- `backend/` — Node.js + Express + Prisma + PostgreSQL
- `docker-compose.yml` — postgres + api
- Prisma schema khớp 1-1 với API contract của tool Java
- 12 endpoint client cần đã được implement
- Seed 1 admin (`admin` / `admin@123`) + 15 game server

## Chạy local lần đầu

### Cách 1: chạy native (nhanh, dev quen)

```bash
# 1. Postgres bằng docker
docker run -d --name vxmm-pg -p 5432:5432 \
  -e POSTGRES_USER=vxmm -e POSTGRES_PASSWORD=vxmm -e POSTGRES_DB=vxmm \
  postgres:16-alpine

# 2. Backend
cd backend
cp .env.example .env
npm install
npx prisma migrate dev --name init   # tạo schema + apply
npm run seed                          # seed admin + 15 server
npm run dev                           # listen 0.0.0.0:8009
```

### Cách 2: full docker-compose

```bash
docker compose up -d --build
docker compose exec api npx prisma migrate deploy
docker compose exec api npm run seed
```

## Kiểm tra

```bash
# health
curl http://localhost:8009/api/health
# login → lấy jwt
curl -X POST http://localhost:8009/api/login \
  -H 'Content-Type: application/json' \
  -d '{"username":"admin","password":"admin@123"}'
# list server (thay $TOKEN)
curl http://localhost:8009/api/resource/server \
  -H "Authorization: Bearer $TOKEN"
```

## Trỏ tool Java về host mới

Hiện tại `constants/API.java:7` hardcode `https://api.nsovxmm.net`. Cờ `--local` của tool
đã sẵn `http://localhost:8009` — chạy `run.bat --local` là tool kết nối ngay backend mới
mà không cần đụng jar (Phase 3 sẽ thay default URL nếu cần).

## Kế tiếp

- Phase 2 — test contract bằng curl + chạy thử client real.
- Phase 3 — patch client URL.
- Phase 4 — React Admin frontend.
- Phase 5 — Caddy + HTTPS cho VPS.
