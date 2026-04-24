package sa.edu.kau.fcit.cpit252.project.proxy;
import sa.edu.kau.fcit.cpit252.project.department.Department;
import sa.edu.kau.fcit.cpit252.project.logger.AuditLogger;


public class DepartmentProxy implements Department {
    private final Department realDepartment;
    private final String currentUser;
    private final AuditLogger audit = AuditLogger.getInstance();

    public DepartmentProxy(Department realDepartment, String currentUser) {
        this.realDepartment = realDepartment;
        this.currentUser = currentUser;
    }

    @Override
    public String getName() {
        return realDepartment.getName() + " (Secured)";
    }

    @Override
    public void processFile(String fileName) {
        // The Proxy intercepts the call to check permissions first
        if (hasAccess()) {
            audit.log(currentUser, "Access Granted to process file in " + realDepartment.getName());
            realDepartment.processFile(fileName);
        } else {
            audit.log(currentUser, "SECURITY ALERT: Access Denied to " + realDepartment.getName());
            System.err.println("Transaction Blocked: " + currentUser + " does not have clearance for " + realDepartment.getName());
        }
    }

    // Simple access control logic
    private boolean hasAccess() {
        // Let's pretend only 'Admin' or 'Abdulaziz_Bukhari' can access the Laboratory
        if (realDepartment.getName().equals("Laboratory")) {
            return currentUser.equals("Abdulaziz_Bukhari") || currentUser.equals("Admin");
        }
        // Emergency Room is open access for all staff
        return true;
    }
}