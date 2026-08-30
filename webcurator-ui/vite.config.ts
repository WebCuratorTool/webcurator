import { fileURLToPath, URL } from "node:url";

import vue from "@vitejs/plugin-vue";
import vueJsx from "@vitejs/plugin-vue-jsx";
import Components from "unplugin-vue-components/vite";
import { defineConfig, loadEnv } from "vite";

// https://vitejs.dev/config/
export default defineConfig(({ mode }) => {
  const env = loadEnv(mode, process.cwd(), "");
  const backendUrl = env.VITE_BACKEND_URL || "http://localhost:8080";

  return {
    base: "/wct/",
    plugins: [vue(), vueJsx(), Components()],
    resolve: {
      alias: { "@": fileURLToPath(new URL("./src", import.meta.url)) },
    },
    server: {
      open: false,
      port: 5173,
      proxy: {
        // Proxy auth and API calls like /wct/auth/v1/* and /wct/api/v1/* in dev.
        "/wct": {
          target: backendUrl,
          changeOrigin: true,
          secure: false,
        },
      },
      // https: false,
      // hotOnly: false,
    },
  };
});
