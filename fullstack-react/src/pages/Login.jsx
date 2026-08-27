import {
  useState,
} from "react";

import {
  Link,
  Navigate,
  useLocation,
  useNavigate,
} from "react-router";

import {
  useAuth,
} from "../context/useAuth";

function Login() {
  const {
    authenticated,
    login,
  } = useAuth();

  const navigate =
    useNavigate();

  const location =
    useLocation();

  const [
    form,
    setForm,
  ] = useState({
    email: "",
    password: "",
  });

  const [
    error,
    setError,
  ] = useState("");

  const [
    submitting,
    setSubmitting,
  ] = useState(false);

  if (authenticated) {
    return (
      <Navigate
        to="/"
        replace
      />
    );
  }

  const handleChange = (
    event
  ) => {

    const {
      name,
      value,
    } = event.target;

    setForm(
      (previous) => ({
        ...previous,
        [name]: value,
      })
    );

    setError("");
  };

  const handleSubmit =
    async (event) => {

      event.preventDefault();

      setSubmitting(true);
      setError("");

      try {

        await login(
          form.email,
          form.password
        );

        const destination =
          location
            .state
            ?.from
            ?.pathname
          || "/";

        navigate(
          destination,
          {
            replace: true,
          }
        );

      } catch (error) {

        setError(
          error.message ||
            "Unable to sign in."
        );

      } finally {

        setSubmitting(false);
      }
    };

  return (
    <main className="auth-page">

      <section className="auth-shell">

        <div className="auth-brand-panel">

          <div className="auth-brand-content">

            <div className="brand-mark brand-mark-large">
              PT
            </div>

            <span className="eyebrow auth-eyebrow">
              Project Workspace
            </span>

            <h1>
              Keep projects moving.
            </h1>

            <p>
              Track tasks, monitor delivery
              health, manage deadlines, and
              keep your project portfolio
              organized in one workspace.
            </p>

            <div className="auth-feature-list">

              <div>
                <strong>
                  Project Health
                </strong>

                <span>
                  Spot delivery risks quickly.
                </span>
              </div>

              <div>
                <strong>
                  Task Tracking
                </strong>

                <span>
                  Manage priorities and deadlines.
                </span>
              </div>

              <div>
                <strong>
                  Progress Analytics
                </strong>

                <span>
                  Measure completion at a glance.
                </span>
              </div>

            </div>

          </div>

        </div>

        <div className="auth-form-panel">

          <div className="auth-form-container">

            <span className="eyebrow">
              Welcome Back
            </span>

            <h2>
              Sign in to your workspace
            </h2>

            <p className="auth-intro">
              Enter your account credentials
              to continue.
            </p>

            {error && (
              <div className="alert alert-error">
                {error}
              </div>
            )}

            <form
              className="auth-form"
              onSubmit={
                handleSubmit
              }
            >

              <div className="form-field">

                <label htmlFor="email">
                  Email Address
                </label>

                <input
                  id="email"
                  type="email"
                  name="email"
                  autoComplete="email"
                  value={
                    form.email
                  }
                  onChange={
                    handleChange
                  }
                  placeholder="you@example.com"
                  required
                />

              </div>

              <div className="form-field">

                <label htmlFor="password">
                  Password
                </label>

                <input
                  id="password"
                  type="password"
                  name="password"
                  autoComplete="current-password"
                  value={
                    form.password
                  }
                  onChange={
                    handleChange
                  }
                  placeholder="Enter your password"
                  required
                />

              </div>

              <button
                type="submit"
                className="button button-primary auth-submit"
                disabled={
                  submitting
                }
              >
                {submitting
                  ? "Signing In..."
                  : "Sign In"}
              </button>

            </form>

            <p className="auth-switch">
              Don't have an account?{" "}

              <Link to="/register">
                Create one
              </Link>
            </p>

          </div>

        </div>

      </section>

    </main>
  );
}

export default Login;