# Distributed Systems - Microservices Application

## Descriere Proiect

Acest proiect implementează o arhitectură de microservicii distribuite folosind Spring Boot pentru backend și Angular pentru frontend. Aplicația folosește Traefik ca reverse proxy și load balancer, PostgreSQL pentru persistarea datelor, și RabbitMQ pentru comunicarea asincronă între servicii.

## Arhitectura Aplicației

```
┌─────────────────────────────────────────────────────────────────┐
│                         Frontend (Angular)                       │
│                    http://localhost                              │
│                         (Nginx)                                  │
└────────────────────────────┬────────────────────────────────────┘
                             │
                             │ HTTP Requests
                             │
┌────────────────────────────▼────────────────────────────────────┐
│                      Traefik (v3.0)                             │
│              Reverse Proxy & Load Balancer                       │
│                    Port 80 (HTTP)                                │
│                    Port 8080 (Dashboard)                        │
└────┬──────────────┬──────────────┬──────────────┬──────────────┘
     │              │              │              │
     │              │              │              │
┌────▼────┐  ┌─────▼─────┐  ┌────▼─────┐  ┌────▼─────┐
│  Auth   │  │   User    │  │  Device   │  │ RabbitMQ │
│ Service │  │  Service  │  │  Service  │  │          │
│  :8083  │  │   :8081   │  │   :8082   │  │  :5672   │
└────┬────┘  └─────┬─────┘  └─────┬─────┘  └──────────┘
     │              │              │
     │              │              │
┌────▼────┐  ┌─────▼─────┐  ┌────▼─────┐
│Postgres │  │ Postgres  │  │ Postgres │
│ AuthDB  │  │  UserDB   │  │ DeviceDB │
└─────────┘  └───────────┘  └──────────┘
```

## Stack Tehnologic

### Backend
- **Java 21** - Languajul de programare
- **Spring Boot 3.x** - Framework pentru microservicii
- **Spring Security** - Autentificare și autorizare (JWT/Basic Auth)
- **Spring Data JPA** - Persistența datelor
- **Spring AMQP** - Integrare RabbitMQ
- **PostgreSQL 15** - Baza de date relatională
- **RabbitMQ 3** - Message broker pentru comunicare asincronă

### Frontend
- **Angular 20** - Framework pentru aplicații web
- **TypeScript** - Languajul de programare
- **RxJS** - Programare reactivă
- **Angular Signals** - State management

### Infrastructure
- **Docker** - Containerizare
- **Docker Compose** - Orchestrare servicii
- **Traefik v3.0** - Reverse proxy și load balancer
- **Nginx** - Web server pentru frontend

##  Structura Proiectului

```
PROJECT/
├── docker-compose.yml              # Orchestrarea tuturor serviciilor
├── README.md                       # Acest fișier
├── README-DOCKER.md                # Documentație Docker detaliată
│
├── traefik/
│   ├── traefik.yml                 # Configurație statică Traefik
│   └── logs/                       # Log-uri Traefik
│
├── dynamic/
│   └── path.yml                    # Configurație dinamică Traefik (routing)
│
├── auth-service/                    # Microserviciu de autentificare
│   ├── Dockerfile
│   ├── .dockerignore
│   ├── pom.xml
│   └── src/
│       └── main/
│           ├── java/
│           │   └── com/example/auth_service/
│           │       ├── config/          # Configurații (Security, DataInitializer)
│           │       ├── controllers/     # REST Controllers
│           │       ├── dtos/            # Data Transfer Objects
│           │       ├── entities/        # Entități JPA
│           │       ├── security/        # JWT, Filters, UserDetails
│           │       └── services/       # Business Logic
│           └── resources/
│               └── application.properties
│
├── user-service/                    # Microserviciu de management utilizatori
│   ├── Dockerfile
│   ├── .dockerignore
│   ├── pom.xml
│   └── src/
│       └── main/
│           ├── java/
│           │   └── com/example/user_service/
│           │       ├── config/          # Configurații Security
│           │       ├── controllers/    # REST Controllers
│           │       ├── dtos/           # DTOs
│           │       ├── entities/       # Entități JPA
│           │       ├── security/      # Remote Auth Filters
│           │       └── services/      # Business Logic + RabbitMQ Publisher
│           └── resources/
│               └── application.properties
│
├── device-service/                  # Microserviciu de management dispozitive
│   ├── Dockerfile
│   ├── .dockerignore
│   ├── pom.xml
│   └── src/
│       └── main/
│           ├── java/
│           │   └── com/example/device_service/
│           │       ├── config/          # Configurații Security
│           │       ├── controllers/    # REST Controllers
│           │       ├── dtos/           # DTOs
│           │       ├── entities/       # Entități JPA (Device, UserLocal)
│           │       ├── security/      # Remote Auth Filters
│           │       ├── services/       # Business Logic + RabbitMQ Consumer
│           │       └── events/        # Event Listeners
│           └── resources/
│               └── application.properties
│
└── frontend/                        # Aplicație Angular
    ├── Dockerfile
    ├── .dockerignore
    ├── nginx.conf                   # Configurație Nginx pentru SPA
    ├── package.json
    ├── angular.json
    └── src/
        └── app/
            ├── login.component.ts    # Componentă login
            ├── home.component.ts    # Componentă principală (devices)
            ├── auth.service.ts      # Serviciu autentificare
            ├── device.service.ts    # Serviciu device API
            ├── user.service.ts      # Serviciu user API
            └── auth.interceptor.ts  # HTTP Interceptor pentru JWT
```

