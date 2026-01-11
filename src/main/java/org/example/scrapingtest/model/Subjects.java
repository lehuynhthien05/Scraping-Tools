package org.example.scrapingtest.model;

import com.fasterxml.jackson.annotation.JsonManagedReference;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "subjects")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Subjects {

    @Id
    @Column(name = "subject_id", nullable = false)
    private String subjectId;

    @Column(name = "class_code")
    private String classCode;

    @Column(name = "credits")
    private int credits;


    @Column(name = "subject_name")
    private String subjectName;

    @Column(name = "capacity")
    private int capacity;

    @OneToMany(
            mappedBy = "subject",
            cascade = CascadeType.ALL,
            orphanRemoval = true
    )
    @JsonManagedReference
    private List<Schedule> schedule = new ArrayList<>();

    public void addSchedule(Schedule s) {
        schedule.add(s);
        s.setSubject(this);
    }
}