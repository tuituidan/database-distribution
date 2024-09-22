CREATE TABLE IF NOT EXISTS `sys_app` (
    `id` bigint(20) NOT NULL COMMENT '主键',
    `app_key` varchar(100) NOT NULL COMMENT '应用标识',
    `app_secret` varchar(100) NOT NULL COMMENT '应用秘钥',
    `app_name` varchar(200) NOT NULL COMMENT '应用名称',
    `url` varchar(200) DEFAULT NULL COMMENT '地址',
    `result_exp` varchar(400) DEFAULT NULL COMMENT '结果解析表达式',
    `create_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (`id`) USING BTREE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 ROW_FORMAT=DYNAMIC COMMENT='应用';

CREATE TABLE IF NOT EXISTS `sys_datasource` (
    `id` bigint(20) NOT NULL COMMENT '主键',
    `name` varchar(100) NOT NULL COMMENT '数据源名',
    `host` varchar(100) NOT NULL COMMENT '数据库地址',
    `port` int(11) NOT NULL COMMENT '数据库端口',
    `username` varchar(100) NOT NULL COMMENT '数据库用户名',
    `password` varchar(100) NOT NULL COMMENT '数据库密码',
    `server_id` bigint(20) NOT NULL COMMENT '服务ID',
    `status` varchar(100) NOT NULL COMMENT '状态',
    `time_zone` varchar(100) DEFAULT NULL COMMENT '时区',
    `last_stop_time` datetime DEFAULT NULL COMMENT '上次停止时间',
    `create_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (`id`) USING BTREE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 ROW_FORMAT=DYNAMIC COMMENT='数据源';

CREATE TABLE IF NOT EXISTS `sys_database_config` (
    `id` bigint(20) NOT NULL COMMENT '主键',
    `datasource_id` bigint(20) NOT NULL COMMENT '数据源ID',
    `database_name` varchar(100) NOT NULL COMMENT '数据库名',
    `table_name` varchar(100) NOT NULL COMMENT '表名',
    `table_comment` varchar(100) DEFAULT NULL COMMENT '表说明',
    `primary_key` varchar(100) DEFAULT NULL COMMENT '主键名',
    `increment_key` varchar(100) DEFAULT NULL COMMENT '增量字段',
    `increment_type` varchar(100) DEFAULT NULL COMMENT '增量类型',
    `create_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (`id`) USING BTREE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 ROW_FORMAT=DYNAMIC COMMENT='数据库表配置';

CREATE TABLE IF NOT EXISTS `sys_app_database_config` (
    `id` bigint(20) NOT NULL COMMENT '主键',
    `app_id` bigint(20) NOT NULL COMMENT '应用ID',
    `datasource_id` bigint(20) NOT NULL COMMENT '数据源ID',
    `database_config_id` bigint(20) NOT NULL COMMENT '数据库配置ID',
    `create_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (`id`) USING BTREE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 ROW_FORMAT=DYNAMIC COMMENT='应用数据库关联配置';

CREATE TABLE IF NOT EXISTS `sys_data_log` (
    `id` bigint(20) NOT NULL COMMENT '主键',
    `datasource_id` bigint(20) NOT NULL COMMENT '数据源ID',
    `database_config_id` bigint(20) NOT NULL COMMENT '数据库配置ID',
    `database_name` varchar(100) NOT NULL COMMENT '数据库名',
    `table_name` varchar(100) NOT NULL COMMENT '表名',
    `primary_key` varchar(100) NOT NULL COMMENT '主键名',
    `oper_type` varchar(100) NOT NULL COMMENT '操作类型',
    `data_log` varchar(8000) NOT NULL COMMENT '数据日志',
    `create_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (`id`) USING BTREE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 ROW_FORMAT=DYNAMIC COMMENT='数据日志';


CREATE TABLE IF NOT EXISTS `sys_push_log` (
    `id` bigint(20) NOT NULL COMMENT '主键',
    `data_log_id` bigint(20) NOT NULL COMMENT '数据日志ID',
    `app_id` bigint(20) NOT NULL COMMENT '应用ID',
    `status` varchar(100) NOT NULL COMMENT '推送状态',
    `response` varchar(4000) NOT NULL COMMENT '推送结果',
    `push_time` datetime NOT NULL COMMENT '推送时间',
    `cost_time` bigint(20) NOT NULL COMMENT '推送耗时',
    `push_times` int(11) NOT NULL COMMENT '推送次数',
    `create_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (`id`) USING BTREE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 ROW_FORMAT=DYNAMIC COMMENT='数据推送日志';

CREATE TABLE IF NOT EXISTS `sys_early_warning_email` (
    `id` bigint(20) NOT NULL COMMENT '主键',
    `host` varchar(100) NOT NULL COMMENT '邮箱服务地址',
    `protocol` varchar(100) NOT NULL COMMENT '邮箱服务',
    `port` int(11) NOT NULL COMMENT '邮箱服务端口',
    `username` varchar(100) NOT NULL COMMENT '邮箱账号',
    `password` varchar(100) NOT NULL COMMENT '邮箱密码',
    `receivers` varchar(400) NOT NULL COMMENT '邮件接收人',
    `create_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (`id`) USING BTREE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 ROW_FORMAT=DYNAMIC COMMENT='预警邮箱配置';
