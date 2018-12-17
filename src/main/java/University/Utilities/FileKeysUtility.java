package University.Utilities;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
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

    public static void writeSign(byte[] sign, String pathToFolder) throws IOException {
        FileOutputStream out = new FileOutputStream(pathToFolder + SIGNATURE_EXT);
        out.write(sign);
        out.close();
    }

    public static void writePublicKeyRSA(PublicKey publicKey, String pathToFolder) throws IOException {
        byte[] publicBytes = publicKey.getEncoded();
        FileOutputStream outPublicKey = new FileOutputStream(pathToFolder + RSA_PUBLIC_KEY_EXT);
        outPublicKey.write(publicBytes);
        outPublicKey.close();
    }

    public static void writePrivateKeyRSA(PrivateKey privateKey, String pathToFolder) throws IOException {
        byte[] privateBytes = privateKey.getEncoded();
        FileOutputStream outPrivateKey = new FileOutputStream(pathToFolder + RSA_PRIVATE_KEY_EXT);
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

    public static void writeToFileKeysRSA(String pathToFolder, PrivateKey privateKey, PublicKey  publicKey){
        String fileName = pathToFolder + "/" + getDateToString();

        try (FileOutputStream out = new FileOutputStream(fileName + PRIVATE_KEY_EXT)) {
            out.write(privateKey.getEncoded());
        } catch (IOException e) {
            logger.log(Level.INFO, e.getMessage());
        }

        try (FileOutputStream out = new FileOutputStream(fileName + PUBLIC_KEY_EXT)) {
            out.write(publicKey.getEncoded());
        } catch (IOException e) {
            logger.log(Level.INFO, e.getMessage());
        }
    }

    public static String getDateToString() {
        DateFormat df = new SimpleDateFormat("MM-dd-yyyy HH mm ss");
        Date today = Calendar.getInstance().getTime();
        return df.format(today);
    }
}