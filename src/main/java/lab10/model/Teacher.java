package lab10.model;

public record Teacher(int id, String lastName, String firstName, String middleName) {

    public String fullName() {
        return middleName == null
                ? lastName + " " + firstName
                : lastName + " " + firstName + " " + middleName;
    }

    @Override
    public String toString() {
        return String.format("#%d %s", id, fullName());
    }
}
