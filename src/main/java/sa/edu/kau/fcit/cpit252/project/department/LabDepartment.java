package sa.edu.kau.fcit.cpit252.project.department;
import sa.edu.kau.fcit.cpit252.project.model.SecureFile;
public class LabDepartment implements Department {

    @Override
    public String getName() {
        return "Laboratory";
    }

    @Override
    public void processFile(SecureFile file) {
        System.out.println("Lab System: Analyzing results for " + file.getName());
    }
}
