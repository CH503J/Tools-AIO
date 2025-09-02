-- 用户表
CREATE TABLE `user`
(
    `id`          BIGINT                            NOT NULL COMMENT '主键ID，雪花算法生成',
    `user_id`     BIGINT                                     DEFAULT NULL COMMENT '正式用户ID（用户自定义，可为空，类比QQ号）',
    `username`    VARCHAR(50)                                DEFAULT NULL COMMENT '用户昵称',
    `phone`       VARCHAR(20)                                DEFAULT NULL COMMENT '手机号',
    `password`    VARCHAR(255)                               DEFAULT NULL COMMENT '密码（加密存储，可为空）',
    `role`        ENUM ('VISITOR', 'USER', 'ADMIN') NOT NULL DEFAULT 'VISITOR' COMMENT '用户角色类型',
    `status`      TINYINT                           NOT NULL DEFAULT 1 COMMENT '账号状态：1正常，0禁用，2冻结等',
    `create_time` DATETIME                          NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time` DATETIME                          NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    `visitor_id`  VARCHAR(64)                       NOT NULL COMMENT '游客账号ID（不可为空，用于游客唯一标识）',
    `last_seen`   DATETIME                                   DEFAULT NULL COMMENT '上次登录时间',
    `last_ip`     VARCHAR(45)                                DEFAULT NULL COMMENT '上次登录IP（IPv4/IPv6均可）',
    `is_delete`   TINYINT                           NOT NULL DEFAULT 0 COMMENT '删除标志位：0未删除，1已删除',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_user_id` (`user_id`),
    UNIQUE KEY `uk_visitor_id` (`visitor_id`),
    KEY `idx_phone` (`phone`)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4 COMMENT ='用户表';
