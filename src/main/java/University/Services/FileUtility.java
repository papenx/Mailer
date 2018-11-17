package University.Services;

import University.Receivers.IMAP.Receiver;

import javax.mail.internet.MimeUtility;
import java.util.logging.Level;
import java.util.logging.Logger;

public class FileUtility {
    private static final Logger logger = Logger.getLogger(Receiver.class.getName());

    private static String decodeMailText(String text) {
        String decodeText= text;
        try {
            decodeText = MimeUtility.decodeText(text);
        } catch (Exception e) {
            logger.log(Level.INFO, e.getMessage());
        }

        return decodeText;
    }


}
