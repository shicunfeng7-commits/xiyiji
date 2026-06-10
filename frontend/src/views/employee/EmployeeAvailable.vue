<template>
  <div class="employee-available">
    <div class="notification-banner">
      <van-icon name="info-o" size="16" color="#2B95FF" />
      <span>新订单实时推送，点击即可抢单</span>
    </div>

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
import { ref } from 'vue'
import { showToast, showConfirmDialog } from 'vant'
import { post } from '../../utils/request'

const availableOrders = ref([
  {
    id: 1,
    buildingType: '食宿楼',
    building: '3栋', room: '301',
    date: '2026-06-15',
    timeSlot: '10:00 ~ 12:00',
    amount: '29.90',
    urgent: true,
    remark: '用户备注：惠而浦洗衣机',
    status: 'pending',
    statusText: '待抢',
  },
  {
    id: 2,
    buildingType: '教师公寓',
    building: 'A栋', room: '208',
    date: '2026-06-15',
    timeSlot: '14:00 ~ 16:00',
    amount: '29.90',
    urgent: false,
    remark: '',
    status: 'pending',
    statusText: '待抢',
  },
  {
    id: 3,
    buildingType: '学生宿舍',
    building: '4栋', room: '506',
    date: '2026-06-16',
    timeSlot: '09:00 ~ 11:00',
    amount: '29.90',
    urgent: false,
    remark: '需要专业除垢',
    status: 'pending',
    statusText: '待抢',
  },
])

async function handleGrab(order: any) {
  try {
    await showConfirmDialog({
      title: '抢单确认',
      message: `${order.buildingType} · ${order.building} ${order.room}\n${order.date} ${order.timeSlot}\n\n确认抢此订单？`,
    })
    // 用户点击确认
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
</script>

<style scoped>
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