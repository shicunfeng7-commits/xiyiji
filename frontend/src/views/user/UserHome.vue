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
        立即预约
        <svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2.5" stroke-linecap="round"><line x1="5" y1="12" x2="19" y2="12"/><polyline points="12 5 19 12 12 19"/></svg>
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
</template>

<script setup lang="ts">
import { useRouter } from 'vue-router'

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
.user-home { padding: 0 0 100px; background: var(--color-bg-deep); }

.hero-section {
  background: linear-gradient(180deg, #F2F2F7 0%, #FAFAFA 40%, rgba(0,122,255,0.03) 100%);
  margin: 0; padding: 52px 24px 44px; text-align: center; position: relative; overflow: hidden;
}
.hero-section::before {
  content: ''; position: absolute; top: -80%; left: -80%; width: 260%; height: 260%;
  background: radial-gradient(ellipse at 40% 30%, rgba(0,122,255,0.06) 0%, transparent 60%);
}

.hero-icon {
  width: 72px; height: 72px;
  background: rgba(255,255,255,0.04);
  backdrop-filter: blur(10px); -webkit-backdrop-filter: blur(10px);
  border: 1px solid rgba(255,255,255,0.08);
  border-radius: 20px; display: flex; align-items: center; justify-content: center;
  margin: 0 auto 20px;
}
.hero-title { font-size: 32px; font-weight: 800; color: var(--color-text-primary); letter-spacing: -0.04em; margin-bottom: 8px; }
.hero-subtitle { font-size: 15px; color: var(--color-text-secondary); margin-bottom: 28px; }

.hero-btn {
  display: inline-flex; align-items: center; gap: 8px;
  background: var(--color-accent); color: white; border: none;
  padding: 14px 36px; border-radius: 25px; font-size: 17px; font-weight: 600;
  cursor: pointer; transition: all 0.3s;
  box-shadow: 0 0 24px rgba(59,130,246,0.3);
}
.hero-btn:active { transform: scale(0.97); }

.section-title { font-size: 18px; font-weight: 700; color: var(--color-text-primary); letter-spacing: -0.02em; padding: 24px 18px 12px; }

.features-grid { display: grid; grid-template-columns: 1fr 1fr; gap: 10px; padding: 0 14px; }

.feature-card {
  background: rgba(255,255,255,0.04); backdrop-filter: blur(12px); -webkit-backdrop-filter: blur(12px);
  border: 1px solid rgba(255,255,255,0.06); border-radius: 16px; padding: 16px;
  display: flex; gap: 12px; align-items: flex-start; transition: all 0.2s;
}
.feature-card:active { background: rgba(255,255,255,0.07); transform: scale(0.98); }

.feature-icon { width: 40px; height: 40px; border-radius: 12px; display: flex; align-items: center; justify-content: center; flex-shrink: 0; }
.feature-name { font-size: 14px; font-weight: 600; color: var(--color-text-primary); margin-bottom: 2px; }
.feature-desc { font-size: 12px; color: var(--color-text-secondary); }

.steps-list { padding: 0 14px; }

.step-item {
  display: flex; gap: 16px; padding: 16px;
  background: rgba(255,255,255,0.04); backdrop-filter: blur(12px); -webkit-backdrop-filter: blur(12px);
  border: 1px solid rgba(255,255,255,0.06); border-radius: 16px; margin-bottom: 10px;
}
.step-number { width: 32px; height: 32px; background: var(--color-accent); color: white; border-radius: 50%; display: flex; align-items: center; justify-content: center; font-size: 14px; font-weight: 700; flex-shrink: 0; }
.step-title { font-size: 15px; font-weight: 600; color: var(--color-text-primary); margin-bottom: 2px; }
.step-desc { font-size: 13px; color: var(--color-text-secondary); }

.bottom-cta { padding: 24px 14px; }
.cta-btn {
  width: 100%; padding: 16px; background: var(--color-accent); color: white; border: none;
  border-radius: 16px; font-size: 17px; font-weight: 600; cursor: pointer; transition: all 0.2s;
  box-shadow: 0 0 24px rgba(59,130,246,0.3);
}
.cta-btn:active { transform: scale(0.98); }
</style>