package com.epam.lenda.gymapp.report.mapper.impl;

import com.epam.lenda.gymapp.report.mapper.RecordMapper;
import com.epam.lenda.gymapp.report.model.TrainingRecord;
import com.epam.lenda.gymapp.report.util.Pair;
import java.time.Month;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.stream.Collectors;
import org.jspecify.annotations.NonNull;
import org.springframework.stereotype.Component;

@Component
public class RecordMapperImpl implements RecordMapper {
    @Override
    public @NonNull Map<Integer, Map<Month, Long>> toMap(@NonNull List<TrainingRecord> records) {
        final var nonEmptyRecords = records.stream()
                .filter(record -> record.getTrainingDurationMinutes() > 0)
                .toList();

        final var years = nonEmptyRecords.stream().map(record -> record.getId().getYear()).collect(Collectors.toSet());

        return years
                .stream()
                .map(year -> Pair.of(year, extractYear(nonEmptyRecords, year)))
                .collect(TreeMap::new,
                         (map, pair) -> map.put(pair.first(), pair.second()),
                         TreeMap::putAll
                );
    }

    private @NonNull Map<Month, Long> extractYear(@NonNull List<TrainingRecord> records, int year) {
        return records
                .stream()
                .filter(record -> record.getId().getYear() == year)
                .collect(TreeMap::new,
                         (map, record) -> map.put(record.getId().getMonth(),
                                                  record.getTrainingDurationMinutes() / 60),
                         TreeMap::putAll
                );
    }
}
