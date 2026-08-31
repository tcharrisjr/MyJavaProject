import {
  useCallback,
  useEffect,
  useState,
} from "react";

import {
  getProjects,
  createProject,
  updateProject,
  deleteProject,
  getProjectHealth,
  getProjectTasksPage,
  createTask,
  updateTask,
  deleteTask,
} from "../api/apiClient";

import {
  getAssignees,
} from "../api/authApi";

import "../Dashboard.css";


function Dashboard() {

  // =========================================================
  // PROJECT STATE
  // =========================================================

  const [
    projects,
    setProjects,
  ] = useState([]);

  const [
    selectedProject,
    setSelectedProject,
  ] = useState(null);

  const [
    showProjectForm,
    setShowProjectForm,
  ] = useState(false);

  const [
    editingProject,
    setEditingProject,
  ] = useState(null);

  const [
    projectForm,
    setProjectForm,
  ] = useState({
    name: "",
    description: "",
  });


  // =========================================================
  // TASK STATE
  // =========================================================

  const [
    tasks,
    setTasks,
  ] = useState([]);

  const [
    showTaskForm,
    setShowTaskForm,
  ] = useState(false);

  const [
    editingTask,
    setEditingTask,
  ] = useState(null);

  const [
    taskForm,
    setTaskForm,
  ] = useState({
    title: "",
    description: "",
    status: "OPEN",
    priority: "MEDIUM",
    dueDate: "",
    assigneeId: "",
  });


  // =========================================================
  // SEQUENCE 13A - ASSIGNEES
  // =========================================================

  const [
    assignees,
    setAssignees,
  ] = useState([]);

  const [
    assigneesLoading,
    setAssigneesLoading,
  ] = useState(false);


  // =========================================================
  // PROJECT HEALTH
  // =========================================================

  const [
    projectHealth,
    setProjectHealth,
  ] = useState({
    totalTasks: 0,
    openTasks: 0,
    inProgressTasks: 0,
    completedTasks: 0,
    overdueTasks: 0,
    dueSoonTasks: 0,
    completionPercentage: 0,
  });


  // =========================================================
  // FILTER STATE
  // =========================================================

  const [
    searchTerm,
    setSearchTerm,
  ] = useState("");

  const [
    debouncedSearch,
    setDebouncedSearch,
  ] = useState("");

  const [
    statusFilter,
    setStatusFilter,
  ] = useState("ALL");

  const [
    priorityFilter,
    setPriorityFilter,
  ] = useState("ALL");

  const [
    dueDateFilter,
    setDueDateFilter,
  ] = useState("ALL");

  const [
    sortOption,
    setSortOption,
  ] = useState(
    "DUE_DATE_ASC"
  );


  // =========================================================
  // PAGINATION
  // =========================================================

  const [
    currentPage,
    setCurrentPage,
  ] = useState(0);

  const [
    pageSize,
    setPageSize,
  ] = useState(10);

  const [
    totalElements,
    setTotalElements,
  ] = useState(0);

  const [
    totalPages,
    setTotalPages,
  ] = useState(0);

  const [
    numberOfElements,
    setNumberOfElements,
  ] = useState(0);

  const [
    firstPage,
    setFirstPage,
  ] = useState(true);

  const [
    lastPage,
    setLastPage,
  ] = useState(true);


  // =========================================================
  // UI STATE
  // =========================================================

  const [
    loading,
    setLoading,
  ] = useState(true);

  const [
    tasksLoading,
    setTasksLoading,
  ] = useState(false);

  const [
    healthLoading,
    setHealthLoading,
  ] = useState(false);

  const [
    error,
    setError,
  ] = useState("");

  const [
    successMessage,
    setSuccessMessage,
  ] = useState("");

  const [
    updatingTaskId,
    setUpdatingTaskId,
  ] = useState(null);


  // =========================================================
  // SEQUENCE 13A - LOAD ASSIGNEES
  // =========================================================

  const loadAssignees =
    useCallback(
      async () => {

        try {

          setAssigneesLoading(
            true
          );


          const response =
            await getAssignees();


          setAssignees(
            Array.isArray(
              response
            )
              ? response
              : []
          );

        } catch (
          err
        ) {

          console.error(
            "Unable to load assignees:",
            err
          );


          setAssignees(
            []
          );


          setError(
            err?.message ||
              "Unable to load task assignees."
          );

        } finally {

          setAssigneesLoading(
            false
          );

        }

      },
      []
    );


  useEffect(
    () => {

      loadAssignees();

    },
    [
      loadAssignees,
    ]
  );


  // =========================================================
  // INITIAL PROJECT LOAD
  // =========================================================

  useEffect(
    () => {

      let active =
        true;


      getProjects()

        .then(
          (
            projectData
          ) => {

            if (
              !active
            ) {

              return;

            }


            const safeProjects =
              Array.isArray(
                projectData
              )
                ? projectData
                : [];


            setProjects(
              safeProjects
            );

          }
        )

        .catch(
          (
            err
          ) => {

            if (
              !active
            ) {

              return;

            }


            console.error(
              "Unable to load projects:",
              err
            );


            setError(
              err?.message ||
                "Unable to load projects."
            );

          }
        )

        .finally(
          () => {

            if (
              active
            ) {

              setLoading(
                false
              );

            }

          }
        );


      return () => {

        active =
          false;

      };

    },
    []
  );


  // =========================================================
  // SEARCH DEBOUNCE
  // =========================================================

  useEffect(
    () => {

      const timer =
        window.setTimeout(
          () => {

            setDebouncedSearch(
              searchTerm.trim()
            );


            setCurrentPage(
              0
            );

          },
          350
        );


      return () => {

        window.clearTimeout(
          timer
        );

      };

    },
    [
      searchTerm,
    ]
  );


  // =========================================================
  // LOAD PROJECTS
  // =========================================================

  const loadProjects =
    async () => {

      try {

        setLoading(
          true
        );


        setError(
          ""
        );


        const projectData =
          await getProjects();


        const safeProjects =
          Array.isArray(
            projectData
          )
            ? projectData
            : [];


        setProjects(
          safeProjects
        );


        if (
          selectedProject
        ) {

          const refreshedProject =
            safeProjects.find(
              (
                project
              ) =>
                project.id ===
                selectedProject.id
            );


          if (
            refreshedProject
          ) {

            setSelectedProject(
              refreshedProject
            );

          } else {

            clearSelectedProject();

          }

        }

      } catch (
        err
      ) {

        console.error(
          "Unable to load projects:",
          err
        );


        setError(
          err?.message ||
            "Unable to load projects."
        );

      } finally {

        setLoading(
          false
        );

      }

    };


  // =========================================================
  // RESET PROJECT HEALTH
  // =========================================================

  const resetProjectHealth =
    () => {

      setProjectHealth({
        totalTasks: 0,
        openTasks: 0,
        inProgressTasks: 0,
        completedTasks: 0,
        overdueTasks: 0,
        dueSoonTasks: 0,
        completionPercentage: 0,
      });

    };


  // =========================================================
  // CLEAR SELECTED PROJECT
  // =========================================================

  const clearSelectedProject =
    () => {

      setSelectedProject(
        null
      );


      setTasks(
        []
      );


      resetProjectHealth();


      setTotalElements(
        0
      );


      setTotalPages(
        0
      );


      setNumberOfElements(
        0
      );


      setCurrentPage(
        0
      );


      setFirstPage(
        true
      );


      setLastPage(
        true
      );

    };


  // =========================================================
  // LOAD PROJECT HEALTH
  // =========================================================

  const loadProjectHealth =
    async (
      projectId
    ) => {

      try {

        setHealthLoading(
          true
        );


        const response =
          await getProjectHealth(
            projectId
          );


        setProjectHealth({
          totalTasks:
            response?.totalTasks ??
            0,

          openTasks:
            response?.openTasks ??
            0,

          inProgressTasks:
            response?.inProgressTasks ??
            0,

          completedTasks:
            response?.completedTasks ??
            0,

          overdueTasks:
            response?.overdueTasks ??
            0,

          dueSoonTasks:
            response?.dueSoonTasks ??
            0,

          completionPercentage:
            response?.completionPercentage ??
            0,
        });

      } catch (
        err
      ) {

        console.error(
          "Unable to load project health:",
          err
        );


        resetProjectHealth();


        throw err;

      } finally {

        setHealthLoading(
          false
        );

      }

    };


  // =========================================================
  // SERVER SORT
  // =========================================================

  const getServerSort =
    useCallback(
      () => {

        switch (
          sortOption
        ) {

          case "DUE_DATE_DESC":

            return {
              sortBy:
                "dueDate",

              sortDirection:
                "desc",
            };


          case "PRIORITY":

            return {
              sortBy:
                "priority",

              sortDirection:
                "asc",
            };


          case "STATUS":

            return {
              sortBy:
                "status",

              sortDirection:
                "asc",
            };


          case "TITLE":

            return {
              sortBy:
                "title",

              sortDirection:
                "asc",
            };


          case "UPDATED_DESC":

            return {
              sortBy:
                "updatedDate",

              sortDirection:
                "desc",
            };


          default:

            return {
              sortBy:
                "dueDate",

              sortDirection:
                "asc",
            };

        }

      },
      [
        sortOption,
      ]
    );


  // =========================================================
  // LOAD TASK PAGE
  // =========================================================

  const loadTaskPage =
    useCallback(
      async (
        projectId
      ) => {

        try {

          setTasksLoading(
            true
          );


          setError(
            ""
          );


          const serverSort =
            getServerSort();


          const response =
            await getProjectTasksPage(
              projectId,
              {
                page:
                  currentPage,

                size:
                  pageSize,

                status:
                  statusFilter,

                priority:
                  priorityFilter,

                search:
                  debouncedSearch,

                dueDateFilter,

                sortBy:
                  serverSort.sortBy,

                sortDirection:
                  serverSort.sortDirection,
              }
            );


          setTasks(
            Array.isArray(
              response?.content
            )
              ? response.content
              : []
          );


          setTotalElements(
            response?.totalElements ??
              0
          );


          setTotalPages(
            response?.totalPages ??
              0
          );


          setNumberOfElements(
            response?.numberOfElements ??
              0
          );


          setFirstPage(
            response?.first ??
              true
          );


          setLastPage(
            response?.last ??
              true
          );


          if (
            typeof response?.number ===
            "number"
          ) {

            setCurrentPage(
              response.number
            );

          }

        } catch (
          err
        ) {

          console.error(
            "Unable to load task page:",
            err
          );


          setError(
            err?.message ||
              "Unable to load tasks."
          );


          setTasks(
            []
          );

        } finally {

          setTasksLoading(
            false
          );

        }

      },
      [
        currentPage,
        pageSize,
        statusFilter,
        priorityFilter,
        debouncedSearch,
        dueDateFilter,
        getServerSort,
      ]
    );


  // =========================================================
  // TASK PAGE EFFECT
  // =========================================================

  useEffect(
    () => {

      if (
        !selectedProject
      ) {

        return;

      }


      Promise.resolve()
        .then(
          () =>
            loadTaskPage(
              selectedProject.id
            )
        );

    },
    [
      selectedProject,
      loadTaskPage,
    ]
  );


  // =========================================================
  // REFRESH TASK DATA
  // =========================================================

  const refreshTaskData =
    async () => {

      if (
        !selectedProject
      ) {

        return;

      }


      await Promise.all([
        loadProjectHealth(
          selectedProject.id
        ),

        loadTaskPage(
          selectedProject.id
        ),
      ]);

    };


  // =========================================================
  // SELECT PROJECT
  // =========================================================

  const handleSelectProject =
    async (
      project
    ) => {

      setSelectedProject(
        project
      );


      setSearchTerm(
        ""
      );


      setDebouncedSearch(
        ""
      );


      setStatusFilter(
        "ALL"
      );


      setPriorityFilter(
        "ALL"
      );


      setDueDateFilter(
        "ALL"
      );


      setSortOption(
        "DUE_DATE_ASC"
      );


      setCurrentPage(
        0
      );


      setShowTaskForm(
        false
      );


      setEditingTask(
        null
      );


      try {

        await loadProjectHealth(
          project.id
        );

      } catch (
        err
      ) {

        setError(
          err?.message ||
            "Unable to load project health."
        );

      }

    };


  // =========================================================
  // PROJECT FORM HELPERS
  // =========================================================

  const resetProjectForm =
    () => {

      setProjectForm({
        name: "",
        description: "",
      });


      setEditingProject(
        null
      );


      setShowProjectForm(
        false
      );

    };


  const handleProjectInputChange =
    (
      event
    ) => {

      const {
        name,
        value,
      } =
        event.target;


      setProjectForm(
        (
          previous
        ) => ({
          ...previous,

          [name]:
            value,
        })
      );

    };


  // =========================================================
  // SAVE PROJECT
  // =========================================================

  const handleProjectSubmit =
    async (
      event
    ) => {

      event.preventDefault();


      try {

        setError(
          ""
        );


        setSuccessMessage(
          ""
        );


        if (
          editingProject
        ) {

          await updateProject(
            editingProject.id,
            projectForm
          );


          setSuccessMessage(
            "Project updated successfully."
          );

        } else {

          await createProject(
            projectForm
          );


          setSuccessMessage(
            "Project created successfully."
          );

        }


        resetProjectForm();


        await loadProjects();

      } catch (
        err
      ) {

        console.error(
          "Unable to save project:",
          err
        );


        setError(
          err?.message ||
            "Unable to save project."
        );

      }

    };


  // =========================================================
  // EDIT PROJECT
  // =========================================================

  const handleEditProject =
    (
      project,
      event
    ) => {

      event.stopPropagation();


      setEditingProject(
        project
      );


      setProjectForm({
        name:
          project.name ||
          "",

        description:
          project.description ||
          "",
      });


      setShowProjectForm(
        true
      );


      window.scrollTo({
        top: 0,

        behavior:
          "smooth",
      });

    };


  // =========================================================
  // DELETE PROJECT
  // =========================================================

  const handleDeleteProject =
    async (
      project,
      event
    ) => {

      event.stopPropagation();


      const confirmed =
        window.confirm(
          `Delete "${project.name}" and its tasks?`
        );


      if (
        !confirmed
      ) {

        return;

      }


      try {

        setError(
          ""
        );


        setSuccessMessage(
          ""
        );


        await deleteProject(
          project.id
        );


        if (
          selectedProject?.id ===
          project.id
        ) {

          clearSelectedProject();

        }


        setSuccessMessage(
          "Project deleted successfully."
        );


        await loadProjects();

      } catch (
        err
      ) {

        console.error(
          "Unable to delete project:",
          err
        );


        setError(
          err?.message ||
            "Unable to delete project."
        );

      }

    };


  // =========================================================
  // TASK FORM HELPERS
  // =========================================================

  const resetTaskForm =
    () => {

      setTaskForm({
        title: "",
        description: "",
        status: "OPEN",
        priority: "MEDIUM",
        dueDate: "",
        assigneeId: "",
      });


      setEditingTask(
        null
      );


      setShowTaskForm(
        false
      );

    };


  const handleTaskInputChange =
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

    };


  // =========================================================
  // SAVE TASK
  // =========================================================

  const handleTaskSubmit =
    async (
      event
    ) => {

      event.preventDefault();


      if (
        !selectedProject
      ) {

        setError(
          "Select a project before creating a task."
        );


        return;

      }


      try {

        setError(
          ""
        );


        setSuccessMessage(
          ""
        );


        const taskPayload = {
          ...taskForm,

          assigneeId:
            taskForm.assigneeId
              ? Number(
                  taskForm.assigneeId
                )
              : null,
        };


        if (
          editingTask
        ) {

          await updateTask(
            selectedProject.id,
            editingTask.id,
            taskPayload
          );


          setSuccessMessage(
            "Task updated successfully."
          );

        } else {

          await createTask(
            selectedProject.id,
            taskPayload
          );


          setSuccessMessage(
            "Task created successfully."
          );

        }


        resetTaskForm();


        setCurrentPage(
          0
        );


        await refreshTaskData();

      } catch (
        err
      ) {

        console.error(
          "Unable to save task:",
          err
        );


        setError(
          err?.message ||
            "Unable to save task."
        );

      }

    };


  // =========================================================
  // EDIT TASK
  // =========================================================

  const handleEditTask =
    (
      task
    ) => {

      setEditingTask(
        task
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

        assigneeId:
          task.assigneeId != null
            ? String(
                task.assigneeId
              )
            : "",
      });


      setShowTaskForm(
        true
      );

    };


  // =========================================================
  // DELETE TASK
  // =========================================================

  const handleDeleteTask =
    async (
      taskId
    ) => {

      if (
        !selectedProject
      ) {

        return;

      }


      const confirmed =
        window.confirm(
          "Are you sure you want to delete this task?"
        );


      if (
        !confirmed
      ) {

        return;

      }


      try {

        setError(
          ""
        );


        setSuccessMessage(
          ""
        );


        await deleteTask(
          selectedProject.id,
          taskId
        );


        setSuccessMessage(
          "Task deleted successfully."
        );


        if (
          numberOfElements === 1 &&
          currentPage > 0
        ) {

          setCurrentPage(
            (
              page
            ) =>
              page - 1
          );


          await loadProjectHealth(
            selectedProject.id
          );

        } else {

          await refreshTaskData();

        }

      } catch (
        err
      ) {

        console.error(
          "Unable to delete task:",
          err
        );


        setError(
          err?.message ||
            "Unable to delete task."
        );

      }

    };


  // =========================================================
  // TASK PAYLOAD
  // =========================================================

  const buildTaskPayload =
    (
      task,
      overrides = {}
    ) => ({
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

      assigneeId:
        task.assigneeId ??
        null,

      ...overrides,
    });


  // =========================================================
  // STATUS DISPLAY
  // =========================================================

  const formatStatus =
    (
      status
    ) => {

      if (
        !status
      ) {

        return "";

      }


      return status
        .replaceAll(
          "_",
          " "
        )
        .toLowerCase()
        .replace(
          /\b\w/g,
          (
            character
          ) =>
            character.toUpperCase()
        );

    };


  const formatPriority =
    (
      priority
    ) => {

      if (
        !priority
      ) {

        return "";

      }


      return priority
        .toLowerCase()
        .replace(
          /\b\w/g,
          (
            character
          ) =>
            character.toUpperCase()
        );

    };


  // =========================================================
  // QUICK STATUS
  // =========================================================

  const handleQuickStatusChange =
    async (
      task,
      newStatus
    ) => {

      if (
        !selectedProject
      ) {

        return;

      }


      if (
        task.status ===
        newStatus
      ) {

        return;

      }


      try {

        setUpdatingTaskId(
          task.id
        );


        setError(
          ""
        );


        setSuccessMessage(
          ""
        );


        await updateTask(
          selectedProject.id,
          task.id,
          buildTaskPayload(
            task,
            {
              status:
                newStatus,
            }
          )
        );


        setSuccessMessage(
          `Task status changed to ${formatStatus(
            newStatus
          )}.`
        );


        await refreshTaskData();

      } catch (
        err
      ) {

        console.error(
          "Unable to update task status:",
          err
        );


        setError(
          err?.message ||
            "Unable to update task status."
        );

      } finally {

        setUpdatingTaskId(
          null
        );

      }

    };


  // =========================================================
  // COMPLETE TASK
  // =========================================================

  const handleMarkTaskComplete =
    async (
      task
    ) => {

      if (
        !selectedProject
      ) {

        return;

      }


      if (
        task.status ===
        "COMPLETED"
      ) {

        return;

      }


      try {

        setUpdatingTaskId(
          task.id
        );


        setError(
          ""
        );


        setSuccessMessage(
          ""
        );


        await updateTask(
          selectedProject.id,
          task.id,
          buildTaskPayload(
            task,
            {
              status:
                "COMPLETED",
            }
          )
        );


        setSuccessMessage(
          `"${task.title}" marked complete.`
        );


        await refreshTaskData();

      } catch (
        err
      ) {

        console.error(
          "Unable to complete task:",
          err
        );


        setError(
          err?.message ||
            "Unable to mark task complete."
        );

      } finally {

        setUpdatingTaskId(
          null
        );

      }

    };


  // =========================================================
  // CLEAR FILTERS
  // =========================================================

  const clearFilters =
    () => {

      setSearchTerm(
        ""
      );


      setDebouncedSearch(
        ""
      );


      setStatusFilter(
        "ALL"
      );


      setPriorityFilter(
        "ALL"
      );


      setDueDateFilter(
        "ALL"
      );


      setCurrentPage(
        0
      );

    };


  // =========================================================
  // FILTER HANDLERS
  // =========================================================

  const handleStatusFilterChange =
    (
      event
    ) => {

      setStatusFilter(
        event.target.value
      );


      setCurrentPage(
        0
      );

    };


  const handlePriorityFilterChange =
    (
      event
    ) => {

      setPriorityFilter(
        event.target.value
      );


      setCurrentPage(
        0
      );

    };


  const handleDueDateFilterChange =
    (
      event
    ) => {

      setDueDateFilter(
        event.target.value
      );


      setCurrentPage(
        0
      );

    };


  const handleSortChange =
    (
      event
    ) => {

      setSortOption(
        event.target.value
      );


      setCurrentPage(
        0
      );

    };


  const handlePageSizeChange =
    (
      event
    ) => {

      setPageSize(
        Number(
          event.target.value
        )
      );


      setCurrentPage(
        0
      );

    };


  // =========================================================
  // PAGINATION
  // =========================================================

  const getVisiblePages =
    () => {

      if (
        totalPages <= 1
      ) {

        return [];

      }


      const pages =
        [];


      const start =
        Math.max(
          0,
          currentPage - 2
        );


      const end =
        Math.min(
          totalPages - 1,
          currentPage + 2
        );


      for (
        let page = start;
        page <= end;
        page += 1
      ) {

        pages.push(
          page
        );

      }


      return pages;

    };


  // =========================================================
  // HEALTH VALUES
  // =========================================================

  const totalTasks =
    projectHealth.totalTasks;

  const openTasks =
    projectHealth.openTasks;

  const inProgressTasks =
    projectHealth.inProgressTasks;

  const completedTasks =
    projectHealth.completedTasks;

  const overdueTasks =
    projectHealth.overdueTasks;

  const dueSoonTasks =
    projectHealth.dueSoonTasks;

  const completionPercentage =
    projectHealth.completionPercentage;


  // =========================================================
  // DATE HELPERS
  // =========================================================

  const formatDate =
    (
      dateValue
    ) => {

      if (
        !dateValue
      ) {

        return "No due date";

      }


      return new Date(
        `${dateValue}T00:00:00`
      )
        .toLocaleDateString(
          undefined,
          {
            year:
              "numeric",

            month:
              "short",

            day:
              "numeric",
          }
        );

    };


  const getDueDateState =
    (
      task
    ) => {

      if (
        !task?.dueDate
      ) {

        return "none";

      }


      const today =
        new Date();


      today.setHours(
        0,
        0,
        0,
        0
      );


      const dueDate =
        new Date(
          `${task.dueDate}T00:00:00`
        );


      if (
        dueDate < today &&
        task.status !==
          "COMPLETED"
      ) {

        return "overdue";

      }


      if (
        dueDate.getTime() ===
        today.getTime()
      ) {

        return "today";

      }


      const dueSoonEnd =
        new Date(
          today
        );


      dueSoonEnd.setDate(
        today.getDate() +
          7
      );


      if (
        dueDate > today &&
        dueDate <=
          dueSoonEnd &&
        task.status !==
          "COMPLETED"
      ) {

        return "soon";

      }


      return "normal";

    };


  const getDueDateLabel =
    (
      task
    ) => {

      const state =
        getDueDateState(
          task
        );


      switch (
        state
      ) {

        case "overdue":

          return "Overdue";


        case "today":

          return "Due Today";


        case "soon":

          return "Due Soon";


        default:

          return "";

      }

    };


  // =========================================================
  // RENDER
  // =========================================================

  return (

    <div className="dashboard-page">

      <div className="dashboard-container">

        <header className="dashboard-header">

          <div>

            <p className="dashboard-eyebrow">
              PROJECT MANAGEMENT
            </p>


            <h1>
              Project Dashboard
            </h1>


            <p className="dashboard-subtitle">
              Manage projects, tasks,
              priorities, deadlines,
              and progress from one
              workspace.
            </p>

          </div>


          <button
            type="button"
            className="primary-button"
            onClick={
              () => {

                setEditingProject(
                  null
                );


                setProjectForm({
                  name: "",
                  description: "",
                });


                setShowProjectForm(
                  true
                );

              }
            }
          >
            + Add Project
          </button>

        </header>


        {error && (

          <div className="alert alert-error">
            {error}
          </div>

        )}


        {successMessage && (

          <div className="alert alert-success">
            {successMessage}
          </div>

        )}


        {showProjectForm && (

          <section className="form-panel">

            <div className="section-heading-row">

              <div>

                <span className="section-kicker">
                  PROJECT
                </span>


                <h2>
                  {editingProject
                    ? "Edit Project"
                    : "Create Project"}
                </h2>

              </div>

            </div>


            <form
              className="project-form"
              onSubmit={
                handleProjectSubmit
              }
            >

              <label>
                Project Name

                <input
                  type="text"
                  name="name"
                  value={
                    projectForm.name
                  }
                  onChange={
                    handleProjectInputChange
                  }
                  placeholder="Enter project name"
                  required
                />

              </label>


              <label>
                Description

                <textarea
                  name="description"
                  value={
                    projectForm.description
                  }
                  onChange={
                    handleProjectInputChange
                  }
                  placeholder="Describe the project"
                  rows="4"
                />

              </label>


              <div className="form-actions">

                <button
                  type="submit"
                  className="primary-button"
                >
                  {editingProject
                    ? "Save Changes"
                    : "Create Project"}
                </button>


                <button
                  type="button"
                  className="secondary-button"
                  onClick={
                    resetProjectForm
                  }
                >
                  Cancel
                </button>

              </div>

            </form>

          </section>

        )}


        <section className="stats-grid">

          <article className="stat-card">

            <span className="stat-label">
              Projects
            </span>

            <strong>
              {projects.length}
            </strong>

          </article>


          <article className="stat-card">

            <span className="stat-label">
              Total Tasks
            </span>

            <strong>
              {totalTasks}
            </strong>

          </article>


          <article className="stat-card">

            <span className="stat-label">
              Open
            </span>

            <strong>
              {openTasks}
            </strong>

          </article>


          <article className="stat-card">

            <span className="stat-label">
              In Progress
            </span>

            <strong>
              {inProgressTasks}
            </strong>

          </article>


          <article className="stat-card">

            <span className="stat-label">
              Completed
            </span>

            <strong>
              {completedTasks}
            </strong>

          </article>

        </section>


        <section className="workspace-section">

          <div className="workspace-heading">

            <span className="section-kicker">
              WORKSPACE
            </span>

            <h2>
              Projects & Tasks
            </h2>

          </div>


          <div className="workspace-grid">

            <aside className="projects-panel">

              <div className="panel-header">

                <div>

                  <span className="section-kicker">
                    PROJECTS
                  </span>

                  <h3>
                    Projects
                  </h3>

                </div>


                <span className="count-badge">
                  {projects.length}
                </span>

              </div>


              {loading ? (

                <p className="muted-text">
                  Loading projects...
                </p>

              ) : projects.length ===
                0 ? (

                <div className="empty-state">

                  <h4>
                    No projects yet
                  </h4>


                  <p>
                    Create your first
                    project to get
                    started.
                  </p>

                </div>

              ) : (

                <div className="project-list">

                  {projects.map(
                    (
                      project
                    ) => (

                      <article
                        key={
                          project.id
                        }
                        className={`project-card ${
                          selectedProject?.id ===
                          project.id
                            ? "project-card-selected"
                            : ""
                        }`}
                        onClick={
                          () =>
                            handleSelectProject(
                              project
                            )
                        }
                      >

                        <div className="project-card-top">

                          <div>

                            <h4>
                              {project.name}
                            </h4>


                            <p>
                              {project.description ||
                                "No description provided."}
                            </p>

                          </div>


                          {selectedProject?.id ===
                            project.id && (

                            <span className="selected-badge">
                              Selected
                            </span>

                          )}

                        </div>


                        <div className="project-card-footer">

                          <span className="open-project-hint">
                            Click to view tasks
                          </span>


                          <div className="project-actions">

                            <button
                              type="button"
                              className="text-button"
                              onClick={
                                (
                                  event
                                ) =>
                                  handleEditProject(
                                    project,
                                    event
                                  )
                              }
                            >
                              Edit Project
                            </button>


                            <button
                              type="button"
                              className="text-button danger-text"
                              onClick={
                                (
                                  event
                                ) =>
                                  handleDeleteProject(
                                    project,
                                    event
                                  )
                              }
                            >
                              Delete
                            </button>

                          </div>

                        </div>

                      </article>

                    )
                  )}

                </div>

              )}

            </aside>


            <main className="tasks-panel">

              {!selectedProject ? (

                <div className="select-project-state">

                  <div className="select-project-icon">
                    ✓
                  </div>


                  <h3>
                    Select a Project
                  </h3>


                  <p>
                    Choose a project
                    from the left to
                    view and manage
                    its tasks.
                  </p>

                </div>

              ) : (

                <>

                  <div className="task-panel-header">

                    <div>

                      <span className="section-kicker">
                        ACTIVE PROJECT
                      </span>


                      <h3>
                        {selectedProject.name}
                      </h3>


                      <p>
                        {selectedProject.description ||
                          "No project description."}
                      </p>

                    </div>


                    <button
                      type="button"
                      className="primary-button"
                      onClick={
                        () => {

                          setEditingTask(
                            null
                          );


                          setTaskForm({
                            title: "",
                            description: "",
                            status: "OPEN",
                            priority: "MEDIUM",
                            dueDate: "",
                            assigneeId: "",
                          });


                          setShowTaskForm(
                            true
                          );

                        }
                      }
                    >
                      + Add Task
                    </button>

                  </div>


                  <section className="project-progress-panel">

                    <div className="project-progress-heading">

                      <div>

                        <span className="section-kicker">
                          PROJECT HEALTH
                        </span>


                        <h4>
                          Completion Progress
                        </h4>

                      </div>


                      <strong className="completion-percentage">

                        {healthLoading
                          ? "..."
                          : `${completionPercentage}%`}

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


                    <div className="project-health-grid">

                      <div className="health-metric">

                        <span>
                          Completed
                        </span>

                        <strong>
                          {completedTasks} / {totalTasks}
                        </strong>

                      </div>


                      <div
                        className={`health-metric ${
                          overdueTasks >
                          0
                            ? "health-metric-warning"
                            : ""
                        }`}
                      >

                        <span>
                          Overdue
                        </span>

                        <strong>
                          {overdueTasks}
                        </strong>

                      </div>


                      <div className="health-metric">

                        <span>
                          In Progress
                        </span>

                        <strong>
                          {inProgressTasks}
                        </strong>

                      </div>


                      <div className="health-metric">

                        <span>
                          Open
                        </span>

                        <strong>
                          {openTasks}
                        </strong>

                      </div>

                    </div>

                  </section>


                  <section className="task-summary-strip">

                    <div className="task-summary-item">

                      <span>
                        All Tasks
                      </span>

                      <strong>
                        {totalTasks}
                      </strong>

                    </div>


                    <div className="task-summary-item">

                      <span>
                        Due Soon
                      </span>

                      <strong>
                        {dueSoonTasks}
                      </strong>

                    </div>


                    <div className="task-summary-item task-summary-warning">

                      <span>
                        Overdue
                      </span>

                      <strong>
                        {overdueTasks}
                      </strong>

                    </div>


                    <div className="task-summary-item task-summary-success">

                      <span>
                        Complete
                      </span>

                      <strong>
                        {completionPercentage}%
                      </strong>

                    </div>

                  </section>


                  {showTaskForm && (

                    <section className="task-form-panel">

                      <div className="section-heading-row">

                        <div>

                          <span className="section-kicker">
                            TASK
                          </span>


                          <h3>
                            {editingTask
                              ? "Edit Task"
                              : "Create Task"}
                          </h3>

                        </div>

                      </div>


                      <form
                        className="task-form"
                        onSubmit={
                          handleTaskSubmit
                        }
                      >

                        <label>
                          Title

                          <input
                            type="text"
                            name="title"
                            value={
                              taskForm.title
                            }
                            onChange={
                              handleTaskInputChange
                            }
                            placeholder="Enter task title"
                            required
                          />

                        </label>


                        <label>
                          Description

                          <textarea
                            name="description"
                            value={
                              taskForm.description
                            }
                            onChange={
                              handleTaskInputChange
                            }
                            placeholder="Describe the task"
                            rows="4"
                          />

                        </label>


                        <div className="task-form-grid">

                          <label>
                            Status

                            <select
                              name="status"
                              value={
                                taskForm.status
                              }
                              onChange={
                                handleTaskInputChange
                              }
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

                          </label>


                          <label>
                            Priority

                            <select
                              name="priority"
                              value={
                                taskForm.priority
                              }
                              onChange={
                                handleTaskInputChange
                              }
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

                          </label>


                          <label>
                            Due Date

                            <input
                              type="date"
                              name="dueDate"
                              value={
                                taskForm.dueDate
                              }
                              onChange={
                                handleTaskInputChange
                              }
                            />

                          </label>


                          <label>
                            Assignee

                            <select
                              name="assigneeId"
                              value={
                                taskForm.assigneeId
                              }
                              onChange={
                                handleTaskInputChange
                              }
                              disabled={
                                assigneesLoading
                              }
                            >

                              <option value="">
                                Unassigned
                              </option>


                              {assignees.map(
                                (
                                  user
                                ) => (

                                  <option
                                    key={
                                      user.id
                                    }
                                    value={
                                      user.id
                                    }
                                  >
                                    {user.name ||
                                      user.email}
                                  </option>

                                )
                              )}

                            </select>


                            {assigneesLoading && (

                              <span className="muted-text">
                                Loading users...
                              </span>

                            )}

                          </label>

                        </div>


                        <div className="form-actions">

                          <button
                            type="submit"
                            className="primary-button"
                          >
                            {editingTask
                              ? "Save Changes"
                              : "Create Task"}
                          </button>


                          <button
                            type="button"
                            className="secondary-button"
                            onClick={
                              resetTaskForm
                            }
                          >
                            Cancel
                          </button>

                        </div>

                      </form>

                    </section>

                  )}


                  <section className="task-filter-panel">

                    <div className="filter-heading">

                      <div>

                        <span className="section-kicker">
                          SERVER FILTERS
                        </span>


                        <h4>
                          Find and organize
                          project tasks
                        </h4>

                      </div>


                      <span className="filter-result-count">

                        {totalElements}{" "}
                        result
                        {totalElements ===
                        1
                          ? ""
                          : "s"}

                      </span>

                    </div>


                    <div className="task-filters">

                      <div className="filter-group filter-search-group">

                        <label htmlFor="taskSearch">
                          Search
                        </label>


                        <input
                          id="taskSearch"
                          type="search"
                          value={
                            searchTerm
                          }
                          onChange={
                            (
                              event
                            ) =>
                              setSearchTerm(
                                event.target.value
                              )
                          }
                          placeholder="Search title or description"
                        />

                      </div>


                      <div className="filter-group">

                        <label htmlFor="statusFilter">
                          Status
                        </label>


                        <select
                          id="statusFilter"
                          value={
                            statusFilter
                          }
                          onChange={
                            handleStatusFilterChange
                          }
                        >

                          <option value="ALL">
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


                      <div className="filter-group">

                        <label htmlFor="priorityFilter">
                          Priority
                        </label>


                        <select
                          id="priorityFilter"
                          value={
                            priorityFilter
                          }
                          onChange={
                            handlePriorityFilterChange
                          }
                        >

                          <option value="ALL">
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


                      <div className="filter-group">

                        <label htmlFor="dueDateFilter">
                          Due Date
                        </label>


                        <select
                          id="dueDateFilter"
                          value={
                            dueDateFilter
                          }
                          onChange={
                            handleDueDateFilterChange
                          }
                        >

                          <option value="ALL">
                            All Dates
                          </option>

                          <option value="OVERDUE">
                            Overdue
                          </option>

                          <option value="DUE_TODAY">
                            Due Today
                          </option>

                          <option value="DUE_SOON">
                            Due Soon
                          </option>

                          <option value="NO_DUE_DATE">
                            No Due Date
                          </option>

                        </select>

                      </div>


                      <div className="filter-group">

                        <label htmlFor="sortOption">
                          Sort By
                        </label>


                        <select
                          id="sortOption"
                          value={
                            sortOption
                          }
                          onChange={
                            handleSortChange
                          }
                        >

                          <option value="DUE_DATE_ASC">
                            Due Date - Earliest
                          </option>

                          <option value="DUE_DATE_DESC">
                            Due Date - Latest
                          </option>

                          <option value="PRIORITY">
                            Priority
                          </option>

                          <option value="STATUS">
                            Status
                          </option>

                          <option value="TITLE">
                            Title - A to Z
                          </option>

                          <option value="UPDATED_DESC">
                            Recently Updated
                          </option>

                        </select>

                      </div>


                      <button
                        type="button"
                        className="clear-filters-button"
                        onClick={
                          clearFilters
                        }
                        disabled={
                          searchTerm ===
                            "" &&
                          statusFilter ===
                            "ALL" &&
                          priorityFilter ===
                            "ALL" &&
                          dueDateFilter ===
                            "ALL"
                        }
                      >
                        Clear Filters
                      </button>

                    </div>

                  </section>


                  <div className="task-list-header">

                    <div>

                      <span className="section-kicker">
                        TASKS
                      </span>


                      <h4>
                        Project Tasks
                      </h4>

                    </div>


                    <div className="task-list-header-actions">

                      <span className="page-result-summary">

                        {totalElements ===
                        0
                          ? "0 results"

                          : `Showing ${
                              currentPage *
                                pageSize +
                              1
                            }–${
                              currentPage *
                                pageSize +
                              numberOfElements
                            } of ${totalElements}`}

                      </span>


                      <label className="page-size-control">

                        <span>
                          Per page
                        </span>


                        <select
                          value={
                            pageSize
                          }
                          onChange={
                            handlePageSizeChange
                          }
                        >

                          <option value="5">
                            5
                          </option>

                          <option value="10">
                            10
                          </option>

                          <option value="20">
                            20
                          </option>

                          <option value="50">
                            50
                          </option>

                        </select>

                      </label>

                    </div>

                  </div>


                  {tasksLoading ? (

                    <div className="task-loading-state">
                      Loading tasks...
                    </div>

                  ) : totalElements ===
                    0 ? (

                    <div className="empty-state large-empty-state">

                      <h4>
                        No matching tasks
                      </h4>


                      <p>
                        No tasks match your
                        current server-side
                        filters.
                      </p>


                      <button
                        type="button"
                        className="secondary-button"
                        onClick={
                          clearFilters
                        }
                      >
                        Clear Filters
                      </button>

                    </div>

                  ) : (

                    <div className="task-list">

                      {tasks.map(
                        (
                          task
                        ) => {

                          const isCompleted =
                            task.status ===
                            "COMPLETED";


                          const isUpdating =
                            updatingTaskId ===
                            task.id;


                          return (

                            <article
                              key={
                                task.id
                              }
                              className={`task-card task-card-${getDueDateState(
                                task
                              )} ${
                                isCompleted
                                  ? "task-card-completed"
                                  : ""
                              }`}
                            >

                              <div className="task-card-header">

                                <div className="task-title-block">

                                  <h4>
                                    {task.title}
                                  </h4>


                                  <p>
                                    {task.description ||
                                      "No description provided."}
                                  </p>

                                </div>


                                <div className="task-badges">

                                  <span
                                    className={`status-badge status-${task.status?.toLowerCase()}`}
                                  >
                                    {formatStatus(
                                      task.status
                                    )}
                                  </span>


                                  <span
                                    className={`priority-badge priority-${task.priority?.toLowerCase()}`}
                                  >
                                    {formatPriority(
                                      task.priority
                                    )}
                                  </span>

                                </div>

                              </div>


                              <div className="task-quick-actions">

                                <div className="quick-status-control">

                                  <label
                                    htmlFor={`status-${task.id}`}
                                  >
                                    Quick Status
                                  </label>


                                  <select
                                    id={`status-${task.id}`}
                                    value={
                                      task.status
                                    }
                                    disabled={
                                      isUpdating
                                    }
                                    onChange={
                                      (
                                        event
                                      ) =>
                                        handleQuickStatusChange(
                                          task,
                                          event.target.value
                                        )
                                    }
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

                                </div>


                                <button
                                  type="button"
                                  className={
                                    isCompleted
                                      ? "completed-button"
                                      : "complete-task-button"
                                  }
                                  disabled={
                                    isCompleted ||
                                    isUpdating
                                  }
                                  onClick={
                                    () =>
                                      handleMarkTaskComplete(
                                        task
                                      )
                                  }
                                >

                                  {isUpdating
                                    ? "Updating..."

                                    : isCompleted
                                      ? "✓ Completed"

                                      : "✓ Complete Task"}

                                </button>

                              </div>


                              <div className="task-card-footer">

                                <div
                                  className={`due-date due-date-${getDueDateState(
                                    task
                                  )}`}
                                >

                                  <span>
                                    Due
                                  </span>


                                  <strong>
                                    {formatDate(
                                      task.dueDate
                                    )}
                                  </strong>


                                  {getDueDateLabel(
                                    task
                                  ) && (

                                    <em className="due-date-indicator">
                                      {getDueDateLabel(
                                        task
                                      )}
                                    </em>

                                  )}

                                </div>


                                <div className="task-assignee">

                                  <span>
                                    Assignee
                                  </span>


                                  <strong>
                                    {task.assigneeName ||
                                      "Unassigned"}
                                  </strong>


                                  {task.assigneeEmail && (

                                    <small>
                                      {task.assigneeEmail}
                                    </small>

                                  )}

                                </div>


                                <div className="task-actions">

                                  <button
                                    type="button"
                                    className="secondary-button small-button"
                                    onClick={
                                      () =>
                                        handleEditTask(
                                          task
                                        )
                                    }
                                  >
                                    Edit Task
                                  </button>


                                  <button
                                    type="button"
                                    className="danger-button small-button"
                                    onClick={
                                      () =>
                                        handleDeleteTask(
                                          task.id
                                        )
                                    }
                                  >
                                    Delete
                                  </button>

                                </div>

                              </div>

                            </article>

                          );

                        }
                      )}

                    </div>

                  )}


                  {totalPages >
                    1 && (

                    <nav
                      className="pagination-panel"
                      aria-label="Task pagination"
                    >

                      <button
                        type="button"
                        className="pagination-button"
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
                        ← Previous
                      </button>


                      <div className="pagination-pages">

                        {getVisiblePages().map(
                          (
                            page
                          ) => (

                            <button
                              key={
                                page
                              }
                              type="button"
                              className={`pagination-number ${
                                page ===
                                currentPage
                                  ? "pagination-number-active"
                                  : ""
                              }`}
                              onClick={
                                () =>
                                  setCurrentPage(
                                    page
                                  )
                              }
                              disabled={
                                tasksLoading
                              }
                              aria-current={
                                page ===
                                currentPage
                                  ? "page"
                                  : undefined
                              }
                            >
                              {page + 1}
                            </button>

                          )
                        )}

                      </div>


                      <span className="pagination-label">

                        Page{" "}
                        {currentPage + 1}{" "}
                        of{" "}
                        {totalPages}

                      </span>


                      <button
                        type="button"
                        className="pagination-button"
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
                                Math.min(
                                  totalPages -
                                    1,
                                  page + 1
                                )
                            )
                        }
                      >
                        Next →
                      </button>

                    </nav>

                  )}

                </>

              )}

            </main>

          </div>

        </section>

      </div>

    </div>

  );

}


export default Dashboard;