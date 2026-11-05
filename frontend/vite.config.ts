import react from '@vitejs/plugin-react'
import { defineConfig } from 'vite'

// https://vite.dev/config/
export default defineConfig({
  plugins: [react()],
  server: {
    host: '127.0.0.1',
    // запросы /api уходят на Spring Boot, CORS не нужен
    proxy: {
      '/api': 'http://localhost:8080',
    },
  },
})
