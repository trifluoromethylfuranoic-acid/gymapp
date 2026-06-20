package com.epam.lenda.gymapp.mapper.sort;

import java.util.Optional;
import org.springframework.stereotype.Component;

@Component
public class TrainerSortMapper implements SortMapper {
    @Override
    public Optional<String> mapPropertyName(String externalName) {
        return switch (externalName) {
            case "firstName" -> Optional.of("user.firstName");
            case "lastName" -> Optional.of("user.lastName");
            case "username" -> Optional.of("user.username");
            case "specialization" -> Optional.of("specialization");
            default -> Optional.empty();
        };
    }
}
