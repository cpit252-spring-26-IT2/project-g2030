package sa.edu.kau.fcit.cpit252.project.core;
import java.net.Socket;
public class SFTSClient {
    public static void main(String[] args) {
        System.out.println("=== SFTS Test Clients Starting ===");

        // محاكاة 3 أقسام أو مستخدمين يرسلون ملفات في نفس اللحظة
        for (int i = 1; i <= 3; i++) {
            final int clientId = i;

            // إنشاء Client Thread منفصل
            new Thread(() -> {
                try {
                    System.out.println("Client " + clientId + " is attempting to connect...");
                    // الاتصال بالسيرفر المحلي على المنفذ 8080
                    Socket socket = new Socket("localhost", 8080);

                    // إبقاء الاتصال مفتوح قليلاً لمحاكاة نقل البيانات
                    Thread.sleep(500);

                    socket.close();
                } catch (Exception e) {
                    System.err.println("Client " + clientId + " Connection Failed: " + e.getMessage());
                }
            }).start();
        }
    }
}
