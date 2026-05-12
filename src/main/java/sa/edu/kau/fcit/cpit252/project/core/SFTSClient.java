package sa.edu.kau.fcit.cpit252.project.core;

import sa.edu.kau.fcit.cpit252.project.security.SecurityManager;

import javax.swing.*;
import javax.swing.filechooser.FileNameExtensionFilter;
import java.awt.*;
import java.io.DataOutputStream;
import java.io.File;
import java.net.Socket;
import java.nio.file.Files;

public class SFTSClient {

    private static final String[] DEPARTMENTS = {
            "Laboratory", "ER", "Radiology", "Pharmacy"
    };

    public static void main(String[] args) {
        SwingUtilities.invokeLater(SFTSClient::showClientGUI);
    }

    private static void showClientGUI() {
        JFrame frame = new JFrame("SFTS Client - Send Secure File");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setSize(500, 420);
        frame.setLayout(new BorderLayout(10, 10));

        // ─── العنوان ───────────────────────────────────────────
        JLabel title = new JLabel("SFTS Secure File Sender", SwingConstants.CENTER);
        title.setFont(new Font("Arial", Font.BOLD, 18));
        title.setForeground(new Color(30, 30, 150));
        frame.add(title, BorderLayout.NORTH);

        // ─── الفورم ────────────────────────────────────────────
        JPanel formPanel = new JPanel(new GridLayout(0, 2, 10, 10));
        formPanel.setBorder(BorderFactory.createEmptyBorder(15, 20, 15, 20));

        // اختيار الملف
        JLabel fileLabel = new JLabel("Selected File:");
        JTextField filePathField = new JTextField("No file selected");
        filePathField.setEditable(false);
        filePathField.setBackground(new Color(240, 240, 240));

        JButton browseButton = new JButton("Browse...");
        browseButton.setBackground(new Color(70, 130, 180));
        browseButton.setForeground(Color.WHITE);

        // اختيار القسم
        JLabel deptLabel = new JLabel("Department:");
        JComboBox<String> deptComboBox = new JComboBox<>(DEPARTMENTS);

        // Sender ID
        JLabel senderLabel = new JLabel("Sender ID:");
        JTextField senderField = new JTextField();

        // Max Views
        JLabel maxViewsLabel = new JLabel("Max Views:");
        JSpinner maxViewsSpinner = new JSpinner(new SpinnerNumberModel(3, 1, 100, 1));

        // Expiry Hours
        JLabel expiryLabel = new JLabel("Expiry (Hours):");
        JSpinner expirySpinner = new JSpinner(new SpinnerNumberModel(4, 1, 72, 1));

        // View Only
        JLabel viewOnlyLabel = new JLabel("View Only Mode:");
        JCheckBox viewOnlyCheckBox = new JCheckBox("Enabled", true);

        formPanel.add(fileLabel);
        formPanel.add(filePathField);
        formPanel.add(new JLabel());
        formPanel.add(browseButton);
        formPanel.add(deptLabel);
        formPanel.add(deptComboBox);
        formPanel.add(senderLabel);
        formPanel.add(senderField);
        formPanel.add(maxViewsLabel);
        formPanel.add(maxViewsSpinner);
        formPanel.add(expiryLabel);
        formPanel.add(expirySpinner);
        formPanel.add(viewOnlyLabel);
        formPanel.add(viewOnlyCheckBox);

        frame.add(formPanel, BorderLayout.CENTER);

        // ─── زر الإرسال ────────────────────────────────────────
        JButton sendButton = new JButton("Send Secure File");
        sendButton.setFont(new Font("Arial", Font.BOLD, 16));
        sendButton.setBackground(new Color(34, 139, 34));
        sendButton.setForeground(Color.WHITE);
        sendButton.setPreferredSize(new Dimension(0, 50));
        frame.add(sendButton, BorderLayout.SOUTH);

        // ─── File Chooser: PDF والصور فقط ──────────────────────
        final File[] selectedFile = {null};

        browseButton.addActionListener(e -> {
            JFileChooser fileChooser = new JFileChooser();
            fileChooser.setDialogTitle("Select a PDF or Image File");
            fileChooser.setAcceptAllFileFilterUsed(false);
            fileChooser.addChoosableFileFilter(
                    new FileNameExtensionFilter("PDF and Images (*.pdf, *.png, *.jpg, *.jpeg)",
                            "pdf", "png", "jpg", "jpeg")
            );

            int result = fileChooser.showOpenDialog(frame);
            if (result == JFileChooser.APPROVE_OPTION) {
                selectedFile[0] = fileChooser.getSelectedFile();
                filePathField.setText(selectedFile[0].getName());
            }
        });

        // ─── إرسال الملف ───────────────────────────────────────
        sendButton.addActionListener(e -> {
            if (selectedFile[0] == null) {
                JOptionPane.showMessageDialog(frame, "Please select a file first.", "Error", JOptionPane.ERROR_MESSAGE);
                return;
            }

            String senderId = senderField.getText().trim();
            if (senderId.isBlank()) {
                JOptionPane.showMessageDialog(frame, "Please enter Sender ID.", "Error", JOptionPane.ERROR_MESSAGE);
                return;
            }

            String department = (String) deptComboBox.getSelectedItem();
            int maxViews = (int) maxViewsSpinner.getValue();
            int expiryHours = (int) expirySpinner.getValue();
            boolean isViewOnly = viewOnlyCheckBox.isSelected();

            sendButton.setEnabled(false);
            sendButton.setText("Sending...");

            new Thread(() -> {
                try {
                    byte[] originalBytes = Files.readAllBytes(selectedFile[0].toPath());

                    System.out.println("Encrypting file...");
                    byte[] encryptedBytes = SecurityManager.encryptData(originalBytes);

                    try (Socket socket = new Socket("localhost", 9090);
                         DataOutputStream dos = new DataOutputStream(socket.getOutputStream())) {

                        dos.writeUTF(selectedFile[0].getName());
                        dos.writeLong(encryptedBytes.length);
                        dos.writeInt(expiryHours);
                        dos.writeInt(maxViews);
                        dos.writeBoolean(isViewOnly);
                        dos.writeUTF(department);
                        dos.writeUTF(senderId);
                        dos.write(encryptedBytes);
                        dos.flush();
                    }

                    SwingUtilities.invokeLater(() -> {
                        JOptionPane.showMessageDialog(frame,
                                "File sent successfully!\n\nFile: " + selectedFile[0].getName()
                                        + "\nDepartment: " + department
                                        + "\nMax Views: " + maxViews
                                        + "\nExpiry: " + expiryHours + " hours",
                                "Success", JOptionPane.INFORMATION_MESSAGE);

                        sendButton.setEnabled(true);
                        sendButton.setText("Send Secure File");
                    });

                } catch (Exception ex) {
                    SwingUtilities.invokeLater(() -> {
                        JOptionPane.showMessageDialog(frame,
                                "Error: " + ex.getMessage(),
                                "Transfer Failed", JOptionPane.ERROR_MESSAGE);
                        sendButton.setEnabled(true);
                        sendButton.setText("Send Secure File");
                    });
                }
            }).start();
        });

        frame.setLocationRelativeTo(null);
        frame.setVisible(true);
    }
}