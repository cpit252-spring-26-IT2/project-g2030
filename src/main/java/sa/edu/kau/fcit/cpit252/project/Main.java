package sa.edu.kau.fcit.cpit252.project;

import sa.edu.kau.fcit.cpit252.project.model.*;
import sa.edu.kau.fcit.cpit252.project.department.*;
import sa.edu.kau.fcit.cpit252.project.proxy.*;

public class Main {
    public static void main(String[] args) {
        System.out.println("=== Secure File Transfer System (SFTS) Initialized ===\n");

        // 1. Using Builder Pattern to create a new medical file
        // 1. Using Builder Pattern
        System.out.println("--- 1. Building Secure File ---");
        SecureFile patientFile = new FileBuilder()
                .setId("PT-9982")
                .setName("MRI_Scan_Results.pdf")
                .setDept("Laboratory")
                .setEncryption("AES-256")
                .setUrgent(true)
                .setMaxViews(2) // خليناها 2 عشان نجرب الحد
                .setViewOnly(true) // نفعل خاصية منع التحميل
                .build();
        System.out.println("System Log: " + patientFile.toString() + " created successfully.\n");

        // 2. Using Factory Pattern to get the target department
        System.out.println("--- 2. Routing to Department ---");
        Department labDepartment = DepartmentFactory.getDepartment("LAB");
        System.out.println("System Log: Routed to " + labDepartment.getName() + ".\n");

        // 3. Using Proxy Pattern (and Singleton Logger internally) for Authorized Access
        System.out.println("--- 3. Attempting Authorized Access ---");
        Department authorizedAccess = new DepartmentProxy(labDepartment, "Abdulaziz_Bukhari");

        // التجربة الأولى (بتنجح ويقولك للعرض فقط)
        authorizedAccess.processFile(patientFile);
        System.out.println();

        // التجربة الثانية (بتنجح)
        authorizedAccess.processFile(patientFile);
        System.out.println();

        // التجربة الثالثة (بترفض لأن الحد الأقصى 2)
        System.out.println("--- Attempting 3rd Access (Should Fail) ---");
        authorizedAccess.processFile(patientFile);
        System.out.println();

        System.out.println("--- 4. Attempting Unauthorized Access ---");
        Department unauthorizedAccess = new DepartmentProxy(labDepartment, "Motaz_Alsayed");
        unauthorizedAccess.processFile(patientFile);

        System.out.println("\n=== System Shutting Down ===");
    }
}