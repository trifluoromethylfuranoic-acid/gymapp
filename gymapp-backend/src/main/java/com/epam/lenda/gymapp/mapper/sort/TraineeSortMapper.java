package com.epam.lenda.gymapp.mapper.sort;

import java.util.Optional;
import org.springframework.stereotype.Component;

@Component
public class TraineeSortMapper implements SortMapper {
    @Override
    public Optional<String> mapPropertyName(String externalName) {
        return switch (externalName) {
            case "firstName" -> Optional.of("user.firstName");
            case "lastName" -> Optional.of("user.lastName");
            case "username" -> Optional.of("user.username");
            case "dateOfBirth" -> Optional.of("dateOfBirth");
            default -> Optional.empty();
        };
    }
}
