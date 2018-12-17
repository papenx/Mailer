package University.Encryption;

import org.bouncycastle.jce.provider.BouncyCastleProvider;

import javax.crypto.*;
import javax.crypto.spec.DESKeySpec;
import javax.xml.bind.DatatypeConverter;
import java.io.UnsupportedEncodingException;
import java.nio.charset.StandardCharsets;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
import java.security.Security;
import java.security.spec.InvalidKeySpecException;
import java.util.Base64;
import java.util.logging.Logger;

import static University.Info.MailInfo.DES_ALG;

public class DES {
    private static final Logger logger = Logger.getLogger(DES.class.getName());
    static {
        Security.addProvider(new BouncyCastleProvider());
    }
    public static String encryptDES(String content, String password) throws InvalidKeyException, NoSuchAlgorithmException,
            InvalidKeySpecException, NoSuchPaddingException, BadPaddingException, IllegalBlockSizeException, NoSuchProviderException, UnsupportedEncodingException {
        byte[] textBytes = content.getBytes(StandardCharsets.UTF_8);

        SecretKey key = getSecretKeyFromPassword(password);

        Cipher desCipher;
        desCipher = Cipher.getInstance("DES/ECB/PKCS7Padding", "BC");
        desCipher.init(Cipher.ENCRYPT_MODE, key);
        byte[] textEncrypted = desCipher.doFinal(textBytes);

        return Base64.getEncoder().encodeToString(textEncrypted);
    }

    public static String decryptDES(String encContent, String password) throws InvalidKeyException, NoSuchAlgorithmException, InvalidKeySpecException,
            NoSuchPaddingException, BadPaddingException, IllegalBlockSizeException, NoSuchProviderException {
        byte[] textBytes = Base64.getDecoder().decode(encContent);

        SecretKey key = getSecretKeyFromPassword(password);

        Cipher desCipher;

        desCipher = Cipher.getInstance("DES/ECB/PKCS7Padding", "BC");

        desCipher.init(Cipher.DECRYPT_MODE, key);

        byte[] textDecrypted = desCipher.doFinal(textBytes);
        return new String(textDecrypted, StandardCharsets.UTF_8);
    }

    private static SecretKey getSecretKeyFromPassword(String password) throws NoSuchAlgorithmException, InvalidKeyException, InvalidKeySpecException {
        String desKey = password;
        byte[] keyBytes = DatatypeConverter.parseHexBinary(desKey);

        SecretKeyFactory factory = SecretKeyFactory.getInstance(DES_ALG);
        return factory.generateSecret(new DESKeySpec(keyBytes));
    }

}
