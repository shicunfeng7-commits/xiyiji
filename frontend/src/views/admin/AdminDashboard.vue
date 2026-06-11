<template>
  <div class="dashboard">
    <div class="dash-header">
      <div>
        <h1 class="dash-title">数据看板</h1>
        <p class="dash-date">{{ today }}</p>
      </div>
      <div class="header-right">
        <div class="range-select">
          <span v-for="r in ranges" :key="r.key" class="range-item" :class="{ active: dateRange === r.key }" @click="switchRange(r.key)">{{ r.label }}</span>
        </div>
        <span class="logout-link" @click="handleLogout">退出</span>
      </div>
    </div>

    <!-- KPI 卡片 -->
    <div class="kpi-grid">
      <div class="kpi-card">
        <div class="kpi-icon orders-icon">
          <van-icon name="shopping-cart-o" size="20" />
        </div>
        <div class="kpi-value">{{ stats.totalOrders }}</div>
        <div class="kpi-label">订单总数</div>
      </div>
      <div class="kpi-card">
        <div class="kpi-icon revenue-icon">
          <van-icon name="gold-coin-o" size="20" />
        </div>
        <div class="kpi-value">¥{{ stats.totalRevenue }}</div>
        <div class="kpi-label">总收入</div>
      </div>
      <div class="kpi-card">
        <div class="kpi-icon emp-icon">
          <van-icon name="friends-o" size="20" />
        </div>
        <div class="kpi-value">{{ stats.activeEmployees }}</div>
        <div class="kpi-label">活跃员工</div>
      </div>
      <div class="kpi-card">
        <div class="kpi-icon star-icon">
          <van-icon name="star-o" size="20" />
        </div>
        <div class="kpi-value">{{ stats.avgRating }}</div>
        <div class="kpi-label">平均评分</div>
      </div>
    </div>

    <!-- 状态分布 -->
    <div class="section">
      <h2 class="section-title">订单状态</h2>
      <div class="status-bars">
        <div v-for="s in statusDistribution" :key="s.status" class="status-row">
          <span class="s-name">{{ s.name }}</span>
          <div class="s-bar-wrap"><div class="s-bar" :class="s.status" :style="{ width: s.percentage + '%' }"></div></div>
          <span class="s-count">{{ s.count }}</span>
        </div>
      </div>
    </div>

    <!-- 营收趋势 -->
    <div class="section">
      <h2 class="section-title">营收趋势</h2>
      <div class="chart-bars">
        <div v-for="(item, i) in revenueTrend" :key="i" class="chart-col">
          <div class="chart-bar" :style="{ height: (item.percentage || 4) + '%' }">
            <span class="bar-val">¥{{ item.amount }}</span>
          </div>
          <span class="bar-label">{{ item.label }}</span>
        </div>
      </div>
    </div>

    <!-- 楼栋排行 -->
    <div class="section">
      <h2 class="section-title">楼栋排行</h2>
      <div class="rank-list">
        <div v-for="(item, i) in buildingRanking" :key="i" class="rank-row">
          <span class="rank-num" :class="i < 3 ? 'top' + i : ''">{{ i + 1 }}</span>
          <span class="rank-name">{{ item.name }}</span>
          <span class="rank-count">{{ item.count }}单</span>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted, computed } from 'vue'
import { useRouter } from 'vue-router'
import { showLoadingToast, closeToast } from 'vant'
import { get } from '../../utils/request'
import { removeAuth } from '../../utils/auth'

const router = useRouter()
const dateRange = ref('week')
const ranges = [
  { key: 'week', label: '本周' },
  { key: 'month', label: '本月' },
  { key: 'quarter', label: '季度' },
]

const today = computed(() => {
  const d = new Date()
  return `${d.getFullYear()}年${d.getMonth()+1}月${d.getDate()}日`
})

const stats = ref({ totalOrders: 0, totalRevenue: 0, activeEmployees: 0, avgRating: '0.0' })
const statusDistribution = ref<any[]>([])
const revenueTrend = ref<any[]>([])
const buildingRanking = ref<any[]>([])

function switchRange(key: string) { dateRange.value = key; loadData() }

function handleLogout() { removeAuth(); router.push('/login') }

async function loadData() {
  showLoadingToast({ message: '加载中...', forbidClick: true })
  try {
    const res = await get<{ code: number; data: any }>(`/api/admin/dashboard?range=${dateRange.value}`)
    if (res.data.code === 200) {
      const d = res.data.data
      stats.value = {
        totalOrders: d.totalOrders || 0,
        totalRevenue: d.totalRevenue || 0,
        activeEmployees: d.activeEmployees || 0,
        avgRating: (d.avgRating || 0).toFixed(1),
      }

      const sd = d.statusDistribution || {}
      const total = Object.values(sd).reduce((s: number, v: number) => s + v, 0) || 1
      statusDistribution.value = [
        { status: 'unpaid', name: '未支付', count: sd.unpaid || 0, percentage: Math.round(((sd.unpaid || 0) / total) * 100) },
        { status: 'paid', name: '待服务', count: sd.paid || 0, percentage: Math.round(((sd.paid || 0) / total) * 100) },
        { status: 'progress', name: '服务中', count: sd.inProgress || 0, percentage: Math.round(((sd.inProgress || 0) / total) * 100) },
        { status: 'done', name: '已完成', count: sd.completed || 0, percentage: Math.round(((sd.completed || 0) / total) * 100) },
      ]

      const rd = d.revenueTrend || []
      const max = Math.max(...rd.map((i: any) => i.amount || 0), 1)
      const labels = ['周一','周二','周三','周四','周五','周六','周日']
      revenueTrend.value = rd.map((i: any, idx: number) => ({
        label: labels[idx], amount: i.amount || 0, percentage: Math.round(((i.amount || 0) / max) * 100),
      }))

      const bd = d.buildingRanking || []
      const bMax = Math.max(...bd.map((i: any) => i.count || 0), 1)
      buildingRanking.value = bd.slice(0, 5).map((i: any) => ({
        name: i.name, count: i.count || 0, percentage: Math.round(((i.count || 0) / bMax) * 100),
      }))
    }
  } catch { /* ignore */ } finally { closeToast() }
}

