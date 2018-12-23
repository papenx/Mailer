package University.Encryption;

import javax.crypto.Cipher;
import java.security.*;
import java.util.Base64;
import java.util.logging.Level;
import java.util.logging.Logger;

import static University.Info.MailInfo.RSA_ALG;
import static University.Utilities.FileKeysUtility.writeToFileKeysRSA;
import static java.nio.charset.StandardCharsets.UTF_8;

public class RSA {
    private static final Logger logger = Logger.getLogger(RSA.class.getName());

    public static void generateKeysRSA(String pathToFolder, String user){
        try {
            KeyPairGenerator keyPairGenerator = KeyPairGenerator.getInstance(RSA_ALG);
            keyPairGenerator.initialize(2048, new SecureRandom());
            KeyPair keyPair = keyPairGenerator.generateKeyPair();

            PrivateKey privateKey = keyPair.getPrivate();
            PublicKey publicKey = keyPair.getPublic();
            writeToFileKeysRSA(pathToFolder, privateKey, publicKey, user);
        } catch (NoSuchAlgorithmException e) {
            logger.log(Level.INFO, e.getMessage());
        }
    }

    public static String encrypt(String password, PublicKey publicKey) throws Exception {
        Cipher encryptCipher = Cipher.getInstance(RSA_ALG);
        encryptCipher.init(Cipher.ENCRYPT_MODE, publicKey);

        byte[] cipherText = encryptCipher.doFinal(password.getBytes(UTF_8));

        return Base64.getEncoder().encodeToString(cipherText);
    }

    public static String decrypt(String password, PrivateKey privateKey) throws Exception {
        byte[] bytes = Base64.getDecoder().decode(password);

        Cipher decriptCipher = Cipher.getInstance(RSA_ALG);
        decriptCipher.init(Cipher.DECRYPT_MODE, privateKey);

        return new String(decriptCipher.doFinal(bytes), UTF_8);
    }
}
