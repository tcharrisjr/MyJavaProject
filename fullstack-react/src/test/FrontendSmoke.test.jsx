// =========================================================
// FRONTEND TEST ENVIRONMENT SMOKE TEST
// =========================================================
//
// Purpose:
//
// Verify that:
//
// 1. Vitest starts successfully.
// 2. jsdom is available.
// 3. React Testing Library can render components.
// 4. jest-dom matchers are loaded correctly.
//
// This is intentionally a simple test.
//
// Once this passes, we know the test infrastructure is
// healthy before testing AuthContext, ProtectedRoute,
// Dashboard, projects, and tasks.
//
// =========================================================

import { render, screen } from "@testing-library/react";


// =========================================================
// SMALL TEST COMPONENT
// =========================================================

function TestComponent() {

  return (
    <main>
      <h1>Project Manager</h1>

      <p>
        Frontend test environment is working.
      </p>
    </main>
  );
}


// =========================================================
// TEST 1
//
// BASIC VITEST TEST
// =========================================================

describe(
  "Frontend test environment",
  () => {

    it(
      "runs Vitest successfully",
      () => {

        expect(true).toBe(true);

      }
    );


    // =====================================================
    // TEST 2
    //
    // REACT TESTING LIBRARY
    // =====================================================

    it(
      "renders a React component",
      () => {

        render(
          <TestComponent />
        );


        const heading =
          screen.getByRole(
            "heading",
            {
              name: "Project Manager",
            }
          );


        expect(
          heading
        )
          .toBeInTheDocument();

      }
    );


    // =====================================================
    // TEST 3
    //
    // JEST-DOM MATCHERS
    // =====================================================

    it(
      "loads jest-dom matchers",
      () => {

        render(
          <TestComponent />
        );


        const message =
          screen.getByText(
            "Frontend test environment is working."
          );


        expect(
          message
        )
          .toBeVisible();


        expect(
          message
        )
          .toHaveTextContent(
            "Frontend test environment is working."
          );

      }
    );

  }
);