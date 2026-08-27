import {
  BrowserRouter,
  Route,
  Routes,
} from "react-router";

import AppHeader from "./components/AppHeader";
import ProtectedRoute from "./components/ProtectedRoute";

import {
  AuthProvider,
} from "./context/AuthContext";

import Dashboard from "./pages/Dashboard";
import Login from "./pages/Login";
import ProjectDetails from "./pages/ProjectDetails";
import Register from "./pages/Register";

import "./App.css";

/*
 * =========================================================
 * PROTECTED PAGE WRAPPER
 * =========================================================
 */

function ProtectedPage({
  children,
}) {
  return (
    <ProtectedRoute>

      <AppHeader />

      {children}

    </ProtectedRoute>
  );
}

function App() {
  return (
    <BrowserRouter>

      <AuthProvider>

        <Routes>

          {/* ===============================================
              PUBLIC
              =============================================== */}

          <Route
            path="/login"
            element={
              <Login />
            }
          />

          <Route
            path="/register"
            element={
              <Register />
            }
          />

          {/* ===============================================
              PROTECTED
              =============================================== */}

          <Route
            path="/"
            element={
              <ProtectedPage>
                <Dashboard />
              </ProtectedPage>
            }
          />

          <Route
            path="/projects/:projectId"
            element={
              <ProtectedPage>
                <ProjectDetails />
              </ProtectedPage>
            }
          />

        </Routes>

      </AuthProvider>

    </BrowserRouter>
  );
}

export default App;