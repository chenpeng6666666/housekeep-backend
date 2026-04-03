-- ----------------------------
-- 1. 账号与权限模块
-- ----------------------------

-- 平台管理员表
CREATE TABLE `sys_admin`
(
    `id`          BIGINT       NOT NULL COMMENT '主键ID',
    `username`    VARCHAR(50)  NOT NULL COMMENT '登录账号',
    `password`    VARCHAR(100) NOT NULL COMMENT '密码哈希值',
    `role_type`   TINYINT      NOT NULL COMMENT '角色类型 (1: 超级管理员, 2: 运营等)',
    `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    `is_deleted`  TINYINT  DEFAULT 0 COMMENT '逻辑删除',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_username` (`username`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='平台管理员表';

-- 普通用户表
CREATE TABLE `user`
(
    `id`              BIGINT      NOT NULL COMMENT '主键ID',
    `phone`           VARCHAR(20) NOT NULL COMMENT '手机号',
    `password`        VARCHAR(100)   DEFAULT NULL COMMENT '密码',
    `nickname`        VARCHAR(50)    DEFAULT NULL COMMENT '用户昵称',
    `default_address` VARCHAR(255)   DEFAULT NULL COMMENT '默认服务地址',
    `default_lat`     DECIMAL(10, 6) DEFAULT NULL COMMENT '默认纬度',
    `default_lng`     DECIMAL(10, 6) DEFAULT NULL COMMENT '默认经度',
    `status`          TINYINT        DEFAULT 1 COMMENT '状态 (1: 正常, 0: 禁用)',
    `create_time`     DATETIME       DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time`     DATETIME       DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    `is_deleted`      TINYINT        DEFAULT 0 COMMENT '逻辑删除',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_phone` (`phone`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='普通用户表';

-- 家政企业信息表
CREATE TABLE `company`
(
    `id`           BIGINT       NOT NULL COMMENT '主键ID',
    `company_name` VARCHAR(100) NOT NULL COMMENT '企业名称',
    `license_no`   VARCHAR(50)  DEFAULT NULL COMMENT '营业执照注册号/统一社会信用代码',
    `license_pic`  VARCHAR(255) DEFAULT NULL COMMENT '营业执照图片地址/URL',
    `credit_score` INT          DEFAULT 100 COMMENT '企业信用分',
    `status`       TINYINT      DEFAULT 0 COMMENT '状态 (0: 待审核, 1: 营业中, 2: 审核驳回)',
    `create_time`  DATETIME     DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time`  DATETIME     DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    `is_deleted`   TINYINT      DEFAULT 0 COMMENT '逻辑删除',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_license_no` (`license_no`) -- 营业执照号全局唯一
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='家政企业实体表';

-- 企业员工表：承载管理员与普通员工角色
CREATE TABLE `company_employee`
(
    `id`          BIGINT      NOT NULL COMMENT '主键ID',
    `company_id`  BIGINT      NOT NULL COMMENT '所属企业ID',
    `real_name`   VARCHAR(50) NOT NULL COMMENT '员工姓名',
    `phone`       VARCHAR(20) NOT NULL COMMENT '联系电话/登录账号',
    `password`    VARCHAR(100)         DEFAULT NULL COMMENT '登录密码',
    `role_type`   TINYINT     NOT NULL DEFAULT 2 COMMENT '角色 (1:企业管理员, 2:家政服务员, 3:兼任两者)',
    `work_status` TINYINT              DEFAULT 1 COMMENT '工作状态 (1:空闲, 2:服务中, 3:请假)',
    `rating`      DECIMAL(3, 2)        DEFAULT 5.00 COMMENT '员工评分',
    `create_time` DATETIME             DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time` DATETIME             DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    `is_deleted`  TINYINT              DEFAULT 0 COMMENT '逻辑删除',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_phone` (`phone`),
    KEY           `idx_company_id` (`company_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='企业员工与账号表';


-- ----------------------------
-- 2. 服务体系与资质关联模块
-- ----------------------------

-- 服务分类表 (平台字典)
CREATE TABLE `service_category`
(
    `id`            BIGINT      NOT NULL COMMENT '主键ID',
    `parent_id`     BIGINT   DEFAULT 0 COMMENT '父级ID',
    `category_name` VARCHAR(50) NOT NULL COMMENT '分类名称',
    `sort_order`    INT      DEFAULT 0 COMMENT '排序权重',
    `status`        TINYINT  DEFAULT 1 COMMENT '状态',
    `create_time`   DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time`   DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    `is_deleted`    TINYINT  DEFAULT 0 COMMENT '逻辑删除',
    PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='服务分类表';

-- 企业服务条目表
CREATE TABLE `service_item`
(
    `id`          BIGINT         NOT NULL COMMENT '主键ID',
    `company_id`  BIGINT         NOT NULL COMMENT '归属企业ID',
    `category_id` BIGINT         NOT NULL COMMENT '关联分类ID',
    `item_name`   VARCHAR(100)   NOT NULL COMMENT '服务名称',
    `price`       DECIMAL(10, 2) NOT NULL COMMENT '单价',
    `unit`        VARCHAR(20) DEFAULT NULL COMMENT '单位',
    `duration`    INT         DEFAULT NULL COMMENT '预计耗时(分钟)',
    `create_time` DATETIME    DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time` DATETIME    DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    `is_deleted`  TINYINT     DEFAULT 0 COMMENT '逻辑删除',
    PRIMARY KEY (`id`),
    KEY           `idx_company_category` (`company_id`, `category_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='企业服务条目表';

-- 员工与服务关联表 (资质匹配)
CREATE TABLE `employee_service_mapping`
(
    `id`              BIGINT NOT NULL COMMENT '主键ID',
    `employee_id`     BIGINT NOT NULL COMMENT '员工ID',
    `service_item_id` BIGINT NOT NULL COMMENT '服务项ID',
    `create_time`     DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_emp_service` (`employee_id`, `service_item_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='员工接单资质表';


-- ----------------------------
-- 3. 交易与调度模块
-- ----------------------------

-- 订单主表
CREATE TABLE `order`
(
    `id`                BIGINT         NOT NULL COMMENT '主键ID',
    `order_no`          VARCHAR(50)    NOT NULL COMMENT '订单号',
    `user_id`           BIGINT         NOT NULL COMMENT '用户ID',
    `company_id`        BIGINT   DEFAULT NULL COMMENT '企业ID',
    `employee_id`       BIGINT   DEFAULT NULL COMMENT '员工ID',
    `service_item_id`   BIGINT         NOT NULL COMMENT '服务项ID',
    `order_type`        TINYINT        NOT NULL COMMENT '类型 (1:自主, 2:智慧匹配)',
    `service_address`   VARCHAR(255)   NOT NULL COMMENT '地址',
    `target_lat`        DECIMAL(10, 6) NOT NULL COMMENT '经度',
    `target_lng`        DECIMAL(10, 6) NOT NULL COMMENT '纬度',
    `expect_start_time` DATETIME       NOT NULL COMMENT '预约开始时间',
    `expect_end_time`   DATETIME       NOT NULL COMMENT '预约结束时间',
    `total_amount`      DECIMAL(10, 2) NOT NULL COMMENT '总额',
    `order_status`      TINYINT        NOT NULL COMMENT '状态',
    `create_time`       DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time`       DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    `is_deleted`        TINYINT  DEFAULT 0 COMMENT '逻辑删除',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_order_no` (`order_no`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='订单主表';