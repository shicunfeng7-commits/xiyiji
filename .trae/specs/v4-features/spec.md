# WashPro v4功能迭代 - 产品需求文档

## Overview
- **Summary**: 基于设计文档v4，实现新增功能：服务评价、订单排序、WebSocket实时消息推送、数据看板等核心功能
- **Purpose**: 提升用户体验，增强系统功能完整性，优化管理员和员工操作效率
- **Target Users**: 用户端、管理端、员工端三端用户

## Goals
- 实现服务评价功能（用户端）
- 实现订单排序功能（管理端）
- 实现WebSocket实时消息推送（跨端）
- 实现数据看板（管理端）
- 实现异常场景处理机制

## Non-Goals (Out of Scope)
- 微信支付接口（已明确排除）
- 微信小程序版本
- 短信通知功能

## Background & Context
- 系统已实现基础功能：用户下单、管理员确认支付、员工抢单
- 需要增强用户体验和管理效率
- 需要完善异常处理机制

## Functional Requirements
- **FR-1**: 用户可对已完成订单进行星级评价和文字评价
- **FR-2**: 管理员可按多种方式排序订单（服务时间、创建时间、状态优先级）
- **FR-3**: WebSocket实时推送新订单和状态变更通知
- **FR-4**: 管理端数据看板展示订单统计和营收分析
- **FR-5**: 完善异常场景处理（网络、数据、业务、并发）

## Non-Functional Requirements
- **NFR-1**: 消息推送延迟 < 1秒
- **NFR-2**: 订单列表加载时间 < 500ms
- **NFR-3**: 数据看板图表响应流畅

## Constraints
- **Technical**: Vue3 + Vant UI + Spring Boot 3.x
- **Dependencies**: Redis用于WebSocket消息发布

## Assumptions
- WebSocket服务已配置好
- Redis服务正常运行

## Acceptance Criteria

### AC-1: 用户服务评价
- **Given**: 用户有已完成的订单
- **When**: 用户进入订单详情页点击评价按钮
- **Then**: 显示评价弹窗，支持1-5星评分和文字输入
- **Verification**: `programmatic`

### AC-2: 管理员订单排序
- **Given**: 管理员进入订单管理页面
- **When**: 选择排序方式（服务时间/创建时间/状态优先级）
- **Then**: 订单列表按选择的方式排序
- **Verification**: `programmatic`

### AC-3: WebSocket消息推送
- **Given**: 员工端在线
- **When**: 有新订单创建
- **Then**: 员工端收到实时通知
- **Verification**: `programmatic`

### AC-4: 数据看板展示
- **Given**: 管理员登录系统
- **When**: 进入数据看板页面
- **Then**: 显示订单统计、营收分析图表
- **Verification**: `human-judgment`

### AC-5: 异常处理
- **Given**: 网络断开或服务不可用
- **When**: 用户操作触发请求
- **Then**: 显示友好错误提示，提供重试选项
- **Verification**: `human-judgment`

## Open Questions
- [ ] 评价是否支持上传图片？（当前设计为文字评价）
- [ ] 数据看板需要哪些具体图表？
