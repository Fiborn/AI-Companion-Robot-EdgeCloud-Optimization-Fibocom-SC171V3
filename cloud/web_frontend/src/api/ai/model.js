import request from '@/utils/request'

export function listAiModel(query) {
  return request({
    url: '/ai/model/list',
    method: 'get',
    params: query
  })
}

export function getAiModel(id) {
  return request({
    url: '/ai/model/' + id,
    method: 'get'
  })
}

export function getActiveAiModel() {
  return request({
    url: '/ai/model/active',
    method: 'get'
  })
}

export function addAiModel(data) {
  return request({
    url: '/ai/model',
    method: 'post',
    data: data
  })
}

export function updateAiModel(data) {
  return request({
    url: '/ai/model',
    method: 'put',
    data: data
  })
}

export function activateAiModel(id) {
  return request({
    url: '/ai/model/' + id + '/active',
    method: 'put'
  })
}

export function delAiModel(id) {
  return request({
    url: '/ai/model/' + id,
    method: 'delete'
  })
}
