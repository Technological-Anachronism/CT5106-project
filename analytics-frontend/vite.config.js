import { defineConfig } from 'vite';
import react from '@vitejs/plugin-react';

// Calls to /api use the same origin in the browser; no CORS changes to teammates' code.
export default defineConfig({
  plugins: [react()],
  server: {
    host: '127.0.0.1',
    proxy: { '/api': 'http://127.0.0.1:8080' }
  }
});
