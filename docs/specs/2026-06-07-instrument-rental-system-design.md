# 虚拟仓库乐器租赁管理系统 — 设计规格说明

> 日期: 2026-06-07
> 版本: 1.0
> 状态: 已确认

## 1. 项目概述

### 1.1 业务背景

面向社会的乐器租赁管理系统，不同乐队可在线浏览、预约、支付租赁乐器，到仓扫码取还。支持多仓库管理，系统定时刷新汇总库存状态。

### 1.2 核心需求

- 用户自助全流程：浏览乐器 → 选日期 → 在线支付 → 到仓扫码取走 → 到期扫码归还
- 扫码 + 定时刷新混合模式确认设备状态
- 多仓库库存统一管理
- 管理后台：库存看板、预约日历、营收统计、维护管理、用户管理
- 微信小程序 + Web 双通道
- 微信支付 + 支付宝
- 逾期由系统提醒、人工处理

### 1.3 规模

- 乐器数量: 500+
- 月活用户: 200+
- 仓库数量: 多个

## 2. 技术方案

### 2.1 技术栈

| 层级 | 选型 | 理由 |
|------|------|------|
| 后端框架 | Java Spring Boot 3.x | 稳定性优先，强类型，企业级生态成熟 |
| API 网关 | Spring Cloud Gateway | 路由、限流、鉴权统一入口 |
| 数据库 | PostgreSQL 15+ | 主数据存储，JSONB 支持定价快照 |
| 缓存/锁 | Redis 7+ | 分布式锁、会话、配置热缓存 |
| 消息队列 | RabbitMQ | 异步通知、延迟队列、支付回调缓冲 |
| 定时任务 | Quartz (Spring Scheduler) | 集群模式，分布式单节点执行 |
| 前端 Web | Vue 3 + Element Plus | 管理后台 |
| 小程序 | 原生 / Taro | 用户端 |
| 支付 | 微信支付 APIv3 + 支付宝 SDK | 双通道支付 |

### 2.2 架构分层

```
微信小程序 / Web SPA (Vue 3)
        ↓ HTTPS / REST API
API Gateway (Spring Cloud Gateway)
        ↓
┌──────────┬──────────┬──────────┬──────────┐
│ 库存服务  │ 预约服务  │ 支付服务  │ 通知服务  │
│ 乐器CRUD  │ 预约锁    │ 微信支付  │ 模板消息  │
│ 状态机    │ 冲突检测  │ 支付宝    │ 短信      │
│ 位置追踪  │ 自动释放  │ 退款      │ 逾期提醒  │
└──────────┴──────────┴──────────┴──────────┘
        ↓           ↓           ↓
   PostgreSQL      Redis      RabbitMQ
```

四个业务模块在单体应用内以服务层分界，接口清晰，后期可独立拆分为微服务。

## 3. 核心状态模型

```
在库(IN_STOCK) ──预约──▶ 已预约(RESERVED) ──扫码取出──▶ 租赁中(RENTED)
    ▲                       │                              │
    │                       │  取消/超时未取                │ 到期扫码归还
    │                       ▼                              │
    │                  已失效(EXPIRED)                     │
    │                                                      │
    ├──────────────────────────────────────────────────────┘
    │
    ├──标记──▶ 维护中(MAINTENANCE) ──修复──▶ 在库
    ├──标记──▶ 损坏待检(DAMAGED_CHECK) ──人工判定──▶ 在库 / 维护中
    └──标记──▶ 报废(SCRAPPED)
```

**状态流转规则：**
- 预约时锁定具体编号乐器，防止多人同时预约同一件
- 预约后 N 小时（可配置）未扫码取货 → 自动释放回库存
- 逾期未归还只触发提醒，不自动转状态，由管理员人工处理
- 归还发现损坏 → 标记 DAMAGED_CHECK，管理员判定后续状态
- 所有状态变更必须通过指定接口，不允许直接跳转

**可用库存定义：** 仅 IN_STOCK 状态计入"可预约数"

## 4. 数据模型

### 4.1 核心表（8 张）

