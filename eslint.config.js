import js from "@eslint/js";
import tseslint from "@typescript-eslint/eslint-plugin";
import tsParser from "@typescript-eslint/parser";
import { FlatCompat } from "@eslint/eslintrc";
import path from "path";
import { fileURLToPath } from "url";
import globals from "globals";

const __filename = fileURLToPath(import.meta.url);
const __dirname = path.dirname(__filename);

export default [
  {
    ignores: ["node_modules/", "dist/", ".angular/"],
  },
  js.configs.recommended,

  // Main TypeScript configurations (for .ts files excluding .spec.ts)
  {
    files: ["src/**/*.ts", "!src/**/*.spec.ts"], // Apply to .ts but not .spec.ts
    languageOptions: {
      parser: tsParser,
      parserOptions: {
        ecmaVersion: "latest",
        sourceType: "module",
        project: ["./tsconfig.json", "./tsconfig.app.json"], // Adjusted for non-spec files
      },
      globals: {
        ...globals.browser,
        "google": "readonly", // Define 'google' as a global variable
      },
    },
    plugins: {
      "@typescript-eslint": tseslint,
    },
    rules: {
        ...(tseslint.configs.recommended && tseslint.configs.recommended.rules ? tseslint.configs.recommended.rules : {}),
        "@typescript-eslint/no-unused-vars": [
          "error",
          { "argsIgnorePattern": "^_", "varsIgnorePattern": "^_" }
        ],
        '@typescript-eslint/naming-convention': [
          'error',
          { selector: 'variable', format: ['camelCase', 'UPPER_CASE', 'PascalCase'], leadingUnderscore: 'allow' },
          { selector: 'function', format: ['camelCase'], leadingUnderscore: 'allow' },
          { selector: 'method', format: ['camelCase'], leadingUnderscore: 'allow' },
          { selector: 'parameter', format: ['camelCase'], leadingUnderscore: 'allow' },
          { selector: 'property', format: ['camelCase', 'UPPER_CASE'], leadingUnderscore: 'allow' },
          { selector: 'typeLike', format: ['PascalCase'] },
        ],
        // Add other non-spec specific rules or overrides here
    }
  },

  // TypeScript Test specific configurations (for .spec.ts files)
  {
    files: ["src/**/*.spec.ts"],
    languageOptions: {
      parser: tsParser,
      parserOptions: {
        ecmaVersion: "latest",
        sourceType: "module",
        project: ["./tsconfig.spec.json"], // Specific tsconfig for tests
      },
      globals: {
        ...globals.browser,
        ...globals.jasmine, // Add Jasmine testing environment globals
        "google": "readonly", // If google is also used in tests
      },
    },
    plugins: {
      "@typescript-eslint": tseslint,
    },
    rules: {
        ...(tseslint.configs.recommended && tseslint.configs.recommended.rules ? tseslint.configs.recommended.rules : {}),
        "@typescript-eslint/no-unused-vars": [
          "error",
          { "argsIgnorePattern": "^_", "varsIgnorePattern": "^_" }
        ],
        '@typescript-eslint/naming-convention': [
          'error',
          { selector: 'variable', format: ['camelCase', 'UPPER_CASE', 'PascalCase'], leadingUnderscore: 'allow' },
          { selector: 'function', format: ['camelCase'], leadingUnderscore: 'allow' },
          { selector: 'method', format: ['camelCase'], leadingUnderscore: 'allow' },
          { selector: 'parameter', format: ['camelCase'], leadingUnderscore: 'allow' },
          { selector: 'property', format: ['camelCase', 'UPPER_CASE'], leadingUnderscore: 'allow' },
          { selector: 'typeLike', format: ['PascalCase'] },
        ],
        // Potentially relax some rules for test files, e.g. no-explicit-any
        // "@typescript-eslint/no-explicit-any": "off",
    }
  },

  // Global rules or overrides affecting all matched files (js, ts, etc.)
  {
    rules: {
      // Example: 'no-console': 'warn',
    }
  }
];
