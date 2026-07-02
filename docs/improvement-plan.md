# WashPro 简历竞争力提升计划

## Context

该项目是一个全栈 O2O 洗衣服务平台（Vue 3 + Spring Boot 3.2），核心业务流程完整，但缺少测试、容器化、分页、环境分离、缓存、CI/CD 等工程化实践。本次改进目标：花费约 2 天投入，补上简历中最关键的短板，让项目在面试中更有竞争力。**不含 SMS 接入**（用户后续单独考虑）。

---

## Phase 0: 单元测试（P0，约 4h）

### 0.1 添加测试依赖
**文件:** `backend/pom.xml`
- 添加 `spring-boot-starter-test`（含 JUnit 5 + Mockito + AssertJ）
- 添加 `h2`（内存数据库，scope=test）

### 0.2 创建测试配置
**新建:** `backend/src/test/resources/application-test.yml`
- H2 内存数据库，MySQL 兼容模式
- 排除 Redis 自动配置（测试不需要 Redis）
- MyBatis-Plus 配置

### 0.3 创建测试 DDL
**新建:** `backend/src/test/resources/schema.sql`
- 7 张表的 H2 兼容 DDL（注意 TINYINT→INT, JSON→VARCHAR）

### 0.4 编写核心测试
**新建:** `backend/src/test/java/com/xiyiji/modules/order/service/impl/OrderServiceImplTest.java`

测试用例（8 个）：
| 测试 | 覆盖点 |
|------|--------|
| `createOrder_Success` | 正常下单，金额=29.90，状态=UNPAID |
| `createOrder_TimeConflict` | 同楼同天时间段重叠抛异常 |
| `createOrder_NoConflict_DifferentBuilding` | 不同楼不冲突 |
| `createOrder_NoConflict_DifferentDate` | 不同天不冲突 |
| `grabOrder_Success` | 抢单成功，状态→PENDING_SERVICE |
| `grabOrder_Concurrent` | 并发抢单，乐观锁保证只有一人成功 |
| `cancelOrder_Success` | 取消待支付订单 |
| `confirmPay_RevertPay` | 确认支付+撤销支付来回 |

### 0.5 验证
```bash
cd backend && mvn test
```

---

## Phase 1: Docker 容器化（P0，约 2h）

### 1.1 后端 Dockerfile
**新建:** `backend/Dockerfile`
- 多阶段构建：maven:3.9-eclipse-temurin-17 编译 → eclipse-temurin:17-jre-alpine 运行
- 默认激活 prod profile

### 1.2 前端 Dockerfile
**新建:** `frontend/Dockerfile`
- 多阶段构建：node:20-alpine 编译 → nginx:alpine 服务
- **复用现有 `nginx/washpro.conf`**，构建时用 `sed` 替换两处：
  - Windows 路径 `E:/java daima/xiyiji/frontend/dist` → `/usr/share/nginx/html`
  - 后端地址 `127.0.0.1:8080` → `backend:8080`
- 不新建 nginx 配置文件，只维护一份

### 1.4 docker-compose.yml
**新建:** `docker-compose.yml`（项目根目录）
- mysql:8.0 + redis:7-alpine + backend + frontend 四个服务
- MySQL/Redis 带健康检查
- 后端通过环境变量覆盖数据库连接
- MySQL 初始化脚本挂载

### 1.5 数据库初始化 SQL
**新建:** `docs/sql/init.sql`
- 从现有数据库导出 7 张表的 DDL（含少量种子数据）

### 1.6 .dockerignore
**新建:** `backend/.dockerignore` 和 `frontend/.dockerignore`

### 1.7 验证
```bash
docker-compose up -d
curl http://localhost/api/auth/admin/login
# 浏览器访问 http://localhost 确认前端正常
```

---

## Phase 2: 分页（P1，约 1h）

MyBatis-Plus 分页插件已在 `MyBatisPlusConfig.java` 中配置好，只需改业务代码。

### 2.1 创建 PageResult DTO
**新建:** `backend/src/main/java/com/xiyiji/common/dto/PageResult.java`
- 字段: list, total, page, pageSize, pages
- 静态工厂方法 `of(IPage<T>)`

### 2.2 修改 OrderService 接口
**文件:** `backend/.../modules/order/service/OrderService.java`
- 4 个列表方法增加 `long page, long size` 参数
- 返回类型改为 `IPage<Order>`

### 2.3 修改 OrderServiceImpl
**文件:** `backend/.../modules/order/service/impl/OrderServiceImpl.java`
- 列表方法从 `list(wrapper)` 改为 `page(new Page<>(page, size), wrapper)`

### 2.4 修改控制器层
- **UserController.getOrders()** — 加 `@RequestParam(defaultValue = "1") page/size`，返回 `PageResult`
- **AdminController.getOrders()** — 同上
- **EmployeeController.getAvailableOrders() / getMyOrders()** — 同上

### 2.5 验证
```bash
# 测试带分页参数的请求
curl "http://localhost:8080/api/user/order/list?page=1&size=2"
# 确认返回 total/page/pages 字段
```

