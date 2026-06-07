package com.instrumentrental.controller.api;

import com.instrumentrental.dto.ApiResponse;
import com.instrumentrental.dto.reservation.CreateReservationRequest;
import com.instrumentrental.dto.reservation.QuoteRequest;
import com.instrumentrental.dto.reservation.QuoteResponse;
import com.instrumentrental.dto.reservation.ReservationResponse;
import com.instrumentrental.service.ReservationService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/reservations")
@RequiredArgsConstructor
public class ReservationController {

    private final ReservationService reservationService;

    @PostMapping("/quote")
    public ApiResponse<QuoteResponse> quote(@Valid @RequestBody QuoteRequest request) {
        return ApiResponse.success(reservationService.quote(request));
    }

    @PostMapping
    public ApiResponse<List<ReservationResponse>> createReservation(
            @Valid @RequestBody CreateReservationRequest request,
            Authentication authentication) {
        Long userId = (Long) authentication.getPrincipal();
        return ApiResponse.success(reservationService.createReservation(request, userId));
    }

    @GetMapping("/my")
    public ApiResponse<Page<ReservationResponse>> getMyReservations(
            Authentication authentication,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        Long userId = (Long) authentication.getPrincipal();
        return ApiResponse.success(reservationService.getMyReservations(userId, PageRequest.of(page, size)));
    }

    @PostMapping("/{id}/cancel")
    public ApiResponse<Void> cancelReservation(
            @PathVariable Long id,
            Authentication authentication) {
        Long userId = (Long) authentication.getPrincipal();
        reservationService.cancelReservation(id, userId);
        return ApiResponse.success(null);
    }
}