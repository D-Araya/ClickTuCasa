# ClickTuCasa — Microservicio de Rifas de Casas (Backend)

> Motor de rifas de viviendas construido con Arquitectura Limpia y DDD táctico, expuesto como microservicio REST.
> Proyecto Integrador — Programa Java Avanzado, Desafío Latam / Globant Talento Ready.

![Java](https://img.shields.io/badge/Java-17%20LTS-orange)
![Spring Boot](https://img.shields.io/badge/Spring%20Boot-3.3.4-6DB33F)
![PostgreSQL](https://img.shields.io/badge/Database-PostgreSQL%2016-336791)
![Docker](https://img.shields.io/badge/Container-Docker%20Compose-2496ED)
![OpenAPI](https://img.shields.io/badge/Docs-Swagger%20(solo%20dev)-85EA2D)
![Coverage](https://img.shields.io/badge/JaCoCo-100%25%20domain%20%2B%20application-success)

**Frontend que consume esta API:** https://github.com/D-Araya/clicktucasa-frontend

---

## Stack Tecnológico

* **Backend:** Java 17, Spring Boot 3.3.4, Spring Web, Spring Data JPA, Hibernate, Bean Validation, OpenAPI/Swagger.
* **Frontend:** TypeScript (`strict`), Vite, Tailwind CSS, DOM nativo sin framework.
* **Infraestructura:** Docker Compose, PostgreSQL 16 Alpine.
* **Calidad y Testing:** JUnit 5, Mockito, JaCoCo, TDD y Clean Architecture / DDD.

---

## Repositorios de Referencia

* Core de Dominio / Hito 1: https://github.com/sebavidal10/neonpulse-ticketera
* Backend Spring Boot / Hito 4: https://github.com/sebavidal10/neonpulse-api-springboot
* Frontend Vite + TS / Hito 2: https://github.com/sebavidal10/neonpulse-frontend

---

## Guía de Puesta en Marcha Local

> Antes del paso 1: `cp .env.example .env`. **No es obligatorio** — cada variable tiene un valor por defecto de desarrollo que coincide con `docker-compose.yml`, así que el proyecto arranca sin configurar nada. Copia el archivo solo si quieres usar tus propias credenciales.

### 1. Levantar la Base de Datos Relacional

    cd ClickTuCasa
    docker compose up -d

### 2. Ejecutar Pruebas Automatizadas

    ./mvnw clean test

*(en Windows: `mvnw.cmd clean test`; si aún no generaste el wrapper, `mvn clean test` funciona igual)*

### 3. Iniciar el Microservicio Backend

    ./mvnw spring-boot:run

* API REST: http://localhost:8080/api/v1/raffles
* Swagger UI (perfil `dev`): http://localhost:8080/swagger-ui.html

### 4. Iniciar la Interfaz Web Frontend

    cd ../click-tu-casa-frontend
    npm install
    npm run dev

* App Web: http://localhost:5173

---

## Datos de Prueba

La base arranca vacía. Un script deja tres rifas y una de ellas con boletos vendidos y reservados, para que la interfaz muestre los tres estados y una barra de progreso real:

    ./scripts/seed.sh          # Linux / macOS / Git Bash
    .\scripts\seed.ps1         # Windows PowerShell

O manualmente, una sola rifa:

    curl -X POST http://localhost:8080/api/v1/raffles \
      -H "Content-Type: application/json" \
      -d '{"id":"raf-001","title":"Casa Mediterranea con Vista al Mar","houseAddress":"Camino Costero 1240, Zapallar","houseValue":185000000,"minTicketsToDraw":60,"totalTickets":100,"ticketPrice":15000}'

Verificar:

    curl -s http://localhost:8080/api/v1/raffles | jq

---

## Variables de Entorno

Todas se leen con la sintaxis `${VARIABLE:valorPorDefecto}`: **ninguna credencial está escrita en el código**. Los nombres están documentados en `.env.example`, que sí se versiona; `.env`, que contiene los valores reales, nunca.

| Variable | Descripción | Valor por defecto |
|---|---|---|
| `SPRING_PROFILES_ACTIVE` | Perfil activo. `dev` habilita Swagger; cualquier otro lo bloquea | `dev` |
| `DB_URL` | URL JDBC de PostgreSQL | `jdbc:postgresql://localhost:5432/clicktucasa_db` |
| `DB_USER` | Usuario de la base de datos (lo usan la app y `docker-compose`) | `dev_user` |
| `DB_PASSWORD` | Contraseña de la base de datos (idem) | `SecureDevPassword123` |
| `DB_NAME` | Nombre de la base que crea `docker-compose` | `clicktucasa_db` |
| `DB_PORT` | Puerto publicado por el contenedor de PostgreSQL | `5432` |
| `JPA_DDL_AUTO` | Estrategia de esquema de Hibernate | `update` (`validate` en `prod`) |
| `CORS_ALLOWED_ORIGINS` | Orígenes autorizados a leer la API, separados por coma | `http://localhost:5173,http://localhost:4173,http://localhost:3000` |

> **Sobre el valor por defecto de `DB_PASSWORD`.** Es una credencial de desarrollo local que coincide con la del `docker-compose.yml`, y existe para que un clon recién bajado arranque con un solo comando. Cualquier variable de entorno la sobreescribe sin tocar una línea de código, que es justamente la propiedad que se busca.

---

## API REST

Base: `/api/v1/raffles`

| Verbo | Ruta | Respuesta | Descripción |
|---|---|---|---|
| `GET` | `/` | `200` · `RaffleSummaryResponse[]` | Catálogo completo, sin la grilla de boletos |
| `GET` | `/{raffleId}` | `200` · `RaffleResponse` | Rifa completa, con todos sus boletos |
| `POST` | `/` | `201` · `RaffleResponse` | Crea una rifa y acuña su inventario de boletos |
| `POST` | `/{raffleId}/tickets/{n}/reservations` | `200` · `RaffleResponse` | Reserva temporal de un boleto |
| `POST` | `/{raffleId}/tickets/{n}/purchases` | `200` · `RaffleResponse` | Compra de un boleto (cobra por la pasarela) |
| `POST` | `/{raffleId}/draw` | `200` · `DrawWinnerResponse` | Sortea el ganador entre los boletos vendidos |
| `POST` | `/{raffleId}/expired-reservations/release` | `200` · `ReleaseExpiredReservationsResponse` | Libera reservas vencidas |
| `DELETE` | `/{raffleId}` | `204` | Cancela una rifa no sorteada |

Todos los errores comparten una forma única, producida por `GlobalExceptionHandler`:

```json
{ "message": "Raffle raf-999 not found", "errorCode": "RESOURCE_NOT_FOUND", "timestamp": "2026-09-05T10:12:33" }
```

| HTTP | `errorCode` | Cuándo |
|---|---|---|
| `400` | `INVALID_INPUT` / `VALIDATION_ERROR` | Datos malformados o que violan un invariante de un Value Object |
| `402` | `PAYMENT_FAILED` | La pasarela rechazó el cobro |
| `404` | `RESOURCE_NOT_FOUND` | La rifa o el boleto no existen |
| `409` | `BUSINESS_RULE_VIOLATION` | El boleto no está disponible, o la rifa no admite la operación |
| `500` | `INTERNAL_ERROR` | Mensaje genérico, sin stacktrace: no se filtra información interna |

---

## Arquitectura

```
src/main/java/com/clicktucasa/
├── domain/                        # Java puro — CERO anotaciones de framework
│   ├── entity/                        Raffle, Ticket, RaffleStatus, TicketStatus
│   ├── valueobject/                   HouseAddress, HouseValue, TicketPrice
│   ├── exception/                     8 excepciones de negocio tipadas
│   ├── port/                          PaymentGateway, RandomNumberGenerator
│   └── repository/                    RaffleRepository (interfaz pura)
├── application/                   # Casos de uso — CERO anotaciones de framework
│   └── usecase/                       List, Create, Get, Reserve, Purchase,
│                                      DrawWinner, ReleaseExpiredReservations, Cancel
└── infrastructure/                # Único lugar donde vive Spring
    ├── web/controller/                RaffleController
    ├── web/dto/                       Requests y Responses con @Schema
    ├── web/exception/                 GlobalExceptionHandler
    ├── persistence/                   RaffleEntity, TicketEntity, RaffleRepositoryAdapter
    ├── payment/ · random/             Adaptadores de los puertos del dominio
    └── config/                        OpenApiConfig, CorsConfig, UseCaseConfig
```

Tres decisiones que sostienen el diseño:

1. **El dominio no conoce a Spring.** Ni `@Entity`, ni `@Component`, ni un `import org.springframework`. Verificable con un `grep`.
2. **Los casos de uso tampoco.** En vez de anotarlos con `@Service`, se declaran como `@Bean` en `UseCaseConfig`; así siguen siendo instanciables con `new` en un test de Mockito, sin levantar un contexto de Spring.
3. **El adaptador es el único traductor.** `RaffleRepositoryAdapter` es la única clase que conoce a la vez el modelo de dominio y el de persistencia.

### Perfiles y superficie expuesta

| | `dev` | `prod` (o sin perfil) |
|---|---|---|
| API REST | Disponible | Disponible |
| Swagger UI / `api-docs` | **Habilitado** | **Bloqueado** |
| `show-sql` | Sí | No |
| `ddl-auto` | `update` | `validate` |

`application.yml` deja Swagger en `false`; solo `application-dev.yml` lo enciende. `application-prod.yml` **sí se versiona** a propósito: no contiene ningún secreto, únicamente los interruptores de endurecimiento.

---

## Testing

    ./mvnw clean test

* JUnit 5 + Mockito, sin base de datos ni contexto de Spring en los tests de dominio y aplicación.
* `src/test/java/**` **replica exactamente** la estructura de paquetes de `src/main/java/**`.
* Todos los nombres de clase, de método y los `@DisplayName` están **en inglés**.
* JaCoCo exige **100 % de líneas y de ramas** sobre `domain` y `application`. La capa `infrastructure` está excluida de la regla a propósito: son adaptadores de framework, y se verifican por Swagger y por el frontend real.

El informe queda en `target/site/jacoco/index.html`.

---

## Historial del proyecto

Los README de las etapas anteriores se conservan sin cambios en `docs/`:

* `docs/README_HITO4.md` — Unidad 4: microservicio REST, JPA/PostgreSQL, Swagger por perfiles.
* `docs/README_HITO3.md` — Unidad 3: refactor a Clean Architecture + DDD.
* `docs/README_HITO1.md` — Unidad 1: dominio construido con TDD.
