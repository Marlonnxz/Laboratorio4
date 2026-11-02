-- =========================================================================
-- Sample Data for Enrollments Service (UPTC Distributed Systems Lab 3)
-- Covers 35 enrollments across students 1-15, courses 1-10, periods 2026-1 and 2026-2
-- Includes full data for Student ID 12 (Course 7, Period 2026-2) for Gateway /detalle testing
-- =========================================================================

-- Clear existing data (in reverse order of foreign key dependency)
DELETE FROM grades;
DELETE FROM enrollment_status_history;
DELETE FROM enrollments;

-- -------------------------------------------------------------------------
-- ENROLLMENTS
-- -------------------------------------------------------------------------

-- Crucial test enrollment for Gateway /detalle contract: Student 12, Course 7
INSERT INTO enrollments (id, student_id, course_id, period, status, enrollment_date)
VALUES (45, 12, 7, '2026-2', 'ACTIVE', '2026-08-20');

-- Additional enrollments for Student 12
INSERT INTO enrollments (id, student_id, course_id, period, status, enrollment_date)
VALUES (1, 12, 3, '2026-2', 'ACTIVE', '2026-08-20');
INSERT INTO enrollments (id, student_id, course_id, period, status, enrollment_date)
VALUES (2, 12, 1, '2026-1', 'COMPLETED', '2026-02-10');

-- Student 1
INSERT INTO enrollments (id, student_id, course_id, period, status, enrollment_date)
VALUES (3, 1, 1, '2026-1', 'COMPLETED', '2026-02-10');
INSERT INTO enrollments (id, student_id, course_id, period, status, enrollment_date)
VALUES (4, 1, 2, '2026-1', 'COMPLETED', '2026-02-11');
INSERT INTO enrollments (id, student_id, course_id, period, status, enrollment_date)
VALUES (5, 1, 7, '2026-2', 'ACTIVE', '2026-08-18');

-- Student 2
INSERT INTO enrollments (id, student_id, course_id, period, status, enrollment_date)
VALUES (6, 2, 2, '2026-1', 'COMPLETED', '2026-02-12');
INSERT INTO enrollments (id, student_id, course_id, period, status, enrollment_date)
VALUES (7, 2, 8, '2026-2', 'ACTIVE', '2026-08-19');

-- Student 3
INSERT INTO enrollments (id, student_id, course_id, period, status, enrollment_date)
VALUES (8, 3, 3, '2026-1', 'COMPLETED', '2026-02-10');
INSERT INTO enrollments (id, student_id, course_id, period, status, enrollment_date)
VALUES (9, 3, 7, '2026-2', 'ACTIVE', '2026-08-20');
INSERT INTO enrollments (id, student_id, course_id, period, status, enrollment_date)
VALUES (10, 3, 9, '2026-2', 'CANCELLED', '2026-08-21');

-- Student 4
INSERT INTO enrollments (id, student_id, course_id, period, status, enrollment_date)
VALUES (11, 4, 4, '2026-1', 'COMPLETED', '2026-02-10');
INSERT INTO enrollments (id, student_id, course_id, period, status, enrollment_date)
VALUES (12, 4, 10, '2026-2', 'ACTIVE', '2026-08-18');

-- Student 5
INSERT INTO enrollments (id, student_id, course_id, period, status, enrollment_date)
VALUES (13, 5, 5, '2026-1', 'COMPLETED', '2026-02-11');
INSERT INTO enrollments (id, student_id, course_id, period, status, enrollment_date)
VALUES (14, 5, 6, '2026-2', 'ACTIVE', '2026-08-19');

-- Student 6
INSERT INTO enrollments (id, student_id, course_id, period, status, enrollment_date)
VALUES (15, 6, 1, '2026-1', 'COMPLETED', '2026-02-12');
INSERT INTO enrollments (id, student_id, course_id, period, status, enrollment_date)
VALUES (16, 6, 7, '2026-2', 'ACTIVE', '2026-08-20');

-- Student 7
INSERT INTO enrollments (id, student_id, course_id, period, status, enrollment_date)
VALUES (17, 7, 2, '2026-1', 'COMPLETED', '2026-02-10');
INSERT INTO enrollments (id, student_id, course_id, period, status, enrollment_date)
VALUES (18, 7, 8, '2026-2', 'ACTIVE', '2026-08-19');

