<template>
  <div class="practice-page">
    <el-button @click="$router.back()" style="margin-bottom:16px">返回</el-button>

    <div v-if="segmentData" class="practice-layout">
      <!-- 唱段信息 -->
      <el-card class="info-card">
        <h2>{{ segmentData.segment.title }}</h2>
        <div class="meta">
          <el-tag>{{ segmentData.segment.banShi || '未标注' }}</el-tag>
          <el-tag type="success">{{ segmentData.segment.diaoMen || '未标注' }}</el-tag>
          <el-tag type="warning" v-if="segmentData.segment.bpm">{{ segmentData.segment.bpm }} BPM</el-tag>
          <el-rate :model-value="segmentData.segment.difficulty || 0" disabled size="small" />
        </div>
        <p v-if="segmentData.segment.difficultyTips" class="tips">
          <strong>难点提示：</strong>{{ segmentData.segment.difficultyTips }}
        </p>
        <p v-if="segmentData.segment.lyrics" class="lyrics">
          {{ segmentData.segment.lyrics }}
        </p>
      </el-card>

      <!-- 播放控制 -->
      <el-card class="player-card">
        <h3>教师范唱 <el-tag size="small" type="info">变速不变调</el-tag></h3>
        <div class="player-controls">
          <el-button :icon="playerStore.isPlaying ? 'Pause' : 'VideoPlay'"
                     circle size="large" type="primary"
                     @click="togglePlay" />
          <span class="time">{{ formatTime(playerStore.currentTime) }} / {{ formatTime(playerStore.duration) }}</span>
          <el-slider
            :model-value="playerStore.currentTime"
            :max="playerStore.duration || 100"
            :step="0.1"
            @change="player.seek"
            style="flex:1;margin:0 16px" />
        </div>

        <div class="speed-control">
          <span>播放速度（原音高）：</span>
          <el-slider v-model="speedValue" :min="25" :max="200" :step="5"
                     :format-tooltip="v => `${v/100}x`"
                     @change="v => player.setSpeed(v/100)"
                     style="width:200px" />
          <span class="speed-label">{{ (speedValue / 100).toFixed(2) }}x</span>
        </div>

        <div class="loop-control">
          <el-checkbox v-model="playerStore.loopEnabled">循环播放</el-checkbox>
          <template v-if="playerStore.loopEnabled">
            <el-input-number v-model="loopStartVal" :min="0" :max="playerStore.duration"
                             :precision="1" :step="0.5" size="small" />
            <span>~</span>
            <el-input-number v-model="loopEndVal" :min="0" :max="playerStore.duration"
                             :precision="1" :step="0.5" size="small" />
            <el-button size="small" @click="applyLoop">设置</el-button>
          </template>
        </div>
      </el-card>

      <!-- 节拍器 -->
      <el-card class="metronome-card">
        <h3>节拍器
          <el-tag v-if="estimatedBpm" size="small" type="success" style="margin-left:8px">
            自动估算: {{ estimatedBpm.bpm }} BPM
          </el-tag>
        </h3>
        <div class="metronome-controls">
          <el-input-number v-model="metronomeStore.bpm" :min="20" :max="300" :step="1" />
          <el-select v-model="metronomeStore.timeSignature" style="width:80px">
            <el-option label="2/4" value="2/4" />
            <el-option label="3/4" value="3/4" />
            <el-option label="4/4" value="4/4" />
            <el-option label="1/4" value="1/4" />
          </el-select>
          <el-checkbox v-model="alignWithWaveform">对齐波形</el-checkbox>
          <el-button :type="metronomeStore.isRunning ? 'danger' : 'success'"
                     @click="toggleMetronome">
            {{ metronomeStore.isRunning ? '停止' : '启动' }}
          </el-button>
          <div class="beat-indicator">
            <span v-for="i in beatsCount" :key="i"
                  :class="['dot', { active: metronomeStore.currentBeat === i - 1 && metronomeStore.isRunning }]" />
          </div>
        </div>
      </el-card>

      <!-- 录音 -->
      <el-card class="recorder-card">
        <h3>跟唱录音</h3>
        <div class="recorder-controls">
          <el-button :type="recorder.isRecording.value ? 'danger' : 'primary'"
                     size="large" circle
                     @click="toggleRecording">
            {{ recorder.isRecording.value ? '⏹' : '⏺' }}
          </el-button>
          <span v-if="recorder.isRecording.value" class="recording-time">
            录音中: {{ recorder.duration.value.toFixed(1) }}s
          </span>
          <template v-if="recorder.audioBlob.value && !recorder.isRecording.value">
            <audio :src="recordingUrl" controls style="height:32px" />
            <el-button type="success" @click="submitPractice" :loading="submitting">
              提交分析
            </el-button>
            <el-button @click="recorder.reset()">重录</el-button>
          </template>
        </div>
      </el-card>

      <!-- 分析结果 -->
      <el-card v-if="analysisResult" class="analysis-card">
        <h3>分析结果</h3>
        <div class="score-display">
          <div class="score-circle" :class="scoreClass">
            {{ analysisResult.overallScore }}
          </div>
          <p>{{ analysisResult.summary }}</p>
        </div>
        <div class="pitch-chart">
          <canvas ref="chartRef"></canvas>
        </div>
        <div v-if="errorSegments.length" class="error-list">
          <h4>偏差段落 <el-tag size="small" type="info">连续3秒低于阈值</el-tag></h4>
          <el-table :data="errorSegments" size="small">
            <el-table-column prop="start_time" label="开始(s)" width="80" />
            <el-table-column prop="end_time" label="结束(s)" width="80" />
            <el-table-column prop="avg_deviation_cents" label="平均偏差(音分)" width="130" />
            <el-table-column prop="window_accuracy" label="窗口准确率" width="110">
              <template #default="{ row }">
                {{ row.window_accuracy }}%
              </template>
            </el-table-column>
            <el-table-column prop="severity" label="程度" width="80">
              <template #default="{ row }">
                <el-tag :type="row.severity === 'SEVERE' ? 'danger' : 'warning'" size="small">
                  {{ row.severity }}
                </el-tag>
              </template>
            </el-table-column>
          </el-table>
        </div>
      </el-card>

      <!-- 历史记录 -->
      <el-card class="history-card">
        <h3>练习历史</h3>
        <el-table :data="practiceHistory" size="small">
          <el-table-column prop="createdAt" label="时间" width="180" />
          <el-table-column prop="status" label="状态" width="100">
            <template #default="{ row }">
              <el-tag :type="statusType(row.status)" size="small">{{ statusLabel(row.status) }}</el-tag>
            </template>
          </el-table-column>
          <el-table-column label="操作" width="120">
            <template #default="{ row }">
              <el-button v-if="row.status === 'COMPLETED'" size="small"
                         @click="viewAnalysis(row.id)">查看结果</el-button>
              <el-button v-if="row.status === 'UPLOADED'" size="small"
                         @click="triggerAnalysis(row.id)">开始分析</el-button>
            </template>
          </el-table-column>
        </el-table>
      </el-card>
    </div>
  </div>
