import {
  apiRequest,
} from "./apiClient";


/*
 * =========================================================
 * SEQUENCE 14B - TASK COMMENT API
 * =========================================================
 *
 * Handles all frontend API communication for task comments.
 *
 * Backend routes:
 *
 * GET
 * /api/projects/{projectId}/tasks/{taskId}/comments
 *
 * POST
 * /api/projects/{projectId}/tasks/{taskId}/comments
 *
 * PUT
 * /api/projects/{projectId}/tasks/{taskId}/comments/{commentId}
 *
 * DELETE
 * /api/projects/{projectId}/tasks/{taskId}/comments/{commentId}
 *
 * GET
 * /api/projects/{projectId}/tasks/{taskId}/comments/count
 *
 * =========================================================
 */


/*
 * =========================================================
 * GET TASK COMMENTS
 * =========================================================
 */

export function getTaskComments(
  projectId,
  taskId
) {

  return apiRequest(
    `/api/projects/${projectId}/tasks/${taskId}/comments`
  );

}


/*
 * =========================================================
 * CREATE TASK COMMENT
 * =========================================================
 */

export function createTaskComment(
  projectId,
  taskId,
  commentText
) {

  return apiRequest(
    `/api/projects/${projectId}/tasks/${taskId}/comments`,
    {
      method:
        "POST",

      body:
        JSON.stringify(
          {
            commentText,
          }
        ),
    }
  );

}


/*
 * =========================================================
 * UPDATE TASK COMMENT
 * =========================================================
 */

export function updateTaskComment(
  projectId,
  taskId,
  commentId,
  commentText
) {

  return apiRequest(
    `/api/projects/${projectId}/tasks/${taskId}/comments/${commentId}`,
    {
      method:
        "PUT",

      body:
        JSON.stringify(
          {
            commentText,
          }
        ),
    }
  );

}


/*
 * =========================================================
 * DELETE TASK COMMENT
 * =========================================================
 */

export function deleteTaskComment(
  projectId,
  taskId,
  commentId
) {

  return apiRequest(
    `/api/projects/${projectId}/tasks/${taskId}/comments/${commentId}`,
    {
      method:
        "DELETE",
    }
  );

}


/*
 * =========================================================
 * GET TASK COMMENT COUNT
 * =========================================================
 */

export function getTaskCommentCount(
  projectId,
  taskId
) {

  return apiRequest(
    `/api/projects/${projectId}/tasks/${taskId}/comments/count`
  );

}