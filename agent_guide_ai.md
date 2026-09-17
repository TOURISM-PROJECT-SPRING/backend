# AI Agent Guide

This is the canonical project guide for AI coding agents (Claude Code, OpenAI Codex, opencode,
GitHub Copilot, Google Antigravity, etc.). Tool-specific config files (`CLAUDE.md`, `AGENTS.md`,
`.github/copilot-instructions.md`) all point back here — this file is the single source of truth.
Update this file when conventions change; keep the pointer files short.

## Project overview

Spring Boot REST API.

- **Language / runtime:** Java 21
- **Framework:** Spring Boot 4.0.8-SNAPSHOT (spring-boot-starter-parent)
- **Persistence:** Spring Data JPA + PostgreSQL (`postgresql` driver, `PostgreSQLDialect`,
  `ddl-auto=update` in `application.properties`)
- **Cache:** spring-boot-starter-data-redis + spring-boot-starter-cache (Redis)
- **Validation:** spring-boot-starter-validation (Jakarta Bean Validation)
- **Security:** spring-boot-starter-security (`SecurityConfig` in `config/`)
- **File/image uploads:** Cloudinary SDK (`cloudinary-http44`)
- **PDF rendering:** OpenPDF
- **Realtime:** spring-boot-starter-websocket (STOMP simple broker, endpoint `/ws-tourism`)
- **Payments:** NBC Bakong KHQR via route `api/v1/bakong` (see Conventions)
- **API docs:** springdoc-openapi-starter-webmvc-ui (Swagger UI)
- **Config:** DB credentials come from environment variables (`DB_HOST`, `DB_PORT`, `DB_NAME`,
  `DB_USER`, `DB_PASSWORD`) via `springboot4-dotenv` (a `.env` file)
- **Boilerplate:** Lombok
- **Build tool:** Maven (use the `./mvnw` wrapper, not a system-wide `mvn`)
- **Base package:** `com.example.spring_boot_project_api`

## Build, run, test

```bash
./mvnw clean compile        # compile
./mvnw spring-boot:run       # run the app locally
./mvnw test                  # run tests
./mvnw clean package         # build the jar
```

Swagger UI is available at `/swagger-ui.html` once the app is running.

Note: `SpringBootProjectApiApplicationTests.contextLoads` bootstraps the full Spring context and
needs a live PostgreSQL instance reachable with the credentials in your `.env`. The Mockito unit
tests in `src/test/java/.../service/` run without a database.

## Environment variables

Read from `.env` (see `.env.example`) via `springboot4-dotenv` and injected with `${VAR:default}` in
`application.properties`:

| Variable                  | Used for                                   | Default        |
|---------------------------|--------------------------------------------|----------------|
| `DB_HOST`, `DB_PORT`      | PostgreSQL host / port                     | localhost/5432 |
| `DB_NAME`                 | Database name                              | —              |
| `DB_USER`, `DB_PASSWORD`  | DB credentials                             | —              |
| `REDIS_HOST`              | Redis host (spring-boot-data-redis)        | localhost      |
| `CLOUDINARY_CLOUD_NAME`   | Cloudinary account (image uploads)         | —              |
| `CLOUDINARY_API_KEY`      | Cloudinary key                             | —              |
| `CLOUDINARY_API_SECRET`   | Cloudinary secret                          | —              |
| `JWT_SECRET`              | JWT signing key (HS256, ≥32 bytes)         | dev-only key   |
| `JWT_EXPIRATION_MS`       | Access-token lifetime                      | 86400000 (24h) |
| `RESET_TOKEN_EXPIRATION_MINUTES` | Password-reset token lifetime      | 30             |
| `SERVER_PORT`             | HTTP port                                  | 8080           |

## API surface

Base package `api`; JSON everywhere unless noted. Standard per-resource CRUD controllers follow:
`GET /, /{id}, /search`, `POST`, `PUT /{id}`, `DELETE /{id}`.

- `api/auth` — authentication & authorization (see **Authentication & authorization** below):
  `POST /register`, `POST /login`, `POST /forgot-password`, `POST /reset-password`,
  `POST /change-password`, `POST /logout`
- `api/management` — **ADMIN-only** read endpoints (users, owners, roles, reviews, promotions,
  notifications, payments, tour-packages, tour-guides)

