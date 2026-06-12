<template>
  <div class="user-profile-wrapper">
    <!-- 登录中全屏动画 -->
    <transition name="login-overlay">
      <div v-if="loginAnimating" class="login-overlay" :class="{ expanding: loginExpanding }">
        <div class="login-overlay-content">
          <div class="spinner-ring">
            <div class="spinner-dot"></div>
          </div>
          <p class="login-overlay-text">登录成功</p>
        </div>
      </div>
    </transition>

    <!-- 加载中 -->
    <div v-if="checkingAuth" class="auth-check">
      <div class="loading-spinner">
        <van-loading size="24px" color="#007AFF" />
        <p class="loading-text">正在检查登录状态...</p>
      </div>
    </div>

    <!-- 未登录状态：现代品牌展示 + 登录表单 -->
    <div v-if="!checkingAuth && !isLogged" class="user-profile guest" key="guest">
      <div class="brand-header animate-fade-in">
        <div class="brand-bg">
          <div class="circle c1"></div>
          <div class="circle c2"></div>
          <div class="circle c3"></div>
        </div>
        <div class="brand-content">
          <div class="brand-icon">
            <svg width="48" height="48" viewBox="0 0 100 100" fill="none">
              <circle cx="50" cy="50" r="40" fill="url(#iconGrad)"/>
              <circle cx="50" cy="50" r="25" fill="rgba(255,255,255,0.2)"/>
              <path d="M40 50L48 58L62 42" stroke="white" stroke-width="5" stroke-linecap="round" stroke-linejoin="round"/>
              <defs>
                <linearGradient id="iconGrad" x1="10" y1="10" x2="90" y2="90">
                  <stop offset="0%" stop-color="#007AFF"/>
                  <stop offset="100%" stop-color="#5856D6"/>
                </linearGradient>
              </defs>
            </svg>
          </div>
          <h1 class="brand-title">WashPro</h1>
          <p class="brand-subtitle">专业洗衣机清洗服务</p>
        </div>
      </div>

      <!-- 登录卡片 -->
      <div class="login-section">
        <div class="login-card">
          <h2 class="login-title">欢迎回来</h2>
          <p class="login-desc">输入手机号即可登录或注册</p>
          
          <div class="input-wrapper">
            <div class="phone-input-group" :class="{ focused: inputFocused, error: phoneError }">
              <span class="country-code">+86</span>
              <input
                ref="phoneInput"
                v-model="phone"
                type="tel"
                maxlength="11"
                placeholder="请输入手机号"
                class="phone-input"
                @focus="inputFocused = true"
                @blur="onPhoneBlur"
                @keydown.enter.prevent="handleLogin"
                @input="onPhoneInput"
                @paste="onPhonePaste"
              />
              <van-icon v-if="phone.length === 11" name="success" color="#34C759" size="20" />
            </div>
            <p v-if="phoneError" class="error-text">{{ phoneError }}</p>
          </div>

          <button 
            class="login-button" 
            :class="{ active: isValidPhone, submitting: loading }" 
            :disabled="!isValidPhone || loading" 
            @click="handleLogin"
          >
            <van-loading v-if="loading" size="20px" color="white" />
            <span v-else>{{ loading ? '登录中...' : '登录 / 注册' }}</span>
          </button>

          <p class="terms-text">
            登录即表示同意
            <a href="#">《服务条款》</a>
            和
            <a href="#">《隐私政策》</a>
          </p>
        </div>

        <!-- 快捷操作 -->
        <div class="quick-actions">
          <button class="action-card" @click="navigateTo('/user/order/create')">
            <div class="action-icon blue">
              <van-icon name="edit" size="22" color="white" />
            </div>
            <div class="action-text">
              <span class="action-title">立即预约</span>
              <span class="action-desc">在线预约清洗服务</span>
            </div>
            <van-icon name="arrow" color="#C7C7CC" size="16" />
          </button>
          
          <button class="action-card" @click="navigateTo('/user/orders')">
            <div class="action-icon purple">
              <van-icon name="records-o" size="22" color="white" />
            </div>
            <div class="action-text">
              <span class="action-title">我的订单</span>
              <span class="action-desc">查看订单状态</span>
            </div>
            <van-icon name="arrow" color="#C7C7CC" size="16" />
          </button>
        </div>

        <!-- 管理员登录 -->
        <button class="admin-link" @click="navigateTo('/admin/login')">
          <van-icon name="manager-o" size="16" />
          <span>管理员登录</span>
        </button>
      </div>
    </div>

    <!-- 已登录状态：现代个人中心 -->
    <div v-else class="user-profile logged-in" key="logged">
      <!-- 头部用户信息 -->
      <div class="profile-header">
        <div class="header-bg">
          <div class="wave w1"></div>
          <div class="wave w2"></div>
        </div>
        <div class="user-info">
          <div class="avatar-section" @click="openAvatarPicker">
            <div class="avatar-ring">
              <div class="avatar">
                <img v-if="avatarUrl" :src="avatarUrl" class="avatar-img" />
                <svg v-else width="40" height="40" viewBox="0 0 24 24" fill="none" stroke="white" stroke-width="1.5">
                  <circle cx="12" cy="8" r="4"/>
                  <path d="M20 21a8 8 0 1 0-16 0"/>
                </svg>
              </div>
            </div>
            <van-icon name="photograph" class="avatar-edit-icon" size="18" color="white" />
          </div>
          <h2 class="username">{{ nickname || '未设置昵称' }}</h2>
          <p class="user-phone">{{ userInfo?.phone || '-' }}</p>
        </div>
      </div>

      <!-- 信息卡片 -->
      <div class="content-section">
        <div class="info-grid">
          <div class="info-item">
            <div class="info-icon blue">
              <van-icon name="phone-o" size="20" color="white" />
            </div>
            <div class="info-content">
              <span class="info-label">手机号</span>
              <span class="info-value">{{ userInfo?.phone || '-' }}</span>
            </div>
          </div>
          
          <div class="info-item">
            <div class="info-icon purple">
              <van-icon name="shop-o" size="20" color="white" />
            </div>
            <div class="info-content">
              <span class="info-label">楼栋</span>
              <span class="info-value">{{ buildingInfo || '未设置' }}</span>
            </div>
          </div>

          <div class="info-item">
            <div class="info-icon green">
              <van-icon name="clock-o" size="20" color="white" />
            </div>
            <div class="info-content">
              <span class="info-label">注册时间</span>
              <span class="info-value">{{ formatTime(userInfo?.createTime) }}</span>
            </div>
          </div>
        </div>

        <!-- 员工状态 -->
        <div class="section-title">员工服务</div>
        <div class="employee-card">
          <div v-if="applyStatus === 'none' || applyStatus === 'rejected'">
            <div v-if="applyStatus === 'rejected'" class="reject-banner">
              <van-icon name="warning-o" size="18" color="#FF3B30" />
              <span>申请被拒绝：{{ rejectReason || '未知原因' }}</span>
            </div>
            <button class="apply-button" @click="showApplyDialog = true">
              <van-icon name="user-add-o" size="20" />
              <span>申请成为员工</span>
            </button>
          </div>
          <div v-else-if="applyStatus === 'pending'" class="status-row">
            <div class="status-badge pending">
              <van-icon name="clock-o" size="18" color="#FF9500" />
              <span>审核中</span>
            </div>
            <p class="status-desc">请耐心等待，通常1-2个工作日内完成审核</p>
          </div>
          <div v-else class="status-row">
            <div class="status-badge approved">
              <van-icon name="checked" size="18" color="#34C759" />
              <span>已通过认证</span>
            </div>
            <p class="status-desc">您已是认证员工，可以享受专属权益</p>
          </div>
        </div>

        <!-- 功能菜单 -->
        <div class="section-title">设置</div>
        <div class="menu-list">
          <div class="menu-item" @click="openEditProfile">
            <van-icon name="setting-o" size="20" color="#007AFF" />
            <span class="menu-label">编辑资料</span>
            <van-icon name="arrow" color="#C7C7CC" size="16" />
          </div>
        </div>

        <!-- 退出登录 -->
        <button class="logout-button" @click="handleLogout">
          <span>退出登录</span>
        </button>
      </div>

      <!-- 弹窗 -->
      <van-dialog v-model:show="showEdit" title="编辑资料" show-cancel-button @confirm="saveProfile">
        <van-field v-model="editNickname" label="昵称" placeholder="请输入昵称" maxlength="20" />
        <van-field v-model="editPhone" label="手机号" placeholder="请输入手机号" maxlength="11" />
        <van-field v-model="editBuilding" label="宿舍楼栋" placeholder="如：食宿楼 · 1栋" />
        <van-field v-model="editRoom" label="房间号" placeholder="如：301" />
      </van-dialog>

      <van-dialog v-model:show="showApplyDialog" title="申请成为员工" show-cancel-button :before-close="handleApplySubmit">
        <van-field v-model="applyName" label="姓名" placeholder="真实姓名" maxlength="20" required />
        <van-field v-model="applyPhone" label="手机号" placeholder="联系电话" maxlength="11" required />
        <van-field v-model="applyMajor" label="专业" placeholder="学院专业" maxlength="50" />
        <van-field v-model="applyGrade" label="年级" placeholder="如：2024级" maxlength="20" />
      </van-dialog>

      <van-dialog v-model:show="showSetupDialog" title="完善信息" show-cancel-button confirm-button-text="确认" cancel-button-text="跳过" @confirm="handleSetupConfirm" @cancel="skipSetup">
        <van-field v-model="setupBuilding" label="宿舍楼栋" placeholder="如：食宿楼 · 1栋" />
        <van-field v-model="setupRoom" label="房间号" placeholder="如：301" />
      </van-dialog>

      <input ref="galleryInput" type="file" accept="image/*" style="display:none" @change="onAvatarFile" />
      <input ref="cameraInput" type="file" accept="image/*" capture="environment" style="display:none" @change="onAvatarFile" />
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, computed, onMounted, watch } from 'vue'
import { useRouter } from 'vue-router'
import { showToast } from 'vant'
import { getUserInfo, setUserInfo, setToken, getRole, removeAuth, getToken } from '../../utils/auth'
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

