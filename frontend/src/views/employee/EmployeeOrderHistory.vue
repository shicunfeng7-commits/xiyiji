<template>
  <div class="order-history">
    <!-- 日期筛选 -->
    <div class="filter-card">
      <div class="filter-row">
        <div class="date-field">
          <span class="field-label">开始</span>
          <input type="date" v-model="startDate" class="date-input" />
        </div>
        <span class="date-sep">—</span>
        <div class="date-field">
          <span class="field-label">结束</span>
          <input type="date" v-model="endDate" class="date-input" />
        </div>
        <button class="reset-btn" @click="resetFilter" v-if="hasFilter">重置</button>
      </div>
      <div class="summary-bar">
        <span>共 <b>{{ filteredOrders.length }}</b> 单</span>
        <span>总收入 <b>¥{{ totalRevenue }}</b></span>
      </div>
    </div>

    <!-- 加载中 -->
    <div v-if="loading" class="loading-state">
      <van-loading size="24px" />
    </div>

    <!-- 订单列表 -->
    <div v-else class="order-list">
      <div class="order-card" v-for="order in filteredOrders" :key="order.id">
        <div class="order-header">
          <span class="order-no">{{ order.orderNo }}</span>
          <span class="order-amount">¥{{ order.amount }}</span>
        </div>
        <div class="order-body">
          <div class="info-line">{{ order.building }} · {{ order.room }}</div>
          <div class="info-line time">{{ order.date }} {{ order.timeSlot }}</div>
          <div class="info-line complete">完成于 {{ order.completeTime?.substring(0, 16)?.replace('T', ' ') }}</div>
        </div>
        <div class="photo-thumbs" v-if="order.photos.length > 0">
          <img v-for="(url, i) in order.photos.slice(0, 4)" :key="i" :src="url" class="photo-thumb" @click="preview(url)" @error="onErr" />
          <span v-if="order.photos.length > 4" class="more-photos">+{{ order.photos.length - 4 }}</span>
        </div>
        <!-- 用户评价 -->
        <div v-if="order.review" class="review-section">
          <div class="review-stars">
            <span v-for="s in 5" :key="s" :class="s <= order.review.score ? 'star active' : 'star'">★</span>
          </div>
          <div v-if="order.review.content" class="review-content">{{ order.review.content }}</div>
        </div>
      </div>

      <div v-if="filteredOrders.length === 0" class="empty-state">
        <van-icon name="records-o" size="48" color="#C7C7CC" />
        <p>{{ hasFilter ? '当前所选时间无订单' : '暂无完成订单' }}</p>
      </div>
    </div>

    <van-image-preview v-model:show="showPreview" :images="previewImages" />
  </div>
</template>

<script setup lang="ts">
import { ref, computed, onMounted } from 'vue'
import { get } from '../../utils/request'

const allOrders = ref<any[]>([])
const loading = ref(true)
const showPreview = ref(false)
const previewImages = ref<string[]>([])

const startDate = ref('')
const endDate = ref('')

const hasFilter = computed(() => startDate.value !== '' || endDate.value !== '')

function resetFilter() {
  startDate.value = ''
  endDate.value = ''
}

function onErr(e: Event) { (e.target as HTMLImageElement).style.display = 'none' }
function preview(url: string) { previewImages.value = [url]; showPreview.value = true }

// 按服务日期（serviceDate）过滤
const filteredOrders = computed(() => {
  let list = allOrders.value
  if (startDate.value) {
    list = list.filter(o => (o.date || '') >= startDate.value)
  }
  if (endDate.value) {
    list = list.filter(o => (o.date || '') <= endDate.value)
  }
  return list
})

const totalRevenue = computed(() =>
  filteredOrders.value.reduce((sum, o) => sum + parseFloat(o.amount || '0'), 0).toFixed(2)
)

