<template>
  <div class="admin-time-config">
    <div class="page-title">服务时间配置</div>

    <div class="period-card" v-for="period in periods" :key="period.key">
      <div class="period-header">
        <span class="period-name">{{ period.label }}</span>
        <van-switch v-model="period.enabled" size="22" />
      </div>
      <div class="period-body" v-if="period.enabled">
        <div class="time-row">
          <span class="time-label">起始时间</span>
          <van-time-picker
            v-if="period.showStartPicker"
            v-model="period.startTime"
            title="选择起始时间"
            @confirm="period.showStartPicker = false"
            @cancel="period.showStartPicker = false"
          />
          <div class="time-value" @click="period.showStartPicker = true">
            {{ period.startTime }}
          </div>
        </div>
        <div class="time-row">
          <span class="time-label">结束时间</span>
          <van-time-picker
            v-if="period.showEndPicker"
            v-model="period.endTime"
            title="选择结束时间"
            @confirm="period.showEndPicker = false"
            @cancel="period.showEndPicker = false"
          />
          <div class="time-value" @click="period.showEndPicker = true">
            {{ period.endTime }}
          </div>
        </div>
      </div>
    </div>

    <div class="save-area">
      <button class="save-btn" @click="saveConfig">保存</button>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { showToast, showDialog } from 'vant'
import { get, put } from '../../utils/request'

interface Period {
  key: string
  label: string
  enabled: boolean
  startTime: string
  endTime: string
  showStartPicker: boolean
  showEndPicker: boolean
}

const periods = ref<Period[]>([
  { key: 'morning', label: '上午', enabled: true, startTime: '08:00', endTime: '12:00', showStartPicker: false, showEndPicker: false },
  { key: 'afternoon', label: '下午', enabled: true, startTime: '13:00', endTime: '18:00', showStartPicker: false, showEndPicker: false },
  { key: 'evening', label: '晚上', enabled: true, startTime: '19:00', endTime: '22:00', showStartPicker: false, showEndPicker: false },
])

function fetchConfig() {
  get('/api/admin/time-config/list').then((res: any) => {
    const data = res.data || []
    if (data.length > 0) {
      data.forEach((item: any) => {
        const p = periods.value.find(period => period.key === item.key)
        if (p) {
          p.enabled = item.enabled
          p.startTime = item.startTime
          p.endTime = item.endTime
        }
      })
    }
  }).catch(() => {
    showToast('加载配置失败')
  })
}

function saveConfig() {
  const data = periods.value.map(p => ({
    key: p.key,
    label: p.label,
    enabled: p.enabled,
    startTime: p.startTime,
    endTime: p.endTime,
  }))
  put('/api/admin/time-config/update', data).then(() => {
    showToast('保存成功')
  }).catch(() => {
    showToast('保存失败')
  })
}

onMounted(() => {
  fetchConfig()
})
</script>

<style scoped>
.admin-time-config {
  padding: 16px 16px 80px;
}

.page-title {
  font-size: 24px;
  font-weight: 700;
  color: #1D1D1F;
  margin-bottom: 16px;
}

.period-card {
  background: white;
  border-radius: 16px;
  padding: 16px;
  margin-bottom: 12px;
  box-shadow: 0 1px 3px rgba(0,0,0,0.04);
}

.period-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.period-name {
  font-size: 18px;
  font-weight: 600;
  color: #1D1D1F;
}

.period-body {
  margin-top: 12px;
  padding-top: 12px;
  border-top: 1px solid #F5F5F7;
}

.time-row {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 8px 0;
}

.time-label {
  font-size: 14px;
  color: #86868B;
}

.time-value {
  font-size: 16px;
  font-weight: 600;
  color: #2B95FF;
  padding: 6px 16px;
  background: rgba(43,149,255,0.08);
  border-radius: 8px;
  cursor: pointer;
}

.time-value:active {
  opacity: 0.7;
}

.save-area {
  position: fixed;
  bottom: 0;
  left: 0;
  right: 0;
  padding: 16px;
  background: white;
  box-shadow: 0 -2px 10px rgba(0,0,0,0.05);
}

.save-btn {
  width: 100%;
  padding: 16px;
  background: #2B95FF;
  color: white;
  border: none;
  border-radius: 14px;
  font-size: 17px;
  font-weight: 600;
  cursor: pointer;
  transition: all 0.2s;
  box-shadow: 0 4px 14px rgba(43,149,255,0.3);
}

.save-btn:active {
  transform: scale(0.98);
}
</style>