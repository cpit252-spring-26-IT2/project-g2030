package sa.edu.kau.fcit.cpit252.project;

import sa.edu.kau.fcit.cpit252.project.model.FileBuilder;
import sa.edu.kau.fcit.cpit252.project.model.FileVersion;
import sa.edu.kau.fcit.cpit252.project.model.SecureFile;

import javax.swing.*;
import java.awt.*;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class SFTSGui extends JFrame {

    private SecureFile currentFile;

    private final JTextField fileNameField = new JTextField();
    private final JComboBox<String> departmentComboBox = new JComboBox<>(new String[]{"Laboratory", "ER", "Radiology", "Pharmacy"});    private final JTextField ownerIdField = new JTextField();
    private final JTextField fileTypeField = new JTextField("txt");
    private final JTextField maxViewsField = new JTextField("3");
    private final JTextField expiryHoursField = new JTextField("4");
    private final JTextField watermarkField = new JTextField();
    private final JCheckBox encryptedCheckBox = new JCheckBox("Encrypted");
    private final JCheckBox viewOnlyCheckBox = new JCheckBox("View Only");
    private final JTextArea contentArea = new JTextArea(8, 40);

    private final JTextArea outputArea = new JTextArea(14, 50);

    private final JButton createButton = new JButton("Create File");
    private final JButton openButton = new JButton("Open File");
    private final JButton updateButton = new JButton("Update File");
    private final JButton deleteButton = new JButton("Move to Recycle Bin");
    private final JButton recoverButton = new JButton("Recover File");
    private final JButton versionsButton = new JButton("Show Versions");
    private final JButton restoreVersionButton = new JButton("Restore Version #");

    public SFTSGui() {
        setTitle("Secure File Transfer System (SFTS)");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(900, 700);
        setLocationRelativeTo(null);

        initLayout();
        initActions();
    }

    private void initLayout() {
        JPanel mainPanel = new JPanel(new BorderLayout(10, 10));

        JPanel formPanel = new JPanel(new GridLayout(0, 2, 8, 8));
        formPanel.setBorder(BorderFactory.createTitledBorder("File Configuration"));

        formPanel.add(new JLabel("File Name:"));
        formPanel.add(fileNameField);

        formPanel.add(new JLabel("Department:"));
        formPanel.add(departmentComboBox);

        formPanel.add(new JLabel("Owner ID:"));
        formPanel.add(ownerIdField);

        formPanel.add(new JLabel("File Type:"));
        formPanel.add(fileTypeField);

        formPanel.add(new JLabel("Max Views:"));
        formPanel.add(maxViewsField);

        formPanel.add(new JLabel("Expiry (Hours):"));
        formPanel.add(expiryHoursField);

        formPanel.add(new JLabel("Watermark Text:"));
        formPanel.add(watermarkField);

        formPanel.add(new JLabel("Encrypted:"));
        formPanel.add(encryptedCheckBox);

        formPanel.add(new JLabel("View Only:"));
        formPanel.add(viewOnlyCheckBox);

        JPanel contentPanel = new JPanel(new BorderLayout());
        contentPanel.setBorder(BorderFactory.createTitledBorder("File Content"));
        contentArea.setLineWrap(true);
        contentArea.setWrapStyleWord(true);
        contentPanel.add(new JScrollPane(contentArea), BorderLayout.CENTER);

        JPanel buttonsPanel = new JPanel(new GridLayout(2, 4, 8, 8));
        buttonsPanel.setBorder(BorderFactory.createTitledBorder("Actions"));
        buttonsPanel.add(createButton);
        buttonsPanel.add(openButton);
        buttonsPanel.add(updateButton);
        buttonsPanel.add(deleteButton);
        buttonsPanel.add(recoverButton);
        buttonsPanel.add(versionsButton);
        buttonsPanel.add(restoreVersionButton);

        outputArea.setEditable(false);
        outputArea.setLineWrap(true);
        outputArea.setWrapStyleWord(true);

        JPanel outputPanel = new JPanel(new BorderLayout());
        outputPanel.setBorder(BorderFactory.createTitledBorder("System Output"));
        outputPanel.add(new JScrollPane(outputArea), BorderLayout.CENTER);

        JPanel topPanel = new JPanel(new BorderLayout(10, 10));
        topPanel.add(formPanel, BorderLayout.NORTH);
        topPanel.add(contentPanel, BorderLayout.CENTER);

        mainPanel.add(topPanel, BorderLayout.NORTH);
        mainPanel.add(buttonsPanel, BorderLayout.CENTER);
        mainPanel.add(outputPanel, BorderLayout.SOUTH);

        add(mainPanel);
    }

    private void initActions() {
        createButton.addActionListener(e -> createFile());
        openButton.addActionListener(e -> openFile());
        updateButton.addActionListener(e -> updateFile());
        deleteButton.addActionListener(e -> deleteFile());
        recoverButton.addActionListener(e -> recoverFile());
        versionsButton.addActionListener(e -> showVersions());
        restoreVersionButton.addActionListener(e -> restoreVersion());
    }

    private void createFile() {
        try {
            String fileName = fileNameField.getText().trim();
            String department = (String) departmentComboBox.getSelectedItem();
            String ownerId = ownerIdField.getText().trim();
            String fileType = fileTypeField.getText().trim();
            String watermark = watermarkField.getText().trim();
            String content = contentArea.getText();

            int maxViews = Integer.parseInt(maxViewsField.getText().trim());
            int expiryHours = Integer.parseInt(expiryHoursField.getText().trim());

            currentFile = new FileBuilder()
                    .setFileName(fileName)
                    .setDepartment(department)
                    .setOwnerId(ownerId)
                    .setFileType(fileType)
                    .setContent(content)
                    .setEncrypted(encryptedCheckBox.isSelected())
                    .setViewOnly(viewOnlyCheckBox.isSelected())
                    .setMaxViews(maxViews)
                    .setExpiryTime(LocalDateTime.now().plusHours(expiryHours))
                    .setWatermarkText(watermark.isBlank() ? "Viewed by " + ownerId : watermark)
                    .build();

            log("File created successfully.");
            log(currentFile.toString());

        } catch (Exception ex) {
            showError("Failed to create file: " + ex.getMessage());
        }
    }

    private void openFile() {
        if (currentFile == null) {
            showError("Please create a file first.");
            return;
        }

        if (currentFile.openFile()) {
            log("File opened successfully.");
            log(currentFile.getDisplayContent());
            log("View count: " + currentFile.getViewCount() + "/" + currentFile.getMaxViews());
        } else {
            log("Access denied: file cannot be opened.");
            if (currentFile.isDeleted()) {
                log("Reason: file is in recycle bin.");
            } else if (currentFile.isAccessRevoked()) {
                log("Reason: access revoked, expired, or max views reached.");
            }
        }
    }

    private void updateFile() {
        if (currentFile == null) {
            showError("Please create a file first.");
            return;
        }

        if (currentFile.isDeleted()) {
            showError("Cannot update a deleted file.");
            return;
        }

        String updatedBy = JOptionPane.showInputDialog(this, "Enter updater ID:");
        if (updatedBy == null || updatedBy.trim().isBlank()) {
            showError("Updater ID is required.");
            return;
        }

        String newContent = contentArea.getText();
        currentFile.addVersion(newContent, updatedBy.trim());

        log("File updated successfully.");
        log("New version created. Total versions: " + currentFile.getVersions().size());
    }

    private void deleteFile() {
        if (currentFile == null) {
            showError("Please create a file first.");
            return;
        }

        currentFile.moveToRecycleBin();
        log("File moved to recycle bin at: " + formatDateTime(currentFile.getDeletedAt()));
    }

    private void recoverFile() {
        if (currentFile == null) {
            showError("Please create a file first.");
            return;
        }

        if (!currentFile.isDeleted()) {
            showError("File is not deleted.");
            return;
        }

        currentFile.recoverFromRecycleBin();
        log("File recovered successfully from recycle bin.");
    }

    private void showVersions() {
        if (currentFile == null) {
            showError("Please create a file first.");
            return;
        }

        if (currentFile.getVersions().isEmpty()) {
            log("No versions available.");
            return;
        }

        StringBuilder builder = new StringBuilder("File Versions:\n");
        for (FileVersion version : currentFile.getVersions()) {
            builder.append("Version #").append(version.getVersionNumber())
                    .append(" | Updated By: ").append(version.getUpdatedBy())
                    .append(" | Time: ").append(formatDateTime(version.getCreatedAt()))
                    .append("\n");
        }

        log(builder.toString());
    }

    private void restoreVersion() {
        if (currentFile == null) {
            showError("Please create a file first.");
            return;
        }

        String input = JOptionPane.showInputDialog(this, "Enter version number to restore:");
        if (input == null || input.trim().isBlank()) {
            return;
        }

        try {
            int versionNumber = Integer.parseInt(input.trim());
            FileVersion restored = currentFile.restoreVersion(versionNumber);

            if (restored != null) {
                contentArea.setText(currentFile.getContent());
                log("Version #" + versionNumber + " restored successfully.");
            } else {
                showError("Version not found.");
            }

        } catch (NumberFormatException ex) {
            showError("Invalid version number.");
        }
    }

    private void log(String message) {
        outputArea.append(message + "\n");
        outputArea.setCaretPosition(outputArea.getDocument().getLength());
    }

    private void showError(String message) {
        JOptionPane.showMessageDialog(this, message, "Error", JOptionPane.ERROR_MESSAGE);
    }

    private String formatDateTime(LocalDateTime time) {
        if (time == null) {
            return "N/A";
        }
        return time.format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> new SFTSGui().setVisible(true));
    }
}