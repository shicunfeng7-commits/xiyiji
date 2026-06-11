<template>
  <div class="employee-profile">
    <!-- 顶部头 -->
    <div class="profile-header">
      <div class="avatar-wrapper" @click="openAvatarPicker">
        <div class="avatar">
          <img v-if="avatarUrl" :src="avatarUrl" class="avatar-img" />
          <van-icon v-else name="contact" size="40" color="white" />
        </div>
        <div class="avatar-badge"><van-icon name="photograph" size="12" color="white" /></div>
      </div>
      <div class="header-info">
        <div class="name" @click="editNickname">{{ displayName }}</div>
        <div class="phone">{{ maskedPhone }}</div>
      </div>
      <div class="status-dot" :class="statusClass"></div>
    </div>

    <!-- 今日统计卡片 -->
    <div class="stats-card">
      <div class="stats-header">
        <span class="stats-title">今日汇总</span>
        <div class="range-tabs">
          <span v-for="r in ranges" :key="r.key" class="range-tab" :class="{ active: activeRange === r.key }" @click="switchRange(r.key)">{{ r.label }}</span>
        </div>
      </div>
      <div class="stats-grid">
        <div class="stat-item">
          <div class="stat-value">{{ stats.completedCount }}</div>
          <div class="stat-label">完成订单</div>
        </div>
        <div class="stat-item">
          <div class="stat-value">¥{{ stats.totalRevenue }}</div>
          <div class="stat-label">总收入</div>
        </div>
      </div>
    </div>

    <!-- 最近完成的订单（仅4条） -->
    <div class="recent-section">
      <div class="section-title">
        <span>最近完成</span>
        <span class="view-all" @click="router.push('/employee/order-history')" v-if="recentOrders.length > 0">查看全部 →</span>
      </div>
      <div class="order-mini" v-for="order in recentOrders.slice(0, 4)" :key="order.id">
        <div class="mini-left">
          <span class="mini-no">{{ order.orderNo }}</span>
          <span class="mini-loc">{{ order.building }} · {{ order.room }}</span>
        </div>
        <div class="mini-right">
          <span class="mini-time">{{ order.completeTime?.substring(0, 10) }}</span>
          <span class="mini-amount">¥{{ order.amount }}</span>
        </div>
      </div>
      <div v-if="recentOrders.length === 0" class="empty-mini">暂无完成订单</div>
    </div>

    <!-- 退出登录 -->
    <div class="logout-section">
      <button class="logout-btn" @click="handleLogout">退出登录</button>
    </div>

    <input ref="galleryInput" type="file" accept="image/*" style="display:none" @change="e => onAvatarFile(e, false)" />
  </div>
</template>

