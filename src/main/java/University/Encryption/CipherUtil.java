package University.Encryption;

import javafx.event.ActionEvent;

import java.io.File;
import java.security.*;
import java.util.logging.Level;
import java.util.logging.Logger;

import static University.Utilities.FileUtility.chooseFile;
import static University.Utilities.FileKeysUtility.readFromFilePrivateKeyRSA;
import static University.Utilities.FileKeysUtility.readFromFilePublicKeyRSA;

public class CipherUtil {
    private static final Logger logger = Logger.getLogger(CipherUtil.class.getName());

    public static String encryptEmail(ActionEvent event, String text, String password) {
        try {
            File publicKeyFile = chooseFile(event, "Public key RSA (*.pub)", new String[]{"*.pub"});

            PublicKey publicKey = readFromFilePublicKeyRSA(publicKeyFile.getAbsolutePath());
            String encryptedPassword = RSA.encrypt(password, publicKey);
            System.out.println(encryptedPassword);
            return String.format("%s:%s", encryptedPassword, AES.encryptAES(text, password));
        } catch (Exception e) {
            logger.log(Level.INFO, e.getMessage());
        }
        return null;
    }

    public static String decryptEmail(ActionEvent event, String encryptedText){
        try {
            File privateKeyFile = chooseFile(event, "Private key RSA (*.pk)", new String[]{"*.pk"});

            PrivateKey privateKey = readFromFilePrivateKeyRSA(privateKeyFile.getAbsolutePath());

            int index = encryptedText.indexOf(":");
            String encryptedPassword = encryptedText.substring(0, index).replaceAll("<html dir=\"ltr\"><head></head><body contenteditable=\"true\">", "");
            String decryptedPassword = RSA.decrypt(encryptedPassword, privateKey);
            return AES.decryptAES(encryptedText.substring(index + 1).replaceAll("</body></html>", ""), decryptedPassword);
        } catch (Exception e) {
            logger.log(Level.INFO, e.getMessage());
        }
        return encryptedText;
    }
}
