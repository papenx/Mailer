package University.Encryption;

import University.Utilities.FileKeysUtility;

import javafx.event.ActionEvent;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.security.*;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.X509EncodedKeySpec;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.mail.BodyPart;

import static University.Info.MailInfo.*;
import static University.Utilities.FileKeysUtility.*;
import static University.Utilities.FileUtility.chooseDirectory;
import static University.Utilities.FileUtility.chooseFile;

import static University.Info.MailInfo.pathToRSAPrivateKey;
import static University.Info.MailInfo.pathToRSAPublicKey;
import static University.Info.MailInfo.globalSignFolder;

public class DigitalSignatureEmail {
    private static String separator = File.separator;
    private static final Logger logger = Logger.getLogger(DigitalSignatureEmail.class.getName());

    public static String signEmailWithSaveSign(ActionEvent event, String content, String username, boolean autoGenerate)
            throws IOException, InvalidKeySpecException, NoSuchAlgorithmException, InvalidKeyException, NoSuchProviderException, SignatureException {
        if (!autoGenerate) {
            try {
                File file = chooseFile(event, "Private key RSA (*.spk)", new String[]{"*.spk"});

                byte[] privateKeyBytes = FileKeysUtility.readPrivateKeyRSA(file.getAbsolutePath());

                PrivateKey privateKey = getPrivateKeyFromBytesRSA(privateKeyBytes);

                File selectedDirectory = chooseDirectory(event, "Выберите директорию для сохраниения подписи");

                if (selectedDirectory != null) {
                    String fileName = selectedDirectory.getAbsoluteFile() + separator + username + " " + getDateToString();
                    writeSign(signEmail(content, privateKey), fileName);
                    return fileName;
                }
            } catch (IOException | NoSuchAlgorithmException | InvalidKeySpecException | NoSuchProviderException | SignatureException | InvalidKeyException | NullPointerException e) {
                logger.log(Level.INFO, e.getMessage());
            }
            return "";
        }

        File signDir = new File(globalSignFolder);
        File file = new File(pathToRSAPrivateKey);
        byte[] pkBytes = FileKeysUtility.readPrivateKeyRSA(file.getAbsolutePath());
        PrivateKey privateKey = getPrivateKeyFromBytesRSA(pkBytes);

        String fileName = signDir.getAbsoluteFile() + separator + username + " " + getDateToString();
        writeSign(signEmail(content, privateKey), fileName);
        return fileName;
    }


    public static boolean verifyEmail(ActionEvent event, String content, BodyPart bodypart) {
        try {

            File publicKeyFile;

            if (!Files.exists(Paths.get(pathToRSAPublicKey))) {
                publicKeyFile = chooseFile(event, "Public key RSA (*.spub)", new String[]{"*.spub"});
            } else {
                publicKeyFile = new File(pathToRSAPublicKey);
            }

            byte[] contentBytes = content.getBytes(StandardCharsets.UTF_8);
            byte[] publicKeyBytes = readPublicKeyRSA(publicKeyFile.getAbsolutePath());

            PublicKey publicKey = getPublicKeyFromBytesRSA(publicKeyBytes);
            byte[] verifyBytes = getSignBytes(bodypart);
            return getVerifySign(publicKey, contentBytes, verifyBytes);

        } catch (IOException | NullPointerException | NoSuchAlgorithmException | InvalidKeySpecException | NoSuchProviderException | InvalidKeyException | SignatureException e) {
            logger.log(Level.INFO, e.getMessage());
        }
        return false;
    }


    public static String generateKeysRSA(String path, String username) {
        String fullPath = "";
        try {
            KeyPair keyPair = generateKeyPair();
            PrivateKey privateKey = keyPair.getPrivate();
            PublicKey publicKey = keyPair.getPublic();

            fullPath = path + separator + username;

            String pubPath = fullPath + RSA_PUBLIC_KEY_EXT;
            String privPath = fullPath + RSA_PRIVATE_KEY_EXT;


            if (!Files.exists(Paths.get(pubPath))) {
                FileKeysUtility.writePublicKeyRSA(publicKey, pubPath);
            }
            if (!Files.exists(Paths.get(privPath))) {
                FileKeysUtility.writePrivateKeyRSA(privateKey, privPath);
            }


            return fullPath;
        } catch (NoSuchAlgorithmException | NoSuchProviderException | IOException e) {
            logger.log(Level.INFO, e.getMessage());
        }
        return fullPath;
    }

    private static byte[] signEmail(String text, PrivateKey key) throws NoSuchAlgorithmException, InvalidKeyException, SignatureException, NoSuchProviderException {
        byte[] textBytes = text.getBytes(StandardCharsets.UTF_8);
        return getSign(textBytes, key);
    }

    private static byte[] getSign(byte[] textBytes, PrivateKey privateKey) throws SignatureException, NoSuchProviderException, NoSuchAlgorithmException, InvalidKeyException {
        Signature signature = Signature.getInstance(SHA_256_WITH_RSA);

        signature.initSign(privateKey);
        signature.update(textBytes);
        return signature.sign();
    }

    private static boolean getVerifySign(PublicKey publicKey, byte[] emailBytes, byte[] sigToVerify) throws InvalidKeyException, SignatureException, NoSuchProviderException, NoSuchAlgorithmException {
        Signature signature = Signature.getInstance(SHA_256_WITH_RSA);
        signature.initVerify(publicKey);

        signature.update(emailBytes);

        return signature.verify(sigToVerify);
    }

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
