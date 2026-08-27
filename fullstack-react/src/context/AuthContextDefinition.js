import {
  createContext,
} from "react";


/*
 * =========================================================
 * AUTH CONTEXT DEFINITION
 * =========================================================
 *
 * The context object lives in its own file so AuthContext.jsx
 * exports only React components.
 *
 * This prevents the ESLint warning:
 *
 * react-refresh/only-export-components
 * =========================================================
 */

export const AuthContext =
  createContext(
    null
  );