<template>
  <div class="admin-login">
    <div class="login-header">
      <div class="login-icon">
        <svg width="40" height="40" viewBox="0 0 100 100" fill="none">
          <rect x="15" y="30" width="70" height="55" rx="10" fill="#2B95FF" opacity="0.15"/>
          <rect x="22" y="24" width="56" height="12" rx="6" fill="#2B95FF" opacity="0.1"/>
          <circle cx="50" cy="56" r="14" fill="#2B95FF" opacity="0.1"/>
          <path d="M44 56L48 60L56 52" stroke="#2B95FF" stroke-width="3" stroke-linecap="round" stroke-linejoin="round"/>
        </svg>
      </div>
      <h1 class="login-title">管理员登录</h1>
      <p class="login-subtitle">WashPro 管理后台</p>
    </div>

    <div class="login-form">
      <van-field
        v-model="username"
        label="账号"
        placeholder="请输入管理员账号"
        clearable
        left-icon="manager-o"
      />
      <van-field
        v-model="password"
        type="password"
        label="密码"
        placeholder="请输入密码"
        left-icon="lock-o"
      />
      <button class="login-btn" @click="handleLogin">登 录</button>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref } from 'vue'
import { useRouter } from 'vue-router'
import { showToast } from 'vant'
import { post } from '../../utils/request'
import { setToken, setUserInfo } from '../../utils/auth'

const router = useRouter()
const username = ref('')
const password = ref('')
const loading = ref(false)

async function handleLogin() {
  if (!username.value || !password.value) {
    showToast('请输入账号和密码')
    return
  }

  loading.value = true
  try {
    const res = await post<any>('/api/auth/admin/login', {
      username: username.value,
      password: password.value,
    })
    // 检查业务状态码
    if (res.data.code !== 200) {
      showToast(res.data.msg || '登录失败')
      return
    }
    const { token, userInfo } = res.data.data
    setToken(token)
    setUserInfo(userInfo)
    showToast('登录成功')
    router.replace('/admin/dashboard')
  } catch (e: any) {
    if (e.response?.status === 401) {
      showToast('账号或密码错误')
    } else if (e.response?.data?.msg) {
      showToast(e.response.data.msg)
    } else {
      showToast('网络异常，请稍后重试')
    }
  } finally {
    loading.value = false
  }
}
</script>

<style scoped>
.admin-login {
  min-height: 100vh;
  display: flex;
  flex-direction: column;
  padding: 60px 24px 40px;
}

.login-header {
  text-align: center;
  margin-bottom: 48px;
}

.login-icon {
  width: 72px;
  height: 72px;
  background: #F5F5F7;
  border-radius: 20px;
  display: flex;
  align-items: center;
  justify-content: center;
  margin: 0 auto 20px;
}

.login-title {
  font-size: 28px;
  font-weight: 700;
  color: #1D1D1F;
  letter-spacing: -0.03em;
  margin-bottom: 6px;
}

.login-subtitle {
  font-size: 15px;
  color: #86868B;
}

.login-form {
  background: white;
  border-radius: 16px;
  padding: 8px 16px;
  box-shadow: 0 1px 3px rgba(0,0,0,0.04);
}

.login-btn {
  width: 100%;
  margin-top: 24px;
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

.login-btn:active {
  transform: scale(0.98);
}
</style>