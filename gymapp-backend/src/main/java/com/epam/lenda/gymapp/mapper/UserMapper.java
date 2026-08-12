package com.epam.lenda.gymapp.mapper;

import com.epam.lenda.gymapp.dto.GymUserDetails;
import com.epam.lenda.gymapp.dto.response.FullTraineeResponse;
import com.epam.lenda.gymapp.dto.response.FullTrainerResponse;
import com.epam.lenda.gymapp.dto.response.TraineeResponse;
import com.epam.lenda.gymapp.dto.response.TrainerResponse;
import com.epam.lenda.gymapp.model.Trainee;
import com.epam.lenda.gymapp.model.Trainer;
import com.epam.lenda.gymapp.model.User;
import java.util.List;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingConstants;
import org.mapstruct.ReportingPolicy;

@Mapper(componentModel = MappingConstants.ComponentModel.SPRING, unmappedTargetPolicy = ReportingPolicy.ERROR, uses = {TrainingTypeMapper.class})
public interface UserMapper {
    @Mapping(target = ".", source = "user")
    TraineeResponse toDto(Trainee trainee);

    @Mapping(target = ".", source = "user")
    TrainerResponse toDto(Trainer trainer);

    @Mapping(target = ".", source = "trainee.user")
    FullTraineeResponse toDto(Trainee trainee, List<Trainer> trainers);

    @Mapping(target = ".", source = "trainer.user")
    FullTrainerResponse toDto(Trainer trainer, List<Trainee> trainees);

    @Mapping(target = "isLocked", expression = "java(user.getLockedAt() != null)")
    GymUserDetails toUserDetails(User user);
}
