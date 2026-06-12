<template>
  <div class="order-create">
    <div class="form-card">
      <!-- 楼栋选择 -->
      <div class="form-section">
        <div class="form-label">楼栋</div>
        <van-field
          :model-value="buildingDisplay"
          placeholder="选择楼栋"
          is-link
          readonly
          @click="openBuildingPopup"
        />
        <van-popup v-model:show="showBuildingPopup" position="bottom" round :style="{ minHeight: '50vh' }">
          <div class="building-picker">
            <div class="bp-header">选择楼栋</div>

            <!-- 分类卡片 -->
            <div class="bp-categories">
              <div
                v-for="cat in categories"
                :key="cat.key"
                class="bp-category"
                :class="{ active: tempCategory === cat.key }"
                @click="tempCategory = cat.key; tempBuilding = ''"
              >
                <div class="bp-cat-icon" :style="{ background: cat.bg }">
                  <van-icon :name="cat.icon" :color="cat.color" size="20" />
                </div>
                <div class="bp-cat-name">{{ cat.label }}</div>
              </div>
            </div>

            <!-- 楼栋网格 -->
            <div class="bp-grid-label">选择{{ tempCategoryLabel }}</div>
            <div class="bp-grid">
              <div
                v-for="b in tempBuildings"
                :key="b"
                class="bp-item"
                :class="{ active: tempBuilding === b }"
                @click="tempBuilding = b"
              >
                {{ b }}
              </div>
            </div>

            <div class="bp-footer">
              <button class="bp-confirm-btn" :disabled="!tempBuilding" @click="confirmBuilding">
                {{ tempBuilding ? `确定选择 ${tempCategoryLabel} ${tempBuilding}` : '请选择楼栋' }}
              </button>
            </div>
          </div>
        </van-popup>
      </div>

      <!-- 房间号 -->
      <div class="form-section">
        <div class="form-label">房间号</div>
        <van-field v-model="roomNo" placeholder="例如：301、502" clearable />
      </div>

      <!-- 手机号 -->
      <div class="form-section">
        <div class="form-label">联系手机号</div>
        <van-field v-model="contactPhone" placeholder="请输入联系电话" clearable maxlength="11" type="tel" />
      </div>

      <!-- 上门日期 -->
      <div class="form-section">
        <div class="form-label">上门日期</div>
        <van-field
          :model-value="serviceDate"
          placeholder="选择日期"
          is-link
          readonly
          @click="showDatePicker = true"
        />
        <van-popup v-model:show="showDatePicker" position="bottom" round>
          <van-date-picker
            title="选择日期"
            :min-date="minDate"
            @confirm="onDateConfirm"
            @cancel="showDatePicker = false"
          />
        </van-popup>
      </div>

      <!-- 时间段 -->
      <div class="form-section">
        <div class="form-label">上门时间段</div>
        <van-field
          :model-value="timeRangeDisplay"
          placeholder="选择起始时间 ~ 结束时间"
          is-link
          readonly
          @click="openTimeRange"
        />
        <van-popup v-model:show="showTimeRange" position="bottom" round :style="{ minHeight: '50vh' }">
          <div class="time-range-picker">
            <div class="tr-header">选择时间段</div>

            <!-- 24小时时间线 -->
            <div class="tr-slots">
              <div
                v-for="(slot, i) in timeSlots"
                :key="i"
                class="tr-slot"
                :class="{
                  'is-start': tempStartTime === slot,
                  'is-end': tempEndTime === slot,
                  'in-range': tempStartTime && tempEndTime && isInRange(slot),
                  'is-past': isPastSlot(slot),
                }"
                @click="!isPastSlot(slot) && selectTimeSlot(slot)"
              >
                <span class="tr-slot-label">{{ slot }}</span>
                <span v-if="tempStartTime === slot" class="tr-badge start">起始</span>
                <span v-if="tempEndTime === slot" class="tr-badge end">结束</span>
              </div>
            </div>

            <div class="tr-footer">
              <span class="tr-selected">
                {{ tempStartTime && tempEndTime ? `${tempStartTime} ~ ${tempEndTime}` : '请点选起始时间和结束时间' }}
              </span>
              <button class="tr-confirm-btn" :disabled="!tempStartTime || !tempEndTime" @click="confirmTime">确定</button>
            </div>
          </div>
        </van-popup>
      </div>

      <!-- 备注 -->
      <div class="form-section">
        <div class="form-label">备注</div>
        <van-field v-model="remark" type="textarea" placeholder="例如：洗衣机品牌、特殊需求..." rows="3" autosize />
      </div>
    </div>

    <div class="price-card">
      <div class="price-row">
        <span class="price-label">清洗服务费</span>
        <span class="price-amount">¥29.9</span>
      </div>
      <div class="price-row total">
        <span class="price-label">合计</span>
        <span class="price-total">¥29.9</span>
      </div>
    </div>

    <div class="submit-area">
      <button class="submit-btn" @click="handleSubmit">提交订单</button>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, computed, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { showToast, showLoadingToast, closeToast, showDialog } from 'vant'
