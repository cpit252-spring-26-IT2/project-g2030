package sa.edu.kau.fcit.cpit252.project.core;

import sa.edu.kau.fcit.cpit252.project.security.SecurityManager;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.filechooser.FileNameExtensionFilter;
import java.awt.*;
import java.io.DataOutputStream;
import java.io.File;
import java.net.Socket;
import java.nio.file.Files;
import java.util.Arrays;
import java.util.Map;
import java.util.logging.Logger;

public class SFTSClient {

    // ── Logger ────────────────────────────────────────────────────────────────
    private static final Logger LOG = Logger.getLogger(SFTSClient.class.getName());

    // ── Theme ─────────────────────────────────────────────────────────────────
    private static final Color BG_DARK      = new Color(30, 30, 46);
    private static final Color BG_PANEL     = new Color(45, 45, 65);
    private static final Color ACCENT_BLUE  = new Color(100, 149, 237);
    private static final Color ACCENT_GREEN = new Color(80, 200, 120);
    private static final Color ACCENT_RED   = new Color(220, 80, 80);
    private static final Color TEXT_PRIMARY = new Color(220, 220, 235);
    private static final Color TEXT_MUTED   = new Color(140, 140, 160);

    // ── Config ────────────────────────────────────────────────────────────────
    private static final String[] ALLOWED_USERS = {"2338742", "2339709", "2337862"};
    private static final String[] DEPARTMENTS   = {"ER", "Laboratory", "Radiology", "Pharmacy"};

    private static final Map<String, Integer> DEPT_PORTS = Map.of(
            "ER",         9090,
            "Laboratory", 9091,
            "Radiology",  9092,
            "Pharmacy",   9093
    );

    private  String loggedInUser;

    // ── Entry Point ───────────────────────────────────────────────────────────
    public static void launchClient() {
        SwingUtilities.invokeLater(() -> {
            SFTSClient client = new SFTSClient(); // instance جديد لكل نافذة
            String user = client.login();
            if (user == null) return;
            client.loggedInUser = user;
            client.showClientGUI();
        });
    }

