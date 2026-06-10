<template>
  <div class="employee-login">
    <div class="login-header">
      <div class="login-icon">
        <van-icon name="contact" size="32" color="#2B95FF" />
      </div>
      <h1 class="login-title">员工登录</h1>
      <p class="login-subtitle">WashPro 员工端</p>
    </div>

    <div class="login-form">
      <van-field
        v-model="username"
        label="账号"
        placeholder="请输入员工账号"
        clearable
        left-icon="contact"
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

const router = useRouter()
const username = ref('')
const password = ref('')

function handleLogin() {
  if (!username.value || !password.value) {
    showToast('请输入账号和密码')
    return
  }
  router.push('/employee/available')
}
</script>

<style scoped>
.employee-login {
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
  background: rgba(43,149,255,0.1);
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