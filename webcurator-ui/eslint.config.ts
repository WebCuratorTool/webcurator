import { defineConfig } from "eslint/config";
import globals from "globals";
import js from "@eslint/js";
import pluginVue from "eslint-plugin-vue";
import prettierPlugin from "eslint-plugin-prettier";
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
    plugins: { js, prettier: prettierPlugin },
    extends: ["js/recommended"],
    languageOptions: {
      globals: globals.browser,
      parserOptions: { parser: tseslint.parser },
    },
    rules: {
      "prettier/prettier": "warn",
      "sort-imports": "warn",
    },
  },

  tseslint.configs.recommended,

  pluginVue.configs["flat/essential"],
]);
