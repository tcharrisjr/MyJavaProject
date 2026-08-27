import {
  useCallback,
  useEffect,
  useState,
} from "react";

import {
  Link,
  useParams,
} from "react-router";

import SummaryCard
  from "../components/SummaryCard";

import TaskCard
  from "../components/TaskCard";

import TaskFilters
  from "../components/TaskFilters";

import TaskForm
  from "../components/TaskForm";

import {
  getProjectActivity,
  getProjectById,
  getProjectHealth,
  getProjectStatsById,
} from "../api/projectApi";

import {
  createTask,
  deleteTask,
  getTasks,
  updateTask,
} from "../api/taskApi";


/*
 * =========================================================
 * FORMAT DATE / TIME
 * =========================================================
 */

function formatDateTime(
  value
) {

  if (
    !value
  ) {

    return "";

  }

  return new Date(
    value
  )
    .toLocaleString();

}


/*
 * =========================================================
 * PROJECT DETAILS
 * =========================================================
 */

function ProjectDetails() {

  const {
    projectId,
  } =
    useParams();


  /*
   * =======================================================
   * PROJECT STATE
   * =======================================================
   */

  const [
    project,
    setProject,
  ] =
    useState(
      null
    );


  /*
   * =======================================================
   * PROJECT STATISTICS
   * =======================================================
   */

  const [
    projectStats,
    setProjectStats,
  ] =
    useState({
      totalTasks: 0,
      openTasks: 0,
      inProgressTasks: 0,
      completedTasks: 0,
      overdueTasks: 0,
      completionPercentage: 0,
    });


  /*
   * =======================================================
   * PROJECT HEALTH
   * =======================================================
   */

  const [
    projectHealth,
    setProjectHealth,
  ] =
    useState({
      dueSoonTasks: 0,
      overdueTasks: 0,
      completionPercentage: 0,
      overduePercentage: 0,
      recentlyCompletedTasks: 0,
      healthStatus:
        "HEALTHY",
    });


  /*
   * =======================================================
   * PROJECT ACTIVITY
   * =======================================================
   */

  const [
    projectActivity,
    setProjectActivity,
  ] =
    useState({
      recentlyUpdated: [],
      recentlyCompleted: [],
    });


  /*
   * =======================================================
   * TASK STATE
   * =======================================================
   */

  const [
    tasks,
    setTasks,
  ] =
    useState(
      []
    );


  const [
    tasksLoading,
    setTasksLoading,
  ] =
    useState(
      false
    );


  /*
   * =======================================================
   * PAGE STATE
   * =======================================================
   */

  const [
    loading,
    setLoading,
  ] =
    useState(
      true
    );


  const [
    error,
    setError,
  ] =
    useState(
      ""
    );


  /*
   * =======================================================
   * SEARCH
   * =======================================================
   */

  const [
    searchText,
    setSearchText,
  ] =
    useState(
      ""
    );


  /*
   * Server-side search uses the debounced value.
   *
   * This keeps the existing 350 ms search delay while
   * preventing a request for every keystroke.
   */

  const [
    debouncedSearchText,
    setDebouncedSearchText,
  ] =
    useState(
      ""
    );


  /*
   * =======================================================
   * FILTERS
   * =======================================================
   */

  const [
    selectedStatusFilter,
    setSelectedStatusFilter,
  ] =
    useState(
      ""
    );


  const [
    selectedPriorityFilter,
    setSelectedPriorityFilter,
  ] =
    useState(
      ""
    );


  /*
   * =======================================================
   * SORTING
   * =======================================================
   */

  const [
    sortBy,
    setSortBy,
  ] =
    useState(
      "dueDate"
    );


  const [
    sortDirection,
    setSortDirection,
  ] =
    useState(
      "asc"
    );


  /*
   * =======================================================
   * PAGINATION
   * =======================================================
   */

  const [
    pageSize,
    setPageSize,
  ] =
    useState(
      10
    );


  const [
    currentPage,
    setCurrentPage,
  ] =
    useState(
      0
    );


  const [
    totalPages,
    setTotalPages,
  ] =
    useState(
      0
    );


  const [
    totalElements,
    setTotalElements,
  ] =
    useState(
      0
    );


  const [
    firstPage,
    setFirstPage,
  ] =
    useState(
      true
    );


  const [
    lastPage,
    setLastPage,
  ] =
    useState(
      true
    );


  /*
   * =======================================================
   * TASK FORM
   * =======================================================
   */

  const [
    taskForm,
    setTaskForm,
  ] =
    useState({
      title: "",
      description: "",
      status: "OPEN",
      priority: "MEDIUM",
      dueDate: "",
    });


  const [
    editingTaskId,
    setEditingTaskId,
  ] =
    useState(
      null
    );


  const [
    taskFieldErrors,
    setTaskFieldErrors,
  ] =
    useState(
      {}
    );


  const [
    taskFormError,
    setTaskFormError,
  ] =
    useState(
      ""
    );


  /*
   * =======================================================
   * LOAD PROJECT SHELL
   * =======================================================
   *
   * useCallback gives the loader a stable reference.
   *
   * This resolves:
   *
   * react-hooks/immutability
   * react-hooks/exhaustive-deps
   * =======================================================
   */

  const loadProjectShell =
    useCallback(
      async () => {

        try {

          setLoading(
            true
          );


          setError(
            ""
          );


          const [
            projectData,
            statsData,
            healthData,
            activityData,
          ] =
            await Promise.all([

              getProjectById(
                projectId
              ),

              getProjectStatsById(
                projectId
              ),

              getProjectHealth(
                projectId
              ),

              getProjectActivity(
                projectId
              ),

            ]);


          setProject(
            projectData
          );


          setProjectStats(
            statsData
          );


          setProjectHealth(
            healthData
          );


          setProjectActivity({

            recentlyUpdated:
              activityData
                ?.recentlyUpdated ||
              [],

            recentlyCompleted:
              activityData
                ?.recentlyCompleted ||
              [],

          });

        } catch (
          error
        ) {

          setError(
            error.message ||
              "Unable to load project."
          );

        } finally {

          setLoading(
            false
          );

        }

      },
      [
        projectId,
      ]
    );


  /*
   * =======================================================
   * LOAD TASKS
   * =======================================================
   */

  const loadTasks =
    useCallback(
      async () => {

        if (
          !projectId
        ) {

          return;

        }


        try {

          setTasksLoading(
            true
          );


          setError(
            ""
          );


          const data =
            await getTasks({

              page:
                currentPage,

              size:
                pageSize,

              projectId,

              status:
                selectedStatusFilter,

              priority:
                selectedPriorityFilter,

              search:
                debouncedSearchText,

              sortBy,

              direction:
                sortDirection,

            });


          setTasks(
            data.content ||
              []
          );


          setTotalPages(
            data.totalPages ||
              0
          );


          setTotalElements(
            data.totalElements ||
              0
          );


          setFirstPage(
            data.first ??
              true
          );


          setLastPage(
            data.last ??
              true
          );

        } catch (
          error
        ) {

          setError(
            error.message ||
              "Unable to load project tasks."
          );

        } finally {

          setTasksLoading(
            false
          );

        }

      },
      [
        projectId,
        currentPage,
        pageSize,
        selectedStatusFilter,
        selectedPriorityFilter,
        debouncedSearchText,
        sortBy,
        sortDirection,
      ]
    );


  /*
   * =======================================================
   * LOAD PROJECT SHELL EFFECT
   * =======================================================
   */

  useEffect(
    () => {

      /*
       * The Promise boundary prevents synchronous state
       * mutations directly inside the effect body.
       */

      Promise
        .resolve()
        .then(
          () =>
            loadProjectShell()
        );

    },
    [
      loadProjectShell,
    ]
  );


  /*
   * =======================================================
   * LOAD TASK PAGE EFFECT
   * =======================================================
   *
   * loadTasks changes whenever pagination, filters, sorting,
   * project ID, or debounced search changes.
   * =======================================================
   */

  useEffect(
    () => {

      Promise
        .resolve()
        .then(
          () =>
            loadTasks()
        );

    },
    [
      loadTasks,
    ]
  );


  /*
   * =======================================================
   * SEARCH DEBOUNCE
   * =======================================================
   */

  useEffect(
    () => {

      const timer =
        setTimeout(
          () => {

            /*
             * Every new search begins on page zero.
             */

            setCurrentPage(
              0
            );


            setDebouncedSearchText(
              searchText.trim()
            );

          },
          350
        );


      return () => {

        clearTimeout(
          timer
        );

      };

    },
    [
      searchText,
    ]
  );


  /*
   * =======================================================
   * REFRESH PROJECT METRICS
   * =======================================================
   */

  const refreshProjectMetrics =
    async () => {

      const [
        statsData,
        healthData,
        activityData,
      ] =
        await Promise.all([

          getProjectStatsById(
            projectId
          ),

          getProjectHealth(
            projectId
          ),

          getProjectActivity(
            projectId
          ),

        ]);


      setProjectStats(
        statsData
      );


      setProjectHealth(
        healthData
      );


      setProjectActivity({

        recentlyUpdated:
          activityData
            ?.recentlyUpdated ||
          [],

        recentlyCompleted:
          activityData
            ?.recentlyCompleted ||
          [],

      });

    };


  /*
   * =======================================================
   * TASK FORM CHANGE
   * =======================================================
   */

  const handleTaskChange =
    (
      event
    ) => {

      const {
        name,
        value,
      } =
        event.target;


      setTaskForm(
        (
          previous
        ) => ({
          ...previous,

          [name]:
            value,
        })
      );


      setTaskFieldErrors(
        (
          previous
        ) => ({
          ...previous,

          [name]:
            undefined,
        })
      );


      setTaskFormError(
        ""
      );

    };


  /*
   * =======================================================
   * RESET TASK FORM
   * =======================================================
   */

  const resetTaskForm =
    () => {

      setTaskForm({
        title: "",
        description: "",
        status: "OPEN",
        priority: "MEDIUM",
        dueDate: "",
      });


      setEditingTaskId(
        null
      );


      setTaskFieldErrors(
        {}
      );


      setTaskFormError(
        ""
      );

    };


  /*
   * =======================================================
   * CREATE / UPDATE TASK
   * =======================================================
   */

  const handleTaskSubmit =
    async (
      event
    ) => {

      event.preventDefault();


      try {

        const requestBody = {

          ...taskForm,

          projectId:
            Number(
              projectId
            ),

        };


        if (
          editingTaskId !==
          null
        ) {

          await updateTask(
            editingTaskId,
            requestBody
          );

        } else {

          await createTask(
            requestBody
          );

        }


        resetTaskForm();


        setCurrentPage(
          0
        );


        await loadTasks();


        await refreshProjectMetrics();

      } catch (
        error
      ) {

        setTaskFieldErrors(
          error.validationErrors ||
            {}
        );


        setTaskFormError(
          error.message ||
            "Unable to save task."
        );

      }

    };


  /*
   * =======================================================
   * EDIT TASK
   * =======================================================
   */

  const handleEditTask =
    (
      task
    ) => {

      setEditingTaskId(
        task.id
      );


      setTaskForm({

        title:
          task.title ||
          "",

        description:
          task.description ||
          "",

        status:
          task.status ||
          "OPEN",

        priority:
          task.priority ||
          "MEDIUM",

        dueDate:
          task.dueDate ||
          "",

      });


      document
        .getElementById(
          "project-task-form-section"
        )
        ?.scrollIntoView({
          behavior:
            "smooth",

          block:
            "start",
        });

    };


  /*
   * =======================================================
   * DELETE TASK
   * =======================================================
   */

  const handleDeleteTask =
    async (
      taskId
    ) => {

      if (
        !window.confirm(
          "Are you sure you want to delete this task?"
        )
      ) {

        return;

      }


      try {

        await deleteTask(
          taskId
        );


        await loadTasks();


        await refreshProjectMetrics();

      } catch (
        error
      ) {

        alert(
          error.message ||
            "Unable to delete task."
        );

      }

    };


  /*
   * =======================================================
   * INITIAL LOADING
   * =======================================================
   */

  if (
    loading
  ) {

    return (

      <main className="app-shell">

        <div className="loading-state">
          Loading project...
        </div>

      </main>

    );

  }


  /*
   * =======================================================
   * INITIAL ERROR
   * =======================================================
   */

  if (
    error &&
    !project
  ) {

    return (

      <main className="app-shell">

        <Link
          to="/"
          className="back-link"
        >
          Back to Dashboard
        </Link>


        <div className="alert alert-error">
          {error}
        </div>

      </main>

    );

  }


  /*
   * =======================================================
   * PROJECT HEALTH CLASS
   * =======================================================
   */

  const healthClass =
    projectHealth
      .healthStatus
      ?.toLowerCase()
      .replaceAll(
        "_",
        "-"
      );


  /*
   * =======================================================
   * RENDER
   * =======================================================
   */

  return (

    <main className="app-shell">

      <Link
        to="/"
        className="back-link"
      >
        ← Back to Dashboard
      </Link>


      {/* ==================================================
          PROJECT HERO
          ================================================== */}

      <section className="project-hero">

        <div>

          <span className="eyebrow">
            Project Workspace
          </span>


          <h1>
            {project.name}
          </h1>


          <p>
            {project.description ||
              "No project description provided."}
          </p>

        </div>


        <div className="project-hero-health">

          <span
            className={
              `health-badge health-${healthClass}`
            }
          >

            {projectHealth
              .healthStatus
              ?.replaceAll(
                "_",
                " "
              )}

          </span>


          <span>
            Project ID {project.id}
          </span>

        </div>

      </section>


      {/* ==================================================
          SUMMARY
          ================================================== */}

      <section className="summary-grid summary-grid-five">

        <SummaryCard
          value={
            projectStats.totalTasks
          }
          label="Total Tasks"
          tone="blue"
        />


        <SummaryCard
          value={
            projectHealth.dueSoonTasks
          }
          label="Due in 7 Days"
          tone="amber"
        />


        <SummaryCard
          value={
            projectStats.completedTasks
          }
          label="Completed"
          tone="green"
        />


        <SummaryCard
          value={
            projectStats.overdueTasks
          }
          label="Overdue"
          tone="red"
        />


        <SummaryCard
          value={
            `${projectHealth.overduePercentage}%`
          }
          label="Overdue Rate"
          tone="neutral"
        />

      </section>


      {/* ==================================================
          PROJECT HEALTH
          ================================================== */}

      <section className="panel health-panel">

        <div className="section-header">

          <div>

            <span className="eyebrow">
              Delivery Health
            </span>


            <h2>
              Project Health
            </h2>


            <p>
              Current schedule exposure
              and recent delivery performance.
            </p>

          </div>


          <span
            className={
              `health-badge health-${healthClass} health-badge-large`
            }
          >

            {projectHealth
              .healthStatus
              ?.replaceAll(
                "_",
                " "
              )}

          </span>

        </div>


        <div className="health-metrics">

          <div className="health-metric">

            <span>
              Due Soon
            </span>


            <strong>
              {projectHealth.dueSoonTasks}
            </strong>


            <small>
              Next 7 days
            </small>

          </div>


          <div className="health-metric">

            <span>
              Overdue
            </span>


            <strong>
              {projectHealth.overdueTasks}
            </strong>


            <small>
              {projectHealth.overduePercentage}%
              {" "}of total
            </small>

          </div>


          <div className="health-metric">

            <span>
              Recently Completed
            </span>


            <strong>
              {projectHealth
                .recentlyCompletedTasks}
            </strong>


            <small>
              Last 30 days
            </small>

          </div>


          <div className="health-metric">

            <span>
              Completion
            </span>


            <strong>
              {projectHealth
                .completionPercentage}%
            </strong>


            <small>
              Overall
            </small>

          </div>

        </div>

      </section>


      {/* ==================================================
          PROGRESS
          ================================================== */}

      <section className="panel progress-panel">

        <div className="progress-panel-header">

          <div>

            <span className="eyebrow">
              Delivery Progress
            </span>


            <h2>
              Project Completion
            </h2>


            <p>
              {projectStats.completedTasks}
              {" "}of{" "}
              {projectStats.totalTasks}
              {" "}tasks completed
            </p>

          </div>


          <strong className="large-progress-value">

            {projectStats
              .completionPercentage}%

          </strong>

        </div>


        <div className="progress-track progress-track-large">

          <div
            className="progress-fill"
            style={{
              width:
                `${projectStats.completionPercentage}%`,
            }}
          />

        </div>

      </section>


      {/* ==================================================
          TASK FORM
          ================================================== */}

      <TaskForm
        project={
          project
        }
        taskForm={
          taskForm
        }
        editingTaskId={
          editingTaskId
        }
        onChange={
          handleTaskChange
        }
        onSubmit={
          handleTaskSubmit
        }
        onCancel={
          resetTaskForm
        }
        fixedProject
        sectionId="project-task-form-section"
        fieldErrors={
          taskFieldErrors
        }
        formError={
          taskFormError
        }
      />


      {/* ==================================================
          ACTIVITY
          ================================================== */}

      <section className="activity-grid">

        <article className="panel">

          <div className="section-header compact">

            <div>

              <span className="eyebrow">
                Activity
              </span>


              <h2>
                Recently Updated
              </h2>

            </div>

          </div>


          {projectActivity
            .recentlyUpdated
            .length ===
          0 ? (

            <div className="empty-state compact">
              No recent updates.
            </div>

          ) : (

            <div className="activity-list">

              {projectActivity
                .recentlyUpdated
                .map(
                  (
                    task
                  ) => (

                    <div
                      className="activity-row"
                      key={
                        task.id
                      }
                    >

                      <div>

                        <strong>
                          {task.title}
                        </strong>


                        <span>

                          {task.status
                            ?.replaceAll(
                              "_",
                              " "
                            )}

                        </span>

                      </div>


                      <time>

                        {formatDateTime(
                          task.updatedDate
                        )}

                      </time>

                    </div>

                  )
                )}

            </div>

          )}

        </article>


        <article className="panel">

          <div className="section-header compact">

            <div>

              <span className="eyebrow">
                Milestones
              </span>


              <h2>
                Recently Completed
              </h2>

            </div>

          </div>


          {projectActivity
            .recentlyCompleted
            .length ===
          0 ? (

            <div className="empty-state compact">
              No recent completions.
            </div>

          ) : (

            <div className="activity-list">

              {projectActivity
                .recentlyCompleted
                .map(
                  (
                    task
                  ) => (

                    <div
                      className="activity-row"
                      key={
                        task.id
                      }
                    >

                      <div>

                        <strong>
                          {task.title}
                        </strong>


                        <span>
                          Completed
                        </span>

                      </div>


                      <time>

                        {formatDateTime(
                          task.completedDate
                        )}

                      </time>

                    </div>

                  )
                )}

            </div>

          )}

        </article>

      </section>


      {/* ==================================================
          TASK FILTERS
          ================================================== */}

      <TaskFilters
        projects={
          []
        }
        searchText={
          searchText
        }
        selectedStatusFilter={
          selectedStatusFilter
        }
        selectedPriorityFilter={
          selectedPriorityFilter
        }
        sortBy={
          sortBy
        }
        sortDirection={
          sortDirection
        }
        pageSize={
          pageSize
        }
        visibleCount={
          tasks.length
        }
        totalCount={
          totalElements
        }
        onSearchChange={
          setSearchText
        }
        onProjectFilterChange={
          () => {}
        }
        onStatusFilterChange={
          (
            value
          ) => {

            setSelectedStatusFilter(
              value
            );


            setCurrentPage(
              0
            );

          }
        }
        onPriorityFilterChange={
          (
            value
          ) => {

            setSelectedPriorityFilter(
              value
            );


            setCurrentPage(
              0
            );

          }
        }
        onSortByChange={
          (
            value
          ) => {

            setSortBy(
              value
            );


            setCurrentPage(
              0
            );

          }
        }
        onSortDirectionChange={
          (
            value
          ) => {

            setSortDirection(
              value
            );


            setCurrentPage(
              0
            );

          }
        }
        onPageSizeChange={
          (
            value
          ) => {

            setPageSize(
              value
            );


            setCurrentPage(
              0
            );

          }
        }
        onClearFilters={
          () => {

            setSearchText(
              ""
            );


            setDebouncedSearchText(
              ""
            );


            setSelectedStatusFilter(
              ""
            );


            setSelectedPriorityFilter(
              ""
            );


            setSortBy(
              "dueDate"
            );


            setSortDirection(
              "asc"
            );


            setPageSize(
              10
            );


            setCurrentPage(
              0
            );

          }
        }
        hideProjectFilter
      />


      {/* ==================================================
          TASK LIST
          ================================================== */}

      <section className="content-section">

        <div className="section-heading-row">

          <div>

            <span className="eyebrow">
              Work Items
            </span>


            <h2>
              Project Tasks
            </h2>


            <p>
              Showing{" "}
              {tasks.length}
              {" "}of{" "}
              {totalElements}
              {" "}matching tasks.
            </p>

          </div>

        </div>


        {tasksLoading ? (

          <div className="loading-state">
            Loading tasks...
          </div>

        ) : tasks.length ===
          0 ? (

          <div className="empty-state">

            No tasks match the
            current filters.

          </div>

        ) : (

          <div className="task-grid">

            {tasks.map(
              (
                task
              ) => (

                <TaskCard
                  key={
                    task.id
                  }
                  task={
                    task
                  }
                  onEdit={
                    handleEditTask
                  }
                  onDelete={
                    handleDeleteTask
                  }
                  showProject={
                    false
                  }
                  showProjectLink={
                    false
                  }
                />

              )
            )}

          </div>

        )}

      </section>


      {/* ==================================================
          PAGINATION
          ================================================== */}

      {totalElements >
        0 && (

        <nav className="pagination">

          <button
            className="button button-secondary"
            disabled={
              firstPage ||
              tasksLoading
            }
            onClick={
              () =>
                setCurrentPage(
                  (
                    page
                  ) =>
                    Math.max(
                      0,
                      page - 1
                    )
                )
            }
          >
            Previous
          </button>


          <div className="pagination-status">

            <strong>

              Page{" "}
              {currentPage + 1}
              {" "}of{" "}
              {totalPages}

            </strong>


            <span>

              {totalElements}
              {" "}matching tasks

            </span>

          </div>


          <button
            className="button button-secondary"
            disabled={
              lastPage ||
              tasksLoading
            }
            onClick={
              () =>
                setCurrentPage(
                  (
                    page
                  ) =>
                    page + 1
                )
            }
          >
            Next
          </button>

        </nav>

      )}

    </main>

  );

}


export default ProjectDetails;