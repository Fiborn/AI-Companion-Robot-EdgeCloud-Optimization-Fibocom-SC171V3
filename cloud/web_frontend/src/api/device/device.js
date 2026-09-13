import request from '@/utils/request'

// 查询设备信息列表
export function listDevice(query) {
  return request({
    url: '/device/info/list',
    method: 'get',
    params: query
  })
}

// 查询设备信息详细
export function getDevice(id) {
  return request({
    url: '/device/info/' + id,
    method: 'get'
  })
}

// 新增设备信息
export function addDevice(data) {
  return request({
    url: '/device/info',
    method: 'post',
    data: data
  })
}

// 修改设备信息
export function updateDevice(data) {
  return request({
    url: '/device/info',
    method: 'put',
    data: data
  })
}

// 删除设备信息
export function delDevice(id) {
  return request({
    url: '/device/info/' + id,
    method: 'delete'
  })
}

// 在线设备下发 RGB PWM（占空比 0～255，字段 pwmLamp1/2/3 依次为红/绿/蓝）
export function setDevicePwm(data) {
  return request({
    url: '/device/info/pwm',
    method: 'post',
    data: data
  })
}
