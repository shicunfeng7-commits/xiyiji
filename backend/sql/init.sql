CREATE DATABASE IF NOT EXISTS washpro DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
USE washpro;

-- user table
CREATE TABLE IF NOT EXISTS `user` (
    `id` BIGINT AUTO_INCREMENT PRIMARY KEY,
    `phone` VARCHAR(20) DEFAULT NULL,
    `nickname` VARCHAR(50) DEFAULT NULL,
    `avatar` VARCHAR(500) DEFAULT NULL,
    `role` TINYINT DEFAULT 0 COMMENT '0-普通用户, 1-员工',
    `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP,
    `update_time` DATETIME DEFAULT NULL ON UPDATE CURRENT_TIMESTAMP,
    INDEX idx_phone (`phone`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- admin table
CREATE TABLE IF NOT EXISTS `admin` (
    `id` BIGINT AUTO_INCREMENT PRIMARY KEY,
    `username` VARCHAR(50) NOT NULL,
    `password` VARCHAR(255) NOT NULL,
    `name` VARCHAR(50) DEFAULT NULL,
    `wechat_qrcode` VARCHAR(500) DEFAULT NULL,
    `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP,
    UNIQUE KEY uk_username (`username`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

INSERT INTO `admin` (`username`, `password`, `name`, `wechat_qrcode`) VALUES
('admin', 'admin123', '管理员', '')
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
    `status` TINYINT DEFAULT 0 COMMENT '0-待审核, 1-已通过, 2-已拒绝',
    `remark` VARCHAR(255) DEFAULT NULL,
    `handler_id` BIGINT DEFAULT NULL,
    `handle_time` DATETIME DEFAULT NULL,
    `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

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
    `period_name` VARCHAR(20) NOT NULL,
    `start_time` VARCHAR(5) NOT NULL,
    `end_time` VARCHAR(5) NOT NULL,
    `enabled` TINYINT(1) DEFAULT 1,
    `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP,
    `update_time` DATETIME DEFAULT NULL ON UPDATE CURRENT_TIMESTAMP
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

INSERT INTO `service_time_config` (`period_name`, `start_time`, `end_time`, `enabled`) VALUES
('上午', '00:00', '12:00', 1),
('下午', '12:00', '18:00', 1),
('晚上', '18:00', '24:00', 1)
ON DUPLICATE KEY UPDATE `id` = `id`;