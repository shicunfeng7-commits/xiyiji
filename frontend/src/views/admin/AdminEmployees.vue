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
          <van-icon name="contact" size="28" :color="emp.isActive === 1 ? (emp.status === 'free' ? '#34C759' : '#FF9500') : '#C7C7CC'" />
        </div>
        <div class="emp-info">
          <div class="emp-name">{{ emp.name }}</div>
          <div class="emp-phone">{{ emp.phone }}</div>
        </div>
        <div class="emp-status">
          <span class="status-dot" :class="emp.isActive === 1 ? emp.status : 'inactive'"></span>
          {{ emp.isActive === 1 ? (emp.status === 'free' ? '空闲' : '服务中') : '已停用' }}
        </div>
        <button
          class="toggle-btn"
          :class="{ disable: emp.isActive === 1 }"
          @click="toggleEmployee(emp)"
        >{{ emp.isActive === 1 ? '停用' : '启用' }}</button>
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
import { ref, onMounted } from 'vue'
import { showToast, showConfirmDialog, showLoadingToast, closeToast } from 'vant'
import { get, post } from '../../utils/request'

const showAdd = ref(false)
const newName = ref('')
const newPhone = ref('')
const newAccount = ref('')

const employees = ref<any[]>([])

async function loadEmployees() {
  showLoadingToast({ message: '加载中...' })
  try {
    const res = await get<{ code: number; data: any[] }>('/api/admin/employees')
    if (res.data.code === 200) {
      employees.value = res.data.data.map(item => ({
        id: item.id,
        name: item.name || '未知',
        phone: item.phone ? item.phone.replace(/(\d{3})\d{4}(\d{4})/, '$1****$2') : '',
        status: item.status === 1 ? 'busy' : 'free',
        isActive: item.isActive ?? 1,
        account: '',
      }))
    }
  } catch (error) {
    showToast('加载失败')
  } finally {
    closeToast()
  }
}

async function addEmployee() {
  if (!newName.value || !newPhone.value) {
    showToast('请填写姓名和手机号')
    return
  }
  try {
    const res = await post<{ code: number }>('/api/admin/employee/add', {
      name: newName.value,
      phone: newPhone.value,
    })
    if (res.data.code === 200) {
      showToast('添加成功')
      newName.value = ''
      newPhone.value = ''
      newAccount.value = ''
      loadEmployees()
    }
  } catch (error) {
    showToast('添加失败')
  }
}

async function toggleEmployee(emp: any) {
  const action = emp.isActive === 1 ? '停用' : '启用'
  showConfirmDialog({
    title: `${action}员工`,
    message: `确定${action} ${emp.name} 吗？${emp.isActive === 1 ? '停用后该员工将无法抢单。' : ''}`,
  }).then(async () => {
    try {
      const res = await post<{ code: number }>(`/api/admin/employee/toggle/${emp.id}`)
      if (res.data.code === 200) {
        emp.isActive = emp.isActive === 1 ? 0 : 1
        showToast(`已${action}`)
      }
    } catch (error) {
      showToast(`${action}失败`)
    }
  })
}

onMounted(() => {
  loadEmployees()
})
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

.status-dot.inactive {
  background: #C7C7CC;
}

.toggle-btn {
  padding: 6px 14px;
  background: rgba(52,199,89,0.1);
  color: #34C759;
  border: none;
  border-radius: 14px;
  font-size: 12px;
  font-weight: 600;
  cursor: pointer;
  transition: all 0.2s;
}

.toggle-btn.disable {
  background: rgba(255,59,48,0.1);
  color: #FF3B30;
}

.toggle-btn:active {
  opacity: 0.7;
}
</style>