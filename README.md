# 🎸 虚拟仓库乐器租赁管理系统

面向社会的乐器租赁管理平台，支持多仓库、微信/支付宝支付、扫码取还、阶梯定价、定时刷新。

## 特性

- **全自助租赁**：浏览乐器 → 选日期 → 在线支付 → 扫码取走 → 到期归还
- **多仓库管理**：轮询汇总全局库存状态，实时看板
- **混合追踪**：扫码确认关键节点 + 系统定时刷新计算库存一致性
- **灵活定价**：阶梯定价（长租优惠）+ 时段系数（工作日/周末/节假日）
- **并发保护**：Redis 分布式锁 + DB 乐观锁，防超卖
- **双端支持**：Vue 3 管理后台 + 微信小程序用户端
- **双支付**：微信支付 + 支付宝

## 技术栈

| 层级 | 技术 |
|------|------|
| 后端框架 | Java 17, Spring Boot 3.2, Spring Security + JWT |
| 数据库 | PostgreSQL 15 (Flyway 版本化迁移) |
| 缓存/锁 | Redis 7 (分布式锁 + 配置热缓存) |
| 消息队列 | RabbitMQ (异步通知) |
| 定时任务 | Quartz (库存校验、预约释放、到期提醒、逾期检测) |
| 管理前端 | Vue 3 + TypeScript + Element Plus + ECharts |
| 用户端 | 微信原生小程序 |
| 测试 | JUnit 5 + Mockito + Testcontainers |

## 架构

```
微信小程序 / Vue 3 Admin
        ↓ REST API
API Gateway (Spring Cloud Gateway)
        ↓
┌──────────┬──────────┬──────────┬──────────┐
│ 库存服务  │ 预约服务  │ 支付服务  │ 通知服务  │
└──────────┴──────────┴──────────┴──────────┘
        ↓           ↓           ↓
   PostgreSQL      Redis      RabbitMQ
```

## 快速开始

### 环境要求

- JDK 17+
- Maven 3.9+
- Docker & Docker Compose
- Node.js 18+

### 启动

```bash
# 1. 启动基础设施
docker compose up -d

# 2. 启动后端 (端口 8080)
cd backend
mvn spring-boot:run -DskipTests

# 3. 启动管理前端 (端口 3000)
cd admin-web
npm install && npm run dev
```

### 默认配置

| 服务 | 端口 | 说明 |
|------|------|------|
| 后端 API | 8080 | Spring Boot REST API |
| 管理后台 | 5173 | Vue 3 Admin |
| PostgreSQL | 5432 | instrument_rental 库 |
| Redis | 6379 | 缓存/分布式锁 |
| RabbitMQ | 5672 (+15672 管理界面) | 消息队列 |

### 开发配置

所有可调参数通过管理后台"系统配置"页面修改，即时生效：

| 参数 | 默认值 | 说明 |
|------|--------|------|
| reservation.timeout_hours | 2 | 预约后N小时未取货自动释放 |
| reservation.max_days | 30 | 单次最长租赁天数 |
| reminder.before_hours | 24 | 到期前N小时发提醒 |
| deposit.default_ratio | 2.0 | 押金系数 |

## 项目结构

```
instrument-rental/
├── backend/                          # Spring Boot 后端
│   ├── src/main/java/com/instrumentrental/
│   │   ├── config/                   # Security, Redis, RabbitMQ 配置
│   │   ├── controller/api/          # 用户端 API
│   │   ├── controller/admin/        # 管理端 API
│   │   ├── domain/
│   │   │   ├── enums/               # 7 个状态枚举
│   │   │   ├── model/               # 13 个 JPA 实体
│   │   │   └── repository/          # 13 个 Repository
│   │   ├── dto/                     # 数据传输对象
│   │   ├── exception/               # 全局异常处理 (25 个错误码)
│   │   ├── scheduler/               # 定时任务 (5 个)
│   │   ├── security/                # JWT 鉴权
│   │   └── service/                 # 10 个业务服务
│   └── src/main/resources/
│       ├── application.yml          # 应用配置
│       └── db/migration/            # Flyway 迁移脚本
├── admin-web/                        # Vue 3 管理后台
│   └── src/
│       ├── views/                    # 9 个页面组件
│       ├── router/                   # 路由 + 鉴权守卫
│       ├── stores/                   # Pinia 状态管理
│       └── api/                      # Axios 封装
├── miniprogram/                      # 微信小程序
│   ├── pages/                        # 5 个页面 (首页/列表/详情/预约/我的)
│   └── utils/                        # API 工具
├── docs/
│   ├── specs/                        # 设计规格说明
│   └── superpowers/plans/            # 实现计划
└── docker-compose.yml                # 基础设施编排
```

## 核心设计

### 状态机

```
在库(IN_STOCK) ──预约──▶ 已预约(RESERVED) ──扫码取出──▶ 租赁中(RENTED)
    │                                                    │
    ├──维护中(MAINTENANCE)──修复──▶ 在库                   │到期扫码归还
    ├──损坏待检(DAMAGED_CHECK)──人工──▶ 在库/维护中         │
    └──报废(SCRAPPED)                                    │
```

### 并发预约

```
1. Redis SETNX 锁定型号+日期段 (30s 超时)
2. 查询可用乐器 → 乐观锁选中 N 件 → 更新 RESERVED
3. 创建预约 + 支付记录
4. 释放锁
5. Redis 宕机 → 降级为 DB 乐观锁
```

### 定价模型

```
总租金 = Σ 日基础价(第N天) × 时段系数(该日)

阶梯定价: 1-3天 ¥100/天, 4-7天 ¥85/天, 8-30天 ¥70/天
时段系数: WEEKDAY=1.0, WEEKEND=1.3, HOLIDAY=1.5 (叠加阶梯)
押金: 型号固定金额 或 日租金 × 天数 × 系数
```

## API 概览

### 用户端
| 方法 | 路径 | 说明 |
|------|------|------|
| POST | /api/auth/login | 手机号登录 |
| POST | /api/auth/wechat-login | 微信登录 |
| GET | /api/categories | 分类树 |
| GET | /api/models | 型号列表(含库存) |
| POST | /api/reservations/quote | 报价预览 |
| POST | /api/reservations | 创建预约 |
| POST | /api/payments/create | 发起支付 |

### 管理端
| 方法 | 路径 | 说明 |
|------|------|------|
| GET | /api/admin/dashboard | 看板汇总 |
| GET | /api/admin/reservations/calendar | 预约日历 |
| POST | /api/admin/scan/checkout | 扫码出库 |
| POST | /api/admin/scan/checkin | 扫码入库 |
| GET | /api/admin/revenue/summary | 营收统计 |

## License

MIT