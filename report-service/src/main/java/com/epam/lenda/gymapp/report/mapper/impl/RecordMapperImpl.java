package com.epam.lenda.gymapp.report.mapper.impl;

import com.epam.lenda.gymapp.report.mapper.RecordMapper;
import com.epam.lenda.gymapp.report.model.YearRecord;
import java.time.Month;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import org.jspecify.annotations.NonNull;
import org.springframework.stereotype.Component;

@Component
public class RecordMapperImpl implements RecordMapper {
    @Override
    public @NonNull Map<Integer, Map<Month, Long>> toMap(@NonNull List<YearRecord> records) {
        final var result = new TreeMap<Integer, Map<Month, Long>>();
        records.forEach(yearRecord -> {
            if (yearRecord == null || yearRecord.getYear() == null || yearRecord.getMonthRecords() == null) {
                return;
            }
            yearRecord.getMonthRecords().forEach(monthRecord -> {
                if (monthRecord != null
                        && monthRecord.getMonth() != null
                        && monthRecord.getTrainingDurationMinutes() != null
                        && monthRecord.getTrainingDurationMinutes() > 0) {
                    result.computeIfAbsent(yearRecord.getYear(), ignored -> new TreeMap<>())
                            .put(monthRecord.getMonth(), monthRecord.getTrainingDurationMinutes() / 60);
                }
            });
        });
        return result;
    }
}
