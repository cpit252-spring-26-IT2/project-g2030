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
import java.util.UUID;

public class DeptClientHandler implements Runnable {
    private final Socket socket;
    private final DepartmentServer server;

    public DeptClientHandler(Socket socket, DepartmentServer server) {
        this.socket = socket;
        this.server = server;
    }

    private boolean isAllowedFileType(String name) {
        String l = name.toLowerCase();
        return l.endsWith(".pdf") || l.endsWith(".png") || l.endsWith(".jpg") || l.endsWith(".jpeg");
    }

    @Override
    public void run() {
        try (DataInputStream dis = new DataInputStream(socket.getInputStream())) {
            String  fileName   = dis.readUTF();
            long    fileSize   = dis.readLong();
            int     expHours   = dis.readInt();
            int     maxViews   = dis.readInt();
            boolean viewOnly   = dis.readBoolean();
            String  department = dis.readUTF();
            String  senderId   = dis.readUTF();

            if (!isAllowedFileType(fileName)) return;

            byte[] encBytes = new byte[(int) fileSize];
            dis.readFully(encBytes);
            byte[] decBytes = SecurityManager.decryptData(encBytes);

            String fileId = "NET-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase();
            SecureFile file = new FileBuilder()
                    .setFileId(fileId)
                    .setFileName(fileName)
                    .setDepartment(server.getDepartment())
                    .setOwnerId(senderId)
                    .setContent("")
                    .setEncrypted(true)
                    .setEncryptionType("AES-256")
                    .setViewOnly(viewOnly)
                    .setMaxViews(maxViews)
                    .setExpiryTime(LocalDateTime.now().plusHours(expHours))
                    .setWatermarkText("Viewer: " + senderId)
                    .build();

            server.addFileToVault(file, decBytes);
            server.registerSender(fileId, senderId);

            try {
                Department dept = DepartmentFactory.getDepartment(server.getDepartment().toUpperCase());
                if (dept == null) dept = DepartmentFactory.getDepartment("LAB");
                if (dept != null) new DepartmentProxy(dept, senderId).processFile(file);
            } catch (Exception ignored) {}

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}