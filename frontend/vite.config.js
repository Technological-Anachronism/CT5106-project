
import { defineConfig } from "vite";
import react from "@vitejs/plugin-react";

// React frontend configuration
export default defineConfig({
    plugins: [react()],

    server: {
        host: "127.0.0.1",
        proxy: {
            "/api": "http://localhost:8080"
        }
    }
});
