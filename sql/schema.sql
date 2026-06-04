CREATE DATABASE IF NOT EXISTS opera_teaching DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
USE opera_teaching;

CREATE TABLE `user` (
    `id`            BIGINT PRIMARY KEY AUTO_INCREMENT,
    `username`      VARCHAR(50) NOT NULL UNIQUE,
    `password_hash` VARCHAR(255) NOT NULL,
    `nickname`      VARCHAR(100),
    `avatar_url`    VARCHAR(512),
    `role`          ENUM('TEACHER', 'STUDENT') NOT NULL,
    `email`         VARCHAR(100),
    `phone`         VARCHAR(20),
    `enabled`       TINYINT(1) DEFAULT 1,
    `created_at`    DATETIME DEFAULT CURRENT_TIMESTAMP,
    `updated_at`    DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE `course` (
    `id`            BIGINT PRIMARY KEY AUTO_INCREMENT,
    `teacher_id`    BIGINT NOT NULL,
    `title`         VARCHAR(200) NOT NULL,
    `opera_type`    VARCHAR(50) COMMENT '剧种',
    `description`   TEXT,
    `cover_url`     VARCHAR(512),
    `status`        ENUM('DRAFT', 'PUBLISHED', 'ARCHIVED') DEFAULT 'DRAFT',
    `created_at`    DATETIME DEFAULT CURRENT_TIMESTAMP,
    `updated_at`    DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    FOREIGN KEY (`teacher_id`) REFERENCES `user`(`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE `segment` (
    `id`              BIGINT PRIMARY KEY AUTO_INCREMENT,
    `course_id`       BIGINT NOT NULL,
    `title`           VARCHAR(200) NOT NULL,
    `sequence_num`    INT NOT NULL,
    `audio_oss_key`   VARCHAR(512) NOT NULL,
    `audio_duration`  DECIMAL(8,2),
    `ban_shi`         VARCHAR(100) COMMENT '板式',
    `diao_men`        VARCHAR(50) COMMENT '调门',
    `difficulty`      TINYINT,
    `difficulty_tips` TEXT,
    `lyrics`          TEXT,
    `bpm`             INT,
    `time_signature`  VARCHAR(10) DEFAULT '4/4',
    `reference_pitch` JSON,
    `created_at`      DATETIME DEFAULT CURRENT_TIMESTAMP,
    `updated_at`      DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    FOREIGN KEY (`course_id`) REFERENCES `course`(`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE `segment_annotation` (
    `id`          BIGINT PRIMARY KEY AUTO_INCREMENT,
    `segment_id`  BIGINT NOT NULL,
    `start_time`  DECIMAL(8,3) NOT NULL,
    `end_time`    DECIMAL(8,3) NOT NULL,
    `label`       VARCHAR(200),
    `type`        ENUM('BANSHI', 'BREATH', 'ORNAMENT', 'DIFFICULTY', 'OTHER') DEFAULT 'OTHER',
    `description` TEXT,
    FOREIGN KEY (`segment_id`) REFERENCES `segment`(`id`) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE `enrollment` (
    `id`          BIGINT PRIMARY KEY AUTO_INCREMENT,
    `student_id`  BIGINT NOT NULL,
    `course_id`   BIGINT NOT NULL,
    `enrolled_at` DATETIME DEFAULT CURRENT_TIMESTAMP,
    UNIQUE KEY `uk_student_course` (`student_id`, `course_id`),
    FOREIGN KEY (`student_id`) REFERENCES `user`(`id`),
    FOREIGN KEY (`course_id`) REFERENCES `course`(`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE `practice_record` (
    `id`              BIGINT PRIMARY KEY AUTO_INCREMENT,
    `student_id`      BIGINT NOT NULL,
    `segment_id`      BIGINT NOT NULL,
    `audio_oss_key`   VARCHAR(512) NOT NULL,
    `audio_duration`  DECIMAL(8,2),
    `playback_speed`  DECIMAL(3,2) DEFAULT 1.00,
    `status`          ENUM('UPLOADED', 'ANALYZING', 'COMPLETED', 'FAILED') DEFAULT 'UPLOADED',
    `created_at`      DATETIME DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (`student_id`) REFERENCES `user`(`id`),
    FOREIGN KEY (`segment_id`) REFERENCES `segment`(`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE `analysis_result` (
    `id`                  BIGINT PRIMARY KEY AUTO_INCREMENT,
    `practice_record_id`  BIGINT NOT NULL UNIQUE,
    `overall_score`       DECIMAL(5,2),
    `pitch_curve_student` JSON,
    `pitch_curve_teacher` JSON,
    `error_segments`      JSON,
    `summary`             TEXT,
    `analyzed_at`         DATETIME DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (`practice_record_id`) REFERENCES `practice_record`(`id`) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE `metronome_preset` (
    `id`              BIGINT PRIMARY KEY AUTO_INCREMENT,
    `user_id`         BIGINT NOT NULL,
    `name`            VARCHAR(100),
    `bpm`             INT NOT NULL,
    `time_signature`  VARCHAR(10) NOT NULL DEFAULT '4/4',
    `accent_pattern`  VARCHAR(50),
    `created_at`      DATETIME DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (`user_id`) REFERENCES `user`(`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;
