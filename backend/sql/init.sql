CREATE DATABASE IF NOT EXISTS washpro DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
USE washpro;

-- 清理已废弃的旧表
DROP TABLE IF EXISTS `admin`;

-- 用户表（统一用户表：管理员0、员工1、用户2）
CREATE TABLE IF NOT EXISTS `user` (
    `id` BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '用户ID',
    `username` VARCHAR(50) DEFAULT NULL COMMENT '管理员/员工用户名',
    `password` VARCHAR(255) DEFAULT NULL COMMENT '登录密码',
    `phone` VARCHAR(20) DEFAULT NULL COMMENT '手机号',
    `nickname` VARCHAR(50) DEFAULT NULL COMMENT '昵称',
    `avatar` VARCHAR(500) DEFAULT NULL COMMENT '头像URL',
    `building_name` VARCHAR(100) DEFAULT NULL COMMENT '宿舍楼栋',
    `room_no` VARCHAR(50) DEFAULT NULL COMMENT '宿舍房号',
    `role` TINYINT DEFAULT 2 COMMENT '角色：0-管理员, 1-员工, 2-用户',
    `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time` DATETIME DEFAULT NULL ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    UNIQUE KEY uk_username (`username`),
    INDEX idx_phone (`phone`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- 插入默认管理员 (role=0)
-- 密码 admin123 的 BCrypt 加密值，忘记密码时记住原文是 admin123 即可
INSERT INTO `user` (`username`, `password`, `nickname`, `role`) VALUES
('admin', '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lE9lBOsl7iKTVKIUi', '管理员', 0),
('admin2', '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lE9lBOsl7iKTVKIUi', '管理员2', 0)
ON DUPLICATE KEY UPDATE `id` = `id`;

-- 员工表
CREATE TABLE IF NOT EXISTS `employee` (
    `id` BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '员工ID',
    `user_id` BIGINT DEFAULT NULL COMMENT '关联用户ID',
    `name` VARCHAR(50) NOT NULL COMMENT '姓名',
    `phone` VARCHAR(20) DEFAULT NULL COMMENT '联系电话',
    `status` TINYINT DEFAULT 0 COMMENT '工作状态：0-空闲, 1-服务中',
    `is_active` TINYINT DEFAULT 1 COMMENT '启用状态：0-停用, 1-启用',
    `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间'
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- 员工申请表
CREATE TABLE IF NOT EXISTS `employee_application` (
    `id` BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '申请ID',
    `user_id` BIGINT DEFAULT NULL COMMENT '申请人用户ID',
    `name` VARCHAR(50) NOT NULL COMMENT '姓名',
    `phone` VARCHAR(20) DEFAULT NULL COMMENT '联系电话',
    `major` VARCHAR(100) DEFAULT NULL COMMENT '学院专业',
    `grade` VARCHAR(50) DEFAULT NULL COMMENT '年级',
    `status` TINYINT DEFAULT 0 COMMENT '审核状态：0-待审核, 1-已通过, 2-已拒绝',
    `remark` VARCHAR(255) DEFAULT NULL COMMENT '审核备注/拒绝原因',
    `handler_id` BIGINT DEFAULT NULL COMMENT '审核人ID',
    `handle_time` DATETIME DEFAULT NULL COMMENT '审核时间',
    `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '申请时间'
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- 订单表
CREATE TABLE IF NOT EXISTS `orders` (
    `id` BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '订单ID',
    `order_no` VARCHAR(32) NOT NULL COMMENT '订单编号',
    `user_id` BIGINT DEFAULT NULL COMMENT '下单用户ID',
    `user_name` VARCHAR(50) DEFAULT NULL COMMENT '下单用户昵称',
    `building_category` VARCHAR(20) NOT NULL COMMENT '楼栋分类（食宿楼/学生宿舍/教师公寓）',
    `building_name` VARCHAR(20) NOT NULL COMMENT '楼栋名称',
    `room_no` VARCHAR(20) NOT NULL COMMENT '房间号',
    `contact_phone` VARCHAR(20) DEFAULT NULL COMMENT '联系电话',
    `service_date` DATE DEFAULT NULL COMMENT '服务日期',
    `start_time` VARCHAR(10) DEFAULT NULL COMMENT '服务开始时间',
    `end_time` VARCHAR(10) DEFAULT NULL COMMENT '服务结束时间',
    `status` TINYINT DEFAULT 0 COMMENT '订单状态：0-未支付, 1-已支付, 2-服务中, 3-已完成, 4-已取消, 5-待服务',
    `employee_id` BIGINT DEFAULT NULL COMMENT '接单员工ID',
    `amount` DECIMAL(10,2) DEFAULT 29.90 COMMENT '订单金额',
    `remark` VARCHAR(255) DEFAULT NULL COMMENT '用户备注',
    `before_photo` VARCHAR(2000) DEFAULT NULL COMMENT '清洗前照片（JSON数组）',
    `after_photo` VARCHAR(2000) DEFAULT NULL COMMENT '清洗后照片（JSON数组）',
    `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '下单时间',
    `pay_time` DATETIME DEFAULT NULL COMMENT '支付时间',
    `complete_time` DATETIME DEFAULT NULL COMMENT '完成时间',
    INDEX idx_order_no (`order_no`),
    INDEX idx_user_id (`user_id`),
    INDEX idx_employee_id (`employee_id`),
    INDEX idx_status (`status`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- 订单状态变更日志表
CREATE TABLE IF NOT EXISTS `order_status_log` (
    `id` BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '日志ID',
    `order_id` BIGINT NOT NULL COMMENT '关联订单ID',
    `from_status` TINYINT DEFAULT NULL COMMENT '变更前状态',
    `to_status` TINYINT NOT NULL COMMENT '变更后状态',
    `operator_type` TINYINT DEFAULT NULL COMMENT '操作人类型：0-用户, 1-管理员, 2-员工',
    `operator_id` BIGINT DEFAULT NULL COMMENT '操作人ID',
    `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '操作时间',
    INDEX idx_order_id (`order_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- 服务时间段配置表
CREATE TABLE IF NOT EXISTS `service_time_config` (
    `id` BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '配置ID',
    `period` VARCHAR(20) NOT NULL COMMENT '时段标识：MORNING/AFTERNOON/EVENING',
    `period_name` VARCHAR(20) NOT NULL COMMENT '时段名称（上午/下午/晚上）',
    `start_hour` INT NOT NULL COMMENT '开始小时（0-23）',
    `end_hour` INT NOT NULL COMMENT '结束小时（0-23）',
    `enabled` TINYINT(1) DEFAULT 1 COMMENT '是否启用：0-停用, 1-启用',
    `sort_order` INT DEFAULT 0 COMMENT '排序序号',
    `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time` DATETIME DEFAULT NULL ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间'
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- 订单评价表
CREATE TABLE IF NOT EXISTS `order_review` (
    `id` BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '评价ID',
    `order_id` BIGINT NOT NULL COMMENT '关联订单ID',
    `user_id` BIGINT DEFAULT NULL COMMENT '评价用户ID',
    `score` INT NOT NULL COMMENT '评分（1-5分）',
    `content` VARCHAR(500) DEFAULT NULL COMMENT '评价内容',
    `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '评价时间',
    INDEX idx_order_id (`order_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

INSERT INTO `service_time_config` (`period`, `period_name`, `start_hour`, `end_hour`, `enabled`, `sort_order`) VALUES
('MORNING', '上午', 0, 12, 1, 1),
('AFTERNOON', '下午', 12, 18, 1, 2),
('EVENING', '晚上', 18, 24, 1, 3)
ON DUPLICATE KEY UPDATE `id` = `id`;
