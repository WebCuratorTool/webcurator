import { fileURLToPath } from "node:url";

import { configDefaults, defineConfig, mergeConfig } from "vitest/config";

import viteConfig from "./vite.config";

export default mergeConfig(
  viteConfig,
  defineConfig({
    resolve: {
      alias: {
        "@": fileURLToPath(new URL("./src", import.meta.url)),
      },
    },
    test: {
      environment: "jsdom",
      globalSetup: "./tests/setup/globalSetup.ts",
      setupFiles: ["./tests/setup/testSetup.ts"],
      include: ["tests/**/*.spec.ts", "src/**/__tests__/**/*.spec.ts"],
      exclude: [...configDefaults.exclude, "e2e/*"],
      root: fileURLToPath(new URL("./", import.meta.url)),
    },
  }),
);
