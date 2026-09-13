-- Move AI settings out of the built-in "系统工具" directory.
-- Run this on databases where sql/ai_model_config.sql was already imported.

UPDATE `sys_menu`
SET `parent_id` = 0,
    `order_num` = 5,
    `path` = 'ai-setting',
    `component` = 'ai/model/index',
    `menu_type` = 'C',
    `visible` = '0',
    `status` = '0',
    `perms` = 'ai:model:list',
    `icon` = 'guide',
    `update_by` = 'admin',
    `update_time` = sysdate(),
    `remark` = 'AI模型与接口配置'
WHERE `menu_id` = 2100;

DELETE FROM `sys_menu`
WHERE `menu_id` = 2099
  AND NOT EXISTS (SELECT 1 FROM (SELECT `menu_id` FROM `sys_menu` WHERE `parent_id` = 2099) AS child_menu);
