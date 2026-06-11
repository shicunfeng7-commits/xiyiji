<template>
  <div class="user-home">
    <!-- Hero Section -->
    <div class="hero-section">
      <div class="hero-icon">
        <svg width="48" height="48" viewBox="0 0 100 100" fill="none">
          <rect x="15" y="30" width="70" height="55" rx="10" fill="white" opacity="0.95"/>
          <rect x="22" y="24" width="56" height="12" rx="6" fill="white" opacity="0.7"/>
          <circle cx="50" cy="56" r="14" fill="#2B95FF" opacity="0.15"/>
          <circle cx="50" cy="56" r="7" fill="#2B95FF" opacity="0.35"/>
          <path d="M44 56L48 60L56 52" stroke="#2B95FF" stroke-width="3" stroke-linecap="round" stroke-linejoin="round"/>
        </svg>
      </div>
      <h1 class="hero-title">洗衣机专业清洗</h1>
      <p class="hero-subtitle">学校宿舍 · 上门服务 · 专业消毒</p>
      <button class="hero-btn" @click="goCreate">
        <span>立即预约</span>
        <van-icon name="arrow" size="16" color="white" />
      </button>
    </div>

    <!-- Features -->
    <div class="features-section">
      <div class="section-title">为什么选择我们</div>
      <div class="features-grid">
        <div class="feature-card" v-for="f in features" :key="f.title">
          <div class="feature-icon" :style="{ background: f.bg }">
            <van-icon :name="f.icon" size="22" :color="f.color" />
          </div>
          <div class="feature-info">
            <div class="feature-name">{{ f.title }}</div>
            <div class="feature-desc">{{ f.desc }}</div>
          </div>
        </div>
      </div>
    </div>

    <!-- Steps -->
    <div class="steps-section">
      <div class="section-title">服务流程</div>
      <div class="steps-list">
        <div class="step-item" v-for="(step, i) in steps" :key="i">
          <div class="step-number">{{ i + 1 }}</div>
          <div class="step-content">
            <div class="step-title">{{ step.title }}</div>
            <div class="step-desc">{{ step.desc }}</div>
          </div>
        </div>
      </div>
    </div>

    <!-- Bottom CTA -->
    <div class="bottom-cta">
      <button class="cta-btn" @click="goCreate">立即预约清洗</button>
    </div>
  </div>
  
  <BottomNav />
</template>

<script setup lang="ts">
import { useRouter } from 'vue-router'
import BottomNav from '../../components/BottomNav.vue'

const router = useRouter()

const features = [
  { title: '专业清洗', desc: '高温消毒除垢', icon: 'certificate', color: '#2B95FF', bg: 'rgba(43,149,255,0.1)' },
  { title: '上门服务', desc: '宿舍楼内上门取送', icon: 'logistics', color: '#34C759', bg: 'rgba(52,199,89,0.1)' },
  { title: '实惠价格', desc: '学生专享优惠价', icon: 'gold-coin-o', color: '#FF9500', bg: 'rgba(255,149,0,0.1)' },
  { title: '安心保障', desc: '不满意免费重洗', icon: 'shield-o', color: '#FF3B30', bg: 'rgba(255,59,48,0.1)' },
]

const steps = [
  { title: '预约下单', desc: '填写楼栋房间和上门时间' },
  { title: '扫码支付', desc: '添加管理员微信完成支付' },
  { title: '等待服务', desc: '管理员确认后等待员工接单' },
  { title: '上门清洗', desc: '员工上门完成洗衣机清洗' },
]

function goCreate() {
  router.push('/user/order/create')
}
</script>

<style scoped>
.user-home {
  padding: 0 0 100px;
}

.hero-section {
  background: linear-gradient(135deg, #2B95FF 0%, #007AFF 50%, #5856D6 100%);
  margin: 0;
  padding: 48px 24px 40px;
  text-align: center;
  position: relative;
  overflow: hidden;
}

.hero-section::before {
  content: '';
  position: absolute;
  top: -50%;
  left: -50%;
  width: 200%;
  height: 200%;
  background: radial-gradient(circle at 30% 40%, rgba(255,255,255,0.1) 0%, transparent 60%);
}

.hero-icon {
  width: 72px;
  height: 72px;
  background: rgba(255,255,255,0.15);
  border-radius: 20px;
  display: flex;
  align-items: center;
  justify-content: center;
  margin: 0 auto 20px;
  backdrop-filter: blur(10px);
}

.hero-title {
  font-size: 32px;
  font-weight: 800;
  color: white;
  letter-spacing: -0.03em;
  margin-bottom: 8px;
}

.hero-subtitle {
  font-size: 15px;
  color: rgba(255,255,255,0.75);
  font-weight: 400;
  margin-bottom: 28px;
  letter-spacing: 0.02em;
}

.hero-btn {
  display: inline-flex;
  align-items: center;
  gap: 8px;
  background: white;
  color: #2B95FF;
  border: none;
  padding: 14px 36px;
  border-radius: 25px;
  font-size: 17px;
  font-weight: 600;
  cursor: pointer;
  transition: all 0.3s;
  box-shadow: 0 4px 20px rgba(0,0,0,0.15);
}

.hero-btn:active {
  transform: scale(0.97);
  box-shadow: 0 2px 10px rgba(0,0,0,0.1);
}

.section-title {
  font-size: 20px;
  font-weight: 700;
  color: #1D1D1F;
  letter-spacing: -0.02em;
  padding: 24px 20px 16px;
}

.features-grid {
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: 12px;
  padding: 0 16px;
}

.feature-card {
  background: white;
  border-radius: 16px;
  padding: 16px;
  display: flex;
  gap: 12px;
  align-items: flex-start;
  box-shadow: 0 1px 3px rgba(0,0,0,0.04);
  transition: all 0.2s;
}

.feature-card:active {
  transform: scale(0.98);
}

.feature-icon {
  width: 40px;
  height: 40px;
  border-radius: 12px;
  display: flex;
  align-items: center;
  justify-content: center;
  flex-shrink: 0;
}

.feature-name {
  font-size: 14px;
  font-weight: 600;
  color: #1D1D1F;
  margin-bottom: 2px;
}

.feature-desc {
  font-size: 12px;
  color: #86868B;
}

.steps-list {
  padding: 0 16px;
}

.step-item {
  display: flex;
  gap: 16px;
  padding: 16px;
  background: white;
  border-radius: 16px;
  margin-bottom: 10px;
  box-shadow: 0 1px 3px rgba(0,0,0,0.04);
}

.step-number {
  width: 32px;
  height: 32px;
  background: #2B95FF;
  color: white;
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 14px;
  font-weight: 700;
  flex-shrink: 0;
  margin-top: 2px;
}

.step-title {
  font-size: 15px;
  font-weight: 600;
  color: #1D1D1F;
  margin-bottom: 2px;
}

.step-desc {
  font-size: 13px;
  color: #86868B;
}

.bottom-cta {
  padding: 24px 16px;
}

.cta-btn {
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

.cta-btn:active {
  transform: scale(0.98);
  box-shadow: 0 2px 8px rgba(43,149,255,0.2);
}
</style>