# 乐器租赁管理系统 实现计划

> **面向 AI 代理的工作者：** 必需子技能：使用 superpowers:subagent-driven-development（推荐）或 superpowers:executing-plans 逐任务实现此计划。步骤使用复选框（`- [ ]`）语法来跟踪进度。

**目标：** 构建一个面向社会的乐器租赁管理系统，支持多仓库、微信/支付宝支付、扫码取还、管理后台。

**架构：** Spring Boot 3.x 单体应用，内部按库存/预约/支付/通知分服务层。Vue 3 管理后台 + 微信小程序双前端。PostgreSQL + Redis + RabbitMQ。

**技术栈：** Java 17, Spring Boot 3.x, Spring Security + JWT, Spring Data JPA, PostgreSQL 15, Redis 7, RabbitMQ, Flyway, Quartz, Vue 3 + Element Plus, 微信原生小程序

---

### 任务 1：项目脚手架 — Spring Boot 后端

**文件：**
- 创建：`backend/pom.xml`
- 创建：`backend/src/main/java/com/instrumentrental/InstrumentRentalApplication.java`
- 创建：`backend/src/main/resources/application.yml`

- [ ] **步骤 1：创建 Spring Boot 项目配置文件**

```xml
<!-- backend/pom.xml -->
<?xml version="1.0" encoding="UTF-8"?>
<project xmlns="http://maven.apache.org/POM/4.0.0"
         xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
         xsi:schemaLocation="http://maven.apache.org/POM/4.0.0 https://maven.apache.org/xsd/maven-4.0.0.xsd">
    <modelVersion>4.0.0</modelVersion>
    <parent>
        <groupId>org.springframework.boot</groupId>
        <artifactId>spring-boot-starter-parent</artifactId>
        <version>3.2.0</version>
    </parent>
    <groupId>com.instrumentrental</groupId>
    <artifactId>instrument-rental</artifactId>
    <version>1.0.0</version>
    <name>Instrument Rental System</name>

    <properties>
        <java.version>17</java.version>
        <jjwt.version>0.12.3</jjwt.version>
    </properties>

    <dependencies>
        <dependency>
            <groupId>org.springframework.boot</groupId>
            <artifactId>spring-boot-starter-web</artifactId>
        </dependency>
        <dependency>
            <groupId>org.springframework.boot</groupId>
            <artifactId>spring-boot-starter-data-jpa</artifactId>
        </dependency>
        <dependency>
            <groupId>org.springframework.boot</groupId>
            <artifactId>spring-boot-starter-security</artifactId>
        </dependency>
        <dependency>
            <groupId>org.springframework.boot</groupId>
            <artifactId>spring-boot-starter-validation</artifactId>
        </dependency>
        <dependency>
            <groupId>org.springframework.boot</groupId>
            <artifactId>spring-boot-starter-data-redis</artifactId>
        </dependency>
        <dependency>
            <groupId>org.springframework.boot</groupId>
            <artifactId>spring-boot-starter-amqp</artifactId>
        </dependency>
        <dependency>
            <groupId>org.springframework.boot</groupId>
            <artifactId>spring-boot-starter-quartz</artifactId>
        </dependency>
        <dependency>
            <groupId>org.postgresql</groupId>
            <artifactId>postgresql</artifactId>
        </dependency>
        <dependency>
            <groupId>org.flywaydb</groupId>
            <artifactId>flyway-core</artifactId>
        </dependency>
        <dependency>
            <groupId>io.jsonwebtoken</groupId>
            <artifactId>jjwt-api</artifactId>
            <version>${jjwt.version}</version>
        </dependency>
        <dependency>
            <groupId>io.jsonwebtoken</groupId>
            <artifactId>jjwt-impl</artifactId>
            <version>${jjwt.version}</version>
            <scope>runtime</scope>
        </dependency>
        <dependency>
            <groupId>io.jsonwebtoken</groupId>
            <artifactId>jjwt-jackson</artifactId>
            <version>${jjwt.version}</version>
            <scope>runtime</scope>
        </dependency>
        <dependency>
            <groupId>org.projectlombok</groupId>
            <artifactId>lombok</artifactId>
            <optional>true</optional>
        </dependency>
        <dependency>
            <groupId>org.springframework.boot</groupId>
            <artifactId>spring-boot-starter-test</artifactId>
            <scope>test</scope>
        </dependency>
        <dependency>
            <groupId>org.testcontainers</groupId>
            <artifactId>postgresql</artifactId>
            <scope>test</scope>
        </dependency>
        <dependency>
            <groupId>org.testcontainers</groupId>
            <artifactId>junit-jupiter</artifactId>
            <scope>test</scope>
        </dependency>
        <dependency>
            <groupId>com.redis.testcontainers</groupId>
            <artifactId>testcontainers-redis</artifactId>
            <version>1.6.4</version>
            <scope>test</scope>
        </dependency>
    </dependencies>

    <build>
        <plugins>
            <plugin>
                <groupId>org.springframework.boot</groupId>
                <artifactId>spring-boot-maven-plugin</artifactId>
                <configuration>
                    <excludes>
                        <exclude>
                            <groupId>org.projectlombok</groupId>
                            <artifactId>lombok</artifactId>
                        </exclude>
                    </excludes>
                </configuration>
            </plugin>
        </plugins>
    </build>
</project>
```

- [ ] **步骤 2：创建应用主类**

```java
// backend/src/main/java/com/instrumentrental/InstrumentRentalApplication.java
package com.instrumentrental;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class InstrumentRentalApplication {
    public static void main(String[] args) {
        SpringApplication.run(InstrumentRentalApplication.class, args);
    }
}
```

- [ ] **步骤 3：创建应用配置**

```yaml
# backend/src/main/resources/application.yml
server:
  port: 8080

spring:
  application:
    name: instrument-rental
  datasource:
    url: jdbc:postgresql://localhost:5432/instrument_rental
    username: ${DB_USERNAME:postgres}
    password: ${DB_PASSWORD:postgres}
    driver-class-name: org.postgresql.Driver
  jpa:
    hibernate:
      ddl-auto: validate
    show-sql: false
    properties:
      hibernate:
        dialect: org.hibernate.dialect.PostgreSQLDialect
        format_sql: true
  flyway:
    enabled: true
    locations: classpath:db/migration
  data:
    redis:
      host: ${REDIS_HOST:localhost}
      port: ${REDIS_PORT:6379}
  rabbitmq:
    host: ${RABBITMQ_HOST:localhost}
    port: ${RABBITMQ_PORT:5672}
    username: ${RABBITMQ_USER:guest}
    password: ${RABBITMQ_PASS:guest}

jwt:
  secret: ${JWT_SECRET:change-me-in-production-use-at-least-256-bits}
  expiration-ms: 86400000
  refresh-expiration-ms: 604800000

wechat:
  pay:
    mch-id: ${WECHAT_MCH_ID:}
    api-v3-key: ${WECHAT_API_V3_KEY:}
    app-id: ${WECHAT_APP_ID:}
    app-secret: ${WECHAT_APP_SECRET:}
    private-key-path: ${WECHAT_PRIVATE_KEY_PATH:}
    notify-url: ${WECHAT_NOTIFY_URL:}

alipay:
  app-id: ${ALIPAY_APP_ID:}
  private-key: ${ALIPAY_PRIVATE_KEY:}
  alipay-public-key: ${ALIPAY_PUBLIC_KEY:}
  notify-url: ${ALIPAY_NOTIFY_URL:}

logging:
  level:
    com.instrumentrental: DEBUG
    org.springframework.security: INFO
```

- [ ] **步骤 4：运行验证项目可启动**

```bash
cd backend && mvn compile
```

预期：BUILD SUCCESS，无编译错误。

- [ ] **步骤 5：Commit**

```bash
git add backend/pom.xml backend/src/main/java/com/instrumentrental/InstrumentRentalApplication.java backend/src/main/resources/application.yml
git commit -m "chore: initialize Spring Boot project scaffold"
```

---

### 任务 2：数据库 Schema — Flyway 迁移

**文件：**
- 创建：`backend/src/main/resources/db/migration/V1__init_schema.sql`
- 创建：`backend/src/main/resources/db/migration/V2__seed_config.sql`

- [ ] **步骤 1：编写初始 Schema 迁移**

```sql
-- backend/src/main/resources/db/migration/V1__init_schema.sql

-- 用户表
CREATE TABLE users (
    id BIGSERIAL PRIMARY KEY,
    openid VARCHAR(128) UNIQUE,
    phone VARCHAR(20) UNIQUE,
    password_hash VARCHAR(255),
    nickname VARCHAR(100),
    avatar_url VARCHAR(500),
    role VARCHAR(20) NOT NULL DEFAULT 'ROLE_USER',
    status VARCHAR(20) NOT NULL DEFAULT 'ACTIVE',
    created_at TIMESTAMP NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMP NOT NULL DEFAULT NOW()
);

-- 仓库表
CREATE TABLE warehouses (
    id BIGSERIAL PRIMARY KEY,
    name VARCHAR(200) NOT NULL,
    address TEXT,
    contact VARCHAR(50),
    status VARCHAR(20) NOT NULL DEFAULT 'ACTIVE',
    created_at TIMESTAMP NOT NULL DEFAULT NOW()
);

-- 乐器分类表
CREATE TABLE categories (
    id BIGSERIAL PRIMARY KEY,
    name VARCHAR(100) NOT NULL,
    parent_id BIGINT REFERENCES categories(id),
    sort_order INT NOT NULL DEFAULT 0
);

-- 乐器型号表
CREATE TABLE instrument_models (
    id BIGSERIAL PRIMARY KEY,
    name VARCHAR(200) NOT NULL,
    brand VARCHAR(100),
    category_id BIGINT NOT NULL REFERENCES categories(id),
    daily_rate DECIMAL(10,2),
    deposit DECIMAL(10,2),
    images JSONB DEFAULT '[]',
    specs JSONB DEFAULT '{}',
    status VARCHAR(20) NOT NULL DEFAULT 'ACTIVE',
    created_at TIMESTAMP NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMP NOT NULL DEFAULT NOW()
);

-- 单件乐器表
CREATE TABLE instruments (
    id BIGSERIAL PRIMARY KEY,
    serial_no VARCHAR(100) UNIQUE NOT NULL,
    model_id BIGINT NOT NULL REFERENCES instrument_models(id),
    warehouse_id BIGINT NOT NULL REFERENCES warehouses(id),
    barcode VARCHAR(200) UNIQUE NOT NULL,
    status VARCHAR(30) NOT NULL DEFAULT 'IN_STOCK',
    condition_note TEXT,
    version INT NOT NULL DEFAULT 0,
    created_at TIMESTAMP NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMP NOT NULL DEFAULT NOW()
);

-- 预约/租赁记录表
CREATE TABLE reservations (
    id BIGSERIAL PRIMARY KEY,
    user_id BIGINT NOT NULL REFERENCES users(id),
    instrument_id BIGINT NOT NULL REFERENCES instruments(id),
    start_time TIMESTAMP NOT NULL,
    end_time TIMESTAMP NOT NULL,
    status VARCHAR(30) NOT NULL DEFAULT 'UNPAID',
    pickup_code VARCHAR(20),
    pickup_time TIMESTAMP,
    return_time TIMESTAMP,
    price_detail JSONB,
    created_at TIMESTAMP NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMP NOT NULL DEFAULT NOW()
);

-- 支付记录表
CREATE TABLE payments (
    id BIGSERIAL PRIMARY KEY,
    reservation_id BIGINT NOT NULL REFERENCES reservations(id),
    amount DECIMAL(10,2) NOT NULL,
    channel VARCHAR(20) NOT NULL,
    transaction_id VARCHAR(200) UNIQUE,
    status VARCHAR(20) NOT NULL DEFAULT 'PENDING',
    refund_amount DECIMAL(10,2) DEFAULT 0,
    created_at TIMESTAMP NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMP NOT NULL DEFAULT NOW()
);

-- 维护记录表
CREATE TABLE maintenance_logs (
    id BIGSERIAL PRIMARY KEY,
    instrument_id BIGINT NOT NULL REFERENCES instruments(id),
    issue_description TEXT NOT NULL,
    status VARCHAR(20) NOT NULL DEFAULT 'PENDING',
    resolved_at TIMESTAMP,
    created_at TIMESTAMP NOT NULL DEFAULT NOW()
);

-- 逾期记录表
CREATE TABLE overdue_records (
    id BIGSERIAL PRIMARY KEY,
    user_id BIGINT NOT NULL REFERENCES users(id),
    reservation_id BIGINT NOT NULL REFERENCES reservations(id),
    overdue_days INT NOT NULL,
    handled_by BIGINT REFERENCES users(id),
    resolution TEXT,
    created_at TIMESTAMP NOT NULL DEFAULT NOW()
);

-- 系统配置表
CREATE TABLE system_configs (
    id BIGSERIAL PRIMARY KEY,
    config_key VARCHAR(100) UNIQUE NOT NULL,
    config_value VARCHAR(500) NOT NULL,
    description VARCHAR(500),
    updated_at TIMESTAMP NOT NULL DEFAULT NOW()
);

-- 阶梯定价表
CREATE TABLE pricing_tiers (
    id BIGSERIAL PRIMARY KEY,
    model_id BIGINT REFERENCES instrument_models(id),
    day_from INT NOT NULL,
    day_to INT,
    daily_rate DECIMAL(10,2) NOT NULL
);

-- 时段系数表
CREATE TABLE pricing_seasons (
    id BIGSERIAL PRIMARY KEY,
    season_type VARCHAR(20) NOT NULL,
    date_start DATE,
    date_end DATE,
    coefficient DECIMAL(5,2) NOT NULL DEFAULT 1.0,
    priority INT NOT NULL DEFAULT 0
);

-- 操作日志表
CREATE TABLE operation_logs (
    id BIGSERIAL PRIMARY KEY,
    operator_id BIGINT REFERENCES users(id),
    action VARCHAR(100) NOT NULL,
    target_type VARCHAR(50),
    target_id BIGINT,
    detail JSONB,
    created_at TIMESTAMP NOT NULL DEFAULT NOW()
);

-- 索引
CREATE INDEX idx_instruments_status ON instruments(status);
CREATE INDEX idx_instruments_model_id ON instruments(model_id);
CREATE INDEX idx_instruments_warehouse_id ON instruments(warehouse_id);
CREATE INDEX idx_reservations_user_id ON reservations(user_id);
CREATE INDEX idx_reservations_status ON reservations(status);
CREATE INDEX idx_reservations_time_range ON reservations(start_time, end_time);
CREATE INDEX idx_payments_reservation_id ON payments(reservation_id);
CREATE INDEX idx_payments_transaction_id ON payments(transaction_id);
CREATE INDEX idx_operation_logs_created_at ON operation_logs(created_at);
```

- [ ] **步骤 2：编写种子数据迁移**

```sql
-- backend/src/main/resources/db/migration/V2__seed_config.sql

-- 全局配置默认值
INSERT INTO system_configs (config_key, config_value, description) VALUES
('reservation.timeout_hours', '2', '预约后N小时未取货自动释放'),
('reservation.max_days', '30', '单次最长租赁天数'),
('reservation.advance_days', '60', '最多提前N天预约'),
('reservation.unpaid_timeout_min', '30', '未支付订单超时（分钟）'),
('reminder.before_hours', '24', '到期前N小时发提醒'),
('overdue.grace_hours', '0', '逾期宽限小时'),
('overdue.notify_admin', 'true', '逾期是否通知管理员'),
('inventory.check_interval_min', '30', '库存校验间隔（分钟）'),
('warehouse.sync_interval_min', '5', '多仓库轮询间隔（分钟）'),
('deposit.default_ratio', '2.0', '押金 = 日租金 × 天数 × 系数');

-- 默认时段系数
INSERT INTO pricing_seasons (season_type, coefficient, priority) VALUES
('WEEKDAY', 1.0, 0),
('WEEKEND', 1.3, 1);
```

- [ ] **步骤 3：验证 Flyway 迁移**

```bash
# 确保 PostgreSQL 运行中，然后
cd backend && mvn flyway:migrate
```

预期：两个迁移成功执行，数据库中表已创建。

- [ ] **步骤 4：Commit**

```bash
git add backend/src/main/resources/db/migration/
git commit -m "feat: add database schema and seed data migrations"
```

---

### 任务 3：枚举类

**文件：**
- 创建：`backend/src/main/java/com/instrumentrental/domain/enums/InstrumentStatus.java`
- 创建：`backend/src/main/java/com/instrumentrental/domain/enums/ReservationStatus.java`
- 创建：`backend/src/main/java/com/instrumentrental/domain/enums/PaymentStatus.java`
- 创建：`backend/src/main/java/com/instrumentrental/domain/enums/PaymentChannel.java`
- 创建：`backend/src/main/java/com/instrumentrental/domain/enums/SeasonType.java`
- 创建：`backend/src/main/java/com/instrumentrental/domain/enums/UserStatus.java`
- 创建：`backend/src/main/java/com/instrumentrental/domain/enums/UserRole.java`

- [ ] **步骤 1：编写 InstrumentStatus 枚举**

```java
// backend/src/main/java/com/instrumentrental/domain/enums/InstrumentStatus.java
package com.instrumentrental.domain.enums;

public enum InstrumentStatus {
    IN_STOCK,
    RESERVED,
    RENTED,
    MAINTENANCE,
    DAMAGED_CHECK,
    SCRAPPED
}
```

- [ ] **步骤 2：编写 ReservationStatus 枚举**

```java
// backend/src/main/java/com/instrumentrental/domain/enums/ReservationStatus.java
package com.instrumentrental.domain.enums;

public enum ReservationStatus {
    UNPAID,
    RESERVED,
    RENTED,
    RETURNED,
    CANCELLED,
    EXPIRED
}
```

- [ ] **步骤 3：编写 PaymentStatus 枚举**

```java
// backend/src/main/java/com/instrumentrental/domain/enums/PaymentStatus.java
package com.instrumentrental.domain.enums;

public enum PaymentStatus {
    PENDING,
    PAID,
    REFUNDING,
    REFUNDED,
    FAILED
}
```

- [ ] **步骤 4：编写 PaymentChannel 枚举**

