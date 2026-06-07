package com.instrumentrental.domain.repository;

import com.instrumentrental.domain.model.PricingTier;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface PricingTierRepository extends JpaRepository<PricingTier, Long> {

    List<PricingTier> findByModelIdOrderByDayFrom(Long modelId);

    List<PricingTier> findByModelIdIsNullOrderByDayFrom();
}