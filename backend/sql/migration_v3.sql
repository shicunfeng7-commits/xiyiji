-- WashPro v3 数据库迁移: 首页精选展示功能
-- 执行方式: 在 washpro 数据库执行此 SQL
USE washpro;

-- 1. orders 表添加精选照片相关字段
ALTER TABLE `orders`
    ADD COLUMN `is_photo_featured` TINYINT DEFAULT 0 COMMENT '是否展示照片：0-否，1-是',
    ADD COLUMN `show_order` INT DEFAULT 0 COMMENT '展示顺序：0不展示，数字越小越靠前',
    ADD COLUMN `featured_photos` VARCHAR(3000) DEFAULT NULL COMMENT '管理员精选照片（JSON数组）';

-- 2. order_review 表添加精选评价字段
ALTER TABLE `order_review`
    ADD COLUMN `is_featured` TINYINT DEFAULT 0 COMMENT '是否精选展示：0-否，1-是';
