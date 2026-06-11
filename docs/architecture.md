# WashPro 架构与数据模型

## 技术栈
- **后端**：Spring Boot 3.2, Java 17, MyBatis-Plus 3.5.5, MySQL 8, Redis（预留）
- **前端**：Vue 3.4, Vant 4, Pinia, Vue Router (Hash), Axios
- **部署**：前端 Vite build 静态文件，后端 jar 包

## 系统架构
```
[Vue SPA] ←→ [Spring Boot REST API :8080] ←→ [MySQL :3306]
     ↕ WebSocket (员工实时推送)
[Employee WebSocket Server :8080/ws/employee]
```

## 角色与权限
| 角色 | role | 可访问路由 |
|------|------|-----------|
| 用户 | 0 | `/user/*` |
| 员工 | 1 | `/user/*` + `/employee/*` |
| 管理员 | 2 | `/admin/*` |

路由守卫 (`router/index.ts`) 强制角色隔离，后端 `LoginInterceptor` 验证 JWT。

## 订单状态机
```
UNPAID(0) → PAID(1) → IN_PROGRESS(2) → COMPLETED(3)
   ↓            ↓
CANCELLED(4)  (管理员回退)
```
- 用户创建 → 0，管理员确认支付 → 1
- 员工抢单（乐观锁 `isNull(employeeId)`）→ 2
- 上传前后照片完成 → 3，员工状态恢复空闲

## 数据模型
### user
| 字段 | 类型 | 说明 |
|------|------|------|
| id | bigint | PK |
| phone | varchar(20) | 登录手机号 |
| nickname | varchar(50) | 昵称 |
| avatar | varchar(500) | 头像URL |
| building_name | varchar(50) | 宿舍楼栋 |
| room_no | varchar(20) | 房间号 |
| role | tinyint | 0/1/2 |

### orders
| 字段 | 类型 | 说明 |
|------|------|------|
| id | bigint | PK |
| order_no | varchar(32) | XS+日期+序号 |
| user_id | bigint | FK→user |
| user_name | varchar(50) | 用户手机号（冗余搜索） |
| building_category/name | varchar | 楼栋分类/名称 |
| room_no | varchar | 房间号 |
| service_date | date | 服务日期 |
| start_time/end_time | varchar(10) | HH:MM |
| status | tinyint | 0/1/2/3/4 |
| employee_id | bigint | FK→employee |
| amount | decimal(10,2) | 默认29.90 |
| before_photo/after_photo | TEXT | JSON数组 |
| pay_time/complete_time | datetime | 时间戳 |

### service_time_config
| 字段 | 说明 |
|------|------|
| period | MORNING/AFTERNOON/EVENING |
| start_hour/end_hour | int（8, 12, 18等） |
| enabled | bool |

## 关键设计决策
- 统一登录：员工和用户共用 `/api/auth/login`，role 字段区分
- 乐观锁抢单：`eq(status, PAID) + isNull(employeeId)` 防并发
- 照片存 JSON 数组：TEXT 字段存 `["url1","url2"]`
- Vant TimePicker v-model 用数组格式 `["08","00"]`
- Vue computed 需显式依赖 `route.path` 触发重算