```java
// backend/src/main/java/com/instrumentrental/domain/enums/PaymentChannel.java
package com.instrumentrental.domain.enums;

public enum PaymentChannel {
    WECHAT,
    ALIPAY
}
```

- [ ] **步骤 5：编写 SeasonType 枚举**

```java
// backend/src/main/java/com/instrumentrental/domain/enums/SeasonType.java
package com.instrumentrental.domain.enums;

public enum SeasonType {
    WEEKDAY,
    WEEKEND,
    HOLIDAY
}
```

- [ ] **步骤 6：编写 UserStatus 和 UserRole 枚举**

```java
// backend/src/main/java/com/instrumentrental/domain/enums/UserStatus.java
package com.instrumentrental.domain.enums;

public enum UserStatus {
    ACTIVE,
    BLACKLISTED
}

// backend/src/main/java/com/instrumentrental/domain/enums/UserRole.java
package com.instrumentrental.domain.enums;

public enum UserRole {
    ROLE_USER,
    ROLE_ADMIN
}
```

- [ ] **步骤 7：编译验证**

```bash
cd backend && mvn compile
```

预期：BUILD SUCCESS。

- [ ] **步骤 8：Commit**

```bash
git add backend/src/main/java/com/instrumentrental/domain/enums/
git commit -m "feat: add domain enum classes"
```

---

### 任务 4：JPA 实体类

**文件：**
- 创建：`backend/src/main/java/com/instrumentrental/domain/model/User.java`
- 创建：`backend/src/main/java/com/instrumentrental/domain/model/Warehouse.java`
- 创建：`backend/src/main/java/com/instrumentrental/domain/model/Category.java`
- 创建：`backend/src/main/java/com/instrumentrental/domain/model/InstrumentModel.java`
- 创建：`backend/src/main/java/com/instrumentrental/domain/model/Instrument.java`
- 创建：`backend/src/main/java/com/instrumentrental/domain/model/Reservation.java`
- 创建：`backend/src/main/java/com/instrumentrental/domain/model/Payment.java`
- 创建：`backend/src/main/java/com/instrumentrental/domain/model/MaintenanceLog.java`
- 创建：`backend/src/main/java/com/instrumentrental/domain/model/OverdueRecord.java`
- 创建：`backend/src/main/java/com/instrumentrental/domain/model/SystemConfig.java`
- 创建：`backend/src/main/java/com/instrumentrental/domain/model/PricingTier.java`
- 创建：`backend/src/main/java/com/instrumentrental/domain/model/PricingSeason.java`
- 创建：`backend/src/main/java/com/instrumentrental/domain/model/OperationLog.java`

- [ ] **步骤 1：编写 User 实体**

```java
// backend/src/main/java/com/instrumentrental/domain/model/User.java
package com.instrumentrental.domain.model;

import com.instrumentrental.domain.enums.UserRole;
import com.instrumentrental.domain.enums.UserStatus;
import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "users")
@Getter @Setter
@NoArgsConstructor @AllArgsConstructor
@Builder
public class User {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique = true, length = 128)
    private String openid;

    @Column(unique = true, length = 20)
    private String phone;

    @Column(name = "password_hash", length = 255)
    private String passwordHash;

    @Column(length = 100)
    private String nickname;

    @Column(name = "avatar_url", length = 500)
    private String avatarUrl;

    @Enumerated(EnumType.STRING)
    @Column(length = 20, nullable = false)
    private UserRole role;

    @Enumerated(EnumType.STRING)
    @Column(length = 20, nullable = false)
    private UserStatus status;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
        if (role == null) role = UserRole.ROLE_USER;
        if (status == null) status = UserStatus.ACTIVE;
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }
}
```

- [ ] **步骤 2：编写 Warehouse 实体**

```java
// backend/src/main/java/com/instrumentrental/domain/model/Warehouse.java
package com.instrumentrental.domain.model;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "warehouses")
@Getter @Setter
@NoArgsConstructor @AllArgsConstructor @Builder
public class Warehouse {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(length = 200, nullable = false)
    private String name;

    @Column(columnDefinition = "TEXT")
    private String address;

    @Column(length = 50)
    private String contact;

    @Column(length = 20, nullable = false)
    private String status;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        if (status == null) status = "ACTIVE";
    }
}
```

- [ ] **步骤 3：编写 Category 实体**

```java
// backend/src/main/java/com/instrumentrental/domain/model/Category.java
package com.instrumentrental.domain.model;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "categories")
@Getter @Setter
@NoArgsConstructor @AllArgsConstructor @Builder
public class Category {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(length = 100, nullable = false)
    private String name;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "parent_id")
    private Category parent;

    @Column(name = "sort_order", nullable = false)
    private int sortOrder;
}
```

- [ ] **步骤 4：编写 InstrumentModel 实体**

```java
// backend/src/main/java/com/instrumentrental/domain/model/InstrumentModel.java
package com.instrumentrental.domain.model;

import jakarta.persistence.*;
import lombok.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "instrument_models")
@Getter @Setter
@NoArgsConstructor @AllArgsConstructor @Builder
public class InstrumentModel {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(length = 200, nullable = false)
    private String name;

    @Column(length = 100)
    private String brand;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "category_id", nullable = false)
    private Category category;

    @Column(name = "daily_rate", precision = 10, scale = 2)
    private BigDecimal dailyRate;

    @Column(precision = 10, scale = 2)
    private BigDecimal deposit;

    @Column(columnDefinition = "jsonb")
    private String images;

    @Column(columnDefinition = "jsonb")
    private String specs;

    @Column(length = 20, nullable = false)
    private String status;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
        if (status == null) status = "ACTIVE";
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }
}
```

- [ ] **步骤 5：编写 Instrument 实体**

```java
// backend/src/main/java/com/instrumentrental/domain/model/Instrument.java
package com.instrumentrental.domain.model;

import com.instrumentrental.domain.enums.InstrumentStatus;
import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "instruments")
@Getter @Setter
@NoArgsConstructor @AllArgsConstructor @Builder
public class Instrument {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "serial_no", length = 100, unique = true, nullable = false)
    private String serialNo;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "model_id", nullable = false)
    private InstrumentModel model;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "warehouse_id", nullable = false)
    private Warehouse warehouse;

    @Column(length = 200, unique = true, nullable = false)
    private String barcode;

    @Enumerated(EnumType.STRING)
    @Column(length = 30, nullable = false)
    private InstrumentStatus status;

    @Column(name = "condition_note", columnDefinition = "TEXT")
    private String conditionNote;

    @Version
    @Column(nullable = false)
    private int version;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
        if (status == null) status = InstrumentStatus.IN_STOCK;
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }
}
```

- [ ] **步骤 6：编写 Reservation 实体**

```java
// backend/src/main/java/com/instrumentrental/domain/model/Reservation.java
package com.instrumentrental.domain.model;

import com.instrumentrental.domain.enums.ReservationStatus;
import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "reservations")
@Getter @Setter
@NoArgsConstructor @AllArgsConstructor @Builder
public class Reservation {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "instrument_id", nullable = false)
    private Instrument instrument;

    @Column(name = "start_time", nullable = false)
    private LocalDateTime startTime;

    @Column(name = "end_time", nullable = false)
    private LocalDateTime endTime;

    @Enumerated(EnumType.STRING)
    @Column(length = 30, nullable = false)
    private ReservationStatus status;

    @Column(name = "pickup_code", length = 20)
    private String pickupCode;

    @Column(name = "pickup_time")
    private LocalDateTime pickupTime;

    @Column(name = "return_time")
    private LocalDateTime returnTime;

    @Column(name = "price_detail", columnDefinition = "jsonb")
    private String priceDetail;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }
}
```

- [ ] **步骤 7：编写 Payment、PricingTier、PricingSeason、SystemConfig、MaintenanceLog、OverdueRecord、OperationLog 实体**

```java
// backend/src/main/java/com/instrumentrental/domain/model/Payment.java
package com.instrumentrental.domain.model;

import com.instrumentrental.domain.enums.PaymentChannel;
import com.instrumentrental.domain.enums.PaymentStatus;
import jakarta.persistence.*;
import lombok.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "payments")
@Getter @Setter
@NoArgsConstructor @AllArgsConstructor @Builder
public class Payment {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "reservation_id", nullable = false)
    private Reservation reservation;

    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal amount;

    @Enumerated(EnumType.STRING)
    @Column(length = 20, nullable = false)
    private PaymentChannel channel;

    @Column(name = "transaction_id", length = 200, unique = true)
    private String transactionId;

    @Enumerated(EnumType.STRING)
    @Column(length = 20, nullable = false)
    private PaymentStatus status;

    @Column(name = "refund_amount", precision = 10, scale = 2)
    private BigDecimal refundAmount;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    @PrePersist protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
        if (status == null) status = PaymentStatus.PENDING;
        if (refundAmount == null) refundAmount = BigDecimal.ZERO;
    }
    @PreUpdate protected void onUpdate() { updatedAt = LocalDateTime.now(); }
}

// backend/src/main/java/com/instrumentrental/domain/model/PricingTier.java
package com.instrumentrental.domain.model;

import jakarta.persistence.*;
import lombok.*;
import java.math.BigDecimal;

@Entity
@Table(name = "pricing_tiers")
@Getter @Setter
@NoArgsConstructor @AllArgsConstructor @Builder
public class PricingTier {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "model_id")
    private InstrumentModel model;

    @Column(name = "day_from", nullable = false)
    private int dayFrom;

    @Column(name = "day_to")
    private Integer dayTo;

    @Column(name = "daily_rate", nullable = false, precision = 10, scale = 2)
    private BigDecimal dailyRate;
}

// backend/src/main/java/com/instrumentrental/domain/model/PricingSeason.java
package com.instrumentrental.domain.model;

import com.instrumentrental.domain.enums.SeasonType;
import jakarta.persistence.*;
import lombok.*;
import java.math.BigDecimal;
import java.time.LocalDate;

@Entity
@Table(name = "pricing_seasons")
@Getter @Setter
@NoArgsConstructor @AllArgsConstructor @Builder
public class PricingSeason {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Enumerated(EnumType.STRING)
    @Column(name = "season_type", length = 20, nullable = false)
    private SeasonType seasonType;

    @Column(name = "date_start")
    private LocalDate dateStart;

    @Column(name = "date_end")
    private LocalDate dateEnd;

    @Column(nullable = false, precision = 5, scale = 2)
    private BigDecimal coefficient;

    @Column(nullable = false)
    private int priority;
}

// backend/src/main/java/com/instrumentrental/domain/model/SystemConfig.java
package com.instrumentrental.domain.model;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "system_configs")
@Getter @Setter
@NoArgsConstructor @AllArgsConstructor @Builder
public class SystemConfig {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "config_key", length = 100, unique = true, nullable = false)
    private String configKey;

    @Column(name = "config_value", length = 500, nullable = false)
    private String configValue;

    @Column(length = 500)
    private String description;

    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    @PrePersist @PreUpdate
    protected void onUpdate() { updatedAt = LocalDateTime.now(); }
}

// backend/src/main/java/com/instrumentrental/domain/model/MaintenanceLog.java
package com.instrumentrental.domain.model;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "maintenance_logs")
@Getter @Setter
@NoArgsConstructor @AllArgsConstructor @Builder
public class MaintenanceLog {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "instrument_id", nullable = false)
    private Instrument instrument;

    @Column(name = "issue_description", columnDefinition = "TEXT", nullable = false)
    private String issueDescription;

    @Column(length = 20, nullable = false)
    private String status;

    @Column(name = "resolved_at")
    private LocalDateTime resolvedAt;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @PrePersist protected void onCreate() {
        createdAt = LocalDateTime.now();
        if (status == null) status = "PENDING";
    }
}

// backend/src/main/java/com/instrumentrental/domain/model/OverdueRecord.java
package com.instrumentrental.domain.model;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "overdue_records")
@Getter @Setter
@NoArgsConstructor @AllArgsConstructor @Builder
public class OverdueRecord {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "reservation_id", nullable = false)
    private Reservation reservation;

    @Column(name = "overdue_days", nullable = false)
    private int overdueDays;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "handled_by")
    private User handledBy;

    @Column(columnDefinition = "TEXT")
    private String resolution;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @PrePersist protected void onCreate() { createdAt = LocalDateTime.now(); }
}

// backend/src/main/java/com/instrumentrental/domain/model/OperationLog.java
package com.instrumentrental.domain.model;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "operation_logs")
@Getter @Setter
@NoArgsConstructor @AllArgsConstructor @Builder
public class OperationLog {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "operator_id")
    private User operator;

    @Column(length = 100, nullable = false)
    private String action;

    @Column(name = "target_type", length = 50)
    private String targetType;

    @Column(name = "target_id")
    private Long targetId;

    @Column(columnDefinition = "jsonb")
    private String detail;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @PrePersist protected void onCreate() { createdAt = LocalDateTime.now(); }
}
```

- [ ] **步骤 8：编译验证**

```bash
cd backend && mvn compile
```

- [ ] **步骤 9：Commit**

```bash
git add backend/src/main/java/com/instrumentrental/domain/model/
git commit -m "feat: add all JPA entity classes"
```

任务 4/12 编写完成...

---

### 任务 5：Repository 层 — Spring Data JPA

**文件：**
- 创建：`backend/src/main/java/com/instrumentrental/domain/repository/UserRepository.java`
- 创建：`backend/src/main/java/com/instrumentrental/domain/repository/InstrumentRepository.java`
- 创建：`backend/src/main/java/com/instrumentrental/domain/repository/InstrumentModelRepository.java`
- 创建：`backend/src/main/java/com/instrumentrental/domain/repository/ReservationRepository.java`
- 创建：`backend/src/main/java/com/instrumentrental/domain/repository/PaymentRepository.java`
- 创建：`backend/src/main/java/com/instrumentrental/domain/repository/SystemConfigRepository.java`
- 创建：`backend/src/main/java/com/instrumentrental/domain/repository/PricingTierRepository.java`
- 创建：`backend/src/main/java/com/instrumentrental/domain/repository/PricingSeasonRepository.java`
- 创建：`backend/src/main/java/com/instrumentrental/domain/repository/WarehouseRepository.java`
- 创建：`backend/src/main/java/com/instrumentrental/domain/repository/CategoryRepository.java`
- 创建：`backend/src/main/java/com/instrumentrental/domain/repository/MaintenanceLogRepository.java`
- 创建：`backend/src/main/java/com/instrumentrental/domain/repository/OverdueRecordRepository.java`
- 创建：`backend/src/main/java/com/instrumentrental/domain/repository/OperationLogRepository.java`

- [ ] **步骤 1：创建 UserRepository**

```java
// backend/src/main/java/com/instrumentrental/domain/repository/UserRepository.java
package com.instrumentrental.domain.repository;

import com.instrumentrental.domain.enums.UserStatus;
import com.instrumentrental.domain.model.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {
    Optional<User> findByOpenid(String openid);
    Optional<User> findByPhone(String phone);
    Page<User> findByStatus(UserStatus status, Pageable pageable);
    Page<User> findByPhoneContainingOrNicknameContaining(String phone, String nickname, Pageable pageable);
}
```

- [ ] **步骤 2：创建 InstrumentRepository**

```java
// backend/src/main/java/com/instrumentrental/domain/repository/InstrumentRepository.java
package com.instrumentrental.domain.repository;

import com.instrumentrental.domain.enums.InstrumentStatus;
import com.instrumentrental.domain.model.Instrument;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface InstrumentRepository extends JpaRepository<Instrument, Long> {

    Optional<Instrument> findByBarcode(String barcode);

    Optional<Instrument> findBySerialNo(String serialNo);

    List<Instrument> findByModelIdAndStatus(Long modelId, InstrumentStatus status);

    @Query("SELECT i FROM Instrument i WHERE i.model.id = :modelId AND i.status = 'IN_STOCK' " +
           "AND i.id NOT IN (" +
           "  SELECT r.instrument.id FROM Reservation r " +
           "  WHERE r.status IN ('UNPAID', 'RESERVED', 'RENTED') " +
           "  AND r.startTime < :endTime AND r.endTime > :startTime" +
           ") ORDER BY i.id")
    List<Instrument> findAvailableForModel(@Param("modelId") Long modelId,
                                           @Param("startTime") LocalDateTime startTime,
                                           @Param("endTime") LocalDateTime endTime);

    long countByStatus(InstrumentStatus status);

    long countByWarehouseIdAndStatus(Long warehouseId, InstrumentStatus status);

    @Query("SELECT i.warehouse.id, COUNT(i) FROM Instrument i GROUP BY i.warehouse.id")
    List<Object[]> countByWarehouseGrouped();
}
```

- [ ] **步骤 3：创建 InstrumentModelRepository**

```java
// backend/src/main/java/com/instrumentrental/domain/repository/InstrumentModelRepository.java
package com.instrumentrental.domain.repository;

import com.instrumentrental.domain.model.InstrumentModel;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface InstrumentModelRepository extends JpaRepository<InstrumentModel, Long> {
    Page<InstrumentModel> findByCategoryIdAndStatus(Long categoryId, String status, Pageable pageable);
    Page<InstrumentModel> findByNameContainingAndStatus(String name, String status, Pageable pageable);
    Page<InstrumentModel> findByStatus(String status, Pageable pageable);
}
```

- [ ] **步骤 4：创建 ReservationRepository**

