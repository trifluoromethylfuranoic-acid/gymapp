package com.epam.lenda.gymapp.report.mapper;

import static org.junit.jupiter.api.Assertions.*;

import com.epam.lenda.gymapp.report.mapper.impl.RecordMapperImpl;
import com.epam.lenda.gymapp.report.model.MonthRecord;
import com.epam.lenda.gymapp.report.model.YearRecord;
import java.time.Month;
import java.util.List;
import java.util.Map;
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
        final var records = getYearRecords();

        final var map = recordMapper.toMap(records);

        assertEquals(Map.of(
                2024, Map.of(Month.JANUARY, 10L),
                2025, Map.of(
                        Month.JANUARY, 8L,
                        Month.FEBRUARY, 1L,
                        Month.MARCH, 3L,
                        Month.NOVEMBER, 13L,
                        Month.DECEMBER, 15L),
                2026, Map.of(Month.APRIL, 11L)), map);

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
                new YearRecord(2025, List.of(
                        new MonthRecord(Month.JANUARY, 100L),
                        new MonthRecord(Month.FEBRUARY, 0L))),
                new YearRecord(2024, List.of(new MonthRecord(Month.MARCH, 0L))));

        final var map = recordMapper.toMap(records);

        assertEquals(java.util.Map.of(2025, java.util.Map.of(Month.JANUARY, 1L)), map);
    }

    private static List<YearRecord> getYearRecords() {
        return List.of(
                new YearRecord(2025, List.of(
                        new MonthRecord(Month.JANUARY, 500L),
                        new MonthRecord(Month.NOVEMBER, 800L),
                        new MonthRecord(Month.DECEMBER, 900L),
                        new MonthRecord(Month.FEBRUARY, 100L),
                        new MonthRecord(Month.MARCH, 200L))),
                new YearRecord(2024, List.of(new MonthRecord(Month.JANUARY, 600L))),
                new YearRecord(2026, List.of(new MonthRecord(Month.APRIL, 700L)))
        );
    }
}
