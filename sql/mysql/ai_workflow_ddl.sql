--
-- Table structure for table `ai_workflow`
--

DROP TABLE IF EXISTS `ai_workflow`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `ai_workflow` (
  `id` bigint unsigned NOT NULL AUTO_INCREMENT COMMENT '工作流ID',
  `name` varchar(64) NOT NULL COMMENT '工作流名称',
  `description` varchar(512) DEFAULT NULL COMMENT '描述',
  `icon` bigint DEFAULT NULL COMMENT '图标文件ID',
  `status` tinyint NOT NULL DEFAULT '0' COMMENT '状态：0草稿、1已发布、2已下架',
  `version` int NOT NULL DEFAULT '1' COMMENT '版本号（每次发布自增）',
  `graph` json DEFAULT NULL COMMENT '画布数据（nodes + edges）',
  `published_graph` json DEFAULT NULL COMMENT '已发布画布快照',
  `usage_count` bigint DEFAULT '0' COMMENT '使用次数',
  `deleted` tinyint unsigned NOT NULL DEFAULT '0' COMMENT '是否删除：1 删除、0 保存',
  `tenant_id` bigint DEFAULT NULL COMMENT '租户',
  `creator` varchar(64) DEFAULT NULL COMMENT '创建者',
  `create_time` timestamp NULL DEFAULT NULL COMMENT '创建时间',
  `updater` varchar(64) DEFAULT NULL COMMENT '更新者',
  `update_time` timestamp NULL DEFAULT NULL COMMENT '更新时间',
  PRIMARY KEY (`id`),
  KEY `idx_name` (`name`),
  KEY `idx_status` (`status`),
  KEY `idx_creator_deleted` (`creator`,`deleted`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='AI工作流定义表';

--
-- Table structure for table `ai_workflow_run`
--

DROP TABLE IF EXISTS `ai_workflow_run`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `ai_workflow_run` (
  `id` bigint unsigned NOT NULL AUTO_INCREMENT COMMENT '运行ID',
  `workflow_id` bigint unsigned NOT NULL COMMENT '工作流ID',
  `status` tinyint NOT NULL DEFAULT '0' COMMENT '运行状态：0运行中、1成功、2失败、3已停止',
  `inputs` json DEFAULT NULL COMMENT '运行输入（变量名 -> 值）',
  `outputs` json DEFAULT NULL COMMENT '运行输出（变量名 -> 值）',
  `node_logs` json DEFAULT NULL COMMENT '节点执行日志（nodeId -> 状态/耗时/输出）',
  `file_infos` json DEFAULT NULL COMMENT '最终产物（presentFiles 工具生成的文件列表）',
  `error` varchar(500) DEFAULT NULL COMMENT '错误信息',
  `elapsed_ms` bigint DEFAULT NULL COMMENT '总耗时（毫秒）',
  `deleted` tinyint unsigned NOT NULL DEFAULT '0' COMMENT '是否删除：1 删除、0 保存',
  `tenant_id` bigint DEFAULT NULL COMMENT '租户',
  `creator` varchar(64) DEFAULT NULL COMMENT '创建者',
  `create_time` timestamp NULL DEFAULT NULL COMMENT '创建时间',
  `updater` varchar(64) DEFAULT NULL COMMENT '更新者',
  `update_time` timestamp NULL DEFAULT NULL COMMENT '更新时间',
  PRIMARY KEY (`id`),
  KEY `idx_workflow_id` (`workflow_id`),
  KEY `idx_creator_deleted` (`creator`,`deleted`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='AI工作流运行记录表';
