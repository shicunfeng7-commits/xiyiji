<template>
  <div class="user-orders">
    <div class="order-list">
      <div class="order-card" v-for="order in orders" :key="order.id">
        <div class="order-top">
          <span class="order-no">订单 {{ order.no }}</span>
          <span class="order-status" :class="order.statusClass">{{ order.statusText }}</span>
        </div>
        <div class="order-info">
          <div class="info-row">
            <van-icon name="location-o" size="14" color="#86868B" />
            <span>{{ order.building }} · {{ order.room }}</span>
          </div>
          <div class="info-row">
            <van-icon name="clock-o" size="14" color="#86868B" />
            <span>{{ order.time }}</span>
          </div>
        </div>
        <div class="order-bottom">
          <span class="order-amount">¥{{ order.amount }}</span>
          <button v-if="order.status === 'unpaid'" class="pay-btn" @click="router.push('/user/order/pay')">去支付</button>
          <button v-else class="detail-btn" @click="router.push('/user/order/detail')">查看详情</button>
        </div>
      </div>

      <div v-if="orders.length === 0" class="empty-state">
        <van-icon name="records-o" size="48" color="#C7C7CC" />
        <p>暂无订单</p>
        <button class="order-now-btn" @click="router.push('/user/order/create')">去预约</button>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref } from 'vue'
import { useRouter } from 'vue-router'

const router = useRouter()

const orders = ref([
  {
    id: 1,
    no: 'WP202606150001',
    building: '食宿楼 · 3栋',
    room: '301',
    time: '2026-06-15 10:00 ~ 12:00',
    amount: '29.90',
    status: 'unpaid',
    statusText: '未支付',
    statusClass: 'status-unpaid',
  },
  {
    id: 2,
    no: 'WP202606140002',
    building: '学生宿舍 · 1栋',
    room: '506',
    time: '2026-06-14 14:00 ~ 16:00',
    amount: '29.90',
    status: 'paid',
    statusText: '待服务',
    statusClass: 'status-paid',
  },
  {
    id: 3,
    no: 'WP202606120003',
    building: '教师公寓 · A栋',
    room: '208',
    time: '2026-06-12 09:00 ~ 11:00',
    amount: '29.90',
    status: 'completed',
    statusText: '已完成',
    statusClass: 'status-completed',
  },
])
</script>

<style scoped>
.user-orders {
  padding: 12px 16px 100px;
}

.order-card {
  background: white;
  border-radius: 16px;
  padding: 16px;
  margin-bottom: 12px;
  box-shadow: 0 1px 3px rgba(0,0,0,0.04);
}

.order-top {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 12px;
}

.order-no {
  font-size: 13px;
  color: #86868B;
  font-weight: 500;
}

.order-status {
  font-size: 12px;
  font-weight: 600;
  padding: 3px 10px;
  border-radius: 10px;
}

.status-unpaid {
  background: rgba(255,149,0,0.1);
  color: #FF9500;
}

.status-paid {
  background: rgba(43,149,255,0.1);
  color: #2B95FF;
}

.status-completed {
  background: rgba(52,199,89,0.1);
  color: #34C759;
}

.order-info {
  padding-bottom: 12px;
  border-bottom: 1px solid #F5F5F7;
}

.info-row {
  display: flex;
  align-items: center;
  gap: 8px;
  font-size: 14px;
  color: #1D1D1F;
  padding: 3px 0;
}

.order-bottom {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding-top: 12px;
}

.order-amount {
  font-size: 18px;
  font-weight: 700;
  color: #1D1D1F;
}

.pay-btn {
  padding: 8px 20px;
  background: #2B95FF;
  color: white;
  border: none;
  border-radius: 20px;
  font-size: 13px;
  font-weight: 600;
  cursor: pointer;
  transition: all 0.2s;
}

.pay-btn:active {
  transform: scale(0.95);
}

.detail-btn {
  padding: 8px 20px;
  background: #F5F5F7;
  color: #2B95FF;
  border: none;
  border-radius: 20px;
  font-size: 13px;
  font-weight: 600;
  cursor: pointer;
}

.empty-state {
  text-align: center;
  padding: 60px 0;
}

.empty-state p {
  font-size: 15px;
  color: #C7C7CC;
  margin: 12px 0 20px;
}

.order-now-btn {
  padding: 12px 32px;
  background: #2B95FF;
  color: white;
  border: none;
  border-radius: 22px;
  font-size: 15px;
  font-weight: 600;
  cursor: pointer;
}
</style>