// 登录相关
const phone = ref('')
const inputFocused = ref(false)
const phoneInput = ref<HTMLInputElement | null>(null)
const loading = ref(false)
const phoneError = ref('')

// 完善信息相关
const showSetupDialog = ref(false)
const setupBuilding = ref('')
const setupRoom = ref('')
let pendingUser: any = null

// 认证检查
const checkingAuth = ref(true)
const isLogged = ref(false)
const loginAnimating = ref(false)
const loginExpanding = ref(false)

// 防重复点击
const isSubmitting = ref(false)

const isValidPhone = computed(() => /^1[3-9]\d{9}$/.test(phone.value))

// 格式化时间
function formatTime(time: unknown): string {
  if (!time) return '-'
  const str = String(time)
  if (str.length >= 19) return str.substring(0, 10)
  return str
}

// 验证手机号格式
function validatePhone(p: string): string {
  if (!p) return '请输入手机号'
  if (!/^1\d{10}$/.test(p)) return '手机号格式不正确'
  if (!/^1[3-9]/.test(p)) return '请输入有效的手机号'
  return ''
}

function onPhoneInput() {
  phone.value = phone.value.replace(/\D/g, '')
  if (phone.value.length === 11) {
    phoneError.value = validatePhone(phone.value)
  } else {
    phoneError.value = ''
  }
}