#### category — 乐器分类
| 字段 | 类型 | 说明 |
|------|------|------|
| id | bigint (PK) | |
| name | varchar(100) | 分类名称 |
| parent_id | bigint (FK→self, nullable) | 父分类，支持树形 |
| sort_order | int | 排序 |

#### instrument_model — 乐器型号
| 字段 | 类型 | 说明 |
|------|------|------|
| id | bigint (PK) | |
| name | varchar(200) | 型号名称 |
| brand | varchar(100) | 品牌 |
| category_id | bigint (FK→category) | 所属分类 |
| daily_rate | decimal | 基准日租金（元），当无 pricing_tier 时作为兜底价 |
| deposit | decimal | 押金（元），设为具体金额则直接使用；设为 NULL 则按 deposit.default_ratio 系数自动计算 |
| images | jsonb | 图片 URL 数组 |
| specs | jsonb | 规格参数 |
| status | enum | ACTIVE / DISABLED |

#### instrument — 单件乐器
| 字段 | 类型 | 说明 |
|------|------|------|
| id | bigint (PK) | |
| serial_no | varchar(100) UNIQUE | 唯一编号 |
| model_id | bigint (FK→instrument_model) | 所属型号 |
| warehouse_id | bigint (FK→warehouse) | 所在仓库 |
| barcode | varchar(200) UNIQUE | 条码号 |
| status | enum | IN_STOCK / RESERVED / RENTED / MAINTENANCE / DAMAGED_CHECK / SCRAPPED |
| condition_note | text | 状况备注 |
| version | int | 乐观锁版本号 |

#### reservation — 预约/租赁记录
| 字段 | 类型 | 说明 |
|------|------|------|
| id | bigint (PK) | |
| user_id | bigint (FK→user) | 租赁用户 |
| instrument_id | bigint (FK→instrument) | 具体乐器 |
| start_time | timestamp | 租赁开始 |
| end_time | timestamp | 租赁结束 |
| status | enum | UNPAID / RESERVED / RENTED / RETURNED / CANCELLED / EXPIRED |
| pickup_code | varchar(20) | 取货码 |
| pickup_time | timestamp (nullable) | 实际取出时间 |
| return_time | timestamp (nullable) | 实际归还时间 |
| price_detail | jsonb | 价格快照（见 5.3） |

#### payment — 支付记录
| 字段 | 类型 | 说明 |
|------|------|------|
| id | bigint (PK) | |
| reservation_id | bigint (FK→reservation) | 关联预约 |
| amount | decimal | 支付金额 |
| channel | enum | WECHAT / ALIPAY |
| transaction_id | varchar(200) UNIQUE | 第三方交易号 |
| status | enum | PENDING / PAID / REFUNDING / REFUNDED / FAILED |
| refund_amount | decimal | 退款金额 |

#### warehouse — 仓库
| 字段 | 类型 | 说明 |
|------|------|------|
| id | bigint (PK) | |
| name | varchar(200) | 仓库名称 |
| address | text | 地址 |
| contact | varchar(50) | 联系电话 |
| status | enum | ACTIVE / DISABLED |

#### maintenance_log — 维护记录
| 字段 | 类型 | 说明 |
|------|------|------|
| id | bigint (PK) | |
| instrument_id | bigint (FK→instrument) | |
| issue_description | text | 问题描述 |
| status | enum | PENDING / IN_PROGRESS / RESOLVED |
| resolved_at | timestamp (nullable) | 修复时间 |

#### overdue_record — 逾期记录
| 字段 | 类型 | 说明 |
|------|------|------|
| id | bigint (PK) | |
| user_id | bigint (FK→user) | |
| reservation_id | bigint (FK→reservation) | |
| overdue_days | int | 逾期天数 |
| handled_by | bigint (FK→user, nullable) | 处理人 |
| resolution | text | 处理备注 |

### 4.2 辅助表

#### system_config — 全局可配置参数
| 字段 | 类型 | 说明 |
|------|------|------|
| id | bigint (PK) | |
| config_key | varchar(100) UNIQUE | 参数键 |
| config_value | varchar(500) | 参数值 |
| description | varchar(500) | 说明 |
| updated_at | timestamp | 更新时间 |

**预设参数：**

