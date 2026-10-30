package lab10.dao;

import lab10.config.DatabaseConnection;
import lab10.model.Lesson;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Repository;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

@Repository
public class ScheduleJdbcReader {

    private static final Logger log = LoggerFactory.getLogger(ScheduleJdbcReader.class);

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

    private final DatabaseConnection databaseConnection;

    public ScheduleJdbcReader(DatabaseConnection databaseConnection) {
        this.databaseConnection = databaseConnection;
    }

    public List<Lesson> findSchedule() throws SQLException {
        log.info("Чтение расписания через JDBC");
        List<Lesson> lessons = new ArrayList<>();
        try (Connection connection = databaseConnection.getJdbcConnection();
             PreparedStatement statement = connection.prepareStatement(SELECT_SCHEDULE);
             ResultSet rs = statement.executeQuery()) {
            while (rs.next()) {
                lessons.add(new Lesson(
                        rs.getInt("id"),
                        rs.getString("day"),
                        rs.getInt("pair_number"),
                        rs.getString("classroom"),
                        rs.getString("subject"),
                        rs.getString("teacher"),
                        rs.getInt("students_count")));
            }
        } catch (SQLException e) {
            log.error("Ошибка чтения расписания", e);
            throw e;
        }
        log.info("Прочитано занятий: {}", lessons.size());
        return lessons;
    }
}