    // ── Login Dialog (JDialog) ────────────────────────────────────────────────
    private String login() {
        JDialog dialog = new JDialog((Frame) null, "Client Login", true);
        dialog.setSize(380, 220);
        dialog.setLocationRelativeTo(null);
        dialog.setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
        dialog.getContentPane().setBackground(BG_DARK);
        dialog.setLayout(new BorderLayout());

        // Header
        JLabel title = new JLabel("Client Login", SwingConstants.CENTER);
        title.setFont(new Font("Segoe UI", Font.BOLD, 15));
        title.setForeground(ACCENT_BLUE);
        title.setBackground(BG_PANEL);
        title.setOpaque(true);
        title.setBorder(new EmptyBorder(12, 0, 12, 0));
        dialog.add(title, BorderLayout.NORTH);

        // Form
        JPanel form = new JPanel(new GridLayout(2, 2, 10, 12));
        form.setBackground(BG_DARK);
        form.setBorder(new EmptyBorder(20, 30, 10, 30));

        JTextField     userField = new JTextField();
        JPasswordField passField = new JPasswordField();
        styleField(userField);
        styleField(passField);

        form.add(styledLabel("User ID:"));  form.add(userField);
        form.add(styledLabel("Password:")); form.add(passField);
        dialog.add(form, BorderLayout.CENTER);

        // Buttons
        JPanel btnPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 12, 10));
        btnPanel.setBackground(BG_DARK);

        JButton okBtn     = styledButton("OK",     ACCENT_BLUE);
        JButton cancelBtn = styledButton("Cancel", new Color(80, 80, 100));

        final boolean[] confirmed = {false};
        okBtn.addActionListener(e     -> { confirmed[0] = true; dialog.dispose(); });
        cancelBtn.addActionListener(e -> dialog.dispose());
        dialog.getRootPane().setDefaultButton(okBtn);

        btnPanel.add(okBtn);
        btnPanel.add(cancelBtn);
        dialog.add(btnPanel, BorderLayout.SOUTH);

        dialog.setVisible(true);

        if (!confirmed[0]) return null;

        String user = userField.getText().trim();
        String pass = new String(passField.getPassword()).trim();

        if (!"sfts1234".equals(pass) || Arrays.stream(ALLOWED_USERS).noneMatch(user::equals)) {
            JOptionPane.showMessageDialog(null, "Invalid credentials or user not allowed.",
                    "Login Failed", JOptionPane.ERROR_MESSAGE);
            return null;
        }
        return user;
    }

    // ── Client GUI ────────────────────────────────────────────────────────────
    private void showClientGUI() {
        JFrame frame = new JFrame("SFTS Client  —  Send Secure File");
        frame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        frame.setSize(560, 500);
        frame.getContentPane().setBackground(BG_DARK);
        frame.setLayout(new BorderLayout(0, 0));

        frame.add(buildClientHeader(), BorderLayout.NORTH);
        frame.add(buildForm(frame),    BorderLayout.CENTER);

        frame.setLocationRelativeTo(null);
        frame.setVisible(true);
    }

    // ── Header ────────────────────────────────────────────────────────────────
    private JPanel buildClientHeader() {
        JPanel header = new JPanel(new GridLayout(2, 1, 0, 4));
        header.setBackground(BG_PANEL);
        header.setBorder(new EmptyBorder(14, 20, 14, 20));

        JLabel title = new JLabel("SFTS Secure File Sender", SwingConstants.CENTER);
        title.setFont(new Font("Segoe UI", Font.BOLD, 20));
        title.setForeground(ACCENT_BLUE);

        JLabel sub = new JLabel("Logged in as: " + loggedInUser, SwingConstants.CENTER);
        sub.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        sub.setForeground(TEXT_MUTED);

        header.add(title);
        header.add(sub);
        return header;
    }

    // ── Form ──────────────────────────────────────────────────────────────────
    private JPanel buildForm(JFrame frame) {
        // ── Form fields ───────────────────────────────────────────────────────
        JTextField         filePathField  = new JTextField("No file selected");
        JButton            browseBtn      = styledButton("Browse...", BG_PANEL);
        JComboBox<String>  deptBox        = new JComboBox<>(DEPARTMENTS);
        JLabel             portLabel      = new JLabel("Port: " + DEPT_PORTS.get(DEPARTMENTS[0]));
        JTextField         senderField    = new JTextField(loggedInUser);
        JSpinner           maxViewsSpinner = new JSpinner(new SpinnerNumberModel(3, 1, 100, 1));
        JSpinner           expirySpinner  = new JSpinner(new SpinnerNumberModel(4, 1, 72, 1));
        JCheckBox          viewOnlyBox    = new JCheckBox("Enabled", true);
        JProgressBar       progressBar    = new JProgressBar();
        JLabel             statusLabel    = new JLabel(" ");

        // Style fields
        filePathField.setEditable(false);
        senderField.setEditable(false);
        styleField(filePathField);
        styleField(senderField);
        styleField(maxViewsSpinner);
        styleField(expirySpinner);

        portLabel.setForeground(ACCENT_GREEN);
        portLabel.setFont(new Font("Segoe UI", Font.BOLD, 13));

        viewOnlyBox.setBackground(BG_DARK);
        viewOnlyBox.setForeground(TEXT_PRIMARY);
        viewOnlyBox.setFont(new Font("Segoe UI", Font.PLAIN, 13));

        styleComboBox(deptBox);
        deptBox.addActionListener(e ->
                portLabel.setText("Port: " + DEPT_PORTS.getOrDefault(
                        (String) deptBox.getSelectedItem(), 9090)));

        progressBar.setVisible(false);
        progressBar.setIndeterminate(true);
        progressBar.setForeground(ACCENT_BLUE);
        progressBar.setBackground(BG_PANEL);

        statusLabel.setFont(new Font("Segoe UI", Font.ITALIC, 11));
        statusLabel.setForeground(TEXT_MUTED);
        statusLabel.setHorizontalAlignment(SwingConstants.CENTER);

        // ── Form layout ───────────────────────────────────────────────────────
        JPanel form = new JPanel(new GridLayout(0, 2, 10, 12));
        form.setBackground(BG_DARK);
        form.setBorder(new EmptyBorder(16, 20, 10, 20));

        form.add(formLabel("Selected File:"));  form.add(filePathField);
        form.add(new JLabel());                 form.add(browseBtn);
        form.add(formLabel("Department:"));     form.add(deptBox);
        form.add(formLabel("Target Port:"));    form.add(portLabel);
        form.add(formLabel("Sender ID:"));      form.add(senderField);
        form.add(formLabel("Max Views:"));      form.add(maxViewsSpinner);
        form.add(formLabel("Expiry (Hours):")); form.add(expirySpinner);
        form.add(formLabel("View Only Mode:")); form.add(viewOnlyBox);

        // ── Bottom panel ──────────────────────────────────────────────────────
        JButton sendBtn = styledButton("Send Secure File", ACCENT_GREEN);
        sendBtn.setFont(new Font("Segoe UI", Font.BOLD, 15));

        JPanel bottom = new JPanel(new BorderLayout(0, 6));
        bottom.setBackground(BG_DARK);
        bottom.setBorder(new EmptyBorder(10, 20, 16, 20));
        bottom.add(sendBtn,    BorderLayout.NORTH);
        bottom.add(progressBar, BorderLayout.CENTER);
        bottom.add(statusLabel, BorderLayout.SOUTH);

        JPanel wrapper = new JPanel(new BorderLayout());
        wrapper.setBackground(BG_DARK);
        wrapper.add(form,   BorderLayout.CENTER);
        wrapper.add(bottom, BorderLayout.SOUTH);

        // ── Browse Action ─────────────────────────────────────────────────────
        final File[] selectedFile = {null};
        browseBtn.addActionListener(e -> {
            JFileChooser fc = new JFileChooser();
            fc.setAcceptAllFileFilterUsed(false);
            fc.addChoosableFileFilter(new FileNameExtensionFilter(
                    "PDF and Images (*.pdf, *.png, *.jpg, *.jpeg)",
                    "pdf", "png", "jpg", "jpeg"));
            if (fc.showOpenDialog(frame) == JFileChooser.APPROVE_OPTION) {
                selectedFile[0] = fc.getSelectedFile();
                filePathField.setText(selectedFile[0].getName());
                statusLabel.setText("File selected: " + selectedFile[0].getName());
            }
        });

        // ── Send Action ───────────────────────────────────────────────────────
        sendBtn.addActionListener(e -> {
            if (selectedFile[0] == null) {
                JOptionPane.showMessageDialog(frame, "Please select a file first.",
                        "No File", JOptionPane.WARNING_MESSAGE);
                return;
            }

            String  dept        = (String) deptBox.getSelectedItem();
            int     targetPort  = DEPT_PORTS.getOrDefault(dept, 9090);
            int     maxViews    = (int) maxViewsSpinner.getValue();
            int     expiryHours = (int) expirySpinner.getValue();
            boolean isViewOnly  = viewOnlyBox.isSelected();

            sendBtn.setEnabled(false);
            progressBar.setVisible(true);
            statusLabel.setText("Sending to " + dept + " server...");

            new Thread(() -> {
                try {
                    byte[] original  = Files.readAllBytes(selectedFile[0].toPath());
                    byte[] encrypted = SecurityManager.encryptData(original);

                    try (Socket socket = new Socket("localhost", targetPort);
                         DataOutputStream dos = new DataOutputStream(socket.getOutputStream())) {
                        dos.writeUTF(selectedFile[0].getName());
                        dos.writeLong(encrypted.length);
                        dos.writeInt(expiryHours);
                        dos.writeInt(maxViews);
                        dos.writeBoolean(isViewOnly);
                        dos.writeUTF(dept);
                        dos.writeUTF(loggedInUser);
                        dos.write(encrypted);
                        dos.flush();
                    }

                    SwingUtilities.invokeLater(() -> {
                        progressBar.setVisible(false);
                        sendBtn.setEnabled(true);
                        statusLabel.setForeground(ACCENT_GREEN);
                        statusLabel.setText("Sent successfully to " + dept + " (port " + targetPort + ")");
                        JOptionPane.showMessageDialog(frame,
                                "File sent to " + dept + " server (port " + targetPort + ")",
                                "Success", JOptionPane.INFORMATION_MESSAGE);
                    });

                } catch (Exception ex) {
                    LOG.warning("Send failed: " + ex.getMessage());
                    SwingUtilities.invokeLater(() -> {
                        progressBar.setVisible(false);
                        sendBtn.setEnabled(true);
                        statusLabel.setForeground(ACCENT_RED);
                        statusLabel.setText("Failed: " + ex.getMessage());
                        JOptionPane.showMessageDialog(frame, "Error: " + ex.getMessage(),
                                "Send Failed", JOptionPane.ERROR_MESSAGE);
                    });
                }
            }).start();
        });

        return wrapper;
    }

    // ── UI Helpers ────────────────────────────────────────────────────────────
    private JLabel formLabel(String text) {
        JLabel lbl = new JLabel(text);
        lbl.setForeground(TEXT_PRIMARY);
        lbl.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        return lbl;
    }

    private JLabel styledLabel(String text) {
        JLabel lbl = new JLabel(text);
        lbl.setForeground(TEXT_PRIMARY);
        lbl.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        return lbl;
    }

    private JButton styledButton(String text, Color color) {
        JButton btn = new JButton(text);
        btn.setBackground(color);
        btn.setForeground(Color.WHITE);
        btn.setFont(new Font("Segoe UI", Font.BOLD, 13));
        btn.setFocusPainted(false);
        btn.setBorder(new EmptyBorder(9, 18, 9, 18));
        btn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        btn.setOpaque(true);
        return btn;
    }

    private void styleField(JComponent field) {
        field.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        field.setBackground(BG_DARK);
        field.setForeground(TEXT_PRIMARY);
        field.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(ACCENT_BLUE, 1),
                new EmptyBorder(4, 8, 4, 8)));
    }

    private void styleComboBox(JComboBox<String> box) {
        box.setBackground(BG_DARK);
        box.setForeground(TEXT_PRIMARY);
        box.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        box.setBorder(BorderFactory.createLineBorder(ACCENT_BLUE, 1));
    }
}