package lab10.dao;

import lab10.config.DatabaseConnection;
import lab10.model.DayStat;
import lab10.model.Lesson;
import lab10.model.Teacher;
import lab10.model.TeacherLoad;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public class ScheduleQueries {

    private static final RowMapper<Teacher> TEACHER_MAPPER = (rs, i) -> new Teacher(
            rs.getInt("id"),
            rs.getString("last_name"),
            rs.getString("first_name"),
            rs.getString("middle_name"));

    private static final RowMapper<DayStat> DAY_STAT_MAPPER = (rs, i) -> new DayStat(
            rs.getInt("id"), rs.getString("name"), rs.getInt("cnt"));

    private static final String SELECT_TEACHERS_BY_DAY_AND_ROOM = """
            SELECT DISTINCT t.id, t.last_name, t.first_name, t.middle_name
            FROM teachers t
                     JOIN lessons l ON l.teacher_id = t.id
                     JOIN classrooms c ON c.id = l.classroom_id
            WHERE l.day_id = ? AND c.number = ?
            ORDER BY t.last_name, t.first_name
            """;

    private static final String SELECT_TEACHERS_FREE_ON_DAY = """
            SELECT t.id, t.last_name, t.first_name, t.middle_name
            FROM teachers t
            WHERE NOT EXISTS (SELECT 1 FROM lessons l WHERE l.teacher_id = t.id AND l.day_id = ?)
            ORDER BY t.last_name, t.first_name
            """;

    private static final String SELECT_DAYS_BY_LESSON_COUNT = """
            SELECT d.id, d.name, COUNT(l.id) AS cnt
            FROM days_of_week d
                     LEFT JOIN lessons l ON l.day_id = d.id
            GROUP BY d.id, d.name
            HAVING COUNT(l.id) = ?
            ORDER BY d.id
            """;

    private static final String SELECT_DAYS_BY_CLASSROOM_COUNT = """
            SELECT d.id, d.name, COUNT(DISTINCT l.classroom_id) AS cnt
            FROM days_of_week d
                     LEFT JOIN lessons l ON l.day_id = d.id
            GROUP BY d.id, d.name
            HAVING COUNT(DISTINCT l.classroom_id) = ?
            ORDER BY d.id
            """;

    private static final String SELECT_SCHEDULE = """
            SELECT l.id, d.name AS day, l.pair_number, c.number AS classroom, s.name AS subject,
                   t.last_name || ' ' || t.first_name || COALESCE(' ' || t.middle_name, '') AS teacher,
                   l.students_count
            FROM lessons l
                     JOIN days_of_week d ON d.id = l.day_id
                     JOIN classrooms c ON c.id = l.classroom_id
                     JOIN subjects s ON s.id = l.subject_id
                     JOIN teachers t ON t.id = l.teacher_id
            ORDER BY l.day_id, l.pair_number, c.number
            """;

    private static final String SELECT_TEACHER_LOAD = """
            SELECT t.last_name || ' ' || t.first_name || COALESCE(' ' || t.middle_name, '') AS teacher,
                   s.name AS subject,
                   tl.pairs_per_week,
                   COALESCE((SELECT string_agg(l.students_count::text, ', ' ORDER BY l.day_id, l.pair_number)
                             FROM lessons l
                             WHERE l.teacher_id = tl.teacher_id AND l.subject_id = tl.subject_id), '-') AS students
            FROM teacher_load tl
                     JOIN teachers t ON t.id = tl.teacher_id
                     JOIN subjects s ON s.id = tl.subject_id
            ORDER BY t.last_name, s.name
            """;

    private final JdbcTemplate jdbc;

    public ScheduleQueries(DatabaseConnection connection) {
        this.jdbc = connection.getJdbcTemplate();
    }

    public List<Teacher> findTeachersByDayAndClassroom(int dayId, String classroom) {
        return jdbc.query(SELECT_TEACHERS_BY_DAY_AND_ROOM, TEACHER_MAPPER, dayId, classroom);
    }

    public List<Teacher> findTeachersFreeOnDay(int dayId) {
        return jdbc.query(SELECT_TEACHERS_FREE_ON_DAY, TEACHER_MAPPER, dayId);
    }

    public List<DayStat> findDaysByLessonCount(int count) {
        return jdbc.query(SELECT_DAYS_BY_LESSON_COUNT, DAY_STAT_MAPPER, count);
    }

    public List<DayStat> findDaysByClassroomCount(int count) {
        return jdbc.query(SELECT_DAYS_BY_CLASSROOM_COUNT, DAY_STAT_MAPPER, count);
    }

    public List<Lesson> findSchedule() {
        return jdbc.query(SELECT_SCHEDULE, (rs, i) -> new Lesson(
                rs.getInt("id"), rs.getString("day"), rs.getInt("pair_number"), rs.getString("classroom"),
                rs.getString("subject"), rs.getString("teacher"), rs.getInt("students_count")));
    }

    public List<TeacherLoad> findTeacherLoad() {
        return jdbc.query(SELECT_TEACHER_LOAD, (rs, i) -> new TeacherLoad(
                rs.getString("teacher"), rs.getString("subject"),
                rs.getInt("pairs_per_week"), rs.getString("students")));
    }
}
