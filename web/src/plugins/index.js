import cache from './cache'
import modal from './modal'
import http from './http'
export default {
  install(Vue) {
    // 缓存对象
    Vue.prototype.$cache = cache
    // 模态框对象
    Vue.prototype.$modal = modal
    // http请求
    Vue.prototype.$http = http
  }
}