- `api/locations` — Location CRUD (+ `/province`, `/district`)
- `api/carts` — cart + nested items: `/{cartId}/items`, `PUT /{cartId}/items/{itemId}`
- `api/foods`, `api/food-categories` — food + category (foods create/update consume `multipart/form-data`)
- `api/food-orders` — orders (+ `/user/{userId}`, `/restaurant/{restaurantId}`, `/status/{status}`, `/from-cart`, `PUT /{id}/status`, `POST /{id}/cancel`)
- `api/hotels`, `api/hotel-rooms`, `api/room-types`, `api/rooms` — hotel domain (+ `/location/{districtId}`, `/owner/{ownerId}`, `/price-range`, `/min-capacity`, `/filter`)
- `api/restaurants` (+ `/tourism-place/{tourismPlaceId}`) — restaurants
- `api/tour-places` — tourism places (pure JSON CRUD; no image params/routes — images go through the dedicated `api/tour-place-attachments` below), plus `/search`, `/district/{districtId}`, `/category/{categoryId}`, `/rating`, `/filter`
- `api/tour-place-attachments` — **dedicated** attachment API (see below); `GET /tour-place/{tourPlaceId}`, `POST` (`tourPlaceId` + `files`/`file` + optional `type`, `multipart/form-data`), `DELETE /{attachmentId}`
- `api/tickets`, `api/ticket-bookings` — tickets + bookings (incl. `/available`, `/date`, `/verify`, `POST /{id}/cancel`, `/use`); `POST /api/ticket-bookings/{id}/payment` and `POST /api/payments/callback[:form]` in `PaymentController`
- `api/ticket-bookings/{id}/eticket` — e-ticket (OpenPDF + QR via `util/QrCodeUtil`)
- `api/room-bookings` — room bookings (+ `/user/{userId}/status/{status}`, `POST /{id}/cancel`)
- `api/place-categories` — categories (create/update consume `multipart/form-data`)
- `api/v1/bakong` — NBC Bakong KHQR payments (see Conventions): `POST /generate-qr`,
  `POST /check-status`, `POST /simulate-payment?md5=` (sandbox helper)
- `api/bookings`, `api/admin/bookings`, `api/admin/dashboard-stats`, `api/owner` — unified bookings + dashboards
  (`AdminOwnerBookingController`, `AdminDashboardController`, `OwnerDashboardController`):
  `POST /api/bookings` (unified ROOM/TICKET/FOOD_ORDER checkout that also fires a realtime
  notification), `GET /api/admin/bookings` (paged global feed with
  `?type=&status=&search=&page=&size=` — page default 0, size default 20, max 200),
  `GET /api/admin/dashboard-stats` (real DB aggregates: totals, booking breakdown,
  non-cancelled revenue, pending orders, active promotions, 6-month revenue trend, recent
  bookings), `GET /api/owner/bookings/{ownerId}`, and the `api/owner` group
  (`/dashboard-stats`, `/bookings[?status=]`, `PUT /bookings/{bookingType}/{id}/status`,
  `/services` GET/POST, `PATCH /services/{offeringType}/{id}/availability`,
  `DELETE /services/{offeringType}/{id}`)
- `api/{entity}/{entityId}/attachments` — attachment joins for User/Hotel/Room/Food/
  Restaurant `POST`, `GET`, `DELETE /{attachmentId}` — one central `attachments` table (Cloudinary
  metadata) linked via `user/hotel/room/tour_place/food/restaurant_attachments` junction tables
  (see Conventions). **Exception:** TourPlace attachments use the dedicated
  `api/tour-place-attachments` routes, not `api/tour-places/{id}/attachments`.

## Authentication & authorization

- **JWT** (`io.jsonwebtoken:jjwt` 0.13): `security/JwtService` issues/parses HS256 tokens
  (subject = username, claims `userId`/`fullname`). `security/JwtAuthenticationFilter` reads
  `Authorization: Bearer <token>` and populates the `SecurityContext` via
  `security/AppUserDetailsService` (authorities are `ROLE_<name>` uppercased from `user_roles`).
  The filter is a plain class instantiated in `SecurityConfig` (not a `@Component`) to avoid
  double servlet registration.
