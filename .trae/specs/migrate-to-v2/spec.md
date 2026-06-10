# 洗洗衣 v2 改造 Spec

## Why
基于新设计文档（设计文档-v2.md），对整个系统进行改造，从原有的三端独立登录模式，改为**手机号统一登录 + 角色动态导航 + 员工申请审核**模式，并增加操作确认弹窗、支付回退等防误触功能。

## What Changes
- **BREAKING**: 登录方式改为手机号统一登录，移除原有用户/员工独立登录
- **BREAKING**: 员工表改为关联 user_id，不再是独立账号
- 用户表增加 `role` 字段标记是否同时是员工
- 新增 `employee_application` 员工申请表
- 新增 Auth 模块（手机号登录/注册、Token 鉴权）
- 新增 Token 登录拦截器，所有接口需校验 Token
- 新增管理员 API：`revert-pay`（回退支付）、`employee/applications`、`approve`、`reject`
- 新增用户端 API：`apply-employee`、`apply-status`
- 新增管理员端页面：员工审核、服务时间配置
- 新增用户端页面：个人中心、申请成为员工
- 前端导航栏改为根据角色动态显示
- 抢单操作增加确认弹窗
- 确认支付操作增加确认弹窗 + 回退功能
- 数据库 schema 更新（user 增加 role 字段，新增 employee_application 表）

## Impact
- Affected specs: 后端所有模块、前端所有页面、数据库 schema
- Affected code: 全部后端 Controller/Service/Entity/Mapper、前端路由/页面/导航

## ADDED Requirements

### Requirement: Auth 手机号登录/注册
The system SHALL provide a unified phone number login and registration endpoint.

#### Scenario: New user registration
- **WHEN** user enters a new phone number
- **THEN** system creates a new user record with role=0 (普通用户) and returns a token

#### Scenario: Existing user login
- **WHEN** user enters an existing phone number
- **THEN** system returns token and user info (including role)

### Requirement: Token 鉴权拦截器
The system SHALL validate Bearer token on all authenticated endpoints.

#### Scenario: Valid token
- **WHEN** request includes valid `Authorization: Bearer {token}` header
- **THEN** request proceeds to controller

#### Scenario: Invalid/missing token
- **WHEN** request has no token or invalid token
- **THEN** return 401 Unauthorized

### Requirement: Employee Application Flow
The system SHALL allow users to apply for employee status and admins to approve/reject.

#### Scenario: User applies
- **WHEN** user submits employee application with name
- **THEN** creates PENDING application record

#### Scenario: Admin approves
- **WHEN** admin approves an application
- **THEN** creates employee record linked to user_id, updates user.role=1, sets application to APPROVED

#### Scenario: Admin rejects
- **WHEN** admin rejects with reason
- **THEN** sets application to REJECTED with remark

### Requirement: Payment Revert
The system SHALL allow admin to revert a confirmed payment before an employee claims the order.

#### Scenario: Revert payment
- **WHEN** admin clicks revert-pay on a PAID order with no employee assigned
- **THEN** order status goes back to UNPAID

#### Scenario: Cannot revert claimed order
- **WHEN** admin tries to revert a PAID order that already has an employee_id
- **THEN** return error "已抢单的订单不可回退"

### Requirement: Confirmation Dialogs
The system SHALL show confirmation dialogs for grab-order and confirm-pay actions.

#### Scenario: Grab order confirmation
- **WHEN** employee clicks grab
- **THEN** show dialog "确认抢此订单？" before sending request

#### Scenario: Confirm pay confirmation
- **WHEN** admin clicks confirm-pay
- **THEN** show dialog with "此操作可回退" before sending request

## MODIFIED Requirements

### Requirement: User Entity - Add role field
User entity SHALL include a `role` field (TINYINT: 0-普通用户, 1-员工).

### Requirement: Employee Entity - Link to user
Employee entity SHALL use `user_id` instead of independent `username`/`password`.

### Requirement: Frontend Navigation - Dynamic tabbar
App tabbar SHALL dynamically show/hide tabs based on user role.

#### User role (role=0): 首页 + 订单 + 我的
#### User+Employee (role=1): 首页 + 订单 + 员工 + 我的
#### Admin (独立登录): 订单 + 员工 + 审核 + 配置

## REMOVED Requirements

### Requirement: User独立登录
**Reason**: 改为手机号统一登录
**Migration**: POST /api/auth/login 替代原有 /api/user/login

### Requirement: Employee独立登录
**Reason**: 员工通过手机号 + 角色关联登录
**Migration**: 删除 /api/employee/login，改为 auth 模块统一处理

### Requirement: Employee独立增删（旧方式）
**Reason**: 改为用户申请 + 管理员审核模式
**Migration**: 保留 employee/remove，新增 application 审批流程