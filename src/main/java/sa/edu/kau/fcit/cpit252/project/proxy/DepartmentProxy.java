package sa.edu.kau.fcit.cpit252.project.proxy;

import sa.edu.kau.fcit.cpit252.project.department.Department;
import sa.edu.kau.fcit.cpit252.project.logger.AuditLogger;
import sa.edu.kau.fcit.cpit252.project.model.SecureFile; // لازم نضيف هذا عشان نقدر نستخدم كائن الملف

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
    public void processFile(SecureFile file) {

        if (!hasAccess()) {
            audit.log(currentUser, "SECURITY ALERT: Access Denied to " + realDepartment.getName());
            System.err.println("Transaction Blocked: " + currentUser + " does not have clearance for " + realDepartment.getName());
            return;
        }

        if (!file.openFile()) {
            audit.log(currentUser, "ALERT: Max view limit reached for file: " + file.getFileName());
            System.err.println("Access Revoked: The viewing limit for " + file.getFileName() + " has been reached.");
            return;
        }

        if (file.isViewOnly()) {
            audit.log(currentUser, "Opened file in View-Only Secure Web Viewer");
            System.out.println("System Notice: " + file.getFileName() + " is opened in a secure web-based viewer (Downloads Disabled).");
        } else {
            audit.log(currentUser, "Access Granted with full permissions");
        }

        realDepartment.processFile(file);
    }

    private boolean hasAccess() {
        if (realDepartment.getName().equals("Laboratory")) {
            return currentUser.equals("Abdulaziz_Bukhari") || currentUser.equals("Abdulmalik_Aldahari") || currentUser.equals("Motaz_Alsayed") ;
        }
        return true;
    }
}