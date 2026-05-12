package sa.edu.kau.fcit.cpit252.project.model;

import java.time.LocalDateTime;

public class RecycleBinItem {
    private final SecureFile file;
    private final LocalDateTime deletedAt;
    private final String deletedBy;

    public RecycleBinItem(SecureFile file, String deletedBy) {
        this.file = file;
        this.deletedBy = deletedBy;
        this.deletedAt = LocalDateTime.now();
    }

    public SecureFile getFile() {
        return file;
    }

    public LocalDateTime getDeletedAt() {
        return deletedAt;
    }

    public String getDeletedBy() {
        return deletedBy;
    }
}