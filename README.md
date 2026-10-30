# PulseFit — Microservices Fitness Membership Platform

## Open and run from VS Code (macOS)

**Prerequisites:** JDK 25, Maven 3.9+, PostgreSQL running locally, and VS Code with the Extension Pack for Java. The project uses PostgreSQL databases `pulsefit_auth`, `pulsefit_member`, `pulsefit_subscription`, and `pulsefit_attendance`.

1. Extract this ZIP to a folder, then open that folder in VS Code (`File → Open Folder`). Open the folder containing this README and root `pom.xml`.
2. Ensure the four PostgreSQL databases exist. If needed, run the statements in `sql/create-databases.sql` using pgAdmin or `psql` (create only databases that do not already exist).
3. The startup task uses `DB_USERNAME=postgres` and `DB_PASSWORD=postgres123` by default. To override, set those environment variables in the VS Code terminal before starting.
4. In VS Code press `Cmd+Shift+P` → **Tasks: Run Task** → **PulseFit: Build all modules** (first run; internet is required for Maven dependencies).
5. Then run **Tasks: Run Task** → **PulseFit: Start all services + frontend**. The task launches services in the correct order and writes logs to `.run-logs/`.
6. Open `http://localhost:5500`. Eureka dashboard: `http://localhost:8761`.
7. Stop with **Tasks: Run Task** → **PulseFit: Stop all services**.

Default demo login: `admin` / `admin123` (for classroom demonstration only).

## Services and ports

| Component | Port | Database |
|---|---:|---|
| Eureka Server | 8761 | — |
| Auth Service | 8081 | pulsefit_auth |
| Member Service | 8082 | pulsefit_member |
| Subscription Service | 8083 | pulsefit_subscription |
| Attendance Service | 8084 | pulsefit_attendance |
| API Gateway | 8080 | — |
| Static frontend | 5500 | — |

All frontend API calls go through `http://localhost:8080/api`. Gateway routes use Eureka service IDs and Spring Cloud LoadBalancer (`lb://...`). JWT authentication is enforced at the Gateway for API routes; `/api/auth/login` is public so a token can be obtained.

## Review demonstration

1. Sign in and show the issued JWT.
2. Show the four service registrations in Eureka.
3. Demonstrate member and plan create/read/update/delete operations.
4. Create a subscription for an existing member, or expand **Add a new member** in the subscription dialog to create a member and then subscribe them in the same workflow.
5. Demonstrate subscription renew/cancel and attendance check-in/check-out.
6. To show client-side load balancing, first start the normal stack, then run **Tasks: Run Task** → **PulseFit: Start extra load-balancing instances**. Wait 20–30 seconds and refresh Eureka. Member Service should show instances on 8082 and 8085; Subscription Service on 8083 and 8086. Both instances of each service share that service's PostgreSQL database.
7. Show JWT protection by calling `GET http://localhost:8080/api/members` without an Authorization header (expect 401), then repeat with `Authorization: Bearer <token>` (expect success).

## Logs and troubleshooting

- Logs: `.run-logs/eureka.log`, `auth.log`, `member.log`, `subscription.log`, `attendance.log`, `gateway.log`, `frontend.log`.
- `mvn clean install -DskipTests` from the root builds all six modules.
- If a service cannot connect to PostgreSQL, confirm PostgreSQL is running, the four databases exist, and the password is correct. Override credentials with `DB_USERNAME` and `DB_PASSWORD`.
- If a port is already occupied, run the stop task and retry. Do not launch duplicate primary instances on ports 8081–8084.
- The first Maven build requires network access to download dependencies.

## Important

The JWT signing secret and demo credentials in this educational project are for local coursework only. Replace them with secure externally managed secrets before any production deployment. MOOC completion marks require genuine course progress/certificates; software changes cannot award those marks.
