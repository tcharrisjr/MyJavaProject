import {
  act,
  render,
  screen,
  waitFor,
} from "@testing-library/react";

import userEvent from "@testing-library/user-event";

import {
  beforeEach,
  describe,
  expect,
  it,
  vi,
} from "vitest";

import {
  AuthProvider,
} from "../context/AuthContext";

import {
  AuthContext,
} from "../context/AuthContextDefinition";
import {
  getCurrentUser,
  loginUser,
  registerUser,
} from "../api/authApi";

import {
  clearAuthToken,
  getAuthToken,
  setAuthToken,
} from "../api/apiClient";


/*
 * =========================================================
 * MODULE MOCKS
 * =========================================================
 */

vi.mock(
  "../api/authApi",
  () => ({
    getCurrentUser: vi.fn(),
    loginUser: vi.fn(),
    registerUser: vi.fn(),
  })
);

vi.mock(
  "../api/apiClient",
  () => ({
    clearAuthToken: vi.fn(),
    getAuthToken: vi.fn(),
    setAuthToken: vi.fn(),
  })
);


/*
 * =========================================================
 * TEST CONSUMER
 * =========================================================
 *
 * This small component exposes AuthContext values in the DOM
 * so React Testing Library can verify state changes.
 * =========================================================
 */

function AuthTestConsumer() {

  return (
    <AuthContext.Consumer>
      {
        ({
          user,
          loading,
          authenticated,
          login,
          register,
          logout,
        }) => (

          <div>

            <div data-testid="loading">
              {String(loading)}
            </div>

            <div data-testid="authenticated">
              {String(authenticated)}
            </div>

            <div data-testid="user-email">
              {user?.email ?? "none"}
            </div>

            <button
              type="button"
              onClick={() =>
                login(
                  "testuser@example.com",
                  "Password123!"
                )
              }
            >
              Login
            </button>

            <button
              type="button"
              onClick={() =>
                register(
                  "Test User",
                  "newuser@example.com",
                  "Password123!"
                )
              }
            >
              Register
            </button>

            <button
              type="button"
              onClick={logout}
            >
              Logout
            </button>

          </div>

        )
      }
    </AuthContext.Consumer>
  );
}


/*
 * =========================================================
 * AUTH CONTEXT TESTS
 * =========================================================
 */

