import { defineStore } from 'pinia'
import { ref } from 'vue'

export const useAnalysisStore = defineStore('analysis', () => {
  const result = ref(null)
  const loading = ref(false)

  return { result, loading }
})