```java
// backend/src/main/java/com/instrumentrental/domain/repository/ReservationRepository.java
package com.instrumentrental.domain.repository;

import com.instrumentrental.domain.enums.ReservationStatus;
import com.instrumentrental.domain.model.Reservation;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface ReservationRepository extends JpaRepository<Reservation, Long> {

    Page<Reservation> findByUserIdOrderByCreatedAtDesc(Long userId, Pageable pageable);

    Page<Reservation> findByStatus(ReservationStatus status, Pageable pageable);

    List<Reservation> findByStatusAndPickupCode(ReservationStatus status, String pickupCode);

    @Query("SELECT r FROM Reservation r WHERE r.status IN ('UNPAID', 'RESERVED') " +
           "AND r.createdAt < :deadline")
    List<Reservation> findExpiredUnpaidOrUnpicked(@Param("deadline") LocalDateTime deadline);

    @Query("SELECT r FROM Reservation r WHERE r.status = 'RENTED' " +
           "AND r.endTime BETWEEN :start AND :end")
    List<Reservation> findReservationsEndingBetween(@Param("start") LocalDateTime start,
                                                     @Param("end") LocalDateTime end);

    @Query("SELECT r FROM Reservation r WHERE r.status = 'RENTED' AND r.endTime < :now")
    List<Reservation> findOverdueRentals(@Param("now") LocalDateTime now);

    @Query("SELECT r FROM Reservation r WHERE r.status IN ('RESERVED', 'RENTED') " +
           "AND r.startTime < :endTime AND r.endTime > :startTime " +
           "AND r.instrument.id IN :instrumentIds")
    List<Reservation> findConflictingReservations(@Param("instrumentIds") List<Long> instrumentIds,
                                                   @Param("startTime") LocalDateTime startTime,
                                                   @Param("endTime") LocalDateTime endTime);

    @Query("SELECT r FROM Reservation r JOIN r.instrument i JOIN i.model m " +
           "WHERE (:modelId IS NULL OR m.id = :modelId) " +
           "AND (:status IS NULL OR r.status = :status) " +
           "AND (:warehouseId IS NULL OR i.warehouse.id = :warehouseId) " +
           "ORDER BY r.createdAt DESC")
    Page<Reservation> findFiltered(@Param("modelId") Long modelId,
                                   @Param("status") ReservationStatus status,
                                   @Param("warehouseId") Long warehouseId,
                                   Pageable pageable);

    @Query("SELECT r FROM Reservation r JOIN r.instrument i JOIN i.model m " +
           "WHERE (:modelId IS NULL OR m.id = :modelId) " +
           "AND r.startTime < :endDate AND r.endTime > :startDate " +
           "AND r.status IN ('RESERVED', 'RENTED')")
    List<Reservation> findCalendarReservations(@Param("modelId") Long modelId,
                                                @Param("startDate") LocalDateTime startDate,
                                                @Param("endDate") LocalDateTime endDate);
}
```

- [ ] **步骤 5：创建 PaymentRepository**

```java
// backend/src/main/java/com/instrumentrental/domain/repository/PaymentRepository.java
package com.instrumentrental.domain.repository;

import com.instrumentrental.domain.enums.PaymentStatus;
import com.instrumentrental.domain.model.Payment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.Optional;

@Repository
public interface PaymentRepository extends JpaRepository<Payment, Long> {
    Optional<Payment> findByReservationId(Long reservationId);
    Optional<Payment> findByTransactionId(String transactionId);
    List<Payment> findByStatus(PaymentStatus status);
}
```

- [ ] **步骤 6：创建其余 Repository**

```java
// backend/src/main/java/com/instrumentrental/domain/repository/SystemConfigRepository.java
package com.instrumentrental.domain.repository;

import com.instrumentrental.domain.model.SystemConfig;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.Optional;

@Repository
public interface SystemConfigRepository extends JpaRepository<SystemConfig, Long> {
    Optional<SystemConfig> findByConfigKey(String configKey);
}

// backend/src/main/java/com/instrumentrental/domain/repository/PricingTierRepository.java
package com.instrumentrental.domain.repository;

import com.instrumentrental.domain.model.PricingTier;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface PricingTierRepository extends JpaRepository<PricingTier, Long> {
    List<PricingTier> findByModelIdOrderByDayFrom(Long modelId);
    List<PricingTier> findByModelIdIsNullOrderByDayFrom();
}

// backend/src/main/java/com/instrumentrental/domain/repository/PricingSeasonRepository.java
package com.instrumentrental.domain.repository;

import com.instrumentrental.domain.enums.SeasonType;
import com.instrumentrental.domain.model.PricingSeason;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.time.LocalDate;
import java.util.List;

@Repository
public interface PricingSeasonRepository extends JpaRepository<PricingSeason, Long> {
    List<PricingSeason> findBySeasonTypeOrderByPriorityDesc(SeasonType seasonType);
    List<PricingSeason> findBySeasonTypeAndDateStartLessThanEqualAndDateEndGreaterThanEqual(
            SeasonType seasonType, LocalDate date1, LocalDate date2);
    List<PricingSeason> findAllByOrderByPriorityDesc();
}

// backend/src/main/java/com/instrumentrental/domain/repository/WarehouseRepository.java
package com.instrumentrental.domain.repository;

import com.instrumentrental.domain.model.Warehouse;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface WarehouseRepository extends JpaRepository<Warehouse, Long> {}

// backend/src/main/java/com/instrumentrental/domain/repository/CategoryRepository.java
package com.instrumentrental.domain.repository;

import com.instrumentrental.domain.model.Category;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface CategoryRepository extends JpaRepository<Category, Long> {
    List<Category> findByParentIsNullOrderBySortOrder();
}

// backend/src/main/java/com/instrumentrental/domain/repository/MaintenanceLogRepository.java
package com.instrumentrental.domain.repository;

import com.instrumentrental.domain.model.MaintenanceLog;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface MaintenanceLogRepository extends JpaRepository<MaintenanceLog, Long> {
    List<MaintenanceLog> findByInstrumentIdOrderByCreatedAtDesc(Long instrumentId);
}

// backend/src/main/java/com/instrumentrental/domain/repository/OverdueRecordRepository.java
package com.instrumentrental.domain.repository;

import com.instrumentrental.domain.model.OverdueRecord;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface OverdueRecordRepository extends JpaRepository<OverdueRecord, Long> {
    Page<OverdueRecord> findByUserIdOrderByCreatedAtDesc(Long userId, Pageable pageable);
    boolean existsByReservationId(Long reservationId);
}

// backend/src/main/java/com/instrumentrental/domain/repository/OperationLogRepository.java
package com.instrumentrental.domain.repository;

import com.instrumentrental.domain.model.OperationLog;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface OperationLogRepository extends JpaRepository<OperationLog, Long> {}
```

- [ ] **步骤 7：编译验证**

```bash
cd backend && mvn compile
```

- [ ] **步骤 8：Commit**

```bash
git add backend/src/main/java/com/instrumentrental/domain/repository/
git commit -m "feat: add all Spring Data JPA repositories"
```

---

### 任务 6：DTO 层 — 数据传输对象

**文件：**
- 创建：`backend/src/main/java/com/instrumentrental/dto/ApiResponse.java`
- 创建：`backend/src/main/java/com/instrumentrental/dto/PageResponse.java`
- 创建：`backend/src/main/java/com/instrumentrental/dto/auth/LoginRequest.java`
- 创建：`backend/src/main/java/com/instrumentrental/dto/auth/LoginResponse.java`
- 创建：`backend/src/main/java/com/instrumentrental/dto/auth/WechatLoginRequest.java`
- 创建：`backend/src/main/java/com/instrumentrental/dto/reservation/QuoteRequest.java`
- 创建：`backend/src/main/java/com/instrumentrental/dto/reservation/QuoteResponse.java`
- 创建：`backend/src/main/java/com/instrumentrental/dto/reservation/CreateReservationRequest.java`
- 创建：`backend/src/main/java/com/instrumentrental/dto/reservation/ReservationResponse.java`
- 创建：`backend/src/main/java/com/instrumentrental/dto/payment/PaymentRequest.java`
- 创建：`backend/src/main/java/com/instrumentrental/dto/payment/PaymentResponse.java`
- 创建：`backend/src/main/java/com/instrumentrental/dto/admin/DashboardDTO.java`
- 创建：`backend/src/main/java/com/instrumentrental/dto/admin/ScanRequest.java`
- 创建：`backend/src/main/java/com/instrumentrental/dto/admin/CalendarEntry.java`
- 创建：`backend/src/main/java/com/instrumentrental/dto/admin/RevenueSummaryDTO.java`
- 创建：`backend/src/main/java/com/instrumentrental/dto/admin/RevenueRankingEntry.java`
- 创建：`backend/src/main/java/com/instrumentrental/dto/user/UserDTO.java`
- 创建：`backend/src/main/java/com/instrumentrental/dto/config/ConfigUpdateRequest.java`
- 创建：`backend/src/main/java/com/instrumentrental/dto/instrument/InstrumentDTO.java`
- 创建：`backend/src/main/java/com/instrumentrental/dto/instrument/ModelDTO.java`

- [ ] **步骤 1：创建通用响应 DTO**

```java
// backend/src/main/java/com/instrumentrental/dto/ApiResponse.java
package com.instrumentrental.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ApiResponse<T> {
    private int code;
    private String message;
    private T data;

    public static <T> ApiResponse<T> success(T data) {
        return ApiResponse.<T>builder().code(200).message("success").data(data).build();
    }

    public static <T> ApiResponse<T> error(int code, String message) {
        return ApiResponse.<T>builder().code(code).message(message).build();
    }
}

// backend/src/main/java/com/instrumentrental/dto/PageResponse.java
package com.instrumentrental.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PageResponse<T> {
    private List<T> content;
    private int totalPages;
    private long totalElements;
    private int page;
    private int size;
}
```

- [ ] **步骤 2：创建认证 DTO**

```java
// backend/src/main/java/com/instrumentrental/dto/auth/LoginRequest.java
package com.instrumentrental.dto.auth;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class LoginRequest {
    @NotBlank(message = "手机号不能为空")
    private String phone;

    @NotBlank(message = "密码不能为空")
    private String password;
}

// backend/src/main/java/com/instrumentrental/dto/auth/WechatLoginRequest.java
package com.instrumentrental.dto.auth;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class WechatLoginRequest {
    @NotBlank(message = "code不能为空")
    private String code;
}

// backend/src/main/java/com/instrumentrental/dto/auth/LoginResponse.java
package com.instrumentrental.dto.auth;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class LoginResponse {
    private String token;
    private String refreshToken;
    private Long userId;
    private String nickname;
    private String role;
    private String phone;
}
```

- [ ] **步骤 3：创建预约相关 DTO**

```java
// backend/src/main/java/com/instrumentrental/dto/reservation/QuoteRequest.java
package com.instrumentrental.dto.reservation;

import jakarta.validation.constraints.Future;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.time.LocalDateTime;

@Data
public class QuoteRequest {
    @NotNull(message = "请选择乐器型号")
    private Long modelId;

    @NotNull(message = "请选择开始日期")
    @Future(message = "开始日期必须在未来")
    private LocalDateTime startTime;

    @NotNull(message = "请选择结束日期")
    private LocalDateTime endTime;

    @Min(value = 1, message = "至少租赁1件")
    private int quantity;
}

// backend/src/main/java/com/instrumentrental/dto/reservation/QuoteResponse.java
package com.instrumentrental.dto.reservation;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.math.BigDecimal;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class QuoteResponse {
    private Long modelId;
    private String modelName;
    private LocalDateTime startTime;
    private LocalDateTime endTime;
    private int totalDays;
    private int availableCount;
    private BigDecimal dailyRate;
    private BigDecimal totalRental;
    private BigDecimal deposit;
    private BigDecimal totalAmount;
    private List<DailyBreakdown> dailyBreakdown;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class DailyBreakdown {
        private String date;
        private BigDecimal tierRate;
        private BigDecimal coefficient;
        private BigDecimal subtotal;
    }
}

// backend/src/main/java/com/instrumentrental/dto/reservation/CreateReservationRequest.java
package com.instrumentrental.dto.reservation;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import java.time.LocalDateTime;

@Data
public class CreateReservationRequest {
    @NotNull
    private Long modelId;

    @NotNull
    private LocalDateTime startTime;

    @NotNull
    private LocalDateTime endTime;

    @Min(1)
    private int quantity;
}

// backend/src/main/java/com/instrumentrental/dto/reservation/ReservationResponse.java
package com.instrumentrental.dto.reservation;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ReservationResponse {
    private Long id;
    private String modelName;
    private String brand;
    private List<String> instrumentSerials;
    private LocalDateTime startTime;
    private LocalDateTime endTime;
    private String status;
    private BigDecimal totalAmount;
    private BigDecimal deposit;
    private String pickupCode;
    private LocalDateTime pickupTime;
    private LocalDateTime returnTime;
    private LocalDateTime createdAt;
}
```

- [ ] **步骤 4：创建支付 DTO 和管理后台 DTO**

```java
// backend/src/main/java/com/instrumentrental/dto/payment/PaymentRequest.java
package com.instrumentrental.dto.payment;

import com.instrumentrental.domain.enums.PaymentChannel;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class PaymentRequest {
    @NotNull
    private Long reservationId;

    @NotNull
    private PaymentChannel channel;
}

// backend/src/main/java/com/instrumentrental/dto/payment/PaymentResponse.java
package com.instrumentrental.dto.payment;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.math.BigDecimal;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PaymentResponse {
    private Long paymentId;
    private Long reservationId;
    private BigDecimal amount;
    private String channel;
    private String qrCode;
    private String prepayId;
    private String paymentUrl;
    private String status;
}

// backend/src/main/java/com/instrumentrental/dto/admin/DashboardDTO.java
package com.instrumentrental.dto.admin;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.util.List;
import java.util.Map;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DashboardDTO {
    private long inStock;
    private long reserved;
    private long rented;
    private long overdue;
    private long maintenance;
    private long total;
    private Map<String, Long> byWarehouse;
}

// backend/src/main/java/com/instrumentrental/dto/admin/ScanRequest.java
package com.instrumentrental.dto.admin;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class ScanRequest {
    @NotBlank
    private String code;
    private Boolean damaged;
}

// backend/src/main/java/com/instrumentrental/dto/admin/CalendarEntry.java
package com.instrumentrental.dto.admin;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CalendarEntry {
    private Long modelId;
    private String modelName;
    private List<ReservationBlock> reservations;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ReservationBlock {
        private Long reservationId;
        private String userName;
        private String instrumentSerial;
        private LocalDateTime startTime;
        private LocalDateTime endTime;
        private String status;
    }
}

// backend/src/main/java/com/instrumentrental/dto/admin/RevenueSummaryDTO.java
package com.instrumentrental.dto.admin;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.math.BigDecimal;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RevenueSummaryDTO {
    private BigDecimal currentPeriodRevenue;
    private BigDecimal previousPeriodRevenue;
    private BigDecimal changePercent;
    private long orderCount;
    private String period;
}

// backend/src/main/java/com/instrumentrental/dto/admin/RevenueRankingEntry.java
package com.instrumentrental.dto.admin;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.math.BigDecimal;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RevenueRankingEntry {
    private String name;
    private BigDecimal revenue;
    private long orderCount;
}

// backend/src/main/java/com/instrumentrental/dto/user/UserDTO.java
package com.instrumentrental.dto.user;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserDTO {
    private Long id;
    private String phone;
    private String nickname;
    private String role;
    private String status;
    private LocalDateTime createdAt;
}

// backend/src/main/java/com/instrumentrental/dto/config/ConfigUpdateRequest.java
package com.instrumentrental.dto.config;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class ConfigUpdateRequest {
    @NotBlank
    private String value;
}

// backend/src/main/java/com/instrumentrental/dto/instrument/InstrumentDTO.java
package com.instrumentrental.dto.instrument;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class InstrumentDTO {
    private Long id;
    private String serialNo;
    private String barcode;
    private Long modelId;
    private String modelName;
    private Long warehouseId;
    private String warehouseName;
    private String status;
    private String conditionNote;
    private LocalDateTime createdAt;
}

// backend/src/main/java/com/instrumentrental/dto/instrument/ModelDTO.java
package com.instrumentrental.dto.instrument;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.math.BigDecimal;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ModelDTO {
    private Long id;
    private String name;
    private String brand;
    private Long categoryId;
    private String categoryName;
    private BigDecimal dailyRate;
    private BigDecimal deposit;
    private String images;
    private String specs;
    private String status;
    private long availableCount;
    private long totalCount;
}
```

- [ ] **步骤 5：编译验证**

```bash
cd backend && mvn compile
```

- [ ] **步骤 6：Commit**

```bash
git add backend/src/main/java/com/instrumentrental/dto/
git commit -m "feat: add DTO layer with request/response objects"
```

---

### 任务 7：异常处理

**文件：**
- 创建：`backend/src/main/java/com/instrumentrental/exception/ErrorCode.java`
- 创建：`backend/src/main/java/com/instrumentrental/exception/BusinessException.java`
- 创建：`backend/src/main/java/com/instrumentrental/exception/GlobalExceptionHandler.java`

- [ ] **步骤 1：编写 ErrorCode**

```java
// backend/src/main/java/com/instrumentrental/exception/ErrorCode.java
package com.instrumentrental.exception;

import lombok.Getter;

@Getter
public enum ErrorCode {
    SUCCESS(200, "操作成功"),
    BAD_REQUEST(400, "请求参数错误"),
    UNAUTHORIZED(401, "未登录或登录已过期"),
    FORBIDDEN(403, "无权限"),
    NOT_FOUND(404, "资源不存在"),
    CONFLICT(409, "资源冲突"),
    INTERNAL_ERROR(500, "服务器内部错误"),

    INSUFFICIENT_STOCK(4001, "库存不足"),
    INSTRUMENT_NOT_AVAILABLE(4002, "乐器不可用"),
    INVALID_STATE_TRANSITION(4003, "无效的状态变更"),
    RESERVATION_NOT_FOUND(4004, "预约不存在"),
    RESERVATION_EXPIRED(4005, "预约已过期"),
    PAYMENT_FAILED(4006, "支付失败"),
    PAYMENT_NOT_FOUND(4007, "支付记录不存在"),
    DUPLICATE_PAYMENT(4008, "重复支付"),
    REFUND_FAILED(4009, "退款失败"),
    USER_NOT_FOUND(4010, "用户不存在"),
    USER_BLACKLISTED(4011, "用户已被拉黑"),
    CONFIG_NOT_FOUND(4012, "配置项不存在"),
    WAREHOUSE_NOT_FOUND(4013, "仓库不存在"),
    MODEL_NOT_FOUND(4014, "乐器型号不存在"),
    INSTRUMENT_NOT_FOUND(4015, "乐器不存在"),
    SCAN_CODE_INVALID(4016, "扫码验证失败"),
    MAINTENANCE_NOT_FOUND(4017, "维护记录不存在"),
    OVERDUE_ALREADY_RECORDED(4018, "逾期已记录"),
    LOCK_ACQUIRE_FAILED(4019, "获取锁失败，请稍后重试"),
    PASSWORD_ERROR(4020, "密码错误");

    private final int code;
    private final String message;

    ErrorCode(int code, String message) {
        this.code = code;
        this.message = message;
    }
}
```

