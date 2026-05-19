package sa.edu.kau.fcit.cpit252.project;

import sa.edu.kau.fcit.cpit252.project.core.DepartmentServer;
import sa.edu.kau.fcit.cpit252.project.logger.AuditLogger;
import sa.edu.kau.fcit.cpit252.project.model.FileVersion;
import sa.edu.kau.fcit.cpit252.project.model.SecureFile;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.DefaultTableCellRenderer;
import java.awt.*;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

public class SFTSGui extends JFrame {

    // ── الألوان الرئيسية ──────────────────────────────────────────────────────
    private static final Color BG_DARK      = new Color(30, 30, 46);
    private static final Color BG_PANEL     = new Color(45, 45, 65);
    private static final Color ACCENT_BLUE  = new Color(100, 149, 237);
    private static final Color ACCENT_GREEN = new Color(80, 200, 120);
    private static final Color ACCENT_RED   = new Color(220, 80, 80);
    private static final Color TEXT_PRIMARY = new Color(220, 220, 235);
    private static final Color TEXT_MUTED   = new Color(140, 140, 160);

    private static final DateTimeFormatter FMT      = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");
    private static final DateTimeFormatter FMT_LONG = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    private DefaultTableModel tableModel;
    private JTable            fileTable;
    private JTextArea         logArea;

    // ── Entry Point ───────────────────────────────────────────────────────────
    public static void launchAdminPanel() {
        SwingUtilities.invokeLater(() -> new SFTSGui().setVisible(true));
    }

    public SFTSGui() {
        setTitle("SFTS Admin Panel — Manage Files");
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setSize(1200, 700);
        setLocationRelativeTo(null);
        setBackground(BG_DARK);

        if (!login()) { dispose(); return; }
        initLayout();
    }

    // ── Login Dialog (JDialog) ────────────────────────────────────────────────
    private boolean login() {
        JDialog dialog = new JDialog((Frame) null, "Admin Login", true);
        dialog.setSize(380, 220);
        dialog.setLocationRelativeTo(null);
        dialog.setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
        dialog.getContentPane().setBackground(BG_DARK);
        dialog.setLayout(new BorderLayout());

        // Header
        JLabel title = new JLabel("Admin Login", SwingConstants.CENTER);
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

        if (!confirmed[0]) return false;

        String user = userField.getText().trim();
        String pass = new String(passField.getPassword()).trim();

        if (!"admin".equals(user) || !"admin".equals(pass)) {
            JOptionPane.showMessageDialog(null, "Invalid credentials.",
                    "Login Failed", JOptionPane.ERROR_MESSAGE);
            return false;
        }
        return true;
    }

    // ── Layout الرئيسي ────────────────────────────────────────────────────────
    private void initLayout() {
        getContentPane().setBackground(BG_DARK);
        setLayout(new BorderLayout(0, 0));

        add(buildHeader(), BorderLayout.NORTH);

        JSplitPane split = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT,
                buildTablePanel(), buildLogPanel());
        split.setDividerLocation(680);
        split.setBackground(BG_DARK);
        split.setBorder(null);
        add(split, BorderLayout.CENTER);

        add(buildButtonPanel(), BorderLayout.SOUTH);

