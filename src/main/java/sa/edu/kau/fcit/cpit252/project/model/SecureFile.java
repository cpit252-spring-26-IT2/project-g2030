package sa.edu.kau.fcit.cpit252.project.model;

import java.time.LocalDateTime;
import java.time.Duration; // استدعاء مكتبة حساب المدة

public class SecureFile {
    private final String id;
    private final String name;
    private final String department;
    private final String sourcePath;
    private final String targetIP;
    private final LocalDateTime expirationTime;
    private final String encryption;
    private final boolean viewOnly;
    private final int maxViews;
    private int currentViews = 0;

    protected SecureFile(String id, String name, String department, String sourcePath, String targetIP, LocalDateTime expirationTime, String encryption, boolean viewOnly, int maxViews) {
        this.id = id;
        this.name = name;
        this.department = department;
        this.sourcePath = sourcePath;
        this.targetIP = targetIP;
        this.expirationTime = expirationTime;
        this.encryption = encryption;
        this.viewOnly = viewOnly;
        this.maxViews = maxViews;
    }

    public String getId() {
        return id; }

    public String getName() {
        return name; }

    public String getEncryption() {
        return encryption; }

    public boolean isViewOnly() {
        return viewOnly; }

    public boolean canView() {
        if (expirationTime != null && LocalDateTime.now().isAfter(expirationTime)) {
            return false;
        }
        if (maxViews > 0 && currentViews >= maxViews) {
            return false;
        }
        return true;
    }

    public void incrementView() {
        this.currentViews++;
    }

    @Override
    public String toString() {
        String viewsStr = String.format("(Views: %d/%s)", currentViews, (maxViews == 0 ? "∞" : maxViews));
        String timeStr = "";

        if (expirationTime != null) {
            Duration duration = Duration.between(LocalDateTime.now(), expirationTime);
            if (duration.isNegative() || duration.isZero()) {
                timeStr = " ❌ [EXPIRED]";
            } else {
                long hours = duration.toHours();
                long minutes = duration.toMinutesPart();
                long seconds = duration.toSecondsPart();
                timeStr = String.format(" ⏳ [Expires in: %02d:%02d:%02d]", hours, minutes, seconds);
            }
        } else {
            timeStr = " ♾️ [No Expiry]";
        }

        return name + "  " + viewsStr + timeStr;
    }
}