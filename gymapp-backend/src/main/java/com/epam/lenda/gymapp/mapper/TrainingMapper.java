package com.epam.lenda.gymapp.mapper;

import com.epam.lenda.gymapp.dto.training.TrainingResponse;
import com.epam.lenda.gymapp.model.Training;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingConstants;
import org.mapstruct.ReportingPolicy;

@Mapper(
        componentModel = MappingConstants.ComponentModel.SPRING, unmappedTargetPolicy = ReportingPolicy.ERROR
)
public interface TrainingMapper {
    @Mapping(target = "trainee", source = "trainee.user.username")
    @Mapping(target = "trainer", source = "trainer.user.username")
    @Mapping(target = "trainingType", source = "type.name")
    TrainingResponse toResponseDto(Training entity);
}
