package com.noc.dashboardservice.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.Map;

/**
 * Generic grouped-count widget response, e.g. {"Critical": 4, "Major": 9}
 * for alarms-by-severity or {"Open": 3, "In Progress": 5} for
 * incidents-by-status. Reused by both dashboard widgets since the shape
 * is identical.
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class GroupedCountResponse {

    /** What the grouping key represents, e.g. "severity" or "status". */
    private String groupedBy;

    /** Group value -> count. */
    private Map<String, Long> counts;

    private long total;
}
