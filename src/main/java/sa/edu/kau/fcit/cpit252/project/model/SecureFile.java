package sa.edu.kau.fcit.cpit252.project.model;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class SecureFile {
    private String fileId;
    private String fileName;
    private String fileType;
    private String department;
    private String ownerId;
    private String content;

    private boolean encrypted;
    private String encryptionType;

    private boolean viewOnly;
    private int viewCount;
    private int maxViews;
    private boolean accessRevoked;

    private LocalDateTime createdAt;
    private LocalDateTime expiryTime;

    private String watermarkText;

    private boolean deleted;
    private LocalDateTime deletedAt;

    private final List<FileVersion> versions;

    public SecureFile() {
        this.createdAt = LocalDateTime.now();
        this.versions = new ArrayList<>();
        this.viewCount = 0;
        this.maxViews = 0;
        this.accessRevoked = false;
        this.viewOnly = false;
        this.deleted = false;
    }

    public SecureFile(String fileId, String fileName, String fileType, String department, String ownerId,
                      String content, boolean encrypted, String encryptionType, boolean viewOnly,
                      int maxViews, LocalDateTime expiryTime, String watermarkText) {
        this.fileId = fileId;
        this.fileName = fileName;
        this.fileType = fileType;
        this.department = department;
        this.ownerId = ownerId;
        this.content = content;
        this.encrypted = encrypted;
        this.encryptionType = encryptionType;
        this.viewOnly = viewOnly;
        this.maxViews = maxViews;
        this.expiryTime = expiryTime;
        this.watermarkText = watermarkText;

        this.createdAt = LocalDateTime.now();
        this.versions = new ArrayList<>();
        this.viewCount = 0;
        this.accessRevoked = false;
        this.deleted = false;
    }

    public boolean canBeOpened() {
        if (deleted) {
            return false;
        }

        if (accessRevoked) {
            return false;
        }

        if (expiryTime != null && LocalDateTime.now().isAfter(expiryTime)) {
            accessRevoked = true;
            return false;
        }

        if (maxViews > 0 && viewCount >= maxViews) {
            accessRevoked = true;
            return false;
        }

        return true;
    }

    public boolean openFile() {
        if (deleted) return false;

        if (accessRevoked) return false;

        if (expiryTime != null && LocalDateTime.now().isAfter(expiryTime)) {
            accessRevoked = true;
            return false;
        }

        if (maxViews > 0 && viewCount >= maxViews) {
            accessRevoked = true;
            return false;
        }

        viewCount++;

        if (maxViews > 0 && viewCount >= maxViews) {
            accessRevoked = true;
        }

        return true;
    }

    public void addVersion(String updatedContent, String updatedBy) {
        int nextVersion = versions.size() + 1;
        versions.add(new FileVersion(nextVersion, updatedContent, updatedBy));
        this.content = updatedContent;
    }

    public FileVersion restoreVersion(int versionNumber) {
        for (FileVersion version : versions) {
            if (version.getVersionNumber() == versionNumber) {
                this.content = version.getContentSnapshot();
                return version;
            }
        }
        return null;
    }

    public void moveToRecycleBin() {
        this.deleted = true;
        this.deletedAt = LocalDateTime.now();
    }

    public void recoverFromRecycleBin() {
        this.deleted = false;
        this.deletedAt = null;
    }

    public void incrementViewCount() {
        this.viewCount++;
    }

    public String getDisplayContent() {
        StringBuilder builder = new StringBuilder();
        builder.append("----- FILE CONTENT -----\n");
        builder.append(content == null ? "" : content).append("\n");

        if (watermarkText != null && !watermarkText.isBlank()) {
            builder.append("\n[WATERMARK: ").append(watermarkText).append("]");
        }

        if (viewOnly) {
            builder.append("\n[VIEW ONLY MODE ENABLED]");
        }

        return builder.toString();
    }

    @Override
    public String toString() {
        return "SecureFile{" +
                "fileId='" + fileId + '\'' +
                ", fileName='" + fileName + '\'' +
                ", fileType='" + fileType + '\'' +
                ", department='" + department + '\'' +
                ", ownerId='" + ownerId + '\'' +
                ", encrypted=" + encrypted +
                ", encryptionType='" + encryptionType + '\'' +
                ", viewOnly=" + viewOnly +
                ", viewCount=" + viewCount +
                ", maxViews=" + maxViews +
                ", accessRevoked=" + accessRevoked +
                ", createdAt=" + createdAt +
                ", expiryTime=" + expiryTime +
                ", watermarkText='" + watermarkText + '\'' +
                ", deleted=" + deleted +
                ", deletedAt=" + deletedAt +
                ", versions=" + versions.size() +
                '}';
    }

    public String getFileId() {
        return fileId;
    }

    public void setFileId(String fileId) {
        this.fileId = fileId;
    }

    public String getFileName() {
        return fileName;
    }

    public void setFileName(String fileName) {
        this.fileName = fileName;
    }

    public String getFileType() {
        return fileType;
    }

    public void setFileType(String fileType) {
        this.fileType = fileType;
    }

    public String getDepartment() {
        return department;
    }

    public void setDepartment(String department) {
        this.department = department;
    }

    public String getOwnerId() {
        return ownerId;
    }

    public void setOwnerId(String ownerId) {
        this.ownerId = ownerId;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public boolean isEncrypted() {
        return encrypted;
    }

    public void setEncrypted(boolean encrypted) {
        this.encrypted = encrypted;
    }

    public String getEncryptionType() {
        return encryptionType;
    }

    public void setEncryptionType(String encryptionType) {
        this.encryptionType = encryptionType;
    }

    public boolean isViewOnly() {
        return viewOnly;
    }

    public void setViewOnly(boolean viewOnly) {
        this.viewOnly = viewOnly;
    }

    public int getViewCount() {
        return viewCount;
    }

    public void setViewCount(int viewCount) {
        this.viewCount = viewCount;
    }

    public int getMaxViews() {
        return maxViews;
    }

    public void setMaxViews(int maxViews) {
        this.maxViews = maxViews;
    }

    public boolean isAccessRevoked() {
        return accessRevoked;
    }

    public void setAccessRevoked(boolean accessRevoked) {
        this.accessRevoked = accessRevoked;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public LocalDateTime getExpiryTime() {
        return expiryTime;
    }

    public void setExpiryTime(LocalDateTime expiryTime) {
        this.expiryTime = expiryTime;
    }

    public String getWatermarkText() {
        return watermarkText;
    }

    public void setWatermarkText(String watermarkText) {
        this.watermarkText = watermarkText;
    }

    public boolean isDeleted() {
        return deleted;
    }

    public void setDeleted(boolean deleted) {
        this.deleted = deleted;
    }

    public LocalDateTime getDeletedAt() {
        return deletedAt;
    }

    public void setDeletedAt(LocalDateTime deletedAt) {
        this.deletedAt = deletedAt;
    }

    public List<FileVersion> getVersions() {
        return versions;
    }
}