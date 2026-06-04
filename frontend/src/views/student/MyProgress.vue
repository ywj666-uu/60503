<template>
  <div class="my-progress">
    <h2>我的学习进度</h2>
    <el-empty v-if="!records.length" description="暂无练习记录" />
    <el-table v-else :data="records">
      <el-table-column prop="createdAt" label="练习时间" width="180" />
      <el-table-column label="唱段">
        <template #default="{ row }">{{ row.segment?.title || '-' }}</template>
      </el-table-column>
      <el-table-column prop="status" label="状态" width="100">
        <template #default="{ row }">
          <el-tag :type="row.status === 'COMPLETED' ? 'success' : 'info'" size="small">
            {{ row.status }}
          </el-tag>
        </template>
      </el-table-column>
      <el-table-column label="操作" width="120">
        <template #default="{ row }">
          <el-button v-if="row.status === 'COMPLETED'" size="small"
                     @click="$router.push(`/student/practice/${row.segment?.id}`)">
            查看
          </el-button>
        </template>
      </el-table-column>
    </el-table>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import api from '@/api/index'

const records = ref([])

onMounted(async () => {
  // Fetch all practice records for this student
  try {
    const { data } = await api.get('/practice/my-history')
    records.value = data
  } catch {
    records.value = []
  }
})
</script>
