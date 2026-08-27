function TaskForm({
  projects = [],
  project = null,
  taskForm,
  editingTaskId,
  onChange,
  onSubmit,
  onCancel,
  fixedProject = false,
  sectionId = "task-form-section",
  fieldErrors = {},
  formError = "",
}) {
  const formDisabled =
    !fixedProject &&
    projects.length === 0;

  return (
    <section
      className="panel form-panel"
      id={sectionId}
    >
      <div className="section-header">
        <div>
          <span className="eyebrow">
            Task Management
          </span>

          <h2>
            {editingTaskId
              ? "Edit Task"
              : "Create Task"}
          </h2>

          <p>
            {fixedProject && project
              ? `This task belongs to ${project.name}.`
              : "Add a task and assign it to one of your projects."}
          </p>
        </div>
      </div>

      {formDisabled && (
        <div className="alert alert-info">
          Create a project before
          creating a task.
        </div>
      )}

      {formError && (
        <div className="alert alert-error">
          {formError}
        </div>
      )}

      <form
        className="form-layout"
        onSubmit={onSubmit}
      >
        {fixedProject && project ? (
          <div className="assignment-panel">
            <div>
              <span className="meta-label">
                Assigned Project
              </span>

              <strong>
                {project.name}
              </strong>
            </div>
          </div>
        ) : (
          <div className="form-field">
            <label htmlFor="task-project">
              Project
            </label>

            <select
              id="task-project"
              name="projectId"
              value={
                taskForm.projectId
              }
              onChange={onChange}
              disabled={formDisabled}
              className={
                fieldErrors.projectId
                  ? "input-error"
                  : ""
              }
            >
              <option value="">
                Select a project
              </option>

              {projects.map(
                (projectItem) => (
                  <option
                    key={projectItem.id}
                    value={projectItem.id}
                  >
                    {projectItem.name}
                  </option>
                )
              )}
            </select>

            {fieldErrors.projectId && (
              <span className="field-error">
                {fieldErrors.projectId}
              </span>
            )}
          </div>
        )}

        <div className="form-field">
          <label htmlFor="task-title">
            Title
          </label>

          <input
            id="task-title"
            type="text"
            name="title"
            value={taskForm.title}
            onChange={onChange}
            placeholder="Example: Build authentication flow"
            disabled={formDisabled}
            className={
              fieldErrors.title
                ? "input-error"
                : ""
            }
          />

          {fieldErrors.title && (
            <span className="field-error">
              {fieldErrors.title}
            </span>
          )}
        </div>

        <div className="form-field">
          <label htmlFor="task-description">
            Description
          </label>

          <textarea
            id="task-description"
            name="description"
            value={
              taskForm.description
            }
            onChange={onChange}
            placeholder="Describe the work required..."
            rows="4"
            disabled={formDisabled}
            className={
              fieldErrors.description
                ? "input-error"
                : ""
            }
          />

          {fieldErrors.description && (
            <span className="field-error">
              {fieldErrors.description}
            </span>
          )}
        </div>

        <div className="form-grid-two">
          <div className="form-field">
            <label htmlFor="task-status">
              Status
            </label>

            <select
              id="task-status"
              name="status"
              value={
                taskForm.status
              }
              onChange={onChange}
              disabled={formDisabled}
            >
              <option value="OPEN">
                Open
              </option>

              <option value="IN_PROGRESS">
                In Progress
              </option>

              <option value="COMPLETED">
                Completed
              </option>
            </select>

            {fieldErrors.status && (
              <span className="field-error">
                {fieldErrors.status}
              </span>
            )}
          </div>

          <div className="form-field">
            <label htmlFor="task-priority">
              Priority
            </label>

            <select
              id="task-priority"
              name="priority"
              value={
                taskForm.priority
              }
              onChange={onChange}
              disabled={formDisabled}
            >
              <option value="LOW">
                Low
              </option>

              <option value="MEDIUM">
                Medium
              </option>

              <option value="HIGH">
                High
              </option>
            </select>

            {fieldErrors.priority && (
              <span className="field-error">
                {fieldErrors.priority}
              </span>
            )}
          </div>
        </div>

        <div className="form-field">
          <label htmlFor="task-due-date">
            Due Date
          </label>

          <input
            id="task-due-date"
            type="date"
            name="dueDate"
            value={taskForm.dueDate}
            onChange={onChange}
            disabled={formDisabled}
          />

          {fieldErrors.dueDate && (
            <span className="field-error">
              {fieldErrors.dueDate}
            </span>
          )}
        </div>

        <div className="form-actions">
          <button
            type="submit"
            className="button button-primary"
            disabled={formDisabled}
          >
            {editingTaskId
              ? "Save Changes"
              : "Create Task"}
          </button>

          {editingTaskId && (
            <button
              type="button"
              className="button button-secondary"
              onClick={onCancel}
            >
              Cancel
            </button>
          )}
        </div>
      </form>
    </section>
  );
}

export default TaskForm;