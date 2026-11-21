import { defineConfig, loadEnv } from 'vite';
import react from '@vitejs/plugin-react';

// Calls to /api use the same origin in the browser; no CORS changes to teammates' code.
export default defineConfig(({ mode }) => {
  const env = loadEnv(mode, process.cwd(), '');
  const target = process.env.ANALYTICS_API_TARGET || env.ANALYTICS_API_TARGET || 'http://127.0.0.1:8080';
  return {
    plugins: [react()],
    server: {
      host: '127.0.0.1',
      proxy: { '/api': target }
    },
    preview: { host: '127.0.0.1', proxy: { '/api': target } }
  };
});