        refreshData();
    }

    // ── Header ────────────────────────────────────────────────────────────────
    private JPanel buildHeader() {
        JPanel header = new JPanel(new BorderLayout());
        header.setBackground(BG_PANEL);
        header.setBorder(new EmptyBorder(12, 20, 12, 20));

        JLabel title = new JLabel("SFTS Admin Panel");
        title.setFont(new Font("Segoe UI", Font.BOLD, 20));
        title.setForeground(ACCENT_BLUE);

        JLabel subtitle = new JLabel("Secure File Transfer System — Admin Console");
        subtitle.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        subtitle.setForeground(TEXT_MUTED);

        JPanel textPanel = new JPanel(new GridLayout(2, 1));
        textPanel.setBackground(BG_PANEL);
        textPanel.add(title);
        textPanel.add(subtitle);
        header.add(textPanel, BorderLayout.WEST);

        JLabel countLabel = new JLabel("Logged in as: admin", SwingConstants.RIGHT);
        countLabel.setForeground(TEXT_MUTED);
        countLabel.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        header.add(countLabel, BorderLayout.EAST);

        return header;
    }

    // ── Table Panel ───────────────────────────────────────────────────────────
    private JScrollPane buildTablePanel() {
        String[] columns = {"#", "Department", "File Name", "Sender", "Expiry", "Views", "Versions", "Status"};
        tableModel = new DefaultTableModel(columns, 0) {
            @Override public boolean isCellEditable(int r, int c) { return false; }
        };

        fileTable = new JTable(tableModel);
        fileTable.setBackground(BG_DARK);
        fileTable.setForeground(TEXT_PRIMARY);
        fileTable.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        fileTable.setRowHeight(32);
        fileTable.setShowGrid(false);
        fileTable.setIntercellSpacing(new Dimension(0, 4));
        fileTable.setSelectionBackground(new Color(70, 90, 130));
        fileTable.setSelectionForeground(Color.WHITE);

        fileTable.getTableHeader().setBackground(BG_PANEL);
        fileTable.getTableHeader().setForeground(ACCENT_BLUE);
        fileTable.getTableHeader().setFont(new Font("Segoe UI", Font.BOLD, 13));
        fileTable.getTableHeader().setBorder(BorderFactory.createEmptyBorder());

        int[] widths = {35, 110, 200, 120, 130, 70, 70, 90};
        for (int i = 0; i < widths.length; i++)
            fileTable.getColumnModel().getColumn(i).setPreferredWidth(widths[i]);

        // Status column color
        fileTable.getColumnModel().getColumn(7).setCellRenderer(new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable t, Object val,
                                                           boolean sel, boolean focus, int row, int col) {
                super.getTableCellRendererComponent(t, val, sel, focus, row, col);
                setHorizontalAlignment(CENTER);
                setFont(new Font("Segoe UI", Font.BOLD, 12));
                String status = val == null ? "" : val.toString();
                setForeground(switch (status) {
                    case "DELETED" -> ACCENT_RED;
                    case "ACTIVE"  -> ACCENT_GREEN;
                    default        -> TEXT_MUTED;
                });
                setBackground(sel ? new Color(70, 90, 130) : BG_DARK);
                return this;
            }
        });

        JScrollPane scroll = new JScrollPane(fileTable);
        scroll.getViewport().setBackground(BG_DARK);
        scroll.setBorder(BorderFactory.createTitledBorder(
                BorderFactory.createLineBorder(BG_PANEL, 2),
                "  Files List", 0, 0,
                new Font("Segoe UI", Font.BOLD, 13), ACCENT_BLUE));
        return scroll;
    }

    // ── Log Panel ─────────────────────────────────────────────────────────────
    private JScrollPane buildLogPanel() {
        logArea = new JTextArea();
        logArea.setEditable(false);
        logArea.setBackground(new Color(20, 20, 35));
        logArea.setForeground(new Color(180, 220, 180));
        logArea.setFont(new Font("JetBrains Mono", Font.PLAIN, 12));
        logArea.setBorder(new EmptyBorder(10, 12, 10, 12));
        logArea.setCaretColor(ACCENT_GREEN);

        JScrollPane scroll = new JScrollPane(logArea);
        scroll.getViewport().setBackground(new Color(20, 20, 35));
        scroll.setBorder(BorderFactory.createTitledBorder(
                BorderFactory.createLineBorder(BG_PANEL, 2),
                "  Activity Logs", 0, 0,
                new Font("Segoe UI", Font.BOLD, 13), ACCENT_BLUE));
        return scroll;
    }

    // ── Button Panel ──────────────────────────────────────────────────────────
    private JPanel buildButtonPanel() {
        JPanel panel = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 10));
        panel.setBackground(BG_PANEL);
        panel.setBorder(new EmptyBorder(4, 10, 4, 10));

        JButton refreshButton    = styledButton("Refresh",        ACCENT_BLUE);
        JButton recoverButton    = styledButton("Recover File",   ACCENT_GREEN);
        JButton versionsButton   = styledButton("Show Versions",  new Color(180, 140, 220));
        JButton addVersionButton = styledButton("Create Version", new Color(220, 170, 80));

        refreshButton.addActionListener(e -> refreshData());

        recoverButton.addActionListener(e -> {
            SecureFile file = getSelectedFile();
            if (file == null) return;
            if (!file.isDeleted()) { showInfo("File is already active — not in recycle bin."); return; }
            file.recoverFromRecycleBin();
            showInfo("File \"" + file.getFileName() + "\" recovered successfully.");
            refreshData();
        });

        versionsButton.addActionListener(e -> {
            SecureFile file = getSelectedFile();
            if (file == null) return;
            if (file.getVersions().isEmpty()) { showInfo("No versions found for this file."); return; }
            showVersionsDialog(file);
        });

        addVersionButton.addActionListener(e -> {
            SecureFile file = getSelectedFile();
            if (file == null) return;
            String content = JOptionPane.showInputDialog(this,
                    "Enter content or notes for the new version:",
                    "Create Version", JOptionPane.PLAIN_MESSAGE);
            if (content == null || content.isBlank()) return;
            file.addVersion(content, "admin");
            showInfo("Version #" + file.getVersions().size() + " created for \"" + file.getFileName() + "\".");
            refreshData();
        });

        panel.add(refreshButton);
        panel.add(recoverButton);
        panel.add(versionsButton);
        panel.add(addVersionButton);

        JLabel status = new JLabel("Ready");
        status.setForeground(TEXT_MUTED);
        status.setFont(new Font("Segoe UI", Font.ITALIC, 11));
        panel.add(Box.createHorizontalStrut(20));
        panel.add(status);

        return panel;
    }

    // ── Versions Dialog ───────────────────────────────────────────────────────
    private void showVersionsDialog(SecureFile file) {
        String[] cols = {"#", "Updated By", "Created At", "Snapshot"};
        DefaultTableModel vModel = new DefaultTableModel(cols, 0) {
            @Override public boolean isCellEditable(int r, int c) { return false; }
        };

        for (FileVersion ver : file.getVersions()) {
            vModel.addRow(new Object[]{
                    ver.getVersionNumber(),
                    ver.getUpdatedBy(),
                    ver.getCreatedAt().format(FMT_LONG),
                    ver.getContentSnapshot()
            });
        }

        JTable vTable = new JTable(vModel);
        vTable.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        vTable.setRowHeight(28);
        vTable.getTableHeader().setFont(new Font("Segoe UI", Font.BOLD, 13));

        JScrollPane scroll = new JScrollPane(vTable);
        scroll.setPreferredSize(new Dimension(700, 300));

        JOptionPane.showMessageDialog(this, scroll,
                "Versions — " + file.getFileName(),
                JOptionPane.INFORMATION_MESSAGE);
    }

    // ── Data Helpers ──────────────────────────────────────────────────────────
    private void refreshData() {
        tableModel.setRowCount(0);
        int i = 1;
        for (SecureFile f : getAllFiles()) {
            String expiry = f.getExpiryTime() != null ? f.getExpiryTime().format(FMT) : "N/A";
            String status = f.isDeleted() ? "DELETED" : "ACTIVE";
            tableModel.addRow(new Object[]{
                    i++, f.getDepartment(), f.getFileName(), getSender(f),
                    expiry, f.getViewCount() + "/" + f.getMaxViews(),
                    f.getVersions().size(), status
            });
        }
        logArea.setText(buildLogs());
        logArea.setCaretPosition(0);
    }

    private SecureFile getSelectedFile() {
        int row = fileTable.getSelectedRow();
        if (row == -1) { showWarning("Please select a file from the list first."); return null; }
        return getAllFiles().get(row);
    }

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

    private String buildLogs() {
        StringBuilder sb = new StringBuilder();

        // ── File Status ───────────────────────────────────────────────────────
        sb.append("╔══════════════════════════════════════╗\n");
        sb.append("║        Recycle Bin & File Status     ║\n");
        sb.append("╚══════════════════════════════════════╝\n\n");

        for (SecureFile file : getAllFiles()) {
            sb.append("File      : ").append(file.getFileName()).append("\n");
            sb.append("Sender    : ").append(getSender(file)).append("\n");
            sb.append("Last Viewer: ").append(file.getLastViewerId() != null
                    ? file.getLastViewerId() : "Not opened yet").append("\n");
            sb.append("Expiry    : ").append(file.getExpiryTime() != null
                    ? file.getExpiryTime().format(FMT_LONG) : "N/A").append("\n");
            sb.append("Views     : ").append(file.getViewCount())
                    .append("/").append(file.getMaxViews()).append("\n");
            sb.append("Deleted   : ").append(file.isDeleted()       ? "Yes" : "No").append("\n");
            sb.append("Revoked   : ").append(file.isAccessRevoked() ? "Yes" : "No").append("\n");
            sb.append("─────────────────────────────────────\n\n");
        }

        // ── Audit Logs ────────────────────────────────────────────────────────
        sb.append(AuditLogger.getInstance().getLogsAsString());

        return sb.toString();
    }

    // ── UI Helpers ────────────────────────────────────────────────────────────
    private JButton styledButton(String text, Color color) {
        JButton btn = new JButton(text);
        btn.setBackground(color);
        btn.setForeground(Color.WHITE);
        btn.setFont(new Font("Segoe UI", Font.BOLD, 13));
        btn.setFocusPainted(false);
        btn.setBorder(BorderFactory.createEmptyBorder(8, 16, 8, 16));
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

    private JLabel styledLabel(String text) {
        JLabel lbl = new JLabel(text);
        lbl.setForeground(TEXT_PRIMARY);
        lbl.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        return lbl;
    }

    private void showInfo(String msg) {
        JOptionPane.showMessageDialog(this, msg, "Info", JOptionPane.INFORMATION_MESSAGE);
    }

    private void showWarning(String msg) {
        JOptionPane.showMessageDialog(this, msg, "Warning", JOptionPane.WARNING_MESSAGE);
    }
}