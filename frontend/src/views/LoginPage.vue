<template>
  <div class="login-page">
    <div class="hero">
      <div class="hero-icon">
        <svg width="44" height="44" viewBox="0 0 100 100" fill="none">
          <rect x="15" y="30" width="70" height="55" rx="10" fill="#007AFF" opacity="0.15"/>
          <rect x="22" y="24" width="56" height="12" rx="6" fill="#007AFF" opacity="0.1"/>
          <circle cx="50" cy="56" r="14" fill="#007AFF" opacity="0.12"/>
          <path d="M44 56L48 60L56 52" stroke="#007AFF" stroke-width="3" stroke-linecap="round" stroke-linejoin="round"/>
        </svg>
      </div>
      <h1 class="app-name">WashPro</h1>
      <p class="app-desc">专业洗衣机清洗服务</p>
    </div>

    <div class="form-area">
      <div class="input-group" :class="{ focused: inputFocused }">
        <span class="input-prefix">+86</span>
        <input
          ref="phoneInput"
          v-model="phone"
          type="tel"
          maxlength="11"
          placeholder="请输入手机号"
          class="phone-input"
          @focus="inputFocused = true"
          @blur="inputFocused = false"
          @keydown.enter.prevent="handleLogin"
          @input="onPhoneInput"
          @paste="onPhonePaste"
        />
      </div>
      <button class="login-btn" :class="{ active: isValid }" :disabled="!isValid" @click="handleLogin">
        登录 / 注册
      </button>
    </div>

    <button class="admin-login-btn" @click="goAdminLogin">管理员登录</button>

    <div class="footer-note">登录即表示同意《服务条款》和《隐私政策》</div>

    <!-- 登录后完善信息弹窗 -->
    <van-dialog v-model:show="showSetupDialog" title="完善信息" show-cancel-button confirm-button-text="确认" cancel-button-text="跳过" @confirm="handleSetupConfirm" @cancel="skipSetup">
      <div class="setup-form">
        <van-field v-model="setupBuilding" label="宿舍楼栋" placeholder="如：食宿楼 · 1栋" />
        <van-field v-model="setupRoom" label="房间号" placeholder="如：301" />
      </div>
    </van-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, computed, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { showToast } from 'vant'
import { post, put } from '../utils/request'
import { setToken, setUserInfo } from '../utils/auth'

const router = useRouter()
const phone = ref('')
const inputFocused = ref(false)
const phoneInput = ref<HTMLInputElement | null>(null)
const loading = ref(false)
const showSetupDialog = ref(false)
const setupBuilding = ref('')
const setupRoom = ref('')
let pendingToken = ''
let pendingUser: any = null

const isValid = computed(() => /^1\d{10}$/.test(phone.value))

function onPhoneInput() { phone.value = phone.value.replace(/\D/g, '') }
function onPhonePaste(e: ClipboardEvent) {
  e.preventDefault()
  const text = e.clipboardData?.getData('text') || ''
  phone.value = text.replace(/\D/g, '').substring(0, 11)
}

async function doLogin() {
  loading.value = true
  try {
    const res = await post<any>('/api/auth/login', { phone: phone.value })
    const { token, user } = res.data.data
    pendingToken = token
    pendingUser = user
    setToken(token)
    setUserInfo(user)
    return true
  } catch { showToast('登录失败，请稍后重试'); return false }
  finally { loading.value = false }
}

async function handleLogin() {
  if (!isValid.value || loading.value) return
  const ok = await doLogin()
  if (!ok) return

  // 如果是新用户或未设置楼栋，弹窗完善信息
  if (!pendingUser.buildingName || !pendingUser.roomNo) {
    setupBuilding.value = pendingUser.buildingName || ''
    setupRoom.value = pendingUser.roomNo || ''
    showSetupDialog.value = true
    return
  }
  redirectByRole()
}

function skipSetup() { showSetupDialog.value = false; redirectByRole() }
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
  redirectByRole()
}

function redirectByRole() {
  const role = pendingUser?.role
  if (role === 'admin' || role === 2) router.replace('/admin/dashboard')
  else if (role === 'employee' || role === 1) router.replace('/employee/available')
  else router.replace('/user/home')
}

function goAdminLogin() { router.push('/admin/login') }

onMounted(() => { phoneInput.value?.focus() })
</script>

<style scoped>
.login-page {
  min-height: 100vh; min-height: 100dvh;
  background: linear-gradient(180deg, #F2F2F7 0%, #FAFAFA 50%, #FFFFFF 100%);
  display: flex; flex-direction: column; align-items: center;
  padding: 0 28px; padding-bottom: max(32px, env(safe-area-inset-bottom));
  box-sizing: border-box; overflow-x: hidden;
}
.hero { display: flex; flex-direction: column; align-items: center; padding-top: 90px; padding-bottom: 48px; }
.hero-icon {
  width: 80px; height: 80px; background: rgba(0,122,255,0.06);
  border: 1px solid rgba(0,122,255,0.1); border-radius: 22px;
  display: flex; align-items: center; justify-content: center; margin-bottom: 20px;
}
.app-name { font-size: 36px; font-weight: 800; color: var(--color-text-primary); letter-spacing: -0.04em; margin: 0 0 6px; }
.app-desc { font-size: 15px; color: var(--color-text-secondary); margin: 0; }

.form-area { width: 100%; max-width: 360px; display: flex; flex-direction: column; gap: 16px; }
.input-group {
  display: flex; align-items: center; background: var(--glass-bg);
  backdrop-filter: blur(12px); -webkit-backdrop-filter: blur(12px);
  border: 1.5px solid rgba(0,0,0,0.06); border-radius: 16px; padding: 0 18px; height: 56px;
  transition: all 0.2s;
}
.input-group.focused { border-color: var(--color-accent); box-shadow: 0 0 0 4px rgba(0,122,255,0.1); }
.input-prefix {
  font-size: 15px; font-weight: 500; color: var(--color-text-primary);
  margin-right: 12px; padding-right: 12px; border-right: 1px solid rgba(0,0,0,0.08); white-space: nowrap;
}
.phone-input {
  flex: 1; border: none; outline: none; background: transparent;
  font-size: 17px; color: var(--color-text-primary); letter-spacing: 0.02em; height: 100%;
}
.phone-input::placeholder { color: var(--color-text-tertiary); }

.login-btn {
  width: 100%; height: 56px; border: none; border-radius: 16px;
  font-size: 17px; font-weight: 600; color: #FFFFFF; background: #C7C7CC;
  cursor: not-allowed; transition: all 0.25s;
}
.login-btn.active { background: #1D1D1F !important; cursor: pointer; box-shadow: 0 4px 16px rgba(0,0,0,0.15) !important; }
.login-btn.active:active { transform: scale(0.97); background: #000 !important; }

.admin-login-btn {
  margin-top: 48px; padding: 10px 26px;
  background: var(--glass-bg); backdrop-filter: blur(12px); -webkit-backdrop-filter: blur(12px);
  border: 1px solid rgba(0,0,0,0.06); border-radius: 14px;
  font-size: 13px; font-weight: 500; color: var(--color-text-secondary);
  cursor: pointer; transition: all 0.2s; box-shadow: var(--shadow-sm);
}
.admin-login-btn:active { background: rgba(0,0,0,0.04); color: var(--color-text-primary); }

.footer-note { margin-top: auto; padding-top: 24px; font-size: 12px; color: var(--color-text-tertiary); text-align: center; }
.setup-form { padding: 8px 0; }
</style>