describe(
  "AuthContext",
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
     * NO TOKEN
     *
     * When no token exists, AuthProvider should finish
     * loading and remain unauthenticated.
     * =====================================================
     */

    it(
      "starts unauthenticated when no token exists",
      async () => {

        getAuthToken.mockReturnValue(
          null
        );


        render(
          <AuthProvider>
            <AuthTestConsumer />
          </AuthProvider>
        );


        await waitFor(
          () => {

            expect(
              screen.getByTestId(
                "loading"
              )
            )
              .toHaveTextContent(
                "false"
              );

          }
        );


        expect(
          screen.getByTestId(
            "authenticated"
          )
        )
          .toHaveTextContent(
            "false"
          );


        expect(
          screen.getByTestId(
            "user-email"
          )
        )
          .toHaveTextContent(
            "none"
          );


        expect(
          getCurrentUser
        )
          .not
          .toHaveBeenCalled();

      }
    );


    /*
     * =====================================================
     * TEST 2
     *
     * RESTORE EXISTING SESSION
     * =====================================================
     */

    it(
      "restores the authenticated user when a token exists",
      async () => {

        const existingUser = {
          id: 1,
          name: "Test User",
          email: "testuser@example.com",
        };


        getAuthToken.mockReturnValue(
          "existing-jwt-token"
        );


        getCurrentUser.mockResolvedValue(
          existingUser
        );


        render(
          <AuthProvider>
            <AuthTestConsumer />
          </AuthProvider>
        );


        await waitFor(
          () => {

            expect(
              screen.getByTestId(
                "loading"
              )
            )
              .toHaveTextContent(
                "false"
              );

          }
        );


        expect(
          getCurrentUser
        )
          .toHaveBeenCalledTimes(1);


        expect(
          screen.getByTestId(
            "authenticated"
          )
        )
          .toHaveTextContent(
            "true"
          );


        expect(
          screen.getByTestId(
            "user-email"
          )
        )
          .toHaveTextContent(
            "testuser@example.com"
          );

      }
    );


    /*
     * =====================================================
     * TEST 3
     *
     * RESTORE FAILURE
     *
     * If the stored token is invalid or expired,
     * AuthProvider should clear it.
     * =====================================================
     */

    it(
      "clears authentication when session restoration fails",
      async () => {

        getAuthToken.mockReturnValue(
          "expired-jwt-token"
        );


        getCurrentUser.mockRejectedValue(
          new Error(
            "Unauthorized"
          )
        );


        render(
          <AuthProvider>
            <AuthTestConsumer />
          </AuthProvider>
        );


        await waitFor(
          () => {

            expect(
              screen.getByTestId(
                "loading"
              )
            )
              .toHaveTextContent(
                "false"
              );

          }
        );


        expect(
          clearAuthToken
        )
          .toHaveBeenCalledTimes(1);


        expect(
          screen.getByTestId(
            "authenticated"
          )
        )
          .toHaveTextContent(
            "false"
          );


        expect(
          screen.getByTestId(
            "user-email"
          )
        )
          .toHaveTextContent(
            "none"
          );

      }
    );


    /*
     * =====================================================
     * TEST 4
     *
     * LOGIN
     * =====================================================
     */

    it(
      "stores the token and authenticates the user after login",
      async () => {

        const user =
          userEvent.setup();


        const loggedInUser = {
          id: 1,
          name: "Test User",
          email: "testuser@example.com",
        };


        getAuthToken.mockReturnValue(
          null
        );


        loginUser.mockResolvedValue({
          token:
            "login-jwt-token",

          user:
            loggedInUser,
        });


        render(
          <AuthProvider>
            <AuthTestConsumer />
          </AuthProvider>
        );


        await waitFor(
          () => {

            expect(
              screen.getByTestId(
                "loading"
              )
            )
              .toHaveTextContent(
                "false"
              );

          }
        );


        await user.click(
          screen.getByRole(
            "button",
            {
              name: "Login",
            }
          )
        );


        await waitFor(
          () => {

            expect(
              loginUser
            )
              .toHaveBeenCalledWith({
                email:
                  "testuser@example.com",

                password:
                  "Password123!",
              });

          }
        );


        expect(
          setAuthToken
        )
          .toHaveBeenCalledWith(
            "login-jwt-token"
          );


        expect(
          screen.getByTestId(
            "authenticated"
          )
        )
          .toHaveTextContent(
            "true"
          );


        expect(
          screen.getByTestId(
            "user-email"
          )
        )
          .toHaveTextContent(
            "testuser@example.com"
          );

      }
    );


    /*
     * =====================================================
     * TEST 5
     *
     * REGISTER
     * =====================================================
     */

    it(
      "stores the token and authenticates the user after registration",
      async () => {

        const user =
          userEvent.setup();


        const registeredUser = {
          id: 2,
          name: "Test User",
          email: "newuser@example.com",
        };


        getAuthToken.mockReturnValue(
          null
        );


        registerUser.mockResolvedValue({
          token:
            "registration-jwt-token",

          user:
            registeredUser,
        });


        render(
          <AuthProvider>
            <AuthTestConsumer />
          </AuthProvider>
        );


        await waitFor(
          () => {

            expect(
              screen.getByTestId(
                "loading"
              )
            )
              .toHaveTextContent(
                "false"
              );

          }
        );


        await user.click(
          screen.getByRole(
            "button",
            {
              name: "Register",
            }
          )
        );


        await waitFor(
          () => {

            expect(
              registerUser
            )
              .toHaveBeenCalledWith({
                name:
                  "Test User",

                email:
                  "newuser@example.com",

                password:
                  "Password123!",
              });

          }
        );


        expect(
          setAuthToken
        )
          .toHaveBeenCalledWith(
            "registration-jwt-token"
          );


        expect(
          screen.getByTestId(
            "authenticated"
          )
        )
          .toHaveTextContent(
            "true"
          );


        expect(
          screen.getByTestId(
            "user-email"
          )
        )
          .toHaveTextContent(
            "newuser@example.com"
          );

      }
    );


    /*
     * =====================================================
     * TEST 6
     *
     * LOGOUT
     * =====================================================
     */

    it(
      "clears the token and user when logout is called",
      async () => {

        const user =
          userEvent.setup();


        getAuthToken.mockReturnValue(
          "existing-token"
        );


        getCurrentUser.mockResolvedValue({
          id: 1,
          name: "Test User",
          email: "testuser@example.com",
        });


        render(
          <AuthProvider>
            <AuthTestConsumer />
          </AuthProvider>
        );


        await waitFor(
          () => {

            expect(
              screen.getByTestId(
                "authenticated"
              )
            )
              .toHaveTextContent(
                "true"
              );

          }
        );


        await user.click(
          screen.getByRole(
            "button",
            {
              name: "Logout",
            }
          )
        );


        expect(
          clearAuthToken
        )
          .toHaveBeenCalledTimes(1);


        expect(
          screen.getByTestId(
            "authenticated"
          )
        )
          .toHaveTextContent(
            "false"
          );


        expect(
          screen.getByTestId(
            "user-email"
          )
        )
          .toHaveTextContent(
            "none"
          );

      }
    );


    /*
     * =====================================================
     * TEST 7
     *
     * AUTH UNAUTHORIZED EVENT
     *
     * apiClient emits:
     *
     * auth:unauthorized
     *
     * AuthProvider should immediately clear its user state.
     * =====================================================
     */

    it(
      "clears the authenticated user when auth:unauthorized is emitted",
      async () => {

        getAuthToken.mockReturnValue(
          "existing-token"
        );


        getCurrentUser.mockResolvedValue({
          id: 1,
          name: "Test User",
          email: "testuser@example.com",
        });


        render(
          <AuthProvider>
            <AuthTestConsumer />
          </AuthProvider>
        );


        await waitFor(
          () => {

            expect(
              screen.getByTestId(
                "authenticated"
              )
            )
              .toHaveTextContent(
                "true"
              );

          }
        );


        act(
          () => {

            window.dispatchEvent(
              new Event(
                "auth:unauthorized"
              )
            );

          }
        );


        expect(
          screen.getByTestId(
            "authenticated"
          )
        )
          .toHaveTextContent(
            "false"
          );


        expect(
          screen.getByTestId(
            "user-email"
          )
        )
          .toHaveTextContent(
            "none"
          );

      }
    );

  }
);