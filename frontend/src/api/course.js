import api from './index'

export const courseApi = {
  list(scope) {
    return api.get('/courses', { params: { scope } })
  },
  getById(id) {
    return api.get(`/courses/${id}`)
  },
  create(data) {
    return api.post('/courses', data)
  },
  update(id, data) {
    return api.put(`/courses/${id}`, data)
  },
  publish(id) {
    return api.put(`/courses/${id}/publish`)
  },
  delete(id) {
    return api.delete(`/courses/${id}`)
  },
  enroll(courseId) {
    return api.post(`/courses/${courseId}/enroll`)
  },
  unenroll(courseId) {
    return api.delete(`/courses/${courseId}/enroll`)
  },
  myEnrollments() {
    return api.get('/my/enrollments')
  }
}
