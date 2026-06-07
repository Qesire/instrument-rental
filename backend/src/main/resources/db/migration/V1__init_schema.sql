-- =============================================================================
-- V1: Initialize Core Schema
-- Tables for Instrument Rental Management System
-- =============================================================================

-- 1. users — 用户表
CREATE TABLE users (
    id           BIGSERIAL    PRIMARY KEY,
    openid       VARCHAR(128) UNIQUE,
    phone        VARCHAR(20)  UNIQUE,
    password_hash VARCHAR(255),
    nickname     VARCHAR(100),
    avatar_url   VARCHAR(500),
    role         VARCHAR(20)  NOT NULL DEFAULT 'ROLE_USER',
    status       VARCHAR(20)  NOT NULL DEFAULT 'ACTIVE',
    created_at   TIMESTAMP    NOT NULL DEFAULT NOW(),
    updated_at   TIMESTAMP    NOT NULL DEFAULT NOW()
);

-- 2. warehouses — 仓库表
CREATE TABLE warehouses (
    id         BIGSERIAL    PRIMARY KEY,
    name       VARCHAR(200) NOT NULL,
    address    TEXT,
    contact    VARCHAR(50),
    status     VARCHAR(20)  NOT NULL DEFAULT 'ACTIVE',
    created_at TIMESTAMP    NOT NULL DEFAULT NOW()
);

-- 3. categories — 分类表（自引用树形结构）
CREATE TABLE categories (
    id         BIGSERIAL    PRIMARY KEY,
    name       VARCHAR(100) NOT NULL,
    parent_id  BIGINT       REFERENCES categories(id),
    sort_order INT          NOT NULL DEFAULT 0
);

-- 4. instrument_models — 仪器型号/模板
CREATE TABLE instrument_models (
    id         BIGSERIAL       PRIMARY KEY,
    name       VARCHAR(200)    NOT NULL,
    brand      VARCHAR(100),
    category_id BIGINT         NOT NULL REFERENCES categories(id),
    daily_rate DECIMAL(10,2),
    deposit    DECIMAL(10,2),
    images     JSONB           DEFAULT '[]',
    specs      JSONB           DEFAULT '{}',
    status     VARCHAR(20)     NOT NULL DEFAULT 'ACTIVE',
    created_at TIMESTAMP       NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMP       NOT NULL DEFAULT NOW()
);

-- 5. instruments — 仪器实物（带序列号、条码）
CREATE TABLE instruments (
    id             BIGSERIAL       PRIMARY KEY,
    serial_no      VARCHAR(100)    UNIQUE NOT NULL,
    model_id       BIGINT          NOT NULL REFERENCES instrument_models(id),
    warehouse_id   BIGINT          NOT NULL REFERENCES warehouses(id),
    barcode        VARCHAR(200)    UNIQUE NOT NULL,
    status         VARCHAR(30)     NOT NULL DEFAULT 'IN_STOCK',
    condition_note TEXT,
    version        INT             NOT NULL DEFAULT 0,
    created_at     TIMESTAMP       NOT NULL DEFAULT NOW(),
    updated_at     TIMESTAMP       NOT NULL DEFAULT NOW()
);

-- 6. reservations — 预约表
CREATE TABLE reservations (
    id           BIGSERIAL    PRIMARY KEY,
    user_id      BIGINT       NOT NULL REFERENCES users(id),
    instrument_id BIGINT      NOT NULL REFERENCES instruments(id),
    start_time   TIMESTAMP    NOT NULL,
    end_time     TIMESTAMP    NOT NULL,
    status       VARCHAR(30)  NOT NULL DEFAULT 'UNPAID',
    pickup_code  VARCHAR(20),
    pickup_time  TIMESTAMP,
    return_time  TIMESTAMP,
    price_detail JSONB,
    created_at   TIMESTAMP    NOT NULL DEFAULT NOW(),
    updated_at   TIMESTAMP    NOT NULL DEFAULT NOW()
);

