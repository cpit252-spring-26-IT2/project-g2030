package sa.edu.kau.fcit.cpit252.project.core;

import sa.edu.kau.fcit.cpit252.project.model.SecureFile;
import sa.edu.kau.fcit.cpit252.project.model.FileBuilder;
import sa.edu.kau.fcit.cpit252.project.department.*;
import sa.edu.kau.fcit.cpit252.project.proxy.*;
import sa.edu.kau.fcit.cpit252.project.security.SecurityManager;

import javax.swing.*;
import java.awt.*;
import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class SFTSServer {
    private static final int PORT = 8080;
    private static final ExecutorService threadPool = Executors.newFixedThreadPool(10);


    private static DefaultListModel<SecureFile> vaultModel = new DefaultListModel<>();
    private static Map<String, byte[]> fileDataVault = new HashMap<>(); // تخزين الملفات في الذاكرة المؤقتة للسرية

    public static void main(String[] args) {
        SwingUtilities.invokeLater(SFTSServer::createAndShowServerGUI);

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

    private static void createAndShowServerGUI() {
        JFrame frame = new JFrame("SFTS Server - Secure Vault");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setSize(750, 450);
        frame.setLayout(new BorderLayout(10, 10));

        JLabel title = new JLabel(" Receiver's Secure Vault (RAM Storage)", SwingConstants.CENTER);
        title.setFont(new Font("Arial", Font.BOLD, 18));
        title.setForeground(new Color(30, 30, 150));
        frame.add(title, BorderLayout.NORTH);

        JList<SecureFile> fileList = new JList<>(vaultModel);
        fileList.setFont(new Font("Monospaced", Font.BOLD, 14));
        fileList.setBackground(new Color(240, 240, 240));
        frame.add(new JScrollPane(fileList), BorderLayout.CENTER);


        Timer liveTimer = new Timer(1000, e -> fileList.repaint());
        liveTimer.start();

        JButton btnOpen = new JButton(" Open Selected File");
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

            if (!selectedFile.canView()) {
                JOptionPane.showMessageDialog(frame,
                        "❌ ACCESS DENIED!\nSecurity limits reached. File has expired or Maximum Views exceeded.",
                        "Security Block", JOptionPane.ERROR_MESSAGE);

                fileList.repaint();
                return;
            }

            byte[] data = fileDataVault.get(selectedFile.getId());
            showSecureViewer(selectedFile.getName(), data);

            selectedFile.incrementView();
            vaultModel.set(selectedIndex, selectedFile);
        });

        frame.setLocationRelativeTo(null);
        frame.setVisible(true);
    }

    public static synchronized void addFileToVault(SecureFile file, byte[] data) {
        vaultModel.addElement(file);
        fileDataVault.put(file.getId(), data);
        System.out.println("File added to Secure Vault: " + file.getName());
    }

    private static void showSecureViewer(String fileName, byte[] fileData) {
        JFrame viewer = new JFrame("SFTS Secure Viewer - " + fileName);
        viewer.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        viewer.setSize(700, 500);
        viewer.setLayout(new BorderLayout());
        viewer.getContentPane().setBackground(Color.DARK_GRAY);

        JLabel watermark = new JLabel("⚠️ CONFIDENTIAL - VIEW ONLY MODE - RAM SECURED ⚠️", SwingConstants.CENTER);
        watermark.setForeground(Color.RED);
        viewer.add(watermark, BorderLayout.NORTH);

        try {
            if (fileName.toLowerCase().matches(".*\\.(png|jpg|jpeg)")) {
                ImageIcon icon = new ImageIcon(fileData);
                Image img = icon.getImage().getScaledInstance(650, 450, Image.SCALE_SMOOTH);
                viewer.add(new JLabel(new ImageIcon(img)), BorderLayout.CENTER);
            } else {
                JTextArea textArea = new JTextArea("\n\n [Document Data Secured. Use Image for visual preview.]");
                textArea.setForeground(Color.GREEN);
                textArea.setBackground(Color.BLACK);
                viewer.add(new JScrollPane(textArea), BorderLayout.CENTER);
            }
        } catch (Exception e) {
            viewer.add(new JLabel("Format error.", SwingConstants.CENTER), BorderLayout.CENTER);
        }

        viewer.setLocationRelativeTo(null);
        viewer.setAlwaysOnTop(true);
        viewer.setVisible(true);
    }
}