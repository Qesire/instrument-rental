package com.instrumentrental.domain.repository;

import com.instrumentrental.domain.enums.InstrumentStatus;
import com.instrumentrental.domain.model.Instrument;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface InstrumentRepository extends JpaRepository<Instrument, Long> {

    Optional<Instrument> findByBarcode(String barcode);

    Optional<Instrument> findBySerialNo(String serialNo);

    List<Instrument> findByModelIdAndStatus(Long modelId, InstrumentStatus status);

    @Query("SELECT i FROM Instrument i WHERE i.model.id = :modelId AND i.status = 'IN_STOCK' AND i.id NOT IN (SELECT r.instrument.id FROM Reservation r WHERE r.status IN ('UNPAID', 'RESERVED', 'RENTED') AND r.startTime < :endTime AND r.endTime > :startTime) ORDER BY i.id")
    List<Instrument> findAvailableForModel(Long modelId, LocalDateTime startTime, LocalDateTime endTime);

    long countByStatus(InstrumentStatus status);

    long countByWarehouseIdAndStatus(Long warehouseId, InstrumentStatus status);

    @Query("SELECT i.warehouse.id, COUNT(i) FROM Instrument i GROUP BY i.warehouse.id")
    List<Object[]> countByWarehouseGrouped();
}