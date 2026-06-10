# Checklist: migrate-to-v2

## 数据库
- [ ] user 表包含 role、update_time 字段
- [ ] employee_application 表已创建
- [ ] employee 表包含 user_id、is_active 字段
- [ ] init.sql 已更新，包含初始管理员和默认服务时间配置

## 后端 Auth
- [ ] POST /api/auth/login 支持手机号登录和自动注册
- [ ] POST /api/auth/login 返回 token 和用户信息
- [ ] GET /api/auth/user-info 返回当前用户信息
- [ ] JwtTokenUtil 工具类正常工作
- [ ] LoginInterceptor 拦截非登录接口
- [ ] /api/auth/login 和 /api/user/time-slots 放行

## 后端用户端
- [ ] 所有接口从 token 解析 userId
- [ ] 用户只能查看自己的订单
- [ ] POST /api/user/apply-employee 创建申请
- [ ] GET /api/user/apply-status 返回申请状态

## 后端管理端
- [ ] POST /api/admin/order/confirm-pay/{id} 确认支付（记录操作日志）
- [ ] POST /api/admin/order/revert-pay/{id} 回退支付（PAID→UNPAID）
- [ ] 已抢单的订单调用 revert-pay 返回错误
- [ ] GET /api/admin/employee/applications 返回待审核列表
- [ ] POST /api/admin/employee/approve/{id} 通过申请（创建employee，更新user.role）
- [ ] POST /api/admin/employee/reject/{id} 拒绝申请

## 后端员工端
- [ ] 员工身份从 token userId 关联 employee 表
- [ ] POST /api/employee/order/grab/{id} 抢单校验正常
- [ ] WebSocket 通知保持正常

## 前端登录页
- [ ] 登录页只显示手机号输入框
- [ ] 调用 /api/auth/login 接口
- [ ] token 和用户信息存 localStorage
- [ ] 登录成功后跳转 /user/home

## 前端用户端
- [ ] UserProfile.vue 页面存在且可访问
- [ ] "申请成为员工"按钮和表单正常
- [ ] 申请状态展示正常
- [ ] 路由 /user/profile 已配置

## 前端管理端
- [ ] 确认支付按钮弹出确认弹窗
- [ ] PAID 订单显示"回退支付"按钮
- [ ] AdminEmployeeAudit.vue 页面存在
- [ ] 审批通过/拒绝操作正常
- [ ] AdminTimeConfig.vue 页面存在
- [ ] 路由已配置新页面

## 前端员工端
- [ ] 抢单按钮弹出确认弹窗
- [ ] 点击确认发送请求，取消不做操作

## 前端导航
- [ ] 未登录显示角色选择页（/login）
- [ ] 用户角色显示 首页+订单+我的
- [ ] 用户+员工角色额外显示 员工 入口
- [ ] 管理员登录显示 订单+员工+审核+配置

## 前端 Token
- [ ] auth.ts 正确管理 token 存取
- [ ] request.ts 自动携带 Authorization header
- [ ] 401 响应自动跳转登录页

## 编译验证
- [ ] mvn clean compile 成功
- [ ] npm run build 成功
- [ ] 按模块分次 git commit 提交