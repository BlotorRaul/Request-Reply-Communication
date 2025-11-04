# Stage 1: Build Angular application
FROM node:20-alpine AS build
WORKDIR /app

# Copiază package.json și package-lock.json
COPY package*.json ./

# Instalează dependențele
RUN npm ci

# Copiază restul codului sursă
COPY . .

# Construiește aplicația pentru production
RUN npx ng build

# Stage 2: Serve aplicația cu nginx
FROM nginx:alpine

# Copiază fișierele build-uite în directorul nginx
COPY --from=build /app/dist/frontend/browser /usr/share/nginx/html

# Copiază configurația nginx custom (pentru routing SPA)
COPY nginx.conf /etc/nginx/conf.d/default.conf

# Expune portul 80
EXPOSE 80

# Nginx rulează automat în modul foreground
CMD ["nginx", "-g", "daemon off;"]