##  Explicații Tehnice

### 1. Autentificare și Autorizare

#### Auth Service (Port 8083)
- **Rol**: Gestionare autentificare și autorizare centralizată
- **Moduri**: JWT sau Basic Authentication (configurabil prin `AUTH_MODE`)
- **Funcționalități**:
  - Login (`POST /api/auth/users/login`)
  - Validare token (`POST /api/auth/users/validate-jwt`)
  - Creare/utilizatori (`POST /api/auth/users` - ADMIN only)
  - Listare utilizatori (`GET /api/auth/users` - ADMIN only)
  - Actualizare/ștergere utilizatori (ADMIN only)
- **Securitate**:
  - `DataInitializer` creează utilizatori default la startup:
    - `admin@example.com` / `admin123` (ADMIN)
    - `user@example.com` / `user123` (USER)
  - Password encoding cu BCrypt
  - CORS configurat pentru Docker

#### User Service & Device Service
- **Autentificare Remote**: Validează token-urile JWT prin `auth-service`
- **Filtre de securitate**:
  - `RemoteJwtAuthFilter` - validează JWT prin HTTP call către `auth-service`
  - `RemoteBasicAuthFilter` - validează Basic Auth prin HTTP call către `auth-service`
- **RBAC (Role-Based Access Control)**:
  - **ADMIN**: Acces complet la toate endpoint-urile
  - **USER**: Acces doar la GET requests

### 2. Comunicare între Microservicii

#### RabbitMQ
- **Exchange**: `user.exchange`
- **Routing Key**: `user.events`
- **Queue**: `device.user.queue`
- **Flux**:
  1. `user-service` publică evenimente când utilizatorii sunt creați/modificați
  2. `device-service` consumă evenimente și sincronizează cache-ul `UserLocal`
  3. `device-service` folosește `UserLocal` pentru a afișa numele utilizatorilor în loc de ID-uri

#### HTTP Inter-Service Communication
- Serviciile comunică prin Traefik folosind numele containerelor:
  - `http://auth-service:8083`
  - `http://user-service:8081`
  - `http://device-service:8082`

### 3. Docker Configuration

#### Multi-Stage Builds
- **Spring Boot Services**: 
  - Stage 1: Build cu Maven (`maven:3.9-eclipse-temurin-21`)
  - Stage 2: Runtime cu JRE (`eclipse-temurin:21-jre-alpine`)
- **Frontend**:
  - Stage 1: Build Angular cu Node.js (`node:20-alpine`)
  - Stage 2: Serve cu Nginx (`nginx:alpine`)

#### Network Architecture
- **Network**: `proxy-network` (bridge)
- Toate serviciile comunică prin acest network
- Traefik este entry point-ul pentru toate request-urile externe

#### Health Checks
- **PostgreSQL**: `pg_isready`
- **RabbitMQ**: `rabbitmq-diagnostics ping`
- **Spring Boot**: `nc` (netcat) sau `wget`
- **Frontend**: `nc` pe port 80

### 4. Traefik Configuration

#### Static Configuration (`traefik/traefik.yml`)
- **Entry Points**: Port 80 pentru HTTP
- **Providers**: Docker (auto-discovery) + File (dynamic config)
- **Dashboard**: Port 8080 (insecure pentru development)

