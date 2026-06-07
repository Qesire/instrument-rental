package com.instrumentrental.service;

import com.instrumentrental.domain.enums.InstrumentStatus;
import com.instrumentrental.domain.model.Instrument;
import com.instrumentrental.domain.model.User;
import com.instrumentrental.domain.repository.InstrumentRepository;
import com.instrumentrental.domain.repository.OperationLogRepository;
import com.instrumentrental.dto.admin.DashboardDTO;
import com.instrumentrental.exception.BusinessException;
import com.instrumentrental.exception.ErrorCode;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@Slf4j
@RequiredArgsConstructor
public class InventoryService {

    private final InstrumentRepository instrumentRepository;
    private final OperationLogService operationLogService;

    public int getAvailableCount(Long modelId, LocalDateTime start, LocalDateTime end) {
        return instrumentRepository.findAvailableForModel(modelId, start, end).size();
    }

    @Transactional
    public List<Instrument> lockInstruments(Long modelId, LocalDateTime start, LocalDateTime end, int count) {
        List<Instrument> available = instrumentRepository.findAvailableForModel(modelId, start, end);
        if (available.size() < count) {
            throw new BusinessException(ErrorCode.INSUFFICIENT_STOCK);
        }

        List<Instrument> selected = available.subList(0, count);
        selected.forEach(instrument -> instrument.setStatus(InstrumentStatus.RESERVED));
        instrumentRepository.saveAll(selected);

        log.info("Locked {} instruments for modelId={} from {} to {}", count, modelId, start, end);
        return selected;
    }

    @Transactional
    public void releaseInstruments(List<Long> instrumentIds) {
        List<Instrument> instruments = instrumentRepository.findAllById(instrumentIds);
        List<Instrument> toRelease = instruments.stream()
                .filter(i -> i.getStatus() == InstrumentStatus.RESERVED)
                .peek(i -> i.setStatus(InstrumentStatus.IN_STOCK))
                .collect(Collectors.toList());

        if (!toRelease.isEmpty()) {
            instrumentRepository.saveAll(toRelease);
            log.info("Released {} instruments: {}", toRelease.size(), instrumentIds);
        }
    }

    @Transactional
    public void markAsRented(Long instrumentId, User operator) {
        Instrument instrument = instrumentRepository.findById(instrumentId)
                .orElseThrow(() -> new BusinessException(ErrorCode.INSTRUMENT_NOT_FOUND));
        instrument.setStatus(InstrumentStatus.RENTED);
        instrumentRepository.save(instrument);
        operationLogService.log(operator, "MARK_RENTED", "Instrument", instrumentId, null);
        log.info("Instrument {} marked as RENTED by user {}", instrumentId, operator.getId());
    }

    @Transactional
    public void markAsReturned(Long instrumentId, User operator) {
        Instrument instrument = instrumentRepository.findById(instrumentId)
                .orElseThrow(() -> new BusinessException(ErrorCode.INSTRUMENT_NOT_FOUND));
        instrument.setStatus(InstrumentStatus.IN_STOCK);
        instrumentRepository.save(instrument);
        operationLogService.log(operator, "MARK_RETURNED", "Instrument", instrumentId, null);
        log.info("Instrument {} marked as IN_STOCK by user {}", instrumentId, operator.getId());
    }

    @Transactional
    public void markAsDamaged(Long instrumentId, User operator) {
        Instrument instrument = instrumentRepository.findById(instrumentId)
                .orElseThrow(() -> new BusinessException(ErrorCode.INSTRUMENT_NOT_FOUND));
        instrument.setStatus(InstrumentStatus.DAMAGED_CHECK);
        instrumentRepository.save(instrument);
        operationLogService.log(operator, "MARK_DAMAGED", "Instrument", instrumentId, null);
        log.info("Instrument {} marked as DAMAGED_CHECK by user {}", instrumentId, operator.getId());
    }

    @Transactional
    public void markMaintenance(Long instrumentId, User operator) {
        Instrument instrument = instrumentRepository.findById(instrumentId)
                .orElseThrow(() -> new BusinessException(ErrorCode.INSTRUMENT_NOT_FOUND));
        instrument.setStatus(InstrumentStatus.MAINTENANCE);
        instrumentRepository.save(instrument);
        operationLogService.log(operator, "MARK_MAINTENANCE", "Instrument", instrumentId, null);
        log.info("Instrument {} marked as MAINTENANCE by user {}", instrumentId, operator.getId());
    }

    @Transactional
    public void resolveMaintenance(Long instrumentId, User operator) {
        Instrument instrument = instrumentRepository.findById(instrumentId)
                .orElseThrow(() -> new BusinessException(ErrorCode.INSTRUMENT_NOT_FOUND));
        instrument.setStatus(InstrumentStatus.IN_STOCK);
        instrumentRepository.save(instrument);
        operationLogService.log(operator, "RESOLVE_MAINTENANCE", "Instrument", instrumentId, null);
        log.info("Instrument {} resolved from MAINTENANCE by user {}", instrumentId, operator.getId());
    }

    public DashboardDTO getDashboardStats() {
        long inStock = instrumentRepository.countByStatus(InstrumentStatus.IN_STOCK);
        long reserved = instrumentRepository.countByStatus(InstrumentStatus.RESERVED);
        long rented = instrumentRepository.countByStatus(InstrumentStatus.RENTED);
        long overdue = instrumentRepository.countByStatus(InstrumentStatus.DAMAGED_CHECK);
        long maintenance = instrumentRepository.countByStatus(InstrumentStatus.MAINTENANCE);
        long total = instrumentRepository.count();

        // 按仓库分组统计
        List<Object[]> warehouseCounts = instrumentRepository.countByWarehouseGrouped();
        Map<String, Long> byWarehouse = new HashMap<>();
        for (Object[] row : warehouseCounts) {
            byWarehouse.put("warehouse_" + row[0], (Long) row[1]);
        }

        return DashboardDTO.builder()
                .inStock(inStock)
                .reserved(reserved)
                .rented(rented)
                .overdue(overdue)
                .maintenance(maintenance)
                .total(total)
                .byWarehouse(byWarehouse)
                .build();
    }
}