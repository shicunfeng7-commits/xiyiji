<template>
  <div class="order-detail">
    <!-- 时间轴进度 -->
    <div class="timeline-section">
      <div class="timeline-title">订单进度</div>
      <div class="timeline">
        <div
          v-for="(step, index) in timelineSteps"
          :key="step.status"
          class="timeline-item"
          :class="{
            completed: step.status <= currentStatus,
            current: step.status === currentStatus,
            pending: step.status > currentStatus,
          }"
        >
          <div class="timeline-node">
            <van-icon v-if="step.status <= currentStatus" :name="step.icon" size="18" />
            <span v-else>{{ index + 1 }}</span>
          </div>
          <div class="timeline-content">
            <div class="timeline-label">{{ step.label }}</div>
            <div class="timeline-time">{{ getStepTime(step.status) }}</div>
          </div>
          <div v-if="index < timelineSteps.length - 1" class="timeline-line"></div>
        </div>
      </div>
    </div>

    <!-- 订单信息 -->
    <div class="info-card">
      <div class="card-header">
        <span class="order-no">订单号：{{ order?.orderNo }}</span>
        <span class="order-status" :class="getStatusClass(order?.status)">{{ getStatusText(order?.status) }}</span>
      </div>
      
      <div class="info-grid">
        <div class="info-item">
          <van-icon name="location-o" size="16" color="#86868B" />
          <span>{{ order?.buildingName }} · {{ order?.roomNo }}</span>
        </div>
        <div class="info-item">
          <van-icon name="clock-o" size="16" color="#86868B" />
          <span>{{ order?.serviceDate }} {{ order?.startTime }} ~ {{ order?.endTime }}</span>
        </div>
        <div class="info-item">
          <van-icon name="user-o" size="16" color="#86868B" />
          <span>{{ order?.userName }}</span>
        </div>
        <div class="info-item">
          <van-icon name="phone-o" size="16" color="#86868B" />
          <span>{{ order?.phone }}</span>
        </div>
      </div>

      <div class="amount-row">
        <span class="amount-label">服务费用</span>
        <span class="amount-value">¥{{ order?.amount }}</span>
      </div>

      <div v-if="order?.remark" class="remark-row">
        <span class="remark-label">备注</span>
        <span class="remark-value">{{ order?.remark }}</span>
      </div>
    </div>

    <!-- 服务照片（清洗前后对比） -->
    <div v-if="beforePhotos.length > 0 || afterPhotos.length > 0" class="photos-card">
      <div class="card-header">
        <span class="section-title">服务照片</span>
      </div>
      <div class="photos-compare">
        <div class="photo-column" v-if="beforePhotos.length > 0">
          <div class="photo-label">清洗前</div>
          <div class="photo-stack">
            <img v-for="(url, i) in beforePhotos" :key="'b'+i" :src="url" class="photo-img" @click="previewImage(url)" @error="onImgError" />
          </div>
        </div>
        <div class="photo-column" v-if="afterPhotos.length > 0">
          <div class="photo-label">清洗后</div>
          <div class="photo-stack">
            <img v-for="(url, i) in afterPhotos" :key="'a'+i" :src="url" class="photo-img" @click="previewImage(url)" @error="onImgError" />
          </div>
        </div>
      </div>
    </div>

    <!-- 服务评价 -->
    <div v-if="order?.status === 3 && review" class="review-card">
      <div class="card-header">
        <span class="section-title">服务评价</span>
      </div>
      <div class="review-content">
        <div class="review-score">
          <van-icon v-for="i in 5" :key="i" name="star" :size="24" :color="i <= review.score ? '#FFD700' : '#E8E8ED'" />
        </div>
        <div v-if="review.content" class="review-text">{{ review.content }}</div>
        <div class="review-time">{{ formatTime(review.createTime) }}</div>
      </div>
    </div>

    <!-- 操作日志 -->
    <div class="logs-section">
      <div class="section-title">操作记录</div>
      <div v-if="logs.length === 0" class="empty-logs">
        <van-icon name="file-text-o" size="32" color="#C7C7CC" />
        <p>暂无操作记录</p>
      </div>
      <div v-else class="logs-list">
        <div v-for="log in logs" :key="log.id" class="log-item">
          <div class="log-time">{{ formatTime(log.createTime) }}</div>
          <div class="log-content">
            <span class="log-operator">{{ getOperatorType(log.operatorType) }}</span>
            <span class="log-action">{{ getStatusChangeText(log.fromStatus, log.toStatus) }}</span>
          </div>
        </div>
      </div>
    </div>

    <!-- 底部操作按钮 -->
    <div class="bottom-actions">
      <button v-if="order?.status === 0" class="action-btn cancel" @click="handleCancel">取消订单</button>
      <button v-if="order?.status === 0" class="action-btn pay" @click="goPay">去支付</button>
      <button v-if="order?.status === 3 && !hasReview" class="action-btn review" @click="showReviewModal = true">去评价</button>
      <button v-if="order?.status === 3 && hasReview" class="action-btn completed" @click="goHome">返回首页</button>
    </div>

    <!-- 评价弹窗 -->
    <van-dialog v-model:show="showReviewModal" title="评价服务" show-cancel-button>
      <div class="review-modal-content">
        <div class="review-score-section">
          <div class="score-label">评分</div>
          <div class="star-group">
            <van-icon
              v-for="i in 5"
              :key="i"
              name="star"
              :size="40"
              :color="i <= reviewScore ? '#FFD700' : '#E8E8ED'"
              class="star-item"
              @click="reviewScore = i"
            />
          </div>
          <div class="score-text">{{ scoreText }}</div>
        </div>
        <div class="review-input-section">
          <div class="input-label">评价内容</div>
          <textarea
            v-model="reviewContent"
            class="review-textarea"
            placeholder="请输入您的评价..."
            maxlength="500"
          ></textarea>
          <div class="input-count">{{ reviewContent.length }}/500</div>
        </div>
      </div>
      <template #confirm>
        <button class="confirm-btn" @click="submitReview">提交评价</button>
      </template>
    </van-dialog>

    <van-image-preview v-model:show="showPreview" :images="previewImages" />
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted, computed } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { showToast, showLoadingToast, closeToast, showDialog } from 'vant'
import { get, post } from '../../utils/request'