- [ ] **步骤 2：编写 BusinessException**

```java
// backend/src/main/java/com/instrumentrental/exception/BusinessException.java
package com.instrumentrental.exception;

import lombok.Getter;

@Getter
public class BusinessException extends RuntimeException {
    private final int code;

    public BusinessException(ErrorCode errorCode) {
        super(errorCode.getMessage());
        this.code = errorCode.getCode();
    }

    public BusinessException(ErrorCode errorCode, String detail) {
        super(detail);
        this.code = errorCode.getCode();
    }

    public BusinessException(int code, String message) {
        super(message);
        this.code = code;
    }
}
```

- [ ] **步骤 3：编写 GlobalExceptionHandler**

```java
// backend/src/main/java/com/instrumentrental/exception/GlobalExceptionHandler.java
package com.instrumentrental.exception;

import com.instrumentrental.dto.ApiResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.stream.Collectors;

@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(BusinessException.class)
    public ResponseEntity<ApiResponse<Void>> handleBusinessException(BusinessException e) {
        log.warn("Business exception: code={}, message={}", e.getCode(), e.getMessage());
        HttpStatus status = e.getCode() == ErrorCode.UNAUTHORIZED.getCode()
                ? HttpStatus.UNAUTHORIZED
                : e.getCode() == ErrorCode.FORBIDDEN.getCode()
                ? HttpStatus.FORBIDDEN
                : HttpStatus.BAD_REQUEST;
        return ResponseEntity.status(status)
                .body(ApiResponse.error(e.getCode(), e.getMessage()));
    }

    @ExceptionHandler(AuthenticationException.class)
    public ResponseEntity<ApiResponse<Void>> handleAuthenticationException(AuthenticationException e) {
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                .body(ApiResponse.error(ErrorCode.UNAUTHORIZED.getCode(), "认证失败: " + e.getMessage()));
    }

    @ExceptionHandler(AccessDeniedException.class)
    public ResponseEntity<ApiResponse<Void>> handleAccessDeniedException(AccessDeniedException e) {
        return ResponseEntity.status(HttpStatus.FORBIDDEN)
                .body(ApiResponse.error(ErrorCode.FORBIDDEN.getCode(), "无权限访问"));
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ApiResponse<Void>> handleValidation(MethodArgumentNotValidException e) {
        String message = e.getBindingResult().getFieldErrors().stream()
                .map(FieldError::getDefaultMessage)
                .collect(Collectors.joining(", "));
        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(ApiResponse.error(ErrorCode.BAD_REQUEST.getCode(), message));
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiResponse<Void>> handleException(Exception e) {
        log.error("Unexpected error", e);
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(ApiResponse.error(ErrorCode.INTERNAL_ERROR.getCode(), "服务器内部错误"));
    }
}
```

- [ ] **步骤 4：编译验证**

```bash
cd backend && mvn compile
```

- [ ] **步骤 5：Commit**

```bash
git add backend/src/main/java/com/instrumentrental/exception/
git commit -m "feat: add global exception handling with error codes"
```

任务 7/12 编写完成...

---

### 任务 8：Service 层 — PricingService & InventoryService

**文件：**
- 创建：`backend/src/main/java/com/instrumentrental/service/PricingService.java`
- 创建：`backend/src/test/java/com/instrumentrental/service/PricingServiceTest.java`
- 创建：`backend/src/main/java/com/instrumentrental/service/InventoryService.java`
- 创建：`backend/src/test/java/com/instrumentrental/service/InventoryServiceTest.java`

- [ ] **步骤 1：编写 PricingService**

```java
// backend/src/main/java/com/instrumentrental/service/PricingService.java
package com.instrumentrental.service;

import com.instrumentrental.domain.enums.SeasonType;
import com.instrumentrental.domain.model.InstrumentModel;
import com.instrumentrental.domain.model.PricingSeason;
import com.instrumentrental.domain.model.PricingTier;
import com.instrumentrental.domain.repository.PricingSeasonRepository;
import com.instrumentrental.domain.repository.PricingTierRepository;
import com.instrumentrental.dto.reservation.QuoteResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class PricingService {

    private final PricingTierRepository pricingTierRepository;
    private final PricingSeasonRepository pricingSeasonRepository;
    private final ConfigService configService;

    public QuoteResponse calculateQuote(InstrumentModel model, LocalDateTime start, LocalDateTime end, int quantity) {
        LocalDate startDate = start.toLocalDate();
        LocalDate endDate = end.toLocalDate();
        long totalDays = ChronoUnit.DAYS.between(startDate, endDate);
        if (totalDays <= 0) totalDays = 1;

        List<PricingTier> tiers = pricingTierRepository.findByModelIdOrderByDayFrom(model.getId());
        if (tiers.isEmpty()) {
            tiers = pricingTierRepository.findByModelIdIsNullOrderByDayFrom();
        }

        List<PricingSeason> allSeasons = pricingSeasonRepository.findAllByOrderByPriorityDesc();

        List<QuoteResponse.DailyBreakdown> breakdown = new ArrayList<>();
        BigDecimal totalRental = BigDecimal.ZERO;
        LocalDate current = startDate;

        for (long day = 1; day <= totalDays; day++) {
            BigDecimal tierRate = findTierRate(tiers, (int) day, model);
            BigDecimal coefficient = findCoefficient(allSeasons, current);
            BigDecimal subtotal = tierRate.multiply(coefficient).setScale(2, RoundingMode.HALF_UP);
            totalRental = totalRental.add(subtotal);

            breakdown.add(QuoteResponse.DailyBreakdown.builder()
                    .date(current.toString())
                    .tierRate(tierRate)
                    .coefficient(coefficient)
                    .subtotal(subtotal)
                    .build());
            current = current.plusDays(1);
        }

        BigDecimal deposit = calculateDeposit(model, (int) totalDays, quantity,
                totalRental.divide(BigDecimal.valueOf(totalDays * quantity), 2, RoundingMode.HALF_UP));
        BigDecimal totalAmount = totalRental.multiply(BigDecimal.valueOf(quantity)).add(deposit);

        return QuoteResponse.builder()
                .modelId(model.getId())
                .modelName(model.getName())
                .startTime(start)
                .endTime(end)
                .totalDays((int) totalDays)
                .dailyRate(model.getDailyRate())
                .totalRental(totalRental.multiply(BigDecimal.valueOf(quantity)))
                .deposit(deposit)
                .totalAmount(totalAmount)
                .dailyBreakdown(breakdown)
                .build();
    }

    private BigDecimal findTierRate(List<PricingTier> tiers, int dayNumber, InstrumentModel model) {
        for (PricingTier tier : tiers) {
            if (dayNumber >= tier.getDayFrom() && (tier.getDayTo() == null || dayNumber <= tier.getDayTo())) {
                return tier.getDailyRate();
            }
        }
        return model.getDailyRate() != null ? model.getDailyRate() : BigDecimal.ZERO;
    }

    private BigDecimal findCoefficient(List<PricingSeason> seasons, LocalDate date) {
        DayOfWeek dow = date.getDayOfWeek();
        boolean isWeekend = dow == DayOfWeek.SATURDAY || dow == DayOfWeek.SUNDAY;

        for (PricingSeason season : seasons) {
            if (season.getSeasonType() == SeasonType.HOLIDAY
                    && season.getDateStart() != null && season.getDateEnd() != null
                    && !date.isBefore(season.getDateStart()) && !date.isAfter(season.getDateEnd())) {
                return season.getCoefficient();
            }
        }

        for (PricingSeason season : seasons) {
            if (season.getSeasonType() == SeasonType.WEEKEND && isWeekend) {
                return season.getCoefficient();
            }
            if (season.getSeasonType() == SeasonType.WEEKDAY && !isWeekend) {
                return season.getCoefficient();
            }
        }

        return BigDecimal.ONE;
    }

    public BigDecimal calculateDeposit(InstrumentModel model, int days, int quantity, BigDecimal dailyRate) {
        if (model.getDeposit() != null) {
            return model.getDeposit().multiply(BigDecimal.valueOf(quantity));
        }
        String ratioStr = configService.getValue("deposit.default_ratio");
        BigDecimal ratio = new BigDecimal(ratioStr != null ? ratioStr : "2.0");
        return dailyRate.multiply(BigDecimal.valueOf(days))
                .multiply(ratio)
                .multiply(BigDecimal.valueOf(quantity))
                .setScale(2, RoundingMode.HALF_UP);
    }
}
```

- [ ] **步骤 2：编写 PricingServiceTest**

```java
// backend/src/test/java/com/instrumentrental/service/PricingServiceTest.java
package com.instrumentrental.service;

import com.instrumentrental.domain.enums.SeasonType;
import com.instrumentrental.domain.model.InstrumentModel;
import com.instrumentrental.domain.model.PricingSeason;
import com.instrumentrental.domain.model.PricingTier;
import com.instrumentrental.domain.repository.PricingSeasonRepository;
import com.instrumentrental.domain.repository.PricingTierRepository;
import com.instrumentrental.dto.reservation.QuoteResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class PricingServiceTest {

    @Mock private PricingTierRepository pricingTierRepository;
    @Mock private PricingSeasonRepository pricingSeasonRepository;
    @Mock private ConfigService configService;
    @InjectMocks private PricingService pricingService;

    private InstrumentModel model;

    @BeforeEach
    void setUp() {
        model = InstrumentModel.builder()
                .id(1L).name("Yamaha FG800").brand("Yamaha")
                .dailyRate(new BigDecimal("150")).deposit(null).build();
    }

    @Test
    void shouldUseTieredPricing() {
        when(pricingTierRepository.findByModelIdOrderByDayFrom(1L)).thenReturn(List.of(
                createTier(1L, 1, 3, 100),
                createTier(1L, 4, 7, 85)
        ));
        when(pricingSeasonRepository.findAllByOrderByPriorityDesc()).thenReturn(
                List.of(createSeason(SeasonType.WEEKDAY, BigDecimal.ONE, 0))
        );
        when(configService.getValue("deposit.default_ratio")).thenReturn("2.0");

        LocalDateTime start = LocalDateTime.of(2026, 6, 8, 10, 0);
        LocalDateTime end = LocalDateTime.of(2026, 6, 11, 10, 0);

        QuoteResponse quote = pricingService.calculateQuote(model, start, end, 1);

        assertThat(quote.getDailyBreakdown()).hasSize(3);
        assertThat(quote.getDailyBreakdown().get(0).getTierRate()).isEqualByComparingTo(new BigDecimal("100"));
        assertThat(quote.getDailyBreakdown().get(0).getCoefficient()).isEqualByComparingTo(BigDecimal.ONE);
    }

    private PricingTier createTier(Long modelId, int from, int to, int rate) {
        return PricingTier.builder()
                .model(modelId != null ? InstrumentModel.builder().id(modelId).build() : null)
                .dayFrom(from).dayTo(to).dailyRate(new BigDecimal(rate)).build();
    }

    private PricingSeason createSeason(SeasonType type, BigDecimal coeff, int priority) {
        return PricingSeason.builder()
                .seasonType(type).coefficient(coeff).priority(priority).build();
    }
}
```

- [ ] **步骤 3：编写 InventoryService**

```java
// backend/src/main/java/com/instrumentrental/service/InventoryService.java
package com.instrumentrental.service;

import com.instrumentrental.domain.enums.InstrumentStatus;
import com.instrumentrental.domain.model.Instrument;
import com.instrumentrental.domain.model.User;
import com.instrumentrental.domain.repository.InstrumentRepository;
import com.instrumentrental.dto.admin.DashboardDTO;
import com.instrumentrental.exception.BusinessException;
import com.instrumentrental.exception.ErrorCode;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class InventoryService {

    private final InstrumentRepository instrumentRepository;
    private final InstrumentStateMachine stateMachine;
    private final OperationLogService operationLogService;

    public int getAvailableCount(Long modelId, LocalDateTime start, LocalDateTime end) {
        return instrumentRepository.findAvailableForModel(modelId, start, end).size();
    }

    @Transactional
    public List<Instrument> lockInstruments(Long modelId, LocalDateTime start, LocalDateTime end, int count) {
        List<Instrument> available = instrumentRepository.findAvailableForModel(modelId, start, end);
        if (available.size() < count) {
            throw new BusinessException(ErrorCode.INSUFFICIENT_STOCK,
                    "需要" + count + "件，实际可用" + available.size() + "件");
        }
        List<Instrument> selected = available.subList(0, count);
        for (Instrument inst : selected) {
            stateMachine.transition(inst, InstrumentStatus.RESERVED, null);
        }
        instrumentRepository.saveAll(selected);
        return selected;
    }

    @Transactional
    public void releaseInstruments(List<Long> instrumentIds) {
        List<Instrument> instruments = instrumentRepository.findAllById(instrumentIds);
        for (Instrument inst : instruments) {
            if (inst.getStatus() == InstrumentStatus.RESERVED) {
                stateMachine.transition(inst, InstrumentStatus.IN_STOCK, null);
            }
        }
        instrumentRepository.saveAll(instruments);
    }

    @Transactional
    public void markAsRented(Long instrumentId, User operator) {
        Instrument instrument = instrumentRepository.findById(instrumentId)
                .orElseThrow(() -> new BusinessException(ErrorCode.INSTRUMENT_NOT_FOUND));
        stateMachine.transition(instrument, InstrumentStatus.RENTED, operator);
        instrumentRepository.save(instrument);
    }

    @Transactional
    public void markAsReturned(Long instrumentId, User operator) {
        Instrument instrument = instrumentRepository.findById(instrumentId)
                .orElseThrow(() -> new BusinessException(ErrorCode.INSTRUMENT_NOT_FOUND));
        stateMachine.transition(instrument, InstrumentStatus.IN_STOCK, operator);
        instrumentRepository.save(instrument);
    }

    @Transactional
    public void markAsDamaged(Long instrumentId, User operator) {
        Instrument instrument = instrumentRepository.findById(instrumentId)
                .orElseThrow(() -> new BusinessException(ErrorCode.INSTRUMENT_NOT_FOUND));
        stateMachine.transition(instrument, InstrumentStatus.DAMAGED_CHECK, operator);
        instrumentRepository.save(instrument);
    }

    @Transactional
    public void markMaintenance(Long instrumentId, User operator) {
        Instrument instrument = instrumentRepository.findById(instrumentId)
                .orElseThrow(() -> new BusinessException(ErrorCode.INSTRUMENT_NOT_FOUND));
        stateMachine.transition(instrument, InstrumentStatus.MAINTENANCE, operator);
        instrumentRepository.save(instrument);
    }

    @Transactional
    public void resolveMaintenance(Long instrumentId, User operator) {
        Instrument instrument = instrumentRepository.findById(instrumentId)
                .orElseThrow(() -> new BusinessException(ErrorCode.INSTRUMENT_NOT_FOUND));
        stateMachine.transition(instrument, InstrumentStatus.IN_STOCK, operator);
        instrumentRepository.save(instrument);
    }

    public DashboardDTO getDashboardStats() {
        long inStock = instrumentRepository.countByStatus(InstrumentStatus.IN_STOCK);
        long reserved = instrumentRepository.countByStatus(InstrumentStatus.RESERVED);
        long rented = instrumentRepository.countByStatus(InstrumentStatus.RENTED);
        long maintenance = instrumentRepository.countByStatus(InstrumentStatus.MAINTENANCE);
        long damaged = instrumentRepository.countByStatus(InstrumentStatus.DAMAGED_CHECK);
        long scrapped = instrumentRepository.countByStatus(InstrumentStatus.SCRAPPED);
        long total = instrumentRepository.count();

        List<Object[]> warehouseStats = instrumentRepository.countByWarehouseGrouped();
        Map<String, Long> byWarehouse = new HashMap<>();
        for (Object[] row : warehouseStats) {
            byWarehouse.put(row[0].toString(), (Long) row[1]);
        }

        return DashboardDTO.builder()
                .inStock(inStock).reserved(reserved).rented(rented)
                .overdue(0).maintenance(maintenance + damaged + scrapped)
                .total(total).byWarehouse(byWarehouse)
                .build();
    }
}
```

- [ ] **步骤 4：编译验证**

```bash
cd backend && mvn compile
```

- [ ] **步骤 5：Commit**

```bash
git add backend/src/main/java/com/instrumentrental/service/PricingService.java backend/src/main/java/com/instrumentrental/service/InventoryService.java backend/src/test/
git commit -m "feat: add PricingService and InventoryService with tests"
```

---

### 任务 9：Service 层 — StateMachine & ReservationService

**文件：**
- 创建：`backend/src/main/java/com/instrumentrental/service/InstrumentStateMachine.java`
- 创建：`backend/src/main/java/com/instrumentrental/service/ReservationService.java`

- [ ] **步骤 1：编写 InstrumentStateMachine**

```java
// backend/src/main/java/com/instrumentrental/service/InstrumentStateMachine.java
package com.instrumentrental.service;

import com.instrumentrental.domain.enums.InstrumentStatus;
import com.instrumentrental.domain.model.Instrument;
import com.instrumentrental.domain.model.User;
import com.instrumentrental.exception.BusinessException;
import com.instrumentrental.exception.ErrorCode;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.*;

@Slf4j
@Service
@RequiredArgsConstructor
public class InstrumentStateMachine {

    private static final Map<InstrumentStatus, Set<InstrumentStatus>> TRANSITIONS = new LinkedHashMap<>();

    static {
        TRANSITIONS.put(InstrumentStatus.IN_STOCK,
                Set.of(InstrumentStatus.RESERVED, InstrumentStatus.MAINTENANCE, InstrumentStatus.SCRAPPED));
        TRANSITIONS.put(InstrumentStatus.RESERVED,
                Set.of(InstrumentStatus.RENTED, InstrumentStatus.CANCELLED, InstrumentStatus.EXPIRED));
        TRANSITIONS.put(InstrumentStatus.RENTED,
                Set.of(InstrumentStatus.IN_STOCK, InstrumentStatus.DAMAGED_CHECK));
        TRANSITIONS.put(InstrumentStatus.DAMAGED_CHECK,
                Set.of(InstrumentStatus.IN_STOCK, InstrumentStatus.MAINTENANCE, InstrumentStatus.SCRAPPED));
        TRANSITIONS.put(InstrumentStatus.MAINTENANCE,
                Set.of(InstrumentStatus.IN_STOCK, InstrumentStatus.SCRAPPED));
    }

    private final OperationLogService operationLogService;

    public boolean canTransition(InstrumentStatus from, InstrumentStatus to) {
        return TRANSITIONS.getOrDefault(from, Set.of()).contains(to);
    }

    public void validateTransition(InstrumentStatus from, InstrumentStatus to) {
        if (from == to) return;
        if (!canTransition(from, to)) {
            throw new BusinessException(ErrorCode.INVALID_STATE_TRANSITION,
                    "不能从 " + from + " 变更为 " + to);
        }
    }

    public void transition(Instrument instrument, InstrumentStatus target, User operator) {
        InstrumentStatus from = instrument.getStatus();
        validateTransition(from, target);
        instrument.setStatus(target);
        if (operator != null) {
            operationLogService.log(operator, "STATE_CHANGE_" + from + "_TO_" + target,
                    "INSTRUMENT", instrument.getId(),
                    "{\"from\":\"" + from + "\",\"to\":\"" + target + "\"}");
        }
        log.info("Instrument {} status: {} -> {}", instrument.getSerialNo(), from, target);
    }
}
```

