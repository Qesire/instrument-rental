package com.instrumentrental.controller.admin;

import com.instrumentrental.domain.model.Instrument;
import com.instrumentrental.domain.model.InstrumentModel;
import com.instrumentrental.domain.repository.InstrumentModelRepository;
import com.instrumentrental.domain.repository.InstrumentRepository;
import com.instrumentrental.dto.ApiResponse;
import com.instrumentrental.dto.PageResponse;
import com.instrumentrental.dto.instrument.InstrumentDTO;
import com.instrumentrental.dto.instrument.ModelDTO;
import com.instrumentrental.exception.BusinessException;
import com.instrumentrental.exception.ErrorCode;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/admin")
@RequiredArgsConstructor
@PreAuthorize("hasRole('ROLE_ADMIN')")
public class AdminInstrumentController {

    private final InstrumentRepository instrumentRepository;
    private final InstrumentModelRepository instrumentModelRepository;

    // ──────────────────────────────────────────────
    //  Instrument CRUD
    // ──────────────────────────────────────────────

    @GetMapping("/instruments")
    public ApiResponse<PageResponse<InstrumentDTO>> getInstruments(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        Page<Instrument> instrumentPage = instrumentRepository.findAll(PageRequest.of(page, size));
        List<InstrumentDTO> dtos = instrumentPage.getContent().stream()
                .map(this::toDTO)
                .collect(Collectors.toList());

        PageResponse<InstrumentDTO> response = PageResponse.<InstrumentDTO>builder()
                .content(dtos)
                .totalPages(instrumentPage.getTotalPages())
                .totalElements(instrumentPage.getTotalElements())
                .page(instrumentPage.getNumber())
                .size(instrumentPage.getSize())
                .build();

        return ApiResponse.success(response);
    }

    @PostMapping("/instruments")
    public ApiResponse<InstrumentDTO> createInstrument(@RequestBody Instrument instrument) {
        Instrument saved = instrumentRepository.save(instrument);
        return ApiResponse.success(toDTO(saved));
    }

    @PutMapping("/instruments/{id}")
    public ApiResponse<InstrumentDTO> updateInstrument(
            @PathVariable Long id,
            @RequestBody Instrument instrument) {
        if (!instrumentRepository.existsById(id)) {
            throw new BusinessException(ErrorCode.INSTRUMENT_NOT_FOUND);
        }
        instrument.setId(id);
        Instrument saved = instrumentRepository.save(instrument);
        return ApiResponse.success(toDTO(saved));
    }

    @DeleteMapping("/instruments/{id}")
    public ApiResponse<Void> deleteInstrument(@PathVariable Long id) {
        if (!instrumentRepository.existsById(id)) {
            throw new BusinessException(ErrorCode.INSTRUMENT_NOT_FOUND);
        }
        instrumentRepository.deleteById(id);
        return ApiResponse.success(null);
    }

    // ──────────────────────────────────────────────
    //  Model CRUD
    // ──────────────────────────────────────────────

    @GetMapping("/models")
    public ApiResponse<PageResponse<ModelDTO>> getModels(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        Page<InstrumentModel> modelPage = instrumentModelRepository.findAll(PageRequest.of(page, size));
        List<ModelDTO> dtos = modelPage.getContent().stream()
                .map(this::modelToDTO)
                .collect(Collectors.toList());

        PageResponse<ModelDTO> response = PageResponse.<ModelDTO>builder()
                .content(dtos)
                .totalPages(modelPage.getTotalPages())
                .totalElements(modelPage.getTotalElements())
                .page(modelPage.getNumber())
                .size(modelPage.getSize())
                .build();

        return ApiResponse.success(response);
    }

    @PostMapping("/models")
    public ApiResponse<ModelDTO> createModel(@RequestBody InstrumentModel model) {
        InstrumentModel saved = instrumentModelRepository.save(model);
        return ApiResponse.success(modelToDTO(saved));
    }

    @PutMapping("/models/{id}")
    public ApiResponse<ModelDTO> updateModel(
            @PathVariable Long id,
            @RequestBody InstrumentModel model) {
        if (!instrumentModelRepository.existsById(id)) {
            throw new BusinessException(ErrorCode.MODEL_NOT_FOUND);
        }
        model.setId(id);
        InstrumentModel saved = instrumentModelRepository.save(model);
        return ApiResponse.success(modelToDTO(saved));
    }

    // ──────────────────────────────────────────────
    //  Mappers
    // ──────────────────────────────────────────────

    private InstrumentDTO toDTO(Instrument i) {
        return InstrumentDTO.builder()
                .id(i.getId())
                .serialNo(i.getSerialNo())
                .barcode(i.getBarcode())
                .modelId(i.getModel() != null ? i.getModel().getId() : null)
                .modelName(i.getModel() != null ? i.getModel().getName() : null)
                .warehouseId(i.getWarehouse() != null ? i.getWarehouse().getId() : null)
                .warehouseName(i.getWarehouse() != null ? i.getWarehouse().getName() : null)
                .status(i.getStatus().name())
                .conditionNote(i.getConditionNote())
                .createdAt(i.getCreatedAt())
                .build();
    }

    private ModelDTO modelToDTO(InstrumentModel model) {
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
                .build();
    }
}