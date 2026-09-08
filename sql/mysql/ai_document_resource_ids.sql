-- =============================================================
-- 文档内容资源文件 ID 列表（增量脚本）
-- 说明：ai_document.resource_ids 记录文章正文（ai_document_content）中引用的
--       资源文件 ID（图片/视频/音频/附件），保存文档内容时由后端解析更新，
--       用于文件访问权限判定（DocumentFileAccessChecker 据此关联文档）。
-- =============================================================

ALTER TABLE `ai_document`
  ADD COLUMN `resource_ids` json DEFAULT NULL COMMENT '内容资源文件ID列表（文章正文引用的图片/视频/附件）' AFTER `file_id`;

-- 多值索引（MySQL 8.0.17+）：让 JSON_CONTAINS(resource_ids, ...) 的文件关联查询走索引，避免全表扫描
ALTER TABLE `ai_document`
  ADD INDEX `idx_resource_ids` ((CAST(`resource_ids` AS UNSIGNED ARRAY)));
