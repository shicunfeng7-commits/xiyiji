<template>
  <div class="admin-orders">
    <!-- 搜索栏 -->
    <div class="search-bar">
      <van-search
        v-model="keyword"
        shape="round"
        placeholder="搜索订单号"
        @search="onSearch"
        @clear="onSearch"
        @input="onSearchDebounced"
        background="transparent"
      />
    </div>

    <!-- 状态筛选标签 -->
    <div class="status-tabs">
      <span
        v-for="s in statusOptions"
        :key="s.value"
        class="status-tab"
        :class="{ active: currentStatus === s.value }"
        @click="filterByStatus(s.value)"
      >{{ s.label }}</span>
    </div>

    <!-- 订单列表 -->
    <div class="order-list" v-if="!loading">
      <div class="order-card" v-for="o in filteredOrders" :key="o.id">
        <div class="card-top">
          <div class="card-left">
            <span class="order-no">{{ o.orderNo }}</span>
            <span class="order-phone" v-if="o.userPhone">{{ o.userPhone }}</span>
          </div>
          <span class="order-status" :class="statusClass(o.status)">{{ statusText(o.status) }}</span>
        </div>
        <div class="card-mid">
          <span class="card-loc">{{ o.buildingName }} · {{ o.roomNo }}</span>
          <span class="card-time">{{ o.serviceDate }} {{ o.startTime }}-{{ o.endTime }}</span>
        </div>
        <div class="card-bottom">
          <span class="card-amount">¥{{ o.amount }}</span>
          <div class="card-actions">
            <span v-if="o.employeeName" class="emp-name">{{ o.employeeName }}</span>
            <span v-else-if="o.status === 1" class="emp-name pending-assign">待分配</span>
            <button v-if="o.status === 0" class="action-btn pay-btn" @click="confirmPay(o)">确认已支付</button>
            <button v-if="o.status === 1 && !o.employeeId" class="action-btn revert-btn" @click="revertPay(o)">回退</button>
            <button class="action-btn del-btn" @click="deleteOrder(o)">删除</button>
          </div>
        </div>
      </div>

      <div v-if="filteredOrders.length === 0" class="empty-state">
        <van-icon name="inbox-o" size="48" color="#C7C7CC" />
        <p>{{ keyword ? '未找到匹配订单' : '暂无订单' }}</p>
      </div>
    </div>

    <div v-else class="loading-state">
      <van-loading size="24px" />
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, computed, onMounted } from 'vue'
import { showConfirmDialog, showToast } from 'vant'
import { post, get, del } from '../../utils/request'

const keyword = ref('')
const currentStatus = ref<number | null>(null)
const orders = ref<any[]>([])
const loading = ref(true)

const statusOptions = [
  { label: '全部', value: null },
  { label: '未支付', value: 0 },
  { label: '已支付', value: 1 },
  { label: '待服务', value: 5 },
  { label: '服务中', value: 2 },
  { label: '已完成', value: 3 },
  { label: '已取消', value: 4 },
]

let debounceTimer: ReturnType<typeof setTimeout> | null = null

function onSearch() { loadOrders() }
function onSearchDebounced() {
  if (debounceTimer) clearTimeout(debounceTimer)
  debounceTimer = setTimeout(() => loadOrders(), 300)
}

function statusClass(s: number) {
  const map: Record<number, string> = { 0: 'unpaid', 1: 'paid', 2: 'progress', 3: 'done', 4: 'cancelled', 5: 'pending' }
  return map[s] || ''
}
function statusText(s: number) {
  const map: Record<number, string> = { 0: '未支付', 1: '已支付', 2: '服务中', 3: '已完成', 4: '已取消', 5: '待服务' }
  return map[s] || '未知'
}

const filteredOrders = computed(() => orders.value)

function filterByStatus(val: number | null) {
  currentStatus.value = val
  loadOrders()
}

async function loadOrders() {
  loading.value = true
  try {
    const params: Record<string, any> = {}
    if (currentStatus.value !== null) params.status = currentStatus.value
    if (keyword.value) params.keyword = keyword.value
    const res = await get<{ code: number; data: any[] }>('/api/admin/orders', params)
    if (res.data.code === 200) {
      orders.value = res.data.data.map((o: any) => ({
        ...o,
        amount: o.amount?.toString() || '29.90',
      }))
    }
  } catch { /* ignore */ } finally { loading.value = false }
}

function confirmPay(o: any) {
  showConfirmDialog({ title: '确认已支付', message: `确认收到 ${o.orderNo} 的付款？` })
    .then(async () => {
      await post(`/api/admin/order/confirm-pay/${o.id}`)
      o.status = 1
      showToast('已确认支付')
    }).catch(() => {})
}

