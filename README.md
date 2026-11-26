# Security Services

Spring Boot 3 / Java 21 starter for authentication, JWT access tokens, rotating refresh tokens, OAuth2 login, role-based authorization, and role-request approvals.

## Stack
- Java 21, Spring Boot 3.3, Gradle
- Spring Security, Spring OAuth2 Client, JJWT
- PostgreSQL + Spring Data JPA + Flyway
- GitHub Actions CI, Docker

## Run locally
1. Copy `.env.example` to `.env` and set secrets.
2. Start PostgreSQL: `docker compose up -d postgres`
3. Export the variables in `.env` (or configure them in your IDE).
4. Run: `./gradlew bootRun`

The API is served at `http://localhost:8080`. Flyway creates the schema and seeds USER, MANAGER and ADMIN roles. The first admin must be provisioned manually in the database; never expose an open admin-registration endpoint.

## Main endpoints
- `POST /api/auth/register`
- `POST /api/auth/login`
- `POST /api/auth/refresh`
- `POST /api/auth/logout`
- `GET /api/users/me`
- `GET /api/roles`
- `POST /api/role-requests`
- `GET /api/admin/role-requests`
- `PATCH /api/admin/role-requests/{id}`

OAuth2 login is configured for Google when `GOOGLE_CLIENT_ID` and `GOOGLE_CLIENT_SECRET` are supplied. The OAuth2 success handler issues the same application JWT/refresh-token pair.

## Security notes
- Access tokens are short-lived (15 minutes).
- Refresh tokens are opaque random values; only SHA-256 hashes are persisted. Refresh rotates and revokes the previous token.
- Logout revokes the presented refresh token.
- Configure a long random `JWT_SECRET` (at least 32 bytes) outside source control.
- CORS, HTTPS, rate limiting, email verification, password reset, audit logging and production secret management should be configured before public deployment.
- The CI workflow runs tests and builds the executable JAR. It does not deploy to AWS until deployment credentials and a target environment are explicitly configured.
