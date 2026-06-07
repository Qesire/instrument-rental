package com.instrumentrental.dto.admin;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ScanRequest {

    @NotBlank(message = "条码不能为空")
    private String code;

    private Boolean damaged;
}