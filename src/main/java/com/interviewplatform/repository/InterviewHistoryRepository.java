package com.interviewplatform.repository;

import com.interviewplatform.entity.InterviewHistory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface InterviewHistoryRepository extends JpaRepository<InterviewHistory, Long> {

    List<InterviewHistory> findByUser_UserIdOrderByAttemptedAtDesc(Long userId);

    long countByUser_UserId(Long userId);

    @Query("SELECT AVG(h.score) FROM InterviewHistory h WHERE h.user.userId = :userId AND h.score IS NOT NULL")
    Double findAverageScoreByUserId(@Param("userId") Long userId);

    List<InterviewHistory> findTop10ByUser_UserIdOrderByAttemptedAtDesc(Long userId);
}
