package com.interviewplatform.repository;

import com.interviewplatform.entity.ResumeAnalysis;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface ResumeAnalysisRepository extends JpaRepository<ResumeAnalysis, Long> {

    Optional<ResumeAnalysis> findByResume_ResumeId(Long resumeId);

    Optional<ResumeAnalysis> findFirstByResume_User_UserIdOrderByAnalyzedAtDesc(Long userId);
}
