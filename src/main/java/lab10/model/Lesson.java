package lab10.model;

public record Lesson(int id, String day, int pairNumber, String classroom,
                     String subject, String teacher, int studentsCount) {

    @Override
    public String toString() {
        return String.format("#%-3d %-12s пара %d  ауд. %-4s %-28s %-30s студентов: %d",
                id, day, pairNumber, classroom, subject, teacher, studentsCount);
    }
}
