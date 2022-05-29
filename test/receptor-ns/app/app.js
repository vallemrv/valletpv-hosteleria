import Vue from 'nativescript-vue'
import Home from './components/Home'
import store from './store'


Vue.use(store)
new Vue({
  render: (h) => h('frame', [h(Home)]),
}).$start()
