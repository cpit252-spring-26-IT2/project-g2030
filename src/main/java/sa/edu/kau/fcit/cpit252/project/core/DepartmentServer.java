package sa.edu.kau.fcit.cpit252.project.core;

import sa.edu.kau.fcit.cpit252.project.logger.AuditLogger;
import sa.edu.kau.fcit.cpit252.project.model.SecureFile;
import java.awt.Desktop;
import javax.imageio.ImageIO;
import javax.swing.*;
import javax.swing.Timer;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.net.ServerSocket;
import java.time.Duration;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.logging.Logger;

public class DepartmentServer {

    // ── Logger ────────────────────────────────────────────────────────────────
    private static final Logger LOG = Logger.getLogger(DepartmentServer.class.getName());

    // ── Theme ─────────────────────────────────────────────────────────────────
    private static final Color BG_DARK      = new Color(30, 30, 46);
    private static final Color BG_PANEL     = new Color(45, 45, 65);
    private static final Color ACCENT_BLUE  = new Color(100, 149, 237);
    private static final Color ACCENT_GREEN = new Color(80, 200, 120);
    private static final Color ACCENT_RED   = new Color(220, 80, 80);
    private static final Color TEXT_PRIMARY = new Color(220, 220, 235);
    private static final Color TEXT_MUTED   = new Color(140, 140, 160);

    private static final DateTimeFormatter FMT = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");

    // ── Static Config ─────────────────────────────────────────────────────────
    public static final Map<String, Integer> DEPT_PORTS = new LinkedHashMap<>();
    static {
        DEPT_PORTS.put("ER",         9090);
        DEPT_PORTS.put("Laboratory", 9091);
        DEPT_PORTS.put("Radiology",  9092);
        DEPT_PORTS.put("Pharmacy",   9093);
    }

    private static final Map<String, String> ALLOWED_USERS = new HashMap<>();
    static {
        ALLOWED_USERS.put("2338742", "sfts1234");
        ALLOWED_USERS.put("2339709", "sfts1234");
        ALLOWED_USERS.put("2337862", "sfts1234");
    }

    private static final Map<Integer, DepartmentServer> INSTANCES = new HashMap<>();

    // ── Instance Fields ───────────────────────────────────────────────────────
    private final String                       department;
    private final int                          port;
    private final DefaultListModel<SecureFile> vaultModel      = new DefaultListModel<>();
    private final Map<String, byte[]>          fileDataVault   = new HashMap<>();
    private final Map<String, String>          fileSenderVault = new HashMap<>();
    private final ExecutorService              threadPool      = Executors.newFixedThreadPool(10);
    private       String                       currentUserId   = "";

    private DefaultTableModel tableModel;
    private JTable            fileTable;

    // ── Constructor & Static Access ───────────────────────────────────────────
    public DepartmentServer(String department) {
        this.department = department;
        this.port = DEPT_PORTS.getOrDefault(department, 9090);
    }

    public static DepartmentServer getByPort(int port) { return INSTANCES.get(port); }
    public String getDepartment()                       { return department; }
    public int    getPort()                             { return port; }

    // ── Vault Operations ──────────────────────────────────────────────────────
    public synchronized void addFileToVault(SecureFile file, byte[] data) {
        SwingUtilities.invokeLater(() -> {
            vaultModel.addElement(file);
            fileDataVault.put(file.getFileId(), data);
        });
    }

    public synchronized void registerSender(String fileId, String senderId) {
        fileSenderVault.put(fileId, senderId);
    }

    public synchronized String getSenderForFile(String fileId) {
        return fileSenderVault.get(fileId);
    }

    public synchronized List<SecureFile> getAllFilesSnapshot() {
        List<SecureFile> list = new ArrayList<>();
        for (int i = 0; i < vaultModel.size(); i++) list.add(vaultModel.getElementAt(i));
        return list;
    }

    // ── Launch ────────────────────────────────────────────────────────────────
    public void launch() {
        if (!login()) return;
        INSTANCES.put(port, this);
        startSocket();
        SwingUtilities.invokeLater(this::buildGUI);
    }

    public void launchHeadless() {
        INSTANCES.put(port, this);
        startSocket();
        LOG.info(department + " headless server started on port " + port);
        try { Thread.currentThread().join(); }
        catch (InterruptedException e) { Thread.currentThread().interrupt(); }
    }

