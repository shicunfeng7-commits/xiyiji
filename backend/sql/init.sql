CREATE DATABASE IF NOT EXISTS washpro DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
USE washpro;

-- 清理已废弃的旧表
DROP TABLE IF EXISTS `admin`;

-- user table (统一用户表：管理员0、员工1、用户2)
CREATE TABLE IF NOT EXISTS `user` (
    `id` BIGINT AUTO_INCREMENT PRIMARY KEY,
    `username` VARCHAR(50) DEFAULT NULL COMMENT '管理员/员工用户名',
    `password` VARCHAR(255) DEFAULT NULL COMMENT '登录密码',
    `phone` VARCHAR(20) DEFAULT NULL,
    `nickname` VARCHAR(50) DEFAULT NULL,
    `avatar` VARCHAR(500) DEFAULT NULL,
    `building_name` VARCHAR(100) DEFAULT NULL COMMENT '宿舍楼栋',
    `room_no` VARCHAR(50) DEFAULT NULL COMMENT '宿舍房号',
    `role` TINYINT DEFAULT 2 COMMENT '0-管理员, 1-员工, 2-用户',
    `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP,
    `update_time` DATETIME DEFAULT NULL ON UPDATE CURRENT_TIMESTAMP,
    UNIQUE KEY uk_username (`username`),
    INDEX idx_phone (`phone`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- 插入默认管理员 (role=0)
INSERT INTO `user` (`username`, `password`, `nickname`, `role`) VALUES
('admin', 'admin123', '管理员', 0),
('admin2', 'admin123', '管理员2', 0)
ON DUPLICATE KEY UPDATE `id` = `id`;

-- employee table
CREATE TABLE IF NOT EXISTS `employee` (
    `id` BIGINT AUTO_INCREMENT PRIMARY KEY,
    `user_id` BIGINT DEFAULT NULL,
    `name` VARCHAR(50) NOT NULL,
    `phone` VARCHAR(20) DEFAULT NULL,
    `status` TINYINT DEFAULT 0 COMMENT '0-空闲, 1-服务中',
    `is_active` TINYINT DEFAULT 1 COMMENT '0-禁用, 1-启用',
    `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- employee_application table
CREATE TABLE IF NOT EXISTS `employee_application` (
    `id` BIGINT AUTO_INCREMENT PRIMARY KEY,
    `user_id` BIGINT DEFAULT NULL,
    `name` VARCHAR(50) NOT NULL,
    `phone` VARCHAR(20) DEFAULT NULL,
    `major` VARCHAR(100) DEFAULT NULL COMMENT '学院专业',
    `grade` VARCHAR(50) DEFAULT NULL COMMENT '年级',
    `status` TINYINT DEFAULT 0 COMMENT '0-待审核, 1-已通过, 2-已拒绝',
    `remark` VARCHAR(255) DEFAULT NULL,
    `handler_id` BIGINT DEFAULT NULL,
    `handle_time` DATETIME DEFAULT NULL,
    `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- employee_application table 已包含 major 和 grade 字段

-- orders table
CREATE TABLE IF NOT EXISTS `orders` (
    `id` BIGINT AUTO_INCREMENT PRIMARY KEY,
    `order_no` VARCHAR(32) NOT NULL,
    `user_id` BIGINT DEFAULT NULL,
    `user_name` VARCHAR(50) DEFAULT NULL,
    `building_category` VARCHAR(20) NOT NULL COMMENT '楼栋分类（食宿楼/学生宿舍/教师公寓）',
    `building_name` VARCHAR(20) NOT NULL,
    `room_no` VARCHAR(20) NOT NULL,
    `contact_phone` VARCHAR(20) DEFAULT NULL,
    `service_date` DATE DEFAULT NULL,
    `start_time` VARCHAR(10) DEFAULT NULL,
    `end_time` VARCHAR(10) DEFAULT NULL,
    `status` TINYINT DEFAULT 0,
    `employee_id` BIGINT DEFAULT NULL,
    `amount` DECIMAL(10,2) DEFAULT 29.90,
    `remark` VARCHAR(255) DEFAULT NULL,
    `before_photo` VARCHAR(2000) DEFAULT NULL COMMENT '清洗前照片JSON数组',
    `after_photo` VARCHAR(2000) DEFAULT NULL COMMENT '清洗后照片JSON数组',
    `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP,
    `pay_time` DATETIME DEFAULT NULL,
    `complete_time` DATETIME DEFAULT NULL,
    INDEX idx_order_no (`order_no`),
    INDEX idx_user_id (`user_id`),
    INDEX idx_employee_id (`employee_id`),
    INDEX idx_status (`status`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- order status log table
CREATE TABLE IF NOT EXISTS `order_status_log` (
    `id` BIGINT AUTO_INCREMENT PRIMARY KEY,
    `order_id` BIGINT NOT NULL,
    `from_status` TINYINT DEFAULT NULL,
    `to_status` TINYINT NOT NULL,
    `operator_type` TINYINT DEFAULT NULL,
    `operator_id` BIGINT DEFAULT NULL,
    `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP,
    INDEX idx_order_id (`order_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- service time config table
CREATE TABLE IF NOT EXISTS `service_time_config` (
    `id` BIGINT AUTO_INCREMENT PRIMARY KEY,
    `period` VARCHAR(20) NOT NULL COMMENT '时段标识（MORNING/AFTERNOON/EVENING）',
    `period_name` VARCHAR(20) NOT NULL COMMENT '时段名称',
    `start_hour` INT NOT NULL COMMENT '开始小时',
    `end_hour` INT NOT NULL COMMENT '结束小时',
    `enabled` TINYINT(1) DEFAULT 1,
    `sort_order` INT DEFAULT 0 COMMENT '排序',
    `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP,
    `update_time` DATETIME DEFAULT NULL ON UPDATE CURRENT_TIMESTAMP
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- order_review table
CREATE TABLE IF NOT EXISTS `order_review` (
    `id` BIGINT AUTO_INCREMENT PRIMARY KEY,
    `order_id` BIGINT NOT NULL,
    `user_id` BIGINT DEFAULT NULL,
    `score` INT NOT NULL COMMENT '评分 1-5',
    `content` VARCHAR(500) DEFAULT NULL COMMENT '评价内容',
    `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP,
    INDEX idx_order_id (`order_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

INSERT INTO `service_time_config` (`period`, `period_name`, `start_hour`, `end_hour`, `enabled`, `sort_order`) VALUES
('MORNING', '上午', 0, 12, 1, 1),
('AFTERNOON', '下午', 12, 18, 1, 2),
('EVENING', '晚上', 18, 24, 1, 3)
ON DUPLICATE KEY UPDATE `id` = `id`;