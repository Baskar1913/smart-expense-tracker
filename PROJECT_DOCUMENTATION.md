# Smart Expense Tracker - Project Documentation

## Project Overview

Smart Expense Tracker is a full-stack web application for secure account access and personal expense management. A customer can register, log in, reset a forgotten password, add expenses, search records, view totals and monthly summaries, delete records, and log out.

The React frontend communicates with the Spring Boot backend through HTTP and JSON. Spring Security protects the API, H2 stores local data, and log files help developers find failures and security events.

![Architecture](smart_expense_tracker_architecture_corrected.png)

## Main Technologies and Their Purpose

| Technology | Purpose |
|---|---|
| React + Vite | Builds the browser user interface and development server. |
| Java 17 + Spring Boot | Runs the REST API and business logic. |
| Spring Security | Protects endpoints and integrates authentication. |
| BCrypt | Stores passwords as one-way salted hashes, not plain text. |
| JWT | Provides signed access and refresh tokens. |
| Spring Data JPA | Maps Java entities to relational database tables. |
| H2 | Provides a local file-based relational database. |
| Spring AOP | Records controller and service execution time. |
| Springdoc OpenAPI | Generates Swagger UI and OpenAPI documents. |
| JUnit | Tests backend classes and Spring integration behavior. |
| REST Assured | Tests the running REST API. |
| Selenium | Tests the application through Chrome. |

## Security

### BCrypt password hashing

BCrypt is a one-way password hashing method. During registration and password reset, only the BCrypt hash is stored in `app_users.password_hash`. The original password cannot be recovered from this value.

### JWT tokens

A successful login returns an access token and refresh token. The access token lasts 15 minutes and is sent in the `Authorization: Bearer` header. The refresh token lasts 7 days and is used to request a new pair. Used refresh tokens and logout tokens are revoked by storing their token IDs in `revoked_tokens`.

### Login rate limiting

Failed attempts are tracked separately for each username. Five failures in a ten-minute window block more attempts for that username and return HTTP 429. This data is stored in memory and is cleared when the backend restarts.

### Password reset

A secure random reset token is created after the username and registered email are verified. Only its SHA-256 hash is stored. The token expires after 10 minutes and is marked used after one successful reset.

### CORS and validation

CORS allows the React origin `http://localhost:5173` to call the backend. DTO validation and service checks reject invalid values. Global exception handling returns clear JSON errors without exposing Java stack traces to the UI.

## H2 Database

The database URL is:

```text
jdbc:h2:file:./src/backend/data/expense_tracker
```

Main tables:

- `app_users`: customer details and BCrypt password hash
- `expenses`: title, amount, category, date and owner
- `password_reset_tokens`: reset-token hash, expiry and used status
- `revoked_tokens`: revoked JWT IDs and expiry

The file is normally located at `src/backend/data/expense_tracker.mv.db`. Only one backend process should open the embedded file at a time.

## Logs

Log path:

```text
src/backend/logs/
```

- `expense-tracker.log`: normal startup, request and service activity
- `error.log`: ERROR events and stack traces
- `audit.log`: login, logout, password reset, token and expense business events

Every request receives an `X-Request-Id`. The same ID appears in log lines and the response header, helping a developer connect one browser request to the related backend messages. The request log includes method, path, HTTP status and duration. The AOP logging aspect also records controller and service execution time.

Passwords and raw tokens must never be written to logs.

### Finding a bug using logs

1. Reproduce the problem and note the time and request ID.
2. Check `error.log` for a stack trace.
3. Search the request ID in `expense-tracker.log`.
4. Check `audit.log` for login, password, token or expense events.
5. Fix the code and repeat the same request.

## Swagger

```text
http://localhost:8080/swagger-ui.html
http://localhost:8080/v3/api-docs
http://localhost:8080/v3/api-docs.yaml
```

## Testing

```bat
mvnw.cmd clean test
mvnw.cmd clean test -Papi-tests
mvnw.cmd clean test -Pui-tests
```

Backend JUnit tests are in `src/backend/src/test/java/`. REST Assured and Selenium tests are in `tests/automation/src/test/java/`.

## Run the Project

Backend:

```bat
mvnw.cmd spring-boot:run
```

Frontend:

```bat
cd src/frontend
npm install
npm run dev
```

Open the UI at `http://localhost:5173/`.

## Conclusion

The project combines a React interface, layered Spring Boot API, local H2 storage, practical security controls, useful logs, automated testing and Swagger documentation. Its current setup is suitable for local development and assignment demonstration. A production version should use a managed database, HTTPS, environment-managed secrets, centralized logging, shared rate-limit storage and email-based reset links.
