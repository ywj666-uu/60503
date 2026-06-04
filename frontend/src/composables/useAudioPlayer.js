import { ref, watch, onUnmounted } from 'vue'
import { usePlayerStore } from '@/stores/player'

export function useAudioPlayer() {
  const playerStore = usePlayerStore()
  const isReady = ref(false)
  const waveformPeaks = ref([])

  let audioContext = null
  let audioBuffer = null
  let sourceNode = null
  let gainNode = null
  let isSourcePlaying = false
  let startOffset = 0
  let startTimestamp = 0
  let animFrameId = null
  let silenceStart = 0
  let silenceEnd = 0

  async function init(src) {
    cleanup()
    audioContext = new (window.AudioContext || window.webkitAudioContext)()
    gainNode = audioContext.createGain()
    gainNode.connect(audioContext.destination)

    const response = await fetch(src)
    const arrayBuffer = await response.arrayBuffer()
    audioBuffer = await audioContext.decodeAudioData(arrayBuffer)

    detectSilenceBounds()
    playerStore.duration = silenceEnd - silenceStart
    isReady.value = true

    extractWaveformPeaks()
  }

  function detectSilenceBounds() {
    if (!audioBuffer) return
    const data = audioBuffer.getChannelData(0)
    const sampleRate = audioBuffer.sampleRate
    const threshold = 0.01
    const windowSamples = Math.floor(sampleRate * 0.02)

    let start = 0
    for (let i = 0; i < data.length - windowSamples; i += windowSamples) {
      let rms = 0
      for (let j = i; j < i + windowSamples; j++) {
        rms += data[j] * data[j]
      }
      rms = Math.sqrt(rms / windowSamples)
      if (rms > threshold) {
        start = i / sampleRate
        break
      }
    }

    let end = audioBuffer.duration
    for (let i = data.length - windowSamples; i >= 0; i -= windowSamples) {
      let rms = 0
      for (let j = i; j < i + windowSamples; j++) {
        rms += data[j] * data[j]
      }
      rms = Math.sqrt(rms / windowSamples)
      if (rms > threshold) {
        end = (i + windowSamples) / sampleRate
        break
      }
    }

    silenceStart = Math.max(0, start - 0.05)
    silenceEnd = Math.min(audioBuffer.duration, end + 0.05)
  }

  function extractWaveformPeaks() {
    if (!audioBuffer) return
    const data = audioBuffer.getChannelData(0)
    const sampleRate = audioBuffer.sampleRate
    const windowSize = Math.floor(sampleRate * 0.05)
    const peaks = []
    const startSample = Math.floor(silenceStart * sampleRate)
    const endSample = Math.floor(silenceEnd * sampleRate)

    for (let i = startSample; i < endSample - windowSize; i += windowSize) {
      let maxVal = 0
      let maxIdx = i
      for (let j = i; j < i + windowSize; j++) {
        const abs = Math.abs(data[j])
        if (abs > maxVal) {
          maxVal = abs
          maxIdx = j
        }
      }
      if (maxVal > 0.1) {
        peaks.push(maxIdx / sampleRate - silenceStart)
      }
    }
    waveformPeaks.value = peaks
  }

  function createStretchedSource(speed) {
    if (!audioBuffer || !audioContext) return null

    const source = audioContext.createBufferSource()
    source.buffer = audioBuffer
    source.playbackRate.value = speed

    const pitchCompensation = -1200 * Math.log2(speed)
    source.detune.value = pitchCompensation

    source.connect(gainNode)
    return source
  }

  function play() {
    if (!audioBuffer || !audioContext) return
    if (audioContext.state === 'suspended') audioContext.resume()

    if (isSourcePlaying) {
      stopSource()
    }

    const speed = playerStore.playbackSpeed
    sourceNode = createStretchedSource(speed)
    if (!sourceNode) return

    const offset = startOffset + silenceStart
    const loopStart = playerStore.loopEnabled
      ? (playerStore.loopStart || 0) + silenceStart
      : silenceStart
    const loopEnd = playerStore.loopEnabled
      ? (playerStore.loopEnd || silenceEnd - silenceStart) + silenceStart
      : silenceEnd

    if (playerStore.loopEnabled) {
      sourceNode.loop = true
      sourceNode.loopStart = loopStart
      sourceNode.loopEnd = loopEnd
    }

    sourceNode.onended = () => {
      if (!playerStore.loopEnabled) {
        playerStore.isPlaying = false
        isSourcePlaying = false
        startOffset = 0
      }
    }

    sourceNode.start(0, offset)
    startTimestamp = audioContext.currentTime
    isSourcePlaying = true
    playerStore.isPlaying = true

    trackTime()
  }

  function pause() {
    if (!isSourcePlaying) return
    const elapsed = (audioContext.currentTime - startTimestamp) * playerStore.playbackSpeed
    startOffset += elapsed

    const effectiveDuration = silenceEnd - silenceStart
    if (playerStore.loopEnabled && playerStore.loopEnd) {
      const loopLen = playerStore.loopEnd - (playerStore.loopStart || 0)
      if (loopLen > 0) {
        startOffset = (playerStore.loopStart || 0) + ((startOffset - (playerStore.loopStart || 0)) % loopLen)
      }
    }

    startOffset = Math.min(startOffset, effectiveDuration)
    stopSource()
    playerStore.isPlaying = false
    cancelAnimationFrame(animFrameId)
  }

  function seek(time) {
    const wasPlaying = isSourcePlaying
    if (isSourcePlaying) stopSource()
    startOffset = time
    playerStore.currentTime = time
    if (wasPlaying) play()
  }

  function setSpeed(rate) {
    playerStore.setSpeed(rate)
    if (isSourcePlaying) {
      const currentPos = getCurrentTime()
      stopSource()
      startOffset = currentPos
      play()
    }
  }

  function getCurrentTime() {
    if (!isSourcePlaying || !audioContext) return startOffset
    const elapsed = (audioContext.currentTime - startTimestamp) * playerStore.playbackSpeed
    let t = startOffset + elapsed

    const effectiveDuration = silenceEnd - silenceStart
    if (playerStore.loopEnabled && playerStore.loopEnd) {
      const loopStart = playerStore.loopStart || 0
      const loopLen = playerStore.loopEnd - loopStart
      if (loopLen > 0 && t > playerStore.loopEnd) {
        t = loopStart + ((t - loopStart) % loopLen)
      }
    }
    return Math.min(t, effectiveDuration)
  }

  function trackTime() {
    playerStore.currentTime = getCurrentTime()
    if (isSourcePlaying) {
      animFrameId = requestAnimationFrame(trackTime)
    }
  }

  function stopSource() {
    if (sourceNode) {
      try { sourceNode.stop() } catch {}
      sourceNode.disconnect()
      sourceNode = null
    }
    isSourcePlaying = false
  }

  function cleanup() {
    cancelAnimationFrame(animFrameId)
    stopSource()
    if (gainNode) { gainNode.disconnect(); gainNode = null }
    if (audioContext) { audioContext.close(); audioContext = null }
    audioBuffer = null
    isReady.value = false
    startOffset = 0
    silenceStart = 0
    silenceEnd = 0
  }

  watch(() => playerStore.playbackSpeed, () => {
    if (isSourcePlaying) {
      const currentPos = getCurrentTime()
      stopSource()
      startOffset = currentPos
      play()
    }
  })

  onUnmounted(cleanup)

  return { isReady, waveformPeaks, init, play, pause, seek, setSpeed }
}
