package sa.edu.kau.fcit.cpit252.project.logger;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class AuditLogger {
    private static AuditLogger instance;
    private final DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    private AuditLogger() {}

    public static synchronized AuditLogger getInstance() {
        if (instance == null) {
            instance = new AuditLogger();
        }
        return instance;
    }

    public void log(String user, String action) {
        String timestamp = LocalDateTime.now().format(formatter);
        System.out.println(String.format("[%s] [USER: %s] -> %s", timestamp, user, action));
    }
}
