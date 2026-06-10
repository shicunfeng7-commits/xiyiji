CREATE DATABASE IF NOT EXISTS washpro DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
USE washpro;

-- user table
CREATE TABLE IF NOT EXISTS `user` (
    `id` BIGINT AUTO_INCREMENT PRIMARY KEY,
    `nickname` VARCHAR(50) DEFAULT NULL,
    `phone` VARCHAR(20) DEFAULT NULL,
    `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP,
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

INSERT INTO `admin` (`username`, `password`, `name`) VALUES
('admin', '$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy', '管理员')
ON DUPLICATE KEY UPDATE `id` = `id`;

-- employee table
CREATE TABLE IF NOT EXISTS `employee` (
    `id` BIGINT AUTO_INCREMENT PRIMARY KEY,
    `username` VARCHAR(50) NOT NULL,
    `password` VARCHAR(255) NOT NULL,
    `name` VARCHAR(50) NOT NULL,
    `phone` VARCHAR(20) DEFAULT NULL,
    `status` TINYINT DEFAULT 0,
    `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP,
    UNIQUE KEY uk_emp_username (`username`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

INSERT INTO `employee` (`username`, `password`, `name`, `phone`) VALUES
('emp001', '$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy', '陈师傅', '13800001111')
ON DUPLICATE KEY UPDATE `id` = `id`;

-- orders table
CREATE TABLE IF NOT EXISTS `orders` (
    `id` BIGINT AUTO_INCREMENT PRIMARY KEY,
    `order_no` VARCHAR(32) NOT NULL,
    `user_id` BIGINT DEFAULT NULL,
    `user_name` VARCHAR(50) DEFAULT NULL,
    `building_category` VARCHAR(20) NOT NULL,
    `building_name` VARCHAR(20) NOT NULL,
    `room_no` VARCHAR(20) NOT NULL,
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
    `period` VARCHAR(20) NOT NULL,
    `period_name` VARCHAR(20) NOT NULL,
    `start_hour` INT NOT NULL,
    `end_hour` INT NOT NULL,
    `enabled` TINYINT(1) DEFAULT 1,
    `sort_order` INT DEFAULT 0,
    `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP,
    UNIQUE KEY uk_period (`period`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

INSERT INTO `service_time_config` (`period`, `period_name`, `start_hour`, `end_hour`, `enabled`, `sort_order`) VALUES
('MORNING', 'Morning', 0, 12, 1, 1),
('AFTERNOON', 'Afternoon', 12, 18, 1, 2),
('EVENING', 'Evening', 18, 24, 1, 3)
ON DUPLICATE KEY UPDATE `id` = `id`;