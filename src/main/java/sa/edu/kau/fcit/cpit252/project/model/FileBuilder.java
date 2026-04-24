package sa.edu.kau.fcit.cpit252.project.model;


public class FileBuilder {
    private String id;
    private String name;
    private String department;
    private String encryption = "AES-256";
    private boolean urgent = false;

    public FileBuilder setId(String id) {
        this.id = id; return this; }

    public FileBuilder setName(String name) {
        this.name = name; return this; }

    public FileBuilder setDept(String dept) {
        this.department = dept; return this; }

    public FileBuilder setEncryption(String enc) {
        this.encryption = enc; return this; }

    public FileBuilder setUrgent(boolean urgent) {
        this.urgent = urgent; return this; }

    public SecureFile build() {
        if (id == null || name == null)
            throw new IllegalStateException("ID and Name are required");

        return new SecureFile(id, name, department, encryption, urgent);
    }
}
