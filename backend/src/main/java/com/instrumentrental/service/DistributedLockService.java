package com.instrumentrental.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.util.concurrent.TimeUnit;

@Service
@Slf4j
@RequiredArgsConstructor
public class DistributedLockService {

    private final StringRedisTemplate stringRedisTemplate;

    /**
     * 尝试获取分布式锁。
     *
     * @param key            锁的键
     * @param timeoutSeconds 锁的超时时间（秒）
     * @return true 表示获取成功，false 表示获取失败
     */
    public boolean acquireLock(String key, long timeoutSeconds) {
        String threadId = String.valueOf(Thread.currentThread().threadId());
        Boolean acquired = stringRedisTemplate.opsForValue()
                .setIfAbsent("lock:" + key, threadId, timeoutSeconds, TimeUnit.SECONDS);
        boolean result = Boolean.TRUE.equals(acquired);
        if (result) {
            log.debug("Lock acquired: key={}, threadId={}, timeout={}s", key, threadId, timeoutSeconds);
        } else {
            log.debug("Lock acquire failed: key={}", key);
        }
        return result;
    }

    /**
     * 释放分布式锁。
     *
     * @param key 锁的键
     */
    public void releaseLock(String key) {
        stringRedisTemplate.delete("lock:" + key);
        log.debug("Lock released: key={}", key);
    }
}