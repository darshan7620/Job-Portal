-- SmartJobTracker sample data (MySQL 8.x)
-- Apply after schema.sql: mysql -u root -p smart_job_tracker < database/seed.sql
--
-- Password columns store BCrypt hashes only (never plaintext). The matching
-- development logins are documented in the project README. Change them anywhere
-- the app is exposed beyond local development.
--
-- The HR account is seeded directly here and must never be created through
-- public registration.

USE smart_job_tracker;

INSERT INTO users (full_name, email, username, password_hash, phone, role, active, created_at)
VALUES ('Hiring Manager', 'hr@smartjobtracker.com', 'hrmanager',
        '$2a$10$XQQ041ckd8zeW6y7XtbkzOV8pnw68FO1K76zCfXZjcZk/ui9gMkJm',
        '+10000000001', 'HR', 1, NOW(6))
ON DUPLICATE KEY UPDATE full_name = VALUES(full_name);

INSERT INTO users (full_name, email, username, password_hash, phone, role, active, created_at)
VALUES ('Jane Candidate', 'jane@example.com', 'janec',
        '$2a$10$BR9RtugQc/Rkwybh7XRgq.YGU6B42EvFScr0hE/.6G3OdTnGv6Ome',
        '+10000000002', 'CANDIDATE', 1, NOW(6))
ON DUPLICATE KEY UPDATE full_name = VALUES(full_name);

INSERT INTO jobs (title, company, location, description, required_skills, employment_type, salary_min, salary_max, status, expires_at, posted_by_user_id, created_at, updated_at)
SELECT 'Backend Java Developer', 'TechNova', 'Remote',
       'Build and maintain Spring Boot services and REST APIs for our hiring platform.',
       'Java, Spring Boot, JPA, MySQL, REST', 'FULL_TIME', 70000, 110000, 'PUBLISHED',
       DATE_ADD(CURDATE(), INTERVAL 30 DAY),
       (SELECT id FROM users WHERE username = 'hrmanager'), NOW(6), NOW(6)
WHERE NOT EXISTS (SELECT 1 FROM jobs WHERE title = 'Backend Java Developer' AND company = 'TechNova');

INSERT INTO jobs (title, company, location, description, required_skills, employment_type, salary_min, salary_max, status, expires_at, posted_by_user_id, created_at, updated_at)
SELECT 'React Frontend Developer', 'TechNova', 'Bengaluru, India',
       'Develop responsive React interfaces and integrate them with REST APIs.',
       'JavaScript, React, Vite, CSS, REST', 'FULL_TIME', 60000, 95000, 'PUBLISHED',
       DATE_ADD(CURDATE(), INTERVAL 21 DAY),
       (SELECT id FROM users WHERE username = 'hrmanager'), NOW(6), NOW(6)
WHERE NOT EXISTS (SELECT 1 FROM jobs WHERE title = 'React Frontend Developer' AND company = 'TechNova');

INSERT INTO jobs (title, company, location, description, required_skills, employment_type, status, expires_at, posted_by_user_id, created_at, updated_at)
SELECT 'QA Engineer Intern', 'TechNova', 'Hybrid',
       'Assist the QA team with manual and automated testing of web applications.',
       'Testing, Selenium, SQL, Attention to detail', 'INTERNSHIP', 'DRAFT',
       DATE_ADD(CURDATE(), INTERVAL 45 DAY),
       (SELECT id FROM users WHERE username = 'hrmanager'), NOW(6), NOW(6)
WHERE NOT EXISTS (SELECT 1 FROM jobs WHERE title = 'QA Engineer Intern' AND company = 'TechNova');

INSERT INTO job_applications (job_id, candidate_id, cover_letter, status, applied_at, updated_at)
VALUES ((SELECT id FROM jobs WHERE title = 'Backend Java Developer' AND company = 'TechNova'),
        (SELECT id FROM users WHERE username = 'janec'),
        'I have built several Spring Boot REST services and would love to contribute to the hiring platform.',
        'APPLIED', NOW(6), NOW(6))
ON DUPLICATE KEY UPDATE cover_letter = VALUES(cover_letter);
