package com.instrumentrental.domain.repository;

import com.instrumentrental.domain.enums.SeasonType;
import com.instrumentrental.domain.model.PricingSeason;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;

@Repository
public interface PricingSeasonRepository extends JpaRepository<PricingSeason, Long> {

    List<PricingSeason> findBySeasonTypeOrderByPriorityDesc(SeasonType seasonType);

    List<PricingSeason> findBySeasonTypeAndDateStartLessThanEqualAndDateEndGreaterThanEqual(SeasonType seasonType, LocalDate date1, LocalDate date2);

    List<PricingSeason> findAllByOrderByPriorityDesc();
}