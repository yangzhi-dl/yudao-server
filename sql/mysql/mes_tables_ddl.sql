-- ======================================================================================
-- MES 制造执行系统 - 完整表结构 DDL（逆向自 114 个 DO 实体类）
-- 数据库类型: MySQL 5.7+ / 8.0
-- ======================================================================================

-- ======================================================================================
-- 一、MD - 主数据（17 表）
-- ======================================================================================

-- 1.1 物料分类表
DROP TABLE IF EXISTS `mes_md_item_type`;
CREATE TABLE `mes_md_item_type` (
    `id`             bigint       NOT NULL AUTO_INCREMENT COMMENT '主键编号',
    `tenant_id`      bigint       NOT NULL DEFAULT 1      COMMENT '租户编号',
    `code`           varchar(64)  NOT NULL                COMMENT '分类编号',
    `name`           varchar(100) NOT NULL                COMMENT '分类名称',
    `parent_id`      bigint       NOT NULL DEFAULT 0      COMMENT '父级编号',
    `item_or_product` int         NOT NULL                COMMENT '物料/产品标识',
    `sort`           int          DEFAULT NULL            COMMENT '排序',
    `status`         int          NOT NULL                COMMENT '状态',
    `remark`         varchar(500) DEFAULT NULL            COMMENT '备注',
    `create_time`    datetime     NOT NULL DEFAULT CURRENT_TIMESTAMP,
    `update_time`    datetime     NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    `creator`        varchar(64)  DEFAULT NULL,
    `updater`        varchar(64)  DEFAULT NULL,
    `deleted`        bit(1)       NOT NULL DEFAULT 0,
    PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='MES 物料分类表';

-- 1.2 物料表
DROP TABLE IF EXISTS `mes_md_item`;
CREATE TABLE `mes_md_item` (
    `id`               bigint       NOT NULL AUTO_INCREMENT COMMENT '主键编号',
    `tenant_id`        bigint       NOT NULL DEFAULT 1      COMMENT '租户编号',
    `code`             varchar(64)  NOT NULL                COMMENT '物料编号',
    `name`             varchar(100) NOT NULL                COMMENT '物料名称',
    `specification`    varchar(200) DEFAULT NULL            COMMENT '规格',
    `unit_measure_id`  bigint       DEFAULT NULL            COMMENT '单位编号（关联 mes_md_unit_measure.id）',
    `item_type_id`     bigint       NOT NULL                COMMENT '物料分类编号',
    `status`           int          NOT NULL                COMMENT '状态',
    `safe_stock_flag`  bit(1)       NOT NULL DEFAULT 0      COMMENT '安全库存标识',
    `min_stock`        decimal(24,6) DEFAULT NULL           COMMENT '最小库存',
    `max_stock`        decimal(24,6) DEFAULT NULL           COMMENT '最大库存',
    `high_value`       bit(1)       NOT NULL DEFAULT 0      COMMENT '高价值标识',
    `batch_flag`       bit(1)       NOT NULL DEFAULT 0      COMMENT '批次管理标识',
    `remark`           varchar(500) DEFAULT NULL            COMMENT '备注',
    `create_time`      datetime     NOT NULL DEFAULT CURRENT_TIMESTAMP,
    `update_time`      datetime     NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    `creator`          varchar(64)  DEFAULT NULL,
    `updater`          varchar(64)  DEFAULT NULL,
    `deleted`          bit(1)       NOT NULL DEFAULT 0,
    PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='MES 物料表';

-- 1.3 物料批次配置表
DROP TABLE IF EXISTS `mes_md_item_batch_config`;
CREATE TABLE `mes_md_item_batch_config` (
    `id`                     bigint  NOT NULL AUTO_INCREMENT COMMENT '主键编号',
    `tenant_id`              bigint  NOT NULL DEFAULT 1      COMMENT '租户编号',
    `item_id`                bigint  NOT NULL                COMMENT '物料编号',
    `produce_date_flag`      bit(1)  NOT NULL DEFAULT 0      COMMENT '生产日期标识',
    `expire_date_flag`       bit(1)  NOT NULL DEFAULT 0      COMMENT '有效期标识',
    `receipt_date_flag`      bit(1)  NOT NULL DEFAULT 0      COMMENT '收货日期标识',
    `vendor_flag`            bit(1)  NOT NULL DEFAULT 0      COMMENT '供应商标识',
    `client_flag`            bit(1)  NOT NULL DEFAULT 0      COMMENT '客户标识',
    `sales_order_code_flag`  bit(1)  NOT NULL DEFAULT 0      COMMENT '销售订单号标识',
    `purchase_order_code_flag` bit(1) NOT NULL DEFAULT 0     COMMENT '采购订单号标识',
    `work_order_flag`        bit(1)  NOT NULL DEFAULT 0      COMMENT '工单标识',
    `task_flag`              bit(1)  NOT NULL DEFAULT 0      COMMENT '任务标识',
    `workstation_flag`       bit(1)  NOT NULL DEFAULT 0      COMMENT '工位标识',
    `tool_flag`              bit(1)  NOT NULL DEFAULT 0      COMMENT '工具标识',
    `mold_flag`              bit(1)  NOT NULL DEFAULT 0      COMMENT '模具标识',
    `lot_number_flag`        bit(1)  NOT NULL DEFAULT 0      COMMENT '批号标识',
    `quality_status_flag`    bit(1)  NOT NULL DEFAULT 0      COMMENT '质量状态标识',
    `create_time`            datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
    `update_time`            datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    `creator`                varchar(64) DEFAULT NULL,
    `updater`                varchar(64) DEFAULT NULL,
    `deleted`                bit(1)    NOT NULL DEFAULT 0,
    PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='MES 物料批次配置表';

-- 1.4 产品 BOM 表
DROP TABLE IF EXISTS `mes_md_product_bom`;
CREATE TABLE `mes_md_product_bom` (
    `id`          bigint         NOT NULL AUTO_INCREMENT COMMENT '主键编号',
    `tenant_id`   bigint         NOT NULL DEFAULT 1      COMMENT '租户编号',
    `item_id`     bigint         NOT NULL                COMMENT '产品编号',
    `bom_item_id` bigint         NOT NULL                COMMENT 'BOM 物料编号',
    `quantity`    decimal(24,6)  NOT NULL                COMMENT '用量',
    `status`      int            NOT NULL                COMMENT '状态',
    `remark`      varchar(500)   DEFAULT NULL            COMMENT '备注',
    `create_time` datetime       NOT NULL DEFAULT CURRENT_TIMESTAMP,
    `update_time` datetime       NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    `creator`     varchar(64)    DEFAULT NULL,
    `updater`     varchar(64)    DEFAULT NULL,
    `deleted`     bit(1)         NOT NULL DEFAULT 0,
    PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='MES 产品 BOM 表';

-- 1.5 产品 SIP 表
DROP TABLE IF EXISTS `mes_md_product_sip`;
CREATE TABLE `mes_md_product_sip` (
    `id`          bigint       NOT NULL AUTO_INCREMENT COMMENT '主键编号',
    `tenant_id`   bigint       NOT NULL DEFAULT 1      COMMENT '租户编号',
    `item_id`     bigint       NOT NULL                COMMENT '产品编号',
    `sort`        int          DEFAULT NULL            COMMENT '排序',
    `process_id`  bigint       DEFAULT NULL            COMMENT '工序编号',
    `title`       varchar(100) DEFAULT NULL            COMMENT '标题',
    `description` varchar(500) DEFAULT NULL            COMMENT '描述',
    `url`         varchar(500) DEFAULT NULL            COMMENT '文件 URL',
    `remark`      varchar(500) DEFAULT NULL            COMMENT '备注',
    `create_time` datetime     NOT NULL DEFAULT CURRENT_TIMESTAMP,
    `update_time` datetime     NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    `creator`     varchar(64)  DEFAULT NULL,
    `updater`     varchar(64)  DEFAULT NULL,
    `deleted`     bit(1)       NOT NULL DEFAULT 0,
    PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='MES 产品 SIP 表';

-- 1.6 产品 SOP 表
DROP TABLE IF EXISTS `mes_md_product_sop`;
CREATE TABLE `mes_md_product_sop` (
    `id`          bigint       NOT NULL AUTO_INCREMENT COMMENT '主键编号',
    `tenant_id`   bigint       NOT NULL DEFAULT 1      COMMENT '租户编号',
    `item_id`     bigint       NOT NULL                COMMENT '产品编号',
    `sort`        int          DEFAULT NULL            COMMENT '排序',
    `process_id`  bigint       DEFAULT NULL            COMMENT '工序编号',
    `title`       varchar(100) DEFAULT NULL            COMMENT '标题',
    `description` varchar(500) DEFAULT NULL            COMMENT '描述',
    `url`         varchar(500) DEFAULT NULL            COMMENT '文件 URL',
    `remark`      varchar(500) DEFAULT NULL            COMMENT '备注',
    `create_time` datetime     NOT NULL DEFAULT CURRENT_TIMESTAMP,
    `update_time` datetime     NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    `creator`     varchar(64)  DEFAULT NULL,
    `updater`     varchar(64)  DEFAULT NULL,
    `deleted`     bit(1)       NOT NULL DEFAULT 0,
    PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='MES 产品 SOP 表';

-- 1.7 计量单位表
DROP TABLE IF EXISTS `mes_md_unit_measure`;
CREATE TABLE `mes_md_unit_measure` (
    `id`           bigint         NOT NULL AUTO_INCREMENT COMMENT '主键编号',
    `tenant_id`    bigint         NOT NULL DEFAULT 1      COMMENT '租户编号',
    `code`         varchar(64)    NOT NULL                COMMENT '单位编号',
    `name`         varchar(100)   NOT NULL                COMMENT '单位名称',
    `primary_flag` bit(1)         NOT NULL DEFAULT 0      COMMENT '主单位标识',
    `primary_id`   bigint         DEFAULT NULL            COMMENT '主单位编号',
    `change_rate`  decimal(24,6)  DEFAULT NULL            COMMENT '换算率',
    `status`       int            NOT NULL                COMMENT '状态',
    `remark`       varchar(500)   DEFAULT NULL            COMMENT '备注',
    `create_time`  datetime       NOT NULL DEFAULT CURRENT_TIMESTAMP,
    `update_time`  datetime       NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    `creator`      varchar(64)    DEFAULT NULL,
    `updater`      varchar(64)    DEFAULT NULL,
    `deleted`      bit(1)         NOT NULL DEFAULT 0,
    PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='MES 计量单位表';

-- 1.8 客户表
DROP TABLE IF EXISTS `mes_md_client`;
CREATE TABLE `mes_md_client` (
    `id`                bigint       NOT NULL AUTO_INCREMENT COMMENT '主键编号',
    `tenant_id`         bigint       NOT NULL DEFAULT 1      COMMENT '租户编号',
    `code`              varchar(64)  NOT NULL                COMMENT '客户编号',
    `name`              varchar(100) NOT NULL                COMMENT '客户名称',
    `nickname`          varchar(100) DEFAULT NULL            COMMENT '简称',
    `english_name`      varchar(100) DEFAULT NULL            COMMENT '英文名称',
    `description`       varchar(500) DEFAULT NULL            COMMENT '描述',
    `logo`              varchar(500) DEFAULT NULL            COMMENT 'LOGO',
    `type`              int          NOT NULL                COMMENT '类型',
    `address`           varchar(255) DEFAULT NULL            COMMENT '地址',
    `website`           varchar(255) DEFAULT NULL            COMMENT '网址',
    `email`             varchar(100) DEFAULT NULL            COMMENT '邮箱',
    `telephone`         varchar(20)  DEFAULT NULL            COMMENT '电话',
    `contact1_name`     varchar(50)  DEFAULT NULL            COMMENT '联系人1',
    `contact1_telephone` varchar(20) DEFAULT NULL            COMMENT '联系人1电话',
    `contact1_email`    varchar(100) DEFAULT NULL            COMMENT '联系人1邮箱',
    `contact2_name`     varchar(50)  DEFAULT NULL            COMMENT '联系人2',
    `contact2_telephone` varchar(20) DEFAULT NULL            COMMENT '联系人2电话',
    `contact2_email`    varchar(100) DEFAULT NULL            COMMENT '联系人2邮箱',
    `credit_code`       varchar(50)  DEFAULT NULL            COMMENT '统一社会信用代码',
    `status`            int          NOT NULL                COMMENT '状态',
    `remark`            varchar(500) DEFAULT NULL            COMMENT '备注',
    `create_time`       datetime     NOT NULL DEFAULT CURRENT_TIMESTAMP,
    `update_time`       datetime     NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    `creator`           varchar(64)  DEFAULT NULL,
    `updater`           varchar(64)  DEFAULT NULL,
    `deleted`           bit(1)       NOT NULL DEFAULT 0,
    PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='MES 客户表';

-- 1.9 供应商表
DROP TABLE IF EXISTS `mes_md_vendor`;
CREATE TABLE `mes_md_vendor` (
    `id`                bigint       NOT NULL AUTO_INCREMENT COMMENT '主键编号',
    `tenant_id`         bigint       NOT NULL DEFAULT 1      COMMENT '租户编号',
    `code`              varchar(64)  NOT NULL                COMMENT '供应商编号',
    `name`              varchar(100) NOT NULL                COMMENT '供应商名称',
    `nickname`          varchar(100) DEFAULT NULL            COMMENT '简称',
    `english_name`      varchar(100) DEFAULT NULL            COMMENT '英文名称',
    `description`       varchar(500) DEFAULT NULL            COMMENT '描述',
    `logo`              varchar(500) DEFAULT NULL            COMMENT 'LOGO',
    `level`             varchar(20)  DEFAULT NULL            COMMENT '级别',
    `score`             decimal(5,2) DEFAULT NULL            COMMENT '评分',
    `address`           varchar(255) DEFAULT NULL            COMMENT '地址',
    `website`           varchar(255) DEFAULT NULL            COMMENT '网址',
    `email`             varchar(100) DEFAULT NULL            COMMENT '邮箱',
    `telephone`         varchar(20)  DEFAULT NULL            COMMENT '电话',
    `contact1_name`     varchar(50)  DEFAULT NULL            COMMENT '联系人1',
    `contact1_telephone` varchar(20) DEFAULT NULL            COMMENT '联系人1电话',
    `contact1_email`    varchar(100) DEFAULT NULL            COMMENT '联系人1邮箱',
    `contact2_name`     varchar(50)  DEFAULT NULL            COMMENT '联系人2',
    `contact2_telephone` varchar(20) DEFAULT NULL            COMMENT '联系人2电话',
    `contact2_email`    varchar(100) DEFAULT NULL            COMMENT '联系人2邮箱',
    `credit_code`       varchar(50)  DEFAULT NULL            COMMENT '统一社会信用代码',
    `status`            int          NOT NULL                COMMENT '状态',
    `remark`            varchar(500) DEFAULT NULL            COMMENT '备注',
    `create_time`       datetime     NOT NULL DEFAULT CURRENT_TIMESTAMP,
    `update_time`       datetime     NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    `creator`           varchar(64)  DEFAULT NULL,
    `updater`           varchar(64)  DEFAULT NULL,
    `deleted`           bit(1)       NOT NULL DEFAULT 0,
    PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='MES 供应商表';

-- 1.10 自动编码规则表
DROP TABLE IF EXISTS `mes_md_auto_code_rule`;
CREATE TABLE `mes_md_auto_code_rule` (
    `id`             bigint       NOT NULL AUTO_INCREMENT COMMENT '主键编号',
    `tenant_id`      bigint       NOT NULL DEFAULT 1      COMMENT '租户编号',
    `code`           varchar(64)  NOT NULL                COMMENT '规则编码',
    `name`           varchar(100) NOT NULL                COMMENT '规则名称',
    `description`    varchar(500) DEFAULT NULL            COMMENT '描述',
    `max_length`     int          NOT NULL                COMMENT '最大长度',
    `padded`         bit(1)       NOT NULL DEFAULT 0      COMMENT '补位标识',
    `padded_char`    varchar(5)   DEFAULT NULL            COMMENT '补位字符',
    `padded_method`  int          DEFAULT NULL            COMMENT '补位方式',
    `status`         int          NOT NULL                COMMENT '状态',
    `remark`         varchar(500) DEFAULT NULL            COMMENT '备注',
    `create_time`    datetime     NOT NULL DEFAULT CURRENT_TIMESTAMP,
    `update_time`    datetime     NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    `creator`        varchar(64)  DEFAULT NULL,
    `updater`        varchar(64)  DEFAULT NULL,
    `deleted`        bit(1)       NOT NULL DEFAULT 0,
    PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='MES 自动编码规则表';

-- 1.11 自动编码规则组成部分表
DROP TABLE IF EXISTS `mes_md_auto_code_part`;
CREATE TABLE `mes_md_auto_code_part` (
    `id`              bigint       NOT NULL AUTO_INCREMENT COMMENT '主键编号',
    `tenant_id`       bigint       NOT NULL DEFAULT 1      COMMENT '租户编号',
    `rule_id`         bigint       NOT NULL                COMMENT '规则编号',
    `sort`            int          NOT NULL                COMMENT '排序',
    `type`            int          NOT NULL                COMMENT '组成部分类型',
    `length`          int          DEFAULT NULL            COMMENT '长度',
    `date_format`     varchar(20)  DEFAULT NULL            COMMENT '日期格式',
    `fix_character`   varchar(50)  DEFAULT NULL            COMMENT '固定字符',
    `serial_start_no` int          DEFAULT NULL            COMMENT '序列起始号',
    `serial_step`     int          DEFAULT NULL            COMMENT '序列步长',
    `cycle_flag`      bit(1)       NOT NULL DEFAULT 0      COMMENT '周期标识',
    `cycle_method`    int          DEFAULT NULL            COMMENT '周期方式',
    `remark`          varchar(500) DEFAULT NULL            COMMENT '备注',
    `create_time`     datetime     NOT NULL DEFAULT CURRENT_TIMESTAMP,
    `update_time`     datetime     NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    `creator`         varchar(64)  DEFAULT NULL,
    `updater`         varchar(64)  DEFAULT NULL,
    `deleted`         bit(1)       NOT NULL DEFAULT 0,
    PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='MES 自动编码规则组成部分表';

-- 1.12 自动编码记录表
DROP TABLE IF EXISTS `mes_md_auto_code_record`;
CREATE TABLE `mes_md_auto_code_record` (
    `id`          bigint       NOT NULL AUTO_INCREMENT COMMENT '主键编号',
    `tenant_id`   bigint       NOT NULL DEFAULT 1      COMMENT '租户编号',
    `rule_id`     bigint       NOT NULL                COMMENT '规则编号',
    `result`      varchar(100) NOT NULL                COMMENT '生成结果',
    `serial_no`   int          NOT NULL                COMMENT '序列号',
    `input_char`  varchar(50)  DEFAULT NULL            COMMENT '输入字符',
    `create_time` datetime     NOT NULL DEFAULT CURRENT_TIMESTAMP,
    `update_time` datetime     NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    `creator`     varchar(64)  DEFAULT NULL,
    `updater`     varchar(64)  DEFAULT NULL,
    `deleted`     bit(1)       NOT NULL DEFAULT 0,
    PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='MES 自动编码记录表';

-- 1.13 车间表
DROP TABLE IF EXISTS `mes_md_workshop`;
CREATE TABLE `mes_md_workshop` (
    `id`             bigint       NOT NULL AUTO_INCREMENT COMMENT '主键编号',
    `tenant_id`      bigint       NOT NULL DEFAULT 1      COMMENT '租户编号',
    `code`           varchar(64)  NOT NULL                COMMENT '车间编号',
    `name`           varchar(100) NOT NULL                COMMENT '车间名称',
    `area`           varchar(100) DEFAULT NULL            COMMENT '面积',
    `charge_user_id` bigint       DEFAULT NULL            COMMENT '负责人编号',
    `status`         int          NOT NULL                COMMENT '状态',
    `remark`         varchar(500) DEFAULT NULL            COMMENT '备注',
    `create_time`    datetime     NOT NULL DEFAULT CURRENT_TIMESTAMP,
    `update_time`    datetime     NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    `creator`        varchar(64)  DEFAULT NULL,
    `updater`        varchar(64)  DEFAULT NULL,
    `deleted`        bit(1)       NOT NULL DEFAULT 0,
    PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='MES 车间表';

-- 1.14 工位表
DROP TABLE IF EXISTS `mes_md_workstation`;
CREATE TABLE `mes_md_workstation` (
    `id`            bigint       NOT NULL AUTO_INCREMENT COMMENT '主键编号',
    `tenant_id`     bigint       NOT NULL DEFAULT 1      COMMENT '租户编号',
    `code`          varchar(64)  NOT NULL                COMMENT '工位编号',
    `name`          varchar(100) NOT NULL                COMMENT '工位名称',
    `address`       varchar(255) DEFAULT NULL            COMMENT '地址',
    `workshop_id`   bigint       DEFAULT NULL            COMMENT '车间编号',
    `process_id`    bigint       DEFAULT NULL            COMMENT '工序编号',
    `warehouse_id`  bigint       DEFAULT NULL            COMMENT '仓库编号',
    `location_id`   bigint       DEFAULT NULL            COMMENT '库位编号',
    `area_id`       bigint       DEFAULT NULL            COMMENT '库区编号',
    `status`        int          NOT NULL                COMMENT '状态',
    `remark`        varchar(500) DEFAULT NULL            COMMENT '备注',
    `create_time`   datetime     NOT NULL DEFAULT CURRENT_TIMESTAMP,
    `update_time`   datetime     NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    `creator`       varchar(64)  DEFAULT NULL,
    `updater`       varchar(64)  DEFAULT NULL,
    `deleted`       bit(1)       NOT NULL DEFAULT 0,
    PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='MES 工位表';

-- 1.15 工位设备表
DROP TABLE IF EXISTS `mes_md_workstation_machine`;
CREATE TABLE `mes_md_workstation_machine` (
    `id`             bigint         NOT NULL AUTO_INCREMENT COMMENT '主键编号',
    `tenant_id`      bigint         NOT NULL DEFAULT 1      COMMENT '租户编号',
    `workstation_id` bigint         NOT NULL                COMMENT '工位编号',
    `machinery_id`   bigint         NOT NULL                COMMENT '设备编号',
    `quantity`       int            NOT NULL DEFAULT 1      COMMENT '数量',
    `remark`         varchar(500)   DEFAULT NULL            COMMENT '备注',
    `create_time`    datetime       NOT NULL DEFAULT CURRENT_TIMESTAMP,
    `update_time`    datetime       NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    `creator`        varchar(64)    DEFAULT NULL,
    `updater`        varchar(64)    DEFAULT NULL,
    `deleted`        bit(1)         NOT NULL DEFAULT 0,
    PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='MES 工位设备表';

-- 1.16 工位工具表
DROP TABLE IF EXISTS `mes_md_workstation_tool`;
CREATE TABLE `mes_md_workstation_tool` (
    `id`             bigint       NOT NULL AUTO_INCREMENT COMMENT '主键编号',
    `tenant_id`      bigint       NOT NULL DEFAULT 1      COMMENT '租户编号',
    `workstation_id` bigint       NOT NULL                COMMENT '工位编号',
    `tool_type_id`   bigint       NOT NULL                COMMENT '工具类型编号',
    `quantity`       int          NOT NULL DEFAULT 1      COMMENT '数量',
    `remark`         varchar(500) DEFAULT NULL            COMMENT '备注',
    `create_time`    datetime     NOT NULL DEFAULT CURRENT_TIMESTAMP,
    `update_time`    datetime     NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    `creator`        varchar(64)  DEFAULT NULL,
    `updater`        varchar(64)  DEFAULT NULL,
    `deleted`        bit(1)       NOT NULL DEFAULT 0,
    PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='MES 工位工具表';

-- 1.17 工位人员配置表
DROP TABLE IF EXISTS `mes_md_workstation_worker`;
CREATE TABLE `mes_md_workstation_worker` (
    `id`             bigint       NOT NULL AUTO_INCREMENT COMMENT '主键编号',
    `tenant_id`      bigint       NOT NULL DEFAULT 1      COMMENT '租户编号',
    `workstation_id` bigint       NOT NULL                COMMENT '工位编号',
    `post_id`        bigint       NOT NULL                COMMENT '岗位编号',
    `quantity`       int          NOT NULL DEFAULT 1      COMMENT '人数',
    `remark`         varchar(500) DEFAULT NULL            COMMENT '备注',
    `create_time`    datetime     NOT NULL DEFAULT CURRENT_TIMESTAMP,
    `update_time`    datetime     NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    `creator`        varchar(64)  DEFAULT NULL,
    `updater`        varchar(64)  DEFAULT NULL,
    `deleted`        bit(1)       NOT NULL DEFAULT 0,
    PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='MES 工位人员配置表';

-- ======================================================================================
-- 二、CAL - 排班管理（7 表）
-- ======================================================================================

-- 2.1 班组团队表
DROP TABLE IF EXISTS `mes_cal_team`;
CREATE TABLE `mes_cal_team` (
    `id`            bigint       NOT NULL AUTO_INCREMENT COMMENT '主键编号',
    `tenant_id`     bigint       NOT NULL DEFAULT 1,
    `code`          varchar(64)  NOT NULL                COMMENT '团队编号',
    `name`          varchar(100) NOT NULL                COMMENT '团队名称',
    `calendar_type` int          NOT NULL                COMMENT '日历类型',
    `remark`        varchar(500) DEFAULT NULL,
    `create_time`   datetime     NOT NULL DEFAULT CURRENT_TIMESTAMP,
    `update_time`   datetime     NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    `creator`       varchar(64)  DEFAULT NULL,
    `updater`       varchar(64)  DEFAULT NULL,
    `deleted`       bit(1)       NOT NULL DEFAULT 0,
    PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='MES 班组团队表';

-- 2.2 班组团队成员表
DROP TABLE IF EXISTS `mes_cal_team_member`;
CREATE TABLE `mes_cal_team_member` (
    `id`          bigint       NOT NULL AUTO_INCREMENT,
    `tenant_id`   bigint       NOT NULL DEFAULT 1,
    `team_id`     bigint       NOT NULL                COMMENT '团队编号',
    `user_id`     bigint       NOT NULL                COMMENT '用户编号',
    `remark`      varchar(500) DEFAULT NULL,
    `create_time` datetime     NOT NULL DEFAULT CURRENT_TIMESTAMP,
    `update_time` datetime     NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    `creator`     varchar(64)  DEFAULT NULL,
    `updater`     varchar(64)  DEFAULT NULL,
    `deleted`     bit(1)       NOT NULL DEFAULT 0,
    PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='MES 班组团队成员表';

-- 2.3 节假日表
DROP TABLE IF EXISTS `mes_cal_holiday`;
CREATE TABLE `mes_cal_holiday` (
    `id`          bigint       NOT NULL AUTO_INCREMENT,
    `tenant_id`   bigint       NOT NULL DEFAULT 1,
    `day`         date         NOT NULL                COMMENT '日期',
    `type`        int          NOT NULL                COMMENT '类型',
    `remark`      varchar(500) DEFAULT NULL,
    `create_time` datetime     NOT NULL DEFAULT CURRENT_TIMESTAMP,
    `update_time` datetime     NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    `creator`     varchar(64)  DEFAULT NULL,
    `updater`     varchar(64)  DEFAULT NULL,
    `deleted`     bit(1)       NOT NULL DEFAULT 0,
    PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='MES 节假日表';

-- 2.4 排班计划表
DROP TABLE IF EXISTS `mes_cal_plan`;
CREATE TABLE `mes_cal_plan` (
    `id`            bigint       NOT NULL AUTO_INCREMENT,
    `tenant_id`     bigint       NOT NULL DEFAULT 1,
    `code`          varchar(64)  NOT NULL                COMMENT '计划编号',
    `name`          varchar(100) NOT NULL                COMMENT '计划名称',
    `calendar_type` int          NOT NULL                COMMENT '日历类型',
    `start_date`    date         NOT NULL                COMMENT '开始日期',
    `end_date`      date         NOT NULL                COMMENT '结束日期',
    `shift_type`    int          NOT NULL                COMMENT '班次类型',
    `shift_method`  int          NOT NULL                COMMENT '轮班方式',
    `shift_count`   int          NOT NULL                COMMENT '轮班天数',
    `status`        int          NOT NULL                COMMENT '状态',
    `remark`        varchar(500) DEFAULT NULL,
    `create_time`   datetime     NOT NULL DEFAULT CURRENT_TIMESTAMP,
    `update_time`   datetime     NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    `creator`       varchar(64)  DEFAULT NULL,
    `updater`       varchar(64)  DEFAULT NULL,
    `deleted`       bit(1)       NOT NULL DEFAULT 0,
    PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='MES 排班计划表';

-- 2.5 排班计划班次表
DROP TABLE IF EXISTS `mes_cal_plan_shift`;
CREATE TABLE `mes_cal_plan_shift` (
    `id`          bigint       NOT NULL AUTO_INCREMENT,
    `tenant_id`   bigint       NOT NULL DEFAULT 1,
    `plan_id`     bigint       NOT NULL                COMMENT '计划编号',
    `sort`        int          NOT NULL                COMMENT '排序',
    `name`        varchar(100) NOT NULL                COMMENT '班次名称',
    `start_time`  time         NOT NULL                COMMENT '开始时间',
    `end_time`    time         NOT NULL                COMMENT '结束时间',
    `remark`      varchar(500) DEFAULT NULL,
    `create_time` datetime     NOT NULL DEFAULT CURRENT_TIMESTAMP,
    `update_time` datetime     NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    `creator`     varchar(64)  DEFAULT NULL,
    `updater`     varchar(64)  DEFAULT NULL,
    `deleted`     bit(1)       NOT NULL DEFAULT 0,
    PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='MES 排班计划班次表';

-- 2.6 排班计划团队关联表
DROP TABLE IF EXISTS `mes_cal_plan_team`;
CREATE TABLE `mes_cal_plan_team` (
    `id`          bigint       NOT NULL AUTO_INCREMENT,
    `tenant_id`   bigint       NOT NULL DEFAULT 1,
    `plan_id`     bigint       NOT NULL                COMMENT '计划编号',
    `team_id`     bigint       NOT NULL                COMMENT '团队编号',
    `remark`      varchar(500) DEFAULT NULL,
    `create_time` datetime     NOT NULL DEFAULT CURRENT_TIMESTAMP,
    `update_time` datetime     NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    `creator`     varchar(64)  DEFAULT NULL,
    `updater`     varchar(64)  DEFAULT NULL,
    `deleted`     bit(1)       NOT NULL DEFAULT 0,
    PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='MES 排班计划团队关联表';

-- 2.7 团队排班表
DROP TABLE IF EXISTS `mes_cal_team_shift`;
CREATE TABLE `mes_cal_team_shift` (
    `id`          bigint       NOT NULL AUTO_INCREMENT,
    `tenant_id`   bigint       NOT NULL DEFAULT 1,
    `plan_id`     bigint       NOT NULL                COMMENT '计划编号',
    `team_id`     bigint       NOT NULL                COMMENT '团队编号',
    `shift_id`    bigint       NOT NULL                COMMENT '班次编号',
    `day`         date         NOT NULL                COMMENT '日期',
    `sort`        int          NOT NULL                COMMENT '排序',
    `remark`      varchar(500) DEFAULT NULL,
    `create_time` datetime     NOT NULL DEFAULT CURRENT_TIMESTAMP,
    `update_time` datetime     NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    `creator`     varchar(64)  DEFAULT NULL,
    `updater`     varchar(64)  DEFAULT NULL,
    `deleted`     bit(1)       NOT NULL DEFAULT 0,
    PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='MES 团队排班表';

-- ======================================================================================
-- 三、DV - 设备维保（12 表）
-- ======================================================================================

-- 3.1 设备类型表
DROP TABLE IF EXISTS `mes_dv_machinery_type`;
CREATE TABLE `mes_dv_machinery_type` (
    `id`          bigint       NOT NULL AUTO_INCREMENT,
    `tenant_id`   bigint       NOT NULL DEFAULT 1,
    `code`        varchar(64)  NOT NULL                COMMENT '类型编号',
    `name`        varchar(100) NOT NULL                COMMENT '类型名称',
    `parent_id`   bigint       NOT NULL DEFAULT 0      COMMENT '父级编号',
    `status`      int          NOT NULL                COMMENT '状态',
    `sort`        int          DEFAULT NULL,
    `remark`      varchar(500) DEFAULT NULL,
    `create_time` datetime     NOT NULL DEFAULT CURRENT_TIMESTAMP,
    `update_time` datetime     NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    `creator`     varchar(64)  DEFAULT NULL,
    `updater`     varchar(64)  DEFAULT NULL,
    `deleted`     bit(1)       NOT NULL DEFAULT 0,
    PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='MES 设备类型表';

-- 3.2 设备表
DROP TABLE IF EXISTS `mes_dv_machinery`;
CREATE TABLE `mes_dv_machinery` (
    `id`                bigint       NOT NULL AUTO_INCREMENT,
    `tenant_id`         bigint       NOT NULL DEFAULT 1,
    `code`              varchar(64)  NOT NULL                COMMENT '设备编号',
    `name`              varchar(100) NOT NULL                COMMENT '设备名称',
    `brand`             varchar(100) DEFAULT NULL            COMMENT '品牌',
    `specification`     varchar(200) DEFAULT NULL            COMMENT '规格',
    `machinery_type_id` bigint       NOT NULL                COMMENT '设备类型编号',
    `workshop_id`       bigint       DEFAULT NULL            COMMENT '车间编号',
    `status`            int          NOT NULL                COMMENT '状态',
    `last_mainten_time` datetime     DEFAULT NULL            COMMENT '上次保养时间',
    `last_check_time`   datetime     DEFAULT NULL            COMMENT '上次点检时间',
    `remark`            varchar(500) DEFAULT NULL,
    `create_time`       datetime     NOT NULL DEFAULT CURRENT_TIMESTAMP,
    `update_time`       datetime     NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    `creator`           varchar(64)  DEFAULT NULL,
    `updater`           varchar(64)  DEFAULT NULL,
    `deleted`           bit(1)       NOT NULL DEFAULT 0,
    PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='MES 设备表';

-- 3.3 维保项目表
DROP TABLE IF EXISTS `mes_dv_subject`;
CREATE TABLE `mes_dv_subject` (
    `id`          bigint       NOT NULL AUTO_INCREMENT,
    `tenant_id`   bigint       NOT NULL DEFAULT 1,
    `code`        varchar(64)  NOT NULL                COMMENT '项目编号',
    `name`        varchar(100) NOT NULL                COMMENT '项目名称',
    `type`        int          NOT NULL                COMMENT '类型',
    `content`     varchar(500) DEFAULT NULL            COMMENT '检查内容',
    `standard`    varchar(500) DEFAULT NULL            COMMENT '检查标准',
    `status`      int          NOT NULL                COMMENT '状态',
    `remark`      varchar(500) DEFAULT NULL,
    `create_time` datetime     NOT NULL DEFAULT CURRENT_TIMESTAMP,
    `update_time` datetime     NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    `creator`     varchar(64)  DEFAULT NULL,
    `updater`     varchar(64)  DEFAULT NULL,
    `deleted`     bit(1)       NOT NULL DEFAULT 0,
    PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='MES 维保项目表';

-- 3.4 点检计划表
DROP TABLE IF EXISTS `mes_dv_check_plan`;
CREATE TABLE `mes_dv_check_plan` (
    `id`          bigint       NOT NULL AUTO_INCREMENT,
    `tenant_id`   bigint       NOT NULL DEFAULT 1,
    `code`        varchar(64)  NOT NULL                COMMENT '计划编号',
    `name`        varchar(100) NOT NULL                COMMENT '计划名称',
    `type`        int          NOT NULL                COMMENT '计划类型',
    `start_date`  date         NOT NULL                COMMENT '开始日期',
    `end_date`    date         NOT NULL                COMMENT '结束日期',
    `cycle_type`  int          NOT NULL                COMMENT '周期类型',
    `cycle_count` int          NOT NULL                COMMENT '周期次数',
    `status`      int          NOT NULL                COMMENT '状态',
    `remark`      varchar(500) DEFAULT NULL,
    `create_time` datetime     NOT NULL DEFAULT CURRENT_TIMESTAMP,
    `update_time` datetime     NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    `creator`     varchar(64)  DEFAULT NULL,
    `updater`     varchar(64)  DEFAULT NULL,
    `deleted`     bit(1)       NOT NULL DEFAULT 0,
    PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='MES 点检计划表';

-- 3.5 点检计划-设备关联表
DROP TABLE IF EXISTS `mes_dv_check_plan_machinery`;
CREATE TABLE `mes_dv_check_plan_machinery` (
    `id`           bigint       NOT NULL AUTO_INCREMENT,
    `tenant_id`    bigint       NOT NULL DEFAULT 1,
    `plan_id`      bigint       NOT NULL                COMMENT '计划编号',
    `machinery_id` bigint       NOT NULL                COMMENT '设备编号',
    `remark`       varchar(500) DEFAULT NULL,
    `create_time`  datetime     NOT NULL DEFAULT CURRENT_TIMESTAMP,
    `update_time`  datetime     NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    `creator`      varchar(64)  DEFAULT NULL,
    `updater`      varchar(64)  DEFAULT NULL,
    `deleted`      bit(1)       NOT NULL DEFAULT 0,
    PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='MES 点检计划-设备关联表';

-- 3.6 点检计划-项目关联表
DROP TABLE IF EXISTS `mes_dv_check_plan_subject`;
CREATE TABLE `mes_dv_check_plan_subject` (
    `id`         bigint       NOT NULL AUTO_INCREMENT,
    `tenant_id`  bigint       NOT NULL DEFAULT 1,
    `plan_id`    bigint       NOT NULL                COMMENT '计划编号',
    `subject_id` bigint       NOT NULL                COMMENT '项目编号',
    `remark`     varchar(500) DEFAULT NULL,
    `create_time` datetime    NOT NULL DEFAULT CURRENT_TIMESTAMP,
    `update_time` datetime    NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    `creator`    varchar(64)  DEFAULT NULL,
    `updater`    varchar(64)  DEFAULT NULL,
    `deleted`    bit(1)       NOT NULL DEFAULT 0,
    PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='MES 点检计划-项目关联表';

-- 3.7 点检记录表
DROP TABLE IF EXISTS `mes_dv_check_record`;
CREATE TABLE `mes_dv_check_record` (
    `id`           bigint       NOT NULL AUTO_INCREMENT,
    `tenant_id`    bigint       NOT NULL DEFAULT 1,
    `plan_id`      bigint       NOT NULL                COMMENT '计划编号',
    `machinery_id` bigint       NOT NULL                COMMENT '设备编号',
    `check_time`   datetime     NOT NULL                COMMENT '点检时间',
    `user_id`      bigint       NOT NULL                COMMENT '点检人',
    `status`       int          NOT NULL                COMMENT '状态',
    `remark`       varchar(500) DEFAULT NULL,
    `create_time`  datetime     NOT NULL DEFAULT CURRENT_TIMESTAMP,
    `update_time`  datetime     NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    `creator`      varchar(64)  DEFAULT NULL,
    `updater`      varchar(64)  DEFAULT NULL,
    `deleted`      bit(1)       NOT NULL DEFAULT 0,
    PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='MES 点检记录表';

-- 3.8 点检记录行表
DROP TABLE IF EXISTS `mes_dv_check_record_line`;
CREATE TABLE `mes_dv_check_record_line` (
    `id`           bigint       NOT NULL AUTO_INCREMENT,
    `tenant_id`    bigint       NOT NULL DEFAULT 1,
    `record_id`    bigint       NOT NULL                COMMENT '记录编号',
    `subject_id`   bigint       NOT NULL                COMMENT '项目编号',
    `check_status` varchar(50)  DEFAULT NULL            COMMENT '检查状态',
    `check_result` varchar(500) DEFAULT NULL            COMMENT '检查结果',
    `remark`       varchar(500) DEFAULT NULL,
    `create_time`  datetime     NOT NULL DEFAULT CURRENT_TIMESTAMP,
    `update_time`  datetime     NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    `creator`      varchar(64)  DEFAULT NULL,
    `updater`      varchar(64)  DEFAULT NULL,
    `deleted`      bit(1)       NOT NULL DEFAULT 0,
    PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='MES 点检记录行表';

-- 3.9 保养记录表
DROP TABLE IF EXISTS `mes_dv_mainten_record`;
CREATE TABLE `mes_dv_mainten_record` (
    `id`            bigint       NOT NULL AUTO_INCREMENT,
    `tenant_id`     bigint       NOT NULL DEFAULT 1,
    `plan_id`       bigint       NOT NULL                COMMENT '计划编号',
    `machinery_id`  bigint       NOT NULL                COMMENT '设备编号',
    `mainten_time`  datetime     NOT NULL                COMMENT '保养时间',
    `user_id`       bigint       NOT NULL                COMMENT '保养人',
    `status`        int          NOT NULL                COMMENT '状态',
    `remark`        varchar(500) DEFAULT NULL,
    `create_time`   datetime     NOT NULL DEFAULT CURRENT_TIMESTAMP,
    `update_time`   datetime     NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    `creator`       varchar(64)  DEFAULT NULL,
    `updater`       varchar(64)  DEFAULT NULL,
    `deleted`       bit(1)       NOT NULL DEFAULT 0,
    PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='MES 保养记录表';

-- 3.10 保养记录行表
DROP TABLE IF EXISTS `mes_dv_mainten_record_line`;
CREATE TABLE `mes_dv_mainten_record_line` (
    `id`          bigint       NOT NULL AUTO_INCREMENT,
    `tenant_id`   bigint       NOT NULL DEFAULT 1,
    `record_id`   bigint       NOT NULL                COMMENT '记录编号',
    `subject_id`  bigint       NOT NULL                COMMENT '项目编号',
    `status`      varchar(50)  DEFAULT NULL            COMMENT '状态',
    `result`      varchar(500) DEFAULT NULL            COMMENT '保养结果',
    `remark`      varchar(500) DEFAULT NULL,
    `create_time` datetime     NOT NULL DEFAULT CURRENT_TIMESTAMP,
    `update_time` datetime     NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    `creator`     varchar(64)  DEFAULT NULL,
    `updater`     varchar(64)  DEFAULT NULL,
    `deleted`     bit(1)       NOT NULL DEFAULT 0,
    PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='MES 保养记录行表';

-- 3.11 维修单表
DROP TABLE IF EXISTS `mes_dv_repair`;
CREATE TABLE `mes_dv_repair` (
    `id`               bigint       NOT NULL AUTO_INCREMENT,
    `tenant_id`        bigint       NOT NULL DEFAULT 1,
    `code`             varchar(64)  NOT NULL                COMMENT '维修单号',
    `name`             varchar(100) NOT NULL                COMMENT '维修名称',
    `machinery_id`     bigint       NOT NULL                COMMENT '设备编号',
    `require_date`     date         DEFAULT NULL            COMMENT '报修日期',
    `finish_date`      date         DEFAULT NULL            COMMENT '完成日期',
    `confirm_date`     date         DEFAULT NULL            COMMENT '确认日期',
    `result`           varchar(500) DEFAULT NULL            COMMENT '维修结果',
    `accepted_user_id` bigint       DEFAULT NULL            COMMENT '受理人',
    `confirm_user_id`  bigint       DEFAULT NULL            COMMENT '确认人',
    `source_doc_type`  int          DEFAULT NULL            COMMENT '来源单据类型',
    `source_doc_id`    bigint       DEFAULT NULL            COMMENT '来源单据编号',
    `source_doc_code`  varchar(64)  DEFAULT NULL            COMMENT '来源单据号',
    `status`           int          NOT NULL                COMMENT '状态',
    `remark`           varchar(500) DEFAULT NULL,
    `create_time`      datetime     NOT NULL DEFAULT CURRENT_TIMESTAMP,
    `update_time`      datetime     NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    `creator`          varchar(64)  DEFAULT NULL,
    `updater`          varchar(64)  DEFAULT NULL,
    `deleted`          bit(1)       NOT NULL DEFAULT 0,
    PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='MES 维修单表';

-- 3.12 维修单行表
DROP TABLE IF EXISTS `mes_dv_repair_line`;
CREATE TABLE `mes_dv_repair_line` (
    `id`            bigint       NOT NULL AUTO_INCREMENT,
    `tenant_id`     bigint       NOT NULL DEFAULT 1,
    `repair_id`     bigint       NOT NULL                COMMENT '维修单编号',
    `subject_id`    bigint       NOT NULL                COMMENT '项目编号',
    `malfunction`   varchar(500) DEFAULT NULL            COMMENT '故障描述',
    `malfunction_url` varchar(500) DEFAULT NULL          COMMENT '故障图片',
    `description`   varchar(500) DEFAULT NULL            COMMENT '说明',
    `remark`        varchar(500) DEFAULT NULL,
    `create_time`   datetime     NOT NULL DEFAULT CURRENT_TIMESTAMP,
    `update_time`   datetime     NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    `creator`       varchar(64)  DEFAULT NULL,
    `updater`       varchar(64)  DEFAULT NULL,
    `deleted`       bit(1)       NOT NULL DEFAULT 0,
    PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='MES 维修单行表';

-- ======================================================================================
-- 四、TM - 工具管理（2 表）
-- ======================================================================================

-- 4.1 工具类型表
DROP TABLE IF EXISTS `mes_tm_tool_type`;
CREATE TABLE `mes_tm_tool_type` (
    `id`              bigint       NOT NULL AUTO_INCREMENT,
    `tenant_id`       bigint       NOT NULL DEFAULT 1,
    `code`            varchar(64)  NOT NULL                COMMENT '类型编号',
    `name`            varchar(100) NOT NULL                COMMENT '类型名称',
    `code_flag`       bit(1)       NOT NULL DEFAULT 0      COMMENT '编码标识',
    `mainten_type`    int          DEFAULT NULL            COMMENT '保养方式',
    `mainten_period`  int          DEFAULT NULL            COMMENT '保养周期',
    `remark`          varchar(500) DEFAULT NULL,
    `create_time`     datetime     NOT NULL DEFAULT CURRENT_TIMESTAMP,
    `update_time`     datetime     NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    `creator`         varchar(64)  DEFAULT NULL,
    `updater`         varchar(64)  DEFAULT NULL,
    `deleted`         bit(1)       NOT NULL DEFAULT 0,
    PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='MES 工具类型表';

-- 4.2 工具表
DROP TABLE IF EXISTS `mes_tm_tool`;
CREATE TABLE `mes_tm_tool` (
    `id`                  bigint         NOT NULL AUTO_INCREMENT,
    `tenant_id`           bigint         NOT NULL DEFAULT 1,
    `code`                varchar(64)    NOT NULL                COMMENT '工具编号',
    `name`                varchar(100)   NOT NULL                COMMENT '工具名称',
    `brand`               varchar(100)   DEFAULT NULL            COMMENT '品牌',
    `specification`       varchar(200)   DEFAULT NULL            COMMENT '规格',
    `tool_type_id`        bigint         NOT NULL                COMMENT '工具类型编号',
    `quantity`            int            NOT NULL                COMMENT '数量',
    `available_quantity`  int            NOT NULL                COMMENT '可用数量',
    `mainten_type`        int            DEFAULT NULL            COMMENT '保养方式',
    `next_mainten_period` int            DEFAULT NULL            COMMENT '下次保养周期',
    `next_mainten_date`   date           DEFAULT NULL            COMMENT '下次保养日期',
    `status`              int            NOT NULL                COMMENT '状态',
    `remark`              varchar(500)   DEFAULT NULL,
    `create_time`         datetime       NOT NULL DEFAULT CURRENT_TIMESTAMP,
    `update_time`         datetime       NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    `creator`             varchar(64)    DEFAULT NULL,
    `updater`             varchar(64)    DEFAULT NULL,
    `deleted`             bit(1)         NOT NULL DEFAULT 0,
    PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='MES 工具表';

-- ======================================================================================
-- 五、PRO - 生产模块（17 表）
-- ======================================================================================

-- 5.1 工序表
DROP TABLE IF EXISTS `mes_pro_process`;
CREATE TABLE `mes_pro_process` (
    `id`          bigint       NOT NULL AUTO_INCREMENT,
    `tenant_id`   bigint       NOT NULL DEFAULT 1,
    `code`        varchar(64)  NOT NULL                COMMENT '工序编号',
    `name`        varchar(100) NOT NULL                COMMENT '工序名称',
    `attention`   varchar(500) DEFAULT NULL            COMMENT '注意事项',
    `status`      int          NOT NULL                COMMENT '状态',
    `remark`      varchar(500) DEFAULT NULL,
    `create_time` datetime     NOT NULL DEFAULT CURRENT_TIMESTAMP,
    `update_time` datetime     NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    `creator`     varchar(64)  DEFAULT NULL,
    `updater`     varchar(64)  DEFAULT NULL,
    `deleted`     bit(1)       NOT NULL DEFAULT 0,
    PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='MES 工序表';

-- 5.2 工序内容表
DROP TABLE IF EXISTS `mes_pro_process_content`;
CREATE TABLE `mes_pro_process_content` (
    `id`          bigint       NOT NULL AUTO_INCREMENT,
    `tenant_id`   bigint       NOT NULL DEFAULT 1,
    `process_id`  bigint       NOT NULL                COMMENT '工序编号',
    `sort`        int          NOT NULL                COMMENT '排序',
    `content`     varchar(500) DEFAULT NULL            COMMENT '内容',
    `device`      varchar(500) DEFAULT NULL            COMMENT '设备',
    `material`    varchar(500) DEFAULT NULL            COMMENT '材料',
    `doc_url`     varchar(500) DEFAULT NULL            COMMENT '文件URL',
    `remark`      varchar(500) DEFAULT NULL,
    `create_time` datetime     NOT NULL DEFAULT CURRENT_TIMESTAMP,
    `update_time` datetime     NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    `creator`     varchar(64)  DEFAULT NULL,
    `updater`     varchar(64)  DEFAULT NULL,
    `deleted`     bit(1)       NOT NULL DEFAULT 0,
    PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='MES 工序内容表';

-- 5.3 工艺路线表
DROP TABLE IF EXISTS `mes_pro_route`;
CREATE TABLE `mes_pro_route` (
    `id`          bigint       NOT NULL AUTO_INCREMENT,
    `tenant_id`   bigint       NOT NULL DEFAULT 1,
    `code`        varchar(64)  NOT NULL                COMMENT '路线编号',
    `name`        varchar(100) NOT NULL                COMMENT '路线名称',
    `description` varchar(500) DEFAULT NULL            COMMENT '描述',
    `status`      int          NOT NULL                COMMENT '状态',
    `remark`      varchar(500) DEFAULT NULL,
    `create_time` datetime     NOT NULL DEFAULT CURRENT_TIMESTAMP,
    `update_time` datetime     NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    `creator`     varchar(64)  DEFAULT NULL,
    `updater`     varchar(64)  DEFAULT NULL,
    `deleted`     bit(1)       NOT NULL DEFAULT 0,
    PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='MES 工艺路线表';

-- 5.4 工艺路线-工序表
DROP TABLE IF EXISTS `mes_pro_route_process`;
CREATE TABLE `mes_pro_route_process` (
    `id`              bigint         NOT NULL AUTO_INCREMENT,
    `tenant_id`       bigint         NOT NULL DEFAULT 1,
    `route_id`        bigint         NOT NULL                COMMENT '路线编号',
    `process_id`      bigint         NOT NULL                COMMENT '工序编号',
    `sort`            int            NOT NULL                COMMENT '排序',
    `next_process_id` bigint         DEFAULT NULL            COMMENT '下道工序编号',
    `link_type`       int            DEFAULT NULL            COMMENT '连接类型',
    `prepare_time`    int            DEFAULT NULL            COMMENT '准备时间',
    `wait_time`       int            DEFAULT NULL            COMMENT '等待时间',
    `color_code`      varchar(20)    DEFAULT NULL            COMMENT '颜色编码',
    `key_flag`        bit(1)         NOT NULL DEFAULT 0      COMMENT '关键工序标识',
    `check_flag`      bit(1)         NOT NULL DEFAULT 0      COMMENT '检验标识',
    `remark`          varchar(500)   DEFAULT NULL,
    `create_time`     datetime       NOT NULL DEFAULT CURRENT_TIMESTAMP,
    `update_time`     datetime       NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    `creator`         varchar(64)    DEFAULT NULL,
    `updater`         varchar(64)    DEFAULT NULL,
    `deleted`         bit(1)         NOT NULL DEFAULT 0,
    PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='MES 工艺路线-工序表';

-- 5.5 工艺路线-产品表
DROP TABLE IF EXISTS `mes_pro_route_product`;
CREATE TABLE `mes_pro_route_product` (
    `id`              bigint         NOT NULL AUTO_INCREMENT,
    `tenant_id`       bigint         NOT NULL DEFAULT 1,
    `route_id`        bigint         NOT NULL                COMMENT '路线编号',
    `item_id`         bigint         NOT NULL                COMMENT '产品编号',
    `quantity`        int            NOT NULL                COMMENT '数量',
    `production_time` decimal(10,2)  DEFAULT NULL            COMMENT '生产时间',
    `time_unit_type`  int            DEFAULT NULL            COMMENT '时间单位',
    `remark`          varchar(500)   DEFAULT NULL,
    `create_time`     datetime       NOT NULL DEFAULT CURRENT_TIMESTAMP,
    `update_time`     datetime       NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    `creator`         varchar(64)    DEFAULT NULL,
    `updater`         varchar(64)    DEFAULT NULL,
    `deleted`         bit(1)         NOT NULL DEFAULT 0,
    PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='MES 工艺路线-产品表';

-- 5.6 工艺路线产品BOM表
DROP TABLE IF EXISTS `mes_pro_route_product_bom`;
CREATE TABLE `mes_pro_route_product_bom` (
    `id`          bigint         NOT NULL AUTO_INCREMENT,
    `tenant_id`   bigint         NOT NULL DEFAULT 1,
    `route_id`    bigint         NOT NULL                COMMENT '路线编号',
    `process_id`  bigint         NOT NULL                COMMENT '工序编号',
    `product_id`  bigint         NOT NULL                COMMENT '产品编号',
    `item_id`     bigint         NOT NULL                COMMENT '物料编号',
    `quantity`    decimal(24,6)  NOT NULL                COMMENT '用量',
    `remark`      varchar(500)   DEFAULT NULL,
    `create_time` datetime       NOT NULL DEFAULT CURRENT_TIMESTAMP,
    `update_time` datetime       NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    `creator`     varchar(64)    DEFAULT NULL,
    `updater`     varchar(64)    DEFAULT NULL,
    `deleted`     bit(1)         NOT NULL DEFAULT 0,
    PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='MES 工艺路线产品BOM表';

-- 5.7 生产工单表
DROP TABLE IF EXISTS `mes_pro_work_order`;
CREATE TABLE `mes_pro_work_order` (
    `id`                 bigint         NOT NULL AUTO_INCREMENT,
    `tenant_id`          bigint         NOT NULL DEFAULT 1,
    `code`               varchar(64)    NOT NULL                COMMENT '工单编号',
    `name`               varchar(100)   NOT NULL                COMMENT '工单名称',
    `type`               int            NOT NULL                COMMENT '工单类型',
    `order_source_type`  int            DEFAULT NULL            COMMENT '订单来源类型',
    `order_source_code`  varchar(64)    DEFAULT NULL            COMMENT '订单来源编号',
    `product_id`         bigint         NOT NULL                COMMENT '产品编号',
    `quantity`           decimal(24,6)  NOT NULL                COMMENT '计划数量',
    `quantity_produced`  decimal(24,6)  NOT NULL DEFAULT 0      COMMENT '已生产数量',
    `quantity_changed`   decimal(24,6)  NOT NULL DEFAULT 0      COMMENT '已变更数量',
    `quantity_scheduled` decimal(24,6)  NOT NULL DEFAULT 0      COMMENT '已排产数量',
    `client_id`          bigint         DEFAULT NULL            COMMENT '客户编号',
    `vendor_id`          bigint         DEFAULT NULL            COMMENT '供应商编号',
    `batch_code`         varchar(64)    DEFAULT NULL            COMMENT '批次号',
    `request_date`       date           NOT NULL                COMMENT '需求日期',
    `parent_id`          bigint         DEFAULT NULL            COMMENT '父工单编号',
    `finish_date`        date           DEFAULT NULL            COMMENT '完成日期',
    `cancel_date`        date           DEFAULT NULL            COMMENT '取消日期',
    `status`             int            NOT NULL                COMMENT '状态',
    `remark`             varchar(500)   DEFAULT NULL,
    `create_time`        datetime       NOT NULL DEFAULT CURRENT_TIMESTAMP,
    `update_time`        datetime       NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    `creator`            varchar(64)    DEFAULT NULL,
    `updater`            varchar(64)    DEFAULT NULL,
    `deleted`            bit(1)         NOT NULL DEFAULT 0,
    PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='MES 生产工单表';

-- 5.8 生产工单BOM表
DROP TABLE IF EXISTS `mes_pro_work_order_bom`;
CREATE TABLE `mes_pro_work_order_bom` (
    `id`           bigint         NOT NULL AUTO_INCREMENT,
    `tenant_id`    bigint         NOT NULL DEFAULT 1,
    `work_order_id` bigint        NOT NULL                COMMENT '工单编号',
    `item_id`      bigint         NOT NULL                COMMENT '物料编号',
    `quantity`     decimal(24,6)  NOT NULL                COMMENT '用量',
    `remark`       varchar(500)   DEFAULT NULL,
    `create_time`  datetime       NOT NULL DEFAULT CURRENT_TIMESTAMP,
    `update_time`  datetime       NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    `creator`      varchar(64)    DEFAULT NULL,
    `updater`      varchar(64)    DEFAULT NULL,
    `deleted`      bit(1)         NOT NULL DEFAULT 0,
    PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='MES 生产工单BOM表';

-- 5.9 生产任务表
DROP TABLE IF EXISTS `mes_pro_task`;
CREATE TABLE `mes_pro_task` (
    `id`                bigint         NOT NULL AUTO_INCREMENT,
    `tenant_id`         bigint         NOT NULL DEFAULT 1,
    `code`              varchar(64)    NOT NULL                COMMENT '任务编号',
    `name`              varchar(100)   NOT NULL                COMMENT '任务名称',
    `work_order_id`     bigint         NOT NULL                COMMENT '工单编号',
    `workstation_id`    bigint         NOT NULL                COMMENT '工位编号',
    `route_id`          bigint         NOT NULL                COMMENT '工艺路线编号',
    `process_id`        bigint         NOT NULL                COMMENT '工序编号',
    `item_id`           bigint         NOT NULL                COMMENT '产品编号',
    `quantity`          decimal(24,6)  NOT NULL                COMMENT '计划数量',
    `produced_quantity` decimal(24,6)  NOT NULL DEFAULT 0      COMMENT '已生产数量',
    `qualify_quantity`  decimal(24,6)  NOT NULL DEFAULT 0      COMMENT '合格数量',
    `unqualify_quantity` decimal(24,6) NOT NULL DEFAULT 0      COMMENT '不合格数量',
    `changed_quantity`  decimal(24,6)  NOT NULL DEFAULT 0      COMMENT '已变更数量',
    `client_id`         bigint         DEFAULT NULL            COMMENT '客户编号',
    `start_time`        datetime       DEFAULT NULL            COMMENT '开始时间',
    `duration`          int            DEFAULT NULL            COMMENT '工时',
    `end_time`          datetime       DEFAULT NULL            COMMENT '结束时间',
    `color_code`        varchar(20)    DEFAULT NULL            COMMENT '颜色编码',
    `finish_date`       date           DEFAULT NULL            COMMENT '完成日期',
    `cancel_date`       date           DEFAULT NULL            COMMENT '取消日期',
    `status`            int            NOT NULL                COMMENT '状态',
    `remark`            varchar(500)   DEFAULT NULL,
    `create_time`       datetime       NOT NULL DEFAULT CURRENT_TIMESTAMP,
    `update_time`       datetime       NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    `creator`           varchar(64)    DEFAULT NULL,
    `updater`           varchar(64)    DEFAULT NULL,
    `deleted`           bit(1)         NOT NULL DEFAULT 0,
    PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='MES 生产任务表';

-- 5.10 生产任务投料表
DROP TABLE IF EXISTS `mes_pro_task_issue`;
CREATE TABLE `mes_pro_task_issue` (
    `id`               bigint         NOT NULL AUTO_INCREMENT,
    `tenant_id`        bigint         NOT NULL DEFAULT 1,
    `task_id`          bigint         NOT NULL                COMMENT '任务编号',
    `work_order_id`    bigint         NOT NULL                COMMENT '工单编号',
    `workstation_id`   bigint         NOT NULL                COMMENT '工位编号',
    `source_doc_type`  int            DEFAULT NULL            COMMENT '来源单据类型',
    `source_doc_id`    bigint         DEFAULT NULL            COMMENT '来源单据编号',
    `source_line_id`   bigint         DEFAULT NULL            COMMENT '来源行编号',
    `source_doc_code`  varchar(64)    DEFAULT NULL            COMMENT '来源单据号',
    `batch_code`       varchar(64)    DEFAULT NULL            COMMENT '批次号',
    `item_id`          bigint         NOT NULL                COMMENT '物料编号',
    `unit_measure_id`  bigint         DEFAULT NULL            COMMENT '单位编号',
    `issued_quantity`  decimal(24,6)  NOT NULL                COMMENT '投料数量',
    `available_quantity` decimal(24,6) NOT NULL DEFAULT 0     COMMENT '可用数量',
    `used_quantity`    decimal(24,6)  NOT NULL DEFAULT 0      COMMENT '已用数量',
    `remark`           varchar(500)   DEFAULT NULL,
    `create_time`      datetime       NOT NULL DEFAULT CURRENT_TIMESTAMP,
    `update_time`      datetime       NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    `creator`          varchar(64)    DEFAULT NULL,
    `updater`          varchar(64)    DEFAULT NULL,
    `deleted`          bit(1)         NOT NULL DEFAULT 0,
    PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='MES 生产任务投料表';

-- 5.11 生产报工表
DROP TABLE IF EXISTS `mes_pro_feedback`;
CREATE TABLE `mes_pro_feedback` (
    `id`                    bigint         NOT NULL AUTO_INCREMENT,
    `tenant_id`             bigint         NOT NULL DEFAULT 1,
    `code`                  varchar(64)    NOT NULL                COMMENT '报工编号',
    `type`                  int            NOT NULL                COMMENT '类型',
    `channel`               int            NOT NULL                COMMENT '渠道',
    `feedback_time`         datetime       NOT NULL                COMMENT '报工时间',
    `workstation_id`        bigint         NOT NULL                COMMENT '工位编号',
    `route_id`              bigint         NOT NULL                COMMENT '工艺路线编号',
    `process_id`            bigint         NOT NULL                COMMENT '工序编号',
    `work_order_id`         bigint         NOT NULL                COMMENT '工单编号',
    `task_id`               bigint         NOT NULL                COMMENT '任务编号',
    `item_id`               bigint         NOT NULL                COMMENT '产品编号',
    `expire_date`           date           DEFAULT NULL            COMMENT '有效期',
    `lot_number`            varchar(64)    DEFAULT NULL            COMMENT '批号',
    `scheduled_quantity`    decimal(24,6)  NOT NULL DEFAULT 0      COMMENT '计划数量',
    `feedback_quantity`     decimal(24,6)  NOT NULL                COMMENT '报工数量',
    `qualified_quantity`    decimal(24,6)  NOT NULL DEFAULT 0      COMMENT '合格数量',
    `unqualified_quantity`  decimal(24,6)  NOT NULL DEFAULT 0      COMMENT '不合格数量',
    `uncheck_quantity`      decimal(24,6)  NOT NULL DEFAULT 0      COMMENT '待检数量',
    `labor_scrap_quantity`  decimal(24,6)  NOT NULL DEFAULT 0      COMMENT '人工报废数量',
    `material_scrap_quantity` decimal(24,6) NOT NULL DEFAULT 0     COMMENT '材料报废数量',
    `other_scrap_quantity`  decimal(24,6)  NOT NULL DEFAULT 0      COMMENT '其他报废数量',
    `feedback_user_id`      bigint         NOT NULL                COMMENT '报工人',
    `approve_user_id`       bigint         DEFAULT NULL            COMMENT '审批人',
    `status`                int            NOT NULL                COMMENT '状态',
    `remark`                varchar(500)   DEFAULT NULL,
    `create_time`           datetime       NOT NULL DEFAULT CURRENT_TIMESTAMP,
    `update_time`           datetime       NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    `creator`               varchar(64)    DEFAULT NULL,
    `updater`               varchar(64)    DEFAULT NULL,
    `deleted`               bit(1)         NOT NULL DEFAULT 0,
    PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='MES 生产报工表';

-- 5.12 流转卡表
DROP TABLE IF EXISTS `mes_pro_card`;
CREATE TABLE `mes_pro_card` (
    `id`                  bigint         NOT NULL AUTO_INCREMENT,
    `tenant_id`           bigint         NOT NULL DEFAULT 1,
    `code`                varchar(64)    NOT NULL                COMMENT '流转卡编号',
    `work_order_id`       bigint         NOT NULL                COMMENT '工单编号',
    `item_id`             bigint         NOT NULL                COMMENT '产品编号',
    `batch_code`          varchar(64)    DEFAULT NULL            COMMENT '批次号',
    `transfered_quantity` decimal(24,6)  NOT NULL DEFAULT 0      COMMENT '已流转数量',
    `status`              int            NOT NULL                COMMENT '状态',
    `remark`              varchar(500)   DEFAULT NULL,
    `create_time`         datetime       NOT NULL DEFAULT CURRENT_TIMESTAMP,
    `update_time`         datetime       NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    `creator`             varchar(64)    DEFAULT NULL,
    `updater`             varchar(64)    DEFAULT NULL,
    `deleted`             bit(1)         NOT NULL DEFAULT 0,
    PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='MES 流转卡表';

-- 5.13 流转卡工序表
DROP TABLE IF EXISTS `mes_pro_card_process`;
CREATE TABLE `mes_pro_card_process` (
    `id`                    bigint         NOT NULL AUTO_INCREMENT,
    `tenant_id`             bigint         NOT NULL DEFAULT 1,
    `card_id`               bigint         NOT NULL                COMMENT '流转卡编号',
    `sort`                  int            NOT NULL                COMMENT '排序',
    `process_id`            bigint         NOT NULL                COMMENT '工序编号',
    `input_time`            datetime       DEFAULT NULL            COMMENT '投入时间',
    `output_time`           datetime       DEFAULT NULL            COMMENT '产出时间',
    `input_quantity`        decimal(24,6)  DEFAULT NULL            COMMENT '投入数量',
    `output_quantity`       decimal(24,6)  DEFAULT NULL            COMMENT '产出数量',
    `unqualified_quantity`  decimal(24,6)  NOT NULL DEFAULT 0      COMMENT '不合格数量',
    `workstation_id`        bigint         DEFAULT NULL            COMMENT '工位编号',
    `user_id`               bigint         DEFAULT NULL            COMMENT '操作人',
    `ipqc_id`               bigint         DEFAULT NULL            COMMENT 'IPQC编号',
    `remark`                varchar(500)   DEFAULT NULL,
    `create_time`           datetime       NOT NULL DEFAULT CURRENT_TIMESTAMP,
    `update_time`           datetime       NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    `creator`               varchar(64)    DEFAULT NULL,
    `updater`               varchar(64)    DEFAULT NULL,
    `deleted`               bit(1)         NOT NULL DEFAULT 0,
    PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='MES 流转卡工序表';

-- 5.14 安灯配置表
DROP TABLE IF EXISTS `mes_pro_andon_config`;
CREATE TABLE `mes_pro_andon_config` (
    `id`              bigint       NOT NULL AUTO_INCREMENT,
    `tenant_id`       bigint       NOT NULL DEFAULT 1,
    `reason`          varchar(200) NOT NULL                COMMENT '原因',
    `level`           int          NOT NULL                COMMENT '级别',
    `handler_role_id` bigint       DEFAULT NULL            COMMENT '处理角色编号',
    `handler_user_id` bigint       DEFAULT NULL            COMMENT '处理人编号',
    `remark`          varchar(500) DEFAULT NULL,
    `create_time`     datetime     NOT NULL DEFAULT CURRENT_TIMESTAMP,
    `update_time`     datetime     NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    `creator`         varchar(64)  DEFAULT NULL,
    `updater`         varchar(64)  DEFAULT NULL,
    `deleted`         bit(1)       NOT NULL DEFAULT 0,
    PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='MES 安灯配置表';

-- 5.15 安灯记录表
DROP TABLE IF EXISTS `mes_pro_andon_record`;
CREATE TABLE `mes_pro_andon_record` (
    `id`              bigint       NOT NULL AUTO_INCREMENT,
    `tenant_id`       bigint       NOT NULL DEFAULT 1,
    `config_id`       bigint       NOT NULL                COMMENT '配置编号',
    `workstation_id`  bigint       NOT NULL                COMMENT '工位编号',
    `user_id`         bigint       NOT NULL                COMMENT '触发人',
    `work_order_id`   bigint       DEFAULT NULL            COMMENT '工单编号',
    `process_id`      bigint       DEFAULT NULL            COMMENT '工序编号',
    `reason`          varchar(200) NOT NULL                COMMENT '原因',
    `level`           int          NOT NULL                COMMENT '级别',
    `status`          int          NOT NULL                COMMENT '状态',
    `handle_time`     datetime     DEFAULT NULL            COMMENT '处理时间',
    `handler_user_id` bigint       DEFAULT NULL            COMMENT '处理人',
    `remark`          varchar(500) DEFAULT NULL,
    `create_time`     datetime     NOT NULL DEFAULT CURRENT_TIMESTAMP,
    `update_time`     datetime     NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    `creator`         varchar(64)  DEFAULT NULL,
    `updater`         varchar(64)  DEFAULT NULL,
    `deleted`         bit(1)       NOT NULL DEFAULT 0,
    PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='MES 安灯记录表';

-- 5.16 上岗记录表
DROP TABLE IF EXISTS `mes_pro_work_record`;
CREATE TABLE `mes_pro_work_record` (
    `id`             bigint       NOT NULL AUTO_INCREMENT,
    `tenant_id`      bigint       NOT NULL DEFAULT 1,
    `user_id`        bigint       NOT NULL                COMMENT '用户编号',
    `workstation_id` bigint       NOT NULL                COMMENT '工位编号',
    `type`           int          NOT NULL                COMMENT '上岗/下岗类型',
    `clock_in_time`  datetime     DEFAULT NULL            COMMENT '上岗时间',
    `clock_out_time` datetime     DEFAULT NULL            COMMENT '下岗时间',
    `remark`         varchar(500) DEFAULT NULL,
    `create_time`    datetime     NOT NULL DEFAULT CURRENT_TIMESTAMP,
    `update_time`    datetime     NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    `creator`        varchar(64)  DEFAULT NULL,
    `updater`        varchar(64)  DEFAULT NULL,
    `deleted`        bit(1)       NOT NULL DEFAULT 0,
    PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='MES 上岗记录表';

-- 5.17 上岗记录日志表
DROP TABLE IF EXISTS `mes_pro_work_record_log`;
CREATE TABLE `mes_pro_work_record_log` (
    `id`             bigint       NOT NULL AUTO_INCREMENT,
    `tenant_id`      bigint       NOT NULL DEFAULT 1,
    `user_id`        bigint       NOT NULL                COMMENT '用户编号',
    `workstation_id` bigint       NOT NULL                COMMENT '工位编号',
    `type`           int          NOT NULL                COMMENT '类型',
    `remark`         varchar(500) DEFAULT NULL,
    `create_time`    datetime     NOT NULL DEFAULT CURRENT_TIMESTAMP,
    `update_time`    datetime     NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    `creator`        varchar(64)  DEFAULT NULL,
    `updater`        varchar(64)  DEFAULT NULL,
    `deleted`        bit(1)       NOT NULL DEFAULT 0,
    PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='MES 上岗记录日志表';

-- ======================================================================================
-- 六、QC - 质量检验（16 表）
-- ======================================================================================

-- 6.1 缺陷表
DROP TABLE IF EXISTS `mes_qc_defect`;
CREATE TABLE `mes_qc_defect` (
    `id`          bigint       NOT NULL AUTO_INCREMENT,
    `tenant_id`   bigint       NOT NULL DEFAULT 1,
    `code`        varchar(64)  NOT NULL                COMMENT '缺陷编号',
    `name`        varchar(100) NOT NULL                COMMENT '缺陷名称',
    `type`        int          NOT NULL                COMMENT '类型',
    `level`       int          NOT NULL                COMMENT '级别',
    `remark`      varchar(500) DEFAULT NULL,
    `create_time` datetime     NOT NULL DEFAULT CURRENT_TIMESTAMP,
    `update_time` datetime     NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    `creator`     varchar(64)  DEFAULT NULL,
    `updater`     varchar(64)  DEFAULT NULL,
    `deleted`     bit(1)       NOT NULL DEFAULT 0,
    PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='MES 缺陷表';

-- 6.2 缺陷记录表
DROP TABLE IF EXISTS `mes_qc_defect_record`;
CREATE TABLE `mes_qc_defect_record` (
    `id`          bigint         NOT NULL AUTO_INCREMENT,
    `tenant_id`   bigint         NOT NULL DEFAULT 1,
    `qc_type`     int            NOT NULL                COMMENT '检验类型',
    `qc_id`       bigint         NOT NULL                COMMENT '检验单编号',
    `line_id`     bigint         DEFAULT NULL            COMMENT '行编号',
    `name`        varchar(100)   NOT NULL                COMMENT '缺陷名称',
    `level`       int            NOT NULL                COMMENT '级别',
    `quantity`    decimal(24,6)  NOT NULL                COMMENT '数量',
    `remark`      varchar(500)   DEFAULT NULL,
    `create_time` datetime       NOT NULL DEFAULT CURRENT_TIMESTAMP,
    `update_time` datetime       NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    `creator`     varchar(64)    DEFAULT NULL,
    `updater`     varchar(64)    DEFAULT NULL,
    `deleted`     bit(1)         NOT NULL DEFAULT 0,
    PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='MES 缺陷记录表';

-- 6.3 检测指标表
DROP TABLE IF EXISTS `mes_qc_indicator`;
CREATE TABLE `mes_qc_indicator` (
    `id`                  bigint       NOT NULL AUTO_INCREMENT,
    `tenant_id`           bigint       NOT NULL DEFAULT 1,
    `code`                varchar(64)  NOT NULL                COMMENT '指标编号',
    `name`                varchar(100) NOT NULL                COMMENT '指标名称',
    `type`                int          NOT NULL                COMMENT '类型',
    `tool`                varchar(50)  DEFAULT NULL            COMMENT '检测工具',
    `result_type`         int          DEFAULT NULL            COMMENT '结果类型',
    `result_specification` varchar(500) DEFAULT NULL           COMMENT '结果规格',
    `remark`              varchar(500) DEFAULT NULL,
    `create_time`         datetime     NOT NULL DEFAULT CURRENT_TIMESTAMP,
    `update_time`         datetime     NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    `creator`             varchar(64)  DEFAULT NULL,
    `updater`             varchar(64)  DEFAULT NULL,
    `deleted`             bit(1)       NOT NULL DEFAULT 0,
    PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='MES 检测指标表';

-- 6.4 检验模板表
DROP TABLE IF EXISTS `mes_qc_template`;
CREATE TABLE `mes_qc_template` (
    `id`          bigint       NOT NULL AUTO_INCREMENT,
    `tenant_id`   bigint       NOT NULL DEFAULT 1,
    `code`        varchar(64)  NOT NULL                COMMENT '模板编号',
    `name`        varchar(100) NOT NULL                COMMENT '模板名称',
    `types`       varchar(255)   DEFAULT NULL            COMMENT '适用检验类型列表（逗号分隔，IntegerListTypeHandler 序列化）',
    `status`      int          NOT NULL                COMMENT '状态',
    `remark`      varchar(500) DEFAULT NULL,
    `create_time` datetime     NOT NULL DEFAULT CURRENT_TIMESTAMP,
    `update_time` datetime     NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    `creator`     varchar(64)  DEFAULT NULL,
    `updater`     varchar(64)  DEFAULT NULL,
    `deleted`     bit(1)       NOT NULL DEFAULT 0,
    PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='MES 检验模板表';

-- 6.5 检验模板-指标表
DROP TABLE IF EXISTS `mes_qc_template_indicator`;
CREATE TABLE `mes_qc_template_indicator` (
    `id`              bigint         NOT NULL AUTO_INCREMENT,
    `tenant_id`       bigint         NOT NULL DEFAULT 1,
    `template_id`     bigint         NOT NULL                COMMENT '模板编号',
    `indicator_id`    bigint         NOT NULL                COMMENT '指标编号',
    `check_method`    varchar(50)    DEFAULT NULL            COMMENT '检验方式',
    `standard_value`  varchar(100)   DEFAULT NULL            COMMENT '标准值',
    `unit_measure_id` bigint         DEFAULT NULL            COMMENT '单位编号',
    `threshold_max`   decimal(24,6)  DEFAULT NULL            COMMENT '上限',
    `threshold_min`   decimal(24,6)  DEFAULT NULL            COMMENT '下限',
    `doc_url`         varchar(500)   DEFAULT NULL            COMMENT '文档URL',
    `remark`          varchar(500)   DEFAULT NULL,
    `create_time`     datetime       NOT NULL DEFAULT CURRENT_TIMESTAMP,
    `update_time`     datetime       NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    `creator`         varchar(64)    DEFAULT NULL,
    `updater`         varchar(64)    DEFAULT NULL,
    `deleted`         bit(1)         NOT NULL DEFAULT 0,
    PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='MES 检验模板-指标表';

-- 6.6 检验模板-物料表
DROP TABLE IF EXISTS `mes_qc_template_item`;
CREATE TABLE `mes_qc_template_item` (
    `id`                   bigint         NOT NULL AUTO_INCREMENT,
    `tenant_id`            bigint         NOT NULL DEFAULT 1,
    `template_id`          bigint         NOT NULL                COMMENT '模板编号',
    `item_id`              bigint         NOT NULL                COMMENT '物料编号',
    `quantity_check`       int            DEFAULT NULL            COMMENT '抽样数量',
    `quantity_unqualified` int            DEFAULT NULL            COMMENT '不合格允许数',
    `critical_rate`        decimal(5,2)   DEFAULT NULL            COMMENT '致命缺陷率',
    `major_rate`           decimal(5,2)   DEFAULT NULL            COMMENT '严重缺陷率',
    `minor_rate`           decimal(5,2)   DEFAULT NULL            COMMENT '轻微缺陷率',
    `remark`               varchar(500)   DEFAULT NULL,
    `create_time`          datetime       NOT NULL DEFAULT CURRENT_TIMESTAMP,
    `update_time`          datetime       NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    `creator`              varchar(64)    DEFAULT NULL,
    `updater`              varchar(64)    DEFAULT NULL,
    `deleted`              bit(1)         NOT NULL DEFAULT 0,
    PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='MES 检验模板-物料表';

-- 6.7 来料检验单表 (IQC)
DROP TABLE IF EXISTS `mes_qc_iqc`;
CREATE TABLE `mes_qc_iqc` (
    `id`                    bigint         NOT NULL AUTO_INCREMENT,
    `tenant_id`             bigint         NOT NULL DEFAULT 1,
    `code`                  varchar(64)    NOT NULL                COMMENT '检验单号',
    `name`                  varchar(100)   NOT NULL                COMMENT '检验名称',
    `template_id`           bigint         DEFAULT NULL            COMMENT '模板编号',
    `source_doc_type`       int            DEFAULT NULL            COMMENT '来源单据类型',
    `source_doc_id`         bigint         DEFAULT NULL            COMMENT '来源单据编号',
    `source_line_id`        bigint         DEFAULT NULL            COMMENT '来源行编号',
    `source_doc_code`       varchar(64)    DEFAULT NULL            COMMENT '来源单据号',
    `vendor_id`             bigint         DEFAULT NULL            COMMENT '供应商编号',
    `vendor_batch`          varchar(64)    DEFAULT NULL            COMMENT '供应商批次',
    `item_id`               bigint         NOT NULL                COMMENT '物料编号',
    `received_quantity`     decimal(24,6)  NOT NULL                COMMENT '收货数量',
    `check_quantity`        decimal(24,6)  NOT NULL                COMMENT '检验数量',
    `qualified_quantity`    decimal(24,6)  NOT NULL DEFAULT 0      COMMENT '合格数量',
    `unqualified_quantity`  decimal(24,6)  NOT NULL DEFAULT 0      COMMENT '不合格数量',
    `critical_rate`         decimal(5,2)   DEFAULT NULL            COMMENT '致命缺陷率',
    `major_rate`            decimal(5,2)   DEFAULT NULL            COMMENT '严重缺陷率',
    `minor_rate`            decimal(5,2)   DEFAULT NULL            COMMENT '轻微缺陷率',
    `critical_quantity`     decimal(24,6)  DEFAULT NULL            COMMENT '致命缺陷数量',
    `major_quantity`        decimal(24,6)  DEFAULT NULL            COMMENT '严重缺陷数量',
    `minor_quantity`        decimal(24,6)  DEFAULT NULL            COMMENT '轻微缺陷数量',
    `check_result`          int            DEFAULT NULL            COMMENT '检验结果',
    `receive_date`          date           DEFAULT NULL            COMMENT '收货日期',
    `inspect_date`          date           DEFAULT NULL            COMMENT '检验日期',
    `inspector_user_id`     bigint         DEFAULT NULL            COMMENT '检验人',
    `status`                int            NOT NULL                COMMENT '状态',
    `remark`                varchar(500)   DEFAULT NULL,
    `create_time`           datetime       NOT NULL DEFAULT CURRENT_TIMESTAMP,
    `update_time`           datetime       NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    `creator`               varchar(64)    DEFAULT NULL,
    `updater`               varchar(64)    DEFAULT NULL,
    `deleted`               bit(1)         NOT NULL DEFAULT 0,
    PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='MES 来料检验单表';

-- 6.8 来料检验单行表
DROP TABLE IF EXISTS `mes_qc_iqc_line`;
CREATE TABLE `mes_qc_iqc_line` (
    `id`                bigint         NOT NULL AUTO_INCREMENT,
    `tenant_id`         bigint         NOT NULL DEFAULT 1,
    `iqc_id`            bigint         NOT NULL                COMMENT 'IQC编号',
    `indicator_id`      bigint         NOT NULL                COMMENT '指标编号',
    `tool`              varchar(50)    DEFAULT NULL            COMMENT '工具',
    `check_method`      varchar(50)    DEFAULT NULL            COMMENT '检验方式',
    `standard_value`    varchar(100)   DEFAULT NULL            COMMENT '标准值',
    `unit_measure_id`   bigint         DEFAULT NULL            COMMENT '单位编号',
    `max_threshold`     decimal(24,6)  DEFAULT NULL            COMMENT '上限',
    `min_threshold`     decimal(24,6)  DEFAULT NULL            COMMENT '下限',
    `critical_quantity` decimal(24,6)  NOT NULL DEFAULT 0      COMMENT '致命缺陷数',
    `major_quantity`    decimal(24,6)  NOT NULL DEFAULT 0      COMMENT '严重缺陷数',
    `minor_quantity`    decimal(24,6)  NOT NULL DEFAULT 0      COMMENT '轻微缺陷数',
    `remark`            varchar(500)   DEFAULT NULL,
    `create_time`       datetime       NOT NULL DEFAULT CURRENT_TIMESTAMP,
    `update_time`       datetime       NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    `creator`           varchar(64)    DEFAULT NULL,
    `updater`           varchar(64)    DEFAULT NULL,
    `deleted`           bit(1)         NOT NULL DEFAULT 0,
    PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='MES 来料检验单行表';

-- 6.9 过程检验单表 (IPQC)
DROP TABLE IF EXISTS `mes_qc_ipqc`;
CREATE TABLE `mes_qc_ipqc` (
    `id`                    bigint         NOT NULL AUTO_INCREMENT,
    `tenant_id`             bigint         NOT NULL DEFAULT 1,
    `code`                  varchar(64)    NOT NULL                COMMENT '检验单号',
    `name`                  varchar(100)   NOT NULL                COMMENT '检验名称',
    `type`                  int            NOT NULL                COMMENT '检验类型',
    `template_id`           bigint         DEFAULT NULL            COMMENT '模板编号',
    `source_doc_type`       int            DEFAULT NULL            COMMENT '来源单据类型',
    `source_doc_id`         bigint         DEFAULT NULL            COMMENT '来源单据编号',
    `source_line_id`        bigint         DEFAULT NULL            COMMENT '来源行编号',
    `source_doc_code`       varchar(64)    DEFAULT NULL            COMMENT '来源单据号',
    `work_order_id`         bigint         DEFAULT NULL            COMMENT '工单编号',
    `task_id`               bigint         DEFAULT NULL            COMMENT '任务编号',
    `workstation_id`        bigint         DEFAULT NULL            COMMENT '工位编号',
    `process_id`            bigint         DEFAULT NULL            COMMENT '工序编号',
    `item_id`               bigint         NOT NULL                COMMENT '物料编号',
    `check_quantity`        decimal(24,6)  NOT NULL                COMMENT '检验数量',
    `qualified_quantity`    decimal(24,6)  NOT NULL DEFAULT 0      COMMENT '合格数量',
    `unqualified_quantity`  decimal(24,6)  NOT NULL DEFAULT 0      COMMENT '不合格数量',
    `labor_scrap_quantity`  decimal(24,6)  NOT NULL DEFAULT 0      COMMENT '人工报废数量',
    `material_scrap_quantity` decimal(24,6) NOT NULL DEFAULT 0     COMMENT '材料报废数量',
    `other_scrap_quantity`  decimal(24,6)  NOT NULL DEFAULT 0      COMMENT '其他报废数量',
    `critical_rate`         decimal(5,2)   DEFAULT NULL            COMMENT '致命缺陷率',
    `major_rate`            decimal(5,2)   DEFAULT NULL            COMMENT '严重缺陷率',
    `minor_rate`            decimal(5,2)   DEFAULT NULL            COMMENT '轻微缺陷率',
    `critical_quantity`     decimal(24,6)  DEFAULT NULL            COMMENT '致命缺陷数量',
    `major_quantity`        decimal(24,6)  DEFAULT NULL            COMMENT '严重缺陷数量',
    `minor_quantity`        decimal(24,6)  DEFAULT NULL            COMMENT '轻微缺陷数量',
    `check_result`          int            DEFAULT NULL            COMMENT '检验结果',
    `inspect_date`          date           DEFAULT NULL            COMMENT '检验日期',
    `inspector_user_id`     bigint         DEFAULT NULL            COMMENT '检验人',
    `status`                int            NOT NULL                COMMENT '状态',
    `remark`                varchar(500)   DEFAULT NULL,
    `create_time`           datetime       NOT NULL DEFAULT CURRENT_TIMESTAMP,
    `update_time`           datetime       NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    `creator`               varchar(64)    DEFAULT NULL,
    `updater`               varchar(64)    DEFAULT NULL,
    `deleted`               bit(1)         NOT NULL DEFAULT 0,
    PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='MES 过程检验单表';

-- 6.10 过程检验单行表
DROP TABLE IF EXISTS `mes_qc_ipqc_line`;
CREATE TABLE `mes_qc_ipqc_line` (
    `id`                bigint         NOT NULL AUTO_INCREMENT,
    `tenant_id`         bigint         NOT NULL DEFAULT 1,
    `ipqc_id`           bigint         NOT NULL                COMMENT 'IPQC编号',
    `indicator_id`      bigint         NOT NULL                COMMENT '指标编号',
    `tool`              varchar(50)    DEFAULT NULL            COMMENT '工具',
    `check_method`      varchar(50)    DEFAULT NULL            COMMENT '检验方式',
    `standard_value`    varchar(100)   DEFAULT NULL            COMMENT '标准值',
    `unit_measure_id`   bigint         DEFAULT NULL            COMMENT '单位编号',
    `max_threshold`     decimal(24,6)  DEFAULT NULL            COMMENT '上限',
    `min_threshold`     decimal(24,6)  DEFAULT NULL            COMMENT '下限',
    `critical_quantity` decimal(24,6)  NOT NULL DEFAULT 0      COMMENT '致命缺陷数',
    `major_quantity`    decimal(24,6)  NOT NULL DEFAULT 0      COMMENT '严重缺陷数',
    `minor_quantity`    decimal(24,6)  NOT NULL DEFAULT 0      COMMENT '轻微缺陷数',
    `remark`            varchar(500)   DEFAULT NULL,
    `create_time`       datetime       NOT NULL DEFAULT CURRENT_TIMESTAMP,
    `update_time`       datetime       NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    `creator`           varchar(64)    DEFAULT NULL,
    `updater`           varchar(64)    DEFAULT NULL,
    `deleted`           bit(1)         NOT NULL DEFAULT 0,
    PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='MES 过程检验单行表';

-- 6.11 出货检验单表 (OQC)
DROP TABLE IF EXISTS `mes_qc_oqc`;
CREATE TABLE `mes_qc_oqc` (
    `id`                      bigint         NOT NULL AUTO_INCREMENT,
    `tenant_id`               bigint         NOT NULL DEFAULT 1,
    `code`                    varchar(64)    NOT NULL                COMMENT '检验单号',
    `name`                    varchar(100)   NOT NULL                COMMENT '检验名称',
    `template_id`             bigint         DEFAULT NULL            COMMENT '模板编号',
    `source_doc_type`         int            DEFAULT NULL            COMMENT '来源单据类型',
    `source_doc_id`           bigint         DEFAULT NULL            COMMENT '来源单据编号',
    `source_line_id`          bigint         DEFAULT NULL            COMMENT '来源行编号',
    `source_doc_code`         varchar(64)    DEFAULT NULL            COMMENT '来源单据号',
    `client_id`               bigint         DEFAULT NULL            COMMENT '客户编号',
    `batch_code`              varchar(64)    DEFAULT NULL            COMMENT '批次号',
    `item_id`                 bigint         NOT NULL                COMMENT '物料编号',
    `min_check_quantity`      decimal(24,6)  DEFAULT NULL            COMMENT '最少检验数量',
    `max_unqualified_quantity` decimal(24,6) DEFAULT NULL            COMMENT '最大不合格数量',
    `out_quantity`            decimal(24,6)  NOT NULL                COMMENT '出货数量',
    `check_quantity`          decimal(24,6)  NOT NULL                COMMENT '检验数量',
    `qualified_quantity`      decimal(24,6)  NOT NULL DEFAULT 0      COMMENT '合格数量',
    `unqualified_quantity`    decimal(24,6)  NOT NULL DEFAULT 0      COMMENT '不合格数量',
    `critical_rate`           decimal(5,2)   DEFAULT NULL            COMMENT '致命缺陷率',
    `major_rate`              decimal(5,2)   DEFAULT NULL            COMMENT '严重缺陷率',
    `minor_rate`              decimal(5,2)   DEFAULT NULL            COMMENT '轻微缺陷率',
    `critical_quantity`       decimal(24,6)  DEFAULT NULL            COMMENT '致命缺陷数量',
    `major_quantity`          decimal(24,6)  DEFAULT NULL            COMMENT '严重缺陷数量',
    `minor_quantity`          decimal(24,6)  DEFAULT NULL            COMMENT '轻微缺陷数量',
    `check_result`            int            DEFAULT NULL            COMMENT '检验结果',
    `out_date`                date           DEFAULT NULL            COMMENT '出货日期',
    `inspect_date`            date           DEFAULT NULL            COMMENT '检验日期',
    `inspector_user_id`       bigint         DEFAULT NULL            COMMENT '检验人',
    `status`                  int            NOT NULL                COMMENT '状态',
    `remark`                  varchar(500)   DEFAULT NULL,
    `create_time`             datetime       NOT NULL DEFAULT CURRENT_TIMESTAMP,
    `update_time`             datetime       NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    `creator`                 varchar(64)    DEFAULT NULL,
    `updater`                 varchar(64)    DEFAULT NULL,
    `deleted`                 bit(1)         NOT NULL DEFAULT 0,
    PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='MES 出货检验单表';

-- 6.12 出货检验单行表
DROP TABLE IF EXISTS `mes_qc_oqc_line`;
CREATE TABLE `mes_qc_oqc_line` (
    `id`                bigint         NOT NULL AUTO_INCREMENT,
    `tenant_id`         bigint         NOT NULL DEFAULT 1,
    `oqc_id`            bigint         NOT NULL                COMMENT 'OQC编号',
    `indicator_id`      bigint         NOT NULL                COMMENT '指标编号',
    `tool`              varchar(50)    DEFAULT NULL            COMMENT '工具',
    `check_method`      varchar(50)    DEFAULT NULL            COMMENT '检验方式',
    `standard_value`    varchar(100)   DEFAULT NULL            COMMENT '标准值',
    `unit_measure_id`   bigint         DEFAULT NULL            COMMENT '单位编号',
    `max_threshold`     decimal(24,6)  DEFAULT NULL            COMMENT '上限',
    `min_threshold`     decimal(24,6)  DEFAULT NULL            COMMENT '下限',
    `critical_quantity` decimal(24,6)  NOT NULL DEFAULT 0      COMMENT '致命缺陷数',
    `major_quantity`    decimal(24,6)  NOT NULL DEFAULT 0      COMMENT '严重缺陷数',
    `minor_quantity`    decimal(24,6)  NOT NULL DEFAULT 0      COMMENT '轻微缺陷数',
    `remark`            varchar(500)   DEFAULT NULL,
    `create_time`       datetime       NOT NULL DEFAULT CURRENT_TIMESTAMP,
    `update_time`       datetime       NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    `creator`           varchar(64)    DEFAULT NULL,
    `updater`           varchar(64)    DEFAULT NULL,
    `deleted`           bit(1)         NOT NULL DEFAULT 0,
    PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='MES 出货检验单行表';

-- 6.13 退货检验单表 (RQC)
DROP TABLE IF EXISTS `mes_qc_rqc`;
CREATE TABLE `mes_qc_rqc` (
    `id`                    bigint         NOT NULL AUTO_INCREMENT,
    `tenant_id`             bigint         NOT NULL DEFAULT 1,
    `code`                  varchar(64)    NOT NULL                COMMENT '检验单号',
    `name`                  varchar(100)   NOT NULL                COMMENT '检验名称',
    `template_id`           bigint         DEFAULT NULL            COMMENT '模板编号',
    `source_doc_type`       int            DEFAULT NULL            COMMENT '来源单据类型',
    `source_doc_id`         bigint         DEFAULT NULL            COMMENT '来源单据编号',
    `source_line_id`        bigint         DEFAULT NULL            COMMENT '来源行编号',
    `source_doc_code`       varchar(64)    DEFAULT NULL            COMMENT '来源单据号',
    `type`                  int            NOT NULL                COMMENT '退货类型',
    `item_id`               bigint         NOT NULL                COMMENT '物料编号',
    `batch_code`            varchar(64)    DEFAULT NULL            COMMENT '批次号',
    `check_quantity`        decimal(24,6)  NOT NULL                COMMENT '检验数量',
    `qualified_quantity`    decimal(24,6)  NOT NULL DEFAULT 0      COMMENT '合格数量',
    `unqualified_quantity`  decimal(24,6)  NOT NULL DEFAULT 0      COMMENT '不合格数量',
    `critical_rate`         decimal(5,2)   DEFAULT NULL            COMMENT '致命缺陷率',
    `major_rate`            decimal(5,2)   DEFAULT NULL            COMMENT '严重缺陷率',
    `minor_rate`            decimal(5,2)   DEFAULT NULL            COMMENT '轻微缺陷率',
    `critical_quantity`     decimal(24,6)  DEFAULT NULL            COMMENT '致命缺陷数量',
    `major_quantity`        decimal(24,6)  DEFAULT NULL            COMMENT '严重缺陷数量',
    `minor_quantity`        decimal(24,6)  DEFAULT NULL            COMMENT '轻微缺陷数量',
    `check_result`          int            DEFAULT NULL            COMMENT '检验结果',
    `inspect_date`          date           DEFAULT NULL            COMMENT '检验日期',
    `inspector_user_id`     bigint         DEFAULT NULL            COMMENT '检验人',
    `status`                int            NOT NULL                COMMENT '状态',
    `remark`                varchar(500)   DEFAULT NULL,
    `create_time`           datetime       NOT NULL DEFAULT CURRENT_TIMESTAMP,
    `update_time`           datetime       NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    `creator`               varchar(64)    DEFAULT NULL,
    `updater`               varchar(64)    DEFAULT NULL,
    `deleted`               bit(1)         NOT NULL DEFAULT 0,
    PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='MES 退货检验单表';

-- 6.14 退货检验单行表
DROP TABLE IF EXISTS `mes_qc_rqc_line`;
CREATE TABLE `mes_qc_rqc_line` (
    `id`                bigint         NOT NULL AUTO_INCREMENT,
    `tenant_id`         bigint         NOT NULL DEFAULT 1,
    `rqc_id`            bigint         NOT NULL                COMMENT 'RQC编号',
    `indicator_id`      bigint         NOT NULL                COMMENT '指标编号',
    `tool`              varchar(50)    DEFAULT NULL            COMMENT '工具',
    `check_method`      varchar(50)    DEFAULT NULL            COMMENT '检验方式',
    `standard_value`    varchar(100)   DEFAULT NULL            COMMENT '标准值',
    `unit_measure_id`   bigint         DEFAULT NULL            COMMENT '单位编号',
    `max_threshold`     decimal(24,6)  DEFAULT NULL            COMMENT '上限',
    `min_threshold`     decimal(24,6)  DEFAULT NULL            COMMENT '下限',
    `critical_quantity` decimal(24,6)  NOT NULL DEFAULT 0      COMMENT '致命缺陷数',
    `major_quantity`    decimal(24,6)  NOT NULL DEFAULT 0      COMMENT '严重缺陷数',
    `minor_quantity`    decimal(24,6)  NOT NULL DEFAULT 0      COMMENT '轻微缺陷数',
    `remark`            varchar(500)   DEFAULT NULL,
    `create_time`       datetime       NOT NULL DEFAULT CURRENT_TIMESTAMP,
    `update_time`       datetime       NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    `creator`           varchar(64)    DEFAULT NULL,
    `updater`           varchar(64)    DEFAULT NULL,
    `deleted`           bit(1)         NOT NULL DEFAULT 0,
    PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='MES 退货检验单行表';

-- 6.15 检验指标结果表
DROP TABLE IF EXISTS `mes_qc_indicator_result`;
CREATE TABLE `mes_qc_indicator_result` (
    `id`          bigint       NOT NULL AUTO_INCREMENT,
    `tenant_id`   bigint       NOT NULL DEFAULT 1,
    `code`        varchar(64)  NOT NULL                COMMENT '结果编号',
    `qc_id`       bigint       NOT NULL                COMMENT '检验单编号',
    `qc_type`     int          NOT NULL                COMMENT '检验类型',
    `item_id`     bigint       NOT NULL                COMMENT '物料编号',
    `sn`          varchar(64)  DEFAULT NULL            COMMENT '序列号',
    `remark`      varchar(500) DEFAULT NULL,
    `create_time` datetime     NOT NULL DEFAULT CURRENT_TIMESTAMP,
    `update_time` datetime     NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    `creator`     varchar(64)  DEFAULT NULL,
    `updater`     varchar(64)  DEFAULT NULL,
    `deleted`     bit(1)       NOT NULL DEFAULT 0,
    PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='MES 检验指标结果表';

-- 6.16 检验指标结果明细表
DROP TABLE IF EXISTS `mes_qc_indicator_result_detail`;
CREATE TABLE `mes_qc_indicator_result_detail` (
    `id`           bigint         NOT NULL AUTO_INCREMENT,
    `tenant_id`    bigint         NOT NULL DEFAULT 1,
    `result_id`    bigint         NOT NULL                COMMENT '结果编号',
    `indicator_id` bigint         NOT NULL                COMMENT '指标编号',
    `value`        decimal(24,6)  NOT NULL                COMMENT '检测值',
    `remark`       varchar(500)   DEFAULT NULL,
    `create_time`  datetime       NOT NULL DEFAULT CURRENT_TIMESTAMP,
    `update_time`  datetime       NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    `creator`      varchar(64)    DEFAULT NULL,
    `updater`      varchar(64)    DEFAULT NULL,
    `deleted`      bit(1)         NOT NULL DEFAULT 0,
    PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='MES 检验指标结果明细表';

-- ======================================================================================
-- 七、WM - 仓储管理（62 表）
-- ======================================================================================

-- 7.1 仓库表
DROP TABLE IF EXISTS `mes_wm_warehouse`;
CREATE TABLE `mes_wm_warehouse` (
    `id`               bigint       NOT NULL AUTO_INCREMENT,
    `tenant_id`        bigint       NOT NULL DEFAULT 1,
    `code`             varchar(64)  NOT NULL                COMMENT '仓库编号',
    `name`             varchar(100) NOT NULL                COMMENT '仓库名称',
    `address`          varchar(255) DEFAULT NULL            COMMENT '地址',
    `area`             varchar(100) DEFAULT NULL            COMMENT '面积',
    `charge_user_id`   bigint       DEFAULT NULL            COMMENT '负责人编号',
    `frozen`           bit(1)       NOT NULL DEFAULT 0      COMMENT '冻结标识',
    `remark`           varchar(500) DEFAULT NULL,
    `create_time`      datetime     NOT NULL DEFAULT CURRENT_TIMESTAMP,
    `update_time`      datetime     NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    `creator`          varchar(64)  DEFAULT NULL,
    `updater`          varchar(64)  DEFAULT NULL,
    `deleted`          bit(1)       NOT NULL DEFAULT 0,
    PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='MES 仓库表';

-- 7.2 库位表
DROP TABLE IF EXISTS `mes_wm_warehouse_location`;
CREATE TABLE `mes_wm_warehouse_location` (
    `id`           bigint       NOT NULL AUTO_INCREMENT,
    `tenant_id`    bigint       NOT NULL DEFAULT 1,
    `code`         varchar(64)  NOT NULL                COMMENT '库位编号',
    `name`         varchar(100) NOT NULL                COMMENT '库位名称',
    `warehouse_id` bigint       NOT NULL                COMMENT '仓库编号',
    `area`         varchar(100) DEFAULT NULL            COMMENT '面积',
    `frozen`       bit(1)       NOT NULL DEFAULT 0      COMMENT '冻结标识',
    `remark`       varchar(500) DEFAULT NULL,
    `create_time`  datetime     NOT NULL DEFAULT CURRENT_TIMESTAMP,
    `update_time`  datetime     NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    `creator`      varchar(64)  DEFAULT NULL,
    `updater`      varchar(64)  DEFAULT NULL,
    `deleted`      bit(1)       NOT NULL DEFAULT 0,
    PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='MES 库位表';

-- 7.3 库区表
DROP TABLE IF EXISTS `mes_wm_warehouse_area`;
CREATE TABLE `mes_wm_warehouse_area` (
    `id`                 bigint       NOT NULL AUTO_INCREMENT,
    `tenant_id`          bigint       NOT NULL DEFAULT 1,
    `code`               varchar(64)  NOT NULL                COMMENT '库区编号',
    `name`               varchar(100) NOT NULL                COMMENT '库区名称',
    `location_id`        bigint       NOT NULL                COMMENT '库位编号',
    `area`               varchar(100) DEFAULT NULL            COMMENT '面积',
    `max_load`           decimal(24,6) DEFAULT NULL           COMMENT '最大载量',
    `position_x`         int          DEFAULT NULL            COMMENT 'X坐标',
    `position_y`         int          DEFAULT NULL            COMMENT 'Y坐标',
    `position_z`         int          DEFAULT NULL            COMMENT 'Z坐标',
    `status`             int          NOT NULL                COMMENT '状态',
    `frozen`             bit(1)       NOT NULL DEFAULT 0      COMMENT '冻结标识',
    `allow_item_mixing`  bit(1)       NOT NULL DEFAULT 1      COMMENT '允许混料',
    `allow_batch_mixing` bit(1)       NOT NULL DEFAULT 1      COMMENT '允许混批',
    `remark`             varchar(500) DEFAULT NULL,
    `create_time`        datetime     NOT NULL DEFAULT CURRENT_TIMESTAMP,
    `update_time`        datetime     NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    `creator`            varchar(64)  DEFAULT NULL,
    `updater`            varchar(64)  DEFAULT NULL,
    `deleted`            bit(1)       NOT NULL DEFAULT 0,
    PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='MES 库区表';

-- 7.4 物料库存表
DROP TABLE IF EXISTS `mes_wm_material_stock`;
CREATE TABLE `mes_wm_material_stock` (
    `id`            bigint         NOT NULL AUTO_INCREMENT,
    `tenant_id`     bigint         NOT NULL DEFAULT 1,
    `item_type_id`  bigint         NOT NULL                COMMENT '物料分类编号',
    `item_id`       bigint         NOT NULL                COMMENT '物料编号',
    `batch_id`      bigint         DEFAULT NULL            COMMENT '批次编号',
    `batch_code`    varchar(64)    DEFAULT NULL            COMMENT '批次号',
    `warehouse_id`  bigint         NOT NULL                COMMENT '仓库编号',
    `location_id`   bigint         NOT NULL                COMMENT '库位编号',
    `area_id`       bigint         NOT NULL                COMMENT '库区编号',
    `vendor_id`     bigint         DEFAULT NULL            COMMENT '供应商编号',
    `quantity`      decimal(24,6)  NOT NULL                COMMENT '库存数量',
    `receipt_time`  datetime       DEFAULT NULL            COMMENT '入库时间',
    `frozen`        bit(1)         NOT NULL DEFAULT 0      COMMENT '冻结标识',
    `create_time`   datetime       NOT NULL DEFAULT CURRENT_TIMESTAMP,
    `update_time`   datetime       NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    `creator`       varchar(64)    DEFAULT NULL,
    `updater`       varchar(64)    DEFAULT NULL,
    `deleted`       bit(1)         NOT NULL DEFAULT 0,
    PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='MES 物料库存表';

-- 7.5 库存交易流水表
DROP TABLE IF EXISTS `mes_wm_transaction`;
CREATE TABLE `mes_wm_transaction` (
    `id`                    bigint         NOT NULL AUTO_INCREMENT,
    `tenant_id`             bigint         NOT NULL DEFAULT 1,
    `type`                  int            NOT NULL                COMMENT '交易类型',
    `biz_type`              int            NOT NULL                COMMENT '业务类型',
    `biz_id`                bigint         NOT NULL                COMMENT '业务编号',
    `biz_code`              varchar(64)    NOT NULL                COMMENT '业务单号',
    `biz_line_id`           bigint         DEFAULT NULL            COMMENT '业务行编号',
    `material_stock_id`     bigint         NOT NULL                COMMENT '物料库存编号',
    `related_transaction_id` bigint        DEFAULT NULL            COMMENT '关联交易编号',
    `item_id`               bigint         NOT NULL                COMMENT '物料编号',
    `quantity`              decimal(24,6)  NOT NULL                COMMENT '数量',
    `batch_id`              bigint         DEFAULT NULL            COMMENT '批次编号',
    `batch_code`            varchar(64)    DEFAULT NULL            COMMENT '批次号',
    `warehouse_id`          bigint         NOT NULL                COMMENT '仓库编号',
    `location_id`           bigint         NOT NULL                COMMENT '库位编号',
    `area_id`               bigint         NOT NULL                COMMENT '库区编号',
    `transaction_time`      datetime       NOT NULL                COMMENT '交易时间',
    `erp_time`              datetime       DEFAULT NULL            COMMENT 'ERP同步时间',
    `receipt_time`          datetime       DEFAULT NULL            COMMENT '入库时间',
    `create_time`           datetime       NOT NULL DEFAULT CURRENT_TIMESTAMP,
    `update_time`           datetime       NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    `creator`               varchar(64)    DEFAULT NULL,
    `updater`               varchar(64)    DEFAULT NULL,
    `deleted`               bit(1)         NOT NULL DEFAULT 0,
    PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='MES 库存交易流水表';

-- 7.6 批次表
DROP TABLE IF EXISTS `mes_wm_batch`;
CREATE TABLE `mes_wm_batch` (
    `id`                  bigint       NOT NULL AUTO_INCREMENT,
    `tenant_id`           bigint       NOT NULL DEFAULT 1,
    `code`                varchar(64)  NOT NULL                COMMENT '批次号',
    `item_id`             bigint       NOT NULL                COMMENT '物料编号',
    `produce_date`        date         DEFAULT NULL            COMMENT '生产日期',
    `expire_date`         date         DEFAULT NULL            COMMENT '有效期',
    `receipt_date`        date         DEFAULT NULL            COMMENT '收货日期',
    `vendor_id`           bigint       DEFAULT NULL            COMMENT '供应商编号',
    `client_id`           bigint       DEFAULT NULL            COMMENT '客户编号',
    `sales_order_code`    varchar(64)  DEFAULT NULL            COMMENT '销售订单号',
    `purchase_order_code` varchar(64)  DEFAULT NULL            COMMENT '采购订单号',
    `work_order_id`       bigint       DEFAULT NULL            COMMENT '工单编号',
    `task_id`             bigint       DEFAULT NULL            COMMENT '任务编号',
    `workstation_id`      bigint       DEFAULT NULL            COMMENT '工位编号',
    `tool_id`             bigint       DEFAULT NULL            COMMENT '工具编号',
    `mold_id`             bigint       DEFAULT NULL            COMMENT '模具编号',
    `lot_number`          varchar(64)  DEFAULT NULL            COMMENT '批号',
    `quality_status`      varchar(20)  DEFAULT NULL            COMMENT '质量状态',
    `remark`              varchar(500) DEFAULT NULL,
    `create_time`         datetime     NOT NULL DEFAULT CURRENT_TIMESTAMP,
    `update_time`         datetime     NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    `creator`             varchar(64)  DEFAULT NULL,
    `updater`             varchar(64)  DEFAULT NULL,
    `deleted`             bit(1)       NOT NULL DEFAULT 0,
    PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='MES 批次表';

-- 7.7 序列号表
DROP TABLE IF EXISTS `mes_wm_sn`;
CREATE TABLE `mes_wm_sn` (
    `id`          bigint       NOT NULL AUTO_INCREMENT,
    `tenant_id`   bigint       NOT NULL DEFAULT 1,
    `uuid`        varchar(64)  NOT NULL                COMMENT 'UUID',
    `code`        varchar(64)  NOT NULL                COMMENT '序列号',
    `item_id`     bigint       NOT NULL                COMMENT '物料编号',
    `batch_code`  varchar(64)  DEFAULT NULL            COMMENT '批次号',
    `work_order_id` bigint     DEFAULT NULL            COMMENT '工单编号',
    `create_time` datetime     NOT NULL DEFAULT CURRENT_TIMESTAMP,
    `update_time` datetime     NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    `creator`     varchar(64)  DEFAULT NULL,
    `updater`     varchar(64)  DEFAULT NULL,
    `deleted`     bit(1)       NOT NULL DEFAULT 0,
    PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='MES 序列号表';

-- 7.8 条码配置表
DROP TABLE IF EXISTS `mes_wm_barcode_config`;
CREATE TABLE `mes_wm_barcode_config` (
    `id`                  bigint       NOT NULL AUTO_INCREMENT,
    `tenant_id`           bigint       NOT NULL DEFAULT 1,
    `format`              varchar(100) NOT NULL                COMMENT '条码格式',
    `biz_type`            int          NOT NULL                COMMENT '业务类型',
    `content_format`      varchar(500) DEFAULT NULL            COMMENT '内容格式',
    `content_example`     varchar(500) DEFAULT NULL            COMMENT '内容示例',
    `auto_generate_flag`  bit(1)       NOT NULL DEFAULT 0      COMMENT '自动生成标识',
    `default_template`    bit(1)       NOT NULL DEFAULT 0      COMMENT '默认模板标识',
    `status`              int          NOT NULL                COMMENT '状态',
    `remark`              varchar(500) DEFAULT NULL,
    `create_time`         datetime     NOT NULL DEFAULT CURRENT_TIMESTAMP,
    `update_time`         datetime     NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    `creator`             varchar(64)  DEFAULT NULL,
    `updater`             varchar(64)  DEFAULT NULL,
    `deleted`             bit(1)       NOT NULL DEFAULT 0,
    PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='MES 条码配置表';

-- 7.9 条码表
DROP TABLE IF EXISTS `mes_wm_barcode`;
CREATE TABLE `mes_wm_barcode` (
    `id`          bigint       NOT NULL AUTO_INCREMENT,
    `tenant_id`   bigint       NOT NULL DEFAULT 1,
    `config_id`   bigint       NOT NULL                COMMENT '配置编号',
    `format`      varchar(100) NOT NULL                COMMENT '条码格式',
    `biz_type`    int          NOT NULL                COMMENT '业务类型',
    `content`     varchar(200) NOT NULL                COMMENT '条码内容',
    `biz_id`      bigint       DEFAULT NULL            COMMENT '业务编号',
    `biz_code`    varchar(64)  DEFAULT NULL            COMMENT '业务单号',
    `biz_name`    varchar(100) DEFAULT NULL            COMMENT '业务名称',
    `status`      int          NOT NULL                COMMENT '状态',
    `remark`      varchar(500) DEFAULT NULL,
    `create_time` datetime     NOT NULL DEFAULT CURRENT_TIMESTAMP,
    `update_time` datetime     NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    `creator`     varchar(64)  DEFAULT NULL,
    `updater`     varchar(64)  DEFAULT NULL,
    `deleted`     bit(1)       NOT NULL DEFAULT 0,
    PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='MES 条码表';

-- 7.10 到货通知单表
DROP TABLE IF EXISTS `mes_wm_arrival_notice`;
CREATE TABLE `mes_wm_arrival_notice` (
    `id`                  bigint       NOT NULL AUTO_INCREMENT,
    `tenant_id`           bigint       NOT NULL DEFAULT 1,
    `code`                varchar(64)  NOT NULL                COMMENT '通知单号',
    `name`                varchar(100) NOT NULL                COMMENT '通知名称',
    `purchase_order_code` varchar(64)  DEFAULT NULL            COMMENT '采购订单号',
    `vendor_id`           bigint       DEFAULT NULL            COMMENT '供应商编号',
    `arrival_date`        date         DEFAULT NULL            COMMENT '到货日期',
    `contact_name`        varchar(50)  DEFAULT NULL            COMMENT '联系人',
    `contact_telephone`   varchar(20)  DEFAULT NULL            COMMENT '联系电话',
    `status`              int          NOT NULL                COMMENT '状态',
    `remark`              varchar(500) DEFAULT NULL,
    `create_time`         datetime     NOT NULL DEFAULT CURRENT_TIMESTAMP,
    `update_time`         datetime     NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    `creator`             varchar(64)  DEFAULT NULL,
    `updater`             varchar(64)  DEFAULT NULL,
    `deleted`             bit(1)       NOT NULL DEFAULT 0,
    PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='MES 到货通知单表';

-- 7.11 到货通知单行表
DROP TABLE IF EXISTS `mes_wm_arrival_notice_line`;
CREATE TABLE `mes_wm_arrival_notice_line` (
    `id`                bigint         NOT NULL AUTO_INCREMENT,
    `tenant_id`         bigint         NOT NULL DEFAULT 1,
    `notice_id`         bigint         NOT NULL                COMMENT '通知单编号',
    `item_id`           bigint         NOT NULL                COMMENT '物料编号',
    `arrival_quantity`  decimal(24,6)  NOT NULL                COMMENT '到货数量',
    `qualified_quantity` decimal(24,6) NOT NULL DEFAULT 0      COMMENT '合格数量',
    `iqc_check_flag`    bit(1)         NOT NULL DEFAULT 0      COMMENT 'IQC检验标识',
    `iqc_id`            bigint         DEFAULT NULL            COMMENT 'IQC编号',
    `remark`            varchar(500)   DEFAULT NULL,
    `create_time`       datetime       NOT NULL DEFAULT CURRENT_TIMESTAMP,
    `update_time`       datetime       NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    `creator`           varchar(64)    DEFAULT NULL,
    `updater`           varchar(64)    DEFAULT NULL,
    `deleted`           bit(1)         NOT NULL DEFAULT 0,
    PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='MES 到货通知单行表';

-- 7.12 物料入库单表
DROP TABLE IF EXISTS `mes_wm_item_receipt`;
CREATE TABLE `mes_wm_item_receipt` (
    `id`                  bigint       NOT NULL AUTO_INCREMENT,
    `tenant_id`           bigint       NOT NULL DEFAULT 1,
    `code`                varchar(64)  NOT NULL                COMMENT '入库单号',
    `name`                varchar(100) NOT NULL                COMMENT '入库名称',
    `iqc_id`              bigint       DEFAULT NULL            COMMENT 'IQC编号',
    `notice_id`           bigint       DEFAULT NULL            COMMENT '通知单编号',
    `purchase_order_code` varchar(64)  DEFAULT NULL            COMMENT '采购订单号',
    `vendor_id`           bigint       DEFAULT NULL            COMMENT '供应商编号',
    `receipt_date`        date         NOT NULL                COMMENT '入库日期',
    `status`              int          NOT NULL                COMMENT '状态',
    `remark`              varchar(500) DEFAULT NULL,
    `create_time`         datetime     NOT NULL DEFAULT CURRENT_TIMESTAMP,
    `update_time`         datetime     NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    `creator`             varchar(64)  DEFAULT NULL,
    `updater`             varchar(64)  DEFAULT NULL,
    `deleted`             bit(1)       NOT NULL DEFAULT 0,
    PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='MES 物料入库单表';

-- 7.13 物料入库单行表
DROP TABLE IF EXISTS `mes_wm_item_receipt_line`;
CREATE TABLE `mes_wm_item_receipt_line` (
    `id`                    bigint         NOT NULL AUTO_INCREMENT,
    `tenant_id`             bigint         NOT NULL DEFAULT 1,
    `receipt_id`            bigint         NOT NULL                COMMENT '入库单编号',
    `arrival_notice_line_id` bigint        DEFAULT NULL            COMMENT '到货通知行编号',
    `item_id`               bigint         NOT NULL                COMMENT '物料编号',
    `received_quantity`     decimal(24,6)  NOT NULL                COMMENT '收货数量',
    `batch_id`              bigint         DEFAULT NULL            COMMENT '批次编号',
    `batch_code`            varchar(64)    DEFAULT NULL            COMMENT '批次号',
    `production_date`       date           DEFAULT NULL            COMMENT '生产日期',
    `expire_date`           date           DEFAULT NULL            COMMENT '有效期',
    `lot_number`            varchar(64)    DEFAULT NULL            COMMENT '批号',
    `remark`                varchar(500)   DEFAULT NULL,
    `create_time`           datetime       NOT NULL DEFAULT CURRENT_TIMESTAMP,
    `update_time`           datetime       NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    `creator`               varchar(64)    DEFAULT NULL,
    `updater`               varchar(64)    DEFAULT NULL,
    `deleted`               bit(1)         NOT NULL DEFAULT 0,
    PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='MES 物料入库单行表';

-- 7.14 物料入库单明细表
DROP TABLE IF EXISTS `mes_wm_item_receipt_detail`;
CREATE TABLE `mes_wm_item_receipt_detail` (
    `id`            bigint         NOT NULL AUTO_INCREMENT,
    `tenant_id`     bigint         NOT NULL DEFAULT 1,
    `line_id`       bigint         NOT NULL                COMMENT '行编号',
    `receipt_id`    bigint         NOT NULL                COMMENT '入库单编号',
    `item_id`       bigint         NOT NULL                COMMENT '物料编号',
    `quantity`      decimal(24,6)  NOT NULL                COMMENT '数量',
    `batch_id`      bigint         DEFAULT NULL            COMMENT '批次编号',
    `warehouse_id`  bigint         NOT NULL                COMMENT '仓库编号',
    `location_id`   bigint         NOT NULL                COMMENT '库位编号',
    `area_id`       bigint         NOT NULL                COMMENT '库区编号',
    `remark`        varchar(500)   DEFAULT NULL,
    `create_time`   datetime       NOT NULL DEFAULT CURRENT_TIMESTAMP,
    `update_time`   datetime       NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    `creator`       varchar(64)    DEFAULT NULL,
    `updater`       varchar(64)    DEFAULT NULL,
    `deleted`       bit(1)         NOT NULL DEFAULT 0,
    PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='MES 物料入库单明细表';

-- 7.15 产品入库单表
DROP TABLE IF EXISTS `mes_wm_product_receipt`;
CREATE TABLE `mes_wm_product_receipt` (
    `id`            bigint       NOT NULL AUTO_INCREMENT,
    `tenant_id`     bigint       NOT NULL DEFAULT 1,
    `code`          varchar(64)  NOT NULL                COMMENT '入库单号',
    `name`          varchar(100) NOT NULL                COMMENT '入库名称',
    `work_order_id` bigint       NOT NULL                COMMENT '工单编号',
    `item_id`       bigint       NOT NULL                COMMENT '产品编号',
    `receipt_date`  date         NOT NULL                COMMENT '入库日期',
    `status`        int          NOT NULL                COMMENT '状态',
    `remark`        varchar(500) DEFAULT NULL,
    `create_time`   datetime     NOT NULL DEFAULT CURRENT_TIMESTAMP,
    `update_time`   datetime     NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    `creator`       varchar(64)  DEFAULT NULL,
    `updater`       varchar(64)  DEFAULT NULL,
    `deleted`       bit(1)       NOT NULL DEFAULT 0,
    PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='MES 产品入库单表';

-- 7.16 产品入库单行表
DROP TABLE IF EXISTS `mes_wm_product_receipt_line`;
CREATE TABLE `mes_wm_product_receipt_line` (
    `id`                bigint         NOT NULL AUTO_INCREMENT,
    `tenant_id`         bigint         NOT NULL DEFAULT 1,
    `receipt_id`        bigint         NOT NULL                COMMENT '入库单编号',
    `item_id`           bigint         NOT NULL                COMMENT '物料编号',
    `material_stock_id` bigint         NOT NULL                COMMENT '物料库存编号',
    `quantity`          decimal(24,6)  NOT NULL                COMMENT '数量',
    `batch_id`          bigint         DEFAULT NULL            COMMENT '批次编号',
    `batch_code`        varchar(64)    DEFAULT NULL            COMMENT '批次号',
    `remark`            varchar(500)   DEFAULT NULL,
    `create_time`       datetime       NOT NULL DEFAULT CURRENT_TIMESTAMP,
    `update_time`       datetime       NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    `creator`           varchar(64)    DEFAULT NULL,
    `updater`           varchar(64)    DEFAULT NULL,
    `deleted`           bit(1)         NOT NULL DEFAULT 0,
    PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='MES 产品入库单行表';

-- 7.17 产品入库单明细表
DROP TABLE IF EXISTS `mes_wm_product_receipt_detail`;
CREATE TABLE `mes_wm_product_receipt_detail` (
    `id`            bigint         NOT NULL AUTO_INCREMENT,
    `tenant_id`     bigint         NOT NULL DEFAULT 1,
    `line_id`       bigint         NOT NULL                COMMENT '行编号',
    `receipt_id`    bigint         NOT NULL                COMMENT '入库单编号',
    `item_id`       bigint         NOT NULL                COMMENT '物料编号',
    `quantity`      decimal(24,6)  NOT NULL                COMMENT '数量',
    `batch_id`      bigint         DEFAULT NULL            COMMENT '批次编号',
    `warehouse_id`  bigint         NOT NULL                COMMENT '仓库编号',
    `location_id`   bigint         NOT NULL                COMMENT '库位编号',
    `area_id`       bigint         NOT NULL                COMMENT '库区编号',
    `remark`        varchar(500)   DEFAULT NULL,
    `create_time`   datetime       NOT NULL DEFAULT CURRENT_TIMESTAMP,
    `update_time`   datetime       NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    `creator`       varchar(64)    DEFAULT NULL,
    `updater`       varchar(64)    DEFAULT NULL,
    `deleted`       bit(1)         NOT NULL DEFAULT 0,
    PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='MES 产品入库单明细表';

-- 7.18 产品生产入库单表
DROP TABLE IF EXISTS `mes_wm_product_produce`;
CREATE TABLE `mes_wm_product_produce` (
    `id`             bigint       NOT NULL AUTO_INCREMENT,
    `tenant_id`      bigint       NOT NULL DEFAULT 1,
    `work_order_id`  bigint       NOT NULL                COMMENT '工单编号',
    `feedback_id`    bigint       NOT NULL                COMMENT '报工编号',
    `task_id`        bigint       NOT NULL                COMMENT '任务编号',
    `workstation_id` bigint       NOT NULL                COMMENT '工位编号',
    `process_id`     bigint       NOT NULL                COMMENT '工序编号',
    `produce_date`   date         NOT NULL                COMMENT '生产日期',
    `status`         int          NOT NULL                COMMENT '状态',
    `remark`         varchar(500) DEFAULT NULL,
    `create_time`    datetime     NOT NULL DEFAULT CURRENT_TIMESTAMP,
    `update_time`    datetime     NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    `creator`        varchar(64)  DEFAULT NULL,
    `updater`        varchar(64)  DEFAULT NULL,
    `deleted`        bit(1)       NOT NULL DEFAULT 0,
    PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='MES 产品生产入库单表';

-- 7.19 产品生产入库单行表
DROP TABLE IF EXISTS `mes_wm_product_produce_line`;
CREATE TABLE `mes_wm_product_produce_line` (
    `id`              bigint         NOT NULL AUTO_INCREMENT,
    `tenant_id`       bigint         NOT NULL DEFAULT 1,
    `produce_id`      bigint         NOT NULL                COMMENT '生产入库编号',
    `feedback_id`     bigint         NOT NULL                COMMENT '报工编号',
    `item_id`         bigint         NOT NULL                COMMENT '物料编号',
    `quantity`        decimal(24,6)  NOT NULL                COMMENT '数量',
    `batch_id`        bigint         DEFAULT NULL            COMMENT '批次编号',
    `batch_code`      varchar(64)    DEFAULT NULL            COMMENT '批次号',
    `expire_date`     date           DEFAULT NULL            COMMENT '有效期',
    `lot_number`      varchar(64)    DEFAULT NULL            COMMENT '批号',
    `quality_status`  varchar(20)    DEFAULT NULL            COMMENT '质量状态',
    `remark`          varchar(500)   DEFAULT NULL,
    `create_time`     datetime       NOT NULL DEFAULT CURRENT_TIMESTAMP,
    `update_time`     datetime       NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    `creator`         varchar(64)    DEFAULT NULL,
    `updater`         varchar(64)    DEFAULT NULL,
    `deleted`         bit(1)         NOT NULL DEFAULT 0,
    PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='MES 产品生产入库单行表';

-- 7.20 产品生产入库单明细表
DROP TABLE IF EXISTS `mes_wm_product_produce_detail`;
CREATE TABLE `mes_wm_product_produce_detail` (
    `id`            bigint         NOT NULL AUTO_INCREMENT,
    `tenant_id`     bigint         NOT NULL DEFAULT 1,
    `produce_id`    bigint         NOT NULL                COMMENT '生产入库编号',
    `line_id`       bigint         NOT NULL                COMMENT '行编号',
    `item_id`       bigint         NOT NULL                COMMENT '物料编号',
    `quantity`      decimal(24,6)  NOT NULL                COMMENT '数量',
    `batch_id`      bigint         DEFAULT NULL            COMMENT '批次编号',
    `batch_code`    varchar(64)    DEFAULT NULL            COMMENT '批次号',
    `warehouse_id`  bigint         NOT NULL                COMMENT '仓库编号',
    `location_id`   bigint         NOT NULL                COMMENT '库位编号',
    `area_id`       bigint         NOT NULL                COMMENT '库区编号',
    `remark`        varchar(500)   DEFAULT NULL,
    `create_time`   datetime       NOT NULL DEFAULT CURRENT_TIMESTAMP,
    `update_time`   datetime       NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    `creator`       varchar(64)    DEFAULT NULL,
    `updater`       varchar(64)    DEFAULT NULL,
    `deleted`       bit(1)         NOT NULL DEFAULT 0,
    PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='MES 产品生产入库单明细表';

-- 7.21 物料消耗单表
DROP TABLE IF EXISTS `mes_wm_item_consume`;
CREATE TABLE `mes_wm_item_consume` (
    `id`             bigint       NOT NULL AUTO_INCREMENT,
    `tenant_id`      bigint       NOT NULL DEFAULT 1,
    `work_order_id`  bigint       NOT NULL                COMMENT '工单编号',
    `task_id`        bigint       NOT NULL                COMMENT '任务编号',
    `workstation_id` bigint       NOT NULL                COMMENT '工位编号',
    `process_id`     bigint       NOT NULL                COMMENT '工序编号',
    `feedback_id`    bigint       NOT NULL                COMMENT '报工编号',
    `consume_date`   date         NOT NULL                COMMENT '消耗日期',
    `status`         int          NOT NULL                COMMENT '状态',
    `remark`         varchar(500) DEFAULT NULL,
    `create_time`    datetime     NOT NULL DEFAULT CURRENT_TIMESTAMP,
    `update_time`    datetime     NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    `creator`        varchar(64)  DEFAULT NULL,
    `updater`        varchar(64)  DEFAULT NULL,
    `deleted`        bit(1)       NOT NULL DEFAULT 0,
    PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='MES 物料消耗单表';

-- 7.22 物料消耗单行表
DROP TABLE IF EXISTS `mes_wm_item_consume_line`;
CREATE TABLE `mes_wm_item_consume_line` (
    `id`          bigint         NOT NULL AUTO_INCREMENT,
    `tenant_id`   bigint         NOT NULL DEFAULT 1,
    `consume_id`  bigint         NOT NULL                COMMENT '消耗单编号',
    `item_id`     bigint         NOT NULL                COMMENT '物料编号',
    `quantity`    decimal(24,6)  NOT NULL                COMMENT '数量',
    `batch_id`    bigint         DEFAULT NULL            COMMENT '批次编号',
    `batch_code`  varchar(64)    DEFAULT NULL            COMMENT '批次号',
    `remark`      varchar(500)   DEFAULT NULL,
    `create_time` datetime       NOT NULL DEFAULT CURRENT_TIMESTAMP,
    `update_time` datetime       NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    `creator`     varchar(64)    DEFAULT NULL,
    `updater`     varchar(64)    DEFAULT NULL,
    `deleted`     bit(1)         NOT NULL DEFAULT 0,
    PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='MES 物料消耗单行表';

-- 7.23 物料消耗单明细表
DROP TABLE IF EXISTS `mes_wm_item_consume_detail`;
CREATE TABLE `mes_wm_item_consume_detail` (
    `id`                bigint         NOT NULL AUTO_INCREMENT,
    `tenant_id`         bigint         NOT NULL DEFAULT 1,
    `consume_id`        bigint         NOT NULL                COMMENT '消耗单编号',
    `line_id`           bigint         NOT NULL                COMMENT '行编号',
    `material_stock_id` bigint         NOT NULL                COMMENT '物料库存编号',
    `item_id`           bigint         NOT NULL                COMMENT '物料编号',
    `quantity`          decimal(24,6)  NOT NULL                COMMENT '数量',
    `batch_id`          bigint         DEFAULT NULL            COMMENT '批次编号',
    `batch_code`        varchar(64)    DEFAULT NULL            COMMENT '批次号',
    `warehouse_id`      bigint         NOT NULL                COMMENT '仓库编号',
    `location_id`       bigint         NOT NULL                COMMENT '库位编号',
    `area_id`           bigint         NOT NULL                COMMENT '库区编号',
    `remark`            varchar(500)   DEFAULT NULL,
    `create_time`       datetime       NOT NULL DEFAULT CURRENT_TIMESTAMP,
    `update_time`       datetime       NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    `creator`           varchar(64)    DEFAULT NULL,
    `updater`           varchar(64)    DEFAULT NULL,
    `deleted`           bit(1)         NOT NULL DEFAULT 0,
    PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='MES 物料消耗单明细表';

-- 7.24 生产领料单表
DROP TABLE IF EXISTS `mes_wm_product_issue`;
CREATE TABLE `mes_wm_product_issue` (
    `id`             bigint       NOT NULL AUTO_INCREMENT,
    `tenant_id`      bigint       NOT NULL DEFAULT 1,
    `code`           varchar(64)  NOT NULL                COMMENT '领料单号',
    `name`           varchar(100) NOT NULL                COMMENT '领料名称',
    `workstation_id` bigint       NOT NULL                COMMENT '工位编号',
    `work_order_id`  bigint       NOT NULL                COMMENT '工单编号',
    `task_id`        bigint       NOT NULL                COMMENT '任务编号',
    `issue_date`     date         NOT NULL                COMMENT '领料日期',
    `required_time`  datetime     DEFAULT NULL            COMMENT '需求时间',
    `status`         int          NOT NULL                COMMENT '状态',
    `remark`         varchar(500) DEFAULT NULL,
    `create_time`    datetime     NOT NULL DEFAULT CURRENT_TIMESTAMP,
    `update_time`    datetime     NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    `creator`        varchar(64)  DEFAULT NULL,
    `updater`        varchar(64)  DEFAULT NULL,
    `deleted`        bit(1)       NOT NULL DEFAULT 0,
    PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='MES 生产领料单表';

-- 7.25 生产领料单行表
DROP TABLE IF EXISTS `mes_wm_product_issue_line`;
CREATE TABLE `mes_wm_product_issue_line` (
    `id`          bigint         NOT NULL AUTO_INCREMENT,
    `tenant_id`   bigint         NOT NULL DEFAULT 1,
    `issue_id`    bigint         NOT NULL                COMMENT '领料单编号',
    `item_id`     bigint         NOT NULL                COMMENT '物料编号',
    `quantity`    decimal(24,6)  NOT NULL                COMMENT '数量',
    `batch_id`    bigint         DEFAULT NULL            COMMENT '批次编号',
    `remark`      varchar(500)   DEFAULT NULL,
    `create_time` datetime       NOT NULL DEFAULT CURRENT_TIMESTAMP,
    `update_time` datetime       NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    `creator`     varchar(64)    DEFAULT NULL,
    `updater`     varchar(64)    DEFAULT NULL,
    `deleted`     bit(1)         NOT NULL DEFAULT 0,
    PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='MES 生产领料单行表';

-- 7.26 生产领料单明细表
DROP TABLE IF EXISTS `mes_wm_product_issue_detail`;
CREATE TABLE `mes_wm_product_issue_detail` (
    `id`                bigint         NOT NULL AUTO_INCREMENT,
    `tenant_id`         bigint         NOT NULL DEFAULT 1,
    `issue_id`          bigint         NOT NULL                COMMENT '领料单编号',
    `line_id`           bigint         NOT NULL                COMMENT '行编号',
    `material_stock_id` bigint         NOT NULL                COMMENT '物料库存编号',
    `item_id`           bigint         NOT NULL                COMMENT '物料编号',
    `quantity`          decimal(24,6)  NOT NULL                COMMENT '数量',
    `batch_id`          bigint         DEFAULT NULL            COMMENT '批次编号',
    `batch_code`        varchar(64)    DEFAULT NULL            COMMENT '批次号',
    `warehouse_id`      bigint         NOT NULL                COMMENT '仓库编号',
    `location_id`       bigint         NOT NULL                COMMENT '库位编号',
    `area_id`           bigint         NOT NULL                COMMENT '库区编号',
    `remark`            varchar(500)   DEFAULT NULL,
    `create_time`       datetime       NOT NULL DEFAULT CURRENT_TIMESTAMP,
    `update_time`       datetime       NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    `creator`           varchar(64)    DEFAULT NULL,
    `updater`           varchar(64)    DEFAULT NULL,
    `deleted`           bit(1)         NOT NULL DEFAULT 0,
    PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='MES 生产领料单明细表';

-- 7.27 杂项入库单表
DROP TABLE IF EXISTS `mes_wm_misc_receipt`;
CREATE TABLE `mes_wm_misc_receipt` (
    `id`              bigint       NOT NULL AUTO_INCREMENT,
    `tenant_id`       bigint       NOT NULL DEFAULT 1,
    `code`            varchar(64)  NOT NULL                COMMENT '入库单号',
    `name`            varchar(100) NOT NULL                COMMENT '入库名称',
    `type`            int          NOT NULL                COMMENT '类型',
    `source_doc_type` int          DEFAULT NULL            COMMENT '来源单据类型',
    `source_doc_id`   bigint       DEFAULT NULL            COMMENT '来源单据编号',
    `source_doc_code` varchar(64)  DEFAULT NULL            COMMENT '来源单据号',
    `receipt_date`    date         NOT NULL                COMMENT '入库日期',
    `status`          int          NOT NULL                COMMENT '状态',
    `remark`          varchar(500) DEFAULT NULL,
    `create_time`     datetime     NOT NULL DEFAULT CURRENT_TIMESTAMP,
    `update_time`     datetime     NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    `creator`         varchar(64)  DEFAULT NULL,
    `updater`         varchar(64)  DEFAULT NULL,
    `deleted`         bit(1)       NOT NULL DEFAULT 0,
    PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='MES 杂项入库单表';

-- 7.28 杂项入库单行表
DROP TABLE IF EXISTS `mes_wm_misc_receipt_line`;
CREATE TABLE `mes_wm_misc_receipt_line` (
    `id`            bigint         NOT NULL AUTO_INCREMENT,
    `tenant_id`     bigint         NOT NULL DEFAULT 1,
    `receipt_id`    bigint         NOT NULL                COMMENT '入库单编号',
    `item_id`       bigint         NOT NULL                COMMENT '物料编号',
    `quantity`      decimal(24,6)  NOT NULL                COMMENT '数量',
    `batch_code`    varchar(64)    DEFAULT NULL            COMMENT '批次号',
    `warehouse_id`  bigint         NOT NULL                COMMENT '仓库编号',
    `location_id`   bigint         NOT NULL                COMMENT '库位编号',
    `area_id`       bigint         NOT NULL                COMMENT '库区编号',
    `remark`        varchar(500)   DEFAULT NULL,
    `create_time`   datetime       NOT NULL DEFAULT CURRENT_TIMESTAMP,
    `update_time`   datetime       NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    `creator`       varchar(64)    DEFAULT NULL,
    `updater`       varchar(64)    DEFAULT NULL,
    `deleted`       bit(1)         NOT NULL DEFAULT 0,
    PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='MES 杂项入库单行表';

-- 7.29 杂项入库单明细表
DROP TABLE IF EXISTS `mes_wm_misc_receipt_detail`;
CREATE TABLE `mes_wm_misc_receipt_detail` (
    `id`            bigint         NOT NULL AUTO_INCREMENT,
    `tenant_id`     bigint         NOT NULL DEFAULT 1,
    `receipt_id`    bigint         NOT NULL                COMMENT '入库单编号',
    `line_id`       bigint         NOT NULL                COMMENT '行编号',
    `item_id`       bigint         NOT NULL                COMMENT '物料编号',
    `quantity`      decimal(24,6)  NOT NULL                COMMENT '数量',
    `batch_code`    varchar(64)    DEFAULT NULL            COMMENT '批次号',
    `warehouse_id`  bigint         NOT NULL                COMMENT '仓库编号',
    `location_id`   bigint         NOT NULL                COMMENT '库位编号',
    `area_id`       bigint         NOT NULL                COMMENT '库区编号',
    `remark`        varchar(500)   DEFAULT NULL,
    `create_time`   datetime       NOT NULL DEFAULT CURRENT_TIMESTAMP,
    `update_time`   datetime       NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    `creator`       varchar(64)    DEFAULT NULL,
    `updater`       varchar(64)    DEFAULT NULL,
    `deleted`       bit(1)         NOT NULL DEFAULT 0,
    PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='MES 杂项入库单明细表';

-- 7.30 杂项出库单表
DROP TABLE IF EXISTS `mes_wm_misc_issue`;
CREATE TABLE `mes_wm_misc_issue` (
    `id`              bigint       NOT NULL AUTO_INCREMENT,
    `tenant_id`       bigint       NOT NULL DEFAULT 1,
    `code`            varchar(64)  NOT NULL                COMMENT '出库单号',
    `name`            varchar(100) NOT NULL                COMMENT '出库名称',
    `type`            int          NOT NULL                COMMENT '类型',
    `source_doc_type` int          DEFAULT NULL            COMMENT '来源单据类型',
    `source_doc_id`   bigint       DEFAULT NULL            COMMENT '来源单据编号',
    `source_doc_code` varchar(64)  DEFAULT NULL            COMMENT '来源单据号',
    `issue_date`      date         NOT NULL                COMMENT '出库日期',
    `status`          int          NOT NULL                COMMENT '状态',
    `remark`          varchar(500) DEFAULT NULL,
    `create_time`     datetime     NOT NULL DEFAULT CURRENT_TIMESTAMP,
    `update_time`     datetime     NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    `creator`         varchar(64)  DEFAULT NULL,
    `updater`         varchar(64)  DEFAULT NULL,
    `deleted`         bit(1)       NOT NULL DEFAULT 0,
    PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='MES 杂项出库单表';

-- 7.31 杂项出库单行表
DROP TABLE IF EXISTS `mes_wm_misc_issue_line`;
CREATE TABLE `mes_wm_misc_issue_line` (
    `id`                bigint         NOT NULL AUTO_INCREMENT,
    `tenant_id`         bigint         NOT NULL DEFAULT 1,
    `issue_id`          bigint         NOT NULL                COMMENT '出库单编号',
    `source_doc_line_id` bigint        DEFAULT NULL            COMMENT '来源行编号',
    `material_stock_id` bigint         NOT NULL                COMMENT '物料库存编号',
    `item_id`           bigint         NOT NULL                COMMENT '物料编号',
    `quantity`          decimal(24,6)  NOT NULL                COMMENT '数量',
    `batch_id`          bigint         DEFAULT NULL            COMMENT '批次编号',
    `batch_code`        varchar(64)    DEFAULT NULL            COMMENT '批次号',
    `warehouse_id`      bigint         NOT NULL                COMMENT '仓库编号',
    `location_id`       bigint         NOT NULL                COMMENT '库位编号',
    `area_id`           bigint         NOT NULL                COMMENT '库区编号',
    `remark`            varchar(500)   DEFAULT NULL,
    `create_time`       datetime       NOT NULL DEFAULT CURRENT_TIMESTAMP,
    `update_time`       datetime       NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    `creator`           varchar(64)    DEFAULT NULL,
    `updater`           varchar(64)    DEFAULT NULL,
    `deleted`           bit(1)         NOT NULL DEFAULT 0,
    PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='MES 杂项出库单行表';

-- 7.32 杂项出库单明细表
DROP TABLE IF EXISTS `mes_wm_misc_issue_detail`;
CREATE TABLE `mes_wm_misc_issue_detail` (
    `id`                bigint         NOT NULL AUTO_INCREMENT,
    `tenant_id`         bigint         NOT NULL DEFAULT 1,
    `issue_id`          bigint         NOT NULL                COMMENT '出库单编号',
    `line_id`           bigint         NOT NULL                COMMENT '行编号',
    `material_stock_id` bigint         NOT NULL                COMMENT '物料库存编号',
    `item_id`           bigint         NOT NULL                COMMENT '物料编号',
    `quantity`          decimal(24,6)  NOT NULL                COMMENT '数量',
    `batch_id`          bigint         DEFAULT NULL            COMMENT '批次编号',
    `batch_code`        varchar(64)    DEFAULT NULL            COMMENT '批次号',
    `warehouse_id`      bigint         NOT NULL                COMMENT '仓库编号',
    `location_id`       bigint         NOT NULL                COMMENT '库位编号',
    `area_id`           bigint         NOT NULL                COMMENT '库区编号',
    `remark`            varchar(500)   DEFAULT NULL,
    `create_time`       datetime       NOT NULL DEFAULT CURRENT_TIMESTAMP,
    `update_time`       datetime       NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    `creator`           varchar(64)    DEFAULT NULL,
    `updater`           varchar(64)    DEFAULT NULL,
    `deleted`           bit(1)         NOT NULL DEFAULT 0,
    PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='MES 杂项出库单明细表';

-- 7.33 委外出库单表
DROP TABLE IF EXISTS `mes_wm_outsource_issue`;
CREATE TABLE `mes_wm_outsource_issue` (
    `id`            bigint       NOT NULL AUTO_INCREMENT,
    `tenant_id`     bigint       NOT NULL DEFAULT 1,
    `code`          varchar(64)  NOT NULL                COMMENT '出库单号',
    `name`          varchar(100) NOT NULL                COMMENT '出库名称',
    `vendor_id`     bigint       NOT NULL                COMMENT '供应商编号',
    `work_order_id` bigint       NOT NULL                COMMENT '工单编号',
    `issue_date`    date         NOT NULL                COMMENT '出库日期',
    `status`        int          NOT NULL                COMMENT '状态',
    `remark`        varchar(500) DEFAULT NULL,
    `create_time`   datetime     NOT NULL DEFAULT CURRENT_TIMESTAMP,
    `update_time`   datetime     NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    `creator`       varchar(64)  DEFAULT NULL,
    `updater`       varchar(64)  DEFAULT NULL,
    `deleted`       bit(1)       NOT NULL DEFAULT 0,
    PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='MES 委外出库单表';

-- 7.34 委外出库单行表
DROP TABLE IF EXISTS `mes_wm_outsource_issue_line`;
CREATE TABLE `mes_wm_outsource_issue_line` (
    `id`                bigint         NOT NULL AUTO_INCREMENT,
    `tenant_id`         bigint         NOT NULL DEFAULT 1,
    `issue_id`          bigint         NOT NULL                COMMENT '出库单编号',
    `material_stock_id` bigint         NOT NULL                COMMENT '物料库存编号',
    `item_id`           bigint         NOT NULL                COMMENT '物料编号',
    `quantity`          decimal(24,6)  NOT NULL                COMMENT '数量',
    `batch_id`          bigint         DEFAULT NULL            COMMENT '批次编号',
    `remark`            varchar(500)   DEFAULT NULL,
    `create_time`       datetime       NOT NULL DEFAULT CURRENT_TIMESTAMP,
    `update_time`       datetime       NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    `creator`           varchar(64)    DEFAULT NULL,
    `updater`           varchar(64)    DEFAULT NULL,
    `deleted`           bit(1)         NOT NULL DEFAULT 0,
    PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='MES 委外出库单行表';

-- 7.35 委外出库单明细表
DROP TABLE IF EXISTS `mes_wm_outsource_issue_detail`;
CREATE TABLE `mes_wm_outsource_issue_detail` (
    `id`                bigint         NOT NULL AUTO_INCREMENT,
    `tenant_id`         bigint         NOT NULL DEFAULT 1,
    `line_id`           bigint         NOT NULL                COMMENT '行编号',
    `issue_id`          bigint         NOT NULL                COMMENT '出库单编号',
    `material_stock_id` bigint         NOT NULL                COMMENT '物料库存编号',
    `item_id`           bigint         NOT NULL                COMMENT '物料编号',
    `quantity`          decimal(24,6)  NOT NULL                COMMENT '数量',
    `batch_id`          bigint         DEFAULT NULL            COMMENT '批次编号',
    `warehouse_id`      bigint         NOT NULL                COMMENT '仓库编号',
    `location_id`       bigint         NOT NULL                COMMENT '库位编号',
    `area_id`           bigint         NOT NULL                COMMENT '库区编号',
    `remark`            varchar(500)   DEFAULT NULL,
    `create_time`       datetime       NOT NULL DEFAULT CURRENT_TIMESTAMP,
    `update_time`       datetime       NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    `creator`           varchar(64)    DEFAULT NULL,
    `updater`           varchar(64)    DEFAULT NULL,
    `deleted`           bit(1)         NOT NULL DEFAULT 0,
    PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='MES 委外出库单明细表';

-- 7.36 委外入库单表
DROP TABLE IF EXISTS `mes_wm_outsource_receipt`;
CREATE TABLE `mes_wm_outsource_receipt` (
    `id`            bigint       NOT NULL AUTO_INCREMENT,
    `tenant_id`     bigint       NOT NULL DEFAULT 1,
    `code`          varchar(64)  NOT NULL                COMMENT '入库单号',
    `name`          varchar(100) NOT NULL                COMMENT '入库名称',
    `work_order_id` bigint       NOT NULL                COMMENT '工单编号',
    `vendor_id`     bigint       NOT NULL                COMMENT '供应商编号',
    `receipt_date`  date         NOT NULL                COMMENT '入库日期',
    `status`        int          NOT NULL                COMMENT '状态',
    `remark`        varchar(500) DEFAULT NULL,
    `create_time`   datetime     NOT NULL DEFAULT CURRENT_TIMESTAMP,
    `update_time`   datetime     NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    `creator`       varchar(64)  DEFAULT NULL,
    `updater`       varchar(64)  DEFAULT NULL,
    `deleted`       bit(1)       NOT NULL DEFAULT 0,
    PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='MES 委外入库单表';

-- 7.37 委外入库单行表
DROP TABLE IF EXISTS `mes_wm_outsource_receipt_line`;
CREATE TABLE `mes_wm_outsource_receipt_line` (
    `id`              bigint         NOT NULL AUTO_INCREMENT,
    `tenant_id`       bigint         NOT NULL DEFAULT 1,
    `receipt_id`      bigint         NOT NULL                COMMENT '入库单编号',
    `item_id`         bigint         NOT NULL                COMMENT '物料编号',
    `quantity`        decimal(24,6)  NOT NULL                COMMENT '数量',
    `batch_id`        bigint         DEFAULT NULL            COMMENT '批次编号',
    `batch_code`      varchar(64)    DEFAULT NULL            COMMENT '批次号',
    `production_date` date           DEFAULT NULL            COMMENT '生产日期',
    `expire_date`     date           DEFAULT NULL            COMMENT '有效期',
    `lot_number`      varchar(64)    DEFAULT NULL            COMMENT '批号',
    `remark`          varchar(500)   DEFAULT NULL,
    `iqc_id`          bigint         DEFAULT NULL            COMMENT 'IQC编号',
    `iqc_check_flag`  bit(1)         NOT NULL DEFAULT 0      COMMENT '检验标识',
    `quality_status`  varchar(20)    DEFAULT NULL            COMMENT '质量状态',
    `create_time`     datetime       NOT NULL DEFAULT CURRENT_TIMESTAMP,
    `update_time`     datetime       NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    `creator`         varchar(64)    DEFAULT NULL,
    `updater`         varchar(64)    DEFAULT NULL,
    `deleted`         bit(1)         NOT NULL DEFAULT 0,
    PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='MES 委外入库单行表';

-- 7.38 委外入库单明细表
DROP TABLE IF EXISTS `mes_wm_outsource_receipt_detail`;
CREATE TABLE `mes_wm_outsource_receipt_detail` (
    `id`            bigint         NOT NULL AUTO_INCREMENT,
    `tenant_id`     bigint         NOT NULL DEFAULT 1,
    `line_id`       bigint         NOT NULL                COMMENT '行编号',
    `receipt_id`    bigint         NOT NULL                COMMENT '入库单编号',
    `item_id`       bigint         NOT NULL                COMMENT '物料编号',
    `quantity`      decimal(24,6)  NOT NULL                COMMENT '数量',
    `batch_id`      bigint         DEFAULT NULL            COMMENT '批次编号',
    `warehouse_id`  bigint         NOT NULL                COMMENT '仓库编号',
    `location_id`   bigint         NOT NULL                COMMENT '库位编号',
    `area_id`       bigint         NOT NULL                COMMENT '库区编号',
    `remark`        varchar(500)   DEFAULT NULL,
    `create_time`   datetime       NOT NULL DEFAULT CURRENT_TIMESTAMP,
    `update_time`   datetime       NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    `creator`       varchar(64)    DEFAULT NULL,
    `updater`       varchar(64)    DEFAULT NULL,
    `deleted`       bit(1)         NOT NULL DEFAULT 0,
    PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='MES 委外入库单明细表';

-- 7.39 发运通知单表
DROP TABLE IF EXISTS `mes_wm_sales_notice`;
CREATE TABLE `mes_wm_sales_notice` (
    `id`                  bigint       NOT NULL AUTO_INCREMENT,
    `tenant_id`           bigint       NOT NULL DEFAULT 1,
    `code`                varchar(64)  NOT NULL                COMMENT '通知单号',
    `name`                varchar(100) NOT NULL                COMMENT '通知名称',
    `sales_order_code`    varchar(64)  DEFAULT NULL            COMMENT '销售订单号',
    `client_id`           bigint       DEFAULT NULL            COMMENT '客户编号',
    `sales_date`          date         DEFAULT NULL            COMMENT '销售日期',
    `recipient_name`      varchar(50)  DEFAULT NULL            COMMENT '收件人',
    `recipient_telephone` varchar(20)  DEFAULT NULL            COMMENT '收件人电话',
    `recipient_address`   varchar(255) DEFAULT NULL            COMMENT '收件地址',
    `status`              int          NOT NULL                COMMENT '状态',
    `remark`              varchar(500) DEFAULT NULL,
    `create_time`         datetime     NOT NULL DEFAULT CURRENT_TIMESTAMP,
    `update_time`         datetime     NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    `creator`             varchar(64)  DEFAULT NULL,
    `updater`             varchar(64)  DEFAULT NULL,
    `deleted`             bit(1)       NOT NULL DEFAULT 0,
    PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='MES 发运通知单表';

-- 7.40 发运通知单行表
DROP TABLE IF EXISTS `mes_wm_sales_notice_line`;
CREATE TABLE `mes_wm_sales_notice_line` (
    `id`              bigint         NOT NULL AUTO_INCREMENT,
    `tenant_id`       bigint         NOT NULL DEFAULT 1,
    `notice_id`       bigint         NOT NULL                COMMENT '通知单编号',
    `item_id`         bigint         NOT NULL                COMMENT '物料编号',
    `batch_id`        bigint         DEFAULT NULL            COMMENT '批次编号',
    `batch_code`      varchar(64)    DEFAULT NULL            COMMENT '批次号',
    `quantity`        decimal(24,6)  NOT NULL                COMMENT '数量',
    `oqc_check_flag`  bit(1)         NOT NULL DEFAULT 0      COMMENT 'OQC检验标识',
    `remark`          varchar(500)   DEFAULT NULL,
    `create_time`     datetime       NOT NULL DEFAULT CURRENT_TIMESTAMP,
    `update_time`     datetime       NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    `creator`         varchar(64)    DEFAULT NULL,
    `updater`         varchar(64)    DEFAULT NULL,
    `deleted`         bit(1)         NOT NULL DEFAULT 0,
    PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='MES 发运通知单行表';

-- 7.41 产品销售出库单表
DROP TABLE IF EXISTS `mes_wm_product_sales`;
CREATE TABLE `mes_wm_product_sales` (
    `id`                bigint       NOT NULL AUTO_INCREMENT,
    `tenant_id`         bigint       NOT NULL DEFAULT 1,
    `code`              varchar(64)  NOT NULL                COMMENT '出货单号',
    `name`              varchar(100) NOT NULL                COMMENT '出货名称',
    `client_id`         bigint       DEFAULT NULL            COMMENT '客户编号',
    `sales_order_code`  varchar(64)  DEFAULT NULL            COMMENT '销售订单号',
    `notice_id`         bigint       DEFAULT NULL            COMMENT '通知单编号',
    `sales_date`        date         NOT NULL                COMMENT '销售日期',
    `contact_name`      varchar(50)  DEFAULT NULL            COMMENT '联系人',
    `contact_telephone` varchar(20)  DEFAULT NULL            COMMENT '联系电话',
    `contact_address`   varchar(255) DEFAULT NULL            COMMENT '联系地址',
    `carrier`           varchar(100) DEFAULT NULL            COMMENT '承运人',
    `shipping_number`   varchar(64)  DEFAULT NULL            COMMENT '运单号',
    `status`            int          NOT NULL                COMMENT '状态',
    `remark`            varchar(500) DEFAULT NULL,
    `create_time`       datetime     NOT NULL DEFAULT CURRENT_TIMESTAMP,
    `update_time`       datetime     NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    `creator`           varchar(64)  DEFAULT NULL,
    `updater`           varchar(64)  DEFAULT NULL,
    `deleted`           bit(1)       NOT NULL DEFAULT 0,
    PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='MES 产品销售出库单表';

-- 7.42 产品销售出库单行表
DROP TABLE IF EXISTS `mes_wm_product_sales_line`;
CREATE TABLE `mes_wm_product_sales_line` (
    `id`                bigint         NOT NULL AUTO_INCREMENT,
    `tenant_id`         bigint         NOT NULL DEFAULT 1,
    `sales_id`          bigint         NOT NULL                COMMENT '出货单编号',
    `notice_line_id`    bigint         DEFAULT NULL            COMMENT '通知行编号',
    `item_id`           bigint         NOT NULL                COMMENT '物料编号',
    `quantity`          decimal(24,6)  NOT NULL                COMMENT '数量',
    `batch_id`          bigint         DEFAULT NULL            COMMENT '批次编号',
    `batch_code`        varchar(64)    DEFAULT NULL            COMMENT '批次号',
    `material_stock_id` bigint         NOT NULL                COMMENT '物料库存编号',
    `oqc_check_flag`    bit(1)         NOT NULL DEFAULT 0      COMMENT 'OQC检验标识',
    `oqc_id`            bigint         DEFAULT NULL            COMMENT 'OQC编号',
    `quality_status`    varchar(20)    DEFAULT NULL            COMMENT '质量状态',
    `remark`            varchar(500)   DEFAULT NULL,
    `create_time`       datetime       NOT NULL DEFAULT CURRENT_TIMESTAMP,
    `update_time`       datetime       NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    `creator`           varchar(64)    DEFAULT NULL,
    `updater`           varchar(64)    DEFAULT NULL,
    `deleted`           bit(1)         NOT NULL DEFAULT 0,
    PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='MES 产品销售出库单行表';

-- 7.43 产品销售出库单明细表
DROP TABLE IF EXISTS `mes_wm_product_sales_detail`;
CREATE TABLE `mes_wm_product_sales_detail` (
    `id`                bigint         NOT NULL AUTO_INCREMENT,
    `tenant_id`         bigint         NOT NULL DEFAULT 1,
    `line_id`           bigint         NOT NULL                COMMENT '行编号',
    `sales_id`          bigint         NOT NULL                COMMENT '出货单编号',
    `item_id`           bigint         NOT NULL                COMMENT '物料编号',
    `quantity`          decimal(24,6)  NOT NULL                COMMENT '数量',
    `material_stock_id` bigint         NOT NULL                COMMENT '物料库存编号',
    `batch_id`          bigint         DEFAULT NULL            COMMENT '批次编号',
    `batch_code`        varchar(64)    DEFAULT NULL            COMMENT '批次号',
    `warehouse_id`      bigint         NOT NULL                COMMENT '仓库编号',
    `location_id`       bigint         NOT NULL                COMMENT '库位编号',
    `area_id`           bigint         NOT NULL                COMMENT '库区编号',
    `remark`            varchar(500)   DEFAULT NULL,
    `create_time`       datetime       NOT NULL DEFAULT CURRENT_TIMESTAMP,
    `update_time`       datetime       NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    `creator`           varchar(64)    DEFAULT NULL,
    `updater`           varchar(64)    DEFAULT NULL,
    `deleted`           bit(1)         NOT NULL DEFAULT 0,
    PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='MES 产品销售出库单明细表';

-- 7.44 装箱单表
DROP TABLE IF EXISTS `mes_wm_package`;
CREATE TABLE `mes_wm_package` (
    `id`               bigint         NOT NULL AUTO_INCREMENT,
    `tenant_id`        bigint         NOT NULL DEFAULT 1,
    `code`             varchar(64)    NOT NULL                COMMENT '箱号',
    `parent_id`        bigint         DEFAULT NULL            COMMENT '父箱编号',
    `package_date`     date           NOT NULL                COMMENT '装箱日期',
    `sales_order_code` varchar(64)    DEFAULT NULL            COMMENT '销售订单号',
    `invoice_code`     varchar(64)    DEFAULT NULL            COMMENT '发票号',
    `client_id`        bigint         DEFAULT NULL            COMMENT '客户编号',
    `length`           decimal(10,2)  DEFAULT NULL            COMMENT '长',
    `width`            decimal(10,2)  DEFAULT NULL            COMMENT '宽',
    `height`           decimal(10,2)  DEFAULT NULL            COMMENT '高',
    `size_unit_id`     bigint         DEFAULT NULL            COMMENT '尺寸单位',
    `net_weight`       decimal(10,2)  DEFAULT NULL            COMMENT '净重',
    `gross_weight`     decimal(10,2)  DEFAULT NULL            COMMENT '毛重',
    `weight_unit_id`   bigint         DEFAULT NULL            COMMENT '重量单位',
    `inspector_user_id` bigint        DEFAULT NULL            COMMENT '检验人',
    `status`           int            NOT NULL                COMMENT '状态',
    `remark`           varchar(500)   DEFAULT NULL,
    `create_time`      datetime       NOT NULL DEFAULT CURRENT_TIMESTAMP,
    `update_time`      datetime       NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    `creator`          varchar(64)    DEFAULT NULL,
    `updater`          varchar(64)    DEFAULT NULL,
    `deleted`          bit(1)         NOT NULL DEFAULT 0,
    PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='MES 装箱单表';

-- 7.45 装箱单行表
DROP TABLE IF EXISTS `mes_wm_package_line`;
CREATE TABLE `mes_wm_package_line` (
    `id`                bigint         NOT NULL AUTO_INCREMENT,
    `tenant_id`         bigint         NOT NULL DEFAULT 1,
    `package_id`        bigint         NOT NULL                COMMENT '装箱单编号',
    `material_stock_id` bigint         NOT NULL                COMMENT '物料库存编号',
    `item_id`           bigint         NOT NULL                COMMENT '物料编号',
    `quantity`          decimal(24,6)  NOT NULL                COMMENT '数量',
    `work_order_id`     bigint         DEFAULT NULL            COMMENT '工单编号',
    `expire_date`       date           DEFAULT NULL            COMMENT '有效期',
    `remark`            varchar(500)   DEFAULT NULL,
    `create_time`       datetime       NOT NULL DEFAULT CURRENT_TIMESTAMP,
    `update_time`       datetime       NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    `creator`           varchar(64)    DEFAULT NULL,
    `updater`           varchar(64)    DEFAULT NULL,
    `deleted`           bit(1)         NOT NULL DEFAULT 0,
    PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='MES 装箱单行表';

-- 7.46 调拨单表
DROP TABLE IF EXISTS `mes_wm_transfer`;
CREATE TABLE `mes_wm_transfer` (
    `id`                  bigint       NOT NULL AUTO_INCREMENT,
    `tenant_id`           bigint       NOT NULL DEFAULT 1,
    `code`                varchar(64)  NOT NULL                COMMENT '调拨单号',
    `name`                varchar(100) NOT NULL                COMMENT '调拨名称',
    `type`                int          NOT NULL                COMMENT '调拨类型',
    `delivery_flag`       bit(1)       NOT NULL DEFAULT 0      COMMENT '发货标识',
    `recipient_name`      varchar(50)  DEFAULT NULL            COMMENT '收货人',
    `recipient_telephone` varchar(20)  DEFAULT NULL            COMMENT '收货人电话',
    `destination_address` varchar(255) DEFAULT NULL            COMMENT '目的地地址',
    `carrier`             varchar(100) DEFAULT NULL            COMMENT '承运人',
    `shipping_number`     varchar(64)  DEFAULT NULL            COMMENT '运单号',
    `confirm_flag`        bit(1)       NOT NULL DEFAULT 0      COMMENT '确认标识',
    `transfer_date`       date         NOT NULL                COMMENT '调拨日期',
    `status`              int          NOT NULL                COMMENT '状态',
    `remark`              varchar(500) DEFAULT NULL,
    `create_time`         datetime     NOT NULL DEFAULT CURRENT_TIMESTAMP,
    `update_time`         datetime     NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    `creator`             varchar(64)  DEFAULT NULL,
    `updater`             varchar(64)  DEFAULT NULL,
    `deleted`             bit(1)       NOT NULL DEFAULT 0,
    PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='MES 调拨单表';

-- 7.47 调拨单行表
DROP TABLE IF EXISTS `mes_wm_transfer_line`;
CREATE TABLE `mes_wm_transfer_line` (
    `id`                bigint         NOT NULL AUTO_INCREMENT,
    `tenant_id`         bigint         NOT NULL DEFAULT 1,
    `transfer_id`       bigint         NOT NULL                COMMENT '调拨单编号',
    `material_stock_id` bigint         NOT NULL                COMMENT '物料库存编号',
    `item_id`           bigint         NOT NULL                COMMENT '物料编号',
    `quantity`          decimal(24,6)  NOT NULL                COMMENT '数量',
    `batch_id`          bigint         DEFAULT NULL            COMMENT '批次编号',
    `from_warehouse_id` bigint         NOT NULL                COMMENT '来源仓库编号',
    `from_location_id`  bigint         NOT NULL                COMMENT '来源库位编号',
    `from_area_id`      bigint         NOT NULL                COMMENT '来源库区编号',
    `remark`            varchar(500)   DEFAULT NULL,
    `create_time`       datetime       NOT NULL DEFAULT CURRENT_TIMESTAMP,
    `update_time`       datetime       NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    `creator`           varchar(64)    DEFAULT NULL,
    `updater`           varchar(64)    DEFAULT NULL,
    `deleted`           bit(1)         NOT NULL DEFAULT 0,
    PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='MES 调拨单行表';

-- 7.48 调拨单明细表
DROP TABLE IF EXISTS `mes_wm_transfer_detail`;
CREATE TABLE `mes_wm_transfer_detail` (
    `id`              bigint         NOT NULL AUTO_INCREMENT,
    `tenant_id`       bigint         NOT NULL DEFAULT 1,
    `line_id`         bigint         NOT NULL                COMMENT '行编号',
    `transfer_id`     bigint         NOT NULL                COMMENT '调拨单编号',
    `item_id`         bigint         NOT NULL                COMMENT '物料编号',
    `quantity`        decimal(24,6)  NOT NULL                COMMENT '数量',
    `batch_id`        bigint         DEFAULT NULL            COMMENT '批次编号',
    `to_warehouse_id` bigint         NOT NULL                COMMENT '目标仓库编号',
    `to_location_id`  bigint         NOT NULL                COMMENT '目标库位编号',
    `to_area_id`      bigint         NOT NULL                COMMENT '目标库区编号',
    `remark`          varchar(500)   DEFAULT NULL,
    `create_time`     datetime       NOT NULL DEFAULT CURRENT_TIMESTAMP,
    `update_time`     datetime       NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    `creator`         varchar(64)    DEFAULT NULL,
    `updater`         varchar(64)    DEFAULT NULL,
    `deleted`         bit(1)         NOT NULL DEFAULT 0,
    PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='MES 调拨单明细表';

-- 7.49 销售退货单表
DROP TABLE IF EXISTS `mes_wm_return_sales`;
CREATE TABLE `mes_wm_return_sales` (
    `id`               bigint       NOT NULL AUTO_INCREMENT,
    `tenant_id`        bigint       NOT NULL DEFAULT 1,
    `code`             varchar(64)  NOT NULL                COMMENT '退货单号',
    `name`             varchar(100) NOT NULL                COMMENT '退货名称',
    `sales_order_code` varchar(64)  DEFAULT NULL            COMMENT '销售订单号',
    `client_id`        bigint       DEFAULT NULL            COMMENT '客户编号',
    `return_date`      date         NOT NULL                COMMENT '退货日期',
    `return_reason`    varchar(500) DEFAULT NULL            COMMENT '退货原因',
    `status`           int          NOT NULL                COMMENT '状态',
    `remark`           varchar(500) DEFAULT NULL,
    `create_time`      datetime     NOT NULL DEFAULT CURRENT_TIMESTAMP,
    `update_time`      datetime     NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    `creator`          varchar(64)  DEFAULT NULL,
    `updater`          varchar(64)  DEFAULT NULL,
    `deleted`          bit(1)       NOT NULL DEFAULT 0,
    PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='MES 销售退货单表';

-- 7.50 销售退货单行表
DROP TABLE IF EXISTS `mes_wm_return_sales_line`;
CREATE TABLE `mes_wm_return_sales_line` (
    `id`              bigint         NOT NULL AUTO_INCREMENT,
    `tenant_id`       bigint         NOT NULL DEFAULT 1,
    `return_id`       bigint         NOT NULL                COMMENT '退货单编号',
    `item_id`         bigint         NOT NULL                COMMENT '物料编号',
    `quantity`        decimal(24,6)  NOT NULL                COMMENT '数量',
    `batch_id`        bigint         DEFAULT NULL            COMMENT '批次编号',
    `batch_code`      varchar(64)    DEFAULT NULL            COMMENT '批次号',
    `rqc_id`          bigint         DEFAULT NULL            COMMENT 'RQC编号',
    `rqc_check_flag`  bit(1)         NOT NULL DEFAULT 0      COMMENT '检验标识',
    `quality_status`  varchar(20)    DEFAULT NULL            COMMENT '质量状态',
    `remark`          varchar(500)   DEFAULT NULL,
    `create_time`     datetime       NOT NULL DEFAULT CURRENT_TIMESTAMP,
    `update_time`     datetime       NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    `creator`         varchar(64)    DEFAULT NULL,
    `updater`         varchar(64)    DEFAULT NULL,
    `deleted`         bit(1)         NOT NULL DEFAULT 0,
    PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='MES 销售退货单行表';

-- 7.51 销售退货单明细表
DROP TABLE IF EXISTS `mes_wm_return_sales_detail`;
CREATE TABLE `mes_wm_return_sales_detail` (
    `id`            bigint         NOT NULL AUTO_INCREMENT,
    `tenant_id`     bigint         NOT NULL DEFAULT 1,
    `return_id`     bigint         NOT NULL                COMMENT '退货单编号',
    `line_id`       bigint         NOT NULL                COMMENT '行编号',
    `item_id`       bigint         NOT NULL                COMMENT '物料编号',
    `quantity`      decimal(24,6)  NOT NULL                COMMENT '数量',
    `batch_id`      bigint         DEFAULT NULL            COMMENT '批次编号',
    `batch_code`    varchar(64)    DEFAULT NULL            COMMENT '批次号',
    `warehouse_id`  bigint         NOT NULL                COMMENT '仓库编号',
    `location_id`   bigint         NOT NULL                COMMENT '库位编号',
    `area_id`       bigint         NOT NULL                COMMENT '库区编号',
    `remark`        varchar(500)   DEFAULT NULL,
    `create_time`   datetime       NOT NULL DEFAULT CURRENT_TIMESTAMP,
    `update_time`   datetime       NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    `creator`       varchar(64)    DEFAULT NULL,
    `updater`       varchar(64)    DEFAULT NULL,
    `deleted`       bit(1)         NOT NULL DEFAULT 0,
    PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='MES 销售退货单明细表';

-- 7.52 采购退货单表
DROP TABLE IF EXISTS `mes_wm_return_vendor`;
CREATE TABLE `mes_wm_return_vendor` (
    `id`                  bigint       NOT NULL AUTO_INCREMENT,
    `tenant_id`           bigint       NOT NULL DEFAULT 1,
    `code`                varchar(64)  NOT NULL                COMMENT '退货单号',
    `name`                varchar(100) NOT NULL                COMMENT '退货名称',
    `purchase_order_code` varchar(64)  DEFAULT NULL            COMMENT '采购订单号',
    `vendor_id`           bigint       DEFAULT NULL            COMMENT '供应商编号',
    `return_date`         date         NOT NULL                COMMENT '退货日期',
    `return_reason`       varchar(500) DEFAULT NULL            COMMENT '退货原因',
    `transport_code`      varchar(64)  DEFAULT NULL            COMMENT '运输单号',
    `transport_telephone` varchar(20)  DEFAULT NULL            COMMENT '运输电话',
    `status`              int          NOT NULL                COMMENT '状态',
    `remark`              varchar(500) DEFAULT NULL,
    `create_time`         datetime     NOT NULL DEFAULT CURRENT_TIMESTAMP,
    `update_time`         datetime     NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    `creator`             varchar(64)  DEFAULT NULL,
    `updater`             varchar(64)  DEFAULT NULL,
    `deleted`             bit(1)       NOT NULL DEFAULT 0,
    PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='MES 采购退货单表';

-- 7.53 采购退货单行表
DROP TABLE IF EXISTS `mes_wm_return_vendor_line`;
CREATE TABLE `mes_wm_return_vendor_line` (
    `id`          bigint         NOT NULL AUTO_INCREMENT,
    `tenant_id`   bigint         NOT NULL DEFAULT 1,
    `return_id`   bigint         NOT NULL                COMMENT '退货单编号',
    `item_id`     bigint         NOT NULL                COMMENT '物料编号',
    `quantity`    decimal(24,6)  NOT NULL                COMMENT '数量',
    `batch_id`    bigint         DEFAULT NULL            COMMENT '批次编号',
    `batch_code`  varchar(64)    DEFAULT NULL            COMMENT '批次号',
    `remark`      varchar(500)   DEFAULT NULL,
    `create_time` datetime       NOT NULL DEFAULT CURRENT_TIMESTAMP,
    `update_time` datetime       NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    `creator`     varchar(64)    DEFAULT NULL,
    `updater`     varchar(64)    DEFAULT NULL,
    `deleted`     bit(1)         NOT NULL DEFAULT 0,
    PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='MES 采购退货单行表';

-- 7.54 采购退货单明细表
DROP TABLE IF EXISTS `mes_wm_return_vendor_detail`;
CREATE TABLE `mes_wm_return_vendor_detail` (
    `id`                bigint         NOT NULL AUTO_INCREMENT,
    `tenant_id`         bigint         NOT NULL DEFAULT 1,
    `return_id`         bigint         NOT NULL                COMMENT '退货单编号',
    `line_id`           bigint         NOT NULL                COMMENT '行编号',
    `material_stock_id` bigint         NOT NULL                COMMENT '物料库存编号',
    `item_id`           bigint         NOT NULL                COMMENT '物料编号',
    `quantity`          decimal(24,6)  NOT NULL                COMMENT '数量',
    `batch_id`          bigint         DEFAULT NULL            COMMENT '批次编号',
    `batch_code`        varchar(64)    DEFAULT NULL            COMMENT '批次号',
    `warehouse_id`      bigint         NOT NULL                COMMENT '仓库编号',
    `location_id`       bigint         NOT NULL                COMMENT '库位编号',
    `area_id`           bigint         NOT NULL                COMMENT '库区编号',
    `remark`            varchar(500)   DEFAULT NULL,
    `create_time`       datetime       NOT NULL DEFAULT CURRENT_TIMESTAMP,
    `update_time`       datetime       NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    `creator`           varchar(64)    DEFAULT NULL,
    `updater`           varchar(64)    DEFAULT NULL,
    `deleted`           bit(1)         NOT NULL DEFAULT 0,
    PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='MES 采购退货单明细表';

-- 7.55 生产退料单表
DROP TABLE IF EXISTS `mes_wm_return_issue`;
CREATE TABLE `mes_wm_return_issue` (
    `id`             bigint       NOT NULL AUTO_INCREMENT,
    `tenant_id`      bigint       NOT NULL DEFAULT 1,
    `code`           varchar(64)  NOT NULL                COMMENT '退料单号',
    `name`           varchar(100) NOT NULL                COMMENT '退料名称',
    `work_order_id`  bigint       NOT NULL                COMMENT '工单编号',
    `workstation_id` bigint       NOT NULL                COMMENT '工位编号',
    `type`           int          NOT NULL                COMMENT '退料类型',
    `return_date`    date         NOT NULL                COMMENT '退料日期',
    `status`         int          NOT NULL                COMMENT '状态',
    `remark`         varchar(500) DEFAULT NULL,
    `create_time`    datetime     NOT NULL DEFAULT CURRENT_TIMESTAMP,
    `update_time`    datetime     NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    `creator`        varchar(64)  DEFAULT NULL,
    `updater`        varchar(64)  DEFAULT NULL,
    `deleted`        bit(1)       NOT NULL DEFAULT 0,
    PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='MES 生产退料单表';

-- 7.56 生产退料单行表
DROP TABLE IF EXISTS `mes_wm_return_issue_line`;
CREATE TABLE `mes_wm_return_issue_line` (
    `id`                bigint         NOT NULL AUTO_INCREMENT,
    `tenant_id`         bigint         NOT NULL DEFAULT 1,
    `issue_id`          bigint         NOT NULL                COMMENT '退料单编号',
    `material_stock_id` bigint         NOT NULL                COMMENT '物料库存编号',
    `item_id`           bigint         NOT NULL                COMMENT '物料编号',
    `quantity`          decimal(24,6)  NOT NULL                COMMENT '数量',
    `batch_id`          bigint         DEFAULT NULL            COMMENT '批次编号',
    `batch_code`        varchar(64)    DEFAULT NULL            COMMENT '批次号',
    `rqc_id`            bigint         DEFAULT NULL            COMMENT 'RQC编号',
    `rqc_check_flag`    bit(1)         NOT NULL DEFAULT 0      COMMENT '检验标识',
    `quality_status`    varchar(20)    DEFAULT NULL            COMMENT '质量状态',
    `remark`            varchar(500)   DEFAULT NULL,
    `create_time`       datetime       NOT NULL DEFAULT CURRENT_TIMESTAMP,
    `update_time`       datetime       NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    `creator`           varchar(64)    DEFAULT NULL,
    `updater`           varchar(64)    DEFAULT NULL,
    `deleted`           bit(1)         NOT NULL DEFAULT 0,
    PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='MES 生产退料单行表';

-- 7.57 生产退料单明细表
DROP TABLE IF EXISTS `mes_wm_return_issue_detail`;
CREATE TABLE `mes_wm_return_issue_detail` (
    `id`                bigint         NOT NULL AUTO_INCREMENT,
    `tenant_id`         bigint         NOT NULL DEFAULT 1,
    `issue_id`          bigint         NOT NULL                COMMENT '退料单编号',
    `line_id`           bigint         NOT NULL                COMMENT '行编号',
    `material_stock_id` bigint         NOT NULL                COMMENT '物料库存编号',
    `item_id`           bigint         NOT NULL                COMMENT '物料编号',
    `quantity`          decimal(24,6)  NOT NULL                COMMENT '数量',
    `batch_id`          bigint         DEFAULT NULL            COMMENT '批次编号',
    `batch_code`        varchar(64)    DEFAULT NULL            COMMENT '批次号',
    `warehouse_id`      bigint         NOT NULL                COMMENT '仓库编号',
    `location_id`       bigint         NOT NULL                COMMENT '库位编号',
    `area_id`           bigint         NOT NULL                COMMENT '库区编号',
    `remark`            varchar(500)   DEFAULT NULL,
    `create_time`       datetime       NOT NULL DEFAULT CURRENT_TIMESTAMP,
    `update_time`       datetime       NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    `creator`           varchar(64)    DEFAULT NULL,
    `updater`           varchar(64)    DEFAULT NULL,
    `deleted`           bit(1)         NOT NULL DEFAULT 0,
    PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='MES 生产退料单明细表';

-- 7.58 盘点计划表
DROP TABLE IF EXISTS `mes_wm_stock_taking_plan`;
CREATE TABLE `mes_wm_stock_taking_plan` (
    `id`          bigint       NOT NULL AUTO_INCREMENT,
    `tenant_id`   bigint       NOT NULL DEFAULT 1,
    `code`        varchar(64)  NOT NULL                COMMENT '计划编号',
    `name`        varchar(100) NOT NULL                COMMENT '计划名称',
    `type`        int          NOT NULL                COMMENT '计划类型',
    `start_time`  datetime     NOT NULL                COMMENT '开始时间',
    `end_time`    datetime     NOT NULL                COMMENT '结束时间',
    `blind_flag`  bit(1)       NOT NULL DEFAULT 0      COMMENT '盲盘标识',
    `frozen`      bit(1)       NOT NULL DEFAULT 0      COMMENT '冻结标识',
    `status`      int          NOT NULL                COMMENT '状态',
    `remark`      varchar(500) DEFAULT NULL,
    `create_time` datetime     NOT NULL DEFAULT CURRENT_TIMESTAMP,
    `update_time` datetime     NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    `creator`     varchar(64)  DEFAULT NULL,
    `updater`     varchar(64)  DEFAULT NULL,
    `deleted`     bit(1)       NOT NULL DEFAULT 0,
    PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='MES 盘点计划表';

-- 7.59 盘点计划参数表
DROP TABLE IF EXISTS `mes_wm_stock_taking_plan_param`;
CREATE TABLE `mes_wm_stock_taking_plan_param` (
    `id`          bigint       NOT NULL AUTO_INCREMENT,
    `tenant_id`   bigint       NOT NULL DEFAULT 1,
    `plan_id`     bigint       NOT NULL                COMMENT '计划编号',
    `type`        varchar(50)  NOT NULL                COMMENT '参数类型',
    `value_id`    bigint       DEFAULT NULL            COMMENT '参数值ID',
    `value_code`  varchar(64)  DEFAULT NULL            COMMENT '参数值编码',
    `value_name`  varchar(100) DEFAULT NULL            COMMENT '参数值名称',
    `remark`      varchar(500) DEFAULT NULL,
    `create_time` datetime     NOT NULL DEFAULT CURRENT_TIMESTAMP,
    `update_time` datetime     NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    `creator`     varchar(64)  DEFAULT NULL,
    `updater`     varchar(64)  DEFAULT NULL,
    `deleted`     bit(1)       NOT NULL DEFAULT 0,
    PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='MES 盘点计划参数表';

-- 7.60 盘点任务表
DROP TABLE IF EXISTS `mes_wm_stock_taking_task`;
CREATE TABLE `mes_wm_stock_taking_task` (
    `id`           bigint       NOT NULL AUTO_INCREMENT,
    `tenant_id`    bigint       NOT NULL DEFAULT 1,
    `code`         varchar(64)  NOT NULL                COMMENT '任务编号',
    `name`         varchar(100) NOT NULL                COMMENT '任务名称',
    `taking_date`  date         NOT NULL                COMMENT '盘点日期',
    `type`         int          NOT NULL                COMMENT '任务类型',
    `user_id`      bigint       NOT NULL                COMMENT '盘点人',
    `plan_id`      bigint       DEFAULT NULL            COMMENT '计划编号',
    `blind_flag`   bit(1)       NOT NULL DEFAULT 0      COMMENT '盲盘标识',
    `frozen`       bit(1)       NOT NULL DEFAULT 0      COMMENT '冻结标识',
    `start_time`   datetime     DEFAULT NULL            COMMENT '开始时间',
    `end_time`     datetime     DEFAULT NULL            COMMENT '结束时间',
    `status`       int          NOT NULL                COMMENT '状态',
    `remark`       varchar(500) DEFAULT NULL,
    `create_time`  datetime     NOT NULL DEFAULT CURRENT_TIMESTAMP,
    `update_time`  datetime     NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    `creator`      varchar(64)  DEFAULT NULL,
    `updater`      varchar(64)  DEFAULT NULL,
    `deleted`      bit(1)       NOT NULL DEFAULT 0,
    PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='MES 盘点任务表';

-- 7.61 盘点任务行表
DROP TABLE IF EXISTS `mes_wm_stock_taking_task_line`;
CREATE TABLE `mes_wm_stock_taking_task_line` (
    `id`                bigint         NOT NULL AUTO_INCREMENT,
    `tenant_id`         bigint         NOT NULL DEFAULT 1,
    `task_id`           bigint         NOT NULL                COMMENT '任务编号',
    `material_stock_id` bigint         NOT NULL                COMMENT '物料库存编号',
    `item_id`           bigint         NOT NULL                COMMENT '物料编号',
    `batch_id`          bigint         DEFAULT NULL            COMMENT '批次编号',
    `batch_code`        varchar(64)    DEFAULT NULL            COMMENT '批次号',
    `quantity`          decimal(24,6)  NOT NULL                COMMENT '账面数量',
    `taking_quantity`   decimal(24,6)  DEFAULT NULL            COMMENT '盘点数量',
    `warehouse_id`      bigint         NOT NULL                COMMENT '仓库编号',
    `location_id`       bigint         NOT NULL                COMMENT '库位编号',
    `area_id`           bigint         NOT NULL                COMMENT '库区编号',
    `status`            int            NOT NULL                COMMENT '状态',
    `remark`            varchar(500)   DEFAULT NULL,
    `create_time`       datetime       NOT NULL DEFAULT CURRENT_TIMESTAMP,
    `update_time`       datetime       NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    `creator`           varchar(64)    DEFAULT NULL,
    `updater`           varchar(64)    DEFAULT NULL,
    `deleted`           bit(1)         NOT NULL DEFAULT 0,
    PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='MES 盘点任务行表';

-- 7.62 盘点结果表
DROP TABLE IF EXISTS `mes_wm_stock_taking_task_result`;
CREATE TABLE `mes_wm_stock_taking_task_result` (
    `id`                bigint         NOT NULL AUTO_INCREMENT,
    `tenant_id`         bigint         NOT NULL DEFAULT 1,
    `task_id`           bigint         NOT NULL                COMMENT '任务编号',
    `line_id`           bigint         NOT NULL                COMMENT '行编号',
    `material_stock_id` bigint         NOT NULL                COMMENT '物料库存编号',
    `item_id`           bigint         NOT NULL                COMMENT '物料编号',
    `batch_id`          bigint         DEFAULT NULL            COMMENT '批次编号',
    `batch_code`        varchar(64)    DEFAULT NULL            COMMENT '批次号',
    `warehouse_id`      bigint         NOT NULL                COMMENT '仓库编号',
    `location_id`       bigint         NOT NULL                COMMENT '库位编号',
    `area_id`           bigint         NOT NULL                COMMENT '库区编号',
    `quantity`          decimal(24,6)  NOT NULL                COMMENT '账面数量',
    `taking_quantity`   decimal(24,6)  NOT NULL                COMMENT '盘点数量',
    `remark`            varchar(500)   DEFAULT NULL,
    `create_time`       datetime       NOT NULL DEFAULT CURRENT_TIMESTAMP,
    `update_time`       datetime       NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    `creator`           varchar(64)    DEFAULT NULL,
    `updater`           varchar(64)    DEFAULT NULL,
    `deleted`           bit(1)         NOT NULL DEFAULT 0,
    PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='MES 盘点结果表';