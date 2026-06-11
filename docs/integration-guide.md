# WashPro 接入指南

## 快速启动
```bash
# 后端（需要 MySQL + Java 17）
cd backend && mvn spring-boot:run

# 前端（需要 Node 18+）
cd frontend && npm install && npm run dev
```

## 环境要求
| 依赖 | 版本 |
|------|------|
| MySQL | 8.0+ |
| Java | 17 |
| Node | 18+ |
| Redis | 可选（预留） |

## API 速查

### 认证
| 方法 | 路径 | 说明 |
|------|------|------|
| POST | `/api/auth/login` | 用户/员工登录 `{phone}` → `{token, user}` |
| POST | `/api/auth/admin/login` | 管理员登录 `{username, password}` |

### 用户端
| 方法 | 路径 | 说明 |
|------|------|------|
| GET | `/api/user/order/list` | 我的订单（含员工名、照片） |
| POST | `/api/user/order/create` | 创建订单 |
| GET | `/api/user/order/detail/{id}` | 订单详情 |
| POST | `/api/user/order/cancel/{id}` | 取消订单（仅未支付） |
| POST | `/api/user/apply-employee` | 申请成为员工 |
| GET | `/api/user/apply-status` | 查看申请状态 |
| PUT | `/api/user/profile` | 更新个人信息 |

### 员工端
| 方法 | 路径 | 说明 |
|------|------|------|
| GET | `/api/employee/orders/available` | 可抢订单（PAID+无员工） |
| POST | `/api/employee/order/grab/{id}` | 抢单 |
| GET | `/api/employee/orders/my-list` | 我的订单 |
| POST | `/api/employee/order/complete/{id}` | 完成 `?beforePhotos=[]&afterPhotos=[]` |
| GET | `/api/employee/stats?range=today\|week\|month` | 统计 |

### 管理端
| 方法 | 路径 | 说明 |
|------|------|------|
| GET | `/api/admin/dashboard?range=` | 数据看板 |
| GET | `/api/admin/orders?keyword=&status=` | 订单管理（支持搜索） |
| POST | `/api/admin/order/confirm-pay/{id}` | 确认支付 |
| DELETE | `/api/admin/order/{id}` | 删除订单 |
| GET | `/api/admin/time-config/list` | 时间配置 |
| PUT | `/api/admin/time-config/update` | 批量更新时间 |
| GET | `/api/admin/employee/applications` | 员工审核列表 |
| POST | `/api/admin/employee/approve/{id}` | 通过申请 |
| POST | `/api/admin/employee/reject/{id}?remark=` | 拒绝申请 |

### 文件上传
| 方法 | 路径 | 说明 |
|------|------|------|
| POST | `/api/employee/upload-photo` | Base64上传 `{image}` → `{data: "/uploads/xxx.jpg"}` |

## 认证方式
所有 `/api/**` 请求 Header：`Authorization: Bearer <token>`

## Vite 代理配置
```ts
proxy: {
  '/api': 'http://localhost:8080',
  '/uploads': 'http://localhost:8080',
  '/ws': { target: 'ws://localhost:8080', ws: true },
}
```