#### Dynamic Configuration (`dynamic/path.yml`)
- **Routers**: Definește routing rules bazate pe `Host` și `PathPrefix`
- **Priority**: API routes (10) au prioritate mai mare decât frontend (1)
- **Services**: Definește backend services și load balancing
- **No Strip Prefix**: Serviciile primesc path-ul complet (inclusiv `/api`)

#### Routing Rules
```
/api/auth/*      → auth-service:8083
/api/users/*     → user-service:8081
/api/devices/*   → device-service:8082
/api/users-local/* → device-service:8082
/*               → frontend:80 (SPA fallback)
```

### 5. Frontend Architecture

#### Nginx Configuration
- **Proxy API Requests**: `/api/*` → `traefik:80`
- **SPA Fallback**: Toate request-urile ne-match → `index.html`
- **Authorization Header**: Transmis explicit către Traefik

#### Angular Services
- **AuthService**: Gestionează login, logout, token storage
- **AuthInterceptor**: Adaugă automat `Authorization: Bearer <token>` la request-uri
- **DeviceService**: API calls pentru devices
- **UserService**: API calls pentru users

#### State Management
- **Signals**: Folosit pentru reactive state (`userNameMap`, `isAdmin`, etc.)
- **Local Storage**: Persistență token și user data

##  Pași de Instalare și Rulare

### Cerințe Prealabile

1. **Docker Desktop** sau **Docker Engine + Docker Compose**
   - Versiune minimă: Docker 20.10+, Compose 2.0+
   - Verificare: `docker --version` și `docker-compose --version`

2. **Resurse Sistem**
   - Minim 4GB RAM disponibil
   - Minim 10GB spațiu liber pe disk
   - Porturile disponibile: 80, 8080, 5672, 15672

### Instalare și Rulare

#### Pasul 1: Clonează sau Descarcă Proiectul
```bash
# Dacă ai repository-ul pe Git
git clone <repository-url>
cd PROJECT

# Sau deschide direct folderul PROJECT în terminal
```

#### Pasul 2: Verifică Structura Proiectului
Asigură-te că ai următoarele fișiere/directoare:
- `docker-compose.yml`
- `traefik/traefik.yml`
- `dynamic/path.yml`
- `auth-service/Dockerfile`
- `user-service/Dockerfile`
- `device-service/Dockerfile`
- `frontend/Dockerfile`
- `frontend/nginx.conf`

#### Pasul 3: Build și Start Serviciile
```bash
# Build toate imaginile și start toate serviciile
docker-compose up --build

# SAU pentru a rula în background:
docker-compose up --build -d
```

**Notă**: Prima rulare poate dura 5-10 minute pentru:
- Descărcarea imaginilor Docker (postgres, rabbitmq, traefik, etc.)
- Build-ul serviciilor Spring Boot (Maven compile)
- Build-ul frontend-ului Angular (npm install + ng build)

#### Pasul 4: Verifică Status Serviciilor
```bash
# Verifică statusul tuturor serviciilor
docker-compose ps

# Toate serviciile ar trebui să fie în status:
# - "healthy" (pentru serviciile cu health checks)
# - "running" (pentru serviciile fără health checks)
```

#### Pasul 5: Accesează Aplicația
- **Frontend**: http://localhost
- **Traefik Dashboard**: http://localhost:8080
- **RabbitMQ Management**: http://localhost:15672 (guest/guest)

#### Pasul 6: Login
- **Admin**: `admin@example.com` / `admin123`
- **User**: `user@example.com` / `user123`

##  Endpoints API

### Auth Service (`/api/auth`)

| Method | Endpoint | Descriere | Rol Necesar |
|--------|----------|-----------|-------------|
| POST | `/api/auth/users/login` | Autentificare utilizator | Public |
| POST | `/api/auth/users/validate-jwt` | Validare JWT token | Public |
| GET | `/api/auth/users/mode` | Obține modul de autentificare | Public |
| GET | `/api/auth/users` | Listă toți utilizatorii | ADMIN |
| POST | `/api/auth/users` | Creează utilizator nou | ADMIN |
| GET | `/api/auth/users/{id}` | Obține utilizator după ID | ADMIN |
| PUT | `/api/auth/users/{id}` | Actualizează utilizator | ADMIN |
| DELETE | `/api/auth/users/{id}` | Șterge utilizator | ADMIN |

### User Service (`/api/users`)

