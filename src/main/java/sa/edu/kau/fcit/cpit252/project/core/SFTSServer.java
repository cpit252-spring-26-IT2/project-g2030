package sa.edu.kau.fcit.cpit252.project.core;

import sa.edu.kau.fcit.cpit252.project.model.SecureFile;

import javax.swing.*;
import java.awt.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class SFTSServer {

    private static final int PORT = 9090;
    private static final ExecutorService threadPool = Executors.newFixedThreadPool(10);

    private static final DefaultListModel<SecureFile> vaultModel = new DefaultListModel<>();
    private static final Map<String, byte[]> fileDataVault = new HashMap<>();

    // بيانات تسجيل الدخول
    private static final String ADMIN_USERNAME = "admin";
    private static final String ADMIN_PASSWORD = "sfts1234";

    public static void main(String[] args) {
        // السيرفر يبدأ يستقبل فوراً بمجرد تشغيل البرنامج
        startServer();

        SwingUtilities.invokeLater(() -> {
            if (showLoginScreen()) {
                createAndShowServerGUI();
            } else {
                JOptionPane.showMessageDialog(null,
                        "Wrong credentials. Exiting.",
                        "Access Denied",
                        JOptionPane.ERROR_MESSAGE);
                System.exit(0);
            }
        });
    }

    // ─── شاشة تسجيل الدخول ────────────────────────────────────────────────────

    private static boolean showLoginScreen() {
        JPanel panel = new JPanel(new GridLayout(3, 2, 8, 8));
        panel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        JLabel userLabel = new JLabel("Username:");
        JTextField userField = new JTextField();

        JLabel passLabel = new JLabel("Password:");
        JPasswordField passField = new JPasswordField();

        panel.add(userLabel);
        panel.add(userField);
        panel.add(passLabel);
        panel.add(passField);

        JLabel titleLabel = new JLabel("SFTS Server - Admin Login", SwingConstants.CENTER);
        titleLabel.setFont(new Font("Arial", Font.BOLD, 14));
        titleLabel.setForeground(new Color(30, 30, 150));

        JPanel mainPanel = new JPanel(new BorderLayout(8, 8));
        mainPanel.add(titleLabel, BorderLayout.NORTH);
        mainPanel.add(panel, BorderLayout.CENTER);

        int result = JOptionPane.showConfirmDialog(
                null,
                mainPanel,
                "Login",
                JOptionPane.OK_CANCEL_OPTION,
                JOptionPane.PLAIN_MESSAGE
        );

        if (result == JOptionPane.OK_OPTION) {
            String username = userField.getText().trim();
            String password = new String(passField.getPassword()).trim();
            return ADMIN_USERNAME.equals(username) && ADMIN_PASSWORD.equals(password);
        }

        return false;
    }

    // ─── تشغيل السيرفر ─────────────────────────────────────────────────────────

    private static void startServer() {
        new Thread(() -> {
            try (ServerSocket serverSocket = new ServerSocket(PORT)) {
                System.out.println("=== SFTS Vault Server Running on Port " + PORT + " ===");
                while (true) {
                    Socket clientSocket = serverSocket.accept();
                    threadPool.execute(new ClientHandler(clientSocket));
                }
            } catch (Exception e) {
                System.err.println("Server Exception: " + e.getMessage());
            }
        }).start();
    }

    // ─── واجهة السيرفر ─────────────────────────────────────────────────────────

    private static void createAndShowServerGUI() {
        JFrame frame = new JFrame("SFTS Server - Secure Vault");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setSize(800, 500);
        frame.setLayout(new BorderLayout(10, 10));

        JLabel title = new JLabel("Receiver's Secure Vault (RAM Storage)", SwingConstants.CENTER);
        title.setFont(new Font("Arial", Font.BOLD, 18));
        title.setForeground(new Color(30, 30, 150));
        frame.add(title, BorderLayout.NORTH);

        JList<SecureFile> fileList = new JList<>(vaultModel);
        fileList.setFont(new Font("Monospaced", Font.BOLD, 14));
        fileList.setBackground(new Color(240, 240, 240));
        fileList.setCellRenderer(new DefaultListCellRenderer() {
            @Override
            public Component getListCellRendererComponent(JList<?> list, Object value,
                                                          int index, boolean isSelected, boolean cellHasFocus) {
                super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
                if (value instanceof SecureFile file) {
                    String status = file.isAccessRevoked() ? " [REVOKED]"
                            : file.isDeleted() ? " [DELETED]"
                            : " [ACTIVE]";
                    String views = " | Views: " + file.getViewCount()
                            + (file.getMaxViews() > 0 ? "/" + file.getMaxViews() : "");
                    String dept = " | Dept: " + file.getDepartment();
                    setText(file.getFileName() + dept + status + views);
                }
                return this;
            }
        });

        frame.add(new JScrollPane(fileList), BorderLayout.CENTER);

        Timer liveTimer = new Timer(1000, e -> fileList.repaint());
        liveTimer.start();

        JButton btnOpen = new JButton("Open Selected File");
        btnOpen.setFont(new Font("Arial", Font.BOLD, 16));
        btnOpen.setBackground(new Color(200, 50, 50));
        btnOpen.setForeground(Color.WHITE);
        frame.add(btnOpen, BorderLayout.SOUTH);

        btnOpen.addActionListener(e -> {
            int selectedIndex = fileList.getSelectedIndex();
            if (selectedIndex == -1) {
                JOptionPane.showMessageDialog(frame, "Please select a file from the vault first.");
                return;
            }

            SecureFile selectedFile = vaultModel.getElementAt(selectedIndex);

            // طلب Viewer ID للـ watermark
            String viewerId = JOptionPane.showInputDialog(frame, "Enter Viewer ID:");
            if (viewerId == null || viewerId.trim().isBlank()) {
                JOptionPane.showMessageDialog(frame, "Viewer ID is required.");
                return;
            }

            // التحقق من الصلاحيات
            if (!selectedFile.openFile()) {
                JOptionPane.showMessageDialog(frame,
                        "ACCESS DENIED!\nSecurity limits reached. File has expired or Maximum Views exceeded.",
                        "Security Block", JOptionPane.ERROR_MESSAGE);
                fileList.repaint();
                return;
            }

            // تسجيل الـ watermark باسم الشخص الذي فتح الملف
            selectedFile.setWatermarkText("Viewer: " + viewerId.trim());

            byte[] data = fileDataVault.get(selectedFile.getFileId());
            showSecureViewer(selectedFile, data, viewerId.trim());

            vaultModel.set(selectedIndex, selectedFile);
        });

        frame.setLocationRelativeTo(null);
        frame.setVisible(true);
    }

    // ─── إضافة ملف للخزنة ──────────────────────────────────────────────────────

    public static synchronized void addFileToVault(SecureFile file, byte[] data) {
        SwingUtilities.invokeLater(() -> {
            vaultModel.addElement(file);
            fileDataVault.put(file.getFileId(), data);
            System.out.println("File added to Secure Vault: " + file.getFileName());
            System.out.println("Vault size = " + vaultModel.getSize());
        });
    }

    // ─── عارض الملفات الآمن مع Watermark ──────────────────────────────────────

    private static void showSecureViewer(SecureFile file, byte[] fileData, String viewerId) {
        JFrame viewer = new JFrame("SFTS Secure Viewer - " + file.getFileName());
        viewer.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        viewer.setSize(900, 650);
        viewer.setLayout(new BorderLayout());
        viewer.getContentPane().setBackground(Color.DARK_GRAY);

        // Watermark في الأعلى
        String watermarkText = "CONFIDENTIAL  |  Viewer: " + viewerId
                + "  |  File: " + file.getFileName()
                + "  |  Dept: " + file.getDepartment();
        JLabel topWatermark = new JLabel(watermarkText, SwingConstants.CENTER);
        topWatermark.setForeground(Color.RED);
        topWatermark.setFont(new Font("Arial", Font.BOLD, 14));
        viewer.add(topWatermark, BorderLayout.NORTH);

        String lower = file.getFileName().toLowerCase();

        try {
            if (lower.endsWith(".png") || lower.endsWith(".jpg") || lower.endsWith(".jpeg")) {

                // عرض الصورة مع watermark مطبوع عليها
                ImageIcon icon = new ImageIcon(fileData);
                Image image = icon.getImage();

                JPanel imagePanel = new JPanel() {
                    @Override
                    protected void paintComponent(Graphics g) {
                        super.paintComponent(g);
                        setBackground(Color.DARK_GRAY);

                        // رسم الصورة
                        g.drawImage(image, 0, 0, getWidth(), getHeight(), this);

                        // طبع Watermark بشكل مائل على الصورة
                        Graphics2D g2d = (Graphics2D) g.create();
                        g2d.setFont(new Font("Arial", Font.BOLD, 26));
                        g2d.setColor(new Color(255, 0, 0, 90));
                        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

                        for (int y = 100; y < getHeight(); y += 150) {
                            for (int x = 20; x < getWidth(); x += 300) {
                                g2d.translate(x, y);
                                g2d.rotate(Math.toRadians(-25));
                                g2d.drawString("Viewer: " + viewerId, 0, 0);
                                g2d.rotate(Math.toRadians(25));
                                g2d.translate(-x, -y);
                            }
                        }
                        g2d.dispose();
                    }
                };

                viewer.add(imagePanel, BorderLayout.CENTER);

            } else if (lower.endsWith(".pdf")) {

                // عرض PDF مع معلومات الـ viewer والـ watermark
                JPanel pdfPanel = new JPanel(new BorderLayout());
                pdfPanel.setBackground(Color.BLACK);

                JTextArea pdfArea = new JTextArea();
                pdfArea.setEditable(false);
                pdfArea.setBackground(Color.BLACK);
                pdfArea.setForeground(Color.WHITE);
                pdfArea.setFont(new Font("Monospaced", Font.PLAIN, 15));
                pdfArea.setText(
                        "\n\n  PDF FILE RECEIVED SUCCESSFULLY\n"
                                + "  ─────────────────────────────────────\n\n"
                                + "  File Name  : " + file.getFileName() + "\n"
                                + "  Department : " + file.getDepartment() + "\n"
                                + "  Owner ID   : " + file.getOwnerId() + "\n"
                                + "  Viewer ID  : " + viewerId + "\n"
                                + "  View Count : " + file.getViewCount() + "/" + file.getMaxViews() + "\n"
                                + "  Expiry     : " + (file.getExpiryTime() != null ? file.getExpiryTime().toString() : "N/A") + "\n\n"
                                + "  ─────────────────────────────────────\n"
                                + "  [WATERMARK: Viewed by " + viewerId + "]\n"
                                + "  [VIEW ONLY MODE - Download Disabled]\n"
                                + "  [RAM Secured - Not saved to disk]\n"
                );

                pdfPanel.add(new JScrollPane(pdfArea), BorderLayout.CENTER);
                viewer.add(pdfPanel, BorderLayout.CENTER);

            } else {
                JLabel unsupported = new JLabel(
                        "Unsupported file type. Only PDF and images are allowed.",
                        SwingConstants.CENTER);
                unsupported.setForeground(Color.WHITE);
                viewer.add(unsupported, BorderLayout.CENTER);
            }

        } catch (Exception ex) {
            JLabel errorLabel = new JLabel("Viewer error: " + ex.getMessage(), SwingConstants.CENTER);
            errorLabel.setForeground(Color.WHITE);
            viewer.add(errorLabel, BorderLayout.CENTER);
        }

        viewer.setLocationRelativeTo(null);
        viewer.setAlwaysOnTop(true);
        viewer.setVisible(true);
    }
}