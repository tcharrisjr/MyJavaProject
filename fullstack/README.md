Yes. The cleanest approach is to have **one root README.md** that documents both the Spring Boot backend and the React client in a single place.

You can copy everything below and replace your existing root README with it.

````markdown
# FullStack Project Management Application

Full-stack project management application built with a Spring Boot backend and a React frontend.

The application provides:

- JWT authentication
- Project management
- Task management
- Project health metrics
- Dashboard statistics
- Search
- Filtering
- Sorting
- Pagination
- Protected routes
- SQL Server persistence
- H2-based integration testing

---

# Technology Stack

## Backend

- Java 21
- Spring Boot 4.1.0
- Spring Web MVC
- Spring Security
- Spring Data JPA
- Hibernate
- JWT Authentication
- SQL Server
- H2 for integration tests
- Maven
- JUnit 5
- Mockito
- MockMvc

## Frontend

- React
- Vite
- React Router
- JavaScript / JSX
- CSS
- Fetch API
- Vitest
- React Testing Library
- Jest DOM
- ESLint

---

# Project Structure

```text
C:\MyJavaProject
│
├── fullstack
│   │
│   ├── src
│   │   ├── main
│   │   │   ├── java
│   │   │   │   └── fullstack
│   │   │   │       ├── controller
│   │   │   │       ├── dto
│   │   │   │       ├── exception
│   │   │   │       ├── model
│   │   │   │       ├── repository
│   │   │   │       ├── security
│   │   │   │       ├── service
│   │   │   │       └── FullstackApplication.java
│   │   │   │
│   │   │   └── resources
│   │   │       ├── application.properties
│   │   │       └── application-prod.properties
│   │   │
│   │   └── test
│   │       ├── java
│   │       └── resources
│   │           └── application-test.properties
│   │
│   ├── pom.xml
│   ├── mvnw
│   └── mvnw.cmd
│
└── fullstack-react
    │
    ├── src
    │   ├── api
    │   ├── components
    │   ├── context
    │   ├── pages
    │   └── test
    │
    ├── public
    ├── .env.production
    ├── package.json
    ├── vite.config.js
    └── eslint.config.js
````

---

# Prerequisites

Install the following:

* Java 21
* Node.js
* npm
* SQL Server
* Git
* PowerShell

Maven does not need to be installed separately because the backend includes the Maven Wrapper.

---

# Java Configuration

Verify Java:

```powershell
java -version
```

Expected:

```text
openjdk version "21"
```

Verify Maven Wrapper:

```powershell
cd C:\MyJavaProject\fullstack
.\mvnw -version
```

If required, set `JAVA_HOME` permanently:

```powershell
[Environment]::SetEnvironmentVariable(
    "JAVA_HOME",
    "C:\Program Files\Eclipse Adoptium\jdk-21.0.12.8-hotspot",
    "User"
)
```

Close and reopen PowerShell after changing permanent environment variables.

Verify:

```powershell
$env:JAVA_HOME
java -version
.\mvnw -version
```

---

# Backend Development Configuration

Backend project:

```text
C:\MyJavaProject\fullstack
```

Development configuration:

```text
src\main\resources\application.properties
```

The development application uses SQL Server.

Example JDBC URL:

```text
jdbc:sqlserver://SERVER_NAME:1422;databaseName=FullStackDemo;encrypt=true;trustServerCertificate=true
```

---

# Development Database

The application uses the SQL Server database:

```text
FullStackDemo
```

Example SQL login:

```text
fullstackuser
```

Test the SQL Server connection:

```powershell
sqlcmd `
  -S tcp:SERVER_NAME,1422 `
  -d FullStackDemo `
  -U fullstackuser `
  -P "YOUR_PASSWORD"
```

If successful:

```text
1>
```

Verify:

```sql
SELECT DB_NAME();
GO
```

Exit:

```sql
EXIT
```

---

# Run the Backend in Development

```powershell
cd C:\MyJavaProject\fullstack
.\mvnw spring-boot:run
```

The backend runs at:

```text
http://localhost:8080
```

Successful startup should include:

```text
Tomcat started on port 8080
Started FullstackApplication
```

---

# Port 8080 Already in Use

Check the process:

```powershell
netstat -ano | findstr :8080
```

Identify the process:

```powershell
Get-Process -Id PROCESS_ID
```

Inspect it:

```powershell
Get-CimInstance Win32_Process `
    -Filter "ProcessId = PROCESS_ID" |
Select-Object ProcessId, Name, CommandLine
```

