package University.Utilities;

import javax.mail.BodyPart;
import javax.mail.MessagingException;
import java.io.*;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.security.KeyFactory;
import java.security.NoSuchAlgorithmException;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.X509EncodedKeySpec;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.logging.Level;
import java.util.logging.Logger;

import static University.Info.MailInfo.*;

public class FileKeysUtility {
    private static final Logger logger = Logger.getLogger(FileKeysUtility.class.getName());
    private static String separator = File.separator;

    public static void writeSign(byte[] sign, String pathToFolder) throws IOException {
        FileOutputStream out = new FileOutputStream(pathToFolder + SIGNATURE_EXT);
        out.write(sign);
        out.close();
    }

    public static void writePublicKeyRSA(PublicKey publicKey, String keyPath) throws IOException {
        byte[] publicBytes = publicKey.getEncoded();
        FileOutputStream outPublicKey = new FileOutputStream(keyPath);
        outPublicKey.write(publicBytes);
        outPublicKey.close();
    }

    public static void writePrivateKeyRSA(PrivateKey privateKey, String keyPath) throws IOException {
        byte[] privateBytes = privateKey.getEncoded();
        FileOutputStream outPrivateKey = new FileOutputStream(keyPath);
        outPrivateKey.write(privateBytes);
        outPrivateKey.close();
    }

    public static byte[] readPrivateKeyRSA(String path) throws IOException {
        return getBytes(path);
    }

    public static byte[] readPublicKeyRSA(String path) throws IOException {
        return getBytes(path);
    }

    public static byte[] readSignature(String path) throws IOException {
        return getBytes(path);
    }

    public static byte[] getSignBytes(BodyPart bodyPart) {
        try {
            InputStream input = bodyPart.getInputStream();
            byte[] buffer = new byte[128];

            input.read(buffer);

            return buffer;
        } catch (MessagingException | IOException e) {
            logger.log(Level.INFO, e.getMessage());
        }
        return null;
    }


    private static byte[] getBytes(String path) throws IOException {
        FileInputStream keyfis = new FileInputStream(path);
        byte[] encKey = new byte[keyfis.available()];
        keyfis.read(encKey);
        keyfis.close();
        return encKey;
    }

    public static PublicKey readFromFilePublicKeyRSA(String path) throws NoSuchAlgorithmException, InvalidKeySpecException, IOException {
        byte[] bytes = Files.readAllBytes(Paths.get(path));
        X509EncodedKeySpec keySpec = new X509EncodedKeySpec(bytes);
        KeyFactory keyFactory = KeyFactory.getInstance(RSA_ALG);
        return keyFactory.generatePublic(keySpec);
    }

    public static PrivateKey readFromFilePrivateKeyRSA(String path) throws IOException, NoSuchAlgorithmException, InvalidKeySpecException {
        byte[] bytes = Files.readAllBytes(Paths.get(path));
        PKCS8EncodedKeySpec keySpec = new PKCS8EncodedKeySpec(bytes);
        KeyFactory keyFactory = KeyFactory.getInstance(RSA_ALG);
        return keyFactory.generatePrivate(keySpec);
    }

    public static void writeToFileKeysRSA(String pathToFolder, PrivateKey privateKey, PublicKey  publicKey, String user){

        pathToRSAPublicKeyCipher = pathToFolder + separator + user + PUBLIC_KEY_EXT;
        pathToRSAPrivateKeyCipher = pathToFolder + separator + user + PRIVATE_KEY_EXT;

        writeKey(pathToRSAPublicKeyCipher, publicKey, null);
        writeKey(pathToRSAPrivateKeyCipher, null, privateKey);
    }

    private static void writeKey(String path, PublicKey pub, PrivateKey pk) {
        byte[] bytes;
        if (pub == null) {
            bytes = pk.getEncoded();
        } else {
            bytes = pub.getEncoded();
        }
        if (!Files.exists(Paths.get(path))) {
            try (FileOutputStream out = new FileOutputStream(path)) {
                out.write(bytes);
            } catch (IOException e) {
                logger.log(Level.INFO, e.getMessage());
            }
        }
    }

    public static String getDateToString() {
        DateFormat df = new SimpleDateFormat("MM-dd-yyyy HH mm ss");
        Date today = Calendar.getInstance().getTime();
        return df.format(today);
    }
}