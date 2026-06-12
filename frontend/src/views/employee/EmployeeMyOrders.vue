<template>
  <div class="employee-myorders">
    <div class="order-list">
      <div class="order-card" v-for="order in myOrders" :key="order.id">
        <div class="order-header">
          <span class="order-no">{{ order.orderNo }}</span>
          <span class="order-status" :class="order.status">{{ order.statusText }}</span>
        </div>
        <div class="order-body">
          <div class="building-type">{{ order.buildingType }}</div>
          <div class="info-line">{{ order.building }} · {{ order.room }}</div>
          <div class="info-line time">{{ order.date }} {{ order.timeSlot }}</div>
          <div class="info-line" v-if="order.remark">备注: {{ order.remark }}</div>
        </div>

        <!-- 服务中 → 照片上传 + 完成 -->
        <div v-if="order.status === 'in_progress'" class="complete-section">
          <!-- 清洗前照片 -->
          <div class="photo-group">
            <div class="photo-group-title">清洗前照片</div>
            <div class="photo-grid">
              <div v-for="(url, i) in order.beforePhotos" :key="'b'+i" class="photo-item">
                <img :src="url" class="photo-preview" @error="onImgError" />
                <van-icon name="close" class="photo-remove" size="16" @click="removePhoto(order, 'before', i)" />
              </div>
              <div class="photo-add" @click="openPhotoSheet(order, 'before')">
                <van-icon name="plus" size="28" color="#C7C7CC" />
                <span>{{ order.beforePhotos.length ? '' : '添加照片' }}</span>
              </div>
            </div>
          </div>

          <!-- 清洗后照片 -->
          <div class="photo-group">
            <div class="photo-group-title">清洗后照片</div>
            <div class="photo-grid">
              <div v-for="(url, i) in order.afterPhotos" :key="'a'+i" class="photo-item">
                <img :src="url" class="photo-preview" @error="onImgError" />
                <van-icon name="close" class="photo-remove" size="16" @click="removePhoto(order, 'after', i)" />
              </div>
              <div class="photo-add" @click="openPhotoSheet(order, 'after')">
                <van-icon name="plus" size="28" color="#C7C7CC" />
                <span>{{ order.afterPhotos.length ? '' : '添加照片' }}</span>
              </div>
            </div>
          </div>

          <button
            class="action-btn complete-btn"
            :disabled="order.beforePhotos.length === 0 || order.afterPhotos.length === 0 || order.completing"
            @click="handleComplete(order)"
          >
            {{ order.completing ? '提交中...' : '完成服务' }}
          </button>
          <p class="photo-hint" v-if="order.beforePhotos.length === 0 || order.afterPhotos.length === 0">
            请至少各上传一张清洗前后的照片
          </p>
        </div>

        <!-- 已完成 → 显示对比照片 -->
        <div v-if="order.status === 'completed' && (order.beforePhotos.length || order.afterPhotos.length)" class="completed-photos">
          <div class="photo-group" v-if="order.beforePhotos.length">
            <div class="photo-group-title">清洗前</div>
            <div class="photo-grid">
              <img v-for="(url, i) in order.beforePhotos" :key="i" :src="url" class="photo-thumb" @click="previewImage(url)" @error="onImgError" />
            </div>
          </div>
          <div class="photo-group" v-if="order.afterPhotos.length">
            <div class="photo-group-title">清洗后</div>
            <div class="photo-grid">
              <img v-for="(url, i) in order.afterPhotos" :key="i" :src="url" class="photo-thumb" @click="previewImage(url)" @error="onImgError" />
            </div>
          </div>
        </div>

        <div class="order-footer">
          <span class="order-amount">¥{{ order.amount }}</span>
          <span v-if="order.status === 'completed'" class="done-badge">已完成</span>
        </div>
      </div>

      <div v-if="myOrders.length === 0 && !loading" class="empty-state">
        <van-icon name="records-o" size="48" color="#C7C7CC" />
        <p>暂无已接订单</p>
        <span class="sub-text">去抢单页面抢单吧</span>
      </div>
    </div>

    <!-- 拍照/相册选择面板 -->
    <van-action-sheet v-model:show="showPhotoSheet" title="选择照片" :actions="[{ name: '拍照', value: 'camera' }, { name: '从相册选择', value: 'gallery' }]" @select="onPhotoActionSelect" />

    <!-- 文件选择器 -->
    <input ref="cameraInput" type="file" accept="image/*" capture="environment" style="display:none" @change="onFileSelected" />
    <input ref="galleryInput" type="file" accept="image/*" style="display:none" @change="onFileSelected" />
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { showToast, showImagePreview } from 'vant'
import { get, post } from '../../utils/request'

