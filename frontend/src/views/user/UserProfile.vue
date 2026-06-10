<template>
  <div class="user-profile">
    <!-- 顶部头像区域 -->
    <div class="profile-header">
      <div class="avatar">
        <van-icon name="contact" size="48" color="white" />
      </div>
      <div class="phone-display">{{ maskedPhone }}</div>
    </div>

    <!-- 用户信息卡片 -->
    <div class="info-card">
      <div class="info-row">
        <span class="info-label">手机号</span>
        <span class="info-value">{{ userInfo?.phone || '-' }}</span>
      </div>
      <div class="divider" />
      <div class="info-row">
        <span class="info-label">注册时间</span>
        <span class="info-value">{{ userInfo?.createTime || '-' }}</span>
      </div>
    </div>

    <!-- 申请员工区域 -->
    <div class="action-section" v-if="applyStatus !== 'approved'">
      <!-- 未申请 / 被拒绝 → 显示申请按钮 -->
      <div v-if="applyStatus === 'none' || applyStatus === 'rejected'" class="apply-card">
        <div v-if="applyStatus === 'rejected'" class="reject-notice">
          <van-icon name="info-o" size="16" color="#FF3B30" />
          <span>申请被拒绝：{{ rejectReason || '未知原因' }}</span>
        </div>
        <button class="apply-btn" @click="showApplyDialog = true">
          <van-icon name="add-o" size="18" color="white" />
          <span>申请成为员工</span>
        </button>
      </div>

      <!-- 待审核 -->
      <div v-else-if="applyStatus === 'pending'" class="status-card">
        <div class="status-icon pending">
          <van-icon name="clock-o" size="28" color="#FF9500" />
        </div>
        <div class="status-text">申请已提交，等待管理员审核</div>
      </div>
    </div>

    <!-- 已通过 -->
    <div v-else class="status-card approved">
      <div class="status-icon">
        <van-icon name="checked" size="28" color="#34C759" />
      </div>
      <div class="status-text">你已通过员工认证</div>
    </div>

    <!-- 退出登录 -->
    <div class="logout-section">
      <button class="logout-btn" @click="handleLogout">退出登录</button>
    </div>

    <!-- 申请对话框 -->
    <van-dialog
      v-model:show="showApplyDialog"
      title="申请成为员工"
      show-cancel-button
      :before-close="handleDialogClose"
    >
      <div class="dialog-content">
        <van-field
          v-model="applyName"
          label="姓名"
          placeholder="请输入你的真实姓名"
          maxlength="20"
          :rules="[{ required: true, message: '请输入姓名' }]"
        />
      </div>
    </van-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, computed, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { showToast, showLoadingToast, closeToast } from 'vant'
import { getUserInfo, removeAuth } from '../../utils/auth'
import { get, post } from '../../utils/request'

interface UserInfo {
  phone?: string
  createTime?: string
  [key: string]: unknown
}

interface ApplyStatusResponse {
  status: 'none' | 'pending' | 'approved' | 'rejected'
  rejectReason?: string
}

const router = useRouter()
const userInfo = ref<UserInfo | null>(null)
const applyStatus = ref<'none' | 'pending' | 'approved' | 'rejected'>('none')
const rejectReason = ref('')
const showApplyDialog = ref(false)
const applyName = ref('')

const maskedPhone = computed(() => {
  const phone = userInfo.value?.phone || ''
  if (phone.length === 11) {
    return phone.replace(/(\d{3})\d{4}(\d{4})/, '$1****$2')
  }
  return phone || '未设置'
})

async function fetchApplyStatus() {
  try {
    const res = await get<ApplyStatusResponse>('/api/user/apply-status')
    applyStatus.value = res.data.status
    if (res.data.rejectReason) {
      rejectReason.value = res.data.rejectReason
    }
  } catch {
    applyStatus.value = 'none'
  }
}

