package com.epam.lenda.gymapp.report.model;

import java.time.Month;
import lombok.*;

@ToString
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@Builder
public class MonthRecord {
    private Month month;

    private Long trainingDurationMinutes;
}
