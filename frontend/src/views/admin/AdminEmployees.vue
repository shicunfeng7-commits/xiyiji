<template>
  <div class="admin-employees">
    <div class="toolbar">
      <button class="add-btn" @click="showAdd = true">
        <van-icon name="plus" size="16" color="white" />
        <span>添加员工</span>
      </button>
    </div>

    <div class="employee-list">
      <div class="employee-card" v-for="emp in employees" :key="emp.id">
        <div class="emp-avatar">
          <van-icon name="contact" size="28" :color="emp.status === 'free' ? '#34C759' : '#FF9500'" />
        </div>
        <div class="emp-info">
          <div class="emp-name">{{ emp.name }}</div>
          <div class="emp-phone">{{ emp.phone }}</div>
        </div>
        <div class="emp-status">
          <span class="status-dot" :class="emp.status"></span>
          {{ emp.status === 'free' ? '空闲' : '服务中' }}
        </div>
        <button class="remove-btn" @click="removeEmp(emp)">移除</button>
      </div>
    </div>

    <van-dialog v-model:show="showAdd" title="添加员工" show-cancel-button confirm-button-text="添加" @confirm="addEmployee">
      <van-field v-model="newName" label="姓名" placeholder="请输入姓名" />
      <van-field v-model="newPhone" label="手机号" placeholder="请输入手机号" />
      <van-field v-model="newAccount" label="账号" placeholder="请输入登录账号" />
    </van-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref } from 'vue'
import { showToast, showConfirmDialog } from 'vant'

const showAdd = ref(false)
const newName = ref('')
const newPhone = ref('')
const newAccount = ref('')

const employees = ref([
  { id: 1, name: '陈师傅', phone: '138****1234', status: 'free', account: 'chen001' },
  { id: 2, name: '刘师傅', phone: '139****5678', status: 'busy', account: 'liu002' },
  { id: 3, name: '张师傅', phone: '137****9012', status: 'free', account: 'zhang003' },
])

function addEmployee() {
  if (newName.value && newPhone.value) {
    employees.value.push({
      id: Date.now(),
      name: newName.value,
      phone: newPhone.value,
      status: 'free',
      account: newAccount.value,
    })
    newName.value = ''
    newPhone.value = ''
    newAccount.value = ''
    showToast('添加成功')
  }
}

function removeEmp(emp: any) {
  showConfirmDialog({
    title: '移除员工',
    message: `确定移除 ${emp.name} 吗？`,
  }).then(() => {
    employees.value = employees.value.filter(e => e.id !== emp.id)
    showToast('已移除')
  })
}
</script>

<style scoped>
.admin-employees {
  padding: 12px 16px 100px;
}

.toolbar {
  margin-bottom: 12px;
}

.add-btn {
  display: flex;
  align-items: center;
  gap: 6px;
  padding: 12px 24px;
  background: #2B95FF;
  color: white;
  border: none;
  border-radius: 12px;
  font-size: 15px;
  font-weight: 600;
  cursor: pointer;
  transition: all 0.2s;
  box-shadow: 0 4px 14px rgba(43,149,255,0.3);
}

.add-btn:active {
  transform: scale(0.97);
}

.employee-card {
  background: white;
  border-radius: 16px;
  padding: 16px;
  margin-bottom: 10px;
  display: flex;
  align-items: center;
  gap: 12px;
  box-shadow: 0 1px 3px rgba(0,0,0,0.04);
}

.emp-avatar {
  width: 48px;
  height: 48px;
  background: #F5F5F7;
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  flex-shrink: 0;
}

.emp-info {
  flex: 1;
}

.emp-name {
  font-size: 16px;
  font-weight: 600;
  color: #1D1D1F;
}

.emp-phone {
  font-size: 13px;
  color: #86868B;
  margin-top: 2px;
}

.emp-status {
  font-size: 12px;
  color: #86868B;
  display: flex;
  align-items: center;
  gap: 4px;
  margin-right: 12px;
}

.status-dot {
  width: 6px;
  height: 6px;
  border-radius: 50%;
}

.status-dot.free {
  background: #34C759;
}

.status-dot.busy {
  background: #FF9500;
}

.remove-btn {
  padding: 6px 14px;
  background: rgba(255,59,48,0.1);
  color: #FF3B30;
  border: none;
  border-radius: 14px;
  font-size: 12px;
  font-weight: 600;
  cursor: pointer;
  transition: all 0.2s;
}

.remove-btn:active {
  opacity: 0.7;
}
</style>