package com.instrumentrental.domain.repository;

import com.instrumentrental.domain.model.MaintenanceLog;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface MaintenanceLogRepository extends JpaRepository<MaintenanceLog, Long> {

    List<MaintenanceLog> findByInstrumentIdOrderByCreatedAtDesc(Long instrumentId);
}