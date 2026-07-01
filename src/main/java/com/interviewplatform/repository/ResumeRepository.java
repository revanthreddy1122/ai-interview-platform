package com.interviewplatform.repository;

import com.interviewplatform.entity.Resume;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ResumeRepository extends JpaRepository<Resume, Long> {

    List<Resume> findByUser_UserIdOrderByUploadDateDesc(Long userId);

    Optional<Resume> findByResumeIdAndUser_UserId(Long resumeId, Long userId);

    long countByUser_UserId(Long userId);

    Optional<Resume> findFirstByUser_UserIdOrderByUploadDateDesc(Long userId);
}
