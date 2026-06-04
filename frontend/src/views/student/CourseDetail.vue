<template>
  <div class="course-detail">
    <h2>可选课程</h2>
    <div class="course-grid">
      <el-card v-for="course in courses" :key="course.id" class="course-card">
        <h3>{{ course.title }}</h3>
        <el-tag size="small">{{ course.operaType || '综合' }}</el-tag>
        <p>{{ course.description || '暂无描述' }}</p>
        <div class="card-actions">
          <el-button
            v-if="!isEnrolled(course.id)"
            type="primary" size="small"
            @click="enroll(course.id)">
            选修
          </el-button>
          <template v-else>
            <el-button size="small" @click="viewSegments(course.id)">开始学习</el-button>
            <el-button size="small" @click="unenroll(course.id)">取消选修</el-button>
          </template>
        </div>
      </el-card>
    </div>

    <el-dialog v-model="showSegments" title="课程唱段" width="600px">
      <el-table :data="segments">
        <el-table-column prop="sequenceNum" label="序号" width="60" />
        <el-table-column prop="title" label="唱段" />
        <el-table-column prop="banShi" label="板式" width="120" />
        <el-table-column prop="diaoMen" label="调门" width="100" />
        <el-table-column label="操作" width="100">
          <template #default="{ row }">
            <el-button size="small" type="primary" @click="goToPractice(row.id)">练习</el-button>
          </template>
        </el-table-column>
      </el-table>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { courseApi } from '@/api/course'
import { segmentApi } from '@/api/segment'
import { ElMessage } from 'element-plus'

const router = useRouter()
const courses = ref([])
const enrolledIds = ref(new Set())
const showSegments = ref(false)
const segments = ref([])

async function fetchData() {
  const [coursesRes, enrollRes] = await Promise.all([
    courseApi.list(),
    courseApi.myEnrollments()
  ])
  courses.value = coursesRes.data
  enrolledIds.value = new Set(enrollRes.data.map(e => e.course?.id))
}

function isEnrolled(courseId) {
  return enrolledIds.value.has(courseId)
}

async function enroll(courseId) {
  await courseApi.enroll(courseId)
  enrolledIds.value.add(courseId)
  ElMessage.success('选修成功')
}

async function unenroll(courseId) {
  await courseApi.unenroll(courseId)
  enrolledIds.value.delete(courseId)
  ElMessage.success('已取消选修')
}

async function viewSegments(courseId) {
  const { data } = await segmentApi.listByCourse(courseId)
  segments.value = data
  showSegments.value = true
}

function goToPractice(segmentId) {
  showSegments.value = false
  router.push(`/student/practice/${segmentId}`)
}

onMounted(fetchData)
</script>

<style scoped>
.course-grid { display: grid; grid-template-columns: repeat(auto-fill, minmax(300px, 1fr)); gap: 16px; margin-top: 16px; }
.course-card p { color: #666; margin: 8px 0; font-size: 14px; }
.card-actions { margin-top: 12px; }
</style>
