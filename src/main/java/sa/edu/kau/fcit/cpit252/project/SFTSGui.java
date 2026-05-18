package sa.edu.kau.fcit.cpit252.project;

import sa.edu.kau.fcit.cpit252.project.core.DepartmentServer;
import sa.edu.kau.fcit.cpit252.project.model.FileVersion;
import sa.edu.kau.fcit.cpit252.project.model.SecureFile;

import javax.swing.*;
import java.awt.*;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

public class SFTSGui extends JFrame {

    public static void launchAdminPanel() {
        SwingUtilities.invokeLater(() -> new SFTSGui().setVisible(true));
    }

    public SFTSGui() {
        setTitle("Admin - Trash, Recover & Versions");
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setSize(1100, 650);
        setLocationRelativeTo(null);
        if (!login()) { dispose(); return; }
        initLayout();
    }

    // جمع الملفات من كل السيرفرات المشغلة
    private List<SecureFile> getAllFiles() {
        List<SecureFile> all = new ArrayList<>();
        for (int port : DepartmentServer.DEPT_PORTS.values()) {
            DepartmentServer srv = DepartmentServer.getByPort(port);
            if (srv != null) all.addAll(srv.getAllFilesSnapshot());
        }
        return all;
    }

    private String getSender(SecureFile file) {
        for (int port : DepartmentServer.DEPT_PORTS.values()) {
            DepartmentServer srv = DepartmentServer.getByPort(port);
            if (srv != null) {
                String sender = srv.getSenderForFile(file.getFileId());
                if (sender != null) return sender;
            }
        }
        return file.getOwnerId();
    }

    private boolean login() {
        JPanel panel = new JPanel(new GridLayout(2, 2, 8, 8));
        JTextField userField = new JTextField();
        JPasswordField passField = new JPasswordField();
        panel.add(new JLabel("User ID:"));  panel.add(userField);
        panel.add(new JLabel("Password:")); panel.add(passField);
        int result = JOptionPane.showConfirmDialog(this, panel, "Admin Login",
                JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);
        return result == JOptionPane.OK_OPTION
                && "admin".equals(userField.getText().trim())
                && "admin".equals(new String(passField.getPassword()).trim());
    }

    private void initLayout() {
        DefaultListModel<SecureFile> model = new DefaultListModel<>();
        for (SecureFile f : getAllFiles()) model.addElement(f);

        JList<SecureFile> list = new JList<>(model);
        list.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        list.setFont(new Font("Monospaced", Font.PLAIN, 13));
        list.setCellRenderer(new DefaultListCellRenderer() {
            @Override
            public Component getListCellRendererComponent(JList<?> list, Object value,
                                                          int index, boolean isSelected, boolean cellHasFocus) {
                super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
                if (value instanceof SecureFile file) {
                    String expiry = file.getExpiryTime() != null
                            ? file.getExpiryTime().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm"))
                            : "N/A";
                    setText("[" + file.getDepartment() + "] " + file.getFileName()
                            + " | Expiry: " + expiry
                            + (file.isDeleted() ? " [DELETED]" : " [ACTIVE]"));
                }
                return this;
            }
        });

        JTextArea logArea = new JTextArea();
        logArea.setEditable(false);
        logArea.setFont(new Font("Monospaced", Font.PLAIN, 13));

        JButton refreshButton    = new JButton("Refresh");
        JButton recoverButton    = new JButton("Recover File");
        JButton versionsButton   = new JButton("Show Versions");
        JButton addVersionButton = new JButton("Create Version");

        refreshButton.addActionListener(e -> {
            model.clear();
            for (SecureFile f : getAllFiles()) model.addElement(f);
            logArea.setText(buildLogs());
        });

        recoverButton.addActionListener(e -> {
            SecureFile file = list.getSelectedValue();
            if (file == null) { JOptionPane.showMessageDialog(this, "Select a file first."); return; }
            if (!file.isDeleted()) { JOptionPane.showMessageDialog(this, "File is not deleted."); return; }
            file.recoverFromRecycleBin();
            JOptionPane.showMessageDialog(this, "File recovered successfully.");
            refreshButton.doClick();
        });

        versionsButton.addActionListener(e -> {
            SecureFile file = list.getSelectedValue();
            if (file == null) { JOptionPane.showMessageDialog(this, "Select a file first."); return; }
            if (file.getVersions().isEmpty()) { JOptionPane.showMessageDialog(this, "No versions available."); return; }
            StringBuilder sb = new StringBuilder("Versions for: " + file.getFileName() + "\n\n");
            for (FileVersion ver : file.getVersions()) {
                sb.append("Version #").append(ver.getVersionNumber())
                        .append(" | By: ").append(ver.getUpdatedBy())
                        .append(" | At: ").append(ver.getCreatedAt()
                                .format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")))
                        .append("\n").append(ver.getContentSnapshot()).append("\n\n");
            }
            JTextArea ta = new JTextArea(sb.toString());
            ta.setEditable(false);
            ta.setFont(new Font("Monospaced", Font.PLAIN, 13));
            JOptionPane.showMessageDialog(this, new JScrollPane(ta), "Versions",
                    JOptionPane.INFORMATION_MESSAGE);
        });

        addVersionButton.addActionListener(e -> {
            SecureFile file = list.getSelectedValue();
            if (file == null) { JOptionPane.showMessageDialog(this, "Select a file first."); return; }
            String newContent = JOptionPane.showInputDialog(this,
                    "Enter new content / notes for this version:", "Create Version",
                    JOptionPane.PLAIN_MESSAGE);
            if (newContent == null || newContent.isBlank()) return;
            file.addVersion(newContent, "admin");
            JOptionPane.showMessageDialog(this,
                    "Version #" + file.getVersions().size() + " created successfully.");
            refreshButton.doClick();
        });

        JSplitPane split = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT,
                new JScrollPane(list), new JScrollPane(logArea));
        split.setDividerLocation(400);

        JPanel bottom = new JPanel(new FlowLayout(FlowLayout.LEFT));
        bottom.add(refreshButton);
        bottom.add(recoverButton);
        bottom.add(versionsButton);
        bottom.add(addVersionButton);

        add(split, BorderLayout.CENTER);
        add(bottom, BorderLayout.SOUTH);
        refreshButton.doClick();
    }

    private String buildLogs() {
        StringBuilder sb = new StringBuilder("=== Recycle Bin & Activity Logs ===\n\n");
        for (SecureFile file : getAllFiles()) {
            sb.append("File      : ").append(file.getFileName()).append("\n");
            sb.append("Sender    : ").append(getSender(file)).append("\n");
            sb.append("Viewer    : ").append(file.getWatermarkText() != null
                    ? file.getWatermarkText() : "N/A").append("\n");
            sb.append("Department: ").append(file.getDepartment()).append("\n");
            sb.append("Expiry    : ").append(file.getExpiryTime() != null
                    ? file.getExpiryTime().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"))
                    : "N/A").append("\n");
            sb.append("Views     : ").append(file.getViewCount()).append("/")
                    .append(file.getMaxViews()).append("\n");
            sb.append("Versions  : ").append(file.getVersions().size()).append("\n");
            sb.append("Deleted   : ").append(file.isDeleted()).append("\n");
            sb.append("Revoked   : ").append(file.isAccessRevoked()).append("\n");
            sb.append("---------------------------\n");
        }
        return sb.toString();
    }
}