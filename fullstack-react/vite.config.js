import { defineConfig } from "vite";
import react from "@vitejs/plugin-react";

export default defineConfig({
  // =========================================================
  // VITE PLUGINS
  // =========================================================
  plugins: [
    react(),
  ],

  // =========================================================
  // VITEST CONFIGURATION
  // =========================================================
  test: {
    // -------------------------------------------------------
    // Simulates a browser DOM environment for React tests.
    // -------------------------------------------------------
    environment: "jsdom",

    // -------------------------------------------------------
    // Shared test configuration.
    //
    // This file loads jest-dom matchers such as:
    //
    // toBeInTheDocument()
    // toHaveTextContent()
    // toBeVisible()
    // -------------------------------------------------------
    setupFiles: [
      "./src/test/setup.js",
    ],

    // -------------------------------------------------------
    // Allows describe(), it(), expect(), beforeEach(), etc.
    // without having to import them into every test file.
    // -------------------------------------------------------
    globals: true,

    // -------------------------------------------------------
    // Reset mock state between tests.
    // -------------------------------------------------------
    clearMocks: true,

    // -------------------------------------------------------
    // Restore mocked functions after each test.
    // -------------------------------------------------------
    restoreMocks: true,
  },
});