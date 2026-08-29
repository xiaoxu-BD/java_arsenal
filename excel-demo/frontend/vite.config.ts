import { defineConfig } from 'vite';
import vue from '@vitejs/plugin-vue';

export default defineConfig({
  plugins: [vue()],
  server: {
    proxy: {
      // 前端开发服务器把 /api 转发给后端 8091，避免跨域
      '/api': 'http://localhost:8091',
    },
  },
});
