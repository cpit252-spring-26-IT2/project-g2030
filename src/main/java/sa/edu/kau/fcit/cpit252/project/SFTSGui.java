package sa.edu.kau.fcit.cpit252.project;

import sa.edu.kau.fcit.cpit252.project.security.SecurityManager;

import javax.swing.*;
import javax.swing.filechooser.FileNameExtensionFilter;
import java.awt.*;
import java.io.*;
import java.net.Socket;
import java.nio.file.Files;
import java.util.Arrays;
import java.util.List;

public class SFTSGui {
    private static String loggedInUser = "";

    // قائمة المهندسين المصرح لهم (أنت وفريقك)
    private static final List<String> AUTHORIZED_USERS = Arrays.asList(
            "Abdulaziz_Bukhari",
            "Motaz_Alsayed",
            "Abdulmalik_Aldahari"
    );

    public static void main(String[] args) {
        // 1. نظام تسجيل الدخول (Authentication)

        String name = JOptionPane.showInputDialog(null,
                "Enter Username to access SFTS Network:",
                "SFTS Secure Login",
                JOptionPane.QUESTION_MESSAGE);

        if (name == null || name.trim().isEmpty() || !AUTHORIZED_USERS.contains(name.trim())) {
            JOptionPane.showMessageDialog(null, "Access Denied! Unauthorized user.", "Security Alert", JOptionPane.ERROR_MESSAGE);
            System.exit(0);
        }
        loggedInUser = name.trim();

        // 2. إعداد واجهة المراقبة (Dashboard)

        JFrame frame = new JFrame("SFTS Secure Client - Logged in as: " + loggedInUser);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setSize(850, 600);
        frame.setLayout(new BorderLayout(10, 10));

        JTextArea logArea = new JTextArea();
        logArea.setEditable(false);
        logArea.setBackground(new Color(15, 15, 15));
        logArea.setForeground(new Color(0, 255, 100)); // لون أخضر تقني (Hacker style)
        logArea.setFont(new Font("Monospaced", Font.BOLD, 14));
        frame.add(new JScrollPane(logArea), BorderLayout.CENTER);

        System.setOut(new PrintStream(new OutputStream() {
            public void write(int b) { logArea.append(String.valueOf((char) b)); }
        }));

        JPanel panel = new JPanel(new FlowLayout());
        JButton btnSendFile = new JButton("🔒 Select & Transfer Secure File");
        JButton btnClear = new JButton("Clear Console");
        panel.add(btnSendFile);
        panel.add(btnClear);
        frame.add(panel, BorderLayout.NORTH);


        btnSendFile.addActionListener(e -> {
            JFileChooser fileChooser = new JFileChooser();
            fileChooser.setDialogTitle("Select a Document or Image");
            // السماح بالصور والـ PDF
            fileChooser.setFileFilter(new FileNameExtensionFilter("PDF & Images", "pdf", "png", "jpg", "jpeg"));

            if (fileChooser.showOpenDialog(frame) == JFileChooser.APPROVE_OPTION) {
                File selectedFile = fileChooser.getSelectedFile();

                // === لوحة الإعدادات الأمنية (Security Policies) ===
                JPanel setupPanel = new JPanel(new GridLayout(5, 2, 5, 5));
                JTextField ipField = new JTextField("localhost");
                JComboBox<Integer> expBox = new JComboBox<>(new Integer[]{0, 2, 4, 8, 24}); // 0 يعني لا ينتهي
                JTextField viewsField = new JTextField("0"); // 0 يعني مفتوح
                JCheckBox viewOnlyCheck = new JCheckBox("Force View-Only Mode (RAM-based)");

                setupPanel.add(new JLabel("Receiver IP Address:")); setupPanel.add(ipField);
                setupPanel.add(new JLabel("Expiration (Hours):")); setupPanel.add(expBox);
                setupPanel.add(new JLabel("Max Views Limit:")); setupPanel.add(viewsField);
                setupPanel.add(new JLabel("Download Restrictions:")); setupPanel.add(viewOnlyCheck);

                int result = JOptionPane.showConfirmDialog(frame, setupPanel, "SFTS Security Configuration", JOptionPane.OK_CANCEL_OPTION);

                if (result == JOptionPane.OK_OPTION) {
                    new Thread(() -> {
                        try {
                            String targetIP = ipField.getText().trim();
                            int expHours = (Integer) expBox.getSelectedItem();
                            int maxViews = Integer.parseInt(viewsField.getText().trim());
                            boolean isViewOnly = viewOnlyCheck.isSelected();

                            if (expHours > 0 || maxViews > 0) {
                                isViewOnly = true;
                            }

                            System.out.println("\n------------------------------------------------");
                            System.out.println("[USER] Initiating secure transfer for: " + selectedFile.getName());
                            System.out.println("[POLICY] Expiry: " + expHours + "h | Max Views: " + maxViews + " | ViewOnly: " + isViewOnly);

                            // قراءة الملف والتشفير
                            byte[] originalBytes = Files.readAllBytes(selectedFile.toPath());
                            System.out.println("[SECURITY] Encrypting raw data with AES-256...");
                            byte[] encryptedBytes = SecurityManager.encryptData(originalBytes);

                            // الاتصال عبر الشبكة
                            System.out.println("[NETWORK] Connecting to " + targetIP + ":8080...");
                            Socket socket = new Socket(targetIP, 8080);
                            DataOutputStream dos = new DataOutputStream(socket.getOutputStream());

                            // إرسال البيانات بترتيب دقيق (يتطابق مع ClientHandler)
                            dos.writeUTF(selectedFile.getName());          // 1. الاسم
                            dos.writeLong(encryptedBytes.length);          // 2. الحجم المشفر
                            dos.writeInt(expHours);                        // 3. الساعات
                            dos.writeInt(maxViews);                        // 4. عدد المشاهدات
                            dos.writeBoolean(isViewOnly);                  // 5. وضع القراءة فقط
                            dos.write(encryptedBytes);                     // 6. البيانات المشفرة
                            dos.flush();

                            // إغلاق الاتصال
                            dos.close();
                            socket.close();

                            System.out.println("[SUCCESS] Encrypted payload securely transmitted!");
                            System.out.println("------------------------------------------------\n");

                        } catch (NumberFormatException nfe) {
                            System.err.println("[ERROR] Please enter a valid number for Max Views.");
                        } catch (Exception ex) {
                            System.err.println("[ERROR] Transfer Failed: Make sure the Server is running! Details: " + ex.getMessage());
                        }
                    }).start();
                }
            }
        });

        btnClear.addActionListener(e -> logArea.setText(""));

        System.out.println("=== SFTS Core Client Initialized Successfully ===");
        System.out.println("System is ready for encrypted network transfers.\n");
        frame.setLocationRelativeTo(null);
        frame.setVisible(true);
    }
}