</template>

<script setup>
import { ref, computed, onMounted, watch, nextTick } from 'vue'
import { useRoute } from 'vue-router'
import { usePlayerStore } from '@/stores/player'
import { useMetronomeStore } from '@/stores/metronome'
import { useAudioPlayer } from '@/composables/useAudioPlayer'
import { useAudioRecorder } from '@/composables/useAudioRecorder'
import { useMetronome } from '@/composables/useMetronome'
import { useOssUpload } from '@/composables/useOssUpload'
import { segmentApi } from '@/api/segment'
import { practiceApi } from '@/api/practice'
import { ElMessage } from 'element-plus'
import { Chart, registerables } from 'chart.js'

Chart.register(...registerables)

const route = useRoute()
const segmentId = route.params.segmentId

const playerStore = usePlayerStore()
const metronomeStore = useMetronomeStore()
const player = useAudioPlayer()
const recorder = useAudioRecorder()
const metronome = useMetronome()
const ossUploader = useOssUpload()

const segmentData = ref(null)
const analysisResult = ref(null)
const errorSegments = ref([])
const practiceHistory = ref([])
const submitting = ref(false)
const speedValue = ref(100)
const loopStartVal = ref(0)
const loopEndVal = ref(10)
const chartRef = ref(null)
const estimatedBpm = ref(null)
const alignWithWaveform = ref(true)
let chartInstance = null

const beatsCount = computed(() => {
  return parseInt(metronomeStore.timeSignature.split('/')[0]) || 4
})

const recordingUrl = computed(() => {
  return recorder.audioBlob.value ? URL.createObjectURL(recorder.audioBlob.value) : null
})

const scoreClass = computed(() => {
  const s = analysisResult.value?.overallScore
  if (s >= 90) return 'excellent'
  if (s >= 75) return 'good'
  if (s >= 60) return 'pass'
  return 'fail'
})

function formatTime(seconds) {
  if (!seconds) return '0:00'
  const min = Math.floor(seconds / 60)
  const sec = Math.floor(seconds % 60)
  return `${min}:${sec.toString().padStart(2, '0')}`
}

function togglePlay() {
  playerStore.isPlaying ? player.pause() : player.play()
}

function applyLoop() {
  playerStore.setLoop(loopStartVal.value, loopEndVal.value)
}

function toggleMetronome() {
  if (metronomeStore.isRunning) {
    metronome.stop()
  } else {
    // 如果开启了波形对齐，传入波形峰值点
    const peaks = alignWithWaveform.value ? player.waveformPeaks.value : null
    metronome.start(peaks)
  }
}

function toggleRecording() {
  recorder.isRecording.value ? recorder.stopRecording() : recorder.startRecording()
}

async function submitPractice() {
  if (!recorder.audioBlob.value) return
  submitting.value = true
  try {
    const ossKey = await ossUploader.upload(recorder.audioBlob.value, `student-audio/${segmentId}`)
    const { data: record } = await practiceApi.submit(segmentId, {
      audioOssKey: ossKey,
      audioDuration: recorder.duration.value,
      playbackSpeed: playerStore.playbackSpeed
    })
    await practiceApi.triggerAnalysis(record.id)
    ElMessage.success('已提交，分析进行中...')
    recorder.reset()
    fetchHistory()
  } catch (e) {
    ElMessage.error('提交失败')
  } finally {
    submitting.value = false
  }
}

