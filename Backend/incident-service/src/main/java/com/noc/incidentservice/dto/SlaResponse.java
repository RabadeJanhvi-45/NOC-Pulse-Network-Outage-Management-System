package com.noc.incidentservice.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SlaResponse {

    private Long incidentId;
    private Integer slaThreshold;
    private Integer resolutionTime;
    private Boolean isBreached;
}
