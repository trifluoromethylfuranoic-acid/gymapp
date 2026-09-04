package com.epam.lenda.gymapp.report.model;

import java.util.ArrayList;
import java.util.List;
import lombok.*;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

@ToString
@NoArgsConstructor
@Getter
@Setter
@Document(collection = "trainers")
public class Trainer implements Cloneable {
    @Id
    private String username;

    private String firstName;

    private String lastName;

    private Boolean isActive;

    private List<YearRecord> yearRecords = new ArrayList<>();

    @Builder
    public Trainer(String username, String firstName, String lastName, Boolean isActive) {
        this.username = username;
        this.firstName = firstName;
        this.lastName = lastName;
        this.isActive = isActive;
        this.yearRecords = new ArrayList<>();
    }

    @Override
    public Trainer clone() {
        try {
            return (Trainer) super.clone();
        } catch (CloneNotSupportedException e) {
            throw new AssertionError();
        }
    }
}
