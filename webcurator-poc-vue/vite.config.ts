import { fileURLToPath, URL } from 'node:url'

import { defineConfig } from 'vite'
import vue from '@vitejs/plugin-vue'

// https://vitejs.dev/config/
export default defineConfig({
  plugins: [vue()],
  resolve: {
    alias: {
      '@': fileURLToPath(new URL('./src', import.meta.url))
    }
  },
  server: {
    proxy: {
      '/auth': {
        target: 'http://localhost:8080/wct/auth/v1/token/',
        changeOrigin: true,
        secure: false,
        rewrite: (path) => path.replace(/^\/auth/, '')
      },
      '/api': {
        target: 'http://localhost:8080/wct/api/v1/',
        changeOrigin: true,
        secure: false,
        rewrite: (path) => path.replace(/^\/api/, '')
      },
    },
    cors:false
  },
})
