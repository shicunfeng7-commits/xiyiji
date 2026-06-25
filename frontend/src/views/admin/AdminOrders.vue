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
        <div class="card-top" @click="toggleExpand(o.id)">
          <div class="card-left">
            <span class="order-no">{{ o.orderNo }}</span>
            <span class="order-phone" v-if="o.userPhone">{{ o.userPhone }}</span>
          </div>
          <div class="card-right">
            <span class="order-status" :class="statusClass(o.status)">{{ statusText(o.status) }}</span>
            <van-icon :name="expandedId === o.id ? 'arrow-up' : 'arrow-down'" size="16" color="#C7C7CC" />
          </div>
        </div>
        <div class="card-mid">
          <span class="card-loc">{{ o.buildingName }} · {{ o.roomNo }}</span>
          <span class="card-time">{{ o.serviceDate }} {{ o.startTime }}-{{ o.endTime }}</span>
        </div>

        <!-- 展开的详情 -->
        <div v-if="expandedId === o.id" class="card-detail">
          <div class="detail-row" v-if="o.contactPhone">
            <span class="detail-label">联系电话</span>
            <span class="detail-value">{{ o.contactPhone }}</span>
          </div>
          <div class="detail-row" v-if="o.remark">
            <span class="detail-label">用户备注</span>
            <span class="detail-value">{{ o.remark }}</span>
          </div>
          <div class="detail-row" v-if="o.employeeName">
            <span class="detail-label">服务员工</span>
            <span class="detail-value">{{ o.employeeName }}</span>
          </div>
          <!-- 照片 -->
          <div v-if="o.beforePhoto || o.afterPhoto" class="detail-photos">
            <div class="detail-photos-header">
              <span class="detail-photos-title">服务照片</span>
              <button class="select-photo-btn" @click.stop="openPhotoSelect(o)">选择照片</button>
            </div>
            <div class="detail-photos-grid">
              <div v-if="getPhotos(o.beforePhoto).length > 0" class="photo-group">
                <div class="photo-group-label">清洗前</div>
                <div class="photo-row">
                  <img v-for="(url, i) in getPhotos(o.beforePhoto).slice(0, 2)" :key="'b'+i" :src="url" class="detail-photo" @error="onImgError" />
                </div>
              </div>
              <div v-if="getPhotos(o.afterPhoto).length > 0" class="photo-group">
                <div class="photo-group-label">清洗后</div>
                <div class="photo-row">
                  <img v-for="(url, i) in getPhotos(o.afterPhoto).slice(0, 2)" :key="'a'+i" :src="url" class="detail-photo" @error="onImgError" />
                </div>
              </div>
            </div>
          </div>
          <!-- 评价 -->
          <div v-if="o.review" class="detail-review">
            <div class="detail-photos-title">用户评价</div>
            <div class="review-stars">
              <span v-for="s in 5" :key="s" :class="s <= o.review.score ? 'star active' : 'star'">★</span>
            </div>
            <div v-if="o.review.content" class="review-text">{{ o.review.content }}</div>
          </div>
        </div>

        <div class="card-bottom">
          <span class="card-amount">¥{{ o.amount }}</span>
          <div class="card-actions">
            <span v-if="o.employeeName" class="emp-name">{{ o.employeeName }}</span>
            <span v-else-if="o.status === 1" class="emp-name pending-assign">待分配</span>
            <button v-if="o.status === 0" class="action-btn pay-btn" @click="confirmPay(o)">确认已支付</button>
            <button v-if="o.status === 1 && !o.employeeId" class="action-btn revert-btn" @click="revertPay(o)">回退</button>
            <button v-if="o.status === 3 && o.beforePhoto" class="action-btn photo-btn" :class="{ active: o.isPhotoFeatured }" @click.stop="togglePhotoFeatured(o)">
              {{ o.isPhotoFeatured ? '已展示' : '展示照片' }}
            </button>
            <button v-if="o.isPhotoFeatured" class="action-btn order-btn" @click.stop="showOrderDialog(o)">设顺序</button>
            <button class="action-btn del-btn" @click.stop="deleteOrder(o)">删除</button>
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

    <!-- 照片选择弹窗 -->
    <van-dialog v-model:show="showPhotoSelect" title="选择展示照片" :show-cancel-button="false" confirm-button-text="完成" @confirm="saveSelectedPhotos">
      <div class="photo-select-content">
        <div class="photo-select-hint">点击选择照片，按选择顺序编号</div>
        <div class="photo-select-list">
          <div
            v-for="(photo, index) in selectablePhotos"
            :key="index"
            class="photo-select-item"
            :class="{ selected: photo.selected }"
            @click="togglePhotoSelect(index)"
          >
            <img :src="photo.url" class="photo-select-img" @error="onImgError" />
            <div v-if="photo.selected" class="photo-select-badge">{{ photo.order }}</div>
            <div class="photo-select-type">{{ photo.type }}</div>
          </div>
        </div>
        <div v-if="selectedPhotos.length > 0" class="photo-select-summary">
          已选择 {{ selectedPhotos.length }} 张照片
        </div>
      </div>
    </van-dialog>

    <!-- 拖动排序弹窗 -->
    <van-dialog v-model:show="showDragSort" title="调整照片顺序" :show-cancel-button="false" confirm-button-text="完成" @confirm="saveDragOrder">
      <div class="drag-sort-content">
        <div class="drag-sort-hint">长按拖动调整顺序</div>
        <div class="drag-sort-list" ref="dragSortListRef">
          <div
            v-for="(photo, index) in dragPhotos"
            :key="index"
            class="drag-sort-item"
            :class="{ 'dragging': dragIndex === index }"
            @touchstart.prevent="onTouchStart(index, $event)"
            @touchmove.prevent="onTouchMove($event)"
            @touchend.prevent="onTouchEnd"
          >
            <span class="drag-sort-num">{{ index + 1 }}</span>
            <img :src="photo" class="drag-sort-img" @error="onImgError" />
            <van-icon name="reorder" size="20" color="#C7C7CC" class="drag-sort-icon" />
          </div>
        </div>
      </div>
    </van-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, computed, onMounted } from 'vue'
