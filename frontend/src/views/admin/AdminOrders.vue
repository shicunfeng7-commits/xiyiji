<template>
  <div class="admin-orders">
    <div class="status-tabs">
      <div
        v-for="tab in statusTabs"
        :key="tab.key"
        class="status-tab"
        :class="{ active: currentTab === tab.key }"
        @click="currentTab = tab.key"
      >
        {{ tab.label }}
        <span class="tab-count">{{ tab.count }}</span>
      </div>
    </div>

    <div class="sort-bar">
      <van-dropdown-menu>
        <van-dropdown-item :value="sortType" :options="sortOptions" @change="handleSortChange" />
      </van-dropdown-menu>
      <van-button size="small" type="default" @click="toggleSortOrder" class="sort-btn">
        {{ sortOrder === 'desc' ? '↓' : '↑' }}
      </van-button>
    </div>

    <div class="order-list">
      <div class="order-card" v-for="order in filteredOrders" :key="order.id">
        <div class="order-header">
          <span class="order-no">{{ order.no }}</span>
          <span class="order-user">{{ order.user }}</span>
        </div>
        <div class="order-body">
          <div class="info-line">{{ order.building }} · {{ order.room }}</div>
          <div class="info-line time">{{ order.time }}</div>
        </div>
        <div class="order-footer">
          <span class="order-amount">¥{{ order.amount }}</span>
          <div class="order-actions">
            <button
              v-if="order.status === 'unpaid'"
              class="action-btn confirm-btn"
              @click="confirmPay(order)"
            >
              确认已支付
            </button>
            <button
              v-if="order.status === 'paid' && !order.employeeId"
              class="action-btn revert-btn"
              @click="revertPay(order)"
            >
              回退支付
            </button>
            <button
              v-if="order.status === 'paid' && order.employeeId"
              class="action-btn revert-btn disabled"
              disabled
            >
              回退支付
            </button>
            <span v-else-if="order.status !== 'unpaid' && order.status !== 'paid'" class="status-badge" :class="order.status">
              {{ order.statusText }}
            </span>
          </div>
        </div>
      </div>

      <div v-if="filteredOrders.length === 0" class="empty-state">
        <van-icon name="records-o" size="48" color="#C7C7CC" />
        <p>暂无订单</p>
      </div>
    </div>
    
    <AdminNav />
  </div>
</template>

<script setup lang="ts">
import { ref, computed, onMounted } from 'vue'
import { showDialog, showToast, showLoadingToast, closeToast } from 'vant'
import { post, get } from '../../utils/request'
import AdminNav from '../../components/AdminNav.vue'

const currentTab = ref('all')
const sortType = ref('createTime')
const sortOrder = ref('desc')
const loading = ref(false)

const sortOptions = [
  { text: '按创建时间', value: 'createTime' },
  { text: '按服务时间', value: 'serviceTime' },
  { text: '按状态', value: 'status' },
]

const statusTabs = ref([
  { key: 'all', label: '全部', count: 0 },
  { key: 'unpaid', label: '未支付', count: 0 },
  { key: 'paid', label: '待服务', count: 0 },
  { key: 'in_progress', label: '服务中', count: 0 },
  { key: 'completed', label: '已完成', count: 0 },
])

const orders = ref<any[]>([])

const filteredOrders = computed(() => {
  if (currentTab.value === 'all') return orders.value
  return orders.value.filter(o => o.status === currentTab.value)
})

function handleTabChange() {
  // 状态切换时保持当前排序
}

function getStatusKey(status: number): string {
  const map: Record<number, string> = {
    0: 'unpaid',
    1: 'paid',
    2: 'in_progress',
    3: 'completed',
    4: 'cancelled',
  }
  return map[status] || 'unknown'
}

function getStatusText(status: number): string {
  const map: Record<number, string> = {
    0: '未支付',
    1: '待服务',
    2: '服务中',
    3: '已完成',
    4: '已取消',
  }
  return map[status] || '未知'
}

