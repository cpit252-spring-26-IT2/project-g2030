package sa.edu.kau.fcit.cpit252.project.core;

import sa.edu.kau.fcit.cpit252.project.model.SecureFile;
import sa.edu.kau.fcit.cpit252.project.model.FileBuilder;
import sa.edu.kau.fcit.cpit252.project.department.*;
import sa.edu.kau.fcit.cpit252.project.proxy.*;
import sa.edu.kau.fcit.cpit252.project.security.SecurityManager;

import java.io.DataInputStream;
import java.net.Socket;

public class ClientHandler implements Runnable {
    private final Socket socket;

    public ClientHandler(Socket socket) {
        this.socket = socket;
    }

    @Override
    public void run() {
        try (DataInputStream dis = new DataInputStream(socket.getInputStream())) {
            // 1. استقبال البيانات
            String fileName = dis.readUTF();
            long fileSize = dis.readLong();
            int expHours = dis.readInt();
            int maxViews = dis.readInt();
            boolean isViewOnly = dis.readBoolean();

            // 2. بناء الملف بالقيود الأمنية
            String fileId = "NET-" + (int)(Math.random()*1000);
            SecureFile networkFile = new FileBuilder()
                    .setId(fileId)
                    .setName(fileName)
                    .setDept("Laboratory")
                    .setEncryption("AES-256")
                    .setExpirationHours(expHours)
                    .setMaxViews(maxViews)
                    .setViewOnly(isViewOnly)
                    .build();

            // 3. فك التشفير
            byte[] encryptedBuffer = new byte[(int) fileSize];
            dis.readFully(encryptedBuffer);
            byte[] decryptedBytes = SecurityManager.decryptData(encryptedBuffer);

            // 4. تطبيق البروكسي للتأكد من الصلاحيات
            Department lab = DepartmentFactory.getDepartment("LAB");
            Department proxyAuthorized = new DepartmentProxy(lab, "Abdulaziz_Bukhari");
            proxyAuthorized.processFile(networkFile);

            // 5. توجيه الملف للخزنة مباشرة (بدون أي فتح تلقائي يحرق العداد!)
            SFTSServer.addFileToVault(networkFile, decryptedBytes);

        } catch (Exception e) {
            System.err.println("Transfer Error: " + e.getMessage());
        }
    }
}