import { showConfirmDialog, showToast } from 'vant'
import { post, get, del } from '../../utils/request'

const keyword = ref('')
const currentStatus = ref<number | string | null>(null)
const orders = ref<any[]>([])
const loading = ref(true)
const expandedId = ref<number | null>(null)

const statusOptions = [
  { label: '全部', value: null },
  { label: '未支付', value: 0 },
  { label: '已支付', value: 1 },
  { label: '待服务', value: 5 },
  { label: '服务中', value: 2 },
  { label: '已完成', value: 3 },
  { label: '已取消', value: 4 },
  { label: '已展示', value: 'featured' },
]

const filteredOrders = computed(() => orders.value)

function toggleExpand(id: number) {
  expandedId.value = expandedId.value === id ? null : id
}

function getPhotos(json: string): string[] {
  if (!json) return []
  try {
    const arr = JSON.parse(json)
    return Array.isArray(arr) ? arr : [json]
  } catch {
    return [json]
  }
}

function onImgError(e: Event) {
  (e.target as HTMLImageElement).style.display = 'none'
}

function statusClass(s: number) {
  const map: Record<number, string> = { 0: 'unpaid', 1: 'paid', 2: 'progress', 3: 'done', 4: 'cancelled', 5: 'pending' }
  return map[s] || ''
}
function statusText(s: number) {
  const map: Record<number, string> = { 0: '未支付', 1: '已支付', 2: '服务中', 3: '已完成', 4: '已取消', 5: '待服务' }
  return map[s] || '未知'
}

function filterByStatus(val: number | string | null) {
  currentStatus.value = val as any
  loadOrders()
}

let debounceTimer: ReturnType<typeof setTimeout> | null = null
function onSearch() { loadOrders() }
function onSearchDebounced() {
  if (debounceTimer) clearTimeout(debounceTimer)
  debounceTimer = setTimeout(() => loadOrders(), 300)
}

