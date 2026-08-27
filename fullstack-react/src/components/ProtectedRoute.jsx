import {
  Navigate,
  useLocation,
} from "react-router";

import {
  useAuth,
} from "../context/useAuth";

function ProtectedRoute({
  children,
}) {
  const {
    authenticated,
    loading,
  } = useAuth();

  const location =
    useLocation();

  if (loading) {
    return (
      <div className="auth-loading">
        <div className="auth-loading-card">
          Loading workspace...
        </div>
      </div>
    );
  }

  if (!authenticated) {
    return (
      <Navigate
        to="/login"
        replace
        state={{
          from: location,
        }}
      />
    );
  }

  return children;
}

export default ProtectedRoute;