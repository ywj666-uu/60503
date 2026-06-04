<template>
  <div class="course-manage">
    <div class="page-header">
      <h2>我的课程</h2>
      <el-button type="primary" @click="showCreate = true">创建课程</el-button>
    </div>

    <div class="course-grid">
      <el-card v-for="course in courses" :key="course.id" class="course-card">
        <h3>{{ course.title }}</h3>
        <el-tag :type="course.status === 'PUBLISHED' ? 'success' : 'info'" size="small">
          {{ course.status === 'PUBLISHED' ? '已发布' : '草稿' }}
        </el-tag>
        <p>{{ course.operaType || '未分类' }}</p>
        <p class="desc">{{ course.description || '暂无描述' }}</p>
        <div class="card-actions">
          <el-button size="small" @click="$router.push(`/teacher/courses/${course.id}/segments`)">
            管理唱段
          </el-button>
          <el-button size="small" v-if="course.status === 'DRAFT'" @click="publish(course.id)">
            发布
          </el-button>
          <el-button size="small" type="danger" @click="deleteCourse(course.id)">删除</el-button>
        </div>
      </el-card>
    </div>

    <el-dialog v-model="showCreate" title="创建课程" width="500px">
      <el-form :model="form" label-position="top">
        <el-form-item label="课程名称">
          <el-input v-model="form.title" placeholder="请输入课程名称" />
        </el-form-item>
        <el-form-item label="剧种">
          <el-select v-model="form.operaType" placeholder="选择剧种">
            <el-option label="京剧" value="京剧" />
            <el-option label="昆曲" value="昆曲" />
            <el-option label="豫剧" value="豫剧" />
            <el-option label="越剧" value="越剧" />
            <el-option label="黄梅戏" value="黄梅戏" />
            <el-option label="其他" value="其他" />
          </el-select>
        </el-form-item>
        <el-form-item label="描述">
          <el-input v-model="form.description" type="textarea" rows="3" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="showCreate = false">取消</el-button>
        <el-button type="primary" @click="createCourse">创建</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { courseApi } from '@/api/course'
import { ElMessage, ElMessageBox } from 'element-plus'

const courses = ref([])
const showCreate = ref(false)
const form = reactive({ title: '', operaType: '', description: '' })

async function fetchCourses() {
  const { data } = await courseApi.list('mine')
  courses.value = data
}

async function createCourse() {
  await courseApi.create(form)
  ElMessage.success('创建成功')
  showCreate.value = false
  form.title = ''
  form.operaType = ''
  form.description = ''
  fetchCourses()
}

async function publish(id) {
  await courseApi.publish(id)
  ElMessage.success('发布成功')
  fetchCourses()
}

async function deleteCourse(id) {
  await ElMessageBox.confirm('确定删除此课程？', '提示', { type: 'warning' })
  await courseApi.delete(id)
  ElMessage.success('删除成功')
  fetchCourses()
}

onMounted(fetchCourses)
</script>

<style scoped>
.page-header { display: flex; justify-content: space-between; align-items: center; margin-bottom: 24px; }
.course-grid { display: grid; grid-template-columns: repeat(auto-fill, minmax(300px, 1fr)); gap: 16px; }
.course-card h3 { margin-bottom: 8px; }
.course-card .desc { color: #999; font-size: 14px; margin: 8px 0; }
.card-actions { margin-top: 12px; display: flex; gap: 8px; }
</style>
