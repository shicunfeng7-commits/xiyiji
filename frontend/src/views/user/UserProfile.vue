<template>
  <div class="user-profile">
    <div class="profile-header">
      <div class="avatar-section">
        <div class="avatar-wrapper" @click="openAvatarPicker">
          <div class="avatar">
            <img v-if="avatarUrl" :src="avatarUrl" class="avatar-img" />
            <svg v-else width="40" height="40" viewBox="0 0 24 24" fill="none" stroke="rgba(255,255,255,0.6)" stroke-width="1.5">
              <path d="M23 19a2 2 0 0 1-2 2H3a2 2 0 0 1-2-2V8a2 2 0 0 1 2-2h4l2-3h6l2 3h4a2 2 0 0 1 2 2z"/>
              <circle cx="12" cy="13" r="4"/>
            </svg>
          </div>
        </div>
        <span class="header-nickname">{{ nickname || '未设置昵称' }}</span>
      </div>
      <span class="edit-link" @click="openEditProfile">编辑资料</span>
    </div>

    <div class="info-card">
      <div class="info-row">
        <span class="info-label">手机号</span>
        <span class="info-value">{{ userInfo?.phone || '-' }}</span>
      </div>
      <div class="info-row">
        <span class="info-label">楼栋</span>
        <span class="info-value">{{ buildingInfo || '未设置' }}</span>
      </div>
      <div class="info-row">
        <span class="info-label">注册时间</span>
        <span class="info-value">{{ userInfo?.createTime || '-' }}</span>
      </div>
    </div>

    <div class="action-section" v-if="applyStatus !== 'approved'">
      <div v-if="applyStatus === 'none' || applyStatus === 'rejected'" class="apply-card">
        <div v-if="applyStatus === 'rejected'" class="reject-notice">
          <van-icon name="info-o" size="16" color="#FF3B30" /><span>申请被拒绝：{{ rejectReason || '未知原因' }}</span>
        </div>
        <button class="apply-btn" @click="showApplyDialog = true">申请成为员工</button>
      </div>
      <div v-else-if="applyStatus === 'pending'" class="status-card">
        <van-icon name="clock-o" size="28" color="#FF9500" /><div class="status-text">申请审核中</div>
      </div>
    </div>
    <div v-else class="status-card approved">
      <van-icon name="checked" size="28" color="#34C759" /><div class="status-text">已通过员工认证</div>
    </div>

    <button class="logout-btn" @click="handleLogout">退出登录</button>

    <!-- 编辑资料弹窗 -->
    <van-dialog v-model:show="showEdit" title="编辑资料" show-cancel-button @confirm="saveProfile">
      <van-field v-model="editNickname" label="昵称" placeholder="请输入昵称" maxlength="20" />
      <van-field v-model="editPhone" label="手机号" placeholder="请输入手机号" maxlength="11" />
      <van-field v-model="editBuilding" label="宿舍楼栋" placeholder="如：食宿楼 · 1栋" />
      <van-field v-model="editRoom" label="房间号" placeholder="如：301" />
    </van-dialog>

    <!-- 员工申请弹窗 -->
    <van-dialog v-model:show="showApplyDialog" title="申请成为员工" show-cancel-button :before-close="handleApplySubmit">
      <van-field v-model="applyName" label="姓名" placeholder="真实姓名" maxlength="20" required />
      <van-field v-model="applyPhone" label="手机号" placeholder="联系电话" maxlength="11" required />
      <van-field v-model="applyMajor" label="专业" placeholder="学院专业" maxlength="50" />
      <van-field v-model="applyGrade" label="年级" placeholder="如：2024级" maxlength="20" />
    </van-dialog>

    <input ref="galleryInput" type="file" accept="image/*" style="display:none" @change="onAvatarFile" />
    <input ref="cameraInput" type="file" accept="image/*" capture="environment" style="display:none" @change="onAvatarFile" />
  </div>
