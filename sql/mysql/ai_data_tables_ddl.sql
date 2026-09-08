-- ----------------------------------------------------------------------------
-- yudao-ai-data 数据采集与治理模块建表脚本
-- 模块包：cn.iocoder.yudao.module.ai.data
-- 生成时间：2026-08-17
-- ----------------------------------------------------------------------------

SET NAMES utf8mb4;
SET FOREIGN_KEY_CHECKS = 0;

-- ----------------------------------------------------------------------------
-- 1. 数据源配置表
-- ----------------------------------------------------------------------------
DROP TABLE IF EXISTS `ai_data_source_config`;
CREATE TABLE `ai_data_source_config` (
  `id` bigint unsigned NOT NULL AUTO_INCREMENT COMMENT '主键 ID',
  `source_type` tinyint NOT NULL COMMENT '采集源类型：1 文件系统 2 HTTP 下载 3 爬虫',
  `name` varchar(120) NOT NULL COMMENT '数据源名称',
  `config` json DEFAULT NULL COMMENT '采集源配置参数（JSON，多态）',
  `enabled` tinyint NOT NULL DEFAULT '1' COMMENT '是否启用：0 禁用 1 启用',
  `remark` varchar(500) DEFAULT NULL COMMENT '备注',
  `creator` varchar(64) DEFAULT '' COMMENT '创建者',
  `create_time` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `updater` varchar(64) DEFAULT '' COMMENT '更新者',
  `update_time` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `deleted` tinyint NOT NULL DEFAULT '0' COMMENT '删除标志位：0 未删除 1 已删除',
  `tenant_id` bigint DEFAULT NULL COMMENT '租户编号',
  PRIMARY KEY (`id`),
  KEY `idx_source_type` (`source_type`),
  KEY `idx_deleted` (`deleted`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='数据源配置表';

-- ----------------------------------------------------------------------------
-- 2. 采集任务表
-- ----------------------------------------------------------------------------
DROP TABLE IF EXISTS `ai_data_collect_task`;
CREATE TABLE `ai_data_collect_task` (
  `id` bigint unsigned NOT NULL AUTO_INCREMENT COMMENT '主键 ID',
  `source_config_id` bigint NOT NULL COMMENT '采集源配置 ID',
  `status` tinyint NOT NULL DEFAULT '0' COMMENT '任务状态：0 进行中 1 成功 2 失败 3 部分成功',
  `total_count` int NOT NULL DEFAULT '0' COMMENT '采集文件总数',
  `success_count` int NOT NULL DEFAULT '0' COMMENT '成功数',
  `fail_count` int NOT NULL DEFAULT '0' COMMENT '失败数',
  `remark` varchar(500) DEFAULT NULL COMMENT '备注',
  `creator` varchar(64) DEFAULT '' COMMENT '创建者',
  `create_time` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `updater` varchar(64) DEFAULT '' COMMENT '更新者',
  `update_time` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `deleted` tinyint NOT NULL DEFAULT '0' COMMENT '删除标志位：0 未删除 1 已删除',
  `tenant_id` bigint DEFAULT NULL COMMENT '租户编号',
  PRIMARY KEY (`id`),
  KEY `idx_source_config_id` (`source_config_id`),
  KEY `idx_deleted` (`deleted`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='采集任务表';

-- ----------------------------------------------------------------------------
-- 3. 采集结果表
-- ----------------------------------------------------------------------------
DROP TABLE IF EXISTS `ai_data_collect_result`;
CREATE TABLE `ai_data_collect_result` (
  `id` bigint unsigned NOT NULL AUTO_INCREMENT COMMENT '主键 ID',
  `task_id` bigint NOT NULL COMMENT '采集任务 ID',
  `source_path` varchar(1024) DEFAULT NULL COMMENT '来源路径（文件路径或 URL）',
  `file_name` varchar(255) DEFAULT NULL COMMENT '文件名',
  `file_type` varchar(32) DEFAULT NULL COMMENT '文件类型（扩展名，小写）',
  `file_size` bigint DEFAULT NULL COMMENT '文件大小（字节）',
  `file_hash` varchar(64) DEFAULT NULL COMMENT '文件 SHA-256 摘要（用于去重）',
  `warehouse_file_id` bigint DEFAULT NULL COMMENT '采集产物（Markdown）归档文件 ID',
  `asset_id` bigint DEFAULT NULL COMMENT '关联的数据资产 ID',
  `status` tinyint NOT NULL DEFAULT '0' COMMENT '结果状态：0 成功 1 失败 2 跳过',
  `convert_status` tinyint NOT NULL DEFAULT '0' COMMENT '转换状态：0 待转换 1 转换中 2 已转换 3 无需转换 4 转换失败',
  `error_msg` varchar(1024) DEFAULT NULL COMMENT '错误信息',
  `creator` varchar(64) DEFAULT '' COMMENT '创建者',
  `create_time` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `updater` varchar(64) DEFAULT '' COMMENT '更新者',
  `update_time` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `deleted` tinyint NOT NULL DEFAULT '0' COMMENT '删除标志位：0 未删除 1 已删除',
  `tenant_id` bigint DEFAULT NULL COMMENT '租户编号',
  PRIMARY KEY (`id`),
  KEY `idx_task_id` (`task_id`),
  KEY `idx_file_hash` (`file_hash`),
  KEY `idx_deleted` (`deleted`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='采集结果表';

-- ----------------------------------------------------------------------------
-- 4. 数据资产表
-- ----------------------------------------------------------------------------
DROP TABLE IF EXISTS `ai_data_asset`;
CREATE TABLE `ai_data_asset` (
  `id` bigint unsigned NOT NULL AUTO_INCREMENT COMMENT '主键 ID',
  `source_config_id` bigint DEFAULT NULL COMMENT '采集源配置 ID',
  `collect_task_id` bigint DEFAULT NULL COMMENT '采集任务 ID',
  `collect_result_id` bigint DEFAULT NULL COMMENT '采集结果 ID',
  `file_id` bigint DEFAULT NULL COMMENT '采集产物（Markdown）归档文件 ID',
  `title` varchar(255) DEFAULT NULL COMMENT '标题',
  `summary` varchar(960) DEFAULT NULL COMMENT '摘要',
  `cover` bigint DEFAULT NULL COMMENT '封面文件 ID',
  `category_id` bigint DEFAULT NULL COMMENT '分类 ID（归档到知识库时使用）',
  `tag_ids` json DEFAULT NULL COMMENT '标签 ID 列表（归档到知识库时使用）',
  `quality_score` int NOT NULL DEFAULT '0' COMMENT '自动质量评分（0-100）',
  `status` tinyint NOT NULL DEFAULT '0' COMMENT '资产状态：0 待转换 1 待治理 2 已治理 3 已归档 4 已废弃 5 转换失败',
  `archived_document_id` bigint DEFAULT NULL COMMENT '归档后的知识库文档 ID',
  `archived_wiki_id` bigint DEFAULT NULL COMMENT '归档后的知识库 ID',
  `archived_catalog_id` bigint DEFAULT NULL COMMENT '归档后的知识库目录 ID',
  `creator` varchar(64) DEFAULT '' COMMENT '创建者',
  `create_time` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `updater` varchar(64) DEFAULT '' COMMENT '更新者',
  `update_time` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `deleted` tinyint NOT NULL DEFAULT '0' COMMENT '删除标志位：0 未删除 1 已删除',
  `tenant_id` bigint DEFAULT NULL COMMENT '租户编号',
  PRIMARY KEY (`id`),
  KEY `idx_status` (`status`),
  KEY `idx_collect_task_id` (`collect_task_id`),
  KEY `idx_deleted` (`deleted`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='数据资产表';

-- ----------------------------------------------------------------------------
-- 5. 数据资产正文表
-- ----------------------------------------------------------------------------
DROP TABLE IF EXISTS `ai_data_asset_content`;
CREATE TABLE `ai_data_asset_content` (
  `id` bigint unsigned NOT NULL AUTO_INCREMENT COMMENT '主键 ID',
  `asset_id` bigint NOT NULL COMMENT '数据资产 ID',
  `content` longtext COMMENT 'Markdown 正文',
  `prompt` text COMMENT '最近一次 AI 清洗使用的提示词',
  `creator` varchar(64) DEFAULT '' COMMENT '创建者',
  `create_time` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `updater` varchar(64) DEFAULT '' COMMENT '更新者',
  `update_time` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `deleted` tinyint NOT NULL DEFAULT '0' COMMENT '删除标志位：0 未删除 1 已删除',
  `tenant_id` bigint DEFAULT NULL COMMENT '租户编号',
  PRIMARY KEY (`id`),
  KEY `idx_asset_id` (`asset_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='数据资产正文表';

-- ----------------------------------------------------------------------------
-- 6. 数据治理记录表
-- ----------------------------------------------------------------------------
DROP TABLE IF EXISTS `ai_data_governance_record`;
CREATE TABLE `ai_data_governance_record` (
  `id` bigint unsigned NOT NULL AUTO_INCREMENT COMMENT '主键 ID',
  `asset_id` bigint NOT NULL COMMENT '数据资产 ID',
  `action` tinyint NOT NULL COMMENT '治理动作：0 创建 1 人工编辑 2 AI 清洗 3 废弃 4 归档',
  `content_before` longtext COMMENT '治理前正文',
  `content_after` longtext COMMENT '治理后正文',
  `prompt` text COMMENT '使用的提示词（AI 清洗时）',
  `operator_id` bigint DEFAULT NULL COMMENT '操作人 ID',
  `remark` varchar(500) DEFAULT NULL COMMENT '备注',
  `creator` varchar(64) DEFAULT '' COMMENT '创建者',
  `create_time` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `updater` varchar(64) DEFAULT '' COMMENT '更新者',
  `update_time` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `deleted` tinyint NOT NULL DEFAULT '0' COMMENT '删除标志位：0 未删除 1 已删除',
  `tenant_id` bigint DEFAULT NULL COMMENT '租户编号',
  PRIMARY KEY (`id`),
  KEY `idx_asset_id` (`asset_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='数据治理记录表';

SET FOREIGN_KEY_CHECKS = 1;
