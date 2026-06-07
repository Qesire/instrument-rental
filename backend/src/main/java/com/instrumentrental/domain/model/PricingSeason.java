package com.instrumentrental.domain.model;

import com.instrumentrental.domain.enums.SeasonType;
import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDate;

@Entity
@Table(name = "pricing_seasons")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PricingSeason {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Enumerated(EnumType.STRING)
    private SeasonType seasonType;

    private LocalDate dateStart;

    private LocalDate dateEnd;

    @Builder.Default
    private BigDecimal coefficient = BigDecimal.ONE;

    @Builder.Default
    private int priority = 0;
}