import { post } from '../../utils/request'
import { isLoggedIn } from '../../utils/auth'

const router = useRouter()

// ---- 楼栋选择 ----
const showBuildingPopup = ref(false)
const selectedCategory = ref('')
const buildingName = ref('')
const tempCategory = ref('')
const tempBuilding = ref('')

// 预存上一次选择的分类，默认选中第一个
const categories = [
  { key: 'dorm', label: '食宿楼', icon: 'home-o', color: '#2B95FF', bg: 'rgba(43,149,255,0.1)' },
  { key: 'student', label: '学生宿舍', icon: 'friends-o', color: '#34C759', bg: 'rgba(52,199,89,0.1)' },
  { key: 'teacher', label: '教师公寓', icon: 'building-o', color: '#FF9500', bg: 'rgba(255,149,0,0.1)' },
]

const buildingMap: Record<string, string[]> = {
  dorm: ['1栋', '2栋', '3栋', '4栋'],
  student: ['1栋', '2栋', '3栋', '4栋'],
  teacher: ['A栋', 'B栋', 'C栋', 'D栋'],
}

const currentCategoryLabel = computed(() => {
  const found = categories.find(c => c.key === selectedCategory.value)
  return found ? found.label : ''
})

const currentBuildings = computed(() => {
  return buildingMap[selectedCategory.value] || []
})

const tempCategoryLabel = computed(() => {
  const found = categories.find(c => c.key === tempCategory.value)
  return found ? found.label : ''
})

const tempBuildings = computed(() => {
  return buildingMap[tempCategory.value] || []
})

const buildingDisplay = computed(() => {
  const cat = categories.find(c => c.key === selectedCategory.value)
  if (cat && buildingName.value) {
    return `${cat.label} · ${buildingName.value}`
  }
  return ''
})

function selectBuilding(b: string) {
  tempBuilding.value = b
}

function confirmBuilding() {
  selectedCategory.value = tempCategory.value
  buildingName.value = tempBuilding.value
  showBuildingPopup.value = false
}

// 打开弹窗时初始化临时变量
function openBuildingPopup() {
  tempCategory.value = selectedCategory.value || 'dorm'
  tempBuilding.value = buildingName.value
  showBuildingPopup.value = true
}

// ---- 房间号 ----
const roomNo = ref('')
const contactPhone = ref('')

// ---- 日期 ----
const showDatePicker = ref(false)
const minDate = new Date()
const serviceDate = ref('')

function onDateConfirm({ selectedValues }: { selectedValues: string[] }) {
  serviceDate.value = `${selectedValues[0]}-${selectedValues[1]}-${selectedValues[2]}`
  showDatePicker.value = false
}

// ---- 时间段（24小时点选） ----
const showTimeRange = ref(false)
const startTime = ref('')
const endTime = ref('')
const tempStartTime = ref('')
const tempEndTime = ref('')

const timeSlots = Array.from({ length: 24 }, (_, i) =>
  `${String(i).padStart(2, '0')}:00`
)

