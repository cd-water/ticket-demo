-- 管理员表
CREATE TABLE `admin`
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
  COLLATE = utf8mb4_0900_ai_ci
    COMMENT = '管理员表';

-- 影院表
CREATE TABLE `cinema`
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
  COLLATE = utf8mb4_0900_ai_ci
    COMMENT = '影院表';
