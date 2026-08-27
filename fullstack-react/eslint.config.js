import js from "@eslint/js";
import globals from "globals";

import reactHooks
  from "eslint-plugin-react-hooks";

import reactRefresh
  from "eslint-plugin-react-refresh";


export default [

  /*
   * =========================================================
   * GLOBAL IGNORES
   * =========================================================
   */

  {
    ignores: [
      "dist",
      "node_modules",
      "coverage",
    ],
  },


  /*
   * =========================================================
   * APPLICATION JAVASCRIPT / JSX
   * =========================================================
   */

  {
    files: [
      "**/*.{js,jsx}",
    ],

    languageOptions: {

      ecmaVersion:
        "latest",

      sourceType:
        "module",

      globals: {
        ...globals.browser,
        ...globals.es2021,
      },

      parserOptions: {

        ecmaFeatures: {
          jsx: true,
        },

      },

    },


    plugins: {

      "react-hooks":
        reactHooks,

      "react-refresh":
        reactRefresh,

    },


    rules: {

      /*
       * -----------------------------------------------------
       * BASE JAVASCRIPT RULES
       * -----------------------------------------------------
       */

      ...js.configs.recommended.rules,


      /*
       * -----------------------------------------------------
       * REACT HOOKS
       * -----------------------------------------------------
       */

      ...reactHooks
        .configs
        .recommended
        .rules,


      /*
       * -----------------------------------------------------
       * VITE / REACT REFRESH
       * -----------------------------------------------------
       */

      "react-refresh/only-export-components": [
        "warn",
        {
          allowConstantExport:
            true,
        },
      ],

    },

  },


  /*
   * =========================================================
   * VITEST TEST FILES
   * =========================================================
   *
   * Vitest globals include:
   *
   * describe
   * it
   * test
   * expect
   * beforeEach
   * afterEach
   * beforeAll
   * afterAll
   * vi
   *
   * =========================================================
   */

  {
    files: [
      "src/test/**/*.{js,jsx}",
      "**/*.test.{js,jsx}",
      "**/*.spec.{js,jsx}",
    ],

    languageOptions: {

      globals: {

        ...globals.browser,
        ...globals.es2021,

        describe:
          "readonly",

        it:
          "readonly",

        test:
          "readonly",

        expect:
          "readonly",

        beforeEach:
          "readonly",

        afterEach:
          "readonly",

        beforeAll:
          "readonly",

        afterAll:
          "readonly",

        vi:
          "readonly",

      },

    },

  },

];