-- =============================================================================
-- V2: Seed Default Configuration Data
-- =============================================================================

-- System configuration defaults
INSERT INTO system_configs (config_key, config_value, description) VALUES
    ('reservation.timeout_hours',      '2',    '未支付订单超时关闭（小时）'),
    ('reservation.max_days',           '30',   '单次租赁最长天数'),
    ('reservation.advance_days',       '60',   '可提前预约的最大天数'),
    ('reservation.unpaid_timeout_min', '30',   '未支付自动取消超时（分钟）'),
    ('reminder.before_hours',          '24',   '到期前提醒时间（小时）'),
    ('overdue.grace_hours',            '0',    '逾期宽限时间（小时）'),
    ('overdue.notify_admin',           'true', '逾期是否通知管理员'),
    ('inventory.check_interval_min',   '30',   '库存盘点间隔（分钟）'),
    ('warehouse.sync_interval_min',    '5',    '仓库同步间隔（分钟）'),
    ('deposit.default_ratio',          '2.0',  '押金默认倍数（相对日租金）');

-- Pricing season defaults (时段系数)
INSERT INTO pricing_seasons (season_type, coefficient, priority) VALUES
    ('WEEKDAY', 1.0, 0),
    ('WEEKEND', 1.3, 1);