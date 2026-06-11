<template>
  <div class="user-orders">
    <div class="order-list">
      <div class="order-card" v-for="order in orders" :key="order.id">
        <div class="order-top">
          <span class="order-no">订单 {{ order.orderNo }}</span>
          <span class="order-status" :class="order.statusClass">{{ order.statusText }}</span>
        </div>
        <div class="order-info">
          <div class="info-row">
            <van-icon name="location-o" size="14" color="#86868B" />
            <span>{{ order.buildingName }} · {{ order.roomNo }}</span>
          </div>
          <div class="info-row">
            <van-icon name="clock-o" size="14" color="#86868B" />
            <span>{{ order.serviceDate }} {{ order.startTime }} ~ {{ order.endTime }}</span>
          </div>
        </div>
        <div class="order-bottom">
          <span class="order-amount">¥{{ order.amount }}</span>
          <button v-if="order.status === 0" class="pay-btn" @click="goPay(order.id)">去支付</button>
          <button v-else class="detail-btn" @click="goDetail(order.id)">查看详情</button>
        </div>
      </div>

      <div v-if="loading" class="loading-state">
        <van-loading size="24px" />
      </div>

      <div v-if="!loading && orders.length === 0" class="empty-state">
        <van-icon name="records-o" size="48" color="#C7C7CC" />
        <p>暂无订单</p>
        <button class="order-now-btn" @click="router.push('/user/order/create')">去预约</button>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted, computed } from 'vue'
import { useRouter } from 'vue-router'
import { showToast, showLoadingToast, closeToast } from 'vant'
import { get } from '../../utils/request'

const router = useRouter()
const orders = ref<any[]>([])
const loading = ref(true)

const statusMap: Record<number, { text: string; class: string }> = {
  0: { text: '未支付', class: 'status-unpaid' },
  1: { text: '待服务', class: 'status-paid' },
  2: { text: '服务中', class: 'status-progress' },
  3: { text: '已完成', class: 'status-completed' },
  4: { text: '已取消', class: 'status-cancelled' },
}

interface Order {
  id: number
  orderNo: string
  buildingName: string
  roomNo: string
  serviceDate: string
  startTime: string
  endTime: string
  amount: string | number
  status: number
  statusText: string
  statusClass: string
}

async function loadOrders() {
  loading.value = true
  showLoadingToast({ message: '加载中...' })
  try {
    const res = await get<{ code: number; data: any[] }>('/api/user/order/list')
    if (res.data.code === 200) {
      orders.value = res.data.data.map((item: any) => ({
        id: item.id,
        orderNo: item.orderNo,
        buildingName: item.buildingName,
        roomNo: item.roomNo,
        serviceDate: item.serviceDate,
        startTime: item.startTime,
        endTime: item.endTime,
        amount: item.amount,
        status: item.status,
        statusText: statusMap[item.status]?.text || '未知',
        statusClass: statusMap[item.status]?.class || 'status-default',
      }))
    }
  } catch (error) {
    showToast('加载订单失败')
  } finally {
    loading.value = false
    closeToast()
  }
}

function goPay(orderId: number) {
  router.push(`/user/order/pay?id=${orderId}`)
}

function goDetail(orderId: number) {
  router.push(`/user/order/detail?id=${orderId}`)
}

onMounted(() => {
  loadOrders()
})
</script>

<style scoped>
.user-orders {
  padding: 12px 16px 100px;
}

.loading-state {
  text-align: center;
  padding: 40px 0;
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

.status-progress {
  background: rgba(142,142,147,0.1);
  color: #8E8E93;
}

.status-cancelled {
  background: rgba(142,142,147,0.1);
  color: #8E8E93;
}

.status-default {
  background: rgba(142,142,147,0.1);
  color: #8E8E93;
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