package sa.edu.kau.fcit.cpit252.project.core;

import sa.edu.kau.fcit.cpit252.project.security.SecurityManager;

import javax.swing.*;
import javax.swing.filechooser.FileNameExtensionFilter;
import java.awt.*;
import java.io.DataOutputStream;
import java.io.File;
import java.net.Socket;
import java.nio.file.Files;
import java.util.Map;

public class SFTSClient {
    private static final String[] ALLOWED_USERS = {"2338742", "2339709", "2337862"};
    private static final String[] DEPARTMENTS   = {"ER", "Laboratory", "Radiology", "Pharmacy"};
    private static String loggedInUser;

    private static final Map<String, Integer> DEPT_PORTS = Map.of(
            "ER",         9090,
            "Laboratory", 9091,
            "Radiology",  9092,
            "Pharmacy",   9093
    );

    public static void launchClient() {
        SwingUtilities.invokeLater(() -> {
            if (loggedInUser == null) loggedInUser = login();
            if (loggedInUser == null) return;
            showClientGUI();
        });
    }

    private static String login() {
        JPanel p = new JPanel(new GridLayout(2, 2, 8, 8));
        JTextField u      = new JTextField();
        JPasswordField pw = new JPasswordField();
        p.add(new JLabel("User ID:"));  p.add(u);
        p.add(new JLabel("Password:")); p.add(pw);
        int r = JOptionPane.showConfirmDialog(null, p, "Client Login",
                JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);
        if (r != JOptionPane.OK_OPTION) return null;
        String user = u.getText().trim();
        String pass = new String(pw.getPassword()).trim();
        if (!"sfts1234".equals(pass)) {
            JOptionPane.showMessageDialog(null, "Invalid credentials.");
            return null;
        }
        for (String a : ALLOWED_USERS) if (a.equals(user)) return user;
        JOptionPane.showMessageDialog(null, "User not allowed.");
        return null;
    }

    private static void showClientGUI() {
        JFrame frame = new JFrame("SFTS Client — Send Secure File");
        frame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        frame.setSize(520, 460);
        frame.setLayout(new BorderLayout(10, 10));

        JPanel form = new JPanel(new GridLayout(0, 2, 10, 10));
        form.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        JTextField filePathField = new JTextField("No file selected");
        filePathField.setEditable(false);
        JButton browseBtn             = new JButton("Browse...");
        JComboBox<String> deptBox     = new JComboBox<>(DEPARTMENTS);
        JTextField senderField        = new JTextField(loggedInUser);
        senderField.setEditable(false);
        JSpinner maxViewsSpinner      = new JSpinner(new SpinnerNumberModel(3, 1, 100, 1));
        JSpinner expirySpinner        = new JSpinner(new SpinnerNumberModel(4, 1, 72, 1));
        JCheckBox viewOnlyBox         = new JCheckBox("Enabled", true);
        JLabel portLabel              = new JLabel("Port: " + DEPT_PORTS.get(DEPARTMENTS[0]));

        deptBox.addActionListener(e ->
                portLabel.setText("Port: " + DEPT_PORTS.getOrDefault((String) deptBox.getSelectedItem(), 9090)));

        form.add(new JLabel("Selected File:"));  form.add(filePathField);
        form.add(new JLabel(""));                form.add(browseBtn);
        form.add(new JLabel("Department:"));     form.add(deptBox);
        form.add(new JLabel("Target Port:"));    form.add(portLabel);
        form.add(new JLabel("Sender ID:"));      form.add(senderField);
        form.add(new JLabel("Max Views:"));      form.add(maxViewsSpinner);
        form.add(new JLabel("Expiry (Hours):")); form.add(expirySpinner);
        form.add(new JLabel("View Only Mode:")); form.add(viewOnlyBox);

        JLabel hdr = new JLabel("SFTS Secure File Sender | User: " + loggedInUser, SwingConstants.CENTER);
        hdr.setFont(new Font("Arial", Font.BOLD, 15));
        frame.add(hdr, BorderLayout.NORTH);
        frame.add(form, BorderLayout.CENTER);

        JButton sendBtn = new JButton("Send Secure File");
        frame.add(sendBtn, BorderLayout.SOUTH);

        final File[] selectedFile = {null};
        browseBtn.addActionListener(e -> {
            JFileChooser fc = new JFileChooser();
            fc.setAcceptAllFileFilterUsed(false);
            fc.addChoosableFileFilter(new FileNameExtensionFilter(
                    "PDF and Images (*.pdf, *.png, *.jpg, *.jpeg)", "pdf", "png", "jpg", "jpeg"));
            if (fc.showOpenDialog(frame) == JFileChooser.APPROVE_OPTION) {
                selectedFile[0] = fc.getSelectedFile();
                filePathField.setText(selectedFile[0].getName());
            }
        });

        sendBtn.addActionListener(e -> {
            if (selectedFile[0] == null) {
                JOptionPane.showMessageDialog(frame, "Please select a file first.");
                return;
            }
            String  department  = (String) deptBox.getSelectedItem();
            int     targetPort  = DEPT_PORTS.getOrDefault(department, 9090);
            int     maxViews    = (int) maxViewsSpinner.getValue();
            int     expiryHours = (int) expirySpinner.getValue();
            boolean isViewOnly  = viewOnlyBox.isSelected();

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
                        dos.writeUTF(department);
                        dos.writeUTF(loggedInUser);
                        dos.write(encrypted);
                        dos.flush();
                    }
                    SwingUtilities.invokeLater(() ->
                            JOptionPane.showMessageDialog(frame,
                                    "Sent to " + department + " server (port " + targetPort + ")"));
                } catch (Exception ex) {
                    SwingUtilities.invokeLater(() ->
                            JOptionPane.showMessageDialog(frame, "Error: " + ex.getMessage()));
                }
            }).start();
        });

        frame.setLocationRelativeTo(null);
        frame.setVisible(true);
    }
}