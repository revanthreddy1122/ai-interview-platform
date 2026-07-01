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
@Table(name = "resume_analysis")
@Getter
@Setter
@ToString(exclude = "resume")
@EqualsAndHashCode(exclude = "resume")
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ResumeAnalysis {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "analysis_id")
    private Long analysisId;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "resume_id", nullable = false, unique = true)
    @JsonIgnore
    private Resume resume;

    @Column(name = "ats_score")
    private Integer atsScore;

    @Lob
    @Column(name = "strengths", columnDefinition = "TEXT")
    private String strengths;

    @Lob
    @Column(name = "weaknesses", columnDefinition = "TEXT")
    private String weaknesses;

    @Lob
    @Column(name = "missing_skills", columnDefinition = "TEXT")
    private String missingSkills;

    @Lob
    @Column(name = "suggestions", columnDefinition = "TEXT")
    private String suggestions;

    @Lob
    @Column(name = "job_description", columnDefinition = "TEXT")
    private String jobDescription;

    @Column(name = "match_percentage")
    private Integer matchPercentage;

    @Column(name = "analyzed_at", nullable = false)
    private LocalDateTime analyzedAt;

    @PrePersist
    protected void onCreate() {
        this.analyzedAt = LocalDateTime.now();
    }
}
