// =========================================================
// VITEST / REACT TESTING LIBRARY SETUP
// =========================================================
//
// This file runs before every test file.
//
// jest-dom extends Vitest's expect() function with useful
// DOM-specific assertions.
//
// Examples:
//
// expect(element).toBeInTheDocument();
// expect(element).toBeVisible();
// expect(element).toHaveTextContent("Project Dashboard");
//
// =========================================================

import "@testing-library/jest-dom/vitest";


// =========================================================
// OPTIONAL BROWSER API MOCKS
// =========================================================
//
// jsdom does not implement every browser API.
//
// These lightweight mocks prevent components from failing
// simply because they reference APIs that normally exist in
// a browser.
// =========================================================


// ---------------------------------------------------------
// MATCH MEDIA
//
// Some UI components and responsive code use:
//
// window.matchMedia(...)
//
// jsdom does not provide it by default.
// ---------------------------------------------------------

if (!window.matchMedia) {

  window.matchMedia = (query) => ({
    matches: false,
    media: query,
    onchange: null,

    addListener: () => {
      // Deprecated browser API.
    },

    removeListener: () => {
      // Deprecated browser API.
    },

    addEventListener: () => {
      // Modern browser API.
    },

    removeEventListener: () => {
      // Modern browser API.
    },

    dispatchEvent: () => false,
  });
}


// ---------------------------------------------------------
// SCROLL TO
//
// Prevent errors if application components call:
// window.scrollTo(...)
// ---------------------------------------------------------

if (!window.scrollTo) {

  window.scrollTo = () => {
    // No operation required in tests.
  };
}