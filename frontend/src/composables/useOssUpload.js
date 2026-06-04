import { ref } from 'vue'
import { ossApi } from '@/api/oss'

export function useOssUpload() {
  const uploading = ref(false)
  const progress = ref(0)

  async function upload(blob, directory) {
    uploading.value = true
    progress.value = 0

    try {
      const filename = `recording_${Date.now()}.webm`
      const { data: credentials } = await ossApi.getUploadCredentials(directory, filename)

      const xhr = new XMLHttpRequest()
      xhr.open('PUT', credentials.uploadUrl, true)
      xhr.setRequestHeader('Content-Type', 'audio/webm')

      xhr.upload.onprogress = (e) => {
        if (e.lengthComputable) {
          progress.value = Math.round((e.loaded / e.total) * 100)
        }
      }

      await new Promise((resolve, reject) => {
        xhr.onload = () => xhr.status < 400 ? resolve() : reject(new Error(`Upload failed: ${xhr.status}`))
        xhr.onerror = () => reject(new Error('Upload error'))
        xhr.send(blob)
      })

      return credentials.objectKey
    } finally {
      uploading.value = false
    }
  }

  return { uploading, progress, upload }
}
