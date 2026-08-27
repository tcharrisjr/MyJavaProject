import {
  apiRequest,
} from "./apiClient";

/*
 * =========================================================
 * TASK STATISTICS
 * =========================================================
 */

export async function getTaskStats() {
  return apiRequest(
    "/api/tasks/stats"
  );
}

/*
 * =========================================================
 * PAGED / FILTERED TASKS
 * =========================================================
 */

export async function getTasks({
  page = 0,
  size = 10,
  projectId = "",
  status = "",
  priority = "",
  search = "",
  sortBy = "dueDate",
  direction = "asc",
} = {}) {

  const params =
    new URLSearchParams();

  params.set(
    "page",
    String(page)
  );

  params.set(
    "size",
    String(size)
  );

  params.set(
    "sortBy",
    sortBy
  );

  params.set(
    "direction",
    direction
  );

  if (projectId) {
    params.set(
      "projectId",
      String(projectId)
    );
  }

  if (status) {
    params.set(
      "status",
      status
    );
  }

  if (priority) {
    params.set(
      "priority",
      priority
    );
  }

  if (search.trim()) {
    params.set(
      "search",
      search.trim()
    );
  }

  return apiRequest(
    `/api/tasks?${params.toString()}`
  );
}

/*
 * =========================================================
 * GET TASK
 * =========================================================
 */

export async function getTaskById(
  taskId
) {
  return apiRequest(
    `/api/tasks/${taskId}`
  );
}

/*
 * =========================================================
 * CREATE
 * =========================================================
 */

export async function createTask(
  task
) {
  return apiRequest(
    "/api/tasks",
    {
      method: "POST",

      body:
        JSON.stringify(
          task
        ),
    }
  );
}

/*
 * =========================================================
 * UPDATE
 * =========================================================
 */

export async function updateTask(
  taskId,
  task
) {
  return apiRequest(
    `/api/tasks/${taskId}`,
    {
      method: "PUT",

      body:
        JSON.stringify(
          task
        ),
    }
  );
}

/*
 * =========================================================
 * DELETE
 * =========================================================
 */

export async function deleteTask(
  taskId
) {
  return apiRequest(
    `/api/tasks/${taskId}`,
    {
      method: "DELETE",
    }
  );
}