package sa.edu.kau.fcit.cpit252.project;
import sa.edu.kau.fcit.cpit252.project.core.DepartmentServer;
import sa.edu.kau.fcit.cpit252.project.core.SFTSClient;


import javax.swing.*;
import java.awt.*;

public class Main {
    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            JFrame frame = new JFrame("SFTS Launcher");
            frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
            frame.setSize(460, 380);
            frame.setLayout(new GridLayout(0, 1, 10, 10));

            JLabel title = new JLabel("Secure File Transfer System", SwingConstants.CENTER);
            title.setFont(new Font("Arial", Font.BOLD, 18));

            JButton erBtn   = new JButton("Start ER Server        (port 9090)");
            JButton labBtn  = new JButton("Start Lab Server       (port 9091)");
            JButton radBtn  = new JButton("Start Radiology Server (port 9092)");
            JButton pharBtn = new JButton("Start Pharmacy Server  (port 9093)");
            JButton clientBtn = new JButton("Open Client (Send File)");
            JButton adminBtn  = new JButton("Open Admin Panel");

            erBtn.addActionListener(e   -> new Thread(() -> new DepartmentServer("ER").launch()).start());
            labBtn.addActionListener(e  -> new Thread(() -> new DepartmentServer("Laboratory").launch()).start());
            radBtn.addActionListener(e  -> new Thread(() -> new DepartmentServer("Radiology").launch()).start());
            pharBtn.addActionListener(e -> new Thread(() -> new DepartmentServer("Pharmacy").launch()).start());
            clientBtn.addActionListener(e -> SFTSClient.launchClient());
            adminBtn.addActionListener(e  -> SFTSGui.launchAdminPanel());

            frame.add(title);
            frame.add(erBtn);
            frame.add(labBtn);
            frame.add(radBtn);
            frame.add(pharBtn);
            frame.add(clientBtn);
            frame.add(adminBtn);

            frame.setLocationRelativeTo(null);
            frame.setVisible(true);
        });
    }
}