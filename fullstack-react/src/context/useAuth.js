import {
  useContext,
} from "react";

import {
  AuthContext,
} from "./AuthContextDefinition";

/*
 * =========================================================
 * USE AUTH
 * =========================================================
 *
 * Provides access to the authentication context.
 * =========================================================
 */

export function useAuth() {

  const context =
    useContext(
      AuthContext
    );


  if (
    !context
  ) {

    throw new Error(
      "useAuth must be used within an AuthProvider."
    );

  }


  return context;

}