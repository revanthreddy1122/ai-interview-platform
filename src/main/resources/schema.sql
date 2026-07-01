-- =====================================================
-- AI-Powered Interview Preparation Platform
-- Full Database Schema (MySQL 8.0+)
-- =====================================================

CREATE DATABASE IF NOT EXISTS interview_platform_db
    CHARACTER SET utf8mb4
    COLLATE utf8mb4_unicode_ci;

USE interview_platform_db;

-- =====================================================
-- Table: users
-- =====================================================
CREATE TABLE IF NOT EXISTS users (
    user_id        BIGINT AUTO_INCREMENT PRIMARY KEY,
    full_name      VARCHAR(150)  NOT NULL,
    email          VARCHAR(150)  NOT NULL,
    password       VARCHAR(255)  NOT NULL,
    phone_number   VARCHAR(20)   NULL,
    role           VARCHAR(20)   NOT NULL DEFAULT 'USER',
    enabled        BOOLEAN       NOT NULL DEFAULT TRUE,
    created_at     DATETIME      NOT NULL,
    updated_at     DATETIME      NULL,
    CONSTRAINT uq_users_email UNIQUE (email),
    CONSTRAINT chk_users_role CHECK (role IN ('USER', 'ADMIN'))
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE INDEX idx_users_email ON users (email);

-- =====================================================
-- Table: resumes
-- =====================================================
CREATE TABLE IF NOT EXISTS resumes (
    resume_id      BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id        BIGINT        NOT NULL,
    file_name      VARCHAR(255)  NOT NULL,
    resume_text    LONGTEXT      NULL,
    file_size      BIGINT        NULL,
    upload_date    DATETIME      NOT NULL,
    CONSTRAINT fk_resumes_user FOREIGN KEY (user_id)
        REFERENCES users (user_id)
        ON DELETE CASCADE
        ON UPDATE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE INDEX idx_resumes_user_id ON resumes (user_id);
CREATE INDEX idx_resumes_upload_date ON resumes (upload_date);

-- =====================================================
-- Table: resume_analysis
-- =====================================================
CREATE TABLE IF NOT EXISTS resume_analysis (
    analysis_id      BIGINT AUTO_INCREMENT PRIMARY KEY,
    resume_id        BIGINT        NOT NULL,
    ats_score        INT           NULL,
    strengths        TEXT          NULL,
    weaknesses       TEXT          NULL,
    missing_skills   TEXT          NULL,
    suggestions      TEXT          NULL,
    job_description  TEXT          NULL,
    match_percentage INT           NULL,
    analyzed_at      DATETIME      NOT NULL,
    CONSTRAINT uq_resume_analysis_resume UNIQUE (resume_id),
    CONSTRAINT fk_resume_analysis_resume FOREIGN KEY (resume_id)
        REFERENCES resumes (resume_id)
        ON DELETE CASCADE
        ON UPDATE CASCADE,
    CONSTRAINT chk_ats_score_range CHECK (ats_score IS NULL OR (ats_score >= 0 AND ats_score <= 100)),
    CONSTRAINT chk_match_percentage_range CHECK (match_percentage IS NULL OR (match_percentage >= 0 AND match_percentage <= 100))
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE INDEX idx_resume_analysis_resume_id ON resume_analysis (resume_id);

-- =====================================================
-- Table: questions
-- =====================================================
CREATE TABLE IF NOT EXISTS questions (
    question_id    BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id        BIGINT        NOT NULL,
    question_text  TEXT          NOT NULL,
    category       VARCHAR(30)   NOT NULL,
    difficulty     VARCHAR(20)   NULL,
    created_at     DATETIME      NOT NULL,
    CONSTRAINT fk_questions_user FOREIGN KEY (user_id)
        REFERENCES users (user_id)
        ON DELETE CASCADE
        ON UPDATE CASCADE,
    CONSTRAINT chk_question_category CHECK (category IN ('JAVA', 'SPRING_BOOT', 'SQL', 'PROJECT', 'GENERAL'))
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE INDEX idx_questions_user_id ON questions (user_id);
CREATE INDEX idx_questions_category ON questions (category);

-- =====================================================
-- Table: interview_history
-- =====================================================
CREATE TABLE IF NOT EXISTS interview_history (
    history_id           BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id               BIGINT       NOT NULL,
    question_id           BIGINT       NULL,
    question_text         TEXT         NOT NULL,
    answer_text           TEXT         NULL,
    score                 INT          NULL,
    feedback              TEXT         NULL,
    suggestions           TEXT         NULL,
    correctness_score     INT          NULL,
    completeness_score    INT          NULL,
    communication_score   INT          NULL,
    attempted_at          DATETIME     NOT NULL,
    CONSTRAINT fk_interview_history_user FOREIGN KEY (user_id)
        REFERENCES users (user_id)
        ON DELETE CASCADE
        ON UPDATE CASCADE,
    CONSTRAINT fk_interview_history_question FOREIGN KEY (question_id)
        REFERENCES questions (question_id)
        ON DELETE SET NULL
        ON UPDATE CASCADE,
    CONSTRAINT chk_score_range CHECK (score IS NULL OR (score >= 0 AND score <= 100)),
    CONSTRAINT chk_correctness_range CHECK (correctness_score IS NULL OR (correctness_score >= 0 AND correctness_score <= 100)),
    CONSTRAINT chk_completeness_range CHECK (completeness_score IS NULL OR (completeness_score >= 0 AND completeness_score <= 100)),
    CONSTRAINT chk_communication_range CHECK (communication_score IS NULL OR (communication_score >= 0 AND communication_score <= 100))
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE INDEX idx_interview_history_user_id ON interview_history (user_id);
CREATE INDEX idx_interview_history_attempted_at ON interview_history (attempted_at);
CREATE INDEX idx_interview_history_question_id ON interview_history (question_id);

-- =====================================================
-- Table: dashboard_metrics
-- =====================================================
CREATE TABLE IF NOT EXISTS dashboard_metrics (
    metric_id                    BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id                      BIGINT        NOT NULL,
    resume_count                 INT           NOT NULL DEFAULT 0,
    latest_ats_score             INT           NULL,
    average_interview_score      DOUBLE        NULL,
    total_interviews_attempted   INT           NOT NULL DEFAULT 0,
    strong_skills                TEXT          NULL,
    weak_skills                  TEXT          NULL,
    last_updated                 DATETIME      NOT NULL,
    CONSTRAINT uq_dashboard_metrics_user UNIQUE (user_id),
    CONSTRAINT fk_dashboard_metrics_user FOREIGN KEY (user_id)
        REFERENCES users (user_id)
        ON DELETE CASCADE
        ON UPDATE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE INDEX idx_dashboard_metrics_user_id ON dashboard_metrics (user_id);

-- =====================================================
-- Seed Data: Default Admin User
-- Password is "Admin@123" encoded with BCrypt strength 12
-- (Generate your own hash before using this in production)
-- =====================================================
-- INSERT INTO users (full_name, email, password, role, enabled, created_at, updated_at)
-- VALUES ('System Admin', 'admin@interviewplatform.com',
--         '$2a$12$REPLACE_WITH_REAL_BCRYPT_HASH', 'ADMIN', TRUE, NOW(), NOW());
