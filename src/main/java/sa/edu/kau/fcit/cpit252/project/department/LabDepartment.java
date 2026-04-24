package sa.edu.kau.fcit.cpit252.project.department;

public class LabDepartment implements Department {

    @Override
    public String getName() {

        return "Laboratory";
    }

    @Override
    public void processFile(String fileName) {

        System.out.println("Lab System: Analyzing results for " + fileName);
    }
}