| Method | Endpoint | Descriere | Rol Necesar |
|--------|----------|-----------|-------------|
| GET | `/api/users` | Listă toți utilizatorii | ADMIN |
| GET | `/api/users/{id}` | Obține utilizator după ID | ADMIN |
| POST | `/api/users` | Creează utilizator nou | ADMIN |
| PUT | `/api/users/{id}` | Actualizează utilizator | ADMIN |
| DELETE | `/api/users/{id}` | Șterge utilizator | ADMIN |
| GET | `/api/users/email/{email}` | Obține utilizator după email | ADMIN |

### Device Service (`/api/devices`)

| Method | Endpoint | Descriere | Rol Necesar |
|--------|----------|-----------|-------------|
| GET | `/api/devices` | Listă toate dispozitivele | ADMIN |
| GET | `/api/devices/user/{userId}` | Listă dispozitive pentru user | USER/ADMIN |
| GET | `/api/devices/{id}` | Obține device după ID | ADMIN |
| POST | `/api/devices` | Creează device nou | ADMIN |
| PUT | `/api/devices/{id}` | Actualizează device | ADMIN |
| DELETE | `/api/devices/{id}` | Șterge device | ADMIN |
| PUT | `/api/devices/{id}/assign/{userId}` | Atribuie device la user | ADMIN |

### User Local (`/api/users-local`)

| Method | Endpoint | Descriere | Rol Necesar |
|--------|----------|-----------|-------------|
| GET | `/api/users-local/{id}` | Obține UserLocal (cache) | ADMIN |

##  Securitate

### JWT Authentication Flow

```
1. Client → POST /api/auth/users/login (email, password)
2. Auth Service → Validează credențiale
3. Auth Service → Generează JWT token (conține email, role)
4. Client → Stochează token în LocalStorage
5. Client → Trimite request cu header: Authorization: Bearer <token>
6. Nginx → Proxyează request către Traefik (cu Authorization header)
7. Traefik → Proxyează către backend service
8. Backend Service → Validează JWT prin RemoteJwtAuthFilter
   - Extrage token din header
   - Face HTTP call către auth-service/validate-jwt
   - Setează SecurityContext cu rolul utilizatorului
9. Backend Service → Verifică autorizarea bazată pe rol
10. Backend Service → Returnează răspuns
```

### RBAC (Role-Based Access Control)

- **ADMIN**: Acces complet la toate endpoint-urile (GET, POST, PUT, DELETE)
- **USER**: Acces doar la GET requests în `user-service` și `device-service`

##  Docker Architecture

### Images Built

1. **project-auth-service**: Spring Boot JAR pentru auth-service
2. **project-user-service**: Spring Boot JAR pentru user-service
3. **project-device-service**: Spring Boot JAR pentru device-service
4. **project-frontend**: Angular build servit prin Nginx

### Volumes

- `postgres_auth_data`: Persistență pentru authDB
- `postgres_user_data`: Persistență pentru userDB
- `postgres_device_data`: Persistență pentru deviceDB
- `rabbitmq_data`: Persistență pentru RabbitMQ

### Networks

- `proxy-network`: Bridge network pentru toate serviciile

## Comenzi Utile

### Management Servicii

```bash
# Start toate serviciile
docker-compose up -d

# Stop toate serviciile
docker-compose down

# Stop și șterge volumes (ȘTERGE DATELE!)
docker-compose down -v

# Restart un serviciu specific
docker-compose restart auth-service

# Rebuild un serviciu specific
docker-compose build auth-service
docker-compose up -d auth-service

# Vezi log-uri pentru toate serviciile
docker-compose logs -f

# Vezi log-uri pentru un serviciu specific
docker-compose logs -f auth-service
docker-compose logs -f device-service
docker-compose logs -f traefik
```

### Verificare Status

```bash
# Status toate serviciile
docker-compose ps

# Status un serviciu specific
docker-compose ps auth-service

# Verifică health checks
docker-compose ps | grep -E "healthy|unhealthy"
```

### Debugging

```bash
# Intră în container
docker exec -it auth-service sh
docker exec -it device-service sh
docker exec -it traefik sh

# Testează conectivitate între containere
docker exec traefik wget -O- http://auth-service:8083/api/auth/users/mode
docker exec device-service wget -O- http://auth-service:8083/api/auth/users/mode

# Verifică network
docker network inspect project_proxy-network

# Verifică volume-uri
docker volume ls | grep project
```

