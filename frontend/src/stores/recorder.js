import { defineStore } from 'pinia'
import { ref } from 'vue'

export const useRecorderStore = defineStore('recorder', () => {
  const isRecording = ref(false)
  const audioBlob = ref(null)
  const audioUrl = ref(null)
  const recordingDuration = ref(0)

  function setRecording(blob) {
    audioBlob.value = blob
    audioUrl.value = blob ? URL.createObjectURL(blob) : null
  }

  function clear() {
    if (audioUrl.value) URL.revokeObjectURL(audioUrl.value)
    audioBlob.value = null
    audioUrl.value = null
    recordingDuration.value = 0
  }

  return { isRecording, audioBlob, audioUrl, recordingDuration, setRecording, clear }
})
