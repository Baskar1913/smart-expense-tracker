# Smart Expense Tracker Automation

This is the automation module inside the complete Smart Expense Tracker Maven repository.

- **Selenium WebDriver + Java** automates the React user interface.
- **REST Assured + Java** automates the Spring Boot REST API.
- **JUnit 5** runs and reports both test layers.
- A Page Object Model keeps UI locators separate from test logic.
- Failed UI tests save screenshots under `target/screenshots`.
- Monthly-summary download tests use `target/downloads`.

## Prerequisites

1. Java 17 or newer.
2. No global Maven installation is required; use the root `mvnw.cmd` or `mvnw`.
3. Chrome, Edge, or Firefox installed.
4. Smart Expense Tracker backend running at `http://localhost:8080`.
5. React frontend running at `http://localhost:5173` for UI tests.

Selenium Manager is used by Selenium itself, so a matching ChromeDriver or EdgeDriver normally does not need to be downloaded manually.

## Import into Eclipse

1. Extract the ZIP.
2. In Eclipse select **File → Import → Maven → Existing Maven Projects**.
3. Choose the complete project root folder.
4. Select the root, backend, frontend, and automation POM files.
5. Click **Finish**, then run **Maven → Update Project**.

## Configuration

Edit:

```text
src/test/resources/config.properties
```

Defaults:

```properties
ui.base.url=http://localhost:5173
api.base.url=http://localhost:8080/api
browser=chrome
headless=false
explicit.wait.seconds=12
```

Any setting can be overridden from Maven. Examples:

```bash
mvnw.cmd -f tests\automation\pom.xml test -Pui-tests -Dheadless=true
mvnw.cmd -f tests\automation\pom.xml test -Pui-tests -Dbrowser=edge
mvnw.cmd -f tests\automation\pom.xml test -Papi-tests -Dapi.base.url=http://localhost:8080/api
```

## Run tests

Run API tests from the complete-project root:

```bat
mvnw.cmd -f tests\automation\pom.xml clean test -Papi-tests
```

Run UI tests from the complete-project root:

```bat
mvnw.cmd -f tests\automation\pom.xml clean test -Pui-tests
```

A normal Maven build skips automation execution so the repository can build without starting the backend and frontend first.

In Eclipse, right-click a test class and select **Run As → JUnit Test**.

## Test output

```text
target/surefire-reports/   JUnit/Maven reports
target/screenshots/        screenshots captured when UI tests fail
target/downloads/          monthly summary text files downloaded by UI tests
```

## Important test-data behavior

The automation creates unique usernames and email addresses on every run. This prevents duplicate-user failures when the application uses the file-based H2 database. Test records remain in the local test database unless the database is cleaned separately.

## Notes about the brute-force test

The API test `failedAttemptsForOneUsernameDoNotBlockAnotherUsername` verifies account-specific login rate limiting:

- six login requests are made for the first username; the sixth is expected to return HTTP 429;
- a second username must still log in successfully.

That test will fail when login attempts are keyed only by IP address.

## References

- Selenium documentation: https://www.selenium.dev/documentation/
- Selenium Manager: https://www.selenium.dev/documentation/selenium_manager/
- REST Assured: https://github.com/rest-assured/rest-assured/wiki/gettingstarted
- JUnit: https://docs.junit.org/