async function viewAnalysis(practiceId) {
  const { data } = await practiceApi.getAnalysis(practiceId)
  analysisResult.value = data
  errorSegments.value = data.errorSegments ? JSON.parse(data.errorSegments) : []
  await nextTick()
  renderChart(data)
}

async function triggerAnalysis(practiceId) {
  await practiceApi.triggerAnalysis(practiceId)
  ElMessage.success('分析已开始')
  setTimeout(fetchHistory, 3000)
}

function renderChart(data) {
  if (!chartRef.value) return
  if (chartInstance) chartInstance.destroy()

  const teacherPitch = data.pitchCurveTeacher ? JSON.parse(data.pitchCurveTeacher) : null
  const studentPitch = data.pitchCurveStudent ? JSON.parse(data.pitchCurveStudent) : null
  if (!teacherPitch || !studentPitch) return

  const labels = teacherPitch.times || teacherPitch.map((_, i) => (i * 0.01).toFixed(2))

  chartInstance = new Chart(chartRef.value, {
    type: 'line',
    data: {
      labels: Array.isArray(labels) ? labels.slice(0, 500) : [],
      datasets: [
        {
          label: '教师音高',
          data: (teacherPitch.frequencies || teacherPitch).slice(0, 500),
          borderColor: '#c41d1d',
          borderWidth: 1.5,
          pointRadius: 0,
          fill: false
        },
        {
          label: '学生音高',
          data: (studentPitch.frequencies || studentPitch).slice(0, 500),
          borderColor: '#409eff',
          borderWidth: 1.5,
          pointRadius: 0,
          fill: false
        }
      ]
    },
    options: {
      responsive: true,
      scales: {
        y: { title: { display: true, text: '频率 (Hz)' } },
        x: { title: { display: true, text: '时间 (s)' }, ticks: { maxTicksLimit: 20 } }
      },
      plugins: { legend: { position: 'top' } }
    }
  })
}

async function fetchHistory() {
  const { data } = await practiceApi.getHistory(segmentId)
  practiceHistory.value = data
}

function statusType(status) {
  const map = { COMPLETED: 'success', ANALYZING: 'warning', FAILED: 'danger', UPLOADED: 'info' }
  return map[status] || 'info'
}

function statusLabel(status) {
  const map = { COMPLETED: '已完成', ANALYZING: '分析中', FAILED: '失败', UPLOADED: '待分析' }
  return map[status] || status
}

onMounted(async () => {
  const { data } = await segmentApi.getById(segmentId)
  segmentData.value = data
  player.init(data.audioUrl)
  fetchHistory()

  // 设置节拍器默认 BPM：优先用教师手动标注，其次用自动估算
  if (data.segment.bpm && data.segment.bpm > 0) {
    metronome.setBpm(data.segment.bpm)
  } else if (data.estimatedBpm) {
    estimatedBpm.value = data.estimatedBpm
    metronome.setEstimatedBpm(data.estimatedBpm)
  }
})
</script>

<style scoped>
.practice-layout { display: flex; flex-direction: column; gap: 16px; }
.info-card .meta { display: flex; gap: 8px; align-items: center; margin: 12px 0; }
.tips { color: #e6a23c; font-size: 14px; }
.lyrics { white-space: pre-wrap; background: #fafafa; padding: 12px; border-radius: 4px; margin-top: 8px; }

.player-controls { display: flex; align-items: center; gap: 12px; }
.time { font-size: 14px; color: #666; min-width: 100px; }
.speed-control { display: flex; align-items: center; gap: 12px; margin-top: 12px; }
.speed-label { font-weight: bold; min-width: 50px; }
.loop-control { display: flex; align-items: center; gap: 8px; margin-top: 12px; }

.metronome-controls { display: flex; align-items: center; gap: 12px; flex-wrap: wrap; }
.beat-indicator { display: flex; gap: 6px; margin-left: 12px; }
.dot { width: 12px; height: 12px; border-radius: 50%; background: #ddd; transition: all 0.1s; }
.dot.active { background: #c41d1d; transform: scale(1.3); }

.recorder-controls { display: flex; align-items: center; gap: 12px; }
.recording-time { color: #f56c6c; font-weight: bold; }

.score-display { display: flex; align-items: center; gap: 20px; margin-bottom: 16px; }
.score-circle {
  width: 80px; height: 80px; border-radius: 50%;
  display: flex; align-items: center; justify-content: center;
  font-size: 24px; font-weight: bold; color: white;
}
.score-circle.excellent { background: #67c23a; }
.score-circle.good { background: #409eff; }
.score-circle.pass { background: #e6a23c; }
.score-circle.fail { background: #f56c6c; }

.pitch-chart { margin: 16px 0; }
.error-list { margin-top: 16px; }
</style>
