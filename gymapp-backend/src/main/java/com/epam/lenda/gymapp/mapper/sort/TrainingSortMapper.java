package com.epam.lenda.gymapp.mapper.sort;

import java.util.Optional;
import org.springframework.stereotype.Component;

@Component
public class TrainingSortMapper implements SortMapper {
    @Override
    public Optional<String> mapPropertyName(String externalName) {
        return switch (externalName) {
            case "trainee" -> Optional.of("trainee.user.username");
            case "trainer" -> Optional.of("trainer.user.username");
            case "name" -> Optional.of("name");
            case "trainingType" -> Optional.of("type.name");
            case "datetime" -> Optional.of("datetime");
            case "duration" -> Optional.of("duration");
            default -> Optional.empty();
        };
    }
}
