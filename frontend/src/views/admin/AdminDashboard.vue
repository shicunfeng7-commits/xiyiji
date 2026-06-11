<template>
  <div class="admin-dashboard">
    <div class="page-header">
      <h1 class="page-title">数据看板</h1>
      <div class="date-range">
        <van-dropdown-menu>
          <van-dropdown-item :value="dateRange" :options="dateOptions" @change="handleDateChange" />
        </van-dropdown-menu>
      </div>
    </div>

    <!-- 统计卡片 -->
    <div class="stats-grid">
      <div class="stat-card">
        <div class="stat-icon order-icon">
          <van-icon name="shopping-cart" size="24" />
        </div>
        <div class="stat-content">
          <div class="stat-value">{{ stats.totalOrders }}</div>
          <div class="stat-label">订单总数</div>
        </div>
        <div class="stat-trend" :class="stats.orderTrend > 0 ? 'up' : 'down'">
          {{ stats.orderTrend > 0 ? '↑' : '↓' }} {{ Math.abs(stats.orderTrend) }}%
        </div>
      </div>

      <div class="stat-card">
        <div class="stat-icon revenue-icon">
          <van-icon name="wallet" size="24" />
        </div>
        <div class="stat-content">
          <div class="stat-value">¥{{ stats.totalRevenue }}</div>
          <div class="stat-label">总收入</div>
        </div>
        <div class="stat-trend" :class="stats.revenueTrend > 0 ? 'up' : 'down'">
          {{ stats.revenueTrend > 0 ? '↑' : '↓' }} {{ Math.abs(stats.revenueTrend) }}%
        </div>
      </div>

      <div class="stat-card">
        <div class="stat-icon employee-icon">
          <van-icon name="user-o" size="24" />
        </div>
        <div class="stat-content">
          <div class="stat-value">{{ stats.activeEmployees }}</div>
          <div class="stat-label">活跃员工</div>
        </div>
        <div class="stat-trend" :class="stats.employeeTrend > 0 ? 'up' : 'down'">
          {{ stats.employeeTrend > 0 ? '↑' : '↓' }} {{ Math.abs(stats.employeeTrend) }}%
        </div>
      </div>

      <div class="stat-card">
        <div class="stat-icon review-icon">
          <van-icon name="star" size="24" />
        </div>
        <div class="stat-content">
          <div class="stat-value">{{ stats.avgRating }}</div>
          <div class="stat-label">平均评分</div>
        </div>
        <div class="stat-trend" :class="stats.ratingTrend > 0 ? 'up' : 'down'">
          {{ stats.ratingTrend > 0 ? '↑' : '↓' }} {{ Math.abs(stats.ratingTrend) }}%
        </div>
      </div>
    </div>

    <!-- 订单状态分布 -->
    <div class="section-card">
      <div class="section-header">
        <h2 class="section-title">订单状态分布</h2>
      </div>
      <div class="status-chart">
        <div v-for="item in statusDistribution" :key="item.status" class="status-item">
          <div class="status-bar-wrapper">
            <div 
              class="status-bar" 
              :class="item.status"
              :style="{ width: item.percentage + '%' }"
            ></div>
          </div>
          <div class="status-info">
            <span class="status-name">{{ item.name }}</span>
            <span class="status-count">{{ item.count }}单</span>
            <span class="status-percent">{{ item.percentage }}%</span>
          </div>
        </div>
      </div>
    </div>

    <!-- 营收趋势 -->
    <div class="section-card">
      <div class="section-header">
        <h2 class="section-title">营收趋势</h2>
      </div>
      <div class="revenue-chart">
        <div class="chart-bars">
          <div 
            v-for="(item, index) in revenueTrend" 
            :key="index" 
            class="chart-bar-wrapper"
          >
            <div 
              class="chart-bar" 
              :style="{ height: item.percentage + '%' }"
            >
              <div class="bar-tooltip">¥{{ item.amount }}</div>
            </div>
            <span class="bar-label">{{ item.label }}</span>
          </div>
        </div>
      </div>
    </div>

    <!-- 楼栋订单排行 -->
    <div class="section-card">
      <div class="section-header">
        <h2 class="section-title">楼栋订单排行</h2>
      </div>
      <div class="ranking-list">
        <div v-for="(item, index) in buildingRanking" :key="item.name" class="ranking-item">
          <div class="rank-badge" :class="getRankClass(index)">{{ index + 1 }}</div>
          <div class="rank-info">
            <span class="rank-name">{{ item.name }}</span>
            <span class="rank-count">{{ item.count }}单</span>
          </div>
          <div class="rank-bar-wrapper">
            <div class="rank-bar" :style="{ width: item.percentage + '%' }"></div>
          </div>
        </div>
      </div>
    </div>

    <!-- 员工服务排行 -->
    <div class="section-card">
      <div class="section-header">
        <h2 class="section-title">员工服务排行</h2>
      </div>
      <div class="ranking-list">
        <div v-for="(item, index) in employeeRanking" :key="item.id" class="ranking-item">
          <div class="rank-badge" :class="getRankClass(index)">{{ index + 1 }}</div>
          <div class="rank-info">
            <span class="rank-name">{{ item.name }}</span>
            <span class="rank-count">{{ item.count }}单 · ¥{{ item.revenue }}</span>
          </div>
          <div class="rank-bar-wrapper">
            <div class="rank-bar" :style="{ width: item.percentage + '%' }"></div>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { showLoadingToast, closeToast } from 'vant'
