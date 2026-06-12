# Tasks

- [ ] Task 1: 修改路由配置
  - [ ] SubTask 1.1: 修改 `/` 和 `/login` 重定向到 `/user/home`
  - [ ] SubTask 1.2: 更新路由守卫，扩展游客可访问页面列表
  - [ ] SubTask 1.3: 删除 LoginPage 路由配置

- [ ] Task 2: 修改 App.vue TabBar 配置
  - [ ] SubTask 2.1: 游客 TabBar 改为 `[首页] [订单] [我的]`
  - [ ] SubTask 2.2: 确保登录后 TabBar 根据角色正确切换

- [ ] Task 3: 修改 UserHome.vue 立即预约逻辑
  - [ ] SubTask 3.1: 删除 `showLoginDialog` 和 `showDialog` 引用
  - [ ] SubTask 3.2: `goCreate()` 直接跳转 `/user/order/create`

- [ ] Task 4: 修改 UserOrders.vue 游客空状态
  - [ ] SubTask 4.1: 检测未登录时显示引导提示
  - [ ] SubTask 4.2: 添加"立即预约"按钮

- [ ] Task 5: 修改 UserOrderCreate.vue 添加登录引导
  - [ ] SubTask 5.1: 提交订单时检测登录状态
  - [ ] SubTask 5.2: 未登录弹出引导对话框
  - [ ] SubTask 5.3: 登录前保存表单数据到 sessionStorage
  - [ ] SubTask 5.4: 从登录页回跳时恢复数据并自动提交

- [ ] Task 6: 修改 UserProfile.vue 登录后回跳逻辑
  - [ ] SubTask 6.1: 登录成功后检查 sessionStorage 中的回跳信息
  - [ ] SubTask 6.2: 有回跳信息则跳转回去，无则留在个人中心

- [ ] Task 7: 删除 LoginPage.vue
  - [ ] SubTask 7.1: 删除 `frontend/src/views/LoginPage.vue`
  - [ ] SubTask 7.2: 清理所有对 `/login` 页面的直接引用（重定向已处理）

- [ ] Task 8: 测试验证
  - [ ] SubTask 8.1: 游客浏览流程测试
  - [ ] SubTask 8.2: 游客下单+登录回跳流程测试
  - [ ] SubTask 8.3: 已登录用户流程测试

# Task Dependencies
- [Task 2] depends on [Task 1]
- [Task 3] depends on [Task 1]
- [Task 4] depends on [Task 1]
- [Task 5] depends on [Task 1]
- [Task 6] depends on [Task 5]
- [Task 7] depends on [Task 1]
- [Task 8] depends on [Task 1, Task 2, Task 3, Task 4, Task 5, Task 6, Task 7]