| config_key | 默认值 | 说明 |
|------------|--------|------|
| reservation.timeout_hours | 2 | 预约后N小时未取货自动释放 |
| reservation.max_days | 30 | 单次最长租赁天数 |
| reservation.advance_days | 60 | 最多提前N天预约 |
| reservation.unpaid_timeout_min | 30 | 未支付订单超时（分钟） |
| reminder.before_hours | 24 | 到期前N小时发提醒 |
| overdue.grace_hours | 0 | 逾期宽限小时 |
| overdue.notify_admin | true | 逾期是否通知管理员 |
| inventory.check_interval_min | 30 | 库存校验间隔 |
| warehouse.sync_interval_min | 5 | 多仓库轮询间隔 |
| deposit.default_ratio | 2.0 | 押金 = 日租金 × 天数 × 系数 |

参数通过管理后台修改，写入 DB 同时更新 Redis，服务读取 Redis 优先。

#### pricing_tier — 阶梯定价
| 字段 | 类型 | 说明 |
|------|------|------|
| id | bigint (PK) | |
| model_id | bigint (FK→instrument_model, nullable) | 绑型号，NULL=全局默认 |
| day_from | int | 起始天数（含） |
| day_to | int (nullable) | 结束天数（含），NULL=无上限 |
| daily_rate | decimal | 该阶梯日租金 |

#### pricing_season — 时段系数
| 字段 | 类型 | 说明 |
|------|------|------|
| id | bigint (PK) | |
| season_type | enum | WEEKDAY / WEEKEND / HOLIDAY |
| date_start | date (nullable) | 仅 HOLIDAY 必填 |
| date_end | date (nullable) | 仅 HOLIDAY 必填 |
| coefficient | decimal | 价格系数（1.0 = 原价） |
| priority | int | 优先级：HOLIDAY > WEEKEND > WEEKDAY |

#### operation_log — 操作日志
| 字段 | 类型 | 说明 |
|------|------|------|
| id | bigint (PK) | |
| operator_id | bigint (FK→user) | 操作人 |
| action | varchar(100) | 操作类型 |
| target_type | varchar(50) | 操作对象类型 |
| target_id | bigint | 操作对象 ID |
| detail | jsonb | 操作详情 |
| created_at | timestamp | |

## 5. 租金定价

### 5.1 定价公式

```
总租金 = Σ 日基础价(第N天) × 时段系数(该日)
```

### 5.2 计算流程

1. 用户选择型号 + 日期范围 → 请求报价
2. 系统逐日计算：
   - 根据 `pricing_tier` 匹配该天的阶梯日租金（优先级：型号专属 tier > 全局默认 tier > 型号兜底 daily_rate）
   - 根据 `pricing_season` 匹配该天的时段系数
   - 当天金额 = 阶梯日租金 × 时段系数
3. 汇总为总价返回给用户

### 5.3 价格快照

用户确认预约时，将完整价格明细序列化为 JSON 存入 `reservation.price_detail`：

```json
{
  "base_total": 520.0,
  "coefficient_total": 60.5,
  "final_amount": 580.5,
  "daily_breakdown": [
    {"date": "2026-06-08", "tier_rate": 100, "coeff": 1.0, "subtotal": 100},
    {"date": "2026-06-09", "tier_rate": 100, "coeff": 1.0, "subtotal": 100}
  ]
}
```

即使后续定价规则变更，已有订单不受影响。

### 5.4 押金计算

- `instrument_model.deposit` 不为 NULL → 直接使用该金额作为押金
- `instrument_model.deposit` 为 NULL → 押金 = 日租金 × 租赁天数 × `deposit.default_ratio`
- 押金在支付时与租金一同收取，归还确认后原路退回

## 6. 核心业务流程

### 6.1 用户租赁全流程

```
1. 浏览乐器列表（按型号/类别筛选，显示总库存/可预约数）
2. 选择型号 + 日期范围 → 查看报价预览
3. 确认预约 → 创建订单（锁定具体编号乐器）→ 在线支付
4. 支付成功 → 生成取货码 → 用户到仓出示
5. 管理员扫码 → 验证预约 → 确认出库 → 状态: RENTED
6. 租赁中 → 到期前24h自动推送归还提醒
7. 用户到仓归还 → 管理员扫码 → 验证租赁记录 → 确认入库
   - 正常: 状态 → IN_STOCK
   - 损坏: 状态 → DAMAGED_CHECK → 通知管理员
   - 逾期: 记录逾期 → 通知管理员人工处理
```

