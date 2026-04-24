package sa.edu.kau.fcit.cpit252.project.department;

public class DepartmentFactory {
    public static Department getDepartment(String type) {

        if (type == null) return null;
        return switch (type.toUpperCase()) {
            case "LAB" -> new LabDepartment();
            case "ER" -> new ERDepartment();
            default -> throw new IllegalArgumentException("Unknown Department: " + type);
        };
    }
}
