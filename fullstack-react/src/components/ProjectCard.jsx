import { Link } from "react-router";

function formatHealthStatus(
  value
) {
  if (!value) {
    return "Unknown";
  }

  return value
    .replaceAll("_", " ")
    .toLowerCase()
    .replace(
      /\b\w/g,
      (letter) =>
        letter.toUpperCase()
    );
}

function ProjectCard({
  project,
  stats = null,
  health = null,
  onEdit,
  onDelete,
}) {
  const totalTasks =
    stats?.totalTasks || 0;

  const completedTasks =
    stats?.completedTasks || 0;

  const openTasks =
    stats?.openTasks || 0;

  const inProgressTasks =
    stats?.inProgressTasks || 0;

  const completionPercentage =
    stats?.completionPercentage || 0;

  const healthStatus =
    health?.healthStatus ||
    "HEALTHY";

  const healthClass =
    healthStatus
      .toLowerCase()
      .replaceAll("_", "-");

  return (
    <article className="project-card">
      <div className="project-card-top">
        <div className="project-card-title-group">
          <span className="eyebrow">
            Project
          </span>

          <h3>
            {project.name}
          </h3>

          <span className="record-id">
            ID {project.id}
          </span>
        </div>

        <span
          className={
            `health-badge health-${healthClass}`
          }
        >
          {formatHealthStatus(
            healthStatus
          )}
        </span>
      </div>

      <p className="project-card-description">
        {project.description ||
          "No project description has been provided."}
      </p>

      <div className="health-summary-row">
        <div>
          <span className="meta-label">
            Due in 7 days
          </span>

          <strong>
            {health?.dueSoonTasks || 0}
          </strong>
        </div>

        <div>
          <span className="meta-label">
            Overdue
          </span>

          <strong>
            {health?.overdueTasks || 0}
          </strong>
        </div>

        <div>
          <span className="meta-label">
            Overdue Rate
          </span>

          <strong>
            {health?.overduePercentage || 0}%
          </strong>
        </div>
      </div>

      <div className="project-progress-section">
        <div className="project-progress-header">
          <div>
            <span className="progress-heading">
              Completion
            </span>

            <span className="progress-description">
              {completedTasks} of{" "}
              {totalTasks} tasks complete
            </span>
          </div>

          <strong className="progress-percentage">
            {completionPercentage}%
          </strong>
        </div>

        <div
          className="progress-track"
          role="progressbar"
          aria-valuemin="0"
          aria-valuemax="100"
          aria-valuenow={
            completionPercentage
          }
        >
          <div
            className="progress-fill"
            style={{
              width:
                `${completionPercentage}%`,
            }}
          />
        </div>
      </div>

      <div className="project-metrics">
        <div className="project-metric">
          <span className="project-metric-value">
            {totalTasks}
          </span>

          <span className="project-metric-label">
            Total
          </span>
        </div>

        <div className="project-metric">
          <span className="project-metric-value">
            {openTasks}
          </span>

          <span className="project-metric-label">
            Open
          </span>
        </div>

        <div className="project-metric">
          <span className="project-metric-value">
            {inProgressTasks}
          </span>

          <span className="project-metric-label">
            In Progress
          </span>
        </div>

        <div className="project-metric">
          <span className="project-metric-value">
            {completedTasks}
          </span>

          <span className="project-metric-label">
            Completed
          </span>
        </div>
      </div>

      <div className="card-actions">
        <Link
          className="button button-primary"
          to={`/projects/${project.id}`}
        >
          View Project
        </Link>

        <button
          type="button"
          className="button button-secondary"
          onClick={() =>
            onEdit(project)
          }
        >
          Edit
        </button>

        <button
          type="button"
          className="button button-danger-ghost"
          onClick={() =>
            onDelete(project.id)
          }
        >
          Delete
        </button>
      </div>
    </article>
  );
}

export default ProjectCard;