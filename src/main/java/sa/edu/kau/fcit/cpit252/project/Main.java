package sa.edu.kau.fcit.cpit252.project;

import sa.edu.kau.fcit.cpit252.project.core.SFTSClient;
import sa.edu.kau.fcit.cpit252.project.core.SFTSServer;

import javax.swing.*;

public class Main {
    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            String[] options = {"Run Server", "Run Client", "Run Both", "Exit"};

            int choice = JOptionPane.showOptionDialog(
                    null,
                    "Welcome to SFTS\nChoose how to start the system:",
                    "SFTS Launcher",
                    JOptionPane.DEFAULT_OPTION,
                    JOptionPane.INFORMATION_MESSAGE,
                    null,
                    options,
                    options[0]
            );

            switch (choice) {
                case 0 -> SFTSServer.main(new String[]{});
                case 1 -> SFTSClient.main(new String[]{});
                case 2 -> {
                    SFTSServer.main(new String[]{});

                    new Thread(() -> {
                        try {
                            Thread.sleep(1500);
                        } catch (InterruptedException ignored) {
                        }
                        SFTSClient.main(new String[]{});
                    }).start();
                }
                default -> System.exit(0);
            }
        });
    }
}