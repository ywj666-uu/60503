import { defineStore } from 'pinia'
import { ref } from 'vue'

export const usePlayerStore = defineStore('player', () => {
  const isPlaying = ref(false)
  const currentTime = ref(0)
  const duration = ref(0)
  const playbackSpeed = ref(1.0)
  const loopStart = ref(null)
  const loopEnd = ref(null)
  const loopEnabled = ref(false)

  function setSpeed(speed) {
    playbackSpeed.value = Math.max(0.25, Math.min(2.0, speed))
  }

  function setLoop(start, end) {
    loopStart.value = start
    loopEnd.value = end
    loopEnabled.value = true
  }

  function clearLoop() {
    loopStart.value = null
    loopEnd.value = null
    loopEnabled.value = false
  }

  return {
    isPlaying, currentTime, duration, playbackSpeed,
    loopStart, loopEnd, loopEnabled,
    setSpeed, setLoop, clearLoop
  }
})
