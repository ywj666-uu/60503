import api from './index'

export const segmentApi = {
  listByCourse(courseId) {
    return api.get(`/courses/${courseId}/segments`)
  },
  getById(id) {
    return api.get(`/segments/${id}`)
  },
  create(courseId, data) {
    return api.post(`/courses/${courseId}/segments`, data)
  },
  update(id, data) {
    return api.put(`/segments/${id}`, data)
  },
  delete(id) {
    return api.delete(`/segments/${id}`)
  },
  addAnnotation(segmentId, data) {
    return api.post(`/segments/${segmentId}/annotations`, data)
  },
  getAnnotations(segmentId) {
    return api.get(`/segments/${segmentId}/annotations`)
  },
  deleteAnnotation(annotationId) {
    return api.delete(`/annotations/${annotationId}`)
  }
}
