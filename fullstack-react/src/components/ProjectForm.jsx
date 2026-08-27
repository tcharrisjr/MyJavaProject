function ProjectForm({
  projectForm,
  editingProjectId,
  onChange,
  onSubmit,
  onCancel,
  fieldErrors = {},
  formError = "",
}) {
  return (
    <section className="panel form-panel">
      <div className="section-header">
        <div>
          <span className="eyebrow">
            Project Management
          </span>

          <h2>
            {editingProjectId
              ? "Edit Project"
              : "Create Project"}
          </h2>

          <p>
            {editingProjectId
              ? "Update the project name or description."
              : "Create a new project to organize related tasks."}
          </p>
        </div>
      </div>

      {formError && (
        <div className="alert alert-error">
          {formError}
        </div>
      )}

      <form
        className="form-layout"
        onSubmit={onSubmit}
      >
        <div className="form-field">
          <label htmlFor="project-name">
            Project Name
          </label>

          <input
            id="project-name"
            type="text"
            name="name"
            value={projectForm.name}
            onChange={onChange}
            placeholder="Example: Client Portal Redesign"
            className={
              fieldErrors.name
                ? "input-error"
                : ""
            }
          />

          {fieldErrors.name && (
            <span className="field-error">
              {fieldErrors.name}
            </span>
          )}
        </div>

        <div className="form-field">
          <label htmlFor="project-description">
            Description
          </label>

          <textarea
            id="project-description"
            name="description"
            value={
              projectForm.description
            }
            onChange={onChange}
            placeholder="Describe the purpose and scope of this project..."
            rows="4"
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

        <div className="form-actions">
          <button
            type="submit"
            className="button button-primary"
          >
            {editingProjectId
              ? "Save Changes"
              : "Create Project"}
          </button>

          {editingProjectId && (
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

export default ProjectForm;