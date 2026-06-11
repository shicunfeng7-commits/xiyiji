<template>
  <div class="admin-employee-audit">
    <div class="page-title">员工审核</div>

    <div class="tab-bar">
      <div
        v-for="tab in tabs"
        :key="tab.key"
        class="tab-item"
        :class="{ active: currentTab === tab.key }"
        @click="switchTab(tab.key)"
      >
        {{ tab.label }}
      </div>
    </div>

    <div class="list">
      <div class="card" v-for="item in list" :key="item.id">
        <div class="card-body">
          <div class="info-row">
            <span class="label">申请人</span>
            <span class="value">{{ item.name }}</span>
          </div>
          <div class="info-row">
            <span class="label">手机号</span>
            <span class="value">{{ item.phone }}</span>
          </div>
          <div class="info-row">
            <span class="label">申请时间</span>
            <span class="value">{{ item.applyTime }}</span>
          </div>
          <div v-if="item.status !== 'pending'" class="info-row">
            <span class="label">审核时间</span>
            <span class="value">{{ item.auditTime }}</span>
          </div>
        </div>
        <div class="card-footer">
          <template v-if="item.status === 'pending'">
            <button class="btn btn-approve" @click="approve(item)">通过</button>
            <button class="btn btn-reject" @click="showRejectDialog(item)">拒绝</button>
          </template>
          <template v-else>
            <span class="status-tag" :class="item.status">
              {{ item.status === 'approved' ? '已通过' : '已拒绝' }}
            </span>
          </template>
        </div>
      </div>

      <div v-if="list.length === 0" class="empty-state">
        <van-icon name="file-o" size="48" color="#C7C7CC" />
        <p>暂无数据</p>
      </div>
    </div>

    <van-dialog v-model:show="rejectDialogShow" title="拒绝原因" show-cancel-button confirm-button-text="提交" @confirm="doReject">
      <van-field
        v-model="rejectReason"
        type="textarea"
        rows="3"
        placeholder="请输入拒绝原因"
        :rules="[{ required: true, message: '请输入拒绝原因' }]"
      />
    </van-dialog>
    
    <AdminNav />
  </div>
</template>

<script setup lang="ts">
import { ref } from 'vue'
import { showToast, showDialog } from 'vant'
import { get, post } from '../../utils/request'
import AdminNav from '../../components/AdminNav.vue'

const tabs = [
  { key: 'pending', label: '待审核' },
  { key: 'approved', label: '已通过' },
  { key: 'rejected', label: '已拒绝' },
]

const currentTab = ref('pending')
const list = ref<any[]>([])
const rejectDialogShow = ref(false)
const rejectReason = ref('')
const currentRejectItem = ref<any>(null)

function switchTab(tab: string) {
  currentTab.value = tab
  fetchList()
}

function fetchList() {
  get('/api/admin/employee/applications', { status: currentTab.value }).then((res: any) => {
    list.value = res.data || []
  }).catch(() => {
    showToast('加载失败')
  })
}

function approve(item: any) {
  showDialog({
    title: '确认通过',
    message: `确定通过 ${item.name} 的申请吗？`,
    confirmButtonText: '确认',
    cancelButtonText: '取消',
  }).then(() => {
    post(`/api/admin/employee/approve/${item.id}`).then(() => {
      showToast('已通过')
      fetchList()
    }).catch(() => {
      showToast('操作失败')
    })
  }).catch(() => {
    // 取消，不做操作
  })
}

function showRejectDialog(item: any) {
  currentRejectItem.value = item
  rejectReason.value = ''
  rejectDialogShow.value = true
}

function doReject() {
  if (!rejectReason.value) {
    showToast('请输入拒绝原因')
    return
  }
  post(`/api/admin/employee/reject/${currentRejectItem.value.id}`, { reason: rejectReason.value }).then(() => {
    showToast('已拒绝')
    rejectDialogShow.value = false
    fetchList()
  }).catch(() => {
    showToast('操作失败')
  })
}

fetchList()
</script>

<style scoped>
.admin-employee-audit {
  padding: 16px 16px 100px;
}

.page-title {
  font-size: 24px;
  font-weight: 700;
  color: #1D1D1F;
  margin-bottom: 16px;
}

.tab-bar {
  display: flex;
  gap: 8px;
  margin-bottom: 16px;
}

.tab-item {
  flex: 1;
  text-align: center;
  padding: 10px 0;
  font-size: 14px;
  font-weight: 500;
  color: #86868B;
  background: white;
  border-radius: 10px;
  cursor: pointer;
  transition: all 0.2s;
}

.tab-item.active {
  background: #2B95FF;
  color: white;
  box-shadow: 0 2px 8px rgba(43,149,255,0.3);
}

.card {
  background: white;
  border-radius: 16px;
  padding: 16px;
  margin-bottom: 12px;
  box-shadow: 0 1px 3px rgba(0,0,0,0.04);
}

.card-body {
  padding-bottom: 12px;
  border-bottom: 1px solid #F5F5F7;
}

.info-row {
  display: flex;
  justify-content: space-between;
  padding: 4px 0;
  font-size: 14px;
}

.info-row .label {
  color: #86868B;
}

.info-row .value {
  color: #1D1D1F;
  font-weight: 500;
}

.card-footer {
  display: flex;
  gap: 10px;
  padding-top: 12px;
  justify-content: flex-end;
}

.btn {
  padding: 8px 24px;
  border: none;
  border-radius: 18px;
  font-size: 13px;
  font-weight: 600;
  cursor: pointer;
  transition: all 0.2s;
}

.btn:active {
  transform: scale(0.95);
}

.btn-approve {
  background: #34C759;
  color: white;
}

.btn-reject {
  background: #FF3B30;
  color: white;
}

.status-tag {
  font-size: 12px;
  font-weight: 600;
  padding: 4px 12px;
  border-radius: 10px;
}

.status-tag.approved {
  background: rgba(52,199,89,0.1);
  color: #34C759;
}

.status-tag.rejected {
  background: rgba(255,59,48,0.1);
  color: #FF3B30;
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
