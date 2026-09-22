# auth-service

Login, JWT issuing, user management, and role/permission management for the
Network Outage Dashboard (Backend Guide §2, build order step 3). Registers
with `eureka-server` and is routed to through `api-gateway`.

## Requirements
- Java 17
- Maven 3.8+
- `eureka-server` (port 8761) running first — see the root backend guide

## Run

```bash
cd auth-service
mvn spring-boot:run
```

Starts on **port 8081**, uses a file-based H2 database at
`auth-service/data/authdb`, and registers with Eureka as `AUTH-SERVICE`.
Reachable directly (`http://localhost:8081/api/...`) and through the gateway
(`http://localhost:8080/api/auth/...`, `/api/users/...`, `/api/roles/...`,
`/api/permissions/...`).

H2 console: `http://localhost:8081/h2-console` — JDBC URL
`jdbc:h2:file:./data/authdb`, user `sa`, password `password`.

## First run — seeded data

On an empty database, `DataSeeder` creates three roles (`ADMIN`,
`NOC_OPERATOR`, `VIEWER`), a representative permission set, and one enabled
admin user so there's a way to log in immediately:

```
username: admin
password: Admin@123
```

**Change this before anything resembling production.** The seed only runs
once — it's skipped on every subsequent startup once at least one role
exists.

## Endpoints

### Auth (US: Login, Logout)

| Method | Endpoint | Purpose |
|---|---|---|
| POST | `/api/auth/login` | Authenticate, returns a JWT + role name |
| POST | `/api/auth/logout` | Records a logout in the audit trail (send `X-Username` header) |

Login body:
```json
{ "username": "admin", "password": "Admin@123" }
```

### Users (US: User Management)

| Method | Endpoint | Purpose |
|---|---|---|
| POST | `/api/users` | Create a user (`password` required, `roleId` required) |
| GET | `/api/users` | List all users |
| GET | `/api/users/{id}` | Get user by id |
| PUT | `/api/users/{id}` | Edit a user (omit `password` to keep it unchanged) |
| DELETE | `/api/users/{id}` | Remove a user |

Send an optional `X-User-Id` header on POST/PUT/DELETE so audit log entries
record who made the change; falls back to `"system"` if omitted.

### Roles & Permissions (US: Role/Permission Management)

| Method | Endpoint | Purpose |
|---|---|---|
| POST | `/api/roles` | Create a role |
| GET | `/api/roles` | List roles, each with its granted permissions |
| GET | `/api/roles/{id}` | Get one role with its permissions |
| PUT | `/api/roles/{id}` | Edit a role's name/description |
| DELETE | `/api/roles/{id}` | Remove a role (and its permission grants) |
| POST | `/api/roles/{id}/permissions` | Grant permissions: `{ "permissionIds": [1,2,3] }` |
| DELETE | `/api/roles/{id}/permissions/{permissionId}` | Revoke one permission |
| POST | `/api/permissions` | Create a permission |
| GET | `/api/permissions` | List permissions |
| GET | `/api/permissions/{id}` | Get one permission |
| PUT | `/api/permissions/{id}` | Edit a permission |
| DELETE | `/api/permissions/{id}` | Remove a permission (and its role grants) |

### Audit log (US: Unauthorized Access Logging)

| Method | Endpoint | Purpose |
|---|---|---|
| GET | `/api/audit-logs?action=&username=` | View the audit trail, optionally filtered |

Every login attempt (success or failure — bad username, bad password, or a
disabled account), every logout, and every user/role/permission change is
written here with an action code (`LOGIN_SUCCESS`, `LOGIN_FAILURE`,
`LOGOUT`, `USER_CREATED`, `USER_UPDATED`, `USER_DELETED`, ...), an optional
`userId`/`username`, and the caller's IP address.

## Design notes

- Passwords are stored as BCrypt hashes (`PasswordEncoder` bean in
  `SecurityConfig`) — never returned in any response DTO.
- `User.roleId`, `RolePermission.roleId/permissionId` are plain `Long`
  columns rather than JPA object relations, matching the project-wide
  convention (see `device-service`'s `DeviceHistory.deviceId`) of resolving
  and validating references in the service layer.
- JWTs are HS256, signed with the shared secret in `application.yml`
  (`jwt.secret` — move to an environment variable before production) and
  carry `userId` and `role` claims. Other services should validate tokens
  signed with this same secret once they wire in JWT checks on protected
  routes (Backend Guide §1.3/§1.7).
- `SecurityConfig` is dev-mode `permitAll`, same as `device-service` — the
  filter chain doesn't yet enforce the JWTs this service issues. Tighten
  once the gateway forwards `Authorization` headers downstream.
- Deleting a role or permission cleans up its `RolePermission` rows; it
  does **not** currently block deleting a role that users still reference
  (kept simple for MVP — revisit if this becomes a real workflow concern).

## Next steps
- Wire JWT validation into `device-service`, `alarm-service`,
  `incident-service`, and `dashboard-reporting-service` (a shared
  `jwt.secret` + a small filter/interceptor in each).
- Add JWT validation at `api-gateway` so unauthenticated requests never
  reach downstream services at all.
- Build `alarm-service` (port 8083) next per the guide's build order — it
  depends on `device-service` (already built) for validation.
