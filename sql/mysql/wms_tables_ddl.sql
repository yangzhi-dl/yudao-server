-- ======================================================================================
-- WMS 仓储模块 - 完整表结构 DDL（逆向自 16 个 DO 实体类）
-- 数据库类型: MySQL 5.7+ / 8.0
-- ======================================================================================

-- ======================================================================================
-- 一、基础数据域 - 商品（4 表）
-- ======================================================================================

-- 1.1 商品分类表
DROP TABLE IF EXISTS `wms_item_category`;
CREATE TABLE `wms_item_category` (
    `id`          bigint       NOT NULL AUTO_INCREMENT COMMENT '主键编号',
    `tenant_id`   bigint       NOT NULL DEFAULT 1      COMMENT '租户编号',
    `parent_id`   bigint       NOT NULL DEFAULT 0      COMMENT '父级编号（0 表示根节点）',
    `code`        varchar(64)  NOT NULL                COMMENT '分类编号',
    `name`        varchar(100) NOT NULL                COMMENT '分类名称',
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
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='WMS 商品分类表';

-- 1.2 商品品牌表
DROP TABLE IF EXISTS `wms_item_brand`;
CREATE TABLE `wms_item_brand` (
    `id`          bigint       NOT NULL AUTO_INCREMENT COMMENT '主键编号',
    `tenant_id`   bigint       NOT NULL DEFAULT 1      COMMENT '租户编号',
    `code`        varchar(64)  NOT NULL                COMMENT '品牌编号',
    `name`        varchar(100) NOT NULL                COMMENT '品牌名称',
    -- BaseDO
    `create_time` datetime     NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time` datetime     NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '最后更新时间',
    `creator`     varchar(64)  DEFAULT NULL            COMMENT '创建者',
    `updater`     varchar(64)  DEFAULT NULL            COMMENT '更新者',
    `deleted`     bit(1)       NOT NULL DEFAULT 0      COMMENT '逻辑删除',
    PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='WMS 商品品牌表';

-- 1.3 商品表
DROP TABLE IF EXISTS `wms_item`;
CREATE TABLE `wms_item` (
    `id`          bigint       NOT NULL AUTO_INCREMENT COMMENT '主键编号',
    `tenant_id`   bigint       NOT NULL DEFAULT 1      COMMENT '租户编号',
    `code`        varchar(64)  NOT NULL                COMMENT '商品编号',
    `name`        varchar(100) NOT NULL                COMMENT '商品名称',
    `unit`        varchar(20)  DEFAULT NULL            COMMENT '单位',
    `category_id` bigint       DEFAULT NULL            COMMENT '商品分类编号（关联 wms_item_category.id）',
    `brand_id`    bigint       DEFAULT NULL            COMMENT '商品品牌编号（关联 wms_item_brand.id）',
    `remark`      varchar(500) DEFAULT NULL            COMMENT '备注',
    -- BaseDO
    `create_time` datetime     NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time` datetime     NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '最后更新时间',
    `creator`     varchar(64)  DEFAULT NULL            COMMENT '创建者',
    `updater`     varchar(64)  DEFAULT NULL            COMMENT '更新者',
    `deleted`     bit(1)       NOT NULL DEFAULT 0      COMMENT '逻辑删除',
    PRIMARY KEY (`id`),
    INDEX `idx_category_id` (`category_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='WMS 商品表';

-- 1.4 商品 SKU 表
DROP TABLE IF EXISTS `wms_item_sku`;
CREATE TABLE `wms_item_sku` (
    `id`            bigint         NOT NULL AUTO_INCREMENT COMMENT '主键编号',
    `tenant_id`     bigint         NOT NULL DEFAULT 1      COMMENT '租户编号',
    `name`          varchar(100)   NOT NULL                COMMENT '规格名称',
    `item_id`       bigint         NOT NULL                COMMENT '商品编号（关联 wms_item.id）',
    `bar_code`      varchar(64)    DEFAULT NULL            COMMENT '条码',
    `code`          varchar(64)    DEFAULT NULL            COMMENT '规格编号',
    `length`        decimal(10,2)  DEFAULT NULL            COMMENT '长，单位 cm',
    `width`         decimal(10,2)  DEFAULT NULL            COMMENT '宽，单位 cm',
    `height`        decimal(10,2)  DEFAULT NULL            COMMENT '高，单位 cm',
    `gross_weight`  decimal(10,2)  DEFAULT NULL            COMMENT '毛重，单位 kg',
    `net_weight`    decimal(10,2)  DEFAULT NULL            COMMENT '净重，单位 kg',
    `cost_price`    decimal(24,6)  DEFAULT NULL            COMMENT '成本价，单位：元',
    `selling_price` decimal(24,6)  DEFAULT NULL            COMMENT '销售价，单位：元',
    -- BaseDO
    `create_time`   datetime       NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time`   datetime       NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '最后更新时间',
    `creator`       varchar(64)    DEFAULT NULL            COMMENT '创建者',
    `updater`       varchar(64)    DEFAULT NULL            COMMENT '更新者',
    `deleted`       bit(1)         NOT NULL DEFAULT 0      COMMENT '逻辑删除',
    PRIMARY KEY (`id`),
    INDEX `idx_item_id` (`item_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='WMS 商品 SKU 表';

-- ======================================================================================
-- 二、基础数据域 - 往来企业（1 表）
-- ======================================================================================

-- 2.1 往来企业表（供应商/客户）
DROP TABLE IF EXISTS `wms_merchant`;
CREATE TABLE `wms_merchant` (
    `id`           bigint       NOT NULL AUTO_INCREMENT COMMENT '主键编号',
    `tenant_id`    bigint       NOT NULL DEFAULT 1      COMMENT '租户编号',
    `code`         varchar(64)  NOT NULL                COMMENT '往来企业编号',
    `name`         varchar(100) NOT NULL                COMMENT '往来企业名称',
    `type`         int          NOT NULL                COMMENT '往来企业类型（WmsMerchantTypeEnum）',
    `level`        varchar(20)  DEFAULT NULL            COMMENT '级别',
    `bank_name`    varchar(100) DEFAULT NULL            COMMENT '开户行',
    `bank_account` varchar(50)  DEFAULT NULL            COMMENT '银行账户',
    `address`      varchar(255) DEFAULT NULL            COMMENT '地址',
    `mobile`       varchar(20)  DEFAULT NULL            COMMENT '手机号',
    `telephone`    varchar(20)  DEFAULT NULL            COMMENT '座机号',
    `contact`      varchar(50)  DEFAULT NULL            COMMENT '联系人',
    `email`        varchar(100) DEFAULT NULL            COMMENT 'Email',
    `remark`       varchar(500) DEFAULT NULL            COMMENT '备注',
    -- BaseDO
    `create_time`  datetime     NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time`  datetime     NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '最后更新时间',
    `creator`      varchar(64)  DEFAULT NULL            COMMENT '创建者',
    `updater`      varchar(64)  DEFAULT NULL            COMMENT '更新者',
    `deleted`      bit(1)       NOT NULL DEFAULT 0      COMMENT '逻辑删除',
    PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='WMS 往来企业表';

-- ======================================================================================
-- 三、基础数据域 - 仓库（1 表）
-- ======================================================================================

-- 3.1 仓库表
DROP TABLE IF EXISTS `wms_warehouse`;
CREATE TABLE `wms_warehouse` (
    `id`          bigint       NOT NULL AUTO_INCREMENT COMMENT '主键编号',
    `tenant_id`   bigint       NOT NULL DEFAULT 1      COMMENT '租户编号',
    `code`        varchar(64)  NOT NULL                COMMENT '仓库编号',
    `name`        varchar(100) NOT NULL                COMMENT '仓库名称',
    `remark`      varchar(500) DEFAULT NULL            COMMENT '备注',
    `sort`        int          NOT NULL DEFAULT 0      COMMENT '排序',
    -- BaseDO
    `create_time` datetime     NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time` datetime     NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '最后更新时间',
    `creator`     varchar(64)  DEFAULT NULL            COMMENT '创建者',
    `updater`     varchar(64)  DEFAULT NULL            COMMENT '更新者',
    `deleted`     bit(1)       NOT NULL DEFAULT 0      COMMENT '逻辑删除',
    PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='WMS 仓库表';

-- ======================================================================================
-- 四、库存域（2 表）
-- ======================================================================================

-- 4.1 库存表（按 SKU + 仓库维度）
DROP TABLE IF EXISTS `wms_inventory`;
CREATE TABLE `wms_inventory` (
    `id`           bigint         NOT NULL AUTO_INCREMENT COMMENT '主键编号',
    `tenant_id`    bigint         NOT NULL DEFAULT 1      COMMENT '租户编号',
    `sku_id`       bigint         NOT NULL                COMMENT '商品 SKU 编号（关联 wms_item_sku.id）',
    `warehouse_id` bigint         NOT NULL                COMMENT '仓库编号（关联 wms_warehouse.id）',
    `quantity`     decimal(24,6)  NOT NULL DEFAULT 0      COMMENT '库存数量',
    `remark`       varchar(500)   DEFAULT NULL            COMMENT '备注',
    -- BaseDO
    `create_time`  datetime       NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time`  datetime       NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '最后更新时间',
    `creator`      varchar(64)    DEFAULT NULL            COMMENT '创建者',
    `updater`      varchar(64)    DEFAULT NULL            COMMENT '更新者',
    `deleted`      bit(1)         NOT NULL DEFAULT 0      COMMENT '逻辑删除',
    PRIMARY KEY (`id`),
    UNIQUE INDEX `uk_sku_warehouse` (`sku_id`, `warehouse_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='WMS 库存表';

-- 4.2 库存流水表
DROP TABLE IF EXISTS `wms_inventory_history`;
CREATE TABLE `wms_inventory_history` (
    `id`             bigint         NOT NULL AUTO_INCREMENT COMMENT '主键编号',
    `tenant_id`      bigint         NOT NULL DEFAULT 1      COMMENT '租户编号',
    `warehouse_id`   bigint         NOT NULL                COMMENT '仓库编号（关联 wms_warehouse.id）',
    `sku_id`         bigint         NOT NULL                COMMENT '商品 SKU 编号（关联 wms_item_sku.id）',
    `quantity`       decimal(24,6)  NOT NULL                COMMENT '库存变化数量',
    `before_quantity` decimal(24,6) NOT NULL                COMMENT '变化前库存数量',
    `after_quantity`  decimal(24,6) NOT NULL                COMMENT '变化后库存数量',
    `price`          decimal(24,6)  DEFAULT NULL            COMMENT '单价',
    `total_price`    decimal(24,6)  DEFAULT NULL            COMMENT '库存变化金额',
    `remark`         varchar(500)   DEFAULT NULL            COMMENT '备注',
    `order_id`       bigint         NOT NULL                COMMENT '单据编号',
    `order_no`       varchar(64)    NOT NULL                COMMENT '单据号',
    `order_type`     int            NOT NULL                COMMENT '单据类型（WmsOrderTypeEnum）',
    -- BaseDO
    `create_time`    datetime       NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time`    datetime       NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '最后更新时间',
    `creator`        varchar(64)    DEFAULT NULL            COMMENT '创建者',
    `updater`        varchar(64)    DEFAULT NULL            COMMENT '更新者',
    `deleted`        bit(1)         NOT NULL DEFAULT 0      COMMENT '逻辑删除',
    PRIMARY KEY (`id`),
    INDEX `idx_sku_warehouse` (`sku_id`, `warehouse_id`),
    INDEX `idx_order` (`order_id`, `order_type`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='WMS 库存流水表';

-- ======================================================================================
-- 五、入库单域（2 表）
-- ======================================================================================

-- 5.1 入库单表
DROP TABLE IF EXISTS `wms_receipt_order`;
CREATE TABLE `wms_receipt_order` (
    `id`             bigint         NOT NULL AUTO_INCREMENT COMMENT '主键编号',
    `tenant_id`      bigint         NOT NULL DEFAULT 1      COMMENT '租户编号',
    `no`             varchar(64)    NOT NULL                COMMENT '入库单号',
    `type`           int            NOT NULL                COMMENT '入库类型（WmsReceiptOrderTypeEnum：采购入库/退货入库/调拨入库/其它）',
    `order_time`     datetime       NOT NULL                COMMENT '单据日期',
    `status`         int            NOT NULL DEFAULT 0      COMMENT '入库状态（ORDER_STATUS 字典）',
    `biz_order_no`   varchar(64)    DEFAULT NULL            COMMENT '业务订单号',
    `merchant_id`    bigint         DEFAULT NULL            COMMENT '供应商编号（关联 wms_merchant.id）',
    `remark`         varchar(500)   DEFAULT NULL            COMMENT '备注',
    `warehouse_id`   bigint         NOT NULL                COMMENT '仓库编号（关联 wms_warehouse.id）',
    `total_quantity` decimal(24,6)  NOT NULL DEFAULT 0      COMMENT '总数量',
    `total_price`    decimal(24,6)  NOT NULL DEFAULT 0      COMMENT '总金额',
    -- BaseDO
    `create_time`    datetime       NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time`    datetime       NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '最后更新时间',
    `creator`        varchar(64)    DEFAULT NULL            COMMENT '创建者',
    `updater`        varchar(64)    DEFAULT NULL            COMMENT '更新者',
    `deleted`        bit(1)         NOT NULL DEFAULT 0      COMMENT '逻辑删除',
    PRIMARY KEY (`id`),
    INDEX `idx_warehouse_id` (`warehouse_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='WMS 入库单表';

-- 5.2 入库单明细表
DROP TABLE IF EXISTS `wms_receipt_order_detail`;
CREATE TABLE `wms_receipt_order_detail` (
    `id`           bigint         NOT NULL AUTO_INCREMENT COMMENT '主键编号',
    `tenant_id`    bigint         NOT NULL DEFAULT 1      COMMENT '租户编号',
    `order_id`     bigint         NOT NULL                COMMENT '入库单编号（关联 wms_receipt_order.id）',
    `sku_id`       bigint         NOT NULL                COMMENT '商品 SKU 编号（关联 wms_item_sku.id）',
    `warehouse_id` bigint         NOT NULL                COMMENT '仓库编号（关联 wms_warehouse.id）',
    `quantity`     decimal(24,6)  NOT NULL                COMMENT '入库数量',
    `price`        decimal(24,6)  NOT NULL                COMMENT '单价',
    `total_price`  decimal(24,6)  NOT NULL                COMMENT '行金额（数量 * 单价）',
    -- BaseDO
    `create_time`  datetime       NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time`  datetime       NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '最后更新时间',
    `creator`      varchar(64)    DEFAULT NULL            COMMENT '创建者',
    `updater`      varchar(64)    DEFAULT NULL            COMMENT '更新者',
    `deleted`      bit(1)         NOT NULL DEFAULT 0      COMMENT '逻辑删除',
    PRIMARY KEY (`id`),
    INDEX `idx_order_id` (`order_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='WMS 入库单明细表';

-- ======================================================================================
-- 六、出库单域（2 表）
-- ======================================================================================

-- 6.1 出库单表
DROP TABLE IF EXISTS `wms_shipment_order`;
CREATE TABLE `wms_shipment_order` (
    `id`             bigint         NOT NULL AUTO_INCREMENT COMMENT '主键编号',
    `tenant_id`      bigint         NOT NULL DEFAULT 1      COMMENT '租户编号',
    `no`             varchar(64)    NOT NULL                COMMENT '出库单号',
    `type`           int            NOT NULL                COMMENT '出库类型（WmsShipmentOrderTypeEnum：销售出库/退货出库/调拨出库/其它）',
    `order_time`     datetime       NOT NULL                COMMENT '单据日期',
    `status`         int            NOT NULL DEFAULT 0      COMMENT '出库状态（ORDER_STATUS 字典）',
    `biz_order_no`   varchar(64)    DEFAULT NULL            COMMENT '业务订单号',
    `merchant_id`    bigint         DEFAULT NULL            COMMENT '客户编号（关联 wms_merchant.id）',
    `remark`         varchar(500)   DEFAULT NULL            COMMENT '备注',
    `warehouse_id`   bigint         NOT NULL                COMMENT '仓库编号（关联 wms_warehouse.id）',
    `total_quantity` decimal(24,6)  NOT NULL DEFAULT 0      COMMENT '总数量',
    `total_price`    decimal(24,6)  NOT NULL DEFAULT 0      COMMENT '总金额',
    -- BaseDO
    `create_time`    datetime       NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time`    datetime       NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '最后更新时间',
    `creator`        varchar(64)    DEFAULT NULL            COMMENT '创建者',
    `updater`        varchar(64)    DEFAULT NULL            COMMENT '更新者',
    `deleted`        bit(1)         NOT NULL DEFAULT 0      COMMENT '逻辑删除',
    PRIMARY KEY (`id`),
    INDEX `idx_warehouse_id` (`warehouse_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='WMS 出库单表';

-- 6.2 出库单明细表
DROP TABLE IF EXISTS `wms_shipment_order_detail`;
CREATE TABLE `wms_shipment_order_detail` (
    `id`           bigint         NOT NULL AUTO_INCREMENT COMMENT '主键编号',
    `tenant_id`    bigint         NOT NULL DEFAULT 1      COMMENT '租户编号',
    `order_id`     bigint         NOT NULL                COMMENT '出库单编号（关联 wms_shipment_order.id）',
    `sku_id`       bigint         NOT NULL                COMMENT '商品 SKU 编号（关联 wms_item_sku.id）',
    `warehouse_id` bigint         NOT NULL                COMMENT '仓库编号（关联 wms_warehouse.id）',
    `quantity`     decimal(24,6)  NOT NULL                COMMENT '出库数量',
    `price`        decimal(24,6)  NOT NULL                COMMENT '单价',
    `total_price`  decimal(24,6)  NOT NULL                COMMENT '行金额（数量 * 单价）',
    -- BaseDO
    `create_time`  datetime       NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time`  datetime       NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '最后更新时间',
    `creator`      varchar(64)    DEFAULT NULL            COMMENT '创建者',
    `updater`      varchar(64)    DEFAULT NULL            COMMENT '更新者',
    `deleted`      bit(1)         NOT NULL DEFAULT 0      COMMENT '逻辑删除',
    PRIMARY KEY (`id`),
    INDEX `idx_order_id` (`order_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='WMS 出库单明细表';

-- ======================================================================================
-- 七、移库单域（2 表）
-- ======================================================================================

-- 7.1 移库单表
DROP TABLE IF EXISTS `wms_movement_order`;
CREATE TABLE `wms_movement_order` (
    `id`                  bigint         NOT NULL AUTO_INCREMENT COMMENT '主键编号',
    `tenant_id`           bigint         NOT NULL DEFAULT 1      COMMENT '租户编号',
    `no`                  varchar(64)    NOT NULL                COMMENT '移库单号',
    `order_time`          datetime       NOT NULL                COMMENT '单据日期',
    `status`              int            NOT NULL DEFAULT 0      COMMENT '移库状态（ORDER_STATUS 字典）',
    `remark`              varchar(500)   DEFAULT NULL            COMMENT '备注',
    `source_warehouse_id` bigint         NOT NULL                COMMENT '来源仓库编号（关联 wms_warehouse.id）',
    `target_warehouse_id` bigint         NOT NULL                COMMENT '目标仓库编号（关联 wms_warehouse.id）',
    `total_quantity`      decimal(24,6)  NOT NULL DEFAULT 0      COMMENT '总数量',
    `total_price`         decimal(24,6)  NOT NULL DEFAULT 0      COMMENT '总金额',
    -- BaseDO
    `create_time`         datetime       NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time`         datetime       NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '最后更新时间',
    `creator`             varchar(64)    DEFAULT NULL            COMMENT '创建者',
    `updater`             varchar(64)    DEFAULT NULL            COMMENT '更新者',
    `deleted`             bit(1)         NOT NULL DEFAULT 0      COMMENT '逻辑删除',
    PRIMARY KEY (`id`),
    INDEX `idx_source_warehouse_id` (`source_warehouse_id`),
    INDEX `idx_target_warehouse_id` (`target_warehouse_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='WMS 移库单表';

-- 7.2 移库单明细表
DROP TABLE IF EXISTS `wms_movement_order_detail`;
CREATE TABLE `wms_movement_order_detail` (
    `id`                  bigint         NOT NULL AUTO_INCREMENT COMMENT '主键编号',
    `tenant_id`           bigint         NOT NULL DEFAULT 1      COMMENT '租户编号',
    `order_id`            bigint         NOT NULL                COMMENT '移库单编号（关联 wms_movement_order.id）',
    `sku_id`              bigint         NOT NULL                COMMENT '商品 SKU 编号（关联 wms_item_sku.id）',
    `source_warehouse_id` bigint         NOT NULL                COMMENT '来源仓库编号（关联 wms_warehouse.id）',
    `target_warehouse_id` bigint         NOT NULL                COMMENT '目标仓库编号（关联 wms_warehouse.id）',
    `quantity`            decimal(24,6)  NOT NULL                COMMENT '移库数量',
    `price`               decimal(24,6)  NOT NULL                COMMENT '单价',
    `total_price`         decimal(24,6)  NOT NULL                COMMENT '行金额（数量 * 单价）',
    -- BaseDO
    `create_time`         datetime       NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time`         datetime       NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '最后更新时间',
    `creator`             varchar(64)    DEFAULT NULL            COMMENT '创建者',
    `updater`             varchar(64)    DEFAULT NULL            COMMENT '更新者',
    `deleted`             bit(1)         NOT NULL DEFAULT 0      COMMENT '逻辑删除',
    PRIMARY KEY (`id`),
    INDEX `idx_order_id` (`order_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='WMS 移库单明细表';

-- ======================================================================================
-- 八、盘库单域（2 表）
-- ======================================================================================

-- 8.1 盘库单表
DROP TABLE IF EXISTS `wms_check_order`;
CREATE TABLE `wms_check_order` (
    `id`             bigint         NOT NULL AUTO_INCREMENT COMMENT '主键编号',
    `tenant_id`      bigint         NOT NULL DEFAULT 1      COMMENT '租户编号',
    `no`             varchar(64)    NOT NULL                COMMENT '盘库单号',
    `order_time`     datetime       NOT NULL                COMMENT '单据日期',
    `status`         int            NOT NULL DEFAULT 0      COMMENT '盘库状态（ORDER_STATUS 字典）',
    `remark`         varchar(500)   DEFAULT NULL            COMMENT '备注',
    `warehouse_id`   bigint         NOT NULL                COMMENT '仓库编号（关联 wms_warehouse.id）',
    `total_quantity` decimal(24,6)  NOT NULL DEFAULT 0      COMMENT '盈亏数量（实盘数量 - 账面数量）',
    `total_price`    decimal(24,6)  NOT NULL DEFAULT 0      COMMENT '账面总金额（账面数量 * 单价）',
    `actual_price`   decimal(24,6)  NOT NULL DEFAULT 0      COMMENT '实际总金额（实盘数量 * 单价）',
    -- BaseDO
    `create_time`    datetime       NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time`    datetime       NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '最后更新时间',
    `creator`        varchar(64)    DEFAULT NULL            COMMENT '创建者',
    `updater`        varchar(64)    DEFAULT NULL            COMMENT '更新者',
    `deleted`        bit(1)         NOT NULL DEFAULT 0      COMMENT '逻辑删除',
    PRIMARY KEY (`id`),
    INDEX `idx_warehouse_id` (`warehouse_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='WMS 盘库单表';

-- 8.2 盘库单明细表
DROP TABLE IF EXISTS `wms_check_order_detail`;
CREATE TABLE `wms_check_order_detail` (
    `id`             bigint         NOT NULL AUTO_INCREMENT COMMENT '主键编号',
    `tenant_id`      bigint         NOT NULL DEFAULT 1      COMMENT '租户编号',
    `order_id`       bigint         NOT NULL                COMMENT '盘库单编号（关联 wms_check_order.id）',
    `sku_id`         bigint         NOT NULL                COMMENT '商品 SKU 编号（关联 wms_item_sku.id）',
    `warehouse_id`   bigint         NOT NULL                COMMENT '仓库编号（关联 wms_warehouse.id）',
    `inventory_id`   bigint         DEFAULT NULL            COMMENT '库存编号（关联 wms_inventory.id）',
    `receipt_time`   datetime       DEFAULT NULL            COMMENT '入库时间',
    `quantity`       decimal(24,6)  NOT NULL                COMMENT '账面数量',
    `check_quantity` decimal(24,6)  NOT NULL                COMMENT '实盘数量',
    `price`          decimal(24,6)  NOT NULL                COMMENT '单价',
    -- BaseDO
    `create_time`    datetime       NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time`    datetime       NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '最后更新时间',
    `creator`        varchar(64)    DEFAULT NULL            COMMENT '创建者',
    `updater`        varchar(64)    DEFAULT NULL            COMMENT '更新者',
    `deleted`        bit(1)         NOT NULL DEFAULT 0      COMMENT '逻辑删除',
    PRIMARY KEY (`id`),
    INDEX `idx_order_id` (`order_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='WMS 盘库单明细表';
