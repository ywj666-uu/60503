import api from './index'

export const practiceApi = {
  submit(segmentId, data) {
    return api.post(`/segments/${segmentId}/practice`, data)
  },
  getHistory(segmentId) {
    return api.get(`/segments/${segmentId}/practice`)
  },
  getDetail(id) {
    return api.get(`/practice/${id}`)
  },
  triggerAnalysis(id) {
    return api.post(`/practice/${id}/analyze`)
  },
  getAnalysis(id) {
    return api.get(`/practice/${id}/analysis`)
  }
}
