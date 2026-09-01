const API_BASE_URL =
  import.meta.env.VITE_API_BASE_URL ||
  "http://localhost:8080";


/*
 * =========================================================
 * TOKEN STORAGE
 * =========================================================
 */

const AUTH_TOKEN_KEY =
  "authToken";


/*
 * =========================================================
 * API ERROR
 * =========================================================
 */

export class ApiRequestError
  extends Error {

  constructor(
    message,
    status = 0,
    data = null
  ) {

    super(
      message
    );

    this.name =
      "ApiRequestError";

    this.status =
      status;

    this.data =
      data;
  }
}


/*
 * =========================================================
 * AUTH TOKEN HELPERS
 * =========================================================
 */

export function getAuthToken() {

  return (
    localStorage.getItem(
      AUTH_TOKEN_KEY
    ) ||

    localStorage.getItem(
      "token"
    ) ||

    localStorage.getItem(
      "accessToken"
    )
  );
}


export function setAuthToken(
  token
) {

  if (
    !token
  ) {

    clearAuthToken();

    return;
  }

  localStorage.setItem(
    AUTH_TOKEN_KEY,
    token
  );
}


export function clearAuthToken() {

  localStorage.removeItem(
    AUTH_TOKEN_KEY
  );

  localStorage.removeItem(
    "token"
  );

  localStorage.removeItem(
    "accessToken"
  );
}


/*
 * =========================================================
 * GENERIC API REQUEST
 * =========================================================
 */

export async function apiRequest(
  endpoint,
  options = {}
) {

  const token =
    getAuthToken();

  const headers = {

    ...(options.body
      ? {
          "Content-Type":
            "application/json",
        }
      : {}),

    ...(options.headers || {}),
  };


  /*
   * Add JWT Bearer token to authenticated API calls.
   */

  if (
    token
  ) {

    headers.Authorization =
      `Bearer ${token}`;
  }


  let response;


  /*
   * =======================================================
   * SEND REQUEST
   * =======================================================
   */

  try {

    response =
      await fetch(
        `${API_BASE_URL}${endpoint}`,
        {
          ...options,
          headers,
        }
      );

  } catch (
    error
  ) {

    console.error(
      "API connection error:",
      error
    );

    throw new ApiRequestError(
      "Unable to connect to the server."
    );
  }


  /*
   * =======================================================
   * NO CONTENT
   * =======================================================
   */

  if (
    response.status === 204
  ) {

    return null;
  }


  /*
   * =======================================================
   * READ RESPONSE
   * =======================================================
   */

  const contentType =
    response.headers.get(
      "content-type"
    );

  const data =
    contentType &&
    contentType.includes(
      "application/json"
    )

      ? await response.json()

      : (
          await response.text()
        ) || null;


  /*
   * =======================================================
   * ERROR RESPONSE
   * =======================================================
   */

  if (
    !response.ok
  ) {

    let message =
      "An unexpected server error occurred.";


    if (
      data &&
      typeof data ===
        "object"
    ) {

      message =
        data.message ||
        data.error ||
        message;
    }


    if (
      typeof data ===
        "string" &&
      data.trim()
    ) {

      message =
        data;
    }


    /*
     * =====================================================
     * UNAUTHORIZED
     * =====================================================
     */

    if (
      response.status === 401
    ) {

      clearAuthToken();

      window.dispatchEvent(
        new Event(
          "auth:unauthorized"
        )
      );
    }


    throw new ApiRequestError(
      message,
      response.status,
      data
    );
  }


  return data;
}


/*
 * =========================================================
 * PROJECT API
 * =========================================================
 */

export function getProjects() {

  return apiRequest(
    "/api/projects"
  );
}


/*
 * =========================================================
 * CREATE PROJECT
 * =========================================================
 */

export function createProject(
  project
) {

  return apiRequest(
    "/api/projects",
    {
      method:
        "POST",

      body:
        JSON.stringify(
          project
        ),
    }
  );
}


/*
 * =========================================================
 * UPDATE PROJECT
 * =========================================================
 */

export function updateProject(
  projectId,
  project
) {

  return apiRequest(
    `/api/projects/${projectId}`,
    {
      method:
        "PUT",

      body:
        JSON.stringify(
          project
        ),
    }
  );
}


/*
 * =========================================================
 * DELETE PROJECT
 * =========================================================
 */

