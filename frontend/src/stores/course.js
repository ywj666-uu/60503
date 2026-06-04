import { defineStore } from 'pinia'
import { ref } from 'vue'
import { courseApi } from '@/api/course'

export const useCourseStore = defineStore('course', () => {
  const courses = ref([])
  const currentCourse = ref(null)
  const loading = ref(false)

  async function fetchCourses(scope) {
    loading.value = true
    try {
      const { data } = await courseApi.list(scope)
      courses.value = data
    } finally {
      loading.value = false
    }
  }

  async function fetchCourse(id) {
    const { data } = await courseApi.getById(id)
    currentCourse.value = data
    return data
  }

  return { courses, currentCourse, loading, fetchCourses, fetchCourse }
})
