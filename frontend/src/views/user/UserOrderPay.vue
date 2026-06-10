<template>
  <div class="order-pay">
    <div class="success-animation">
      <div class="check-circle">
        <van-icon name="passed" size="40" color="white" />
      </div>
    </div>

    <h2 class="pay-title">订单提交成功！</h2>
    <p class="pay-subtitle">请扫描下方管理员微信二维码完成支付</p>

    <div class="order-summary">
      <div class="summary-row">
        <span class="label">订单编号</span>
        <span class="value">WP202606150001</span>
      </div>
      <div class="summary-row">
        <span class="label">服务地址</span>
        <span class="value">食宿楼 · 3栋 · 301</span>
      </div>
      <div class="summary-row">
        <span class="label">预约时间</span>
        <span class="value">2026-06-15 10:00 ~ 12:00</span>
      </div>
      <div class="summary-row total">
        <span class="label">支付金额</span>
        <span class="value highlight">¥29.90</span>
      </div>
    </div>

    <div class="qrcode-card">
      <div class="qrcode-header">
        <van-icon name="wechat" size="20" color="#07C160" />
        <span>管理员微信</span>
      </div>
      <div class="qrcode-placeholder">
        <div class="mock-qrcode">
          <svg width="140" height="140" viewBox="0 0 140 140">
            <rect width="140" height="140" fill="white" rx="8"/>
            <g transform="translate(10,10)">
              <rect x="0" y="0" width="120" height="120" fill="white"/>
              <rect x="0" y="0" width="28" height="28" fill="#1D1D1F"/>
              <rect x="92" y="0" width="28" height="28" fill="#1D1D1F"/>
              <rect x="0" y="92" width="28" height="28" fill="#1D1D1F"/>
              <rect x="92" y="92" width="28" height="28" fill="#1D1D1F"/>
              <rect x="36" y="36" width="16" height="16" fill="#1D1D1F"/>
              <rect x="68" y="36" width="16" height="16" fill="#1D1D1F"/>
              <rect x="36" y="68" width="16" height="16" fill="#1D1D1F"/>
              <rect x="52" y="68" width="16" height="16" fill="#1D1D1F"/>
              <rect x="52" y="52" width="16" height="16" fill="#1D1D1F"/>
              <rect x="36" y="0" width="4" height="4" fill="#1D1D1F"/>
              <rect x="48" y="0" width="4" height="4" fill="#1D1D1F"/>
              <rect x="52" y="0" width="4" height="4" fill="#1D1D1F"/>
              <rect x="0" y="36" width="4" height="4" fill="#1D1D1F"/>
              <rect x="0" y="48" width="4" height="4" fill="#1D1D1F"/>
              <rect x="0" y="52" width="4" height="4" fill="#1D1D1F"/>
              <rect x="116" y="36" width="4" height="4" fill="#1D1D1F"/>
              <rect x="116" y="48" width="4" height="4" fill="#1D1D1F"/>
              <rect x="36" y="116" width="4" height="4" fill="#1D1D1F"/>
              <rect x="48" y="116" width="4" height="4" fill="#1D1D1F"/>
              <rect x="52" y="116" width="4" height="4" fill="#1D1D1F"/>
              <rect x="68" y="0" width="4" height="4" fill="#1D1D1F"/>
              <rect x="80" y="0" width="4" height="4" fill="#1D1D1F"/>
              <rect x="84" y="0" width="4" height="4" fill="#1D1D1F"/>
              <rect x="116" y="68" width="4" height="4" fill="#1D1D1F"/>
              <rect x="116" y="80" width="4" height="4" fill="#1D1D1F"/>
              <rect x="68" y="116" width="4" height="4" fill="#1D1D1F"/>
              <rect x="80" y="116" width="4" height="4" fill="#1D1D1F"/>
              <rect x="84" y="116" width="4" height="4" fill="#1D1D1F"/>
              <rect x="92" y="56" width="28" height="8" fill="#1D1D1F"/>
              <rect x="56" y="92" width="8" height="28" fill="#1D1D1F"/>
            </g>
          </svg>
        </div>
      </div>
      <p class="qrcode-tip">长按或截图保存二维码 → 微信扫码添加</p>
    </div>

    <div class="pay-status-area">
      <button class="status-btn" @click="router.push('/user/orders')">
        查看我的订单
      </button>
    </div>
  </div>
</template>

<script setup lang="ts">
import { useRouter } from 'vue-router'
const router = useRouter()
</script>

<style scoped>
.order-pay {
  padding: 24px 16px 100px;
  text-align: center;
}

.success-animation {
  margin-bottom: 16px;
}

.check-circle {
  width: 64px;
  height: 64px;
  background: #34C759;
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  margin: 0 auto;
  box-shadow: 0 4px 14px rgba(52,199,89,0.3);
}

.pay-title {
  font-size: 22px;
  font-weight: 700;
  color: #1D1D1F;
  margin-bottom: 6px;
  letter-spacing: -0.02em;
}

.pay-subtitle {
  font-size: 14px;
  color: #86868B;
  margin-bottom: 24px;
}

.order-summary {
  background: white;
  border-radius: 16px;
  padding: 16px;
  text-align: left;
  box-shadow: 0 1px 3px rgba(0,0,0,0.04);
  margin-bottom: 16px;
}

.summary-row {
  display: flex;
  justify-content: space-between;
  padding: 6px 0;
}

.summary-row .label {
  font-size: 14px;
  color: #86868B;
}

.summary-row .value {
  font-size: 14px;
  color: #1D1D1F;
  font-weight: 500;
}

.summary-row .value.highlight {
  color: #2B95FF;
  font-size: 18px;
  font-weight: 700;
}

.qrcode-card {
  background: white;
  border-radius: 16px;
  padding: 20px;
  box-shadow: 0 1px 3px rgba(0,0,0,0.04);
  margin-bottom: 24px;
}

.qrcode-header {
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 8px;
  margin-bottom: 16px;
  font-size: 15px;
  font-weight: 600;
  color: #1D1D1F;
}

.qrcode-placeholder {
  display: flex;
  justify-content: center;
  margin-bottom: 12px;
}

.mock-qrcode {
  width: 160px;
  height: 160px;
  background: white;
  border-radius: 12px;
  display: flex;
  align-items: center;
  justify-content: center;
  box-shadow: 0 0 0 1px rgba(0,0,0,0.05);
}

.qrcode-tip {
  font-size: 12px;
  color: #C7C7CC;
  margin-top: 4px;
}

.pay-status-area {
  padding: 0;
}

.status-btn {
  width: 100%;
  padding: 14px;
  background: #F5F5F7;
  color: #2B95FF;
  border: none;
  border-radius: 14px;
  font-size: 16px;
  font-weight: 600;
  cursor: pointer;
  transition: all 0.2s;
}

.status-btn:active {
  background: #E8E8ED;
}
</style>