import { defineConfig } from 'vite';

export default defineConfig({
  server: {
    proxy: {
      // 前端开发服务器把 /api 转发给后端 8090，避免跨域
      '/api': 'http://localhost:8090',
    },
  },
});
