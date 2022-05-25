const { defineConfig } = require('@vue/cli-service')
module.exports = defineConfig({
  transpileDependencies: true,
  pages:{
    index:{
      entry: 'src/main.js',
      title: "Receptor ValleTPV"
    }
  },
  pluginOptions: {
    vuetify: {
			// https://github.com/vuetifyjs/vuetify-loader/tree/next/packages/vuetify-loader
		}
  },
  publicPath: process.env.NODE_ENV === 'production'
    ? '/static/receptor/'
    : '/',
})
