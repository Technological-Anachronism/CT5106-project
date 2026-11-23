ServiceTicketPortal:

To run Service ticket portal, run CT5106-project folder in powershell, then use: 
.\mvnw.cmd spring-boot:run

If port 8080 is in use, run:
.\mvnw.cmd spring-boot:run "-Dspring-boot.run.arguments=--server.port=8081"

RepairTicketTest tests info:
openingATicketStartsItAsOpen checks that a new ticket with a description starts as OPEN.
aBlankDescriptionIsRefused checks that blank descriptions are rejected and a ticket isnt made.
anOpenTicketCannotBeClosed checks that closing an open ticket fails, the error is “A ticket has to be resolved before it can be closed”, and the ticket stays OPEN.
aResolvedTicketCanBeClosed checks that resolving a ticket and then closing it leaves it status as CLOSED.

to run these tests use:
.\mvnw.cmd test


## Service Analytics — Senlei Liu

Analytics is now a page inside the existing `frontend` React application. Start this
single application and choose **Service Analytics** in its navigation. The old
`analytics-frontend` application has been migrated; Portal and Management source
code and their backend rules are unchanged by this contribution.

### Run Analytics locally

Prerequisites: Java 17+, Node 20.19+ or 22.12+, PostgreSQL, and an existing `bms` database.
From the repository root, configure `DB_URL`, `DB_USER`, `DB_PASSWORD` for your local
PostgreSQL installation. Choose temporary Analytics administrator credentials:

```sh
export BMS_ANALYTICS_ADMIN_USERNAME=analytics-admin
read -s BMS_ANALYTICS_ADMIN_PASSWORD
export BMS_ANALYTICS_ADMIN_PASSWORD
sh ./mvnw spring-boot:run
```

In PowerShell use `$env:BMS_ANALYTICS_ADMIN_USERNAME = 'analytics-admin'` and set
`$env:BMS_ANALYTICS_ADMIN_PASSWORD` to your chosen password before running
`.\mvnw.cmd spring-boot:run`. Do not commit passwords or place them in frontend files.

In another terminal:

```sh
cd frontend
npm ci
npm run dev
```

Open the URL Vite prints and select Service Analytics. Sign in with the configured
credentials; Refresh reads the current persisted tickets. Use the existing Management
workflow to resolve assigned tickets and Portal to close them, then refresh Analytics.

### Analytics architecture and statistics

`AnalyticsController` handles HTTP output; `AnalyticsAdminFilter` handles its temporary
credentials. `AnalyticsService` reads the existing `TicketRepository` port, captures
states, invokes the domain report factory and stores the latest snapshot through
`AnalyticsRepository`. The existing JPA adapter owns ticket persistence; the latest
derived analytics snapshot is cached in memory and recomputed on each GET.

`GET /api/analytics/overview` returns total/open/resolved/closed counts, service workload,
feature usage, pending actions and a generation timestamp. OPEN means pending resolution;
RESOLVED means pending closure. Service outstanding = OPEN + RESOLVED. Created/resolve/close
usage is estimated as total / (RESOLVED + CLOSED) / CLOSED. These estimates rely on the
current lifecycle without ticket deletion or reopening. Service status now changes between
TASKS_OUTSTANDING and NO_OUTSTANDING_TASKS; it describes business workload, not uptime.
Only the existing repair-ticket service is integrated. No historical/time-window analytics
is claimed. The filter remains independent of team login/roles, and its default is to deny
access until credentials are configured. Use HTTPS for remote access.

### Analytics tests and build

```sh
# Analytics unit + real MVC/JPA integration tests; no external database needed
sh ./mvnw -Dtest=AnalyticsServiceTest,AnalyticsAdminFilterTest,AnalyticsIntegrationTest test
# Run the entire existing suite against an isolated in-memory database
sh ./mvnw -Dspring.profiles.active=analytics-test test
# Component logout regression check (from frontend)
node src/analytics/AnalyticsPage.test.cjs
npm run build
```

The `analytics-test` profile uses test-only H2 in PostgreSQL mode. To verify the Analytics
integration test with PostgreSQL instead, set ANALYTICS_TEST_DB_URL, ANALYTICS_TEST_DB_USER
and ANALYTICS_TEST_DB_PASSWORD to a **disposable test database**. Its tables are created and
dropped by Hibernate. Never supply the normal `bms` database. Production datasource settings
and teammates' test configuration files are unchanged.

On JDKs where Mockito dynamic attachment is restricted, start it explicitly using Maven
`-DargLine=-javaagent:/absolute/path/to/mockito-core-VERSION.jar`, using the Mockito version
resolved by Maven (the local verification used 5.17.0). The frontend regression check tests
component state logic with mocked hooks, not a complete browser end-to-end environment.

For deployment, run `npm run build` in frontend, then serve `frontend/dist` and proxy `/api`
to Spring Boot on the same origin. Build/package the backend with `sh ./mvnw package` and
run `java -jar target/service-portal-0.1.0.jar` with database and Analytics environment
variables configured. The new Analytics CI workflow builds this shared frontend and runs
Analytics tests; it does not replace the team's responsibility for other modules and security.
