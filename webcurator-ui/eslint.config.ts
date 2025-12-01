import js from "@eslint/js";
import { defineConfig } from "eslint/config";
import prettierPlugin from "eslint-plugin-prettier";
import simpleImportSort from "eslint-plugin-simple-import-sort";
import pluginVue from "eslint-plugin-vue";
import globals from "globals";
import tseslint from "typescript-eslint";

export default defineConfig([
  {
    ignores: [
      "node_modules/**",
      "dist/**",
      "build/**",
      "coverage/**",
      ".next/**",
      "public/**",
      "tmp/**",
      ".out/**",
      "**/*.min.js",
      "**/*.map",
      "**/*.svg",
      "**/*.png",
      "**/*.jpg",
    ],
  },

  {
    files: ["**/*.{js,mjs,cjs,ts,mts,cts,vue}"],
    plugins: {
      js,
      "simple-import-sort": simpleImportSort,
      prettier: prettierPlugin,
    },
    extends: ["js/recommended"],
    languageOptions: {
      globals: globals.browser,
      parserOptions: { parser: tseslint.parser },
    },
    rules: {
      "prettier/prettier": "warn",
      "simple-import-sort/imports": "error",
      "simple-import-sort/exports": "error",
    },
  },

  tseslint.configs.recommended,

  pluginVue.configs["flat/essential"],
]);
