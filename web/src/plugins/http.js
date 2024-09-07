import axios from 'axios'
import {Message} from 'element-ui'
import QS from 'qs';

axios.defaults.headers['Content-Type'] = 'application/json;charset=utf-8'
// 创建axios实例
const service = axios.create({
  // axios中请求配置有baseURL选项，表示请求URL公共部分
  baseURL: process.env.VUE_APP_BASE_API,
  // 超时
  timeout: 100000
})

// request拦截器
service.interceptors.request.use(config => {
  config.headers['X-Requested-With'] = 'XMLHttpRequest';
  config.paramsSerializer = param => QS.stringify(param, {indices: false});
  if (config.params) {
    for (const key in config.params) {
      const obj = config.params[key];
      if ([null, undefined, '', NaN].includes(obj) || (Array.isArray(obj) && obj.length <= 0)) {
        delete config.params[key];
      }
    }
  }
  return config;
}, error => {
  console.log(error)
  Promise.reject(error)
})

// 响应拦截器
service.interceptors.response.use(res => {
    return res.data;
  },
  err => {
    let {message} = err;
    if (message === "Network Error") {
      Message.error('后端接口连接异常');
      return Promise.reject(err)
    }
    if (message.includes("timeout")) {
      Message.error('系统接口请求超时');
      return Promise.reject(err)
    }
    Message.error(err.response.data);
    return Promise.reject(err)
  }
)
service.save = (url, data) => data.id ? service.patch(`${url}/${data.id}`, data) : service.post(url, data);

export default service
