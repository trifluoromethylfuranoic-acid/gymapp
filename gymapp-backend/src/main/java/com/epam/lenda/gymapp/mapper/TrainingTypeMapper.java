package com.epam.lenda.gymapp.mapper;

import com.epam.lenda.gymapp.model.TrainingType;
import org.mapstruct.Mapper;
import org.mapstruct.MappingConstants;
import org.mapstruct.ReportingPolicy;

@Mapper(componentModel = MappingConstants.ComponentModel.SPRING, unmappedTargetPolicy = ReportingPolicy.ERROR)
public interface TrainingTypeMapper {
    default String getName(TrainingType trainingType) {
        return trainingType.getName();
    }
}
