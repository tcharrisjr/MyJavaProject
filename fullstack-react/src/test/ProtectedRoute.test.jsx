import {
  render,
  screen,
} from "@testing-library/react";

import {
  MemoryRouter,
  Route,
  Routes,
  useLocation,
} from "react-router";

import {
  beforeEach,
  describe,
  expect,
  it,
  vi,
} from "vitest";

import ProtectedRoute
  from "../components/ProtectedRoute";

import {
  useAuth,
} from "../context/useAuth";


/*
 * =========================================================
 * MOCK useAuth
 * =========================================================
 *
 * ProtectedRoute depends on useAuth().
 *
 * Mocking the hook lets each test control:
 *
 * - authenticated
 * - loading
 *
 * without loading the full AuthProvider.
 * =========================================================
 */

vi.mock(
  "../context/useAuth",
  () => ({
    useAuth: vi.fn(),
  })
);


/*
 * =========================================================
 * LOCATION DISPLAY
 * =========================================================
 *
 * Used to verify redirect behavior.
 * =========================================================
 */

function LocationDisplay() {

  const location =
    useLocation();

  return (
    <div>
      <div data-testid="location-path">
        {location.pathname}
      </div>

      <div data-testid="location-state">
        {
          location.state?.from?.pathname
            ?? "none"
        }
      </div>
    </div>
  );
}


/*
 * =========================================================
 * TEST APPLICATION
 * =========================================================
 */

function renderProtectedRoute(
  initialPath = "/dashboard"
) {

  return render(

    <MemoryRouter
      initialEntries={[
        initialPath,
      ]}
    >

      <Routes>

        <Route
          path="/dashboard"
          element={
            <ProtectedRoute>
              <div>
                Protected Dashboard
              </div>
            </ProtectedRoute>
          }
        />

        <Route
          path="/login"
          element={
            <>
              <div>
                Login Page
              </div>

              <LocationDisplay />
            </>
          }
        />

      </Routes>

    </MemoryRouter>
  );
}


/*
 * =========================================================
 * PROTECTED ROUTE TESTS
 * =========================================================
 */

describe(
  "ProtectedRoute",
  () => {

    beforeEach(
      () => {

        vi.clearAllMocks();

      }
    );


    /*
     * =====================================================
     * TEST 1
     *
     * LOADING STATE
     * =====================================================
     */

    it(
      "shows the loading workspace message while authentication is loading",
      () => {

        useAuth.mockReturnValue({
          authenticated: false,
          loading: true,
        });


        renderProtectedRoute();


        expect(
          screen.getByText(
            "Loading workspace..."
          )
        )
          .toBeInTheDocument();


        expect(
          screen.queryByText(
            "Protected Dashboard"
          )
        )
          .not
          .toBeInTheDocument();


        expect(
          screen.queryByText(
            "Login Page"
          )
        )
          .not
          .toBeInTheDocument();

      }
    );


    /*
     * =====================================================
     * TEST 2
     *
     * UNAUTHENTICATED USER
     * =====================================================
     */

    it(
      "redirects an unauthenticated user to the login page",
      () => {

        useAuth.mockReturnValue({
          authenticated: false,
          loading: false,
        });


        renderProtectedRoute();


        expect(
          screen.getByText(
            "Login Page"
          )
        )
          .toBeInTheDocument();


        expect(
          screen.getByTestId(
            "location-path"
          )
        )
          .toHaveTextContent(
            "/login"
          );


        expect(
          screen.queryByText(
            "Protected Dashboard"
          )
        )
          .not
          .toBeInTheDocument();

      }
    );


    /*
     * =====================================================
     * TEST 3
     *
     * ORIGINAL LOCATION PRESERVED
     *
     * ProtectedRoute stores the original location in:
     *
     * state.from
     *
     * This lets the login flow redirect the user back to
     * the page they originally attempted to visit.
     * =====================================================
     */

    it(
      "preserves the original protected location when redirecting to login",
      () => {

        useAuth.mockReturnValue({
          authenticated: false,
          loading: false,
        });


        renderProtectedRoute(
          "/dashboard"
        );


        expect(
          screen.getByTestId(
            "location-state"
          )
        )
          .toHaveTextContent(
            "/dashboard"
          );

      }
    );


    /*
     * =====================================================
     * TEST 4
     *
     * AUTHENTICATED USER
     * =====================================================
     */

    it(
      "renders protected content for an authenticated user",
      () => {

        useAuth.mockReturnValue({
          authenticated: true,
          loading: false,
        });


        renderProtectedRoute();


        expect(
          screen.getByText(
            "Protected Dashboard"
          )
        )
          .toBeInTheDocument();


        expect(
          screen.queryByText(
            "Login Page"
          )
        )
          .not
          .toBeInTheDocument();


        expect(
          screen.queryByText(
            "Loading workspace..."
          )
        )
          .not
          .toBeInTheDocument();

      }
    );

  }
);