# Tasks: migrate-to-v2

## Task 1: 数据库 Schema 更新
更新数据库表结构（user 增加 role 字段，新增 employee_application 表，employee 表关联 user_id）。

- [x] 1.1 修改 user 表，增加 role TINYINT、update_time 字段
- [x] 1.2 创建 employee_application 表
- [x] 1.3 修改 employee 表：移除 username/password，增加 user_id、is_active
- [x] 1.4 在 init.sql 中添加初始管理员数据和管理员微信二维码配置
- [x] 1.5 更新对应的后端 Entity 和 Mapper

**Dependencies**: Task 2 (后端 entity 需要在数据库创建前定义好)

## Task 2: 后端 Auth 模块（手机号登录/注册 + Token 鉴权）
创建 Auth 模块，实现手机号登录/注册和 Token 鉴权拦截器。

- [x] 2.1 创建 AuthController (POST /api/auth/login, GET /api/auth/user-info)
- [x] 2.2 创建 AuthService (phone 登录/注册逻辑，token 生成)
- [x] 2.3 创建 JwtTokenUtil 工具类（token 生成和解析）
- [x] 2.4 创建 LoginInterceptor（Token 校验拦截器）
- [x] 2.5 更新 WebConfig 注册拦截器，配置放行路径（/api/auth/login, /api/user/time-slots, /api/admin/login）

**Dependencies**: Task 1 (user entity)

## Task 3: 后端用户端 API 改造
新增用户端接口：创建订单、订单列表、申请员工、申请状态查询。

- [x] 3.1 修改 UserController：所有接口从 token 获取 userId
- [x] 3.2 新增 apply-employee 接口
- [x] 3.3 新增 apply-status 接口
- [x] 3.4 修改 OrderService 确保用户只能看自己的订单

**Dependencies**: Task 1, Task 2

## Task 4: 后端管理端 API 改造
新增管理员接口：回退支付、员工申请审核、通过/拒绝。

- [x] 4.1 修改 AdminController：新增 revert-pay 接口（PAID→UNPAID，含状态校验）
- [x] 4.2 新增 employee/applications 列表接口
- [x] 4.3 新增 employee/approve 接口（创建 employee 记录、更新 user.role）
- [x] 4.4 新增 employee/reject 接口
- [x] 4.5 修改 confirm-pay 接口增加操作日志

**Dependencies**: Task 1, Task 2

## Task 5: 后端员工端 API 改造
修改员工端接口，员工通过 user_id 关联认证，增加抢单确认的后端校验。

- [ ] 5.1 修改 EmployeeController，员工身份从 token 中的 userId 查找 employee 记录
- [ ] 5.2 修改 grab 接口，增加抢单校验逻辑
- [ ] 5.3 保持原有 WebSocket 通知逻辑

**Dependencies**: Task 1, Task 2

## Task 6: 前端登录页改造（手机号登录）
将 LoginPage.vue 改为手机号输入登录页。

- [x] 6.1 重新设计 LoginPage.vue（手机号输入 + 登录按钮，Apple 风格）
- [x] 6.2 调用 POST /api/auth/login 接口
- [x] 6.3 存储 token 和用户信息到 localStorage
- [x] 6.4 登录成功后根据角色跳转

**Dependencies**: Task 2 (后端 auth 接口)

## Task 7: 前端用户端改造（个人中心 + 申请员工）
新增用户个人中心页面和员工申请功能。

- [x] 7.1 创建 UserProfile.vue（个人信息展示 + "申请成为员工"按钮）
- [x] 7.2 实现申请员工表单（姓名输入 + 提交）
- [x] 7.3 实现申请状态查看（待审核/已通过/已拒绝）
- [x] 7.4 更新路由配置
- [x] 7.5 userId 从 localStorage 的 token 解析获取

**Dependencies**: Task 2, Task 3

## Task 8: 前端管理端改造（员工审核 + 支付回退 + 服务时间配置）

- [ ] 8.1 创建 AdminEmployeeAudit.vue（待审核列表 + 通过/拒绝操作）
- [ ] 8.2 修改 AdminOrders.vue：确认支付按钮增加确认弹窗
- [ ] 8.3 修改 AdminOrders.vue：PAID 状态订单增加"回退支付"按钮
- [ ] 8.4 创建 AdminTimeConfig.vue（上午/下午/晚上时段配置）
- [ ] 8.5 更新路由配置

**Dependencies**: Task 3, Task 4

## Task 9: 前端员工端改造（抢单确认弹窗）

- [x] 9.1 修改 EmployeeAvailable.vue：抢单按钮增加确认弹窗
- [x] 9.2 订单信息中显示楼栋分类 + 时间段

**Dependencies**: Task 5

## Task 10: 前端导航栏动态角色改造
App.vue 根据用户角色动态显示底部导航栏。

- [ ] 10.1 读取 localStorage 中的用户角色信息
- [ ] 10.2 根据 role 动态计算 tabs 数组
- [ ] 10.3 管理员登录后显示管理端导航
- [ ] 10.4 用户+员工角色增加"员工"入口

**Dependencies**: Task 6, Task 7

## Task 11: 前端 utils 层改造（Token 管理 + Axios 拦截器）

- [x] 11.1 修改 utils/auth.ts（Token 存储和读取逻辑）
- [x] 11.2 修改 utils/request.ts（所有请求自动携带 Authorization header）
- [x] 11.3 统一处理 401 跳转到登录页

**Dependencies**: Task 2

## Task 12: 编译验证 + Git 提交
重新编译前后端项目，验证接口和页面正常，分模块提交 git。

- [x] 12.1 编译后端 mvn clean compile
- [x] 12.2 验证前端 npm run build 无报错
- [x] 12.3 按模块分次 git add + git commit
- [x] 12.4 更新设计文档-v2.md 确认与实现一致

**Dependencies**: Task 1-11

# Task Dependencies
- Task 2, 11 depend on Task 1
- Task 3, 4, 5 depend on Task 2
- Task 6 depends on Task 2
- Task 7 depends on Task 2, 3
- Task 8 depends on Task 3, 4
- Task 9 depends on Task 5
- Task 10 depends on Task 6, 7
- Task 12 depends on Task 1-11

# Parallelizable Groups
- Group A: Task 1 (独立)
- Group B: Task 11 (可与 Task 2 并行)
- Group C: Task 3, 4, 5 (可并行，依赖 Task 2)
- Group D: Task 6, 7 (与 Group E 并行)
- Group E: Task 8, 9 (与 Group D 并行)
- Group F: Task 10 (依赖 D/E 完成后)