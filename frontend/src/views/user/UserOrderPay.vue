<template>
  <div class="order-pay">
    <!-- 加载中 -->
    <div v-if="!order" class="loading-container">
      <van-loading size="24px" color="#2B95FF" vertical>加载中...</van-loading>
    </div>

    <template v-else>
    <div class="success-hero">
      <div class="check-circle">
        <van-icon name="passed" size="36" color="white" />
      </div>
      <h2 class="pay-title">下单成功</h2>
      <p class="pay-subtitle">添加管理员微信完成支付</p>
    </div>

    <!-- 订单号（核心） -->
    <div class="order-no-card">
      <p class="no-label">订单号</p>
      <h1 class="no-value">{{ order?.orderNo || '—' }}</h1>
      <p class="no-hint">请将此订单号发送给管理员核对</p>
      <button class="copy-btn" @click="copyOrderNo">
        <van-icon name="copy-o" size="16" />
        复制订单号
      </button>
    </div>

    <!-- 订单详情 -->
    <div class="info-card" v-if="order">
      <div class="info-row">
        <span class="info-label">服务地址</span>
        <span class="info-value">{{ order.buildingName }} · {{ order.roomNo }}</span>
      </div>
      <div class="info-row">
        <span class="info-label">预约时间</span>
        <span class="info-value">{{ order.serviceDate }} {{ order.startTime }} ~ {{ order.endTime }}</span>
      </div>
      <div class="info-row total">
        <span class="info-label">支付金额</span>
        <span class="info-value price">¥{{ order.amount }}</span>
      </div>
    </div>

    <!-- 二维码 -->
    <div class="qrcode-card">
      <div class="qrcode-header">
        <van-icon name="wechat" size="20" color="#07C160" />
        <span>管理员微信收款码</span>
      </div>
      <div class="qrcode-placeholder">
        <svg width="120" height="120" viewBox="0 0 120 120">
          <rect width="120" height="120" fill="white" rx="8"/>
          <g transform="translate(8,8)">
            <rect x="0" y="0" width="44" height="44" rx="4" fill="none" stroke="#1D1D1F" stroke-width="4"/>
            <rect x="8" y="8" width="12" height="12" rx="2" fill="#1D1D1F"/>
            <rect x="24" y="8" width="12" height="12" rx="2" fill="#1D1D1F"/>
            <rect x="8" y="24" width="12" height="12" rx="2" fill="#1D1D1F"/>
            <rect x="24" y="24" width="12" height="12" rx="2" fill="#1D1D1F"/>
            <rect x="60" y="0" width="44" height="44" rx="4" fill="none" stroke="#1D1D1F" stroke-width="4"/>
            <rect x="68" y="8" width="12" height="12" rx="2" fill="#1D1D1F"/>
            <rect x="84" y="8" width="12" height="12" rx="2" fill="#1D1D1F"/>
            <rect x="68" y="24" width="12" height="12" rx="2" fill="#1D1D1F"/>
            <rect x="84" y="24" width="12" height="12" rx="2" fill="#1D1D1F"/>
            <rect x="0" y="60" width="44" height="44" rx="4" fill="none" stroke="#1D1D1F" stroke-width="4"/>
            <rect x="8" y="68" width="12" height="12" rx="2" fill="#1D1D1F"/>
            <rect x="24" y="68" width="12" height="12" rx="2" fill="#1D1D1F"/>
            <rect x="8" y="84" width="12" height="12" rx="2" fill="#1D1D1F"/>
            <rect x="24" y="84" width="12" height="12" rx="2" fill="#1D1D1F"/>
            <rect x="60" y="60" width="44" height="44" rx="4" fill="#1D1D1F" opacity="0.1"/>
          </g>
        </svg>
      </div>
    </div>

    <button class="view-btn" @click="router.push('/user/orders')">查看我的订单</button>
    </template>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { showToast } from 'vant'
import { get } from '../../utils/request'

const route = useRoute()
const router = useRouter()
const order = ref<any>(null)

