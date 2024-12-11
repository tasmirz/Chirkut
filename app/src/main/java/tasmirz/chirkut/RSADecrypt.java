package tasmirz.chirkut;

import javax.crypto.Cipher;
import java.security.PrivateKey;
import java.util.Base64;

public class RSADecrypt {
    public static String decrypt(String cipherText, PrivateKey privateKey) throws Exception {
        Cipher cipher = Cipher.getInstance("RSA");
        cipher.init(Cipher.DECRYPT_MODE, privateKey);

        byte[] decryptedBytes = cipher.doFinal(Base64.getDecoder().decode(cipherText));
        return new String(decryptedBytes);
    }
}