import { get } from '../../utils/request'

const dateRange = ref('week')
const loading = ref(false)

const dateOptions = [
  { text: '本周', value: 'week' },
  { text: '本月', value: 'month' },
  { text: '本季度', value: 'quarter' },
]

const stats = ref({
  totalOrders: 0,
  orderTrend: 0,
  totalRevenue: 0,
  revenueTrend: 0,
  activeEmployees: 0,
  employeeTrend: 0,
  avgRating: 0,
  ratingTrend: 0,
})

const statusDistribution = ref([
  { status: 'unpaid', name: '未支付', count: 0, percentage: 0 },
  { status: 'paid', name: '待服务', count: 0, percentage: 0 },
  { status: 'in_progress', name: '服务中', count: 0, percentage: 0 },
  { status: 'completed', name: '已完成', count: 0, percentage: 0 },
])

const revenueTrend = ref([
  { label: '周一', amount: 0, percentage: 0 },
  { label: '周二', amount: 0, percentage: 0 },
  { label: '周三', amount: 0, percentage: 0 },
  { label: '周四', amount: 0, percentage: 0 },
  { label: '周五', amount: 0, percentage: 0 },
  { label: '周六', amount: 0, percentage: 0 },
  { label: '周日', amount: 0, percentage: 0 },
])

const buildingRanking = ref([
  { name: '食宿楼1栋', count: 0, percentage: 0 },
  { name: '食宿楼2栋', count: 0, percentage: 0 },
  { name: '学生宿舍1栋', count: 0, percentage: 0 },
  { name: '学生宿舍2栋', count: 0, percentage: 0 },
])

const employeeRanking = ref([
  { id: 1, name: '员工A', count: 0, revenue: 0, percentage: 0 },
  { id: 2, name: '员工B', count: 0, revenue: 0, percentage: 0 },
  { id: 3, name: '员工C', count: 0, revenue: 0, percentage: 0 },
])

function getRankClass(index: number): string {
  if (index === 0) return 'rank-1'
  if (index === 1) return 'rank-2'
  if (index === 2) return 'rank-3'
  return ''
}

