<template>
  <div class="login-page">
    <div class="hero">
      <div class="hero-icon">
        <van-icon name="gem-o" size="48" color="#2B95FF" />
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
          @input="onPhoneInput"
        />
      </div>

      <button
        class="login-btn"
        :class="{ active: isValid }"
        :disabled="!isValid"
        @click="handleLogin"
      >
        登录 / 注册
      </button>
    </div>

    <button class="admin-login-btn" @click="goAdminLogin">
      管理员登录
    </button>

    <div class="footer-note">
      登录即表示同意《服务条款》和《隐私政策》
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, computed } from 'vue'
import { useRouter } from 'vue-router'
import { showToast } from 'vant'
import { post } from '../utils/request'
import { setToken, setUserInfo } from '../utils/auth'

const router = useRouter()
const phone = ref('')
const inputFocused = ref(false)
const phoneInput = ref<HTMLInputElement | null>(null)
const loading = ref(false)

const isValid = computed(() => /^1\d{10}$/.test(phone.value))

function onPhoneInput() {
  phone.value = phone.value.replace(/\D/g, '')
}

async function handleLogin() {
  if (!isValid.value || loading.value) return

  loading.value = true
  try {
    const res = await post<any>('/api/auth/login', { phone: phone.value })
    const { token, user } = res.data.data
    setToken(token)
    setUserInfo(user)
    router.replace('/user/home')
  } catch {
    showToast('登录失败，请稍后重试')
  } finally {
    loading.value = false
  }
}

function goAdminLogin() {
  router.push('/admin/login')
}
</script>

<style scoped>
.login-page {
  min-height: 100vh;
  min-height: 100dvh;
  background: linear-gradient(180deg, #F5F5F7 0%, #FFFFFF 50%);
  display: flex;
  flex-direction: column;
  align-items: center;
  padding: 0 32px;
  padding-bottom: max(32px, env(safe-area-inset-bottom));
  box-sizing: border-box;
}

.hero {
  display: flex;
  flex-direction: column;
  align-items: center;
  padding-top: 100px;
  padding-bottom: 60px;
}

.hero-icon {
  width: 88px;
  height: 88px;
  background: rgba(43, 149, 255, 0.1);
  border-radius: 24px;
  display: flex;
  align-items: center;
  justify-content: center;
  margin-bottom: 24px;
  transition: transform 0.3s;
}

.hero-icon:active {
  transform: scale(0.95);
}

.app-name {
  font-size: 36px;
  font-weight: 700;
  color: #1D1D1F;
  letter-spacing: -0.03em;
  margin: 0 0 8px 0;
}

.app-desc {
  font-size: 16px;
  color: #86868B;
  margin: 0;
  letter-spacing: 0.02em;
}

.form-area {
  width: 100%;
  max-width: 360px;
  display: flex;
  flex-direction: column;
  gap: 24px;
}

.input-group {
  display: flex;
  align-items: center;
  background: rgba(0, 0, 0, 0.04);
  border-radius: 16px;
  padding: 0 20px;
  height: 56px;
  border: 2px solid transparent;
  transition: all 0.2s;
}

.input-group.focused {
  border-color: #2B95FF;
  background: #FFFFFF;
  box-shadow: 0 0 0 4px rgba(43, 149, 255, 0.1);
}

.input-prefix {
  font-size: 16px;
  font-weight: 500;
  color: #1D1D1F;
  margin-right: 12px;
  padding-right: 12px;
  border-right: 1px solid rgba(0, 0, 0, 0.1);
  white-space: nowrap;
}

.phone-input {
  flex: 1;
  border: none;
  outline: none;
  background: transparent;
  font-size: 17px;
  color: #1D1D1F;
  letter-spacing: 0.02em;
  height: 100%;
}

.phone-input::placeholder {
  color: #C7C7CC;
  font-weight: 400;
}

.login-btn {
  width: 100%;
  height: 56px;
  border: none;
  border-radius: 16px;
  font-size: 17px;
  font-weight: 600;
  color: #FFFFFF;
  background: #C7C7CC;
  cursor: not-allowed;
  transition: all 0.25s;
  letter-spacing: 0.02em;
}

.login-btn.active {
  background: #2B95FF;
  cursor: pointer;
  box-shadow: 0 4px 14px rgba(43, 149, 255, 0.3);
}

.login-btn.active:active {
  transform: scale(0.97);
  box-shadow: 0 2px 8px rgba(43, 149, 255, 0.2);
}

.admin-login-btn {
  width: 100%;
  max-width: 360px;
  height: 48px;
  border: 1px solid rgba(43, 149, 255, 0.3);
  border-radius: 12px;
  font-size: 15px;
  font-weight: 500;
  color: #2B95FF;
  background: transparent;
  cursor: pointer;
  transition: all 0.2s;
  letter-spacing: 0.02em;
}

.admin-login-btn:active {
  background: rgba(43, 149, 255, 0.05);
}

.footer-note {
  margin-top: auto;
  padding-top: 40px;
  font-size: 12px;
  color: #C7C7CC;
  text-align: center;
  line-height: 1.5;
}
</style>