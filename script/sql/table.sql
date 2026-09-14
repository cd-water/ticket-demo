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
    `cinema_id`   BIGINT         NOT NULL COMMENT '关联影院ID',
    `start_time`  DATETIME       NOT NULL COMMENT '开场时间',
    `price`       DECIMAL(10, 2) NOT NULL COMMENT '票价（元）',
    `create_time` DATETIME       NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time` DATETIME       NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
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
    `order_no`        BIGINT         NOT NULL COMMENT '订单号（雪花ID）',
    `user_id`         BIGINT         NOT NULL COMMENT '关联用户ID',
    `screening_id`    BIGINT         NOT NULL COMMENT '关联排场ID',
    `movie_id`        BIGINT         NOT NULL COMMENT '关联电影ID',
    `cinema_id`       BIGINT         NOT NULL COMMENT '关联影院ID',
    `status`          TINYINT        NOT NULL DEFAULT 0 COMMENT '状态（0-待支付 1-已支付 2-已取消）',
    `total_amount`    DECIMAL(10, 2) NOT NULL COMMENT '总金额（元）',
    `version`         INT            NOT NULL DEFAULT 0 COMMENT '乐观锁版本号',
    `pay_expire_time` DATETIME       NOT NULL COMMENT '支付截止时间',
    `pay_time`        DATETIME       NULL COMMENT '支付时间',
    `create_time`     DATETIME       NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time`     DATETIME       NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_order_no` (`order_no`),
    KEY `idx_user_status` (`user_id`, `status`),
    KEY `idx_cinema_status` (`cinema_id`, `status`),
    KEY `idx_status_expire` (`status`, `pay_expire_time`)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4
  COLLATE = utf8mb4_0900_ai_ci COMMENT = '订单表';

-- 10. 订单明细表
CREATE TABLE `t_order_item`
(
    `id`           BIGINT         NOT NULL AUTO_INCREMENT COMMENT '明细ID（主键）',
    `order_id`     BIGINT         NOT NULL COMMENT '关联订单ID',
    `screening_id` BIGINT         NOT NULL COMMENT '关联场次ID',
    `seat_row`     INT            NOT NULL COMMENT '排（从1起）',
    `seat_col`     INT            NOT NULL COMMENT '座（从1起）',
    `seat_no`      VARCHAR(10)    NOT NULL COMMENT '展示座位号（如"3排5座"）',
    `price`        DECIMAL(10, 2) NOT NULL COMMENT '票价（元）',
    `create_time`  DATETIME(3)    NOT NULL DEFAULT CURRENT_TIMESTAMP(3) COMMENT '创建时间',
    PRIMARY KEY (`id`),
    KEY `idx_order` (`order_id`)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4
  COLLATE = utf8mb4_0900_ai_ci COMMENT = '订单明细表';

-- 11. 支付流水表
CREATE TABLE `t_payment`
(
    `id`           BIGINT         NOT NULL AUTO_INCREMENT COMMENT '支付流水ID（主键）',
    `payment_no`   BIGINT         NOT NULL COMMENT '支付流水号（雪花ID）',
    `order_id`     BIGINT         NOT NULL COMMENT '关联订单ID',
    `channel`      TINYINT        NOT NULL COMMENT '支付渠道（1-支付宝 2-微信）',
    `amount`       DECIMAL(10, 2) NOT NULL COMMENT '支付金额（元）',
    `status`       TINYINT        NOT NULL DEFAULT 0 COMMENT '状态（0-待支付 1-成功 2-失败 3-已关闭）',
    `out_trade_no` VARCHAR(64)    NOT NULL COMMENT '商户订单号（给第三方，与 channel 联合唯一）',
    `trade_no`     VARCHAR(64)    NULL COMMENT '第三方交易号（alipay trade_no / wxpay transaction_id）',
    `paid_time`    DATETIME       NULL COMMENT '支付成功时间',
    `notify_raw`   VARCHAR(2048)  NULL COMMENT '第三方回调原始报文',
    `create_time`  DATETIME       NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time`  DATETIME       NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_payment_no` (`payment_no`),
    UNIQUE KEY `uk_channel_out_trade_no` (`channel`, `out_trade_no`),
    KEY `idx_order` (`order_id`)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4
  COLLATE = utf8mb4_0900_ai_ci COMMENT = '支付流水表';

-- 12. 退款流水表
CREATE TABLE `t_refund`
(
    `id`            BIGINT         NOT NULL AUTO_INCREMENT COMMENT '退款流水ID（主键）',
    `refund_no`     BIGINT         NOT NULL COMMENT '退款流水号（雪花ID）',
    `payment_id`    BIGINT         NOT NULL COMMENT '关联支付流水ID',
    `order_id`      BIGINT         NOT NULL COMMENT '关联订单ID',
    `channel`       TINYINT        NOT NULL COMMENT '原支付渠道（1-支付宝 2-微信）',
    `refund_amount` DECIMAL(10, 2) NOT NULL COMMENT '退款金额（元）',
    `status`        TINYINT        NOT NULL DEFAULT 0 COMMENT '状态（0-待退款 1-退款中 2-成功 3-失败）',
    `out_refund_no` VARCHAR(64)    NOT NULL COMMENT '商户退款单号（给第三方，与 channel 联合唯一）',
    `trade_no`      VARCHAR(64)    NULL COMMENT '第三方退款单号（alipay refund_id / wxpay refund_id）',
    `reason`        VARCHAR(255)   NULL COMMENT '退款原因',
    `refund_time`   DATETIME       NULL COMMENT '退款成功时间',
    `notify_raw`    VARCHAR(2048)  NULL COMMENT '第三方回调原始报文',
    `create_time`   DATETIME       NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time`   DATETIME       NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_refund_no` (`refund_no`),
    UNIQUE KEY `uk_channel_out_refund_no` (`channel`, `out_refund_no`),
    KEY `idx_payment` (`payment_id`),
    KEY `idx_order` (`order_id`)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4
  COLLATE = utf8mb4_0900_ai_ci COMMENT = '退款流水表';

-- 13. 本地消息表
CREATE TABLE `t_local_message`
(
    `id`              BIGINT        NOT NULL AUTO_INCREMENT COMMENT '消息ID（主键）',
    `msg_type`        VARCHAR(32)   NOT NULL COMMENT '消息类型',
    `biz_id`          BIGINT        NOT NULL COMMENT '业务ID',
    `payload`         VARCHAR(1024) NOT NULL COMMENT '消息内容（JSON）',
    `status`          TINYINT       NOT NULL DEFAULT 0 COMMENT '状态：0-待发送 1-已发送 2-终态失败',
    `retry_count`     INT           NOT NULL DEFAULT 0 COMMENT '已重试次数',
    `next_retry_time` DATETIME      NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '下次可重试时间',
    `create_time`     DATETIME      NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time`     DATETIME      NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_type_biz` (`msg_type`, `biz_id`),
    KEY `idx_dispatch` (`status`, `next_retry_time`)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4
  COLLATE = utf8mb4_0900_ai_ci COMMENT = '本地消息表';
