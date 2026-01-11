package org.example.scrapingtest.model;

import com.fasterxml.jackson.annotation.JsonBackReference;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.UUID;

@Entity
@Table(name = "schedule")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Schedule {

    @Id
    @Column(name = "schedule_id", length = 36)
    private String scheduleId;

    @Setter
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "subject_id",nullable = false)
    @JsonBackReference
    private Subjects subject;

    @Column(name = "day_of_week")
    private String dayOfWeek;

    @Column(name = "start_period")
    private int startPeriod;

    @Column(name = "duration")
    private int duration;

    @Column(name = "room")
    private String room;

    @Column(name = "lecturer")
    private String lecturer;

    @PrePersist
    public void prePersist() {
        if (scheduleId == null) {
            scheduleId = UUID.randomUUID().toString();
        }
    }
}