export function deleteProject(
  projectId
) {

  return apiRequest(
    `/api/projects/${projectId}`,
    {
      method:
        "DELETE",
    }
  );
}


/*
 * =========================================================
 * GLOBAL PROJECT STATISTICS
 * =========================================================
 */

export function getProjectStats() {

  return apiRequest(
    "/api/projects/stats"
  );
}


/*
 * =========================================================
 * PROJECT HEALTH
 *
 * With projectId:
 *
 * GET /api/projects/{id}/health
 *
 * Without projectId:
 *
 * GET /api/projects/health
 * =========================================================
 */

export function getProjectHealth(
  projectId = null
) {

  if (
    projectId !== null &&
    projectId !== undefined
  ) {

    return apiRequest(
      `/api/projects/${projectId}/health`
    );
  }


  return apiRequest(
    "/api/projects/health"
  );
}


/*
 * =========================================================
 * SEQUENCE 15A - PROJECT ACTIVITY
 *
 * GET /api/projects/{projectId}/activity
 * =========================================================
 */

export function getProjectActivity(
  projectId
) {

  return apiRequest(
    `/api/projects/${projectId}/activity`
  );
}


/*
 * =========================================================
 * PROJECT ACTIVITY COUNT
 *
 * Retained for future dashboard/statistics use.
 * =========================================================
 */

export function getProjectActivityCount(
  projectId
) {

  return apiRequest(
    `/api/projects/${projectId}/activity/count`
  );
}


/*
 * =========================================================
 * TASK API
 * =========================================================
 */


/*
 * =========================================================
 * GLOBAL TASK STATISTICS
 * =========================================================
 */

export function getTaskStats() {

  return apiRequest(
    "/api/tasks/stats"
  );
}


/*
 * =========================================================
 * FULL PROJECT TASK LIST
 * =========================================================
 */

export function getProjectTasks(
  projectId
) {

  return apiRequest(
    `/api/projects/${projectId}/tasks`
  );
}


/*
 * =========================================================
 * PAGINATED / FILTERED PROJECT TASKS
 * =========================================================
 */

export function getProjectTasksPage(
  projectId,
  options = {}
) {

  const {

    page = 0,

    size = 10,

    status = null,

    priority = null,

    search = null,

    dueDateFilter = null,

    sortBy =
      "dueDate",

    sortDirection =
      "asc",

  } = options;


  const params =
    new URLSearchParams();


  /*
   * PAGINATION
   */

  params.set(
    "page",
    String(
      page
    )
  );

  params.set(
    "size",
    String(
      size
    )
  );


  /*
   * SORTING
   */

  params.set(
    "sortBy",
    sortBy
  );

  params.set(
    "sortDirection",
    sortDirection
  );


  /*
   * STATUS FILTER
   */

  if (
    status &&
    status !==
      "ALL"
  ) {

    params.set(
      "status",
      status
    );
  }


  /*
   * PRIORITY FILTER
   */

  if (
    priority &&
    priority !==
      "ALL"
  ) {

    params.set(
      "priority",
      priority
    );
  }


  /*
   * SEARCH
   */

  if (
    search &&
    search.trim()
  ) {

    params.set(
      "search",
      search.trim()
    );
  }


  /*
   * DUE DATE FILTER
   */

  if (
    dueDateFilter &&
    dueDateFilter !==
      "ALL"
  ) {

    params.set(
      "dueDateFilter",
      dueDateFilter
    );
  }


  return apiRequest(
    `/api/projects/${projectId}/tasks/page?${params.toString()}`
  );
}


/*
 * =========================================================
 * CREATE TASK
 * =========================================================
 */

export function createTask(
  projectId,
  task
) {

  return apiRequest(
    `/api/projects/${projectId}/tasks`,
    {
      method:
        "POST",

      body:
        JSON.stringify(
          task
        ),
    }
  );
}


/*
 * =========================================================
 * UPDATE TASK
 * =========================================================
 */

export function updateTask(
  projectId,
  taskId,
  task
) {

  return apiRequest(
    `/api/projects/${projectId}/tasks/${taskId}`,
    {
      method:
        "PUT",

      body:
        JSON.stringify(
          task
        ),
    }
  );
}


/*
 * =========================================================
 * DELETE TASK
 * =========================================================
 */

export function deleteTask(
  projectId,
  taskId
) {

  return apiRequest(
    `/api/projects/${projectId}/tasks/${taskId}`,
    {
      method:
        "DELETE",
    }
  );
}