# CT5106 – User Story 1.1 Role Management

This version is based on the supplied `boyue-assignment4` snapshot. Existing teammate Java source files, ticket portal and analytics frontend were left unchanged. The only existing frontend code changed is `frontend/src/Shell.jsx` (add Role Management navigation) and appended role-specific CSS in `frontend/src/style.css`.

## Features

- Create, list, rename and delete **custom roles**.
- Each custom role inherits one of the four built-in enum roles: `USER`, `STAFF`, `ADMIN`, or `SUPER_ADMIN`.
- Assign a custom role to an existing user; the user's original enum role is updated to the custom role's base role, so existing Ticket Management `Role.STAFF` checks continue to work.
- Refuse changing the base role or deleting a custom role while assigned to a user.
- Remove an assignment without changing the user's current base role.
- Missing/deleted users and legacy edits to the base role are reconciled when assignments are loaded.
- PostgreSQL stores `managed_roles` and `user_managed_roles` via Spring Data JPA (`ddl-auto=update` in development).

**Important limit:** This implements custom role catalogues/assignments; it does **not** add authentication or authorization controls to the HTTP APIs. Without a shared login/security mechanism, endpoints remain accessible to callers who can reach the server. Do not expose it publicly as a secure admin dashboard. The system's `Role` enum is unchanged, and custom roles inherit rather than define arbitrary permissions.

## Endpoints

| Method | Path | Purpose |
|---|---|---|
| GET | `/api/roles` | List custom roles |
| GET | `/api/roles/{code}` | Fetch a role |
| POST | `/api/roles` | Create custom role `{ "code": "SUPPORT_AGENT", "name": "Support Agent", "baseRole": "STAFF" }` |
| PUT | `/api/roles/{code}` | Rename/update unused role (code cannot change) |
| DELETE | `/api/roles/{code}` | Delete unused role |
| GET | `/api/roles/assignments` | List custom user assignments |
| PUT | `/api/roles/assignments/{userId}` | Assign `{ "roleCode": "SUPPORT_AGENT" }` |
| DELETE | `/api/roles/assignments/{userId}` | Remove custom assignment |

## Running on macOS with Java 17 and Homebrew PostgreSQL

From the project root:

```bash
export JAVA_HOME=$(/usr/libexec/java_home -v 17)
export PATH="$JAVA_HOME/bin:$PATH"
export DB_USER="$(whoami)"
export DB_PASSWORD=""
# Create a separate development database only once:
/opt/homebrew/opt/postgresql@17/bin/createdb bms
bash ./mvnw spring-boot:run
```

In another terminal:

```bash
cd frontend
npm ci
npm run dev
```

Open `http://127.0.0.1:5173`, select **Role Management**.

Development database defaults to `jdbc:postgresql://localhost:5432/bms`. Test database defaults to `bms_test` and is set to `ddl-auto=create-drop`, so **never point the test datasource at production data**.

For tests:

```bash
export TEST_DB_URL="jdbc:postgresql://localhost:5432/bms_test"
bash ./mvnw clean verify
```

If running tests locally, make sure PostgreSQL is started and `bms_test` exists. Unit + MVC tests can also be run independently with `bash ./mvnw -Dtest=UserServiceTest,UserControllerIntegrationTest,RoleManagementServiceTest,RoleManagementControllerTest test`.

## Repository update safety

Do not upload this archive onto the GitHub website expecting it to merge source files. Start from a fresh clone, copy only reviewed source changes onto a feature branch, inspect `git diff`, run tests, push branch and open a Pull Request. GitHub will combine only changes represented in the commit; untouched teammate files are preserved. First synchronize with the current main branch and resolve any conflicts.