function onPhoneBlur() {
  inputFocused.value = false
  if (phone.value.length === 11) {
    phoneError.value = validatePhone(phone.value)
  }
}

function onPhonePaste(e: ClipboardEvent) {
  e.preventDefault()
  const text = e.clipboardData?.getData('text') || ''
  phone.value = text.replace(/\D/g, '').substring(0, 11)
}

// 验证 token 是否有效
async function checkAuth() {
  const token = getToken()
  const savedUser = getUserInfo()
  
  // 优先使用本地已保存的登录状态
  if (token && savedUser) {
    isLogged.value = true
    checkingAuth.value = false
    // 后台静默验证 token（不阻塞 UI，失败也不清除状态）
    try {
      const res = await get<{code: number; data: any}>('/api/user/profile')
      if (res.data && res.data.code === 200) {
        setUserInfo(res.data.data)
        loadUserInfo()
        fetchApplyStatus()
      }
      // 即使验证失败（401等），也不清除本地状态
      // 让后续实际业务 API 调用时自然发现 token 失效
    } catch {
      // 静默失败，保持登录状态
    }
    return
  }

  // 没有本地状态，需要验证
  if (!token) {
    checkingAuth.value = false
    isLogged.value = false
    return
  }

  try {
    const res = await get<{code: number; data: any}>('/api/user/profile')
    if (res.data && res.data.code === 200) {
      isLogged.value = true
      setUserInfo(res.data.data)
    } else {
      removeAuth()
      isLogged.value = false
    }
  } catch {
    removeAuth()
    isLogged.value = false
  }
  checkingAuth.value = false
}

