-- 1. 管理员表
CREATE TABLE `t_admin`
(
    `id`          BIGINT       NOT NULL AUTO_INCREMENT COMMENT '管理员ID（主键）',
    `username`    VARCHAR(32)  NOT NULL COMMENT '用户名',
    `password`    VARCHAR(100) NOT NULL COMMENT '密码（BCrypt）',
    `status`      TINYINT      NOT NULL DEFAULT 1 COMMENT '状态（0-禁用 1-启用）',
    `create_time` DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time` DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_username` (`username`)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4
  COLLATE = utf8mb4_0900_ai_ci COMMENT = '管理员表';

-- 2. 用户表
CREATE TABLE `t_user`
(
    `id`          BIGINT       NOT NULL AUTO_INCREMENT COMMENT '用户ID（主键）',
    `phone`       VARCHAR(20)  NOT NULL COMMENT '手机号',
    `password`    VARCHAR(100) NULL COMMENT '密码（BCrypt）',
    `nickname`    VARCHAR(50)  NOT NULL COMMENT '昵称',
    `status`      TINYINT      NOT NULL DEFAULT 1 COMMENT '状态（0-禁用 1-启用）',
    `create_time` DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time` DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_phone` (`phone`)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4
  COLLATE = utf8mb4_0900_ai_ci COMMENT = '用户表';

-- 3. 电影表
CREATE TABLE `t_movie`
(
    `id`           BIGINT        NOT NULL AUTO_INCREMENT COMMENT '电影ID（主键）',
    `title`        VARCHAR(100)  NOT NULL COMMENT '片名',
    `poster`       VARCHAR(255)  NOT NULL COMMENT '海报URL',
    `description`  VARCHAR(1024) NOT NULL COMMENT '简介',
    `duration`     INT           NOT NULL COMMENT '时长（分钟）',
    `release_date` DATE          NOT NULL COMMENT '上映日期',
    `status`       TINYINT       NOT NULL DEFAULT 1 COMMENT '状态（0-下架 1-上架；热映/待映由 release_date 与当前时间比对得出）',
    `create_time`  DATETIME      NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time`  DATETIME      NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (`id`),
    KEY `idx_status_release` (`status`, `release_date`)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4
  COLLATE = utf8mb4_0900_ai_ci COMMENT = '电影表';

-- 4. 轮播图表
CREATE TABLE `t_banner`
(
    `id`          BIGINT       NOT NULL AUTO_INCREMENT COMMENT '轮播图ID（主键）',
    `image`       VARCHAR(255) NOT NULL COMMENT '图片URL',
    `link_url`    VARCHAR(255) NOT NULL COMMENT '跳转链接',
    `sort`        INT          NOT NULL DEFAULT 0 COMMENT '排序（小在前）',
    `status`      TINYINT      NOT NULL DEFAULT 0 COMMENT '状态（0-禁用 1-启用）',
    `create_time` DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time` DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (`id`)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4
  COLLATE = utf8mb4_0900_ai_ci COMMENT = '轮播图表';

-- 5. 影院表
CREATE TABLE `t_cinema`
(
    `id`          BIGINT       NOT NULL AUTO_INCREMENT COMMENT '影院ID（主键）',
    `name`        VARCHAR(100) NOT NULL COMMENT '影院名称',
    `address`     VARCHAR(255) NOT NULL COMMENT '详细地址',
    `status`      TINYINT      NOT NULL DEFAULT 1 COMMENT '状态（0-停业 1-营业）',
    `create_time` DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time` DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (`id`)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4
  COLLATE = utf8mb4_0900_ai_ci COMMENT = '影院表';

-- 6. 影厅表
CREATE TABLE `t_hall`
(
    `id`          BIGINT      NOT NULL AUTO_INCREMENT COMMENT '影厅ID（主键）',
    `cinema_id`   BIGINT      NOT NULL COMMENT '关联影院ID',
    `name`        VARCHAR(50) NOT NULL COMMENT '影厅名称',
    `seat_rows`   INT         NOT NULL COMMENT '座位排数',
    `seat_cols`   INT         NOT NULL COMMENT '每排座位数',
    `status`      TINYINT     NOT NULL DEFAULT 1 COMMENT '状态（0-禁用 1-启用）',
    `create_time` DATETIME    NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time` DATETIME    NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    `deleted`     TINYINT     NOT NULL DEFAULT 0 COMMENT '逻辑删除（0-否 1-是）',
    PRIMARY KEY (`id`)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4
  COLLATE = utf8mb4_0900_ai_ci COMMENT = '影厅表';

-- 7. 座位模板表
CREATE TABLE `t_seat_config`
(
    `id`          BIGINT      NOT NULL AUTO_INCREMENT COMMENT '座位ID（主键）',
    `hall_id`     BIGINT      NOT NULL COMMENT '关联影厅ID',
    `seat_row`    INT         NOT NULL COMMENT '排（从1起）',
    `seat_col`    INT         NOT NULL COMMENT '座（从1起）',
    `seat_no`     VARCHAR(10) NOT NULL COMMENT '展示座位号（如"3排5座"）',
    `status`      TINYINT     NOT NULL DEFAULT 1 COMMENT '状态（0-禁用 1-启用）',
    `create_time` DATETIME    NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time` DATETIME    NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    `deleted`     TINYINT     NOT NULL DEFAULT 0 COMMENT '逻辑删除（0-否 1-是）',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_hall_row_col` (`hall_id`, `seat_row`, `seat_col`)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4
  COLLATE = utf8mb4_0900_ai_ci COMMENT = '座位模板表';

-- 8. 排场表
CREATE TABLE `t_screening`
(
    `id`          BIGINT         NOT NULL AUTO_INCREMENT COMMENT '排场ID（主键）',
    `movie_id`    BIGINT         NOT NULL COMMENT '关联电影ID',
    `hall_id`     BIGINT         NOT NULL COMMENT '关联影厅ID',
    `cinema_id`   BIGINT         NOT NULL COMMENT '关联影院ID（冗余）',
    `start_time`  DATETIME       NOT NULL COMMENT '开场时间',
    `price`       DECIMAL(10, 2) NOT NULL COMMENT '票价（元）',
    `create_time` DATETIME       NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time` DATETIME       NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    `deleted`     TINYINT        NOT NULL DEFAULT 0 COMMENT '逻辑删除（0-否 1-是）',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_hall_start` (`hall_id`, `start_time`),
    KEY `idx_movie_time` (`movie_id`, `start_time`),
    KEY `idx_cinema_time` (`cinema_id`, `start_time`)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4
  COLLATE = utf8mb4_0900_ai_ci COMMENT = '排场表';

-- 9. 订单表
CREATE TABLE `t_order`
(
    `id`              BIGINT         NOT NULL AUTO_INCREMENT COMMENT '订单ID（主键）',
    `order_no`        VARCHAR(32)    NOT NULL COMMENT '订单号（取票码复用此号）',
    `user_id`         BIGINT         NOT NULL COMMENT '关联用户ID',
    `screening_id`    BIGINT         NOT NULL COMMENT '关联排场ID',
    `movie_id`        BIGINT         NOT NULL COMMENT '关联电影ID（冗余）',
    `movie_title`     VARCHAR(100)   NOT NULL COMMENT '影片名快照',
    `cinema_id`       BIGINT         NOT NULL COMMENT '关联影院ID',
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

-- 10. 订单座位明细表
CREATE TABLE `t_order_item`
(
    `id`          BIGINT         NOT NULL AUTO_INCREMENT COMMENT '明细ID（主键）',
    `order_id`    BIGINT         NOT NULL COMMENT '关联订单ID',
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

-- 11. 支付记录表
CREATE TABLE `t_payment_record`
(
    `id`          BIGINT         NOT NULL AUTO_INCREMENT COMMENT '支付记录ID（主键）',
    `payment_no`  VARCHAR(32)    NOT NULL COMMENT '支付流水号',
    `order_id`    BIGINT         NOT NULL COMMENT '关联订单ID',
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

-- 12. 本地消息表
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
