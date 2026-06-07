package com.instrumentrental.controller.admin;

import com.instrumentrental.domain.enums.ReservationStatus;
import com.instrumentrental.domain.model.Reservation;
import com.instrumentrental.domain.repository.ReservationRepository;
import com.instrumentrental.dto.ApiResponse;
import com.instrumentrental.dto.PageResponse;
import com.instrumentrental.dto.admin.CalendarEntry;
import com.instrumentrental.dto.reservation.ReservationResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/admin/reservations")
@RequiredArgsConstructor
@PreAuthorize("hasRole('ROLE_ADMIN')")
public class AdminReservationController {

    private final ReservationRepository reservationRepository;

    @GetMapping
    public ApiResponse<PageResponse<ReservationResponse>> getReservations(
            @RequestParam(required = false) Long warehouseId,
            @RequestParam(required = false) Long modelId,
            @RequestParam(required = false) String status,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {

        ReservationStatus reservationStatus = null;
        if (status != null && !status.isBlank()) {
            reservationStatus = ReservationStatus.valueOf(status);
        }

        Page<Reservation> reservationPage = reservationRepository.findFiltered(
                modelId, reservationStatus, warehouseId, PageRequest.of(page, size));

        List<ReservationResponse> responses = reservationPage.getContent().stream()
                .map(this::buildResponse)
                .collect(Collectors.toList());

        PageResponse<ReservationResponse> pageResponse = PageResponse.<ReservationResponse>builder()
                .content(responses)
                .totalPages(reservationPage.getTotalPages())
                .totalElements(reservationPage.getTotalElements())
                .page(reservationPage.getNumber())
                .size(reservationPage.getSize())
                .build();

        return ApiResponse.success(pageResponse);
    }

    @GetMapping("/calendar")
    public ApiResponse<List<CalendarEntry>> getCalendar(
            @RequestParam(required = false) Long modelId,
            @RequestParam LocalDateTime start,
            @RequestParam LocalDateTime end) {

        List<Reservation> reservations = reservationRepository.findCalendarReservations(modelId, start, end);

        Map<Long, List<Reservation>> groupedByModel = reservations.stream()
                .collect(Collectors.groupingBy(r -> r.getInstrument().getModel().getId()));

        List<CalendarEntry> entries = groupedByModel.entrySet().stream()
                .map(entry -> {
                    Long mId = entry.getKey();
                    List<Reservation> resList = entry.getValue();
                    String modelName = resList.get(0).getInstrument().getModel().getName();

                    List<CalendarEntry.ReservationBlock> blocks = resList.stream()
                            .map(r -> CalendarEntry.ReservationBlock.builder()
                                    .reservationId(r.getId())
                                    .userName(r.getUser().getNickname())
                                    .instrumentSerial(r.getInstrument().getSerialNo())
                                    .startTime(r.getStartTime())
                                    .endTime(r.getEndTime())
                                    .status(r.getStatus().name())
                                    .build())
                            .collect(Collectors.toList());

                    return CalendarEntry.builder()
                            .modelId(mId)
                            .modelName(modelName)
                            .reservations(blocks)
                            .build();
                })
                .collect(Collectors.toList());

        return ApiResponse.success(entries);
    }

    private ReservationResponse buildResponse(Reservation r) {
        BigDecimal totalAmount = BigDecimal.ZERO;
        BigDecimal deposit = BigDecimal.ZERO;

        if (r.getPriceDetail() != null && !r.getPriceDetail().isEmpty()) {
            totalAmount = parseJsonNumber(r.getPriceDetail(), "totalAmount");
            deposit = parseJsonNumber(r.getPriceDetail(), "deposit");
        }

        return ReservationResponse.builder()
                .id(r.getId())
                .modelName(r.getInstrument().getModel().getName())
                .brand(r.getInstrument().getModel().getBrand())
                .instrumentSerials(List.of(r.getInstrument().getSerialNo()))
                .startTime(r.getStartTime())
                .endTime(r.getEndTime())
                .status(r.getStatus().name())
                .totalAmount(totalAmount)
                .deposit(deposit)
                .pickupCode(r.getPickupCode())
                .pickupTime(r.getPickupTime())
                .returnTime(r.getReturnTime())
                .createdAt(r.getCreatedAt())
                .build();
    }

    private BigDecimal parseJsonNumber(String json, String key) {
        String search = "\"" + key + "\":";
        int start = json.indexOf(search);
        if (start < 0) return BigDecimal.ZERO;
        start += search.length();
        int end = start;
        while (end < json.length()) {
            char c = json.charAt(end);
            if (c == ',' || c == '}' || c == ' ') break;
            end++;
        }
        if (start >= end) return BigDecimal.ZERO;
        return new BigDecimal(json.substring(start, end).trim());
    }
}