async function handleLogin() {
  if (!isValidPhone.value || loading.value) return
  
  const error = validatePhone(phone.value)
  if (error) {
    phoneError.value = error
    return
  }
  
  loading.value = true
  try {
    const res = await post<any>('/api/auth/login', { phone: phone.value })
    const { token, user } = res.data.data
    pendingUser = user
    setToken(token)
    setUserInfo(user)

    // 如果是新用户或未设置楼栋，弹窗完善信息
    if (!user.buildingName || !user.roomNo) {
      setupBuilding.value = user.buildingName || ''
      setupRoom.value = user.roomNo || ''
      showSetupDialog.value = true
    } else {
      handleLoginSuccess()
    }
    phone.value = ''
  } catch {
    showToast('登录失败，请稍后重试')
  } finally {
    loading.value = false
  }
}

function handleLoginSuccess() {
  // 触发登录成功动画
  loginAnimating.value = true
  loginExpanding.value = false
  
  setTimeout(() => {
    isLogged.value = true
    loadUserInfo()
    fetchApplyStatus()

    // 检查是否需要回跳到订单创建页
    if (sessionStorage.getItem('order_redirect') === 'true') {
      sessionStorage.removeItem('order_redirect')
      router.push('/user/order/create')
    }
    
    // 开始扩散动画
    loginExpanding.value = true
    
    // 动画结束后隐藏
    setTimeout(() => {
      loginAnimating.value = false
      loginExpanding.value = false
    }, 800)
  }, 500)
}

function skipSetup() { showSetupDialog.value = false; handleLoginSuccess() }
async function handleSetupConfirm() {
  if (setupBuilding.value && setupRoom.value) {
    try {
      await put('/api/user/profile', { buildingName: setupBuilding.value, roomNo: setupRoom.value })
      pendingUser.buildingName = setupBuilding.value
      pendingUser.roomNo = setupRoom.value
      setUserInfo(pendingUser)
    } catch { /* ignore */ }
  }
  showSetupDialog.value = false
  handleLoginSuccess()
}

function goAdminLogin() { router.push('/admin/login') }

// 防重复点击导航
function navigateTo(path: string) {
  if (isSubmitting.value) return
  isSubmitting.value = true
  router.push(path)
  setTimeout(() => { isSubmitting.value = false }, 500)
}

// 监听登录状态变化
watch(() => localStorage.getItem('washpro_token'), (val) => {
  isLogged.value = !!val
})

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
    const res = await get<{code:number;data:{status:number;remark?:string}|null}>('/api/user/apply-status')
    const app = res.data.data
    if (!app) applyStatus.value = 'none'
    else if (app.status === 0) applyStatus.value = 'pending'
    else if (app.status === 1) applyStatus.value = 'approved'
    else { applyStatus.value = 'rejected'; rejectReason.value = app.remark || '未知原因' }
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

function handleLogout() {
  removeAuth()
  phone.value = ''
  inputFocused.value = false
  isLogged.value = false
  showToast('已退出登录')
  router.push('/user/home')
}

onMounted(() => {
  checkAuth().then(() => {
    if (isLogged.value) {
      loadUserInfo()
      fetchApplyStatus()
    }
  }).catch(() => {
    checkingAuth.value = false
    isLogged.value = false
  })
})
</script>

