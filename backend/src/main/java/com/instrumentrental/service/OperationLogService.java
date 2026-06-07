package com.instrumentrental.service;

import com.instrumentrental.domain.model.OperationLog;
import com.instrumentrental.domain.model.User;
import com.instrumentrental.domain.repository.OperationLogRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Service
@Slf4j
@RequiredArgsConstructor
public class OperationLogService {

    private final OperationLogRepository operationLogRepository;

    public void log(User operator, String action, String targetType, Long targetId, String detail) {
        OperationLog operationLog = OperationLog.builder()
                .operator(operator)
                .action(action)
                .targetType(targetType)
                .targetId(targetId)
                .detail(detail)
                .build();
        operationLogRepository.save(operationLog);
    }
}