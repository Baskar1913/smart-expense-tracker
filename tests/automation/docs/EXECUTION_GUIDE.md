# Execution Guide

## Recommended order

1. Start Spring Boot backend.
2. Start React frontend.
3. Run API tests first.
4. Run UI tests.
5. Review `target/surefire-reports`.
6. Review `target/screenshots` when a UI case fails.

## Eclipse

- Import as **Existing Maven Project**.
- Right-click `AuthApiTests.java`, `ExpenseApiTests.java`, `AuthenticationUiTests.java`, or `ExpenseUiTests.java`.
- Select **Run As → JUnit Test**.

## Command Prompt

```bash
mvn clean test
mvn test -Papi-tests
mvn test -Pui-tests
```

Headless Chrome:

```bash
mvn test -Pui-tests -Dheadless=true
```

Edge:

```bash
mvn test -Pui-tests -Dbrowser=edge
```

## Common failures

### Connection refused

Backend or frontend is not running, or a configured URL is wrong.

### HTTP 429 appears for every username

The application is probably still using the client IP as the rate-limit key. The intended implementation must key failed attempts by normalized username.

### Browser driver error

Confirm internet access for Selenium Manager on the first run and confirm the browser is installed. A corporate proxy may require proxy configuration.

### Monthly summary file is not found

Check browser download permission and `target/downloads`. Chrome or Edge is the preferred browser for this test.

### Old application response appears

Stop every old Java process on port 8080, clean the backend project, and start the current application version.
