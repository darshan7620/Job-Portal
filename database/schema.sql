-- SmartJobTracker database schema (MySQL 8.x)
-- Creates the database and all tables. Safe to run more than once.
-- Apply: mysql -u root -p < database/schema.sql

CREATE DATABASE IF NOT EXISTS smart_job_tracker
    CHARACTER SET utf8mb4
    COLLATE utf8mb4_unicode_ci;

USE smart_job_tracker;

CREATE TABLE IF NOT EXISTS users (
    id            BIGINT       NOT NULL AUTO_INCREMENT,
    full_name     VARCHAR(100) NOT NULL,
    email         VARCHAR(100) NOT NULL,
    username      VARCHAR(50)  NOT NULL,
    password_hash VARCHAR(255) NOT NULL,
    phone         VARCHAR(15),
    role          VARCHAR(20)  NOT NULL,
    active        BIT(1)       NOT NULL DEFAULT b'1',
    created_at    DATETIME(6)  NOT NULL,
    PRIMARY KEY (id),
    UNIQUE KEY uq_users_email (email),
    UNIQUE KEY uq_users_username (username)
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COLLATE = utf8mb4_unicode_ci;

CREATE TABLE IF NOT EXISTS jobs (
    id                BIGINT       NOT NULL AUTO_INCREMENT,
    title             VARCHAR(150) NOT NULL,
    company           VARCHAR(150) NOT NULL,
    location          VARCHAR(150),
    description       TEXT         NOT NULL,
    required_skills   VARCHAR(1000),
    employment_type   VARCHAR(50),
    salary_min        INT,
    salary_max        INT,
    status            VARCHAR(20)  NOT NULL,
    expires_at        DATE,
    posted_by_user_id BIGINT       NOT NULL,
    created_at        DATETIME(6)  NOT NULL,
    updated_at        DATETIME(6),
    PRIMARY KEY (id),
    KEY idx_jobs_status (status),
    KEY idx_jobs_posted_by (posted_by_user_id),
    CONSTRAINT fk_jobs_posted_by FOREIGN KEY (posted_by_user_id) REFERENCES users (id)
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COLLATE = utf8mb4_unicode_ci;

CREATE TABLE IF NOT EXISTS job_applications (
    id           BIGINT      NOT NULL AUTO_INCREMENT,
    job_id       BIGINT      NOT NULL,
    candidate_id BIGINT      NOT NULL,
    cover_letter TEXT,
    status       VARCHAR(20) NOT NULL,
    applied_at   DATETIME(6) NOT NULL,
    updated_at   DATETIME(6),
    PRIMARY KEY (id),
    UNIQUE KEY uq_job_candidate (job_id, candidate_id),
    KEY idx_applications_candidate (candidate_id),
    CONSTRAINT fk_applications_job FOREIGN KEY (job_id) REFERENCES jobs (id) ON DELETE CASCADE,
    CONSTRAINT fk_applications_candidate FOREIGN KEY (candidate_id) REFERENCES users (id)
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COLLATE = utf8mb4_unicode_ci;