    // ── Login Dialog (JDialog) ────────────────────────────────────────────────
    private boolean login() {
        JDialog dialog = new JDialog((Frame) null, "Login — " + department + " Server", true);
        dialog.setSize(380, 220);
        dialog.setLocationRelativeTo(null);
        dialog.setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
        dialog.getContentPane().setBackground(BG_DARK);
        dialog.setLayout(new BorderLayout());

        // Header
        JLabel title = new JLabel("Login — " + department + " Server", SwingConstants.CENTER);
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

        if (!ALLOWED_USERS.containsKey(user) || !ALLOWED_USERS.get(user).equals(pass)) {
            JOptionPane.showMessageDialog(null, "Invalid credentials.",
                    "Login Failed", JOptionPane.ERROR_MESSAGE);
            return false;
        }
        currentUserId = user;
        return true;
    }

    // ── Socket Thread ─────────────────────────────────────────────────────────
    private void startSocket() {
        new Thread(() -> {
            try (ServerSocket ss = new ServerSocket(port)) {
                LOG.info(department + " server listening on port " + port);
                while (true) threadPool.execute(new DeptClientHandler(ss.accept(), this));
            } catch (Exception e) {
                LOG.severe(department + " server error: " + e.getMessage());
            }
        }, department + "-ServerThread").start();
    }

    // ── Main GUI ──────────────────────────────────────────────────────────────
    private void buildGUI() {
        JFrame frame = new JFrame("SFTS  —  " + department + " Server  |  Port " + port);
        frame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        frame.setSize(1100, 700);
        frame.getContentPane().setBackground(BG_DARK);
        frame.setLayout(new BorderLayout(0, 0));

        frame.add(buildServerHeader(), BorderLayout.NORTH);
        frame.add(buildTablePanel(),   BorderLayout.CENTER);
        frame.add(buildActionPanel(frame), BorderLayout.SOUTH);

        frame.setLocationRelativeTo(null);
        frame.setVisible(true);
    }

    // ── Header ────────────────────────────────────────────────────────────────
    private JPanel buildServerHeader() {
        JPanel header = new JPanel(new BorderLayout());
        header.setBackground(BG_PANEL);
        header.setBorder(new EmptyBorder(12, 20, 12, 20));

        JLabel title = new JLabel(department + " Secure Vault");
        title.setFont(new Font("Segoe UI", Font.BOLD, 20));
        title.setForeground(ACCENT_BLUE);

        JLabel info = new JLabel("Port: " + port + "   |   User: " + currentUserId);
        info.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        info.setForeground(TEXT_MUTED);

        JPanel left = new JPanel(new GridLayout(2, 1));
        left.setBackground(BG_PANEL);
        left.add(title);
        left.add(info);
        header.add(left, BorderLayout.WEST);

        JLabel clock = new JLabel("", SwingConstants.RIGHT);
        clock.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        clock.setForeground(TEXT_MUTED);
        header.add(clock, BorderLayout.EAST);

        new Timer(1000, e -> clock.setText(
                java.time.LocalTime.now().format(DateTimeFormatter.ofPattern("HH:mm:ss"))
        )).start();

        return header;
    }

    // ── File Table ────────────────────────────────────────────────────────────
    private JScrollPane buildTablePanel() {
        String[] cols = {"#", "File Name", "Views", "Expiry", "Remaining", "Status"};
        tableModel = new DefaultTableModel(cols, 0) {
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

        // Status column color
        fileTable.getColumnModel().getColumn(5).setCellRenderer(new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable t, Object val,
                                                           boolean sel, boolean focus, int row, int col) {
                super.getTableCellRendererComponent(t, val, sel, focus, row, col);
                setHorizontalAlignment(CENTER);
                setFont(new Font("Segoe UI", Font.BOLD, 12));
                String s = val == null ? "" : val.toString();
                setForeground(switch (s) {
                    case "ACTIVE"  -> ACCENT_GREEN;
                    case "DELETED" -> ACCENT_RED;
                    case "REVOKED" -> new Color(220, 170, 80);
                    default        -> TEXT_MUTED;
                });
                setBackground(sel ? new Color(70, 90, 130) : BG_DARK);
                return this;
            }
        });

