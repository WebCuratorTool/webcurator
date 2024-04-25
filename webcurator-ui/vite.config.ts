import { fileURLToPath, URL } from 'node:url'

import { defineConfig } from 'vite'
import vue from '@vitejs/plugin-vue'
import vueJsx from '@vitejs/plugin-vue-jsx'

// https://vitejs.dev/config/
export default defineConfig({
  base: './',
  plugins: [
    vue(),
    vueJsx(),
  ],
  resolve: {
    alias: {
      '@': fileURLToPath(new URL('./src', import.meta.url))
    }
  },
  server: {
    open: false,
    port: 5173,
    https: false,
    hotOnly: false,
    // proxy: {
    //   "/wct/api": {
    //     target: "http://localhost:6090/wct/api",
    //     changeOrigin: true, 
    //     // rewrite: (path) => path.replace(/^\/mis/, ""),
    //     // ws: true,                      
    //     // secure: true,
    //   },
    //   "/wct/auth": {
    //     target: "http://localhost:6090/wct/auth",
    //     changeOrigin: true, 
    //     // rewrite: (path) => path.replace(/^\/mis/, ""),
    //     // ws: true,                      
    //     // secure: true,
    //   },
    // },
  },
})
