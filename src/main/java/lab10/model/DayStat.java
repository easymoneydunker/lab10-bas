package lab10.model;

public record DayStat(int dayId, String day, int value) {

    @Override
    public String toString() {
        return String.format("%d. %s (%d)", dayId, day, value);
    }
}
