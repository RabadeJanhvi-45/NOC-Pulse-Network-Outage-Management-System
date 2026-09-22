# Network Outage Dashboard — Backend

Seven Spring Boot 3.2.5 / Java 17 services: `eureka-server` and
`api-gateway` (foundational), plus five business services — `auth-service`,
`device-service`, `alarm-service`, `incident-service`, and
`dashboard-reporting-service`. Every business service registers with
`eureka-server` and is reachable through `api-gateway`; every business
service now also validates the JWT `auth-service` issues, and enforces
role-based access (`ADMIN` / `NOC_OPERATOR` / `VIEWER`) on its endpoints.

## Requirements
- Java 17
- Maven 3.8+

## Run order

Eureka has to be up before anything else registers; the rest can start
in any order after that, but nothing is reachable through the gateway
until both the gateway and the service you're calling are UP in Eureka.

```bash
cd eureka-server && mvn spring-boot:run        # 1. http://localhost:8761
cd api-gateway && mvn spring-boot:run          # 2. port 8080
cd auth-service && mvn spring-boot:run         # 3. port 8081
cd device-service && mvn spring-boot:run       # 4. port 8082
cd alarm-service && mvn spring-boot:run        # 5. port 8083
cd incident-service && mvn spring-boot:run     # 6. port 8084
cd dashboard-reporting-service && mvn spring-boot:run  # 7. port 8085
```

Each service runs in its own terminal (or background process). Watch
**http://localhost:8761** — every service should show as UP before you
start driving traffic through the gateway.

## Authentication

All endpoints except `POST /api/auth/login` and each service's
`/h2-console/**` require a `Bearer` token:

```bash
# 1. Log in (seeded default admin — change this before anything real)
curl -X POST http://localhost:8080/api/auth/login \
  -H "Content-Type: application/json" \
  -d '{"username":"admin","password":"Admin@123"}'
# -> { "token": "...", "roleName": "ADMIN", ... }

# 2. Use the token on everything else
curl http://localhost:8080/api/devices \
  -H "Authorization: Bearer <token>"
```

Tokens are HS256, signed with a shared secret (`jwt.secret` in each
service's `application.yml` — **must match auth-service's value exactly**,
that's how downstream services verify a token without calling back to
auth-service). The three seeded roles and what they can reach:

| Role | Can read | Can write |
|---|---|---|
| `VIEWER` | devices, alarms, alarm rules, incidents, dashboard, reports | — |
| `NOC_OPERATOR` | everything `VIEWER` can | devices, alarms (raise/ack), incidents (create/assign/notes/escalate) |
| `ADMIN` | everything | everything `NOC_OPERATOR` can, plus severity rules, users, roles, permissions, audit logs |

Service-to-service calls (e.g. alarm-service → device-service via Feign
when raising an alarm) forward the caller's token automatically — see
`config/FeignAuthInterceptor.java` in alarm-service, incident-service,
and dashboard-reporting-service. There's no separate service-account /
API-key path yet — if you plan to have monitoring systems push alarms
without a human login, `POST /api/alarms` will need one before that's
usable.

## Services

| Service | Port | Own DB | Depends on (via Feign) |
|---|---|---|---|
| eureka-server | 8761 | — | — |
| api-gateway | 8080 | — | routes to all business services |
| auth-service | 8081 | `authdb` | — |
| device-service | 8082 | `devicedb` | — |
| alarm-service | 8083 | `alarmdb` | device-service |
| incident-service | 8084 | `incidentdb` | device-service, alarm-service |
| dashboard-reporting-service | 8085 | none (stateless aggregator) | device-service, alarm-service, incident-service |

Each service with a DB has an H2 console at `http://localhost:<port>/h2-console`
(JDBC URL `jdbc:h2:file:./data/<name>db`, user `sa`, password `password`),
reachable without a token.

## Route map (api-gateway/application.yml)

| Incoming path (via :8080) | Forwarded to |
|---|---|
| `/api/auth/**`, `/api/users/**`, `/api/roles/**`, `/api/permissions/**`, `/api/audit-logs/**` | auth-service |
| `/api/devices/**` | device-service |
| `/api/alarms/**` | alarm-service |
| `/api/incidents/**` | incident-service |
| `/api/dashboard/**`, `/api/reports/**` | dashboard-reporting-service |

Routing uses `lb://<eureka-app-name>` — no hardcoded ports, so it also
load-balances across multiple instances of the same service if you ever
run more than one.

## Tests

Each business service has a `*SecurityIntegrationTest` under
`src/test/java` exercising the JWT/role rules above (401 with no token,
403 with the wrong role, success with the right one), plus a few core
business-logic flows (register/update a device, raise/acknowledge an
alarm, create/assign an incident). They run against an in-memory H2
instance with Eureka disabled, so no other services need to be running:

```bash
cd device-service && mvn test
# ...same for alarm-service, incident-service, dashboard-reporting-service, auth-service
```

Feign-dependent services (`incident-service`, `dashboard-reporting-service`)
mock their upstream clients with `@MockBean` rather than requiring the
real services to be up. `alarm-service`'s tests run with
`alarm.device-validation.enabled=false` for the same reason.

## Known gaps / things to revisit before production

- **CORS is wide open** (`allowedOriginPatterns: "*"`) for dev convenience
  — tighten once the frontend origin is known.
- **`jwt.secret` is a plaintext dev value** duplicated across every
  service's `application.yml` — move to an environment variable or
  secrets manager, and rotate it, before anything resembling production.
- **No refresh-token flow** — tokens simply expire after `jwt.expiration-ms`
  (1 hour) and the user logs in again.
- **No service-account path for machine-to-machine calls** that don't
  originate from a logged-in user (see the alarm-ingestion note above).
- Test coverage is security- and happy-path-focused, not exhaustive —
  there's room for more edge-case and validation-failure tests per service.
