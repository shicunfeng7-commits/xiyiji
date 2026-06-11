<template>
  <div class="time-config">
    <div class="page-header">
      <h1 class="page-title">服务时间</h1>
      <p class="page-desc">设置可预约的服务时间段</p>
    </div>

    <div class="period-card" v-for="period in periods" :key="period.key">
      <div class="period-header">
        <div class="period-info">
          <span class="period-name">{{ period.label }}</span>
          <span class="period-range">{{ period.startTime }} — {{ period.endTime }}</span>
        </div>
        <van-switch v-model="period.enabled" size="24" active-color="#34C759" />
      </div>

      <div class="period-body" v-if="period.enabled">
        <div class="time-row">
          <span class="time-label">开始</span>
          <span class="time-value" @click="openPicker(period, 'start')">{{ period.startTime }}</span>
        </div>
        <div class="time-row">
          <span class="time-label">结束</span>
          <span class="time-value" @click="openPicker(period, 'end')">{{ period.endTime }}</span>
        </div>
      </div>
    </div>

    <div class="save-area">
      <button class="save-btn" @click="saveConfig">保存配置</button>
    </div>

    <!-- 时间选择器弹窗 -->
    <van-popup v-model:show="showPicker" position="bottom" round>
      <van-time-picker
        v-if="showPicker"
        v-model="pickerValue"
        title="选择时间"
        @confirm="onPickerConfirm"
        @cancel="showPicker = false"
      />
    </van-popup>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { showToast } from 'vant'
import { get, put } from '../../utils/request'

interface Period {
  key: string
  label: string
  enabled: boolean
  startTime: string
  endTime: string
}

const periods = ref<Period[]>([
  { key: 'morning', label: '上午', enabled: true, startTime: '08:00', endTime: '12:00' },
  { key: 'afternoon', label: '下午', enabled: true, startTime: '13:00', endTime: '18:00' },
  { key: 'evening', label: '晚上', enabled: true, startTime: '19:00', endTime: '22:00' },
])

const showPicker = ref(false)
const pickerValue = ref(['08', '00'])
let currentPeriod: Period | null = null
let currentField: 'start' | 'end' = 'start'

function openPicker(period: Period, field: 'start' | 'end') {
  currentPeriod = period
  currentField = field
  pickerValue.value = (field === 'start' ? period.startTime : period.endTime).split(':')
  showPicker.value = true
}

function onPickerConfirm() {
  if (currentPeriod) {
    const time = pickerValue.value[0] + ':' + pickerValue.value[1]
    if (currentField === 'start') {
      currentPeriod.startTime = time
    } else {
      currentPeriod.endTime = time
    }
  }
  showPicker.value = false
}

async function fetchConfig() {
  try {
    const res = await get<{ code: number; data: any[] }>('/api/admin/time-config/list')
    if (res.data.code === 200 && res.data.data) {
      res.data.data.forEach((item: any) => {
        const p = periods.value.find(p => p.key === item.key)
        if (p) {
          p.enabled = item.enabled
          p.startTime = item.startTime
          p.endTime = item.endTime
        }
      })
    }
  } catch { /* use defaults */ }
}

async function saveConfig() {
  const data = periods.value.map(p => ({
    key: p.key,
    label: p.label,
    enabled: p.enabled,
    startTime: p.startTime,
    endTime: p.endTime,
  }))
  try {
    await put('/api/admin/time-config/update', data)
    showToast('保存成功')
  } catch {
    showToast('保存失败')
  }
}

onMounted(fetchConfig)
</script>

<style scoped>
.time-config { padding: 16px 14px 100px; min-height: 100vh; background: #F5F5F7; }

.page-header { margin-bottom: 20px; }
.page-title { font-size: 28px; font-weight: 800; color: #1D1D1F; letter-spacing: -0.03em; margin: 0; }
.page-desc { font-size: 13px; color: #86868B; margin: 4px 0 0; }

.period-card {
  background: white; border-radius: 18px; padding: 18px; margin-bottom: 12px;
  box-shadow: 0 1px 3px rgba(0,0,0,0.03);
}
.period-header { display: flex; justify-content: space-between; align-items: center; }
.period-info { display: flex; flex-direction: column; gap: 2px; }
.period-name { font-size: 17px; font-weight: 700; color: #1D1D1F; }
.period-range { font-size: 13px; color: #86868B; }

.period-body { margin-top: 14px; padding-top: 14px; border-top: 1px solid #F5F5F7; }
.time-row { display: flex; justify-content: space-between; align-items: center; padding: 6px 0; }
.time-label { font-size: 14px; color: #86868B; }
.time-value {
  font-size: 16px; font-weight: 600; color: #2B95FF; padding: 8px 18px;
  background: rgba(43,149,255,0.06); border-radius: 10px; cursor: pointer;
  transition: all 0.2s;
}
.time-value:active { background: rgba(43,149,255,0.15); }

.save-area { position: fixed; bottom: 64px; left: 0; right: 0; padding: 16px; background: white; box-shadow: 0 -2px 10px rgba(0,0,0,0.05); }
.save-btn {
  width: 100%; max-width: 430px; display: block; margin: 0 auto;
  padding: 16px; background: #1D1D1F; color: white; border: none;
  border-radius: 16px; font-size: 17px; font-weight: 600; cursor: pointer;
  transition: all 0.2s;
}
.save-btn:active { transform: scale(0.98); background: #333; }
</style>
