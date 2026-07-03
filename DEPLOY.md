# Hướng dẫn deploy tool VXMM lên VPS (Cloudflare Proxy)

Kiến trúc production trên `bota.io.vn`:

```
                Internet (HTTPS 443)
                       │
                Cloudflare Edge
                       │  (proxy HTTP → origin port 8080)
                ┌──────▼──────┐
                │    Caddy    │  (VPS_IP:8080)
                └──┬───────┬──┘
       bota.io.vn │       │ api.bota.io.vn
                ┌─▼─┐   ┌─▼──┐
                │adm│   │api │
                │nx │   │node│
                └───┘   └─┬──┘
                          │
                      ┌───▼────┐
                      │postgres│
                      └────────┘
```

Cloudflare terminate HTTPS ở edge, forward về VPS qua HTTP port `8080`. VPS chỉ cần mở 1 port lạ, không cần 80/443.

---

## 1. Cấu hình DNS trên Cloudflare

Vào dashboard bota.io.vn → **DNS → Records**, tạo 2 record:

| Type | Name | IPv4 address | Proxy status |
|------|------|--------------|--------------|
| A | @ | `IP_VPS` | **Proxied** (đám mây cam) |
| A | api | `IP_VPS` | **Proxied** (đám mây cam) |

Bắt buộc bật **Proxied** — nếu để DNS only (đám mây xám) sẽ trỏ thẳng vào port 8080 và không có HTTPS.

---

## 2. Cấu hình SSL/TLS mode

Vào **SSL/TLS → Overview**, chọn mode:

- **Flexible** — Cloudflare → VPS đi HTTP (dễ setup, khuyến nghị cho lần đầu)

Chọn Flexible xong sang bước tiếp.

---

## 3. Tạo Origin Rule để forward về port 8080

Cloudflare mặc định gọi origin ở port 80. Ta chỉ mở 8080 nên cần rule đổi port.

Vào **Rules → Origin Rules → Create rule**:

- **Rule name**: `Forward to 8080`
- **When incoming requests match**: chọn `All incoming requests`
- **Then**: chọn `Rewrite to` → `Destination Port` → nhập `8080`
- **Deploy**

Xong. Mọi request đến `*.bota.io.vn` sẽ được Cloudflare gọi về `IP_VPS:8080`.

---

## 4. Chuẩn bị VPS

SSH vào VPS (Ubuntu 22.04+):

```bash
# Cài Docker + Compose plugin
curl -fsSL https://get.docker.com | sh
sudo usermod -aG docker $USER
newgrp docker

# Firewall: mở SSH + port 8080
sudo ufw allow 22
sudo ufw allow 8080
sudo ufw enable
```

---

## 5. Clone repo lên VPS

```bash
cd /opt
git clone <repo-url> vxmm
cd vxmm
```

Nếu không có git remote, dùng `scp` upload thư mục từ máy local lên `/opt/vxmm`.

---

## 6. Tạo file `.env`

```bash
cp .env.example .env
nano .env
```

Đổi các dòng `change-me-*`:

- `POSTGRES_PASSWORD`: random 32 ký tự
- `JWT_SECRET`: `openssl rand -hex 32` rồi paste vào
- `ADMIN_PASSWORD`: mật khẩu login admin của bạn

**Không commit `.env` lên git.**

---

## 7. Build & chạy

```bash
docker compose up -d --build
```

Đợi ~30s cho tất cả service khoẻ, kiểm tra:

```bash
docker compose ps
docker compose logs -f caddy
```

Test trực tiếp trên VPS (bypass Cloudflare):
```bash
curl -H "Host: bota.io.vn" http://localhost:8080
curl -H "Host: api.bota.io.vn" http://localhost:8080/api/health
```

Cả hai phải trả về HTML/JSON, không phải 502.

---

## 8. Kiểm tra qua Cloudflare

- Mở `https://bota.io.vn` → trang login admin SPA (có ổ khoá xanh)
- Mở `https://api.bota.io.vn/api/auth/login` → API trả JSON

Nếu lỗi:
- **520/521** → VPS chưa mở port 8080, xem lại `ufw status`
- **502 từ Caddy** → service `api` hoặc `admin` chưa healthy, xem `docker compose logs`
- **Redirect loop** → mode SSL/TLS đang là **Full** hoặc **Full (strict)**, đổi về **Flexible**

---

## 9. Đóng gói client Java cho khách

Trên máy dev (đã build lại `libs/app.jar` bằng workflow trong `tool-vxmm-collect-coins.md`):

1. `run.bat` cho khách — **bỏ flag `--local`** để dùng URL production đã hardcode:
   ```bat
   @echo off
   cd /d "%~dp0"
   java -cp "libs\*" main.Application
   pause
   ```
2. Zip 2 thứ: thư mục `libs/` + file `run.bat` ở trên
3. Gửi cho khách. Khách cần cài **Java 8+**, chạy `run.bat` là được.

Khi tool khởi động sẽ gọi `https://api.bota.io.vn` với tài khoản bạn cấp.

---

## 10. Vận hành

**Update code:**
```bash
cd /opt/vxmm
git pull
docker compose up -d --build
```

**Xem log:**
```bash
docker compose logs -f api        # backend
docker compose logs -f caddy      # reverse proxy
docker compose logs -f postgres
```

**Backup DB:**
```bash
docker compose exec -T postgres pg_dump -U vxmm vxmm > backup-$(date +%F).sql
```

**Restore:**
```bash
docker compose exec -T postgres psql -U vxmm vxmm < backup-2026-07-02.sql
```

**Dừng toàn bộ:**
```bash
docker compose down
```

**Dừng và xoá dữ liệu (cẩn thận):**
```bash
docker compose down -v
```
