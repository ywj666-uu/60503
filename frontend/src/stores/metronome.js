import { defineStore } from 'pinia'
import { ref } from 'vue'

export const useMetronomeStore = defineStore('metronome', () => {
  const isRunning = ref(false)
  const bpm = ref(80)
  const timeSignature = ref('4/4')
  const currentBeat = ref(0)

  function setBpm(value) {
    bpm.value = Math.max(20, Math.min(300, value))
  }

  return { isRunning, bpm, timeSignature, currentBeat, setBpm }
})
