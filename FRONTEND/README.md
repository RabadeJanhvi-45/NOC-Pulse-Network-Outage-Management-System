# Network Outage Dashboard — Frontend (Angular 17)

Standalone-component Angular 17 app for the Network Outage backend
(7 Spring Boot microservices behind `api-gateway` on `:8080`).

## Setup

Requires Node 18+ and npm.

```bash
npm install
npm start        # ng serve, http://localhost:4200
```

Make sure the backend is running first (see `backend/README.md`):
eureka-server → api-gateway → auth-service → device-service →
alarm-service → incident-service → dashboard-reporting-service, all
showing UP at http://localhost:8761 before you drive traffic through
the app.

`src/environments/environment.ts` points at `http://localhost:8080/api`
(the gateway) — never at an individual service port directly.

## Structure

```
src/app/
  core/                    # cross-cutting: auth, interceptors, guards
    services/
      auth.service.ts        # login/logout, token + role signals
      notification.service.ts # toast queue used by interceptors & features
    interceptors/
      auth.interceptor.ts     # attaches Authorization: Bearer <token>
      error.interceptor.ts    # 401 -> logout, 403/else -> toast
    guards/
      auth.guard.ts           # must be logged in
      role.guard.ts           # route restricted via route `data.roles`
  shared/
    models/                 # TypeScript mirrors of every backend DTO
    components/
      placeholder.component.ts  # stand-in until a feature page is built
  app.routes.ts            # full route table, real guards/roles already wired
  app.config.ts            # providers: router + HttpClient w/ interceptors
  app.component.*          # shell: toast stack + <router-outlet>
```

## Segment plan

| # | Segment | Status |
|---|---|---|
| 0 | Scaffold, models, interceptors, guards, routing | ✅ done |
| 1 | Authentication (US-01–03) — login page, app shell/nav | next |
| 2 | Device Management (US-04–06) | |
| 3 | Alarm Management (US-07–10) | |
| 4 | Incident Management + Assignment (US-11–15) | |
| 5 | Resolution Tracking (US-16–20) | |
| 6 | Dashboard (US-21–23) | |
| 7 | Reporting (US-24) | |
| 8 | User Management (US-25–26) | |

Every route in `app.routes.ts` already points at `PlaceholderComponent`
with the correct guard and `data.roles` for its module — building a
segment means swapping that one `loadComponent` line for the real
component, nothing else in routing changes.

## Auth model reminder

- Roles: `ADMIN`, `NOC_OPERATOR`, `VIEWER` (see backend `DataSeeder`).
- Seeded login: `admin` / `Admin@123`.
- Token expires after 1 hour, no refresh flow — `AuthService` will need
  the user to log in again (the error interceptor already routes a 401
  back to `/login`).
