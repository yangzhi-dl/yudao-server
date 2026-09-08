-- ======================================================================================
-- BPM 工作流模块 - 完整表结构 DDL（逆向自 DO 实体类）
-- 数据库类型: MySQL 5.7+ / 8.0
-- 所有表均继承 BaseDO 通用字段: create_time, update_time, creator, updater, deleted
-- 
-- 注意：Flowable 引擎自身的表 (ACT_*) 由 Flowable 自动管理，不在本文档范围内
-- ======================================================================================

-- --------------------------------------------------------------------
-- 1. 流程分类表
-- --------------------------------------------------------------------
DROP TABLE IF EXISTS `bpm_category`;
CREATE TABLE `bpm_category` (
    `id`          bigint       NOT NULL AUTO_INCREMENT COMMENT '分类编号（主键自增）',
    `name`        varchar(100) NOT NULL                COMMENT '分类名称',
    `code`        varchar(64)  NOT NULL                COMMENT '分类标志（唯一标识）',
    `description` varchar(255) DEFAULT NULL            COMMENT '分类描述',
    `status`      int          NOT NULL DEFAULT 0      COMMENT '状态（CommonStatusEnum: 0-禁用 1-启用）',
    `sort`        int          NOT NULL DEFAULT 0      COMMENT '排序',
    `tenant_id`   bigint       NOT NULL DEFAULT 1      COMMENT '租户编号',
    -- BaseDO 通用字段
    `create_time` datetime     NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time` datetime     NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '最后更新时间',
    `creator`     varchar(64)  DEFAULT NULL            COMMENT '创建者',
    `updater`     varchar(64)  DEFAULT NULL            COMMENT '更新者',
    `deleted`     bit(1)       NOT NULL DEFAULT 0      COMMENT '逻辑删除',
    PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='BPM 流程分类表';

-- --------------------------------------------------------------------
-- 2. 动态表单定义表
-- --------------------------------------------------------------------
DROP TABLE IF EXISTS `bpm_form`;
CREATE TABLE `bpm_form` (
    `id`          bigint          NOT NULL AUTO_INCREMENT COMMENT '表单编号（主键自增）',
    `name`        varchar(100)    NOT NULL                COMMENT '表单名称',
    `status`      int             NOT NULL DEFAULT 0      COMMENT '状态（CommonStatusEnum: 0-禁用 1-启用）',
    `conf`        json            DEFAULT NULL            COMMENT '表单配置（form-generator JSON 串）',
    `fields`      json            DEFAULT NULL            COMMENT '表单项数组（JSON 数组，Jackson3TypeHandler 序列化）',
    `remark`      varchar(500)    DEFAULT NULL            COMMENT '备注',
    `tenant_id`   bigint       NOT NULL DEFAULT 1      COMMENT '租户编号',
    -- BaseDO 通用字段
    `create_time` datetime        NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time` datetime        NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '最后更新时间',
    `creator`     varchar(64)     DEFAULT NULL            COMMENT '创建者',
    `updater`     varchar(64)     DEFAULT NULL            COMMENT '更新者',
    `deleted`     bit(1)          NOT NULL DEFAULT 0      COMMENT '逻辑删除',
    PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='BPM 动态表单定义表';

-- --------------------------------------------------------------------
-- 3. 流程定义扩展信息表（核心配置表，最复杂）
--    用途：扩展 Flowable 原生 ProcessDefinition，补充业务字段
-- --------------------------------------------------------------------
DROP TABLE IF EXISTS `bpm_process_definition_info`;
CREATE TABLE `bpm_process_definition_info` (
    `id`                           bigint          NOT NULL AUTO_INCREMENT COMMENT '编号（主键自增）',
    `process_definition_id`        varchar(64)     NOT NULL                COMMENT '流程定义编号（关联 Flowable ProcessDefinition.id）',
    `model_id`                     varchar(64)     NOT NULL                COMMENT '流程模型编号（关联 Flowable Model.id）',
    `model_type`                   int             DEFAULT NULL            COMMENT '流程模型类型（BpmModelTypeEnum: BPMN / SIMPLE）',

    -- 基本信息
    `category`                     varchar(64)     DEFAULT NULL            COMMENT '流程分类编码（关联 bpm_category.code）',
    `icon`                         varchar(255)    DEFAULT NULL            COMMENT '图标',
    `description`                  varchar(500)    DEFAULT NULL            COMMENT '描述',

    -- 表单相关
    `form_type`                    int             DEFAULT NULL            COMMENT '表单类型（BpmModelFormTypeEnum: NORMAL-动态表单 / CUSTOM-自定义表单）',
    `form_id`                      bigint          DEFAULT NULL            COMMENT '动态表单编号（关联 bpm_form.id，formType=NORMAL 时）',
    `form_conf`                    json            DEFAULT NULL            COMMENT '表单配置（冗余 bpm_form.conf）',
    `form_fields`                  json            DEFAULT NULL            COMMENT '表单项数组（JSON，冗余 bpm_form.fields，Jackson3TypeHandler）',
    `form_custom_create_path`      varchar(255)    DEFAULT NULL            COMMENT '自定义表单提交路径（Vue 路由地址，formType=CUSTOM 时）',
    `form_custom_view_path`        varchar(255)    DEFAULT NULL            COMMENT '自定义表单查看路径（Vue 路由地址，formType=CUSTOM 时）',

    -- SIMPLE 设计器
    `simple_model`                 json            DEFAULT NULL            COMMENT '仿钉钉设计器模型数据（JSON，发布时的快照）',

    -- 可见性与排序
    `visible`                      bit(1)          NOT NULL DEFAULT 1      COMMENT '是否可见（不可见则不展示在发起流程列表）',
    `sort`                         bigint          DEFAULT NULL            COMMENT '排序值',

    -- 权限控制
    `start_user_ids`               varchar(255)    DEFAULT NULL            COMMENT '可发起用户编号数组（逗号分隔，LongListTypeHandler；为空则全部可发起）',
    `start_dept_ids`               varchar(255)    DEFAULT NULL            COMMENT '可发起部门编号数组（逗号分隔，LongListTypeHandler）',
    `manager_user_ids`             varchar(255)    DEFAULT NULL            COMMENT '可管理用户编号数组（逗号分隔，LongListTypeHandler）',

    -- 操作开关
    `allow_cancel_running_process` bit(1)          DEFAULT NULL            COMMENT '是否允许撤销审批中的申请',
    `allow_withdraw_task`          bit(1)          DEFAULT NULL            COMMENT '是否允许审批人撤回任务',

    -- 流程 ID 规则
    `process_id_rule`              json            DEFAULT NULL            COMMENT '流程ID生成规则（JSON，Jackson3TypeHandler: BpmModelMetaInfoVO.ProcessIdRule）',

    -- 自动去重
    `auto_approval_type`           int             DEFAULT NULL            COMMENT '自动去重类型（BpmAutoApproveTypeEnum）',

    -- 标题与摘要
    `title_setting`                json            DEFAULT NULL            COMMENT '标题设置（JSON，Jackson3TypeHandler: BpmModelMetaInfoVO.TitleSetting）',
    `summary_setting`              json            DEFAULT NULL            COMMENT '摘要设置（JSON，Jackson3TypeHandler: BpmModelMetaInfoVO.SummarySetting）',

    -- HTTP 触发器设置（流程前后置通知 + 任务前后置通知）
    `process_before_trigger_setting` json            DEFAULT NULL           COMMENT '流程前置HTTP通知设置（JSON，Jackson3TypeHandler: HttpRequestSetting）',
    `process_after_trigger_setting`  json            DEFAULT NULL           COMMENT '流程后置HTTP通知设置（JSON，Jackson3TypeHandler: HttpRequestSetting）',
    `task_before_trigger_setting`    json            DEFAULT NULL           COMMENT '任务前置HTTP通知设置（JSON，Jackson3TypeHandler: HttpRequestSetting）',
    `task_after_trigger_setting`     json            DEFAULT NULL           COMMENT '任务后置HTTP通知设置（JSON，Jackson3TypeHandler: HttpRequestSetting）',

    -- 打印模板
    `print_template_setting`        json            DEFAULT NULL            COMMENT '自定义打印模板设置（JSON，Jackson3TypeHandler: PrintTemplateSetting）',

    `tenant_id`   bigint       NOT NULL DEFAULT 1      COMMENT '租户编号',
    -- BaseDO 通用字段
    `create_time`                   datetime       NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time`                   datetime       NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '最后更新时间',
    `creator`                       varchar(64)    DEFAULT NULL            COMMENT '创建者',
    `updater`                       varchar(64)    DEFAULT NULL            COMMENT '更新者',
    `deleted`                       bit(1)         NOT NULL DEFAULT 0      COMMENT '逻辑删除',
    PRIMARY KEY (`id`),
    INDEX `idx_process_definition_id` (`process_definition_id`),
    INDEX `idx_model_id` (`model_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='BPM 流程定义扩展信息表';

-- --------------------------------------------------------------------
-- 4. 流程表达式表（条件表达式模版）
-- --------------------------------------------------------------------
DROP TABLE IF EXISTS `bpm_process_expression`;
CREATE TABLE `bpm_process_expression` (
    `id`          bigint        NOT NULL AUTO_INCREMENT COMMENT '编号（主键自增）',
    `name`        varchar(100)  NOT NULL                COMMENT '表达式名称',
    `status`      int           NOT NULL DEFAULT 0      COMMENT '状态（CommonStatusEnum: 0-禁用 1-启用）',
    `expression`  text          NOT NULL                COMMENT '表达式内容',
    `tenant_id`   bigint       NOT NULL DEFAULT 1      COMMENT '租户编号',
    -- BaseDO 通用字段
    `create_time` datetime      NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time` datetime      NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '最后更新时间',
    `creator`     varchar(64)   DEFAULT NULL            COMMENT '创建者',
    `updater`     varchar(64)   DEFAULT NULL            COMMENT '更新者',
    `deleted`     bit(1)        NOT NULL DEFAULT 0      COMMENT '逻辑删除',
    PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='BPM 流程表达式表';

-- --------------------------------------------------------------------
-- 5. 流程监听器模板表
-- --------------------------------------------------------------------
DROP TABLE IF EXISTS `bpm_process_listener`;
CREATE TABLE `bpm_process_listener` (
    `id`          bigint        NOT NULL AUTO_INCREMENT COMMENT '编号（主键自增）',
    `name`        varchar(100)  NOT NULL                COMMENT '监听器名称',
    `status`      int           NOT NULL DEFAULT 0      COMMENT '状态（CommonStatusEnum: 0-禁用 1-启用）',
    `type`        varchar(32)   NOT NULL                COMMENT '监听类型（execution: ExecutionListener / task: TaskListener）',
    `event`       varchar(32)   NOT NULL                COMMENT '监听事件（execution: start/end; task: create/assignment/complete/delete/update/timeout）',
    `value_type`  varchar(32)   NOT NULL                COMMENT '值类型（class / delegateExpression / expression）',
    `value`       text          NOT NULL                COMMENT '值（全限定类名 / Spring Bean名称 / 表达式）',
    `tenant_id`   bigint       NOT NULL DEFAULT 1      COMMENT '租户编号',
    -- BaseDO 通用字段
    `create_time` datetime      NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time` datetime      NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '最后更新时间',
    `creator`     varchar(64)   DEFAULT NULL            COMMENT '创建者',
    `updater`     varchar(64)   DEFAULT NULL            COMMENT '更新者',
    `deleted`     bit(1)        NOT NULL DEFAULT 0      COMMENT '逻辑删除',
    PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='BPM 流程监听器模板表';

-- --------------------------------------------------------------------
-- 6. 用户组表
-- --------------------------------------------------------------------
DROP TABLE IF EXISTS `bpm_user_group`;
CREATE TABLE `bpm_user_group` (
    `id`          bigint          NOT NULL AUTO_INCREMENT COMMENT '编号（主键自增）',
    `name`        varchar(100)    NOT NULL                COMMENT '用户组名称',
    `description` varchar(255)    DEFAULT NULL            COMMENT '描述',
    `status`      int             NOT NULL DEFAULT 0      COMMENT '状态（CommonStatusEnum: 0-禁用 1-启用）',
    `user_ids`    json            DEFAULT NULL            COMMENT '成员用户编号集合（JSON，Jackson3TypeHandler: Set<Long>）',
    `tenant_id`   bigint       NOT NULL DEFAULT 1      COMMENT '租户编号',
    -- BaseDO 通用字段
    `create_time` datetime        NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time` datetime        NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '最后更新时间',
    `creator`     varchar(64)     DEFAULT NULL            COMMENT '创建者',
    `updater`     varchar(64)     DEFAULT NULL            COMMENT '更新者',
    `deleted`     bit(1)          NOT NULL DEFAULT 0      COMMENT '逻辑删除',
    PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='BPM 用户组表';

-- --------------------------------------------------------------------
-- 7. 流程抄送表
-- --------------------------------------------------------------------
DROP TABLE IF EXISTS `bpm_process_instance_copy`;
CREATE TABLE `bpm_process_instance_copy` (
    `id`                    bigint        NOT NULL AUTO_INCREMENT COMMENT '编号（主键自增）',
    `start_user_id`         bigint        NOT NULL                COMMENT '发起人编号（冗余 ProcessInstance.startUserId）',
    `process_instance_name` varchar(200)  NOT NULL                COMMENT '流程实例名称（冗余 ProcessInstance.name）',
    `process_instance_id`   varchar(64)   NOT NULL                COMMENT '流程实例编号（关联 Flowable ProcessInstance.id）',
    `process_definition_id` varchar(64)   NOT NULL                COMMENT '流程定义编号（关联 ProcessInstance.processDefinitionId）',
    `category`              varchar(64)   DEFAULT NULL            COMMENT '流程分类（冗余 ProcessInstance.category）',
    `activity_id`           varchar(64)   DEFAULT NULL            COMMENT '流程活动编号（BPMN XML 节点 ID 或 抄送节点 ID）',
    `activity_name`         varchar(200)  DEFAULT NULL            COMMENT '流程活动名称（BPMN XML 节点 Name）',
    `task_id`               varchar(64)   DEFAULT NULL            COMMENT '任务编号（关联 Flowable HistoricTaskInstance.id）',
    `user_id`               bigint        NOT NULL                COMMENT '被抄送用户编号（关联 AdminUserDO.id）',
    `reason`                varchar(500)  DEFAULT NULL            COMMENT '抄送意见',
    `tenant_id`   bigint       NOT NULL DEFAULT 1      COMMENT '租户编号',
    -- BaseDO 通用字段
    `create_time`           datetime      NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间（即抄送时间）',
    `update_time`           datetime      NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '最后更新时间',
    `creator`               varchar(64)   DEFAULT NULL            COMMENT '创建者',
    `updater`               varchar(64)   DEFAULT NULL            COMMENT '更新者',
    `deleted`               bit(1)        NOT NULL DEFAULT 0      COMMENT '逻辑删除',
    PRIMARY KEY (`id`),
    INDEX `idx_process_instance_id` (`process_instance_id`),
    INDEX `idx_user_id` (`user_id`),
    INDEX `idx_activity_id` (`activity_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='BPM 流程抄送表';

-- --------------------------------------------------------------------
-- 8. OA 请假申请表
-- --------------------------------------------------------------------
DROP TABLE IF EXISTS `bpm_oa_leave`;
CREATE TABLE `bpm_oa_leave` (
    `id`                  bigint        NOT NULL AUTO_INCREMENT COMMENT '请假表单编号（主键自增）',
    `user_id`             bigint        NOT NULL                COMMENT '申请人用户编号（关联 AdminUserDO.id）',
    `type`                int           NOT NULL                COMMENT '请假类型',
    `reason`              varchar(500)  DEFAULT NULL            COMMENT '请假原因',
    `start_time`          datetime      NOT NULL                COMMENT '开始时间',
    `end_time`            datetime      NOT NULL                COMMENT '结束时间',
    `day`                 bigint        DEFAULT NULL            COMMENT '请假天数',
    `status`              int           NOT NULL DEFAULT 0      COMMENT '审批结果（BpmTaskStatusEnum: 复用 BpmProcessInstanceStatusEnum）',
    `process_instance_id` varchar(64)   DEFAULT NULL            COMMENT '对应的流程实例编号（关联 Flowable ProcessInstance.id）',
    `tenant_id`   bigint       NOT NULL DEFAULT 1      COMMENT '租户编号',
    -- BaseDO 通用字段
    `create_time`         datetime      NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time`         datetime      NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '最后更新时间',
    `creator`             varchar(64)   DEFAULT NULL            COMMENT '创建者',
    `updater`             varchar(64)   DEFAULT NULL            COMMENT '更新者',
    `deleted`             bit(1)        NOT NULL DEFAULT 0      COMMENT '逻辑删除',
    PRIMARY KEY (`id`),
    INDEX `idx_user_id` (`user_id`),
    INDEX `idx_process_instance_id` (`process_instance_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='BPM OA 请假申请表';

-- ======================================================================================
-- 索引建议
-- ======================================================================================

-- bpm_category
ALTER TABLE `bpm_category` ADD UNIQUE INDEX `uk_code` (`code`);

-- bpm_form
ALTER TABLE `bpm_form` ADD INDEX `idx_status` (`status`);

-- bpm_process_expression
ALTER TABLE `bpm_process_expression` ADD INDEX `idx_status` (`status`);

-- bpm_process_listener
ALTER TABLE `bpm_process_listener` ADD INDEX `idx_type_event` (`type`, `event`);

-- bpm_user_group
ALTER TABLE `bpm_user_group` ADD INDEX `idx_status` (`status`);