// 当天已过的时间段不可选
const isToday = computed(() => {
  if (!serviceDate.value) return false
  const today = new Date()
  const d = serviceDate.value.split('-').map(Number)
  return d[0] === today.getFullYear() && d[1] === today.getMonth()+1 && d[2] === today.getDate()
})

const currentHour = new Date().getHours()

function isPastSlot(slot: string): boolean {
  if (!isToday.value) return false
  return parseInt(slot) <= currentHour
}

const timeRangeDisplay = computed(() => {
  if (startTime.value && endTime.value) {
    return `${startTime.value} ~ ${endTime.value}`
  }
  return ''
})

function isInRange(slot: string) {
  if (!tempStartTime.value || !tempEndTime.value) return false
  const s = parseInt(slot)
  const a = parseInt(tempStartTime.value)
  const b = parseInt(tempEndTime.value)
  return s > a && s < b
}

function selectTimeSlot(slot: string) {
  if (!tempStartTime.value || (tempStartTime.value && tempEndTime.value)) {
    // 首次点击或重新开始
    tempStartTime.value = slot
    tempEndTime.value = ''
  } else if (slot === tempStartTime.value) {
    // 点击同一个，取消
    tempStartTime.value = ''
  } else {
    // 点结束时间，保证结束 > 开始
    const s = parseInt(slot)
    const st = parseInt(tempStartTime.value)
    if (s <= st) {
      showToast('结束时间必须晚于起始时间')
      return
    }
    tempEndTime.value = slot
  }
}

function confirmTime() {
  startTime.value = tempStartTime.value
  endTime.value = tempEndTime.value
  showTimeRange.value = false
}

function openTimeRange() {
  tempStartTime.value = startTime.value || ''
  tempEndTime.value = endTime.value || ''
  showTimeRange.value = true
}

// ---- 备注 ----
const remark = ref('')

// ---- 提交状态 ----
const isSubmitting = ref(false)

function collectFormData() {
  return {
    selectedCategory: selectedCategory.value,
    buildingName: buildingName.value,
    roomNo: roomNo.value,
    contactPhone: contactPhone.value,
    serviceDate: serviceDate.value,
    startTime: startTime.value,
    endTime: endTime.value,
    remark: remark.value,
  }
}

function restoreFormData(data: any) {
  selectedCategory.value = data.selectedCategory
  buildingName.value = data.buildingName
  tempCategory.value = data.selectedCategory
  tempBuilding.value = data.buildingName
  roomNo.value = data.roomNo
  contactPhone.value = data.contactPhone
  serviceDate.value = data.serviceDate
  startTime.value = data.startTime
  endTime.value = data.endTime
  remark.value = data.remark
}

// ---- 提交 ----
async function handleSubmit() {
  if (isSubmitting.value) {
    showToast('请稍候，正在提交...')
    return
  }
  
  if (!selectedCategory.value || !buildingName.value) {
    showToast('请选择楼栋')
    return
  }
  if (!roomNo.value) {
    showToast('请输入房间号')
    return
  }
  if (!serviceDate.value) {
    showToast('请选择上门日期')
    return
  }
  if (!startTime.value || !endTime.value) {
    showToast('请选择时间段')
    return
  }

  // 检查登录状态
  if (!isLoggedIn()) {
    showDialog({
      title: '请先登录',
      message: '登录后即可享受订单跟踪、预约提醒等服务',
      confirmButtonText: '去登录',
      cancelButtonText: '暂不登录',
    }).then(() => {
      // 保存表单数据
      sessionStorage.setItem('order_form_data', JSON.stringify(collectFormData()))
      sessionStorage.setItem('order_redirect', 'true')
      router.push('/user/profile')
    }).catch(() => {
      // 用户取消，留在当前页
    })
    return
  }

  // 已登录，执行提交
  await doSubmit()
}

