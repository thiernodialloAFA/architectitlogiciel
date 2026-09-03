import { defineConfig } from 'vite'
import react from '@vitejs/plugin-react'

// https://vite.dev/config/
export default defineConfig({
  plugins: [react()],
  server: {
    proxy: {
      // Local development: forward API calls to the Spring Boot backend.
      '/api': 'http://localhost:8080',
    },
  },
})
