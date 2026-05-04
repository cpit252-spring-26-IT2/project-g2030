package sa.edu.kau.fcit.cpit252.project;

import sa.edu.kau.fcit.cpit252.project.model.*;
import sa.edu.kau.fcit.cpit252.project.department.*;
import sa.edu.kau.fcit.cpit252.project.proxy.*;
import javax.swing.*;
import java.awt.*;
import java.io.OutputStream;
import java.io.PrintStream;
import java.util.Arrays;
import java.util.List;

public class SFTSGui {
    private static String loggedInUser = "";

    // Authorized team members
    private static final List<String> AUTHORIZED_USERS = Arrays.asList(
            "Abdulaziz_Bukhari",
            "Motaz_Alsayed",
            "Abdulmalik_Aldahari"
    );

    public static void main(String[] args) {
        // 1. Initial Login Phase
        String name = JOptionPane.showInputDialog(null,
                "Enter Username to start session:",
                "SFTS Authentication",
                JOptionPane.QUESTION_MESSAGE);

        if (name == null || name.trim().isEmpty() || !AUTHORIZED_USERS.contains(name.trim())) {
            JOptionPane.showMessageDialog(null, "Unauthorized Access! System Locked.", "Security Alert", JOptionPane.ERROR_MESSAGE);
            System.exit(0);
        }

        loggedInUser = name.trim();

        // 2. Main Dashboard Setup
        JFrame frame = new JFrame("SFTS Dashboard - Active User: " + loggedInUser);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setSize(800, 600);
        frame.setLayout(new BorderLayout(10, 10));

        // System Log Terminal
        JTextArea logArea = new JTextArea();
        logArea.setEditable(false);
        logArea.setBackground(Color.BLACK);
        logArea.setForeground(new Color(0, 255, 0));
        logArea.setFont(new Font("Monospaced", Font.BOLD, 13));
        frame.add(new JScrollPane(logArea), BorderLayout.CENTER);

        // Redirect System.out to the logArea
        System.setOut(new PrintStream(new OutputStream() {
            public void write(int b) { logArea.append(String.valueOf((char) b)); }
        }));

        // Control Panel
        JPanel panel = new JPanel(new FlowLayout());
        JButton btnSendFile = new JButton("📤 Send New File");
        JButton btnClear = new JButton("Clear Logs");
        JButton btnExit = new JButton("Exit");

        panel.add(btnSendFile);
        panel.add(btnClear);
        panel.add(btnExit);
        frame.add(panel, BorderLayout.NORTH);

        // 3. Logic for "Send New File" Button
        btnSendFile.addActionListener(e -> {
            // Step A: Ask for File Type (e.g., PDF, Image)
            String fileType = JOptionPane.showInputDialog(frame, "Enter File Type (e.g., PDF, X-Ray):");
            if (fileType == null || fileType.isEmpty()) return;

            // Step B: Ask for File Name
            String fileName = JOptionPane.showInputDialog(frame, "Enter File Name:");
            if (fileName == null || fileName.isEmpty()) return;

            // Step C: Ask for Target Department
            String[] depts = {"LAB", "ER"};
            String targetDept = (String) JOptionPane.showInputDialog(frame,
                    "Select Target Department:",
                    "Routing",
                    JOptionPane.QUESTION_MESSAGE,
                    null, depts, depts[0]);

            if (targetDept == null) return;

            // Step D: Execute Transfer Logic using Patterns
            System.out.println("\n[USER ACTION]: Initiating transfer for " + fileName + " (" + fileType + ")");

            // 1. Use Builder to create the dynamic file
            SecureFile userFile = new FileBuilder()
                    .setId("GEN-" + (int)(Math.random()*1000))
                    .setName(fileName + "." + fileType.toLowerCase())
                    .setDept(targetDept.equals("LAB") ? "Laboratory" : "Emergency Room")
                    .build();

            // 2. Use Factory to get the department object
            Department destination = DepartmentFactory.getDepartment(targetDept);

            // 3. Use Proxy to handle security and processing
            Department proxy = new DepartmentProxy(destination, loggedInUser);
            proxy.processFile(userFile);
        });

        btnClear.addActionListener(e -> logArea.setText(""));
        btnExit.addActionListener(e -> System.exit(0));

        System.out.println("Login Successful. Welcome back, " + loggedInUser + ".");
        System.out.println("Ready to process secure transfers...\n");

        frame.setLocationRelativeTo(null);
        frame.setVisible(true);
    }
}