package com.instrumentrental.exception;

import lombok.Getter;

@Getter
public enum ErrorCode {

    // 通用
    SUCCESS(200, "操作成功"),
    BAD_REQUEST(400, "请求参数错误"),
    UNAUTHORIZED(401, "未授权"),
    FORBIDDEN(403, "禁止访问"),
    NOT_FOUND(404, "资源不存在"),
    CONFLICT(409, "资源冲突"),
    INTERNAL_ERROR(500, "服务器内部错误"),

    // 业务
    INSUFFICIENT_STOCK(4001, "库存不足"),
    INSTRUMENT_NOT_AVAILABLE(4002, "乐器不可用"),
    INVALID_STATE_TRANSITION(4003, "无效的状态转换"),
    RESERVATION_NOT_FOUND(4004, "预约不存在"),
    RESERVATION_EXPIRED(4005, "预约已过期"),
    PAYMENT_FAILED(4006, "支付失败"),
    PAYMENT_NOT_FOUND(4007, "支付记录不存在"),
    DUPLICATE_PAYMENT(4008, "重复支付"),
    REFUND_FAILED(4009, "退款失败"),
    USER_NOT_FOUND(4010, "用户不存在"),
    USER_BLACKLISTED(4011, "用户已被拉黑"),
    CONFIG_NOT_FOUND(4012, "配置不存在"),
    WAREHOUSE_NOT_FOUND(4013, "仓库不存在"),
    MODEL_NOT_FOUND(4014, "乐器型号不存在"),
    INSTRUMENT_NOT_FOUND(4015, "乐器不存在"),
    SCAN_CODE_INVALID(4016, "扫描码无效"),
    MAINTENANCE_NOT_FOUND(4017, "维护记录不存在"),
    OVERDUE_ALREADY_RECORDED(4018, "逾期记录已存在"),
    LOCK_ACQUIRE_FAILED(4019, "获取锁失败"),
    PASSWORD_ERROR(4020, "密码错误");

    private final int code;
    private final String message;

    ErrorCode(int code, String message) {
        this.code = code;
        this.message = message;
    }

    ErrorCode(int code) {
        this.code = code;
        this.message = this.name();
    }
}