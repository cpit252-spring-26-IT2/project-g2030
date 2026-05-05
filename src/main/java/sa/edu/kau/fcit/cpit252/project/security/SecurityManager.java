package sa.edu.kau.fcit.cpit252.project.security;

import javax.crypto.Cipher;
import javax.crypto.spec.SecretKeySpec;
import java.security.Key;

/**
 * SecurityManager is responsible for handling all cryptographic operations
 * within the Secure File Transfer System (SFTS).
 * It uses the AES algorithm for robust data encryption and decryption.
 */
public class SecurityManager {

    // خوارزمية التشفير المستخدمة (Advanced Encryption Standard)
    private static final String ALGORITHM = "AES";

    // المفتاح السري المشترك (يجب أن يكون 16 حرفاً لتوافق AES-128)
    // ملاحظة: في الأنظمة الواقعية، يتم تخزين هذا المفتاح في مكان آمن أو Vault
    private static final byte[] SECRET_KEY = "SFTS_SecureKey12".getBytes();

    /**
     * تشفير البيانات باستخدام مفتاح AES.
     * @param data مصفوفة البايتات للملف الأصلي.
     * @return مصفوفة البايتات المشفرة.
     * @throws Exception في حال حدوث خطأ أثناء عملية التشفير.
     */
    public static byte[] encryptData(byte[] data) throws Exception {
        Key key = new SecretKeySpec(SECRET_KEY, ALGORITHM);
        Cipher cipher = Cipher.getInstance(ALGORITHM);
        cipher.init(Cipher.ENCRYPT_MODE, key);
        return cipher.doFinal(data);
    }

    /**
     * فك تشفير البيانات باستخدام مفتاح AES.
     * @param encryptedData مصفوفة البايتات المشفرة المستلمة.
     * @return مصفوفة البايتات الأصلية بعد فك التشفير.
     * @throws Exception في حال حدوث خطأ أو إذا كان المفتاح غير صحيح.
     */
    public static byte[] decryptData(byte[] encryptedData) throws Exception {
        Key key = new SecretKeySpec(SECRET_KEY, ALGORITHM);
        Cipher cipher = Cipher.getInstance(ALGORITHM);
        cipher.init(Cipher.DECRYPT_MODE, key);
        return cipher.doFinal(encryptedData);
    }

    /**
     * دالة مساعدة لتحويل النصوص البسيطة إلى Base64 (اختياري)
     * يمكن استخدامها للعرض فقط وليس للحماية الفعلية.
     */
    public static String secureData(String data) {
        return java.util.Base64.getEncoder().encodeToString(data.getBytes());
    }
}