</template>

<script setup lang="ts">
import { ref, computed, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { showToast } from 'vant'
import { getUserInfo, setUserInfo, removeAuth } from '../../utils/auth'
import { get, post, put } from '../../utils/request'

const router = useRouter()
const userInfo = ref<Record<string, unknown> | null>(null)
const applyStatus = ref<'none'|'pending'|'approved'|'rejected'>('none')
const rejectReason = ref('')
const showApplyDialog = ref(false)
const applyName = ref(''); const applyPhone = ref(''); const applyMajor = ref(''); const applyGrade = ref('')
const showEdit = ref(false)
const editNickname = ref(''); const editPhone = ref(''); const editBuilding = ref(''); const editRoom = ref('')
const avatarUrl = ref('')
const nickname = ref('')
const buildingInfo = ref('')
const galleryInput = ref<HTMLInputElement|null>(null)
const cameraInput = ref<HTMLInputElement|null>(null)

async function loadUserInfo() {
  const info = getUserInfo() as Record<string, unknown> | null
  userInfo.value = info
  nickname.value = (info?.nickname as string) || ''
  avatarUrl.value = (info?.avatar as string) || ''
  const b = (info?.buildingName as string) || ''
  const r = (info?.roomNo as string) || ''
  buildingInfo.value = b && r ? `${b} · ${r}` : (b || '')
}

function openEditProfile() {
  const info = getUserInfo() as Record<string, unknown> | null
  editNickname.value = (info?.nickname as string) || ''
  editPhone.value = (info?.phone as string) || ''
  editBuilding.value = (info?.buildingName as string) || ''
  editRoom.value = (info?.roomNo as string) || ''
  showEdit.value = true
}

async function saveProfile() {
  try {
    await put('/api/user/profile', {
      nickname: editNickname.value, phone: editPhone.value,
      buildingName: editBuilding.value, roomNo: editRoom.value,
    })
    const info = getUserInfo() as Record<string, unknown> | null
    if (info) {
      info.nickname = editNickname.value; info.phone = editPhone.value
      info.buildingName = editBuilding.value; info.roomNo = editRoom.value
      setUserInfo(info)
    }
    loadUserInfo()
    showToast({ message: '资料已保存', duration: 1500, className: 'save-toast' })
  } catch { showToast('保存失败') }
}

function openAvatarPicker() { galleryInput.value?.click() }

async function onAvatarFile(e: Event) {
  const file = (e.target as HTMLInputElement).files?.[0]
  if (!file) return
  const reader = new FileReader()
  reader.onload = async () => {
    const base64 = (reader.result as string).split(',')[1]
    try {
      const res = await post<{code:number;data:string}>('/api/employee/upload-photo', {image:base64})
      if (res.data.code === 200) {
        avatarUrl.value = res.data.data
        await put('/api/user/profile', {avatar:res.data.data})
        const info = getUserInfo() as Record<string, unknown> | null
        if (info) { info.avatar = res.data.data; setUserInfo(info) }
        showToast('头像已更新')
      }
    } catch { showToast('上传失败') }
  }
  reader.readAsDataURL(file)
}

async function fetchApplyStatus() {
  try {
    const res = await get<{code:number;data:{status:number;rejectReason?:string}|null}>('/api/user/apply-status')
    const app = res.data.data
    if (!app) applyStatus.value = 'none'
    else if (app.status === 0) applyStatus.value = 'pending'
    else if (app.status === 1) applyStatus.value = 'approved'
    else { applyStatus.value = 'rejected'; rejectReason.value = app.rejectReason || '' }
  } catch { applyStatus.value = 'none' }
}

async function handleApplySubmit(action: string): Promise<boolean> {
  if (action !== 'confirm') return true
  if (!applyName.value.trim()) { showToast('请输入姓名'); return false }
  if (!applyPhone.value.trim()) { showToast('请输入手机号'); return false }
  try {
    await post('/api/user/apply-employee', {name:applyName.value.trim(),phone:applyPhone.value.trim(),major:applyMajor.value.trim(),grade:applyGrade.value.trim()})
    showToast('申请已提交'); showApplyDialog.value = false
    applyName.value=''; applyPhone.value=''; applyMajor.value=''; applyGrade.value=''
    await fetchApplyStatus(); return true
  } catch { showToast('提交失败'); return false }
}

function handleLogout() { removeAuth(); router.push('/login') }

onMounted(() => { loadUserInfo(); fetchApplyStatus() })
</script>

<style scoped>
.user-profile { min-height: 100vh; background: var(--color-bg-deep); padding-bottom: 40px; overflow-x: hidden; }

.profile-header {
  background: linear-gradient(135deg, #007AFF, #5856D6);
  padding: 44px 20px 36px; display: flex; align-items: center; justify-content: center; position: relative;
}
.avatar-section { display: flex; flex-direction: column; align-items: center; gap: 8px; }
.avatar-wrapper { cursor: pointer; }
.avatar {
  width: 80px; height: 80px; border-radius: 50%; display: flex; align-items: center; justify-content: center;
  background: rgba(255,255,255,0.15); border: 3px solid rgba(255,255,255,0.3); overflow: hidden;
}
.avatar-img { width: 100%; height: 100%; object-fit: cover; }
.header-nickname { font-size: 15px; font-weight: 600; color: white; margin-top: 2px; }
.edit-link {
  position: absolute; right: 20px; top: 50%; transform: translateY(-50%);
  font-size: 12px; color: rgba(255,255,255,0.8); cursor: pointer;
  padding: 7px 16px; background: rgba(255,255,255,0.15); border-radius: 14px;
  white-space: nowrap;
}
.edit-link:active { background: rgba(255,255,255,0.3); }

.info-card { background: var(--glass-bg-card); backdrop-filter: blur(12px); -webkit-backdrop-filter: blur(12px); border: 1px solid rgba(0,0,0,0.06); border-radius: 18px; margin: -16px 14px 14px; padding: 8px 0; box-shadow: var(--shadow-sm); }
.info-row { display: flex; align-items: center; padding: 12px 18px; }
.info-label { width: 60px; font-size: 14px; color: var(--color-text-secondary); flex-shrink: 0; }
.info-value { flex: 1; font-size: 14px; color: var(--color-text-primary); text-align: right; font-weight: 500; }

.action-section { margin: 0 14px 14px; }
.apply-card { background: var(--glass-bg-card); backdrop-filter: blur(12px); -webkit-backdrop-filter: blur(12px); border: 1px solid rgba(0,0,0,0.06); border-radius: 18px; padding: 18px; }
.apply-btn { width: 100%; padding: 14px; background: var(--color-accent); color: white; border: none; border-radius: 14px; font-size: 16px; font-weight: 600; cursor: pointer; }
.apply-btn:active { transform: scale(0.98); }
.reject-notice { display: flex; align-items: center; gap: 8px; font-size: 13px; color: #FF3B30; background: rgba(255,59,48,0.06); padding: 10px 14px; border-radius: 10px; margin-bottom: 14px; }
.status-card { background: var(--glass-bg-card); backdrop-filter: blur(12px); -webkit-backdrop-filter: blur(12px); border: 1px solid rgba(0,0,0,0.06); border-radius: 18px; padding: 24px; text-align: center; margin: 0 14px 14px; }
.status-text { font-size: 15px; color: var(--color-text-primary); font-weight: 500; margin-top: 8px; }
.logout-btn { display: block; width: calc(100% - 28px); margin: 8px 14px; padding: 16px; background: white; color: #FF3B30; border: none; border-radius: 16px; font-size: 16px; font-weight: 600; cursor: pointer; box-shadow: var(--shadow-sm); }
.logout-btn:active { background: #FFF5F5; }
</style>