const myOrders = ref<any[]>([])
const loading = ref(true)
const showPhotoSheet = ref(false)
const previewImages = ref<string[]>([])
const cameraInput = ref<HTMLInputElement | null>(null)
const galleryInput = ref<HTMLInputElement | null>(null)

let pendingOrder: any = null
let pendingPhotoType: 'before' | 'after' = 'before'

function onImgError(e: Event) {
  const img = e.target as HTMLImageElement
  img.style.display = 'none'
}

function openPhotoSheet(order: any, type: 'before' | 'after') {
  pendingOrder = order
  pendingPhotoType = type
  showPhotoSheet.value = true
}

function onPhotoActionSelect(action: { name: string; value: string }) {
  showPhotoSheet.value = false
  if (action.value === 'camera') {
    cameraInput.value?.click()
  } else {
    galleryInput.value?.click()
  }
}

async function onFileSelected(e: Event) {
  const file = (e.target as HTMLInputElement).files?.[0]
  if (!file || !pendingOrder) return

  const reader = new FileReader()
  reader.onload = async () => {
    const base64 = (reader.result as string).split(',')[1]
    try {
      const res = await post<{ code: number; data: string }>('/api/employee/upload-photo', { image: base64 })
      if (res.data.code === 200) {
        const url = res.data.data
        if (pendingPhotoType === 'before') {
          if (!pendingOrder.beforePhotos) pendingOrder.beforePhotos = []
          pendingOrder.beforePhotos.push(url)
        } else {
          if (!pendingOrder.afterPhotos) pendingOrder.afterPhotos = []
          pendingOrder.afterPhotos.push(url)
        }
        showToast('照片已添加')
      }
    } catch {
      showToast('上传失败，请重试')
    }
  }
  reader.readAsDataURL(file)
  // Reset input
  if (e.target instanceof HTMLInputElement) e.target.value = ''
}

function removePhoto(order: any, type: 'before' | 'after', index: number) {
  if (type === 'before') {
    order.beforePhotos.splice(index, 1)
  } else {
    order.afterPhotos.splice(index, 1)
  }
}

function previewImage(url: string) {
  showImagePreview({ images: [url] })
}

async function loadOrders() {
  loading.value = true
  try {
    const res = await get<{ code: number; data: any[] }>('/api/employee/orders/my-list')
    if (res.data.code === 200) {
      myOrders.value = res.data.data.map((item: any) => {
        const buildingInfo = item.buildingName?.split(' · ') || ['', '']
        let beforePhotos: string[] = []
        let afterPhotos: string[] = []
        try {
          if (item.beforePhoto) beforePhotos = JSON.parse(item.beforePhoto)
        } catch { if (item.beforePhoto) beforePhotos = [item.beforePhoto] }
        try {
          if (item.afterPhoto) afterPhotos = JSON.parse(item.afterPhoto)
        } catch { if (item.afterPhoto) afterPhotos = [item.afterPhoto] }

        return {
          id: item.id,
          orderNo: item.orderNo,
          buildingType: buildingInfo[0] || '',
          building: buildingInfo[1] || '',
          room: item.roomNo,
          date: item.serviceDate,
          timeSlot: `${item.startTime} ~ ${item.endTime}`,
          amount: item.amount?.toString() || '29.90',
          remark: item.remark,
          status: item.status === 2 ? 'in_progress' : item.status === 3 ? 'completed' : 'pending',
          statusText: item.status === 2 ? '服务中' : item.status === 3 ? '已完成' : '待处理',
          beforePhotos,
          afterPhotos,
          completing: false,
        }
      })
    }
  } catch {
    // Ignore error
  } finally {
    loading.value = false
  }
}

