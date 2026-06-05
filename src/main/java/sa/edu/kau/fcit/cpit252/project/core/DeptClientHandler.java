package sa.edu.kau.fcit.cpit252.project.core;

import sa.edu.kau.fcit.cpit252.project.department.Department;
import sa.edu.kau.fcit.cpit252.project.department.DepartmentFactory;
import sa.edu.kau.fcit.cpit252.project.model.FileBuilder;
import sa.edu.kau.fcit.cpit252.project.model.SecureFile;
import sa.edu.kau.fcit.cpit252.project.proxy.DepartmentProxy;
import sa.edu.kau.fcit.cpit252.project.security.SecurityManager;

import java.io.DataInputStream;
import java.net.Socket;
import java.time.LocalDateTime;
import java.util.Set;
import java.util.UUID;
import java.util.logging.Logger;

public class DeptClientHandler implements Runnable {

    private static final Logger LOG = Logger.getLogger(DeptClientHandler.class.getName());

    private static final Set<String> ALLOWED_EXTENSIONS = Set.of(".pdf", ".png", ".jpg", ".jpeg");

    private final Socket           socket;
    private final DepartmentServer server;

    public DeptClientHandler(Socket socket, DepartmentServer server) {
        this.socket = socket;
        this.server = server;
    }

    // ── Entry Point ───────────────────────────────────────────────────────────
    @Override
    public void run() {
        try (DataInputStream dis = new DataInputStream(socket.getInputStream())) {
            FileMetadata meta = readMetadata(dis);
            if (meta == null) return;

            byte[] decBytes = readAndDecrypt(dis, meta.fileSize);
            if (decBytes == null) return;

            SecureFile file = buildSecureFile(meta);
            server.addFileToVault(file, decBytes);
            server.registerSender(file.getFileId(), meta.senderId);

            processThroughDepartment(file, meta.senderId);

        } catch (Exception e) {
            LOG.warning("Error handling client connection: " + e.getMessage());
        }
    }

    // ── Read Metadata ─────────────────────────────────────────────────────────
    private FileMetadata readMetadata(DataInputStream dis) throws Exception {
        FileMetadata meta = new FileMetadata();
        meta.fileName   = dis.readUTF();
        meta.fileSize   = dis.readLong();
        meta.expHours   = dis.readInt();
        meta.maxViews   = dis.readInt();
        meta.viewOnly   = dis.readBoolean();
        meta.department = dis.readUTF();
        meta.senderId   = dis.readUTF();

        if (!isAllowedFileType(meta.fileName)) {
            LOG.warning("Rejected file with unsupported type: " + meta.fileName);
            return null;
        }
        return meta;
    }

    // ── Read & Decrypt ────────────────────────────────────────────────────────
    private byte[] readAndDecrypt(DataInputStream dis, long fileSize) {
        try {
            byte[] encBytes = new byte[(int) fileSize];
            dis.readFully(encBytes);
            return SecurityManager.decryptData(encBytes);
        } catch (Exception e) {
            LOG.warning("Failed to read or decrypt file data: " + e.getMessage());
            return null;
        }
    }

    // ── Build SecureFile ──────────────────────────────────────────────────────
    private SecureFile buildSecureFile(FileMetadata meta) {
        String fileId = "NET-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase();
        return new FileBuilder()
                .setFileId(fileId)
                .setFileName(meta.fileName)
                .setDepartment(server.getDepartment())
                .setOwnerId(meta.senderId)
                .setContent("")
                .setEncrypted(true)
                .setEncryptionType("AES-256")
                .setViewOnly(meta.viewOnly)
                .setMaxViews(meta.maxViews)
                .setExpiryTime(LocalDateTime.now().plusHours(meta.expHours))
                .setWatermarkText("Sender: " + meta.senderId)
                .build();
    }

    // ── Process Through Department ────────────────────────────────────────────
    private void processThroughDepartment(SecureFile file, String senderId) {
        try {
            String deptKey = server.getDepartment().toUpperCase();
            Department dept = DepartmentFactory.getDepartment(deptKey);
            if (dept == null) {
                LOG.warning("Department not found: " + deptKey + " — falling back to LAB.");
                dept = DepartmentFactory.getDepartment("LAB");
            }
            if (dept != null) {
                new DepartmentProxy(dept, senderId).processFile(file);
            }
        } catch (Exception e) {
            LOG.warning("Department processing failed: " + e.getMessage());
        }
    }

    // ── File Type Validation ──────────────────────────────────────────────────
    private boolean isAllowedFileType(String fileName) {
        if (fileName == null || !fileName.contains(".")) return false;
        String ext = fileName.substring(fileName.lastIndexOf('.')).toLowerCase();
        return ALLOWED_EXTENSIONS.contains(ext);
    }

    // ── Metadata DTO ─────────────────────────────────────────────────────────
    private static class FileMetadata {
        String  fileName;
        long    fileSize;
        int     expHours;
        int     maxViews;
        boolean viewOnly;
        String  department;
        String  senderId;
    }
}