import {
  beforeEach,
  describe,
  expect,
  it,
  vi,
} from "vitest";

import {
  fireEvent,
  render,
  screen,
  waitFor,
  within,
} from "@testing-library/react";

import userEvent
  from "@testing-library/user-event";

import Dashboard
  from "../pages/Dashboard";

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


/*
 * =============================================================
 * MOCK API CLIENT
 * =============================================================
 */

vi.mock(
  "../api/apiClient",
  () => ({
    getProjects: vi.fn(),
    createProject: vi.fn(),
    updateProject: vi.fn(),
    deleteProject: vi.fn(),

    getProjectHealth: vi.fn(),
    getProjectTasksPage: vi.fn(),

    createTask: vi.fn(),
    updateTask: vi.fn(),
    deleteTask: vi.fn(),
  })
);

vi.mock(
  "../api/authApi",
  () => ({
    getAssignees: vi.fn(),
  })
);


/*
 * =============================================================
 * TEST DATA
 * =============================================================
 */

const mockProjects = [
  {
    id: 1,
    name: "Website Redesign",
    description:
      "Redesign the company website.",
  },
  {
    id: 2,
    name: "Mobile Application",
    description:
      "Build the mobile application.",
  },
];


const mockAssignees = [
  {
    id: 201,
    name: "Alex Developer",
    email: "alex@example.com",
    role: "USER",
  },
  {
    id: 202,
    name: "Jordan Tester",
    email: "jordan@example.com",
    role: "USER",
  },
];


const mockHealth = {
  totalTasks: 4,
  openTasks: 1,
  inProgressTasks: 2,
  completedTasks: 1,
  overdueTasks: 1,
  dueSoonTasks: 1,
  completionPercentage: 25,
};


const mockTasks = [
  {
    id: 101,
    title: "Create homepage",
    description:
      "Build the new homepage.",
    status: "OPEN",
    priority: "HIGH",
    dueDate: "2026-09-10",
  },
  {
    id: 102,
    title: "Create navigation",
    description:
      "Build the navigation menu.",
    status: "IN_PROGRESS",
    priority: "MEDIUM",
    dueDate: "2026-09-15",
  },
];


const mockTaskPage = {
  content: mockTasks,

  totalElements: 2,
  totalPages: 1,
  numberOfElements: 2,

  number: 0,

  first: true,
  last: true,
};


/*
 * =============================================================
 * DASHBOARD TESTS
 * =============================================================
 */

