-- 用户表（所有用户都在这里，包括游客用户和注册用户）
CREATE TABLE `user`
(
    `id`           BIGINT UNSIGNED                   NOT NULL AUTO_INCREMENT COMMENT '主键ID（数据库内部）',
    `user_id`      BIGINT UNSIGNED                   NOT NULL UNIQUE COMMENT '业务用户ID（雪花算法生成）',
    `username`     VARCHAR(50)                                DEFAULT NULL COMMENT '用户名（可空，游客可以没有）',
    `phone`        VARCHAR(20)                                DEFAULT NULL COMMENT '手机号（游客为空，注册用户才有）',
    `password`     VARCHAR(255)                               DEFAULT NULL COMMENT '密码（游客为空，注册用户才有，BCrypt加密）',
    `role`         ENUM ('VISITOR', 'USER', 'ADMIN') NOT NULL DEFAULT 'VISITOR' COMMENT '用户角色',
    `status`       TINYINT                           NOT NULL DEFAULT 1 COMMENT '状态：1=正常，0=禁用',
    `created_time` TIMESTAMP                         NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `updated_time` TIMESTAMP                         NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    `visitor_id`   VARCHAR(36) UNIQUE                         DEFAULT NULL COMMENT '游客唯一ID',
    `is_delete`    TINYINT                           NOT NULL DEFAULT 0 COMMENT '删除标志位',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_phone` (`phone`) COMMENT '手机号唯一约束',
    UNIQUE KEY `uk_username` (`username`) COMMENT '用户名唯一约束'
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4 COMMENT ='用户表';


-- 游客用户表（游客访问时自动生成游客id）
CREATE TABLE `visitor_user`
(
    `id`          BIGINT UNSIGNED NOT NULL AUTO_INCREMENT PRIMARY KEY COMMENT '主键ID',
    `visitor_id`  VARCHAR(36)     NOT NULL UNIQUE COMMENT '游客唯一ID，对应 Cookie UUID',
    `user_id`     BIGINT UNSIGNED          DEFAULT NULL COMMENT '关联的注册用户ID，游客升级后填入',
    `create_time` TIMESTAMP       NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '首次创建时间',
    `last_seen`   TIMESTAMP       NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '最后访问时间'
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4 COMMENT ='游客用户表';