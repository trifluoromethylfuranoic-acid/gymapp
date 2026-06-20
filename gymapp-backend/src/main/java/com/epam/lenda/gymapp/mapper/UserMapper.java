package com.epam.lenda.gymapp.mapper;

import com.epam.lenda.gymapp.dto.auth.UserDetails;
import com.epam.lenda.gymapp.dto.trainee.FullTraineeResponse;
import com.epam.lenda.gymapp.dto.trainee.TraineeResponse;
import com.epam.lenda.gymapp.dto.trainer.FullTrainerResponse;
import com.epam.lenda.gymapp.dto.trainer.TrainerResponse;
import com.epam.lenda.gymapp.model.Trainee;
import com.epam.lenda.gymapp.model.Trainee2Trainer;
import com.epam.lenda.gymapp.model.Trainer;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingConstants;
import org.mapstruct.ReportingPolicy;

@Mapper(
        componentModel = MappingConstants.ComponentModel.SPRING, unmappedTargetPolicy = ReportingPolicy.ERROR
)
public interface UserMapper {
    @Mapping(target = "username", source = "user.username")
    @Mapping(target = "firstName", source = "user.firstName")
    @Mapping(target = "lastName", source = "user.lastName")
    @Mapping(target = "isActive", source = "user.isActive")
    TraineeResponse toResponseDto(Trainee entity);

    @Mapping(target = "username", source = "user.username")
    @Mapping(target = "firstName", source = "user.firstName")
    @Mapping(target = "lastName", source = "user.lastName")
    @Mapping(target = "isActive", source = "user.isActive")
    @Mapping(target = "trainers", source = "trainerAssignments")
    FullTraineeResponse toFullResponseDto(Trainee entity);

    @Mapping(target = ".", source = "trainee")
    @Mapping(target = "username", source = "trainee.user.username")
    @Mapping(target = "firstName", source = "trainee.user.firstName")
    @Mapping(target = "lastName", source = "trainee.user.lastName")
    @Mapping(target = "isActive", source = "trainee.user.isActive")
    TraineeResponse toTraineeResponseDto(Trainee2Trainer entity);

    @Mapping(target = "username", source = "user.username")
    @Mapping(target = "firstName", source = "user.firstName")
    @Mapping(target = "lastName", source = "user.lastName")
    @Mapping(target = "isActive", source = "user.isActive")
    @Mapping(target = "specialization", source = "specialization.name")
    TrainerResponse toResponseDto(Trainer entity);

    @Mapping(target = "username", source = "user.username")
    @Mapping(target = "firstName", source = "user.firstName")
    @Mapping(target = "lastName", source = "user.lastName")
    @Mapping(target = "isActive", source = "user.isActive")
    @Mapping(target = "specialization", source = "specialization.name")
    @Mapping(target = "trainees", source = "traineeAssignments")
    FullTrainerResponse toFullResponseDto(Trainer entity);

    @Mapping(target = ".", source = "trainer")
    @Mapping(target = "username", source = "trainer.user.username")
    @Mapping(target = "firstName", source = "trainer.user.firstName")
    @Mapping(target = "lastName", source = "trainer.user.lastName")
    @Mapping(target = "isActive", source = "trainer.user.isActive")
    @Mapping(target = "specialization", source = "trainer.specialization.name")
    TrainerResponse toTrainerResponseDto(Trainee2Trainer entity);

    @Mapping(target = "username", source = "user.username")
    @Mapping(target = "password", source = "user.password")
    @Mapping(target = "isActive", source = "user.isActive")
    @Mapping(target = "role", constant = "TRAINEE")
    UserDetails toUserDetails(Trainee entity);

    @Mapping(target = "username", source = "user.username")
    @Mapping(target = "password", source = "user.password")
    @Mapping(target = "isActive", source = "user.isActive")
    @Mapping(target = "role", constant = "TRAINER")
    UserDetails toUserDetails(Trainer entity);
}
