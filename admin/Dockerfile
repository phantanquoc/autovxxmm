# syntax=docker/dockerfile:1.6
FROM node:20-alpine AS build
WORKDIR /app
COPY package.json package-lock.json* ./
RUN npm install --no-audit --no-fund
COPY tsconfig.json tsconfig.app.json tsconfig.node.json vite.config.ts ./
COPY postcss.config.js tailwind.config.ts components.json index.html ./
COPY .env.production ./
COPY src ./src
# Cho phép override API base lúc build. Mặc định = URL production (giữ nguyên hành vi prod).
# Local truyền --build-arg VITE_API_URL= (rỗng) để admin gọi API theo path tương đối /api.
ARG VITE_API_URL=https://api.bota.io.vn
ENV VITE_API_URL=$VITE_API_URL
RUN npm run build

FROM nginx:1.27-alpine AS runtime
RUN rm -f /etc/nginx/conf.d/default.conf
COPY --from=build /app/dist /usr/share/nginx/html
COPY nginx.conf /etc/nginx/conf.d/default.conf
EXPOSE 80