const route = useRoute()
const router = useRouter()

const order = ref<any>(null)
const logs = ref<any[]>([])
const loading = ref(true)
const review = ref<any>(null)
const hasReview = ref(false)
const showReviewModal = ref(false)
const reviewScore = ref(5)
const reviewContent = ref('')
const beforePhotos = ref<string[]>([])
const afterPhotos = ref<string[]>([])
const showPreview = ref(false)
const previewImages = ref<string[]>([])

function onImgError(e: Event) {
  (e.target as HTMLImageElement).style.display = 'none'
}

function previewImage(url: string) {
  previewImages.value = [url]
  showPreview.value = true
}

function parsePhotos(item: any) {
  let b: string[] = [], a: string[] = []
  try { if (item.beforePhoto) b = JSON.parse(item.beforePhoto) } catch { if (item.beforePhoto) b = [item.beforePhoto] }
  try { if (item.afterPhoto) a = JSON.parse(item.afterPhoto) } catch { if (item.afterPhoto) a = [item.afterPhoto] }
  beforePhotos.value = b
  afterPhotos.value = a
}

const timelineSteps = [
  { status: 0, label: '订单创建', icon: 'circle-o' },
  { status: 1, label: '已支付', icon: 'check-circle-o' },
  { status: 2, label: '服务中', icon: 'clock-o' },
  { status: 3, label: '已完成', icon: 'success' },
]

const currentStatus = computed(() => order.value?.status ?? 0)

const statusMap: Record<number, { text: string; class: string }> = {
  0: { text: '未支付', class: 'status-unpaid' },
  1: { text: '待服务', class: 'status-paid' },
  2: { text: '服务中', class: 'status-progress' },
  3: { text: '已完成', class: 'status-completed' },
  4: { text: '已取消', class: 'status-cancelled' },
}

