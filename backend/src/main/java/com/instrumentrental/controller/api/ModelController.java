package com.instrumentrental.controller.api;

import com.instrumentrental.domain.model.InstrumentModel;
import com.instrumentrental.domain.repository.InstrumentModelRepository;
import com.instrumentrental.domain.repository.InstrumentRepository;
import com.instrumentrental.dto.ApiResponse;
import com.instrumentrental.dto.PageResponse;
import com.instrumentrental.dto.instrument.ModelDTO;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/models")
@RequiredArgsConstructor
public class ModelController {

    private final InstrumentModelRepository instrumentModelRepository;
    private final InstrumentRepository instrumentRepository;

    @GetMapping
    public ApiResponse<PageResponse<ModelDTO>> getModels(
            @RequestParam(required = false) Long category,
            @RequestParam(required = false) String keyword,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {

        Page<InstrumentModel> modelPage;
        if (category != null) {
            modelPage = instrumentModelRepository.findByCategoryIdAndStatus(category, "ACTIVE", PageRequest.of(page, size));
        } else if (keyword != null && !keyword.isBlank()) {
            modelPage = instrumentModelRepository.findByNameContainingAndStatus(keyword, "ACTIVE", PageRequest.of(page, size));
        } else {
            modelPage = instrumentModelRepository.findByStatus("ACTIVE", PageRequest.of(page, size));
        }

        List<ModelDTO> dtos = modelPage.getContent().stream()
                .map(this::toDTO)
                .collect(Collectors.toList());

        PageResponse<ModelDTO> pageResponse = PageResponse.<ModelDTO>builder()
                .content(dtos)
                .totalPages(modelPage.getTotalPages())
                .totalElements(modelPage.getTotalElements())
                .page(modelPage.getNumber())
                .size(modelPage.getSize())
                .build();

        return ApiResponse.success(pageResponse);
    }

    @GetMapping("/{id}/availability")
    public ApiResponse<Integer> getAvailability(
            @PathVariable Long id,
            @RequestParam LocalDateTime start,
            @RequestParam LocalDateTime end) {

        int availableCount = instrumentRepository.findAvailableForModel(id, start, end).size();
        return ApiResponse.success(availableCount);
    }

    private ModelDTO toDTO(InstrumentModel model) {
        long totalCount = instrumentRepository.findByModelIdAndStatus(model.getId(), null).size();
        long availableCount = instrumentRepository.findByModelIdAndStatus(model.getId(),
                com.instrumentrental.domain.enums.InstrumentStatus.IN_STOCK).size();

        List<String> images = null;
        if (model.getImages() != null && !model.getImages().isBlank()) {
            try {
                images = Arrays.asList(model.getImages().split(","));
            } catch (Exception ignored) {
                images = Collections.emptyList();
            }
        }

        return ModelDTO.builder()
                .id(model.getId())
                .name(model.getName())
                .brand(model.getBrand())
                .categoryId(model.getCategory() != null ? model.getCategory().getId() : null)
                .categoryName(model.getCategory() != null ? model.getCategory().getName() : null)
                .dailyRate(model.getDailyRate())
                .deposit(model.getDeposit())
                .images(images)
                .specs(model.getSpecs())
                .status(model.getStatus())
                .availableCount(availableCount)
                .totalCount(totalCount)
                .build();
    }
}