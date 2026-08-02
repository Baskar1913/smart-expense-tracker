<div align="center">

# 💰 Smart Expense Tracker

### Secure expense management with Spring Boot, React, JWT, automated testing, and Swagger

![Java](https://img.shields.io/badge/Java-17-orange?style=for-the-badge&logo=openjdk)
![Spring Boot](https://img.shields.io/badge/Spring%20Boot-3.5.16-brightgreen?style=for-the-badge&logo=springboot)
![React](https://img.shields.io/badge/React-Vite-blue?style=for-the-badge&logo=react)
![JUnit](https://img.shields.io/badge/JUnit-5-red?style=for-the-badge&logo=junit5)
![Selenium](https://img.shields.io/badge/Selenium-WebDriver-green?style=for-the-badge&logo=selenium)
![Swagger](https://img.shields.io/badge/OpenAPI-Swagger-85EA2D?style=for-the-badge&logo=swagger)

</div>

---

## 📌 About the Project

Smart Expense Tracker is a full-stack web application used to manage daily expenses securely.  
Users can register, log in, add expenses, view records, filter expenses, calculate totals, and delete entries.  
The backend is developed using Java and Spring Boot, while the frontend is built using React and Vite.  
The project also includes JWT security, local H2 storage, JUnit testing, Selenium automation, and Swagger API documentation.

---

## ✨ What's New

| Feature | Implementation |
|---|---|
| 🔐 Login Framework | Spring Security with JWT access and refresh tokens |
| 🛡️ Password Brute-Force Protection | Login rate limiting after repeated failed attempts |
| 🔒 Password Protection | BCrypt password hashing |
| 🧪 JUnit Test Cases | Backend unit, security, and integration tests |
| 🤖 Selenium Automation | Automated browser-based UI testing |
| 📘 Swagger Documentation | Interactive OpenAPI documentation |

---

## 🛠️ Tech Stack

| Layer | Technology |
|---|---|
| Frontend | React, Vite, JavaScript, HTML, CSS |
| Backend | Java 17, Spring Boot, Spring Security, REST API |
| Storage | Local file-based H2 database |
| Authentication | JWT access token and refresh token |
| Password Security | BCrypt |
| Unit Testing | JUnit 5 with Java |
| API Testing | REST Assured with Java |
| UI Automation | Selenium WebDriver with Java |
| API Documentation | Springdoc OpenAPI and Swagger UI |
| Build Tool | Maven Wrapper |

---

## 📁 Project Structure

```text
smart-expense-tracker-complete/
│
├── .mvn/
├── pom.xml
├── mvnw
├── mvnw.cmd
├── build-frontend.cmd
├── run-backend-tests.cmd
├── run-api-tests.cmd
├── run-ui-tests.cmd
├── setup-frontend.cmd
├── start-backend.cmd
├── start-frontend.cmd
│
├── src/
│   ├── backend/
│   │   └── src/
│   │       ├── main/
│   │       │   ├── java/com/example/expensetracker/
│   │       │   │   └── ExpenseTrackerApplication.java
│   │       │   └── resources/
│   │       │       └── application.yml
│   │       └── test/java/
│   │
│   └── frontend/
│       ├── package.json
│       └── src/
│
└── tests/
    └── automation/
        └── src/test/java/com/example/expensetracker/automation/
            ├── api/
            └── ui/
```

---

# 🚀 Installation and Setup

## 1️⃣ Install the Required Software

Install the following tools before running the project:

```text
Java JDK 17
Node.js and npm
Google Chrome
Eclipse IDE for Java Developers
Git
```

Verify Java:

```bat
java -version
```

Verify Node.js:

```bat
node -v
```

Verify npm:

```bat
npm -v
```

A separate Maven installation is not required because the project already contains:

```text
mvnw.cmd
```

---

## 2️⃣ Download the Project

### Option A — Clone from GitHub

Open Command Prompt:

```bat
git clone <your-github-repository-url>
cd smart-expense-tracker-complete
```

### Option B — Download as ZIP

1. Download the project ZIP from GitHub.
2. Extract the ZIP file.
3. Open the extracted folder.

Project root path:

```text
smart-expense-tracker-complete/
```

The root folder must contain:

```text
pom.xml
mvnw.cmd
src/
tests/
```

---

## 3️⃣ Import the Project into Eclipse

1. Open Eclipse IDE.
2. Click:

```text
File → Import
```

3. Select:

```text
Maven → Existing Maven Projects
```

4. Click **Next**.
5. Select the project root folder:

```text
smart-expense-tracker-complete/
```

6. Confirm that Eclipse detects:

```text
pom.xml
```

7. Click **Finish**.
8. Right-click the imported project.
9. Select:

```text
Maven → Update Project
```

10. Enable:

```text
Force Update of Snapshots/Releases
```

11. Click **OK**.

---

## 4️⃣ Install Frontend Dependencies

Open Command Prompt in the project root:

```text
smart-expense-tracker-complete/
```

Run:

```bat
cd src\frontend
npm install
cd ..\..
```

This step is normally required only the first time.

---

# ▶️ Run the Application

## 5️⃣ Run the Backend

### Backend Main File Path

```text
src/backend/src/main/java/com/example/expensetracker/ExpenseTrackerApplication.java
```

### Run Using Eclipse

1. Open `ExpenseTrackerApplication.java`.
2. Right-click the file.
3. Select:

```text
Run As → Java Application
```

Wait until the Eclipse Console displays:

```text
Tomcat started on port 8080
Started ExpenseTrackerApplication
```

### Run Using Command Prompt

Open Command Prompt in:

```text
smart-expense-tracker-complete/
```

Run:

```bat
mvnw.cmd spring-boot:run
```

Backend URL:

```text
http://localhost:8080
```

> Keep the backend running while using the frontend, Selenium tests, REST Assured tests, or Swagger.

---

## 6️⃣ Run the Frontend

### Frontend Folder Path

```text
src/frontend/
```

Open a second Command Prompt in the project root:

```bat
cd src\frontend
npm run dev
```

The terminal will display:

```text
http://localhost:5173/
```

Hold **Ctrl** and click the URL to open the application in the browser.

> Keep the frontend terminal running while using the application or running Selenium tests.

---

# 🧪 Testing

## 7️⃣ Run JUnit Tests

### JUnit Test Path

```text
src/backend/src/test/java/
```

Open Command Prompt in the project root:

```text
smart-expense-tracker-complete/
```

Run:

```bat
mvnw.cmd clean test
```

This command runs the backend JUnit test cases.

---

## 8️⃣ Run Selenium Automation

Before running Selenium automation, start both:

```text
Backend:  http://localhost:8080
Frontend: http://localhost:5173
```

### Selenium Test Path

```text
tests/automation/src/test/java/com/example/expensetracker/automation/ui/
```

Open Command Prompt in the project root and run:

```bat
mvnw.cmd clean test -Pui-tests
```

### One-Click Selenium Execution

Double-click:

```text
run-ui-tests.cmd
```

This runs all Selenium UI automation test cases.

---

# 📘 Swagger / OpenAPI

## 9️⃣ Open Swagger UI

Start the backend first.

Open this URL in the browser:

```text
http://localhost:8080/swagger-ui.html
```

OpenAPI JSON:

```text
http://localhost:8080/v3/api-docs
```

You can also double-click:

```text
open-swagger.cmd
```

### Test Protected APIs

1. Open `POST /api/auth/login`.
2. Click **Try it out**.
3. Enter valid credentials.
4. Click **Execute**.
5. Copy the returned `accessToken`.
6. Click **Authorize** at the top.
7. Paste the token.
8. Click **Authorize**.
9. Test the protected expense endpoints.

---

## ✅ Recommended Running Order

```text
1. Start the backend
2. Start the frontend
3. Open http://localhost:5173/
4. Run JUnit tests
5. Run Selenium automation
6. Open Swagger UI
```

---

## 🏁 Conclusion

Smart Expense Tracker provides secure authentication, expense management, local data storage, automated testing, and API documentation in one complete full-stack project. The application can be imported into Eclipse and operated using either Eclipse or Command Prompt with clearly separated backend, frontend, testing, and Swagger workflows.

---

<div align="center">

### Built with Java, Spring Boot, React, JUnit, Selenium, and Swagger

</div>
