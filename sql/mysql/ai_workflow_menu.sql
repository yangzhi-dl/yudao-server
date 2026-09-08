-- ============================================================
-- AI 工作流管理菜单（挂在「智能中心」6735 下）
-- 组件路径 /ai/workflow/index 对应 web-app/src/views/ai/workflow/index.vue
-- 画布编辑器为独立路由 /ai/app/workflow（core.ts 已注册，无需菜单）
-- ============================================================

-- 查询智能中心下已有菜单的排序，避免冲突：
-- 6736 智能问答中心 (sort 0)、6740 智能体配置中心 (sort 4)
INSERT INTO `system_menu` VALUES (6741,'工作流管理','ai:workflow:list',2,5,6735,'workflow','carbon:workflow-automation','/ai/workflow/index','AiWorkflow',0,_binary '1',_binary '1',_binary '1','1','2026-08-20 12:00:00','1','2026-08-20 12:00:00',_binary '\0');

-- 按钮权限（可选，列表页当前未做按钮级权限控制，可留空）
-- INSERT INTO `system_menu` VALUES (6742,'工作流新增','ai:workflow:create',3,1,6741,'',NULL,NULL,NULL,0,_binary '1',_binary '1',_binary '1','1','2026-08-20 12:00:00','1','2026-08-20 12:00:00',_binary '\0');
-- INSERT INTO `system_menu` VALUES (6743,'工作流编辑','ai:workflow:update',3,2,6741,'',NULL,NULL,NULL,0,_binary '1',_binary '1',_binary '1','1','2026-08-20 12:00:00','1','2026-08-20 12:00:00',_binary '\0');
-- INSERT INTO `system_menu` VALUES (6744,'工作流删除','ai:workflow:delete',3,3,6741,'',NULL,NULL,NULL,0,_binary '1',_binary '1',_binary '1','1','2026-08-20 12:00:00','1','2026-08-20 12:00:00',_binary '\0');
-- INSERT INTO `system_menu` VALUES (6745,'工作流发布','ai:workflow:publish',3,4,6741,'',NULL,NULL,NULL,0,_binary '1',_binary '1',_binary '1','1','2026-08-20 12:00:00','1','2026-08-20 12:00:00',_binary '\0');
