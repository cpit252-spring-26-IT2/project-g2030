package sa.edu.kau.fcit.cpit252.project.department;

import sa.edu.kau.fcit.cpit252.project.model.SecureFile; // ضفنا هذي عشان يتعرف على الملف

public interface Department {
    String getName();
    void processFile(SecureFile file); // غيرناها من String إلى SecureFile
}