---

## Phase 3: 环境配置分离（P1，约 0.5h）

### 3.1 拆分 application.yml
**修改:** `backend/src/main/resources/application.yml`
- 只保留公共配置（multipart, mybatis-plus 基础配置, knife4j, upload, server.port）
- 移除数据库密码、Redis host 等敏感/环境相关配置

### 3.2 创建 application-dev.yml
**新建:** `backend/src/main/resources/application-dev.yml`
- 本地 MySQL 连接（保留现有开发密码）
- StdOutImpl SQL 日志
- DEBUG 级别日志

### 3.3 创建 application-prod.yml
**新建:** `backend/src/main/resources/application-prod.yml`
- 全部使用 `${ENV_VAR:default}` 占位符
- SLF4J SQL 日志
- INFO 级别日志

### 3.4 验证
```bash
# dev 模式启动（行为不变）
mvn spring-boot:run -Dspring-boot.run.profiles=dev

# prod 模式启动（通过环境变量注入配置）
java -jar app.jar --spring.profiles.active=prod
```

---

## Phase 4: Redis 缓存（P2，约 2h）

### 4.1 创建 RedisConfig（JSON 序列化）
**新建:** `backend/.../common/config/RedisConfig.java`
- 自定义 `RedisTemplate<String, Object>`，value 用 Jackson2JsonRedisSerializer
- 支持 JavaTimeModule（LocalDateTime 序列化）

### 4.2 创建 CacheService 工具
**新建:** `backend/.../common/service/CacheService.java`
- `get(key, ttl, loader)` — cache-aside 模式
- `evict(key)` — 单键失效
- `evictByPattern(pattern)` — 批量失效

### 4.3 提取 DashboardService
**新建:** `backend/.../modules/admin/service/DashboardService.java`
- 将 `AdminController.getDashboard()` 中的聚合逻辑移入此服务
- 用 `CacheService` 缓存仪表盘数据（TTL 5 分钟）
- 提供 `invalidateDashboard()` 方法

### 4.4 缓存可用订单列表
**修改:** `backend/.../modules/employee/controller/EmployeeController.java`
- 可用订单列表加 60 秒缓存

### 4.5 数据变更时失效缓存
**修改:** `AdminController` 中的 `confirmPay`/`revertPay` 方法
**修改:** `OrderServiceImpl` 中的 `grabOrder` 方法
- 调用 `dashboardService.invalidateDashboard()`
- 调用 `cacheService.evict("orders:available")`

### 4.6 验证
```bash
# 第一次请求仪表盘（慢，走 DB）
curl http://localhost:8080/api/admin/dashboard
# 第二次请求（快，走 Redis）
curl http://localhost:8080/api/admin/dashboard
# 确认支付一笔订单后，缓存失效
```

---

## Phase 5: GitHub Actions CI（P2，约 1h）

### 5.1 创建 CI 工作流
**新建:** `.github/workflows/ci.yml`
- 触发条件: push/PR 到 main
- backend job: JDK 17 + Maven 构建 + 跑测试
- frontend job: Node 20 + npm ci + build
- 两个 job 并行执行

### 5.2 验证
- Push 代码到 GitHub，查看 Actions 选项卡确认两个 job 都通过

---

## 执行顺序

```
Phase 0 (测试) → Phase 1 (Docker) → Phase 2 (分页) → Phase 3 (配置分离) → Phase 4 (缓存) → Phase 5 (CI)
```

每个 Phase 可独立合入，Phase 0 必须最先做（为后续重构提供安全网）。

---

## 涉及文件汇总

**新建 15 个文件：**
- `backend/src/test/resources/application-test.yml`
- `backend/src/test/resources/schema.sql`
- `backend/src/test/java/.../order/service/impl/OrderServiceImplTest.java`
- `backend/Dockerfile` + `.dockerignore`
- `frontend/Dockerfile` + `.dockerignore`
- `docker-compose.yml`
- `docs/sql/init.sql`
- `backend/.../common/dto/PageResult.java`
- `backend/.../common/config/RedisConfig.java`
- `backend/.../common/service/CacheService.java`
- `backend/.../modules/admin/service/DashboardService.java`
- `backend/src/main/resources/application-dev.yml`
- `backend/src/main/resources/application-prod.yml`
- `.github/workflows/ci.yml`

**修改 8 个文件：**
- `backend/pom.xml` — 加测试依赖
- `backend/.../order/service/OrderService.java` — 分页参数
- `backend/.../order/service/impl/OrderServiceImpl.java` — 分页实现 + 缓存失效
- `backend/.../user/controller/UserController.java` — 分页
- `backend/.../admin/controller/AdminController.java` — 分页 + 提取 DashboardService
- `backend/.../employee/controller/EmployeeController.java` — 分页 + 缓存
- `backend/src/main/resources/application.yml` — 精简为公共配置
- `backend/Dockerfile` — prod profile 参数