<script setup lang="ts">
import { ref, computed, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { showToast, showDialog } from 'vant'
import { get, post, put } from '../../utils/request'
import { getUserInfo, setUserInfo, removeAuth } from '../../utils/auth'

const router = useRouter()
const activeRange = ref('today')
const ranges = [
  { key: 'today', label: '今日' },
  { key: 'week', label: '本周' },
  { key: 'month', label: '本月' },
]

const stats = ref({ completedCount: 0, totalRevenue: '0.00' })
const recentOrders = ref<any[]>([])

const userInfo = getUserInfo() as Record<string, unknown> | null
const employeeName = computed(() => userInfo?.employeeName as string || userInfo?.nickname as string || '员工')
const maskedPhone = computed(() => {
  const phone = (userInfo?.phone as string) || ''
  return phone.length === 11 ? phone.replace(/(\d{3})\d{4}(\d{4})/, '$1****$2') : phone
})
const statusClass = computed(() => 'idle')

async function loadStats() {
  try {
    const res = await get<{ code: number; data: any }>(`/api/employee/stats?range=${activeRange.value}`)
    if (res.data.code === 200) {
      stats.value = res.data.data
    }
  } catch { /* ignore */ }
}

async function loadRecentOrders() {
  try {
    const res = await get<{ code: number; data: any[] }>('/api/employee/orders/my-list')
    if (res.data.code === 200) {
      recentOrders.value = res.data.data
        .filter((o: any) => o.status === 3)
        .sort((a: any, b: any) => (b.completeTime || '').localeCompare(a.completeTime || ''))
        .slice(0, 4)
        .map((item: any) => {
          const buildingInfo = item.buildingName?.split(' · ') || ['', '']
          return {
            id: item.id,
            orderNo: item.orderNo,
            building: buildingInfo[1] || '',
            room: item.roomNo,
            amount: item.amount?.toString() || '29.90',
            completeTime: item.completeTime,
          }
        })
    }
  } catch { /* ignore */ }
}

const showEdit = ref(false)
const editName = ref('')
const editPhone = ref('')
const avatarUrl = ref('')
const avatarInput = ref<HTMLInputElement | null>(null)
const galleryInput = ref<HTMLInputElement | null>(null)
const displayName = ref('')

function loadProfile() {
  const info = getUserInfo() as Record<string, unknown> | null
  displayName.value = (info?.nickname as string) || (info?.employeeName as string) || '员工'
  avatarUrl.value = (info?.avatar as string) || ''
}

function openAvatarPicker() {
  const actions = [{ name: '拍照' }, { name: '从相册选择' }]
  const div = document.createElement('div')
  // Simple approach: directly use file input
  galleryInput.value?.click()
}

function editNickname() { showEdit.value = true; editName.value = displayName.value }

async function onAvatarFile(e: Event, useCamera: boolean) {
  const file = (e.target as HTMLInputElement).files?.[0]
  if (!file) return
  const reader = new FileReader()
  reader.onload = async () => {
    const base64 = (reader.result as string).split(',')[1]
    try {
      const res = await post<{ code: number; data: string }>('/api/employee/upload-photo', { image: base64 })
      if (res.data.code === 200) {
        avatarUrl.value = res.data.data
        await put('/api/user/profile', { avatar: res.data.data })
        const info = getUserInfo() as Record<string, unknown> | null
        if (info) { info.avatar = res.data.data; setUserInfo(info) }
        showToast('头像已更新')
      }
    } catch { showToast('上传失败') }
  }
  reader.readAsDataURL(file)
}

function openEdit() {
  const info = getUserInfo() as Record<string, unknown> | null
  editName.value = (info?.employeeName as string) || (info?.nickname as string) || ''
  editPhone.value = (info?.phone as string) || ''
  showEdit.value = true
}

function saveProfile() {
  put('/api/user/profile', { nickname: editName.value }).then(() => {
    const info = getUserInfo() as Record<string, unknown> | null
    if (info) { info.nickname = editName.value; setUserInfo(info) }
    showToast('保存成功')
  }).catch(() => showToast('保存失败'))
}

function switchRange(key: string) {
  activeRange.value = key
  loadStats()
}

function handleLogout() {
  removeAuth()
  router.push('/login')
}

onMounted(() => {
  loadProfile()
  loadStats()
  loadRecentOrders()
})
</script>

<style scoped>
.employee-profile {
  min-height: 100vh;
  background: #F5F5F7;
  padding-bottom: 80px;
}

.profile-header {
  background: linear-gradient(135deg, #2B95FF, #007AFF);
  padding: 48px 20px 32px;
  display: flex;
  align-items: center;
  gap: 14px;
}

.avatar {
  width: 64px;
  height: 64px;
  background: rgba(255,255,255,0.2);
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  border: 3px solid rgba(255,255,255,0.3);
}

.header-info { flex: 1; }
.name { font-size: 20px; font-weight: 700; color: white; margin-bottom: 2px; }
.phone { font-size: 14px; color: rgba(255,255,255,0.7); }

.status-dot {
  width: 12px; height: 12px; border-radius: 50%;
  background: #34C759; box-shadow: 0 0 0 3px rgba(52,199,89,0.3);
}
.status-dot.busy { background: #FF9500; }

.stats-card {
  background: white;
  border-radius: 16px;
  margin: -16px 14px 14px;
  padding: 20px;
  box-shadow: 0 2px 12px rgba(0,0,0,0.06);
  position: relative;
}

.stats-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 20px;
}

.stats-title { font-size: 16px; font-weight: 700; color: #1D1D1F; }

.range-tabs {
  display: flex;
  background: #F5F5F7;
  border-radius: 8px;
  padding: 2px;
}

.range-tab {
  padding: 5px 14px;
  font-size: 12px;
  font-weight: 600;
  color: #86868B;
  border-radius: 6px;
  cursor: pointer;
  transition: all 0.25s cubic-bezier(0.4, 0, 0.2, 1);
}
.range-tab.active {
  background: white;
  color: #2B95FF;
  box-shadow: 0 1px 3px rgba(0,0,0,0.08);
}

.stats-grid {
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: 12px;
}

.stat-item {
  background: #F5F5F7;
  border-radius: 12px;
  padding: 16px;
  text-align: center;
}

.stat-value {
  font-size: 26px;
  font-weight: 800;
  color: #1D1D1F;
  letter-spacing: -0.02em;
}

.stat-label {
  font-size: 12px;
  color: #86868B;
  margin-top: 2px;
}

.recent-section {
  background: white;
  border-radius: 16px;
  margin: 0 14px 14px;
  padding: 20px;
}

.section-title {
  display: flex;
  justify-content: space-between;
  align-items: center;
  font-size: 16px;
  font-weight: 700;
  color: #1D1D1F;
  margin-bottom: 12px;
}
.view-all { font-size: 13px; font-weight: 500; color: #2B95FF; cursor: pointer; }
.view-all:active { opacity: 0.7; }

.order-mini {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 10px 0;
  border-bottom: 1px solid #F5F5F7;
}
.order-mini:last-child { border-bottom: none; }

.mini-no { font-size: 13px; color: #86868B; margin-right: 8px; }
.mini-loc { font-size: 14px; color: #1D1D1F; }
.mini-time { font-size: 12px; color: #C7C7CC; display: block; }
.mini-amount { font-size: 14px; font-weight: 600; color: #34C759; }

.empty-mini {
  text-align: center;
  color: #C7C7CC;
  font-size: 14px;
  padding: 24px 0;
}

.logout-section { padding: 0 14px; margin-top: 8px; }

.logout-btn {
  width: 100%;
  padding: 16px;
  background: white;
  color: #FF3B30;
  border: none;
  border-radius: 14px;
  font-size: 16px;
  font-weight: 600;
  cursor: pointer;
  box-shadow: 0 2px 12px rgba(0,0,0,0.04);
  transition: all 0.2s;
}
.logout-btn:active { transform: scale(0.98); background: #FFF5F5; }
</style>
