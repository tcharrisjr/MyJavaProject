# React + Vite

This template provides a minimal setup to get React working in Vite with HMR and some ESLint rules.

Currently, two official plugins are available:

- [@vitejs/plugin-react](https://github.com/vitejs/vite-plugin-react/blob/main/packages/plugin-react) uses [Oxc](https://oxc.rs)
- [@vitejs/plugin-react-swc](https://github.com/vitejs/vite-plugin-react/blob/main/packages/plugin-react-swc) uses [SWC](https://swc.rs/)

## React Compiler

The React Compiler is not enabled on this template because of its impact on dev & build performances. To add it, see [this documentation](https://react.dev/learn/react-compiler/installation).

## Expanding the ESLint configuration

If you are developing a production application, we recommend using TypeScript with type-aware lint rules enabled. Check out the [TS template](https://github.com/vitejs/vite/tree/main/packages/create-vite/template-react-ts) for information on how to integrate TypeScript and [`typescript-eslint`](https://typescript-eslint.io) in your project.

# Project Manager React Client

Frontend client for the **Project Manager Application**.

The application provides a React-based user interface for managing projects and tasks. It communicates with the Project Manager Spring Boot REST API and uses JWT authentication to secure application resources.

---

## Technology Stack

### Frontend

- React
- Vite
- JavaScript / JSX
- React Router
- CSS
- Fetch API
- JWT Authentication

### Testing

- Vitest
- React Testing Library
- `@testing-library/jest-dom`
- `@testing-library/user-event`
- jsdom

### Backend

The React client communicates with a separate Spring Boot REST API.

Default development backend:

```text
http://localhost:8080
```

Default React/Vite development server:

```text
http://localhost:5173
```

---

# Application Features

The React client supports:

- User registration
- User login
- JWT authentication
- Protected routes
- Session restoration
- Logout
- Project dashboard
- Create projects
- Edit projects
- Delete projects
- Select projects
- Project health statistics
- Task statistics
- Create tasks
- Edit tasks
- Delete tasks
- Update task status
- Mark tasks complete
- Task priorities
- Due dates
- Task searching
- Task filtering
- Task sorting
- Server-side pagination
- Loading states
- Success messages
- Error handling

---

# Prerequisites

The following software should be installed before running the React client.

## Node.js

Install a current Node.js LTS release.

Verify the installation:

```powershell
node --version
```

Example:

```text
v22.x.x
```

## npm

npm is installed with Node.js.

Verify:

```powershell
npm --version
```

---

# Project Location

Example Windows development location:

```text
C:\MyJavaProject\fullstack-react
```

Navigate to the project:

```powershell
cd C:\MyJavaProject\fullstack-react
```

---

# Install Dependencies

From the React project directory run:

```powershell
npm install
```

This installs the packages defined in:

```text
package.json
```

The installed packages are placed in:

```text
node_modules
```

---

# Environment Configuration

The React application communicates with the Spring Boot backend.

Create a file named:

```text
.env
```

in the root of the React project.

Example:

```env
VITE_API_BASE_URL=http://localhost:8080
```

The resulting architecture is:

```text
React
http://localhost:5173

        |
        | HTTP / REST / JWT
        v

Spring Boot
http://localhost:8080

        |
        v

SQL Server
```

---

# Production Environment Configuration

For production, create:

```text
.env.production
```

Example:

```env
VITE_API_BASE_URL=https://api.example.com
```

Vite environment variables exposed to browser code must begin with:

```text
VITE_
```

Do NOT store the following in React environment files:

- Database passwords
- JWT signing secrets
- Private keys
- Server credentials
- SQL Server credentials
- Other confidential server secrets

Frontend environment variables are visible to browser users after the application is built.

---

# Start the Development Server

Navigate to the React application:

```powershell
cd C:\MyJavaProject\fullstack-react
```

Start Vite:

```powershell
npm run dev
```

The application normally starts at:

```text
http://localhost:5173
```

Open that address in a browser.

---

# Vite Command-Line Arguments

Arguments passed through an npm script must appear after:

```text
--
```

## Start on Port 5173

```powershell
npm run dev -- --port 5173
```

## Start on Port 5176

```powershell
npm run dev -- --port 5176
```

## Allow Network Access

```powershell
npm run dev -- --host 0.0.0.0
```

## Specify Host and Port

```powershell
npm run dev -- --host 0.0.0.0 --port 5173
```

## Require a Specific Port

```powershell
npm run dev -- --port 5173 --strictPort
```

If port `5173` is already being used, Vite will fail instead of automatically selecting another port.

---

# npm Commands

## Start Development Server

```powershell
npm run dev
```

## Create Production Build

```powershell
npm run build
```

## Preview Production Build

```powershell
npm run preview
```

## Run ESLint

```powershell
npm run lint
```

## Run Tests

```powershell
npm test
```

or:

```powershell
npm run test
```

## Run Tests Once

```powershell
npm run test:run
```

This is useful for automated builds and CI/CD pipelines.

---

# Automated Testing

The frontend uses:

- Vitest
- React Testing Library
- jest-dom
- user-event
- jsdom

Current automated testing includes:

- Frontend smoke tests
- Authentication context
- Protected routes
- Dashboard rendering
- Project loading
- Project selection
- Project health
- Task loading
- Empty states
- API error handling
- Project CRUD behavior

Run the complete frontend test suite:

```powershell
npm run test:run
```

---

# Run Individual Test Files

Run the authentication tests:

```powershell
npx vitest run src/test/AuthContext.test.jsx
```

Run protected-route tests:

```powershell
npx vitest run src/test/ProtectedRoute.test.jsx
```

Run Dashboard tests:

```powershell
npx vitest run src/test/Dashboard.test.jsx
```

Run the frontend smoke tests:

```powershell
npx vitest run src/test/FrontendSmoke.test.jsx
```

---

# Starting the Complete Application

The Spring Boot backend and React frontend run as separate applications during development.

## Terminal 1 — Start Spring Boot

Open PowerShell:

```powershell
cd C:\MyJavaProject\fullstack
```

Run:

```powershell
.\mvnw.cmd spring-boot:run
```

The backend normally starts at:

```text
http://localhost:8080
```

---

## Terminal 2 — Start React

Open another PowerShell window:

```powershell
cd C:\MyJavaProject\fullstack-react
```

Run:

```powershell
npm run dev
```

The frontend normally starts at:

```text
http://localhost:5173
```

Open:

```text
http://localhost:5173
```

---

# Authentication Architecture

The application uses JWT authentication.

Typical authentication flow:

```text
User
 |
 v
React Login Page
 |
 v
POST /api/auth/login
 |
 v
Spring Security
 |
 v
Validate Credentials
 |
 v
Generate JWT
 |
 v
Return JWT to React
 |
 v
Store Authentication Token
 |
 v
Authorization: Bearer <token>
 |
 v
Protected REST API
```

Authenticated requests contain:

```http
Authorization: Bearer <token>
```

The React authentication layer handles:

- Login
- Registration
- Token storage
- Session restoration
- Current-user restoration
- Logout
- Unauthorized responses

---

# Backend API Endpoints

The React client communicates with the Spring Boot backend.

## Authentication

```text
POST /api/auth/register
POST /api/auth/login
GET  /api/auth/me
```

## Projects

```text
GET    /api/projects
GET    /api/projects/{id}
POST   /api/projects
PUT    /api/projects/{id}
DELETE /api/projects/{id}
```

## Project Health

```text
GET /api/projects/{id}/health
```

## Tasks

```text
GET    /api/projects/{projectId}/tasks
POST   /api/projects/{projectId}/tasks
PUT    /api/projects/{projectId}/tasks/{taskId}
DELETE /api/projects/{projectId}/tasks/{taskId}
```

## Paginated Tasks

```text
GET /api/projects/{projectId}/tasks/page
```

Supported query parameters include:

```text
page
size
status
priority
search
dueDateFilter
sortBy
sortDirection
```

Example request:

```text
/api/projects/1/tasks/page?page=0&size=10&status=ALL&priority=ALL
```

---

# Production Build

Before deploying the React application, create an optimized production build.

Run:

```powershell
npm run build
```

Vite creates:

```text
dist\
```

The `dist` directory contains the production application.

---

# Preview the Production Build

After building:

```powershell
npm run preview
```

This allows the production bundle to be tested locally before deployment.

Recommended sequence:

```powershell
npm run test:run
npm run build
npm run preview
```

---

# Deployment Process

The React/Vite client becomes a static web application after the production build.

Typical deployment process:

```text
Source Code
    |
    v
npm ci
    |
    v
Run Tests
    |
    v
npm run build
    |
    v
dist/
    |
    v
Web Server / CDN
```

The `dist` directory can be deployed to:

- IIS
- Nginx
- Apache
- AWS S3
- AWS CloudFront
- Azure Static Web Apps
- Azure Storage
- Netlify
- Vercel
- Cloudflare Pages
- Docker

---

# Deployment with IIS

Create the production build:

```powershell
npm ci
npm run test:run
npm run build
```

Copy the contents of:

```text
dist\
```

to the IIS website directory.

Because React Router performs client-side routing, IIS must be configured so frontend routes fall back to:

```text
index.html
```

For example, refreshing:

```text
/dashboard
```

must load:

```text
index.html
```

and allow React Router to process the route.

API requests intended for Spring Boot should NOT be rewritten to `index.html`.

---

# Deployment with Nginx

Build the application:

```powershell
npm ci
npm run test:run
npm run build
```

Copy the `dist` directory to the web server.

A typical React SPA Nginx configuration contains:

```nginx
location / {
    try_files $uri $uri/ /index.html;
}
```

This ensures browser refreshes on React routes work correctly.

---

# Docker Deployment

The React application can also be deployed using Docker.

Example:

```dockerfile
FROM node:22-alpine AS build

WORKDIR /app

COPY package*.json ./

RUN npm ci

COPY . .

RUN npm run build


FROM nginx:alpine

COPY --from=build /app/dist /usr/share/nginx/html

EXPOSE 80

CMD ["nginx", "-g", "daemon off;"]
```

Build the image:

```powershell
docker build -t project-manager-client .
```

Run the container:

```powershell
docker run -p 8081:80 project-manager-client
```

Open:

```text
http://localhost:8081
```

---

# CORS Configuration

The Spring Boot backend must allow the React frontend origin.

For local development:

```text
http://localhost:5173
```

If Vite runs on:

```text
http://localhost:5176
```

that is a different origin.

The backend CORS configuration must permit the frontend origin being used.

Production example:

```text
https://projects.example.com
```

---

# Clean Installation

If dependency problems occur, remove `node_modules`:

```powershell
Remove-Item -Recurse -Force node_modules
```

Then reinstall:

```powershell
npm install
```

Normally, `package-lock.json` should remain committed to source control.

If dependency resolution itself is corrupted, the lock file can also be regenerated deliberately.

---

# CI/CD Build

A typical CI/CD pipeline should execute:

```powershell
npm ci
npm run lint
npm run test:run
npm run build
```

Pipeline:

```text
Install Dependencies
        |
        v
       Lint
        |
        v
       Test
        |
        v
       Build
        |
        v
      Deploy
```

`npm ci` is recommended for automated builds because it uses the exact dependency versions stored in:

```text
package-lock.json
```

---

# Troubleshooting

## npm Is Not Recognized

Run:

```powershell
node --version
npm --version
```

If either command fails, install or repair Node.js.

---

## Port 5173 Is Already Being Used

Start Vite on another port:

```powershell
npm run dev -- --port 5176
```

Or force Vite to require port 5173:

```powershell
npm run dev -- --port 5173 --strictPort
```

---

## Frontend Cannot Connect to Backend

Verify Spring Boot is running:

```text
http://localhost:8080
```

Verify the frontend environment configuration:

```env
VITE_API_BASE_URL=http://localhost:8080
```

Also verify Spring Security/CORS configuration.

---

## HTTP 401 Unauthorized

Possible causes:

- User is not logged in
- JWT is missing
- JWT expired
- JWT is invalid
- Authentication token was cleared
- Backend rejected authentication

Log in again and inspect the browser Network tab if the problem continues.

---

## CORS Error

Verify that Spring Boot allows the exact frontend origin.

For example:

```text
http://localhost:5173
```

and:

```text
http://localhost:5176
```

are different origins.

---

## Production Route Returns 404

If refreshing a route such as:

```text
/dashboard
```

returns `404`, configure the web server for SPA fallback routing.

Unknown frontend routes should normally return:

```text
index.html
```

React Router will then process the URL.

---

# Project Structure

Typical React client structure:

```text
fullstack-react/
|
|-- public/
|
|-- src/
|   |
|   |-- api/
|   |   |-- apiClient.js
|   |   |-- authApi.js
|   |
|   |-- components/
|   |   |-- AppHeader.jsx
|   |   |-- ProtectedRoute.jsx
|   |
|   |-- context/
|   |   |-- AuthContext.jsx
|   |   |-- useAuth.js
|   |
|   |-- pages/
|   |   |-- Dashboard.jsx
|   |   |-- ProjectDetails.jsx
|   |
|   |-- test/
|   |   |-- setup.js
|   |   |-- FrontendSmoke.test.jsx
|   |   |-- AuthContext.test.jsx
|   |   |-- ProtectedRoute.test.jsx
|   |   |-- Dashboard.test.jsx
|   |
|   |-- App.jsx
|   |-- main.jsx
|   |-- Dashboard.css
|
|-- .env
|-- .env.production
|-- package.json
|-- package-lock.json
|-- vite.config.js
|-- README.md
```

---

# Pre-Deployment Checklist

Before deploying:

```powershell
npm ci
npm run lint
npm run test:run
npm run build
```

Optionally preview:

```powershell
npm run preview
```

Perform a final smoke test of:

- Login
- Logout
- Protected routes
- Dashboard
- Project creation
- Project editing
- Project deletion
- Project selection
- Task creation
- Task editing
- Task deletion
- Task status updates
- Task filtering
- Task sorting
- Task pagination
- JWT authentication
- Browser refresh on protected routes

---

# Application Architecture

```text
+---------------------------+
|         Browser           |
+-------------+-------------+
              |
              v
+---------------------------+
|      React / Vite         |
|     localhost:5173        |
+-------------+-------------+
              |
              | REST / JSON
              | JWT
              v
+---------------------------+
|       Spring Boot         |
|     localhost:8080        |
+-------------+-------------+
              |
              | JPA / Hibernate
              v
+---------------------------+
|        SQL Server         |
+---------------------------+
```

The React application does **not** connect directly to SQL Server.

All database access is performed through the Spring Boot backend.

---

# Quick Start

## Start Backend

```powershell
cd C:\MyJavaProject\fullstack

.\mvnw.cmd spring-boot:run
```

## Start Frontend

Open another PowerShell window:

```powershell
cd C:\MyJavaProject\fullstack-react

npm install

npm run dev
```

Open:

```text
http://localhost:5173
```

---

# Production Quick Start

Run:

```powershell
cd C:\MyJavaProject\fullstack-react

npm ci

npm run lint

npm run test:run

npm run build
```

The deployable application will be located in:

```text
dist\
```

Deploy the contents of `dist` to the production web server.