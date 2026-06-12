# 游客模式与登录整合 Spec

## Why
当前用户打开系统需要强制登录才能使用，限制了用户先浏览服务再决定是否注册的体验。需要将登录功能整合到个人中心页面，让游客可以先浏览、填写订单，在关键操作时再引导登录。

## What Changes
- `/` 根路径从跳转 `/user/profile` 改为跳转 `/user/home`
- 游客 TabBar 从单按钮改为完整导航 `[首页] [订单] [我的]`
- 首页"立即预约"不再弹登录对话框，直接跳转预约页
- 订单列表页游客显示空状态引导
- 预约页提交时检测登录状态，未登录则引导登录后继续
- 登录后支持回跳到之前的页面（带表单数据）
- 删除独立的 LoginPage.vue（已整合到 UserProfile）

## Impact
- Affected specs: 游客模式（新）、登录流程
- Affected code: 
  - `frontend/src/router/index.ts` - 路由配置和守卫
  - `frontend/src/App.vue` - TabBar 配置
  - `frontend/src/views/user/UserHome.vue` - 首页立即预约逻辑
  - `frontend/src/views/user/UserOrders.vue` - 游客空状态
  - `frontend/src/views/user/UserOrderCreate.vue` - 登录引导+回跳
  - `frontend/src/views/user/UserProfile.vue` - 已整合登录
  - `frontend/src/views/LoginPage.vue` - 可删除
- Database: 无影响

## ADDED Requirements

### Requirement: 游客模式浏览
系统 SHALL 允许未登录用户访问首页、订单列表、预约页面，在提交订单等关键操作时才要求登录。

#### Scenario: 游客进入系统
- **WHEN** 用户打开网站
- **THEN** 自动进入 `/user/home` 首页，可以看到服务介绍和预约流程
- **AND** 底部 TabBar 显示 `[首页] [订单] [我的]`

#### Scenario: 游客查看订单列表
- **WHEN** 游客点击"订单"Tab
- **THEN** 进入订单列表页面
- **AND** 显示空状态提示："登录后即可查看您的订单"
- **AND** 提供"立即预约"按钮引导下单

#### Scenario: 游客点击"我的"
- **WHEN** 游客点击"我的"Tab
- **THEN** 进入个人中心页面，显示品牌展示和登录表单
- **AND** 提供快捷入口："先预约清洗"、"查看我的订单"

### Requirement: 登录后回跳
系统 SHALL 在用户登录后自动跳转回之前的页面，并保持表单数据。

#### Scenario: 游客填写订单后触发登录
- **WHEN** 游客在预约页填写完表单点击"提交订单"
- **AND** 系统检测未登录，弹出登录引导对话框
- **AND** 用户点击"去登录"
- **THEN** 跳转到个人中心登录页
- **AND** 记录来源页面路径和表单数据到 sessionStorage
- **AND** 登录成功后自动跳回预约页
- **AND** 恢复表单数据并自动提交订单

### Requirement: 游客 TabBar 导航
系统 SHALL 为游客提供完整的底部导航，而非单一登录按钮。

#### Scenario: 游客底部导航
- **WHEN** 用户处于未登录状态
- **THEN** TabBar 显示 `[首页(/user/home)] [订单(/user/orders)] [我的(/user/profile)]`
- **AND** 登录后 TabBar 根据角色自动切换

## MODIFIED Requirements

### Requirement: 首页立即预约行为
修改：游客点击"立即预约"不再弹出登录对话框，直接进入预约填写页面。

**Reason**: 提升转化率，让用户先看到服务内容再决定是否登录。

### Requirement: 路由守卫
修改：游客可访问的页面列表扩展为：
- `/user/home` - 首页
- `/user/orders` - 订单列表（空状态）
- `/user/order/create` - 预约页（提交时检查登录）
- `/user/order/pay` - 支付页（需登录）
- `/user/order/detail` - 订单详情（需登录）
- `/user/profile` - 个人中心（包含登录表单）

**Reason**: 支持游客先浏览后登录的流程。

### Requirement: 根路径重定向
修改：`/` 和 `/login` 都重定向到 `/user/home`（不再是 `/user/profile`）。

**Reason**: 用户打开系统首先看到服务介绍，而不是登录页。

## REMOVED Requirements

### Requirement: 独立登录页面
**Reason**: 登录功能已整合到个人中心页面，无需单独的登录页。
**Migration**: `LoginPage.vue` 可删除，所有引用改为 `/user/profile`。
