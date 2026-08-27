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
 * TEST HELPERS
 * =============================================================
 */

async function selectWebsiteProject(
  user
) {

  await user.click(
    await screen.findByText(
      "Website Redesign"
    )
  );

}


async function getHomepageTaskCard() {

  const taskTitle =
    await screen.findByText(
      "Create homepage"
    );

  return taskTitle.closest(
    "article"
  );

}


/*
 * =============================================================
 * DASHBOARD TESTS
 * =============================================================
 */

describe(
  "Dashboard",
  () => {

    /*
     * =========================================================
     * COMMON MOCK SETUP
     * =========================================================
     */

    beforeEach(
      () => {

        vi.clearAllMocks();


        getProjects.mockResolvedValue(
          mockProjects
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

        const user =
          userEvent.setup();


        render(
          <Dashboard />
        );


        await selectWebsiteProject(
          user
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

        const user =
          userEvent.setup();


        render(
          <Dashboard />
        );


        await selectWebsiteProject(
          user
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

        const user =
          userEvent.setup();


        render(
          <Dashboard />
        );


        await selectWebsiteProject(
          user
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

        const user =
          userEvent.setup();


        render(
          <Dashboard />
        );


        await selectWebsiteProject(
          user
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
                  status: "ALL",
                  priority: "ALL",
                  search: "",
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

        const user =
          userEvent.setup();


        render(
          <Dashboard />
        );


        await selectWebsiteProject(
          user
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

        const user =
          userEvent.setup();


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


        await selectWebsiteProject(
          user
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
     * OPEN CREATE PROJECT
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

      }
    );


    /*
     * =========================================================
     * TEST 18
     *
     * CANCEL PROJECT DELETE
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
     * TEST 20
     *
     * OPEN CREATE TASK
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


        await selectWebsiteProject(
          user
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

      }
    );


    /*
     * =========================================================
     * TEST 21
     *
     * CREATE TASK
     * =========================================================
     */

    it(
      "creates a task for the selected project",
      async () => {

        const user =
          userEvent.setup();


        render(
          <Dashboard />
        );


        await selectWebsiteProject(
          user
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


        const taskPanel =
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
            taskPanel
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
                }
              );

          }
        );

      }
    );


    /*
     * =========================================================
     * TEST 22
     *
     * EDIT TASK FORM
     * =========================================================
     */

    it(
      "opens the edit task form with existing values",
      async () => {

        const user =
          userEvent.setup();


        render(
          <Dashboard />
        );


        await selectWebsiteProject(
          user
        );


        const taskCard =
          await getHomepageTaskCard();


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
     * TEST 23
     *
     * UPDATE TASK
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


        await selectWebsiteProject(
          user
        );


        const taskCard =
          await getHomepageTaskCard();


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


        await user.clear(
          titleInput
        );


        await user.type(
          titleInput,
          "Updated Dashboard Task"
        );


        const taskPanel =
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
            taskPanel
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
              .toHaveBeenCalled();

          }
        );

      }
    );


    /*
     * =========================================================
     * TEST 24
     *
     * DELETE TASK
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


        await selectWebsiteProject(
          user
        );


        const taskCard =
          await getHomepageTaskCard();


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

      }
    );


    /*
     * =========================================================
     * TEST 25
     *
     * CANCEL TASK DELETE
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


        await selectWebsiteProject(
          user
        );


        const taskCard =
          await getHomepageTaskCard();


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
     * TEST 26
     *
     * QUICK STATUS
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


        await selectWebsiteProject(
          user
        );


        const taskCard =
          await getHomepageTaskCard();


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
                }
              );

          }
        );

      }
    );


    /*
     * =========================================================
     * TEST 27
     *
     * MARK COMPLETE
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


        await selectWebsiteProject(
          user
        );


        const taskCard =
          await getHomepageTaskCard();


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
                }
              );

          }
        );

      }
    );


    /*
     * =========================================================
     * TEST 28
     *
     * TASK SAVE ERROR
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


        await selectWebsiteProject(
          user
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


    /*
     * =========================================================
     * TEST 29
     *
     * SEARCH TASKS
     * =========================================================
     */

    it(
      "sends the debounced search term to the server",
      async () => {

        const user =
          userEvent.setup();


        render(
          <Dashboard />
        );


        await selectWebsiteProject(
          user
        );


        await screen.findByText(
          "Create homepage"
        );


        await user.type(
          screen.getByPlaceholderText(
            "Search title or description"
          ),
          "homepage"
        );


        await waitFor(
          () => {

            expect(
              getProjectTasksPage
            )
              .toHaveBeenLastCalledWith(
                1,
                {
                  page: 0,
                  size: 10,
                  status: "ALL",
                  priority: "ALL",
                  search:
                    "homepage",
                  dueDateFilter:
                    "ALL",
                  sortBy:
                    "dueDate",
                  sortDirection:
                    "asc",
                }
              );

          },
          {
            timeout:
              2000,
          }
        );

      }
    );


    /*
     * =========================================================
     * TEST 30
     *
     * STATUS FILTER
     * =========================================================
     */

    it(
      "sends the selected status filter to the server",
      async () => {

        const user =
          userEvent.setup();


        render(
          <Dashboard />
        );


        await selectWebsiteProject(
          user
        );


        await screen.findByText(
          "Create homepage"
        );


        await user.selectOptions(
          screen.getByLabelText(
            "Status"
          ),
          "IN_PROGRESS"
        );


        await waitFor(
          () => {

            expect(
              getProjectTasksPage
            )
              .toHaveBeenLastCalledWith(
                1,
                expect.objectContaining({
                  page: 0,
                  status:
                    "IN_PROGRESS",
                })
              );

          }
        );

      }
    );


    /*
     * =========================================================
     * TEST 31
     *
     * PRIORITY FILTER
     * =========================================================
     */

    it(
      "sends the selected priority filter to the server",
      async () => {

        const user =
          userEvent.setup();


        render(
          <Dashboard />
        );


        await selectWebsiteProject(
          user
        );


        await screen.findByText(
          "Create homepage"
        );


        await user.selectOptions(
          screen.getByLabelText(
            "Priority"
          ),
          "HIGH"
        );


        await waitFor(
          () => {

            expect(
              getProjectTasksPage
            )
              .toHaveBeenLastCalledWith(
                1,
                expect.objectContaining({
                  page: 0,
                  priority:
                    "HIGH",
                })
              );

          }
        );

      }
    );


    /*
     * =========================================================
     * TEST 32
     *
     * DUE DATE FILTER
     * =========================================================
     */

    it(
      "sends the selected due date filter to the server",
      async () => {

        const user =
          userEvent.setup();


        render(
          <Dashboard />
        );


        await selectWebsiteProject(
          user
        );


        await screen.findByText(
          "Create homepage"
        );


        await user.selectOptions(
          screen.getByLabelText(
            "Due Date"
          ),
          "OVERDUE"
        );


        await waitFor(
          () => {

            expect(
              getProjectTasksPage
            )
              .toHaveBeenLastCalledWith(
                1,
                expect.objectContaining({
                  page: 0,
                  dueDateFilter:
                    "OVERDUE",
                })
              );

          }
        );

      }
    );


    /*
     * =========================================================
     * TEST 33
     *
     * SORT OPTION
     * =========================================================
     */

    it(
      "maps the selected sort option to backend sort parameters",
      async () => {

        const user =
          userEvent.setup();


        render(
          <Dashboard />
        );


        await selectWebsiteProject(
          user
        );


        await screen.findByText(
          "Create homepage"
        );


        await user.selectOptions(
          screen.getByLabelText(
            "Sort By"
          ),
          "DUE_DATE_DESC"
        );


        await waitFor(
          () => {

            expect(
              getProjectTasksPage
            )
              .toHaveBeenLastCalledWith(
                1,
                expect.objectContaining({
                  page: 0,
                  sortBy:
                    "dueDate",
                  sortDirection:
                    "desc",
                })
              );

          }
        );

      }
    );


    /*
     * =========================================================
     * TEST 34
     *
     * CLEAR FILTERS
     * =========================================================
     */

    it(
      "clears active filters and reloads the default task query",
      async () => {

        const user =
          userEvent.setup();


        render(
          <Dashboard />
        );


        await selectWebsiteProject(
          user
        );


        await screen.findByText(
          "Create homepage"
        );


        await user.selectOptions(
          screen.getByLabelText(
            "Status"
          ),
          "COMPLETED"
        );


        await user.selectOptions(
          screen.getByLabelText(
            "Priority"
          ),
          "LOW"
        );


        await user.selectOptions(
          screen.getByLabelText(
            "Due Date"
          ),
          "DUE_SOON"
        );


        const searchInput =
          screen.getByPlaceholderText(
            "Search title or description"
          );


        await user.type(
          searchInput,
          "navigation"
        );


        await waitFor(
          () => {

            expect(
              getProjectTasksPage
            )
              .toHaveBeenLastCalledWith(
                1,
                expect.objectContaining({
                  search:
                    "navigation",
                  status:
                    "COMPLETED",
                  priority:
                    "LOW",
                  dueDateFilter:
                    "DUE_SOON",
                })
              );

          },
          {
            timeout:
              2000,
          }
        );


        await user.click(
          screen.getByRole(
            "button",
            {
              name:
                "Clear Filters",
            }
          )
        );


        expect(
          searchInput
        )
          .toHaveValue(
            ""
          );


        expect(
          screen.getByLabelText(
            "Status"
          )
        )
          .toHaveValue(
            "ALL"
          );


        expect(
          screen.getByLabelText(
            "Priority"
          )
        )
          .toHaveValue(
            "ALL"
          );


        expect(
          screen.getByLabelText(
            "Due Date"
          )
        )
          .toHaveValue(
            "ALL"
          );


        await waitFor(
          () => {

            expect(
              getProjectTasksPage
            )
              .toHaveBeenLastCalledWith(
                1,
                expect.objectContaining({
                  page: 0,
                  search: "",
                  status:
                    "ALL",
                  priority:
                    "ALL",
                  dueDateFilter:
                    "ALL",
                })
              );

          }
        );

      }
    );


    /*
     * =========================================================
     * TEST 35
     *
     * PAGE SIZE
     * =========================================================
     */

    it(
      "reloads the first task page when page size changes",
      async () => {

        const user =
          userEvent.setup();


        render(
          <Dashboard />
        );


        await selectWebsiteProject(
          user
        );


        await screen.findByText(
          "Create homepage"
        );


        const pageSizeLabel =
          screen.getByText(
            "Per page"
          )
            .closest(
              "label"
            );


        const pageSizeSelect =
          within(
            pageSizeLabel
          )
            .getByRole(
              "combobox"
            );


        await user.selectOptions(
          pageSizeSelect,
          "20"
        );


        await waitFor(
          () => {

            expect(
              getProjectTasksPage
            )
              .toHaveBeenLastCalledWith(
                1,
                expect.objectContaining({
                  page: 0,
                  size: 20,
                })
              );

          }
        );

      }
    );


    /*
     * =========================================================
     * TEST 36
     *
     * PAGINATION
     * =========================================================
     */

    it(
      "loads the next and previous server pages",
      async () => {

        const user =
          userEvent.setup();


        getProjectTasksPage.mockImplementation(
          async (
            projectId,
            options
          ) => {

            const page =
              options.page;


            return {
              content: [
                {
                  id:
                    200 +
                    page,

                  title:
                    `Page ${page + 1} Task`,

                  description:
                    `Task on page ${page + 1}`,

                  status:
                    "OPEN",

                  priority:
                    "MEDIUM",

                  dueDate:
                    "2026-10-20",
                },
              ],

              totalElements:
                3,

              totalPages:
                3,

              numberOfElements:
                1,

              number:
                page,

              first:
                page === 0,

              last:
                page === 2,
            };

          }
        );


        render(
          <Dashboard />
        );


        await selectWebsiteProject(
          user
        );


        expect(
          await screen.findByText(
            "Page 1 Task"
          )
        )
          .toBeInTheDocument();


        let pagination =
          screen.getByRole(
            "navigation",
            {
              name:
                "Task pagination",
            }
          );


        await user.click(
          within(
            pagination
          )
            .getByRole(
              "button",
              {
                name:
                  "Next →",
              }
            )
        );


        expect(
          await screen.findByText(
            "Page 2 Task"
          )
        )
          .toBeInTheDocument();


        await waitFor(
          () => {

            expect(
              getProjectTasksPage
            )
              .toHaveBeenLastCalledWith(
                1,
                expect.objectContaining({
                  page: 1,
                })
              );

          }
        );


        expect(
          screen.getByText(
            "Page 2 of 3"
          )
        )
          .toBeInTheDocument();


        /*
         * Re-query after React re-render.
         */

        pagination =
          screen.getByRole(
            "navigation",
            {
              name:
                "Task pagination",
            }
          );


        await user.click(
          within(
            pagination
          )
            .getByRole(
              "button",
              {
                name:
                  "← Previous",
              }
            )
        );


        expect(
          await screen.findByText(
            "Page 1 Task"
          )
        )
          .toBeInTheDocument();


        await waitFor(
          () => {

            expect(
              getProjectTasksPage
            )
              .toHaveBeenLastCalledWith(
                1,
                expect.objectContaining({
                  page: 0,
                })
              );

          }
        );

      }
    );

  }
);