Stop an obsolete process:

```powershell
Stop-Process -Id PROCESS_ID -Force
```

---

# Backend Test Configuration

Integration tests use the `test` Spring profile.

Test configuration:

```text
src\test\resources\application-test.properties
```

Integration tests use an H2 in-memory database instead of SQL Server.

Example test configuration:

```properties
spring.datasource.url=jdbc:h2:mem:fullstacktest;DB_CLOSE_DELAY=-1;DB_CLOSE_ON_EXIT=FALSE

spring.datasource.driver-class-name=org.h2.Driver

spring.datasource.username=sa

spring.datasource.password=

spring.jpa.database-platform=org.hibernate.dialect.H2Dialect

spring.jpa.hibernate.ddl-auto=create-drop

spring.jpa.show-sql=false

spring.jpa.open-in-view=false

app.jwt.secret=VGhpcy1pcy1hLXRlc3Qtc2VjcmV0LWtleS10aGF0LWlzLWxvbmc=

app.jwt.expiration-ms=86400000

app.cors.allowed-origins=http://localhost:5173
```

Spring integration tests use:

```java
@ActiveProfiles("test")
```

This keeps tests independent from:

* SQL Server availability
* SQL Server credentials
* production environment variables
* production configuration

---

# Run Backend Tests

```powershell
cd C:\MyJavaProject\fullstack
.\mvnw clean test
```

Current validated baseline:

```text
Tests run: 49
Failures: 0
Errors: 0
Skipped: 0
BUILD SUCCESS
```

---

# Backend Test Categories

The backend test suite includes:

* Application context tests
* Project controller integration tests
* Task controller integration tests
* Spring Security integration tests
* Project service unit tests
* Task service unit tests

---

# Frontend Setup

Frontend project:

```text
C:\MyJavaProject\fullstack-react
```

Install dependencies:

```powershell
cd C:\MyJavaProject\fullstack-react
npm install
```

For a clean install:

```powershell
npm ci
```

---

# Run the React Development Client

Make sure the backend is already running on:

```text
http://localhost:8080
```

Then:

```powershell
cd C:\MyJavaProject\fullstack-react
npm run dev
```

Vite will display a URL such as:

```text
http://localhost:5173
```

If that port is occupied, Vite may use:

```text
http://localhost:5174
http://localhost:5175
http://localhost:5176
```

---

# Frontend API Configuration

The frontend reads:

```javascript
import.meta.env.VITE_API_BASE_URL
```

and falls back to:

```text
http://localhost:8080
```

Production configuration is stored in:

```text
.env.production
```

For local production validation:

```properties
VITE_API_BASE_URL=http://localhost:8080
```

For deployment:

```properties
VITE_API_BASE_URL=https://api.example.com
```

Do not store secrets in Vite environment files.

Anything beginning with:

```text
VITE_
```

is exposed to browser JavaScript.

Never place the following in the React project:

```text
DB_PASSWORD
JWT_SECRET
SQL Server credentials
private server-side API keys
```

---

# Authentication

Authentication uses JWT bearer tokens.

Authenticated API calls send:

```text
Authorization: Bearer <JWT>
```

Authentication state is managed with React context.

Protected routes use:

```text
ProtectedRoute
```

When the backend returns HTTP `401`, the frontend clears the stored token and emits:

```text
auth:unauthorized
```

The authenticated user state is then cleared.

---

# Frontend Lint

```powershell
cd C:\MyJavaProject\fullstack-react
npm run lint
```

A release candidate should have no ESLint errors.

---

# Frontend Tests

Run:

```powershell
npm run test:run
```

Current validated baseline:

```text
Test Files  4 passed
Tests       50 passed
```

The frontend tests include:

* Smoke tests
* Protected route tests
* Authentication context tests
* Dashboard tests

Some Dashboard tests intentionally simulate failures and may print messages such as:

```text
Unable to load projects
Unable to save project
Unable to save task
```

These are expected when the final test result reports all tests passed.

---

# Production Frontend Build

```powershell
cd C:\MyJavaProject\fullstack-react
npm run build
```

The production artifact is generated in:

```text
dist\
```

Example:

```text
dist
│
├── index.html
└── assets
    ├── index-*.css
    └── index-*.js
```

---

# Preview the Production React Build

```powershell
npm run preview
```

Typical URL:

