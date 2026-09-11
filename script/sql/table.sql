-- =============================================================
-- 电影票务系统 CD-TICKET 数据库结构（12 张表）
-- 设计依据：docs/superpowers/specs/2026-09-11-database-design.md
-- 幂等重建脚本（dev 环境）：先清理旧表，再建全部表
-- 执行：mysql -uroot -p cd_ticket < script/sql/table.sql
-- =============================================================

-- 清理旧表（上一版裸名表）与全部新表，保证重复执行幂等
DROP TABLE IF EXISTS `t_local_message`, `t_payment_record`, `t_order_item`, `t_order`,
    `t_screening`, `t_seat_config`, `t_hall`, `t_banner`, `t_movie`, `t_user`,
    `t_cinema`, `t_admin`, `admin`, `cinema`;

-- 管理员表
CREATE TABLE `t_admin`
(
    `id`          BIGINT       NOT NULL AUTO_INCREMENT COMMENT '管理员ID（主键）',
    `username`    VARCHAR(32)  NOT NULL COMMENT '用户名',
    `password`    VARCHAR(100) NOT NULL COMMENT '密码（BCrypt加密）',
    `role`        TINYINT      NOT NULL COMMENT '角色（0-超级管理员 1-影院管理员）',
    `cinema_id`   BIGINT       NOT NULL DEFAULT 0 COMMENT '关联影院ID（超级管理员为0）',
    `status`      TINYINT      NOT NULL DEFAULT 1 COMMENT '状态（0-禁用 1-启用）',
    `create_time` DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time` DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    `deleted`     TINYINT      NOT NULL DEFAULT 0 COMMENT '逻辑删除（0-否 1-是）',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_username` (`username`),
    KEY `idx_cinema` (`cinema_id`)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4
  COLLATE = utf8mb4_0900_ai_ci COMMENT = '管理员表';

-- 用户表（C端）
CREATE TABLE `t_user`
(
    `id`          BIGINT       NOT NULL AUTO_INCREMENT COMMENT '用户ID（主键）',
    `phone`       VARCHAR(20)  NOT NULL COMMENT '手机号（登录凭证）',
    `password`    VARCHAR(100) NULL COMMENT '密码（BCrypt，可空=未设置密码）',
    `nickname`    VARCHAR(50)  NOT NULL DEFAULT '' COMMENT '昵称（默认"用户+手机尾号"，应用层生成）',
    `status`      TINYINT      NOT NULL DEFAULT 1 COMMENT '状态（0-禁用 1-正常）',
    `create_time` DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time` DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    `deleted`     TINYINT      NOT NULL DEFAULT 0 COMMENT '逻辑删除（0-否 1-是）',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_phone` (`phone`)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4
  COLLATE = utf8mb4_0900_ai_ci COMMENT = '用户表';

-- 电影表
CREATE TABLE `t_movie`
(
    `id`           BIGINT       NOT NULL AUTO_INCREMENT COMMENT '电影ID（主键）',
    `title`        VARCHAR(100) NOT NULL COMMENT '片名',
    `poster`       VARCHAR(255) NOT NULL DEFAULT '' COMMENT '海报URL（MinIO）',
    `description`  TEXT         NULL COMMENT '简介',
    `duration`     INT          NOT NULL DEFAULT 0 COMMENT '时长（分钟）',
    `release_date` DATE         NULL COMMENT '上映日期',
    `status`       TINYINT      NOT NULL DEFAULT 1 COMMENT '状态（0-下架 1-上架；热映/待映由 release_date 与当前时间比对得出）',
    `create_time`  DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time`  DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    `deleted`      TINYINT      NOT NULL DEFAULT 0 COMMENT '逻辑删除（0-否 1-是）',
    PRIMARY KEY (`id`),
    KEY `idx_status_release` (`status`, `release_date`)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4
  COLLATE = utf8mb4_0900_ai_ci COMMENT = '电影表';

-- 轮播图表
CREATE TABLE `t_banner`
(
    `id`          BIGINT       NOT NULL AUTO_INCREMENT COMMENT '轮播图ID（主键）',
    `image`       VARCHAR(255) NOT NULL COMMENT '图片URL',
    `link_url`    VARCHAR(255) NOT NULL DEFAULT '' COMMENT '跳转链接',
    `sort`        INT          NOT NULL DEFAULT 0 COMMENT '排序（小在前）',
    `status`      TINYINT      NOT NULL DEFAULT 0 COMMENT '状态（0-下线 1-上线）',
    `create_time` DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time` DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    `deleted`     TINYINT      NOT NULL DEFAULT 0 COMMENT '逻辑删除（0-否 1-是）',
    PRIMARY KEY (`id`),
    KEY `idx_status_sort` (`status`, `sort`)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4
  COLLATE = utf8mb4_0900_ai_ci COMMENT = '轮播图表';

-- 影院表
CREATE TABLE `t_cinema`
(
    `id`          BIGINT       NOT NULL AUTO_INCREMENT COMMENT '影院ID（主键）',
    `name`        VARCHAR(100) NOT NULL COMMENT '影院名称',
    `address`     VARCHAR(255) NOT NULL COMMENT '详细地址',
    `status`      TINYINT      NOT NULL DEFAULT 1 COMMENT '状态（0-停业 1-营业）',
    `create_time` DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time` DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    `deleted`     TINYINT      NOT NULL DEFAULT 0 COMMENT '逻辑删除（0-否 1-是）',
    PRIMARY KEY (`id`)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4
  COLLATE = utf8mb4_0900_ai_ci COMMENT = '影院表';

-- 影厅表
CREATE TABLE `t_hall`
(
    `id`          BIGINT      NOT NULL AUTO_INCREMENT COMMENT '影厅ID（主键）',
    `cinema_id`   BIGINT      NOT NULL COMMENT '所属影院ID',
    `name`        VARCHAR(50) NOT NULL COMMENT '影厅名称（如"1号厅"）',
    `seat_rows`   INT         NOT NULL COMMENT '座位排数',
    `seat_cols`   INT         NOT NULL COMMENT '每排座位数',
    `status`      TINYINT     NOT NULL DEFAULT 1 COMMENT '状态（0-停用 1-启用）',
    `create_time` DATETIME    NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time` DATETIME    NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    `deleted`     TINYINT     NOT NULL DEFAULT 0 COMMENT '逻辑删除（0-否 1-是）',
    PRIMARY KEY (`id`),
    KEY `idx_cinema` (`cinema_id`)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4
  COLLATE = utf8mb4_0900_ai_ci COMMENT = '影厅表';

-- 座位模板表（每座位一行；实时占用不落库，由 Redis Bitmap 运行时维护）
CREATE TABLE `t_seat_config`
(
    `id`          BIGINT      NOT NULL AUTO_INCREMENT COMMENT '座位ID（主键）',
    `hall_id`     BIGINT      NOT NULL COMMENT '所属影厅ID',
    `seat_row`    INT         NOT NULL COMMENT '排（从1起）',
    `seat_col`    INT         NOT NULL COMMENT '座（从1起）',
    `seat_no`     VARCHAR(10) NOT NULL COMMENT '展示座位号（如"3排5座"）',
    `status`      TINYINT     NOT NULL DEFAULT 0 COMMENT '状态（0-可售 1-不可售）',
    `create_time` DATETIME    NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time` DATETIME    NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    `deleted`     TINYINT     NOT NULL DEFAULT 0 COMMENT '逻辑删除（0-否 1-是）',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_hall_row_col` (`hall_id`, `seat_row`, `seat_col`),
    KEY `idx_hall` (`hall_id`)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4
  COLLATE = utf8mb4_0900_ai_ci COMMENT = '座位模板表';

-- 排场表
CREATE TABLE `t_screening`
(
    `id`          BIGINT         NOT NULL AUTO_INCREMENT COMMENT '排场ID（主键）',
    `movie_id`    BIGINT         NOT NULL COMMENT '影片ID',
    `hall_id`     BIGINT         NOT NULL COMMENT '影厅ID',
    `cinema_id`   BIGINT         NOT NULL COMMENT '影院ID（冗余，隔离与查询便捷）',
    `start_time`  DATETIME       NOT NULL COMMENT '开场时间',
    `price`       DECIMAL(10, 2) NOT NULL COMMENT '票价（该场次统一价）',
    `status`      TINYINT        NOT NULL DEFAULT 0 COMMENT '状态（0-未开始 1-已开场）',
    `create_time` DATETIME       NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time` DATETIME       NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    `deleted`     TINYINT        NOT NULL DEFAULT 0 COMMENT '逻辑删除（0-否 1-是）',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_hall_start` (`hall_id`, `start_time`),
    KEY `idx_movie_time` (`movie_id`, `start_time`),
    KEY `idx_cinema_time` (`cinema_id`, `start_time`),
    KEY `idx_hall_time` (`hall_id`, `start_time`)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4
  COLLATE = utf8mb4_0900_ai_ci COMMENT = '排场表';

-- 订单表
CREATE TABLE `t_order`
(
    `id`              BIGINT         NOT NULL AUTO_INCREMENT COMMENT '订单ID（主键）',
    `order_no`        VARCHAR(32)    NOT NULL COMMENT '订单号（取票码复用此号）',
    `user_id`         BIGINT         NOT NULL COMMENT '下单用户ID',
    `screening_id`    BIGINT         NOT NULL COMMENT '排场ID',
    `movie_id`        BIGINT         NOT NULL COMMENT '影片ID（冗余快照）',
    `movie_title`     VARCHAR(100)   NOT NULL COMMENT '影片名快照',
    `cinema_id`       BIGINT         NOT NULL COMMENT '影院ID',
    `status`          TINYINT        NOT NULL DEFAULT 0 COMMENT '状态（0-待支付 1-已支付 2-已取消）',
    `total_amount`    DECIMAL(10, 2) NOT NULL COMMENT '总金额',
    `version`         INT            NOT NULL DEFAULT 0 COMMENT '乐观锁版本号',
    `pay_expire_time` DATETIME       NOT NULL COMMENT '支付截止时间（创建+15分钟）',
    `pay_time`        DATETIME       NULL COMMENT '支付时间（冗余）',
    `cancel_type`     TINYINT        NULL COMMENT '取消类型（1-手动 2-超时）',
    `create_time`     DATETIME       NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time`     DATETIME       NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    `deleted`         TINYINT        NOT NULL DEFAULT 0 COMMENT '逻辑删除（0-否 1-是）',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_order_no` (`order_no`),
    KEY `idx_user_status` (`user_id`, `status`),
    KEY `idx_cinema_status` (`cinema_id`, `status`),
    KEY `idx_screening` (`screening_id`),
    KEY `idx_pay_expire` (`pay_expire_time`)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4
  COLLATE = utf8mb4_0900_ai_ci COMMENT = '订单表';

-- 订单座位明细表（座位/票价快照）
CREATE TABLE `t_order_item`
(
    `id`          BIGINT         NOT NULL AUTO_INCREMENT COMMENT '明细ID（主键）',
    `order_id`    BIGINT         NOT NULL COMMENT '订单ID',
    `seat_row`    INT            NOT NULL COMMENT '排快照',
    `seat_col`    INT            NOT NULL COMMENT '座快照',
    `seat_no`     VARCHAR(10)    NOT NULL COMMENT '座位号快照',
    `price`       DECIMAL(10, 2) NOT NULL COMMENT '该座票价快照',
    `create_time` DATETIME       NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time` DATETIME       NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    `deleted`     TINYINT        NOT NULL DEFAULT 0 COMMENT '逻辑删除（0-否 1-是）',
    PRIMARY KEY (`id`),
    KEY `idx_order` (`order_id`)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4
  COLLATE = utf8mb4_0900_ai_ci COMMENT = '订单座位明细表';

-- 支付记录表
CREATE TABLE `t_payment_record`
(
    `id`          BIGINT         NOT NULL AUTO_INCREMENT COMMENT '支付记录ID（主键）',
    `payment_no`  VARCHAR(32)    NOT NULL COMMENT '支付流水号',
    `order_id`    BIGINT         NOT NULL COMMENT '订单ID',
    `order_no`    VARCHAR(32)    NOT NULL COMMENT '订单号',
    `amount`      DECIMAL(10, 2) NOT NULL COMMENT '支付金额',
    `status`      TINYINT        NOT NULL DEFAULT 0 COMMENT '状态（0-待支付 1-成功 2-退款）',
    `pay_time`    DATETIME       NULL COMMENT '支付成功时间',
    `create_time` DATETIME       NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time` DATETIME       NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    `deleted`     TINYINT        NOT NULL DEFAULT 0 COMMENT '逻辑删除（0-否 1-是）',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_payment_no` (`payment_no`),
    KEY `idx_order_no` (`order_no`),
    KEY `idx_status` (`status`)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4
  COLLATE = utf8mb4_0900_ai_ci COMMENT = '支付记录表';

-- 本地消息表（Kafka 可靠投递）
CREATE TABLE `t_local_message`
(
    `id`          BIGINT      NOT NULL AUTO_INCREMENT COMMENT '消息ID（主键）',
    `msg_type`    VARCHAR(32) NOT NULL COMMENT '消息类型（sms/boxoffice/refund）',
    `biz_id`      VARCHAR(64) NOT NULL COMMENT '业务ID（订单号等）',
    `payload`     JSON        NOT NULL COMMENT '消息内容',
    `status`      TINYINT     NOT NULL DEFAULT 0 COMMENT '状态（0-待发送 1-已发送）',
    `retry_count` INT         NOT NULL DEFAULT 0 COMMENT '重试次数',
    `create_time` DATETIME    NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time` DATETIME    NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    `deleted`     TINYINT     NOT NULL DEFAULT 0 COMMENT '逻辑删除（0-否 1-是）',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_type_biz` (`msg_type`, `biz_id`),
    KEY `idx_status` (`status`)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4
  COLLATE = utf8mb4_0900_ai_ci COMMENT = '本地消息表';