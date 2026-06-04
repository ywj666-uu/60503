<template>
  <div class="segment-manage">
    <div class="page-header">
      <el-button @click="$router.back()">返回</el-button>
      <h2>唱段管理</h2>
      <el-button type="primary" @click="showUpload = true">上传唱段</el-button>
    </div>

    <el-table :data="segments" stripe>
      <el-table-column prop="sequenceNum" label="序号" width="60" />
      <el-table-column prop="title" label="唱段名称" />
      <el-table-column prop="banShi" label="板式" width="120" />
      <el-table-column prop="diaoMen" label="调门" width="100" />
      <el-table-column prop="difficulty" label="难度" width="80">
        <template #default="{ row }">
          <el-rate v-model="row.difficulty" disabled :max="5" size="small" />
        </template>
      </el-table-column>
      <el-table-column prop="bpm" label="BPM" width="80" />
      <el-table-column label="操作" width="200">
        <template #default="{ row }">
          <el-button size="small" @click="editAnnotations(row)">标注</el-button>
          <el-button size="small" type="danger" @click="deleteSegment(row.id)">删除</el-button>
        </template>
      </el-table-column>
    </el-table>

    <el-dialog v-model="showUpload" title="上传唱段" width="600px">
      <el-form :model="uploadForm" label-position="top">
        <el-form-item label="唱段名称">
          <el-input v-model="uploadForm.title" />
        </el-form-item>
        <el-form-item label="音频文件">
          <el-upload
            :auto-upload="false"
            :on-change="handleFileChange"
            accept=".wav,.mp3,.flac,.webm"
            :limit="1">
            <el-button>选择音频文件</el-button>
          </el-upload>
        </el-form-item>
        <el-row :gutter="16">
          <el-col :span="8">
            <el-form-item label="板式">
              <el-input v-model="uploadForm.banShi" placeholder="如：西皮原板" />
            </el-form-item>
          </el-col>
          <el-col :span="8">
            <el-form-item label="调门">
              <el-input v-model="uploadForm.diaoMen" placeholder="如：二黄" />
            </el-form-item>
          </el-col>
          <el-col :span="8">
            <el-form-item label="BPM">
              <el-input-number v-model="uploadForm.bpm" :min="20" :max="300" />
            </el-form-item>
          </el-col>
        </el-row>
        <el-form-item label="难度">
          <el-rate v-model="uploadForm.difficulty" :max="5" />
        </el-form-item>
        <el-form-item label="难点提示">
          <el-input v-model="uploadForm.difficultyTips" type="textarea" rows="2" />
        </el-form-item>
        <el-form-item label="唱词">
          <el-input v-model="uploadForm.lyrics" type="textarea" rows="3" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="showUpload = false">取消</el-button>
        <el-button type="primary" :loading="uploading" @click="handleUpload">上传</el-button>
      </template>
    </el-dialog>

    <el-dialog v-model="showAnnotation" title="标注管理" width="700px">
      <div v-if="currentSegment">
        <el-form :model="annotationForm" inline>
          <el-form-item label="开始(秒)">
            <el-input-number v-model="annotationForm.startTime" :precision="2" :step="0.1" :min="0" />
          </el-form-item>
          <el-form-item label="结束(秒)">
            <el-input-number v-model="annotationForm.endTime" :precision="2" :step="0.1" :min="0" />
          </el-form-item>
          <el-form-item label="类型">
            <el-select v-model="annotationForm.type">
              <el-option label="板式" value="BANSHI" />
              <el-option label="气口" value="BREATH" />
              <el-option label="装饰音" value="ORNAMENT" />
              <el-option label="难点" value="DIFFICULTY" />
              <el-option label="其他" value="OTHER" />
            </el-select>
          </el-form-item>
          <el-form-item label="标签">
            <el-input v-model="annotationForm.label" />
          </el-form-item>
          <el-form-item>
            <el-button type="primary" @click="addAnnotation">添加</el-button>
          </el-form-item>
        </el-form>
        <el-table :data="annotations" size="small" style="margin-top:16px">
          <el-table-column prop="startTime" label="开始" width="80" />
          <el-table-column prop="endTime" label="结束" width="80" />
          <el-table-column prop="type" label="类型" width="80" />
          <el-table-column prop="label" label="标签" />
          <el-table-column label="操作" width="80">
            <template #default="{ row }">
              <el-button size="small" type="danger" @click="removeAnnotation(row.id)">删除</el-button>
            </template>
          </el-table-column>
        </el-table>
      </div>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { useRoute } from 'vue-router'
import { segmentApi } from '@/api/segment'
import { ossApi } from '@/api/oss'
import { ElMessage, ElMessageBox } from 'element-plus'

const route = useRoute()
const courseId = route.params.courseId

const segments = ref([])
const showUpload = ref(false)
const showAnnotation = ref(false)
const uploading = ref(false)
const currentSegment = ref(null)
const annotations = ref([])
let selectedFile = null

const uploadForm = reactive({
  title: '', banShi: '', diaoMen: '', bpm: 80,
  difficulty: 3, difficultyTips: '', lyrics: ''
})

const annotationForm = reactive({
  startTime: 0, endTime: 1, type: 'OTHER', label: ''
})

async function fetchSegments() {
  const { data } = await segmentApi.listByCourse(courseId)
  segments.value = data
}

function handleFileChange(file) {
  selectedFile = file.raw
}

async function handleUpload() {
  if (!selectedFile) return ElMessage.warning('请选择音频文件')
  uploading.value = true
  try {
    const { data: creds } = await ossApi.getUploadCredentials(
      `teacher-audio/${courseId}`, selectedFile.name
    )
    await ossApi.uploadFile(creds.uploadUrl, selectedFile, 'audio/wav')

    await segmentApi.create(courseId, {
      ...uploadForm,
      audioOssKey: creds.objectKey
    })
    ElMessage.success('上传成功')
    showUpload.value = false
    fetchSegments()
  } catch (e) {
    ElMessage.error('上传失败')
  } finally {
    uploading.value = false
  }
}

async function editAnnotations(segment) {
  currentSegment.value = segment
  const { data } = await segmentApi.getAnnotations(segment.id)
  annotations.value = data
  showAnnotation.value = true
}

async function addAnnotation() {
  await segmentApi.addAnnotation(currentSegment.value.id, annotationForm)
  const { data } = await segmentApi.getAnnotations(currentSegment.value.id)
  annotations.value = data
  ElMessage.success('添加成功')
}

async function removeAnnotation(id) {
  await segmentApi.deleteAnnotation(id)
  annotations.value = annotations.value.filter(a => a.id !== id)
}

async function deleteSegment(id) {
  await ElMessageBox.confirm('确定删除此唱段？', '提示', { type: 'warning' })
  await segmentApi.delete(id)
  fetchSegments()
}

onMounted(fetchSegments)
</script>

<style scoped>
.page-header { display: flex; align-items: center; gap: 16px; margin-bottom: 24px; }
.page-header h2 { flex: 1; }
</style>
