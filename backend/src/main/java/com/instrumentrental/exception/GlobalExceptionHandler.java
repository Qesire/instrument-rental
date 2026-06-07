package com.instrumentrental.exception;

import com.instrumentrental.dto.ApiResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(BusinessException.class)
    public ResponseEntity<ApiResponse<Void>> handleBusinessException(BusinessException e) {
        HttpStatus status = mapHttpStatus(e.getCode());
        return ResponseEntity.status(status)
                .body(ApiResponse.error(e.getCode(), e.getMessage()));
    }

    @ExceptionHandler(AuthenticationException.class)
    public ResponseEntity<ApiResponse<Void>> handleAuthenticationException(AuthenticationException e) {
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                .body(ApiResponse.error(ErrorCode.UNAUTHORIZED.getCode(), e.getMessage()));
    }

    @ExceptionHandler(AccessDeniedException.class)
    public ResponseEntity<ApiResponse<Void>> handleAccessDeniedException(AccessDeniedException e) {
        return ResponseEntity.status(HttpStatus.FORBIDDEN)
                .body(ApiResponse.error(ErrorCode.FORBIDDEN.getCode(), e.getMessage()));
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ApiResponse<Void>> handleMethodArgumentNotValidException(MethodArgumentNotValidException e) {
        StringBuilder sb = new StringBuilder();
        e.getBindingResult().getFieldErrors().forEach(error ->
                sb.append(error.getField()).append(": ").append(error.getDefaultMessage()).append("; ")
        );
        String message = sb.toString().trim();
        if (message.endsWith(";")) {
            message = message.substring(0, message.length() - 1);
        }
        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(ApiResponse.error(ErrorCode.BAD_REQUEST.getCode(), message));
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiResponse<Void>> handleException(Exception e) {
        log.error("未捕获的异常", e);
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(ApiResponse.error(ErrorCode.INTERNAL_ERROR.getCode(), ErrorCode.INTERNAL_ERROR.getMessage()));
    }

    private HttpStatus mapHttpStatus(int code) {
        if (code == ErrorCode.SUCCESS.getCode()) {
            return HttpStatus.OK;
        }
        if (code == ErrorCode.BAD_REQUEST.getCode()) {
            return HttpStatus.BAD_REQUEST;
        }
        if (code == ErrorCode.UNAUTHORIZED.getCode()) {
            return HttpStatus.UNAUTHORIZED;
        }
        if (code == ErrorCode.FORBIDDEN.getCode()) {
            return HttpStatus.FORBIDDEN;
        }
        if (code == ErrorCode.NOT_FOUND.getCode()) {
            return HttpStatus.NOT_FOUND;
        }
        if (code == ErrorCode.CONFLICT.getCode()) {
            return HttpStatus.CONFLICT;
        }
        if (code == ErrorCode.INTERNAL_ERROR.getCode()) {
            return HttpStatus.INTERNAL_SERVER_ERROR;
        }
        // 业务错误码映射
        if (code == ErrorCode.INSUFFICIENT_STOCK.getCode()
                || code == ErrorCode.INSTRUMENT_NOT_AVAILABLE.getCode()
                || code == ErrorCode.INVALID_STATE_TRANSITION.getCode()
                || code == ErrorCode.RESERVATION_EXPIRED.getCode()
                || code == ErrorCode.PAYMENT_FAILED.getCode()
                || code == ErrorCode.REFUND_FAILED.getCode()
                || code == ErrorCode.SCAN_CODE_INVALID.getCode()) {
            return HttpStatus.BAD_REQUEST;
        }
        if (code == ErrorCode.RESERVATION_NOT_FOUND.getCode()
                || code == ErrorCode.PAYMENT_NOT_FOUND.getCode()
                || code == ErrorCode.USER_NOT_FOUND.getCode()
                || code == ErrorCode.CONFIG_NOT_FOUND.getCode()
                || code == ErrorCode.WAREHOUSE_NOT_FOUND.getCode()
                || code == ErrorCode.MODEL_NOT_FOUND.getCode()
                || code == ErrorCode.INSTRUMENT_NOT_FOUND.getCode()
                || code == ErrorCode.MAINTENANCE_NOT_FOUND.getCode()) {
            return HttpStatus.NOT_FOUND;
        }
        if (code == ErrorCode.DUPLICATE_PAYMENT.getCode()
                || code == ErrorCode.OVERDUE_ALREADY_RECORDED.getCode()
                || code == ErrorCode.LOCK_ACQUIRE_FAILED.getCode()) {
            return HttpStatus.CONFLICT;
        }
        if (code == ErrorCode.USER_BLACKLISTED.getCode()) {
            return HttpStatus.FORBIDDEN;
        }
        if (code == ErrorCode.PASSWORD_ERROR.getCode()) {
            return HttpStatus.UNAUTHORIZED;
        }
        return HttpStatus.INTERNAL_SERVER_ERROR;
    }
}