- **SecurityConfig** (`config/SecurityConfig`): stateless; role-based route matrix in
  `authorizeHttpRequests` (rules are evaluated top-down, first match wins, so specific
  sub-paths like `/user/**`, `/status/**`, `/restaurant/**` are declared **before** generic
  `/{id}` patterns for the same resource). Custom `security/RestAuthenticationEntryPoint`
  (401 JSON) and `security/RestAccessDeniedHandler` (403 JSON) output the standard
  `{timestamp, status, error, message}` shape.

  Route → role map (HTTP methods qualifiers apply unless the pattern is unqualified):

  | Area | Route | Allowed |
  |------|-------|---------|
  | Public | `api/auth/register`, `login`, `forgot-password`, `reset-password`; Swagger paths; `POST /api/payments/callback[:form]`; `POST /api/contact`; `POST /api/newsletter/subscribe`; all catalog GETs (hotels, rooms, foods, tickets, tour-places, attachments, reviews, promotions) | everyone |
  | Realtime/payments | `POST /api/bookings`, `/api/v1/bakong/**`, `/ws-tourism/**` | `permitAll` |
  | Dashboard (admin) | `GET /api/admin/**` | `ADMIN` |
  | Dashboard (owner) | `/api/owner/**` | `ADMIN`, `OWNER` |
  | Admin module | `GET/POST/PUT/DELETE /api/management/**`, `/api/contact/**`, `/api/newsletter/**` | `ADMIN` |
  | Categories | `POST/PUT/DELETE /api/place-categories/**` | `ADMIN` |
  | Business write | `POST/PUT/DELETE /api/hotels/**`, `/api/hotel-rooms/**`, `/api/room-types/**`, `/api/rooms/**`, `/api/restaurants/**`, `/api/foods/**`, `/api/food-categories/**`, `/api/promotions/**`, `/api/tour-places/**`, `/api/tickets/**`; `POST/DELETE` of `api/tour-place-attachments/**` and `api/{hotels,rooms,foods,restaurants}/*/attachments` | `ADMIN`, `OWNER` |
  | Restaurant ops | `GET /api/food-orders/restaurant/**`; `PUT /api/food-orders/*/status`; `GET /api/food-orders` (all orders), `GET /api/food-orders/status/**`; `GET /api/room-bookings` (all) | `ADMIN`, `OWNER` |
  | Ticket staff | `POST /api/ticket-bookings/verify`, `POST /api/ticket-bookings/*/use`; `GET /api/ticket-bookings` (all), `/status/**`, `/date`, `/ticket/**` | `ADMIN`, `OWNER` |
  | Booking views | `GET /api/room-bookings/status/**`, `/room/**`; `PUT /api/room-bookings/*` | `ADMIN`, `OWNER` |
  | Tourist own-read | `GET /api/food-orders/user/**`, `/{id}`; `/api/ticket-bookings/user/**`, `/{id}`, `/*/eticket`; `/api/room-bookings/user/**`, `/{id}` | `ADMIN`, `TOURIST` |
  | Tourist write | `POST /api/food-orders/**`, `/api/ticket-bookings`, `/*/cancel`, `/*/payment`, `/api/room-bookings`, `/*/cancel`; all `/api/carts/**` | `ADMIN`, `TOURIST` |
  | Any logged-in | `POST/DELETE /api/users/*/attachments`; `POST /api/auth/change-password`, `POST /api/auth/logout` | authenticated |
  | Hard delete | `DELETE /api/food-orders/*`, `/api/ticket-bookings/*`, `/api/room-bookings/*` | `ADMIN` |
  | Everything else | — | `permitAll` |

  Note: a single `requestMatchers` accepts **one** `HttpMethod` + varargs patterns (Spring
  Security 7); for multi-method rules declare one rule per method, in the same order.
- **Roles:** stored in the `roles` table, joined via `user_roles`. `config/DataSeeder`
  (a `CommandLineRunner`) seeds `ADMIN`, `OWNER`, `TOURIST` plus default accounts
  `admin/admin123`, `owner/owner123`, `tourist/tourist123` (and a sample
  `BusinesssOwnerProfiles` for the owner). New registrations always get `TOURIST`
  (see `util/RoleNames`).
- **Logout/token handling:** `security/TokenBlacklistService` revokes a token by storing it in
  Redis (`blacklisted:token:<jwt>`) until its natural expiry; Redis outages degrade gracefully to
  stateless expiry only. Never store tokens server-side otherwise.
- **Password flow:** `forgot-password` returns a UUID token (dev-mode: returned in JSON, no email);
  `reset-password` consumes it (30-min expiry, one-time use via `model/PasswordResetToken`);
  `change-password` requires the current password and an authenticated request.
- Note for Spring Security 7: `DaoAuthenticationProvider` has **no** no-arg constructor — use
  `new DaoAuthenticationProvider(userDetailsService)`.

## Domain models

All entities live in `model/`. Grouped: users/roles/perms (`Users`, `Roles`, `UserRoles`,
`BusinesssOwnerProfiles`, `Notifications`); locations (`Location`, `PlaceCategoties`,
`TourPlaces`); hospitality (`Hotels`, `HotelRooms`, `RoomTypes`, `Rooms`,
`Restaurants`, `Foods`, `FoodCategories`); commerce (`Carts`, `CartItems`, `Promotions`,
`Favorites`, `Reviews`); bookings/orders (`RoomBookings`, `TicketBookings`, `Tickets`, `FoodOrders`,
`FoodOrderItems`, `TourBookings`, `TourPackages`, `TourPackageStops`, `TourGuides`, `Payments`);
media/attachment system (`Attachments` central table — Cloudinary metadata only — plus six junction
entities `UserAttachments`, `HotelAttachments`, `RoomAttachments`, `TourPlaceAttachments`,
`FoodAttachments`, `RestaurantAttachments`; see Conventions).

