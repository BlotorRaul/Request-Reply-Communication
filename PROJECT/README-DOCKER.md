# Docker Setup - Distributed Systems Project

Acest document descrie cum să rulezi întregul proiect folosind Docker și Docker Compose.

## Structura Proiectului

```
PROJECT/
├── docker-compose.yml          # Orchestrarea tuturor serviciilor
├── traefik/
│   ├── traefik.yml            # Configurație statică Traefik
│   └── logs/                  # Log-uri Traefik
├── dynamic/
│   └── path.yml               # Configurație dinamică Traefik (routing)
├── auth-service/
│   ├── Dockerfile
│   └── .dockerignore
├── user-service/
│   ├── Dockerfile
│   └── .dockerignore
├── device-service/
│   ├── Dockerfile
│   └── .dockerignore
└── frontend/
    ├── Dockerfile
    ├── nginx.conf
    └── .dockerignore
```

## Cerințe

- Docker Desktop sau Docker Engine + Docker Compose
- Minim 4GB RAM disponibil
- Porturile 80, 8080, 5672, 15672 disponibile

## Rularea Proiectului

### 1. Build și Start toate serviciile

```bash
docker-compose up --build
```

Pentru a rula în background:

```bash
docker-compose up --build -d
```

### 2. Verificare Status

```bash
docker-compose ps
```

Toate serviciile ar trebui să fie în status `healthy` sau `running`.

### 3. Accesare Aplicație

- **Frontend**: http://localhost
- **Traefik Dashboard**: http://localhost:8080
- **RabbitMQ Management**: http://localhost:15672 (guest/guest)

### 4. API Endpoints (via Traefik)

- **Auth Service**: http://localhost/api/auth/*
- **User Service**: http://localhost/api/users/*
- **Device Service**: http://localhost/api/devices/*
- **User Local (Device Service)**: http://localhost/api/users-local/*

## Servicii

### Backend Services

1. **auth-service** (Port 8083)
   - Autentificare (JWT/Basic)
   - Baza de date: PostgreSQL (authDB)

2. **user-service** (Port 8081)
   - Management utilizatori
   - Baza de date: PostgreSQL (userDB)
   - RabbitMQ Publisher

3. **device-service** (Port 8082)
   - Management dispozitive
   - Baza de date: PostgreSQL (deviceDB)
   - RabbitMQ Consumer

### Infrastructure Services

1. **Traefik** (Port 80, 8080)
   - Reverse Proxy
   - Load Balancer
   - Dashboard: http://localhost:8080

2. **PostgreSQL** (3 instanțe)
   - postgres-auth (authDB)
   - postgres-user (userDB)
   - postgres-device (deviceDB)

3. **RabbitMQ** (Port 5672, 15672)
   - Message Broker pentru comunicare între servicii

### Frontend

1. **frontend** (Port 80, servit prin Traefik)
   - Angular Application
   - Servit prin Nginx

## Comenzi Utile

### Logs

```bash
# Toate serviciile
docker-compose logs -f

# Serviciu specific
docker-compose logs -f auth-service
docker-compose logs -f traefik
```

### Restart Serviciu

```bash
docker-compose restart auth-service
```

### Stop Toate Serviciile

```bash
docker-compose down
```

### Stop și Șterge Volumes (șterge datele din baze de date)

```bash
docker-compose down -v
```

### Rebuild un Serviciu Specific

```bash
docker-compose build auth-service
docker-compose up -d auth-service
```

## Configurație Traefik

### Static Configuration (`traefik/traefik.yml`)

Definește:
- Entry Points (port 80)
- Providers (Docker + File)
- Dashboard settings
- Logging

### Dynamic Configuration (`dynamic/path.yml`)

Definește:
- Routers (routing rules)
- Services (backend services)
- Priority pentru routing (API routes au prioritate mai mare decât frontend)

## Troubleshooting

### Serviciile nu pornesc

1. Verifică log-urile:
   ```bash
   docker-compose logs auth-service
   ```

2. Verifică dacă porturile sunt disponibile:
   ```bash
   # Windows
   netstat -ano | findstr :80
   ```

### 404 pe endpoint-uri API

1. Verifică Traefik Dashboard: http://localhost:8080
2. Verifică routing în `dynamic/path.yml`
3. Verifică log-urile Traefik:
   ```bash
   docker-compose logs traefik
   ```

### Frontend nu se conectează la backend

1. Verifică că frontend-ul folosește `/api/` prefix pentru request-uri
2. Verifică configurația nginx (`frontend/nginx.conf`)
3. Verifică că Traefik rulează și este healthy

### Baza de date nu se conectează

1. Verifică health checks:
   ```bash
   docker-compose ps
   ```
2. Verifică variabilele de mediu în `docker-compose.yml`
3. Verifică log-urile:
   ```bash
   docker-compose logs postgres-auth
   ```

## Credențiale Default

- **PostgreSQL**: postgres / 1234
- **RabbitMQ Management**: guest / guest
- **Admin User**: admin@example.com / admin123
- **Regular User**: user@example.com / user123

## Note Importante

1. **Prima rulare** poate dura mai mult din cauza build-ului și descărcării imaginilor
2. **Health checks** pot dura până la 60 de secunde pentru serviciile Spring Boot
3. **Traefik Dashboard** este activat în mod insecure (doar pentru development!)
4. **Volumes** pentru PostgreSQL păstrează datele între restart-uri

## Scalare Servicii

Pentru a rula multiple instanțe ale unui serviciu:

```bash
docker-compose up --scale auth-service=3
```

Traefik va distribui automat traficul între instanțe (load balancing).

## Cleanup

Pentru a șterge totul și a începe de la zero:

```bash
docker-compose down -v
docker system prune -a
```

**ATENȚIE**: Această comandă va șterge toate containerele, imagini și volume-uri!

