package com.instrumentrental.controller.admin;

import com.instrumentrental.domain.model.MaintenanceLog;
import com.instrumentrental.domain.model.User;
import com.instrumentrental.domain.repository.MaintenanceLogRepository;
import com.instrumentrental.domain.repository.UserRepository;
import com.instrumentrental.dto.ApiResponse;
import com.instrumentrental.exception.BusinessException;
import com.instrumentrental.exception.ErrorCode;
import com.instrumentrental.service.InventoryService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/admin/maintenance")
@RequiredArgsConstructor
@PreAuthorize("hasAuthority('ROLE_ADMIN')")
public class AdminMaintenanceController {

    private final MaintenanceLogRepository maintenanceLogRepository;
    private final InventoryService inventoryService;
    private final UserRepository userRepository;

    @GetMapping
    public ApiResponse<List<MaintenanceLog>> getAll() {
        return ApiResponse.success(maintenanceLogRepository.findAll());
    }

    @PostMapping
    public ApiResponse<MaintenanceLog> create(
            @RequestBody Map<String, Object> request,
            Authentication authentication) {
        Long operatorId = (Long) authentication.getPrincipal();
        User operator = userRepository.findById(operatorId)
                .orElseThrow(() -> new BusinessException(ErrorCode.USER_NOT_FOUND));

        Long instrumentId = Long.valueOf(request.get("instrumentId").toString());
        String issueDescription = request.get("issueDescription") != null
                ? request.get("issueDescription").toString() : "";

        inventoryService.markMaintenance(instrumentId, operator);

        MaintenanceLog log = MaintenanceLog.builder()
                .instrument(com.instrumentrental.domain.model.Instrument.builder().id(instrumentId).build())
                .issueDescription(issueDescription)
                .status("MAINTENANCE")
                .createdAt(LocalDateTime.now())
                .build();
        MaintenanceLog saved = maintenanceLogRepository.save(log);
        return ApiResponse.success(saved);
    }

    @PutMapping("/{id}/resolve")
    public ApiResponse<MaintenanceLog> resolve(
            @PathVariable Long id,
            Authentication authentication) {
        Long operatorId = (Long) authentication.getPrincipal();
        User operator = userRepository.findById(operatorId)
                .orElseThrow(() -> new BusinessException(ErrorCode.USER_NOT_FOUND));

        MaintenanceLog log = maintenanceLogRepository.findById(id)
                .orElseThrow(() -> new BusinessException(ErrorCode.MAINTENANCE_NOT_FOUND));

        inventoryService.resolveMaintenance(log.getInstrument().getId(), operator);

        log.setStatus("RESOLVED");
        log.setResolvedAt(LocalDateTime.now());
        MaintenanceLog saved = maintenanceLogRepository.save(log);
        return ApiResponse.success(saved);
    }
}