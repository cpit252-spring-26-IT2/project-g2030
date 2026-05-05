package sa.edu.kau.fcit.cpit252.project.department;

import sa.edu.kau.fcit.cpit252.project.model.SecureFile;

public interface Department {
    String getName();
    void processFile(SecureFile file);
}