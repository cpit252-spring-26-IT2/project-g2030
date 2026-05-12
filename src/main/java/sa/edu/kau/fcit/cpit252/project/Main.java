package sa.edu.kau.fcit.cpit252.project;

import sa.edu.kau.fcit.cpit252.project.model.*;
import sa.edu.kau.fcit.cpit252.project.department.*;
import sa.edu.kau.fcit.cpit252.project.proxy.*;
import sa.edu.kau.fcit.cpit252.project.security.SecurityManager;
import sa.edu.kau.fcit.cpit252.project.logger.AuditLogger;

import java.time.LocalDateTime;

public class Main {
    public static void main(String[] args) {
        // البداية: تهيئة نظام سجلات الرقابة (Singleton)
        AuditLogger logger = AuditLogger.getInstance();
        System.out.println("==================================================");
        System.out.println("   SFTS - Secure File Transfer System    ");
        System.out.println("==================================================\n");

        try {
            // 1. استخدام الـ Builder Pattern لبناء ملف طبي مع خصائص التشفير والوقت
            System.out.println("[STEP 1] Building Secure Medical File...");
            SecureFile patientFile = new FileBuilder()
                    .setFileId("PT-2026")
                    .setFileName("Confidential_Scan.pdf")
                    .setDepartment("Laboratory")
                    .setEncryptionType("AES-256") // تم تفعيلها في الـ Builder
                    .setExpiryTime(LocalDateTime.now().plusHours(2))   // صلاحية الوصول للملف
                    .build();

            System.out.println("File Created: " + patientFile.getFileName() + " [Encrypted with " + patientFile.getEncryptionType() + "]");

            // 2. محاكاة عملية التشفير الفعلي للبيانات (AES Encryption)
            System.out.println("\n[STEP 2] Encrypting raw data using SecurityManager...");
            String rawContent = "Patient ID: 9982 - Diagnosis: Normal Condition";
            byte[] encryptedData = SecurityManager.encryptData(rawContent.getBytes());
            System.out.println("Raw Data Encrypted Successfully (Ready for Network Transfer).");

            // 3. اختبار الوصول عبر الـ Proxy Pattern (السيناريو الأول: مستخدم مصرح له)
            System.out.println("\n[STEP 3] Testing Authorization (Authorized User)...");
            String activeUser = "Abdulaziz_Bukhari"; // أحد الأعضاء الموثوقين
            Department lab = DepartmentFactory.getDepartment("LAB");
            Department proxyAuthorized = new DepartmentProxy(lab, activeUser);

            System.out.println("Attempting to process file as: " + activeUser);
            proxyAuthorized.processFile(patientFile);

            // 4. اختبار الوصول عبر الـ Proxy Pattern (السيناريو الثاني: مستخدم غير معروف)
            System.out.println("\n[STEP 4] Testing Authorization (Unauthorized Access)...");
            String unknownUser = "Hacker_External";
            Department proxyUnauthorized = new DepartmentProxy(lab, unknownUser);

            System.out.println("Attempting to process file as: " + unknownUser);
            proxyUnauthorized.processFile(patientFile);

            // 5. فك التشفير عند الاستلام (Decryption)
            System.out.println("\n[STEP 5] Decrypting data at Destination...");
            byte[] decryptedData = SecurityManager.decryptData(encryptedData);
            System.out.println("Decrypted Content: " + new String(decryptedData));

        } catch (Exception e) {
            System.err.println("Critical System Error: " + e.getMessage());
        }

        System.out.println("\n==================================================");
        System.out.println("        System Process Completed Successfully     ");
        System.out.println("==================================================");
    }
}