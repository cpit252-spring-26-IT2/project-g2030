package sa.edu.kau.fcit.cpit252.project.core;

import java.io.*;
import java.net.Socket;

public class SFTSClient {
    public static void main(String[] args) {
        System.out.println("=== SFTS Client Starting Real File Transfer ===");

        // اسم الملف اللي بنرسله (تأكد إنه موجود في مجلد المشروع الرئيسي)
        File fileToSend = new File("test_image.png");

        if (!fileToSend.exists()) {
            System.err.println("Error: File does not exist! Please put 'test_image.png' in the project folder.");
            return;
        }

        // إعداد الاتصال (إذا بتنقل لجهاز ثاني، غير "localhost" إلى IP جهاز خويك)
        try (Socket socket = new Socket("localhost", 8080);
             DataOutputStream dos = new DataOutputStream(socket.getOutputStream());
             FileInputStream fis = new FileInputStream(fileToSend)) {

            System.out.println("Connected to Server. Sending file: " + fileToSend.getName());

            // 1. إرسال اسم وحجم الملف
            dos.writeUTF(fileToSend.getName());
            dos.writeLong(fileToSend.length());

            // 2. قراءة الملف كـ Bytes وإرسالها
            byte[] buffer = new byte[4096];
            int bytesRead;
            while ((bytesRead = fis.read(buffer)) != -1) {
                dos.write(buffer, 0, bytesRead);
            }

            System.out.println("File '" + fileToSend.getName() + "' sent successfully to the server!");

        } catch (Exception e) {
            System.err.println("Client Connection Error: " + e.getMessage());
        }
    }
}