async function handleComplete(order: any) {
  if (order.beforePhotos.length === 0 || order.afterPhotos.length === 0) {
    showToast('请至少各上传一张照片')
    return
  }
  order.completing = true
  try {
    const beforeJson = JSON.stringify(order.beforePhotos)
    const afterJson = JSON.stringify(order.afterPhotos)
    await post(`/api/employee/order/complete/${order.id}?beforePhotos=${encodeURIComponent(beforeJson)}&afterPhotos=${encodeURIComponent(afterJson)}`)
    order.status = 'completed'
    order.statusText = '已完成'
    showToast('服务已完成！')
  } catch {
    showToast('操作失败，请重试')
  } finally {
    order.completing = false
  }
}

onMounted(() => {
  loadOrders()
})
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
  align-items: center;
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
.order-status.in_progress { background: rgba(255,149,0,0.1); color: #FF9500; }
.order-status.completed { background: rgba(52,199,89,0.1); color: #34C759; }

.order-body {
  padding-bottom: 12px;
  border-bottom: 1px solid #F5F5F7;
}

.building-type {
  font-size: 12px;
  color: #2B95FF;
  font-weight: 500;
  margin-bottom: 2px;
}

.info-line {
  font-size: 14px;
  color: #1D1D1F;
  padding: 2px 0;
}
.info-line.time { color: #86868B; font-size: 13px; }

.photo-group {
  padding-top: 12px;
}

.photo-group-title {
  font-size: 13px;
  font-weight: 600;
  color: #1D1D1F;
  margin-bottom: 8px;
}

.photo-grid {
  display: flex;
  flex-wrap: wrap;
  gap: 8px;
}

.photo-item {
  position: relative;
  width: 80px;
  height: 80px;
  border-radius: 10px;
  overflow: hidden;
}

.photo-item .photo-preview {
  width: 100%;
  height: 100%;
  object-fit: cover;
  background: #F5F5F7;
}

.photo-remove {
  position: absolute;
  top: -2px;
  right: -2px;
  background: rgba(0,0,0,0.5);
  color: white;
  border-radius: 50%;
  padding: 2px;
  cursor: pointer;
}

.photo-add {
  width: 80px;
  height: 80px;
  border: 2px dashed #E8E8ED;
  border-radius: 10px;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  gap: 4px;
  cursor: pointer;
  transition: all 0.2s;
  background: #FAFAFA;
  font-size: 11px;
  color: #C7C7CC;
}
.photo-add:active { background: #F0F0F0; border-color: #C7C7CC; }

.complete-section {
  padding-top: 12px;
}

.action-btn {
  width: 100%;
  padding: 12px;
  border: none;
  border-radius: 12px;
  font-size: 15px;
  font-weight: 600;
  cursor: pointer;
  transition: all 0.2s;
  margin-top: 12px;
}
.complete-btn { background: #34C759; color: white; }
.complete-btn:disabled { background: #C7C7CC; cursor: not-allowed; }
.complete-btn:not(:disabled):active { transform: scale(0.97); }

.photo-hint { text-align: center; font-size: 12px; color: #FF9500; margin-top: 6px; }

.completed-photos { padding-top: 12px; }

.photo-thumb {
  width: 80px;
  height: 80px;
  border-radius: 10px;
  object-fit: cover;
  background: #F5F5F7;
  cursor: pointer;
}
.photo-thumb:active { opacity: 0.8; }

.order-footer {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding-top: 12px;
}

.order-amount { font-size: 18px; font-weight: 700; color: #1D1D1F; }

.done-badge {
  font-size: 12px;
  color: #34C759;
  font-weight: 600;
  padding: 4px 12px;
  background: rgba(52,199,89,0.1);
  border-radius: 10px;
}

.empty-state { text-align: center; padding: 80px 0; }
.empty-state p { font-size: 15px; color: #C7C7CC; margin-top: 12px; }
.sub-text { font-size: 13px; color: #E8E8ED; }
</style>
