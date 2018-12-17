package University.Encryption;

import University.Utilities.FileKeysUtility;
import javafx.event.ActionEvent;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.security.*;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.X509EncodedKeySpec;
import java.util.logging.Level;
import java.util.logging.Logger;

import static University.Info.MailInfo.*;
import static University.Utilities.FileKeysUtility.*;
import static University.Utilities.FileUtility.chooseDirectory;
import static University.Utilities.FileUtility.chooseFile;

public class DigitalSignatureEmail {
    private static final Logger logger = Logger.getLogger(DigitalSignatureEmail.class.getName());

    public static String signEmailWithSaveSign(ActionEvent event, String content, String username) {
        try {
            File file = chooseFile(event, "Private key RSA (*.spk)", new String[]{"*.spk"});

            byte[] privateKeyBytes = FileKeysUtility.readPrivateKeyRSA(file.getAbsolutePath());

            PrivateKey privateKey = getPrivateKeyFromBytesRSA(privateKeyBytes);

            File selectedDirectory = chooseDirectory(event, "Выберите директорию для сохраниения подписи");

            if (selectedDirectory != null) {
                String fileName = selectedDirectory.getAbsoluteFile() + "/" + username + " " + getDateToString();
                writeSign(signEmail(content, privateKey), fileName);
                return fileName;
            }
        } catch (IOException | NoSuchAlgorithmException | InvalidKeySpecException | NoSuchProviderException | SignatureException | InvalidKeyException | NullPointerException e) {
            logger.log(Level.INFO, e.getMessage());
        }
        return "";
    }

    public static boolean verifyEmail(ActionEvent event, String content) {
        try {
            File publicKeyFile = chooseFile(event, "Public key RSA (*.spub)", new String[]{"*.spub"});
            File signFile = chooseFile(event, "Digital Signature (*.sig)", new String[]{"*.sig"});

            byte[] contentBytes = content.getBytes(StandardCharsets.UTF_8);
            byte[] publicKeyBytes = readPublicKeyRSA(publicKeyFile.getAbsolutePath());

            PublicKey publicKey = getPublicKeyFromBytesRSA(publicKeyBytes);
            byte[] verifyBytes = readSignature(signFile.getAbsolutePath());
            return getVerifySign(publicKey, contentBytes, verifyBytes);

        } catch (IOException | NullPointerException | NoSuchAlgorithmException | InvalidKeySpecException | NoSuchProviderException | InvalidKeyException | SignatureException e) {
            logger.log(Level.INFO, e.getMessage());
        }
        return false;
    }

    public static String generateKeysRSA(String path, String username) {
        try {
            KeyPair keyPair = generateKeyPair();
            PrivateKey privateKey = keyPair.getPrivate();
            PublicKey publicKey = keyPair.getPublic();

            String fullPath = path + "/" + username + " " + getDateToString();

            FileKeysUtility.writePrivateKeyRSA(privateKey, fullPath);
            FileKeysUtility.writePublicKeyRSA(publicKey, fullPath);

            return fullPath;
        } catch (NoSuchAlgorithmException | NoSuchProviderException | IOException e) {
            logger.log(Level.INFO, e.getMessage());
        }
        return "";
    }

    private static byte[] signEmail(String text, PrivateKey key) throws NoSuchAlgorithmException, InvalidKeyException, SignatureException, NoSuchProviderException {
        byte[] textBytes = text.getBytes(StandardCharsets.UTF_8);
        return getSign(textBytes, key);
    }

    private static byte[] getSign(byte[] textBytes, PrivateKey privateKey) throws SignatureException, NoSuchProviderException, NoSuchAlgorithmException, InvalidKeyException {
        Signature signature = Signature.getInstance(MD5_WITH_RSA);

        signature.initSign(privateKey);
        signature.update(textBytes);
        return signature.sign();
    }

    private static boolean getVerifySign(PublicKey publicKey, byte[] emailBytes, byte[] sigToVerify) throws InvalidKeyException, SignatureException, NoSuchProviderException, NoSuchAlgorithmException {
        Signature signature = Signature.getInstance(MD5_WITH_RSA);
        signature.initVerify(publicKey);

        signature.update(emailBytes);

        return signature.verify(sigToVerify);
    }
    // SunRsaSign or SunJSSE
    private static KeyPair generateKeyPair() throws NoSuchProviderException, NoSuchAlgorithmException {
        KeyPairGenerator keyPairGenerator = KeyPairGenerator.getInstance(RSA_ALG);
        SecureRandom secureRandom = new SecureRandom();

        keyPairGenerator.initialize(1024, secureRandom);
        return keyPairGenerator.generateKeyPair();
    }

    private static PrivateKey getPrivateKeyFromBytesRSA(byte[] bytes) throws InvalidKeySpecException, NoSuchAlgorithmException {
        PKCS8EncodedKeySpec privKeySpec = new PKCS8EncodedKeySpec(bytes);
        KeyFactory keyFactory = KeyFactory.getInstance("RSA");
        return keyFactory.generatePrivate(privKeySpec);
    }

    private static PublicKey getPublicKeyFromBytesRSA(byte[] encKey) throws InvalidKeySpecException, NoSuchProviderException, NoSuchAlgorithmException {
        X509EncodedKeySpec pubKeySpec = new X509EncodedKeySpec(encKey);
        KeyFactory keyFactory = KeyFactory.getInstance("RSA");
        return keyFactory.generatePublic(pubKeySpec);
    }

}
