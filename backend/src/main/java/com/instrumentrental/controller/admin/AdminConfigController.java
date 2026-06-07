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
@RequestMapping("/api/admin/config")
@RequiredArgsConstructor
@PreAuthorize("hasRole('ROLE_ADMIN')")
public class AdminConfigController {

    private final ConfigService configService;

    @GetMapping
    public ApiResponse<List<SystemConfig>> getAllConfigs() {
        return ApiResponse.success(configService.getAllConfigs());
    }

    @PutMapping("/{key}")
    public ApiResponse<Void> updateConfig(
            @PathVariable String key,
            @Valid @RequestBody ConfigUpdateRequest request) {
        configService.updateValue(key, request.getValue());
        return ApiResponse.success(null);
    }
}