onMounted(loadData)
</script>

<style scoped>
.dashboard { padding: 20px 16px 80px; min-height: 100vh; background: #F5F5F7; }

.dash-header { margin-bottom: 20px; }
.dash-title { font-size: 28px; font-weight: 800; color: #1D1D1F; letter-spacing: -0.03em; margin: 0; }
.dash-date { font-size: 13px; color: #86868B; margin: 2px 0 0; }

.header-right { display: flex; justify-content: space-between; align-items: center; margin-top: 12px; }

.range-select { display: flex; background: #E8E8ED; border-radius: 10px; padding: 2px; }
.range-item { padding: 6px 16px; font-size: 13px; font-weight: 600; color: #86868B; border-radius: 8px; cursor: pointer; transition: all 0.25s cubic-bezier(0.25,0.1,0.25,1); }
.range-item.active { background: white; color: #1D1D1F; box-shadow: 0 1px 3px rgba(0,0,0,0.08); }

.logout-link { font-size: 14px; color: #FF3B30; cursor: pointer; font-weight: 500; }
.logout-link:active { opacity: 0.7; }

.kpi-grid { display: grid; grid-template-columns: 1fr 1fr; gap: 12px; margin-bottom: 20px; }

.kpi-card {
  background: white; border-radius: 20px; padding: 18px;
  box-shadow: 0 1px 3px rgba(0,0,0,0.03);
  transition: transform 0.2s;
}
.kpi-card:active { transform: scale(0.98); }

.kpi-icon { width: 40px; height: 40px; border-radius: 12px; display: flex; align-items: center; justify-content: center; margin-bottom: 12px; }
.orders-icon { background: #E3F2FD; color: #1565C0; }
.revenue-icon { background: #E8F5E9; color: #2E7D32; }
.emp-icon { background: #FFF3E0; color: #E65100; }
.star-icon { background: #FFF8E1; color: #F9A825; }

.kpi-value { font-size: 26px; font-weight: 800; color: #1D1D1F; letter-spacing: -0.02em; }
.kpi-label { font-size: 12px; color: #86868B; margin-top: 2px; }

.section {
  background: white; border-radius: 20px; padding: 18px; margin-bottom: 14px;
  box-shadow: 0 1px 3px rgba(0,0,0,0.03);
}
.section-title { font-size: 16px; font-weight: 700; color: #1D1D1F; margin: 0 0 14px; }

.status-row { display: flex; align-items: center; gap: 10px; margin-bottom: 10px; }
.status-row:last-child { margin-bottom: 0; }
.s-name { font-size: 13px; color: #1D1D1F; width: 48px; font-weight: 500; }
.s-bar-wrap { flex: 1; height: 8px; background: #F5F5F7; border-radius: 4px; overflow: hidden; }
.s-bar { height: 100%; border-radius: 4px; transition: width 0.5s cubic-bezier(0.25,0.1,0.25,1); }
.s-bar.unpaid { background: #FF9500; }
.s-bar.paid { background: #2B95FF; }
.s-bar.progress { background: #8E8E93; }
.s-bar.done { background: #34C759; }
.s-count { font-size: 13px; font-weight: 600; color: #86868B; width: 32px; text-align: right; }

.chart-bars { display: flex; justify-content: space-between; align-items: flex-end; height: 120px; padding-top: 12px; }
.chart-col { display: flex; flex-direction: column; align-items: center; width: 12%; height: 100%; justify-content: flex-end; }
.chart-bar { width: 24px; background: #1D1D1F; border-radius: 6px 6px 0 0; position: relative; transition: height 0.5s cubic-bezier(0.25,0.1,0.25,1); min-height: 4px; }
.bar-val { position: absolute; top: -20px; left: 50%; transform: translateX(-50%); font-size: 10px; color: #86868B; white-space: nowrap; }
.bar-label { font-size: 10px; color: #C7C7CC; margin-top: 6px; }

.rank-row { display: flex; align-items: center; gap: 12px; padding: 8px 0; border-bottom: 1px solid #F5F5F7; }
.rank-row:last-child { border-bottom: none; }
.rank-num { width: 24px; height: 24px; border-radius: 8px; background: #F5F5F7; display: flex; align-items: center; justify-content: center; font-size: 12px; font-weight: 700; color: #86868B; }
.rank-num.top0 { background: #FFD700; color: white; }
.rank-num.top1 { background: #C0C0C0; color: white; }
.rank-num.top2 { background: #CD7F32; color: white; }
.rank-name { flex: 1; font-size: 14px; color: #1D1D1F; font-weight: 500; }
.rank-count { font-size: 13px; color: #86868B; font-weight: 600; }
</style>
