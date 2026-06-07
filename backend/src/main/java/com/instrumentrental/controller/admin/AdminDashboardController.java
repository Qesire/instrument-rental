package com.instrumentrental.controller.admin;

import com.instrumentrental.dto.ApiResponse;
import com.instrumentrental.dto.admin.DashboardDTO;
import com.instrumentrental.service.InventoryService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/admin/dashboard")
@RequiredArgsConstructor
@PreAuthorize("hasAuthority('ROLE_ADMIN')")
public class AdminDashboardController {

    private final InventoryService inventoryService;

    @GetMapping
    public ApiResponse<DashboardDTO> getDashboard() {
        return ApiResponse.success(inventoryService.getDashboardStats());
    }
}