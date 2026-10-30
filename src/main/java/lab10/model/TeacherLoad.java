package lab10.model;

/** Предмет преподавателя: количество пар в неделю и студентов на каждой паре. */
public record TeacherLoad(String teacher, String subject, int pairsPerWeek, String studentsPerPair) {

    @Override
    public String toString() {
        return String.format("%-30s %-28s пар в неделю: %d, студентов на парах: %s",
                teacher, subject, pairsPerWeek, studentsPerPair);
    }
}
