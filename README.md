# Business Management System (CT5106 Group 8)

A full stack application that can be used to handle repair tickets. The system allows users to submit repair requests (tickets) via a portal. Administrator(s) will then review and prioritize each ticket. Once the ticket has been assigned to a specific employee, he/she will resolve the issue. Upon resolution, the user may close the ticket. In addition to being able to submit and resolve repair tickets, the application includes two separate dashboards; one for managing users and roles and another for analyzing repair ticket activity.

Backend: Spring Boot 3.5 (Java 17) using PostgreSQL via Spring Data JPA. Frontend: React 19 using Vite.

## Features

| Story | Feature | Owner |
|---|---|---|
| 1.1.1 | User Management dashboard and API, plus Role Management (see [ROLE_MANAGEMENT_README.md](ROLE_MANAGEMENT_README.md)) | Boyue Qiu |
| 1.2.2 | Service Management dashboard and API (see [below](#service-management-story-122)) | Abdulhaq Elkhbuli |
| 1.2.3 | Service Analytics dashboard and API | Senlei Liu |
| 1.3.1 | Service Portal and API, PostgreSQL/JPA persistence | Patrick Gallagher |

## Architecture

The back end of this application uses the Clean Architecture pattern. Clean Architecture is designed to ensure that the dependencies of an application go inward. This means that the "domain" layer does not have knowledge of other components of the application such as Spring, HTTP or the database.

| Layer | Packages | What it holds |
|---|---|---|
| Domain | `bms.domain`, `bms.analytics.domain`, `bms.roles.domain` | Entities and business rules (`RepairTicket`, `User`, `Priority`, `Role`) and repository interfaces |
| Application | `bms.application`, `bms.management.application`, `bms.analytics.application`, `bms.roles.application` | Use cases: load an aggregate, call a domain method, save it |
| Infrastructure | `bms.infrastructure`, `bms.analytics.infrastructure`, `bms.roles.infrastructure` | JPA entities and adapters that implement the repository interfaces, and the starter data |
| Presentation | `bms.ui`, `bms.controller`, `bms.management.presentation`, `bms.analytics.presentation`, `bms.roles.presentation` | REST controllers |

`RepairTicket` is the central aggregate. The only states for `RepairTicket` are OPEN, RESOLVED, and CLOSED, and all rules regarding these transitions exist within the class itself. If a rule is violated, `RepairTicket` will throw `RepairTicket.TicketProblem`. `TicketProblemHandler` then converts this exception to an HTTP 400 response with a description of the problem.

Persistence has 2 repositories for each aggregate: a Domain Interface (`TicketRepository`), which is implemented by a JPA Adapter (`JpaTicketRepository`) that will use a JPA entity class (`TicketEntity`) and delegate to a Spring Data repository. The Domain Classes do not have any JPA annotations.

## Running locally

### Prerequisites

- JDK 17 or newer, with `JAVA_HOME` set
- PostgreSQL 17 on `localhost:5432`
- Node.js 22

### 1. Create the databases

```
psql -U postgres -c "CREATE DATABASE bms"
psql -U postgres -c "CREATE DATABASE bms_test"
```

`bms` is utilized by the application while `bms_test` is utilized for testing. All tables are automatically generated when the system boots up.

### 2. Start the backend

```
.\mvnw.cmd spring-boot:run
```

For macOS/Linux systems you can run the application with `./mvnw spring-boot:run`. Default port is 8081 (see `application.properties`). If you need another port:

```
.\mvnw.cmd spring-boot:run "-Dspring-boot.run.arguments=--server.port=8081"
```

When you run the application for the first time, you will see that there are two sample tickets created along with an administrative user (with user id 1234) as well as two staff members: Sam Staff (with user id 2001) and Alex Staff (with user id 2002).

### 3. Start a frontend

```
cd frontend
npm install
npm run dev
```

Open the URL printed by Vite (http://127.0.0.1:5173). Requests to `/api` are proxied to the backend at port 8081. Portal, Analytics, User, Role and Service Management are all tabs in this one React app.

## Configuration

| Variable | Default | Used for |
|---|---|---|
| `DB_URL` | `jdbc:postgresql://localhost:5432/bms` | App database |
| `DB_USER` / `DB_PASSWORD` | `postgres` / `postgres` | Database login (app and tests) |
| `TEST_DB_URL` | `jdbc:postgresql://localhost:5432/bms_test` | Test database |
| `BMS_ANALYTICS_ADMIN_USERNAME` / `BMS_ANALYTICS_ADMIN_PASSWORD` | `admin` / `admin` | Login for the Analytics tab. Override these in production |
| `SERVER_PORT` | `8081` | Backend HTTP port (8080 is often taken on Windows) |

## Tests and CI

```
.\mvnw.cmd test
```

- **Unit tests:** These tests are focused on the domain logic and use cases. They do not depend on a database but instead use in-memory fakes to represent the repository objects (e.g. `RepairTicketTest`, `TicketAssignmentTest`, `TicketManagementTest`, `UserServiceTest`, `AnalyticsServiceTest`).
- **Integration tests:** These tests use a live `bms_test` database (`JpaRepositoryTest`), and/or send real HTTP requests to the controller using MockMvc (`UserControllerIntegrationTest`, `RoleManagementControllerTest`).

The GitHub Actions workflow (`.github/workflows/build-and-test.yml`) runs on pushes and pull requests to main. It starts PostgreSQL 17, runs `mvnw clean verify`, and builds the single React app in `frontend/`.

## Building and deploying

```
.\mvnw.cmd clean package
java -jar target/service-portal-0.1.0.jar
```

When you are ready to begin running your jar file, make sure to set your `DB_URL`, `DB_USER`, and `DB_PASSWORD` variables to refer to the actual production database. For the front end, use `npm run build` and then serve the files in the `dist` directory using a static web server of some kind (for example, Apache, Nginx, etc.). Then, configure that static web server to forward requests for `/api` to the back end.

## Project structure

```
src/main/java/bms/         Spring Boot backend (layers above)
src/main/resources/        application.properties, static test page
src/test/java/bms/         Unit and integration tests
frontend/                  Single React app (User, Role, Portal, Management, Analytics tabs)
.github/workflows/         CI workflow
```

## Troubleshooting

- **`violates check constraint "users_role_check"` upon startup:** the `bms` database was created prior to the creation of the `STAFF` role. To fix this issue, run the command `ALTER TABLE users DROP CONSTRAINT users_role_check;` one time in psql while connected to the `bms` database.
- **`Failed to load ApplicationContext` in `JpaRepositoryTest`:** The `bms_test` database has not been created as specified in Step 1.
- **"No compiler is provided" or "class file version 61.0":** Maven is using an older Java version. Set `JAVA_HOME` to JDK 17 or later.

## Service Management (story 1.2.2)

> As an admin, I would like to have a service management dashboard where I can assign, prioritize, and monitor the progress of all outstanding repair tickets until they are completed.

Front End: Service Management Tab within `ManagementPage.jsx` in `frontend/src`. Back End: `TicketManagement` (Use Case) and `ManagementController` (API) using the `RepairTicket` rules.

### Business rules

1. A new ticket is OPEN, MEDIUM priority, with no assignee.
2. Only an OPEN ticket can be assigned, and only to a current user who has the role of STAFF.
3. A ticket must first be assigned before being resolved; only the person that has the assignment can then resolve it. Resolving a ticket occurs via `RepairTicket.resolveBy`, and this is the only publicly accessible method for resolving a ticket, therefore the portal cannot bypass this verification process.
4. Only an OPEN ticket's priority can change.
5. The work queue displays all tickets that are not CLOSED, first displaying those with the HIGH priority rating, and then allows users to filter the list based on assignee and priority level.

`TicketManagement` has the "staff only" check since it requires knowing the user's role (which is contained in the `User` aggregate). Assignment is a property of an OPEN ticket, not a separate status, therefore, the analytics count (OPEN, RESOLVED, CLOSED) will continue to function.

### API

| Method | Path | Does |
|---|---|---|
| GET | `/api/management/tickets?assigneeId=&priority=` | Work queue (both filters optional) |
| POST | `/api/management/tickets/{id}/assign?assigneeId=` | Assign to a staff member |
| POST | `/api/management/tickets/{id}/priority?priority=` | Change priority (HIGH, MEDIUM, LOW) |
| POST | `/api/management/tickets/{id}/resolve?userId=` | Resolve, if `userId` is the assignee |

A broken rule will return 400 (e.g. `{"error": "Only the assignee can resolve this ticket"}`), and the dashboard will display the message. Since we do not have a login, the dashboard will include an "Acting as" drop down for selecting which staff member is resolving.

### Tests

- `TicketAssignmentTest`: the rules that define `RepairTicket` (assigning tickets to staff, resolving tickets, and prioritizing tickets).
- `TicketManagementTest`: the use cases, such as "only staff can be assigned", using in-memory fakes rather than a database.