async function doSubmit() {
  isSubmitting.value = true
  showLoadingToast({ message: '提交中...' })
  try {
    const res = await post<{ code: number; data: { id: number } }>('/api/user/order/create', {
      buildingCategory: selectedCategory.value,
      buildingName: `${currentCategoryLabel.value} · ${buildingName.value}`,
      roomNo: roomNo.value,
      serviceDate: serviceDate.value,
      startTime: startTime.value,
      endTime: endTime.value,
      remark: remark.value,
      contactPhone: contactPhone.value,
      amount: 29.9,
    })
    if (res.data.code === 200) {
      showToast('订单创建成功')
      setTimeout(() => {
        router.push(`/user/order/pay?id=${res.data.data.id}`)
      }, 1500)
    }
  } catch (error) {
    showToast('订单创建失败，请重试')
  } finally {
    isSubmitting.value = false
    closeToast()
  }
}

onMounted(() => {
  // 检查是否从登录页回跳
  if (sessionStorage.getItem('order_redirect') === 'true') {
    sessionStorage.removeItem('order_redirect')
    const saved = sessionStorage.getItem('order_form_data')
    if (saved) {
      try {
        const data = JSON.parse(saved)
        restoreFormData(data)
        sessionStorage.removeItem('order_form_data')
        // 自动提交
        setTimeout(() => { doSubmit() }, 300)
      } catch { /* ignore parse error */ }
    }
  }
})
</script>

<style scoped>
.order-create {
  padding: 16px;
  padding-bottom: 100px;
}

.form-card {
  background: white;
  border-radius: 16px;
  overflow: hidden;
  box-shadow: 0 1px 3px rgba(0,0,0,0.04);
}

.form-section {
  padding: 4px 16px;
}

.form-section + .form-section {
  border-top: 1px solid #F5F5F7;
}

.form-label {
  font-size: 13px;
  font-weight: 500;
  color: #86868B;
  padding: 12px 0 0;
  letter-spacing: 0.01em;
}

:deep(.van-field) {
  padding: 8px 0 12px;
}

:deep(.van-field__control) {
  font-size: 16px;
  color: #1D1D1F;
}

:deep(.van-field__control::placeholder) {
  color: #C7C7CC;
}

.price-card {
  background: white;
  border-radius: 16px;
  padding: 16px;
  margin-top: 16px;
  box-shadow: 0 1px 3px rgba(0,0,0,0.04);
}

.price-row {
  display: flex;
  justify-content: space-between;
  padding: 4px 0;
}

.price-row.total {
  border-top: 1px solid #F5F5F7;
  margin-top: 8px;
  padding-top: 12px;
}

.price-label {
  font-size: 14px;
  color: #86868B;
}

.price-amount {
  font-size: 14px;
  color: #1D1D1F;
  font-weight: 500;
}

.price-total {
  font-size: 22px;
  font-weight: 700;
  color: #2B95FF;
}

.submit-area {
  margin-top: 24px;
}

.submit-btn {
  width: 100%;
  padding: 16px;
  background: #2B95FF;
  color: white;
  border: none;
  border-radius: 14px;
  font-size: 17px;
  font-weight: 600;
  cursor: pointer;
  transition: all 0.2s;
  box-shadow: 0 4px 14px rgba(43,149,255,0.3);
}

.submit-btn:active {
  transform: scale(0.98);
  box-shadow: 0 2px 8px rgba(43,149,255,0.2);
}

/* ====== 楼栋选择器 ====== */
.building-picker {
  padding: 20px 16px 24px;
}

.bp-header {
  text-align: center;
  font-size: 17px;
  font-weight: 700;
  color: #1D1D1F;
  margin-bottom: 20px;
  letter-spacing: -0.02em;
}

.bp-categories {
  display: flex;
  gap: 10px;
  margin-bottom: 20px;
}

.bp-category {
  flex: 1;
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 6px;
  padding: 14px 8px;
  background: #F5F5F7;
  border-radius: 14px;
  cursor: pointer;
  transition: all 0.2s;
  border: 2px solid transparent;
}

.bp-category.active {
  background: white;
  border-color: #2B95FF;
  box-shadow: 0 2px 8px rgba(43,149,255,0.12);
}

