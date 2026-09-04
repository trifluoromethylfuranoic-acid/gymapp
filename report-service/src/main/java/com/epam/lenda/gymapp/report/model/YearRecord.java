package com.epam.lenda.gymapp.report.model;

import java.util.ArrayList;
import java.util.List;
import lombok.*;

@ToString
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@Builder
public class YearRecord {
    private Integer year;

    @Builder.Default
    private List<MonthRecord> monthRecords = new ArrayList<>();
}