        // Remaining column color
        fileTable.getColumnModel().getColumn(4).setCellRenderer(new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable t, Object val,
                                                           boolean sel, boolean focus, int row, int col) {
                super.getTableCellRendererComponent(t, val, sel, focus, row, col);
                String s = val == null ? "" : val.toString();
                setForeground(s.equals("Expired") ? ACCENT_RED : new Color(220, 170, 80));
                setBackground(sel ? new Color(70, 90, 130) : BG_DARK);
                return this;
            }
        });

        int[] widths = {35, 300, 80, 140, 110, 90};
        for (int i = 0; i < widths.length; i++)
            fileTable.getColumnModel().getColumn(i).setPreferredWidth(widths[i]);

        fileTable.addMouseListener(new MouseAdapter() {
            @Override public void mouseClicked(MouseEvent e) {
                if (e.getClickCount() == 2) openSelected(null);
            }
        });

        new Timer(1000, e -> refreshTable()).start();

        JScrollPane scroll = new JScrollPane(fileTable);
        scroll.getViewport().setBackground(BG_DARK);
        scroll.setBorder(BorderFactory.createTitledBorder(
                BorderFactory.createLineBorder(BG_PANEL, 2),
                "  Vault Files",
                0, 0,
                new Font("Segoe UI", Font.BOLD, 13),
                ACCENT_BLUE));
        return scroll;
    }

    private void refreshTable() {
        int selected = fileTable.getSelectedRow();
        tableModel.setRowCount(0);
        int i = 1;
        for (SecureFile f : getAllFilesSnapshot()) {
            String expiry = f.getExpiryTime() != null ? f.getExpiryTime().format(FMT) : "N/A";
            String rem    = f.getExpiryTime() != null ? countdown(f.getExpiryTime()) : "N/A";
            String status = f.isDeleted() ? "DELETED" : f.isAccessRevoked() ? "REVOKED" : "ACTIVE";
            tableModel.addRow(new Object[]{
                    i++, f.getFileName(),
                    f.getViewCount() + "/" + f.getMaxViews(),
                    expiry, rem, status
            });
        }
        if (selected >= 0 && selected < tableModel.getRowCount())
            fileTable.setRowSelectionInterval(selected, selected);
    }

    // ── Action Panel ──────────────────────────────────────────────────────────
    private JPanel buildActionPanel(JFrame frame) {
        JPanel panel = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 10));
        panel.setBackground(BG_PANEL);
        panel.setBorder(new EmptyBorder(4, 10, 4, 10));

        JButton openBtn   = styledButton("Open File",   ACCENT_BLUE);
        JButton removeBtn = styledButton("Remove File", ACCENT_RED);

        openBtn.addActionListener(e -> openSelected(frame));
        removeBtn.addActionListener(e -> {
            int row = fileTable.getSelectedRow();
            if (row < 0) { showWarning(frame, "Select a file first."); return; }
            SecureFile f = getAllFilesSnapshot().get(row);
            int c = JOptionPane.showConfirmDialog(frame,
                    "Move to recycle bin: " + f.getFileName() + "?",
                    "Confirm Delete", JOptionPane.YES_NO_OPTION);
            if (c == JOptionPane.YES_OPTION) {
                f.moveToRecycleBin();
                fileTable.clearSelection();
            }
        });

        panel.add(openBtn);
        panel.add(removeBtn);
        return panel;
    }

    // ── Open File ─────────────────────────────────────────────────────────────
    private void openSelected(JFrame parent) {
        int row = fileTable.getSelectedRow();
        if (row < 0) { showWarning(parent, "Select a file first."); return; }

        SecureFile f = getAllFilesSnapshot().get(row);
        if (!f.openFile()) {
            JOptionPane.showMessageDialog(parent, "Access denied or file expired.",
                    "Access Denied", JOptionPane.WARNING_MESSAGE);
            return;
        }
        f.setLastViewerId(currentUserId);
        AuditLogger.getInstance().log(currentUserId,
                "FILE_OPENED | " + f.getFileName() + " | Dept: " + department);
        showViewer(f, fileDataVault.get(f.getFileId()), currentUserId);
    }

    // ── File Viewer ───────────────────────────────────────────────────────────
    private void showViewer(SecureFile file, byte[] data, String viewerId) {
        JFrame viewer = new JFrame("Viewer — " + file.getFileName() + " [" + department + "]");
        viewer.setSize(900, 650);
        viewer.getContentPane().setBackground(BG_DARK);
        viewer.setLayout(new BorderLayout());

        // ── Info Bar ──────────────────────────────────────────────────────────
        JLabel infoBar = new JLabel(
                "Viewer ID: " + viewerId + " | Department: " + department,
                SwingConstants.CENTER);
        infoBar.setFont(new Font("Segoe UI", Font.BOLD, 13));
        infoBar.setForeground(TEXT_PRIMARY);
        infoBar.setBackground(BG_PANEL);
        infoBar.setOpaque(true);
        infoBar.setBorder(new EmptyBorder(10, 0, 10, 0));
        viewer.add(infoBar, BorderLayout.NORTH);

        String lower = file.getFileName().toLowerCase();

        try {
            if (data != null && (lower.endsWith(".png") || lower.endsWith(".jpg") || lower.endsWith(".jpeg"))) {
                // ── Images ────────────────────────────────────────────────────
                BufferedImage img = ImageIO.read(new ByteArrayInputStream(data));
                if (img != null)
                    viewer.add(new WatermarkedImagePanel(img, viewerId), BorderLayout.CENTER);

            } else if (data != null && lower.endsWith(".pdf")) {
                // ── PDF ───────────────────────────────────────────────────────
                try {
                    org.apache.pdfbox.pdmodel.PDDocument doc =
                            org.apache.pdfbox.Loader.loadPDF(data);
                    org.apache.pdfbox.rendering.PDFRenderer renderer =
                            new org.apache.pdfbox.rendering.PDFRenderer(doc);

                    JPanel pagesPanel = new JPanel();
                    pagesPanel.setBackground(BG_DARK);
                    pagesPanel.setLayout(new BoxLayout(pagesPanel, BoxLayout.Y_AXIS));

                    for (int i = 0; i < doc.getNumberOfPages(); i++) {
                        BufferedImage pageImg = renderer.renderImageWithDPI(i, 150);

                        // Watermark على كل صفحة
                        Graphics2D g = pageImg.createGraphics();
                        g.setColor(new Color(255, 50, 50, 80));
                        g.setFont(new Font("Segoe UI", Font.BOLD, 28));
                        for (int y = 80; y < pageImg.getHeight(); y += 180)
                            for (int x = 20; x < pageImg.getWidth(); x += 300)
                                g.drawString("Viewer: " + viewerId, x, y);
                        g.dispose();

                        JLabel pageLabel = new JLabel(new ImageIcon(pageImg));
                        pageLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
                        pagesPanel.add(pageLabel);
                        pagesPanel.add(Box.createVerticalStrut(10));
                    }

                    doc.close();

                    JScrollPane scroll = new JScrollPane(pagesPanel);
                    scroll.getViewport().setBackground(BG_DARK);
                    scroll.getVerticalScrollBar().setUnitIncrement(20);
                    viewer.add(scroll, BorderLayout.CENTER);

                } catch (Exception ex) {
                    JLabel err = new JLabel("PDF Error: " + ex.getMessage(), SwingConstants.CENTER);
                    err.setForeground(ACCENT_RED);
                    viewer.add(err, BorderLayout.CENTER);
                }

            } else {
                // ── Unsupported ───────────────────────────────────────────────
                JLabel lbl = new JLabel("Unsupported or missing file data.", SwingConstants.CENTER);
                lbl.setForeground(TEXT_MUTED);
                viewer.add(lbl, BorderLayout.CENTER);
            }

        } catch (Exception ex) {
            JLabel err = new JLabel("Viewer error: " + ex.getMessage(), SwingConstants.CENTER);
            err.setForeground(ACCENT_RED);
            viewer.add(err, BorderLayout.CENTER);
        }

        viewer.setLocationRelativeTo(null);
        viewer.setVisible(true);
    }

    // ── Watermarked Image Panel ───────────────────────────────────────────────
    private static class WatermarkedImagePanel extends JPanel {
        private final BufferedImage img;
        private final String        viewerId;

        WatermarkedImagePanel(BufferedImage img, String viewerId) {
            this.img = img;
            this.viewerId = viewerId;
            setBackground(new Color(20, 20, 35));
        }

        @Override
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            g.drawImage(img, 0, 0, getWidth(), getHeight(), this);
            g.setColor(new Color(255, 50, 50, 100));
            g.setFont(new Font("Segoe UI", Font.BOLD, 24));
            for (int y = 60; y < getHeight(); y += 160)
                for (int x = 20; x < getWidth(); x += 260)
                    g.drawString("Viewer: " + viewerId, x, y);
        }
    }

    // ── Helpers ───────────────────────────────────────────────────────────────
    private static String countdown(LocalDateTime expiry) {
        Duration d = Duration.between(LocalDateTime.now(), expiry);
        if (d.isNegative() || d.isZero()) return "Expired";
        return String.format("%02dh:%02dm:%02ds", d.toHours(), d.toMinutesPart(), d.toSecondsPart());
    }

    private JButton styledButton(String text, Color color) {
        JButton btn = new JButton(text);
        btn.setBackground(color);
        btn.setForeground(Color.WHITE);
        btn.setFont(new Font("Segoe UI", Font.BOLD, 13));
        btn.setFocusPainted(false);
        btn.setBorder(new EmptyBorder(8, 20, 8, 20));
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

    private void showWarning(Component parent, String msg) {
        JOptionPane.showMessageDialog(parent, msg, "Warning", JOptionPane.WARNING_MESSAGE);
    }
}