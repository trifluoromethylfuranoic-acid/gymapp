package com.epam.lenda.gymapp.service.impl;

import com.epam.lenda.gymapp.model.TrainingType;
import com.epam.lenda.gymapp.repository.TrainingTypeRepository;
import com.epam.lenda.gymapp.service.TrainingTypeService;
import jakarta.annotation.Nonnull;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.jspecify.annotations.NonNull;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class TrainingTypeServiceImpl extends BaseServiceImpl<TrainingType, UUID> implements TrainingTypeService {
    private final TrainingTypeRepository trainingTypeRepository;

    @Override
    protected @Nonnull TrainingTypeRepository getRepository() {
        return trainingTypeRepository;
    }

    @Override
    protected @NonNull String getResourceName() {
        return "training type";
    }
}
