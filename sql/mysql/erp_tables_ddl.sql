-- ======================================================================================
-- ERP 模块 - 完整表结构 DDL（逆向自 33 个 DO 实体类）
-- 数据库类型: MySQL 5.7+ / 8.0
-- 所有表均继承 BaseDO 通用字段: create_time, update_time, creator, updater, deleted
-- ======================================================================================

-- ======================================================================================
-- 一、产品域（3 表）
-- ======================================================================================

-- 1.1 产品分类表
DROP TABLE IF EXISTS `erp_product_category`;
CREATE TABLE `erp_product_category` (
    `id`          bigint       NOT NULL AUTO_INCREMENT COMMENT '分类编号',
    `tenant_id`   bigint       NOT NULL DEFAULT 1      COMMENT '租户编号',
    `parent_id`   bigint       NOT NULL DEFAULT 0       COMMENT '父分类编号（0 表示根分类）',
    `name`        varchar(100) NOT NULL                COMMENT '分类名称',
    `code`        varchar(64)  NOT NULL                COMMENT '分类编码',
    `sort`        int          NOT NULL DEFAULT 0      COMMENT '排序',
    `status`      int          NOT NULL DEFAULT 0      COMMENT '状态（CommonStatusEnum: 0-禁用 1-启用）',
    -- BaseDO
    `create_time` datetime     NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time` datetime     NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '最后更新时间',
    `creator`     varchar(64)  DEFAULT NULL            COMMENT '创建者',
    `updater`     varchar(64)  DEFAULT NULL            COMMENT '更新者',
    `deleted`     bit(1)       NOT NULL DEFAULT 0      COMMENT '逻辑删除',
    PRIMARY KEY (`id`),
    INDEX `idx_parent_id` (`parent_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='ERP 产品分类表';

-- 1.2 产品单位表
DROP TABLE IF EXISTS `erp_product_unit`;
CREATE TABLE `erp_product_unit` (
    `id`          bigint       NOT NULL AUTO_INCREMENT COMMENT '单位编号',
    `tenant_id`   bigint       NOT NULL DEFAULT 1      COMMENT '租户编号',
    `name`        varchar(50)  NOT NULL                COMMENT '单位名称（如：个、箱、kg）',
    `status`      int          NOT NULL DEFAULT 0      COMMENT '状态（CommonStatusEnum）',
    -- BaseDO
    `create_time` datetime     NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time` datetime     NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '最后更新时间',
    `creator`     varchar(64)  DEFAULT NULL            COMMENT '创建者',
    `updater`     varchar(64)  DEFAULT NULL            COMMENT '更新者',
    `deleted`     bit(1)       NOT NULL DEFAULT 0      COMMENT '逻辑删除',
    PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='ERP 产品单位表';

-- 1.3 产品表
DROP TABLE IF EXISTS `erp_product`;
CREATE TABLE `erp_product` (
    `id`             bigint         NOT NULL AUTO_INCREMENT COMMENT '产品编号',
    `tenant_id`   bigint       NOT NULL DEFAULT 1      COMMENT '租户编号',
    `name`           varchar(100)   NOT NULL                COMMENT '产品名称',
    `bar_code`       varchar(64)    DEFAULT NULL            COMMENT '产品条码',
    `category_id`    bigint         NOT NULL                COMMENT '产品分类编号（关联 erp_product_category.id）',
    `unit_id`        bigint         NOT NULL                COMMENT '单位编号（关联 erp_product_unit.id）',
    `status`         int            NOT NULL DEFAULT 0      COMMENT '状态（CommonStatusEnum）',
    `standard`       varchar(255)   DEFAULT NULL            COMMENT '产品规格',
    `remark`         varchar(500)   DEFAULT NULL            COMMENT '备注',
    `expiry_day`     int            DEFAULT NULL            COMMENT '保质期天数',
    `weight`         decimal(24,6)  DEFAULT NULL            COMMENT '基础重量（kg）',
    `purchase_price` decimal(24,6)  DEFAULT NULL            COMMENT '采购价格，单位：元',
    `sale_price`     decimal(24,6)  DEFAULT NULL            COMMENT '销售价格，单位：元',
    `min_price`      decimal(24,6)  DEFAULT NULL            COMMENT '最低价格，单位：元',
    -- BaseDO
    `create_time`    datetime       NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time`    datetime       NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '最后更新时间',
    `creator`        varchar(64)    DEFAULT NULL            COMMENT '创建者',
    `updater`        varchar(64)    DEFAULT NULL            COMMENT '更新者',
    `deleted`        bit(1)         NOT NULL DEFAULT 0      COMMENT '逻辑删除',
    PRIMARY KEY (`id`),
    INDEX `idx_category_id` (`category_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='ERP 产品表';

-- ======================================================================================
-- 二、客户 / 供应商域（2 表）
-- ======================================================================================

-- 2.1 客户表
DROP TABLE IF EXISTS `erp_customer`;
CREATE TABLE `erp_customer` (
    `id`           bigint         NOT NULL AUTO_INCREMENT COMMENT '客户编号',
    `tenant_id`   bigint       NOT NULL DEFAULT 1      COMMENT '租户编号',
    `name`         varchar(100)   NOT NULL                COMMENT '客户名称',
    `contact`      varchar(50)    DEFAULT NULL            COMMENT '联系人',
    `mobile`       varchar(20)    DEFAULT NULL            COMMENT '手机号',
    `telephone`    varchar(20)    DEFAULT NULL            COMMENT '电话',
    `email`        varchar(100)   DEFAULT NULL            COMMENT '邮箱',
    `fax`          varchar(20)    DEFAULT NULL            COMMENT '传真',
    `remark`       varchar(500)   DEFAULT NULL            COMMENT '备注',
    `status`       int            NOT NULL DEFAULT 0      COMMENT '状态（CommonStatusEnum）',
    `sort`         int            DEFAULT NULL            COMMENT '排序',
    `tax_no`       varchar(50)    DEFAULT NULL            COMMENT '纳税人识别号',
    `tax_percent`  decimal(10,2)  DEFAULT NULL            COMMENT '税率',
    `bank_name`    varchar(100)   DEFAULT NULL            COMMENT '开户行',
    `bank_account` varchar(50)    DEFAULT NULL            COMMENT '开户账号',
    `bank_address` varchar(255)   DEFAULT NULL            COMMENT '开户地址',
    -- BaseDO
    `create_time`  datetime       NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time`  datetime       NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '最后更新时间',
    `creator`      varchar(64)    DEFAULT NULL            COMMENT '创建者',
    `updater`      varchar(64)    DEFAULT NULL            COMMENT '更新者',
    `deleted`      bit(1)         NOT NULL DEFAULT 0      COMMENT '逻辑删除',
    PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='ERP 客户表';

-- 2.2 供应商表
DROP TABLE IF EXISTS `erp_supplier`;
CREATE TABLE `erp_supplier` (
    `id`           bigint         NOT NULL AUTO_INCREMENT COMMENT '供应商编号',
    `tenant_id`   bigint       NOT NULL DEFAULT 1      COMMENT '租户编号',
    `name`         varchar(100)   NOT NULL                COMMENT '供应商名称',
    `contact`      varchar(50)    DEFAULT NULL            COMMENT '联系人',
    `mobile`       varchar(20)    DEFAULT NULL            COMMENT '手机号',
    `telephone`    varchar(20)    DEFAULT NULL            COMMENT '电话',
    `email`        varchar(100)   DEFAULT NULL            COMMENT '邮箱',
    `fax`          varchar(20)    DEFAULT NULL            COMMENT '传真',
    `remark`       varchar(500)   DEFAULT NULL            COMMENT '备注',
    `status`       int            NOT NULL DEFAULT 0      COMMENT '状态（CommonStatusEnum）',
    `sort`         int            DEFAULT NULL            COMMENT '排序',
    `tax_no`       varchar(50)    DEFAULT NULL            COMMENT '纳税人识别号',
    `tax_percent`  decimal(10,2)  DEFAULT NULL            COMMENT '税率',
    `bank_name`    varchar(100)   DEFAULT NULL            COMMENT '开户行',
    `bank_account` varchar(50)    DEFAULT NULL            COMMENT '开户账号',
    `bank_address` varchar(255)   DEFAULT NULL            COMMENT '开户地址',
    -- BaseDO
    `create_time`  datetime       NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time`  datetime       NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '最后更新时间',
    `creator`      varchar(64)    DEFAULT NULL            COMMENT '创建者',
    `updater`      varchar(64)    DEFAULT NULL            COMMENT '更新者',
    `deleted`      bit(1)         NOT NULL DEFAULT 0      COMMENT '逻辑删除',
    PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='ERP 供应商表';

-- ======================================================================================
-- 三、销售域（7 表）
-- ======================================================================================

-- 3.1 销售订单表
DROP TABLE IF EXISTS `erp_sale_order`;
CREATE TABLE `erp_sale_order` (
    `id`                  bigint         NOT NULL AUTO_INCREMENT COMMENT '编号',
    `tenant_id`   bigint       NOT NULL DEFAULT 1      COMMENT '租户编号',
    `no`                  varchar(64)    NOT NULL                COMMENT '销售订单号',
    `status`              int            NOT NULL DEFAULT 0      COMMENT '状态（ErpAuditStatus）',
    `customer_id`         bigint         NOT NULL                COMMENT '客户编号（关联 erp_customer.id）',
    `account_id`          bigint         NOT NULL                COMMENT '结算账户编号（关联 erp_account.id）',
    `sale_user_id`        bigint         DEFAULT NULL            COMMENT '销售员编号（关联 AdminUserDO.id）',
    `order_time`          datetime       NOT NULL                COMMENT '下单时间',
    `total_count`         decimal(24,6)  NOT NULL DEFAULT 0      COMMENT '合计数量',
    `total_price`         decimal(24,6)  NOT NULL DEFAULT 0      COMMENT '最终合计价格，单位：元（totalPrice = totalProductPrice + totalTaxPrice - discountPrice）',
    `total_product_price` decimal(24,6)  NOT NULL DEFAULT 0      COMMENT '合计产品价格，单位：元',
    `total_tax_price`     decimal(24,6)  NOT NULL DEFAULT 0      COMMENT '合计税额，单位：元',
    `discount_percent`    decimal(10,2)  NOT NULL DEFAULT 0      COMMENT '优惠率，百分比',
    `discount_price`      decimal(24,6)  NOT NULL DEFAULT 0      COMMENT '优惠金额，单位：元',
    `deposit_price`       decimal(24,6)  DEFAULT NULL            COMMENT '定金金额，单位：元',
    `file_url`            varchar(500)   DEFAULT NULL            COMMENT '附件地址',
    `remark`              varchar(500)   DEFAULT NULL            COMMENT '备注',
    `out_count`           decimal(24,6)  NOT NULL DEFAULT 0      COMMENT '销售出库数量',
    `return_count`        decimal(24,6)  NOT NULL DEFAULT 0      COMMENT '销售退货数量',
    -- BaseDO
    `create_time`         datetime       NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time`         datetime       NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '最后更新时间',
    `creator`             varchar(64)    DEFAULT NULL            COMMENT '创建者',
    `updater`             varchar(64)    DEFAULT NULL            COMMENT '更新者',
    `deleted`             bit(1)         NOT NULL DEFAULT 0      COMMENT '逻辑删除',
    PRIMARY KEY (`id`),
    INDEX `idx_customer_id` (`customer_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='ERP 销售订单表';

-- 3.2 销售订单项表
DROP TABLE IF EXISTS `erp_sale_order_items`;
CREATE TABLE `erp_sale_order_items` (
    `id`             bigint         NOT NULL AUTO_INCREMENT COMMENT '编号',
    `tenant_id`   bigint       NOT NULL DEFAULT 1      COMMENT '租户编号',
    `order_id`       bigint         NOT NULL                COMMENT '销售订单编号（关联 erp_sale_order.id）',
    `product_id`     bigint         NOT NULL                COMMENT '产品编号（关联 erp_product.id）',
    `product_unit_id` bigint        NOT NULL                COMMENT '产品单位编号',
    `product_price`  decimal(24,6)  NOT NULL                COMMENT '产品单价，单位：元',
    `count`          decimal(24,6)  NOT NULL                COMMENT '数量',
    `total_price`    decimal(24,6)  NOT NULL                COMMENT '总计价格，单位：元',
    `tax_percent`    decimal(10,2)  DEFAULT NULL            COMMENT '税率，百分比',
    `tax_price`      decimal(24,6)  DEFAULT NULL            COMMENT '税额，单位：元',
    `remark`         varchar(500)   DEFAULT NULL            COMMENT '备注',
    `out_count`      decimal(24,6)  NOT NULL DEFAULT 0      COMMENT '销售出库数量',
    `return_count`   decimal(24,6)  NOT NULL DEFAULT 0      COMMENT '销售退货数量',
    -- BaseDO
    `create_time`    datetime       NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time`    datetime       NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '最后更新时间',
    `creator`        varchar(64)    DEFAULT NULL            COMMENT '创建者',
    `updater`        varchar(64)    DEFAULT NULL            COMMENT '更新者',
    `deleted`        bit(1)         NOT NULL DEFAULT 0      COMMENT '逻辑删除',
    PRIMARY KEY (`id`),
    INDEX `idx_order_id` (`order_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='ERP 销售订单项表';

-- 3.3 销售出库单表
DROP TABLE IF EXISTS `erp_sale_out`;
CREATE TABLE `erp_sale_out` (
    `id`                  bigint         NOT NULL AUTO_INCREMENT COMMENT '编号',
    `tenant_id`   bigint       NOT NULL DEFAULT 1      COMMENT '租户编号',
    `no`                  varchar(64)    NOT NULL                COMMENT '销售出库单号',
    `status`              int            NOT NULL DEFAULT 0      COMMENT '状态（ErpAuditStatus）',
    `customer_id`         bigint         NOT NULL                COMMENT '客户编号（关联 erp_customer.id）',
    `account_id`          bigint         NOT NULL                COMMENT '结算账户编号（关联 erp_account.id）',
    `sale_user_id`        bigint         DEFAULT NULL            COMMENT '销售员编号（关联 AdminUserDO.id）',
    `out_time`            datetime       NOT NULL                COMMENT '出库时间',
    `order_id`            bigint         NOT NULL                COMMENT '销售订单编号（关联 erp_sale_order.id）',
    `order_no`            varchar(64)    DEFAULT NULL            COMMENT '销售订单号（冗余）',
    `total_count`         decimal(24,6)  NOT NULL DEFAULT 0      COMMENT '合计数量',
    `total_price`         decimal(24,6)  NOT NULL DEFAULT 0      COMMENT '最终合计价格，单位：元',
    `receipt_price`       decimal(24,6)  NOT NULL DEFAULT 0      COMMENT '已收款金额，单位：元',
    `total_product_price` decimal(24,6)  NOT NULL DEFAULT 0      COMMENT '合计产品价格，单位：元',
    `total_tax_price`     decimal(24,6)  NOT NULL DEFAULT 0      COMMENT '合计税额，单位：元',
    `discount_percent`    decimal(10,2)  NOT NULL DEFAULT 0      COMMENT '优惠率，百分比',
    `discount_price`      decimal(24,6)  NOT NULL DEFAULT 0      COMMENT '优惠金额，单位：元',
    `other_price`         decimal(24,6)  DEFAULT NULL            COMMENT '其它金额，单位：元',
    `file_url`            varchar(500)   DEFAULT NULL            COMMENT '附件地址',
    `remark`              varchar(500)   DEFAULT NULL            COMMENT '备注',
    -- BaseDO
    `create_time`         datetime       NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time`         datetime       NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '最后更新时间',
    `creator`             varchar(64)    DEFAULT NULL            COMMENT '创建者',
    `updater`             varchar(64)    DEFAULT NULL            COMMENT '更新者',
    `deleted`             bit(1)         NOT NULL DEFAULT 0      COMMENT '逻辑删除',
    PRIMARY KEY (`id`),
    INDEX `idx_order_id` (`order_id`),
    INDEX `idx_customer_id` (`customer_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='ERP 销售出库单表';

-- 3.4 销售出库单项表
DROP TABLE IF EXISTS `erp_sale_out_items`;
CREATE TABLE `erp_sale_out_items` (
    `id`              bigint         NOT NULL AUTO_INCREMENT COMMENT '编号',
    `tenant_id`   bigint       NOT NULL DEFAULT 1      COMMENT '租户编号',
    `out_id`          bigint         NOT NULL                COMMENT '销售出库单编号（关联 erp_sale_out.id）',
    `order_item_id`   bigint         NOT NULL                COMMENT '销售订单项编号（关联 erp_sale_order_items.id）',
    `warehouse_id`    bigint         NOT NULL                COMMENT '仓库编号（关联 erp_warehouse.id）',
    `product_id`      bigint         NOT NULL                COMMENT '产品编号（关联 erp_product.id）',
    `product_unit_id` bigint         NOT NULL                COMMENT '产品单位编号',
    `product_price`   decimal(24,6)  NOT NULL                COMMENT '产品单价，单位：元',
    `count`           decimal(24,6)  NOT NULL                COMMENT '数量',
    `total_price`     decimal(24,6)  NOT NULL                COMMENT '总计价格，单位：元',
    `tax_percent`     decimal(10,2)  DEFAULT NULL            COMMENT '税率，百分比',
    `tax_price`       decimal(24,6)  DEFAULT NULL            COMMENT '税额，单位：元',
    `remark`          varchar(500)   DEFAULT NULL            COMMENT '备注',
    -- BaseDO
    `create_time`     datetime       NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time`     datetime       NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '最后更新时间',
    `creator`         varchar(64)    DEFAULT NULL            COMMENT '创建者',
    `updater`         varchar(64)    DEFAULT NULL            COMMENT '更新者',
    `deleted`         bit(1)         NOT NULL DEFAULT 0      COMMENT '逻辑删除',
    PRIMARY KEY (`id`),
    INDEX `idx_out_id` (`out_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='ERP 销售出库单项表';

-- 3.5 销售退货单表
DROP TABLE IF EXISTS `erp_sale_return`;
CREATE TABLE `erp_sale_return` (
    `id`                  bigint         NOT NULL AUTO_INCREMENT COMMENT '编号',
    `tenant_id`   bigint       NOT NULL DEFAULT 1      COMMENT '租户编号',
    `no`                  varchar(64)    NOT NULL                COMMENT '销售退货单号',
    `status`              int            NOT NULL DEFAULT 0      COMMENT '状态（ErpAuditStatus）',
    `customer_id`         bigint         NOT NULL                COMMENT '客户编号（关联 erp_customer.id）',
    `account_id`          bigint         NOT NULL                COMMENT '结算账户编号（关联 erp_account.id）',
    `sale_user_id`        bigint         DEFAULT NULL            COMMENT '销售员编号（关联 AdminUserDO.id）',
    `return_time`         datetime       NOT NULL                COMMENT '退货时间',
    `order_id`            bigint         NOT NULL                COMMENT '销售订单编号（关联 erp_sale_order.id）',
    `order_no`            varchar(64)    DEFAULT NULL            COMMENT '销售订单号（冗余）',
    `total_count`         decimal(24,6)  NOT NULL DEFAULT 0      COMMENT '合计数量',
    `total_price`         decimal(24,6)  NOT NULL DEFAULT 0      COMMENT '最终合计价格，单位：元',
    `refund_price`        decimal(24,6)  NOT NULL DEFAULT 0      COMMENT '已退款金额，单位：元',
    `total_product_price` decimal(24,6)  NOT NULL DEFAULT 0      COMMENT '合计产品价格，单位：元',
    `total_tax_price`     decimal(24,6)  NOT NULL DEFAULT 0      COMMENT '合计税额，单位：元',
    `discount_percent`    decimal(10,2)  NOT NULL DEFAULT 0      COMMENT '优惠率，百分比',
    `discount_price`      decimal(24,6)  NOT NULL DEFAULT 0      COMMENT '优惠金额，单位：元',
    `other_price`         decimal(24,6)  DEFAULT NULL            COMMENT '其它金额，单位：元',
    `file_url`            varchar(500)   DEFAULT NULL            COMMENT '附件地址',
    `remark`              varchar(500)   DEFAULT NULL            COMMENT '备注',
    -- BaseDO
    `create_time`         datetime       NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time`         datetime       NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '最后更新时间',
    `creator`             varchar(64)    DEFAULT NULL            COMMENT '创建者',
    `updater`             varchar(64)    DEFAULT NULL            COMMENT '更新者',
    `deleted`             bit(1)         NOT NULL DEFAULT 0      COMMENT '逻辑删除',
    PRIMARY KEY (`id`),
    INDEX `idx_order_id` (`order_id`),
    INDEX `idx_customer_id` (`customer_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='ERP 销售退货单表';

-- 3.6 销售退货单项表
DROP TABLE IF EXISTS `erp_sale_return_items`;
CREATE TABLE `erp_sale_return_items` (
    `id`              bigint         NOT NULL AUTO_INCREMENT COMMENT '编号',
    `tenant_id`   bigint       NOT NULL DEFAULT 1      COMMENT '租户编号',
    `return_id`       bigint         NOT NULL                COMMENT '销售退货单编号（关联 erp_sale_return.id）',
    `order_item_id`   bigint         NOT NULL                COMMENT '销售订单项编号（关联 erp_sale_order_items.id）',
    `warehouse_id`    bigint         NOT NULL                COMMENT '仓库编号（关联 erp_warehouse.id）',
    `product_id`      bigint         NOT NULL                COMMENT '产品编号（关联 erp_product.id）',
    `product_unit_id` bigint         NOT NULL                COMMENT '产品单位编号',
    `product_price`   decimal(24,6)  NOT NULL                COMMENT '产品单价，单位：元',
    `count`           decimal(24,6)  NOT NULL                COMMENT '数量',
    `total_price`     decimal(24,6)  NOT NULL                COMMENT '总计价格，单位：元',
    `tax_percent`     decimal(10,2)  DEFAULT NULL            COMMENT '税率，百分比',
    `tax_price`       decimal(24,6)  DEFAULT NULL            COMMENT '税额，单位：元',
    `remark`          varchar(500)   DEFAULT NULL            COMMENT '备注',
    -- BaseDO
    `create_time`     datetime       NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time`     datetime       NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '最后更新时间',
    `creator`         varchar(64)    DEFAULT NULL            COMMENT '创建者',
    `updater`         varchar(64)    DEFAULT NULL            COMMENT '更新者',
    `deleted`         bit(1)         NOT NULL DEFAULT 0      COMMENT '逻辑删除',
    PRIMARY KEY (`id`),
    INDEX `idx_return_id` (`return_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='ERP 销售退货单项表';

-- ======================================================================================
-- 四、采购域（7 表）
-- ======================================================================================

-- 4.1 采购订单表
DROP TABLE IF EXISTS `erp_purchase_order`;
CREATE TABLE `erp_purchase_order` (
    `id`                  bigint         NOT NULL AUTO_INCREMENT COMMENT '编号',
    `tenant_id`   bigint       NOT NULL DEFAULT 1      COMMENT '租户编号',
    `no`                  varchar(64)    NOT NULL                COMMENT '采购订单号',
    `status`              int            NOT NULL DEFAULT 0      COMMENT '状态（ErpAuditStatus）',
    `supplier_id`         bigint         NOT NULL                COMMENT '供应商编号（关联 erp_supplier.id）',
    `account_id`          bigint         NOT NULL                COMMENT '结算账户编号（关联 erp_account.id）',
    `order_time`          datetime       NOT NULL                COMMENT '下单时间',
    `total_count`         decimal(24,6)  NOT NULL DEFAULT 0      COMMENT '合计数量',
    `total_price`         decimal(24,6)  NOT NULL DEFAULT 0      COMMENT '最终合计价格，单位：元',
    `total_product_price` decimal(24,6)  NOT NULL DEFAULT 0      COMMENT '合计产品价格，单位：元',
    `total_tax_price`     decimal(24,6)  NOT NULL DEFAULT 0      COMMENT '合计税额，单位：元',
    `discount_percent`    decimal(10,2)  NOT NULL DEFAULT 0      COMMENT '优惠率，百分比',
    `discount_price`      decimal(24,6)  NOT NULL DEFAULT 0      COMMENT '优惠金额，单位：元',
    `deposit_price`       decimal(24,6)  DEFAULT NULL            COMMENT '定金金额，单位：元',
    `file_url`            varchar(500)   DEFAULT NULL            COMMENT '附件地址',
    `remark`              varchar(500)   DEFAULT NULL            COMMENT '备注',
    `in_count`            decimal(24,6)  NOT NULL DEFAULT 0      COMMENT '采购入库数量',
    `return_count`        decimal(24,6)  NOT NULL DEFAULT 0      COMMENT '采购退货数量',
    -- BaseDO
    `create_time`         datetime       NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time`         datetime       NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '最后更新时间',
    `creator`             varchar(64)    DEFAULT NULL            COMMENT '创建者',
    `updater`             varchar(64)    DEFAULT NULL            COMMENT '更新者',
    `deleted`             bit(1)         NOT NULL DEFAULT 0      COMMENT '逻辑删除',
    PRIMARY KEY (`id`),
    INDEX `idx_supplier_id` (`supplier_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='ERP 采购订单表';

-- 4.2 采购订单项表
DROP TABLE IF EXISTS `erp_purchase_order_items`;
CREATE TABLE `erp_purchase_order_items` (
    `id`              bigint         NOT NULL AUTO_INCREMENT COMMENT '编号',
    `tenant_id`   bigint       NOT NULL DEFAULT 1      COMMENT '租户编号',
    `order_id`        bigint         NOT NULL                COMMENT '采购订单编号（关联 erp_purchase_order.id）',
    `product_id`      bigint         NOT NULL                COMMENT '产品编号（关联 erp_product.id）',
    `product_unit_id` bigint         NOT NULL                COMMENT '产品单位编号',
    `product_price`   decimal(24,6)  NOT NULL                COMMENT '产品单价，单位：元',
    `count`           decimal(24,6)  NOT NULL                COMMENT '数量',
    `total_price`     decimal(24,6)  NOT NULL                COMMENT '总计价格，单位：元',
    `tax_percent`     decimal(10,2)  DEFAULT NULL            COMMENT '税率，百分比',
    `tax_price`       decimal(24,6)  DEFAULT NULL            COMMENT '税额，单位：元',
    `remark`          varchar(500)   DEFAULT NULL            COMMENT '备注',
    `in_count`        decimal(24,6)  NOT NULL DEFAULT 0      COMMENT '采购入库数量',
    `return_count`    decimal(24,6)  NOT NULL DEFAULT 0      COMMENT '采购退货数量',
    -- BaseDO
    `create_time`     datetime       NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time`     datetime       NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '最后更新时间',
    `creator`         varchar(64)    DEFAULT NULL            COMMENT '创建者',
    `updater`         varchar(64)    DEFAULT NULL            COMMENT '更新者',
    `deleted`         bit(1)         NOT NULL DEFAULT 0      COMMENT '逻辑删除',
    PRIMARY KEY (`id`),
    INDEX `idx_order_id` (`order_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='ERP 采购订单项表';

-- 4.3 采购入库单表
DROP TABLE IF EXISTS `erp_purchase_in`;
CREATE TABLE `erp_purchase_in` (
    `id`                  bigint         NOT NULL AUTO_INCREMENT COMMENT '编号',
    `tenant_id`   bigint       NOT NULL DEFAULT 1      COMMENT '租户编号',
    `no`                  varchar(64)    NOT NULL                COMMENT '采购入库单号',
    `status`              int            NOT NULL DEFAULT 0      COMMENT '状态（ErpAuditStatus）',
    `supplier_id`         bigint         NOT NULL                COMMENT '供应商编号（关联 erp_supplier.id）',
    `account_id`          bigint         NOT NULL                COMMENT '结算账户编号（关联 erp_account.id）',
    `in_time`             datetime       NOT NULL                COMMENT '入库时间',
    `order_id`            bigint         NOT NULL                COMMENT '采购订单编号（关联 erp_purchase_order.id）',
    `order_no`            varchar(64)    DEFAULT NULL            COMMENT '采购订单号（冗余）',
    `total_count`         decimal(24,6)  NOT NULL DEFAULT 0      COMMENT '合计数量',
    `total_price`         decimal(24,6)  NOT NULL DEFAULT 0      COMMENT '最终合计价格，单位：元',
    `payment_price`       decimal(24,6)  NOT NULL DEFAULT 0      COMMENT '已支付金额，单位：元',
    `total_product_price` decimal(24,6)  NOT NULL DEFAULT 0      COMMENT '合计产品价格，单位：元',
    `total_tax_price`     decimal(24,6)  NOT NULL DEFAULT 0      COMMENT '合计税额，单位：元',
    `discount_percent`    decimal(10,2)  NOT NULL DEFAULT 0      COMMENT '优惠率，百分比',
    `discount_price`      decimal(24,6)  NOT NULL DEFAULT 0      COMMENT '优惠金额，单位：元',
    `other_price`         decimal(24,6)  DEFAULT NULL            COMMENT '其它金额，单位：元',
    `file_url`            varchar(500)   DEFAULT NULL            COMMENT '附件地址',
    `remark`              varchar(500)   DEFAULT NULL            COMMENT '备注',
    -- BaseDO
    `create_time`         datetime       NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time`         datetime       NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '最后更新时间',
    `creator`             varchar(64)    DEFAULT NULL            COMMENT '创建者',
    `updater`             varchar(64)    DEFAULT NULL            COMMENT '更新者',
    `deleted`             bit(1)         NOT NULL DEFAULT 0      COMMENT '逻辑删除',
    PRIMARY KEY (`id`),
    INDEX `idx_order_id` (`order_id`),
    INDEX `idx_supplier_id` (`supplier_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='ERP 采购入库单表';

-- 4.4 采购入库单项表
DROP TABLE IF EXISTS `erp_purchase_in_items`;
CREATE TABLE `erp_purchase_in_items` (
    `id`              bigint         NOT NULL AUTO_INCREMENT COMMENT '编号',
    `tenant_id`   bigint       NOT NULL DEFAULT 1      COMMENT '租户编号',
    `in_id`           bigint         NOT NULL                COMMENT '采购入库单编号（关联 erp_purchase_in.id）',
    `order_item_id`   bigint         NOT NULL                COMMENT '采购订单项编号（关联 erp_purchase_order_items.id）',
    `warehouse_id`    bigint         NOT NULL                COMMENT '仓库编号（关联 erp_warehouse.id）',
    `product_id`      bigint         NOT NULL                COMMENT '产品编号（关联 erp_product.id）',
    `product_unit_id` bigint         NOT NULL                COMMENT '产品单位编号',
    `product_price`   decimal(24,6)  NOT NULL                COMMENT '产品单价，单位：元',
    `count`           decimal(24,6)  NOT NULL                COMMENT '数量',
    `total_price`     decimal(24,6)  NOT NULL                COMMENT '总计价格，单位：元',
    `tax_percent`     decimal(10,2)  DEFAULT NULL            COMMENT '税率，百分比',
    `tax_price`       decimal(24,6)  DEFAULT NULL            COMMENT '税额，单位：元',
    `remark`          varchar(500)   DEFAULT NULL            COMMENT '备注',
    -- BaseDO
    `create_time`     datetime       NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time`     datetime       NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '最后更新时间',
    `creator`         varchar(64)    DEFAULT NULL            COMMENT '创建者',
    `updater`         varchar(64)    DEFAULT NULL            COMMENT '更新者',
    `deleted`         bit(1)         NOT NULL DEFAULT 0      COMMENT '逻辑删除',
    PRIMARY KEY (`id`),
    INDEX `idx_in_id` (`in_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='ERP 采购入库单项表';

-- 4.5 采购退货单表
DROP TABLE IF EXISTS `erp_purchase_return`;
CREATE TABLE `erp_purchase_return` (
    `id`                  bigint         NOT NULL AUTO_INCREMENT COMMENT '编号',
    `tenant_id`   bigint       NOT NULL DEFAULT 1      COMMENT '租户编号',
    `no`                  varchar(64)    NOT NULL                COMMENT '采购退货单号',
    `status`              int            NOT NULL DEFAULT 0      COMMENT '状态（ErpAuditStatus）',
    `supplier_id`         bigint         NOT NULL                COMMENT '供应商编号（关联 erp_supplier.id）',
    `account_id`          bigint         NOT NULL                COMMENT '结算账户编号（关联 erp_account.id）',
    `return_time`         datetime       NOT NULL                COMMENT '退货时间',
    `order_id`            bigint         NOT NULL                COMMENT '采购订单编号（关联 erp_purchase_order.id）',
    `order_no`            varchar(64)    DEFAULT NULL            COMMENT '采购订单号（冗余）',
    `total_count`         decimal(24,6)  NOT NULL DEFAULT 0      COMMENT '合计数量',
    `total_price`         decimal(24,6)  NOT NULL DEFAULT 0      COMMENT '最终合计价格，单位：元',
    `refund_price`        decimal(24,6)  NOT NULL DEFAULT 0      COMMENT '已退款金额，单位：元',
    `total_product_price` decimal(24,6)  NOT NULL DEFAULT 0      COMMENT '合计产品价格，单位：元',
    `total_tax_price`     decimal(24,6)  NOT NULL DEFAULT 0      COMMENT '合计税额，单位：元',
    `discount_percent`    decimal(10,2)  NOT NULL DEFAULT 0      COMMENT '优惠率，百分比',
    `discount_price`      decimal(24,6)  NOT NULL DEFAULT 0      COMMENT '优惠金额，单位：元',
    `other_price`         decimal(24,6)  DEFAULT NULL            COMMENT '其它金额，单位：元',
    `file_url`            varchar(500)   DEFAULT NULL            COMMENT '附件地址',
    `remark`              varchar(500)   DEFAULT NULL            COMMENT '备注',
    -- BaseDO
    `create_time`         datetime       NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time`         datetime       NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '最后更新时间',
    `creator`             varchar(64)    DEFAULT NULL            COMMENT '创建者',
    `updater`             varchar(64)    DEFAULT NULL            COMMENT '更新者',
    `deleted`             bit(1)         NOT NULL DEFAULT 0      COMMENT '逻辑删除',
    PRIMARY KEY (`id`),
    INDEX `idx_order_id` (`order_id`),
    INDEX `idx_supplier_id` (`supplier_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='ERP 采购退货单表';

-- 4.6 采购退货单项表
DROP TABLE IF EXISTS `erp_purchase_return_items`;
CREATE TABLE `erp_purchase_return_items` (
    `id`              bigint         NOT NULL AUTO_INCREMENT COMMENT '编号',
    `tenant_id`   bigint       NOT NULL DEFAULT 1      COMMENT '租户编号',
    `return_id`       bigint         NOT NULL                COMMENT '采购退货单编号（关联 erp_purchase_return.id）',
    `order_item_id`   bigint         NOT NULL                COMMENT '采购订单项编号（关联 erp_purchase_order_items.id）',
    `warehouse_id`    bigint         NOT NULL                COMMENT '仓库编号（关联 erp_warehouse.id）',
    `product_id`      bigint         NOT NULL                COMMENT '产品编号（关联 erp_product.id）',
    `product_unit_id` bigint         NOT NULL                COMMENT '产品单位编号',
    `product_price`   decimal(24,6)  NOT NULL                COMMENT '产品单价，单位：元',
    `count`           decimal(24,6)  NOT NULL                COMMENT '数量',
    `total_price`     decimal(24,6)  NOT NULL                COMMENT '总计价格，单位：元',
    `tax_percent`     decimal(10,2)  DEFAULT NULL            COMMENT '税率，百分比',
    `tax_price`       decimal(24,6)  DEFAULT NULL            COMMENT '税额，单位：元',
    `remark`          varchar(500)   DEFAULT NULL            COMMENT '备注',
    -- BaseDO
    `create_time`     datetime       NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time`     datetime       NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '最后更新时间',
    `creator`         varchar(64)    DEFAULT NULL            COMMENT '创建者',
    `updater`         varchar(64)    DEFAULT NULL            COMMENT '更新者',
    `deleted`         bit(1)         NOT NULL DEFAULT 0      COMMENT '逻辑删除',
    PRIMARY KEY (`id`),
    INDEX `idx_return_id` (`return_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='ERP 采购退货单项表';

-- ======================================================================================
-- 五、库存域（11 表）
-- ======================================================================================

-- 5.1 仓库表
DROP TABLE IF EXISTS `erp_warehouse`;
CREATE TABLE `erp_warehouse` (
    `id`              bigint         NOT NULL AUTO_INCREMENT COMMENT '仓库编号',
    `tenant_id`   bigint       NOT NULL DEFAULT 1      COMMENT '租户编号',
    `name`            varchar(100)   NOT NULL                COMMENT '仓库名称',
    `address`         varchar(255)   DEFAULT NULL            COMMENT '仓库地址',
    `sort`            bigint         NOT NULL DEFAULT 0      COMMENT '排序',
    `remark`          varchar(500)   DEFAULT NULL            COMMENT '备注',
    `principal`       varchar(50)    DEFAULT NULL            COMMENT '负责人',
    `warehouse_price` decimal(24,6)  DEFAULT NULL            COMMENT '仓储费，单位：元',
    `truckage_price`  decimal(24,6)  DEFAULT NULL            COMMENT '搬运费，单位：元',
    `status`          int            NOT NULL DEFAULT 0      COMMENT '状态（CommonStatusEnum）',
    `default_status`  bit(1)         NOT NULL DEFAULT 0      COMMENT '是否默认',
    -- BaseDO
    `create_time`     datetime       NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time`     datetime       NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '最后更新时间',
    `creator`         varchar(64)    DEFAULT NULL            COMMENT '创建者',
    `updater`         varchar(64)    DEFAULT NULL            COMMENT '更新者',
    `deleted`         bit(1)         NOT NULL DEFAULT 0      COMMENT '逻辑删除',
    PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='ERP 仓库表';

-- 5.2 产品库存表（按产品+仓库维度）
DROP TABLE IF EXISTS `erp_stock`;
CREATE TABLE `erp_stock` (
    `id`           bigint         NOT NULL AUTO_INCREMENT COMMENT '编号',
    `tenant_id`   bigint       NOT NULL DEFAULT 1      COMMENT '租户编号',
    `product_id`   bigint         NOT NULL                COMMENT '产品编号（关联 erp_product.id）',
    `warehouse_id` bigint         NOT NULL                COMMENT '仓库编号（关联 erp_warehouse.id）',
    `count`        decimal(24,6)  NOT NULL DEFAULT 0      COMMENT '库存数量',
    -- BaseDO
    `create_time`  datetime       NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time`  datetime       NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '最后更新时间',
    `creator`      varchar(64)    DEFAULT NULL            COMMENT '创建者',
    `updater`      varchar(64)    DEFAULT NULL            COMMENT '更新者',
    `deleted`      bit(1)         NOT NULL DEFAULT 0      COMMENT '逻辑删除',
    PRIMARY KEY (`id`),
    UNIQUE INDEX `uk_product_warehouse` (`product_id`, `warehouse_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='ERP 产品库存表';

-- 5.3 产品库存明细表（出入库流水记录）
DROP TABLE IF EXISTS `erp_stock_record`;
CREATE TABLE `erp_stock_record` (
    `id`           bigint         NOT NULL AUTO_INCREMENT COMMENT '编号',
    `tenant_id`   bigint       NOT NULL DEFAULT 1      COMMENT '租户编号',
    `product_id`   bigint         NOT NULL                COMMENT '产品编号（关联 erp_product.id）',
    `warehouse_id` bigint         NOT NULL                COMMENT '仓库编号（关联 erp_warehouse.id）',
    `count`        decimal(24,6)  NOT NULL                COMMENT '出入库数量（正数=入库，负数=出库）',
    `total_count`  decimal(24,6)  NOT NULL                COMMENT '总库存量（出入库之后的库存）',
    `biz_type`     int            NOT NULL                COMMENT '业务类型（ErpStockRecordBizTypeEnum）',
    `biz_id`       bigint         NOT NULL                COMMENT '业务编号（关联对应业务单 id）',
    `biz_item_id`  bigint         NOT NULL                COMMENT '业务项编号（关联对应业务单项 id）',
    `biz_no`       varchar(64)    NOT NULL                COMMENT '业务单号',
    -- BaseDO
    `create_time`  datetime       NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time`  datetime       NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '最后更新时间',
    `creator`      varchar(64)    DEFAULT NULL            COMMENT '创建者',
    `updater`      varchar(64)    DEFAULT NULL            COMMENT '更新者',
    `deleted`      bit(1)         NOT NULL DEFAULT 0      COMMENT '逻辑删除',
    PRIMARY KEY (`id`),
    INDEX `idx_product_warehouse` (`product_id`, `warehouse_id`),
    INDEX `idx_biz` (`biz_type`, `biz_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='ERP 产品库存明细表';

-- 5.4 其它入库单表
DROP TABLE IF EXISTS `erp_stock_in`;
CREATE TABLE `erp_stock_in` (
    `id`          bigint         NOT NULL AUTO_INCREMENT COMMENT '入库编号',
    `tenant_id`   bigint       NOT NULL DEFAULT 1      COMMENT '租户编号',
    `no`          varchar(64)    NOT NULL                COMMENT '入库单号',
    `supplier_id` bigint         DEFAULT NULL            COMMENT '供应商编号（关联 erp_supplier.id）',
    `in_time`     datetime       NOT NULL                COMMENT '入库时间',
    `total_count` decimal(24,6)  NOT NULL DEFAULT 0      COMMENT '合计数量',
    `total_price` decimal(24,6)  NOT NULL DEFAULT 0      COMMENT '合计金额，单位：元',
    `status`      int            NOT NULL DEFAULT 0      COMMENT '状态（ErpAuditStatus）',
    `remark`      varchar(500)   DEFAULT NULL            COMMENT '备注',
    `file_url`    varchar(500)   DEFAULT NULL            COMMENT '附件 URL',
    -- BaseDO
    `create_time` datetime       NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time` datetime       NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '最后更新时间',
    `creator`     varchar(64)    DEFAULT NULL            COMMENT '创建者',
    `updater`     varchar(64)    DEFAULT NULL            COMMENT '更新者',
    `deleted`     bit(1)         NOT NULL DEFAULT 0      COMMENT '逻辑删除',
    PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='ERP 其它入库单表';

-- 5.5 其它入库单项表
DROP TABLE IF EXISTS `erp_stock_in_item`;
CREATE TABLE `erp_stock_in_item` (
    `id`              bigint         NOT NULL AUTO_INCREMENT COMMENT '编号',
    `tenant_id`   bigint       NOT NULL DEFAULT 1      COMMENT '租户编号',
    `in_id`           bigint         NOT NULL                COMMENT '入库单编号（关联 erp_stock_in.id）',
    `warehouse_id`    bigint         NOT NULL                COMMENT '仓库编号（关联 erp_warehouse.id）',
    `product_id`      bigint         NOT NULL                COMMENT '产品编号（关联 erp_product.id）',
    `product_unit_id` bigint         NOT NULL                COMMENT '产品单位编号',
    `product_price`   decimal(24,6)  NOT NULL                COMMENT '产品单价，单位：元',
    `count`           decimal(24,6)  NOT NULL                COMMENT '数量',
    `total_price`     decimal(24,6)  NOT NULL                COMMENT '总计价格，单位：元',
    `remark`          varchar(500)   DEFAULT NULL            COMMENT '备注',
    -- BaseDO
    `create_time`     datetime       NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time`     datetime       NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '最后更新时间',
    `creator`         varchar(64)    DEFAULT NULL            COMMENT '创建者',
    `updater`         varchar(64)    DEFAULT NULL            COMMENT '更新者',
    `deleted`         bit(1)         NOT NULL DEFAULT 0      COMMENT '逻辑删除',
    PRIMARY KEY (`id`),
    INDEX `idx_in_id` (`in_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='ERP 其它入库单项表';

-- 5.6 其它出库单表
DROP TABLE IF EXISTS `erp_stock_out`;
CREATE TABLE `erp_stock_out` (
    `id`          bigint         NOT NULL AUTO_INCREMENT COMMENT '出库编号',
    `tenant_id`   bigint       NOT NULL DEFAULT 1      COMMENT '租户编号',
    `no`          varchar(64)    NOT NULL                COMMENT '出库单号',
    `customer_id` bigint         DEFAULT NULL            COMMENT '客户编号（关联 erp_customer.id）',
    `out_time`    datetime       NOT NULL                COMMENT '出库时间',
    `total_count` decimal(24,6)  NOT NULL DEFAULT 0      COMMENT '合计数量',
    `total_price` decimal(24,6)  NOT NULL DEFAULT 0      COMMENT '合计金额，单位：元',
    `status`      int            NOT NULL DEFAULT 0      COMMENT '状态（ErpAuditStatus）',
    `remark`      varchar(500)   DEFAULT NULL            COMMENT '备注',
    `file_url`    varchar(500)   DEFAULT NULL            COMMENT '附件 URL',
    -- BaseDO
    `create_time` datetime       NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time` datetime       NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '最后更新时间',
    `creator`     varchar(64)    DEFAULT NULL            COMMENT '创建者',
    `updater`     varchar(64)    DEFAULT NULL            COMMENT '更新者',
    `deleted`     bit(1)         NOT NULL DEFAULT 0      COMMENT '逻辑删除',
    PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='ERP 其它出库单表';

-- 5.7 其它出库单项表
DROP TABLE IF EXISTS `erp_stock_out_item`;
CREATE TABLE `erp_stock_out_item` (
    `id`              bigint         NOT NULL AUTO_INCREMENT COMMENT '编号',
    `tenant_id`   bigint       NOT NULL DEFAULT 1      COMMENT '租户编号',
    `out_id`          bigint         NOT NULL                COMMENT '出库单编号（关联 erp_stock_out.id）',
    `warehouse_id`    bigint         NOT NULL                COMMENT '仓库编号（关联 erp_warehouse.id）',
    `product_id`      bigint         NOT NULL                COMMENT '产品编号（关联 erp_product.id）',
    `product_unit_id` bigint         NOT NULL                COMMENT '产品单位编号',
    `product_price`   decimal(24,6)  NOT NULL                COMMENT '产品单价，单位：元',
    `count`           decimal(24,6)  NOT NULL                COMMENT '数量',
    `total_price`     decimal(24,6)  NOT NULL                COMMENT '总计价格，单位：元',
    `remark`          varchar(500)   DEFAULT NULL            COMMENT '备注',
    -- BaseDO
    `create_time`     datetime       NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time`     datetime       NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '最后更新时间',
    `creator`         varchar(64)    DEFAULT NULL            COMMENT '创建者',
    `updater`         varchar(64)    DEFAULT NULL            COMMENT '更新者',
    `deleted`         bit(1)         NOT NULL DEFAULT 0      COMMENT '逻辑删除',
    PRIMARY KEY (`id`),
    INDEX `idx_out_id` (`out_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='ERP 其它出库单项表';

-- 5.8 库存调拨单表
DROP TABLE IF EXISTS `erp_stock_move`;
CREATE TABLE `erp_stock_move` (
    `id`          bigint         NOT NULL AUTO_INCREMENT COMMENT '调拨编号',
    `tenant_id`   bigint       NOT NULL DEFAULT 1      COMMENT '租户编号',
    `no`          varchar(64)    NOT NULL                COMMENT '调拨单号',
    `move_time`   datetime       NOT NULL                COMMENT '调拨时间',
    `total_count` decimal(24,6)  NOT NULL DEFAULT 0      COMMENT '合计数量',
    `total_price` decimal(24,6)  NOT NULL DEFAULT 0      COMMENT '合计金额，单位：元',
    `status`      int            NOT NULL DEFAULT 0      COMMENT '状态（ErpAuditStatus）',
    `remark`      varchar(500)   DEFAULT NULL            COMMENT '备注',
    `file_url`    varchar(500)   DEFAULT NULL            COMMENT '附件 URL',
    -- BaseDO
    `create_time` datetime       NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time` datetime       NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '最后更新时间',
    `creator`     varchar(64)    DEFAULT NULL            COMMENT '创建者',
    `updater`     varchar(64)    DEFAULT NULL            COMMENT '更新者',
    `deleted`     bit(1)         NOT NULL DEFAULT 0      COMMENT '逻辑删除',
    PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='ERP 库存调拨单表';

-- 5.9 库存调拨单项表
DROP TABLE IF EXISTS `erp_stock_move_item`;
CREATE TABLE `erp_stock_move_item` (
    `id`               bigint         NOT NULL AUTO_INCREMENT COMMENT '编号',
    `tenant_id`   bigint       NOT NULL DEFAULT 1      COMMENT '租户编号',
    `move_id`          bigint         NOT NULL                COMMENT '调拨单编号（关联 erp_stock_move.id）',
    `from_warehouse_id` bigint        NOT NULL                COMMENT '调出仓库编号（关联 erp_warehouse.id）',
    `to_warehouse_id`  bigint         NOT NULL                COMMENT '调入仓库编号（关联 erp_warehouse.id）',
    `product_id`       bigint         NOT NULL                COMMENT '产品编号（关联 erp_product.id）',
    `product_unit_id`  bigint         NOT NULL                COMMENT '产品单位编号',
    `product_price`    decimal(24,6)  NOT NULL                COMMENT '产品单价，单位：元',
    `count`            decimal(24,6)  NOT NULL                COMMENT '数量',
    `total_price`      decimal(24,6)  NOT NULL                COMMENT '总计价格，单位：元',
    `remark`           varchar(500)   DEFAULT NULL            COMMENT '备注',
    -- BaseDO
    `create_time`      datetime       NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time`      datetime       NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '最后更新时间',
    `creator`          varchar(64)    DEFAULT NULL            COMMENT '创建者',
    `updater`          varchar(64)    DEFAULT NULL            COMMENT '更新者',
    `deleted`          bit(1)         NOT NULL DEFAULT 0      COMMENT '逻辑删除',
    PRIMARY KEY (`id`),
    INDEX `idx_move_id` (`move_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='ERP 库存调拨单项表';

-- 5.10 库存盘点单表
DROP TABLE IF EXISTS `erp_stock_check`;
CREATE TABLE `erp_stock_check` (
    `id`          bigint         NOT NULL AUTO_INCREMENT COMMENT '盘点编号',
    `tenant_id`   bigint       NOT NULL DEFAULT 1      COMMENT '租户编号',
    `no`          varchar(64)    NOT NULL                COMMENT '盘点单号',
    `check_time`  datetime       NOT NULL                COMMENT '盘点时间',
    `total_count` decimal(24,6)  NOT NULL DEFAULT 0      COMMENT '合计数量',
    `total_price` decimal(24,6)  NOT NULL DEFAULT 0      COMMENT '合计金额，单位：元',
    `status`      int            NOT NULL DEFAULT 0      COMMENT '状态（ErpAuditStatus）',
    `remark`      varchar(500)   DEFAULT NULL            COMMENT '备注',
    `file_url`    varchar(500)   DEFAULT NULL            COMMENT '附件 URL',
    -- BaseDO
    `create_time` datetime       NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time` datetime       NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '最后更新时间',
    `creator`     varchar(64)    DEFAULT NULL            COMMENT '创建者',
    `updater`     varchar(64)    DEFAULT NULL            COMMENT '更新者',
    `deleted`     bit(1)         NOT NULL DEFAULT 0      COMMENT '逻辑删除',
    PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='ERP 库存盘点单表';

-- 5.11 库存盘点单项表
DROP TABLE IF EXISTS `erp_stock_check_item`;
CREATE TABLE `erp_stock_check_item` (
    `id`              bigint         NOT NULL AUTO_INCREMENT COMMENT '编号',
    `tenant_id`   bigint       NOT NULL DEFAULT 1      COMMENT '租户编号',
    `check_id`        bigint         NOT NULL                COMMENT '盘点单编号（关联 erp_stock_check.id）',
    `warehouse_id`    bigint         NOT NULL                COMMENT '仓库编号（关联 erp_warehouse.id）',
    `product_id`      bigint         NOT NULL                COMMENT '产品编号（关联 erp_product.id）',
    `product_unit_id` bigint         NOT NULL                COMMENT '产品单位编号',
    `product_price`   decimal(24,6)  NOT NULL                COMMENT '产品单价，单位：元',
    `stock_count`     decimal(24,6)  NOT NULL                COMMENT '账面数量（系统库存）',
    `actual_count`    decimal(24,6)  NOT NULL                COMMENT '实际数量（盘点数量）',
    `count`           decimal(24,6)  NOT NULL                COMMENT '盈亏数量（正=盘盈，负=盘亏）',
    `total_price`     decimal(24,6)  NOT NULL                COMMENT '盈亏金额，单位：元',
    `remark`          varchar(500)   DEFAULT NULL            COMMENT '备注',
    -- BaseDO
    `create_time`     datetime       NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time`     datetime       NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '最后更新时间',
    `creator`         varchar(64)    DEFAULT NULL            COMMENT '创建者',
    `updater`         varchar(64)    DEFAULT NULL            COMMENT '更新者',
    `deleted`         bit(1)         NOT NULL DEFAULT 0      COMMENT '逻辑删除',
    PRIMARY KEY (`id`),
    INDEX `idx_check_id` (`check_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='ERP 库存盘点单项表';

-- ======================================================================================
-- 六、财务域（5 表）
-- ======================================================================================

-- 6.1 结算账户表
DROP TABLE IF EXISTS `erp_account`;
CREATE TABLE `erp_account` (
    `id`             bigint       NOT NULL AUTO_INCREMENT COMMENT '结算账户编号',
    `tenant_id`   bigint       NOT NULL DEFAULT 1      COMMENT '租户编号',
    `name`           varchar(100) NOT NULL                COMMENT '账户名称',
    `no`             varchar(64)  NOT NULL                COMMENT '账户编码',
    `remark`         varchar(500) DEFAULT NULL            COMMENT '备注',
    `status`         int          NOT NULL DEFAULT 0      COMMENT '状态（CommonStatusEnum）',
    `sort`           int          DEFAULT NULL            COMMENT '排序',
    `default_status` bit(1)       NOT NULL DEFAULT 0      COMMENT '是否默认',
    -- BaseDO
    `create_time`    datetime     NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time`    datetime     NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '最后更新时间',
    `creator`        varchar(64)  DEFAULT NULL            COMMENT '创建者',
    `updater`        varchar(64)  DEFAULT NULL            COMMENT '更新者',
    `deleted`        bit(1)       NOT NULL DEFAULT 0      COMMENT '逻辑删除',
    PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='ERP 结算账户表';

-- 6.2 收款单表
DROP TABLE IF EXISTS `erp_finance_receipt`;
CREATE TABLE `erp_finance_receipt` (
    `id`             bigint         NOT NULL AUTO_INCREMENT COMMENT '编号',
    `tenant_id`   bigint       NOT NULL DEFAULT 1      COMMENT '租户编号',
    `no`             varchar(64)    NOT NULL                COMMENT '收款单号',
    `status`         int            NOT NULL DEFAULT 0      COMMENT '状态（ErpAuditStatus）',
    `receipt_time`   datetime       NOT NULL                COMMENT '收款时间',
    `finance_user_id` bigint        DEFAULT NULL            COMMENT '财务人员编号（关联 AdminUserDO.id）',
    `customer_id`    bigint         NOT NULL                COMMENT '客户编号（关联 erp_customer.id）',
    `account_id`     bigint         NOT NULL                COMMENT '收款账户编号（关联 erp_account.id）',
    `total_price`    decimal(24,6)  NOT NULL                COMMENT '合计金额，单位：元',
    `discount_price` decimal(24,6)  NOT NULL DEFAULT 0      COMMENT '优惠金额，单位：元',
    `receipt_price`  decimal(24,6)  NOT NULL                COMMENT '实收金额，单位：元（receiptPrice = totalPrice - discountPrice）',
    `remark`         varchar(500)   DEFAULT NULL            COMMENT '备注',
    -- BaseDO
    `create_time`    datetime       NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time`    datetime       NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '最后更新时间',
    `creator`        varchar(64)    DEFAULT NULL            COMMENT '创建者',
    `updater`        varchar(64)    DEFAULT NULL            COMMENT '更新者',
    `deleted`        bit(1)         NOT NULL DEFAULT 0      COMMENT '逻辑删除',
    PRIMARY KEY (`id`),
    INDEX `idx_customer_id` (`customer_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='ERP 收款单表';

-- 6.3 收款单项表
DROP TABLE IF EXISTS `erp_finance_receipt_item`;
CREATE TABLE `erp_finance_receipt_item` (
    `id`             bigint         NOT NULL AUTO_INCREMENT COMMENT '编号',
    `tenant_id`   bigint       NOT NULL DEFAULT 1      COMMENT '租户编号',
    `receipt_id`     bigint         NOT NULL                COMMENT '收款单编号（关联 erp_finance_receipt.id）',
    `biz_type`       int            NOT NULL                COMMENT '业务类型（ErpBizTypeEnum: 销售出库/退货）',
    `biz_id`         bigint         NOT NULL                COMMENT '业务编号（关联 erp_sale_out.id / erp_sale_return.id）',
    `biz_no`         varchar(64)    NOT NULL                COMMENT '业务单号',
    `total_price`    decimal(24,6)  NOT NULL                COMMENT '应收金额，单位：元',
    `receipted_price` decimal(24,6) NOT NULL DEFAULT 0      COMMENT '已收金额，单位：元',
    `receipt_price`  decimal(24,6)  NOT NULL                COMMENT '本次收款金额，单位：元',
    `remark`         varchar(500)   DEFAULT NULL            COMMENT '备注',
    -- BaseDO
    `create_time`    datetime       NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time`    datetime       NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '最后更新时间',
    `creator`        varchar(64)    DEFAULT NULL            COMMENT '创建者',
    `updater`        varchar(64)    DEFAULT NULL            COMMENT '更新者',
    `deleted`        bit(1)         NOT NULL DEFAULT 0      COMMENT '逻辑删除',
    PRIMARY KEY (`id`),
    INDEX `idx_receipt_id` (`receipt_id`),
    INDEX `idx_biz` (`biz_type`, `biz_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='ERP 收款单项表';

-- 6.4 付款单表
DROP TABLE IF EXISTS `erp_finance_payment`;
CREATE TABLE `erp_finance_payment` (
    `id`              bigint         NOT NULL AUTO_INCREMENT COMMENT '编号',
    `tenant_id`   bigint       NOT NULL DEFAULT 1      COMMENT '租户编号',
    `no`              varchar(64)    NOT NULL                COMMENT '付款单号',
    `status`          int            NOT NULL DEFAULT 0      COMMENT '状态（ErpAuditStatus）',
    `payment_time`    datetime       NOT NULL                COMMENT '付款时间',
    `finance_user_id` bigint         DEFAULT NULL            COMMENT '财务人员编号（关联 AdminUserDO.id）',
    `supplier_id`     bigint         NOT NULL                COMMENT '供应商编号（关联 erp_supplier.id）',
    `account_id`      bigint         NOT NULL                COMMENT '付款账户编号（关联 erp_account.id）',
    `total_price`     decimal(24,6)  NOT NULL                COMMENT '合计金额，单位：元',
    `discount_price`  decimal(24,6)  NOT NULL DEFAULT 0      COMMENT '优惠金额，单位：元',
    `payment_price`   decimal(24,6)  NOT NULL                COMMENT '实付金额，单位：元',
    `remark`          varchar(500)   DEFAULT NULL            COMMENT '备注',
    -- BaseDO
    `create_time`     datetime       NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time`     datetime       NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '最后更新时间',
    `creator`         varchar(64)    DEFAULT NULL            COMMENT '创建者',
    `updater`         varchar(64)    DEFAULT NULL            COMMENT '更新者',
    `deleted`         bit(1)         NOT NULL DEFAULT 0      COMMENT '逻辑删除',
    PRIMARY KEY (`id`),
    INDEX `idx_supplier_id` (`supplier_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='ERP 付款单表';

-- 6.5 付款单项表
DROP TABLE IF EXISTS `erp_finance_payment_item`;
CREATE TABLE `erp_finance_payment_item` (
    `id`            bigint         NOT NULL AUTO_INCREMENT COMMENT '编号',
    `tenant_id`   bigint       NOT NULL DEFAULT 1      COMMENT '租户编号',
    `payment_id`    bigint         NOT NULL                COMMENT '付款单编号（关联 erp_finance_payment.id）',
    `biz_type`      int            NOT NULL                COMMENT '业务类型（ErpBizTypeEnum: 采购入库/退货）',
    `biz_id`        bigint         NOT NULL                COMMENT '业务编号（关联 erp_purchase_in.id / erp_purchase_return.id）',
    `biz_no`        varchar(64)    NOT NULL                COMMENT '业务单号',
    `total_price`   decimal(24,6)  NOT NULL                COMMENT '应付金额，单位：元',
    `paid_price`    decimal(24,6)  NOT NULL DEFAULT 0      COMMENT '已付金额，单位：元',
    `payment_price` decimal(24,6)  NOT NULL                COMMENT '本次付款金额，单位：元',
    `remark`        varchar(500)   DEFAULT NULL            COMMENT '备注',
    -- BaseDO
    `create_time`   datetime       NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time`   datetime       NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '最后更新时间',
    `creator`       varchar(64)    DEFAULT NULL            COMMENT '创建者',
    `updater`       varchar(64)    DEFAULT NULL            COMMENT '更新者',
    `deleted`       bit(1)         NOT NULL DEFAULT 0      COMMENT '逻辑删除',
    PRIMARY KEY (`id`),
    INDEX `idx_payment_id` (`payment_id`),
    INDEX `idx_biz` (`biz_type`, `biz_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='ERP 付款单项表';
