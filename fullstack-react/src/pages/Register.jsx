import {
  useState,
} from "react";

import {
  Link,
  Navigate,
  useNavigate,
} from "react-router";

import {
  useAuth,
} from "../context/useAuth";
function Register() {
  const {
    authenticated,
    register,
  } = useAuth();

  const navigate =
    useNavigate();

  const [
    form,
    setForm,
  ] = useState({
    name: "",
    email: "",
    password: "",
    confirmPassword: "",
  });

  const [
    error,
    setError,
  ] = useState("");

  const [
    fieldErrors,
    setFieldErrors,
  ] = useState({});

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

    setFieldErrors(
      (previous) => ({
        ...previous,
        [name]: undefined,
      })
    );

    setError("");
  };

  const handleSubmit =
    async (event) => {

      event.preventDefault();

      setError("");
      setFieldErrors({});

      if (
        form.password !==
        form.confirmPassword
      ) {
        setFieldErrors({
          confirmPassword:
            "Passwords do not match.",
        });

        return;
      }

      setSubmitting(true);

      try {

        await register(
          form.name,
          form.email,
          form.password
        );

        navigate(
          "/",
          {
            replace: true,
          }
        );

      } catch (error) {

        setFieldErrors(
          error.validationErrors ||
            {}
        );

        setError(
          error.message ||
            "Unable to create account."
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
              Build a clearer view of your work.
            </h1>

            <p>
              Create an account to manage
              projects, tasks, deadlines,
              progress, and delivery health.
            </p>

          </div>

        </div>

        <div className="auth-form-panel">

          <div className="auth-form-container">

            <span className="eyebrow">
              New Workspace User
            </span>

            <h2>
              Create your account
            </h2>

            <p className="auth-intro">
              Start managing your project
              portfolio.
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

                <label htmlFor="name">
                  Name
                </label>

                <input
                  id="name"
                  type="text"
                  name="name"
                  autoComplete="name"
                  value={
                    form.name
                  }
                  onChange={
                    handleChange
                  }
                  placeholder="Your name"
                  className={
                    fieldErrors.name
                      ? "input-error"
                      : ""
                  }
                />

                {fieldErrors.name && (
                  <span className="field-error">
                    {fieldErrors.name}
                  </span>
                )}

              </div>

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
                  className={
                    fieldErrors.email
                      ? "input-error"
                      : ""
                  }
                />

                {fieldErrors.email && (
                  <span className="field-error">
                    {fieldErrors.email}
                  </span>
                )}

              </div>

              <div className="form-field">

                <label htmlFor="password">
                  Password
                </label>

                <input
                  id="password"
                  type="password"
                  name="password"
                  autoComplete="new-password"
                  value={
                    form.password
                  }
                  onChange={
                    handleChange
                  }
                  placeholder="Minimum 8 characters"
                  className={
                    fieldErrors.password
                      ? "input-error"
                      : ""
                  }
                />

                {fieldErrors.password && (
                  <span className="field-error">
                    {fieldErrors.password}
                  </span>
                )}

              </div>

              <div className="form-field">

                <label htmlFor="confirmPassword">
                  Confirm Password
                </label>

                <input
                  id="confirmPassword"
                  type="password"
                  name="confirmPassword"
                  autoComplete="new-password"
                  value={
                    form.confirmPassword
                  }
                  onChange={
                    handleChange
                  }
                  placeholder="Re-enter password"
                  className={
                    fieldErrors
                      .confirmPassword
                      ? "input-error"
                      : ""
                  }
                />

                {fieldErrors
                  .confirmPassword && (
                  <span className="field-error">
                    {
                      fieldErrors
                        .confirmPassword
                    }
                  </span>
                )}

              </div>

              <button
                type="submit"
                className="button button-primary auth-submit"
                disabled={
                  submitting
                }
              >
                {submitting
                  ? "Creating Account..."
                  : "Create Account"}
              </button>

            </form>

            <p className="auth-switch">
              Already have an account?{" "}

              <Link to="/login">
                Sign in
              </Link>
            </p>

          </div>

        </div>

      </section>

    </main>
  );
}

export default Register;