async function loadOrders() {
  loading.value = true
  showLoadingToast({ message: '加载中...' })
  try {
    const res = await get<{ code: number; data: any[] }>(
      `/api/admin/orders?sort=${sortType.value}&order=${sortOrder.value}`
    )
    if (res.data.code === 200) {
      orders.value = res.data.data.map((item: any) => ({
        id: item.id,
        no: item.orderNo,
        user: item.userName || '未知用户',
        building: item.buildingName?.split(' · ')[0] || '',
        room: item.roomNo,
        time: `${item.serviceDate} ${item.startTime} ~ ${item.endTime}`,
        amount: item.amount,
        status: getStatusKey(item.status),
        statusText: getStatusText(item.status),
        employeeId: item.employeeId,
        buildingName: item.buildingName,
      }))
      updateTabCounts()
    }
  } catch (error) {
    showToast('加载失败')
  } finally {
    loading.value = false
    closeToast()
  }
}

function updateTabCounts() {
  statusTabs.value.forEach(tab => {
    if (tab.key === 'all') {
      tab.count = orders.value.length
    } else {
      tab.count = orders.value.filter(o => o.status === tab.key).length
    }
  })
}

function handleSortChange(value: string) {
  sortType.value = value
  loadOrders()
}

function toggleSortOrder() {
  sortOrder.value = sortOrder.value === 'desc' ? 'asc' : 'desc'
  loadOrders()
}

onMounted(() => {
  loadOrders()
})

function confirmPay(order: any) {
  showDialog({
    title: '确认已支付',
    message: '确认已收到用户的支付？此操作可回退',
    confirmButtonText: '确认',
    cancelButtonText: '取消',
  }).then(() => {
    post(`/api/admin/order/confirm-pay/${order.id}`).then(() => {
      order.status = 'paid'
      order.statusText = '待服务'
      showToast('已确认支付')
    }).catch(() => {
      showToast('操作失败')
    })
  }).catch(() => {
    // 取消，不做操作
  })
}

function revertPay(order: any) {
  showDialog({
    title: '回退支付',
    message: '确认回退此订单？订单将回到未支付状态',
    confirmButtonText: '确认',
    cancelButtonText: '取消',
  }).then(() => {
    post(`/api/admin/order/revert-pay/${order.id}`).then(() => {
      order.status = 'unpaid'
      order.statusText = '未支付'
      showToast('已回退支付')
    }).catch(() => {
      showToast('操作失败')
    })
  }).catch(() => {
    // 取消，不做操作
  })
}
</script>

<style scoped>
.admin-orders {
  padding: 8px 16px 100px;
}

.sort-bar {
  display: flex;
  align-items: center;
  justify-content: flex-end;
  gap: 8px;
  margin-bottom: 12px;
}

.sort-btn {
  font-size: 16px;
  width: 40px;
  height: 32px;
  padding: 0;
  display: flex;
  align-items: center;
  justify-content: center;
}

.status-tabs {
  display: flex;
  gap: 8px;
  overflow-x: auto;
  padding: 8px 0 12px;
  -webkit-overflow-scrolling: touch;
}

.status-tab {
  white-space: nowrap;
  padding: 8px 16px;
  background: white;
  border-radius: 20px;
  font-size: 13px;
  font-weight: 500;
  color: #86868B;
  cursor: pointer;
  transition: all 0.2s;
  display: flex;
  align-items: center;
  gap: 6px;
}

.status-tab.active {
  background: #2B95FF;
  color: white;
  box-shadow: 0 2px 8px rgba(43,149,255,0.3);
}

.tab-count {
  font-size: 11px;
  background: rgba(0,0,0,0.05);
  padding: 1px 6px;
  border-radius: 8px;
}

.status-tab.active .tab-count {
  background: rgba(255,255,255,0.2);
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

.order-user {
  font-size: 13px;
  color: #1D1D1F;
  font-weight: 500;
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

.confirm-btn {
  background: #34C759;
  color: white;
}

.confirm-btn:active {
  transform: scale(0.95);
  opacity: 0.9;
}

.revert-btn {
  background: #FF3B30;
  color: white;
}

.revert-btn:active {
  transform: scale(0.95);
  opacity: 0.9;
}

.revert-btn.disabled {
  background: #C7C7CC;
  cursor: not-allowed;
  opacity: 0.5;
}

.status-badge {
  font-size: 12px;
  font-weight: 600;
  padding: 4px 12px;
  border-radius: 10px;
}

.status-badge.paid {
  background: rgba(43,149,255,0.1);
  color: #2B95FF;
}

.status-badge.in_progress {
  background: rgba(255,149,0,0.1);
  color: #FF9500;
}

.status-badge.completed {
  background: rgba(52,199,89,0.1);
  color: #34C759;
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