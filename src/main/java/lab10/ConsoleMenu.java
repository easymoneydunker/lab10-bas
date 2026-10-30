package lab10;

import lab10.config.DatabaseConnection;
import lab10.dao.ScheduleModifier;
import lab10.dao.ScheduleQueries;
import org.springframework.boot.CommandLineRunner;
import org.springframework.dao.DataAccessException;
import org.springframework.stereotype.Component;

import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

@Component
public class ConsoleMenu implements CommandLineRunner {

    private static final String DAYS_HINT =
            "1-Пн, 2-Вт, 3-Ср, 4-Чт, 5-Пт, 6-Сб, 7-Вс";

    private final DatabaseConnection databaseConnection;
    private final ScheduleQueries queries;
    private final ScheduleModifier modifier;
    private final Scanner in = new Scanner(System.in);

    public ConsoleMenu(DatabaseConnection databaseConnection, ScheduleQueries queries, ScheduleModifier modifier) {
        this.databaseConnection = databaseConnection;
        this.queries = queries;
        this.modifier = modifier;
    }

    @Override
    public void run(String... args) throws SQLException {
        printConnectionInfo();
        while (true) {
            System.out.println("""

                    ===== Расписание занятий =====
                    1. Показать расписание
                    2. Нагрузка преподавателей (пар в неделю, студентов на парах)
                    3. Преподаватели, работающие в заданный день в заданной аудитории
                    4. Преподаватели, не ведущие занятия в заданный день
                    5. Дни недели с заданным количеством занятий
                    6. Дни недели с заданным количеством занятых аудиторий
                    7. Перенести первые занятия заданных дней на последнее место
                    8. Добавить занятие
                    9. Удалить занятие
                    0. Выход""");
            String choice = prompt("Выберите пункт: ");
            if (choice == null || choice.equals("0")) {
                return;
            }
            try {
                handle(choice);
            } catch (NumberFormatException e) {
                System.out.println("Ошибка: ожидалось число");
            } catch (DataAccessException e) {
                System.out.println("Ошибка БД: " + e.getMostSpecificCause().getMessage());
            }
        }
    }

    private void handle(String choice) {
        switch (choice) {
            case "1" -> print(queries.findSchedule());
            case "2" -> print(queries.findTeacherLoad());
            case "3" -> {
                int day = readDay();
                String room = prompt("Аудитория: ");
                print(queries.findTeachersByDayAndClassroom(day, room));
            }
            case "4" -> print(queries.findTeachersFreeOnDay(readDay()));
            case "5" -> print(queries.findDaysByLessonCount(readInt("Количество занятий: ")));
            case "6" -> print(queries.findDaysByClassroomCount(readInt("Количество аудиторий: ")));
            case "7" -> {
                List<Integer> days = new ArrayList<>();
                for (String s : prompt("Дни через пробел (" + DAYS_HINT + "): ").trim().split("[\\s,]+")) {
                    days.add(Integer.parseInt(s));
                }
                System.out.println("Перенесено занятий: " + modifier.moveFirstLessonsToEnd(days));
            }
            case "8" -> {
                modifier.addLesson(
                        readInt("id преподавателя: "),
                        readInt("id предмета: "),
                        readDay(),
                        readInt("Номер пары: "),
                        prompt("Аудитория: "),
                        readInt("Количество студентов: "));
                System.out.println("Занятие добавлено");
            }
            case "9" -> System.out.println(modifier.deleteLesson(readInt("id занятия: "))
                    ? "Занятие удалено" : "Занятие не найдено");
            default -> System.out.println("Нет такого пункта");
        }
    }

    private void printConnectionInfo() throws SQLException {
        Connection connection = databaseConnection.getConnection();
        try {
            DatabaseMetaData meta = connection.getMetaData();
            System.out.printf("Подключено: %s %s, %s%n",
                    meta.getDatabaseProductName(), meta.getDatabaseProductVersion(), meta.getURL());
        } finally {
            databaseConnection.releaseConnection(connection);
        }
    }

    private int readDay() {
        return readInt("День недели (" + DAYS_HINT + "): ");
    }

    private int readInt(String message) {
        return Integer.parseInt(prompt(message).trim());
    }

    private String prompt(String message) {
        System.out.print(message);
        return in.hasNextLine() ? in.nextLine() : null;
    }

    private static void print(List<?> rows) {
        if (rows.isEmpty()) {
            System.out.println("Ничего не найдено");
        }
        rows.forEach(System.out::println);
    }
}
