package sa.edu.kau.fcit.cpit252.project;

import sa.edu.kau.fcit.cpit252.project.core.DepartmentServer;
import sa.edu.kau.fcit.cpit252.project.core.SFTSClient;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.util.LinkedHashMap;
import java.util.Map;

public class Main {

    // ── الألوان (نفس ثيم Admin Panel) ────────────────────────────────────────
    private static final Color BG_DARK     = new Color(30, 30, 46);
    private static final Color BG_PANEL    = new Color(45, 45, 65);
    private static final Color ACCENT_BLUE = new Color(100, 149, 237);
    private static final Color TEXT_PRIMARY = new Color(220, 220, 235);
    private static final Color TEXT_MUTED   = new Color(140, 140, 160);

    public static void main(String[] args) {
        SwingUtilities.invokeLater(Main::buildLauncher);
    }

    private static void buildLauncher() {
        JFrame frame = new JFrame("SFTS Launcher");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setSize(480, 480);
        frame.setLocationRelativeTo(null);
        frame.getContentPane().setBackground(BG_DARK);
        frame.setLayout(new BorderLayout());

        // ── Header ────────────────────────────────────────────────────────────
        JPanel header = new JPanel(new GridLayout(2, 1, 0, 4));
        header.setBackground(BG_PANEL);
        header.setBorder(new EmptyBorder(18, 20, 18, 20));

        JLabel title = new JLabel(" Secure File Transfer System", SwingConstants.CENTER);
        title.setFont(new Font("Segoe UI", Font.BOLD, 20));
        title.setForeground(ACCENT_BLUE);

        JLabel subtitle = new JLabel("Select a component to launch", SwingConstants.CENTER);
        subtitle.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        subtitle.setForeground(TEXT_MUTED);

        header.add(title);
        header.add(subtitle);
        frame.add(header, BorderLayout.NORTH);

        // ── Servers Section ───────────────────────────────────────────────────
        JPanel serversPanel = new JPanel(new GridLayout(4, 1, 0, 8));
        serversPanel.setBackground(BG_DARK);
        serversPanel.setBorder(BorderFactory.createTitledBorder(
                BorderFactory.createLineBorder(BG_PANEL, 2),
                " Department Servers",
                0, 0,
                new Font("Segoe UI", Font.BOLD, 12),
                ACCENT_BLUE));

        // port → department name
        Map<String, String> servers = new LinkedHashMap<>();
        servers.put("ER",          "9090");
        servers.put("Laboratory",  "9091");
        servers.put("Radiology",   "9092");
        servers.put("Pharmacy",    "9093");

        Map<String, Color> deptColors = new LinkedHashMap<>();
        deptColors.put("ER",         new Color(220, 80,  80));
        deptColors.put("Laboratory", new Color(80,  180, 120));
        deptColors.put("Radiology",  new Color(100, 149, 237));
        deptColors.put("Pharmacy",   new Color(220, 170, 80));

        for (Map.Entry<String, String> entry : servers.entrySet()) {
            String dept = entry.getKey();
            String port = entry.getValue();
            Color  color = deptColors.get(dept);

            JButton btn = serverButton("Start " + dept + " Server", port, color);
            btn.addActionListener(e -> {
                btn.setEnabled(false);
                btn.setText(dept + " Server Running  (port " + port + ")");
                new Thread(() -> new DepartmentServer(dept).launch()).start();
            });
            serversPanel.add(btn);
        }

        // ── Tools Section ─────────────────────────────────────────────────────
        JPanel toolsPanel = new JPanel(new GridLayout(1, 2, 10, 0));
        toolsPanel.setBackground(BG_DARK);
        toolsPanel.setBorder(BorderFactory.createTitledBorder(
                BorderFactory.createLineBorder(BG_PANEL, 2),
                "   Tools",
                0, 0,
                new Font("Segoe UI", Font.BOLD, 12),
                ACCENT_BLUE));

        JButton clientBtn = toolButton("  Open Client",      new Color(130, 100, 220));
        JButton adminBtn  = toolButton("  Open Admin Panel", new Color(60,  140, 180));

        clientBtn.addActionListener(e -> SFTSClient.launchClient());
        adminBtn.addActionListener(e  -> SFTSGui.launchAdminPanel());

        toolsPanel.add(clientBtn);
        toolsPanel.add(adminBtn);

        // ── Center wrapper ────────────────────────────────────────────────────
        JPanel center = new JPanel(new GridLayout(2, 1, 0, 12));
        center.setBackground(BG_DARK);
        center.setBorder(new EmptyBorder(14, 16, 14, 16));
        center.add(serversPanel);
        center.add(toolsPanel);

        frame.add(center, BorderLayout.CENTER);

        // ── Footer ────────────────────────────────────────────────────────────
        JLabel footer = new JLabel("King Abdulaziz University — CPIT-252  |  Spring 2026",
                SwingConstants.CENTER);
        footer.setFont(new Font("Segoe UI", Font.ITALIC, 11));
        footer.setForeground(TEXT_MUTED);
        footer.setBorder(new EmptyBorder(6, 0, 10, 0));
        footer.setBackground(BG_PANEL);
        footer.setOpaque(true);
        frame.add(footer, BorderLayout.SOUTH);

        frame.setVisible(true);
    }

    // ── Button Factories ──────────────────────────────────────────────────────
    private static JButton serverButton(String text, String port, Color color) {
        // خلفية داكنة مشتقة من لون الـ department
        Color bgColor = new Color(
                Math.min(color.getRed()   / 6 + 25, 255),
                Math.min(color.getGreen() / 6 + 25, 255),
                Math.min(color.getBlue()  / 6 + 25, 255)
        );

        JButton btn = new JButton(text + "   ·   port " + port);
        btn.setBackground(bgColor);
        btn.setForeground(color);
        btn.setFont(new Font("Segoe UI", Font.BOLD, 13));
        btn.setFocusPainted(false);
        btn.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(color, 1),
                new EmptyBorder(10, 16, 10, 16)));
        btn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        btn.setOpaque(true);
        return btn;
    }

    private static JButton toolButton(String text, Color color) {
        JButton btn = new JButton(text);
        btn.setBackground(color);
        btn.setForeground(Color.WHITE);
        btn.setFont(new Font("Segoe UI", Font.BOLD, 14));
        btn.setFocusPainted(false);
        btn.setBorder(new EmptyBorder(12, 20, 12, 20));
        btn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        btn.setOpaque(true);
        return btn;
    }
}