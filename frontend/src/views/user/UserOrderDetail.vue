<template>
  <div class="order-detail">
    <div class="status-timeline">
      <div class="timeline-item" v-for="(step, i) in timeline" :key="i" :class="{ active: step.active }">
        <div class="timeline-dot" :class="{ done: step.done }">
          <van-icon v-if="step.done" name="success" size="12" color="white" />
        </div>
        <div class="timeline-content">
          <div class="tl-title">{{ step.title }}</div>
          <div class="tl-time">{{ step.time }}</div>
        </div>
      </div>
    </div>

    <div class="detail-card">
      <div class="detail-row">
        <span class="label">订单编号</span>
        <span class="value">WP202606150001</span>
      </div>
      <div class="detail-row">
        <span class="label">服务地址</span>
        <span class="value">食宿楼 · 3栋 · 301</span>
      </div>
      <div class="detail-row">
        <span class="label">预约时间</span>
        <span class="value">2026-06-15 10:00 ~ 12:00</span>
      </div>
      <div class="detail-row">
        <span class="label">订单状态</span>
        <span class="value status-text">待支付</span>
      </div>
      <div class="detail-row total">
        <span class="label">支付金额</span>
        <span class="value highlight">¥29.90</span>
      </div>
    </div>

    <button class="back-btn" @click="router.push('/user/orders')">返回列表</button>
  </div>
</template>

<script setup lang="ts">
import { ref } from 'vue'
import { useRouter } from 'vue-router'
const router = useRouter()

const timeline = ref([
  { title: '已下单', time: '2026-06-15 09:30', done: true, active: false },
  { title: '已支付', time: '等待支付中...', done: false, active: false },
  { title: '服务中', time: '', done: false, active: false },
  { title: '已完成', time: '', done: false, active: true },
])
</script>

<style scoped>
.order-detail {
  padding: 16px;
  padding-bottom: 100px;
}

.status-timeline {
  background: white;
  border-radius: 16px;
  padding: 20px;
  margin-bottom: 12px;
  box-shadow: 0 1px 3px rgba(0,0,0,0.04);
}

.timeline-item {
  display: flex;
  gap: 12px;
  position: relative;
  padding-bottom: 20px;
}

.timeline-item:last-child {
  padding-bottom: 0;
}

.timeline-item::before {
  content: '';
  position: absolute;
  left: 11px;
  top: 24px;
  width: 2px;
  height: calc(100% - 24px);
  background: #E8E8ED;
}

.timeline-item:last-child::before {
  display: none;
}

.timeline-item.active::before {
  background: #E8E8ED;
}

.timeline-dot {
  width: 24px;
  height: 24px;
  border-radius: 50%;
  background: #E8E8ED;
  display: flex;
  align-items: center;
  justify-content: center;
  flex-shrink: 0;
  margin-top: 2px;
}

.timeline-dot.done {
  background: #34C759;
}

.timeline-item.active .timeline-dot {
  background: #2B95FF;
  box-shadow: 0 0 0 4px rgba(43,149,255,0.15);
}

.tl-title {
  font-size: 14px;
  font-weight: 500;
  color: #C7C7CC;
}

.timeline-item.active .tl-title,
.timeline-item .timeline-dot.done ~ .timeline-content .tl-title {
  color: #1D1D1F;
  font-weight: 600;
}

.tl-time {
  font-size: 12px;
  color: #C7C7CC;
  margin-top: 2px;
}

.detail-card {
  background: white;
  border-radius: 16px;
  padding: 16px;
  margin-bottom: 24px;
  box-shadow: 0 1px 3px rgba(0,0,0,0.04);
}

.detail-row {
  display: flex;
  justify-content: space-between;
  padding: 8px 0;
}

.detail-row + .detail-row {
  border-top: 1px solid #F5F5F7;
}

.detail-row .label {
  font-size: 14px;
  color: #86868B;
}

.detail-row .value {
  font-size: 14px;
  color: #1D1D1F;
  font-weight: 500;
}

.detail-row .value.highlight {
  color: #2B95FF;
  font-size: 18px;
  font-weight: 700;
}

.detail-row .value.status-text {
  color: #FF9500;
}

.back-btn {
  width: 100%;
  padding: 14px;
  background: #F5F5F7;
  color: #2B95FF;
  border: none;
  border-radius: 14px;
  font-size: 16px;
  font-weight: 600;
  cursor: pointer;
}
</style>