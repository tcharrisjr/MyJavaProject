import {
  useEffect,
  useState,
} from "react";

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

import {
  AuthContext,
} from "./AuthContextDefinition";


/*
 * =========================================================
 * AUTH PROVIDER
 * =========================================================
 */

export function AuthProvider({
  children,
}) {

  /*
   * =======================================================
   * INITIAL TOKEN
   * =======================================================
   *
   * Read the stored token once when AuthProvider is created.
   *
   * If no token exists, there is no session to restore and
   * loading can start as false.
   * =======================================================
   */

  const [
    initialToken,
  ] = useState(
    () =>
      getAuthToken()
  );


  /*
   * =======================================================
   * USER STATE
   * =======================================================
   */

  const [
    user,
    setUser,
  ] = useState(
    null
  );


  /*
   * =======================================================
   * LOADING STATE
   * =======================================================
   *
   * If a token exists, authentication restoration must run.
   *
   * If no token exists, authentication is already resolved.
   * =======================================================
   */

  const [
    loading,
    setLoading,
  ] = useState(
    () =>
      Boolean(
        initialToken
      )
  );


  /*
   * =======================================================
   * SESSION RESTORATION
   * =======================================================
   *
   * State updates happen from asynchronous Promise handlers
   * instead of synchronously inside the effect body.
   *
   * This avoids:
   *
   * react-hooks/set-state-in-effect
   * =======================================================
   */

  useEffect(
    () => {

      /*
       * ---------------------------------------------------
       * RESTORE EXISTING SESSION
       * ---------------------------------------------------
       */

      if (
        initialToken
      ) {

        getCurrentUser()

          .then(
            (
              currentUser
            ) => {

              setUser(
                currentUser
              );

            }
          )

          .catch(
            () => {

              /*
               * Stored token is invalid or expired.
               */

              clearAuthToken();


              setUser(
                null
              );

            }
          )

          .finally(
            () => {

              setLoading(
                false
              );

            }
          );

      }


      /*
       * ---------------------------------------------------
       * UNAUTHORIZED EVENT
       * ---------------------------------------------------
       *
       * apiClient dispatches this event when the backend
       * returns HTTP 401.
       * ---------------------------------------------------
       */

      const handleUnauthorized =
        () => {

          setUser(
            null
          );

        };


      window.addEventListener(
        "auth:unauthorized",
        handleUnauthorized
      );


      /*
       * ---------------------------------------------------
       * CLEANUP
       * ---------------------------------------------------
       */

      return () => {

        window.removeEventListener(
          "auth:unauthorized",
          handleUnauthorized
        );

      };

    },
    [
      initialToken,
    ]
  );


  /*
   * =======================================================
   * LOGIN
   * =======================================================
   */

  const login =
    async (
      email,
      password
    ) => {

      const response =
        await loginUser({
          email,
          password,
        });


      setAuthToken(
        response.token
      );


      setUser(
        response.user
      );


      return response.user;

    };


  /*
   * =======================================================
   * REGISTER
   * =======================================================
   */

  const register =
    async (
      name,
      email,
      password
    ) => {

      const response =
        await registerUser({
          name,
          email,
          password,
        });


      setAuthToken(
        response.token
      );


      setUser(
        response.user
      );


      return response.user;

    };


  /*
   * =======================================================
   * LOGOUT
   * =======================================================
   */

  const logout =
    () => {

      clearAuthToken();


      setUser(
        null
      );

    };


  /*
   * =======================================================
   * CONTEXT VALUE
   * =======================================================
   */

  const value = {

    user,

    loading,

    authenticated:
      Boolean(
        user
      ),

    login,

    register,

    logout,

  };


  /*
   * =======================================================
   * PROVIDER
   * =======================================================
   */

  return (

    <AuthContext.Provider
      value={
        value
      }
    >

      {children}

    </AuthContext.Provider>

  );

}