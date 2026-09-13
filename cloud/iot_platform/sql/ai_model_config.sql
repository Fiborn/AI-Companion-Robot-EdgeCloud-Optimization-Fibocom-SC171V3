-- AI API and model configuration module.
-- Import this file into the same database used by iot_platform.sql.

DROP TABLE IF EXISTS `s_ai_model_config`;
CREATE TABLE `s_ai_model_config` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT '主键',
  `name` varchar(64) NOT NULL COMMENT '配置名称',
  `provider` varchar(64) NOT NULL COMMENT '供应商',
  `base_url` varchar(255) NOT NULL COMMENT 'API地址',
  `api_key` varchar(512) DEFAULT NULL COMMENT 'API Key',
  `model_name` varchar(128) NOT NULL COMMENT '模型名称',
  `api_type` varchar(32) DEFAULT 'openai' COMMENT '接口类型',
  `temperature` decimal(4,2) DEFAULT 0.70 COMMENT '温度',
  `max_tokens` int(11) DEFAULT 1024 COMMENT '最大输出长度',
  `system_prompt` text COMMENT '系统提示词',
  `enabled` tinyint(1) DEFAULT 1 COMMENT '是否启用',
  `active` tinyint(1) DEFAULT 0 COMMENT '是否当前使用',
  `create_by` varchar(64) DEFAULT '' COMMENT '创建者',
  `create_time` datetime DEFAULT NULL COMMENT '创建时间',
  `update_by` varchar(64) DEFAULT '' COMMENT '更新者',
  `update_time` datetime DEFAULT NULL COMMENT '更新时间',
  `remark` varchar(500) DEFAULT NULL COMMENT '备注',
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE KEY `uk_s_ai_model_config_name` (`name`) USING BTREE,
  KEY `idx_s_ai_model_config_active` (`active`, `enabled`) USING BTREE
) ENGINE=InnoDB AUTO_INCREMENT=1 DEFAULT CHARSET=utf8mb4 COMMENT='AI模型配置表';

INSERT INTO `s_ai_model_config`
(`name`, `provider`, `base_url`, `api_key`, `model_name`, `api_type`, `temperature`, `max_tokens`, `system_prompt`, `enabled`, `active`, `create_by`, `create_time`, `remark`)
VALUES
('DeepSeek默认', 'deepseek', 'https://api.deepseek.com/v1/chat/completions', '', 'deepseek-chat', 'openai', 0.70, 1024, '你是SC171边缘AIoT桌面机器人的中文助手，回答要简洁、可靠、适合家庭和办公场景。', 1, 1, 'admin', sysdate(), '把API Key填入后即可使用');

-- Optional menu entries. AI settings are shown as a top-level sidebar item.
-- Adjust menu_id if these IDs already exist in your database.
INSERT INTO `sys_menu` VALUES (2100, 'AI设置', 0, 5, 'ai-setting', 'ai/model/index', '', '', 1, 0, 'C', '0', '0', 'ai:model:list', 'guide', 'admin', sysdate(), '', NULL, 'AI模型与接口配置');
INSERT INTO `sys_menu` VALUES (2101, 'AI设置查询', 2100, 1, '#', '', '', '', 1, 0, 'F', '0', '0', 'ai:model:query', '#', 'admin', sysdate(), '', NULL, '');
INSERT INTO `sys_menu` VALUES (2102, 'AI设置新增', 2100, 2, '#', '', '', '', 1, 0, 'F', '0', '0', 'ai:model:add', '#', 'admin', sysdate(), '', NULL, '');
INSERT INTO `sys_menu` VALUES (2103, 'AI设置修改', 2100, 3, '#', '', '', '', 1, 0, 'F', '0', '0', 'ai:model:edit', '#', 'admin', sysdate(), '', NULL, '');
INSERT INTO `sys_menu` VALUES (2104, 'AI设置删除', 2100, 4, '#', '', '', '', 1, 0, 'F', '0', '0', 'ai:model:remove', '#', 'admin', sysdate(), '', NULL, '');