function revertPay(o: any) {
  showConfirmDialog({ title: '回退支付', message: `将 ${o.orderNo} 回退为未支付？` })
    .then(async () => {
      await post(`/api/admin/order/revert-pay/${o.id}`)
      o.status = 0
      showToast('已回退')
    }).catch(() => {})
}

function deleteOrder(o: any) {
  showConfirmDialog({ title: '删除订单', message: `确定删除 ${o.orderNo}？此操作不可恢复`, confirmButtonText: '确认删除', cancelButtonText: '取消' })
    .then(async () => {
      await del(`/api/admin/order/${o.id}`)
      orders.value = orders.value.filter(x => x.id !== o.id)
      showToast('已删除')
    }).catch(() => {})
}

onMounted(loadOrders)
</script>

<style scoped>
.admin-orders { padding-bottom: 80px; min-height: 100vh; background: #F5F5F7; }

.search-bar { padding: 8px 12px 0; }
.search-bar :deep(.van-search__content) { background: white; border-radius: 24px; box-shadow: 0 1px 4px rgba(0,0,0,0.04); }

.status-tabs {
  display: flex; gap: 8px; padding: 12px 14px; overflow-x: auto;
  -webkit-overflow-scrolling: touch; scrollbar-width: none;
}
.status-tabs::-webkit-scrollbar { display: none; }

.status-tab {
  flex-shrink: 0; padding: 8px 18px; border-radius: 20px; font-size: 13px;
  font-weight: 600; color: #86868B; background: white; cursor: pointer;
  transition: all 0.25s cubic-bezier(0.25, 0.1, 0.25, 1);
  box-shadow: 0 1px 2px rgba(0,0,0,0.03);
}
.status-tab.active { background: #1D1D1F; color: white; box-shadow: 0 2px 8px rgba(0,0,0,0.12); }

.order-list { padding: 0 14px; }

.order-card {
  background: white; border-radius: 18px; padding: 16px; margin-bottom: 12px;
  box-shadow: 0 1px 3px rgba(0,0,0,0.03); transition: transform 0.2s cubic-bezier(0.25,0.1,0.25,1);
}
.order-card:active { transform: scale(0.99); }

.card-top { display: flex; justify-content: space-between; align-items: flex-start; margin-bottom: 10px; }
.card-left { display: flex; flex-direction: column; gap: 2px; }
.order-no { font-size: 15px; font-weight: 700; color: #1D1D1F; letter-spacing: -0.02em; }
.order-phone { font-size: 12px; color: #86868B; }

.order-status { font-size: 11px; font-weight: 700; padding: 4px 10px; border-radius: 10px; letter-spacing: 0.02em; }
.order-status.unpaid { background: #FFF3E0; color: #E65100; }
.order-status.paid { background: #E3F2FD; color: #1565C0; }
.order-status.progress { background: #FFF8E1; color: #E65100; }
.order-status.done { background: #E8F5E9; color: #2E7D32; }
.order-status.cancelled { background: #F5F5F5; color: #86868B; }
.order-status.pending { background: #F3E5F5; color: #7B1FA2; }

.card-mid { display: flex; flex-direction: column; gap: 3px; padding-bottom: 12px; border-bottom: 1px solid #F5F5F7; margin-bottom: 10px; }
.card-loc { font-size: 14px; color: #1D1D1F; }
.card-time { font-size: 12px; color: #86868B; }

.card-bottom { display: flex; justify-content: space-between; align-items: center; }
.card-amount { font-size: 18px; font-weight: 800; color: #1D1D1F; letter-spacing: -0.02em; }
.card-actions { display: flex; align-items: center; gap: 8px; }
.emp-name { font-size: 12px; color: #2B95FF; font-weight: 500; }
.emp-name.pending-assign { color: #FF9500; font-style: italic; }

.action-btn {
  padding: 8px 18px; border: none; border-radius: 18px; font-size: 13px;
  font-weight: 600; cursor: pointer; transition: all 0.2s cubic-bezier(0.25,0.1,0.25,1);
}
.pay-btn { background: #2B95FF; color: white; }
.pay-btn:active { transform: scale(0.96); opacity: 0.9; }
.revert-btn { background: #F5F5F7; color: #FF3B30; }
.revert-btn:active { transform: scale(0.96); }
.del-btn { background: #F5F5F7; color: #C7C7CC; font-size: 11px; padding: 4px 10px; }
.del-btn:active { background: #FFF5F5; color: #FF3B30; }

.loading-state { text-align: center; padding: 60px 0; }
.empty-state { text-align: center; padding: 80px 0; }
.empty-state p { font-size: 14px; color: #C7C7CC; margin-top: 12px; }
</style>
