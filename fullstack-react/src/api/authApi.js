import {
  apiRequest,
} from "./apiClient";

/*
 * =========================================================
 * REGISTER
 * =========================================================
 */

export async function registerUser(
  request
) {
  return apiRequest(
    "/api/auth/register",
    {
      method: "POST",

      body:
        JSON.stringify(
          request
        ),
    },
    {
      authenticated:
        false,

      handleUnauthorized:
        false,
    }
  );
}

/*
 * =========================================================
 * LOGIN
 * =========================================================
 */

export async function loginUser(
  request
) {
  return apiRequest(
    "/api/auth/login",
    {
      method: "POST",

      body:
        JSON.stringify(
          request
        ),
    },
    {
      authenticated:
        false,

      handleUnauthorized:
        false,
    }
  );
}

/*
 * =========================================================
 * CURRENT USER
 * =========================================================
 */

export async function getCurrentUser() {
  return apiRequest(
    "/api/auth/me"
  );
}

/*
 * =========================================================
 * SEQUENCE 13A - TASK ASSIGNEES
 * =========================================================
 *
 * Returns the enabled users that may be selected
 * as task assignees.
 *
 * Backend endpoint:
 *
 * GET /api/auth/assignees
 * =========================================================
 */

export async function getAssignees() {
  return apiRequest(
    "/api/auth/assignees"
  );
}