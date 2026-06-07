package com.instrumentrental.service;

import com.instrumentrental.domain.model.SystemConfig;
import com.instrumentrental.domain.repository.SystemConfigRepository;
import com.instrumentrental.exception.BusinessException;
import com.instrumentrental.exception.ErrorCode;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Slf4j
@RequiredArgsConstructor
public class ConfigService {

    private final SystemConfigRepository systemConfigRepository;

    public String getConfigValue(String key, String defaultValue) {
        return systemConfigRepository.findByConfigKey(key)
                .map(config -> config.getConfigValue())
                .orElse(defaultValue);
    }

    /**
     * 获取所有系统配置。
     */
    public List<SystemConfig> getAllConfigs() {
        return systemConfigRepository.findAll();
    }

    /**
     * 更新指定配置项的值。
     */
    @Transactional
    public void updateValue(String key, String value) {
        SystemConfig config = systemConfigRepository.findByConfigKey(key)
                .orElseThrow(() -> new BusinessException(ErrorCode.CONFIG_NOT_FOUND));
        config.setConfigValue(value);
        systemConfigRepository.save(config);
        log.info("Config updated: key={}, value={}", key, value);
    }
}