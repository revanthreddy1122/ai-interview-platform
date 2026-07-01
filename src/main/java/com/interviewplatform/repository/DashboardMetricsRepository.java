package com.interviewplatform.repository;

import com.interviewplatform.entity.DashboardMetrics;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface DashboardMetricsRepository extends JpaRepository<DashboardMetrics, Long> {

    Optional<DashboardMetrics> findByUser_UserId(Long userId);
}
