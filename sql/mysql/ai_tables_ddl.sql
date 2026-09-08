-- MySQL dump 10.13  Distrib 8.0.19, for Win64 (x86_64)
--
-- Host: local.dzint.cn    Database: dzint-nexus-pro
-- ------------------------------------------------------
-- Server version	8.0.31

/*!40101 SET @OLD_CHARACTER_SET_CLIENT=@@CHARACTER_SET_CLIENT */;
/*!40101 SET @OLD_CHARACTER_SET_RESULTS=@@CHARACTER_SET_RESULTS */;
/*!40101 SET @OLD_COLLATION_CONNECTION=@@COLLATION_CONNECTION */;
/*!50503 SET NAMES utf8mb4 */;
/*!40103 SET @OLD_TIME_ZONE=@@TIME_ZONE */;
/*!40103 SET TIME_ZONE='+00:00' */;
/*!40014 SET @OLD_UNIQUE_CHECKS=@@UNIQUE_CHECKS, UNIQUE_CHECKS=0 */;
/*!40014 SET @OLD_FOREIGN_KEY_CHECKS=@@FOREIGN_KEY_CHECKS, FOREIGN_KEY_CHECKS=0 */;
/*!40101 SET @OLD_SQL_MODE=@@SQL_MODE, SQL_MODE='NO_AUTO_VALUE_ON_ZERO' */;
/*!40111 SET @OLD_SQL_NOTES=@@SQL_NOTES, SQL_NOTES=0 */;

--
-- Table structure for table `ai_chat_tools`
--

DROP TABLE IF EXISTS `ai_chat_tools`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `ai_chat_tools` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '雪花ID',
  `name` varchar(90) NOT NULL COMMENT '工具英文名称（唯一）',
  `rename` varchar(100) DEFAULT NULL COMMENT '工具中文名称',
  `type` tinyint DEFAULT NULL COMMENT '0 系统工具、1 MCP工具',
  `description` varchar(390) DEFAULT NULL COMMENT '描述',
  `settings` json DEFAULT NULL COMMENT '工具设置信息',
  `functions` json DEFAULT NULL COMMENT '工具内部实现的功能方法',
  `deleted` tinyint DEFAULT '0',
  `is_online` tinyint NOT NULL DEFAULT '1' COMMENT '是否在线',
  `creator` varchar(64) DEFAULT NULL COMMENT '创建者',
  `create_time` timestamp NULL DEFAULT NULL COMMENT '创建时间',
  `updater` varchar(64) DEFAULT NULL COMMENT '更新者',
  `update_time` timestamp NULL DEFAULT NULL COMMENT '更新时间',
  `tenant_id` bigint DEFAULT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_name_deleted` (`name`,`deleted`)
) ENGINE=InnoDB AUTO_INCREMENT=437414798733504515 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='模型工具';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `ai_chat_dialogue_content`
--

DROP TABLE IF EXISTS `ai_chat_dialogue_content`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `ai_chat_dialogue_content` (
  `dialogue_id` bigint unsigned NOT NULL COMMENT '对话ID（关联主表id）',
  `tenant_id` bigint DEFAULT NULL,
  `content` json DEFAULT NULL COMMENT '对话内容',
  PRIMARY KEY (`dialogue_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='对话内容详情';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `ai_chat_settings`
--

