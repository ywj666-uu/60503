export function hzToNoteName(hz) {
  if (!hz || hz <= 0) return '--'
  const noteNames = ['C', 'C#', 'D', 'D#', 'E', 'F', 'F#', 'G', 'G#', 'A', 'A#', 'B']
  const semitone = 12 * Math.log2(hz / 440) + 69
  const note = Math.round(semitone)
  const name = noteNames[note % 12]
  const octave = Math.floor(note / 12) - 1
  return `${name}${octave}`
}

export function hzToCents(hz, referenceHz) {
  if (!hz || !referenceHz || hz <= 0 || referenceHz <= 0) return 0
  return 1200 * Math.log2(hz / referenceHz)
}

export function formatDuration(seconds) {
  const min = Math.floor(seconds / 60)
  const sec = Math.floor(seconds % 60)
  return `${min}:${sec.toString().padStart(2, '0')}`
}
