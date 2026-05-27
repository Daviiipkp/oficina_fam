import { defineConfig } from 'vite'
import react from '@vitejs/plugin-react'
import tailwindcss from '@tailwindcss/vite'

// https://vite.dev/config/
export default defineConfig({
  base: './', 
  plugins: [
    react(),
    tailwindcss(),
  ],
  server: {
    host: '0.0.0.0',
    allowedHosts: ['code.daviipkp.org'],
    port: 5173,
    hmr: {
      // Diz ao Vite para usar a porta HTTPS padrão do seu domínio e o caminho do proxy
      clientPort: 443, 
      path: '/proxy/5173/', 
    }
  }
})