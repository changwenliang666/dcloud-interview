-- 简历分析投递去重。发布前执行一次。
-- utf8mb4 下 content、evaluation 的 VARCHAR 已经占满行宽，先改为 TEXT 才能加列。
ALTER TABLE resume
    MODIFY COLUMN content MEDIUMTEXT NULL COMMENT '简历内容',
    MODIFY COLUMN evaluation MEDIUMTEXT NULL COMMENT 'AI简历分析';

ALTER TABLE resume
    ADD COLUMN analyse_message_id VARCHAR(64) NULL COMMENT '分析任务消息ID，用于投递去重' AFTER status,
    ADD COLUMN analyse_accepted TINYINT NOT NULL DEFAULT 0 COMMENT 'Serverless是否已接收：0否 1是' AFTER analyse_message_id;
