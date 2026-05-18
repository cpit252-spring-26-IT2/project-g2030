package sa.edu.kau.fcit.cpit252.project.core;

import sa.edu.kau.fcit.cpit252.project.model.SecureFile;
import javax.imageio.ImageIO;
import javax.swing.Timer;
import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.net.ServerSocket;
import java.time.Duration;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class DepartmentServer {

    public static final Map<String, Integer> DEPT_PORTS = new HashMap<>();
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

    private final String department;
    private final int port;
    private final DefaultListModel<SecureFile> vaultModel = new DefaultListModel<>();
    private final Map<String, byte[]> fileDataVault = new HashMap<>();
    private final Map<String, String> fileSenderVault = new HashMap<>();
    private final ExecutorService threadPool = Executors.newFixedThreadPool(10);
    private String currentUserId = "";

    private static final Map<Integer, DepartmentServer> INSTANCES = new HashMap<>();

    public DepartmentServer(String department) {
        this.department = department;
        this.port = DEPT_PORTS.getOrDefault(department, 9090);
    }

    public static DepartmentServer getByPort(int port) {
        return INSTANCES.get(port);
    }

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
        for (int i = 0; i < vaultModel.size(); i++) {
            list.add(vaultModel.getElementAt(i));
        }
        return list;
    }

    public String getDepartment() {
        return department;
    }

    public int getPort() {
        return port;
    }

    public void launch() {
        if (!login()) return;
        INSTANCES.put(port, this);
        startSocket();
        SwingUtilities.invokeLater(this::buildGUI);
    }

    public void launchHeadless() {
        INSTANCES.put(port, this);
        startSocket();
        System.out.println(department + " headless server started on port " + port);
        try {
            Thread.currentThread().join();
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }

    private boolean login() {
        JPanel panel = new JPanel(new GridLayout(2, 2, 8, 8));
        JTextField userField = new JTextField();
        JPasswordField passField = new JPasswordField();

        panel.add(new JLabel("User ID:"));
        panel.add(userField);
        panel.add(new JLabel("Password:"));
        panel.add(passField);

        int r = JOptionPane.showConfirmDialog(
                null,
                panel,
                "Login — " + department + " Server",
                JOptionPane.OK_CANCEL_OPTION,
                JOptionPane.PLAIN_MESSAGE
        );

        if (r != JOptionPane.OK_OPTION) return false;

        String user = userField.getText().trim();
        String pass = new String(passField.getPassword()).trim();

        if (!ALLOWED_USERS.containsKey(user) || !ALLOWED_USERS.get(user).equals(pass)) {
            JOptionPane.showMessageDialog(null, "Invalid credentials.");
            return false;
        }

        currentUserId = user;
        return true;
    }

    private void startSocket() {
        new Thread(() -> {
            try (ServerSocket ss = new ServerSocket(port)) {
                System.out.println(department + " server listening on port " + port);
                while (true) {
                    threadPool.execute(new DeptClientHandler(ss.accept(), this));
                }
            } catch (Exception e) {
                System.err.println(department + " server error: " + e.getMessage());
            }
        }, department + "-ServerThread").start();
    }

    private void buildGUI() {
        JFrame frame = new JFrame("SFTS — " + department + " Server | Port " + port);
        frame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        frame.setSize(1100, 700);
        frame.setLayout(new BorderLayout(10, 10));

        JLabel title = new JLabel(
                department + " Secure Vault | Port: " + port + " | User: " + currentUserId,
                SwingConstants.CENTER
        );
        title.setFont(new Font("Arial", Font.BOLD, 17));
        frame.add(title, BorderLayout.NORTH);

        DefaultListModel<SecureFile> visibleModel = new DefaultListModel<>();
        JList<SecureFile> fileList = new JList<>(visibleModel);
        fileList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        fileList.setFont(new Font("Monospaced", Font.BOLD, 13));

        fileList.setCellRenderer(new DefaultListCellRenderer() {
            @Override
            public Component getListCellRendererComponent(
                    JList<?> list, Object value, int index, boolean isSelected, boolean hasFocus
            ) {
                super.getListCellRendererComponent(list, value, index, isSelected, hasFocus);
                if (value instanceof SecureFile f) {
                    String exp = f.getExpiryTime() != null
                            ? f.getExpiryTime().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm"))
                            : "N/A";

                    String rem = f.getExpiryTime() != null
                            ? countdown(f.getExpiryTime())
                            : "N/A";

                    String st = f.isDeleted()
                            ? " [DELETED]"
                            : f.isAccessRevoked()
                            ? " [REVOKED]"
                            : " [ACTIVE]";

                    setText(
                            f.getFileName()
                                    + " | Views: " + f.getViewCount() + "/" + f.getMaxViews()
                                    + " | Expiry: " + exp
                                    + " | Remaining: " + rem
                                    + st
                    );
                }
                return this;
            }
        });

        new Timer(1000, e -> {
            visibleModel.clear();
            for (int i = 0; i < vaultModel.size(); i++) {
                visibleModel.addElement(vaultModel.getElementAt(i));
            }
        }).start();

        fileList.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                if (e.getClickCount() == 2) {
                    openSelected(fileList, visibleModel, frame);
                }
            }
        });

        JButton openBtn = new JButton("Open File");
        JButton removeBtn = new JButton("Remove File");

        openBtn.addActionListener(e -> openSelected(fileList, visibleModel, frame));

        removeBtn.addActionListener(e -> {
            SecureFile f = fileList.getSelectedValue();
            if (f == null) return;

            int c = JOptionPane.showConfirmDialog(
                    frame,
                    "Delete: " + f.getFileName() + "?",
                    "Confirm",
                    JOptionPane.YES_NO_OPTION
            );

            if (c == JOptionPane.YES_OPTION) {
                f.moveToRecycleBin();
                fileList.clearSelection();
            }
        });

        JPanel btns = new JPanel(new GridLayout(1, 2, 10, 0));
        btns.add(openBtn);
        btns.add(removeBtn);

        frame.add(new JScrollPane(fileList), BorderLayout.CENTER);
        frame.add(btns, BorderLayout.SOUTH);
        frame.setLocationRelativeTo(null);
        frame.setVisible(true);
    }

    private void openSelected(JList<SecureFile> list, DefaultListModel<SecureFile> model, JFrame parent) {
        int idx = list.getSelectedIndex();
        if (idx < 0) return;

        SecureFile f = model.getElementAt(idx);

        if (!f.openFile()) {
            JOptionPane.showMessageDialog(parent, "Access denied or expired.");
            return;
        }

        showViewer(f, fileDataVault.get(f.getFileId()), currentUserId);
        list.clearSelection();
    }

    private static String countdown(LocalDateTime expiry) {
        Duration d = Duration.between(LocalDateTime.now(), expiry);
        if (d.isNegative() || d.isZero()) return "Expired";
        return String.format("%02dh:%02dm:%02ds",
                d.toHours(),
                d.toMinutesPart(),
                d.toSecondsPart());
    }

    private void showViewer(SecureFile file, byte[] data, String viewerId) {
        JFrame viewer = new JFrame("Viewer — " + file.getFileName() + " [" + department + "]");
        viewer.setSize(900, 650);
        viewer.setLayout(new BorderLayout());

        viewer.add(
                new JLabel("Viewer ID: " + viewerId + " | Dept: " + department, SwingConstants.CENTER),
                BorderLayout.NORTH
        );

        String lower = file.getFileName().toLowerCase();

        try {
            if (data != null && (lower.endsWith(".png") || lower.endsWith(".jpg") || lower.endsWith(".jpeg"))) {
                BufferedImage img = ImageIO.read(new ByteArrayInputStream(data));
                if (img != null) {
                    JPanel panel = new JPanel() {
                        @Override
                        protected void paintComponent(Graphics g) {
                            super.paintComponent(g);
                            g.drawImage(img, 0, 0, getWidth(), getHeight(), this);

                            g.setColor(new Color(255, 0, 0, 110));
                            g.setFont(new Font("Arial", Font.BOLD, 26));

                            for (int y = 50; y < getHeight(); y += 160) {
                                for (int x = 20; x < getWidth(); x += 260) {
                                    g.drawString("Viewer: " + viewerId, x, y);
                                }
                            }
                        }
                    };
                    viewer.add(panel, BorderLayout.CENTER);
                }
            } else if (data != null && lower.endsWith(".pdf")) {
                JTextArea ta = new JTextArea("PDF: " + file.getFileName() + "\nViewer: " + viewerId);
                ta.setEditable(false);
                viewer.add(new JScrollPane(ta), BorderLayout.CENTER);
            } else {
                viewer.add(new JLabel("Unsupported or missing data.", SwingConstants.CENTER), BorderLayout.CENTER);
            }
        } catch (Exception ex) {
            viewer.add(new JLabel("Viewer error: " + ex.getMessage(), SwingConstants.CENTER), BorderLayout.CENTER);
        }

        viewer.setLocationRelativeTo(null);
        viewer.setVisible(true);
    }
}