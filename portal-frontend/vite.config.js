import { defineConfig, loadEnv } from 'vite';
import react from '@vitejs/plugin-react';

// Calls to /api are passed on to Spring Boot, so the browser never needs CORS.
export default defineConfig(({ mode }) => {
  const env = loadEnv(mode, process.cwd(), '');
  const target = process.env.PORTAL_API_TARGET || env.PORTAL_API_TARGET || 'http://127.0.0.1:8080';
  return {
    plugins: [react()],
    server: {
      host: '127.0.0.1',
      proxy: { '/api': target }
    },
    preview: { host: '127.0.0.1', proxy: { '/api': target } }
  };
});