async function loadOrders() {
  loading.value = true
  try {
    const params: Record<string, any> = {}
    if (currentStatus.value === 'featured') {
      params.featured = 1
    } else if (currentStatus.value !== null) {
      params.status = currentStatus.value
    }
    if (keyword.value) params.keyword = keyword.value
    const res = await get<{ code: number; data: any[] }>('/api/admin/orders', params)
    if (res.data.code === 200) {
      const orderList = res.data.data.map((o: any) => ({
        ...o,
        amount: o.amount?.toString() || '29.90',
        review: null,
      }))
      for (const o of orderList) {
        if (o.status === 3) {
          try {
            const reviewRes = await get<{ code: number; data: any }>(`/api/user/order/review/${o.id}`)
            if (reviewRes.data.code === 200 && reviewRes.data.data) {
              o.review = reviewRes.data.data
            }
          } catch { /* ignore */ }
        }
      }
      orders.value = orderList
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

async function togglePhotoFeatured(o: any) {
  try {
    const featured = !o.isPhotoFeatured
    await post(`/api/admin/order/photo-featured/${o.id}?featured=${featured}`)
    o.isPhotoFeatured = featured ? 1 : 0
    showToast(featured ? '已设为展示' : '已取消展示')
  } catch {
    showToast('操作失败')
  }
}

// 照片选择相关
const showPhotoSelect = ref(false)
const selectablePhotos = ref<{ url: string; type: string; selected: boolean; order: number }[]>([])
const selectedPhotos = computed(() => selectablePhotos.value.filter(p => p.selected).sort((a, b) => a.order - b.order))

function openPhotoSelect(o: any) {
  currentSelectOrder.value = o
  const allPhotos: { url: string; type: string; selected: boolean; order: number }[] = []
  getPhotos(o.beforePhoto).forEach(url => {
    allPhotos.push({ url, type: '清洗前', selected: false, order: 0 })
  })
  getPhotos(o.afterPhoto).forEach(url => {
    allPhotos.push({ url, type: '清洗后', selected: false, order: 0 })
  })
  // 如果已有精选照片，标记为已选中
  const existing = getPhotos(o.featuredPhotos)
  allPhotos.forEach(p => {
    const idx = existing.indexOf(p.url)
    if (idx >= 0) {
      p.selected = true
      p.order = idx + 1
    }
  })
  selectablePhotos.value = allPhotos
  showPhotoSelect.value = true
}

function togglePhotoSelect(index: number) {
  const photo = selectablePhotos.value[index]
  if (photo.selected) {
    // 取消选择，后面的序号自动补上
    const removedOrder = photo.order
    photo.selected = false
    photo.order = 0
    // 后面的序号减1
    selectablePhotos.value.forEach(p => {
      if (p.selected && p.order > removedOrder) {
        p.order--
      }
    })
  } else {
    // 选择，分配下一个序号
    const maxOrder = Math.max(0, ...selectablePhotos.value.filter(p => p.selected).map(p => p.order))
    photo.selected = true
    photo.order = maxOrder + 1
  }
}

async function saveSelectedPhotos() {
  if (!currentSelectOrder.value) return
  const photos = selectedPhotos.value.map(p => p.url)
  try {
    await post(`/api/admin/order/featured-photos/${currentSelectOrder.value.id}`, { photos: JSON.stringify(photos) })
    currentSelectOrder.value.featuredPhotos = JSON.stringify(photos)
    currentSelectOrder.value.isPhotoFeatured = photos.length > 0 ? 1 : 0
    showPhotoSelect.value = false
    showToast('照片已保存')
  } catch {
    showToast('保存失败')
  }
}

// 拖动排序相关
const showDragSort = ref(false)
const dragPhotos = ref<string[]>([])
const dragIndex = ref<number>(-1)
const dragSortListRef = ref<HTMLElement | null>(null)
let dragStartY = 0
let dragStartIndex = 0
const currentDragOrder = ref<any>(null)

function showOrderDialog(o: any) {
  currentDragOrder.value = o
  dragPhotos.value = getPhotos(o.featuredPhotos || o.beforePhoto || '[]')
  showDragSort.value = true
}

function onTouchStart(index: number, e: TouchEvent) {
  dragIndex.value = index
  dragStartIndex = index
  dragStartY = e.touches[0].clientY
}

function onTouchMove(e: TouchEvent) {
  if (dragIndex.value < 0 || !dragSortListRef.value) return
  const list = dragSortListRef.value
  const items = list.querySelectorAll('.drag-sort-item')
  const listRect = list.getBoundingClientRect()

  for (let i = 0; i < items.length; i++) {
    const rect = items[i].getBoundingClientRect()
    const midY = rect.top + rect.height / 2
    if (e.touches[0].clientY < midY && i !== dragIndex.value) {
      const item = dragPhotos.value.splice(dragIndex.value, 1)[0]
      dragPhotos.value.splice(i, 0, item)
      dragIndex.value = i
      break
    }
  }
  // 处理拖到最后一个的情况
  const lastRect = items[items.length - 1].getBoundingClientRect()
  if (e.touches[0].clientY > lastRect.bottom && dragIndex.value < dragPhotos.value.length - 1) {
    const item = dragPhotos.value.splice(dragIndex.value, 1)[0]
    dragPhotos.value.push(item)
    dragIndex.value = dragPhotos.value.length - 1
  }
}

function onTouchEnd() {
  dragIndex.value = -1
}

async function saveDragOrder() {
  if (!currentDragOrder.value) return
  try {
    await post(`/api/admin/order/featured-photos/${currentDragOrder.value.id}`, { photos: JSON.stringify(dragPhotos.value) })
    currentDragOrder.value.featuredPhotos = JSON.stringify(dragPhotos.value)
    showDragSort.value = false
    showToast('顺序已更新')
  } catch {
    showToast('保存失败')
  }
}

const currentSelectOrder = ref<any>(null)

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

.card-right { display: flex; align-items: center; gap: 8px; }

.card-detail { padding: 12px 0; border-bottom: 1px solid #F5F5F7; margin-bottom: 10px; }
.detail-row { display: flex; gap: 12px; margin-bottom: 8px; }
.detail-label { font-size: 13px; color: #86868B; flex-shrink: 0; width: 70px; }
.detail-value { font-size: 13px; color: #1D1D1F; }

.detail-photos { margin-top: 12px; }
.detail-photos-header { display: flex; justify-content: space-between; align-items: center; margin-bottom: 8px; }
.detail-photos-title { font-size: 13px; font-weight: 600; color: #1D1D1F; }
.select-photo-btn { padding: 4px 12px; background: #2B95FF; color: white; border: none; border-radius: 12px; font-size: 12px; cursor: pointer; }
.detail-photos-grid { display: flex; flex-direction: column; gap: 12px; }
.photo-group { }
.photo-group-label { font-size: 12px; color: #86868B; margin-bottom: 6px; }
.photo-row { display: flex; gap: 8px; }
.detail-photo { width: 80px; height: 80px; border-radius: 10px; object-fit: cover; background: #F5F5F7; }

.detail-review { margin-top: 12px; }
.detail-review .review-stars { display: flex; gap: 2px; margin-bottom: 4px; }
.detail-review .star { font-size: 14px; color: #3a3a3c; }
.detail-review .star.active { color: #FFD60A; }
.detail-review .review-text { font-size: 13px; color: #86868B; line-height: 1.5; }

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
.photo-btn { background: #F5F5F7; color: #86868B; }
.photo-btn.active { background: #34C759; color: white; }
.photo-btn:active { transform: scale(0.96); }
.order-btn { background: #F5F5F7; color: #2B95FF; }
.order-btn:active { transform: scale(0.96); }
.del-btn { background: #FFF5F5; color: #FF3B30; font-size: 11px; padding: 4px 10px; }
.del-btn:active { background: #FFEBEE; }

.photo-select-content { padding: 16px; }
.photo-select-hint { font-size: 13px; color: #86868B; margin-bottom: 12px; }
.photo-select-list { display: grid; grid-template-columns: repeat(3, 1fr); gap: 8px; }
.photo-select-item {
  position: relative; border-radius: 10px; overflow: hidden; cursor: pointer;
  border: 2px solid transparent; transition: all 0.2s;
}
.photo-select-item.selected { border-color: #2B95FF; }
.photo-select-img { width: 100%; aspect-ratio: 1; object-fit: cover; display: block; }
.photo-select-badge {
  position: absolute; top: 4px; right: 4px; width: 24px; height: 24px;
  background: #2B95FF; color: white; border-radius: 50%;
  display: flex; align-items: center; justify-content: center;
  font-size: 12px; font-weight: 700;
}
.photo-select-type {
  position: absolute; bottom: 0; left: 0; right: 0;
  background: rgba(0,0,0,0.5); color: white; font-size: 10px;
  text-align: center; padding: 2px 0;
}
.photo-select-summary { text-align: center; font-size: 13px; color: #2B95FF; margin-top: 12px; font-weight: 600; }

.drag-sort-content { padding: 16px; }
.drag-sort-hint { font-size: 13px; color: #86868B; margin-bottom: 12px; }
.drag-sort-list { display: flex; flex-direction: column; gap: 8px; }
.drag-sort-item {
  display: flex; align-items: center; gap: 12px; padding: 10px;
  background: #F9F9FB; border-radius: 12px; cursor: grab;
  transition: background 0.2s;
}
.drag-sort-item:active { background: #F0F0F5; cursor: grabbing; }
.drag-sort-item.dragging { opacity: 0.5; background: #E8F0FE; }
.drag-sort-num {
  width: 24px; height: 24px; background: #2B95FF; color: white;
  border-radius: 50%; display: flex; align-items: center; justify-content: center;
  font-size: 12px; font-weight: 700; flex-shrink: 0;
}
.drag-sort-img { width: 50px; height: 50px; border-radius: 8px; object-fit: cover; }
.drag-sort-icon { margin-left: auto; }

.loading-state { text-align: center; padding: 60px 0; }
.empty-state { text-align: center; padding: 80px 0; }
.empty-state p { font-size: 14px; color: #C7C7CC; margin-top: 12px; }
</style>