const statusTextMap: Record<number, string> = {
  0: '未支付',
  1: '已支付',
  2: '服务中',
  3: '已完成',
  4: '已取消',
}

function getStatusText(status?: number): string {
  return status !== undefined ? statusMap[status]?.text || '未知' : '未知'
}

function getStatusClass(status?: number): string {
  return status !== undefined ? statusMap[status]?.class || 'status-default' : 'status-default'
}

function getStepTime(status: number): string {
  if (status === 0) return order.value?.createTime?.substring(0, 16) || ''
  if (status === 1) return order.value?.payTime?.substring(0, 16) || ''
  if (status === 3) return order.value?.completeTime?.substring(0, 16) || ''
  return ''
}

function formatTime(timeStr?: string): string {
  if (!timeStr) return ''
  return timeStr.substring(0, 19).replace('T', ' ')
}

function getOperatorType(type?: number): string {
  const types: Record<number, string> = {
    0: '用户',
    1: '管理员',
    2: '员工',
  }
  return types[type ?? 0] || '系统'
}

function getStatusChangeText(from?: number, to?: number): string {
  if (from === null || from === undefined) {
    return `创建订单`
  }
  return `${statusTextMap[from]} → ${statusTextMap[to ?? 0]}`
}

const scoreText = computed(() => {
  const texts = ['', '非常差', '差', '一般', '好', '非常好']
  return texts[reviewScore.value] || ''
})

async function loadReview() {
  if (order.value?.status !== 3) return
  try {
    const orderId = route.query.id
    const res = await get<{ code: number; data: boolean }>(`/api/user/order/review/check/${orderId}`)
    if (res.data.code === 200) {
      hasReview.value = res.data.data
      if (hasReview.value) {
        const reviewRes = await get<{ code: number; data: any }>(`/api/user/order/review/${orderId}`)
        if (reviewRes.data.code === 200) {
          review.value = reviewRes.data.data
        }
      }
    }
  } catch (error) {
    console.log('加载评价失败')
  }
}

async function submitReview() {
  if (reviewScore.value === 0) {
    showToast('请选择评分')
    return
  }
  const orderId = route.query.id
  try {
    const res = await post<{ code: number }>('/api/user/order/review', {
      orderId,
      score: reviewScore.value,
      content: reviewContent.value,
    })
    if (res.data.code === 200) {
      showToast('评价成功')
      showReviewModal.value = false
      hasReview.value = true
      review.value = {
        score: reviewScore.value,
        content: reviewContent.value,
        createTime: new Date().toISOString(),
      }
    }
  } catch (error: any) {
    showToast(error?.response?.data?.msg || '评价失败')
  }
}

async function loadOrder() {
  loading.value = true
  showLoadingToast({ message: '加载中...' })
  try {
    const orderId = route.query.id
    const res = await get<{ code: number; data: any }>(`/api/user/order/detail/${orderId}`)
    if (res.data.code === 200) {
      order.value = res.data.data
      parsePhotos(res.data.data)
    }
  } catch (error) {
    showToast('加载订单失败')
  } finally {
    loading.value = false
    closeToast()
  }
}

async function loadLogs() {
  try {
    const orderId = route.query.id
    const res = await get<{ code: number; data: any[] }>(`/api/admin/order/logs/${orderId}`)
    if (res.data.code === 200) {
      logs.value = res.data.data
    }
  } catch (error) {
    console.log('加载日志失败')
  }
}

async function handleCancel() {
  const orderId = route.query.id
  try {
    const res = await post<{ code: number }>(`/api/user/order/cancel/${orderId}`)
    if (res.data.code === 200) {
      showToast('取消成功')
      setTimeout(() => {
        router.push('/user/orders')
      }, 1500)
    }
  } catch (error) {
    showToast('取消失败')
  }
}

function goPay() {
  router.push(`/user/order/pay?id=${route.query.id}`)
}

function goHome() {
  router.push('/user/home')
}

onMounted(() => {
  loadOrder()
  loadLogs()
  setTimeout(() => {
    loadReview()
  }, 500)
})
</script>

