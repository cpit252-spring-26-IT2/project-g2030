package sa.edu.kau.fcit.cpit252.project.model;

import java.time.LocalDateTime;

public class FileVersion {
    private final int versionNumber;
    private final String contentSnapshot;
    private final String updatedBy;
    private final LocalDateTime createdAt;

    public FileVersion(int versionNumber, String contentSnapshot, String updatedBy) {
        this.versionNumber = versionNumber;
        this.contentSnapshot = contentSnapshot;
        this.updatedBy = updatedBy;
        this.createdAt = LocalDateTime.now();
    }

    public int getVersionNumber() {
        return versionNumber;
    }

    public String getContentSnapshot() {
        return contentSnapshot;
    }

    public String getUpdatedBy() {
        return updatedBy;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }
}