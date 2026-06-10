<template>
  <div class="employee-myorders">
    <div class="order-list">
      <div class="order-card" v-for="order in myOrders" :key="order.id">
        <div class="order-header">
          <span class="order-no">{{ order.no }}</span>
          <span class="order-status" :class="order.status">{{ order.statusText }}</span>
        </div>
        <div class="order-body">
          <div class="info-line">{{ order.building }} · {{ order.room }}</div>
          <div class="info-line time">{{ order.time }}</div>
        </div>
        <div class="order-footer">
          <span class="order-amount">¥{{ order.amount }}</span>
          <div class="order-actions">
            <button v-if="order.status === 'in_progress'" class="action-btn complete-btn" @click="completeOrder(order)">
              完成服务
            </button>
            <span v-else class="done-badge">已完成</span>
          </div>
        </div>
      </div>

      <div v-if="myOrders.length === 0" class="empty-state">
        <van-icon name="records-o" size="48" color="#C7C7CC" />
        <p>暂无已接订单</p>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref } from 'vue'
import { showToast } from 'vant'

const myOrders = ref([
  {
    id: 1, no: 'WP202606140003',
    building: '教师公寓 · A栋', room: '208',
    time: '2026-06-14 09:00 ~ 11:00',
    amount: '29.90',
    status: 'in_progress',
    statusText: '服务中',
  },
  {
    id: 2, no: 'WP202606120005',
    building: '食宿楼 · 2栋', room: '612',
    time: '2026-06-12 11:00 ~ 13:00',
    amount: '29.90',
    status: 'completed',
    statusText: '已完成',
  },
])

function completeOrder(order: any) {
  order.status = 'completed'
  order.statusText = '已完成'
  showToast('已完成服务')
}
</script>

<style scoped>
.employee-myorders {
  padding: 12px 16px 100px;
}

.order-card {
  background: white;
  border-radius: 16px;
  padding: 16px;
  margin-bottom: 12px;
  box-shadow: 0 1px 3px rgba(0,0,0,0.04);
}

.order-header {
  display: flex;
  justify-content: space-between;
  margin-bottom: 10px;
}

.order-no {
  font-size: 13px;
  font-weight: 500;
  color: #86868B;
}

.order-status {
  font-size: 12px;
  font-weight: 600;
  padding: 3px 10px;
  border-radius: 10px;
}

.order-status.in_progress {
  background: rgba(255,149,0,0.1);
  color: #FF9500;
}

.order-status.completed {
  background: rgba(52,199,89,0.1);
  color: #34C759;
}

.order-body {
  padding-bottom: 12px;
  border-bottom: 1px solid #F5F5F7;
}

.info-line {
  font-size: 14px;
  color: #1D1D1F;
  padding: 2px 0;
}

.info-line.time {
  color: #86868B;
  font-size: 13px;
}

.order-footer {
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

.order-actions {
  display: flex;
  gap: 8px;
}

.action-btn {
  padding: 8px 18px;
  border: none;
  border-radius: 18px;
  font-size: 13px;
  font-weight: 600;
  cursor: pointer;
  transition: all 0.2s;
}

.complete-btn {
  background: #34C759;
  color: white;
}

.complete-btn:active {
  transform: scale(0.95);
}

.done-badge {
  font-size: 12px;
  color: #34C759;
  font-weight: 600;
  padding: 4px 12px;
  background: rgba(52,199,89,0.1);
  border-radius: 10px;
}

.empty-state {
  text-align: center;
  padding: 80px 0;
}

.empty-state p {
  font-size: 15px;
  color: #C7C7CC;
  margin-top: 12px;
}
</style>