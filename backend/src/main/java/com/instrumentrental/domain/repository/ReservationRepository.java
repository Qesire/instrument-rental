package com.instrumentrental.domain.repository;

import com.instrumentrental.domain.enums.ReservationStatus;
import com.instrumentrental.domain.model.Reservation;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface ReservationRepository extends JpaRepository<Reservation, Long> {

    Page<Reservation> findByUserIdOrderByCreatedAtDesc(Long userId, Pageable pageable);

    Page<Reservation> findByStatus(ReservationStatus status, Pageable pageable);

    List<Reservation> findByStatusAndPickupCode(ReservationStatus status, String pickupCode);

    @Query("SELECT r FROM Reservation r WHERE r.status IN ('UNPAID', 'RESERVED') AND r.createdAt < :deadline")
    List<Reservation> findExpiredUnpaidOrUnpicked(LocalDateTime deadline);

    @Query("SELECT r FROM Reservation r WHERE r.status = 'RENTED' AND r.endTime BETWEEN :start AND :end")
    List<Reservation> findReservationsEndingBetween(LocalDateTime start, LocalDateTime end);

    @Query("SELECT r FROM Reservation r WHERE r.status = 'RENTED' AND r.endTime < :now")
    List<Reservation> findOverdueRentals(LocalDateTime now);

    @Query("SELECT r FROM Reservation r JOIN r.instrument i JOIN i.model m WHERE (:modelId IS NULL OR m.id = :modelId) AND (:status IS NULL OR r.status = :status) AND (:warehouseId IS NULL OR i.warehouse.id = :warehouseId) ORDER BY r.createdAt DESC")
    Page<Reservation> findFiltered(Long modelId, ReservationStatus status, Long warehouseId, Pageable pageable);

    @Query("SELECT r FROM Reservation r JOIN r.instrument i JOIN i.model m WHERE (:modelId IS NULL OR m.id = :modelId) AND r.startTime < :endDate AND r.endTime > :startDate AND r.status IN ('RESERVED', 'RENTED')")
    List<Reservation> findCalendarReservations(Long modelId, LocalDateTime startDate, LocalDateTime endDate);

    @Query("SELECT r FROM Reservation r WHERE r.status IN ('RESERVED', 'RENTED') AND r.startTime < :endTime AND r.endTime > :startTime AND r.instrument.id IN :instrumentIds")
    List<Reservation> findConflictingReservations(List<Long> instrumentIds, LocalDateTime startTime, LocalDateTime endTime);
}