package sa.edu.kau.fcit.cpit252.project.department;
import sa.edu.kau.fcit.cpit252.project.model.SecureFile;
public class ERDepartment implements Department {

    @Override
    public String getName() {
        return "Emergency Room";
    }

    @Override
    public void processFile(SecureFile file) {
        System.out.println("ER System: Prioritizing vitals for " + file.getName());
    }
}
