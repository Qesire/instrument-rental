package com.instrumentrental.domain.repository;

import com.instrumentrental.domain.model.OverdueRecord;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface OverdueRecordRepository extends JpaRepository<OverdueRecord, Long> {

    Page<OverdueRecord> findByUserIdOrderByCreatedAtDesc(Long userId, Pageable pageable);

    boolean existsByReservationId(Long reservationId);
}