package lab10.model;

/** День недели и значение некоторого показателя (количество занятий, аудиторий). */
public record DayStat(int dayId, String day, int value) {

    @Override
    public String toString() {
        return String.format("%d. %s (%d)", dayId, day, value);
    }
}
