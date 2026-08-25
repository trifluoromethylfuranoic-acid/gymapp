package com.epam.lenda.gymapp.exception;

import jakarta.annotation.Nonnull;
import lombok.Getter;

@Getter
public class ResourceNotFoundException extends RuntimeException {
    private final String resourceType;
    private final Object[] resourceIds;

    public ResourceNotFoundException() {
        super("Resource not found");
        resourceType = null;
        resourceIds = null;
    }

    public ResourceNotFoundException(@Nonnull String message) {
        super(message);
        resourceType = null;
        resourceIds = null;
    }

    public ResourceNotFoundException(@Nonnull String resourceType, @Nonnull Object resourceId) {
        super("%s with id %s was not found".formatted(resourceType, resourceId));
        this.resourceType = resourceType;
        this.resourceIds = new Object[]{resourceId};
    }

    public ResourceNotFoundException(@Nonnull String resourceType, @Nonnull Object... resourceIds) {
        super(generateMessage(resourceType, resourceIds));
        this.resourceType = resourceType;
        this.resourceIds = resourceIds;
    }

    private static String generateMessage(@Nonnull String resourceType, @Nonnull Object... resourceIds) {
        if (resourceIds.length == 0) {
            return "%s was not found".formatted(resourceType);
        }

        final var sb = new StringBuilder();

        sb.append(resourceType);
        if (resourceIds.length == 1) {
            sb.append(" with id ");
        } else {
            sb.append(" with ids ");
        }
        sb.append(resourceIds[0]);

        for (var i = 1; i < resourceIds.length; i++) {
            sb.append(", ");
            sb.append(resourceIds[i]);
        }

        sb.append(" were not found");
        return sb.toString();
    }
}
