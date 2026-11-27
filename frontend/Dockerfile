# ---------- Étape 1 : build ----------
FROM node:20-alpine AS build
WORKDIR /app

COPY package*.json ./
RUN npm ci

COPY . .

# URL de l'API vue depuis le NAVIGATEUR (injectée au build par Vite)
ARG VITE_API_BASE_URL=http://localhost:8080/api/v1
ENV VITE_API_BASE_URL=$VITE_API_BASE_URL
ENV VITE_USE_MOCK=false

RUN npm run build

# ---------- Étape 2 : runtime ----------
FROM nginx:1.27-alpine
# Mise à jour des paquets système (correctifs de sécurité openssl, brotli)
RUN apk upgrade --no-cache
COPY nginx.conf /etc/nginx/conf.d/default.conf
COPY --from=build /app/dist /usr/share/nginx/html
EXPOSE 80
HEALTHCHECK --interval=30s --timeout=5s --retries=3 \
  CMD wget -qO- http://127.0.0.1/ >/dev/null 2>&1 || exit 1