async function loadStats() {
  loading.value = true
  showLoadingToast({ message: '加载中...' })
  try {
    const res = await get<{ code: number; data: any }>(`/api/admin/dashboard?range=${dateRange.value}`)
    if (res.data.code === 200) {
      const data = res.data.data
      stats.value = {
        totalOrders: data.totalOrders || 0,
        orderTrend: data.orderTrend || 0,
        totalRevenue: data.totalRevenue || 0,
        revenueTrend: data.revenueTrend || 0,
        activeEmployees: data.activeEmployees || 0,
        employeeTrend: data.employeeTrend || 0,
        avgRating: (data.avgRating || 0).toFixed(1),
        ratingTrend: data.ratingTrend || 0,
      }
      
      // 订单状态分布
      const statusData = data.statusDistribution || {}
      const total = Object.values(statusData).reduce((sum: number, val: number) => sum + val, 0) || 1
      statusDistribution.value = [
        { status: 'unpaid', name: '未支付', count: statusData.unpaid || 0, percentage: Math.round(((statusData.unpaid || 0) / total) * 100) },
        { status: 'paid', name: '待服务', count: statusData.paid || 0, percentage: Math.round(((statusData.paid || 0) / total) * 100) },
        { status: 'in_progress', name: '服务中', count: statusData.inProgress || 0, percentage: Math.round(((statusData.inProgress || 0) / total) * 100) },
        { status: 'completed', name: '已完成', count: statusData.completed || 0, percentage: Math.round(((statusData.completed || 0) / total) * 100) },
      ]
      
      // 营收趋势
      const revenueData = data.revenueTrend || []
      const maxRevenue = Math.max(...revenueData.map((item: any) => item.amount || 0), 1)
      revenueTrend.value = revenueData.map((item: any, index: number) => ({
        label: ['周一', '周二', '周三', '周四', '周五', '周六', '周日'][index],
        amount: item.amount || 0,
        percentage: Math.round(((item.amount || 0) / maxRevenue) * 100),
      }))
      
      // 楼栋排行
      const buildingData = data.buildingRanking || []
      const maxBuilding = Math.max(...buildingData.map((item: any) => item.count || 0), 1)
      buildingRanking.value = buildingData.slice(0, 4).map(item => ({
        name: item.name || '',
        count: item.count || 0,
        percentage: Math.round(((item.count || 0) / maxBuilding) * 100),
      }))
      
      // 员工排行
      const employeeData = data.employeeRanking || []
      const maxEmployee = Math.max(...employeeData.map((item: any) => item.count || 0), 1)
      employeeRanking.value = employeeData.slice(0, 3).map(item => ({
        id: item.id || 0,
        name: item.name || '',
        count: item.count || 0,
        revenue: item.revenue || 0,
        percentage: Math.round(((item.count || 0) / maxEmployee) * 100),
      }))
    }
  } catch (error) {
    console.error('加载数据失败:', error)
  } finally {
    loading.value = false
    closeToast()
  }
}

function handleDateChange(value: string) {
  dateRange.value = value
  loadStats()
}

onMounted(() => {
  loadStats()
})
</script>

<style scoped>
.admin-dashboard {
  padding: 16px;
  padding-bottom: 100px;
  background: #F5F5F7;
  min-height: 100vh;
}

.page-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 20px;
}

.page-title {
  font-size: 22px;
  font-weight: 700;
  color: #1D1D1F;
}

.date-range {
  width: 120px;
}

/* ====== 统计卡片 ====== */
.stats-grid {
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: 12px;
  margin-bottom: 20px;
}

.stat-card {
  background: white;
  border-radius: 16px;
  padding: 16px;
  position: relative;
  overflow: hidden;
}

.stat-icon {
  width: 44px;
  height: 44px;
  border-radius: 12px;
  display: flex;
  align-items: center;
  justify-content: center;
  margin-bottom: 12px;
  color: white;
}

