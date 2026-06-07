package com.instrumentrental.service;

import java.util.EnumMap;
import java.util.Map;
import java.util.Set;

import org.springframework.stereotype.Service;

import com.instrumentrental.domain.enums.InstrumentStatus;
import com.instrumentrental.domain.model.Instrument;
import com.instrumentrental.domain.model.User;
import com.instrumentrental.exception.BusinessException;
import com.instrumentrental.exception.ErrorCode;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * 乐器状态机 — 控制 Instrument.status 在各枚举值之间的合法迁移。
 */
@Service
@Slf4j
@RequiredArgsConstructor
public class InstrumentStateMachine {

    private static final Map<InstrumentStatus, Set<InstrumentStatus>> transitions = new EnumMap<>(InstrumentStatus.class);

    static {
        transitions.put(InstrumentStatus.IN_STOCK, Set.of(InstrumentStatus.RESERVED, InstrumentStatus.MAINTENANCE, InstrumentStatus.SCRAPPED));
        transitions.put(InstrumentStatus.RESERVED, Set.of(InstrumentStatus.RENTED, InstrumentStatus.CANCELLED, InstrumentStatus.EXPIRED));
        transitions.put(InstrumentStatus.RENTED, Set.of(InstrumentStatus.IN_STOCK, InstrumentStatus.DAMAGED_CHECK));
        transitions.put(InstrumentStatus.DAMAGED_CHECK, Set.of(InstrumentStatus.IN_STOCK, InstrumentStatus.MAINTENANCE, InstrumentStatus.SCRAPPED));
        transitions.put(InstrumentStatus.MAINTENANCE, Set.of(InstrumentStatus.IN_STOCK, InstrumentStatus.SCRAPPED));
        transitions.put(InstrumentStatus.SCRAPPED, Set.of());
    }

    private final OperationLogService operationLogService;

    public boolean canTransition(InstrumentStatus from, InstrumentStatus to) {
        Set<InstrumentStatus> allowed = transitions.get(from);
        return allowed != null && allowed.contains(to);
    }

    public void validateTransition(InstrumentStatus from, InstrumentStatus to) {
        if (!canTransition(from, to)) {
            throw new BusinessException(ErrorCode.INVALID_STATE_TRANSITION);
        }
    }

    public void transition(Instrument instrument, InstrumentStatus target, User operator) {
        InstrumentStatus from = instrument.getStatus();
        validateTransition(from, target);
        instrument.setStatus(target);

        if (operator != null) {
            operationLogService.log(operator, "STATUS_" + target.name(), "Instrument", instrument.getId(), from.name() + " → " + target.name());
        }

        log.info("Instrument {} status transition: {} → {}", instrument.getId(), from, target);
    }
}