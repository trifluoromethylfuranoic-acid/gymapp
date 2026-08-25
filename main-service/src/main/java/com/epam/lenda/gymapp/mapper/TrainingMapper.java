package com.epam.lenda.gymapp.mapper;

import com.epam.lenda.gymapp.dto.response.*;
import com.epam.lenda.gymapp.model.Trainee;
import com.epam.lenda.gymapp.model.Trainer;
import com.epam.lenda.gymapp.model.Training;
import org.mapstruct.Mapper;
import org.mapstruct.MappingConstants;
import org.mapstruct.ReportingPolicy;

@Mapper(componentModel = MappingConstants.ComponentModel.SPRING, unmappedTargetPolicy = ReportingPolicy.ERROR, uses = {TrainingTypeMapper.class})
public interface TrainingMapper {
    TrainingResponse toDto(Training training);

    default String getUsername(Trainee trainee) {
        return trainee == null ? null : trainee.getUser().getUsername();
    }

    default String getUsername(Trainer trainer) {
        return trainer.getUser().getUsername();
    }
}