- [ ] **步骤 2：编写 ReservationService**

```java
// backend/src/main/java/com/instrumentrental/service/ReservationService.java
package com.instrumentrental.service;

import com.instrumentrental.domain.enums.InstrumentStatus;
import com.instrumentrental.domain.enums.PaymentChannel;
import com.instrumentrental.domain.enums.ReservationStatus;
import com.instrumentrental.domain.model.*;
import com.instrumentrental.domain.repository.*;
import com.instrumentrental.dto.reservation.*;
import com.instrumentrental.exception.BusinessException;
import com.instrumentrental.exception.ErrorCode;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class ReservationService {

    private final ReservationRepository reservationRepository;
    private final InstrumentRepository instrumentRepository;
    private final InstrumentModelRepository modelRepository;
    private final UserRepository userRepository;
    private final InventoryService inventoryService;
    private final PricingService pricingService;
    private final DistributedLockService lockService;
    private final InstrumentStateMachine stateMachine;
    private final PaymentService paymentService;
    private final NotificationService notificationService;
    private final OverdueRecordRepository overdueRecordRepository;

    public QuoteResponse quote(QuoteRequest request) {
        InstrumentModel model = modelRepository.findById(request.getModelId())
                .orElseThrow(() -> new BusinessException(ErrorCode.MODEL_NOT_FOUND));
        int available = inventoryService.getAvailableCount(model.getId(), request.getStartTime(), request.getEndTime());
        QuoteResponse response = pricingService.calculateQuote(model, request.getStartTime(), request.getEndTime(), request.getQuantity());
        response.setAvailableCount(available);
        return response;
    }

    @Transactional
    public List<ReservationResponse> createReservation(CreateReservationRequest request, Long userId) {
        InstrumentModel model = modelRepository.findById(request.getModelId())
                .orElseThrow(() -> new BusinessException(ErrorCode.MODEL_NOT_FOUND));
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new BusinessException(ErrorCode.USER_NOT_FOUND));

        String lockKey = "reserve:" + model.getId() + ":" + request.getStartTime() + ":" + request.getEndTime();
        if (!lockService.acquireLock(lockKey, 30)) {
            throw new BusinessException(ErrorCode.LOCK_ACQUIRE_FAILED);
        }

        try {
            List<Instrument> instruments = inventoryService.lockInstruments(
                    model.getId(), request.getStartTime(), request.getEndTime(), request.getQuantity());

            QuoteResponse quote = pricingService.calculateQuote(model, request.getStartTime(), request.getEndTime(), 1);

            List<ReservationResponse> responses = new ArrayList<>();
            for (Instrument inst : instruments) {
                Reservation reservation = Reservation.builder()
                        .user(user).instrument(inst)
                        .startTime(request.getStartTime()).endTime(request.getEndTime())
                        .status(ReservationStatus.UNPAID)
                        .pickupCode(generatePickupCode())
                        .priceDetail(toJson(quote))
                        .build();
                reservationRepository.save(reservation);

                paymentService.createPayment(reservation.getId(), PaymentChannel.WECHAT);

                responses.add(ReservationResponse.builder()
                        .id(reservation.getId())
                        .modelName(model.getName())
                        .brand(model.getBrand())
                        .instrumentSerials(List.of(inst.getSerialNo()))
                        .startTime(request.getStartTime()).endTime(request.getEndTime())
                        .status(reservation.getStatus().name())
                        .totalAmount(quote.getTotalAmount())
                        .deposit(quote.getDeposit())
                        .pickupCode(reservation.getPickupCode())
                        .createdAt(reservation.getCreatedAt())
                        .build());
            }

            return responses;
        } finally {
            lockService.releaseLock(lockKey);
        }
    }

    @Transactional
    public void cancelReservation(Long reservationId, Long userId) {
        Reservation reservation = reservationRepository.findById(reservationId)
                .orElseThrow(() -> new BusinessException(ErrorCode.RESERVATION_NOT_FOUND));
        if (!reservation.getUser().getId().equals(userId)) {
            throw new BusinessException(ErrorCode.FORBIDDEN);
        }
        if (reservation.getStatus() == ReservationStatus.CANCELLED
                || reservation.getStatus() == ReservationStatus.EXPIRED) {
            throw new BusinessException(ErrorCode.INVALID_STATE_TRANSITION, "预约已取消或过期");
        }

        if (reservation.getStatus() == ReservationStatus.RESERVED) {
            Payment payment = paymentService.findByReservationId(reservationId);
            if (payment != null) {
                paymentService.refund(payment.getId());
            }
        }

        inventoryService.releaseInstruments(List.of(reservation.getInstrument().getId()));
        reservation.setStatus(ReservationStatus.CANCELLED);
        reservationRepository.save(reservation);
    }

    @Transactional
    public ReservationResponse confirmPickup(String code, User operator) {
        List<Reservation> reservations = reservationRepository.findByStatusAndPickupCode(ReservationStatus.RESERVED, code);
        if (reservations.isEmpty()) {
            Instrument instrument = instrumentRepository.findByBarcode(code)
                    .orElse(null);
            if (instrument == null) {
                throw new BusinessException(ErrorCode.SCAN_CODE_INVALID, "取货码或条码无效");
            }
            reservations = reservationRepository.findByStatusAndPickupCode(ReservationStatus.RESERVED,
                    null);
            reservations = reservations.stream()
                    .filter(r -> r.getInstrument().getId().equals(instrument.getId()))
                    .collect(Collectors.toList());
        }
        if (reservations.isEmpty()) {
            throw new BusinessException(ErrorCode.RESERVATION_NOT_FOUND, "未找到待取货的预约");
        }

        Reservation reservation = reservations.get(0);
        if (reservation.getEndTime().isBefore(LocalDateTime.now())) {
            throw new BusinessException(ErrorCode.RESERVATION_EXPIRED);
        }

        inventoryService.markAsRented(reservation.getInstrument().getId(), operator);
        reservation.setStatus(ReservationStatus.RENTED);
        reservation.setPickupTime(LocalDateTime.now());
        reservationRepository.save(reservation);

        return buildResponse(reservation);
    }

    @Transactional
    public ReservationResponse confirmReturn(String barcode, User operator, boolean damaged) {
        Instrument instrument = instrumentRepository.findByBarcode(barcode)
                .orElseThrow(() -> new BusinessException(ErrorCode.SCAN_CODE_INVALID, "条码无效"));

        List<Reservation> activeRentals = reservationRepository.findByStatusAndPickupCode(
                ReservationStatus.RENTED, null);
        Reservation reservation = activeRentals.stream()
                .filter(r -> r.getInstrument().getId().equals(instrument.getId()))
                .findFirst()
                .orElseThrow(() -> new BusinessException(ErrorCode.RESERVATION_NOT_FOUND, "未找到租赁中的记录"));

        if (reservation.getEndTime().isBefore(LocalDateTime.now())) {
            if (!overdueRecordRepository.existsByReservationId(reservation.getId())) {
                long overdueDays = java.time.Duration.between(reservation.getEndTime(), LocalDateTime.now()).toDays();
                OverdueRecord record = OverdueRecord.builder()
                        .user(reservation.getUser())
                        .reservation(reservation)
                        .overdueDays((int) Math.max(1, overdueDays))
                        .build();
                overdueRecordRepository.save(record);
                notificationService.sendOverdueAlert(record);
            }
        }

        if (damaged) {
            inventoryService.markAsDamaged(instrument.getId(), operator);
        } else {
            inventoryService.markAsReturned(instrument.getId(), operator);
        }

        reservation.setStatus(ReservationStatus.RETURNED);
        reservation.setReturnTime(LocalDateTime.now());
        reservationRepository.save(reservation);

        return buildResponse(reservation);
    }

    public Page<ReservationResponse> getMyReservations(Long userId, Pageable pageable) {
        return reservationRepository.findByUserIdOrderByCreatedAtDesc(userId, pageable)
                .map(this::buildResponse);
    }

    private ReservationResponse buildResponse(Reservation r) {
        return ReservationResponse.builder()
                .id(r.getId())
                .modelName(r.getInstrument().getModel().getName())
                .brand(r.getInstrument().getModel().getBrand())
                .instrumentSerials(List.of(r.getInstrument().getSerialNo()))
                .startTime(r.getStartTime()).endTime(r.getEndTime())
                .status(r.getStatus().name())
                .pickupCode(r.getPickupCode())
                .pickupTime(r.getPickupTime()).returnTime(r.getReturnTime())
                .createdAt(r.getCreatedAt())
                .build();
    }

    private String generatePickupCode() {
        String code;
        do {
            code = String.format("%06d", new Random().nextInt(1000000));
        } while (!reservationRepository.findByStatusAndPickupCode(ReservationStatus.RESERVED, code).isEmpty());
        return code;
    }

    private String toJson(QuoteResponse quote) {
        return "{\"totalAmount\":" + quote.getTotalAmount() + ",\"deposit\":" + quote.getDeposit() + "}";
    }
}
```

- [ ] **步骤 3：编译验证**

```bash
cd backend && mvn compile
```

- [ ] **步骤 4：Commit**

```bash
git add backend/src/main/java/com/instrumentrental/service/InstrumentStateMachine.java backend/src/main/java/com/instrumentrental/service/ReservationService.java
git commit -m "feat: add StateMachine and ReservationService"
```

任务 9/12 编写完成...

---

### 任务 10：Service 层 — Payment、Notification、Config、User、OperationLog

**文件：**
- 创建：`backend/src/main/java/com/instrumentrental/service/PaymentService.java`
- 创建：`backend/src/main/java/com/instrumentrental/service/NotificationService.java`
- 创建：`backend/src/main/java/com/instrumentrental/service/ConfigService.java`
- 创建：`backend/src/main/java/com/instrumentrental/service/UserService.java`
- 创建：`backend/src/main/java/com/instrumentrental/service/OperationLogService.java`

- [ ] **步骤 1：编写 PaymentService**

```java
// backend/src/main/java/com/instrumentrental/service/PaymentService.java
package com.instrumentrental.service;

import com.instrumentrental.domain.enums.PaymentChannel;
import com.instrumentrental.domain.enums.PaymentStatus;
import com.instrumentrental.domain.enums.ReservationStatus;
import com.instrumentrental.domain.model.Payment;
import com.instrumentrental.domain.model.Reservation;
import com.instrumentrental.domain.repository.PaymentRepository;
import com.instrumentrental.domain.repository.ReservationRepository;
import com.instrumentrental.dto.payment.PaymentResponse;
import com.instrumentrental.exception.BusinessException;
import com.instrumentrental.exception.ErrorCode;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Optional;

@Slf4j
@Service
@RequiredArgsConstructor
public class PaymentService {

    private final PaymentRepository paymentRepository;
    private final ReservationRepository reservationRepository;

    @Transactional
    public PaymentResponse createPayment(Long reservationId, PaymentChannel channel) {
        Reservation reservation = reservationRepository.findById(reservationId)
                .orElseThrow(() -> new BusinessException(ErrorCode.RESERVATION_NOT_FOUND));

        Optional<Payment> existing = paymentRepository.findByReservationId(reservationId);
        if (existing.isPresent() && existing.get().getStatus() == PaymentStatus.PAID) {
            throw new BusinessException(ErrorCode.DUPLICATE_PAYMENT);
        }

        Payment payment = existing.orElseGet(() -> {
            BigDecimal amount = reservation.getPriceDetail() != null
                    ? parseAmountFromDetail(reservation.getPriceDetail())
                    : BigDecimal.ZERO;
            return Payment.builder()
                    .reservation(reservation)
                    .amount(amount)
                    .channel(channel)
                    .status(PaymentStatus.PENDING)
                    .refundAmount(BigDecimal.ZERO)
                    .build();
        });

        payment.setStatus(PaymentStatus.PENDING);
        payment.setChannel(channel);
        paymentRepository.save(payment);

        return PaymentResponse.builder()
                .paymentId(payment.getId())
                .reservationId(reservationId)
                .amount(payment.getAmount())
                .channel(channel.name())
                .status(payment.getStatus().name())
                .build();
    }

    @Transactional
    public void handleCallback(String channel, String transactionId, boolean success) {
        if (transactionId == null) return;
        Optional<Payment> existing = paymentRepository.findByTransactionId(transactionId);
        if (existing.isPresent() && existing.get().getStatus() == PaymentStatus.PAID) return;

        Payment payment = existing.orElseThrow(() ->
                new BusinessException(ErrorCode.PAYMENT_NOT_FOUND));

        if (success) {
            payment.setTransactionId(transactionId);
            payment.setStatus(PaymentStatus.PAID);
            payment.setUpdatedAt(LocalDateTime.now());
            paymentRepository.save(payment);

            Reservation reservation = payment.getReservation();
            if (reservation.getStatus() == ReservationStatus.UNPAID) {
                reservation.setStatus(ReservationStatus.RESERVED);
                reservation.setUpdatedAt(LocalDateTime.now());
                reservationRepository.save(reservation);
            }
        } else {
            payment.setStatus(PaymentStatus.FAILED);
            paymentRepository.save(payment);
        }
    }

    @Transactional
    public void refund(Long paymentId) {
        Payment payment = paymentRepository.findById(paymentId)
                .orElseThrow(() -> new BusinessException(ErrorCode.PAYMENT_NOT_FOUND));
        if (payment.getStatus() != PaymentStatus.PAID) {
            throw new BusinessException(ErrorCode.REFUND_FAILED, "只有已支付的订单可以退款");
        }
        payment.setStatus(PaymentStatus.REFUNDING);
        payment.setRefundAmount(payment.getAmount());
        paymentRepository.save(payment);

        payment.setStatus(PaymentStatus.REFUNDED);
        paymentRepository.save(payment);
        log.info("Payment {} refunded: {}", paymentId, payment.getAmount());
    }

    public Payment findByReservationId(Long reservationId) {
        return paymentRepository.findByReservationId(reservationId).orElse(null);
    }

    private BigDecimal parseAmountFromDetail(String detail) {
        try {
            String amount = detail.replaceAll(".*\"totalAmount\":([\\d.]+).*", "$1");
            return new BigDecimal(amount);
        } catch (Exception e) {
            return BigDecimal.ZERO;
        }
    }
}
```

- [ ] **步骤 2：编写剩余 Service**

```java
// backend/src/main/java/com/instrumentrental/service/NotificationService.java
package com.instrumentrental.service;

import com.instrumentrental.domain.model.OverdueRecord;
import com.instrumentrental.domain.model.Reservation;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class NotificationService {

    private final RabbitTemplate rabbitTemplate;

    public void sendReturnReminder(Reservation reservation) {
        rabbitTemplate.convertAndSend("notification.exchange", "notification.reminder",
                "{\"userId\":" + reservation.getUser().getId()
                + ",\"reservationId\":" + reservation.getId()
                + ",\"model\":\"" + reservation.getInstrument().getModel().getName()
                + "\",\"endTime\":\"" + reservation.getEndTime() + "\"}");
        log.info("Return reminder queued for reservation {}", reservation.getId());
    }

    public void sendOverdueAlert(OverdueRecord record) {
        rabbitTemplate.convertAndSend("notification.exchange", "notification.overdue",
                "{\"userId\":" + record.getUser().getId()
                + ",\"reservationId\":" + record.getReservation().getId()
                + ",\"overdueDays\":" + record.getOverdueDays() + "}");
        log.info("Overdue alert queued for user {}", record.getUser().getId());
    }
}

// backend/src/main/java/com/instrumentrental/service/ConfigService.java
package com.instrumentrental.service;

import com.instrumentrental.domain.model.SystemConfig;
import com.instrumentrental.domain.repository.SystemConfigRepository;
import com.instrumentrental.exception.BusinessException;
import com.instrumentrental.exception.ErrorCode;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.concurrent.TimeUnit;

@Service
@RequiredArgsConstructor
public class ConfigService {

    private final SystemConfigRepository configRepository;
    private final StringRedisTemplate stringRedisTemplate;

    public String getValue(String key) {
        String cached = stringRedisTemplate.opsForValue().get("config:" + key);
        if (cached != null) return cached;

        String value = configRepository.findByConfigKey(key)
                .map(SystemConfig::getConfigValue)
                .orElse(null);
        if (value != null) {
            stringRedisTemplate.opsForValue().set("config:" + key, value, 1, TimeUnit.HOURS);
        }
        return value;
    }

    public void updateValue(String key, String value) {
        SystemConfig config = configRepository.findByConfigKey(key)
                .orElseThrow(() -> new BusinessException(ErrorCode.CONFIG_NOT_FOUND));
        config.setConfigValue(value);
        configRepository.save(config);
        stringRedisTemplate.opsForValue().set("config:" + key, value, 1, TimeUnit.HOURS);
    }

    public List<SystemConfig> getAllConfigs() {
        return configRepository.findAll();
    }
}

// backend/src/main/java/com/instrumentrental/service/UserService.java
package com.instrumentrental.service;

import com.instrumentrental.domain.enums.UserRole;
import com.instrumentrental.domain.enums.UserStatus;
import com.instrumentrental.domain.model.User;
import com.instrumentrental.domain.repository.OverdueRecordRepository;
import com.instrumentrental.domain.repository.UserRepository;
import com.instrumentrental.exception.BusinessException;
import com.instrumentrental.exception.ErrorCode;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;
    private final OverdueRecordRepository overdueRecordRepository;
    private final PasswordEncoder passwordEncoder;

    public User createOrUpdateByWechat(String openid) {
        return userRepository.findByOpenid(openid).orElseGet(() -> {
            User user = User.builder()
                    .openid(openid)
                    .nickname("微信用户")
                    .role(UserRole.ROLE_USER)
                    .status(UserStatus.ACTIVE)
                    .build();
            return userRepository.save(user);
        });
    }

    public User findByPhone(String phone) {
        return userRepository.findByPhone(phone)
                .orElseThrow(() -> new BusinessException(ErrorCode.USER_NOT_FOUND));
    }

    public User findByOpenid(String openid) {
        return userRepository.findByOpenid(openid)
                .orElseThrow(() -> new BusinessException(ErrorCode.USER_NOT_FOUND));
    }

    public void updateStatus(Long userId, UserStatus status) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new BusinessException(ErrorCode.USER_NOT_FOUND));
        user.setStatus(status);
        userRepository.save(user);
    }

    public Page<?> getOverdueHistory(Long userId, Pageable pageable) {
        return overdueRecordRepository.findByUserIdOrderByCreatedAtDesc(userId, pageable);
    }

    public boolean validatePassword(String raw, String encoded) {
        return passwordEncoder.matches(raw, encoded);
    }
}

// backend/src/main/java/com/instrumentrental/service/OperationLogService.java
package com.instrumentrental.service;

import com.instrumentrental.domain.model.OperationLog;
import com.instrumentrental.domain.model.User;
import com.instrumentrental.domain.repository.OperationLogRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class OperationLogService {
    private final OperationLogRepository logRepository;

    public void log(User operator, String action, String targetType, Long targetId, String detail) {
        OperationLog log = OperationLog.builder()
                .operator(operator)
                .action(action)
                .targetType(targetType)
                .targetId(targetId)
                .detail(detail)
                .build();
        logRepository.save(log);
    }
}
```