.bp-cat-icon {
  width: 40px;
  height: 40px;
  border-radius: 12px;
  display: flex;
  align-items: center;
  justify-content: center;
}

.bp-cat-name {
  font-size: 13px;
  font-weight: 600;
  color: #1D1D1F;
}

.bp-grid-label {
  font-size: 13px;
  font-weight: 500;
  color: #86868B;
  margin-bottom: 10px;
}

.bp-grid {
  display: grid;
  grid-template-columns: repeat(4, 1fr);
  gap: 10px;
  margin-bottom: 20px;
}

.bp-item {
  padding: 14px 0;
  text-align: center;
  background: #F5F5F7;
  border-radius: 12px;
  font-size: 16px;
  font-weight: 600;
  color: #1D1D1F;
  cursor: pointer;
  transition: all 0.2s;
}

.bp-item:active {
  transform: scale(0.95);
}

.bp-item.active {
  background: #2B95FF;
  color: white;
  box-shadow: 0 2px 8px rgba(43,149,255,0.3);
}

.bp-footer {
  text-align: center;
}

.bp-confirm-btn {
  width: 100%;
  padding: 14px;
  background: #2B95FF;
  color: white;
  border: none;
  border-radius: 12px;
  font-size: 16px;
  font-weight: 600;
  cursor: pointer;
  transition: all 0.2s;
}

.bp-confirm-btn:disabled {
  background: #E8E8ED;
  color: #C7C7CC;
  cursor: not-allowed;
}

.bp-confirm-btn:not(:disabled):active {
  transform: scale(0.98);
}

/* ====== 时间段选择器 ====== */
.time-range-picker {
  padding: 20px 16px 24px;
}

.tr-header {
  text-align: center;
  font-size: 17px;
  font-weight: 700;
  color: #1D1D1F;
  margin-bottom: 16px;
  letter-spacing: -0.02em;
}

.tr-slots {
  display: grid;
  grid-template-columns: repeat(4, 1fr);
  gap: 8px;
  margin-bottom: 20px;
  max-height: 400px;
  overflow-y: auto;
  padding: 4px 0;
}

.tr-slot {
  position: relative;
  padding: 12px 0;
  text-align: center;
  background: #F5F5F7;
  border-radius: 10px;
  font-size: 13px;
  font-weight: 500;
  color: #1D1D1F;
  cursor: pointer;
  transition: all 0.2s;
}

.tr-slot:active {
  transform: scale(0.93);
}

.tr-slot.in-range {
  background: rgba(43,149,255,0.1);
  color: #2B95FF;
}

.tr-slot.is-start {
  background: #2B95FF;
  color: white;
  box-shadow: 0 2px 6px rgba(43,149,255,0.3);
}

.tr-slot.is-end {
  background: #2B95FF;
  color: white;
  box-shadow: 0 2px 6px rgba(43,149,255,0.3);
}

.tr-slot.is-past {
  background: #F0F0F0;
  color: #C7C7CC;
  cursor: not-allowed;
  pointer-events: none;
}

.tr-badge {
  position: absolute;
  top: -6px;
  right: -4px;
  font-size: 9px;
  font-weight: 700;
  padding: 1px 5px;
  border-radius: 6px;
  color: white;
}

.tr-badge.start {
  background: #34C759;
}

.tr-badge.end {
  background: #FF3B30;
}

.tr-footer {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 12px;
}

.tr-selected {
  font-size: 14px;
  font-weight: 600;
  color: #1D1D1F;
  flex: 1;
}

.tr-confirm-btn {
  padding: 12px 28px;
  background: #2B95FF;
  color: white;
  border: none;
  border-radius: 12px;
  font-size: 15px;
  font-weight: 600;
  cursor: pointer;
  transition: all 0.2s;
  white-space: nowrap;
}

.tr-confirm-btn:disabled {
  background: #E8E8ED;
  color: #C7C7CC;
  cursor: not-allowed;
}

.tr-confirm-btn:not(:disabled):active {
  transform: scale(0.96);
}
</style>