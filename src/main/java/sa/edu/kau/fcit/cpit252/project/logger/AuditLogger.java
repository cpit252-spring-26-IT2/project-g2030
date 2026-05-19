package sa.edu.kau.fcit.cpit252.project.logger;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class AuditLogger {

    private static AuditLogger instance;
    private static final DateTimeFormatter FMT = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    private final List<String> logs = new ArrayList<>();

    private AuditLogger() {}

    public static synchronized AuditLogger getInstance() {
        if (instance == null) instance = new AuditLogger();
        return instance;
    }

    // ── Log ───────────────────────────────────────────────────────────────────
    public synchronized void log(String user, String action) {
        String entry = String.format("[%s] [USER: %-10s] -> %s",
                LocalDateTime.now().format(FMT), user, action);
        logs.add(entry);
        System.out.println(entry);
    }

    // ── Read ──────────────────────────────────────────────────────────────────
    public synchronized List<String> getLogs() {
        return Collections.unmodifiableList(logs);
    }

    public synchronized String getLogsAsString() {
        StringBuilder sb = new StringBuilder();
        sb.append("╔══════════════════════════════════════╗\n");
        sb.append("║          Audit Logger — Logs         ║\n");
        sb.append("╚══════════════════════════════════════╝\n\n");
        for (String log : logs) {
            sb.append(log).append("\n");
        }
        return sb.toString();
    }

    public synchronized void clear() {
        logs.clear();
    }
}