Note the historical rename in this codebase: `TourPlaces` was once `TourismPlaces`, and the
`@ManyToOne` back-refs on `Restaurants`/`Tickets`/`Reviews` are `tourPlaces` /
`tourPlace` (not `tourismPlaces`). Repositories derived queries and `mappedBy`
must use the **current** field names.

## Error handling

`exception/GlobalExceptionHandler` (`@RestControllerAdvice`) maps every response to the
`{timestamp, status, error, message}` shape (validation also returns a per-field `errors` map):

- `ResourceNotFoundException` → 404 "Not Found"
- `UnauthorizedException` → 401 "Unauthorized"
- `MethodArgumentNotValidException` → 400 "Validation Failed" with per-field `errors`
- `IllegalArgumentException` → 400 "Bad Request"
- `InvalidFileException` → 400 "Invalid File"
- `FileStorageException` → 500 "File Storage Error"
- `CloudinaryUploadException` → 500 "Cloudinary Upload Error"
- `CloudinaryDeleteException` → 500 "Cloudinary Delete Error"
- `Exception` → 500 "Internal Server Error"

Services throw specific exceptions from `exception/` (e.g.
`new ResourceNotFoundException("Resource", id)` for missing records); controllers never catch —
the global handler translates them into consistent JSON responses.

## Folder structure

```
src/main/java/com/example/spring_boot_project_api/
├── SpringBootProjectApiApplication.java   # main entry point
├── config/           # @Configuration classes (SecurityConfig, CorsConfig, OpenApiConfig,
│                     # BakongConfig, WebSocketConfig, etc.)
│                     # + AttachmentOrphanCleanupListener (JPA @PreRemove Cloudinary cleanup)
├── controller/        # @RestController — HTTP layer only, delegates to service
├── dto/
│   ├── request/       # inbound request payloads (validated with jakarta.validation)
│   └── response/       # outbound response payloads
├── model/             # @Entity JPA persistence models
├── enums/             # enum types (e.g. AttachmentFileType, AttachmentRole, GenderEnum, UserEnum)
├── Redis/             # Redis cache configuration (Cacheconfig)
├── exception/          # custom exceptions + @RestControllerAdvice global handler
├── mapper/            # model <-> DTO conversion
├── repository/          # Spring Data JPA repositories
├── service/
│   ├── *.java          # service interfaces
│   └── impl/          # service implementations
└── util/              # stateless helpers/constants

src/test/java/com/example/spring_boot_project_api/
├── controller/         # @WebMvcTest slice tests
├── repository/         # @DataJpaTest slice tests
└── service/            # unit tests (Mockito)
```

## Conventions

- **Layering:** `controller` → `service` → `repository`. Controllers never touch entities or
  repositories directly; they work with DTOs and call service interfaces.
- **DTOs:** never expose JPA `model` classes directly over the API — always map to a
  `dto/request` or `dto/response` type via the `mapper` package.
- **Services:** define an interface in `service/`, implementation in `service/impl/`.
  (Exception: `BookingNotificationService` is a concrete `@Service` — it has no interface.)
- **Errors:** throw specific exceptions from `exception/`, handled centrally by a
  `@RestControllerAdvice` — don't catch-and-swallow in controllers.
- **Validation:** use `jakarta.validation` annotations on request DTOs; let Spring's validation
  handle rejection rather than manual null-checks in controllers.
- **Lombok:** prefer `@Getter/@Setter`/`@Builder`/`@RequiredArgsConstructor` over hand-written
  boilerplate; use constructor injection (via `@RequiredArgsConstructor`) instead of `@Autowired`
  field injection.
- **Config:** database connection and environment-specific settings belong in
  `application.properties` (or profile-specific `application-{profile}.properties`), not
  hardcoded in Java.
