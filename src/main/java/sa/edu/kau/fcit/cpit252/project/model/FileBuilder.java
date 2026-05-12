package sa.edu.kau.fcit.cpit252.project.model;

import java.time.LocalDateTime;
import java.util.UUID;

public class FileBuilder {
    private final SecureFile secureFile;

    public FileBuilder() {
        this.secureFile = new SecureFile();
        this.secureFile.setFileId(generateFileId());
    }

    public FileBuilder setFileId(String fileId) {
        secureFile.setFileId(fileId);
        return this;
    }

    public FileBuilder setFileName(String fileName) {
        secureFile.setFileName(fileName);
        return this;
    }

    public FileBuilder setFileType(String fileType) {
        secureFile.setFileType(fileType);
        return this;
    }

    public FileBuilder setDepartment(String department) {
        secureFile.setDepartment(department);
        return this;
    }

    public FileBuilder setOwnerId(String ownerId) {
        secureFile.setOwnerId(ownerId);
        return this;
    }

    public FileBuilder setContent(String content) {
        secureFile.setContent(content);
        return this;
    }

    public FileBuilder setEncrypted(boolean encrypted) {
        secureFile.setEncrypted(encrypted);
        return this;
    }

    public FileBuilder setEncryptionType(String encryptionType) {
        secureFile.setEncryptionType(encryptionType);
        return this;
    }

    public FileBuilder setViewOnly(boolean viewOnly) {
        secureFile.setViewOnly(viewOnly);
        return this;
    }

    public FileBuilder setMaxViews(int maxViews) {
        secureFile.setMaxViews(maxViews);
        return this;
    }

    public FileBuilder setExpiryTime(LocalDateTime expiryTime) {
        secureFile.setExpiryTime(expiryTime);
        return this;
    }

    public FileBuilder setWatermarkText(String watermarkText) {
        secureFile.setWatermarkText(watermarkText);
        return this;
    }

    public FileBuilder setCreatedAt(LocalDateTime createdAt) {
        secureFile.setCreatedAt(createdAt);
        return this;
    }

    public SecureFile build() {
        validateMandatoryFields();
        applyDefaults();
        return secureFile;
    }

    private void validateMandatoryFields() {
        if (secureFile.getFileName() == null || secureFile.getFileName().isBlank()) {
            throw new IllegalStateException("File name is required.");
        }

        if (secureFile.getDepartment() == null || secureFile.getDepartment().isBlank()) {
            throw new IllegalStateException("Department is required.");
        }

        if (secureFile.getOwnerId() == null || secureFile.getOwnerId().isBlank()) {
            throw new IllegalStateException("Owner ID is required.");
        }

        if (secureFile.getContent() == null) {
            throw new IllegalStateException("Content is required.");
        }
    }

    private void applyDefaults() {
        if (secureFile.getFileType() == null || secureFile.getFileType().isBlank()) {
            secureFile.setFileType("txt");
        }

        if (!secureFile.isEncrypted()) {
            secureFile.setEncryptionType("NONE");
        } else if (secureFile.getEncryptionType() == null || secureFile.getEncryptionType().isBlank()) {
            secureFile.setEncryptionType("AES-256");
        }

        if (secureFile.getWatermarkText() == null || secureFile.getWatermarkText().isBlank()) {
            secureFile.setWatermarkText("Confidential - " + secureFile.getOwnerId());
        }
    }

    private String generateFileId() {
        return "FILE-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase();
    }
}