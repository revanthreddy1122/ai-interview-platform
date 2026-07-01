package com.interviewplatform.repository;

import com.interviewplatform.entity.Question;
import com.interviewplatform.entity.QuestionCategory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface QuestionRepository extends JpaRepository<Question, Long> {

    List<Question> findByUser_UserIdOrderByCreatedAtDesc(Long userId);

    List<Question> findByUser_UserIdAndCategoryOrderByCreatedAtDesc(Long userId, QuestionCategory category);
}
