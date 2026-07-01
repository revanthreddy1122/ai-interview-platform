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
@Table(name = "interview_history")
@Getter
@Setter
@ToString(exclude = {"user", "question"})
@EqualsAndHashCode(exclude = {"user", "question"})
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class InterviewHistory {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "history_id")
    private Long historyId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    @JsonIgnore
    private User user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "question_id")
    @JsonIgnore
    private Question question;

    @Lob
    @Column(name = "question_text", nullable = false, columnDefinition = "TEXT")
    private String questionText;

    @Lob
    @Column(name = "answer_text", columnDefinition = "TEXT")
    private String answerText;

    @Column(name = "score")
    private Integer score;

    @Lob
    @Column(name = "feedback", columnDefinition = "TEXT")
    private String feedback;

    @Lob
    @Column(name = "suggestions", columnDefinition = "TEXT")
    private String suggestions;

    @Column(name = "correctness_score")
    private Integer correctnessScore;

    @Column(name = "completeness_score")
    private Integer completenessScore;

    @Column(name = "communication_score")
    private Integer communicationScore;

    @Column(name = "attempted_at", nullable = false)
    private LocalDateTime attemptedAt;

    @PrePersist
    protected void onCreate() {
        this.attemptedAt = LocalDateTime.now();
    }
}
