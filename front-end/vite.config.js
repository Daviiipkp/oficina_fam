import { defineConfig } from 'vite'
import react from '@vitejs/plugin-react'
import tailwindcss from '@tailwindcss/vite'

export default defineConfig({
  base: '/absproxy/5173/', 
  plugins: [
    react(),
    tailwindcss(),
  ],
  server: {
    host: '0.0.0.0',
    allowedHosts: ['code.daviipkp.org'],
    port: 5173,
    hmr: {
      protocol: 'wss',
      clientPort: 443, 
      path: '/absproxy/5173/', 
    }
  }
})