### 6.2 并发预约控制

```
1. 用户提交预约（model_id + 日期范围 + 数量N）
2. Redis SETNX lock:reserve:{model_id}:{start}_{end} 30s
3. 获锁成功 → 查询可用 instrument → 按乐观锁选中N件 → UPDATE status=RESERVED
4. 创建 reservation + payment 记录
5. 释放 Redis 锁
6. 锁超时 → 自动释放，防止死锁
```

同一型号同一日期段同时只处理一个预约请求，DB 乐观锁（version 字段）作为 Redis 宕机时的降级方案。

### 6.3 定时任务

| 任务 | 周期 | 说明 |
|------|------|------|
| 库存一致性校验 | 每 30 分钟 | SUM(各状态) = 总库存，异常告警 |
| 预约超时释放 | 每 15 分钟 | 预约后超时未取 → 释放库存 |
| 未支付超时取消 | 每 10 分钟 | UNPAID 超时订单 → 取消+释放库存 |
| 到期提醒扫描 | 每天 9:00 / 18:00 | 未来 24h 到期 → 推送消息 |
| 逾期检测 | 每天 1:00 | 扫描逾期记录 → 通知管理员 |
| 多仓库汇总 | 每 5 分钟 | 各仓库状态 → 全局缓存 |
| 支付对账 | 每 30 分钟 | 主动查询支付平台订单状态，补单 |

Quartz 集群模式 + Redis 分布式锁确保定时任务单节点执行。

## 7. 管理后台

### 7.1 实时库存看板

- 顶部汇总卡片：在库 / 已预约 / 租赁中 / 逾期 / 维护中（实时数字）
- 乐器列表：按仓库筛选、按状态/型号搜索、点击展开详情（单件状态、历史）
- 仓库分布：各仓库库存占比饼图

### 7.2 预约日历

- 甘特图风格时间轴，按型号展示每周/每月预约占用情况
- 支持按型号筛选、左右翻页
- 点击占用条查看预约详情

### 7.3 营收统计

- 概览卡片：本月收入、环比变化、订单数
- 收入趋势折线图（按日/周/月切换）
- 收入排行（按型号 / 按仓库 / 按用户）

### 7.4 维护管理

- 报修登记：选择乐器 → 填写问题描述 → 自动标记 MAINTENANCE
- 维护列表：按状态筛选，查看处理进度
- 修复完成 → 改回 IN_STOCK

### 7.5 用户管理

- 用户列表：搜索、筛选
- 拉黑/解封操作
- 逾期记录查看（按用户汇总）
- 用户租赁历史

### 7.6 全局参数配置

- 表单式编辑所有 system_config 参数
- 修改即时生效（写 DB + Redis）
- 变更历史可追溯

## 8. REST API 设计

### 8.1 用户端 API

| 模块 | 方法 | 路径 | 说明 |
|------|------|------|------|
| 认证 | POST | /api/auth/wechat-login | 微信小程序 code 换 token |
| 认证 | POST | /api/auth/login | Web 端手机号/密码登录 |
| 认证 | POST | /api/auth/refresh | 刷新 Token |
| 浏览 | GET | /api/categories | 分类树 |
| 浏览 | GET | /api/models?category=&keyword=&page= | 型号列表（含可用库存数） |
| 浏览 | GET | /api/models/{id}/availability?start=&end= | 某型号日期段可用数量 |
| 预约 | POST | /api/reservations/quote | 报价预览 |
| 预约 | POST | /api/reservations | 创建预约（锁库存 + 生成支付单） |
| 预约 | GET | /api/reservations/my | 我的预约 |
| 预约 | POST | /api/reservations/{id}/cancel | 取消预约（释放库存 + 退款） |
| 支付 | POST | /api/payments/create | 发起支付 |
| 支付 | POST | /api/payments/callback/{channel} | 支付回调 |

### 8.2 管理端 API

