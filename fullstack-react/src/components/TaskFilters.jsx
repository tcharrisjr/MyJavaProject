function TaskFilters({
  projects = [],
  searchText,
  selectedProjectFilter = "",
  selectedStatusFilter,
  selectedPriorityFilter,
  sortBy,
  sortDirection,
  pageSize,
  visibleCount,
  totalCount,
  onSearchChange,
  onProjectFilterChange,
  onStatusFilterChange,
  onPriorityFilterChange,
  onSortByChange,
  onSortDirectionChange,
  onPageSizeChange,
  onClearFilters,
  hideProjectFilter = false,
}) {
  return (
    <section className="panel filter-panel">
      <div className="filter-toolbar-header">
        <div>
          <span className="eyebrow">
            Task Explorer
          </span>

          <h2>
            Tasks
          </h2>

          <p>
            Showing {visibleCount} of{" "}
            {totalCount} matching tasks
          </p>
        </div>

        <button
          type="button"
          className="button button-secondary"
          onClick={
            onClearFilters
          }
        >
          Reset Filters
        </button>
      </div>

      <div className="filter-toolbar">
        <div className="form-field filter-search">
          <label htmlFor="task-search">
            Search
          </label>

          <input
            id="task-search"
            type="search"
            value={searchText}
            onChange={(event) =>
              onSearchChange(
                event.target.value
              )
            }
            placeholder="Search tasks..."
          />
        </div>

        {!hideProjectFilter && (
          <div className="form-field">
            <label htmlFor="filter-project">
              Project
            </label>

            <select
              id="filter-project"
              value={
                selectedProjectFilter
              }
              onChange={(event) =>
                onProjectFilterChange(
                  event.target.value
                )
              }
            >
              <option value="">
                All Projects
              </option>

              {projects.map(
                (project) => (
                  <option
                    key={project.id}
                    value={project.id}
                  >
                    {project.name}
                  </option>
                )
              )}
            </select>
          </div>
        )}

        <div className="form-field">
          <label htmlFor="filter-status">
            Status
          </label>

          <select
            id="filter-status"
            value={
              selectedStatusFilter
            }
            onChange={(event) =>
              onStatusFilterChange(
                event.target.value
              )
            }
          >
            <option value="">
              All Statuses
            </option>

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
        </div>

        <div className="form-field">
          <label htmlFor="filter-priority">
            Priority
          </label>

          <select
            id="filter-priority"
            value={
              selectedPriorityFilter
            }
            onChange={(event) =>
              onPriorityFilterChange(
                event.target.value
              )
            }
          >
            <option value="">
              All Priorities
            </option>

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
        </div>

        <div className="form-field">
          <label htmlFor="filter-sort">
            Sort By
          </label>

          <select
            id="filter-sort"
            value={sortBy}
            onChange={(event) =>
              onSortByChange(
                event.target.value
              )
            }
          >
            <option value="dueDate">
              Due Date
            </option>

            <option value="title">
              Title
            </option>

            <option value="priority">
              Priority
            </option>

            <option value="status">
              Status
            </option>

            <option value="createdDate">
              Created Date
            </option>

            <option value="updatedDate">
              Updated Date
            </option>
          </select>
        </div>

        <div className="form-field">
          <label htmlFor="filter-direction">
            Direction
          </label>

          <select
            id="filter-direction"
            value={
              sortDirection
            }
            onChange={(event) =>
              onSortDirectionChange(
                event.target.value
              )
            }
          >
            <option value="asc">
              Ascending
            </option>

            <option value="desc">
              Descending
            </option>
          </select>
        </div>

        <div className="form-field">
          <label htmlFor="filter-page-size">
            Per Page
          </label>

          <select
            id="filter-page-size"
            value={pageSize}
            onChange={(event) =>
              onPageSizeChange(
                Number(
                  event.target.value
                )
              )
            }
          >
            <option value={5}>
              5
            </option>

            <option value={10}>
              10
            </option>

            <option value={20}>
              20
            </option>

            <option value={50}>
              50
            </option>
          </select>
        </div>
      </div>
    </section>
  );
}

export default TaskFilters;