### Cleanup

```bash
# Șterge containerele oprite
docker-compose rm

# Șterge imagini nefolosite
docker image prune

# Șterge tot (CONTAINERE, IMAGINI, VOLUME-URI, NETWORK-URI)
docker system prune -a --volumes
```

##  Troubleshooting

### Problema: Serviciile nu pornesc

**Soluție**:
1. Verifică log-urile: `docker-compose logs <service-name>`
2. Verifică dacă porturile sunt disponibile:
   ```bash
   # Windows
   netstat -ano | findstr :80
   netstat -ano | findstr :8080
   ```
3. Verifică resursele sistemului (RAM, disk space)
4. Verifică dacă Docker Desktop rulează

### Problema: 502 Bad Gateway

**Cauze posibile**:
1. Backend service nu rulează sau nu este healthy
2. Traefik nu poate conecta la backend service
3. Header-ul Authorization nu este transmis corect

**Soluție**:
1. Verifică status serviciilor: `docker-compose ps`
2. Verifică log-urile Traefik: `docker-compose logs traefik`
3. Verifică log-urile backend: `docker-compose logs device-service`
4. Verifică că frontend trimite token-ul (check browser DevTools Network tab)

### Problema: 401 Unauthorized

**Cauze posibile**:
1. Token JWT expirat sau invalid
2. Token nu este trimis în request
3. Backend service nu poate valida token-ul prin auth-service

**Soluție**:
1. Loghează-te din nou pentru a obține un token nou
2. Verifică în browser DevTools că header-ul `Authorization` este trimis
3. Verifică log-urile backend service pentru erori de validare

### Problema: RabbitMQ Connection Refused

**Soluție**:
1. Verifică că RabbitMQ este healthy: `docker-compose ps rabbitmq`
2. Restart serviciile care depind de RabbitMQ:
   ```bash
   docker-compose restart user-service device-service
   ```
3. Verifică log-urile: `docker-compose logs rabbitmq`

### Problema: Frontend nu se conectează la backend

**Soluție**:
1. Verifică că frontend folosește prefix-ul `/api/` pentru request-uri
2. Verifică configurația nginx: `docker exec frontend cat /etc/nginx/conf.d/default.conf`
3. Verifică că Traefik rulează: `docker-compose ps traefik`
4. Verifică routing în Traefik Dashboard: http://localhost:8080

### Problema: Datele se pierd la restart

**Cauză**: Volume-urile nu sunt configurate corect sau au fost șterse.

**Soluție**:
- Nu folosi `docker-compose down -v` dacă vrei să păstrezi datele
- Verifică volume-urile: `docker volume ls | grep project`

##  Resurse Suplimentare

- [Traefik Documentation](https://doc.traefik.io/traefik/)
- [Spring Boot Documentation](https://spring.io/projects/spring-boot)
- [Angular Documentation](https://angular.io/docs)
- [Docker Compose Documentation](https://docs.docker.com/compose/)
- [RabbitMQ Documentation](https://www.rabbitmq.com/documentation.html)

##  Credențiale Default

| Serviciu | Username | Password |
|----------|----------|----------|
| PostgreSQL | postgres | 1234 |
| RabbitMQ Management | guest | guest |
| Auth Service - Admin | admin@example.com | admin123 |
| Auth Service - User | user@example.com | user123 |

##  Note Importante

1. **Securitate**: Configurația actuală este pentru **development**. Pentru production:
   - Dezactivează Traefik Dashboard sau adaugă autentificare
   - Folosește HTTPS (TLS)
   - Configurează firewall-uri
   - Schimbă credențialele default

2. **Performance**: 
   - Prima rulare este mai lentă din cauza build-ului
   - Health checks pot dura până la 90 de secunde pentru serviciile Spring Boot
   - RabbitMQ necesită cel puțin 30 de secunde pentru a porni complet

3. **Persistență Date**:
   - Volume-urile Docker păstrează datele între restart-uri
   - Pentru reset complet, folosește `docker-compose down -v`

4. **Scalare**:
   - Poți scala serviciile cu: `docker-compose up --scale auth-service=3`
   - Traefik va distribui automat traficul (load balancing)

##  Licență

Acest proiect este realizat în scop educațional pentru cursul de Distributed Systems.

---

**Realizat de**:Blotor Raul 

**Data**: 2025  
**Universitate**: Faculty of Automation and Computer Science

