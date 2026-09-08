-- ======================================================================================
-- CRM 模块 - 完整表结构 DDL（逆向自 DO 实体类）
-- 数据库类型: MySQL 5.7+ / 8.0
-- 所有表均继承 BaseDO 通用字段: create_time, update_time, creator, updater, deleted
-- ======================================================================================

-- --------------------------------------------------------------------
-- 1. 产品分类表
-- --------------------------------------------------------------------
DROP TABLE IF EXISTS `crm_product_category`;
CREATE TABLE `crm_product_category` (
    `id`            bigint      NOT NULL AUTO_INCREMENT  COMMENT '分类编号（主键自增）',
    `name`          varchar(100) NOT NULL                COMMENT '分类名称',
    `parent_id`     bigint      NOT NULL DEFAULT 0       COMMENT '父级编号，0 表示根分类（关联自身 id）',
    `tenant_id`   bigint       NOT NULL DEFAULT 1      COMMENT '租户编号',
    -- BaseDO 通用字段
    `create_time`   datetime    NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time`   datetime    NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '最后更新时间',
    `creator`       varchar(64)  DEFAULT NULL            COMMENT '创建者（AdminUserDO 的 id）',
    `updater`       varchar(64)  DEFAULT NULL            COMMENT '更新者（AdminUserDO 的 id）',
    `deleted`       bit(1)      NOT NULL DEFAULT 0       COMMENT '逻辑删除（0-未删除 1-已删除）',
    PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='CRM 产品分类表';

-- --------------------------------------------------------------------
-- 2. 产品表
-- --------------------------------------------------------------------
DROP TABLE IF EXISTS `crm_product`;
CREATE TABLE `crm_product` (
    `id`            bigint       NOT NULL AUTO_INCREMENT  COMMENT '产品编号（主键自增）',
    `name`          varchar(100) NOT NULL                 COMMENT '产品名称',
    `no`            varchar(100) NOT NULL                 COMMENT '产品编码',
    `unit`          int          DEFAULT NULL             COMMENT '单位（字典 CRM_PRODUCT_UNIT）',
    `price`         decimal(24,6) DEFAULT NULL            COMMENT '价格，单位：元',
    `status`        int          NOT NULL DEFAULT 0       COMMENT '状态（CrmProductStatusEnum: 0-下架 1-上架）',
    `category_id`   bigint       NOT NULL                 COMMENT '产品分类 ID（关联 crm_product_category.id）',
    `description`   varchar(500) DEFAULT NULL             COMMENT '产品描述',
    `owner_user_id` bigint       DEFAULT NULL             COMMENT '负责人用户编号（关联 AdminUserDO.id）',
    `tenant_id`   bigint       NOT NULL DEFAULT 1      COMMENT '租户编号',
    -- BaseDO 通用字段
    `create_time`   datetime    NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time`   datetime    NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '最后更新时间',
    `creator`       varchar(64)  DEFAULT NULL             COMMENT '创建者',
    `updater`       varchar(64)  DEFAULT NULL             COMMENT '更新者',
    `deleted`       bit(1)      NOT NULL DEFAULT 0        COMMENT '逻辑删除',
    PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='CRM 产品表';

-- --------------------------------------------------------------------
-- 3. 客户表
-- --------------------------------------------------------------------
DROP TABLE IF EXISTS `crm_customer`;
CREATE TABLE `crm_customer` (
    `id`                 bigint       NOT NULL AUTO_INCREMENT COMMENT '客户编号（主键自增）',
    `name`               varchar(200) NOT NULL                COMMENT '客户名称',
    `follow_up_status`   bit(1)       NOT NULL DEFAULT 0      COMMENT '跟进状态（0-未跟进 1-已跟进）',
    `contact_last_time`  datetime     DEFAULT NULL            COMMENT '最后跟进时间',
    `contact_last_content` varchar(1000) DEFAULT NULL         COMMENT '最后跟进内容',
    `contact_next_time`  datetime     DEFAULT NULL            COMMENT '下次联系时间',
    `owner_user_id`      bigint       DEFAULT NULL            COMMENT '负责人用户编号（关联 AdminUserDO.id）',
    `owner_time`         datetime     DEFAULT NULL            COMMENT '成为负责人的时间',
    `lock_status`        bit(1)       NOT NULL DEFAULT 0      COMMENT '锁定状态（0-未锁定 1-已锁定）',
    `deal_status`        bit(1)       NOT NULL DEFAULT 0      COMMENT '成交状态（0-未成交 1-已成交）',
    `mobile`             varchar(20)  DEFAULT NULL            COMMENT '手机号',
    `telephone`          varchar(20)  DEFAULT NULL            COMMENT '电话',
    `qq`                 varchar(20)  DEFAULT NULL            COMMENT 'QQ',
    `wechat`             varchar(50)  DEFAULT NULL            COMMENT '微信',
    `email`              varchar(100) DEFAULT NULL            COMMENT '邮箱',
    `area_id`            int          DEFAULT NULL            COMMENT '所在地（关联 Area.id）',
    `detail_address`     varchar(255) DEFAULT NULL            COMMENT '详细地址',
    `industry_id`        int          DEFAULT NULL            COMMENT '所属行业（字典 CRM_CUSTOMER_INDUSTRY）',
    `level`              int          DEFAULT NULL            COMMENT '客户等级（字典 CRM_CUSTOMER_LEVEL）',
    `source`             int          DEFAULT NULL            COMMENT '客户来源（字典 CRM_CUSTOMER_SOURCE）',
    `remark`             varchar(500) DEFAULT NULL            COMMENT '备注',
    `tenant_id`   bigint       NOT NULL DEFAULT 1      COMMENT '租户编号',
    -- BaseDO 通用字段
    `create_time`        datetime     NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time`        datetime     NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '最后更新时间',
    `creator`            varchar(64)  DEFAULT NULL            COMMENT '创建者',
    `updater`            varchar(64)  DEFAULT NULL            COMMENT '更新者',
    `deleted`            bit(1)       NOT NULL DEFAULT 0      COMMENT '逻辑删除',
    PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='CRM 客户表';

-- --------------------------------------------------------------------
-- 4. 线索表
-- --------------------------------------------------------------------
DROP TABLE IF EXISTS `crm_clue`;
CREATE TABLE `crm_clue` (
    `id`                 bigint       NOT NULL AUTO_INCREMENT COMMENT '线索编号（主键自增）',
    `name`               varchar(200) NOT NULL                COMMENT '线索名称',
    `follow_up_status`   bit(1)       NOT NULL DEFAULT 0      COMMENT '跟进状态（0-未跟进 1-已跟进）',
    `contact_last_time`  datetime     DEFAULT NULL            COMMENT '最后跟进时间',
    `contact_last_content` varchar(1000) DEFAULT NULL         COMMENT '最后跟进内容',
    `contact_next_time`  datetime     DEFAULT NULL            COMMENT '下次联系时间',
    `owner_user_id`      bigint       DEFAULT NULL            COMMENT '负责人用户编号（关联 AdminUserDO.id）',
    `transform_status`   bit(1)       NOT NULL DEFAULT 0      COMMENT '转化状态（0-未转化 1-已转化）',
    `customer_id`        bigint       DEFAULT NULL            COMMENT '转化后的客户编号（关联 crm_customer.id）',
    `mobile`             varchar(20)  DEFAULT NULL            COMMENT '手机号',
    `telephone`          varchar(20)  DEFAULT NULL            COMMENT '电话',
    `qq`                 varchar(20)  DEFAULT NULL            COMMENT 'QQ',
    `wechat`             varchar(50)  DEFAULT NULL            COMMENT '微信',
    `email`              varchar(100) DEFAULT NULL            COMMENT '邮箱',
    `area_id`            int          DEFAULT NULL            COMMENT '所在地（关联 Area.id）',
    `detail_address`     varchar(255) DEFAULT NULL            COMMENT '详细地址',
    `industry_id`        int          DEFAULT NULL            COMMENT '所属行业（字典 CRM_CUSTOMER_INDUSTRY）',
    `level`              int          DEFAULT NULL            COMMENT '客户等级（字典 CRM_CUSTOMER_LEVEL）',
    `source`             int          DEFAULT NULL            COMMENT '客户来源（字典 CRM_CUSTOMER_SOURCE）',
    `remark`             varchar(500) DEFAULT NULL            COMMENT '备注',
    `tenant_id`   bigint       NOT NULL DEFAULT 1      COMMENT '租户编号',
    -- BaseDO 通用字段
    `create_time`        datetime     NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time`        datetime     NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '最后更新时间',
    `creator`            varchar(64)  DEFAULT NULL            COMMENT '创建者',
    `updater`            varchar(64)  DEFAULT NULL            COMMENT '更新者',
    `deleted`            bit(1)       NOT NULL DEFAULT 0      COMMENT '逻辑删除',
    PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='CRM 线索表';

-- --------------------------------------------------------------------
-- 5. 客户公海配置表
-- --------------------------------------------------------------------
DROP TABLE IF EXISTS `crm_customer_pool_config`;
CREATE TABLE `crm_customer_pool_config` (
    `id`                  bigint    NOT NULL AUTO_INCREMENT COMMENT '编号（主键自增）',
    `enabled`             bit(1)    NOT NULL DEFAULT 0      COMMENT '是否启用客户公海',
    `contact_expire_days` int       DEFAULT NULL            COMMENT '未跟进放入公海天数',
    `deal_expire_days`    int       DEFAULT NULL            COMMENT '未成交放入公海天数',
    `notify_enabled`      bit(1)    DEFAULT NULL            COMMENT '是否开启提前提醒',
    `notify_days`         int       DEFAULT NULL            COMMENT '提前提醒天数',
    `tenant_id`   bigint       NOT NULL DEFAULT 1      COMMENT '租户编号',
    -- BaseDO 通用字段
    `create_time`         datetime  NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time`         datetime  NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '最后更新时间',
    `creator`             varchar(64) DEFAULT NULL          COMMENT '创建者',
    `updater`             varchar(64) DEFAULT NULL          COMMENT '更新者',
    `deleted`             bit(1)    NOT NULL DEFAULT 0      COMMENT '逻辑删除',
    PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='CRM 客户公海配置表';

-- --------------------------------------------------------------------
-- 6. 客户限制配置表
-- --------------------------------------------------------------------
DROP TABLE IF EXISTS `crm_customer_limit_config`;
CREATE TABLE `crm_customer_limit_config` (
    `id`                bigint         NOT NULL AUTO_INCREMENT COMMENT '编号（主键自增）',
    `type`              int            NOT NULL                COMMENT '规则类型（CrmCustomerLimitConfigTypeEnum: 1-拥有客户上限 2-锁定客户上限）',
    `user_ids`          json           DEFAULT NULL            COMMENT '规则适用人群（JSON 数组，LongListTypeHandler 序列化）',
    `dept_ids`          json           DEFAULT NULL            COMMENT '规则适用部门（JSON 数组，LongListTypeHandler 序列化）',
    `max_count`         int            NOT NULL                COMMENT '数量上限',
    `deal_count_enabled` bit(1)        DEFAULT NULL            COMMENT '成交客户是否占有拥有客户数（仅 type=1 时生效）',
    `tenant_id`   bigint       NOT NULL DEFAULT 1      COMMENT '租户编号',
    -- BaseDO 通用字段
    `create_time`       datetime       NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time`       datetime       NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '最后更新时间',
    `creator`           varchar(64)    DEFAULT NULL            COMMENT '创建者',
    `updater`           varchar(64)    DEFAULT NULL            COMMENT '更新者',
    `deleted`           bit(1)         NOT NULL DEFAULT 0      COMMENT '逻辑删除',
    PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='CRM 客户限制配置表';

-- --------------------------------------------------------------------
-- 7. 联系人表
-- --------------------------------------------------------------------
DROP TABLE IF EXISTS `crm_contact`;
CREATE TABLE `crm_contact` (
    `id`                  bigint       NOT NULL AUTO_INCREMENT COMMENT '联系人编号（主键自增）',
    `name`                varchar(100) NOT NULL                COMMENT '联系人姓名',
    `customer_id`         bigint       NOT NULL                COMMENT '客户编号（关联 crm_customer.id）',
    `contact_last_time`   datetime     DEFAULT NULL            COMMENT '最后跟进时间',
    `contact_last_content` varchar(1000) DEFAULT NULL          COMMENT '最后跟进内容',
    `contact_next_time`   datetime     DEFAULT NULL            COMMENT '下次联系时间',
    `owner_user_id`       bigint       DEFAULT NULL            COMMENT '负责人用户编号（关联 AdminUserDO.id）',
    `mobile`              varchar(20)  DEFAULT NULL            COMMENT '手机号',
    `telephone`           varchar(20)  DEFAULT NULL            COMMENT '电话',
    `email`               varchar(100) DEFAULT NULL            COMMENT '邮箱',
    `qq`                  bigint       DEFAULT NULL            COMMENT 'QQ 号',
    `wechat`              varchar(50)  DEFAULT NULL            COMMENT '微信',
    `area_id`             int          DEFAULT NULL            COMMENT '所在地（关联 Area.id）',
    `detail_address`      varchar(255) DEFAULT NULL            COMMENT '详细地址',
    `sex`                 int          DEFAULT NULL            COMMENT '性别（SexEnum: 0-未知 1-男 2-女）',
    `master`              bit(1)       NOT NULL DEFAULT 0      COMMENT '是否关键决策人',
    `post`                varchar(50)  DEFAULT NULL            COMMENT '职位',
    `parent_id`           bigint       DEFAULT NULL            COMMENT '直属上级联系人（关联自身 id）',
    `remark`              varchar(500) DEFAULT NULL            COMMENT '备注',
    `tenant_id`   bigint       NOT NULL DEFAULT 1      COMMENT '租户编号',
    -- BaseDO 通用字段
    `create_time`         datetime     NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time`         datetime     NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '最后更新时间',
    `creator`             varchar(64)  DEFAULT NULL            COMMENT '创建者',
    `updater`             varchar(64)  DEFAULT NULL            COMMENT '更新者',
    `deleted`             bit(1)       NOT NULL DEFAULT 0      COMMENT '逻辑删除',
    PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='CRM 联系人表';

-- --------------------------------------------------------------------
-- 8. 联系人与商机关联表
-- --------------------------------------------------------------------
DROP TABLE IF EXISTS `crm_contact_business`;
CREATE TABLE `crm_contact_business` (
    `id`          bigint    NOT NULL AUTO_INCREMENT COMMENT '主键',
    `contact_id`  bigint    NOT NULL                   COMMENT '联系人编号（关联 crm_contact.id）',
    `business_id` bigint    NOT NULL                   COMMENT '商机编号（关联 crm_business.id）',
    `tenant_id`   bigint       NOT NULL DEFAULT 1      COMMENT '租户编号',
    -- BaseDO 通用字段
    `create_time` datetime  NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time` datetime  NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '最后更新时间',
    `creator`     varchar(64) DEFAULT NULL             COMMENT '创建者',
    `updater`     varchar(64) DEFAULT NULL             COMMENT '更新者',
    `deleted`     bit(1)    NOT NULL DEFAULT 0         COMMENT '逻辑删除',
    PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='CRM 联系人与商机关联表';

-- --------------------------------------------------------------------
-- 9. 商机状态类型表（配置表 - 状态组）
-- --------------------------------------------------------------------
DROP TABLE IF EXISTS `crm_business_status_type`;
CREATE TABLE `crm_business_status_type` (
    `id`          bigint         NOT NULL AUTO_INCREMENT COMMENT '主键',
    `name`        varchar(100)   NOT NULL                COMMENT '状态类型名称',
    `dept_ids`    varchar(255)   DEFAULT NULL            COMMENT '使用的部门编号（逗号分隔，LongListTypeHandler 序列化）',
    `tenant_id`   bigint       NOT NULL DEFAULT 1      COMMENT '租户编号',
    -- BaseDO 通用字段
    `create_time` datetime       NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time` datetime       NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '最后更新时间',
    `creator`     varchar(64)    DEFAULT NULL            COMMENT '创建者',
    `updater`     varchar(64)    DEFAULT NULL            COMMENT '更新者',
    `deleted`     bit(1)         NOT NULL DEFAULT 0      COMMENT '逻辑删除',
    PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='CRM 商机状态类型表（状态组配置）';

-- --------------------------------------------------------------------
-- 10. 商机状态表（配置表 - 具体状态）
-- --------------------------------------------------------------------
DROP TABLE IF EXISTS `crm_business_status`;
CREATE TABLE `crm_business_status` (
    `id`          bigint       NOT NULL AUTO_INCREMENT COMMENT '主键',
    `type_id`     bigint       NOT NULL                COMMENT '状态类型编号（关联 crm_business_status_type.id）',
    `name`        varchar(100) NOT NULL                COMMENT '状态名称（如：初步接触、需求分析、方案报价...）',
    `percent`     int          DEFAULT NULL            COMMENT '赢单率，百分比',
    `sort`        int          DEFAULT NULL            COMMENT '排序',
    `tenant_id`   bigint       NOT NULL DEFAULT 1      COMMENT '租户编号',
    -- BaseDO 通用字段
    `create_time` datetime     NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time` datetime     NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '最后更新时间',
    `creator`     varchar(64)  DEFAULT NULL            COMMENT '创建者',
    `updater`     varchar(64)  DEFAULT NULL            COMMENT '更新者',
    `deleted`     bit(1)       NOT NULL DEFAULT 0      COMMENT '逻辑删除',
    PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='CRM 商机状态表';

-- --------------------------------------------------------------------
-- 11. 商机表
-- --------------------------------------------------------------------
DROP TABLE IF EXISTS `crm_business`;
CREATE TABLE `crm_business` (
    `id`                  bigint         NOT NULL AUTO_INCREMENT COMMENT '商机编号（主键自增）',
    `name`                varchar(200)   NOT NULL                COMMENT '商机名称',
    `customer_id`         bigint         NOT NULL                COMMENT '客户编号（关联 crm_customer.id）',
    `follow_up_status`   bit(1)         NOT NULL DEFAULT 0      COMMENT '跟进状态',
    `contact_last_time`  datetime       DEFAULT NULL            COMMENT '最后跟进时间',
    `contact_next_time`  datetime       DEFAULT NULL            COMMENT '下次联系时间',
    `owner_user_id`       bigint         DEFAULT NULL            COMMENT '负责人用户编号（关联 AdminUserDO.id）',
    `status_type_id`      bigint         DEFAULT NULL            COMMENT '商机状态组编号（关联 crm_business_status_type.id）',
    `status_id`           bigint         DEFAULT NULL            COMMENT '商机状态编号（关联 crm_business_status.id）',
    `end_status`          int            DEFAULT NULL            COMMENT '结束状态（CrmBusinessEndStatusEnum: 1-赢单 2-输单 3-无效）',
    `end_remark`          varchar(500)   DEFAULT NULL            COMMENT '结束时的备注',
    `deal_time`           datetime       DEFAULT NULL            COMMENT '预计成交日期',
    `total_product_price` decimal(24,6)  DEFAULT NULL            COMMENT '产品总金额，单位：元（∑关联产品价格）',
    `discount_percent`    decimal(10,2)  DEFAULT NULL            COMMENT '整单折扣，百分比',
    `total_price`         decimal(24,6)  DEFAULT NULL            COMMENT '商机总金额，单位：元',
    `remark`              varchar(500)   DEFAULT NULL            COMMENT '备注',
    `tenant_id`   bigint       NOT NULL DEFAULT 1      COMMENT '租户编号',
    -- BaseDO 通用字段
    `create_time`         datetime       NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time`         datetime       NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '最后更新时间',
    `creator`             varchar(64)    DEFAULT NULL            COMMENT '创建者',
    `updater`             varchar(64)    DEFAULT NULL            COMMENT '更新者',
    `deleted`             bit(1)         NOT NULL DEFAULT 0      COMMENT '逻辑删除',
    PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='CRM 商机表';

-- --------------------------------------------------------------------
-- 12. 商机产品关联表
-- --------------------------------------------------------------------
DROP TABLE IF EXISTS `crm_business_product`;
CREATE TABLE `crm_business_product` (
    `id`             bigint         NOT NULL AUTO_INCREMENT COMMENT '主键',
    `business_id`    bigint         NOT NULL                COMMENT '商机编号（关联 crm_business.id）',
    `product_id`     bigint         NOT NULL                COMMENT '产品编号（关联 crm_product.id）',
    `product_price`  decimal(24,6)  NOT NULL                COMMENT '产品单价，单位：元（冗余自 crm_product.price）',
    `business_price` decimal(24,6)  NOT NULL                COMMENT '商机价格，单位：元',
    `count`          decimal(24,6)  DEFAULT NULL            COMMENT '数量',
    `total_price`    decimal(24,6)  DEFAULT NULL            COMMENT '总计价格，单位：元（total_price = business_price * count）',
    `tenant_id`   bigint       NOT NULL DEFAULT 1      COMMENT '租户编号',
    -- BaseDO 通用字段
    `create_time`    datetime       NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time`    datetime       NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '最后更新时间',
    `creator`        varchar(64)    DEFAULT NULL            COMMENT '创建者',
    `updater`        varchar(64)    DEFAULT NULL            COMMENT '更新者',
    `deleted`        bit(1)         NOT NULL DEFAULT 0      COMMENT '逻辑删除',
    PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='CRM 商机产品关联表';

-- --------------------------------------------------------------------
-- 13. 合同配置表
-- --------------------------------------------------------------------
DROP TABLE IF EXISTS `crm_contract_config`;
CREATE TABLE `crm_contract_config` (
    `id`             bigint    NOT NULL AUTO_INCREMENT COMMENT '编号（主键自增）',
    `notify_enabled` bit(1)    DEFAULT NULL            COMMENT '是否开启提前提醒',
    `notify_days`    int       DEFAULT NULL            COMMENT '提前提醒天数',
    `tenant_id`   bigint       NOT NULL DEFAULT 1      COMMENT '租户编号',
    -- BaseDO 通用字段
    `create_time`    datetime  NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time`    datetime  NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '最后更新时间',
    `creator`        varchar(64) DEFAULT NULL          COMMENT '创建者',
    `updater`        varchar(64) DEFAULT NULL          COMMENT '更新者',
    `deleted`        bit(1)    NOT NULL DEFAULT 0      COMMENT '逻辑删除',
    PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='CRM 合同配置表';

-- --------------------------------------------------------------------
-- 14. 合同表
-- --------------------------------------------------------------------
DROP TABLE IF EXISTS `crm_contract`;
CREATE TABLE `crm_contract` (
    `id`                  bigint         NOT NULL AUTO_INCREMENT COMMENT '合同编号（主键自增）',
    `name`                varchar(200)   NOT NULL                COMMENT '合同名称',
    `no`                  varchar(100)   NOT NULL                COMMENT '合同编号',
    `customer_id`         bigint         NOT NULL                COMMENT '客户编号（关联 crm_customer.id）',
    `business_id`         bigint         DEFAULT NULL            COMMENT '商机编号（关联 crm_business.id，非必须）',
    `contact_last_time`   datetime       DEFAULT NULL            COMMENT '最后跟进时间',
    `owner_user_id`       bigint         DEFAULT NULL            COMMENT '负责人用户编号（关联 AdminUserDO.id）',
    `process_instance_id` varchar(64)    DEFAULT NULL            COMMENT '工作流编号（关联 Flowable ProcessInstance.id）',
    `audit_status`        int            NOT NULL DEFAULT 0      COMMENT '审批状态（CrmAuditStatusEnum: 0-未提交 1-审批中 2-审批通过 3-审批不通过）',
    `order_date`          datetime       DEFAULT NULL            COMMENT '下单日期',
    `start_time`          datetime       DEFAULT NULL            COMMENT '合同开始时间',
    `end_time`            datetime       DEFAULT NULL            COMMENT '合同结束时间',
    `total_product_price` decimal(24,6)  DEFAULT NULL            COMMENT '产品总金额，单位：元',
    `discount_percent`    decimal(10,2)  DEFAULT NULL            COMMENT '整单折扣，百分比',
    `total_price`         decimal(24,6)  DEFAULT NULL            COMMENT '合同总金额，单位：元',
    `sign_contact_id`     bigint         DEFAULT NULL            COMMENT '客户签约人编号（关联 crm_contact.id，非必须）',
    `sign_user_id`        bigint         DEFAULT NULL            COMMENT '公司签约人编号（关联 AdminUserDO.id，非必须）',
    `remark`              varchar(500)   DEFAULT NULL            COMMENT '备注',
    `tenant_id`   bigint       NOT NULL DEFAULT 1      COMMENT '租户编号',
    -- BaseDO 通用字段
    `create_time`         datetime       NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time`         datetime       NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '最后更新时间',
    `creator`             varchar(64)    DEFAULT NULL            COMMENT '创建者',
    `updater`             varchar(64)    DEFAULT NULL            COMMENT '更新者',
    `deleted`             bit(1)         NOT NULL DEFAULT 0      COMMENT '逻辑删除',
    PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='CRM 合同表';

-- --------------------------------------------------------------------
-- 15. 合同产品关联表
-- --------------------------------------------------------------------
DROP TABLE IF EXISTS `crm_contract_product`;
CREATE TABLE `crm_contract_product` (
    `id`             bigint         NOT NULL AUTO_INCREMENT COMMENT '主键',
    `contract_id`    bigint         NOT NULL                COMMENT '合同编号（关联 crm_contract.id）',
    `product_id`     bigint         NOT NULL                COMMENT '产品编号（关联 crm_product.id）',
    `product_price`  decimal(24,6)  NOT NULL                COMMENT '产品单价，单位：元',
    `contract_price` decimal(24,6)  NOT NULL                COMMENT '合同价格，单位：元',
    `count`          decimal(24,6)  DEFAULT NULL            COMMENT '数量',
    `total_price`    decimal(24,6)  DEFAULT NULL            COMMENT '总计价格，单位：元',
    `tenant_id`   bigint       NOT NULL DEFAULT 1      COMMENT '租户编号',
    -- BaseDO 通用字段
    `create_time`    datetime       NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time`    datetime       NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '最后更新时间',
    `creator`        varchar(64)    DEFAULT NULL            COMMENT '创建者',
    `updater`        varchar(64)    DEFAULT NULL            COMMENT '更新者',
    `deleted`        bit(1)         NOT NULL DEFAULT 0      COMMENT '逻辑删除',
    PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='CRM 合同产品关联表';

-- --------------------------------------------------------------------
-- 16. 回款计划表
-- --------------------------------------------------------------------
DROP TABLE IF EXISTS `crm_receivable_plan`;
CREATE TABLE `crm_receivable_plan` (
    `id`            bigint         NOT NULL AUTO_INCREMENT COMMENT '编号（主键自增）',
    `period`        int            NOT NULL                COMMENT '期数',
    `customer_id`   bigint         NOT NULL                COMMENT '客户编号（关联 crm_customer.id）',
    `contract_id`   bigint         NOT NULL                COMMENT '合同编号（关联 crm_contract.id）',
    `owner_user_id` bigint         DEFAULT NULL            COMMENT '负责人编号（关联 AdminUserDO.id）',
    `return_time`   datetime       NOT NULL                COMMENT '计划回款日期',
    `return_type`   int            DEFAULT NULL            COMMENT '计划回款方式（CrmReceivableReturnTypeEnum）',
    `price`         decimal(24,6)  NOT NULL                COMMENT '计划回款金额，单位：元',
    `receivable_id` bigint         DEFAULT NULL            COMMENT '实际回款编号（关联 crm_receivable.id，回款后回填）',
    `remind_days`   int            DEFAULT NULL            COMMENT '提前几天提醒',
    `remind_time`   datetime       DEFAULT NULL            COMMENT '提醒日期',
    `remark`        varchar(500)   DEFAULT NULL            COMMENT '备注',
    `tenant_id`   bigint       NOT NULL DEFAULT 1      COMMENT '租户编号',
    -- BaseDO 通用字段
    `create_time`   datetime       NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time`   datetime       NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '最后更新时间',
    `creator`       varchar(64)    DEFAULT NULL            COMMENT '创建者',
    `updater`       varchar(64)    DEFAULT NULL            COMMENT '更新者',
    `deleted`       bit(1)         NOT NULL DEFAULT 0      COMMENT '逻辑删除',
    PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='CRM 回款计划表';

-- --------------------------------------------------------------------
-- 17. 回款表
-- --------------------------------------------------------------------
DROP TABLE IF EXISTS `crm_receivable`;
CREATE TABLE `crm_receivable` (
    `id`                 bigint         NOT NULL AUTO_INCREMENT COMMENT '编号（主键自增）',
    `no`                 varchar(100)   NOT NULL                COMMENT '回款编号',
    `plan_id`            bigint         DEFAULT NULL            COMMENT '回款计划编号（关联 crm_receivable_plan.id，非必须）',
    `customer_id`        bigint         NOT NULL                COMMENT '客户编号（关联 crm_customer.id）',
    `contract_id`        bigint         NOT NULL                COMMENT '合同编号（关联 crm_contract.id）',
    `owner_user_id`      bigint         DEFAULT NULL            COMMENT '负责人编号（关联 AdminUserDO.id）',
    `return_time`        datetime       NOT NULL                COMMENT '回款日期',
    `return_type`        int            DEFAULT NULL            COMMENT '回款方式（CrmReceivableReturnTypeEnum）',
    `price`              decimal(24,6)  NOT NULL                COMMENT '回款金额，单位：元',
    `remark`             varchar(500)   DEFAULT NULL            COMMENT '备注',
    `process_instance_id` varchar(64)   DEFAULT NULL            COMMENT '工作流编号（关联 Flowable ProcessInstance.id）',
    `audit_status`       int            NOT NULL DEFAULT 0      COMMENT '审批状态（CrmAuditStatusEnum）',
    `tenant_id`   bigint       NOT NULL DEFAULT 1      COMMENT '租户编号',
    -- BaseDO 通用字段
    `create_time`        datetime       NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time`        datetime       NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '最后更新时间',
    `creator`            varchar(64)    DEFAULT NULL            COMMENT '创建者',
    `updater`            varchar(64)    DEFAULT NULL            COMMENT '更新者',
    `deleted`            bit(1)         NOT NULL DEFAULT 0      COMMENT '逻辑删除',
    PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='CRM 回款表';

-- --------------------------------------------------------------------
-- 18. 数据权限表
-- --------------------------------------------------------------------
DROP TABLE IF EXISTS `crm_permission`;
CREATE TABLE `crm_permission` (
    `id`          bigint       NOT NULL AUTO_INCREMENT COMMENT '编号（主键自增）',
    `biz_type`    int          NOT NULL                COMMENT '数据类型（CrmBizTypeEnum: 1-客户 2-联系人 3-商机 4-合同 5-回款 6-线索...）',
    `biz_id`      bigint       NOT NULL                COMMENT '数据编号（关联对应业务表 id）',
    `user_id`     bigint       NOT NULL                COMMENT '用户编号（关联 AdminUserDO.id）',
    `level`       int          NOT NULL                COMMENT '权限级别（CrmPermissionLevelEnum: 1-只读 2-读写 3-负责人）',
    `tenant_id`   bigint       NOT NULL DEFAULT 1      COMMENT '租户编号',
    -- BaseDO 通用字段
    `create_time` datetime     NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time` datetime     NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '最后更新时间',
    `creator`     varchar(64)  DEFAULT NULL            COMMENT '创建者',
    `updater`     varchar(64)  DEFAULT NULL            COMMENT '更新者',
    `deleted`     bit(1)       NOT NULL DEFAULT 0      COMMENT '逻辑删除',
    PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='CRM 数据权限表';

-- --------------------------------------------------------------------
-- 19. 负责人变更记录表
-- --------------------------------------------------------------------
DROP TABLE IF EXISTS `crm_owner_record`;
CREATE TABLE `crm_owner_record` (
    `id`                bigint       NOT NULL AUTO_INCREMENT COMMENT '编号（主键自增）',
    `biz_type`          int          NOT NULL                COMMENT 'CRM 业务类型（CrmBizTypeEnum）',
    `biz_id`            bigint       NOT NULL                COMMENT 'CRM 业务编号（关联对应业务表 id）',
    `pre_owner_user_id` bigint       NOT NULL                COMMENT '变更前负责人（关联 AdminUserDO.id）',
    `post_owner_user_id` bigint      NOT NULL                COMMENT '变更后负责人（关联 AdminUserDO.id）',
    `tenant_id`   bigint       NOT NULL DEFAULT 1      COMMENT '租户编号',
    -- BaseDO 通用字段
    `create_time`       datetime     NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time`       datetime     NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '最后更新时间',
    `creator`           varchar(64)  DEFAULT NULL            COMMENT '创建者',
    `updater`           varchar(64)  DEFAULT NULL            COMMENT '更新者',
    `deleted`           bit(1)       NOT NULL DEFAULT 0      COMMENT '逻辑删除',
    PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='CRM 负责人变更记录表';

-- --------------------------------------------------------------------
-- 20. 跟进记录表
-- --------------------------------------------------------------------
DROP TABLE IF EXISTS `crm_follow_up_record`;
CREATE TABLE `crm_follow_up_record` (
    `id`          bigint          NOT NULL AUTO_INCREMENT COMMENT '编号（主键自增）',
    `biz_type`    int             NOT NULL                COMMENT '数据类型（CrmBizTypeEnum）',
    `biz_id`      bigint          NOT NULL                COMMENT '数据编号（关联对应业务表 id）',
    `type`        int             NOT NULL                COMMENT '跟进类型（字典 CRM_FOLLOW_UP_TYPE）',
    `content`     varchar(2000)   DEFAULT NULL            COMMENT '跟进内容',
    `next_time`   datetime        DEFAULT NULL            COMMENT '下次联系时间',
    `pic_urls`    json            DEFAULT NULL            COMMENT '图片（JSON 数组，StringListTypeHandler 序列化）',
    `file_urls`   json            DEFAULT NULL            COMMENT '附件（JSON 数组，StringListTypeHandler 序列化）',
    `business_ids` json           DEFAULT NULL            COMMENT '关联商机编号数组（JSON 数组，LongListTypeHandler 序列化）',
    `contact_ids`  json           DEFAULT NULL            COMMENT '关联联系人编号数组（JSON 数组，LongListTypeHandler 序列化）',
    `tenant_id`   bigint       NOT NULL DEFAULT 1      COMMENT '租户编号',
    -- BaseDO 通用字段
    `create_time` datetime        NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time` datetime        NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '最后更新时间',
    `creator`     varchar(64)     DEFAULT NULL            COMMENT '创建者',
    `updater`     varchar(64)     DEFAULT NULL            COMMENT '更新者',
    `deleted`     bit(1)          NOT NULL DEFAULT 0      COMMENT '逻辑删除',
    PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='CRM 跟进记录表';

-- --------------------------------------------------------------------
-- 21. 业绩目标配置表
-- --------------------------------------------------------------------
DROP TABLE IF EXISTS `crm_performance_config`;
CREATE TABLE `crm_performance_config` (
    `id`                    bigint         NOT NULL AUTO_INCREMENT COMMENT '编号（主键自增）',
    `biz_type`              int            NOT NULL                COMMENT '目标类型（CrmPerformanceConfigBizTypeEnum）',
    `object_id`             bigint         NOT NULL                COMMENT '目标对象编号（用户ID 或 部门ID）',
    `object_type`           int            NOT NULL                COMMENT '目标对象类型（CrmPerformanceConfigObjectTypeEnum: 1-用户 2-部门）',
    `year`                  int            NOT NULL                COMMENT '年份',
    `year_target_price`     decimal(24,6)  DEFAULT NULL            COMMENT '年度目标金额',
    `january_target_price`  decimal(24,6)  DEFAULT NULL            COMMENT '一月目标金额',
    `february_target_price` decimal(24,6)  DEFAULT NULL            COMMENT '二月目标金额',
    `march_target_price`    decimal(24,6)  DEFAULT NULL            COMMENT '三月目标金额',
    `april_target_price`    decimal(24,6)  DEFAULT NULL            COMMENT '四月目标金额',
    `may_target_price`      decimal(24,6)  DEFAULT NULL            COMMENT '五月目标金额',
    `june_target_price`     decimal(24,6)  DEFAULT NULL            COMMENT '六月目标金额',
    `july_target_price`     decimal(24,6)  DEFAULT NULL            COMMENT '七月目标金额',
    `august_target_price`   decimal(24,6)  DEFAULT NULL            COMMENT '八月目标金额',
    `september_target_price` decimal(24,6)  DEFAULT NULL           COMMENT '九月目标金额',
    `october_target_price`  decimal(24,6)  DEFAULT NULL            COMMENT '十月目标金额',
    `november_target_price` decimal(24,6)  DEFAULT NULL            COMMENT '十一月目标金额',
    `december_target_price` decimal(24,6)  DEFAULT NULL            COMMENT '十二月目标金额',
    `tenant_id`   bigint       NOT NULL DEFAULT 1      COMMENT '租户编号',
    -- BaseDO 通用字段
    `create_time`           datetime       NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time`           datetime       NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '最后更新时间',
    `creator`               varchar(64)    DEFAULT NULL            COMMENT '创建者',
    `updater`               varchar(64)    DEFAULT NULL            COMMENT '更新者',
    `deleted`               bit(1)         NOT NULL DEFAULT 0      COMMENT '逻辑删除',
    PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='CRM 业绩目标配置表';

-- ======================================================================================
-- 索引建议（根据业务常见查询场景）
-- ======================================================================================

-- crm_customer
ALTER TABLE `crm_customer` ADD INDEX `idx_owner_user_id` (`owner_user_id`);
ALTER TABLE `crm_customer` ADD INDEX `idx_deal_status` (`deal_status`);
ALTER TABLE `crm_customer` ADD INDEX `idx_lock_status` (`lock_status`);

-- crm_clue
ALTER TABLE `crm_clue` ADD INDEX `idx_owner_user_id` (`owner_user_id`);
ALTER TABLE `crm_clue` ADD INDEX `idx_customer_id` (`customer_id`);
ALTER TABLE `crm_clue` ADD INDEX `idx_transform_status` (`transform_status`);

-- crm_contact
ALTER TABLE `crm_contact` ADD INDEX `idx_customer_id` (`customer_id`);
ALTER TABLE `crm_contact` ADD INDEX `idx_owner_user_id` (`owner_user_id`);

-- crm_contact_business
ALTER TABLE `crm_contact_business` ADD INDEX `idx_contact_id` (`contact_id`);
ALTER TABLE `crm_contact_business` ADD INDEX `idx_business_id` (`business_id`);

-- crm_business
ALTER TABLE `crm_business` ADD INDEX `idx_customer_id` (`customer_id`);
ALTER TABLE `crm_business` ADD INDEX `idx_owner_user_id` (`owner_user_id`);
ALTER TABLE `crm_business` ADD INDEX `idx_status_id` (`status_id`);

-- crm_business_product
ALTER TABLE `crm_business_product` ADD INDEX `idx_business_id` (`business_id`);
ALTER TABLE `crm_business_product` ADD INDEX `idx_product_id` (`product_id`);

-- crm_business_status
ALTER TABLE `crm_business_status` ADD INDEX `idx_type_id` (`type_id`);

-- crm_contract
ALTER TABLE `crm_contract` ADD INDEX `idx_customer_id` (`customer_id`);
ALTER TABLE `crm_contract` ADD INDEX `idx_business_id` (`business_id`);
ALTER TABLE `crm_contract` ADD INDEX `idx_owner_user_id` (`owner_user_id`);
ALTER TABLE `crm_contract` ADD INDEX `idx_audit_status` (`audit_status`);

-- crm_contract_product
ALTER TABLE `crm_contract_product` ADD INDEX `idx_contract_id` (`contract_id`);
ALTER TABLE `crm_contract_product` ADD INDEX `idx_product_id` (`product_id`);

-- crm_receivable
ALTER TABLE `crm_receivable` ADD INDEX `idx_customer_id` (`customer_id`);
ALTER TABLE `crm_receivable` ADD INDEX `idx_contract_id` (`contract_id`);
ALTER TABLE `crm_receivable` ADD INDEX `idx_owner_user_id` (`owner_user_id`);

-- crm_receivable_plan
ALTER TABLE `crm_receivable_plan` ADD INDEX `idx_customer_id` (`customer_id`);
ALTER TABLE `crm_receivable_plan` ADD INDEX `idx_contract_id` (`contract_id`);
ALTER TABLE `crm_receivable_plan` ADD INDEX `idx_owner_user_id` (`owner_user_id`);

-- crm_permission
ALTER TABLE `crm_permission` ADD UNIQUE INDEX `uk_biz_user` (`biz_type`, `biz_id`, `user_id`);
ALTER TABLE `crm_permission` ADD INDEX `idx_user_id` (`user_id`);

-- crm_owner_record
ALTER TABLE `crm_owner_record` ADD INDEX `idx_biz` (`biz_type`, `biz_id`);

-- crm_follow_up_record
ALTER TABLE `crm_follow_up_record` ADD INDEX `idx_biz` (`biz_type`, `biz_id`);

-- crm_product
ALTER TABLE `crm_product` ADD INDEX `idx_category_id` (`category_id`);
ALTER TABLE `crm_product` ADD INDEX `idx_owner_user_id` (`owner_user_id`);
ALTER TABLE `crm_product` ADD UNIQUE INDEX `uk_no` (`no`);

-- crm_product_category
ALTER TABLE `crm_product_category` ADD INDEX `idx_parent_id` (`parent_id`);

-- crm_performance_config
ALTER TABLE `crm_performance_config` ADD INDEX `idx_object_year` (`object_id`, `year`);