```text
http://localhost:4173
```

If the port is occupied:

```text
http://localhost:4174
```

The Vite preview server is for validation only and is not normally used as the production web server.

---

# CORS Configuration

The Spring Boot backend controls allowed frontend origins using:

```text
APP_CORS_ALLOWED_ORIGINS
```

For local development:

```text
http://localhost:5173
http://localhost:5174
http://localhost:5175
http://localhost:5176
```

For local production preview:

```text
http://localhost:4173
http://localhost:4174
```

Example:

```powershell
$env:APP_CORS_ALLOWED_ORIGINS = "http://localhost:4174"
```

Production example:

```powershell
$env:APP_CORS_ALLOWED_ORIGINS = "https://your-frontend.example.com"
```

Multiple origins may be comma-separated.

---

# Production Backend Configuration

Production configuration:

```text
src\main\resources\application-prod.properties
```

Activate the production profile:

```powershell
$env:SPRING_PROFILES_ACTIVE = "prod"
```

---

# Production Environment Variables

## Database URL

```powershell
$env:DB_URL = "jdbc:sqlserver://SERVER_NAME:1422;databaseName=FullStackDemo;encrypt=true;trustServerCertificate=true"
```

## Database Username

```powershell
$env:DB_USERNAME = "fullstackuser"
```

## Database Password

```powershell
$env:DB_PASSWORD = "YOUR_DATABASE_PASSWORD"
```

## JWT Secret

Generate a secure 256-bit Base64 key:

```powershell
$rng = [System.Security.Cryptography.RandomNumberGenerator]::Create()

$bytes =
    New-Object byte[] 32

$rng.GetBytes(
    $bytes
)

$rng.Dispose()

$env:JWT_SECRET =
    [Convert]::ToBase64String(
        $bytes
    )
```

Verify:

```powershell
[Convert]::FromBase64String(
    $env:JWT_SECRET
).Length
```

Expected:

```text
32
```

## JWT Expiration

Default:

```text
86400000
```

This equals 24 hours.

Override:

```powershell
$env:JWT_EXPIRATION_MS = "86400000"
```

## CORS

```powershell
$env:APP_CORS_ALLOWED_ORIGINS = "https://your-frontend.example.com"
```

---

# Run Backend With Production Profile

Example:

```powershell
$env:SPRING_PROFILES_ACTIVE = "prod"

$env:DB_URL = "jdbc:sqlserver://SERVER_NAME:1422;databaseName=FullStackDemo;encrypt=true;trustServerCertificate=true"

$env:DB_USERNAME = "fullstackuser"

$env:DB_PASSWORD = "YOUR_DATABASE_PASSWORD"

$env:JWT_SECRET = "YOUR_BASE64_JWT_SECRET"

$env:APP_CORS_ALLOWED_ORIGINS = "http://localhost:4174"

cd C:\MyJavaProject\fullstack

.\mvnw spring-boot:run
```

Successful production startup should include:

```text
The following 1 profile is active: "prod"

HikariPool-1 - Start completed

Initialized JPA EntityManagerFactory

Tomcat started on port 8080

Started FullstackApplication
```

---

# Production Database Schema

Development may use:

```properties
spring.jpa.hibernate.ddl-auto=update
```

Production should normally use:

```properties
spring.jpa.hibernate.ddl-auto=validate
```

Production schema changes should ideally be performed using controlled migrations such as:

* Flyway
* Liquibase

---

# Package the Backend

```powershell
cd C:\MyJavaProject\fullstack
.\mvnw clean package
```

The command runs tests before packaging.

Expected:

```text
BUILD SUCCESS
```

The deployable JAR is created under:

```text
target\
```

Example:

```text
target\fullstack-0.0.1-SNAPSHOT.jar
```

List generated JARs:

```powershell
Get-ChildItem .\target\*.jar
```

---

# Run the Packaged JAR

Development-style execution:

```powershell
java -jar .\target\fullstack-0.0.1-SNAPSHOT.jar
```

Production execution:

```powershell
$env:SPRING_PROFILES_ACTIVE = "prod"

$env:DB_URL = "jdbc:sqlserver://SERVER_NAME:1422;databaseName=FullStackDemo;encrypt=true;trustServerCertificate=true"

$env:DB_USERNAME = "fullstackuser"

$env:DB_PASSWORD = "YOUR_DATABASE_PASSWORD"

$env:JWT_SECRET = "YOUR_BASE64_JWT_SECRET"

$env:APP_CORS_ALLOWED_ORIGINS = "https://your-frontend.example.com"

java -jar .\target\fullstack-0.0.1-SNAPSHOT.jar
```

