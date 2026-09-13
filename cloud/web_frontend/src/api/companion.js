import request from '@/utils/request'

export function getCompanionSensors(did = '000001') {
  return request({
    url: '/companion/sensors',
    method: 'get',
    params: { did }
  })
}

export function getCompanionCamera(did = '000001') {
  return request({
    url: '/companion/camera',
    method: 'get',
    params: { did }
  })
}

export function takeCompanionCameraPhoto(did = '000001') {
  return request({
    url: '/companion/camera/photo',
    method: 'get',
    params: { did }
  })
}

export function configureCompanionCamera(did = '000001', data = {}) {
  return request({
    url: '/companion/camera/config',
    method: 'post',
    params: { did },
    data
  })
}

export function chatWithCompanion(data) {
  return request({
    url: '/companion/chat',
    method: 'post',
    data
  })
}