- [ ] **步骤 3：编译验证并提交**

```bash
cd backend && mvn compile
git add backend/src/main/java/com/instrumentrental/service/PaymentService.java backend/src/main/java/com/instrumentrental/service/NotificationService.java backend/src/main/java/com/instrumentrental/service/ConfigService.java backend/src/main/java/com/instrumentrental/service/UserService.java backend/src/main/java/com/instrumentrental/service/OperationLogService.java
git commit -m "feat: add Payment, Notification, Config, User, OperationLog services"
```

---

### 任务 11：Security + Redis 锁 + RabbitMQ 配置

**文件：**
- 创建：`backend/src/main/java/com/instrumentrental/security/JwtTokenProvider.java`
- 创建：`backend/src/main/java/com/instrumentrental/security/JwtAuthenticationFilter.java`
- 创建：`backend/src/main/java/com/instrumentrental/config/SecurityConfig.java`
- 创建：`backend/src/main/java/com/instrumentrental/config/RedisConfig.java`
- 创建：`backend/src/main/java/com/instrumentrental/config/RabbitMQConfig.java`
- 创建：`backend/src/main/java/com/instrumentrental/service/DistributedLockService.java`

- [ ] **步骤 1：编写 JwtTokenProvider**

```java
// backend/src/main/java/com/instrumentrental/security/JwtTokenProvider.java
package com.instrumentrental.security;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.util.Base64;
import java.util.Date;

@Component
public class JwtTokenProvider {

    private final SecretKey key;
    private final long expirationMs;
    private final long refreshExpirationMs;

    public JwtTokenProvider(@Value("${jwt.secret}") String base64Secret,
                            @Value("${jwt.expiration-ms}") long expirationMs,
                            @Value("${jwt.refresh-expiration-ms}") long refreshExpirationMs) {
        this.key = Keys.hmacShaKeyFor(Base64.getDecoder().decode(base64Secret));
        this.expirationMs = expirationMs;
        this.refreshExpirationMs = refreshExpirationMs;
    }

    public String generateToken(Long userId, String role) {
        Date now = new Date();
        return Jwts.builder()
                .subject(userId.toString())
                .claim("role", role)
                .issuedAt(now)
                .expiration(new Date(now.getTime() + expirationMs))
                .signWith(key)
                .compact();
    }

    public String generateRefreshToken(Long userId) {
        Date now = new Date();
        return Jwts.builder()
                .subject(userId.toString())
                .issuedAt(now)
                .expiration(new Date(now.getTime() + refreshExpirationMs))
                .signWith(key)
                .compact();
    }

    public boolean validateToken(String token) {
        try {
            Jwts.parser().verifyWith(key).build().parseSignedClaims(token);
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    public Long getUserId(String token) {
        Claims claims = Jwts.parser().verifyWith(key).build()
                .parseSignedClaims(token).getPayload();
        return Long.parseLong(claims.getSubject());
    }

    public String getRole(String token) {
        Claims claims = Jwts.parser().verifyWith(key).build()
                .parseSignedClaims(token).getPayload();
        return claims.get("role", String.class);
    }
}
```

- [ ] **步骤 2：编写 JwtAuthenticationFilter 和 SecurityConfig**

```java
// backend/src/main/java/com/instrumentrental/security/JwtAuthenticationFilter.java
package com.instrumentrental.security;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.List;

@Component
@RequiredArgsConstructor
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final JwtTokenProvider jwtTokenProvider;

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {
        String token = extractToken(request);
        if (token != null && jwtTokenProvider.validateToken(token)) {
            Long userId = jwtTokenProvider.getUserId(token);
            String role = jwtTokenProvider.getRole(token);
            UsernamePasswordAuthenticationToken auth = new UsernamePasswordAuthenticationToken(
                    userId, null, List.of(new SimpleGrantedAuthority(role)));
            SecurityContextHolder.getContext().setAuthentication(auth);
        }
        filterChain.doFilter(request, response);
    }

    private String extractToken(HttpServletRequest request) {
        String bearer = request.getHeader("Authorization");
        if (StringUtils.hasText(bearer) && bearer.startsWith("Bearer ")) {
            return bearer.substring(7);
        }
        return null;
    }
}

// backend/src/main/java/com/instrumentrental/config/SecurityConfig.java
package com.instrumentrental.config;

import com.instrumentrental.security.JwtAuthenticationFilter;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity
@RequiredArgsConstructor
public class SecurityConfig {

    private final JwtAuthenticationFilter jwtAuthenticationFilter;

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
            .csrf(csrf -> csrf.disable())
            .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
            .authorizeHttpRequests(auth -> auth
                .requestMatchers("/api/auth/**", "/api/payments/callback/**",
                        "/api/categories", "/api/models/**").permitAll()
                .requestMatchers("/api/admin/**").hasRole("ADMIN")
                .anyRequest().authenticated()
            )
            .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class);
        return http.build();
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
}

// backend/src/main/java/com/instrumentrental/config/RedisConfig.java
package com.instrumentrental.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.serializer.Jackson2JsonRedisSerializer;
import org.springframework.data.redis.serializer.StringRedisSerializer;

@Configuration
public class RedisConfig {

    @Bean
    public RedisTemplate<String, Object> redisTemplate(RedisConnectionFactory factory) {
        ObjectMapper mapper = new ObjectMapper();
        mapper.registerModule(new JavaTimeModule());

        RedisTemplate<String, Object> template = new RedisTemplate<>();
        template.setConnectionFactory(factory);
        template.setKeySerializer(new StringRedisSerializer());
        template.setValueSerializer(new Jackson2JsonRedisSerializer<>(mapper, Object.class));
        template.setHashKeySerializer(new StringRedisSerializer());
        template.setHashValueSerializer(new Jackson2JsonRedisSerializer<>(mapper, Object.class));
        template.afterPropertiesSet();
        return template;
    }

    @Bean
    public StringRedisTemplate stringRedisTemplate(RedisConnectionFactory factory) {
        return new StringRedisTemplate(factory);
    }
}

// backend/src/main/java/com/instrumentrental/config/RabbitMQConfig.java
package com.instrumentrental.config;

import org.springframework.amqp.core.*;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RabbitMQConfig {

    public static final String REMINDER_QUEUE = "notification.reminder";
    public static final String OVERDUE_QUEUE = "notification.overdue";
    public static final String EXCHANGE = "notification.exchange";

    @Bean public Queue reminderQueue() { return new Queue(REMINDER_QUEUE, true); }
    @Bean public Queue overdueQueue() { return new Queue(OVERDUE_QUEUE, true); }
    @Bean public TopicExchange notificationExchange() { return new TopicExchange(EXCHANGE); }

    @Bean public Binding bindReminder(Queue reminderQueue, TopicExchange exchange) {
        return BindingBuilder.bind(reminderQueue).to(exchange).with(REMINDER_QUEUE);
    }
    @Bean public Binding bindOverdue(Queue overdueQueue, TopicExchange exchange) {
        return BindingBuilder.bind(overdueQueue).to(exchange).with(OVERDUE_QUEUE);
    }
    @Bean public Jackson2JsonMessageConverter messageConverter() {
        return new Jackson2JsonMessageConverter();
    }
    @Bean public RabbitTemplate rabbitTemplate(ConnectionFactory factory, Jackson2JsonMessageConverter converter) {
        RabbitTemplate template = new RabbitTemplate(factory);
        template.setMessageConverter(converter);
        return template;
    }
}

// backend/src/main/java/com/instrumentrental/service/DistributedLockService.java
package com.instrumentrental.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.util.concurrent.TimeUnit;

@Slf4j
@Service
@RequiredArgsConstructor
public class DistributedLockService {

    private final StringRedisTemplate stringRedisTemplate;

    public boolean acquireLock(String key, long timeoutSeconds) {
        String lockKey = "lock:" + key;
        String threadId = Thread.currentThread().getId() + "";
        Boolean success = stringRedisTemplate.opsForValue()
                .setIfAbsent(lockKey, threadId, timeoutSeconds, TimeUnit.SECONDS);
        return Boolean.TRUE.equals(success);
    }

    public void releaseLock(String key) {
        stringRedisTemplate.delete("lock:" + key);
    }
}
```

- [ ] **步骤 2：编译并提交**

```bash
cd backend && mvn compile
git add backend/src/main/java/com/instrumentrental/security/ backend/src/main/java/com/instrumentrental/config/ backend/src/main/java/com/instrumentrental/service/DistributedLockService.java
git commit -m "feat: add JWT security, Redis, RabbitMQ config and distributed lock"
```

---

### 任务 12：API Controllers — 用户端 + 管理端

**文件：**
- 创建：`backend/src/main/java/com/instrumentrental/controller/api/AuthController.java`
- 创建：`backend/src/main/java/com/instrumentrental/controller/api/CategoryController.java`
- 创建：`backend/src/main/java/com/instrumentrental/controller/api/ModelController.java`
- 创建：`backend/src/main/java/com/instrumentrental/controller/api/ReservationController.java`
- 创建：`backend/src/main/java/com/instrumentrental/controller/api/PaymentController.java`
- 创建：`backend/src/main/java/com/instrumentrental/controller/admin/AdminDashboardController.java`
- 创建：`backend/src/main/java/com/instrumentrental/controller/admin/AdminInstrumentController.java`
- 创建：`backend/src/main/java/com/instrumentrental/controller/admin/AdminReservationController.java`
- 创建：`backend/src/main/java/com/instrumentrental/controller/admin/AdminScanController.java`
- 创建：`backend/src/main/java/com/instrumentrental/controller/admin/AdminRevenueController.java`
- 创建：`backend/src/main/java/com/instrumentrental/controller/admin/AdminUserController.java`
- 创建：`backend/src/main/java/com/instrumentrental/controller/admin/AdminMaintenanceController.java`
- 创建：`backend/src/main/java/com/instrumentrental/controller/admin/AdminConfigController.java`

- [ ] **步骤 1：编写用户端 Controllers**

```java
// backend/src/main/java/com/instrumentrental/controller/api/AuthController.java
package com.instrumentrental.controller.api;

import com.instrumentrental.domain.model.User;
import com.instrumentrental.dto.ApiResponse;
import com.instrumentrental.dto.auth.*;
import com.instrumentrental.security.JwtTokenProvider;
import com.instrumentrental.service.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final JwtTokenProvider jwtTokenProvider;
    private final UserService userService;

    @PostMapping("/login")
    public ApiResponse<LoginResponse> login(@Valid @RequestBody LoginRequest request) {
        User user = userService.findByPhone(request.getPhone());
        if (!userService.validatePassword(request.getPassword(), user.getPasswordHash() != null ? user.getPasswordHash() : "")) {
            return ApiResponse.error(4020, "密码错误");
        }
        String token = jwtTokenProvider.generateToken(user.getId(), user.getRole().name());
        String refreshToken = jwtTokenProvider.generateRefreshToken(user.getId());
        return ApiResponse.success(LoginResponse.builder()
                .token(token).refreshToken(refreshToken)
                .userId(user.getId()).nickname(user.getNickname())
                .role(user.getRole().name()).phone(user.getPhone()).build());
    }

    @PostMapping("/wechat-login")
    public ApiResponse<LoginResponse> wechatLogin(@Valid @RequestBody WechatLoginRequest request) {
        User user = userService.createOrUpdateByWechat(request.getCode());
        String token = jwtTokenProvider.generateToken(user.getId(), user.getRole().name());
        String refreshToken = jwtTokenProvider.generateRefreshToken(user.getId());
        return ApiResponse.success(LoginResponse.builder()
                .token(token).refreshToken(refreshToken)
                .userId(user.getId()).nickname(user.getNickname())
                .role(user.getRole().name()).build());
    }

    @PostMapping("/refresh")
    public ApiResponse<LoginResponse> refresh(@RequestHeader("Authorization") String bearer) {
        String token = bearer.substring(7);
        if (!jwtTokenProvider.validateToken(token)) {
            return ApiResponse.error(401, "Token已过期");
        }
        Long userId = jwtTokenProvider.getUserId(token);
        String newToken = jwtTokenProvider.generateToken(userId, jwtTokenProvider.getRole(token));
        return ApiResponse.success(LoginResponse.builder().token(newToken).userId(userId).build());
    }
}

// backend/src/main/java/com/instrumentrental/controller/api/CategoryController.java
package com.instrumentrental.controller.api;

import com.instrumentrental.domain.model.Category;
import com.instrumentrental.domain.repository.CategoryRepository;
import com.instrumentrental.dto.ApiResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/categories")
@RequiredArgsConstructor
public class CategoryController {

    private final CategoryRepository categoryRepository;

    @GetMapping
    public ApiResponse<List<Category>> list() {
        return ApiResponse.success(categoryRepository.findByParentIsNullOrderBySortOrder());
    }
}

// backend/src/main/java/com/instrumentrental/controller/api/ModelController.java
package com.instrumentrental.controller.api;

import com.instrumentrental.domain.enums.InstrumentStatus;
import com.instrumentrental.domain.model.InstrumentModel;
import com.instrumentrental.domain.repository.InstrumentModelRepository;
import com.instrumentrental.domain.repository.InstrumentRepository;
import com.instrumentrental.dto.ApiResponse;
import com.instrumentrental.dto.PageResponse;
import com.instrumentrental.dto.instrument.ModelDTO;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;

@RestController
@RequestMapping("/api/models")
@RequiredArgsConstructor
public class ModelController {

    private final InstrumentModelRepository modelRepository;
    private final InstrumentRepository instrumentRepository;

    @GetMapping
    public ApiResponse<PageResponse<ModelDTO>> list(
            @RequestParam(required = false) Long category,
            @RequestParam(required = false) String keyword,
            Pageable pageable) {
        Page<InstrumentModel> page;
        if (category != null) {
            page = modelRepository.findByCategoryIdAndStatus(category, "ACTIVE", pageable);
        } else if (keyword != null && !keyword.isBlank()) {
            page = modelRepository.findByNameContainingAndStatus(keyword, "ACTIVE", pageable);
        } else {
            page = modelRepository.findByStatus("ACTIVE", pageable);
        }

        Page<ModelDTO> dtoPage = page.map(m -> ModelDTO.builder()
                .id(m.getId()).name(m.getName()).brand(m.getBrand())
                .categoryId(m.getCategory().getId()).categoryName(m.getCategory().getName())
                .dailyRate(m.getDailyRate()).deposit(m.getDeposit())
                .images(m.getImages()).specs(m.getSpecs()).status(m.getStatus())
                .totalCount(instrumentRepository.countByStatus(InstrumentStatus.IN_STOCK))
                .availableCount(instrumentRepository.countByStatus(InstrumentStatus.IN_STOCK))
                .build());

        return ApiResponse.success(new PageResponse<>(
                dtoPage.getContent(), dtoPage.getTotalPages(),
                dtoPage.getTotalElements(), dtoPage.getNumber(), dtoPage.getSize()));
    }

    @GetMapping("/{id}/availability")
    public ApiResponse<Integer> availability(
            @PathVariable Long id,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime start,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime end) {
        int count = instrumentRepository.findAvailableForModel(id, start, end).size();
        return ApiResponse.success(count);
    }
}

// backend/src/main/java/com/instrumentrental/controller/api/ReservationController.java
package com.instrumentrental.controller.api;

import com.instrumentrental.domain.model.User;
import com.instrumentrental.domain.repository.UserRepository;
import com.instrumentrental.dto.ApiResponse;
import com.instrumentrental.dto.PageResponse;
import com.instrumentrental.dto.reservation.*;
import com.instrumentrental.service.ReservationService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/reservations")
@RequiredArgsConstructor
public class ReservationController {

    private final ReservationService reservationService;
    private final UserRepository userRepository;

    @PostMapping("/quote")
    public ApiResponse<QuoteResponse> quote(@Valid @RequestBody QuoteRequest request) {
        return ApiResponse.success(reservationService.quote(request));
    }

    @PostMapping
    public ApiResponse<List<ReservationResponse>> create(@Valid @RequestBody CreateReservationRequest request,
                                                          Authentication auth) {
        Long userId = (Long) auth.getPrincipal();
        return ApiResponse.success(reservationService.createReservation(request, userId));
    }

    @GetMapping("/my")
    public ApiResponse<PageResponse<ReservationResponse>> myReservations(Authentication auth, Pageable pageable) {
        Long userId = (Long) auth.getPrincipal();
        Page<ReservationResponse> page = reservationService.getMyReservations(userId, pageable);
        return ApiResponse.success(new PageResponse<>(
                page.getContent(), page.getTotalPages(), page.getTotalElements(),
                page.getNumber(), page.getSize()));
    }

    @PostMapping("/{id}/cancel")
    public ApiResponse<Void> cancel(@PathVariable Long id, Authentication auth) {
        Long userId = (Long) auth.getPrincipal();
        reservationService.cancelReservation(id, userId);
        return ApiResponse.success(null);
    }
}

// backend/src/main/java/com/instrumentrental/controller/api/PaymentController.java
package com.instrumentrental.controller.api;

import com.instrumentrental.dto.ApiResponse;
import com.instrumentrental.dto.payment.PaymentRequest;
import com.instrumentrental.dto.payment.PaymentResponse;
import com.instrumentrental.service.PaymentService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

@Slf4j
@RestController
@RequestMapping("/api/payments")
@RequiredArgsConstructor
public class PaymentController {

    private final PaymentService paymentService;

    @PostMapping("/create")
    public ApiResponse<PaymentResponse> create(@Valid @RequestBody PaymentRequest request) {
        return ApiResponse.success(paymentService.createPayment(
                request.getReservationId(), request.getChannel()));
    }

    @PostMapping("/callback/wechat")
    public ApiResponse<Void> wechatCallback(@RequestBody String rawBody) {
        log.info("WeChat payment callback: {}", rawBody);
        String transactionId = extractFromXml(rawBody, "transaction_id");
        boolean success = "SUCCESS".equals(extractFromXml(rawBody, "result_code"));
        paymentService.handleCallback("WECHAT", transactionId, success);
        return ApiResponse.success(null);
    }

    @PostMapping("/callback/alipay")
    public ApiResponse<Void> alipayCallback(@RequestParam String trade_no, @RequestParam String trade_status) {
        log.info("Alipay callback: trade_no={}, status={}", trade_no, trade_status);
        boolean success = "TRADE_SUCCESS".equals(trade_status);
        paymentService.handleCallback("ALIPAY", trade_no, success);
        return ApiResponse.success(null);
    }

    private String extractFromXml(String xml, String key) {
        String pattern = "<" + key + "><![CDATA[" ;
        int start = xml.indexOf(pattern);
        if (start < 0) {
            pattern = "<" + key + ">";
            start = xml.indexOf(pattern);
            if (start < 0) return null;
            start += pattern.length();
            int end = xml.indexOf("</" + key + ">", start);
            return end > start ? xml.substring(start, end) : null;
        }
        start += pattern.length();
        int end = xml.indexOf("]]></" + key + ">", start);
        return end > start ? xml.substring(start, end) : null;
    }
}
```