describe(
  "Dashboard",
  () => {

    beforeEach(
      () => {

        vi.clearAllMocks();


        getProjects.mockResolvedValue(
          mockProjects
        );


        getAssignees.mockResolvedValue(
          mockAssignees
        );


        getProjectHealth.mockResolvedValue(
          mockHealth
        );


        getProjectTasksPage.mockResolvedValue(
          mockTaskPage
        );


        createProject.mockResolvedValue({
          id: 3,
          name: "New Project",
          description:
            "New project description.",
        });


        updateProject.mockResolvedValue({
          id: 1,
          name: "Updated Website",
          description:
            "Updated description.",
        });


        deleteProject.mockResolvedValue(
          undefined
        );


        createTask.mockResolvedValue({
          id: 103,
          title: "New Dashboard Task",
        });


        updateTask.mockResolvedValue({
          id: 101,
          title: "Updated Dashboard Task",
        });


        deleteTask.mockResolvedValue(
          undefined
        );


        window.scrollTo =
          vi.fn();


        window.confirm =
          vi.fn(
            () => true
          );

      }
    );


    /*
     * =========================================================
     * TEST 1
     *
     * DASHBOARD RENDERS
     * =========================================================
     */

    it(
      "renders the dashboard",
      async () => {

        render(
          <Dashboard />
        );


        expect(
          screen.getByRole(
            "heading",
            {
              name:
                "Project Dashboard",
            }
          )
        )
          .toBeInTheDocument();


        expect(
          screen.getByRole(
            "button",
            {
              name:
                "+ Add Project",
            }
          )
        )
          .toBeInTheDocument();


        await waitFor(
          () => {

            expect(
              getProjects
            )
              .toHaveBeenCalledTimes(
                1
              );

          }
        );

      }
    );


    /*
     * =========================================================
     * TEST 2
     *
     * PROJECTS LOAD
     * =========================================================
     */

    it(
      "loads and displays projects",
      async () => {

        render(
          <Dashboard />
        );


        expect(
          await screen.findByText(
            "Website Redesign"
          )
        )
          .toBeInTheDocument();


        expect(
          screen.getByText(
            "Mobile Application"
          )
        )
          .toBeInTheDocument();


        expect(
          screen.getByText(
            "Redesign the company website."
          )
        )
          .toBeInTheDocument();


        expect(
          screen.getByText(
            "Build the mobile application."
          )
        )
          .toBeInTheDocument();


        expect(
          getProjects
        )
          .toHaveBeenCalledTimes(
            1
          );

      }
    );


    /*
     * =========================================================
     * TEST 3
     *
     * EMPTY PROJECT STATE
     * =========================================================
     */

    it(
      "shows the empty project state when no projects exist",
      async () => {

        getProjects.mockResolvedValue(
          []
        );


        render(
          <Dashboard />
        );


        expect(
          await screen.findByText(
            "No projects yet"
          )
        )
          .toBeInTheDocument();


        expect(
          screen.getByText(
            /Create your first project/i
          )
        )
          .toBeInTheDocument();

      }
    );


    /*
     * =========================================================
     * TEST 4
     *
     * INITIAL TASK PANEL
     * =========================================================
     */

    it(
      "asks the user to select a project initially",
      async () => {

        render(
          <Dashboard />
        );


        await screen.findByText(
          "Website Redesign"
        );


        expect(
          screen.getByRole(
            "heading",
            {
              name:
                "Select a Project",
            }
          )
        )
          .toBeInTheDocument();


        expect(
          screen.getByText(
            /Choose a project from the left/i
          )
        )
          .toBeInTheDocument();

      }
    );


    /*
     * =========================================================
     * TEST 5
     *
     * SELECT PROJECT
     * =========================================================
     */

    it(
      "selects a project when its card is clicked",
      async () => {

        render(
          <Dashboard />
        );


        fireEvent.click(
          await screen.findByText(
            "Website Redesign"
          )
        );


        expect(
          await screen.findByText(
            "Selected"
          )
        )
          .toBeInTheDocument();


        expect(
          screen.getByRole(
            "button",
            {
              name:
                "+ Add Task",
            }
          )
        )
          .toBeInTheDocument();

      }
    );


    /*
     * =========================================================
     * TEST 6
     *
     * PROJECT HEALTH
     * =========================================================
     */

    it(
      "loads project health after selecting a project",
      async () => {

        render(
          <Dashboard />
        );


        fireEvent.click(
          await screen.findByText(
            "Website Redesign"
          )
        );


        await waitFor(
          () => {

            expect(
              getProjectHealth
            )
              .toHaveBeenCalledWith(
                1
              );

          }
        );


        const completionPercentages =
          await screen.findAllByText(
            "25%"
          );


        expect(
          completionPercentages.length
        )
          .toBeGreaterThanOrEqual(
            1
          );


        expect(
          screen.getByRole(
            "progressbar"
          )
        )
          .toHaveAttribute(
            "aria-valuenow",
            "25"
          );

      }
    );


    /*
     * =========================================================
     * TEST 7
     *
     * TASK PAGE LOADS
     * =========================================================
     */

    it(
      "loads tasks after selecting a project",
      async () => {

        render(
          <Dashboard />
        );


        fireEvent.click(
          await screen.findByText(
            "Website Redesign"
          )
        );


        await waitFor(
          () => {

            expect(
              getProjectTasksPage
            )
              .toHaveBeenCalled();

          }
        );


        expect(
          await screen.findByText(
            "Create homepage"
          )
        )
          .toBeInTheDocument();


        expect(
          screen.getByText(
            "Create navigation"
          )
        )
          .toBeInTheDocument();


        expect(
          screen.getByText(
            "Build the new homepage."
          )
        )
          .toBeInTheDocument();

      }
    );


    /*
     * =========================================================
     * TEST 8
     *
     * DEFAULT SERVER PARAMETERS
     * =========================================================
     */

    it(
      "requests the first task page using the default filters",
      async () => {

        render(
          <Dashboard />
        );


        fireEvent.click(
          await screen.findByText(
            "Website Redesign"
          )
        );


        await waitFor(
          () => {

            expect(
              getProjectTasksPage
            )
              .toHaveBeenCalledWith(
                1,
                {
                  page: 0,
                  size: 10,

                  status:
                    "ALL",

                  priority:
                    "ALL",

                  search:
                    "",

                  dueDateFilter:
                    "ALL",

                  sortBy:
                    "dueDate",

                  sortDirection:
                    "asc",
                }
              );

          }
        );

      }
    );


    /*
     * =========================================================
     * TEST 9
     *
     * HEALTH STATISTICS
     * =========================================================
     */

    it(
      "displays backend project health statistics",
      async () => {

        render(
          <Dashboard />
        );


        fireEvent.click(
          await screen.findByText(
            "Website Redesign"
          )
        );


        await waitFor(
          () => {

            expect(
              getProjectHealth
            )
              .toHaveBeenCalledWith(
                1
              );

          }
        );


        expect(
          await screen.findByText(
            "1 / 4"
          )
        )
          .toBeInTheDocument();


        expect(
          screen.getByText(
            "2 results"
          )
        )
          .toBeInTheDocument();


        expect(
          screen.getByText(
            "Showing 1–2 of 2"
          )
        )
          .toBeInTheDocument();

      }
    );


    /*
     * =========================================================
     * TEST 10
     *
     * PROJECT LOAD ERROR
     * =========================================================
     */

    it(
      "displays an error when projects cannot be loaded",
      async () => {

        getProjects.mockRejectedValue(
          new Error(
            "Unable to reach server."
          )
        );


        render(
          <Dashboard />
        );


        expect(
          await screen.findByText(
            "Unable to reach server."
          )
        )
          .toBeInTheDocument();

      }
    );


    /*
     * =========================================================
     * TEST 11
     *
     * EMPTY TASK PAGE
     * =========================================================
     */

    it(
      "shows the empty task state when the selected project has no tasks",
      async () => {

        getProjectHealth.mockResolvedValue({
          totalTasks: 0,
          openTasks: 0,
          inProgressTasks: 0,
          completedTasks: 0,
          overdueTasks: 0,
          dueSoonTasks: 0,
          completionPercentage: 0,
        });


        getProjectTasksPage.mockResolvedValue({
          content: [],
          totalElements: 0,
          totalPages: 0,
          numberOfElements: 0,
          number: 0,
          first: true,
          last: true,
        });


        render(
          <Dashboard />
        );


        fireEvent.click(
          await screen.findByText(
            "Website Redesign"
          )
        );


        expect(
          await screen.findByText(
            "No matching tasks"
          )
        )
          .toBeInTheDocument();

      }
    );


    /*
     * =========================================================
     * TEST 12
     *
     * OPEN CREATE PROJECT FORM
     * =========================================================
     */

    it(
      "opens the create project form",
      async () => {

        const user =
          userEvent.setup();


        render(
          <Dashboard />
        );


        await screen.findByText(
          "Website Redesign"
        );


        await user.click(
          screen.getByRole(
            "button",
            {
              name:
                "+ Add Project",
            }
          )
        );


        expect(
          screen.getByRole(
            "heading",
            {
              name:
                "Create Project",
            }
          )
        )
          .toBeInTheDocument();


        expect(
          screen.getByPlaceholderText(
            "Enter project name"
          )
        )
          .toBeInTheDocument();


        expect(
          screen.getByPlaceholderText(
            "Describe the project"
          )
        )
          .toBeInTheDocument();

      }
    );


    /*
     * =========================================================
     * TEST 13
     *
     * CREATE PROJECT
     * =========================================================
     */

    it(
      "creates a project and refreshes the project list",
      async () => {

        const user =
          userEvent.setup();


        render(
          <Dashboard />
        );


        await screen.findByText(
          "Website Redesign"
        );


        await user.click(
          screen.getByRole(
            "button",
            {
              name:
                "+ Add Project",
            }
          )
        );


        await user.type(
          screen.getByPlaceholderText(
            "Enter project name"
          ),
          "New Project"
        );


        await user.type(
          screen.getByPlaceholderText(
            "Describe the project"
          ),
          "New project description."
        );


        await user.click(
          screen.getByRole(
            "button",
            {
              name:
                "Create Project",
            }
          )
        );


        await waitFor(
          () => {

            expect(
              createProject
            )
              .toHaveBeenCalledWith({
                name:
                  "New Project",

                description:
                  "New project description.",
              });

          }
        );


        expect(
          await screen.findByText(
            "Project created successfully."
          )
        )
          .toBeInTheDocument();


        expect(
          getProjects.mock.calls.length
        )
          .toBeGreaterThanOrEqual(
            2
          );

      }
    );


    /*
     * =========================================================
     * TEST 14
     *
     * CANCEL CREATE PROJECT
     * =========================================================
     */

    it(
      "closes the project form when cancel is clicked",
      async () => {

        const user =
          userEvent.setup();


        render(
          <Dashboard />
        );


        await screen.findByText(
          "Website Redesign"
        );


        await user.click(
          screen.getByRole(
            "button",
            {
              name:
                "+ Add Project",
            }
          )
        );


        expect(
          screen.getByRole(
            "heading",
            {
              name:
                "Create Project",
            }
          )
        )
          .toBeInTheDocument();


        await user.click(
          screen.getByRole(
            "button",
            {
              name:
                "Cancel",
            }
          )
        );


        expect(
          screen.queryByRole(
            "heading",
            {
              name:
                "Create Project",
            }
          )
        )
          .not
          .toBeInTheDocument();

      }
    );


    /*
     * =========================================================
     * TEST 15
     *
     * EDIT PROJECT FORM
     * =========================================================
     */

    it(
      "opens the edit project form with existing values",
      async () => {

        const user =
          userEvent.setup();


        render(
          <Dashboard />
        );


        await screen.findByText(
          "Website Redesign"
        );


        const editButtons =
          screen.getAllByRole(
            "button",
            {
              name:
                "Edit Project",
            }
          );


        await user.click(
          editButtons[0]
        );


        expect(
          screen.getByRole(
            "heading",
            {
              name:
                "Edit Project",
            }
          )
        )
          .toBeInTheDocument();


        expect(
          screen.getByPlaceholderText(
            "Enter project name"
          )
        )
          .toHaveValue(
            "Website Redesign"
          );


        expect(
          screen.getByPlaceholderText(
            "Describe the project"
          )
        )
          .toHaveValue(
            "Redesign the company website."
          );


        expect(
          window.scrollTo
        )
          .toHaveBeenCalled();

      }
    );


    /*
     * =========================================================
     * TEST 16
     *
     * UPDATE PROJECT
     * =========================================================
     */

    it(
      "updates an existing project",
      async () => {

        const user =
          userEvent.setup();


        render(
          <Dashboard />
        );


        await screen.findByText(
          "Website Redesign"
        );


        const editButtons =
          screen.getAllByRole(
            "button",
            {
              name:
                "Edit Project",
            }
          );


        await user.click(
          editButtons[0]
        );


        const nameInput =
          screen.getByPlaceholderText(
            "Enter project name"
          );


        const descriptionInput =
          screen.getByPlaceholderText(
            "Describe the project"
          );


        await user.clear(
          nameInput
        );


        await user.type(
          nameInput,
          "Updated Website"
        );


        await user.clear(
          descriptionInput
        );


        await user.type(
          descriptionInput,
          "Updated description."
        );


        await user.click(
          screen.getByRole(
            "button",
            {
              name:
                "Save Changes",
            }
          )
        );


        await waitFor(
          () => {

            expect(
              updateProject
            )
              .toHaveBeenCalledWith(
                1,
                {
                  name:
                    "Updated Website",

                  description:
                    "Updated description.",
                }
              );

          }
        );


        expect(
          await screen.findByText(
            "Project updated successfully."
          )
        )
          .toBeInTheDocument();


        expect(
          getProjects.mock.calls.length
        )
          .toBeGreaterThanOrEqual(
            2
          );

      }
    );


    /*
     * =========================================================
     * TEST 17
     *
     * DELETE PROJECT
     * =========================================================
     */

    it(
      "deletes a project after confirmation",
      async () => {

        const user =
          userEvent.setup();


        render(
          <Dashboard />
        );


        await screen.findByText(
          "Website Redesign"
        );


        const deleteButtons =
          screen.getAllByRole(
            "button",
            {
              name:
                "Delete",
            }
          );


        await user.click(
          deleteButtons[0]
        );


        expect(
          window.confirm
        )
          .toHaveBeenCalledWith(
            'Delete "Website Redesign" and its tasks?'
          );


        await waitFor(
          () => {

            expect(
              deleteProject
            )
              .toHaveBeenCalledWith(
                1
              );

          }
        );


        expect(
          await screen.findByText(
            "Project deleted successfully."
          )
        )
          .toBeInTheDocument();


        expect(
          getProjects.mock.calls.length
        )
          .toBeGreaterThanOrEqual(
            2
          );

      }
    );


    /*
     * =========================================================
     * TEST 18
     *
     * CANCEL DELETE PROJECT
     * =========================================================
     */

    it(
      "does not delete a project when confirmation is cancelled",
      async () => {

        const user =
          userEvent.setup();


        window.confirm =
          vi.fn(
            () => false
          );


        render(
          <Dashboard />
        );


        await screen.findByText(
          "Website Redesign"
        );


        const deleteButtons =
          screen.getAllByRole(
            "button",
            {
              name:
                "Delete",
            }
          );


        await user.click(
          deleteButtons[0]
        );


        expect(
          window.confirm
        )
          .toHaveBeenCalled();


        expect(
          deleteProject
        )
          .not
          .toHaveBeenCalled();

      }
    );


    /*
     * =========================================================
     * TEST 19
     *
     * PROJECT SAVE ERROR
     * =========================================================
     */

    it(
      "displays an error when project creation fails",
      async () => {

        const user =
          userEvent.setup();


        createProject.mockRejectedValue(
          new Error(
            "Unable to save project."
          )
        );


        render(
          <Dashboard />
        );


        await screen.findByText(
          "Website Redesign"
        );


        await user.click(
          screen.getByRole(
            "button",
            {
              name:
                "+ Add Project",
            }
          )
        );


        await user.type(
          screen.getByPlaceholderText(
            "Enter project name"
          ),
          "Broken Project"
        );


        await user.click(
          screen.getByRole(
            "button",
            {
              name:
                "Create Project",
            }
          )
        );


        expect(
          await screen.findByText(
            "Unable to save project."
          )
        )
          .toBeInTheDocument();

      }
    );


    /*
     * =========================================================
     * TEST 20 - OPEN CREATE TASK FORM
     * =========================================================
     */

    it(
      "opens the create task form for the selected project",
      async () => {

        const user =
          userEvent.setup();

        render(
          <Dashboard />
        );

        await user.click(
          await screen.findByText(
            "Website Redesign"
          )
        );

        await user.click(
          screen.getByRole(
            "button",
            {
              name:
                "+ Add Task",
            }
          )
        );

        expect(
          screen.getByRole(
            "heading",
            {
              name:
                "Create Task",
            }
          )
        )
          .toBeInTheDocument();

        expect(
          screen.getByPlaceholderText(
            "Enter task title"
          )
        )
          .toBeInTheDocument();

      }
    );


    /*
     * =========================================================
     * TEST 21 - CREATE TASK
     * =========================================================
     */

    it(
      "creates a task for the selected project and refreshes task data",
      async () => {

        const user =
          userEvent.setup();

        render(
          <Dashboard />
        );

        await user.click(
          await screen.findByText(
            "Website Redesign"
          )
        );

        await screen.findByText(
          "Create homepage"
        );

        await user.click(
          screen.getByRole(
            "button",
            {
              name:
                "+ Add Task",
            }
          )
        );

        await user.type(
          screen.getByPlaceholderText(
            "Enter task title"
          ),
          "New Dashboard Task"
        );

        await user.type(
          screen.getByPlaceholderText(
            "Describe the task"
          ),
          "Created by the Dashboard test."
        );

        const taskForm =
          screen.getByRole(
            "heading",
            {
              name:
                "Create Task",
            }
          )
            .closest(
              ".task-form-panel"
            );

        const form =
          within(
            taskForm
          );

        await user.selectOptions(
          form.getByLabelText(
            "Status"
          ),
          "IN_PROGRESS"
        );

        await user.selectOptions(
          form.getByLabelText(
            "Priority"
          ),
          "HIGH"
        );

        fireEvent.change(
          form.getByLabelText(
            "Due Date"
          ),
          {
            target: {
              value:
                "2026-10-01",
            },
          }
        );

        await user.click(
          form.getByRole(
            "button",
            {
              name:
                "Create Task",
            }
          )
        );

        await waitFor(
          () => {

            expect(
              createTask
            )
              .toHaveBeenCalledWith(
                1,
                {
                  title:
                    "New Dashboard Task",
                  description:
                    "Created by the Dashboard test.",
                  status:
                    "IN_PROGRESS",
                  priority:
                    "HIGH",
                  dueDate:
                    "2026-10-01",
                  assigneeId:
                    null,
                }
              );

          }
        );

        expect(
          await screen.findByText(
            "Task created successfully."
          )
        )
          .toBeInTheDocument();

      }
    );


    /*
     * =========================================================
     * TEST 22 - EDIT TASK FORM
     * =========================================================
     */

    it(
      "opens the edit task form with the existing task values",
      async () => {

        const user =
          userEvent.setup();

        render(
          <Dashboard />
        );

        await user.click(
          await screen.findByText(
            "Website Redesign"
          )
        );

        const taskTitle =
          await screen.findByText(
            "Create homepage"
          );

        const taskCard =
          taskTitle.closest(
            "article"
          );

        await user.click(
          within(
            taskCard
          )
            .getByRole(
              "button",
              {
                name:
                  "Edit Task",
              }
            )
        );

        expect(
          screen.getByRole(
            "heading",
            {
              name:
                "Edit Task",
            }
          )
        )
          .toBeInTheDocument();

        expect(
          screen.getByPlaceholderText(
            "Enter task title"
          )
        )
          .toHaveValue(
            "Create homepage"
          );

        expect(
          screen.getByPlaceholderText(
            "Describe the task"
          )
        )
          .toHaveValue(
            "Build the new homepage."
          );

      }
    );


    /*
     * =========================================================
     * TEST 23 - UPDATE TASK
     * =========================================================
     */

    it(
      "updates an existing task",
      async () => {

        const user =
          userEvent.setup();

        render(
          <Dashboard />
        );

        await user.click(
          await screen.findByText(
            "Website Redesign"
          )
        );

        const taskTitle =
          await screen.findByText(
            "Create homepage"
          );

        const taskCard =
          taskTitle.closest(
            "article"
          );

        await user.click(
          within(
            taskCard
          )
            .getByRole(
              "button",
              {
                name:
                  "Edit Task",
              }
            )
        );

        const titleInput =
          screen.getByPlaceholderText(
            "Enter task title"
          );

        const descriptionInput =
          screen.getByPlaceholderText(
            "Describe the task"
          );

        await user.clear(
          titleInput
        );

        await user.type(
          titleInput,
          "Updated Dashboard Task"
        );

        await user.clear(
          descriptionInput
        );

        await user.type(
          descriptionInput,
          "Updated task description."
        );

        const taskForm =
          screen.getByRole(
            "heading",
            {
              name:
                "Edit Task",
            }
          )
            .closest(
              ".task-form-panel"
            );

        const form =
          within(
            taskForm
          );

        await user.selectOptions(
          form.getByLabelText(
            "Status"
          ),
          "COMPLETED"
        );

        await user.selectOptions(
          form.getByLabelText(
            "Priority"
          ),
          "LOW"
        );

        fireEvent.change(
          form.getByLabelText(
            "Due Date"
          ),
          {
            target: {
              value:
                "2026-10-15",
            },
          }
        );

        await user.click(
          form.getByRole(
            "button",
            {
              name:
                "Save Changes",
            }
          )
        );

        await waitFor(
          () => {

            expect(
              updateTask
            )
              .toHaveBeenCalledWith(
                1,
                101,
                {
                  title:
                    "Updated Dashboard Task",
                  description:
                    "Updated task description.",
                  status:
                    "COMPLETED",
                  priority:
                    "LOW",
                  dueDate:
                    "2026-10-15",
                  assigneeId:
                    null,
                }
              );

          }
        );

        expect(
          await screen.findByText(
            "Task updated successfully."
          )
        )
          .toBeInTheDocument();

      }
    );


    /*
     * =========================================================
     * TEST 24 - DELETE TASK
     * =========================================================
     */

    it(
      "deletes a task after confirmation",
      async () => {

        const user =
          userEvent.setup();

        render(
          <Dashboard />
        );

        await user.click(
          await screen.findByText(
            "Website Redesign"
          )
        );

        const taskTitle =
          await screen.findByText(
            "Create homepage"
          );

        const taskCard =
          taskTitle.closest(
            "article"
          );

        await user.click(
          within(
            taskCard
          )
            .getByRole(
              "button",
              {
                name:
                  "Delete",
              }
            )
        );

        expect(
          window.confirm
        )
          .toHaveBeenCalledWith(
            "Are you sure you want to delete this task?"
          );

        await waitFor(
          () => {

            expect(
              deleteTask
            )
              .toHaveBeenCalledWith(
                1,
                101
              );

          }
        );

        expect(
          await screen.findByText(
            "Task deleted successfully."
          )
        )
          .toBeInTheDocument();

      }
    );


    /*
     * =========================================================
     * TEST 25 - CANCEL TASK DELETE
     * =========================================================
     */

    it(
      "does not delete a task when confirmation is cancelled",
      async () => {

        const user =
          userEvent.setup();

        window.confirm =
          vi.fn(
            () => false
          );

        render(
          <Dashboard />
        );

        await user.click(
          await screen.findByText(
            "Website Redesign"
          )
        );

        const taskTitle =
          await screen.findByText(
            "Create homepage"
          );

        const taskCard =
          taskTitle.closest(
            "article"
          );

        await user.click(
          within(
            taskCard
          )
            .getByRole(
              "button",
              {
                name:
                  "Delete",
              }
            )
        );

        expect(
          deleteTask
        )
          .not
          .toHaveBeenCalled();

      }
    );


    /*
     * =========================================================
     * TEST 26 - QUICK STATUS CHANGE
     * =========================================================
     */

    it(
      "updates a task through the quick status control",
      async () => {

        const user =
          userEvent.setup();

        render(
          <Dashboard />
        );

        await user.click(
          await screen.findByText(
            "Website Redesign"
          )
        );

        const taskTitle =
          await screen.findByText(
            "Create homepage"
          );

        const taskCard =
          taskTitle.closest(
            "article"
          );

        await user.selectOptions(
          within(
            taskCard
          )
            .getByLabelText(
              "Quick Status"
            ),
          "IN_PROGRESS"
        );

        await waitFor(
          () => {

            expect(
              updateTask
            )
              .toHaveBeenCalledWith(
                1,
                101,
                {
                  title:
                    "Create homepage",
                  description:
                    "Build the new homepage.",
                  status:
                    "IN_PROGRESS",
                  priority:
                    "HIGH",
                  dueDate:
                    "2026-09-10",
                  assigneeId:
                    null,
                }
              );

          }
        );

        expect(
          await screen.findByText(
            "Task status changed to In Progress."
          )
        )
          .toBeInTheDocument();

      }
    );


    /*
     * =========================================================
     * TEST 27 - MARK TASK COMPLETE
     * =========================================================
     */

    it(
      "marks an open task complete",
      async () => {

        const user =
          userEvent.setup();

        render(
          <Dashboard />
        );

        await user.click(
          await screen.findByText(
            "Website Redesign"
          )
        );

        const taskTitle =
          await screen.findByText(
            "Create homepage"
          );

        const taskCard =
          taskTitle.closest(
            "article"
          );

        await user.click(
          within(
            taskCard
          )
            .getByRole(
              "button",
              {
                name:
                  "✓ Complete Task",
              }
            )
        );

        await waitFor(
          () => {

            expect(
              updateTask
            )
              .toHaveBeenCalledWith(
                1,
                101,
                {
                  title:
                    "Create homepage",
                  description:
                    "Build the new homepage.",
                  status:
                    "COMPLETED",
                  priority:
                    "HIGH",
                  dueDate:
                    "2026-09-10",
                  assigneeId:
                    null,
                }
              );

          }
        );

        expect(
          await screen.findByText(
            '"Create homepage" marked complete.'
          )
        )
          .toBeInTheDocument();

      }
    );


    /*
     * =========================================================
     * TEST 28 - TASK SAVE ERROR
     * =========================================================
     */

    it(
      "displays an error when task creation fails",
      async () => {

        const user =
          userEvent.setup();

        createTask.mockRejectedValue(
          new Error(
            "Unable to save task."
          )
        );

        render(
          <Dashboard />
        );

        await user.click(
          await screen.findByText(
            "Website Redesign"
          )
        );

        await user.click(
          screen.getByRole(
            "button",
            {
              name:
                "+ Add Task",
            }
          )
        );

        await user.type(
          screen.getByPlaceholderText(
            "Enter task title"
          ),
          "Broken Task"
        );

        await user.click(
          screen.getByRole(
            "button",
            {
              name:
                "Create Task",
            }
          )
        );

        expect(
          await screen.findByText(
            "Unable to save task."
          )
        )
          .toBeInTheDocument();

      }
    );

  }
);