onMounted(async () => {
  const orderId = route.query.id
  if (orderId) {
    try {
      const res = await get<{ code: number; data: any }>(`/api/user/order/detail/${orderId}`)
      if (res.data.code === 200) {
        order.value = res.data.data
      } else {
        showToast('订单不存在')
      }
    } catch {
      showToast('加载订单失败')
    }
  }
})

function copyOrderNo() {
  const no = order.value?.orderNo
  if (!no) return
  navigator.clipboard.writeText(no).then(() => {
    showToast('已复制订单号')
  }).catch(() => {
    showToast(no)
  })
}
</script>

<style scoped>
.order-pay { padding: 24px 16px 100px; text-align: center; min-height: 100vh; background: #F5F5F7; }

.loading-container {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  padding: 120px 0;
}

.success-hero { margin-bottom: 24px; }

.check-circle {
  width: 72px; height: 72px; border-radius: 36px;
  background: linear-gradient(135deg, #34C759, #30D158);
  display: flex; align-items: center; justify-content: center;
  margin: 0 auto 16px; box-shadow: 0 8px 24px rgba(52,199,89,0.3);
  animation: popIn 0.4s cubic-bezier(0.34,1.56,0.64,1);
}
@keyframes popIn {
  from { transform: scale(0); opacity: 0; }
  to { transform: scale(1); opacity: 1; }
}

.pay-title { font-size: 22px; font-weight: 800; color: #1D1D1F; margin: 0 0 4px; }
.pay-subtitle { font-size: 14px; color: #86868B; margin: 0; }

.order-no-card {
  background: white; border-radius: 20px; padding: 24px; margin-bottom: 16px;
  box-shadow: 0 2px 12px rgba(0,0,0,0.04);
}
.no-label { font-size: 12px; color: #86868B; margin: 0 0 4px; font-weight: 600; letter-spacing: 0.05em; }
.no-value { font-size: 24px; font-weight: 800; color: #2B95FF; margin: 0 0 8px; letter-spacing: 0.02em; font-family: 'SF Mono', 'Menlo', monospace; }
.no-hint { font-size: 12px; color: #C7C7CC; margin: 0 0 14px; }

.copy-btn {
  display: inline-flex; align-items: center; gap: 6px;
  padding: 10px 24px; border: 1.5px solid #2B95FF; border-radius: 24px;
  background: white; color: #2B95FF; font-size: 14px; font-weight: 600;
  cursor: pointer; transition: all 0.2s;
}
.copy-btn:active { background: #F0F7FF; transform: scale(0.97); }

.info-card {
  background: white; border-radius: 20px; padding: 20px; margin-bottom: 16px;
  text-align: left; box-shadow: 0 2px 12px rgba(0,0,0,0.04);
}
.info-row {
  display: flex; justify-content: space-between; align-items: center;
  padding: 10px 0; border-bottom: 1px solid #F5F5F7;
}
.info-row:last-child { border-bottom: none; }
.info-label { font-size: 14px; color: #86868B; }
.info-value { font-size: 14px; color: #1D1D1F; font-weight: 500; }
.price { font-size: 20px; font-weight: 800; color: #1D1D1F; letter-spacing: -0.02em; }

.qrcode-card {
  background: white; border-radius: 20px; padding: 20px; margin-bottom: 20px;
  box-shadow: 0 2px 12px rgba(0,0,0,0.04);
}
.qrcode-header { display: flex; align-items: center; justify-content: center; gap: 8px; font-size: 14px; font-weight: 600; color: #1D1D1F; margin-bottom: 16px; }
.qrcode-placeholder { display: flex; justify-content: center; }

.view-btn {
  width: 100%; padding: 16px; background: #1D1D1F; color: white; border: none;
  border-radius: 16px; font-size: 16px; font-weight: 600; cursor: pointer;
  transition: all 0.2s;
}
.view-btn:active { transform: scale(0.98); background: #333; }
</style>
