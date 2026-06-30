import { defineConfig } from "vite";
import react from "@vitejs/plugin-react";

// In dev, the frontend talks to the Query Service through this proxy.
// Browser -> http://localhost:5173/api/services  ->  http://localhost:8080/api/v1/services
// Proxying makes dev same-origin, so CORS never enters the picture locally.
// The target is overridable with VITE_QUERY_PROXY_TARGET (e.g. when not using Docker).
export default defineConfig(() => {
  const target = process.env.VITE_QUERY_PROXY_TARGET || "http://localhost:8080";
  return {
    plugins: [react()],
    server: {
      port: 5173,
      proxy: {
        "/api": {
          target,
          changeOrigin: true,
          rewrite: (p) => p.replace(/^\/api/, "/api/v1"),
        },
      },
    },
    preview: { port: 4173 },
  };
});