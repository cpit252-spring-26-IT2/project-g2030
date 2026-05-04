package sa.edu.kau.fcit.cpit252.project.model;

public class SecureFile {
    private final String id;
    private final String name;
    private final String department;
    private final String encryption;
    private final boolean urgent;

    // الإضافات الجديدة
    private final int maxViews;
    private final boolean viewOnly;
    private int currentViews = 0; // عداد المشاهدات يبدأ من صفر

    protected SecureFile(String id, String name, String department, String encryption, boolean urgent, int maxViews, boolean viewOnly) {
        this.id = id;
        this.name = name;
        this.department = department;
        this.encryption = encryption;
        this.urgent = urgent;
        this.maxViews = maxViews;
        this.viewOnly = viewOnly;
    }

    public String getName() {
        return name;
    }

    public boolean isViewOnly() {
        return viewOnly;
    }

    // ميثود تشيك إذا يقدر يشوف الملف أو خلصت محاولاته
    public boolean canView() {
        if (currentViews < maxViews) {
            currentViews++;
            return true;
        }
        return false; // تجاوز الحد المسموح
    }

    @Override
    public String toString() {
        return String.format("File[ID=%s, Name=%s, Dept=%s, Enc=%s, Urgent=%b, MaxViews=%d, ViewOnly=%b]",
                id, name, department, encryption, urgent, maxViews, viewOnly);
    }
}