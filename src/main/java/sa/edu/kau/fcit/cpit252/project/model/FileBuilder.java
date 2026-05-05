package sa.edu.kau.fcit.cpit252.project.model;

import java.time.LocalDateTime;

public class FileBuilder {
    private String id;
    private String name;
    private String department;
    private String sourcePath;
    private String targetIP = "localhost";
    private LocalDateTime expirationTime = null;
    private String encryption = "None";
    private boolean viewOnly = false;
    private int maxViews = 0;

    public FileBuilder setId(String id) {
        this.id = id; return this; }

    public FileBuilder setName(String name) {
        this.name = name; return this; }

    public FileBuilder setDept(String dept) {
        this.department = dept; return this; }

    public FileBuilder setSourcePath(String sourcePath)
    { this.sourcePath = sourcePath; return this; }

    public FileBuilder setTargetIP(String ip) {
        this.targetIP = ip; return this; }

    public FileBuilder setEncryption(String encryption)
    { this.encryption = encryption; return this; }

    public FileBuilder setExpirationHours(int hours) {
        if (hours > 0) {
            this.expirationTime = LocalDateTime.now().plusHours(hours);
        }
        return this;
    }

    public FileBuilder setViewOnly(boolean viewOnly) {
        this.viewOnly = viewOnly;
        return this;
    }

    public FileBuilder setMaxViews(int maxViews) {
        this.maxViews = maxViews;
        return this;
    }

    public SecureFile build() {
        return new SecureFile(id, name, department, sourcePath, targetIP, expirationTime, encryption, viewOnly, maxViews);
    }
}