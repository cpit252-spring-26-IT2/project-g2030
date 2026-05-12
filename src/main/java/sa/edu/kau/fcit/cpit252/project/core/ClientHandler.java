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

public class ClientHandler implements Runnable {
    private final Socket socket;

    public ClientHandler(Socket socket) {
        this.socket = socket;
    }

    private boolean isAllowedFileType(String fileName) {
        String lower = fileName.toLowerCase();
        return lower.endsWith(".pdf")
                || lower.endsWith(".png")
                || lower.endsWith(".jpg")
                || lower.endsWith(".jpeg");
    }

    @Override
    public void run() {
        try (DataInputStream dis = new DataInputStream(socket.getInputStream())) {

            System.out.println("Client connected: " + socket.getInetAddress());

            String fileName = dis.readUTF();
            long fileSize = dis.readLong();
            int expHours = dis.readInt();
            int maxViews = dis.readInt();
            boolean isViewOnly = dis.readBoolean();
            String department = dis.readUTF();
            String senderId = dis.readUTF();

            System.out.println("Incoming file: " + fileName);
            System.out.println("Department: " + department);
            System.out.println("Sender ID: " + senderId);
            System.out.println("File size: " + fileSize);
            System.out.println("Expiry: " + expHours);
            System.out.println("Max views: " + maxViews);
            System.out.println("View only: " + isViewOnly);

            if (!isAllowedFileType(fileName)) {
                System.err.println("Rejected: only PDF and images are allowed.");
                return;
            }

            byte[] encryptedBuffer = new byte[(int) fileSize];
            dis.readFully(encryptedBuffer);
            System.out.println("Encrypted bytes received.");

            byte[] decryptedBytes = SecurityManager.decryptData(encryptedBuffer);
            System.out.println("Decryption complete.");

            String fileId = "NET-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase();

            SecureFile networkFile = new FileBuilder()
                    .setFileId(fileId)
                    .setFileName(fileName)
                    .setDepartment(department)
                    .setOwnerId(senderId)
                    .setContent("")
                    .setEncrypted(true)
                    .setEncryptionType("AES-256")
                    .setViewOnly(isViewOnly)
                    .setMaxViews(maxViews)
                    .setExpiryTime(LocalDateTime.now().plusHours(expHours))
                    .setWatermarkText("Sender: " + senderId)
                    .build();

            System.out.println("SecureFile object built successfully.");

            // أضف للـ vault أولًا عشان نتأكد أن المشكلة ليست من GUI أو الشبكة
            SFTSServer.addFileToVault(networkFile, decryptedBytes);
            System.out.println("File passed to vault.");

            // جرّب الـ proxy بعد ذلك
            try {
                Department dept = DepartmentFactory.getDepartment(department.toUpperCase());
                if (dept == null) {
                    System.out.println("DepartmentFactory returned null, fallback to LAB");
                    dept = DepartmentFactory.getDepartment("LAB");
                }

                if (dept != null) {
                    Department proxyAuthorized = new DepartmentProxy(dept, senderId);
                    proxyAuthorized.processFile(networkFile);
                    System.out.println("Proxy processing completed.");
                } else {
                    System.out.println("Department still null, proxy skipped.");
                }
            } catch (Exception ex) {
                System.err.println("Proxy/Department error: " + ex.getMessage());
                ex.printStackTrace();
            }

        } catch (Exception e) {
            System.err.println("ClientHandler Error: " + e.getMessage());
            e.printStackTrace();
        }
    }
}