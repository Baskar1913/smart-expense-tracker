# One-Click Automation Files

This folder contains simple Windows batch files used to run the project without typing long commands.

The main purpose of these files is **one-click setup, execution, build, and testing**.

---

## Folder Location

Keep this folder inside the main project:

```text
smart-expense-tracker/
├── .mvn/
├── mvnw
├── mvnw.cmd
├── pom.xml
├── src/
├── tests/
└── bat-files/
    ├── README.md
    ├── setup-frontend.cmd
    ├── start-backend.cmd
    ├── start-frontend.cmd
    ├── build-frontend.cmd
    ├── run-backend-tests.cmd
    ├── run-api-tests.cmd
    └── run-ui-tests.cmd
```

`mvnw`, `mvnw.cmd`, `.mvn`, and `pom.xml` should stay in the **main project folder**.

---

## Why These Files Are Used

Normally, the project is started and tested by entering commands in Command Prompt.

These batch files store those commands already.

The user only needs to double-click the required `.cmd` file.

---

## 1. `setup-frontend.cmd`

### Purpose

Installs the packages required by the React frontend.

### Command used

```bat
npm --prefix src\frontend install
```

### When to run

Run it once after downloading and extracting the project.

Double-click:

```text
setup-frontend.cmd
```

---

## 2. `start-backend.cmd`

### Purpose

Starts the Spring Boot backend server.

### Command used

```bat
mvnw.cmd spring-boot:run
```

### Backend URL

```text
http://localhost:8080
```

### When to run

Run it before opening the frontend, Swagger, REST Assured tests, or Selenium tests.

Double-click:

```text
start-backend.cmd
```

Keep the opened window running.

---

## 3. `start-frontend.cmd`

### Purpose

Starts the React frontend using Vite.

### Command used

```bat
npm --prefix src\frontend run dev
```

### Frontend URL

```text
http://localhost:5173/
```

### When to run

Run it after starting the backend.

Double-click:

```text
start-frontend.cmd
```

Keep the opened window running.

---

## 4. `build-frontend.cmd`

### Purpose

Cleans the old build files, builds the backend, and creates the frontend production build.

### Command used

```bat
mvnw.cmd clean package -Pfrontend-build -DskipTests
```

### When to run

Run it when you want to check whether the complete project builds successfully.

Double-click:

```text
build-frontend.cmd
```

---

## 5. `run-backend-tests.cmd`

### Purpose

Runs the backend JUnit test cases.

### Command used

```bat
mvnw.cmd clean test
```

### Test path

```text
src/backend/src/test/java/
```

### When to run

Run it when you want to test backend services, security, validation, and other backend functions.

Double-click:

```text
run-backend-tests.cmd
```

---

## 6. `run-api-tests.cmd`

### Purpose

Runs the REST Assured API automation tests.

### Command used

```bat
mvnw.cmd clean test -Papi-tests
```

### Test path

```text
tests/automation/src/test/java/com/example/expensetracker/automation/api/
```

### Before running

The backend must be running at:

```text
http://localhost:8080
```

Double-click:

```text
run-api-tests.cmd
```

---

## 7. `run-ui-tests.cmd`

### Purpose

Runs the Selenium UI automation tests.

This is the main one-click browser automation file.

### Command used

```bat
mvnw.cmd clean test -Pui-tests
```

### Test path

```text
tests/automation/src/test/java/com/example/expensetracker/automation/ui/
```

### Before running

Both servers must be running:

```text
Backend:  http://localhost:8080
Frontend: http://localhost:5173/
```

Double-click:

```text
run-ui-tests.cmd
```

Chrome will open automatically and Selenium will test the application.

---

## Recommended Order

```text
1. Double-click setup-frontend.cmd once
2. Double-click start-backend.cmd
3. Wait until the backend starts on port 8080
4. Double-click start-frontend.cmd
5. Open http://localhost:5173/
6. Double-click run-api-tests.cmd when API testing is needed
7. Double-click run-ui-tests.cmd when Selenium testing is needed
8. Double-click run-backend-tests.cmd when JUnit testing is needed
```

---

## Important Note About the Folder

Because these files are stored inside the `bat-files` folder, every `.cmd` file should first move to the main project folder.

Use this line near the top of each batch file:

```bat
cd /d "%~dp0.."
```

Example:

```bat
@echo off
cd /d "%~dp0.."
call mvnw.cmd clean test -Pui-tests
pause
```

`%~dp0..` means the parent folder of `bat-files`, which is the main project folder containing `pom.xml`.

---

## Conclusion

These batch files are helper files for Windows. They provide one-click commands for frontend setup, backend startup, frontend startup, project build, JUnit testing, REST Assured API testing, and Selenium UI automation.