<style scoped>
.order-detail {
  padding: 16px;
  padding-bottom: 120px;
  background: #F5F5F7;
  min-height: 100vh;
}

/* ====== 时间轴 ====== */
.timeline-section {
  background: white;
  border-radius: 16px;
  padding: 20px;
  margin-bottom: 16px;
}

.timeline-title {
  font-size: 17px;
  font-weight: 600;
  color: #1D1D1F;
  margin-bottom: 20px;
}

.timeline {
  position: relative;
  padding-left: 24px;
}

.timeline-item {
  position: relative;
  padding-bottom: 24px;
}

.timeline-item:last-child {
  padding-bottom: 0;
}

.timeline-node {
  position: absolute;
  left: -24px;
  top: 2px;
  width: 32px;
  height: 32px;
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 14px;
  font-weight: 600;
}

.timeline-item.pending .timeline-node {
  background: #F5F5F7;
  color: #C7C7CC;
}

.timeline-item.completed:not(.current) .timeline-node {
  background: #34C759;
  color: white;
}

.timeline-item.current .timeline-node {
  background: #2B95FF;
  color: white;
  box-shadow: 0 0 0 4px rgba(43, 149, 255, 0.15);
}

.timeline-content {
  padding-left: 12px;
}

.timeline-label {
  font-size: 15px;
  font-weight: 500;
  color: #1D1D1F;
  margin-bottom: 4px;
}

.timeline-time {
  font-size: 12px;
  color: #86868B;
}

.timeline-line {
  position: absolute;
  left: -12px;
  top: 36px;
  width: 2px;
  height: calc(100% - 36px);
  background: #F5F5F7;
}

.timeline-item.completed:not(.current) .timeline-line {
  background: #34C759;
}

.timeline-item.current .timeline-line {
  background: #2B95FF;
}

/* ====== 订单信息 ====== */
.photos-card {
  background: white;
  border-radius: 16px;
  padding: 16px;
  margin-bottom: 16px;
}

.photos-compare {
  display: flex;
  gap: 12px;
  margin-top: 12px;
}

.photo-column {
  flex: 1;
}

.photo-label {
  font-size: 13px;
  color: #86868B;
  font-weight: 600;
  margin-bottom: 8px;
}

.photo-stack {
  display: flex;
  flex-direction: column;
  gap: 8px;
}

.photo-img {
  width: 100%;
  aspect-ratio: 1;
  object-fit: cover;
  border-radius: 10px;
  background: #F5F5F7;
  cursor: pointer;
}
.photo-img:active { opacity: 0.8; }

.info-card {
  background: white;
  border-radius: 16px;
  padding: 16px;
  margin-bottom: 16px;
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 16px;
  padding-bottom: 12px;
  border-bottom: 1px solid #F5F5F7;
}