-- Student 8
INSERT INTO enrollments (id, student_id, course_id, period, status, enrollment_date)
VALUES (19, 8, 3, '2026-1', 'COMPLETED', '2026-02-10');
INSERT INTO enrollments (id, student_id, course_id, period, status, enrollment_date)
VALUES (20, 8, 9, '2026-2', 'ACTIVE', '2026-08-20');

-- Student 9
INSERT INTO enrollments (id, student_id, course_id, period, status, enrollment_date)
VALUES (21, 9, 4, '2026-1', 'COMPLETED', '2026-02-11');
INSERT INTO enrollments (id, student_id, course_id, period, status, enrollment_date)
VALUES (22, 9, 10, '2026-2', 'CANCELLED', '2026-08-22');

-- Student 10
INSERT INTO enrollments (id, student_id, course_id, period, status, enrollment_date)
VALUES (23, 10, 5, '2026-1', 'COMPLETED', '2026-02-12');
INSERT INTO enrollments (id, student_id, course_id, period, status, enrollment_date)
VALUES (24, 10, 6, '2026-2', 'ACTIVE', '2026-08-19');

-- Student 11
INSERT INTO enrollments (id, student_id, course_id, period, status, enrollment_date)
VALUES (25, 11, 1, '2026-1', 'COMPLETED', '2026-02-10');
INSERT INTO enrollments (id, student_id, course_id, period, status, enrollment_date)
VALUES (26, 11, 7, '2026-2', 'ACTIVE', '2026-08-20');

-- Student 13
INSERT INTO enrollments (id, student_id, course_id, period, status, enrollment_date)
VALUES (27, 13, 2, '2026-1', 'COMPLETED', '2026-02-10');
INSERT INTO enrollments (id, student_id, course_id, period, status, enrollment_date)
VALUES (28, 13, 8, '2026-2', 'ACTIVE', '2026-08-19');

-- Student 14
INSERT INTO enrollments (id, student_id, course_id, period, status, enrollment_date)
VALUES (29, 14, 3, '2026-1', 'COMPLETED', '2026-02-11');
INSERT INTO enrollments (id, student_id, course_id, period, status, enrollment_date)
VALUES (30, 14, 9, '2026-2', 'ACTIVE', '2026-08-20');

-- Student 15
INSERT INTO enrollments (id, student_id, course_id, period, status, enrollment_date)
VALUES (31, 15, 4, '2026-1', 'COMPLETED', '2026-02-12');
INSERT INTO enrollments (id, student_id, course_id, period, status, enrollment_date)
VALUES (32, 15, 10, '2026-2', 'ACTIVE', '2026-08-21');
INSERT INTO enrollments (id, student_id, course_id, period, status, enrollment_date)
VALUES (33, 15, 5, '2026-2', 'ACTIVE', '2026-08-22');
INSERT INTO enrollments (id, student_id, course_id, period, status, enrollment_date)
VALUES (34, 15, 6, '2026-1', 'COMPLETED', '2026-02-10');

-- -------------------------------------------------------------------------
-- GRADES
-- -------------------------------------------------------------------------

-- Grades for Enrollment 45 (Student 12, Course 7) - Exactly matches specification contract
INSERT INTO grades (id, enrollment_id, type, value, percentage)
VALUES (1, 45, 'Midterm 1', 4.2, 30.00);
INSERT INTO grades (id, enrollment_id, type, value, percentage)
VALUES (2, 45, 'Midterm 2', 3.8, 30.00);
INSERT INTO grades (id, enrollment_id, type, value, percentage)
VALUES (3, 45, 'Final Exam', 4.5, 40.00);

-- Grades for Enrollment 1 (Student 12, Course 3)
INSERT INTO grades (id, enrollment_id, type, value, percentage)
VALUES (4, 1, 'Midterm 1', 4.0, 35.00);
INSERT INTO grades (id, enrollment_id, type, value, percentage)
VALUES (5, 1, 'Workshop 1', 4.6, 25.00);

-- Grades for Enrollment 2 (Student 12, Course 1)
INSERT INTO grades (id, enrollment_id, type, value, percentage)
VALUES (6, 2, 'Midterm 1', 3.5, 30.00);
INSERT INTO grades (id, enrollment_id, type, value, percentage)
VALUES (7, 2, 'Midterm 2', 4.0, 30.00);
INSERT INTO grades (id, enrollment_id, type, value, percentage)
VALUES (8, 2, 'Final Exam', 4.2, 40.00);

