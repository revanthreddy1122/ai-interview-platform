package com.interviewplatform.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

import java.time.LocalDateTime;

@Entity
@Table(name = "dashboard_metrics")
@Getter
@Setter
@ToString(exclude = "user")
@EqualsAndHashCode(exclude = "user")
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DashboardMetrics {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "metric_id")
    private Long metricId;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false, unique = true)
    @JsonIgnore
    private User user;

    @Column(name = "resume_count")
    @Builder.Default
    private Integer resumeCount = 0;

    @Column(name = "latest_ats_score")
    private Integer latestAtsScore;

    @Column(name = "average_interview_score")
    private Double averageInterviewScore;

    @Column(name = "total_interviews_attempted")
    @Builder.Default
    private Integer totalInterviewsAttempted = 0;

    @Lob
    @Column(name = "strong_skills", columnDefinition = "TEXT")
    private String strongSkills;

    @Lob
    @Column(name = "weak_skills", columnDefinition = "TEXT")
    private String weakSkills;

    @Column(name = "last_updated", nullable = false)
    private LocalDateTime lastUpdated;

    @PrePersist
    @PreUpdate
    protected void onUpdate() {
        this.lastUpdated = LocalDateTime.now();
    }
}
