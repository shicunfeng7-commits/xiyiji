<template>
  <div class="user-profile">
    <!-- 头像区域 -->
    <div class="profile-header">
      <div class="avatar-wrapper" @click="showAvatarSheet = true">
        <div class="avatar" :style="{ background: avatarBg }">
          <img v-if="avatarUrl" :src="avatarUrl" class="avatar-img" />
          <van-icon v-else :name="avatarIcon" size="44" color="white" />
        </div>
        <div class="avatar-badge">
          <van-icon name="photograph" size="14" color="white" />
        </div>
      </div>
      <div class="header-nickname" @click="editNickname">{{ nickname || '点击设置昵称' }}</div>
    </div>

    <!-- 信息卡片 -->
    <div class="info-card">
      <div class="info-row" @click="editNickname">
        <span class="info-label">昵称</span>
        <span class="info-value">{{ nickname || '未设置' }}</span>
        <van-icon name="arrow" size="14" color="#C7C7CC" />
      </div>
      <div class="info-row">
        <span class="info-label">手机号</span>
        <span class="info-value">{{ userInfo?.phone || '-' }}</span>
      </div>
      <div class="info-row">
        <span class="info-label">注册时间</span>
        <span class="info-value">{{ userInfo?.createTime || '-' }}</span>
      </div>
    </div>

    <!-- 员工申请区域 -->
    <div class="action-section" v-if="applyStatus !== 'approved'">
      <div v-if="applyStatus === 'none' || applyStatus === 'rejected'" class="apply-card">
        <div v-if="applyStatus === 'rejected'" class="reject-notice">
          <van-icon name="info-o" size="16" color="#FF3B30" />
          <span>申请被拒绝：{{ rejectReason || '未知原因' }}</span>
        </div>
        <button class="apply-btn" @click="showApplyDialog = true">申请成为员工</button>
      </div>
      <div v-else-if="applyStatus === 'pending'" class="status-card">
        <van-icon name="clock-o" size="28" color="#FF9500" />
        <div class="status-text">申请审核中</div>
      </div>
    </div>
    <div v-else class="status-card approved">
      <van-icon name="checked" size="28" color="#34C759" />
      <div class="status-text">已通过员工认证</div>
    </div>

    <!-- 退出 -->
    <button class="logout-btn" @click="handleLogout">退出登录</button>

    <!-- 编辑昵称弹窗 -->
    <van-dialog v-model:show="showEditNickname" title="修改昵称" show-cancel-button @confirm="saveNickname">
      <van-field v-model="nickname" placeholder="请输入昵称" maxlength="20" />
    </van-dialog>

    <!-- 员工申请弹窗 -->
    <van-dialog v-model:show="showApplyDialog" title="申请成为员工" show-cancel-button :before-close="handleApplySubmit">
      <van-field v-model="applyName" label="姓名" placeholder="真实姓名" maxlength="20" required />
      <van-field v-model="applyPhone" label="手机号" placeholder="联系电话" maxlength="11" type="tel" required />
      <van-field v-model="applyMajor" label="专业" placeholder="学院专业" maxlength="50" />
      <van-field v-model="applyGrade" label="年级" placeholder="如：2024级" maxlength="20" />
    </van-dialog>

    <!-- 头像选择面板 -->
    <van-action-sheet v-model:show="showAvatarSheet" title="更换头像" :actions="[{ name: '拍照' }, { name: '从相册选择' }]" @select="onAvatarSelect" />
    <input ref="avatarInput" type="file" accept="image/*" capture="environment" style="display:none" @change="onAvatarFile" />
    <input ref="galleryInput" type="file" accept="image/*" style="display:none" @change="onAvatarFile" />
  </div>
</template>