-- Grades for other enrollments
INSERT INTO grades (id, enrollment_id, type, value, percentage)
VALUES (9, 3, 'Midterm 1', 4.5, 50.00);
INSERT INTO grades (id, enrollment_id, type, value, percentage)
VALUES (10, 3, 'Final Exam', 4.8, 50.00);

INSERT INTO grades (id, enrollment_id, type, value, percentage)
VALUES (11, 4, 'Midterm 1', 3.2, 40.00);
INSERT INTO grades (id, enrollment_id, type, value, percentage)
VALUES (12, 4, 'Final Project', 3.9, 60.00);

INSERT INTO grades (id, enrollment_id, type, value, percentage)
VALUES (13, 5, 'Midterm 1', 4.0, 30.00);

INSERT INTO grades (id, enrollment_id, type, value, percentage)
VALUES (14, 6, 'Final Exam', 4.1, 100.00);

INSERT INTO grades (id, enrollment_id, type, value, percentage)
VALUES (15, 7, 'Quiz 1', 4.7, 20.00);
INSERT INTO grades (id, enrollment_id, type, value, percentage)
VALUES (16, 7, 'Midterm 1', 4.3, 30.00);

INSERT INTO grades (id, enrollment_id, type, value, percentage)
VALUES (17, 8, 'Midterm 1', 3.0, 50.00);
INSERT INTO grades (id, enrollment_id, type, value, percentage)
VALUES (18, 8, 'Final Exam', 3.5, 50.00);

INSERT INTO grades (id, enrollment_id, type, value, percentage)
VALUES (19, 9, 'Midterm 1', 4.4, 30.00);

INSERT INTO grades (id, enrollment_id, type, value, percentage)
VALUES (20, 11, 'Midterm 1', 4.9, 50.00);
INSERT INTO grades (id, enrollment_id, type, value, percentage)
VALUES (21, 11, 'Final Exam', 5.0, 50.00);

INSERT INTO grades (id, enrollment_id, type, value, percentage)
VALUES (22, 13, 'Coursework', 4.0, 100.00);

INSERT INTO grades (id, enrollment_id, type, value, percentage)
VALUES (23, 15, 'Midterm 1', 3.8, 50.00);
INSERT INTO grades (id, enrollment_id, type, value, percentage)
VALUES (24, 15, 'Final Exam', 4.0, 50.00);

INSERT INTO grades (id, enrollment_id, type, value, percentage)
VALUES (25, 17, 'Final Exam', 4.5, 100.00);

INSERT INTO grades (id, enrollment_id, type, value, percentage)
VALUES (26, 19, 'Final Exam', 3.7, 100.00);

INSERT INTO grades (id, enrollment_id, type, value, percentage)
VALUES (27, 23, 'Midterm 1', 4.2, 50.00);
INSERT INTO grades (id, enrollment_id, type, value, percentage)
VALUES (28, 23, 'Final Exam', 4.6, 50.00);

INSERT INTO grades (id, enrollment_id, type, value, percentage)
VALUES (29, 25, 'Midterm 1', 4.0, 50.00);
INSERT INTO grades (id, enrollment_id, type, value, percentage)
VALUES (30, 25, 'Final Exam', 4.3, 50.00);

INSERT INTO grades (id, enrollment_id, type, value, percentage)
VALUES (31, 27, 'Final Exam', 3.9, 100.00);

INSERT INTO grades (id, enrollment_id, type, value, percentage)
VALUES (32, 29, 'Final Exam', 4.8, 100.00);

INSERT INTO grades (id, enrollment_id, type, value, percentage)
VALUES (33, 31, 'Final Exam', 4.4, 100.00);

INSERT INTO grades (id, enrollment_id, type, value, percentage)
VALUES (34, 34, 'Final Exam', 4.0, 100.00);

-- -------------------------------------------------------------------------
-- ENROLLMENT STATUS HISTORY
-- -------------------------------------------------------------------------

-- History for Enrollment 45
INSERT INTO enrollment_status_history (id, enrollment_id, status, date, reason)
VALUES (1, 45, 'ACTIVE', '2026-08-20 09:00:00', 'Initial regular registration for semester 2026-2');

-- History for Enrollment 1
INSERT INTO enrollment_status_history (id, enrollment_id, status, date, reason)
VALUES (2, 1, 'ACTIVE', '2026-08-20 09:15:00', 'Initial regular registration for semester 2026-2');

