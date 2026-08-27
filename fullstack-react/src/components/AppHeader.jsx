import {
  Link,
  useNavigate,
} from "react-router";


import {
  useAuth,
} from "../context/useAuth";

function AppHeader() {
  const {
    user,
    logout,
  } = useAuth();

  const navigate =
    useNavigate();

  const handleLogout = () => {

    logout();

    navigate(
      "/login",
      {
        replace: true,
      }
    );
  };

  return (
    <header className="app-header">
      <div className="app-header-inner">

        <Link
          to="/"
          className="app-brand"
        >
          <div className="brand-mark">
            PT
          </div>

          <div>
            <strong>
              Project Task Manager
            </strong>

            <span>
              Workspace
            </span>
          </div>
        </Link>

        <div className="header-user">

          <div className="user-avatar">
            {user?.name
              ?.charAt(0)
              ?.toUpperCase() ||
              "U"}
          </div>

          <div className="user-details">
            <strong>
              {user?.name}
            </strong>

            <span>
              {user?.email}
            </span>
          </div>

          <button
            type="button"
            className="button button-secondary logout-button"
            onClick={
              handleLogout
            }
          >
            Sign Out
          </button>

        </div>

      </div>
    </header>
  );
}

export default AppHeader;