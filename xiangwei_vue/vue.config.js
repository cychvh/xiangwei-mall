// vue.config.js
const { defineConfig } = require('@vue/cli-service')

// vue.config.js
module.exports = {
  devServer: {
    client: {
      overlay: false
    },
    proxy: {
      '/api': {
        target: 'http://localhost:8080',
        changeOrigin: true,
        pathRewrite: { '^/api': '' }
      }
    }
  }
}
