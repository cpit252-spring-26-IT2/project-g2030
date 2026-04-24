package sa.edu.kau.fcit.cpit252.project.model;

public class SecureFile {
    private final String id;
    private final String name;
    private final String department;
    private final String encryption;
    private final boolean urgent;

    protected SecureFile(String id, String name, String department, String encryption, boolean urgent) {
        this.id = id;
        this.name = name;
        this.department = department;
        this.encryption = encryption;
        this.urgent = urgent;
    }

    @Override
    public String toString() {
        return String.format("File[ID=%s, Name=%s, Dept=%s, Enc=%s, Urgent=%b]",
                id, name, department, encryption, urgent);
    }
}
