# Checklist: migrate-to-v2

## 数据库
- [x] user 表包含 role、update_time 字段
- [x] employee_application 表已创建
- [x] employee 表包含 user_id、is_active 字段
- [x] init.sql 已更新，包含初始管理员和默认服务时间配置

## 后端 Auth
- [x] POST /api/auth/login 支持手机号登录和自动注册
- [x] POST /api/auth/login 返回 token 和用户信息
- [x] GET /api/auth/user-info 返回当前用户信息
- [x] JwtTokenUtil 工具类正常工作
- [x] LoginInterceptor 拦截非登录接口
- [x] /api/auth/login 和 /api/user/time-slots 放行

## 后端用户端
- [x] 所有接口从 token 解析 userId
- [x] 用户只能查看自己的订单
- [x] POST /api/user/apply-employee 创建申请
- [x] GET /api/user/apply-status 返回申请状态

## 后端管理端
- [x] POST /api/admin/order/confirm-pay/{id} 确认支付（记录操作日志）
- [x] POST /api/admin/order/revert-pay/{id} 回退支付（PAID→UNPAID）
- [x] 已抢单的订单调用 revert-pay 返回错误
- [x] GET /api/admin/employee/applications 返回待审核列表
- [x] POST /api/admin/employee/approve/{id} 通过申请（创建employee，更新user.role）
- [x] POST /api/admin/employee/reject/{id} 拒绝申请

## 后端员工端
- [x] 员工身份从 token userId 关联 employee 表
- [x] POST /api/employee/order/grab/{id} 抢单校验正常
- [x] WebSocket 通知保持正常

## 前端登录页
- [x] 登录页只显示手机号输入框
- [x] 调用 /api/auth/login 接口
- [x] token 和用户信息存 localStorage
- [x] 登录成功后跳转 /user/home

## 前端用户端
- [x] UserProfile.vue 页面存在且可访问
- [x] "申请成为员工"按钮和表单正常
- [x] 申请状态展示正常
- [x] 路由 /user/profile 已配置

## 前端管理端
- [x] 确认支付按钮弹出确认弹窗
- [x] PAID 订单显示"回退支付"按钮
- [x] AdminEmployeeAudit.vue 页面存在
- [x] 审批通过/拒绝操作正常
- [x] AdminTimeConfig.vue 页面存在
- [x] 路由已配置新页面

## 前端员工端
- [x] 抢单按钮弹出确认弹窗
- [x] 点击确认发送请求，取消不做操作

## 前端导航
- [x] 未登录显示角色选择页（/login）
- [x] 用户角色显示 首页+订单+我的
- [x] 用户+员工角色额外显示 员工 入口
- [x] 管理员登录显示 订单+员工+审核+配置

## 前端 Token
- [x] auth.ts 正确管理 token 存取
- [x] request.ts 自动携带 Authorization header
- [x] 401 响应自动跳转登录页

## 编译验证
- [x] mvn clean compile 成功
- [x] npm run build 成功
- [x] 按模块分次 git commit 提交