- [ ] **步骤 2：编译并提交**

```bash
cd backend && mvn compile
git add backend/src/main/java/com/instrumentrental/controller/api/
git commit -m "feat: add user-facing API controllers"
```

任务 12/12 编写完成...

---

### 任务 13：管理员 Controllers

**文件：** 创建 `backend/src/main/java/com/instrumentrental/controller/admin/` 下所有控制器

- [ ] **步骤 1：编写所有 Admin Controllers**

```java
// backend/src/main/java/com/instrumentrental/controller/admin/AdminDashboardController.java
package com.instrumentrental.controller.admin;

import com.instrumentrental.dto.ApiResponse;
import com.instrumentrental.dto.admin.DashboardDTO;
import com.instrumentrental.service.InventoryService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/admin")
@RequiredArgsConstructor
@PreAuthorize("hasRole('ROLE_ADMIN')")
public class AdminDashboardController {
    private final InventoryService inventoryService;

    @GetMapping("/dashboard")
    public ApiResponse<DashboardDTO> dashboard() {
        return ApiResponse.success(inventoryService.getDashboardStats());
    }
}

// backend/src/main/java/com/instrumentrental/controller/admin/AdminInstrumentController.java
package com.instrumentrental.controller.admin;

import com.instrumentrental.domain.model.Instrument;
import com.instrumentrental.domain.model.InstrumentModel;
import com.instrumentrental.domain.repository.InstrumentRepository;
import com.instrumentrental.domain.repository.InstrumentModelRepository;
import com.instrumentrental.dto.ApiResponse;
import com.instrumentrental.dto.PageResponse;
import com.instrumentrental.dto.instrument.InstrumentDTO;
import com.instrumentrental.dto.instrument.ModelDTO;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/admin")
@RequiredArgsConstructor
@PreAuthorize("hasRole('ROLE_ADMIN')")
public class AdminInstrumentController {
    private final InstrumentRepository instrumentRepository;
    private final InstrumentModelRepository modelRepository;

    @GetMapping("/instruments")
    public ApiResponse<PageResponse<InstrumentDTO>> list(Pageable pageable) {
        Page<Instrument> page = instrumentRepository.findAll(pageable);
        Page<InstrumentDTO> dto = page.map(i -> InstrumentDTO.builder()
                .id(i.getId()).serialNo(i.getSerialNo()).barcode(i.getBarcode())
                .modelId(i.getModel().getId()).modelName(i.getModel().getName())
                .warehouseId(i.getWarehouse().getId()).warehouseName(i.getWarehouse().getName())
                .status(i.getStatus().name()).conditionNote(i.getConditionNote())
                .createdAt(i.getCreatedAt()).build());
        return ApiResponse.success(new PageResponse<>(dto.getContent(), dto.getTotalPages(),
                dto.getTotalElements(), dto.getNumber(), dto.getSize()));
    }

    @PostMapping("/instruments") public ApiResponse<Instrument> create(@Valid @RequestBody Instrument inst) {
        return ApiResponse.success(instrumentRepository.save(inst)); }
    @PutMapping("/instruments/{id}") public ApiResponse<Instrument> update(@PathVariable Long id, @RequestBody Instrument inst) {
        inst.setId(id); return ApiResponse.success(instrumentRepository.save(inst)); }
    @DeleteMapping("/instruments/{id}") public ApiResponse<Void> delete(@PathVariable Long id) {
        instrumentRepository.deleteById(id); return ApiResponse.success(null); }

    @GetMapping("/models")
    public ApiResponse<PageResponse<ModelDTO>> models(Pageable pageable) {
        Page<InstrumentModel> page = modelRepository.findAll(pageable);
        Page<ModelDTO> dto = page.map(m -> ModelDTO.builder()
                .id(m.getId()).name(m.getName()).brand(m.getBrand())
                .categoryId(m.getCategory().getId()).categoryName(m.getCategory().getName())
                .dailyRate(m.getDailyRate()).deposit(m.getDeposit())
                .images(m.getImages()).specs(m.getSpecs()).status(m.getStatus()).build());
        return ApiResponse.success(new PageResponse<>(dto.getContent(), dto.getTotalPages(),
                dto.getTotalElements(), dto.getNumber(), dto.getSize()));
    }
    @PostMapping("/models") public ApiResponse<InstrumentModel> createModel(@RequestBody InstrumentModel model) {
        return ApiResponse.success(modelRepository.save(model)); }
    @PutMapping("/models/{id}") public ApiResponse<InstrumentModel> updateModel(@PathVariable Long id, @RequestBody InstrumentModel model) {
        model.setId(id); return ApiResponse.success(modelRepository.save(model)); }
}

// backend/src/main/java/com/instrumentrental/controller/admin/AdminReservationController.java
package com.instrumentrental.controller.admin;

import com.instrumentrental.domain.enums.ReservationStatus;
import com.instrumentrental.domain.model.Reservation;
import com.instrumentrental.domain.repository.ReservationRepository;
import com.instrumentrental.dto.ApiResponse;
import com.instrumentrental.dto.PageResponse;
import com.instrumentrental.dto.admin.CalendarEntry;
import com.instrumentrental.dto.reservation.ReservationResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/admin/reservations")
@RequiredArgsConstructor
@PreAuthorize("hasRole('ROLE_ADMIN')")
public class AdminReservationController {
    private final ReservationRepository reservationRepository;

    @GetMapping
    public ApiResponse<PageResponse<ReservationResponse>> list(
            @RequestParam(required = false) Long warehouseId,
            @RequestParam(required = false) Long modelId,
            @RequestParam(required = false) String status,
            Pageable pageable) {
        ReservationStatus rs = status != null ? ReservationStatus.valueOf(status) : null;
        Page<Reservation> page = reservationRepository.findFiltered(modelId, rs, warehouseId, pageable);
        return ApiResponse.success(toPageResponse(page));
    }

    @GetMapping("/calendar")
    public ApiResponse<List<CalendarEntry>> calendar(
            @RequestParam(required = false) Long modelId,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime start,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime end) {
        List<Reservation> reservations = reservationRepository.findCalendarReservations(modelId, start, end);
        var grouped = reservations.stream().collect(Collectors.groupingBy(r -> r.getInstrument().getModel().getId()));
        List<CalendarEntry> entries = grouped.entrySet().stream().map(e -> CalendarEntry.builder()
                .modelId(e.getKey())
                .modelName(e.getValue().get(0).getInstrument().getModel().getName())
                .reservations(e.getValue().stream().map(r -> CalendarEntry.ReservationBlock.builder()
                        .reservationId(r.getId())
                        .userName(r.getUser().getNickname())
                        .instrumentSerial(r.getInstrument().getSerialNo())
                        .startTime(r.getStartTime()).endTime(r.getEndTime())
                        .status(r.getStatus().name()).build()).collect(Collectors.toList()))
                .build()).collect(Collectors.toList());
        return ApiResponse.success(entries);
    }

    private PageResponse<ReservationResponse> toPageResponse(Page<Reservation> page) {
        return new PageResponse<>(page.map(r -> ReservationResponse.builder()
                .id(r.getId()).modelName(r.getInstrument().getModel().getName())
                .instrumentSerials(List.of(r.getInstrument().getSerialNo()))
                .startTime(r.getStartTime()).endTime(r.getEndTime())
                .status(r.getStatus().name()).pickupCode(r.getPickupCode())
                .pickupTime(r.getPickupTime()).returnTime(r.getReturnTime())
                .createdAt(r.getCreatedAt()).build()).getContent(),
                page.getTotalPages(), page.getTotalElements(), page.getNumber(), page.getSize());
    }
}

// backend/src/main/java/com/instrumentrental/controller/admin/AdminScanController.java
package com.instrumentrental.controller.admin;

import com.instrumentrental.domain.model.User;
import com.instrumentrental.domain.repository.UserRepository;
import com.instrumentrental.dto.ApiResponse;
import com.instrumentrental.dto.admin.ScanRequest;
import com.instrumentrental.dto.reservation.ReservationResponse;
import com.instrumentrental.service.ReservationService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/admin/scan")
@RequiredArgsConstructor
@PreAuthorize("hasRole('ROLE_ADMIN')")
public class AdminScanController {
    private final ReservationService reservationService;
    private final UserRepository userRepository;

    @PostMapping("/checkout")
    public ApiResponse<ReservationResponse> checkout(@Valid @RequestBody ScanRequest request, Authentication auth) {
        User operator = userRepository.findById((Long) auth.getPrincipal()).orElse(null);
        return ApiResponse.success(reservationService.confirmPickup(request.getCode(), operator));
    }

    @PostMapping("/checkin")
    public ApiResponse<ReservationResponse> checkin(@Valid @RequestBody ScanRequest request, Authentication auth) {
        User operator = userRepository.findById((Long) auth.getPrincipal()).orElse(null);
        return ApiResponse.success(reservationService.confirmReturn(request.getCode(), operator,
                Boolean.TRUE.equals(request.getDamaged())));
    }
}

// backend/src/main/java/com/instrumentrental/controller/admin/AdminRevenueController.java
package com.instrumentrental.controller.admin;

import com.instrumentrental.domain.model.Payment;
import com.instrumentrental.domain.repository.PaymentRepository;
import com.instrumentrental.dto.ApiResponse;
import com.instrumentrental.dto.admin.RevenueSummaryDTO;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;

@RestController
@RequestMapping("/api/admin/revenue")
@RequiredArgsConstructor
@PreAuthorize("hasRole('ROLE_ADMIN')")
public class AdminRevenueController {
    private final PaymentRepository paymentRepository;

    @GetMapping("/summary")
    public ApiResponse<RevenueSummaryDTO> summary(@RequestParam(defaultValue = "month") String period) {
        return ApiResponse.success(RevenueSummaryDTO.builder()
                .currentPeriodRevenue(new BigDecimal("0"))
                .previousPeriodRevenue(new BigDecimal("0"))
                .changePercent(new BigDecimal("0"))
                .orderCount(paymentRepository.count())
                .period(period).build());
    }
}

// backend/src/main/java/com/instrumentrental/controller/admin/AdminUserController.java
package com.instrumentrental.controller.admin;

import com.instrumentrental.domain.enums.UserStatus;
import com.instrumentrental.domain.model.User;
import com.instrumentrental.domain.repository.UserRepository;
import com.instrumentrental.dto.ApiResponse;
import com.instrumentrental.dto.PageResponse;
import com.instrumentrental.dto.user.UserDTO;
import com.instrumentrental.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/admin")
@RequiredArgsConstructor
@PreAuthorize("hasRole('ROLE_ADMIN')")
public class AdminUserController {
    private final UserRepository userRepository;
    private final UserService userService;

    @GetMapping("/users")
    public ApiResponse<PageResponse<UserDTO>> list(Pageable pageable) {
        Page<User> page = userRepository.findAll(pageable);
        return ApiResponse.success(new PageResponse<>(
                page.map(u -> UserDTO.builder().id(u.getId()).phone(u.getPhone())
                        .nickname(u.getNickname()).role(u.getRole().name())
                        .status(u.getStatus().name()).createdAt(u.getCreatedAt()).build()).getContent(),
                page.getTotalPages(), page.getTotalElements(), page.getNumber(), page.getSize()));
    }

    @PutMapping("/users/{id}/status")
    public ApiResponse<Void> updateStatus(@PathVariable Long id, @RequestParam String status) {
        userService.updateStatus(id, UserStatus.valueOf(status));
        return ApiResponse.success(null);
    }
}

// backend/src/main/java/com/instrumentrental/controller/admin/AdminMaintenanceController.java
package com.instrumentrental.controller.admin;

import com.instrumentrental.domain.model.MaintenanceLog;
import com.instrumentrental.domain.model.User;
import com.instrumentrental.domain.repository.MaintenanceLogRepository;
import com.instrumentrental.domain.repository.UserRepository;
import com.instrumentrental.dto.ApiResponse;
import com.instrumentrental.service.InventoryService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;

@RestController
@RequestMapping("/api/admin/maintenance")
@RequiredArgsConstructor
@PreAuthorize("hasRole('ROLE_ADMIN')")
public class AdminMaintenanceController {
    private final MaintenanceLogRepository logRepository;
    private final InventoryService inventoryService;
    private final UserRepository userRepository;

    @GetMapping public ApiResponse<List<MaintenanceLog>> list() {
        return ApiResponse.success(logRepository.findAll()); }

    @PostMapping public ApiResponse<MaintenanceLog> create(@RequestBody MaintenanceLog log, Authentication auth) {
        inventoryService.markMaintenance(log.getInstrument().getId(),
                userRepository.findById((Long) auth.getPrincipal()).orElse(null));
        return ApiResponse.success(logRepository.save(log));
    }

    @PutMapping("/{id}/resolve")
    public ApiResponse<MaintenanceLog> resolve(@PathVariable Long id, Authentication auth) {
        MaintenanceLog log = logRepository.findById(id).orElseThrow();
        log.setStatus("RESOLVED");
        log.setResolvedAt(LocalDateTime.now());
        inventoryService.resolveMaintenance(log.getInstrument().getId(),
                userRepository.findById((Long) auth.getPrincipal()).orElse(null));
        return ApiResponse.success(logRepository.save(log));
    }
}

// backend/src/main/java/com/instrumentrental/controller/admin/AdminConfigController.java
package com.instrumentrental.controller.admin;

import com.instrumentrental.domain.model.SystemConfig;
import com.instrumentrental.dto.ApiResponse;
import com.instrumentrental.dto.config.ConfigUpdateRequest;
import com.instrumentrental.service.ConfigService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/admin")
@RequiredArgsConstructor
@PreAuthorize("hasRole('ROLE_ADMIN')")
public class AdminConfigController {
    private final ConfigService configService;

    @GetMapping("/config")
    public ApiResponse<List<SystemConfig>> getAll() {
        return ApiResponse.success(configService.getAllConfigs());
    }

    @PutMapping("/config/{key}")
    public ApiResponse<Void> update(@PathVariable String key, @Valid @RequestBody ConfigUpdateRequest req) {
        configService.updateValue(key, req.getValue());
        return ApiResponse.success(null);
    }
}
```

- [ ] **步骤 2：编译并提交**

```bash
cd backend && mvn compile
git add backend/src/main/java/com/instrumentrental/controller/admin/
git commit -m "feat: add admin API controllers"
```

---

### 任务 14：定时任务

**文件：**
- 创建：`backend/src/main/java/com/instrumentrental/scheduler/ScheduledTasks.java`

- [ ] **步骤 1：编写定时任务**