async function loadOrders() {
  loading.value = true
  try {
    const res = await get<{ code: number; data: any[] }>('/api/employee/orders/my-list')
    if (res.data.code === 200) {
      const orders = res.data.data
        .filter((o: any) => o.status === 3)
        .sort((a: any, b: any) => (b.completeTime || '').localeCompare(a.completeTime || ''))
        .map((item: any) => {
          const bi = item.buildingName?.split(' · ') || ['', '']
          let photos: string[] = []
          try { if (item.beforePhoto) photos = photos.concat(JSON.parse(item.beforePhoto)) } catch { if (item.beforePhoto) photos.push(item.beforePhoto) }
          try { if (item.afterPhoto) photos = photos.concat(JSON.parse(item.afterPhoto)) } catch { if (item.afterPhoto) photos.push(item.afterPhoto) }
          return {
            id: item.id, orderNo: item.orderNo,
            building: bi[1] || '', room: item.roomNo,
            date: item.serviceDate, timeSlot: `${item.startTime} ~ ${item.endTime}`,
            amount: item.amount?.toString() || '29.90',
            completeTime: item.completeTime, photos, review: null,
          }
        })
      
      // 加载每个订单的评价
      for (const order of orders) {
        try {
          const reviewRes = await get<{ code: number; data: any }>(`/api/employee/order/review/${order.id}`)
          if (reviewRes.data.code === 200 && reviewRes.data.data) {
            order.review = reviewRes.data.data
          }
        } catch { /* ignore */ }
      }
      
      allOrders.value = orders
    }
  } catch { /* ignore */ } finally { loading.value = false }
}

onMounted(loadOrders)
</script>

<style scoped>
.order-history { padding: 12px 16px 100px; min-height: 100vh; background: #F5F5F7; }

.filter-card {
  background: white; border-radius: 14px; padding: 14px; margin-bottom: 12px;
  box-shadow: 0 1px 3px rgba(0,0,0,0.04); position: sticky; top: 0; z-index: 10;
}

.filter-row { display: flex; align-items: center; gap: 10px; margin-bottom: 12px; }

.date-field { flex: 1; }
.field-label { font-size: 11px; color: #86868B; display: block; margin-bottom: 4px; }

.date-input {
  width: 100%; padding: 10px 10px; border: 1px solid #E8E8ED; border-radius: 10px;
  font-size: 14px; color: #1D1D1F; background: #F5F5F7; box-sizing: border-box;
}

.date-sep { font-size: 13px; color: #C7C7CC; padding-top: 18px; }

.reset-btn {
  padding: 6px 14px; background: none; border: 1px solid #E8E8ED; border-radius: 8px;
  font-size: 12px; color: #86868B; cursor: pointer; margin-top: 18px; white-space: nowrap;
}
.reset-btn:active { background: #F5F5F7; }

.summary-bar { display: flex; justify-content: space-between; font-size: 14px; color: #1D1D1F; }
.summary-bar b { color: #2B95FF; }

.loading-state { text-align: center; padding: 60px 0; }

.order-card {
  background: white; border-radius: 14px; padding: 14px; margin-bottom: 10px;
  box-shadow: 0 1px 3px rgba(0,0,0,0.04);
}

.order-header { display: flex; justify-content: space-between; align-items: center; margin-bottom: 6px; }
.order-no { font-size: 13px; color: #86868B; }
.order-amount { font-size: 16px; font-weight: 700; color: #34C759; }

.order-body { margin-bottom: 8px; }
.info-line { font-size: 14px; color: #1D1D1F; padding: 1px 0; }
.info-line.time { font-size: 12px; color: #86868B; }
.info-line.complete { font-size: 11px; color: #C7C7CC; }

.photo-thumbs { display: flex; gap: 6px; padding: 8px 0; flex-wrap: wrap; }
.photo-thumb { width: 56px; height: 56px; border-radius: 8px; object-fit: cover; background: #F5F5F7; cursor: pointer; }
.photo-thumb:active { opacity: 0.8; }
.more-photos { font-size: 12px; color: #86868B; align-self: flex-end; }

.empty-state { text-align: center; padding: 60px 0; }
.empty-state p { font-size: 14px; color: #C7C7CC; margin-top: 10px; }

.review-section { padding: 8px 0 0; border-top: 1px solid #F5F5F7; margin-top: 8px; }
.review-stars { display: flex; gap: 2px; margin-bottom: 4px; }
.star { font-size: 14px; color: #3a3a3c; }
.star.active { color: #FFD60A; }
.review-content { font-size: 13px; color: #86868B; line-height: 1.5; }
</style>
