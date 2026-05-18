package sa.edu.kau.fcit.cpit252.project.security;

import javax.crypto.Cipher;
import javax.crypto.spec.SecretKeySpec;
import java.security.Key;


public class SecurityManager {

    private static final String ALGORITHM = "AES";

    private static final byte[] SECRET_KEY = "SFTS_SecureKey12".getBytes();


    public static byte[] encryptData(byte[] data) throws Exception {
        Key key = new SecretKeySpec(SECRET_KEY, ALGORITHM);
        Cipher cipher = Cipher.getInstance(ALGORITHM);
        cipher.init(Cipher.ENCRYPT_MODE, key);
        return cipher.doFinal(data);
    }


    public static byte[] decryptData(byte[] encryptedData) throws Exception {
        Key key = new SecretKeySpec(SECRET_KEY, ALGORITHM);
        Cipher cipher = Cipher.getInstance(ALGORITHM);
        cipher.init(Cipher.DECRYPT_MODE, key);
        return cipher.doFinal(encryptedData);
    }


    public static String secureData(String data) {
        return java.util.Base64.getEncoder().encodeToString(data.getBytes());
    }
}