package sa.edu.kau.fcit.cpit252.project.model;

public class FileBuilder {
    private String id;
    private String name;
    private String department;
    private String encryption = "AES-256";
    private boolean urgent = false;

    // الإضافات الجديدة بقيم افتراضية
    private int maxViews = 3;
    private boolean viewOnly = true;

    public FileBuilder setId(String id) { this.id = id; return this; }
    public FileBuilder setName(String name) { this.name = name; return this; }
    public FileBuilder setDept(String dept) { this.department = dept; return this; }
    public FileBuilder setEncryption(String enc) { this.encryption = enc; return this; }
    public FileBuilder setUrgent(boolean urgent) { this.urgent = urgent; return this; }

    // دوال البيلدر الجديدة
    public FileBuilder setMaxViews(int maxViews) { this.maxViews = maxViews; return this; }
    public FileBuilder setViewOnly(boolean viewOnly) { this.viewOnly = viewOnly; return this; }

    public SecureFile build() {
        if (id == null || name == null)
            throw new IllegalStateException("ID and Name are required");

        // تمرير الخصائص الجديدة
        return new SecureFile(id, name, department, encryption, urgent, maxViews, viewOnly);
    }
}