DROP TABLE IF EXISTS `ai_chat_settings`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `ai_chat_settings` (
  `user_id` bigint NOT NULL COMMENT '用户ID',
  `model_id` bigint DEFAULT NULL COMMENT '模型ID',
  `embedding_model` bigint DEFAULT NULL COMMENT '嵌入向量模型',
  `settings` json DEFAULT NULL COMMENT '对话界面的设置信息',
  `deleted` tinyint DEFAULT NULL,
  `creator` varchar(64) NOT NULL,
  `create_time` timestamp NOT NULL,
  `updater` varchar(64) NOT NULL,
  `update_time` timestamp NOT NULL,
  `tenant_id` bigint DEFAULT NULL,
  UNIQUE KEY `iims_aigc_settings_user_id_uindex` (`user_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='用户设置表';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `ai_task_history_data`
--

DROP TABLE IF EXISTS `ai_task_history_data`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `ai_task_history_data` (
  `task_id` bigint NOT NULL COMMENT '任务ID，关联主表id',
  `unprocessed` json DEFAULT NULL COMMENT '未处理内容',
  `result` json DEFAULT NULL COMMENT '执行结果',
  `tenant_id` bigint DEFAULT NULL,
  PRIMARY KEY (`task_id`),
  CONSTRAINT `fk_task_data_task_id` FOREIGN KEY (`task_id`) REFERENCES `ai_task_history` (`id`) ON DELETE CASCADE ON UPDATE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='任务调度历史数据扩展表';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `ai_chat_models`
--

DROP TABLE IF EXISTS `ai_chat_models`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `ai_chat_models` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `rename` varchar(45) DEFAULT NULL COMMENT '重命名',
  `name` varchar(90) NOT NULL COMMENT '模型名称',
  `key` varchar(360) DEFAULT NULL COMMENT '模型调用密钥',
  `token` int DEFAULT NULL COMMENT '模型最大令牌数',
  `type` tinyint DEFAULT NULL COMMENT '模型接口类型：\n1. Agent: 0\n2. OpenAI：1\n3. Ollama：2',
  `url` varchar(360) DEFAULT NULL COMMENT '模型访问地址',
  `description` varchar(120) DEFAULT NULL COMMENT '描述',
  `model_type` bigint DEFAULT NULL COMMENT '模型类型：0 向量化、1 大语言、2 视觉模型、3多模态模型',
  `is_online` int DEFAULT NULL COMMENT '是否在线：0 离线、1 在线',
  `deleted` tinyint(1) NOT NULL COMMENT '是否删除',
  `detection_time` timestamp NULL DEFAULT NULL COMMENT '检测模型连通性时间',
  `creator` varchar(64) NOT NULL COMMENT '创建者',
  `create_time` timestamp NOT NULL COMMENT '创建时间',
  `updater` varchar(64) NOT NULL COMMENT '更新者',
  `update_time` timestamp NOT NULL COMMENT '更新时间',
  `tenant_id` bigint DEFAULT NULL,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=9 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='模型仓库';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `ai_usage_record`
--

DROP TABLE IF EXISTS `ai_usage_record`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `ai_usage_record` (
  `id` bigint unsigned NOT NULL AUTO_INCREMENT,
  `type` tinyint NOT NULL COMMENT '使用记录类型：wiki 0、document 1',
  `object_id` bigint DEFAULT NULL COMMENT '记录类型对象ID',
  `deleted` tinyint unsigned NOT NULL DEFAULT '0' COMMENT '是否删除：1 删除、0 保存',
  `tenant_id` bigint DEFAULT NULL,
  `creator` varchar(64) DEFAULT NULL COMMENT '创建者',
  `create_time` timestamp NULL DEFAULT NULL COMMENT '创建时间',
  `updater` varchar(64) DEFAULT NULL COMMENT '更新者',
  `update_time` timestamp NULL DEFAULT NULL COMMENT '更新时间',
  PRIMARY KEY (`id`),
  UNIQUE KEY `iims_integral_usage_record_pk_2` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='使用记录';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `ai_chat_token`
--

DROP TABLE IF EXISTS `ai_chat_token`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `ai_chat_token` (
  `id` bigint unsigned NOT NULL AUTO_INCREMENT COMMENT '智能体ID',
  `total_tokens` bigint DEFAULT NULL COMMENT '总 token 使用量',
  `prompt_tokens` bigint DEFAULT NULL COMMENT '输入(prompt) token 使用量',
  `completion_tokens` bigint DEFAULT NULL COMMENT '输出(completion) token 使用量',
  `type` tinyint DEFAULT NULL COMMENT '模型接口类型： 1. Agent: 0 2. OpenAI：1 3. Ollama：2',
  `model` varchar(90) DEFAULT NULL COMMENT '模型名称',
  `deleted` tinyint DEFAULT NULL,
  `creator` varchar(64) DEFAULT NULL COMMENT '创建者',
  `create_time` timestamp NULL DEFAULT NULL COMMENT '创建时间',
  `updater` varchar(64) DEFAULT NULL COMMENT '更新者',
  `update_time` timestamp NULL DEFAULT NULL COMMENT '更新时间',
  `tenant_id` bigint DEFAULT NULL,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=93 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='token统计';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `ai_wiki_catalog`
--

DROP TABLE IF EXISTS `ai_wiki_catalog`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `ai_wiki_catalog` (
  `id` bigint unsigned NOT NULL AUTO_INCREMENT COMMENT 'id',
  `wiki_id` bigint unsigned NOT NULL COMMENT '知识库id',
  `document_id` bigint unsigned DEFAULT NULL COMMENT '文档id',
  `title` text NOT NULL COMMENT '标题',
  `level` tinyint NOT NULL DEFAULT '1' COMMENT '目录层级',
  `parent_id` bigint unsigned DEFAULT NULL COMMENT '父目录id',
  `sort` tinyint unsigned NOT NULL DEFAULT '1' COMMENT '排序',
  `deleted` tinyint NOT NULL DEFAULT '0' COMMENT '删除标志位：0：未删除 1：已删除',
  `is_embedding` tinyint DEFAULT '0' COMMENT '是否向量化：0 未向量化、1 向量化',
  `creator` varchar(64) NOT NULL COMMENT '创建者',
  `create_time` timestamp NOT NULL COMMENT '创建时间',
  `updater` varchar(64) NOT NULL COMMENT '更新者',
  `update_time` timestamp NOT NULL COMMENT '最后一次更新时间',
  `tenant_id` bigint DEFAULT NULL,
  PRIMARY KEY (`id`),
  KEY `idx_parent_id` (`parent_id`),
  KEY `idx_sort` (`sort`),
  KEY `idx_wiki_id` (`wiki_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci ROW_FORMAT=DYNAMIC COMMENT='知识库目录表';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `ai_chat_dialogue`
--

DROP TABLE IF EXISTS `ai_chat_dialogue`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `ai_chat_dialogue` (
  `id` bigint unsigned NOT NULL AUTO_INCREMENT COMMENT '对话ID',
  `last_id` bigint DEFAULT NULL COMMENT '对话记录下一个的ID',
  `topic_id` bigint DEFAULT NULL COMMENT '话题ID',
  `sender` char(9) DEFAULT NULL COMMENT '发送人：user、assistant',
  `metadata` json DEFAULT NULL COMMENT '元数据',
  `tools` json DEFAULT NULL COMMENT '使用工具',
  `file_ids` json DEFAULT NULL COMMENT '存放聊天文件IDS',
  `deleted` tinyint unsigned NOT NULL DEFAULT '0' COMMENT '是否删除：1 删除、0 保存',
  `feedback_status` bigint DEFAULT '0' COMMENT '0：无状态、-1：负反馈、1：正反馈',
  `is_star` tinyint(1) NOT NULL DEFAULT '0' COMMENT '是否收藏：收藏 1、未收藏 0',
  `tenant_id` bigint DEFAULT NULL COMMENT '租户',
  `creator` varchar(64) DEFAULT NULL COMMENT '创建者',
  `create_time` timestamp NULL DEFAULT NULL COMMENT '创建时间',
  `updater` varchar(64) DEFAULT NULL COMMENT '更新者',
  `update_time` timestamp NULL DEFAULT NULL COMMENT '更新时间',
  PRIMARY KEY (`id`),
  KEY `idx_create_time` (`create_time`),
  KEY `idx_topic_deleted_createtime` (`topic_id`,`deleted`,`create_time`) USING BTREE,
  KEY `idx_topic_deleted_time_desc` (`topic_id`,`deleted`,`create_time` DESC),
  KEY `idx_cover_topic_time` (`topic_id`,`deleted`,`create_time` DESC,`id`)
) ENGINE=InnoDB AUTO_INCREMENT=208 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='对话记录';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `ai_chat_topic`
--

DROP TABLE IF EXISTS `ai_chat_topic`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `ai_chat_topic` (
  `id` bigint unsigned NOT NULL AUTO_INCREMENT COMMENT '话题ID',
  `top` int DEFAULT NULL COMMENT '置顶索引',
  `title` varchar(90) DEFAULT NULL COMMENT '话题标题',
  `is_archived` tinyint DEFAULT '0' COMMENT '是否归档',
  `deleted` tinyint unsigned NOT NULL DEFAULT '0' COMMENT '是否删除：1 删除、0 保存',
  `creator` varchar(64) DEFAULT NULL COMMENT '创建者',
  `create_time` timestamp NULL DEFAULT NULL COMMENT '创建时间',
  `updater` varchar(64) DEFAULT NULL COMMENT '更新者',
  `update_time` timestamp NULL DEFAULT NULL COMMENT '更新时间',
  `tenant_id` bigint DEFAULT NULL,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=109 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='话题表';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `ai_task_history`
--

DROP TABLE IF EXISTS `ai_task_history`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `ai_task_history` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '雪花ID，任务ID',
  `object_id` bigint NOT NULL COMMENT '雪花ID，不同任务对象的ID',
  `task_type` tinyint NOT NULL COMMENT '不同任务类型：0 文档任务',
  `schedule_type` tinyint DEFAULT '1' COMMENT '调度类型：1-立即 2-定时',
  `task_name` varchar(100) NOT NULL COMMENT '任务名称',
  `deleted` tinyint NOT NULL DEFAULT '0' COMMENT '是否删除',
  `status` tinyint NOT NULL COMMENT '任务状态：0-待执行 1-执行中 2-成功 3-部分失败 4-失败 5-取消',
  `creator` varchar(64) NOT NULL COMMENT '创建者',
  `create_time` timestamp NULL DEFAULT NULL COMMENT '创建时间',
  `updater` varchar(64) DEFAULT NULL COMMENT '更新者',
  `update_time` timestamp NULL DEFAULT NULL COMMENT '更新时间',
  `tenant_id` bigint DEFAULT NULL,
  PRIMARY KEY (`object_id`,`task_type`,`creator`),
  UNIQUE KEY `iims_integral_task_pk` (`id`),
  KEY `iims_integral_task_id_index` (`id`),
  KEY `idx_create_time` (`create_time`),
  KEY `idx_query_sort` (`deleted`,`create_time` DESC,`task_type`,`status`)
) ENGINE=InnoDB AUTO_INCREMENT=3 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='任务调度历史表';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `ai_document_content`
--

DROP TABLE IF EXISTS `ai_document_content`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `ai_document_content` (
  `id` bigint unsigned NOT NULL AUTO_INCREMENT COMMENT '文章内容id',
  `document_id` bigint NOT NULL COMMENT '文档id',
  `content` mediumtext CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci COMMENT '正文',
  `chunk_keys` json DEFAULT NULL COMMENT '块的MD5值',
  `deleted` tinyint DEFAULT NULL,
  `creator` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL COMMENT '创建者',
  `create_time` timestamp NOT NULL COMMENT '创建时间',
  `updater` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL COMMENT '更新者',
  `update_time` timestamp NOT NULL COMMENT '更新时间',
  `tenant_id` bigint DEFAULT NULL,
  PRIMARY KEY (`id`),
  KEY `idx_article_id` (`document_id`),
  KEY `idx_document_id` (`document_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci ROW_FORMAT=DYNAMIC COMMENT='文章内容表';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `ai_wiki`
--

DROP TABLE IF EXISTS `ai_wiki`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `ai_wiki` (
  `id` bigint unsigned NOT NULL AUTO_INCREMENT COMMENT 'id',
  `type` tinyint DEFAULT '0' COMMENT '知识库类型：0 企业，1 组织，2 个人',
  `title` varchar(120) NOT NULL DEFAULT '' COMMENT '标题',
  `cover` bigint NOT NULL COMMENT '封面：文件ID',
  `weight` int unsigned NOT NULL DEFAULT '0' COMMENT '权重，用于是否置顶（0: 未置顶；>0: 参与置顶，权重值越高越靠前）',
  `summary` varchar(390) DEFAULT '' COMMENT '摘要',
  `settings` json DEFAULT NULL COMMENT '知识库设置参数',
  `deleted` tinyint NOT NULL DEFAULT '0' COMMENT '删除标志位：0：未删除 1：已删除',
  `creator` varchar(64) NOT NULL COMMENT '创建者',
  `create_time` timestamp NOT NULL COMMENT '创建时间',
  `updater` varchar(64) NOT NULL COMMENT '更新者',
  `update_time` timestamp NOT NULL COMMENT '更新时间',
  `tenant_id` bigint DEFAULT NULL,
  PRIMARY KEY (`id`),
  KEY `idx_create_by_deleted` (`creator`,`deleted`)
) ENGINE=InnoDB AUTO_INCREMENT=444746214974582785 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci ROW_FORMAT=DYNAMIC COMMENT='知识库表';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `ai_chat_agents`
--

DROP TABLE IF EXISTS `ai_chat_agents`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `ai_chat_agents` (
  `id` bigint unsigned NOT NULL AUTO_INCREMENT COMMENT '智能体ID',
  `type` tinyint NOT NULL DEFAULT '0' COMMENT '智能体类型：0 ReAct、1 Multi',
  `name` varchar(30) DEFAULT NULL COMMENT '名称',
  `tools` json DEFAULT NULL COMMENT '可用工具唯一值列表',
  `skill_ids` json DEFAULT NULL COMMENT '关联技能ID列表',
  `cover` bigint DEFAULT NULL COMMENT '图片ID',
  `prompt` text CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci COMMENT '智能体-提示词',
  `settings` json DEFAULT NULL COMMENT '提供给多智能体使用的设置，存放子智能体的配置',
  `model_id` bigint NOT NULL COMMENT '模型ID',
  `tag_ids` json DEFAULT NULL COMMENT '标签IDS',
  `category_id` bigint DEFAULT NULL COMMENT '分类ID',
  `usage_count` bigint DEFAULT '0' COMMENT '使用次数',
  `description` varchar(120) DEFAULT NULL COMMENT '智能体-描述',
  `deleted` tinyint unsigned NOT NULL DEFAULT '0' COMMENT '是否删除：1 删除、0 保存',
  `creator` varchar(64) DEFAULT NULL COMMENT '创建者',
  `create_time` timestamp NULL DEFAULT NULL COMMENT '创建时间',
  `updater` varchar(64) DEFAULT NULL COMMENT '更新者',
  `update_time` timestamp NULL DEFAULT NULL COMMENT '更新时间',
  `tenant_id` bigint DEFAULT NULL,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=2042164486403522561 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='智能体';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `ai_document`
--

DROP TABLE IF EXISTS `ai_document`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `ai_document` (
  `id` bigint unsigned NOT NULL AUTO_INCREMENT COMMENT '文档id',
  `title` varchar(120) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL DEFAULT '' COMMENT '标题',
  `cover` varchar(120) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci DEFAULT '' COMMENT '封面',
  `summary` varchar(960) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci DEFAULT '' COMMENT '摘要',
  `read_num` int unsigned NOT NULL DEFAULT '0' COMMENT '被阅读次数',
  `type` tinyint NOT NULL DEFAULT '1' COMMENT '类型 - 1：普通，2：收录于知识库',
  `weight` int unsigned NOT NULL DEFAULT '0' COMMENT '权重，用于是否置顶（0: 未置顶；>0: 参与置顶，权重值越高越靠前）',
  `file_id` bigint DEFAULT NULL COMMENT '文件ID',
  `resource_ids` json DEFAULT NULL COMMENT '内容资源文件ID列表（文章正文引用的图片/视频/附件）',
  `dict_tag_ids` json DEFAULT NULL COMMENT '标签：字典值IDS',
  `dict_category_id` bigint DEFAULT NULL COMMENT '分类：字典值ID',
  `deleted` tinyint NOT NULL DEFAULT '0' COMMENT '删除标志位：0：未删除 1：已删除',
  `creator` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL COMMENT '创建者',
  `create_time` timestamp NOT NULL COMMENT '创建时间',
  `updater` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL COMMENT '更新者',
  `update_time` timestamp NOT NULL COMMENT '更新时间',
  `tenant_id` bigint DEFAULT NULL,
  PRIMARY KEY (`id`),
  KEY `idx_create_by_deleted` (`creator`,`deleted`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci ROW_FORMAT=DYNAMIC COMMENT='文档表';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `ai_skill`
--

DROP TABLE IF EXISTS `ai_skill`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `ai_skill` (
  `id` bigint unsigned NOT NULL AUTO_INCREMENT COMMENT '技能ID',
  `name` varchar(64) NOT NULL COMMENT '技能名称（小写字母、数字、连字符，最长64字符，对应SKILL.md的name字段）',
  `display_name` varchar(120) DEFAULT NULL COMMENT '技能展示名称',
  `description` varchar(512) NOT NULL COMMENT '技能描述（LLM据此判断何时使用该技能，对应SKILL.md的description字段）',
  `skill_md_content` text CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT 'SKILL.md完整内容（YAML frontmatter + Markdown正文）',
  `icon` bigint DEFAULT NULL COMMENT '技能图标文件ID',
  `category_id` bigint DEFAULT NULL COMMENT '技能分类ID',
  `tag_ids` json DEFAULT NULL COMMENT '技能标签ID列表',
  `version` int NOT NULL DEFAULT '1' COMMENT '版本号',
  `status` tinyint NOT NULL DEFAULT '1' COMMENT '状态：0=禁用、1=启用',
  `deleted` tinyint unsigned NOT NULL DEFAULT '0' COMMENT '是否删除：1=删除、0=保存',
  `creator` varchar(64) NOT NULL COMMENT '创建者',
  `create_time` timestamp NOT NULL COMMENT '创建时间',
  `updater` varchar(64) NOT NULL COMMENT '更新者',
  `update_time` timestamp NOT NULL COMMENT '更新时间',
  `tenant_id` bigint DEFAULT NULL COMMENT '租户ID',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_name_tenant` (`name`, `tenant_id`, `deleted`),
  KEY `idx_category_id` (`category_id`),
  KEY `idx_status` (`status`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='AI技能定义表';

--
-- Table structure for table `ai_skill_resource`
--

DROP TABLE IF EXISTS `ai_skill_resource`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `ai_skill_resource` (
  `id` bigint unsigned NOT NULL AUTO_INCREMENT COMMENT '资源ID',
  `skill_id` bigint unsigned NOT NULL COMMENT '关联技能ID',
  `resource_type` tinyint NOT NULL DEFAULT '0' COMMENT '资源类型：0=脚本(scripts)、1=参考资料(references)、2=示例(examples)、3=其他',
  `file_id` bigint DEFAULT NULL COMMENT '文件ID（关联文件上传系统）',
  `sort_order` int NOT NULL DEFAULT '0' COMMENT '排序序号',
  `deleted` tinyint unsigned NOT NULL DEFAULT '0' COMMENT '是否删除：1=删除、0=保存',
  `creator` varchar(64) NOT NULL COMMENT '创建者',
  `create_time` timestamp NOT NULL COMMENT '创建时间',
  `updater` varchar(64) NOT NULL COMMENT '更新者',
  `update_time` timestamp NOT NULL COMMENT '更新时间',
  `tenant_id` bigint DEFAULT NULL COMMENT '租户ID',
  PRIMARY KEY (`id`),
  KEY `idx_skill_id` (`skill_id`),
  KEY `idx_resource_type` (`resource_type`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='AI技能资源表';

--
-- Table structure for table `ai_skill_execution`
--

DROP TABLE IF EXISTS `ai_skill_execution`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `ai_skill_execution` (
  `id` bigint unsigned NOT NULL AUTO_INCREMENT COMMENT '执行记录ID',
  `agent_id` bigint unsigned DEFAULT NULL COMMENT '智能体ID',
  `skill_id` bigint unsigned DEFAULT NULL COMMENT '技能ID',
  `skill_name` varchar(64) NOT NULL COMMENT '技能名称（冗余，便于查询）',
  `conversation_id` varchar(64) DEFAULT NULL COMMENT '会话ID',
  `session_id` varchar(128) DEFAULT NULL COMMENT 'Graph线程/会话标识',
  `input_params` json DEFAULT NULL COMMENT '输入参数（JSON）',
  `output_result` longtext CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci COMMENT '输出结果',
  `status` tinyint NOT NULL DEFAULT '0' COMMENT '执行状态：0=待执行、1=执行中、2=成功、3=失败、4=超时',
  `error_message` text CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci COMMENT '错误信息',
  `duration_ms` int DEFAULT NULL COMMENT '执行耗时（毫秒）',
  `token_usage` int DEFAULT NULL COMMENT 'Token消耗量',
  `deleted` tinyint unsigned NOT NULL DEFAULT '0' COMMENT '是否删除：1=删除、0=保存',
  `creator` varchar(64) NOT NULL COMMENT '创建者',
  `create_time` timestamp NOT NULL COMMENT '创建时间',
  `updater` varchar(64) NOT NULL COMMENT '更新者',
  `update_time` timestamp NOT NULL COMMENT '更新时间',
  `tenant_id` bigint DEFAULT NULL COMMENT '租户ID',
  PRIMARY KEY (`id`),
  KEY `idx_agent_id` (`agent_id`),
  KEY `idx_skill_id` (`skill_id`),
  KEY `idx_skill_name` (`skill_name`),
  KEY `idx_conversation_id` (`conversation_id`),
  KEY `idx_status` (`status`),
  KEY `idx_create_time` (`create_time`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='AI技能执行记录表';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `ai_chat_regenerate`
--

DROP TABLE IF EXISTS `ai_chat_regenerate`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `ai_chat_regenerate` (
  `id` bigint unsigned NOT NULL AUTO_INCREMENT COMMENT '主键',
  `dialogue_id` bigint unsigned NOT NULL COMMENT '归属的assistant对话ID（锚点，重生成时不变）',
  `topic_id` bigint unsigned NOT NULL COMMENT '话题ID',
  `last_id` bigint unsigned NOT NULL COMMENT '用户消息ID（重生成的问题锚点）',
  `version` int NOT NULL DEFAULT '1' COMMENT '版本序号（第几次回答，1 起）',
  `is_current` tinyint NOT NULL DEFAULT '0' COMMENT '是否当前生效版本：1 是、0 否',
  `content` json DEFAULT NULL COMMENT '回答内容（aiContent数组 或 aiAgentMessage对象）',
  `metadata` json DEFAULT NULL COMMENT '知识库引用元数据',
  `tools` json DEFAULT NULL COMMENT '工具调用记录',
  `file_ids` json DEFAULT NULL COMMENT '生成的文件ID列表',
  `deleted` tinyint unsigned NOT NULL DEFAULT '0' COMMENT '是否删除：1 删除、0 保存',
  `tenant_id` bigint DEFAULT NULL COMMENT '租户',
  `creator` varchar(64) DEFAULT NULL COMMENT '创建者',
  `create_time` timestamp NULL DEFAULT NULL COMMENT '创建时间',
  `updater` varchar(64) DEFAULT NULL COMMENT '更新者',
  `update_time` timestamp NULL DEFAULT NULL COMMENT '更新时间',
  PRIMARY KEY (`id`),
  KEY `idx_dialogue_deleted` (`dialogue_id`,`deleted`,`create_time`),
  KEY `idx_topic_deleted` (`topic_id`,`deleted`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='AI回答重新生成版本表';
/*!40103 SET TIME_ZONE=@OLD_TIME_ZONE */;

/*!40101 SET SQL_MODE=@OLD_SQL_MODE */;
/*!40014 SET FOREIGN_KEY_CHECKS=@OLD_FOREIGN_KEY_CHECKS */;
/*!40014 SET UNIQUE_CHECKS=@OLD_UNIQUE_CHECKS */;
/*!40101 SET CHARACTER_SET_CLIENT=@OLD_CHARACTER_SET_CLIENT */;
/*!40101 SET CHARACTER_SET_RESULTS=@OLD_CHARACTER_SET_RESULTS */;
/*!40101 SET COLLATION_CONNECTION=@OLD_COLLATION_CONNECTION */;
/*!40111 SET SQL_NOTES=@OLD_SQL_NOTES */;

-- Dump completed on 2026-07-30 18:39:12
