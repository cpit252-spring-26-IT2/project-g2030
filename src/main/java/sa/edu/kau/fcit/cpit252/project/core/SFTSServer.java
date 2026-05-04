package sa.edu.kau.fcit.cpit252.project.core;

import java.net.ServerSocket;
import java.net.Socket;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import sa.edu.kau.fcit.cpit252.project.department.*;
import sa.edu.kau.fcit.cpit252.project.proxy.*;

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
            System.out.println("[" + threadName + "] Processing secure data transmission...");

            try {
                // هنا ندمج مع الكلاسات الحالية (Proxy & Factory)
                Department lab = DepartmentFactory.getDepartment("LAB");
                // محاكاة طلب قادم من الشبكة لمستخدم مصرح له
                Department proxyAuthorized = new DepartmentProxy(lab, "Abdulaziz_Bukhari");
                proxyAuthorized.processFile("Network_Received_Scan.pdf");

                // إغلاق الاتصال بأمان بعد الانتهاء
                socket.close();
                System.out.println("[" + threadName + "] Transmission and processing completed safely.\n");

            } catch (Exception e) {
                System.err.println("[" + threadName + "] Transmission Error: " + e.getMessage());
            }
        }
}
