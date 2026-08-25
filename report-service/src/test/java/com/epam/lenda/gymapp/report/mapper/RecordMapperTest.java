package com.epam.lenda.gymapp.report.mapper;

import static org.junit.jupiter.api.Assertions.*;

import com.epam.lenda.gymapp.report.mapper.impl.RecordMapperImpl;
import com.epam.lenda.gymapp.report.model.TrainingRecord;
import java.time.Month;
import java.util.List;
import org.jspecify.annotations.NonNull;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.junit.jupiter.SpringExtension;

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@ExtendWith(SpringExtension.class)
@Import(RecordMapperImpl.class)
public class RecordMapperTest {
    @Autowired
    private RecordMapper recordMapper;

    @Test
    void toMap_success() {
        final var records = getTrainingRecords();

        final var map = recordMapper.toMap(records);

        records.forEach(record -> {
            final var mapForYear = map.get(record.getId().getYear());
            assertNotNull(mapForYear);
            final var durationForMonthHours = mapForYear.get(record.getId().getMonth());
            assertEquals(record.getTrainingDurationMinutes() / 60, durationForMonthHours);
        });

        var previousYear = Integer.MIN_VALUE;
        for (var year : map.keySet()) {
            assertTrue(previousYear < year);
            previousYear = year;
        }

        map.values().forEach(mapForMonth -> {
            Month previousMonth = null;
            for(var month : mapForMonth.keySet()) {
                assertTrue(previousMonth == null || previousMonth.compareTo(month) < 0);
                previousMonth = month;
            }
        });
    }

    @Test
    void toMap_removesZeroRows() {
        final var username = "username";
        final var records = List.of(
                new TrainingRecord(new TrainingRecord.Id(username, 2025, Month.JANUARY), 100L),
                new TrainingRecord(new TrainingRecord.Id(username, 2025, Month.FEBRUARY), 0L),
                new TrainingRecord(new TrainingRecord.Id(username, 2024, Month.MARCH), 0L));

        final var map = recordMapper.toMap(records);

        assertEquals(java.util.Map.of(2025, java.util.Map.of(Month.JANUARY, 1L)), map);
    }

    private static @NonNull List<TrainingRecord> getTrainingRecords() {
        final var username = "username";
        return List.of(
                new TrainingRecord(new TrainingRecord.Id(username, 2025, Month.JANUARY), 500L),
                new TrainingRecord(new TrainingRecord.Id(username, 2024, Month.JANUARY), 600L),
                new TrainingRecord(new TrainingRecord.Id(username, 2026, Month.APRIL), 700L),
                new TrainingRecord(new TrainingRecord.Id(username, 2025, Month.NOVEMBER), 800L),
                new TrainingRecord(new TrainingRecord.Id(username, 2025, Month.DECEMBER), 900L),
                new TrainingRecord(new TrainingRecord.Id(username, 2025, Month.FEBRUARY), 100L),
                new TrainingRecord(new TrainingRecord.Id(username, 2025, Month.MARCH), 200L)
        );
    }
}
