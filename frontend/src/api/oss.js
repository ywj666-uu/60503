import api from './index'

export const ossApi = {
  getUploadCredentials(directory, filename) {
    return api.post('/oss/upload-credentials', { directory, filename })
  },

  async uploadFile(uploadUrl, file, contentType) {
    const response = await fetch(uploadUrl, {
      method: 'PUT',
      body: file,
      headers: { 'Content-Type': contentType }
    })
    return response.ok
  }
}