---

# Deployment Artifacts

Backend:

```text
C:\MyJavaProject\fullstack\target\fullstack-0.0.1-SNAPSHOT.jar
```

Frontend:

```text
C:\MyJavaProject\fullstack-react\dist\
```

---

# Frontend Deployment

Deploy the contents of:

```text
dist\
```

to a static web host or web server.

Possible options include:

* IIS
* Nginx
* Apache
* Azure Static Web Apps
* AWS S3 / CloudFront
* Cloudflare Pages
* Netlify
* Vercel
* containerized Nginx

---

# React Router SPA Requirement

Because the application uses React Router, the production web server must route unknown frontend paths back to:

```text
index.html
```

For example:

```text
/projects/123
```

must load the React application instead of returning HTTP 404.

Example Nginx configuration:

```nginx
server {
    listen 80;

    server_name your-frontend.example.com;

    root /var/www/fullstack-react;

    index index.html;

    location / {
        try_files $uri $uri/ /index.html;
    }
}
```

---

# End-to-End Development Startup

## Terminal 1 — Backend

```powershell
cd C:\MyJavaProject\fullstack
.\mvnw spring-boot:run
```

Backend:

```text
http://localhost:8080
```

## Terminal 2 — Frontend

```powershell
cd C:\MyJavaProject\fullstack-react
npm run dev
```

Open the URL shown by Vite.

---

# End-to-End Production-Style Local Validation

## Backend

```powershell
$env:SPRING_PROFILES_ACTIVE = "prod"

$env:DB_URL = "jdbc:sqlserver://SERVER_NAME:1422;databaseName=FullStackDemo;encrypt=true;trustServerCertificate=true"

$env:DB_USERNAME = "fullstackuser"

$env:DB_PASSWORD = "YOUR_DATABASE_PASSWORD"

$env:JWT_SECRET = "YOUR_BASE64_JWT_SECRET"

$env:APP_CORS_ALLOWED_ORIGINS = "http://localhost:4174"

cd C:\MyJavaProject\fullstack

.\mvnw spring-boot:run
```

## Frontend

```powershell
cd C:\MyJavaProject\fullstack-react

npm run build

npm run preview
```

Open the Vite preview URL.

---

# Final Release Validation

## Backend

```powershell
cd C:\MyJavaProject\fullstack

.\mvnw clean test

.\mvnw clean package
```

Expected baseline:

```text
49 tests passed
BUILD SUCCESS
```

## Frontend

```powershell
cd C:\MyJavaProject\fullstack-react

npm run lint

npm run test:run

npm run build
```

Expected baseline:

```text
4 test files passed
50 tests passed
Vite production build successful
```

---

# End-to-End Smoke Test

Before deployment verify:

1. Register
2. Login
3. Restore authenticated session
4. Dashboard loads
5. Projects load
6. Create project
7. Edit project
8. Select project
9. Create task
10. Edit task
11. Change task status
12. Search tasks
13. Filter tasks
14. Sort tasks
15. Test pagination
16. Verify project health metrics
17. Delete task
18. Delete project
19. Logout
20. Login again

---

# Security Notes

Do not commit:

* Production database passwords
* Production JWT secrets
* Private API keys
* Server credentials

Do not place backend secrets in React environment files.

Use environment variables or a proper deployment secret store.

If a database password or JWT signing secret has been exposed, rotate it before production deployment.

Changing the JWT signing key invalidates previously issued JWT tokens.

---

# Source Control Recommendations

Do not commit:

```text
node_modules/
dist/
.env.local
.env.*.local
target/
```

A `.env.production` file may contain a public frontend API URL, but it must not contain secrets.

---

# Current Validation Baseline

Backend:

```text
Tests: 49
Failures: 0
Errors: 0
Skipped: 0
```

Frontend:

```text
Test Files: 4 passed
Tests: 50 passed
```

Frontend production build:

```text
Vite build successful
```

The application has also been validated with:

* Spring Boot production profile
* SQL Server
* JWT authentication
* configurable CORS
* Vite production preview
* H2 isolated integration testing

```

This single README can live at `C:\MyJavaProject\README.md`, or you can paste it into either project's existing `README.md` if you want one canonical document duplicated in both repositories.
```
