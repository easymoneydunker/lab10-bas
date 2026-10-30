package lab10.dao;

import lab10.config.DatabaseConnection;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collection;

@Repository
public class ScheduleModifier {

    private static final String MOVE_FIRST_LESSONS_TO_END = """
            WITH bounds AS (SELECT MIN(pair_number) AS first_pair, MAX(pair_number) AS last_pair
                            FROM lessons
                            WHERE day_id = ?)
            UPDATE lessons l
            SET pair_number = b.last_pair + 1
            FROM bounds b
            WHERE l.day_id = ?
              AND l.pair_number = b.first_pair
              AND b.first_pair < b.last_pair
            """;

    private static final String INSERT_TEACHER =
            "INSERT INTO teachers (last_name, first_name, middle_name) VALUES (?, ?, ?) RETURNING id";

    private static final String ASSIGN_SUBJECT = """
            INSERT INTO teacher_subjects (teacher_id, subject_id) VALUES (?, ?)
            ON CONFLICT DO NOTHING
            """;

    private static final String INSERT_LESSON = """
            INSERT INTO lessons (teacher_id, subject_id, day_id, pair_number, classroom_id, students_count)
            VALUES (?, ?, ?, ?, (SELECT id FROM classrooms WHERE number = ?), ?)
            """;

    private static final String DELETE_LESSON = "DELETE FROM lessons WHERE id = ?";

    private final JdbcTemplate jdbc;

    public ScheduleModifier(DatabaseConnection connection) {
        this.jdbc = connection.getJdbcTemplate();
    }

    @Transactional
    public int moveFirstLessonsToEnd(Collection<Integer> dayIds) {
        int moved = 0;
        for (int dayId : dayIds) {
            moved += jdbc.update(MOVE_FIRST_LESSONS_TO_END, dayId, dayId);
        }
        return moved;
    }

    public int addTeacher(String lastName, String firstName, String middleName) {
        Integer id = jdbc.queryForObject(INSERT_TEACHER, Integer.class, lastName, firstName, middleName);
        return id == null ? -1 : id;
    }

    public void assignSubject(int teacherId, int subjectId) {
        jdbc.update(ASSIGN_SUBJECT, teacherId, subjectId);
    }

    @Transactional
    public void addLesson(int teacherId, int subjectId, int dayId, int pairNumber, String classroom, int students) {
        assignSubject(teacherId, subjectId);
        jdbc.update(INSERT_LESSON, teacherId, subjectId, dayId, pairNumber, classroom, students);
    }

    public boolean deleteLesson(int lessonId) {
        return jdbc.update(DELETE_LESSON, lessonId) > 0;
    }
}