- **File/image uploads:** images are uploaded to Cloudinary (`cloudinary-http44`), never stored on the
  local server. Controllers that accept files use `consumes = MediaType.MULTIPART_FORM_DATA_VALUE`
  with a `@ModelAttribute` DTO containing `MultipartFile` fields. `service/CloudinaryService` wraps the
  SDK: `uploadImage(file[, baseFolder, fileType])` returns an `UploadResult` record
  (`secureUrl`, `publicId`, `resourceType`), and `delete(publicId[, resourceType])` removes an asset
  later by its public id. Entities that store a single image keep only the `secure_url` string (see
  `FoodServiceImpl`). The `attachments` system persists Cloudinary metadata
  only — `cloudinary_url`, `cloudinary_public_id` (required so the image can be deleted later) and
  `cloudinary_resource_type` — organised under `smart-tourism/{entity}/{entityId}/{uuid}`. Credentials
  come from env vars via `application.properties`; never hardcode the API secret.
- **Security:** JWT-based (stateless) — see **Authentication & authorization** above.
  `config/SecurityConfig` applies the role-based route matrix (`ADMIN`/`OWNER`/`TOURIST`,
  see the route→role table); catalog GETs stay public, and the auth-only endpoints require
  authentication. Passwords are BCrypt-encoded.
  JWT secret/lifetime come from env (`JWT_SECRET`, `JWT_EXPIRATION_MS`); never hardcode a real secret.
- **Realtime (WebSocket/STOMP):** `config/WebSocketConfig` enables an in-memory simple broker
  (`/topic`) with app prefix `/app` and exposes `/ws-tourism` (SockJS + native). `BookingNotificationService`
  broadcasts `BookingNotificationDTO` events to `/topic/admin/bookings` and `/topic/owner/{ownerId}/bookings`
  when a booking is created or its status changes.
- **Bakong KHQR payments:** `config/BakongConfig` supplies the `bakong.*` properties (code defaults
  only — not wired through `application.properties`/`.env`). `util/KhqrGenerator` builds EMVCo-compliant
  KHQR strings + MD5 hashes; `BakongPaymentServiceImpl` renders a Base64 QR (`util/QrCodeUtil`), tracks
  QR sessions in an in-memory `ConcurrentHashMap` (nothing persisted server-side), queries Bakong's
  `check_transaction_by_md5` only when `bakong.api.token` is set, and auto-confirms the matching
  TICKET/ROOM/FOOD_ORDER row on success. `POST /api/v1/bakong/simulate-payment` is a sandbox-only
  dev helper (no real bank funds).
- **Attachment system (one central table + 6 junction tables):** there is exactly one `attachments`
  table (Cloudinary metadata only) and six junction entities — `UserAttachments`, `HotelAttachments`,
  `RoomAttachments`, `TourPlaceAttachments`, `FoodAttachments`, `RestaurantAttachments`. Each
  junction is a `@Entity` join table linking `{Entity}_id` to `attachments_id` with
  `UNIQUE({entity}_id, attachments_id)` (no `@ManyToMany`); owners use
  `@OneToMany(mappedBy = "{lowerCamelEntity}", cascade = ALL, orphanRemoval = true)`. All logic is
  consolidated in `AttachmentUploadService` (+ impl) and exposed via `api/{entity}/{entityId}/attachments`
  (POST single/`files` multiple `multipart/form-data` + optional `type` role, GET list, DELETE
  `/{attachmentId}`) for User/Hotel/Room/Food/Restaurant. **TourPlace is the exception:** its
  attachments use the dedicated `api/tour-place-attachments` routes (`GET /tour-place/{tourPlaceId}`,
  `POST`, `DELETE /{attachmentId}`) and `api/tour-places` stays a pure JSON CRUD API with no
  image/`MultipartFile` parameters. All uploads are organised under
  `smart-tourism/{entity}/{entityId}/{uuid}`; deletion is reference-counted across the 6 junction
  tables before the Cloudinary asset is removed, and a failed transaction deletes already-uploaded
  Cloudinary images. When JPA derives queries or validates `mappedBy`, the property name must match
  the actual owning field — after a rename, update both the owning field and every
  `mappedBy`/derived-query that references it.

## Notes for agents

- Don't add a new architectural layer or dependency unless the task actually needs it.
- Keep controller methods thin; business logic belongs in the service layer.
- When adding a new resource (e.g. `Product`), create matching files across `model`,
  `repository`, `dto/request`, `dto/response`, `mapper`, `service` (+ `impl`), and `controller` —
  don't skip the DTO/mapper layer "just this once."
- Before renaming a JPA field, grep for `mappedBy`, derived-query methods, mapper setters, and test
  fixtures that reference the old name — a compiler-clean build can still fail at context load.
- Tests: service unit tests (`src/test/java/.../service/`) use Mockito mocks without a DB; mirror the
  existing test style (e.g. `TicketBookingServiceImplTest`) when adding new ones.
- Run `./mvnw test` before considering a change complete.