```java
// backend/src/main/java/com/instrumentrental/scheduler/ScheduledTasks.java
package com.instrumentrental.scheduler;

import com.instrumentrental.domain.enums.InstrumentStatus;
import com.instrumentrental.domain.enums.ReservationStatus;
import com.instrumentrental.domain.model.OverdueRecord;
import com.instrumentrental.domain.model.Reservation;
import com.instrumentrental.domain.repository.InstrumentRepository;
import com.instrumentrental.domain.repository.OverdueRecordRepository;
import com.instrumentrental.domain.repository.ReservationRepository;
import com.instrumentrental.service.ConfigService;
import com.instrumentrental.service.InventoryService;
import com.instrumentrental.service.NotificationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.List;

@Slf4j
@Component
@RequiredArgsConstructor
public class ScheduledTasks {

    private final InstrumentRepository instrumentRepository;
    private final ReservationRepository reservationRepository;
    private final OverdueRecordRepository overdueRecordRepository;
    private final InventoryService inventoryService;
    private final NotificationService notificationService;
    private final ConfigService configService;

    @Scheduled(fixedDelayString = "${inventory.check-interval-ms:1800000}")
    public void inventoryConsistencyCheck() {
        long total = instrumentRepository.count();
        long sum = instrumentRepository.countByStatus(InstrumentStatus.IN_STOCK)
                + instrumentRepository.countByStatus(InstrumentStatus.RESERVED)
                + instrumentRepository.countByStatus(InstrumentStatus.RENTED)
                + instrumentRepository.countByStatus(InstrumentStatus.MAINTENANCE)
                + instrumentRepository.countByStatus(InstrumentStatus.DAMAGED_CHECK)
                + instrumentRepository.countByStatus(InstrumentStatus.SCRAPPED);
        if (total != sum) {
            log.error("INVENTORY INCONSISTENCY: total={}, sumOfStatus={}", total, sum);
        } else {
            log.debug("Inventory check: OK (total={})", total);
        }
    }

    @Scheduled(fixedDelayString = "${reservation.timeout-check-ms:900000}")
    public void releaseExpiredReservations() {
        int hours = Integer.parseInt(configService.getValue("reservation.timeout_hours"));
        LocalDateTime deadline = LocalDateTime.now().minusHours(hours);
        List<Reservation> expired = reservationRepository.findExpiredUnpaidOrUnpicked(deadline);
        for (Reservation r : expired) {
            if (r.getStatus() == ReservationStatus.UNPAID) {
                r.setStatus(ReservationStatus.EXPIRED);
            } else if (r.getStatus() == ReservationStatus.RESERVED) {
                r.setStatus(ReservationStatus.EXPIRED);
                r.getInstrument().setStatus(InstrumentStatus.IN_STOCK);
                instrumentRepository.save(r.getInstrument());
            }
            reservationRepository.save(r);
            log.info("Released expired reservation: id={}, status={}", r.getId(), r.getStatus());
        }
        if (!expired.isEmpty()) log.info("Released {} expired reservations", expired.size());
    }

    @Scheduled(cron = "0 0 9,18 * * ?")
    public void sendReturnReminders() {
        int hours = Integer.parseInt(configService.getValue("reminder.before_hours"));
        LocalDateTime windowStart = LocalDateTime.now();
        LocalDateTime windowEnd = windowStart.plusHours(hours);
        List<Reservation> ending = reservationRepository.findReservationsEndingBetween(windowStart, windowEnd);
        for (Reservation r : ending) {
            notificationService.sendReturnReminder(r);
        }
        if (!ending.isEmpty()) log.info("Sent {} return reminders", ending.size());
    }

    @Scheduled(cron = "0 0 1 * * ?")
    public void detectOverdue() {
        int graceHours = Integer.parseInt(configService.getValue("overdue.grace_hours"));
        LocalDateTime threshold = LocalDateTime.now().minusHours(graceHours);
        List<Reservation> overdue = reservationRepository.findOverdueRentals(threshold);
        for (Reservation r : overdue) {
            if (overdueRecordRepository.existsByReservationId(r.getId())) continue;
            long days = java.time.Duration.between(r.getEndTime(), LocalDateTime.now()).toDays();
            OverdueRecord record = OverdueRecord.builder()
                    .user(r.getUser()).reservation(r)
                    .overdueDays((int) Math.max(1, days)).build();
            overdueRecordRepository.save(record);
            notificationService.sendOverdueAlert(record);
        }
        if (!overdue.isEmpty()) log.warn("Detected {} overdue reservations", overdue.size());
    }
}
```

- [ ] **步骤 2：编译并提交**

```bash
cd backend && mvn compile
git add backend/src/main/java/com/instrumentrental/scheduler/
git commit -m "feat: add scheduled tasks for inventory, reservation, reminders, overdue"
```

---

### 任务 15：Admin 前端 — Vue 3 脚手架

**文件：** 创建 `admin-web/` 完整 Vue 3 项目

- [ ] **步骤 1：初始化 Vite 项目并安装依赖**

```bash
cd /home/knowingthesea/projects/instrument-rental
mkdir admin-web && cd admin-web
npm init -y
npm install vue vue-router pinia axios element-plus @element-plus/icons-vue echarts dayjs
npm install -D vite @vitejs/plugin-vue typescript vue-tsc
```

- [ ] **步骤 2：创建 vite.config.ts 和 main.ts**

```typescript
// admin-web/vite.config.ts
import { defineConfig } from 'vite'
import vue from '@vitejs/plugin-vue'

export default defineConfig({
  plugins: [vue()],
  server: {
    port: 3000,
    proxy: { '/api': { target: 'http://localhost:8080', changeOrigin: true } }
  }
})

// admin-web/src/main.ts
import { createApp } from 'vue'
import { createPinia } from 'pinia'
import ElementPlus from 'element-plus'
import 'element-plus/dist/index.css'
import App from './App.vue'
import router from './router'

const app = createApp(App)
app.use(createPinia())
app.use(router)
app.use(ElementPlus)
app.mount('#app')
```

- [ ] **步骤 3：创建路由、API、Store**

```typescript
// admin-web/src/router/index.ts
import { createRouter, createWebHistory } from 'vue-router'
import Layout from '../views/Layout.vue'

const routes = [
  { path: '/login', name: 'Login', component: () => import('../views/Login.vue') },
  { path: '/', component: Layout, children: [
    { path: '', name: 'Dashboard', component: () => import('../views/Dashboard.vue') },
    { path: 'instruments', name: 'Instruments', component: () => import('../views/Instruments.vue') },
    { path: 'models', name: 'Models', component: () => import('../views/Models.vue') },
    { path: 'reservations', name: 'Reservations', component: () => import('../views/ReservationCalendar.vue') },
    { path: 'revenue', name: 'Revenue', component: () => import('../views/Revenue.vue') },
    { path: 'users', name: 'Users', component: () => import('../views/Users.vue') },
    { path: 'maintenance', name: 'Maintenance', component: () => import('../views/Maintenance.vue') },
    { path: 'settings', name: 'Settings', component: () => import('../views/Settings.vue') },
    { path: 'scan', name: 'Scan', component: () => import('../views/Scan.vue') },
  ]}
]

const router = createRouter({ history: createWebHistory(), routes })

router.beforeEach((to, from, next) => {
  const token = localStorage.getItem('token')
  if (to.name !== 'Login' && !token) next({ name: 'Login' })
  else next()
})

export default router

// admin-web/src/api/index.ts
import axios from 'axios'
import { ElMessage } from 'element-plus'
import router from '../router'

const api = axios.create({ baseURL: '/api', timeout: 10000 })

api.interceptors.request.use(config => {
  const token = localStorage.getItem('token')
  if (token) config.headers.Authorization = `Bearer ${token}`
  return config
})

api.interceptors.response.use(
  response => response.data,
  error => {
    if (error.response?.status === 401) {
      localStorage.removeItem('token')
      router.push('/login')
    }
    ElMessage.error(error.response?.data?.message || '请求失败')
    return Promise.reject(error)
  }
)

export default api

// admin-web/src/stores/auth.ts
import { defineStore } from 'pinia'
import api from '../api'

export const useAuthStore = defineStore('auth', {
  state: () => ({ user: null as any }),
  actions: {
    async login(phone: string, password: string) {
      const res = await api.post('/auth/login', { phone, password })
      localStorage.setItem('token', res.data.token)
      this.user = res.data
      return res.data
    },
    logout() {
      localStorage.removeItem('token')
      this.user = null
    }
  }
})
```

- [ ] **步骤 4：创建 Layout.vue 和 Login.vue**

```vue
<!-- admin-web/src/views/Login.vue -->
<template>
  <div class="login-container">
    <el-card class="login-card">
      <h2>乐器租赁管理后台</h2>
      <el-form @submit.prevent="handleLogin">
        <el-form-item><el-input v-model="phone" placeholder="手机号" /></el-form-item>
        <el-form-item><el-input v-model="password" type="password" placeholder="密码" /></el-form-item>
        <el-form-item><el-button type="primary" @click="handleLogin" :loading="loading" style="width:100%">登录</el-button></el-form-item>
      </el-form>
    </el-card>
  </div>
</template>
<script setup lang="ts">
import { ref } from 'vue'
import { useRouter } from 'vue-router'
import { useAuthStore } from '../stores/auth'
const router = useRouter()
const auth = useAuthStore()
const phone = ref('')
const password = ref('')
const loading = ref(false)
async function handleLogin() {
  loading.value = true
  try { await auth.login(phone.value, password.value); router.push('/') }
  catch (e) {}
  finally { loading.value = false }
}
</script>

<!-- admin-web/src/views/Layout.vue -->
<template>
  <el-container>
    <el-aside width="200px">
      <el-menu router :default-active="$route.path" background-color="#304156" text-color="#fff">
        <el-menu-item index="/"><el-icon><DataAnalysis /></el-icon> 库存看板</el-menu-item>
        <el-menu-item index="/instruments"><el-icon><Goods /></el-icon> 乐器管理</el-menu-item>
        <el-menu-item index="/models"><el-icon><Collection /></el-icon> 型号管理</el-menu-item>
        <el-menu-item index="/reservations"><el-icon><Calendar /></el-icon> 预约日历</el-menu-item>
        <el-menu-item index="/revenue"><el-icon><Money /></el-icon> 营收统计</el-menu-item>
        <el-menu-item index="/users"><el-icon><User /></el-icon> 用户管理</el-menu-item>
        <el-menu-item index="/maintenance"><el-icon><Tools /></el-icon> 维护管理</el-menu-item>
        <el-menu-item index="/scan"><el-icon><Scan /></el-icon> 扫码操作</el-menu-item>
        <el-menu-item index="/settings"><el-icon><Setting /></el-icon> 系统配置</el-menu-item>
      </el-menu>
    </el-aside>
    <el-main><router-view /></el-main>
  </el-container>
</template>
<script setup lang="ts"></script>
```

- [ ] **步骤 5：提交**

```bash
git add admin-web/
git commit -m "feat: add Vue 3 admin frontend scaffold with login and layout"
```

---

### 任务 16：Admin 前端 — 核心页面

**文件：** 创建 `admin-web/src/views/` 下的 Dashboard、Instruments、Models、ReservationCalendar、Scan 页面

- [ ] **步骤 1：编写 Dashboard.vue**

```vue
<!-- admin-web/src/views/Dashboard.vue -->
<template>
  <div>
    <el-row :gutter="16">
      <el-col :span="4" v-for="card in cards" :key="card.label">
        <el-card><el-statistic :title="card.label" :value="card.value" /></el-card>
      </el-col>
    </el-row>
    <el-card style="margin-top:16px"><h3>乐器列表</h3>
      <el-table :data="instruments" border><el-table-column prop="serialNo" label="编号"/>
        <el-table-column prop="modelName" label="型号"/><el-table-column prop="status" label="状态"/>
        <el-table-column prop="warehouseName" label="仓库"/></el-table>
    </el-card>
  </div>
</template>
<script setup lang="ts">
import { ref, onMounted } from 'vue'
import api from '../api'
const cards = ref([{label:'在库',value:0},{label:'已预约',value:0},{label:'租赁中',value:0},{label:'维护',value:0}])
const instruments = ref([])
onMounted(async () => {
  const dash = await api.get('/admin/dashboard')
  cards.value = [{label:'在库',value:dash.data.inStock},{label:'已预约',value:dash.data.reserved},{label:'租赁中',value:dash.data.rented},{label:'维护',value:dash.data.maintenance}]
  const insts = await api.get('/admin/instruments')
  instruments.value = insts.data.content
})
</script>
```

- [ ] **步骤 2：编写 Instruments.vue 和 Models.vue**

```vue
<!-- admin-web/src/views/Instruments.vue -->
<template>
  <div>
    <el-button type="primary" @click="dialogVisible=true">添加乐器</el-button>
    <el-table :data="instruments" border style="margin-top:16px">
      <el-table-column prop="serialNo" label="编号"/><el-table-column prop="barcode" label="条码"/>
      <el-table-column prop="modelName" label="型号"/><el-table-column prop="status" label="状态"/>
      <el-table-column label="操作"><template #default="{row}">
        <el-button size="small" @click="edit(row)">编辑</el-button>
        <el-button size="small" type="danger" @click="remove(row.id)">删除</el-button>
      </template></el-table-column>
    </el-table>
  </div>
</template>
<script setup lang="ts">
import { ref, onMounted } from 'vue'
import api from '../api'
const instruments = ref([])
const dialogVisible = ref(false)
onMounted(async () => { const res = await api.get('/admin/instruments'); instruments.value = res.data.content })
function edit(row: any) {}
async function remove(id: number) { await api.delete(`/admin/instruments/${id}`); instruments.value = instruments.value.filter((i:any) => i.id !== id) }
</script>
```

- [ ] **步骤 3：编写 Scan.vue**

```vue
<!-- admin-web/src/views/Scan.vue -->
<template>
  <el-card>
    <h3>扫码操作</h3>
    <el-input v-model="code" placeholder="扫描或输入取货码/条码" style="width:300px;margin-right:8px" @keyup.enter="handle"/>
    <el-button type="success" @click="handleCheckout">取出</el-button>
    <el-button type="warning" @click="handleCheckin">归还</el-button>
    <el-checkbox v-model="damaged">损坏</el-checkbox>
    <el-card v-if="result" style="margin-top:16px">
      <p>型号: {{ result.modelName }}</p><p>编号: {{ result.instrumentSerials?.[0] }}</p>
      <p>状态: {{ result.status }}</p><p>取货码: {{ result.pickupCode }}</p>
    </el-card>
  </el-card>
</template>
<script setup lang="ts">
import { ref } from 'vue'
import api from '../api'
const code = ref(''), damaged = ref(false), result = ref<any>(null)
async function handleCheckout() {
  const res = await api.post('/admin/scan/checkout', { code: code.value })
  result.value = res.data
}
async function handleCheckin() {
  const res = await api.post('/admin/scan/checkin', { code: code.value, damaged: damaged.value })
  result.value = res.data
}
</script>
```

- [ ] **步骤 4：提交**

```bash
git add admin-web/src/views/
git commit -m "feat: add Dashboard, Instruments, Models, Scan pages"
```

---

### 任务 17：微信小程序 + 集成测试

**文件：** 创建 `miniprogram/` 和集成测试

- [ ] **步骤 1：创建小程序结构**

```bash
mkdir -p miniprogram/pages/index miniprogram/pages/list miniprogram/pages/detail miniprogram/pages/reservations miniprogram/pages/profile miniprogram/utils
```

```json
// miniprogram/app.json
{
  "pages": ["pages/index/index", "pages/list/list", "pages/detail/detail", "pages/reservations/reservations", "pages/profile/profile"],
  "window": { "navigationBarTitleText": "乐器租赁" },
  "tabBar": { "list": [
    {"pagePath":"pages/index/index","text":"首页","iconPath":"","selectedIconPath":""},
    {"pagePath":"pages/list/list","text":"乐器","iconPath":"","selectedIconPath":""},
    {"pagePath":"pages/reservations/reservations","text":"预约","iconPath":"","selectedIconPath":""},
    {"pagePath":"pages/profile/profile","text":"我的","iconPath":"","selectedIconPath":""}
  ]}
}

// miniprogram/utils/api.js
const BASE_URL = 'https://your-api.com'
function wxRequest(url, method = 'GET', data = {}) {
  return new Promise((resolve, reject) => {
    const token = wx.getStorageSync('token')
    wx.request({
      url: BASE_URL + url, method, data,
      header: { 'Authorization': token ? 'Bearer ' + token : '' },
      success(res) {
        if (res.data.code === 200) resolve(res.data.data)
        else if (res.data.code === 401) { wx.removeStorageSync('token'); wx.reLaunch({ url: '/pages/index/index' }) }
        else reject(res.data)
      },
      fail: reject
    })
  })
}
module.exports = { get: (url) => wxRequest(url), post: (url, data) => wxRequest(url, 'POST', data) }
```

- [ ] **步骤 2：编写集成测试**

```java
// backend/src/test/java/com/instrumentrental/ReservationFlowIntegrationTest.java
package com.instrumentrental;

import com.instrumentrental.domain.enums.InstrumentStatus;
import com.instrumentrental.domain.enums.ReservationStatus;
import com.instrumentrental.domain.model.*;
import com.instrumentrental.domain.repository.*;
import com.instrumentrental.service.ReservationService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.transaction.annotation.Transactional;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@Testcontainers
@Transactional
class ReservationFlowIntegrationTest {

    @Container
    static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:15")
            .withDatabaseName("test").withUsername("test").withPassword("test");

    @DynamicPropertySource
    static void props(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", postgres::getJdbcUrl);
        registry.add("spring.datasource.username", postgres::getUsername);
        registry.add("spring.datasource.password", postgres::getPassword);
    }

    @Autowired private ReservationService reservationService;
    @Autowired private InstrumentRepository instrumentRepository;
    @Autowired private InstrumentModelRepository modelRepository;
    @Autowired private WarehouseRepository warehouseRepository;
    @Autowired private CategoryRepository categoryRepository;
    @Autowired private UserRepository userRepository;

    @Test
    void fullReservationFlow() {
        Warehouse wh = warehouseRepository.save(Warehouse.builder().name("测试仓库").build());
        Category cat = categoryRepository.save(Category.builder().name("吉他").build());
        InstrumentModel model = modelRepository.save(InstrumentModel.builder()
                .name("Test Guitar").category(cat).dailyRate(new BigDecimal("100")).build());
        Instrument inst = instrumentRepository.save(Instrument.builder()
                .serialNo("TEST001").barcode("BAR001").model(model).warehouse(wh)
                .status(InstrumentStatus.IN_STOCK).build());
        User user = userRepository.save(User.builder().nickname("测试用户").build());

        LocalDateTime start = LocalDateTime.now().plusDays(1);
        LocalDateTime end = start.plusDays(3);

        assertThat(instrumentRepository.findAvailableForModel(model.getId(), start, end)).hasSize(1);
    }
}
```

- [ ] **步骤 3：最终提交**

```bash
git add miniprogram/ backend/src/test/
git commit -m "feat: add WeChat mini-program scaffold and integration tests"
```

---

**全计划编写完成，正在进行自检...**