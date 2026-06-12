-- 数据库迁移脚本：添加 contact_phone 字段
USE washpro;
ALTER TABLE `orders` ADD COLUMN `contact_phone` VARCHAR(20) DEFAULT NULL AFTER `room_no`;