-- 7. payments — 支付表
CREATE TABLE payments (
    id             BIGSERIAL       PRIMARY KEY,
    reservation_id BIGINT          NOT NULL REFERENCES reservations(id),
    amount         DECIMAL(10,2)   NOT NULL,
    channel        VARCHAR(20)     NOT NULL,
    transaction_id VARCHAR(200)    UNIQUE,
    status         VARCHAR(20)     NOT NULL DEFAULT 'PENDING',
    refund_amount  DECIMAL(10,2)   DEFAULT 0,
    created_at     TIMESTAMP       NOT NULL DEFAULT NOW(),
    updated_at     TIMESTAMP       NOT NULL DEFAULT NOW()
);

-- 8. maintenance_logs — 维护记录
CREATE TABLE maintenance_logs (
    id                BIGSERIAL    PRIMARY KEY,
    instrument_id     BIGINT       NOT NULL REFERENCES instruments(id),
    issue_description TEXT         NOT NULL,
    status            VARCHAR(20)  NOT NULL DEFAULT 'PENDING',
    resolved_at       TIMESTAMP,
    created_at        TIMESTAMP    NOT NULL DEFAULT NOW()
);

-- 9. overdue_records — 逾期记录
CREATE TABLE overdue_records (
    id             BIGSERIAL  PRIMARY KEY,
    user_id        BIGINT     NOT NULL REFERENCES users(id),
    reservation_id BIGINT     NOT NULL REFERENCES reservations(id),
    overdue_days   INT        NOT NULL,
    handled_by     BIGINT     REFERENCES users(id),
    resolution     TEXT,
    created_at     TIMESTAMP  NOT NULL DEFAULT NOW()
);

-- 10. system_configs — 系统配置
CREATE TABLE system_configs (
    id           BIGSERIAL       PRIMARY KEY,
    config_key   VARCHAR(100)    UNIQUE NOT NULL,
    config_value VARCHAR(500)    NOT NULL,
    description  VARCHAR(500),
    updated_at   TIMESTAMP       NOT NULL DEFAULT NOW()
);

-- 11. pricing_tiers — 阶梯定价
CREATE TABLE pricing_tiers (
    id         BIGSERIAL       PRIMARY KEY,
    model_id   BIGINT          REFERENCES instrument_models(id),
    day_from   INT             NOT NULL,
    day_to     INT,
    daily_rate DECIMAL(10,2)   NOT NULL
);

-- 12. pricing_seasons — 季节/时段系数
CREATE TABLE pricing_seasons (
    id          BIGSERIAL       PRIMARY KEY,
    season_type VARCHAR(20)     NOT NULL,
    date_start  DATE,
    date_end    DATE,
    coefficient DECIMAL(5,2)    NOT NULL DEFAULT 1.0,
    priority    INT             NOT NULL DEFAULT 0
);

-- 13. operation_logs — 操作日志
CREATE TABLE operation_logs (
    id          BIGSERIAL    PRIMARY KEY,
    operator_id BIGINT       REFERENCES users(id),
    action      VARCHAR(100) NOT NULL,
    target_type VARCHAR(50),
    target_id   BIGINT,
    detail      JSONB,
    created_at  TIMESTAMP    NOT NULL DEFAULT NOW()
);

-- =============================================================================
-- Indexes
-- =============================================================================

-- instruments
CREATE INDEX idx_instruments_status     ON instruments(status);
CREATE INDEX idx_instruments_model_id   ON instruments(model_id);
CREATE INDEX idx_instruments_warehouse  ON instruments(warehouse_id);

-- reservations
CREATE INDEX idx_reservations_user_id   ON reservations(user_id);
CREATE INDEX idx_reservations_status    ON reservations(status);
CREATE INDEX idx_reservations_time      ON reservations(start_time, end_time);

-- payments
CREATE INDEX idx_payments_reservation   ON payments(reservation_id);
CREATE INDEX idx_payments_transaction   ON payments(transaction_id);

-- operation_logs
CREATE INDEX idx_operation_logs_time    ON operation_logs(created_at);