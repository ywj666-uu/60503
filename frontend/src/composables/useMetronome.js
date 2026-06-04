import { ref, onUnmounted } from 'vue'
import { useMetronomeStore } from '@/stores/metronome'

export function useMetronome() {
  const store = useMetronomeStore()
  const beatTimes = ref([])

  let audioContext = null
  let nextNoteTime = 0
  let currentBeatInBar = 0
  let timerID = null
  let alignedBeatTimes = null
  let alignedBeatIndex = 0

  const lookahead = 25.0
  const scheduleAheadTime = 0.1

  function createClick(time, isAccent) {
    const osc = audioContext.createOscillator()
    const gain = audioContext.createGain()
    osc.connect(gain)
    gain.connect(audioContext.destination)

    osc.frequency.value = isAccent ? 1000 : 800
    gain.gain.value = isAccent ? 1.0 : 0.5
    gain.gain.exponentialRampToValueAtTime(0.001, time + 0.05)

    osc.start(time)
    osc.stop(time + 0.05)
  }

  function scheduleNote() {
    const [beats] = store.timeSignature.split('/').map(Number)
    const isAccent = currentBeatInBar === 0

    createClick(nextNoteTime, isAccent)
    beatTimes.value.push(nextNoteTime)

    if (alignedBeatTimes && alignedBeatIndex < alignedBeatTimes.length - 1) {
      alignedBeatIndex++
      const nextAligned = alignedBeatTimes[alignedBeatIndex]
      const currentAligned = alignedBeatTimes[alignedBeatIndex - 1]
      nextNoteTime += (nextAligned - currentAligned)
    } else {
      const secondsPerBeat = 60.0 / store.bpm
      nextNoteTime += secondsPerBeat
    }

    currentBeatInBar = (currentBeatInBar + 1) % beats
    store.currentBeat = currentBeatInBar
  }

  function scheduler() {
    while (nextNoteTime < audioContext.currentTime + scheduleAheadTime) {
      scheduleNote()
    }
    timerID = setTimeout(scheduler, lookahead)
  }

  function start(waveformPeaks = null) {
    if (store.isRunning) return
    audioContext = new (window.AudioContext || window.webkitAudioContext)()
    currentBeatInBar = 0
    beatTimes.value = []

    if (waveformPeaks && waveformPeaks.length > 0) {
      alignedBeatTimes = buildAlignedBeats(waveformPeaks, store.bpm)
      alignedBeatIndex = 0
      // 第一拍与第一个波形峰值对齐
      nextNoteTime = audioContext.currentTime + waveformPeaks[0]
    } else {
      alignedBeatTimes = null
      alignedBeatIndex = 0
      nextNoteTime = audioContext.currentTime
    }

    store.isRunning = true
    scheduler()
  }

  function stop() {
    store.isRunning = false
    clearTimeout(timerID)
    if (audioContext) {
      audioContext.close()
      audioContext = null
    }
    store.currentBeat = 0
    alignedBeatTimes = null
    alignedBeatIndex = 0
  }

  function buildAlignedBeats(peaks, bpm) {
    if (!peaks || peaks.length === 0) return null

    const beatInterval = 60.0 / bpm
    const firstPeak = peaks[0]
    const duration = peaks[peaks.length - 1] || 0
    const numBeats = Math.ceil((duration - firstPeak) / beatInterval) + 1
    const alignedBeats = []

    for (let i = 0; i < numBeats; i++) {
      const theoreticalTime = firstPeak + i * beatInterval
      let closestPeak = theoreticalTime
      let minDistance = beatInterval * 0.25

      for (const peak of peaks) {
        const distance = Math.abs(peak - theoreticalTime)
        if (distance < minDistance) {
          minDistance = distance
          closestPeak = peak
        }
      }
      alignedBeats.push(closestPeak)
    }

    return alignedBeats
  }

  function setBpm(value) {
    store.setBpm(value)
  }

  function setEstimatedBpm(bpmEstimation) {
    if (bpmEstimation && bpmEstimation.bpm) {
      store.setBpm(Math.round(bpmEstimation.bpm))
      if (bpmEstimation.beat_times && bpmEstimation.beat_times.length > 0) {
        alignedBeatTimes = bpmEstimation.beat_times
      }
    }
  }

  function setTimeSignature(ts) {
    store.timeSignature = ts
    currentBeatInBar = 0
  }

  onUnmounted(() => {
    stop()
  })

  return { beatTimes, start, stop, setBpm, setTimeSignature, setEstimatedBpm }
}
