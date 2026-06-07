package com.instrumentrental.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

/**
 * 分布式锁服务 — 占位实现（Task 11 用 Redis 替换）。
 */
@Service
@Slf4j
public class DistributedLockService {

    public boolean acquireLock(String key, long timeoutSeconds) {
        log.debug("DistributedLock acquired (noop): key={}, timeout={}s", key, timeoutSeconds);
        return true;
    }

    public void releaseLock(String key) {
        log.debug("DistributedLock released (noop): key={}", key);
    }
}