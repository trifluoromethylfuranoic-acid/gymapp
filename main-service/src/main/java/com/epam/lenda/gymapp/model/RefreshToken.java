package com.epam.lenda.gymapp.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import java.time.LocalDateTime;
import lombok.*;

@ToString
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@Builder
@Entity
public class RefreshToken extends AbstractEntity {
    @Column(nullable = false)
    private LocalDateTime issued;

    @Column(nullable = false)
    private LocalDateTime validUntil;

    @ManyToOne(optional = false)
    @JoinColumn(nullable = false)
    private User user;

    @Column(nullable = false)
    private Boolean revoked;

    @Column(nullable = false)
    private String tokenHash;
}
