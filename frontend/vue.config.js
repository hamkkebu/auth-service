const { defineConfig } = require('@vue/cli-service')
module.exports = defineConfig({
  transpileDependencies: true,
  devServer: {
    // History API fallback for SPA routing
    historyApiFallback: true,

    // Proxy only API requests to backend
    proxy: {
      '/api': {
        // auth-service backend은 개발 환경에서 8081 포트 사용
        target: process.env.VUE_APP_API_URL || 'http://localhost:8081',
        changeOrigin: true,
        ws: false,
      }
    }
  }
})