async function handleApply() {
  const name = applyName.value.trim()
  if (!name) {
    showToast('请输入姓名')
    return false
  }
  const toast = showLoadingToast({ message: '提交中...', forbidClick: true })
  try {
    await post('/api/user/apply-employee', { name })
    closeToast()
    showToast('申请已提交')
    showApplyDialog.value = false
    applyName.value = ''
    await fetchApplyStatus()
    return true
  } catch {
    closeToast()
    showToast('提交失败，请重试')
    return false
  }
}

async function handleDialogClose(action: string): Promise<boolean> {
  if (action === 'confirm') {
    return await handleApply()
  }
  return true
}

function handleLogout() {
  removeAuth()
  router.push('/login')
}

onMounted(() => {
  userInfo.value = getUserInfo() as UserInfo | null
  fetchApplyStatus()
})
</script>

<style scoped>
.user-profile {
  min-height: 100vh;
  background: #F5F5F7;
  padding-bottom: 40px;
}

.profile-header {
  background: linear-gradient(135deg, #2B95FF 0%, #007AFF 50%, #5856D6 100%);
  padding: 48px 24px 36px;
  text-align: center;
}

.avatar {
  width: 80px;
  height: 80px;
  background: rgba(255, 255, 255, 0.2);
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  margin: 0 auto 16px;
  backdrop-filter: blur(10px);
  border: 3px solid rgba(255, 255, 255, 0.3);
}

.phone-display {
  font-size: 20px;
  font-weight: 700;
  color: white;
  letter-spacing: 2px;
}

.info-card {
  background: white;
  border-radius: 16px;
  margin: -20px 16px 16px;
  padding: 20px 20px 8px;
  box-shadow: 0 2px 12px rgba(0, 0, 0, 0.04);
}

.info-row {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 10px 0;
}

.info-label {
  font-size: 15px;
  color: #86868B;
}

.info-value {
  font-size: 15px;
  color: #1D1D1F;
  font-weight: 500;
}

.divider {
  height: 1px;
  background: #F0F0F0;
  margin: 0;
}

.action-section {
  margin: 0 16px 16px;
}

.apply-card {
  background: white;
  border-radius: 16px;
  padding: 20px;
  box-shadow: 0 2px 12px rgba(0, 0, 0, 0.04);
}

.reject-notice {
  display: flex;
  align-items: center;
  gap: 8px;
  font-size: 13px;
  color: #FF3B30;
  background: rgba(255, 59, 48, 0.06);
  padding: 10px 14px;
  border-radius: 10px;
  margin-bottom: 16px;
}

.apply-btn {
  width: 100%;
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 8px;
  background: #2B95FF;
  color: white;
  border: none;
  padding: 14px;
  border-radius: 14px;
  font-size: 16px;
  font-weight: 600;
  cursor: pointer;
  transition: all 0.2s;
  box-shadow: 0 4px 14px rgba(43, 149, 255, 0.3);
}

.apply-btn:active {
  transform: scale(0.98);
  box-shadow: 0 2px 8px rgba(43, 149, 255, 0.2);
}

.status-card {
  background: white;
  border-radius: 16px;
  padding: 28px 20px;
  text-align: center;
  box-shadow: 0 2px 12px rgba(0, 0, 0, 0.04);
}

.status-card.approved {
  margin: 0 16px 16px;
}

.status-icon {
  margin-bottom: 12px;
}

.status-icon.pending {
  animation: pulse 2s infinite;
}

@keyframes pulse {
  0%, 100% { opacity: 1; }
  50% { opacity: 0.6; }
}

.status-text {
  font-size: 15px;
  color: #1D1D1F;
  font-weight: 500;
}

.logout-section {
  padding: 0 16px;
  margin-top: 16px;
}

.logout-btn {
  width: 100%;
  padding: 14px;
  background: white;
  color: #FF3B30;
  border: none;
  border-radius: 14px;
  font-size: 16px;
  font-weight: 600;
  cursor: pointer;
  transition: all 0.2s;
  box-shadow: 0 2px 12px rgba(0, 0, 0, 0.04);
}

.logout-btn:active {
  transform: scale(0.98);
  background: #FFF5F5;
}

.dialog-content {
  padding: 16px 20px 8px;
}
</style>