<script setup lang="ts">
import { ref, computed, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { showToast } from 'vant'
import { getUserInfo, setUserInfo, removeAuth } from '../../utils/auth'
import { get, post, put } from '../../utils/request'

interface UserInfo {
  phone?: string
  createTime?: string
  [key: string]: unknown
}

const router = useRouter()
const userInfo = ref<UserInfo | null>(null)
const applyStatus = ref<'none' | 'pending' | 'approved' | 'rejected'>('none')
const rejectReason = ref('')
const showApplyDialog = ref(false)
const applyName = ref('')
const applyPhone = ref('')
const applyMajor = ref('')
const applyGrade = ref('')
const showEditNickname = ref(false)
const nickname = ref('')
const showAvatarSheet = ref(false)
const avatarUrl = ref('')
const avatarInput = ref<HTMLInputElement | null>(null)
const galleryInput = ref<HTMLInputElement | null>(null)

const avatarIcon = computed(() => 'contact')
const avatarBg = computed(() => avatarUrl.value ? 'transparent' : 'linear-gradient(135deg, #2B95FF, #5856D6)')

async function loadUserInfo() {
  const info = getUserInfo() as Record<string, unknown> | null
  userInfo.value = info as UserInfo | null
  nickname.value = (info?.nickname as string) || ''
  avatarUrl.value = (info?.avatar as string) || ''
}

function editNickname() { showEditNickname.value = true }

function saveNickname() {
  put('/api/user/profile', { nickname: nickname.value }).then(() => {
    const info = getUserInfo() as Record<string, unknown> | null
    if (info) { info.nickname = nickname.value; setUserInfo(info) }
    showToast('已保存')
  }).catch(() => showToast('保存失败'))
}

function onAvatarSelect(action: { name: string }) {
  showAvatarSheet.value = false
  if (action.name === '拍照') { avatarInput.value?.click() }
  else { galleryInput.value?.click() }
}

async function onAvatarFile(e: Event) {
  const file = (e.target as HTMLInputElement).files?.[0]
  if (!file) return
  const reader = new FileReader()
  reader.onload = async () => {
    const base64 = (reader.result as string).split(',')[1]
    try {
      const res = await post<{ code: number; data: string }>('/api/employee/upload-photo', { image: base64 })
      if (res.data.code === 200) {
        const url = res.data.data
        avatarUrl.value = url
        await put('/api/user/profile', { avatar: url })
        const info = getUserInfo() as Record<string, unknown> | null
        if (info) { info.avatar = url; setUserInfo(info) }
        showToast('头像已更新')
      }
    } catch { showToast('上传失败') }
  }
  reader.readAsDataURL(file)
}

async function fetchApplyStatus() {
  try {
    const res = await get<{ code: number; data: { status: number; rejectReason?: string } | null }>('/api/user/apply-status')
    const app = res.data.data
    if (!app) { applyStatus.value = 'none' }
    else if (app.status === 0) { applyStatus.value = 'pending' }
    else if (app.status === 1) { applyStatus.value = 'approved' }
    else { applyStatus.value = 'rejected'; rejectReason.value = app.rejectReason || '' }
  } catch { applyStatus.value = 'none' }
}

async function handleApplySubmit(action: string): Promise<boolean> {
  if (action !== 'confirm') return true
  if (!applyName.value.trim()) { showToast('请输入姓名'); return false }
  if (!applyPhone.value.trim()) { showToast('请输入手机号'); return false }
  try {
    await post('/api/user/apply-employee', {
      name: applyName.value.trim(),
      phone: applyPhone.value.trim(),
      major: applyMajor.value.trim(),
      grade: applyGrade.value.trim(),
    })
    showToast('申请已提交')
    showApplyDialog.value = false
    applyName.value = ''; applyPhone.value = ''; applyMajor.value = ''; applyGrade.value = ''
    await fetchApplyStatus()
    return true
  } catch { showToast('提交失败'); return false }
}

function handleLogout() { removeAuth(); router.push('/login') }

onMounted(() => { loadUserInfo(); fetchApplyStatus() })
</script>

<style scoped>
.user-profile { min-height: 100vh; background: #F5F5F7; padding-bottom: 40px; }

.profile-header {
  background: linear-gradient(135deg, #2B95FF 0%, #007AFF 50%, #5856D6 100%);
  padding: 52px 20px 36px; text-align: center;
}
.avatar-wrapper { position: relative; display: inline-block; cursor: pointer; }
.avatar {
  width: 80px; height: 80px; border-radius: 50%; display: flex; align-items: center;
  justify-content: center; margin: 0 auto; border: 3px solid rgba(255,255,255,0.3);
  overflow: hidden;
}
.avatar-img { width: 100%; height: 100%; object-fit: cover; }
.avatar-badge {
  position: absolute; bottom: 2px; right: 2px; width: 26px; height: 26px;
  border-radius: 13px; background: rgba(0,0,0,0.4); display: flex;
  align-items: center; justify-content: center; border: 2px solid rgba(255,255,255,0.6);
}
.header-nickname { font-size: 18px; font-weight: 700; color: white; margin-top: 10px; cursor: pointer; }

.info-card {
  background: white; border-radius: 16px; margin: -16px 14px 14px;
  padding: 8px 0; box-shadow: 0 2px 12px rgba(0,0,0,0.04);
}
.info-row {
  display: flex; align-items: center; padding: 12px 18px;
  cursor: default;
}
.info-row:last-child { cursor: default; }
.info-label { width: 64px; font-size: 14px; color: #86868B; flex-shrink: 0; }
.info-value { flex: 1; font-size: 14px; color: #1D1D1F; text-align: right; font-weight: 500; }

.action-section { margin: 0 14px 14px; }

.apply-card { background: white; border-radius: 16px; padding: 18px; box-shadow: 0 2px 12px rgba(0,0,0,0.04); }
.apply-btn {
  width: 100%; padding: 14px; background: #2B95FF; color: white; border: none;
  border-radius: 14px; font-size: 16px; font-weight: 600; cursor: pointer;
  box-shadow: 0 4px 14px rgba(43,149,255,0.3);
}
.apply-btn:active { transform: scale(0.98); }

.reject-notice {
  display: flex; align-items: center; gap: 8px; font-size: 13px; color: #FF3B30;
  background: rgba(255,59,48,0.06); padding: 10px 14px; border-radius: 10px; margin-bottom: 14px;
}

.status-card {
  background: white; border-radius: 16px; padding: 24px; text-align: center;
  margin: 0 14px 14px; box-shadow: 0 2px 12px rgba(0,0,0,0.04);
}
.status-text { font-size: 15px; color: #1D1D1F; font-weight: 500; margin-top: 8px; }

.logout-btn {
  display: block; width: calc(100% - 28px); margin: 8px 14px; padding: 16px;
  background: white; color: #FF3B30; border: none; border-radius: 16px;
  font-size: 16px; font-weight: 600; cursor: pointer; box-shadow: 0 2px 12px rgba(0,0,0,0.04);
}
.logout-btn:active { background: #FFF5F5; }
</style>
