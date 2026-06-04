import { ref, onUnmounted } from 'vue'

export function useAudioRecorder() {
  const isRecording = ref(false)
  const audioBlob = ref(null)
  const duration = ref(0)

  let mediaRecorder = null
  let chunks = []
  let startTime = 0
  let timerInterval = null

  async function startRecording() {
    const stream = await navigator.mediaDevices.getUserMedia({
      audio: {
        sampleRate: 16000,
        channelCount: 1,
        echoCancellation: true,
        noiseSuppression: true
      }
    })

    chunks = []
    mediaRecorder = new MediaRecorder(stream, {
      mimeType: MediaRecorder.isTypeSupported('audio/webm;codecs=opus')
        ? 'audio/webm;codecs=opus'
        : 'audio/webm'
    })

    mediaRecorder.ondataavailable = (e) => {
      if (e.data.size > 0) chunks.push(e.data)
    }

    mediaRecorder.onstop = () => {
      audioBlob.value = new Blob(chunks, { type: mediaRecorder.mimeType })
      stream.getTracks().forEach(track => track.stop())
      clearInterval(timerInterval)
    }

    mediaRecorder.start(100)
    isRecording.value = true
    startTime = Date.now()
    timerInterval = setInterval(() => {
      duration.value = (Date.now() - startTime) / 1000
    }, 100)
  }

  function stopRecording() {
    if (mediaRecorder && mediaRecorder.state === 'recording') {
      mediaRecorder.stop()
      isRecording.value = false
    }
  }

  function reset() {
    audioBlob.value = null
    duration.value = 0
  }

  onUnmounted(() => {
    if (mediaRecorder && mediaRecorder.state === 'recording') {
      mediaRecorder.stop()
    }
    clearInterval(timerInterval)
  })

  return { isRecording, audioBlob, duration, startRecording, stopRecording, reset }
}