.order-icon {
  background: linear-gradient(135deg, #2B95FF, #007AFF);
}

.revenue-icon {
  background: linear-gradient(135deg, #34C759, #30D158);
}

.employee-icon {
  background: linear-gradient(135deg, #FF9500, #FF6D00);
}

.review-icon {
  background: linear-gradient(135deg, #FFCC00, #FFB600);
}

.stat-content {
  margin-bottom: 8px;
}

.stat-value {
  font-size: 24px;
  font-weight: 700;
  color: #1D1D1F;
}

.stat-label {
  font-size: 12px;
  color: #86868B;
  margin-top: 2px;
}

.stat-trend {
  font-size: 12px;
  font-weight: 600;
  position: absolute;
  top: 16px;
  right: 16px;
}

.stat-trend.up {
  color: #34C759;
}

.stat-trend.down {
  color: #FF3B30;
}

/* ====== 区域卡片 ====== */
.section-card {
  background: white;
  border-radius: 16px;
  padding: 16px;
  margin-bottom: 16px;
}

.section-header {
  margin-bottom: 16px;
}

.section-title {
  font-size: 16px;
  font-weight: 600;
  color: #1D1D1F;
}

/* ====== 状态分布图 ====== */
.status-chart {
  display: flex;
  flex-direction: column;
  gap: 12px;
}

.status-item {
  display: flex;
  align-items: center;
  gap: 12px;
}

.status-bar-wrapper {
  flex: 1;
  height: 12px;
  background: #F5F5F7;
  border-radius: 6px;
  overflow: hidden;
}

.status-bar {
  height: 100%;
  border-radius: 6px;
  transition: width 0.3s ease;
}

.status-bar.unpaid {
  background: #FF9500;
}

.status-bar.paid {
  background: #2B95FF;
}

.status-bar.in_progress {
  background: #8E8E93;
}

.status-bar.completed {
  background: #34C759;
}

.status-info {
  width: 100px;
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.status-name {
  font-size: 13px;
  color: #1D1D1F;
}

.status-count {
  font-size: 12px;
  color: #86868B;
}

.status-percent {
  font-size: 12px;
  font-weight: 600;
  color: #2B95FF;
}

/* ====== 营收趋势图 ====== */
.revenue-chart {
  padding: 8px 0;
}

.chart-bars {
  display: flex;
  justify-content: space-between;
  align-items: flex-end;
  height: 160px;
  padding-top: 20px;
}

.chart-bar-wrapper {
  display: flex;
  flex-direction: column;
  align-items: center;
  width: 12%;
  height: 100%;
  justify-content: flex-end;
}

.chart-bar {
  width: 28px;
  background: linear-gradient(180deg, #2B95FF 0%, #007AFF 100%);
  border-radius: 6px 6px 0 0;
  position: relative;
  transition: height 0.3s ease;
  min-height: 4px;
}

.bar-tooltip {
  position: absolute;
  top: -28px;
  left: 50%;
  transform: translateX(-50%);
  background: rgba(0,0,0,0.8);
  color: white;
  font-size: 11px;
  padding: 4px 8px;
  border-radius: 4px;
  white-space: nowrap;
  opacity: 0;
  pointer-events: none;
  transition: opacity 0.2s;
}

.chart-bar:hover .bar-tooltip {
  opacity: 1;
}

.bar-label {
  font-size: 11px;
  color: #86868B;
  margin-top: 8px;
}

/* ====== 排行榜 ====== */
.ranking-list {
  display: flex;
  flex-direction: column;
  gap: 12px;
}

.ranking-item {
  display: flex;
  align-items: center;
  gap: 10px;
}

.rank-badge {
  width: 24px;
  height: 24px;
  border-radius: 50%;
  background: #F5F5F7;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 12px;
  font-weight: 600;
  color: #86868B;
}

.rank-badge.rank-1 {
  background: linear-gradient(135deg, #FFD700, #FFA500);
  color: white;
}

.rank-badge.rank-2 {
  background: linear-gradient(135deg, #C0C0C0, #A8A8A8);
  color: white;
}

.rank-badge.rank-3 {
  background: linear-gradient(135deg, #CD7F32, #B87333);
  color: white;
}

.rank-info {
  width: 140px;
}

.rank-name {
  font-size: 13px;
  color: #1D1D1F;
  font-weight: 500;
}

.rank-count {
  font-size: 11px;
  color: #86868B;
  margin-left: 8px;
}

.rank-bar-wrapper {
  flex: 1;
  height: 8px;
  background: #F5F5F7;
  border-radius: 4px;
  overflow: hidden;
}

.rank-bar {
  height: 100%;
  background: linear-gradient(90deg, #2B95FF, #007AFF);
  border-radius: 4px;
  transition: width 0.3s ease;
}
</style>
