import {
  apiRequest,
} from "./apiClient";

/*
 * =========================================================
 * PROJECTS
 * =========================================================
 */

export async function getAllProjects() {
  return apiRequest(
    "/api/projects"
  );
}

export async function getProjectById(
  projectId
) {
  return apiRequest(
    `/api/projects/${projectId}`
  );
}

/*
 * =========================================================
 * STATISTICS
 * =========================================================
 */

export async function getProjectStats() {
  return apiRequest(
    "/api/projects/stats"
  );
}

export async function getProjectStatsById(
  projectId
) {
  return apiRequest(
    `/api/projects/${projectId}/stats`
  );
}

/*
 * =========================================================
 * HEALTH
 * =========================================================
 */

export async function getAllProjectHealth() {
  return apiRequest(
    "/api/projects/health"
  );
}

export async function getProjectHealth(
  projectId
) {
  return apiRequest(
    `/api/projects/${projectId}/health`
  );
}

/*
 * =========================================================
 * ACTIVITY
 * =========================================================
 */

export async function getProjectActivity(
  projectId
) {
  return apiRequest(
    `/api/projects/${projectId}/activity`
  );
}

/*
 * =========================================================
 * CREATE
 * =========================================================
 */

export async function createProject(
  project
) {
  return apiRequest(
    "/api/projects",
    {
      method: "POST",

      body:
        JSON.stringify(
          project
        ),
    }
  );
}

/*
 * =========================================================
 * UPDATE
 * =========================================================
 */

export async function updateProject(
  projectId,
  project
) {
  return apiRequest(
    `/api/projects/${projectId}`,
    {
      method: "PUT",

      body:
        JSON.stringify(
          project
        ),
    }
  );
}

/*
 * =========================================================
 * DELETE
 * =========================================================
 */

export async function deleteProject(
  projectId
) {
  return apiRequest(
    `/api/projects/${projectId}`,
    {
      method: "DELETE",
    }
  );
}