<template>
  <div class="employee-available">
    <!-- 下单悬浮按钮 -->
    <div class="fab" @click="router.push('/user/order/create')">
      <van-icon name="add-o" size="22" color="white" />
    </div>

    <div class="notification-banner">
      <van-icon name="info-o" size="16" color="#2B95FF" />
      <span>新订单实时推送，点击即可抢单</span>
      <van-badge :content="unreadCount" class="notification-badge" />
    </div>

    <!-- 新订单通知 -->
    <van-popup v-model:show="showNotification" position="top" :style="{ height: 'auto' }">
      <div class="notification-item" @click="handleNotificationClick">
        <van-icon name="bell-o" size="24" color="#2B95FF" />
        <div class="notification-content">
          <div class="notification-title">新订单提醒</div>
          <div class="notification-desc">{{ notificationMessage }}</div>
        </div>
        <van-icon name="chevron-right" size="20" color="#C7C7CC" />
      </div>
    </van-popup>

    <div class="order-list">
      <div class="order-card" v-for="order in availableOrders" :key="order.id">
        <div class="order-urgent" v-if="order.urgent">
          <van-icon name="fire-o" size="12" color="#FF3B30" />
          即将开始
        </div>
        <div class="order-main">
          <div class="order-left">
            <div class="order-building-type">{{ order.buildingType }}</div>
            <div class="order-location">{{ order.building }} · {{ order.room }}</div>
            <div class="order-date">
              <van-icon name="clock-o" size="12" color="#86868B" />
              {{ order.date }} {{ order.timeSlot }}
            </div>
            <div class="order-remark" v-if="order.remark">{{ order.remark }}</div>
          </div>
          <div class="order-right">
            <span class="order-status-tag" :class="order.status">{{ order.statusText }}</span>
            <div class="order-price">¥{{ order.amount }}</div>
            <button class="grab-btn" @click="handleGrab(order)">
              抢单
            </button>
          </div>
        </div>
      </div>

      <div v-if="availableOrders.length === 0" class="empty-state">
        <van-icon name="smile-o" size="48" color="#C7C7CC" />
        <p>暂无待抢订单</p>
        <span class="sub-text">休息一下，有订单会实时通知</span>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted, onUnmounted } from 'vue'
import { useRouter } from 'vue-router'
import { showToast, showConfirmDialog, showNotify } from 'vant'
import { post, get } from '../../utils/request'

const router = useRouter()

const availableOrders = ref<any[]>([])
const showNotification = ref(false)
const notificationMessage = ref('')
const unreadCount = ref(0)

let ws: WebSocket | null = null
let reconnectTimer: number | null = null

function connectWebSocket() {
  const token = localStorage.getItem('washpro_token')
  if (!token) return
  const protocol = window.location.protocol === 'https:' ? 'wss:' : 'ws:'
  const backendHost = import.meta.env.VITE_API_BASE_URL || 'http://localhost:8080'
  const backendUrl = new URL(backendHost)
  const wsUrl = `${protocol}//${backendUrl.host}/ws/employee?token=${token}`
  
  ws = new WebSocket(wsUrl)
  
  ws.onopen = () => {
    console.log('WebSocket connected')
    if (reconnectTimer) {
      clearTimeout(reconnectTimer)
      reconnectTimer = null
    }
  }
  
  ws.onmessage = (event) => {
    try {
      const message = JSON.parse(event.data)
      if (message.type === 'NEW_ORDER') {
        handleNewOrder(message)
      }
    } catch (error) {
      console.error('WebSocket message parse error:', error)
    }
  }
  
  ws.onerror = (error) => {
    // WebSocket server not running
  }
  
  ws.onclose = () => {
    // reconnecting
    reconnectTimer = window.setTimeout(() => {
      connectWebSocket()
    }, 5000)
  }
}

function handleNewOrder(message: any) {
  unreadCount.value++
  
  // 解析楼栋信息
  const buildingInfo = message.buildingName?.split(' · ') || ['', '']
  const buildingType = buildingInfo[0] || ''
  const building = buildingInfo[1] || ''
  
  const newOrder = {
    id: message.orderId,
    buildingType,
    building,
    room: message.roomNo,
    date: message.serviceDate,
    timeSlot: `${message.startTime} ~ ${message.endTime}`,
    amount: message.amount?.toString() || '29.90',
    urgent: false,
    remark: '',
    status: 'pending',
    statusText: '待抢',
  }
  
  // 添加到列表开头
  availableOrders.value.unshift(newOrder)
  
  // 显示通知
  notificationMessage.value = `${buildingType} · ${building} ${message.roomNo}\n${message.serviceDate} ${message.startTime}`
  showNotification.value = true
  
  // 3秒后自动关闭通知
  setTimeout(() => {
    showNotification.value = false
  }, 3000)
  
  // 显示Toast提示
  showNotify({
    type: 'primary',
    message: '有新订单！',
    duration: 2000,
  })
}

function handleNotificationClick() {
  showNotification.value = false
  unreadCount.value = 0
}