-- History for Enrollment 2
INSERT INTO enrollment_status_history (id, enrollment_id, status, date, reason)
VALUES (3, 2, 'ACTIVE', '2026-02-10 10:00:00', 'Initial regular registration for semester 2026-1');
INSERT INTO enrollment_status_history (id, enrollment_id, status, date, reason)
VALUES (4, 2, 'COMPLETED', '2026-06-30 18:00:00', 'Academic term successfully finished with final grade passing');

-- History for Enrollment 3
INSERT INTO enrollment_status_history (id, enrollment_id, status, date, reason)
VALUES (5, 3, 'ACTIVE', '2026-02-10 10:30:00', 'Initial regular registration for semester 2026-1');
INSERT INTO enrollment_status_history (id, enrollment_id, status, date, reason)
VALUES (6, 3, 'COMPLETED', '2026-06-30 18:00:00', 'Academic term successfully completed');

-- History for Enrollment 10 (Cancelled)
INSERT INTO enrollment_status_history (id, enrollment_id, status, date, reason)
VALUES (7, 10, 'ACTIVE', '2026-08-21 08:00:00', 'Initial registration');
INSERT INTO enrollment_status_history (id, enrollment_id, status, date, reason)
VALUES (8, 10, 'CANCELLED', '2026-08-25 14:00:00', 'Course dropped voluntarily by student due to schedule conflict');

-- History for Enrollment 22 (Cancelled)
INSERT INTO enrollment_status_history (id, enrollment_id, status, date, reason)
VALUES (9, 22, 'ACTIVE', '2026-08-22 11:00:00', 'Initial registration');
INSERT INTO enrollment_status_history (id, enrollment_id, status, date, reason)
VALUES (10, 22, 'CANCELLED', '2026-08-26 16:30:00', 'Cancelled due to administrative credit limit readjustment');

-- History entries for remaining active enrollments
INSERT INTO enrollment_status_history (id, enrollment_id, status, date, reason)
VALUES (11, 4, 'ACTIVE', '2026-02-11 08:30:00', 'Initial registration');
INSERT INTO enrollment_status_history (id, enrollment_id, status, date, reason)
VALUES (12, 4, 'COMPLETED', '2026-06-30 18:00:00', 'Completed successfully');

INSERT INTO enrollment_status_history (id, enrollment_id, status, date, reason)
VALUES (13, 5, 'ACTIVE', '2026-08-18 09:00:00', 'Initial registration for semester 2026-2');

INSERT INTO enrollment_status_history (id, enrollment_id, status, date, reason)
VALUES (14, 6, 'ACTIVE', '2026-02-12 11:00:00', 'Initial registration');
INSERT INTO enrollment_status_history (id, enrollment_id, status, date, reason)
VALUES (15, 6, 'COMPLETED', '2026-06-30 18:00:00', 'Term completed');

INSERT INTO enrollment_status_history (id, enrollment_id, status, date, reason)
VALUES (16, 7, 'ACTIVE', '2026-08-19 10:00:00', 'Initial registration for semester 2026-2');

INSERT INTO enrollment_status_history (id, enrollment_id, status, date, reason)
VALUES (17, 8, 'ACTIVE', '2026-02-10 09:30:00', 'Initial registration');
INSERT INTO enrollment_status_history (id, enrollment_id, status, date, reason)
VALUES (18, 8, 'COMPLETED', '2026-06-30 18:00:00', 'Term completed');

INSERT INTO enrollment_status_history (id, enrollment_id, status, date, reason)
VALUES (19, 9, 'ACTIVE', '2026-08-20 14:00:00', 'Initial registration for semester 2026-2');

INSERT INTO enrollment_status_history (id, enrollment_id, status, date, reason)
VALUES (20, 11, 'ACTIVE', '2026-02-10 11:30:00', 'Initial registration');
INSERT INTO enrollment_status_history (id, enrollment_id, status, date, reason)
VALUES (21, 11, 'COMPLETED', '2026-06-30 18:00:00', 'Term completed');

-- Adjust sequences if PostgreSQL sequences exist
SELECT setval('enrollments_id_seq', (SELECT COALESCE(MAX(id), 1) FROM enrollments));
SELECT setval('grades_id_seq', (SELECT COALESCE(MAX(id), 1) FROM grades));
SELECT setval('enrollment_status_history_id_seq', (SELECT COALESCE(MAX(id), 1) FROM enrollment_status_history));
