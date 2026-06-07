package com.instrumentrental.dto.instrument;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class InstrumentDTO {

    private Long id;
    private String serialNo;
    private String barcode;
    private Long modelId;
    private String modelName;
    private Long warehouseId;
    private String warehouseName;
    private String status;
    private String conditionNote;
    private LocalDateTime createdAt;
}