async function loadOrders() {
  try {
    const res = await get<{ code: number; data: any[] }>('/api/employee/orders/available')
    if (res.data.code === 200) {
      availableOrders.value = res.data.data.map((item: any) => {
        const buildingInfo = item.buildingName?.split(' · ') || ['', '']
        return {
          id: item.id,
          buildingType: buildingInfo[0] || '',
          building: buildingInfo[1] || '',
          room: item.roomNo,
          date: item.serviceDate,
          timeSlot: `${item.startTime} ~ ${item.endTime}`,
          amount: item.amount?.toString() || '29.90',
          urgent: isUrgent(item.serviceDate, item.startTime),
          remark: item.remark || '',
          status: 'pending',
          statusText: '待抢',
        }
      })
    }
  } catch (error) {
    console.log('加载订单失败')
  }
}

function isUrgent(date: string, time: string): boolean {
  const now = new Date()
  const orderTime = new Date(`${date}T${time}`)
  const diffMinutes = (orderTime.getTime() - now.getTime()) / (1000 * 60)
  return diffMinutes < 60
}

async function handleGrab(order: any) {
  try {
    await showConfirmDialog({
      title: '抢单确认',
      message: `${order.buildingType} · ${order.building} ${order.room}\n${order.date} ${order.timeSlot}\n\n确认抢此订单？`,
    })
    
    await post(`/api/employee/order/grab/${order.id}`)
    showToast({
      message: '抢单成功！',
      icon: 'success',
      duration: 1500,
    })
    availableOrders.value = availableOrders.value.filter(o => o.id !== order.id)
  } catch {
    // 用户点击取消，不做任何操作
  }
}

onMounted(() => {
  loadOrders()
  connectWebSocket()
})

onUnmounted(() => {
  if (ws) {
    ws.close()
  }
  if (reconnectTimer) {
    clearTimeout(reconnectTimer)
  }
})
</script>

<style scoped>
.fab {
  position: fixed; bottom: 80px; right: 20px; z-index: 99;
  width: 48px; height: 48px; border-radius: 24px;
  background: #2B95FF; display: flex; align-items: center; justify-content: center;
  box-shadow: 0 4px 16px rgba(43,149,255,0.4); cursor: pointer;
  transition: transform 0.2s cubic-bezier(0.34,1.56,0.64,1);
}
.fab:active { transform: scale(0.9); }

.employee-available {
  padding: 12px 16px 100px;
}

.notification-banner {
  display: flex;
  align-items: center;
  gap: 8px;
  padding: 10px 16px;
  background: rgba(43,149,255,0.08);
  border-radius: 12px;
  font-size: 13px;
  color: #2B95FF;
  font-weight: 500;
  margin-bottom: 12px;
  position: relative;
}

.notification-badge {
  position: absolute;
  top: -6px;
  right: -6px;
}

.notification-item {
  display: flex;
  align-items: center;
  gap: 12px;
  padding: 16px;
  background: white;
}

.notification-content {
  flex: 1;
}

.notification-title {
  font-size: 15px;
  font-weight: 600;
  color: #1D1D1F;
  margin-bottom: 4px;
}

.notification-desc {
  font-size: 13px;
  color: #86868B;
  white-space: pre-wrap;
}

.order-card {
  background: white;
  border-radius: 16px;
  padding: 16px;
  margin-bottom: 12px;
  box-shadow: 0 1px 3px rgba(0,0,0,0.04);
  transition: all 0.2s;
}

.order-card:active {
  transform: scale(0.99);
}

.order-urgent {
  display: flex;
  align-items: center;
  gap: 4px;
  font-size: 11px;
  color: #FF3B30;
  font-weight: 600;
  margin-bottom: 8px;
  padding: 2px 8px;
  background: rgba(255,59,48,0.08);
  border-radius: 6px;
  width: fit-content;
}

.order-main {
  display: flex;
  justify-content: space-between;
  align-items: flex-start;
}

.order-location {
  font-size: 16px;
  font-weight: 600;
  color: #1D1D1F;
  margin-bottom: 4px;
}

.order-building-type {
  font-size: 12px;
  color: #2B95FF;
  font-weight: 500;
  margin-bottom: 2px;
}

.order-date {
  font-size: 13px;
  color: #86868B;
  display: flex;
  align-items: center;
  gap: 4px;
  margin-bottom: 4px;
}

.order-remark {
  font-size: 12px;
  color: #C7C7CC;
  margin-top: 4px;
}

.order-right {
  text-align: right;
  display: flex;
  flex-direction: column;
  align-items: flex-end;
  gap: 8px;
}

.order-price {
  font-size: 20px;
  font-weight: 700;
  color: #2B95FF;
}

.order-status-tag {
  font-size: 11px;
  font-weight: 600;
  padding: 2px 8px;
  border-radius: 8px;
}

.order-status-tag.pending {
  background: rgba(43,149,255,0.08);
  color: #2B95FF;
}

.grab-btn {
  padding: 10px 24px;
  background: linear-gradient(135deg, #2B95FF, #007AFF);
  color: white;
  border: none;
  border-radius: 20px;
  font-size: 14px;
  font-weight: 600;
  cursor: pointer;
  transition: all 0.2s;
  box-shadow: 0 4px 12px rgba(43,149,255,0.3);
}

.grab-btn:active {
  transform: scale(0.95);
  box-shadow: 0 2px 6px rgba(43,149,255,0.2);
}

.empty-state {
  text-align: center;
  padding: 80px 0;
}

.empty-state p {
  font-size: 16px;
  color: #C7C7CC;
  margin: 12px 0 4px;
}

.sub-text {
  font-size: 13px;
  color: #E8E8ED;
}
</style>
