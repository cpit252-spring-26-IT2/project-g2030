package sa.edu.kau.fcit.cpit252.project.core;
// كلاس داخلي يمثل الـ Isolated Thread لكل عملية
import java.io.*;
import sa.edu.kau.fcit.cpit252.project.model.SecureFile;
import sa.edu.kau.fcit.cpit252.project.model.FileBuilder;
import sa.edu.kau.fcit.cpit252.project.department.*;
import sa.edu.kau.fcit.cpit252.project.proxy.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import sa.edu.kau.fcit.cpit252.project.department.*;
import sa.edu.kau.fcit.cpit252.project.proxy.*;
// استدعاء الكلاسات الخاصة بالملف والـ Builder (مهم جداً لحل الإيرور)
import sa.edu.kau.fcit.cpit252.project.model.SecureFile;
import sa.edu.kau.fcit.cpit252.project.model.FileBuilder;

public class SFTSServer {
    // تحديد منفذ الاتصال للخادم المركزي
    private static final int PORT = 8080;
    // إنشاء Thread Pool بحد أقصى 10 مسارات معزولة (Isolated Threads)
    private static final ExecutorService threadPool = Executors.newFixedThreadPool(10);

    public static void startServer() {
        try (ServerSocket serverSocket = new ServerSocket(PORT)) {
            System.out.println("=== Central Server Started on Port " + PORT + " ===");

            while (true) {
                // انتظار اتصال جديد
                Socket clientSocket = serverSocket.accept();

                // عزل معالجة البيانات في Thread منفصل لضمان استقرار الإرسال
                threadPool.execute(new ClientHandler(clientSocket));
            }
        } catch (Exception e) {
            System.err.println("Server Exception: " + e.getMessage());
        }
    }
}



// كلاس داخلي يمثل الـ Isolated Thread لكل عملية
class ClientHandler implements Runnable {
    private final Socket socket;

    public ClientHandler(Socket socket) {
        this.socket = socket;
    }

    @Override
    public void run() {
        String threadName = Thread.currentThread().getName();
        System.out.println("[" + threadName + "] Connection established. Preparing to receive file...");

        try (DataInputStream dis = new DataInputStream(socket.getInputStream())) {

            // 1. استقبال معلومات الملف
            String fileName = dis.readUTF();
            long fileSize = dis.readLong();
            System.out.println("[" + threadName + "] Incoming real file: " + fileName + " (" + fileSize + " bytes)");

            // --- تطبيق الحماية والـ Design Patterns اللي سويناها ---
            SecureFile networkFile = new FileBuilder()
                    .setId("NET-1010")
                    .setName(fileName)
                    .setDept("Laboratory")
                    .setEncryption("AES-256")
                    .setUrgent(true)
                    .build();

            Department lab = DepartmentFactory.getDepartment("LAB");
            Department proxyAuthorized = new DepartmentProxy(lab, "Abdulaziz_Bukhari");
            proxyAuthorized.processFile(networkFile); // التحقق من الصلاحية
            // ----------------------------------------------------

            // 2. إنشاء المجلد لحفظ الملفات المستقبلة (إذا مو موجود)
            File directory = new File("Server_Received_Files");
            if (!directory.exists()) {
                directory.mkdir();
            }

            // 3. تجهيز مسار الحفظ
            File fileToSave = new File(directory, "Secure_" + fileName);

            // 4. تحميل وحفظ الـ Bytes كملف حقيقي في جهازك
            try (FileOutputStream fos = new FileOutputStream(fileToSave)) {
                byte[] buffer = new byte[4096];
                int bytesRead;
                long totalRead = 0;

                while (totalRead < fileSize && (bytesRead = dis.read(buffer, 0, (int)Math.min(buffer.length, fileSize - totalRead))) != -1) {
                    fos.write(buffer, 0, bytesRead);
                    totalRead += bytesRead;
                }
            }

            System.out.println("[" + threadName + "] SUCCESS: Real File downloaded and saved at -> " + fileToSave.getAbsolutePath() + "\n");

        } catch (Exception e) {
            System.err.println("[" + threadName + "] Transfer Error: " + e.getMessage());
        } finally {
            try { socket.close(); } catch (IOException e) { e.printStackTrace(); }
        }
    }
}