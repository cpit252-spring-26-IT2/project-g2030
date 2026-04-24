package sa.edu.kau.fcit.cpit252.project.department;

public class ERDepartment implements Department {

    @Override
    public String getName() {
        return "Emergency Room"; }

    @Override
    public void processFile(String fileName) {
        System.out.println("ER System: Prioritizing vitals for " + fileName);
    }
}
