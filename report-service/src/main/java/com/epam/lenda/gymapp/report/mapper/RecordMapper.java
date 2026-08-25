package com.epam.lenda.gymapp.report.mapper;

import com.epam.lenda.gymapp.report.model.TrainingRecord;
import java.time.Month;
import java.util.List;
import java.util.Map;
import org.jspecify.annotations.NonNull;

public interface RecordMapper {
    @NonNull Map<Integer, Map<Month, Long>> toMap(@NonNull List<TrainingRecord> records);
}
