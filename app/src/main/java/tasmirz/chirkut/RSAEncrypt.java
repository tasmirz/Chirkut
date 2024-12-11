package tasmirz.chirkut;

import javax.crypto.Cipher;
import java.security.PublicKey;
import java.util.Base64;

public class RSAEncrypt {
    public static String encrypt(String plainText, PublicKey publicKey) throws Exception {
        Cipher cipher = Cipher.getInstance("RSA");
        cipher.init(Cipher.ENCRYPT_MODE, publicKey);

        byte[] encryptedBytes = cipher.doFinal(plainText.getBytes());
        return Base64.getEncoder().encodeToString(encryptedBytes);
    }
}
