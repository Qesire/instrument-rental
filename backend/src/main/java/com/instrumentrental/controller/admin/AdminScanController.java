package com.instrumentrental.controller.admin;

import com.instrumentrental.domain.model.User;
import com.instrumentrental.domain.repository.UserRepository;
import com.instrumentrental.dto.ApiResponse;
import com.instrumentrental.dto.admin.ScanRequest;
import com.instrumentrental.dto.reservation.ReservationResponse;
import com.instrumentrental.exception.BusinessException;
import com.instrumentrental.exception.ErrorCode;
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
    public ApiResponse<ReservationResponse> checkout(
            @Valid @RequestBody ScanRequest request,
            Authentication authentication) {
        Long operatorId = (Long) authentication.getPrincipal();
        User operator = userRepository.findById(operatorId)
                .orElseThrow(() -> new BusinessException(ErrorCode.USER_NOT_FOUND));
        return ApiResponse.success(reservationService.confirmPickup(request.getCode(), operator));
    }

    @PostMapping("/checkin")
    public ApiResponse<ReservationResponse> checkin(
            @Valid @RequestBody ScanRequest request,
            Authentication authentication) {
        Long operatorId = (Long) authentication.getPrincipal();
        User operator = userRepository.findById(operatorId)
                .orElseThrow(() -> new BusinessException(ErrorCode.USER_NOT_FOUND));
        boolean damaged = request.getDamaged() != null && request.getDamaged();
        return ApiResponse.success(reservationService.confirmReturn(request.getCode(), operator, damaged));
    }
}