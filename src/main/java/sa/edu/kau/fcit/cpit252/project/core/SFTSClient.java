package sa.edu.kau.fcit.cpit252.project.core;

import sa.edu.kau.fcit.cpit252.project.security.SecurityManager;
import java.io.*;
import java.net.Socket;
import java.nio.file.Files;

public class SFTSClient {
    public static void main(String[] args) {
        System.out.println("=== SFTS Client Starting Real File Transfer ===");

        // test
        File fileToSend = new File("test_image.png");

        if (!fileToSend.exists()) {
            System.err.println("Error: File does not exist! Please put 'test_image.png' in the project folder.");
            return;
        }

        // إعداد الاتصال (إذا بتنقل لجهاز ثاني، غير "localhost" إلى IP جهاز خويك)
        try (Socket socket = new Socket("localhost", 8080);
             DataOutputStream dos = new DataOutputStream(socket.getOutputStream())) {

            System.out.println("Connected to Server. Preparing to send file: " + fileToSend.getName());

            // 1. قراءة الملف الأصلي بالكامل وتحويله إلى Bytes
            byte[] originalBytes = Files.readAllBytes(fileToSend.toPath());

            // 2. تشفير الملف باستخدام AES
            System.out.println("Encrypting file before transmission...");
            byte[] encryptedBytes = SecurityManager.encryptData(originalBytes);
            // --------------------------------

            // 3. إرسال اسم وحجم الملف المشفر
            dos.writeUTF(fileToSend.getName());
            dos.writeLong(encryptedBytes.length); // مهم جداً: نرسل حجم الملف بعد التشفير

            // 4. إرسال البيانات المشفرة مباشرة
            dos.write(encryptedBytes);
            dos.flush();

            System.out.println("File '" + fileToSend.getName() + "' encrypted and sent successfully to the server!");

        } catch (Exception e) {
            System.err.println("Client Connection Error: " + e.getMessage());
        }
    }
}