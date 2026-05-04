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
    public void processFile(SecureFile file) { // التعديل هنا: صار يستقبل كائن SecureFile كامل

        // 1. أول شيء نشيك إذا المستخدم عنده صلاحية يدخل القسم أساساً
        if (!hasAccess()) {
            audit.log(currentUser, "SECURITY ALERT: Access Denied to " + realDepartment.getName());
            System.err.println("Transaction Blocked: " + currentUser + " does not have clearance for " + realDepartment.getName());
            return; // نوقف التنفيذ هنا ما نخليه يكمل
        }

        // 2. تطبيق طلب الدكتور الأول (Quantitative View Limits): نشيك إذا الملف تعدى عدد المشاهدات
        if (!file.canView()) {
            audit.log(currentUser, "ALERT: Max view limit reached for file: " + file.getName());
            System.err.println("Access Revoked: The viewing limit for " + file.getName() + " has been reached.");
            return; // نوقف التنفيذ لأن محاولات المشاهدة خلصت
        }

        // 3. تطبيق طلب الدكتور الثاني (Download Restrictions): نشيك إذا الملف للقراءة فقط
        if (file.isViewOnly()) {
            audit.log(currentUser, "Opened file in View-Only Secure Web Viewer");
            System.out.println("System Notice: " + file.getName() + " is opened in a secure web-based viewer (Downloads Disabled).");
        } else {
            audit.log(currentUser, "Access Granted with full permissions");
        }

        // إذا كل الشروط فوق تمام، نمرر الملف للقسم الفعلي عشان يعالجه
        realDepartment.processFile(file);
    }

    // نفس اللوجيك حقك في الصلاحيات ما غيرنا فيه شيء
    private boolean hasAccess() {
        // قسم المختبر محمي للمستخدمين هذولي بس
        if (realDepartment.getName().equals("Laboratory")) {
            return currentUser.equals("Abdulaziz_Bukhari") || currentUser.equals("Admin");
        }
        // الطوارئ مفتوح للكل
        return true;
    }
}