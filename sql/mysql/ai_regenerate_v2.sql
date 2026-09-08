-- =============================================================
-- AI 回答重新生成版本表 V2 增量脚本
-- 说明：版本表改为存储全部版本（含当前生效版本），
--       通过 version（第几次回答）排序 + is_current（是否当前生效）标记；
--       主行仍保存当前版本内容作为消息链锚点（上下文/导出/收藏/反馈无感）。
-- 旧数据兼容：老版本行 version 默认 1、is_current 默认 0，
--       查询时按 (version, create_time, id) 排序，若无 current 行则由主行补当前版，
--       首次切换时会自动把主行内容补为最新版本行，无需手工修复数据。
-- =============================================================

ALTER TABLE `ai_chat_regenerate`
  ADD COLUMN `version` int NOT NULL DEFAULT 1 COMMENT '版本序号（第几次回答，1 起）' AFTER `last_id`,
  ADD COLUMN `is_current` tinyint NOT NULL DEFAULT '0' COMMENT '是否当前生效版本：1 是、0 否' AFTER `version`;