| 模块 | 方法 | 路径 | 说明 |
|------|------|------|------|
| 库存 | CRUD | /api/admin/instruments | 单件乐器管理 |
| 库存 | CRUD | /api/admin/models | 型号管理 |
| 库存 | GET | /api/admin/dashboard | 看板汇总 |
| 预约 | GET | /api/admin/reservations | 预约列表（多条件筛选） |
| 预约 | GET | /api/admin/reservations/calendar | 日历视图数据 |
| 扫码 | POST | /api/admin/scan/checkout | 扫码出库 |
| 扫码 | POST | /api/admin/scan/checkin | 扫码入库 |
| 营收 | GET | /api/admin/revenue/summary?period= | 营收汇总 |
| 营收 | GET | /api/admin/revenue/ranking?type= | 收入排行 |
| 用户 | GET | /api/admin/users | 用户列表 |
| 用户 | PUT | /api/admin/users/{id}/status | 拉黑/解封 |
| 用户 | GET | /api/admin/users/{id}/history | 用户租赁/逾期历史 |
| 维护 | POST | /api/admin/maintenance | 登记维护 |
| 维护 | PUT | /api/admin/maintenance/{id}/resolve | 维护完成 |
| 配置 | GET | /api/admin/config | 获取全部配置 |
| 配置 | PUT | /api/admin/config/{key} | 修改单个配置项 |
| 定价 | CRUD | /api/admin/pricing-tiers | 阶梯定价管理 |
| 定价 | CRUD | /api/admin/pricing-seasons | 时段系数管理 |

### 8.3 鉴权

- JWT Token，用户端和管理端用不同角色（ROLE_USER / ROLE_ADMIN）
- Spring Security + 方法级注解 `@PreAuthorize`
- Token 过期后通过 refresh token 续期

## 9. 异常与边界处理

### 9.1 支付异常

| 场景 | 处理 |
|------|------|
| 创建预约但不支付 | UNPAID 状态 + 定时任务扫描超时 → 自动释放库存 |
| 已付款但回调未到达 | 定时任务主动查询支付平台 → 对账补单 |
| 支付成功但库存锁已过期 | 回调中校验 → 自动退款 + 通知用户 |
| 重复回调 | transaction_id 唯一索引 + 业务层幂等判断 |
| 退款 | 调用支付平台退款 API + 本地事务释放库存，任一步失败则回滚 |

### 9.2 库存异常

| 场景 | 处理 |
|------|------|
| 并发预约同一件乐器 | Redis 锁 + DB 乐观锁双重保护 |
| 预约超时未取货 | 定时任务释放库存 + 通知用户 |
| 扫码出错 | 支持手动输入条码号 fallback |
| 归还时发现损坏 | 标记 DAMAGED_CHECK → 管理员判定 |
| 库存校验不一致 | 生成差异报告 → 通知管理员 |

### 9.3 系统级容错

| 场景 | 处理 |
|------|------|
| Redis 宕机 | 降级为 DB 乐观锁，核心功能可用（性能下降） |
| RabbitMQ 宕机 | 消息本地持久化 + 重试，关键通知走 DB 轮询兜底 |
| DB 主从延迟 | 写后关键读取走主库 |
| 定时任务重复执行 | Quartz 集群 + Redis 分布式锁 |

### 9.4 操作日志

所有状态变更、支付操作、扫码动作、配置修改全量记录 `operation_log`，不可删除，用于审计和问题追溯。

## 10. 非功能需求

- **可用性：** 核心租赁流程 99.5% 可用
- **一致性：** 库存状态最终一致，定时校验兜底
- **安全性：** HTTPS、JWT 鉴权、支付签名验证、SQL 注入防护
- **可观测性：** Spring Boot Actuator + 日志结构化 + 关键指标监控
- **可扩展性：** 模块化服务层，后期可独立拆分为微服务

## 11. 待定事项

- 实名认证方式（微信授权手机号 or 独立实名流程）
- 押金管理方式（支付时冻结 vs 预充值押金池）
- 通知渠道（微信模板消息 + 短信 or 仅模板消息）
- 具体 UI 视觉设计

以上待定项可在实现计划阶段细化。

---

> 文档版本: 1.0 | 最后更新: 2026-06-07