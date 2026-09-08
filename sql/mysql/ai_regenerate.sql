-- =============================================================
-- AI 回答重新生成版本表（增量脚本）
-- 说明：版本表存储全部回答版本（含当前生效版本），
--       当前生效的回答同时存于 ai_chat_dialogue 主行（消息链锚点，id 不变）；
--       version 为版本序号（第几次回答，1 起），is_current 标记当前生效版本。
-- =============================================================

DROP TABLE IF EXISTS `ai_chat_regenerate`;
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
