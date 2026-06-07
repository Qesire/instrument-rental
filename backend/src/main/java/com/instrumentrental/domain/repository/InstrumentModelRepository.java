package com.instrumentrental.domain.repository;

import com.instrumentrental.domain.model.InstrumentModel;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface InstrumentModelRepository extends JpaRepository<InstrumentModel, Long> {

    Page<InstrumentModel> findByCategoryIdAndStatus(Long categoryId, String status, Pageable pageable);

    Page<InstrumentModel> findByNameContainingAndStatus(String name, String status, Pageable pageable);

    Page<InstrumentModel> findByStatus(String status, Pageable pageable);
}