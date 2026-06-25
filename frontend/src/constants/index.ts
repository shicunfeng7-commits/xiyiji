/** 订单状态常量，与后端 OrderStatus 一致 */
export const ORDER_STATUS = {
  UNPAID: 0,
  PAID: 1,
  IN_PROGRESS: 2,
  COMPLETED: 3,
  CANCELLED: 4,
  PENDING_SERVICE: 5,
} as const

export const ORDER_STATUS_MAP: Record<number, string> = {
  [ORDER_STATUS.UNPAID]: '未支付',
  [ORDER_STATUS.PAID]: '已支付',
  [ORDER_STATUS.IN_PROGRESS]: '服务中',
  [ORDER_STATUS.COMPLETED]: '已完成',
  [ORDER_STATUS.CANCELLED]: '已取消',
  [ORDER_STATUS.PENDING_SERVICE]: '待服务',
}

export const ORDER_STATUS_COLOR: Record<number, string> = {
  [ORDER_STATUS.UNPAID]: '#FF9500',
  [ORDER_STATUS.PAID]: '#007AFF',
  [ORDER_STATUS.IN_PROGRESS]: '#34C759',
  [ORDER_STATUS.COMPLETED]: '#86868B',
  [ORDER_STATUS.CANCELLED]: '#FF3B30',
  [ORDER_STATUS.PENDING_SERVICE]: '#5AC8FA',
}

/** 角色常量 */
export const ROLE = {
  ADMIN: 'admin',
  EMPLOYEE: 'employee',
  USER: 'user',
} as const
