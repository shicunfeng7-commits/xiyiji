# WashPro v4功能迭代 - 实现计划

## [x] Task 1: 创建评价表数据库表结构
- **Priority**: P0
- **Depends On**: None
- **Description**: 
  - 创建order_review表，包含订单ID、用户ID、评分、评价内容、创建时间
- **Acceptance Criteria Addressed**: AC-1
- **Test Requirements**:
  - `programmatic` TR-1.1: 表结构创建成功，包含正确字段
- **Notes**: 评价表关联订单表和用户表

## [x] Task 2: 实现服务评价后端API
- **Priority**: P0
- **Depends On**: Task 1
- **Description**: 
  - 创建评价Controller、Service、Mapper
  - 实现提交评价接口
  - 实现查询评价接口
- **Acceptance Criteria Addressed**: AC-1
- **Test Requirements**:
  - `programmatic` TR-2.1: POST /api/user/order/review 提交评价成功返回200
  - `programmatic` TR-2.2: GET /api/user/order/review/{orderId} 获取评价成功
- **Notes**: 需要验证订单已完成且未评价过

## [x] Task 3: 实现用户端评价功能页面
- **Priority**: P0
- **Depends On**: Task 2
- **Description**: 
  - 在订单详情页添加评价按钮和弹窗
  - 实现1-5星评分组件
  - 实现文字评价输入
- **Acceptance Criteria Addressed**: AC-1
- **Test Requirements**:
  - `programmatic` TR-3.1: 评价弹窗正确显示
  - `human-judgment` TR-3.2: 评分交互流畅，按钮响应及时
- **Notes**: 只有已完成订单显示评价按钮

## [x] Task 4: 实现管理员订单排序后端API
- **Priority**: P0
- **Depends On**: None
- **Description**: 
  - 修改订单列表接口，支持排序参数
  - 支持按服务时间、创建时间、状态优先级排序
- **Acceptance Criteria Addressed**: AC-2
- **Test Requirements**:
  - `programmatic` TR-4.1: GET /api/admin/order/list?sort=serviceTime 返回按服务时间排序的订单
  - `programmatic` TR-4.2: GET /api/admin/order/list?sort=createTime 返回按创建时间排序的订单
  - `programmatic` TR-4.3: GET /api/admin/order/list?sort=status 返回按状态优先级排序的订单
- **Notes**: 状态优先级：未支付 > 待服务 > 服务中 > 已完成 > 已取消

## [x] Task 5: 实现管理员订单排序前端功能
- **Priority**: P0
- **Depends On**: Task 4
- **Description**: 
  - 在管理员订单列表页面添加排序下拉选择
  - 实现排序切换功能
- **Acceptance Criteria Addressed**: AC-2
- **Test Requirements**:
  - `programmatic` TR-5.1: 排序下拉菜单正确显示选项
  - `human-judgment` TR-5.2: 切换排序方式后列表正确刷新
- **Notes**: 默认按创建时间降序

## [x] Task 6: 实现WebSocket消息推送后端
- **Priority**: P0
- **Depends On**: None
- **Description**: 
  - 创建WebSocket配置类
  - 创建消息推送服务
  - 在订单创建时推送消息给员工端
- **Acceptance Criteria Addressed**: AC-3
- **Test Requirements**:
  - `programmatic` TR-6.1: WebSocket连接成功
  - `programmatic` TR-6.2: 新订单创建时推送消息
- **Notes**: 使用Spring WebSocket

## [x] Task 7: 实现员工端WebSocket消息接收
- **Priority**: P0
- **Depends On**: Task 6
- **Description**: 
  - 员工端建立WebSocket连接
  - 接收新订单通知
  - 显示通知提示
- **Acceptance Criteria Addressed**: AC-3
- **Test Requirements**:
  - `programmatic` TR-7.1: WebSocket连接状态正常
  - `human-judgment` TR-7.2: 收到新订单时有视觉提示
- **Notes**: 需要处理连接断开重连

## [x] Task 8: 创建数据看板页面
- **Priority**: P1
- **Depends On**: None
- **Description**: 
  - 创建管理员数据看板页面
  - 展示订单统计卡片
  - 展示营收趋势图表
- **Acceptance Criteria Addressed**: AC-4
- **Test Requirements**:
  - `human-judgment` TR-8.1: 数据展示清晰美观
  - `human-judgment` TR-8.2: 图表响应流畅
- **Notes**: 使用简单的CSS图表

## [x] Task 9: 实现异常处理机制
- **Priority**: P1
- **Depends On**: None
- **Description**: 
  - 统一前端错误处理
  - 添加网络异常、数据异常、业务异常处理
  - 显示友好错误提示
- **Acceptance Criteria Addressed**: AC-5
- **Test Requirements**:
  - `human-judgment` TR-9.1: 网络异常时显示友好提示
  - `human-judgment` TR-9.2: 空数据时显示空状态页面
- **Notes**: 利用已创建的EmptyState组件

## [x] Task 10: 测试验证与代码提交
- **Priority**: P0
- **Depends On**: 所有其他任务
- **Description**: 
  - 测试所有新增功能
  - 修复发现的问题
  - Git提交代码
- **Acceptance Criteria Addressed**: 所有AC
- **Test Requirements**:
  - `human-judgment` TR-10.1: 所有功能正常运行
  - `human-judgment` TR-10.2: 代码提交成功
- **Notes**: 按功能模块提交