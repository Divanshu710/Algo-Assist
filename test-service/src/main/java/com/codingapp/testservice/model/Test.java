package com.codingapp.testservice.model;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "tests") // Removed the custom indexes
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Test implements Serializable {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String title;

    @Column(nullable = false)
    private String creatorId; // Extracted from X-User-Id header

    @Column(nullable = false)
    private String inviteeId;

    @Column(nullable = false)
    private Integer durationMinutes; // Total test time in minutes

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private TestStatus status; // Initialized to PENDING on creation

    private LocalDateTime startTime; // Set strictly on the backend when candidate clicks start

    private LocalDateTime endTime; // Evaluated as (startTime + durationMinutes)

    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(name = "test_problem_ids", joinColumns = @JoinColumn(name = "test_id"))
    @Column(name = "problem_id", nullable = false)
    @Builder.Default
    private List<Long> problemIds = new ArrayList<>();

    @Column(nullable = true)
    private Integer scoreAchieved; // Null until the test is completed/graded

    @Column(nullable = false)
    @Builder.Default
    private Integer totalMaxScore = 0; // Set during test creation

    @CreationTimestamp
    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    private LocalDateTime updatedAt;
}