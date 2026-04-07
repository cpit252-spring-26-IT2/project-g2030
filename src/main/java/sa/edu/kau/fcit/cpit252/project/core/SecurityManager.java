package sa.edu.kau.fcit.cpit252.project.core;

import java.util.Base64;

public class SecurityManager {
    public static String secureData(String data) {
        return Base64.getEncoder().encodeToString(data.getBytes());
    }
}
