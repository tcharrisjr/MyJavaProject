import { Link } from "react-router";

function getTodayLocalIso() {
  const now =
    new Date();

  const year =
    now.getFullYear();

  const month =
    String(
      now.getMonth() + 1
    ).padStart(2, "0");

  const day =
    String(
      now.getDate()
    ).padStart(2, "0");

  return `${year}-${month}-${day}`;
}

function formatDateTime(value) {
  if (!value) {
    return "";
  }

  return new Date(
    value
  ).toLocaleString();
}

function formatDate(value) {
  if (!value) {
    return "No due date";
  }

  const [
    year,
    month,
    day,
  ] = value.split("-");

  const date =
    new Date(
      Number(year),
      Number(month) - 1,
      Number(day)
    );

  return date.toLocaleDateString();
}

function TaskCard({
  task,
  onEdit,
  onDelete,
  showProject = true,
  showProjectLink = false,
}) {
  const statusLabel =
    task.status?.replaceAll(
      "_",
      " "
    ) || "";

  const statusClass =
    task.status
      ?.toLowerCase()
      .replaceAll(
        "_",
        "-"
      );

  const priorityClass =
    task.priority
      ?.toLowerCase();

  const today =
    getTodayLocalIso();

  const isOverdue =
    Boolean(task.dueDate) &&
    task.dueDate < today &&
    task.status !==
      "COMPLETED";

  return (
    <article
      className={
        isOverdue
          ? "task-card task-card-overdue"
          : "task-card"
      }
    >
      <div className="task-card-header">
        <div className="task-card-title">
          <span className="eyebrow">
            Task
          </span>

          <h3>
            {task.title}
          </h3>
        </div>

        <div className="task-badge-group">
          {isOverdue && (
            <span className="badge badge-danger">
              Overdue
            </span>
          )}

          <span
            className={
              `badge priority-badge priority-${priorityClass}`
            }
          >
            {task.priority}
          </span>
        </div>
      </div>

      <p className="task-description">
        {task.description ||
          "No task description has been provided."}
      </p>

      <div className="task-meta-grid">
        {showProject && (
          <div className="task-meta-item">
            <span className="meta-label">
              Project
            </span>

            <span className="meta-value">
              {task.project?.name ||
                "Unknown"}
            </span>
          </div>
        )}

        <div className="task-meta-item">
          <span className="meta-label">
            Status
          </span>

          <span
            className={
              `badge status-badge status-${statusClass}`
            }
          >
            {statusLabel}
          </span>
        </div>

        <div className="task-meta-item">
          <span className="meta-label">
            Due Date
          </span>

          <span
            className={
              isOverdue
                ? "meta-value overdue-text"
                : "meta-value"
            }
          >
            {formatDate(
              task.dueDate
            )}
          </span>
        </div>

        <div className="task-meta-item">
          <span className="meta-label">
            Task ID
          </span>

          <span className="meta-value">
            {task.id}
          </span>
        </div>
      </div>

      {(task.createdDate ||
        task.updatedDate ||
        task.completedDate) && (
        <div className="task-lifecycle">
          {task.createdDate && (
            <div>
              <span>
                Created
              </span>

              <strong>
                {formatDateTime(
                  task.createdDate
                )}
              </strong>
            </div>
          )}

          {task.updatedDate && (
            <div>
              <span>
                Updated
              </span>

              <strong>
                {formatDateTime(
                  task.updatedDate
                )}
              </strong>
            </div>
          )}

          {task.completedDate && (
            <div>
              <span>
                Completed
              </span>

              <strong>
                {formatDateTime(
                  task.completedDate
                )}
              </strong>
            </div>
          )}
        </div>
      )}

      <div className="card-actions">
        {showProjectLink &&
          task.project?.id && (
            <Link
              className="button button-secondary"
              to={`/projects/${task.project.id}`}
            >
              View Project
            </Link>
          )}

        <button
          type="button"
          className="button button-primary"
          onClick={() =>
            onEdit(task)
          }
        >
          Edit Task
        </button>

        <button
          type="button"
          className="button button-danger-ghost"
          onClick={() =>
            onDelete(task.id)
          }
        >
          Delete
        </button>
      </div>
    </article>
  );
}

export default TaskCard;