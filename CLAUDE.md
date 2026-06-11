# WashPro — 洗衣机清洗服务系统

Spring Boot 3.2 + Vue 3 + Vant 4 全栈项目。学校宿舍洗衣机上门清洗预约平台。

## 项目结构
```
backend/   → Spring Boot 3.2, Java 17, MyBatis-Plus, MySQL
frontend/  → Vue 3.4, Vant 4, Pinia, Vue Router (Hash)
```

## 核心规则

- **统一登录**：所有角色通过 `/api/auth/login` 手机号登录，role 字段区分（0=用户, 1=员工, 2=管理员）
- **角色路由守卫**：router/index.ts beforeEach 严格限制：管理员只能 `/admin/*`，普通用户不能进 `/admin/*` 和 `/employee/*`，员工可同时访问员工端和用户端
- **订单状态机**：0=未支付 → 1=已支付 → 2=服务中 → 3=已完成 → 4=已取消（不可逆）
- **图片上传**：Base64 经 `/api/employee/upload-photo` 存 `uploads/` 目录，前端通过 Vite proxy `/uploads` 访问
- **照片存 JSON 数组**：orders.before_photo / after_photo 为 TEXT 字段存 JSON 字符串数组
- **localStorage key**：token=`washpro_token`, userInfo=`washpro_user_info`（不是 `userInfo`）
- **Vue computed 响应式陷阱**：computed 内纯读 localStorage 不会触发重算，必须在 computed 内访问 `route.path` 建立依赖
- **van-time-picker v-model**：值为 `["08","00"]` 数组格式，不是字符串 `"08:00"`
- **MySQL 密码**：`root / Ff75684538.`，数据库 `washpro`

## 数据库核心表
| 表 | 用途 |
|------|------|
| user | 统一账户（phone + role + avatar + building_name/room_no） |
| employee | 员工信息（user_id 关联，status 0=空闲/1=服务中） |
| employee_application | 员工申请表（status 0=待审/1=通过/2=拒绝） |
| orders | 订单（status + employee_id + before_photo/after_photo JSON） |
| order_status_log | 操作日志 |
| admin | 管理员 |
| service_time_config | 服务时段（period + start_hour/end_hour int） |

## 端口与代理
- 后端：8080，前端 Vite：5173（备用端口自动+1）
- Vite proxy：`/api` → 8080，`/uploads` → 8080，`/ws` → ws://8080

## 深入文档
- 架构与数据模型 → docs/architecture.md
- 接入指南与 API 速查 → docs/integration-guide.md