<style>
/* 全局 CSS 变量 - 不使用 scoped */
.user-profile-wrapper {
  --primary: #007AFF;
  --primary-gradient: linear-gradient(135deg, #007AFF, #5856D6);
  --success: #34C759;
  --warning: #FF9500;
  --danger: #FF3B30;
  --text-primary: #1D1D1F;
  --text-secondary: #86868B;
  --text-tertiary: #C7C7CC;
  --bg-primary: #F2F2F7;
  --bg-card: #FFFFFF;
  --border-light: rgba(0,0,0,0.06);
  --shadow-sm: 0 1px 3px rgba(0,0,0,0.04);
  --shadow-md: 0 4px 12px rgba(0,0,0,0.06);
  --shadow-lg: 0 8px 24px rgba(0,0,0,0.08);
  --radius-sm: 12px;
  --radius-md: 16px;
  --radius-lg: 20px;
  background: var(--bg-primary);
  min-height: 100vh;
}
</style>

<style scoped>
/* ====== Wrapper 容器 ====== */
.user-profile-wrapper {
  min-height: 100vh;
  width: 100%;
  background: var(--bg-primary);
}

/* ====== 认证检查页面 ====== */
.auth-check {
  min-height: 100vh;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  background: var(--bg-primary);
}
.loading-spinner {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 16px;
}
.loading-text {
  font-size: 14px;
  color: var(--text-secondary);
}

/* ====== 未登录状态 ====== */
.user-profile.guest {
  min-height: 100vh;
  background: var(--bg-primary);
  padding-bottom: max(24px, env(safe-area-inset-bottom));
}

/* 品牌头部 */
.brand-header {
  position: relative;
  height: 220px;
  overflow: hidden;
  display: flex;
  align-items: center;
  justify-content: center;
}
.brand-bg {
  position: absolute;
  inset: 0;
  background: linear-gradient(135deg, #007AFF 0%, #5856D6 100%);
}
.brand-bg .circle {
  position: absolute;
  border-radius: 50%;
  background: rgba(255,255,255,0.08);
}
.c1 { width: 200px; height: 200px; top: -60px; right: -40px; }
.c2 { width: 120px; height: 120px; bottom: -30px; left: 20px; }
.c3 { width: 80px; height: 80px; top: 30px; left: 30%; }

.brand-content {
  position: relative;
  z-index: 1;
  display: flex;
  flex-direction: column;
  align-items: center;
  text-align: center;
}
.brand-icon {
  width: 72px;
  height: 72px;
  background: rgba(255,255,255,0.2);
  backdrop-filter: blur(10px);
  border: 2px solid rgba(255,255,255,0.3);
  border-radius: 20px;
  display: flex;
  align-items: center;
  justify-content: center;
  margin-bottom: 16px;
}
.brand-title {
  font-size: 32px;
  font-weight: 800;
  color: white;
  letter-spacing: -0.04em;
  margin: 0 0 4px;
}
.brand-subtitle {
  font-size: 14px;
  color: rgba(255,255,255,0.8);
  margin: 0;
}

/* 登录区域 */
.login-section {
  padding: 0 20px;
  margin-top: -30px;
  position: relative;
  z-index: 2;
}
.login-card {
  background: var(--bg-card);
  border-radius: var(--radius-lg);
  padding: 24px;
  box-shadow: var(--shadow-lg);
  margin-bottom: 20px;
}
.login-title {
  font-size: 22px;
  font-weight: 700;
  color: var(--text-primary);
  margin: 0 0 6px;
}
.login-desc {
  font-size: 14px;
  color: var(--text-secondary);
  margin: 0 0 20px;
}

.input-wrapper {
  margin-bottom: 16px;
}
.phone-input-group {
  display: flex;
  align-items: center;
  background: var(--bg-primary);
  border: 2px solid transparent;
  border-radius: var(--radius-sm);
  padding: 0 16px;
  height: 54px;
  transition: all 0.2s;
}
.phone-input-group.focused {
  border-color: var(--primary);
  background: white;
  box-shadow: 0 0 0 4px rgba(0,122,255,0.1);
}
.phone-input-group.error {
  border-color: var(--danger);
}
.country-code {
  font-size: 16px;
  font-weight: 600;
  color: var(--text-primary);
  margin-right: 12px;
  padding-right: 12px;
  border-right: 2px solid var(--border-light);
}
.phone-input {
  flex: 1;
  border: none;
  outline: none;
  background: transparent;
  font-size: 16px;
  color: var(--text-primary);
  letter-spacing: 0.02em;
}
.phone-input::placeholder {
  color: var(--text-tertiary);
}
.error-text {
  font-size: 12px;
  color: var(--danger);
  margin: 6px 0 0;
}

.login-button {
  width: 100%;
  height: 54px;
  border: none;
  border-radius: var(--radius-sm);
  font-size: 16px;
  font-weight: 600;
  color: white;
  background: var(--text-tertiary);
  cursor: not-allowed;
  transition: all 0.25s;
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 8px;
}
.login-button.active {
  background: var(--primary-gradient);
  cursor: pointer;
  box-shadow: 0 4px 16px rgba(0,122,255,0.3);
}
.login-button.active:active {
  transform: scale(0.98);
}
.login-button.submitting {
  opacity: 0.8;
  cursor: not-allowed;
}

.terms-text {
  font-size: 12px;
  color: var(--text-tertiary);
  text-align: center;
  margin: 16px 0 0;
}
.terms-text a {
  color: var(--primary);
  text-decoration: none;
}

/* 快捷操作 */
.quick-actions {
  display: flex;
  flex-direction: column;
  gap: 12px;
  margin-bottom: 16px;
}
.action-card {
  display: flex;
  align-items: center;
  gap: 14px;
  background: var(--bg-card);
  border: none;
  border-radius: var(--radius-md);
  padding: 16px 18px;
  cursor: pointer;
  box-shadow: var(--shadow-sm);
  transition: all 0.2s;
  text-align: left;
}
.action-card:active {
  transform: scale(0.98);
  box-shadow: var(--shadow-md);
}
.action-icon {
  width: 42px;
  height: 42px;
  border-radius: 12px;
  display: flex;
  align-items: center;
  justify-content: center;
  flex-shrink: 0;
}
.action-icon.blue { background: linear-gradient(135deg, #007AFF, #5AC8FA); }
.action-icon.purple { background: linear-gradient(135deg, #5856D6, #AF52DE); }
.action-icon.green { background: linear-gradient(135deg, #34C759, #30D158); }
.action-text {
  flex: 1;
  display: flex;
  flex-direction: column;
  gap: 2px;
}
.action-title {
  font-size: 15px;
  font-weight: 600;
  color: var(--text-primary);
}
.action-desc {
  font-size: 12px;
  color: var(--text-secondary);
}

/* 管理员登录 */
.admin-link {
  display: inline-flex;
  align-items: center;
  gap: 6px;
  background: none;
  border: none;
  font-size: 13px;
  color: var(--text-secondary);
  cursor: pointer;
  padding: 8px 16px;
  margin: 0 auto;
  transition: color 0.2s;
}
.admin-link:hover {
  color: var(--primary);
}

/* ====== 已登录状态 ====== */
.user-profile.logged-in {
  min-height: 100vh;
  background: var(--bg-primary);
  padding-bottom: max(24px, env(safe-area-inset-bottom));
}

/* 头部 */
.profile-header {
  position: relative;
  height: 200px;
  overflow: hidden;
}
.header-bg {
  position: absolute;
  inset: 0;
  background: linear-gradient(135deg, #007AFF, #5856D6);
}
.header-bg .wave {
  position: absolute;
  border-radius: 50%;
  background: rgba(255,255,255,0.08);
}
.w1 { width: 250px; height: 250px; bottom: -120px; right: -50px; }
.w2 { width: 150px; height: 150px; top: -40px; left: -30px; }

.user-info {
  position: absolute;
  bottom: 0;
  left: 0;
  right: 0;
  display: flex;
  flex-direction: column;
  align-items: center;
  padding-bottom: 16px;
  z-index: 1;
}
.avatar-section {
  position: relative;
  cursor: pointer;
  margin-bottom: 12px;
}
.avatar-ring {
  width: 76px;
  height: 76px;
  border-radius: 50%;
  border: 3px solid rgba(255,255,255,0.5);
  display: flex;
  align-items: center;
  justify-content: center;
}
.avatar {
  width: 68px;
  height: 68px;
  border-radius: 50%;
  background: rgba(255,255,255,0.2);
  display: flex;
  align-items: center;
  justify-content: center;
  overflow: hidden;
}
.avatar-img {
  width: 100%;
  height: 100%;
  object-fit: cover;
}
.avatar-edit-icon {
  position: absolute;
  bottom: 2px;
  right: 2px;
  width: 22px;
  height: 22px;
  background: rgba(0,0,0,0.3);
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
}
.username {
  font-size: 18px;
  font-weight: 700;
  color: white;
  margin: 0 0 4px;
}
.user-phone {
  font-size: 13px;
  color: rgba(255,255,255,0.7);
  margin: 0;
}

/* 内容区 */
.content-section {
  padding: 0 16px;
  margin-top: 16px;
}

.info-grid {
  display: grid;
  grid-template-columns: repeat(3, 1fr);
  gap: 12px;
  margin-bottom: 24px;
}
.info-item {
  background: var(--bg-card);
  border-radius: var(--radius-md);
  padding: 16px 12px;
  display: flex;
  flex-direction: column;
  align-items: center;
  text-align: center;
  box-shadow: var(--shadow-sm);
}
.info-icon {
  width: 36px;
  height: 36px;
  border-radius: 10px;
  display: flex;
  align-items: center;
  justify-content: center;
  margin-bottom: 10px;
}
.info-icon.blue { background: linear-gradient(135deg, #007AFF, #5AC8FA); }
.info-icon.purple { background: linear-gradient(135deg, #5856D6, #AF52DE); }
.info-icon.green { background: linear-gradient(135deg, #34C759, #30D158); }
.info-content {
  display: flex;
  flex-direction: column;
  gap: 4px;
  width: 100%;
  overflow: hidden;
}
.info-label {
  font-size: 11px;
  color: var(--text-secondary);
}
.info-value {
  font-size: 13px;
  font-weight: 600;
  color: var(--text-primary);
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
  max-width: 100%;
}

.section-title {
  font-size: 13px;
  font-weight: 600;
  color: var(--text-secondary);
  margin: 16px 0 10px;
  padding-left: 4px;
}

.employee-card {
  background: var(--bg-card);
  border-radius: var(--radius-md);
  padding: 18px;
  box-shadow: var(--shadow-sm);
}
.reject-banner {
  display: flex;
  align-items: center;
  gap: 8px;
  font-size: 13px;
  color: var(--danger);
  background: rgba(255,59,48,0.06);
  padding: 10px 14px;
  border-radius: 10px;
  margin-bottom: 14px;
}
.apply-button {
  width: 100%;
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 10px;
  padding: 14px;
  background: var(--primary-gradient);
  color: white;
  border: none;
  border-radius: var(--radius-sm);
  font-size: 15px;
  font-weight: 600;
  cursor: pointer;
  transition: all 0.2s;
}
.apply-button:active {
  transform: scale(0.98);
}
.status-row {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 8px;
}
.status-badge {
  display: inline-flex;
  align-items: center;
  gap: 6px;
  padding: 8px 16px;
  border-radius: 20px;
  font-size: 14px;
  font-weight: 600;
}
.status-badge.pending {
  background: rgba(255,149,0,0.1);
  color: var(--warning);
}
.status-badge.approved {
  background: rgba(52,199,89,0.1);
  color: var(--success);
}
.status-desc {
  font-size: 12px;
  color: var(--text-secondary);
  text-align: center;
  margin: 0;
}

.menu-list {
  background: var(--bg-card);
  border-radius: var(--radius-md);
  overflow: hidden;
  box-shadow: var(--shadow-sm);
}
.menu-item {
  display: flex;
  align-items: center;
  gap: 14px;
  padding: 16px 18px;
  cursor: pointer;
  transition: background 0.2s;
}
.menu-item:active {
  background: var(--bg-primary);
}
.menu-label {
  flex: 1;
  font-size: 15px;
  color: var(--text-primary);
}

.logout-button {
  display: block;
  width: 100%;
  padding: 16px;
  margin-top: 24px;
  background: var(--bg-card);
  color: var(--danger);
  border: none;
  border-radius: var(--radius-md);
  font-size: 15px;
  font-weight: 600;
  cursor: pointer;
  box-shadow: var(--shadow-sm);
  transition: all 0.2s;
}
.logout-button:active {
  background: #FFF5F5;
}

/* ====== 过渡动画 ====== */
.fade-enter-active,
.fade-leave-active {
  transition: opacity 0.3s ease;
}
.fade-enter-from,
.fade-leave-to {
  opacity: 0;
}

.slide-up-enter-active {
  transition: all 0.4s cubic-bezier(0.16, 1, 0.3, 1);
}
.slide-up-leave-active {
  transition: all 0.3s ease;
}
.slide-up-enter-from {
  opacity: 0;
  transform: translateY(30px);
}
.slide-up-leave-to {
  opacity: 0;
  transform: translateY(-20px);
}

/* ====== 动画类 ====== */
.animate-fade-in {
  animation: fadeIn 0.6s ease-out;
}
@keyframes fadeIn {
  from { opacity: 0; transform: translateY(-10px); }
  to { opacity: 1; transform: translateY(0); }
}

/* ====== 按钮点击反馈 ====== */
.login-button,
.apply-button,
.action-card,
.menu-item {
  position: relative;
  overflow: hidden;
}
.login-button::after,
.apply-button::after,
.action-card::after,
.menu-item::after {
  content: '';
  position: absolute;
  top: 50%;
  left: 50%;
  width: 0;
  height: 0;
  border-radius: 50%;
  background: rgba(255,255,255,0.3);
  transform: translate(-50%, -50%);
  transition: width 0.4s, height 0.4s;
}
.login-button:active::after,
.apply-button:active::after,
.action-card:active::after,
.menu-item:active::after {
  width: 300px;
  height: 300px;
}

/* ===== Login Overlay Animation ===== */
.login-overlay {
  position: fixed;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  background: rgba(242, 242, 247, 0.97);
  backdrop-filter: blur(20px);
  -webkit-backdrop-filter: blur(20px);
  display: flex;
  align-items: center;
  justify-content: center;
  z-index: 9999;
}
.login-overlay.expanding {
  animation: expandReveal 0.8s cubic-bezier(0.4, 0, 0.2, 1) forwards;
}
@keyframes expandReveal {
  0% {
    clip-path: circle(100% at 50% 50%);
    opacity: 1;
  }
  100% {
    clip-path: circle(0% at 50% 50%);
    opacity: 0;
  }
}
.login-overlay-content {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 20px;
}

/* Spinning ring */
.spinner-ring {
  width: 80px;
  height: 80px;
  border-radius: 50%;
  border: 3px solid rgba(52, 199, 89, 0.2);
  border-top-color: #34C759;
  animation: spin 0.8s linear infinite;
}
.spinner-dot {
  width: 100%;
  height: 100%;
  border-radius: 50%;
  background: radial-gradient(circle, rgba(52, 199, 89, 0.3) 0%, transparent 70%);
  animation: pulse 1s ease-in-out infinite;
}

@keyframes spin {
  to { transform: rotate(360deg); }
}
@keyframes pulse {
  0%, 100% { transform: scale(0.8); opacity: 0.5; }
  50% { transform: scale(1.2); opacity: 1; }
}

.login-overlay-text {
  font-size: 18px;
  font-weight: 700;
  color: #34C759;
  opacity: 0;
  transform: translateY(10px);
  animation: fadeSlideUp 0.4s ease 0.3s forwards;
}

@keyframes fadeSlideUp {
  to { opacity: 1; transform: translateY(0); }
}

/* Login overlay transition */
.login-overlay-enter-active {
  transition: opacity 0.3s ease;
}
.login-overlay-leave-active {
  transition: opacity 0.4s ease;
}
.login-overlay-enter-from,
.login-overlay-leave-to {
  opacity: 0;
}

/* Page fade transition */
.page-fade-enter-active {
  transition: opacity 0.35s ease, transform 0.35s ease;
}
.page-fade-leave-active {
  transition: opacity 0.25s ease, transform 0.25s ease;
}
.page-fade-enter-from {
  opacity: 0;
  transform: translateY(16px);
}
.page-fade-leave-to {
  opacity: 0;
  transform: translateY(-16px);
}
</style>
