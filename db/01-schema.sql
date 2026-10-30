-- Расписание занятий. Схема приведена к 3НФ:
-- справочники (дни недели, аудитории, предметы, преподаватели) вынесены в отдельные таблицы,
-- связь "преподаватель ведет предмет" - отдельная таблица,
-- занятие (пара) ссылается на справочники по ключам.
-- Количество пар в неделю по предмету не хранится отдельно (это производная величина),
-- а вычисляется представлением teacher_load.

CREATE TABLE days_of_week (
    id   SMALLINT PRIMARY KEY CHECK (id BETWEEN 1 AND 7),
    name VARCHAR(20) NOT NULL UNIQUE
);

CREATE TABLE classrooms (
    id     SERIAL PRIMARY KEY,
    number VARCHAR(10) NOT NULL UNIQUE
);

CREATE TABLE subjects (
    id   SERIAL PRIMARY KEY,
    name VARCHAR(100) NOT NULL UNIQUE
);

CREATE TABLE teachers (
    id          SERIAL PRIMARY KEY,
    last_name   VARCHAR(50) NOT NULL,
    first_name  VARCHAR(50) NOT NULL,
    middle_name VARCHAR(50)
);

CREATE TABLE teacher_subjects (
    teacher_id INT NOT NULL REFERENCES teachers (id) ON DELETE CASCADE,
    subject_id INT NOT NULL REFERENCES subjects (id) ON DELETE CASCADE,
    PRIMARY KEY (teacher_id, subject_id)
);

CREATE TABLE lessons (
    id             SERIAL PRIMARY KEY,
    teacher_id     INT      NOT NULL,
    subject_id     INT      NOT NULL,
    day_id         SMALLINT NOT NULL REFERENCES days_of_week (id),
    pair_number    SMALLINT NOT NULL CHECK (pair_number > 0),
    classroom_id   INT      NOT NULL REFERENCES classrooms (id),
    students_count INT      NOT NULL CHECK (students_count >= 0),
    FOREIGN KEY (teacher_id, subject_id) REFERENCES teacher_subjects (teacher_id, subject_id) ON DELETE CASCADE,
    CONSTRAINT uq_room_slot UNIQUE (day_id, pair_number, classroom_id),
    CONSTRAINT uq_teacher_slot UNIQUE (day_id, pair_number, teacher_id)
);

CREATE VIEW teacher_load AS
SELECT ts.teacher_id,
       ts.subject_id,
       COUNT(l.id)                          AS pairs_per_week,
       COALESCE(SUM(l.students_count), 0)   AS students_total
FROM teacher_subjects ts
         LEFT JOIN lessons l ON l.teacher_id = ts.teacher_id AND l.subject_id = ts.subject_id
GROUP BY ts.teacher_id, ts.subject_id;