.order-no {
  font-size: 14px;
  color: #86868B;
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

.status-progress {
  background: rgba(142,142,147,0.1);
  color: #8E8E93;
}

.status-completed {
  background: rgba(52,199,89,0.1);
  color: #34C759;
}

.status-cancelled {
  background: rgba(142,142,147,0.1);
  color: #8E8E93;
}

.info-grid {
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: 12px;
}

.info-item {
  display: flex;
  align-items: center;
  gap: 8px;
  font-size: 14px;
  color: #1D1D1F;
}

.amount-row {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-top: 16px;
  padding-top: 12px;
  border-top: 1px solid #F5F5F7;
}

.amount-label {
  font-size: 14px;
  color: #86868B;
}

.amount-value {
  font-size: 22px;
  font-weight: 700;
  color: #2B95FF;
}

.remark-row {
  display: flex;
  gap: 12px;
  margin-top: 12px;
  padding: 12px;
  background: #F9F9FB;
  border-radius: 12px;
}

.remark-label {
  font-size: 13px;
  color: #86868B;
  flex-shrink: 0;
}

.remark-value {
  font-size: 14px;
  color: #1D1D1F;
}

/* ====== 服务评价 ====== */
.review-card {
  background: white;
  border-radius: 16px;
  padding: 16px;
  margin-bottom: 16px;
}

.review-content {
  padding-top: 12px;
}

.review-score {
  margin-bottom: 12px;
}

.review-text {
  font-size: 14px;
  color: #1D1D1F;
  line-height: 1.6;
  margin-bottom: 8px;
}

.review-time {
  font-size: 12px;
  color: #86868B;
}

/* ====== 评价弹窗 ====== */
.review-modal-content {
  padding: 16px 0;
}

.review-score-section {
  text-align: center;
  margin-bottom: 20px;
}

.score-label {
  font-size: 14px;
  color: #86868B;
  margin-bottom: 12px;
  display: block;
}

.star-group {
  display: flex;
  justify-content: center;
  gap: 8px;
  margin-bottom: 8px;
}

.star-item {
  cursor: pointer;
  transition: transform 0.2s;
}

.star-item:active {
  transform: scale(1.1);
}

.score-text {
  font-size: 14px;
  color: #2B95FF;
}

.review-input-section {
  margin-top: 12px;
}

.input-label {
  font-size: 14px;
  color: #86868B;
  margin-bottom: 8px;
  display: block;
}

.review-textarea {
  width: 100%;
  height: 100px;
  padding: 12px;
  border: 1px solid #E8E8ED;
  border-radius: 12px;
  font-size: 14px;
  resize: none;
  box-sizing: border-box;
}

.review-textarea:focus {
  outline: none;
  border-color: #2B95FF;
}

.input-count {
  text-align: right;
  font-size: 12px;
  color: #C7C7CC;
  margin-top: 8px;
}

.confirm-btn {
  width: 100%;
  padding: 12px;
  background: #2B95FF;
  color: white;
  border: none;
  border-radius: 12px;
  font-size: 16px;
  font-weight: 600;
  cursor: pointer;
}

/* ====== 操作日志 ====== */
.logs-section {
  background: white;
  border-radius: 16px;
  padding: 16px;
}

.section-title {
  font-size: 16px;
  font-weight: 600;
  color: #1D1D1F;
  margin-bottom: 16px;
}

.empty-logs {
  text-align: center;
  padding: 32px 0;
  color: #C7C7CC;
}

.empty-logs p {
  margin-top: 8px;
  font-size: 14px;
}

.logs-list {
  display: flex;
  flex-direction: column;
  gap: 12px;
}

.log-item {
  display: flex;
  gap: 16px;
  padding: 12px;
  background: #F9F9FB;
  border-radius: 12px;
}

.log-time {
  font-size: 12px;
  color: #86868B;
  flex-shrink: 0;
  white-space: nowrap;
}

.log-content {
  font-size: 14px;
  color: #1D1D1F;
}

.log-operator {
  font-weight: 600;
  margin-right: 8px;
}

.log-action {
  color: #86868B;
}

/* ====== 底部操作按钮 ====== */
.bottom-actions {
  position: fixed;
  bottom: 0;
  left: 0;
  right: 0;
  display: flex;
  gap: 12px;
  padding: 16px;
  padding-bottom: calc(16px + env(safe-area-inset-bottom));
  background: white;
  box-shadow: 0 -2px 10px rgba(0,0,0,0.05);
}

.action-btn {
  flex: 1;
  padding: 14px;
  border: none;
  border-radius: 14px;
  font-size: 16px;
  font-weight: 600;
  cursor: pointer;
  transition: all 0.2s;
}

.action-btn.cancel {
  background: #F5F5F7;
  color: #86868B;
}

.action-btn.pay {
  background: #2B95FF;
  color: white;
  box-shadow: 0 4px 14px rgba(43,149,255,0.3);
}

.action-btn.complete {
  background: #34C759;
  color: white;
  box-shadow: 0 4px 14px rgba(52,199,89,0.3);
}

.action-btn.review {
  background: linear-gradient(135deg, #FFD700 0%, #FFA500 100%);
  color: white;
  box-shadow: 0 4px 14px rgba(255,215,0,0.3);
}

.action-btn:active {
  transform: scale(0.98);
}
</style>
